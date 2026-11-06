const mongoose = require('mongoose');
const { getDBState } = require('../config/db');

const colorSchema = new mongoose.Schema({
  name: { type: String, required: true },
  hex: { type: String, required: true }
});

const variantSchema = new mongoose.Schema({
  id: { type: String },
  name: { type: String, required: true },
  size: { type: String, default: '' },
  color: { type: String, default: '' },
  colorHex: { type: String, default: '' },
  price: { type: Number, required: true },
  originalPrice: { type: Number },
  stock: { type: Number, default: 5 },
  sku: { type: String, default: '' },
  image: { type: String, default: '' }
});

const productSchema = new mongoose.Schema({
  id: { type: String, unique: true },
  name: { type: String, required: true },
  category: { type: String, required: true, index: true },
  categoryName: { type: String, required: true },
  price: { type: Number, required: true },
  originalPrice: { type: Number },
  image: { type: String, required: true },
  images: [{ type: String }],
  badge: { type: String, default: 'Handcrafted' },
  rating: { type: Number, default: 5.0 },
  reviewsCount: { type: Number, default: 0 },
  leadTime: { type: String, default: 'Ready to ship (1-2 days)' },
  description: { type: String, required: true },
  specs: { type: mongoose.Schema.Types.Mixed, default: {} },
  colors: [colorSchema],
  sizes: [{ type: String }],
  variants: [variantSchema],
  inStock: { type: Boolean, default: true }
}, { timestamps: true });

const MongooseProduct = mongoose.model('Product', productSchema);

const isDbReady = () => mongoose.connection.readyState === 1 || getDBState().isConnected;

const Product = {
  find: (query) => {
    if (isDbReady()) return MongooseProduct.find(query);
    return getDBState().memoryDB.products.find(query);
  },
  findOne: (query) => {
    if (isDbReady()) return MongooseProduct.findOne(query);
    return getDBState().memoryDB.products.findOne(query);
  },
  findById: (id) => {
    if (isDbReady()) return MongooseProduct.findById(id);
    return getDBState().memoryDB.products.findById(id);
  },
  create: (doc) => {
    if (isDbReady()) return MongooseProduct.create(doc);
    return getDBState().memoryDB.products.create(doc);
  },
  insertMany: (docs) => {
    if (isDbReady()) return MongooseProduct.insertMany(docs);
    return getDBState().memoryDB.products.insertMany(docs);
  },
  countDocuments: (query) => {
    if (isDbReady()) return MongooseProduct.countDocuments(query);
    return getDBState().memoryDB.products.countDocuments(query);
  },
  findByIdAndUpdate: (id, update, options) => {
    if (isDbReady()) return MongooseProduct.findByIdAndUpdate(id, update, options);
    return getDBState().memoryDB.products.findByIdAndUpdate(id, update, options);
  },
  findByIdAndDelete: (id) => {
    if (isDbReady()) return MongooseProduct.findByIdAndDelete(id);
    return getDBState().memoryDB.products.findByIdAndDelete(id);
  },
  findOneAndUpdate: (query, update, options) => {
    if (isDbReady()) return MongooseProduct.findOneAndUpdate(query, update, options);
    return getDBState().memoryDB.products.findOneAndUpdate(query, update, options);
  },
  findOneAndDelete: (query) => {
    if (isDbReady()) return MongooseProduct.findOneAndDelete(query);
    return getDBState().memoryDB.products.findOneAndDelete(query);
  }
};

module.exports = Product;
