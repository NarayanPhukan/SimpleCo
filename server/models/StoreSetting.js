const mongoose = require('mongoose');

const storeSettingSchema = new mongoose.Schema({
  key: {
    type: String,
    required: true,
    unique: true,
    default: 'main_store_config'
  },
  storeName: {
    type: String,
    default: 'BabyyGurll Crochet Studio'
  },
  tagline: {
    type: String,
    default: 'Bespoke Handmade Crochet Creations'
  },
  announcementText: {
    type: String,
    default: 'Free delivery across India on all orders • Handcrafted with 100% milk cotton yarn'
  },
  announcementBadge: {
    type: String,
    default: 'Code: BABYYGURLL for 15% OFF'
  },
  announcementActive: {
    type: Boolean,
    default: true
  },
  hasSeededProducts: {
    type: Boolean,
    default: false
  },
  freeShippingThreshold: {
    type: Number,
    default: 0
  },
  standardShippingFee: {
    type: Number,
    default: 0
  },
  expressShippingFee: {
    type: Number,
    default: 0
  },
  supportEmail: {
    type: String,
    default: 'hello@babyygurll.in'
  },
  supportPhone: {
    type: String,
    default: '+91 98200 12345'
  },
  studioAddress: {
    type: String,
    default: 'Bandra West, Mumbai, Maharashtra 400050'
  },
  adminPassword: {
    type: String,
    default: 'admin123'
  },
  adminRazorpayAccountId: {
    type: String,
    default: ''
  },
  hasInitializedCoupons: {
    type: Boolean,
    default: false
  },
  appVersion: {
    latestVersionCode: { type: Number, default: 7 },
    latestVersionName: { type: String, default: '1.3.2' },
    minRequiredVersionCode: { type: Number, default: 1 },
    releaseNotes: {
      type: String,
      default: '• Fixed product update & delete reliability across MongoDB and memory\n• Separated admin app updates from customer app updates\n• Direct deletion support by slug and MongoDB _id\n• Enhanced error message reporting'
    },
    downloadUrl: { type: String, default: 'https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-admin/babyygurll-admin.apk' },
    releaseDate: { type: String, default: '2026-11-06' },
    critical: { type: Boolean, default: false }
  },
  customerAppVersion: {
    latestVersionCode: { type: Number, default: 10 },
    latestVersionName: { type: String, default: '1.0.9' },
    minRequiredVersionCode: { type: Number, default: 1 },
    releaseNotes: {
      type: String,
      default: '• Cancel order button placed exclusively in My Orders > Order Details\n• Automatic Razorpay refund integration for cancelled prepaid orders\n• Live UPI intent payment support & quiet luxury boutique UI'
    },
    downloadUrl: { type: String, default: 'https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-customer/babyygurll-boutique.apk' },
    releaseDate: { type: String, default: '2026-11-06' },
    critical: { type: Boolean, default: false }
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('StoreSetting', storeSettingSchema);
