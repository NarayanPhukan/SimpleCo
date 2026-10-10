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
app.use('/api', paymentRoutes);
app.use('/api/payment', paymentRoutes);

// Health Check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    message: '🌸 BabyyGurll Crochet API is running with MongoDB & Express!',
    timestamp: new Date().toISOString()
  });
});

// Root API Welcome / Status
app.get('/', (req, res) => {
  if (process.env.NODE_ENV === 'production' && !process.env.VERCEL) {
    return res.sendFile(path.resolve(__dirname, '../client', 'dist', 'index.html'));
  }
  res.json({
    status: 'ok',
    message: '🌸 BabyyGurll Crochet Express & MongoDB API Server',
    health: '/api/health',
    products: '/api/products',
    orders: '/api/orders'
  });
});

// Serve frontend in production if built (for traditional monolithic VPS / Render hosting)
if (process.env.NODE_ENV === 'production' && !process.env.VERCEL) {
  app.use(express.static(path.join(__dirname, '../client/dist')));
  app.get('*', (req, res) => {
    res.sendFile(path.resolve(__dirname, '../client', 'dist', 'index.html'));
  });
}

// Start Server locally or on persistent hosts (Render / Railway / VPS)
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`✨ BabyyGurll Crochet Express Server running on http://localhost:${PORT}`);
  });
}

module.exports = app;
