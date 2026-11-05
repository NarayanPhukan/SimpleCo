const crypto = require('crypto');
const Order = require('../models/Order');

/**
 * Helper to generate PayU request hash
 */
const generatePayUHash = ({
  key,
  txnid,
  amount,
  productinfo,
  firstname,
  email,
  udf1 = '',
  udf2 = '',
  udf3 = '',
  udf4 = '',
  udf5 = '',
  salt
}) => {
  const hashString = `${key}|${txnid}|${amount}|${productinfo}|${firstname}|${email}|${udf1}|${udf2}|${udf3}|${udf4}|${udf5}||||||${salt}`;
  return crypto.createHash('sha512').update(hashString).digest('hex');
};

/**
 * Helper to verify PayU response hash
 */
const verifyPayUResponseHash = (body, salt) => {
  const {
    status,
    udf1 = '',
    udf2 = '',
    udf3 = '',
    udf4 = '',
    udf5 = '',
    email = '',
    firstname = '',
    productinfo = '',
    amount,
    txnid,
    key,
    additionalCharges,
    hash: receivedHash
  } = body;

  let hashString = '';
  if (additionalCharges) {
    hashString = `${additionalCharges}|${salt}|${status}||||||${udf5}|${udf4}|${udf3}|${udf2}|${udf1}|${email}|${firstname}|${productinfo}|${amount}|${txnid}|${key}`;
  } else {
    hashString = `${salt}|${status}||||||${udf5}|${udf4}|${udf3}|${udf2}|${udf1}|${email}|${firstname}|${productinfo}|${amount}|${txnid}|${key}`;
  }

  const calculatedHash = crypto.createHash('sha512').update(hashString).digest('hex');
  return calculatedHash.toLowerCase() === String(receivedHash || '').toLowerCase();
};

/**
 * @desc    Initiate PayU Payment (Generates parameters, hash & action URL)
 * @route   POST /api/payu/initiate
 */
exports.initiatePayment = async (req, res) => {
  try {
    const key = process.env.PAYU_KEY;
    const salt = process.env.PAYU_SALT;
    const baseUrl = process.env.PAYU_BASE_URL || 'https://secure.payu.in/_payment';

    if (!key || !salt) {
      return res.status(500).json({
        success: false,
        message: 'PayU credentials not configured on server. Please set PAYU_KEY and PAYU_SALT.'
      });
    }

    const {
      amount,
      productinfo = 'BabyyGurll Crochet Order',
      firstname = 'Customer',
      email = 'customer@example.com',
      phone = '9820012345',
      orderId,
      udf1 = '',
      udf2 = '',
      udf3 = '',
      udf4 = '',
      udf5 = ''
    } = req.body;

    const numericAmount = parseFloat(amount);
    if (isNaN(numericAmount) || numericAmount <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Invalid payment amount.'
      });
    }

    // Amount formatted to 2 decimals as required by PayU
    const formattedAmount = numericAmount.toFixed(2);
    const txnid = orderId ? `txn_${orderId}_${Date.now()}` : `txn_${Date.now()}`;
    const effectiveUdf1 = udf1 || orderId || '';

    // Determine host for redirect URLs
    const protocol = req.headers['x-forwarded-proto'] || req.protocol || 'http';
    const host = req.get('host');
    const callbackUrl = `${protocol}://${host}/api/payu/response`;

    // Calculate SHA-512 request hash
    const hash = generatePayUHash({
      key,
      txnid,
      amount: formattedAmount,
      productinfo: productinfo.trim(),
      firstname: firstname.trim(),
      email: email.trim(),
      udf1: effectiveUdf1,
      udf2,
      udf3,
      udf4,
      udf5,
      salt
    });

    const params = {
      key,
      txnid,
      amount: formattedAmount,
      productinfo: productinfo.trim(),
      firstname: firstname.trim(),
      email: email.trim(),
      phone: phone.trim(),
      surl: callbackUrl,
      furl: callbackUrl,
      hash,
      udf1: effectiveUdf1,
      udf2,
      udf3,
      udf4,
      udf5
    };

    return res.status(200).json({
      success: true,
      action: baseUrl,
      params
    });
  } catch (error) {
    console.error('PayU Initiate Payment Error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Failed to initiate PayU payment.'
    });
  }
};

/**
 * @desc    Handle PayU Callback (surl / furl)
 * @route   POST /api/payu/response
 */
exports.handleResponse = async (req, res) => {
  try {
    const salt = process.env.PAYU_SALT;
    const body = req.body;

    console.log('PayU Callback Received for txnid:', body.txnid, 'Status:', body.status);

    if (!salt) {
      console.error('PayU Salt missing on server callback');
      return res.redirect('/?payment=error&msg=Server+Salt+Missing');
    }

    const isVerified = verifyPayUResponseHash(body, salt);
    if (!isVerified) {
      console.warn('PayU Response hash mismatch for txnid:', body.txnid);
      return res.redirect('/?payment=failed&msg=Signature+Verification+Failed');
    }

    const orderId = body.udf1 || body.txnid?.replace(/^txn_/, '')?.split('_')?.[0];
    const isSuccess = body.status === 'success';

    if (orderId) {
      try {
        const order = await Order.findOne({
          $or: [{ orderId: orderId }, { id: orderId }]
        });

        if (order) {
          order.paymentStatus = isSuccess ? 'paid' : 'failed';
          order.paymentMethod = `PayU (${body.mode || 'Online'})`;
          order.paymentDetails = {
            payuMoneyId: body.payuMoneyId,
            mihpayid: body.mihpayid,
            bank_ref_num: body.bank_ref_num,
            mode: body.mode,
            status: body.status,
            txnid: body.txnid
          };

          if (isSuccess && order.statusCode === 'received') {
            order.statusCode = 'crafted';
            order.statusLabel = 'Crafting in Studio';
          }

          await order.save();
        }
      } catch (err) {
        console.error('Error updating order on PayU callback:', err);
      }
    }

    if (isSuccess) {
      return res.redirect(`/?payment=success&orderId=${encodeURIComponent(orderId || body.txnid)}&txnid=${encodeURIComponent(body.txnid)}`);
    } else {
      const errorMsg = body.error_Message || body.field9 || 'Payment was unsuccessful or cancelled.';
      return res.redirect(`/?payment=failed&orderId=${encodeURIComponent(orderId || body.txnid)}&msg=${encodeURIComponent(errorMsg)}`);
    }
  } catch (error) {
    console.error('PayU Response Handler Error:', error);
    return res.redirect('/?payment=error&msg=Processing+Error');
  }
};

/**
 * @desc    Get active payment gateway info
 * @route   GET /api/payu/config
 */
exports.getConfig = (req, res) => {
  const isPayuConfigured = Boolean(process.env.PAYU_KEY && process.env.PAYU_SALT);
  res.json({
    success: true,
    payu: {
      enabled: isPayuConfigured,
      key: process.env.PAYU_KEY || '',
      mode: process.env.PAYU_MODE || 'live'
    }
  });
};
