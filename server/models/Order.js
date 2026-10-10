const mongoose = require('mongoose');
const { getDBState } = require('../config/db');

const orderItemSchema = new mongoose.Schema({
  id: { type: String, required: true },
  name: { type: String, required: true },
  color: { type: String, required: true },
  quantity: { type: Number, required: true, min: 1 },
  price: { type: Number, required: true },
  image: { type: String, required: true },
  customNote: { type: String, default: '' }
});

const timelineStepSchema = new mongoose.Schema({
  stage: { type: String, required: true },
  title: { type: String, required: true },
  time: { type: String, required: true },
  completed: { type: Boolean, default: false },
  current: { type: Boolean, default: false },
  desc: { type: String, required: true }
});

const orderSchema = new mongoose.Schema({
  orderId: { type: String, required: true, unique: true, index: true },
  userEmail: { type: String, required: true, index: true },
  dateFormatted: { type: String, required: true },
  statusStage: { type: Number, default: 0, min: 0, max: 4 },
  statusLabel: { type: String, default: 'Order Placed & Yarn Selected' },
  statusCode: { type: String, default: 'received' },
  carrier: { type: String, default: 'CraftExpress Priority Courier' },
  trackingNumber: { type: String, required: true },
  estimatedDelivery: { type: String, required: true },
  shippingAddress: {
    recipientName: { type: String, required: true },
    phone: { type: String, required: true },
    street: { type: String, required: true },
    city: { type: String, required: true },
    state: { type: String, required: true },
    zip: { type: String, required: true },
    country: { type: String, default: 'United States' }
  },
  paymentMethod: { type: String, default: 'Visa ending in 4242' },
  subtotal: { type: Number, required: true },
  discount: { type: Number, default: 0 },
  shippingCost: { type: Number, default: 0 },
  tax: { type: Number, default: 0 },
  total: { type: Number, required: true },
  items: [orderItemSchema],
  timeline: [timelineStepSchema],
  returnReason: { type: String, default: '' },
  returnDate: { type: String, default: '' },
  returnStatus: { type: String, default: '' }
}, { timestamps: true });

const MongooseOrder = mongoose.model('Order', orderSchema);

const Order = {
  find: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseOrder.find(query);
    return memoryDB.orders.find(query);
  },
  findOne: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseOrder.findOne(query);
    return memoryDB.orders.findOne(query);
  },
  findById: (id) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseOrder.findById(id);
    return memoryDB.orders.findById(id);
  },
  create: (doc) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseOrder.create(doc);
    return memoryDB.orders.create(doc);
  },
  insertMany: (docs) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseOrder.insertMany(docs);
    return memoryDB.orders.insertMany(docs);
  },
  countDocuments: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseOrder.countDocuments(query);
    return memoryDB.orders.countDocuments(query);
  }
};

module.exports = Order;
