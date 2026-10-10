// API Client connecting React Frontend to Express & MongoDB Backend
const API_BASE = '/api';

export const api = {
  // Products
  async getProducts(params = {}) {
    const query = new URLSearchParams();
    if (params.category && params.category !== 'all') query.append('category', params.category);
    if (params.search) query.append('search', params.search);
    if (params.sort) query.append('sort', params.sort);
    
    const res = await fetch(`${API_BASE}/products?${query.toString()}`);
    if (!res.ok) throw new Error('Failed to fetch products');
    return res.json();
  },

  async getProductById(id) {
    const res = await fetch(`${API_BASE}/products/${id}`);
    if (!res.ok) throw new Error('Failed to fetch product details');
    return res.json();
  },

  // Auth & Addresses
  async login(email, password) {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Login failed');
    }
    return res.json();
  },

  async register(userData) {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Registration failed');
    }
    return res.json();
  },

  async getMe(email) {
    const res = await fetch(`${API_BASE}/auth/me?email=${encodeURIComponent(email)}`);
    if (!res.ok) throw new Error('Failed to fetch user profile');
    return res.json();
  },

  async addAddress(email, address) {
    const res = await fetch(`${API_BASE}/auth/addresses`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, address })
    });
    if (!res.ok) throw new Error('Failed to add address');
    return res.json();
  },

  async updateAddress(email, addressId, address) {
    const res = await fetch(`${API_BASE}/auth/addresses/${addressId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, address })
    });
    if (!res.ok) throw new Error('Failed to update address');
    return res.json();
  },

  async deleteAddress(email, addressId) {
    const res = await fetch(`${API_BASE}/auth/addresses/${addressId}?email=${encodeURIComponent(email)}`, {
      method: 'DELETE'
    });
    if (!res.ok) throw new Error('Failed to delete address');
    return res.json();
  },

  async setDefaultAddress(email, addressId) {
    const res = await fetch(`${API_BASE}/auth/addresses/${addressId}/default`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email })
    });
    if (!res.ok) throw new Error('Failed to set default address');
    return res.json();
  },

  // Orders & Live Tracking
  async createOrder(orderData) {
    const res = await fetch(`${API_BASE}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(orderData)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to place order');
    }
    return res.json();
  },

  async getOrders(email) {
    const res = await fetch(`${API_BASE}/orders?email=${encodeURIComponent(email)}`);
    if (!res.ok) throw new Error('Failed to fetch orders');
    return res.json();
  },

  async getOrderById(orderId) {
    const res = await fetch(`${API_BASE}/orders/${encodeURIComponent(orderId)}`);
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Order not found');
    }
    return res.json();
  },

  async advanceOrderStage(orderId) {
    const res = await fetch(`${API_BASE}/orders/${encodeURIComponent(orderId)}/advance`, {
      method: 'PATCH'
    });
    if (!res.ok) throw new Error('Failed to advance order stage');
    return res.json();
  },

  async requestReturn(orderId, reason) {
    const res = await fetch(`${API_BASE}/orders/${encodeURIComponent(orderId)}/return`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reason })
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to submit return request');
    }
    return res.json();
  },

  // Shiprocket Shipping Client API
  async getShippingStatus() {
    const res = await fetch(`${API_BASE}/shipping/status`);
    if (!res.ok) return { connected: false };
    return res.json();
  },

  async checkPincodeServiceability(deliveryPostcode) {
    const res = await fetch(`${API_BASE}/shipping/serviceability`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ deliveryPostcode })
    });
    if (!res.ok) throw new Error('Serviceability check failed');
    return res.json();
  },

  async trackShiprocketAWB(awb) {
    const res = await fetch(`${API_BASE}/shipping/track/${encodeURIComponent(awb)}`);
    if (!res.ok) throw new Error('AWB tracking lookup failed');
    return res.json();
  },

  // Razorpay Standard Web Checkout API
  async createRazorpayOrder(payload) {
    const res = await fetch(`${API_BASE}/create-order`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to initialize Razorpay checkout order');
    }
    return res.json();
  },

  async verifyRazorpayPayment(payload) {
    const res = await fetch(`${API_BASE}/verify-payment`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Payment signature verification failed');
    }
    return res.json();
  },

  async getRazorpayKey() {
    try {
      const res = await fetch(`${API_BASE}/razorpay-key`);
      if (res.ok) {
        const data = await res.json();
        return data.key_id;
      }
    } catch (e) {}
    return import.meta.env.VITE_RAZORPAY_KEY_ID || '';
  }
};

