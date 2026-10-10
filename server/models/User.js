const mongoose = require('mongoose');
const { getDBState } = require('../config/db');

const addressSchema = new mongoose.Schema({
  label: { type: String, default: 'Home' },
  recipientName: { type: String, required: true },
  phone: { type: String, required: true },
  street: { type: String, required: true },
  city: { type: String, required: true },
  state: { type: String, required: true },
  zip: { type: String, required: true },
  country: { type: String, default: 'United States' },
  isDefault: { type: Boolean, default: false }
}, { timestamps: true });

const userSchema = new mongoose.Schema({
  name: { type: String, required: true },
  email: { type: String, required: true, unique: true, lowercase: true, trim: true },
  phone: { type: String, default: '+1 (555) 234-5678' },
  password: { type: String, required: true },
  memberTier: { type: String, default: 'VIP Stitcher Club 🧶' },
  memberSince: { type: String, default: 'October 2026' },
  savedAddresses: [addressSchema]
}, { timestamps: true });

const MongooseUser = mongoose.model('User', userSchema);

const User = {
  find: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseUser.find(query);
    return memoryDB.users.find(query);
  },
  findOne: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseUser.findOne(query);
    return memoryDB.users.findOne(query);
  },
  findById: (id) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseUser.findById(id);
    return memoryDB.users.findById(id);
  },
  create: (doc) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseUser.create(doc);
    return memoryDB.users.create(doc);
  },
  countDocuments: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseUser.countDocuments(query);
    return memoryDB.users.countDocuments(query);
  }
};

module.exports = User;
