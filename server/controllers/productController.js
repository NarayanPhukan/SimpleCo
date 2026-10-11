const Product = require('../models/Product');

// @desc    Get all products with filtering, search, sort
// @route   GET /api/products
exports.getProducts = async (req, res) => {
  try {
    const { category, search, sort } = req.query;
    let query = {};

    if (category && category !== 'all') {
      query.category = category;
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
    const product = await Product.findOne({ id: req.params.id }) || await Product.findById(req.params.id);
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
      price,
      originalPrice,
      image,
      badge,
      description,
      leadTime,
      colors,
      inStock,
      specs
    } = req.body;

    if (!name || !price) {
      return res.status(400).json({ success: false, message: 'Name and price are required' });
    }

    const slug = name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)+/g, '') + '-' + Math.floor(100 + Math.random() * 900);

    const newProduct = await Product.create({
      id: slug,
      name,
      category: category || 'accessories',
      categoryName: categoryName || (category ? category.charAt(0).toUpperCase() + category.slice(1) : 'Handcrafted Accessories'),
      price: Number(price),
      originalPrice: originalPrice ? Number(originalPrice) : Math.round(Number(price) * 1.3),
      image: image || '/assets/images/strawberry-cow.jpg',
      badge: badge || 'Handcrafted',
      rating: 5.0,
      reviewsCount: 1,
      leadTime: leadTime || 'Ready to ship (1-2 days)',
      description: description || 'Artisanal crochet piece hand-stitched with premium milk cotton yarn.',
      specs: specs || { 'Yarn Type': '100% Milk Cotton', 'Craft Technique': 'Hand Crocheted' },
      colors: colors || [{ name: 'Original', hex: '#FF7B9C' }],
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
    const paramId = req.params.id;
    let product = await Product.findOne({ id: paramId }) || await Product.findById(paramId);
    if (!product) {
      return res.status(404).json({ success: false, message: 'Product not found' });
    }

    const updates = { ...req.body };
    if (updates.price) updates.price = Number(updates.price);
    if (updates.originalPrice) updates.originalPrice = Number(updates.originalPrice);

    if (product.save) {
      Object.assign(product, updates);
      await product.save();
    } else {
      product = await Product.findByIdAndUpdate(product._id || product.id, updates, { new: true });
    }

    res.json({ success: true, data: product });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Delete a product (Admin)
// @route   DELETE /api/products/:id
exports.deleteProduct = async (req, res) => {
  try {
    const paramId = req.params.id;
    const product = await Product.findOne({ id: paramId }) || await Product.findById(paramId);
    if (!product) {
      return res.status(404).json({ success: false, message: 'Product not found' });
    }

    await Product.findByIdAndDelete(product._id || product.id);
    res.json({ success: true, message: 'Product deleted successfully', id: paramId });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
