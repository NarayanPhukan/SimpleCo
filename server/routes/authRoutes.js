const express = require('express');
const router = express.Router();
const {
  register,
  login,
  getMe,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  forgotPassword
} = require('../controllers/authController');

router.post('/register', register);
router.post('/login', login);
router.post('/forgot-password', forgotPassword);
router.get('/me', getMe);

// Saved Addresses Endpoints
router.post('/addresses', addAddress);
router.put('/addresses/:id', updateAddress);
router.delete('/addresses/:id', deleteAddress);
router.patch('/addresses/:id/default', setDefaultAddress);

module.exports = router;
