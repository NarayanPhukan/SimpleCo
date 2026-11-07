const express = require('express');
const router = express.Router();
const {
  getProducts,
  getProductById,
  createProduct,
  updateProduct,
  deleteProduct,
  validateSku
} = require('../controllers/productController');

router.get('/', getProducts);
router.get('/validate-sku', validateSku);
router.post('/', createProduct);
router.get('/:id', getProductById);
router.put('/:id', updateProduct);
router.delete('/:id', deleteProduct);

module.exports = router;
