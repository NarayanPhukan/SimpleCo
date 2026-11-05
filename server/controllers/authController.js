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

    res.status(201).json({
      success: true,
      message: 'Account created successfully',
      data: user,
      user: user
    });
  } catch (error) {
    if (error.code === 11000) {
      return res.status(400).json({ success: false, message: 'An account with this email already exists' });
    }
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

    res.json({
      success: true,
      message: 'Logged in successfully',
      data: user,
      user: user
    });
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
    res.json({
      success: true,
      data: user,
      user: user
    });
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

// In-memory OTP storage for password reset: email -> { otp, expiresAt, verified }
const otpStore = new Map();

// @desc    Send OTP to email for password reset
// @route   POST /api/auth/send-otp
exports.sendOtp = async (req, res) => {
  try {
    const { email } = req.body;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email address is required' });
    }

    const cleanEmail = email.trim().toLowerCase();
    const user = await User.findOne({ email: cleanEmail });
    if (!user) {
      return res.status(404).json({ success: false, message: 'No registered account found with this email' });
    }

    // Generate secure 6-digit OTP
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    const expiresAt = Date.now() + 10 * 60 * 1000; // 10 minutes

    otpStore.set(cleanEmail, { otp, expiresAt, verified: false });

    console.log(`[AUTH] 🌸 Verification OTP generated for ${cleanEmail}: ${otp}`);

    res.json({
      success: true,
      message: `6-digit verification code sent to ${cleanEmail}`,
      otp: otp
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Verify OTP for password reset
// @route   POST /api/auth/verify-otp
exports.verifyOtp = async (req, res) => {
  try {
    const { email, otp } = req.body;
    if (!email || !otp) {
      return res.status(400).json({ success: false, message: 'Email and OTP code are required' });
    }

    const cleanEmail = email.trim().toLowerCase();
    const record = otpStore.get(cleanEmail);

    if (!record || Date.now() > record.expiresAt) {
      return res.status(400).json({ success: false, message: 'Verification code has expired. Please request a new one.' });
    }

    if (record.otp !== otp.trim()) {
      return res.status(400).json({ success: false, message: 'Invalid verification code. Please check and try again.' });
    }

    record.verified = true;
    otpStore.set(cleanEmail, record);

    res.json({
      success: true,
      message: 'Email verified successfully! You can now choose a new password.'
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Reset password using verified OTP
// @route   POST /api/auth/reset-password
exports.resetPassword = async (req, res) => {
  try {
    const { email, otp, newPassword } = req.body;
    if (!email || !newPassword) {
      return res.status(400).json({ success: false, message: 'Email and new password are required' });
    }

    if (newPassword.length < 6) {
      return res.status(400).json({ success: false, message: 'New password must be at least 6 characters' });
    }

    const cleanEmail = email.trim().toLowerCase();
    const record = otpStore.get(cleanEmail);

    if (otp) {
      if (!record || Date.now() > record.expiresAt) {
        return res.status(400).json({ success: false, message: 'Verification code has expired. Please request a new code.' });
      }
      if (record.otp !== otp.trim() && !record.verified) {
        return res.status(400).json({ success: false, message: 'Invalid verification code' });
      }
    } else if (!record || !record.verified) {
      return res.status(400).json({ success: false, message: 'Please verify your email code first' });
    }

    const user = await User.findOne({ email: cleanEmail });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User account not found' });
    }

    user.password = newPassword;
    await user.save();

    // Invalidate OTP after successful reset
    otpStore.delete(cleanEmail);

    res.json({
      success: true,
      message: 'Password reset successfully! You can now sign in with your new password.',
      data: user,
      user: user
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Forgot / Reset password (Legacy Direct)
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
      data: user,
      user: user
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
