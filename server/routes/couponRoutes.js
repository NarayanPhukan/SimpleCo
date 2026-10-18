const express = require('express');
const router = express.Router();
const {
  getCoupons,
  validateCoupon,
  createCoupon,
  updateCoupon,
  deleteCoupon,
  resetDefaultCoupons
} = require('../controllers/couponController');

router.get('/', getCoupons);
router.post('/validate', validateCoupon);
router.post('/reset-defaults', resetDefaultCoupons);
router.post('/', createCoupon);
router.put('/:id', updateCoupon);
router.delete('/:id', deleteCoupon);

module.exports = router;
