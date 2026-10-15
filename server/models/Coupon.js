const mongoose = require('mongoose');

const couponSchema = new mongoose.Schema({
  code: {
    type: String,
    required: true,
    unique: true,
    uppercase: true,
    trim: true
  },
  description: {
    type: String,
    default: ''
  },
  discountType: {
    type: String,
    enum: ['percentage', 'flat', 'free_shipping'],
    default: 'percentage'
  },
  discountValue: {
    type: Number,
    default: 10
  },
  minOrderValue: {
    type: Number,
    default: 0
  },
  expiryDate: {
    type: Date,
    default: null
  },
  isPublic: {
    type: Boolean,
    default: true
  },
  isActive: {
    type: Boolean,
    default: true
  },
  usageCount: {
    type: Number,
    default: 0
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('Coupon', couponSchema);
