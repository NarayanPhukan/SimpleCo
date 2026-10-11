// Shiprocket API Integration Service
// Official API Docs: https://apidocs.shiprocket.in/

const https = require('https');
const dns = require('dns').promises;

const SHIPROCKET_BASE_URL = 'https://apiv2.shiprocket.in/v1/external';

let cachedToken = null;
let tokenExpiresAt = null;
let resolvedShiprocketIPs = null;

async function getShiprocketIPs() {
  if (resolvedShiprocketIPs && resolvedShiprocketIPs.length > 0) return resolvedShiprocketIPs;
  try {
    const resolver = new dns.Resolver();
    resolver.setServers(['8.8.8.8', '1.1.1.1']);
    resolvedShiprocketIPs = await resolver.resolve4('apiv2.shiprocket.in');
    return resolvedShiprocketIPs;
  } catch (e) {
    return null;
  }
}

async function requestShiprocket(endpointPath, options = {}) {
  // Try standard fetch first
  try {
    const res = await fetch(`${SHIPROCKET_BASE_URL}${endpointPath}`, options);
    const data = await res.json();
    return { ok: res.ok, status: res.status, data };
  } catch (err) {
    // Fallback to direct HTTPS request using public DNS resolvers if local DNS fails
    try {
      const ips = await getShiprocketIPs();
      if (!ips || ips.length === 0) throw err;

      return await new Promise((resolve, reject) => {
        const bodyStr = options.body || '';
        const req = https.request({
          hostname: 'apiv2.shiprocket.in',
          path: `/v1/external${endpointPath}`,
          method: options.method || 'GET',
          headers: {
            ...options.headers,
            ...(bodyStr ? { 'Content-Length': Buffer.byteLength(bodyStr) } : {})
          },
          lookup: (hostname, lookupOpts, callback) => {
            if (typeof lookupOpts === 'function') {
              callback = lookupOpts;
              lookupOpts = {};
            }
            if (lookupOpts && lookupOpts.all) {
              callback(null, ips.map(ip => ({ address: ip, family: 4 })));
            } else {
              callback(null, ips[0], 4);
            }
          }
        }, (res) => {
          let dataStr = '';
          res.on('data', chunk => dataStr += chunk);
          res.on('end', () => {
            try {
              const parsed = JSON.parse(dataStr);
              resolve({ ok: res.statusCode >= 200 && res.statusCode < 300, status: res.statusCode, data: parsed });
            } catch (e) {
              resolve({ ok: false, status: res.statusCode, data: { message: dataStr } });
            }
          });
        });

        req.on('error', reject);
        if (bodyStr) req.write(bodyStr);
        req.end();
      });
    } catch (fallbackErr) {
      console.warn('[Shiprocket] Request error:', err.message);
      return { ok: false, status: 500, error: err.message, data: { message: err.message } };
    }
  }
}

class ShiprocketService {
  constructor() {
    this.email = process.env.SHIPROCKET_EMAIL;
    this.password = process.env.SHIPROCKET_PASSWORD;
  }

  // Reload credentials from environment dynamically
  reloadCredentials() {
    this.email = process.env.SHIPROCKET_EMAIL;
    this.password = process.env.SHIPROCKET_PASSWORD;
  }

  // Obtain or return cached JWT Bearer Token (tokens are valid for 10 days / 240 hours)
  async getToken() {
    this.reloadCredentials();

    if (!this.email || !this.password) {
      return null;
    }

    // Return cached token if valid (buffer by 1 hour)
    if (cachedToken && tokenExpiresAt && Date.now() < tokenExpiresAt - 3600000) {
      return cachedToken;
    }

    try {
      const res = await requestShiprocket('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: this.email.trim(),
          password: this.password.trim()
        })
      });

      const data = res.data;

      if (!res.ok || !data.token) {
        console.warn('[Shiprocket] Auth warning:', data.message || 'Login failed');
        return null;
      }

      cachedToken = data.token;
      // Valid for 10 days (240 hours)
      tokenExpiresAt = Date.now() + (240 * 3600 * 1000);
      console.log('[Shiprocket] API authenticated successfully. Token cached for 10 days.');
      return cachedToken;
    } catch (err) {
      console.warn('[Shiprocket] Connection error:', err.message);
      return null;
    }
  }

  // Check courier serviceability for Indian PIN codes
  async checkServiceability(pickupPostcode = '400050', deliveryPostcode = '560038', weightKg = 0.5, cod = 0) {
    const token = await this.getToken();
    if (!token) {
      return {
        isAvailable: true,
        isSimulated: true,
        estimatedDays: 3,
        couriers: [
          { name: 'Priority Express Delivery', rate: 0, etd: '2-3 Days', rating: 4.8 },
          { name: 'Standard Surface Delivery', rate: 0, etd: '3-4 Days', rating: 4.6 }
        ]
      };
    }

    try {
      const res = await requestShiprocket(`/courier/serviceability?pickup_postcode=${pickupPostcode}&delivery_postcode=${deliveryPostcode}&weight=${weightKg}&cod=${cod}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      return {
        isAvailable: res.status === 200,
        data: res.data?.data || res.data
      };
    } catch (err) {
      return { isAvailable: false, error: err.message };
    }
  }

  // Track live package by AWB Number
  async trackByAWB(awbNumber) {
    const token = await this.getToken();
    if (!token) {
      return null;
    }

    try {
      const res = await requestShiprocket(`/courier/track/awb/${encodeURIComponent(awbNumber)}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      return res.data;
    } catch (err) {
      console.warn('Shiprocket tracking error:', err.message);
      return null;
    }
  }

  // Track by Order ID or Shipment ID
  async trackByOrderId(orderId) {
    const token = await this.getToken();
    if (!token) {
      return null;
    }

    try {
      const res = await requestShiprocket(`/courier/track?order_id=${encodeURIComponent(orderId)}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      return res.data;
    } catch (err) {
      return null;
    }
  }

  // Create an order in Shiprocket
  async createShipmentOrder(orderDoc) {
    const token = await this.getToken();
    if (!token) {
      return { simulated: true, message: 'Shiprocket offline fallback: local waybill generated' };
    }

    try {
      const payload = {
        order_id: orderDoc.orderId,
        order_date: new Date().toISOString().split('T')[0],
        pickup_location: 'Primary Studio',
        channel_id: '',
        comment: 'BabyyGurll Handcrafted Crochet',
        billing_customer_name: orderDoc.shippingAddress.recipientName.split(' ')[0],
        billing_last_name: orderDoc.shippingAddress.recipientName.split(' ').slice(1).join(' ') || 'Customer',
        billing_address: orderDoc.shippingAddress.street,
        billing_city: orderDoc.shippingAddress.city,
        billing_pincode: orderDoc.shippingAddress.zip,
        billing_state: orderDoc.shippingAddress.state,
        billing_country: 'India',
        billing_email: orderDoc.userEmail,
        billing_phone: orderDoc.shippingAddress.phone.replace(/[^0-9]/g, '').slice(-10),
        shipping_is_billing: true,
        order_items: orderDoc.items.map(it => ({
          name: it.name,
          sku: it.id,
          units: it.quantity,
          selling_price: it.price,
          discount: 0,
          tax: 0,
          hsn: 65050090
        })),
        payment_method: orderDoc.paymentMethod.toLowerCase().includes('cod') ? 'COD' : 'Prepaid',
        shipping_charges: 0,
        giftwrap_charges: 0,
        transaction_charges: 0,
        total_discount: orderDoc.discount || 0,
        sub_total: orderDoc.total,
        length: 15,
        breadth: 15,
        height: 10,
        weight: 0.4
      };

      const res = await requestShiprocket('/orders/create/adhoc', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      return res.data;
    } catch (err) {
      console.warn('Shiprocket order creation error:', err.message);
      return { error: err.message };
    }
  }
}

module.exports = new ShiprocketService();
