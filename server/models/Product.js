const mongoose = require('mongoose');
const { getDBState } = require('../config/db');

const colorSchema = new mongoose.Schema({
  name: { type: String, required: true },
  hex: { type: String, required: true }
});

const productSchema = new mongoose.Schema({
  id: { type: String, unique: true },
  name: { type: String, required: true },
  category: { type: String, required: true, index: true },
  categoryName: { type: String, required: true },
  price: { type: Number, required: true },
  originalPrice: { type: Number },
  image: { type: String, required: true },
  badge: { type: String, default: 'Handcrafted' },
  rating: { type: Number, default: 5.0 },
  reviewsCount: { type: Number, default: 0 },
  leadTime: { type: String, default: 'Ready to ship (1-2 days)' },
  description: { type: String, required: true },
  specs: { type: mongoose.Schema.Types.Mixed, default: {} },
  colors: [colorSchema],
  inStock: { type: Boolean, default: true }
}, { timestamps: true });

const MongooseProduct = mongoose.model('Product', productSchema);

const Product = {
  find: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseProduct.find(query);
    return memoryDB.products.find(query);
  },
  findOne: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseProduct.findOne(query);
    return memoryDB.products.findOne(query);
  },
  findById: (id) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseProduct.findById(id);
    return memoryDB.products.findById(id);
  },
  create: (doc) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseProduct.create(doc);
    return memoryDB.products.create(doc);
  },
  insertMany: (docs) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseProduct.insertMany(docs);
    return memoryDB.products.insertMany(docs);
  },
  countDocuments: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseProduct.countDocuments(query);
    return memoryDB.products.countDocuments(query);
  },
  findByIdAndUpdate: (id, update, options) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseProduct.findByIdAndUpdate(id, update, options);
    return memoryDB.products.findByIdAndUpdate(id, update, options);
  },
  findByIdAndDelete: (id) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseProduct.findByIdAndDelete(id);
    return memoryDB.products.findByIdAndDelete(id);
  },
  findOneAndUpdate: (query, update, options) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseProduct.findOneAndUpdate(query, update, options);
    return memoryDB.products.findOneAndUpdate(query, update, options);
  },
  findOneAndDelete: (query) => {
    const { isConnected, memoryDB } = getDBState();
    if (isConnected) return MongooseProduct.findOneAndDelete(query);
    return memoryDB.products.findOneAndDelete(query);
  }
};

module.exports = Product;
