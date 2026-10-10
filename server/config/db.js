const mongoose = require('mongoose');
const { MemoryCollection } = require('./memoryStore');
const { seedProducts } = require('../data/seedData');

let isConnected = false;
let memoryDB = {
  products: null,
  users: null,
  orders: null
};

const connectDB = async () => {
  const mongoUri = process.env.MONGODB_URI;

  if (mongoUri) {
    try {
      console.log(`Connecting to MongoDB Atlas at: ${mongoUri.replace(/:[^:@]+@/, ':****@')}...`);
      await mongoose.connect(mongoUri, { serverSelectionTimeoutMS: 5000 });
      isConnected = true;
      console.log('🌸 MongoDB Atlas Connected successfully!');

      // Seed Atlas collections if empty or update catalog
      await seedAtlasDatabase();
      return;
    } catch (err) {
      console.warn('⚠️ Could not connect to MONGODB_URI:', err.message);
    }
  } else {
    try {
      await mongoose.connect('mongodb://127.0.0.1:27017/babyygurll', { serverSelectionTimeoutMS: 1500 });
      isConnected = true;
      console.log('🌸 Connected to local MongoDB on port 27017!');
      await seedAtlasDatabase();
      return;
    } catch (localErr) {
      // Local mongod not running
    }
  }

  // Fallback to embedded in-memory MongoDB store
  console.log('🌸 Operating in embedded in-memory MongoDB mode (Zero-config out of the box)!');
  memoryDB.products = new MemoryCollection(seedProducts);
  memoryDB.users = new MemoryCollection([]);
  memoryDB.orders = new MemoryCollection([]);
  console.log(`🌱 Initialized ${seedProducts.length} crochet products in MongoDB memory store.`);
};

const seedAtlasDatabase = async () => {
  try {
    const Product = require('../models/Product');

    const productCount = await Product.countDocuments();
    if (productCount === 0) {
      console.log('🌱 Seeding initial products to MongoDB Atlas...');
      await Product.insertMany(seedProducts);
      console.log(`✅ Seeded ${seedProducts.length} crochet products in Atlas.`);
    }
  } catch (err) {
    console.error('Error during Atlas database seeding:', err.message);
  }
};

const getDBState = () => ({
  isConnected,
  memoryDB
});

module.exports = { connectDB, getDBState };
