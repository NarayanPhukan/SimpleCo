const express = require('express');
const router = express.Router();
const {
  getSettings,
  updateSettings,
  verifyAdmin
} = require('../controllers/settingController');

router.get('/', getSettings);
router.put('/', updateSettings);
router.post('/verify-admin', verifyAdmin);

module.exports = router;
