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
router.patch('/:orderId/advance', advanceOrderStage);
router.patch('/:orderId/status', updateOrderStatus);
router.post('/:orderId/sync-shiprocket', syncShiprocket);
router.post('/:orderId/return', requestOrderReturn);
router.post('/:orderId/cancel', cancelOrder);
router.delete('/:orderId', deleteOrder);

module.exports = router;
