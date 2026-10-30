const mongoose = require('mongoose');
const { MemoryCollection } = require('./memoryStore');

let isConnected = false;
let memoryDB = {
  products: new MemoryCollection([]),
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
      return;
    } catch (err) {
      console.warn('[Database] Could not connect to MONGODB_URI:', err.message);
    }
  } else {
    try {
      await mongoose.connect('mongodb://127.0.0.1:27017/babyygurll', { serverSelectionTimeoutMS: 1500 });
      isConnected = true;
      console.log('[Database] Connected to local MongoDB on port 27017.');
      return;
    } catch (localErr) {
      // Local mongod not running
    }
  }

  // Fallback to embedded in-memory MongoDB store
  console.log('[Database] Operating in embedded in-memory MongoDB mode.');
  memoryDB.products = new MemoryCollection([]);
  memoryDB.users = new MemoryCollection([]);
  memoryDB.orders = new MemoryCollection([]);
  memoryDB.coupons = new MemoryCollection([]);
};

const seedAtlasDatabase = async () => {
  // Permanently disabled: The product catalog is managed exclusively by the store owner/admin.
  // Never re-seed deleted products!
  return;
};

const getDBState = () => ({
  isConnected,
  memoryDB
});

module.exports = { connectDB, getDBState };
