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
    default: 'Free delivery across India on orders over ₹999 • Handcrafted with 100% milk cotton yarn'
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
    default: 999
  },
  standardShippingFee: {
    type: Number,
    default: 79
  },
  expressShippingFee: {
    type: Number,
    default: 149
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
  hasInitializedCoupons: {
    type: Boolean,
    default: false
  },
  appVersion: {
    latestVersionCode: { type: Number, default: 4 },
    latestVersionName: { type: String, default: '1.2.1' },
    minRequiredVersionCode: { type: Number, default: 1 },
    releaseNotes: {
      type: String,
      default: '• Dynamic version detection (no repeated update prompts after installing)\n• Direct local photo selection with Coil DataUriFetcher\n• Instant in-memory and Base64 image preview for new & edited pieces\n• Customer POV live card preview before publishing\n• High-contrast UI: crisp white text across all blue buttons & chips\n• Real-time cloud-synced dynamic fulfillment dashboard'
    },
    downloadUrl: { type: String, default: 'https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-admin/babyygurll-admin.apk' },
    releaseDate: { type: String, default: '2026-10-31' },
    critical: { type: Boolean, default: false }
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('StoreSetting', storeSettingSchema);
