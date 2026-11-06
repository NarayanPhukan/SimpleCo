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

    // Automated Route transfer: If Admin's Linked Account ID is configured,
    // automatically route 100% of the funds directly to the Admin's bank account.
    let adminAccountId = process.env.ADMIN_RAZORPAY_ACCOUNT_ID;
    if (!adminAccountId || !adminAccountId.trim().startsWith('acc_')) {
      try {
        const StoreSetting = require('../models/StoreSetting');
        const settingDoc = await StoreSetting.findOne({ key: 'main_store_config' });
        if (settingDoc?.adminRazorpayAccountId?.trim().startsWith('acc_')) {
          adminAccountId = settingDoc.adminRazorpayAccountId.trim();
        }
      } catch (e) {
        // Fallback to env
      }
    }

    if (adminAccountId && adminAccountId.trim().startsWith('acc_')) {
      options.transfers = [
        {
          account: adminAccountId.trim(),
          amount: numericAmount,
          currency: currency.toUpperCase(),
          on_hold: 0 // Settles automatically to Admin's bank account
        }
      ];
    }

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

// @desc    Issue a full or partial refund via Razorpay
exports.issueRefund = async (paymentId, amountInPaise, notes = {}) => {
  const razorpay = getRazorpayInstance();
  if (!razorpay) {
    throw new Error('Razorpay credentials not configured');
  }
  const refundOptions = { 
    notes,
    speed: 'optimum' // Attempt instant refund if supported by customer's bank/UPI, fallback to normal
  };
  if (amountInPaise && amountInPaise > 0) {
    refundOptions.amount = Math.round(amountInPaise);
  }
  return await razorpay.payments.refund(paymentId, refundOptions);
};

// @desc    Fetch successful payment ID for an order ID from Razorpay
exports.fetchPaymentForOrder = async (orderId) => {
  const razorpay = getRazorpayInstance();
  if (!razorpay || !orderId) return null;
  const payments = await razorpay.orders.fetchPayments(orderId);
  if (payments && payments.items && payments.items.length > 0) {
    const successful = payments.items.find(p => p.status === 'captured') || payments.items[0];
    return successful ? successful.id : null;
  }
  return null;
};

// @desc    Get all refunds from Razorpay API + DB records
// @route   GET /api/payment/refunds
exports.getAllRefunds = async (req, res) => {
  try {
    const razorpay = getRazorpayInstance();
    let rzpRefunds = [];
    if (razorpay) {
      try {
        const response = await razorpay.refunds.all({ count: 50 });
        if (response && response.items) {
          rzpRefunds = response.items;
        }
      } catch (err) {
        console.warn('Razorpay refunds.all fetch error (falling back to database):', err.message);
      }
    }

    const Order = require('../models/Order');
    let orderRefunds = [];
    try {
      const orders = await Order.find({});
      orderRefunds = (orders || []).filter(o => 
        (o.razorpayRefundId && o.razorpayRefundId.trim() !== '') ||
        (o.refundStatus && o.refundStatus.trim() !== '') ||
        o.paymentStatus === 'refunded' ||
        o.paymentStatus === 'refund_pending'
      ).map(o => ({
        id: o.razorpayRefundId || `rfnd_${o.orderId}`,
        orderId: o.orderId,
        paymentId: o.razorpayPaymentId || '',
        amount: Math.round((o.refundAmount || o.total || 0) * 100), // in paise
        currency: 'INR',
        status: o.refundStatus || (o.paymentStatus === 'refunded' ? 'processed' : 'pending'),
        speed_processed: 'optimum',
        created_at: o.cancelledAt ? Math.floor(new Date(o.cancelledAt).getTime() / 1000) : Math.floor(Date.now() / 1000),
        customerName: o.shippingAddress?.recipientName || (o.userEmail ? o.userEmail.split('@')[0] : 'Customer'),
        customerEmail: o.userEmail || '',
        notes: {
          reason: o.cancelReason || 'Order cancellation refund',
          orderId: o.orderId
        }
      }));
    } catch (e) {
      console.error('Error fetching order refunds from DB:', e);
    }

    const allRefundMap = new Map();
    orderRefunds.forEach(r => allRefundMap.set(r.id, r));
    rzpRefunds.forEach(r => {
      allRefundMap.set(r.id, {
        id: r.id,
        orderId: r.notes?.orderId || r.payment_id || '',
        paymentId: r.payment_id,
        amount: r.amount,
        currency: r.currency || 'INR',
        status: r.status,
        speed_processed: r.speed_processed || 'normal',
        created_at: r.created_at,
        customerName: r.notes?.customerName || '',
        customerEmail: r.notes?.customerEmail || '',
        notes: r.notes || {}
      });
    });

    const refunds = Array.from(allRefundMap.values()).sort((a, b) => (b.created_at || 0) - (a.created_at || 0));

    return res.status(200).json({
      success: true,
      count: refunds.length,
      refunds
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || 'Failed to fetch refunds'
    });
  }
};

// @desc    Issue a manual or automated refund for an order via Razorpay API
// @route   POST /api/payment/refund
exports.issueOrderRefund = async (req, res) => {
  try {
    const { orderId, amount, reason } = req.body;
    if (!orderId) {
      return res.status(400).json({ success: false, message: 'Order ID is required' });
    }

    const Order = require('../models/Order');
    const order = await Order.findOne({ orderId: String(orderId) });
    if (!order) {
      return res.status(404).json({ success: false, message: 'Order not found' });
    }

    let paymentId = order.razorpayPaymentId;
    if (!paymentId && order.razorpayOrderId) {
      paymentId = await exports.fetchPaymentForOrder(order.razorpayOrderId);
      if (paymentId) order.razorpayPaymentId = paymentId;
    }

    if (!paymentId) {
      return res.status(400).json({ 
        success: false, 
        message: 'No Razorpay payment ID recorded for this order. It might be COD or unpaid.' 
      });
    }

    const refundAmountInPaise = amount ? Math.round(Number(amount) * 100) : Math.round(order.total * 100);
    const refund = await exports.issueRefund(paymentId, refundAmountInPaise, {
      orderId: String(orderId),
      reason: reason || 'Admin initiated refund'
    });

    order.razorpayRefundId = refund.id;
    order.refundStatus = refund.status || 'processed';
    order.refundAmount = refund.amount ? refund.amount / 100 : (refundAmountInPaise / 100);
    order.paymentStatus = 'refunded';
    await order.save();

    return res.status(200).json({
      success: true,
      message: `Refund of ₹${order.refundAmount} issued successfully.`,
      refund
    });
  } catch (error) {
    console.error('Issue order refund error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Failed to issue refund'
    });
  }
};

