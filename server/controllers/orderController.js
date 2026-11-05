const Order = require('../models/Order');
const shiprocketService = require('../services/shiprocketService');
const paymentController = require('./paymentController');

// @desc    Create new crochet order
// @route   POST /api/orders
exports.createOrder = async (req, res) => {
  try {
    const {
      userEmail,
      shippingAddress,
      paymentMethod,
      paymentStatus,
      razorpayPaymentId,
      razorpayOrderId,
      items,
      totals
    } = req.body;

    // Enforce Online Razorpay Payments: Reject any COD attempts
    if (paymentMethod && paymentMethod.toLowerCase().includes('cod')) {
      return res.status(400).json({
        success: false,
        message: 'Cash on Delivery (COD) is temporarily disabled. Please pay securely online via Razorpay.'
      });
    }

    const newOrderId = 'BGC-' + Math.floor(10000 + Math.random() * 90000);
    const trackingNumber = 'TRK-' + newOrderId + '-IN';
    const now = new Date();
    const dateFormatted = now.toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });
    const estDate = new Date(now.getTime() + 4 * 24 * 60 * 60 * 1000).toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });

    const timeline = [
      {
        stage: 'received',
        title: 'Order Placed & Yarn Selected',
        time: 'Just now (' + now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' }) + ')',
        completed: true,
        current: true,
        desc: 'Order confirmed! Artisanal milk cotton yarn skeins assigned to master crochet hook.'
      },
      {
        stage: 'crafted',
        title: 'Handmade Stitched with Love',
        time: 'Pending Crafting',
        completed: false,
        current: false,
        desc: 'Our master crafter is preparing the crochet hooks for your bespoke order.'
      },
      {
        stage: 'packed',
        title: 'Ribbon Wrapped & Sweet Scented',
        time: 'Pending Inspection',
        completed: false,
        current: false,
        desc: 'Will be checked for neat stitches, tied with satin ribbon and scented with sweet floral mist.'
      },
      {
        stage: 'shipped',
        title: 'Departed Studio & In Transit',
        time: 'Pending Pickup',
        completed: false,
        current: false,
        desc: 'Departed from studio: Tracking waybill will activate upon transit scan.'
      },
      {
        stage: 'delivered',
        title: 'Delivered to Doorstep',
        time: 'Est. ' + estDate,
        completed: false,
        current: false,
        desc: 'Artisan package will be gently delivered to your doorstep across India.'
      }
    ];

    const normalizedAddress = {
      recipientName: shippingAddress?.recipientName || shippingAddress?.name || 'Valued Customer',
      phone: shippingAddress?.phone || '+91 98200 00000',
      street: shippingAddress?.street || shippingAddress?.addressLine1 || shippingAddress?.address || 'Studio Address',
      city: shippingAddress?.city || 'Mumbai',
      state: shippingAddress?.state || 'Maharashtra',
      zip: String(shippingAddress?.zip || shippingAddress?.pincode || '400050'),
      country: shippingAddress?.country || 'India'
    };

    const normalizedItems = (items || []).map(it => ({
      id: it.id || it.productId || 'item-' + Math.random().toString(36).substring(2, 7),
      name: it.name || 'Handmade Crochet Piece',
      color: it.color || (it.selectedColor && it.selectedColor.name) || 'Pastel Harmony',
      quantity: Number(it.quantity) || 1,
      price: Number(it.price) || 0,
      image: it.image || '/assets/images/strawberry-cow.jpg',
      customNote: it.customNote || ''
    }));

    const order = await Order.create({
      orderId: newOrderId,
      userEmail: (userEmail || shippingAddress?.email || 'guest@babyygurll.in').trim().toLowerCase(),
      dateFormatted,
      statusStage: 0,
      statusLabel: 'Order Placed & Yarn Selected',
      statusCode: 'received',
      carrier: 'Express Delivery',
      trackingNumber,
      estimatedDelivery: estDate,
      shippingAddress: normalizedAddress,
      paymentMethod: paymentMethod || 'Razorpay Online',
      paymentStatus: paymentStatus || 'paid',
      razorpayPaymentId: razorpayPaymentId || '',
      razorpayOrderId: razorpayOrderId || '',
      subtotal: totals?.subtotal || 0,
      discount: totals?.discount || 0,
      shippingCost: totals?.shipping || 0,
      tax: totals?.tax || 0,
      total: totals?.total || 0,
      items: normalizedItems,
      timeline
    });

    // Sync order dispatch automatically with Shiprocket API
    shiprocketService.createShipmentOrder(order).then(async (srResult) => {
      try {
        if (srResult && (srResult.order_id || srResult.shipment_id)) {
          order.shiprocketOrderId = String(srResult.order_id || '');
          order.shiprocketShipmentId = String(srResult.shipment_id || '');
          order.shiprocketStatus = srResult.status || 'NEW';
          if (srResult.awb_code) {
            order.trackingNumber = srResult.awb_code;
          }
          await order.save();
          console.log(`[Shiprocket] Order #${order.orderId} successfully synced: SR Order ID ${order.shiprocketOrderId}, Shipment ID ${order.shiprocketShipmentId}`);
        } else if (srResult && srResult.message) {
          console.warn(`[Shiprocket] Notice for #${order.orderId}:`, srResult.message);
        }
      } catch (saveErr) {
        console.warn(`[Shiprocket] Failed to update order #${order.orderId} with Shiprocket info:`, saveErr.message);
      }
    }).catch(err => {
      console.warn('Shiprocket auto-creation note:', err.message);
    });

    res.status(201).json({ success: true, data: order });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get user's past orders or all orders for Admin
// @route   GET /api/orders
exports.getOrders = async (req, res) => {
  try {
    const rawEmail = req.query.email ? req.query.email.trim().toLowerCase() : null;
    if (req.query.all === 'true' || !rawEmail) {
      const orders = await Order.find({}).sort({ createdAt: -1 });
      return res.json({ success: true, count: orders.length, data: orders });
    }

    const emailPattern = new RegExp(`^${rawEmail.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}$`, 'i');
    const conditions = [
      { userEmail: emailPattern },
      { 'shippingAddress.email': emailPattern }
    ];

    // Check if the user exists and has a phone number to also match orders placed by phone
    try {
      const User = require('../models/User');
      const user = await User.findOne({ email: emailPattern });
      if (user && user.phone) {
        const rawPhone = user.phone.replace(/[^0-9]/g, '').slice(-10);
        if (rawPhone.length >= 10) {
          conditions.push({ 'shippingAddress.phone': { $regex: rawPhone } });
        }
      }
    } catch (uErr) {
      // Ignore user lookup error
    }

    // Special match for narayanphukan variants (e.g. narayanphukn30 typo)
    if (rawEmail.includes('narayanphuk')) {
      conditions.push({ userEmail: { $regex: 'narayanphuk', $options: 'i' } });
    }

    const orders = await Order.find({ $or: conditions }).sort({ createdAt: -1 });
    res.json({ success: true, count: orders.length, data: orders });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Update order status directly (Admin)
// @route   PATCH /api/orders/:orderId/status
exports.updateOrderStatus = async (req, res) => {
  try {
    const cleanId = req.params.orderId.trim().toUpperCase();
    const order = await Order.findOne({ orderId: cleanId });
    if (!order) {
      return res.status(404).json({ success: false, message: 'Order not found' });
    }

    const { statusCode, statusLabel, paymentStatus, carrier, trackingNumber, estimatedDelivery } = req.body;
    if (statusCode) order.statusCode = statusCode;
    if (statusLabel) order.statusLabel = statusLabel;
    if (paymentStatus) order.paymentStatus = paymentStatus;
    if (carrier) order.carrier = carrier;
    if (trackingNumber) order.trackingNumber = trackingNumber;
    if (estimatedDelivery) order.estimatedDelivery = estimatedDelivery;

    const stages = ['received', 'crafted', 'packed', 'shipped', 'delivered'];
    const stageIdx = stages.indexOf(statusCode);
    if (stageIdx !== -1) {
      order.statusStage = stageIdx;
      if (!statusLabel) {
        const labels = [
          'Order Placed & Yarn Selected',
          'Artisan Handcrafting in Progress',
          'Ribbon Wrapped & Sweet Scented',
          'In Transit with Courier',
          'Delivered to Doorstep'
        ];
        order.statusLabel = labels[stageIdx];
      }
      order.timeline.forEach((item, idx) => {
        if (idx < stageIdx) {
          item.completed = true;
          item.current = false;
        } else if (idx === stageIdx) {
          item.completed = true;
          item.current = true;
          item.time = 'Updated just now (' + new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ')';
        } else {
          item.completed = false;
          item.current = false;
        }
      });
    }

    await order.save();
    res.json({ success: true, data: order });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Delete an order (Admin)
// @route   DELETE /api/orders/:orderId
exports.deleteOrder = async (req, res) => {
  try {
    const rawId = (req.params.orderId || '').trim();
    const cleanId = rawId.toUpperCase();
    let order = await Order.findOneAndDelete({ orderId: cleanId });
    if (!order && rawId.length === 24) {
      order = await Order.findByIdAndDelete(rawId);
    }
    if (!order) {
      order = await Order.findOneAndDelete({ _id: rawId });
    }
    if (!order) {
      return res.status(404).json({ success: false, message: 'Order not found' });
    }
    res.json({ success: true, message: `Order #${order.orderId || cleanId} deleted successfully` });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get order by Order ID (for live order tracking)
// @route   GET /api/orders/:orderId
exports.getOrderById = async (req, res) => {
  try {
    const cleanId = req.params.orderId.trim().toUpperCase();
    const order = await Order.findOne({ orderId: cleanId });
    if (!order) {
      return res.status(404).json({ success: false, message: `Order #${cleanId} not found` });
    }
    res.json({ success: true, data: order });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Simulate advancing order stage (0 to 4)
// @route   PATCH /api/orders/:orderId/advance
exports.advanceOrderStage = async (req, res) => {
  try {
    const cleanId = req.params.orderId.trim().toUpperCase();
    const order = await Order.findOne({ orderId: cleanId });
    if (!order) {
      return res.status(404).json({ success: false, message: 'Order not found' });
    }

    if (order.statusStage < 4) {
      order.statusStage += 1;
      const stages = ['received', 'crafted', 'packed', 'shipped', 'delivered'];
      const labels = [
        'Order Placed & Yarn Selected',
        'Artisan Handcrafting in Progress',
        'Ribbon Wrapped & Sweet Scented',
        'In Transit with Courier',
        'Delivered to Doorstep'
      ];
      order.statusLabel = labels[order.statusStage];
      order.statusCode = stages[order.statusStage];

      order.timeline.forEach((item, idx) => {
        if (idx < order.statusStage) {
          item.completed = true;
          item.current = false;
        } else if (idx === order.statusStage) {
          item.completed = true;
          item.current = true;
          item.time = 'Updated just now (' + new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ')';
        } else {
          item.completed = false;
          item.current = false;
        }
      });

      await order.save();
    }

    res.json({ success: true, data: order });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Request return on an order
// @route   POST /api/orders/:orderId/return
exports.requestOrderReturn = async (req, res) => {
  try {
    const cleanId = req.params.orderId.trim().toUpperCase();
    const order = await Order.findOne({ orderId: cleanId });
    if (!order) {
      return res.status(404).json({ success: false, message: 'Order not found' });
    }

    const { reason } = req.body;
    order.statusCode = 'returned';
    order.statusLabel = 'Return Requested • Doorstep Pickup Scheduled';
    order.returnReason = reason || 'Customer requested return / exchange';
    order.returnDate = new Date().toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });
    order.returnStatus = 'Pickup Scheduled with Doorstep Courier';

    await order.save();
    res.json({ success: true, data: order });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Cancel an order (if in-progress or placed)
// @route   POST /api/orders/:orderId/cancel
exports.cancelOrder = async (req, res) => {
  try {
    const cleanId = req.params.orderId.trim().toUpperCase();
    const order = await Order.findOne({ orderId: cleanId });
    if (!order) {
      return res.status(404).json({ success: false, message: 'Order not found' });
    }

    if (order.statusCode === 'delivered') {
      return res.status(400).json({
        success: false,
        message: 'Delivered orders cannot be cancelled. You can request a return or exchange instead.'
      });
    }

    if (order.statusCode === 'cancelled') {
      return res.status(400).json({
        success: false,
        message: 'Order is already cancelled.'
      });
    }

    const { reason } = req.body;
    const cancelReason = (reason && reason.trim()) ? reason.trim() : 'Customer requested order cancellation';
    const now = new Date();
    const cancelDateFormatted = now.toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });
    const isPrepaid = order.paymentStatus === 'paid' || 
      (order.paymentMethod && order.paymentMethod.toLowerCase().includes('razorpay') && order.paymentStatus !== 'failed');

    let refundSuccess = false;
    let refundId = null;

    // Issue automatic Razorpay refund for prepaid orders
    if (isPrepaid) {
      try {
        let paymentId = order.razorpayPaymentId;
        if (!paymentId && order.razorpayOrderId) {
          paymentId = await paymentController.fetchPaymentForOrder(order.razorpayOrderId);
          if (paymentId) {
            order.razorpayPaymentId = paymentId;
          }
        }

        if (paymentId) {
          const amountInPaise = Math.round(order.total * 100);
          const refund = await paymentController.issueRefund(paymentId, amountInPaise, {
            orderId: cleanId,
            reason: cancelReason
          });
          refundId = refund.id;
          order.razorpayRefundId = refund.id;
          order.refundStatus = refund.status || 'processed';
          order.refundAmount = (refund.amount || amountInPaise) / 100;
          order.paymentStatus = 'refunded';
          refundSuccess = true;
        } else {
          order.paymentStatus = 'refund_pending';
          order.refundStatus = 'manual_review';
        }
      } catch (refundError) {
        console.error(`Razorpay Refund failed for Order #${cleanId}:`, refundError.message);
        order.paymentStatus = 'refund_pending';
        order.refundStatus = 'failed: ' + refundError.message;
      }
    } else {
      order.paymentStatus = 'cancelled';
    }

    order.statusCode = 'cancelled';
    order.statusLabel = refundSuccess
      ? 'Order Cancelled • Refund Processed'
      : (isPrepaid ? 'Order Cancelled • Refund Initiated' : 'Order Cancelled');
    order.cancelReason = cancelReason;
    order.cancelledAt = cancelDateFormatted;

    // Mark current timeline steps as completed/cancelled
    order.timeline.forEach(step => {
      step.current = false;
    });

    const refundDesc = refundSuccess
      ? `Cancellation confirmed: ${cancelReason}. Razorpay full refund of ₹${order.total} has been issued (Refund ID: ${refundId}) back to your original payment method / UPI.`
      : (isPrepaid
        ? `Cancellation confirmed: ${cancelReason}. Full refund of ₹${order.total} has been initiated back to your source account / UPI (settlement in 2-3 business days).`
        : `Cancellation confirmed: ${cancelReason}. As this was a Cash on Delivery order, no amount was charged.`);

    order.timeline.push({
      stage: 'cancelled',
      title: refundSuccess ? 'Order Cancelled & Refund Processed' : 'Order Cancelled & Refund Initiated',
      time: cancelDateFormatted + ' (' + now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' }) + ')',
      completed: true,
      current: true,
      desc: refundDesc
    });

    await order.save();

    res.json({
      success: true,
      message: `Order #${cleanId} has been successfully cancelled${refundSuccess ? ' and refund processed via Razorpay' : ''}`,
      data: order
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Manually push / sync order to Shiprocket
// @route   POST /api/orders/:orderId/sync-shiprocket
exports.syncShiprocket = async (req, res) => {
  try {
    const cleanId = req.params.orderId.trim().toUpperCase();
    const order = await Order.findOne({ orderId: cleanId });
    if (!order) {
      return res.status(404).json({ success: false, message: 'Order not found' });
    }

    const srResult = await shiprocketService.createShipmentOrder(order);
    if (srResult && (srResult.order_id || srResult.shipment_id)) {
      order.shiprocketOrderId = String(srResult.order_id || '');
      order.shiprocketShipmentId = String(srResult.shipment_id || '');
      order.shiprocketStatus = srResult.status || 'NEW';
      if (srResult.awb_code) {
        order.trackingNumber = srResult.awb_code;
      }
      await order.save();
      return res.json({
        success: true,
        message: `Order #${cleanId} synced to Shiprocket successfully!`,
        data: order,
        shiprocket: srResult
      });
    }

    return res.status(400).json({
      success: false,
      message: srResult?.message || srResult?.error || 'Shiprocket sync returned without order confirmation',
      details: srResult
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
