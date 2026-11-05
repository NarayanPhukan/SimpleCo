const express = require('express');
const router = express.Router();
const {
  initiatePayment,
  handleResponse,
  getConfig
} = require('../controllers/payuController');

router.post('/initiate', initiatePayment);
router.post('/response', handleResponse);
router.get('/config', getConfig);

module.exports = router;
