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

  async createProduct(productData) {
    const res = await fetch(`${API_BASE}/products`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(productData)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to create product');
    }
    return res.json();
  },

  async updateProduct(id, productData) {
    const res = await fetch(`${API_BASE}/products/${encodeURIComponent(id)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(productData)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to update product');
    }
    return res.json();
  },

  async deleteProduct(id) {
    const res = await fetch(`${API_BASE}/products/${encodeURIComponent(id)}`, {
      method: 'DELETE'
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to delete product');
    }
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

  async forgotPassword(email, newPassword) {
    const res = await fetch(`${API_BASE}/auth/forgot-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, newPassword })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Password reset failed');
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
    const url = email ? `${API_BASE}/orders?email=${encodeURIComponent(email)}` : `${API_BASE}/orders?all=true`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch orders');
    return res.json();
  },

  async getAllOrders() {
    const res = await fetch(`${API_BASE}/orders?all=true`);
    if (!res.ok) throw new Error('Failed to fetch all orders');
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

  async updateOrderStatus(orderId, statusData) {
    const res = await fetch(`${API_BASE}/orders/${encodeURIComponent(orderId)}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(statusData)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to update order status');
    }
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

  async cancelOrder(orderId, reason) {
    const res = await fetch(`${API_BASE}/orders/${encodeURIComponent(orderId)}/cancel`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reason })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to cancel order');
    }
    return res.json();
  },

  async deleteOrder(orderId) {
    const res = await fetch(`${API_BASE}/orders/${encodeURIComponent(orderId)}`, {
      method: 'DELETE'
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to delete order');
    }
    return res.json();
  },

  async syncShiprocket(orderId) {
    const res = await fetch(`${API_BASE}/orders/${encodeURIComponent(orderId)}/sync-shiprocket`, {
      method: 'POST'
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      throw new Error(data.message || 'Failed to sync with Shiprocket');
    }
    return data;
  },

  // Dynamic Coupons Management
  async getCoupons() {
    const res = await fetch(`${API_BASE}/coupons`);
    if (!res.ok) throw new Error('Failed to fetch coupons');
    return res.json();
  },

  async validateCoupon(code, cartSubtotal = 0) {
    const res = await fetch(`${API_BASE}/coupons/validate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, cartSubtotal })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Invalid coupon');
    }
    return res.json();
  },

  async createCoupon(couponData) {
    const res = await fetch(`${API_BASE}/coupons`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(couponData)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to create coupon');
    }
    return res.json();
  },

  async updateCoupon(id, couponData) {
    const res = await fetch(`${API_BASE}/coupons/${encodeURIComponent(id)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(couponData)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to update coupon');
    }
    return res.json();
  },

  async deleteCoupon(id) {
    const res = await fetch(`${API_BASE}/coupons/${encodeURIComponent(id)}`, {
      method: 'DELETE'
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to delete coupon');
    }
    return res.json();
  },

  // Store Settings & Admin Auth
  async getSettings() {
    const res = await fetch(`${API_BASE}/settings`);
    if (!res.ok) throw new Error('Failed to fetch store settings');
    return res.json();
  },

  async updateSettings(settingsData) {
    const res = await fetch(`${API_BASE}/settings`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(settingsData)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to update store settings');
    }
    return res.json();
  },

  async verifyAdminPassword(password) {
    const res = await fetch(`${API_BASE}/settings/verify-admin`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ password })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Invalid administrator password');
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

