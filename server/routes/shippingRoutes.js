const express = require('express');
const router = express.Router();
const shiprocketService = require('../services/shiprocketService');

// @desc    Check Shiprocket connection status
// @route   GET /api/shipping/status
router.get('/status', async (req, res) => {
  const token = await shiprocketService.getToken();
  res.json({
    connected: !!token,
    message: token ? '🌸 Shiprocket API Connected & Active' : 'Shiprocket offline fallback active (Awaiting API user email)',
    emailConfigured: !!process.env.SHIPROCKET_EMAIL,
    passwordConfigured: !!process.env.SHIPROCKET_PASSWORD
  });
});

// @desc    Check courier serviceability by Indian Pincode
// @route   POST /api/shipping/serviceability
router.post('/serviceability', async (req, res) => {
  const { pickupPostcode, deliveryPostcode, weightKg, cod } = req.body;
  const result = await shiprocketService.checkServiceability(pickupPostcode, deliveryPostcode, weightKg, cod);
  res.json(result);
});

// @desc    Track shipment by AWB number
// @route   GET /api/shipping/track/:awb
router.get('/track/:awb', async (req, res) => {
  const { awb } = req.params;
  const trackingData = await shiprocketService.trackByAWB(awb);
  if (!trackingData) {
    return res.status(404).json({
      success: false,
      message: `Tracking info for AWB ${awb} currently in transit with express delivery network.`
    });
  }
  res.json({ success: true, data: trackingData });
});

module.exports = router;
