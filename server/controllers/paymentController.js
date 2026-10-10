const Razorpay = require('razorpay');
const crypto = require('crypto');

// Initialize Razorpay instance lazily with active environment credentials
const getRazorpayInstance = () => {
  const key_id = process.env.RAZORPAY_KEY_ID;
  const key_secret = process.env.RAZORPAY_KEY_SECRET;

  if (!key_id || !key_secret) {
    return null;
  }

  return new Razorpay({
    key_id,
    key_secret
  });
};

// @desc    Create Razorpay Order
// @route   POST /api/create-order (or POST /api/payment/create-order)
exports.createOrder = async (req, res) => {
  try {
    const key_id = process.env.RAZORPAY_KEY_ID;
    const key_secret = process.env.RAZORPAY_KEY_SECRET;

    if (!key_id || !key_secret) {
      return res.status(401).json({
        success: false,
        message: 'Razorpay credentials not configured. Please set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.'
      });
    }

    const { amount, currency = 'INR', receipt } = req.body;

    const numericAmount = parseInt(amount, 10);
    // Validate amount >= 100 paise (₹1.00)
    if (isNaN(numericAmount) || numericAmount < 100) {
      return res.status(400).json({
        success: false,
        message: 'Invalid amount. Minimum amount must be at least 100 paise (₹1.00).'
      });
    }

    const razorpay = getRazorpayInstance();
    const options = {
      amount: numericAmount, // Amount in paise
      currency: currency.toUpperCase(),
      receipt: receipt || `rcpt_${Date.now()}`
    };

    const order = await razorpay.orders.create(options);

    return res.status(200).json({
      success: true,
      order_id: order.id,
      id: order.id,
      amount: order.amount,
      currency: order.currency,
      key_id: key_id
    });
  } catch (error) {
    console.error('Razorpay Order Creation Error:', error);

    // Handle authentication / bad request failures
    if (error.statusCode === 401 || (error.error && error.error.code === 'BAD_REQUEST_ERROR')) {
      return res.status(error.statusCode || 401).json({
        success: false,
        message: error.error?.description || 'Razorpay authentication failed.'
      });
    }

    return res.status(500).json({
      success: false,
      message: error.error?.description || error.message || 'Failed to create Razorpay order.'
    });
  }
};

// @desc    Verify Razorpay Payment Signature
// @route   POST /api/verify-payment (or POST /api/payment/verify-payment)
exports.verifyPayment = async (req, res) => {
  try {
    const key_secret = process.env.RAZORPAY_KEY_SECRET;
    if (!key_secret) {
      return res.status(401).json({
        success: false,
        message: 'Razorpay secret key not configured on server.'
      });
    }

    const {
      razorpay_order_id,
      razorpay_payment_id,
      razorpay_signature,
      order_id,
      payment_id,
      signature
    } = req.body;

    const effectiveOrderId = razorpay_order_id || order_id;
    const effectivePaymentId = razorpay_payment_id || payment_id;
    const effectiveSignature = razorpay_signature || signature;

    // Validate presence of required verification parameters
    if (!effectiveOrderId || !effectivePaymentId || !effectiveSignature) {
      return res.status(400).json({
        success: false,
        message: 'Missing required payment verification parameters (order_id, payment_id, signature).'
      });
    }

    // HMAC-SHA256 (order_id + "|" + payment_id, KEY_SECRET)
    const hmac = crypto.createHmac('sha256', key_secret);
    hmac.update(`${effectiveOrderId}|${effectivePaymentId}`);
    const generated_signature = hmac.digest('hex');

    // Signature comparison
    if (generated_signature !== effectiveSignature) {
      return res.status(400).json({
        success: false,
        message: 'Payment verification failed: signature mismatch.',
        verified: false
      });
    }

    return res.status(200).json({
      success: true,
      message: 'Payment verified successfully.',
      verified: true,
      order_id: effectiveOrderId,
      payment_id: effectivePaymentId
    });
  } catch (error) {
    console.error('Razorpay Signature Verification Error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Internal server error verifying payment signature.'
    });
  }
};

// @desc    Get Razorpay Public Key ID
// @route   GET /api/razorpay-key (or GET /api/payment/key)
exports.getKey = (req, res) => {
  const key_id = process.env.RAZORPAY_KEY_ID || '';
  return res.status(200).json({
    success: true,
    key_id
  });
};
