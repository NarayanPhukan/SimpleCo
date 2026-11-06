const express = require('express');
const cors = require('cors');
const path = require('path');
require('dotenv').config();

const { connectDB } = require('./config/db');
const productRoutes = require('./routes/productRoutes');
const authRoutes = require('./routes/authRoutes');
const orderRoutes = require('./routes/orderRoutes');
const shippingRoutes = require('./routes/shippingRoutes');
const paymentRoutes = require('./routes/paymentRoutes');
const couponRoutes = require('./routes/couponRoutes');
const settingRoutes = require('./routes/settingRoutes');
const payuRoutes = require('./routes/payuRoutes');
const notificationRoutes = require('./routes/notificationRoutes');

const app = express();
const PORT = process.env.PORT || 5000;

// Connect to MongoDB
connectDB();

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Serve static images directly from assets
app.use('/assets', express.static(path.join(__dirname, '../assets')));
app.use('/assets', express.static(path.join(__dirname, '../client/public/assets')));
app.use('/assets/images', express.static(path.join(__dirname, '../assets/images')));
app.use('/assets/images', express.static(path.join(__dirname, '../client/public/assets/images')));

// Middleware: Ensure database is connected before handling any /api routes
app.use('/api', async (req, res, next) => {
  try {
    const mongoose = require('mongoose');
    if (mongoose.connection.readyState !== 1) {
      await connectDB();
    }
  } catch (err) {
    console.warn('[Database] Middleware connect error:', err.message);
  }
  next();
});

// API Routes
app.use('/api/products', productRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/orders', orderRoutes);
app.use('/api/shipping', shippingRoutes);
app.use('/api/coupons', couponRoutes);
app.use('/api/settings', settingRoutes);
app.use('/api/payu', payuRoutes);
app.use('/api/notifications', notificationRoutes);
app.use('/api', paymentRoutes);
app.use('/api/payment', paymentRoutes);

// Health Check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    message: 'BabyyGurll Crochet API is running with MongoDB & Express',
    timestamp: new Date().toISOString()
  });
});

// Direct Customer App APK Download Route
app.get('/download/app', (req, res) => {
  const fs = require('fs');
  const publicApk = path.join(__dirname, '../client/public/babyygurll-boutique.apk');
  const repoApk = path.join(__dirname, '../android-customer/babyygurll-boutique.apk');
  if (fs.existsSync(publicApk)) {
    return res.download(publicApk, 'babyygurll-boutique.apk');
  }
  if (fs.existsSync(repoApk)) {
    return res.download(repoApk, 'babyygurll-boutique.apk');
  }
  res.redirect('https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-customer/babyygurll-boutique.apk');
});

// Direct Admin App APK Download Route
app.get('/download/admin', (req, res) => {
  const fs = require('fs');
  const adminApk = path.join(__dirname, '../android-admin/babyygurll-admin.apk');
  if (fs.existsSync(adminApk)) {
    return res.download(adminApk, 'babyygurll-admin.apk');
  }
  res.redirect('https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-admin/babyygurll-admin.apk');
});

// Root API Welcome / Status
app.get('/', (req, res) => {
  const fs = require('fs');
  const distIndex = path.resolve(__dirname, '../client', 'dist', 'index.html');
  if (process.env.NODE_ENV === 'production' && !process.env.VERCEL && fs.existsSync(distIndex)) {
    return res.sendFile(distIndex);
  }
  res.json({
    status: 'ok',
    message: 'BabyyGurll Crochet Express & MongoDB API Server',
    health: '/api/health',
    products: '/api/products',
    orders: '/api/orders'
  });
});

// Serve frontend in production if built (for traditional monolithic VPS / Render hosting)
const distPath = path.join(__dirname, '../client/dist');
const fs = require('fs');
if (process.env.NODE_ENV === 'production' && !process.env.VERCEL && fs.existsSync(distPath)) {
  app.use(express.static(distPath));
  app.get('*', (req, res) => {
    const indexPath = path.resolve(distPath, 'index.html');
    if (fs.existsSync(indexPath)) {
      res.sendFile(indexPath);
    } else {
      res.json({
        status: 'ok',
        message: 'BabyyGurll Crochet Express API Server'
      });
    }
  });
}

// Start Server locally or on persistent hosts (Render / Railway / VPS)
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`[Server] BabyyGurll Express Server running on http://localhost:${PORT}`);
  });
}

module.exports = app;
