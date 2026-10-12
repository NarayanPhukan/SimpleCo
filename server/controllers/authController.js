const User = require('../models/User');

// @desc    Register a new user
// @route   POST /api/auth/register
exports.register = async (req, res) => {
  try {
    const { name, email, phone, password } = req.body;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email is required' });
    }

    const cleanEmail = email.trim().toLowerCase();
    let user = await User.findOne({ email: cleanEmail });
    if (user) {
      return res.status(400).json({ success: false, message: 'User already exists with this email' });
    }

    user = await User.create({
      name: name || 'Customer',
      email: cleanEmail,
      phone: phone || '',
      password: password || '123456',
      memberTier: 'VIP Collector Club',
      memberSince: new Date().toLocaleDateString('en-IN', { month: 'long', year: 'numeric' }),
      savedAddresses: []
    });

    res.status(201).json({ success: true, data: user });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Login user
// @route   POST /api/auth/login
exports.login = async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email is required' });
    }

    const cleanEmail = email.trim().toLowerCase();
    let user = await User.findOne({ email: cleanEmail });
    if (!user) {
      return res.status(401).json({ success: false, message: 'No account found with this email' });
    }

    if (user.password !== password) {
      return res.status(401).json({ success: false, message: 'Incorrect password' });
    }

    res.json({ success: true, data: user });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get user profile with addresses
// @route   GET /api/auth/me
exports.getMe = async (req, res) => {
  try {
    const email = req.query.email ? req.query.email.trim().toLowerCase() : null;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email query parameter required' });
    }
    let user = await User.findOne({ email });
    if (!user) {
      const displayName = email.split('@')[0].replace(/[._]/g, ' ').replace(/\b\w/g, (l) => l.toUpperCase());
      user = await User.create({
        name: displayName,
        email,
        phone: '+91 98765 43210',
        password: 'session_user',
        memberTier: 'VIP Collector Club',
        memberSince: new Date().toLocaleDateString('en-IN', { month: 'long', year: 'numeric' }),
        savedAddresses: []
      });
    }
    res.json({ success: true, data: user });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Add a saved address
// @route   POST /api/auth/addresses
exports.addAddress = async (req, res) => {
  try {
    const { email, address } = req.body;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email is required' });
    }
    const cleanEmail = email.trim().toLowerCase();
    const user = await User.findOne({ email: cleanEmail });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    if (address.isDefault || user.savedAddresses.length === 0) {
      user.savedAddresses.forEach(a => { a.isDefault = false; });
      address.isDefault = true;
    }

    user.savedAddresses.push(address);
    await user.save();

    res.status(201).json({ success: true, data: user.savedAddresses });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Update a saved address
// @route   PUT /api/auth/addresses/:id
exports.updateAddress = async (req, res) => {
  try {
    const { email, address } = req.body;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email is required' });
    }
    const cleanEmail = email.trim().toLowerCase();
    const user = await User.findOne({ email: cleanEmail });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    const addr = user.savedAddresses.id(req.params.id);
    if (!addr) {
      return res.status(404).json({ success: false, message: 'Address not found' });
    }

    if (address.isDefault) {
      user.savedAddresses.forEach(a => { a.isDefault = false; });
    }

    Object.assign(addr, address);
    await user.save();

    res.json({ success: true, data: user.savedAddresses });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Delete a saved address
// @route   DELETE /api/auth/addresses/:id
exports.deleteAddress = async (req, res) => {
  try {
    const email = req.query.email ? req.query.email.trim().toLowerCase() : null;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email query parameter required' });
    }
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    const wasDefault = user.savedAddresses.id(req.params.id)?.isDefault;
    user.savedAddresses.pull({ _id: req.params.id });

    if (wasDefault && user.savedAddresses.length > 0) {
      user.savedAddresses[0].isDefault = true;
    }

    await user.save();
    res.json({ success: true, data: user.savedAddresses });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Set address as default
// @route   PATCH /api/auth/addresses/:id/default
exports.setDefaultAddress = async (req, res) => {
  try {
    const { email } = req.body;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email is required' });
    }
    const cleanEmail = email.trim().toLowerCase();
    const user = await User.findOne({ email: cleanEmail });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    user.savedAddresses.forEach(a => {
      a.isDefault = (a._id.toString() === req.params.id);
    });

    await user.save();
    res.json({ success: true, data: user.savedAddresses });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Forgot / Reset password
// @route   POST /api/auth/forgot-password
exports.forgotPassword = async (req, res) => {
  try {
    const { email, newPassword } = req.body;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email address is required' });
    }

    const cleanEmail = email.trim().toLowerCase();
    const user = await User.findOne({ email: cleanEmail });
    if (!user) {
      return res.status(404).json({ success: false, message: 'No registered account found with this email' });
    }

    if (!newPassword || newPassword.length < 6) {
      return res.status(400).json({ success: false, message: 'New password must be at least 6 characters' });
    }

    user.password = newPassword;
    await user.save();

    res.json({
      success: true,
      message: 'Password reset successfully. Please sign in with your new password.',
      data: user
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
