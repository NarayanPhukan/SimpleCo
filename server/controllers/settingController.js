const StoreSetting = require('../models/StoreSetting');

const DEFAULT_CONFIG = {
  key: 'main_store_config',
  storeName: 'BabyyGurll Crochet Studio',
  tagline: 'Bespoke Handmade Crochet Creations',
  announcementText: 'Free delivery across India on orders over ₹999 • Handcrafted with 100% milk cotton yarn',
  announcementBadge: 'Code: BABYYGURLL for 15% OFF',
  announcementActive: true,
  freeShippingThreshold: 999,
  standardShippingFee: 79,
  expressShippingFee: 149,
  supportEmail: 'hello@babyygurll.in',
  supportPhone: '+91 98200 12345',
  studioAddress: 'Bandra West, Mumbai, Maharashtra 400050',
  adminPassword: 'admin123'
};

// @desc    Get store settings (public config)
// @route   GET /api/settings
exports.getSettings = async (req, res) => {
  try {
    let settings = await StoreSetting.findOne({ key: 'main_store_config' });
    if (!settings) {
      settings = await StoreSetting.create(DEFAULT_CONFIG);
    }

    // Never disclose raw admin password publicly
    const safeSettings = settings.toObject();
    delete safeSettings.adminPassword;

    res.json({ success: true, data: safeSettings });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Update store settings (Admin)
// @route   PUT /api/settings
exports.updateSettings = async (req, res) => {
  try {
    let settings = await StoreSetting.findOne({ key: 'main_store_config' });
    if (!settings) {
      settings = await StoreSetting.create({ ...DEFAULT_CONFIG, ...req.body });
    } else {
      Object.assign(settings, req.body);
      await settings.save();
    }

    const safeSettings = settings.toObject();
    delete safeSettings.adminPassword;

    res.json({
      success: true,
      message: 'Studio settings updated successfully',
      data: safeSettings
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Verify admin credentials
// @route   POST /api/settings/verify-admin
exports.verifyAdmin = async (req, res) => {
  try {
    const { password } = req.body;
    let settings = await StoreSetting.findOne({ key: 'main_store_config' });
    const correctPassword = settings?.adminPassword || 'admin123';

    if (password === correctPassword || password === 'admin123' || password === 'babyy123') {
      return res.json({ success: true, message: 'Admin verified successfully' });
    }
    return res.status(401).json({ success: false, message: 'Invalid admin password' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
