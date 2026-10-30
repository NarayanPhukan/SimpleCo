const express = require('express');
const router = express.Router();
const {
  getSettings,
  updateSettings,
  verifyAdmin,
  getAppVersion,
  updateAppVersion,
  getSystemDiagnostics,
  reportError,
  clearErrorLogs
} = require('../controllers/settingController');

router.get('/', getSettings);
router.put('/', updateSettings);
router.post('/verify-admin', verifyAdmin);

// Auto-Update Detection
router.get('/app-version', getAppVersion);
router.put('/app-version', updateAppVersion);

// Error Detection & System Diagnostics
router.get('/diagnostics', getSystemDiagnostics);
router.post('/report-error', reportError);
router.post('/clear-error-logs', clearErrorLogs);

module.exports = router;
