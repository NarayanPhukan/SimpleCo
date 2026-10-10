const express = require('express');
const router = express.Router();
const {
  createOrder,
  getOrders,
  getOrderById,
  advanceOrderStage,
  requestOrderReturn
} = require('../controllers/orderController');

router.post('/', createOrder);
router.get('/', getOrders);
router.get('/:orderId', getOrderById);
router.patch('/:orderId/advance', advanceOrderStage);
router.post('/:orderId/return', requestOrderReturn);

module.exports = router;
