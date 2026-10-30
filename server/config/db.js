const mongoose = require('mongoose');
const { MemoryCollection } = require('./memoryStore');
const { seedProducts } = require('../data/seedData');

let isConnected = false;
let memoryDB = {
  products: new MemoryCollection(seedProducts),
  users: new MemoryCollection([]),
  orders: new MemoryCollection([]),
  coupons: new MemoryCollection([])
};

const connectDB = async () => {
  if (isConnected && mongoose.connection.readyState === 1) {
    return;
  }
  const mongoUri = process.env.MONGODB_URI;

  if (mongoUri) {
    try {
      console.log(`Connecting to MongoDB Atlas at: ${mongoUri.replace(/:[^:@]+@/, ':****@')}...`);
      await mongoose.connect(mongoUri, { serverSelectionTimeoutMS: 5000 });
      isConnected = true;
      console.log('[Database] MongoDB Atlas connected successfully.');

      // Seed Atlas collections if empty or update catalog
      await seedAtlasDatabase();
      return;
    } catch (err) {
      console.warn('[Database] Could not connect to MONGODB_URI:', err.message);
    }
  } else {
    try {
      await mongoose.connect('mongodb://127.0.0.1:27017/babyygurll', { serverSelectionTimeoutMS: 1500 });
      isConnected = true;
      console.log('[Database] Connected to local MongoDB on port 27017.');
      await seedAtlasDatabase();
      return;
    } catch (localErr) {
      // Local mongod not running
    }
  }

  // Fallback to embedded in-memory MongoDB store
  console.log('[Database] Operating in embedded in-memory MongoDB mode.');
  memoryDB.products = new MemoryCollection(seedProducts);
  memoryDB.users = new MemoryCollection([]);
  memoryDB.orders = new MemoryCollection([]);
  memoryDB.coupons = new MemoryCollection([]);
  console.log(`[Database] Initialized ${seedProducts.length} crochet products in MongoDB memory store.`);
};

const seedAtlasDatabase = async () => {
  try {
    const Product = require('../models/Product');
    const StoreSetting = require('../models/StoreSetting');

    const settingDoc = await StoreSetting.findOne({ key: 'main_store_config' });
    if (settingDoc && settingDoc.hasSeededProducts) {
      // Products have already been seeded in the past. Never re-seed deleted products!
      return;
    }

    const productCount = await Product.countDocuments();
    if (productCount === 0) {
      console.log('[Database] One-time initial seeding of catalog products in MongoDB Atlas...');
      await Product.insertMany(seedProducts);
      console.log(`[Database] Seeded ${seedProducts.length} crochet products in Atlas.`);
    }

    // Permanently mark products as seeded so user-deleted products never come back
    await StoreSetting.updateOne(
      { key: 'main_store_config' },
      { $set: { hasSeededProducts: true } },
      { upsert: true }
    );
  } catch (err) {
    console.error('Error during Atlas database seeding:', err.message);
  }
};

const getDBState = () => ({
  isConnected,
  memoryDB
});

module.exports = { connectDB, getDBState };
