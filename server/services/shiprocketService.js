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
    const targetPin = deliveryPostcode || '400050';
    const token = await this.getToken();
    if (!token) {
      return {
        success: true,
        isAvailable: true,
        isSimulated: true,
        postcode: targetPin,
        city: 'All-India Coverage',
        state: 'Express Transit',
        estimatedDays: '3-4 Business Days',
        etd: '3-4 Days',
        codAvailable: true,
        prepaidAvailable: true,
        courier: 'Express Surface & Air Network',
        rate: 79,
        couriers: [
          { name: 'Priority Air Express', rate: 149, etd: '2-3 Days', days: '2-3', cod: true, rating: 4.9 },
          { name: 'Standard Surface Delivery', rate: 79, etd: '3-5 Days', days: '3-5', cod: true, rating: 4.7 }
        ]
      };
    }

    try {
      const res = await requestShiprocket(`/courier/serviceability?pickup_postcode=${pickupPostcode}&delivery_postcode=${targetPin}&weight=${weightKg}&cod=${cod}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      const rawData = res.data?.data || res.data;
      const courierList = rawData?.available_courier_companies || [];

      if (res.status === 200 && courierList.length > 0) {
        const topCourier = courierList[0];
        const hasCod = courierList.some(c => c.cod === 1);
        
        return {
          success: true,
          isAvailable: true,
          isLive: true,
          postcode: targetPin,
          city: topCourier.city || 'Hub Location',
          state: topCourier.state || 'India',
          estimatedDays: `${topCourier.estimated_delivery_days || 3} Days`,
          etd: topCourier.etd || '3-4 Days',
          codAvailable: hasCod,
          prepaidAvailable: true,
          courier: topCourier.courier_name,
          rate: topCourier.rate,
          totalCouriersCount: courierList.length,
          couriers: courierList.slice(0, 8).map(c => ({
            name: c.courier_name,
            mode: c.courier_name.toLowerCase().includes('air') ? 'Air Express' : 'Surface Ground',
            rate: Math.round(c.rate),
            etd: c.etd,
            days: c.estimated_delivery_days,
            cod: c.cod === 1,
            rating: c.rating || 4.5
          }))
        };
      }

      return {
        success: false,
        isAvailable: false,
        postcode: targetPin,
        message: rawData?.message || 'Delivery currently unavailable for this PIN code.'
      };
    } catch (err) {
      return { 
        success: false, 
        isAvailable: false, 
        postcode: targetPin, 
        error: err.message 
      };
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

  // Get active pickup location nickname from Shiprocket account
  async getPrimaryPickupLocation() {
    const token = await this.getToken();
    if (!token) return 'Primary';
    try {
      const res = await requestShiprocket('/settings/company/pickup', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const addrs = res.data?.data?.shipping_address || [];
      const primary = addrs.find(a => a.is_primary_location === 1) || addrs[0];
      return primary ? primary.pickup_location : 'Primary';
    } catch (e) {
      return 'Primary';
    }
  }

  // Create an order in Shiprocket
  async createShipmentOrder(orderDoc) {
    const token = await this.getToken();
    if (!token) {
      return { simulated: true, message: 'Shiprocket offline fallback: local waybill generated' };
    }

    try {
      const pickupLocation = await this.getPrimaryPickupLocation();
      const rawName = orderDoc.shippingAddress?.recipientName || 'Customer';
      const nameParts = rawName.trim().split(/\s+/);
      const firstName = nameParts[0] || 'Customer';
      const lastName = nameParts.slice(1).join(' ') || '.';
      const rawPhone = String(orderDoc.shippingAddress?.phone || '').replace(/[^0-9]/g, '');
      const cleanPhone = rawPhone.length >= 10 ? rawPhone.slice(-10) : '9876543210';
      const now = new Date();
      const orderDateFormatted = now.toISOString().replace('T', ' ').slice(0, 16);

      const itemsList = (orderDoc.items && orderDoc.items.length > 0)
        ? orderDoc.items
        : [{ name: 'Handcrafted Crochet Piece', price: orderDoc.total || 499, quantity: 1 }];

      const isCod = String(orderDoc.paymentMethod || '').toLowerCase().includes('cod');

      const payload = {
        order_id: String(orderDoc.orderId),
        order_date: orderDateFormatted,
        pickup_location: pickupLocation,
        channel_id: '',
        comment: 'BabyyGurll Artisanal Handcrafted Crochet',
        billing_customer_name: firstName,
        billing_last_name: lastName,
        billing_address: (orderDoc.shippingAddress?.street || 'Delivery Address').trim(),
        billing_city: (orderDoc.shippingAddress?.city || 'Sibsagar').trim(),
        billing_pincode: String(orderDoc.shippingAddress?.zip || '785670').trim(),
        billing_state: (orderDoc.shippingAddress?.state || 'Assam').trim(),
        billing_country: 'India',
        billing_email: orderDoc.userEmail || 'hello@babyygurll.in',
        billing_phone: cleanPhone,
        shipping_is_billing: true,
        order_items: itemsList.map((it, idx) => ({
          name: (it.name || 'Crochet Item').substring(0, 50),
          sku: String(it.id || it._id || `BG-SKU-${idx + 1}`).replace(/[^a-zA-Z0-9_-]/g, '') || `BG-SKU-${idx + 1}`,
          units: Number(it.quantity || 1),
          selling_price: Math.max(1, Number(it.price || 1)),
          discount: 0,
          tax: 0,
          hsn: 65050090
        })),
        payment_method: isCod ? 'COD' : 'Prepaid',
        shipping_charges: Number(orderDoc.shippingCost || 0),
        giftwrap_charges: 0,
        transaction_charges: 0,
        total_discount: Number(orderDoc.discount || 0),
        sub_total: Math.max(1, Number(orderDoc.total || 1)),
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

      console.log(`[Shiprocket] Auto-created adhoc order #${orderDoc.orderId}: status ${res.status}`, res.data?.order_id ? `SUCCESS (SR #${res.data.order_id}, Shipment #${res.data.shipment_id})` : (res.data?.message || JSON.stringify(res.data)));
      return res.data;
    } catch (err) {
      console.warn('Shiprocket order creation error:', err.message);
      return { error: err.message };
    }
  }
}

module.exports = new ShiprocketService();
