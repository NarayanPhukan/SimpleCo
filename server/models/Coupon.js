const mongoose = require('mongoose');
const { getDBState } = require('../config/db');

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

const MongooseCoupon = mongoose.model('Coupon', couponSchema);

const Coupon = {
  find: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseCoupon.find(query);
    return memoryDB.coupons.find(query);
  },
  findOne: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseCoupon.findOne(query);
    return memoryDB.coupons.findOne(query);
  },
  findById: (id) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseCoupon.findById(id);
    return memoryDB.coupons.findById(id);
  },
  create: (doc) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseCoupon.create(doc);
    return memoryDB.coupons.create(doc);
  },
  insertMany: (docs) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseCoupon.insertMany(docs);
    return memoryDB.coupons.insertMany(docs);
  },
  countDocuments: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseCoupon.countDocuments(query);
    return memoryDB.coupons.countDocuments(query);
  },
  findByIdAndUpdate: (id, update, options) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseCoupon.findByIdAndUpdate(id, update, options);
    return memoryDB.coupons.findByIdAndUpdate(id, update, options);
  },
  findByIdAndDelete: (id) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseCoupon.findByIdAndDelete(id);
    return memoryDB.coupons.findByIdAndDelete(id);
  },
  findOneAndUpdate: (query, update, options) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseCoupon.findOneAndUpdate(query, update, options);
    return memoryDB.coupons.findOneAndUpdate(query, update, options);
  },
  findOneAndDelete: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseCoupon.findOneAndDelete(query);
    return memoryDB.coupons.findOneAndDelete(query);
  }
};

module.exports = Coupon;
