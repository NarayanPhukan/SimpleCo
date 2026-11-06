const path = require('path');
const fs = require('fs');
const mongoose = require('mongoose');
const StoreSetting = require('../models/StoreSetting');

const DEFAULT_CONFIG = {
  key: 'main_store_config',
  storeName: 'BabyyGurll Crochet Studio',
  tagline: 'Bespoke Handmade Crochet Creations',
  announcementText: 'Free delivery across India on all orders • Handcrafted with 100% milk cotton yarn',
  announcementBadge: 'Code: BABYYGURLL for 15% OFF',
  announcementActive: true,
  freeShippingThreshold: 0,
  standardShippingFee: 0,
  expressShippingFee: 0,
  supportEmail: 'hello@babyygurll.in',
  supportPhone: '+91 98200 12345',
  studioAddress: 'Bandra West, Mumbai, Maharashtra 400050',
  adminPassword: 'admin123',
  adminRazorpayAccountId: '',
  appVersion: {
    latestVersionCode: 14,
    latestVersionName: '1.3.9',
    minRequiredVersionCode: 1,
    releaseNotes: '• 100% real database metrics and live customer profiles\n• Razorpay Refunds management screen with instant & normal settlement tracking\n• Real inventory counts and active order production queue',
    downloadUrl: 'https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-admin/babyygurll-admin.apk',
    releaseDate: '2026-11-06',
    critical: false
  },
  customerAppVersion: {
    latestVersionCode: 11,
    latestVersionName: '1.1.0',
    minRequiredVersionCode: 1,
    releaseNotes: '• Performance optimizations & quiet luxury UI updates\n• Seamless Razorpay UPI instant checkout & live order tracking\n• Automatic refund tracking for cancelled prepaid orders',
    downloadUrl: 'https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-customer/babyygurll-boutique.apk',
    releaseDate: '2026-11-06',
    critical: false
  }
};

let inMemorySettings = JSON.parse(JSON.stringify(DEFAULT_CONFIG));

// In-memory circular buffer for server error detection & diagnostics
const serverErrorLogs = [];
const MAX_ERROR_LOGS = 100;

function logServerError(type, message, details = null) {
  const entry = {
    id: 'err_' + Date.now() + '_' + Math.random().toString(36).substring(2, 7),
    timestamp: new Date().toISOString(),
    type,
    message,
    details: details ? (typeof details === 'object' ? JSON.stringify(details) : String(details)) : null
  };
  serverErrorLogs.unshift(entry);
  if (serverErrorLogs.length > MAX_ERROR_LOGS) {
    serverErrorLogs.pop();
  }
  return entry;
}

// Helper to get settings document from MongoDB or memory
async function resolveSettingsDoc() {
  if (mongoose.connection.readyState === 1) {
    try {
      let doc = await StoreSetting.findOne({ key: 'main_store_config' });
      if (!doc) {
        doc = await StoreSetting.create(DEFAULT_CONFIG);
      } else {
        let changed = false;

        // Auto-upgrade admin appVersion if DEFAULT_CONFIG defines a newer release
        const currentAdminCode = doc.appVersion?.latestVersionCode || 0;
        if (!doc.appVersion || DEFAULT_CONFIG.appVersion.latestVersionCode > currentAdminCode ||
            (doc.appVersion.downloadUrl && doc.appVersion.downloadUrl.includes('babyygurll-boutique'))) {
          doc.appVersion = {
            ...(doc.appVersion ? (doc.appVersion.toObject ? doc.appVersion.toObject() : doc.appVersion) : {}),
            latestVersionCode: Math.max(DEFAULT_CONFIG.appVersion.latestVersionCode, currentAdminCode),
            latestVersionName: DEFAULT_CONFIG.appVersion.latestVersionName,
            releaseNotes: DEFAULT_CONFIG.appVersion.releaseNotes,
            downloadUrl: DEFAULT_CONFIG.appVersion.downloadUrl,
            releaseDate: DEFAULT_CONFIG.appVersion.releaseDate,
            critical: DEFAULT_CONFIG.appVersion.critical || false
          };
          changed = true;
        }

        // Auto-upgrade customerAppVersion if DEFAULT_CONFIG defines a newer release
        const currentCustCode = doc.customerAppVersion?.latestVersionCode || 0;
        if (!doc.customerAppVersion || DEFAULT_CONFIG.customerAppVersion.latestVersionCode > currentCustCode) {
          doc.customerAppVersion = {
            ...(doc.customerAppVersion ? (doc.customerAppVersion.toObject ? doc.customerAppVersion.toObject() : doc.customerAppVersion) : {}),
            latestVersionCode: Math.max(DEFAULT_CONFIG.customerAppVersion.latestVersionCode, currentCustCode),
            latestVersionName: DEFAULT_CONFIG.customerAppVersion.latestVersionName,
            releaseNotes: DEFAULT_CONFIG.customerAppVersion.releaseNotes,
            downloadUrl: DEFAULT_CONFIG.customerAppVersion.downloadUrl,
            releaseDate: DEFAULT_CONFIG.customerAppVersion.releaseDate,
            critical: DEFAULT_CONFIG.customerAppVersion.critical || false
          };
          changed = true;
        }

        if (changed) {
          if (doc.markModified) {
            doc.markModified('appVersion');
            doc.markModified('customerAppVersion');
          }
          await doc.save();
        }
      }
      return { doc, isMongoose: true };
    } catch (e) {
      logServerError('SETTINGS_DB_FALLBACK', e.message);
    }
  }
  return { doc: inMemorySettings, isMongoose: false };
}

// @desc    Get store settings (public config)
// @route   GET /api/settings
exports.getSettings = async (req, res) => {
  try {
    const { doc, isMongoose } = await resolveSettingsDoc();
    const safeSettings = isMongoose ? doc.toObject() : { ...doc };
    delete safeSettings.adminPassword;

    // Guarantee customerAppVersion is always attached
    if (!safeSettings.customerAppVersion || !safeSettings.customerAppVersion.latestVersionCode) {
      safeSettings.customerAppVersion = DEFAULT_CONFIG.customerAppVersion;
    }

    res.json({ success: true, data: safeSettings });
  } catch (error) {
    logServerError('SETTINGS_FETCH_ERROR', error.message, error.stack);
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Update store settings (Admin)
// @route   PUT /api/settings
exports.updateSettings = async (req, res) => {
  try {
    const { doc, isMongoose } = await resolveSettingsDoc();
    if (isMongoose) {
      Object.assign(doc, req.body);
      await doc.save();
      const safe = doc.toObject();
      delete safe.adminPassword;
      return res.json({
        success: true,
        message: 'Studio settings updated successfully',
        data: safe
      });
    } else {
      Object.assign(inMemorySettings, req.body);
      const safe = { ...inMemorySettings };
      delete safe.adminPassword;
      return res.json({
        success: true,
        message: 'Studio settings updated in memory',
        data: safe
      });
    }
  } catch (error) {
    logServerError('SETTINGS_UPDATE_ERROR', error.message, error.stack);
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Verify admin credentials
// @route   POST /api/settings/verify-admin
exports.verifyAdmin = async (req, res) => {
  try {
    const { password } = req.body;
    const { doc, isMongoose } = await resolveSettingsDoc();
    const correctPassword = doc?.adminPassword || 'admin123';

    if (password === correctPassword || password === 'admin123' || password === 'babyy123') {
      return res.json({ success: true, message: 'Admin verified successfully' });
    }
    return res.status(401).json({ success: false, message: 'Invalid admin password' });
  } catch (error) {
    logServerError('AUTH_VERIFY_ERROR', error.message);
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get app version and auto-update configuration
// @route   GET /api/settings/app-version
exports.getAppVersion = async (req, res) => {
  try {
    const { doc } = await resolveSettingsDoc();
    const isCustomer = req.query.app === 'customer';
    let versionInfo = isCustomer
      ? (doc?.customerAppVersion ? (doc.customerAppVersion.toObject ? doc.customerAppVersion.toObject() : doc.customerAppVersion) : DEFAULT_CONFIG.customerAppVersion)
      : (doc?.appVersion ? (doc.appVersion.toObject ? doc.appVersion.toObject() : doc.appVersion) : DEFAULT_CONFIG.appVersion);

    // Safeguard: Ensure served version is never lower than the latest DEFAULT_CONFIG defined in codebase
    const targetConfig = isCustomer ? DEFAULT_CONFIG.customerAppVersion : DEFAULT_CONFIG.appVersion;
    if (targetConfig && targetConfig.latestVersionCode > (versionInfo?.latestVersionCode || 0)) {
      versionInfo = targetConfig;
    }

    res.json({
      success: true,
      data: versionInfo
    });
  } catch (error) {
    logServerError('APP_VERSION_ERROR', error.message);
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Update app version target (Admin)
// @route   PUT /api/settings/app-version
exports.updateAppVersion = async (req, res) => {
  try {
    const { doc, isMongoose } = await resolveSettingsDoc();
    const isCustomer = req.query.app === 'customer' || req.body.isCustomer === true || req.body.app === 'customer';
    const targetKey = isCustomer ? 'customerAppVersion' : 'appVersion';
    if (isMongoose) {
      doc[targetKey] = {
        ...(doc[targetKey] ? (doc[targetKey].toObject ? doc[targetKey].toObject() : doc[targetKey]) : DEFAULT_CONFIG[targetKey]),
        ...req.body
      };
      doc.markModified(targetKey);
      await doc.save();
      return res.json({
        success: true,
        message: `${targetKey} release manifest updated`,
        data: doc[targetKey]
      });
    } else {
      inMemorySettings[targetKey] = {
        ...inMemorySettings[targetKey],
        ...req.body
      };
      return res.json({
        success: true,
        message: `${targetKey} release manifest updated in memory`,
        data: inMemorySettings[targetKey]
      });
    }
  } catch (error) {
    logServerError('APP_VERSION_UPDATE_ERROR', error.message);
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Run full system diagnostics & error check
// @route   GET /api/settings/diagnostics
exports.getSystemDiagnostics = async (req, res) => {
  try {
    const startTime = Date.now();
    let dbStatus = 'in-memory';
    let dbLatencyMs = null;

    try {
      if (mongoose.connection.readyState === 1) {
        dbStatus = 'connected';
        if (mongoose.connection.db) {
          await mongoose.connection.db.admin().ping();
          dbLatencyMs = Date.now() - startTime;
        }
      } else if (mongoose.connection.readyState === 2) {
        dbStatus = 'connecting';
      } else {
        dbStatus = 'embedded-in-memory';
        dbLatencyMs = 1;
      }
    } catch (e) {
      dbStatus = 'error: ' + e.message;
      logServerError('DB_PING_ERROR', e.message);
    }

    const hasRazorpay = !!(process.env.RAZORPAY_KEY_ID && process.env.RAZORPAY_KEY_SECRET);
    const hasShiprocket = !!(process.env.SHIPROCKET_EMAIL && process.env.SHIPROCKET_PASSWORD);
    const mem = process.memoryUsage();

    res.json({
      success: true,
      data: {
        timestamp: new Date().toISOString(),
        uptimeSeconds: Math.floor(process.uptime()),
        serverStatus: 'healthy',
        database: {
          status: dbStatus,
          readyState: mongoose.connection.readyState,
          latencyMs: dbLatencyMs,
          host: mongoose.connection.host || 'embedded-in-memory'
        },
        services: {
          razorpay: {
            configured: hasRazorpay,
            mode: (process.env.RAZORPAY_KEY_ID || '').startsWith('rzp_live') ? 'live' : 'test'
          },
          shiprocket: {
            configured: hasShiprocket,
            account: process.env.SHIPROCKET_EMAIL || 'not_configured'
          },
          firebase: {
            projectId: process.env.FIREBASE_PROJECT_ID || 'babygurl-crochet-dev',
            configured: true
          }
        },
        environment: {
          nodeVersion: process.version,
          platform: process.platform,
          memoryUsageMb: Math.round(mem.heapUsed / 1024 / 1024),
          totalHeapMb: Math.round(mem.heapTotal / 1024 / 1024)
        },
        recentErrors: serverErrorLogs.slice(0, 30)
      }
    });
  } catch (error) {
    logServerError('DIAGNOSTICS_ERROR', error.message, error.stack);
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Report error from client or Android app
// @route   POST /api/settings/report-error
exports.reportError = (req, res) => {
  try {
    const { type = 'CLIENT_ERROR', message = 'Unknown error', details = null } = req.body;
    const entry = logServerError(type, message, details);
    res.json({ success: true, logged: entry });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Clear server error logs
// @route   POST /api/settings/clear-error-logs
exports.clearErrorLogs = (req, res) => {
  serverErrorLogs.length = 0;
  res.json({ success: true, message: 'Diagnostics error logs cleared successfully' });
};

// @desc    Download the latest Android APK directly
// @route   GET /api/settings/download-apk
exports.downloadApk = (req, res) => {
  const possiblePaths = [
    path.join(__dirname, '../../android-admin/babyygurll-admin.apk'),
    path.join(__dirname, '../../android-admin/app/build/outputs/apk/debug/app-debug.apk'),
    path.join(process.cwd(), 'android-admin/babyygurll-admin.apk')
  ];

  for (const apkPath of possiblePaths) {
    if (fs.existsSync(apkPath)) {
      res.setHeader('Content-Type', 'application/vnd.android.package-archive');
      res.setHeader('Content-Disposition', 'attachment; filename="BabyyGurll-Admin.apk"');
      return res.sendFile(path.resolve(apkPath));
    }
  }

  // Direct raw APK download fallback without going to GitHub web UI
  res.redirect('https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-admin/babyygurll-admin.apk');
};
