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

const app = express();
const PORT = process.env.PORT || 5000;

// Connect to MongoDB
connectDB();

// Middleware
app.use(cors());
app.use(express.json());

// Serve static images directly from assets
app.use('/assets/images', express.static(path.join(__dirname, '../client/public/assets/images')));

// API Routes
app.use('/api/products', productRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/orders', orderRoutes);
app.use('/api/shipping', shippingRoutes);
app.use('/api/coupons', couponRoutes);
app.use('/api/settings', settingRoutes);
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
