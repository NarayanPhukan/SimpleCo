const express = require('express');
const router = express.Router();
const {
  createOrder,
  verifyPayment,
  getKey,
  getAllRefunds,
  issueOrderRefund
} = require('../controllers/paymentController');

// Standard endpoints as specified in task
router.post('/create-order', createOrder);
router.post('/verify-payment', verifyPayment);
router.get('/razorpay-key', getKey);
router.get('/key', getKey);
router.get('/refunds', getAllRefunds);
router.post('/refund', issueOrderRefund);

module.exports = router;
