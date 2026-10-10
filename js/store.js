// BabyyGurll Crochet - Centralized Storage and State Management
// Handles LocalStorage persistence for Cart, Auth, Saved Addresses, Orders & Order Tracking

const STORAGE_KEYS = {
  USER: "bgc_user_v1",
  CART: "bgc_cart_v1",
  COUPON: "bgc_coupon_v1",
  WISHLIST: "bgc_wishlist_v1",
  ORDERS: "bgc_orders_v1",
  CURRENCY: "bgc_currency_v1"
};

const CURRENCY_RATES = {
  USD: { symbol: "$", rate: 1.00, label: "USD ($)" },
  EUR: { symbol: "€", rate: 0.92, label: "EUR (€)" },
  GBP: { symbol: "£", rate: 0.79, label: "GBP (£)" },
  INR: { symbol: "₹", rate: 83.50, label: "INR (₹)" }
};

// Initial Seed Data
const DEFAULT_USER = null;

const DEFAULT_ORDERS = [
  {
    orderId: "BGC-89241",
    createdAt: "2026-10-08T09:30:00Z",
    dateFormatted: "Oct 8, 2026",
    statusStage: 3, // 0: received, 1: crafted, 2: packed, 3: shipped, 4: delivered
    statusLabel: "In Transit with Courier",
    statusCode: "shipped",
    carrier: "CraftExpress Priority Courier",
    trackingNumber: "TRK-BGC-89241-US",
    estimatedDelivery: "Oct 12, 2026",
    shippingAddress: {
      recipientName: "Chloe Bennett",
      phone: "+1 (555) 234-5678",
      street: "742 Evergreen Blossom Way, Apt 4B",
      city: "Portland",
      state: "OR",
      zip: "97201",
      country: "United States"
    },
    paymentMethod: "Visa ending in •••• 4242",
    subtotal: 34.99,
    discount: 5.25,
    shippingCost: 0.00,
    tax: 2.38,
    total: 32.12,
    items: [
      {
        id: "bgc-cow-01",
        name: "Strawberry Cow Amigurumi Plush",
        color: "Strawberry Pink",
        quantity: 1,
        price: 34.99,
        image: "assets/images/strawberry-cow.jpg"
      }
    ],
    timeline: [
      {
        stage: "received",
        title: "Order Placed & Yarn Selected",
        time: "Oct 8, 09:30 AM",
        completed: true,
        current: false,
        desc: "Order confirmed! Artisanal milk cotton yarn skeins assigned to master crochet hook."
      },
      {
        stage: "crafted",
        title: "Handmade Stitched with Love",
        time: "Oct 9, 02:15 PM",
        completed: true,
        current: false,
        desc: "Over 850 stitches completed by hand. Strawberry leaf horns attached and secured."
      },
      {
        stage: "packed",
        title: "Ribbon Wrapped & Sweet Scented",
        time: "Oct 9, 06:40 PM",
        completed: true,
        current: false,
        desc: "Inspected for stitch perfection, packed in eco-friendly cotton wrap with handwritten note."
      },
      {
        stage: "shipped",
        title: "Departed Studio & In Transit",
        time: "Oct 10, 08:20 AM",
        completed: true,
        current: true,
        desc: "Carrier scan: CraftExpress Portland sorting center. Package en route to destination."
      },
      {
        stage: "delivered",
        title: "Delivered to Doorstep",
        time: "Est. Oct 12, By 5:00 PM",
        completed: false,
        current: false,
        desc: "Artisan package will be gently delivered to your front porch or parcel locker."
      }
    ]
  },
  {
    orderId: "BGC-91402",
    createdAt: "2026-10-10T11:00:00Z",
    dateFormatted: "Oct 10, 2026",
    statusStage: 1, // crafting stage
    statusLabel: "Artisan Handcrafting in Progress",
    statusCode: "crafting",
    carrier: "Pending Dispatch",
    trackingNumber: "TRK-BGC-91402-EXP",
    estimatedDelivery: "Oct 16, 2026",
    shippingAddress: {
      recipientName: "Chloe Bennett",
      phone: "+1 (555) 234-5678",
      street: "742 Evergreen Blossom Way, Apt 4B",
      city: "Portland",
      state: "OR",
      zip: "97201",
      country: "United States"
    },
    paymentMethod: "Apple Pay (Card •••• 8821)",
    subtotal: 42.00,
    discount: 0.00,
    shippingCost: 0.00,
    tax: 3.36,
    total: 45.36,
    items: [
      {
        id: "bgc-tulip-02",
        name: "Everlasting Pastel Tulip Bouquet",
        color: "Blush Harmony",
        quantity: 1,
        price: 42.00,
        image: "assets/images/tulip-bouquet.jpg"
      }
    ],
    timeline: [
      {
        stage: "received",
        title: "Order Placed & Yarn Selected",
        time: "Oct 10, 11:00 AM",
        completed: true,
        current: false,
        desc: "Artisan ticket issued. Soft blush and sage cotton skeins wound for stems and petals."
      },
      {
        stage: "crafted",
        title: "Handmade Stitched with Love",
        time: "Oct 10, 03:30 PM",
        completed: true,
        current: true,
        desc: "Currently crocheting the 9 flower heads with flexible wire floral stems."
      },
      {
        stage: "packed",
        title: "Ribbon Wrapped & Sweet Scented",
        time: "Scheduled Oct 11",
        completed: false,
        current: false,
        desc: "Will be wrapped in aesthetic matte Korean floral paper with pink satin ribbon."
      },
      {
        stage: "shipped",
        title: "Departed Studio & In Transit",
        time: "Est. Oct 12",
        completed: false,
        current: false,
        desc: "Carrier pickup scheduled."
      },
      {
        stage: "delivered",
        title: "Delivered to Doorstep",
        time: "Est. Oct 16",
        completed: false,
        current: false,
        desc: "Expected arrival at recipient address."
      }
    ]
  }
];

class Store {
  constructor() {
    this.user = this.loadUser();
    this.cart = this.loadCart();
    this.coupon = this.loadCoupon();
    this.wishlist = this.loadWishlist();
    this.orders = this.loadOrders();
    this.currency = this.loadCurrency();
  }

  // Currency
  loadCurrency() {
    return localStorage.getItem(STORAGE_KEYS.CURRENCY) || "USD";
  }

  setCurrency(curr) {
    if (CURRENCY_RATES[curr]) {
      this.currency = curr;
      localStorage.setItem(STORAGE_KEYS.CURRENCY, curr);
    }
  }

  formatPrice(amountInUSD) {
    const info = CURRENCY_RATES[this.currency] || CURRENCY_RATES.USD;
    const converted = amountInUSD * info.rate;
    if (this.currency === "INR") {
      return `${info.symbol}${Math.round(converted).toLocaleString()}`;
    }
    return `${info.symbol}${converted.toFixed(2)}`;
  }

  // User Authentication & Profile
  loadUser() {
    const raw = localStorage.getItem(STORAGE_KEYS.USER);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw);
    } catch (e) {
      return null;
    }
  }

  saveUser() {
    if (this.user) {
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(this.user));
    } else {
      localStorage.removeItem(STORAGE_KEYS.USER);
    }
  }

  getUser() {
    return this.user;
  }

  login(email, password, remember = true) {
    if (!this.user || this.user.email !== email) {
      this.user = {
        isLoggedIn: true,
        id: "usr_" + Date.now(),
        name: email.split("@")[0].replace(/[._]/g, " ").replace(/\b\w/g, c => c.toUpperCase()),
        email: email,
        phone: "+91 98765 00000",
        memberTier: "VIP Stitcher Club 🧶",
        memberSince: "October 2026",
        savedAddresses: []
      };
    } else {
      this.user.isLoggedIn = true;
    }
    this.saveUser();
    return this.user;
  }

  register({ name, email, phone, password }) {
    this.user = {
      isLoggedIn: true,
      id: "usr_" + Date.now(),
      name: name || "Crochet Cutie",
      email: email,
      phone: phone || "+1 (555) 000-0000",
      memberTier: "VIP Stitcher Club 🧶",
      memberSince: "October 2026",
      savedAddresses: [
        {
          id: "addr_" + Date.now(),
          label: "Primary Home",
          recipientName: name,
          phone: phone,
          street: "100 Cozy Lane",
          city: "Sunnyvale",
          state: "CA",
          zip: "94086",
          country: "United States",
          isDefault: true
        }
      ]
    };
    this.saveUser();
    return this.user;
  }

  updateProfile({ name, email, phone }) {
    if (!this.user) return;
    this.user.name = name;
    this.user.email = email;
    this.user.phone = phone;
    this.saveUser();
    return this.user;
  }

  logout() {
    if (this.user) {
      this.user.isLoggedIn = false;
      this.saveUser();
    }
  }

  // Address Management
  getAddresses() {
    return (this.user && this.user.savedAddresses) ? this.user.savedAddresses : [];
  }

  getDefaultAddress() {
    const list = this.getAddresses();
    return list.find(a => a.isDefault) || list[0] || null;
  }

  addAddress(addressData) {
    if (!this.user.savedAddresses) this.user.savedAddresses = [];
    const newId = "addr_" + Date.now();
    const isFirst = this.user.savedAddresses.length === 0;

    const newAddr = {
      id: newId,
      label: addressData.label || "Home",
      recipientName: addressData.recipientName || this.user.name,
      phone: addressData.phone || this.user.phone,
      street: addressData.street,
      city: addressData.city,
      state: addressData.state,
      zip: addressData.zip,
      country: addressData.country || "United States",
      isDefault: isFirst || Boolean(addressData.isDefault)
    };

    if (newAddr.isDefault) {
      this.user.savedAddresses.forEach(a => { a.isDefault = false; });
    }

    this.user.savedAddresses.push(newAddr);
    this.saveUser();
    return newAddr;
  }

  updateAddress(id, addressData) {
    if (!this.user || !this.user.savedAddresses) return;
    const index = this.user.savedAddresses.findIndex(a => a.id === id);
    if (index === -1) return;

    if (addressData.isDefault) {
      this.user.savedAddresses.forEach(a => { a.isDefault = false; });
    }

    this.user.savedAddresses[index] = {
      ...this.user.savedAddresses[index],
      ...addressData
    };
    this.saveUser();
  }

  deleteAddress(id) {
    if (!this.user || !this.user.savedAddresses) return;
    const wasDefault = this.user.savedAddresses.find(a => a.id === id)?.isDefault;
    this.user.savedAddresses = this.user.savedAddresses.filter(a => a.id !== id);

    if (wasDefault && this.user.savedAddresses.length > 0) {
      this.user.savedAddresses[0].isDefault = true;
    }
    this.saveUser();
  }

  setDefaultAddress(id) {
    if (!this.user || !this.user.savedAddresses) return;
    this.user.savedAddresses.forEach(a => {
      a.isDefault = (a.id === id);
    });
    this.saveUser();
  }

  // Cart
  loadCart() {
    const raw = localStorage.getItem(STORAGE_KEYS.CART);
    if (!raw) {
      // Seed default cart item so cart isn't empty initially!
      const initialCart = [
        {
          productId: "bgc-cow-01",
          color: "Strawberry Pink",
          quantity: 1,
          customNote: "Extra cute please! 🍓"
        }
      ];
      localStorage.setItem(STORAGE_KEYS.CART, JSON.stringify(initialCart));
      return initialCart;
    }
    try {
      return JSON.parse(raw);
    } catch (e) {
      return [];
    }
  }

  saveCart() {
    localStorage.setItem(STORAGE_KEYS.CART, JSON.stringify(this.cart));
  }

  getCart() {
    return this.cart;
  }

  getCartItemCount() {
    return this.cart.reduce((total, item) => total + item.quantity, 0);
  }

  addToCart(productId, color, quantity = 1, customNote = "") {
    const existingIndex = this.cart.findIndex(
      item => item.productId === productId && item.color === color
    );
    if (existingIndex > -1) {
      this.cart[existingIndex].quantity += quantity;
      if (customNote) this.cart[existingIndex].customNote = customNote;
    } else {
      this.cart.push({
        productId,
        color,
        quantity,
        customNote
      });
    }
    this.saveCart();
  }

  updateQuantity(index, quantity) {
    if (quantity <= 0) {
      this.removeFromCart(index);
    } else {
      this.cart[index].quantity = quantity;
      this.saveCart();
    }
  }

  removeFromCart(index) {
    this.cart.splice(index, 1);
    this.saveCart();
  }

  clearCart() {
    this.cart = [];
    this.saveCart();
  }

  // Coupon
  loadCoupon() {
    const raw = localStorage.getItem(STORAGE_KEYS.COUPON);
    if (!raw) return null;
    try { return JSON.parse(raw); } catch (e) { return null; }
  }

  applyCoupon(code) {
    const upper = code.trim().toUpperCase();
    if (upper === "BABYYGURLL") {
      this.coupon = { code: upper, discountPercent: 15, label: "15% Off VIP Welcome" };
    } else if (upper === "CUTIE10") {
      this.coupon = { code: upper, discountFlat: 10, label: "$10.00 Off Order" };
    } else if (upper === "FREESHIP") {
      this.coupon = { code: upper, freeShipping: true, label: "Free Express Shipping" };
    } else {
      return { success: false, message: "Invalid coupon code! Try BABYYGURLL or CUTIE10" };
    }
    localStorage.setItem(STORAGE_KEYS.COUPON, JSON.stringify(this.coupon));
    return { success: true, coupon: this.coupon };
  }

  removeCoupon() {
    this.coupon = null;
    localStorage.removeItem(STORAGE_KEYS.COUPON);
  }

  getCartTotals() {
    let subtotal = 0;
    this.cart.forEach(item => {
      const prod = PRODUCTS_DATA.find(p => p.id === item.productId);
      if (prod) {
        subtotal += prod.price * item.quantity;
      }
    });

    let discount = 0;
    let shipping = subtotal >= 45 ? 0 : (subtotal > 0 ? 4.99 : 0);

    if (this.coupon) {
      if (this.coupon.discountPercent) {
        discount = subtotal * (this.coupon.discountPercent / 100);
      } else if (this.coupon.discountFlat) {
        discount = Math.min(subtotal, this.coupon.discountFlat);
      }
      if (this.coupon.freeShipping) {
        shipping = 0;
      }
    }

    const discountedSubtotal = Math.max(0, subtotal - discount);
    const tax = subtotal > 0 ? discountedSubtotal * 0.07 : 0; // 7% tax
    const total = discountedSubtotal + shipping + tax;

    return {
      subtotal,
      discount,
      shipping,
      tax,
      total,
      freeShippingThreshold: 45,
      remainingForFreeShipping: Math.max(0, 45 - subtotal)
    };
  }

  // Wishlist
  loadWishlist() {
    const raw = localStorage.getItem(STORAGE_KEYS.WISHLIST);
    if (!raw) return ["bgc-cow-01", "bgc-tulip-02"];
    try { return JSON.parse(raw); } catch (e) { return []; }
  }

  toggleWishlist(productId) {
    const idx = this.wishlist.indexOf(productId);
    let added = false;
    if (idx > -1) {
      this.wishlist.splice(idx, 1);
      added = false;
    } else {
      this.wishlist.push(productId);
      added = true;
    }
    localStorage.setItem(STORAGE_KEYS.WISHLIST, JSON.stringify(this.wishlist));
    return added;
  }

  isInWishlist(productId) {
    return this.wishlist.includes(productId);
  }

  // Orders and Tracking System
  loadOrders() {
    const raw = localStorage.getItem(STORAGE_KEYS.ORDERS);
    if (!raw) {
      localStorage.setItem(STORAGE_KEYS.ORDERS, JSON.stringify(DEFAULT_ORDERS));
      return [...DEFAULT_ORDERS];
    }
    try {
      return JSON.parse(raw);
    } catch (e) {
      return [...DEFAULT_ORDERS];
    }
  }

  saveOrders() {
    localStorage.setItem(STORAGE_KEYS.ORDERS, JSON.stringify(this.orders));
  }

  getOrders() {
    return this.orders;
  }

  getOrderById(orderId) {
    if (!orderId) return null;
    const cleanId = orderId.trim().toUpperCase();
    return this.orders.find(o => o.orderId.toUpperCase() === cleanId) || null;
  }

  createOrder({ shippingAddress, paymentMethod, items, totals }) {
    const newOrderId = "BGC-" + Math.floor(10000 + Math.random() * 90000);
    const trackingNumber = "TRK-" + newOrderId + "-US";
    const now = new Date();
    const dateFormatted = now.toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
    const estDate = new Date(now.getTime() + 4 * 24 * 60 * 60 * 1000).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });

    const newOrder = {
      orderId: newOrderId,
      createdAt: now.toISOString(),
      dateFormatted: dateFormatted,
      statusStage: 0, // Just received
      statusLabel: "Order Placed & Spun",
      statusCode: "received",
      carrier: "CraftExpress Priority Courier",
      trackingNumber: trackingNumber,
      estimatedDelivery: estDate,
      shippingAddress: shippingAddress,
      paymentMethod: paymentMethod,
      subtotal: totals.subtotal,
      discount: totals.discount,
      shippingCost: totals.shipping,
      tax: totals.tax,
      total: totals.total,
      items: items.map(cartItem => {
        const prod = PRODUCTS_DATA.find(p => p.id === cartItem.productId);
        return {
          id: cartItem.productId,
          name: prod ? prod.name : "Crochet Creation",
          color: cartItem.color,
          quantity: cartItem.quantity,
          price: prod ? prod.price : 25.00,
          image: prod ? prod.image : "assets/images/strawberry-cow.jpg",
          customNote: cartItem.customNote || ""
        };
      }),
      timeline: [
        {
          stage: "received",
          title: "Order Placed & Yarn Selected",
          time: "Just now (" + now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ")",
          completed: true,
          current: true,
          desc: "Order confirmed! Artisanal milk cotton yarn skeins assigned to master crochet hook."
        },
        {
          stage: "crafted",
          title: "Handmade Stitched with Love",
          time: "Pending Crafting",
          completed: false,
          current: false,
          desc: "Artisan Elena is preparing her golden crochet hooks for your bespoke order."
        },
        {
          stage: "packed",
          title: "Ribbon Wrapped & Sweet Scented",
          time: "Pending Inspection",
          completed: false,
          current: false,
          desc: "Will be checked for neat stitches, tied with satin ribbon and scented with sweet floral mist."
        },
        {
          stage: "shipped",
          title: "Departed Studio & In Transit",
          time: "Pending Pickup",
          completed: false,
          current: false,
          desc: "Carrier scan: CraftExpress tracking will activate upon studio dispatch."
        },
        {
          stage: "delivered",
          title: "Delivered to Doorstep",
          time: "Est. " + estDate,
          completed: false,
          current: false,
          desc: "Artisan package will be gently delivered to your front porch."
        }
      ]
    };

    this.orders.unshift(newOrder);
    this.saveOrders();
    this.clearCart();
    this.removeCoupon();
    return newOrder;
  }

  advanceOrderStage(orderId) {
    const order = this.getOrderById(orderId);
    if (!order) return null;

    if (order.statusStage < 4) {
      order.statusStage += 1;
      const stages = ["received", "crafted", "packed", "shipped", "delivered"];
      const labels = [
        "Order Placed & Yarn Selected",
        "Artisan Handcrafting in Progress",
        "Ribbon Wrapped & Sweet Scented",
        "In Transit with Courier",
        "Delivered with Love 🌸"
      ];
      order.statusLabel = labels[order.statusStage];
      order.statusCode = stages[order.statusStage];

      // Update timeline flags
      order.timeline.forEach((item, idx) => {
        if (idx < order.statusStage) {
          item.completed = true;
          item.current = false;
        } else if (idx === order.statusStage) {
          item.completed = true;
          item.current = true;
          item.time = "Updated just now (" + new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ")";
        } else {
          item.completed = false;
          item.current = false;
        }
      });

      this.saveOrders();
    }
    return order;
  }
}

// Global store instance
const store = new Store();
