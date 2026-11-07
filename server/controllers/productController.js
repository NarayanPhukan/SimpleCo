const mongoose = require('mongoose');
const Product = require('../models/Product');
const { generateAutomatedTags } = require('../utils/tagAutomation');

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
        { categoryName: { $regex: q, $options: 'i' } },
        { subcategoryName: { $regex: q, $options: 'i' } },
        { tags: { $regex: q, $options: 'i' } }
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
      tags,
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

    let finalTags = [];
    if (Array.isArray(tags)) {
      finalTags = tags.map(t => String(t).trim().toLowerCase()).filter(Boolean);
    } else if (typeof tags === 'string' && tags.trim()) {
      finalTags = tags.split(',').map(t => t.trim().toLowerCase()).filter(Boolean);
    }
    const autoTags = generateAutomatedTags({
      category,
      categoryName,
      subcategory,
      subcategoryName,
      name,
      description
    });
    finalTags = Array.from(new Set([...finalTags, ...autoTags])).slice(0, 15);

    // Generate or lock product code (SKU) - permanent once generated
    let initialSku = (req.body.sku || '').trim();
    if (!initialSku) {
      const cat = (category || '').toLowerCase();
      const catPrefix = cat.includes('bag') ? 'BGB-OCB'
        : cat.includes('plush') ? 'BGB-PLS'
        : cat.includes('floral') ? 'BGB-FLR'
        : cat.includes('apparel') ? 'BGB-CKC'
        : 'BGB-PWS';
      initialSku = `${catPrefix}-0${Math.floor(10 + Math.random() * 90)}`;
    }

    const newProduct = await Product.create({
      id: slug,
      sku: initialSku,
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
      tags: finalTags,
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

    if (updates.tags !== undefined) {
      if (Array.isArray(updates.tags)) {
        updates.tags = updates.tags.map(t => String(t).trim().toLowerCase()).filter(Boolean);
      } else if (typeof updates.tags === 'string') {
        updates.tags = updates.tags.split(',').map(t => t.trim().toLowerCase()).filter(Boolean);
      }
    }
    if (!updates.tags || updates.tags.length === 0) {
      const autoTags = generateAutomatedTags({
        category: updates.category || product.category,
        categoryName: updates.categoryName || product.categoryName,
        subcategory: updates.subcategory || product.subcategory,
        subcategoryName: updates.subcategoryName || product.subcategoryName,
        name: updates.name || product.name,
        description: updates.description || product.description
      });
      updates.tags = Array.from(new Set([...(product.tags || []), ...autoTags])).slice(0, 15);
    }

    // Do not overwrite immutable MongoDB identifiers, version keys, or SKU
    delete updates._id;
    delete updates.__v;
    delete updates.id;
    // Product code / SKU is permanently fixed once generated/created and can never be changed
    delete updates.sku;

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
