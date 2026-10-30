const Coupon = require('../models/Coupon');
const StoreSetting = require('../models/StoreSetting');
const mongoose = require('mongoose');

// Default initial coupons
const DEFAULT_COUPONS = [
  {
    code: 'BABYYGURLL',
    description: 'Store opening special welcome discount for all collectors',
    discountType: 'percentage',
    discountValue: 15,
    minOrderValue: 0,
    expiryDate: new Date('2026-12-31T23:59:59.000Z'),
    isPublic: true,
    isActive: true,
    usageCount: 0
  },
  {
    code: 'CUTIE100',
    description: 'Instant flat ₹100 deduction on orders above ₹500',
    discountType: 'flat',
    discountValue: 100,
    minOrderValue: 500,
    expiryDate: new Date('2026-11-30T23:59:59.000Z'),
    isPublic: true,
    isActive: true,
    usageCount: 0
  },
  {
    code: 'FREESHIP',
    description: 'Free pan-India tracked courier delivery across all states',
    discountType: 'free_shipping',
    discountValue: 0,
    minOrderValue: 0,
    expiryDate: new Date('2026-12-31T23:59:59.000Z'),
    isPublic: true,
    isActive: true,
    usageCount: 0
  }
];

// Seed default coupons ONLY ONCE upon initial store configuration.
// Once initialized, coupons are never re-seeded so admin deletions persist permanently.
const ensureDefaultCoupons = async () => {
  try {
    let settings = await StoreSetting.findOne({ key: 'main_store_config' });
    if (!settings) {
      settings = await StoreSetting.create({
        key: 'main_store_config',
        hasInitializedCoupons: false
      });
    }

    if (!settings.hasInitializedCoupons) {
      const count = await Coupon.countDocuments();
      if (count === 0) {
        await Coupon.insertMany(DEFAULT_COUPONS);
      }
      if (typeof settings.save === 'function') {
        settings.hasInitializedCoupons = true;
        await settings.save();
      } else {
        await StoreSetting.findOneAndUpdate(
          { key: 'main_store_config' },
          { hasInitializedCoupons: true }
        );
      }
    }
  } catch (err) {
    console.warn('Coupon auto-seed note:', err.message);
  }
};

// @desc    Get all coupons (Admin and Storefront Offers)
// @route   GET /api/coupons
exports.getCoupons = async (req, res) => {
  try {
    await ensureDefaultCoupons();
    const coupons = await Coupon.find().sort({ createdAt: -1 });
    res.json({ success: true, count: coupons.length, data: coupons });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Validate a coupon code (Storefront Checkout)
// @route   POST /api/coupons/validate
exports.validateCoupon = async (req, res) => {
  try {
    const { code, cartSubtotal = 0 } = req.body;
    if (!code) {
      return res.status(400).json({ success: false, message: 'Please enter a coupon code' });
    }

    await ensureDefaultCoupons();
    const cleanCode = code.trim().toUpperCase();
    const coupon = await Coupon.findOne({ code: cleanCode, isActive: true });

    if (!coupon) {
      return res.status(404).json({ success: false, message: `Coupon "${cleanCode}" is invalid or inactive.` });
    }

    // 1. Expiry Date Check
    if (coupon.expiryDate && new Date() > new Date(coupon.expiryDate)) {
      const expDateStr = new Date(coupon.expiryDate).toLocaleDateString('en-IN', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      });
      return res.status(400).json({
        success: false,
        message: `Promo code "${cleanCode}" expired on ${expDateStr}.`
      });
    }

    // 2. Shopping Limit / Minimum Cart Value Check
    if (coupon.minOrderValue > 0 && cartSubtotal < coupon.minOrderValue) {
      const diff = Math.round(coupon.minOrderValue - cartSubtotal);
      return res.status(400).json({
        success: false,
        message: `Code "${cleanCode}" requires a minimum cart spend of ₹${coupon.minOrderValue}. Add ₹${diff} more to apply!`
      });
    }

    let calculatedDiscount = 0;
    if (coupon.discountType === 'percentage') {
      calculatedDiscount = Math.round(cartSubtotal * (coupon.discountValue / 100));
    } else if (coupon.discountType === 'flat') {
      calculatedDiscount = Math.min(cartSubtotal, coupon.discountValue);
    }

    res.json({
      success: true,
      message: `Coupon "${coupon.code}" applied successfully!`,
      data: {
        code: coupon.code,
        description: coupon.description,
        discountType: coupon.discountType,
        discountValue: coupon.discountValue,
        calculatedDiscount,
        freeShipping: coupon.discountType === 'free_shipping',
        minOrderValue: coupon.minOrderValue,
        expiryDate: coupon.expiryDate
      }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Create new coupon (Admin)
// @route   POST /api/coupons
exports.createCoupon = async (req, res) => {
  try {
    const { code, description, discountType, discountValue, minOrderValue, expiryDate, isPublic, isActive } = req.body;
    if (!code) {
      return res.status(400).json({ success: false, message: 'Coupon code is required' });
    }

    const cleanCode = code.trim().toUpperCase();
    const existing = await Coupon.findOne({ code: cleanCode });
    if (existing) {
      return res.status(400).json({ success: false, message: `Coupon with code "${cleanCode}" already exists` });
    }

    const coupon = await Coupon.create({
      code: cleanCode,
      description: description || '',
      discountType: discountType || 'percentage',
      discountValue: Number(discountValue) || 0,
      minOrderValue: Number(minOrderValue) || 0,
      expiryDate: expiryDate ? new Date(expiryDate) : null,
      isPublic: isPublic !== false,
      isActive: isActive !== false,
      usageCount: 0
    });

    res.status(201).json({ success: true, data: coupon });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Update coupon (Admin)
// @route   PUT /api/coupons/:id
exports.updateCoupon = async (req, res) => {
  try {
    const rawId = (req.params.id || '').trim();
    let coupon = null;
    const isObjectId = /^[0-9a-fA-F]{24}$/.test(rawId);

    if (isObjectId) {
      coupon = await Coupon.findByIdAndUpdate(rawId, req.body, { new: true, runValidators: true });
      if (!coupon) {
        coupon = await Coupon.findOneAndUpdate({ _id: rawId }, req.body, { new: true, runValidators: true });
      }
    }

    if (!coupon) {
      coupon = await Coupon.findOneAndUpdate({ code: rawId.toUpperCase() }, req.body, { new: true, runValidators: true });
    }

    if (!coupon) {
      return res.status(404).json({ success: false, message: 'Coupon not found' });
    }
    res.json({ success: true, data: coupon });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Delete coupon (Admin)
// @route   DELETE /api/coupons/:id
exports.deleteCoupon = async (req, res) => {
  try {
    const rawId = (req.params.id || '').trim();
    if (!rawId) {
      return res.status(400).json({ success: false, message: 'Coupon identifier is required' });
    }

    let coupon = null;
    const isObjectId = /^[0-9a-fA-F]{24}$/.test(rawId);

    if (isObjectId) {
      coupon = await Coupon.findByIdAndDelete(rawId);
      if (!coupon) {
        coupon = await Coupon.findOneAndDelete({ _id: rawId });
      }
    }

    if (!coupon) {
      coupon = await Coupon.findOneAndDelete({ code: rawId.toUpperCase() });
    }

    if (!coupon) {
      return res.status(404).json({ success: false, message: 'Coupon not found or already deleted' });
    }
    res.json({ success: true, message: `Coupon "${coupon.code}" deleted successfully` });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Restore default starter coupons manually (Admin)
// @route   POST /api/coupons/reset-defaults
exports.resetDefaultCoupons = async (req, res) => {
  try {
    for (const def of DEFAULT_COUPONS) {
      const exists = await Coupon.findOne({ code: def.code });
      if (!exists) {
        await Coupon.create(def);
      }
    }
    const coupons = await Coupon.find().sort({ createdAt: -1 });
    res.json({ success: true, message: 'Default store coupons restored successfully', data: coupons });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
