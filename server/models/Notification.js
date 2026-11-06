const mongoose = require('mongoose');
const { getDBState } = require('../config/db');

const notificationSchema = new mongoose.Schema({
  recipient: { type: String, default: 'admin', index: true },
  role: { type: String, enum: ['customer', 'admin', 'all'], default: 'all', index: true },
  title: { type: String, required: true },
  message: { type: String, required: true },
  type: {
    type: String,
    enum: ['order', 'shipping', 'stock', 'custom_request', 'promotion', 'system', 'general'],
    default: 'general',
    index: true
  },
  data: { type: mongoose.Schema.Types.Mixed, default: {} },
  isRead: { type: Boolean, default: false, index: true },
  readAt: { type: Date, default: null },
  createdAt: { type: Date, default: Date.now, index: true }
});

const MongooseNotification = mongoose.models.Notification || mongoose.model('Notification', notificationSchema);

const Notification = {
  find: (query = {}) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.find(query);
    return memoryDB.notifications.find(query);
  },
  findOne: (query = {}) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.findOne(query);
    return memoryDB.notifications.findOne(query);
  },
  findById: (id) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.findById(id);
    return memoryDB.notifications.findById(id);
  },
  create: (docData) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.create(docData);
    return memoryDB.notifications.create(docData);
  },
  insertMany: (docs) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.insertMany(docs);
    return memoryDB.notifications.insertMany(docs);
  },
  countDocuments: (query = {}) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.countDocuments(query);
    return memoryDB.notifications.countDocuments(query);
  },
  findByIdAndUpdate: (id, update, options) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.findByIdAndUpdate(id, update, options);
    return memoryDB.notifications.findByIdAndUpdate(id, update, options);
  },
  findByIdAndDelete: (id) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.findByIdAndDelete(id);
    return memoryDB.notifications.findByIdAndDelete(id);
  },
  findOneAndUpdate: (query, update, options) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.findOneAndUpdate(query, update, options);
    return memoryDB.notifications.findOneAndUpdate(query, update, options);
  },
  findOneAndDelete: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.findOneAndDelete(query);
    return memoryDB.notifications.findOneAndDelete(query);
  },
  updateMany: (query, update, options) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.updateMany(query, update, options);
    return memoryDB.notifications.updateMany ? memoryDB.notifications.updateMany(query, update, options) : Promise.resolve({ modifiedCount: 0 });
  },
  deleteMany: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseNotification.deleteMany(query);
    return memoryDB.notifications.deleteMany ? memoryDB.notifications.deleteMany(query) : Promise.resolve({ deletedCount: 0 });
  }
};

module.exports = Notification;
