const User = require('../models/User');

// @desc    Register a new user
// @route   POST /api/auth/register
exports.register = async (req, res) => {
  try {
    const { name, email, phone, password } = req.body;

    let user = await User.findOne({ email });
    if (user) {
      return res.status(400).json({ success: false, message: 'User already exists with this email' });
    }

    user = await User.create({
      name,
      email,
      phone: phone || '+1 (555) 000-0000',
      password, // for full production use bcrypt hash
      memberTier: 'VIP Stitcher Club 🧶',
      memberSince: 'October 2026',
      savedAddresses: [
        {
          label: 'Primary Home',
          recipientName: name,
          phone: phone || '+1 (555) 000-0000',
          street: '100 Cozy Lane',
          city: 'Sunnyvale',
          state: 'CA',
          zip: '94086',
          country: 'United States',
          isDefault: true
        }
      ]
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

    let user = await User.findOne({ email: email.toLowerCase() });
    if (!user) {
      // Auto-create or return error
      return res.status(401).json({ success: false, message: 'Invalid email or password' });
    }

    if (user.password !== password) {
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
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
    const email = req.query.email || 'chloe@babyygurll.dev';
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
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
    const user = await User.findOne({ email: email || 'chloe@babyygurll.dev' });
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
    const user = await User.findOne({ email: email || 'chloe@babyygurll.dev' });
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
    const email = req.query.email || 'chloe@babyygurll.dev';
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
    const user = await User.findOne({ email: email || 'chloe@babyygurll.dev' });
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
