const express = require('express');
const router = express.Router();
const {
  createOrder,
  getOrders,
  getOrderById,
  advanceOrderStage,
  updateOrderStatus,
  requestOrderReturn,
  cancelOrder,
  deleteOrder,
  syncShiprocket
} = require('../controllers/orderController');

router.post('/', createOrder);
router.get('/', getOrders);
router.get('/:orderId', getOrderById);

// Support both POST and PATCH for advance stage
router.patch('/:orderId/advance', advanceOrderStage);
router.post('/:orderId/advance', advanceOrderStage);

// Support PUT, PATCH, and POST for order status & tracking updates
router.patch('/:orderId/status', updateOrderStatus);
router.put('/:orderId/status', updateOrderStatus);
router.patch('/:orderId/tracking', updateOrderStatus);
router.put('/:orderId/tracking', updateOrderStatus);
router.post('/:orderId/tracking', updateOrderStatus);

router.post('/:orderId/sync-shiprocket', syncShiprocket);
router.post('/:orderId/return', requestOrderReturn);
router.post('/:orderId/cancel', cancelOrder);
router.delete('/:orderId', deleteOrder);

module.exports = router;
