const mongoose = require('mongoose');
const Product = require('../models/Product');

// Helper to construct robust query matching by slug ID or MongoDB _id
const buildProductQuery = (rawId) => {
  if (!rawId) return { id: '__none__' };
  const paramId = decodeURIComponent(rawId).trim();
  const conditions = [{ id: paramId }, { id: rawId }];
  try {
    const mg = mongoose || require('mongoose');
    if (mg && mg.Types && mg.Types.ObjectId.isValid(paramId)) {
      try {
        conditions.push({ _id: new mg.Types.ObjectId(paramId) });
      } catch (e) {}
      conditions.push({ _id: paramId });
    }
  } catch (err) {
    if (/^[0-9a-fA-F]{24}$/.test(paramId)) {
      conditions.push({ _id: paramId });
    }
  }
  return { $or: conditions };
};

// @desc    Get all products with filtering, search, sort
// @route   GET /api/products
exports.getProducts = async (req, res) => {
  try {
    const { category, subcategory, search, sort } = req.query;
    let query = {};

    if (category && category !== 'all') {
      query.category = category;
    }

    if (subcategory && subcategory !== 'all') {
      query.subcategory = subcategory;
    }

    if (search && search.trim()) {
      const q = search.trim();
      query.$or = [
        { name: { $regex: q, $options: 'i' } },
        { description: { $regex: q, $options: 'i' } },
        { categoryName: { $regex: q, $options: 'i' } }
      ];
    }

    let sortOptions = {};
    if (sort === 'price-asc') {
      sortOptions.price = 1;
    } else if (sort === 'price-desc') {
      sortOptions.price = -1;
    } else if (sort === 'rating') {
      sortOptions.rating = -1;
    }

    const products = await Product.find(query).sort(sortOptions);
    res.json({ success: true, count: products.length, data: products });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Get single product by ID
// @route   GET /api/products/:id
exports.getProductById = async (req, res) => {
  try {
    const query = buildProductQuery(req.params.id);
    const product = await Product.findOne(query);
    if (!product) {
      return res.status(404).json({ success: false, message: 'Product not found' });
    }
    res.json({ success: true, data: product });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Create a new product (Admin)
// @route   POST /api/products
exports.createProduct = async (req, res) => {
  try {
    const {
      name,
      category,
      categoryName,
      subcategory,
      subcategoryName,
      price,
      originalPrice,
      image,
      images,
      badge,
      description,
      leadTime,
      colors,
      sizes,
      variants,
      inStock,
      specs
    } = req.body;

    if (!name || !price) {
      return res.status(400).json({ success: false, message: 'Name and price are required' });
    }

    const imagesList = Array.isArray(images) && images.length > 0 
      ? images 
      : (image ? [image] : []);
    const validImages = imagesList.filter(img => img && img.trim().length > 0);
    const nonMockImage = validImages.find(img => !img.includes('/assets/images/'));
    const primaryImage = nonMockImage || validImages[0] || (image && !image.includes('/assets/images/') ? image : '') || '';

    const slug = name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)+/g, '') + '-' + Math.floor(100 + Math.random() * 900);

    const newProduct = await Product.create({
      id: slug,
      name,
      category: category || 'accessories',
      categoryName: categoryName || (category ? category.charAt(0).toUpperCase() + category.slice(1) : 'Handcrafted Accessories'),
      subcategory: subcategory || '',
      subcategoryName: subcategoryName || '',
      price: Number(price),
      originalPrice: originalPrice ? Number(originalPrice) : undefined,
      image: primaryImage,
      images: validImages.length > 0 ? validImages : (primaryImage ? [primaryImage] : []),
      badge: badge || '',
      rating: 5.0,
      reviewsCount: 0,
      leadTime: leadTime || '',
      description: description || '',
      specs: specs || {},
      colors: Array.isArray(colors) ? colors : [],
      sizes: Array.isArray(sizes) ? sizes : [],
      variants: Array.isArray(variants) ? variants : [],
      inStock: inStock !== undefined ? Boolean(inStock) : true
    });

    res.status(201).json({ success: true, data: newProduct });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Update an existing product (Admin)
// @route   PUT /api/products/:id
exports.updateProduct = async (req, res) => {
  try {
    const query = buildProductQuery(req.params.id);
    let product = await Product.findOne(query);
    if (!product) {
      return res.status(404).json({ success: false, message: 'Product not found' });
    }

    const updates = { ...req.body };
    if (updates.price !== undefined) updates.price = Number(updates.price);
    if (updates.originalPrice !== undefined) updates.originalPrice = Number(updates.originalPrice);
    if (updates.images && Array.isArray(updates.images) && updates.images.length > 0) {
      const nonMockImage = updates.images.find(img => !img.includes('/assets/images/'));
      if (nonMockImage && (!updates.image || updates.image.includes('/assets/images/'))) {
        updates.image = nonMockImage;
      } else if (!updates.image) {
        updates.image = updates.images[0];
      }
    }

    // Do not overwrite immutable MongoDB identifiers or version keys
    delete updates._id;
    delete updates.__v;
    if (product.id && updates.id === product.id) {
      delete updates.id;
    }

    const updated = await Product.findOneAndUpdate(query, { $set: updates }, { new: true });
    res.json({ success: true, data: updated || product });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Delete a product (Admin)
// @route   DELETE /api/products/:id
exports.deleteProduct = async (req, res) => {
  try {
    const query = buildProductQuery(req.params.id);
    const deleted = await Product.findOneAndDelete(query);
    if (!deleted) {
      return res.status(404).json({ success: false, message: 'Product not found' });
    }

    // Permanently mark database as seeded so deleted dummy products never re-seed
    const StoreSetting = require('../models/StoreSetting');
    await StoreSetting.updateOne(
      { key: 'main_store_config' },
      { $set: { hasSeededProducts: true } },
      { upsert: true }
    );

    res.json({ success: true, message: 'Product deleted permanently', id: req.params.id });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
