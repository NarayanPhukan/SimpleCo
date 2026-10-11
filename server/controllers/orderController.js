const Order = require('../models/Order');
const shiprocketService = require('../services/shiprocketService');

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
        desc: 'Artisan Priya is preparing her golden crochet hooks for your bespoke order.'
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
        desc: 'Carrier scan: BlueDart Express tracking will activate upon studio dispatch.'
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

    const order = await Order.create({
      orderId: newOrderId,
      userEmail: (userEmail || shippingAddress?.email || 'guest@babyygurll.in').trim().toLowerCase(),
      dateFormatted,
      statusStage: 0,
      statusLabel: 'Order Placed & Yarn Selected',
      statusCode: 'received',
      carrier: 'BlueDart Express India',
      trackingNumber,
      estimatedDelivery: estDate,
      shippingAddress,
      paymentMethod: paymentMethod || 'Razorpay Online',
      paymentStatus: paymentStatus || (paymentMethod?.toLowerCase().includes('cod') ? 'pending' : 'paid'),
      razorpayPaymentId: razorpayPaymentId || '',
      razorpayOrderId: razorpayOrderId || '',
      subtotal: totals.subtotal,
      discount: totals.discount || 0,
      shippingCost: totals.shipping || 0,
      tax: totals.tax || 0,
      total: totals.total,
      items,
      timeline
    });

    // Sync order dispatch with Shiprocket API
    shiprocketService.createShipmentOrder(order).catch(err => {
      console.warn('Shiprocket order sync note:', err.message);
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
    const userEmail = req.query.email ? req.query.email.trim().toLowerCase() : null;
    const query = (req.query.all === 'true' || !userEmail) ? {} : { userEmail };
    const orders = await Order.find(query).sort({ createdAt: -1 });
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

    const { statusCode, statusLabel, paymentStatus } = req.body;
    if (statusCode) order.statusCode = statusCode;
    if (statusLabel) order.statusLabel = statusLabel;
    if (paymentStatus) order.paymentStatus = paymentStatus;

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
          'Delivered with Love 🌸'
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
        'Delivered with Love 🌸'
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
    order.statusLabel = 'Return Requested • BlueDart Courier Pickup Scheduled';
    order.returnReason = reason || 'Customer requested return / exchange';
    order.returnDate = new Date().toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });
    order.returnStatus = 'Pickup Scheduled with BlueDart Express Courier';

    await order.save();
    res.json({ success: true, data: order });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
