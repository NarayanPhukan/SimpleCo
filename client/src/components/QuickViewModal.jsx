import React, { useState, useEffect } from 'react';
import { useCart } from '../context/CartContext';
import { IconBag } from './Icons';

export const QuickViewModal = ({ product, onClose, onShowToast, onOpenCart }) => {
  const { formatPrice, addToCart } = useCart();
  const [selectedColor, setSelectedColor] = useState('');
  const [customNote, setCustomNote] = useState('');

  useEffect(() => {
    if (product && product.colors && product.colors.length > 0) {
      setSelectedColor(product.colors[0].name);
    }
    setCustomNote('');
  }, [product]);

  if (!product) return null;

  const handleAddToCart = () => {
    addToCart(product, selectedColor, 1, customNote);
    onShowToast(`Added ${product.name} (${selectedColor}) to bag`, '✓');
    onClose();
    onOpenCart();
  };

  return (
    <div className="modal-overlay active">
      <div className="modal-card quick-view-card">
        <button className="modal-close-btn" onClick={onClose}>&times;</button>
        
        <div className="qv-media">
          <img src={product.image} alt={product.name} />
        </div>

        <div className="qv-content">
          <span className="qv-badge">{product.badge}</span>
          <h3 className="qv-title">{product.name}</h3>

          <div className="qv-price-wrap">
            <span className="qv-price">{formatPrice(product.price)}</span>
            {product.originalPrice && (
              <span className="original-price" style={{ textDecoration: 'line-through', color: 'var(--text-light)' }}>
                {formatPrice(product.originalPrice)}
              </span>
            )}
          </div>

          <p className="qv-desc">{product.description}</p>

          <div className="qv-specs-box">
            <strong>Artisan Specifications:</strong><br />
            • Yarn: {product.specs?.yarn || '100% Milk Cotton'}<br />
            • Dimensions: {product.specs?.height || product.specs?.dimensions || 'Handmade scale'}<br />
            • Lead Time: {product.leadTime}<br />
            • Care: {product.specs?.care || 'Hand wash cold, air dry'}
          </div>

          <div className="color-picker-section">
            <h5>Select Color Palette:</h5>
            <div className="swatch-options">
              {product.colors && product.colors.map((c, i) => (
                <button
                  key={i}
                  className={`swatch-btn ${selectedColor === c.name ? 'selected' : ''}`}
                  onClick={() => setSelectedColor(c.name)}
                >
                  <span className="swatch-circle" style={{ backgroundColor: c.hex }} />
                  <span>{c.name}</span>
                </button>
              ))}
            </div>
          </div>

          <div className="form-group" style={{ marginBottom: '1.2rem' }}>
            <label className="form-label" htmlFor="qvNote">Custom Note / Gift Request (Optional):</label>
            <input 
              type="text" 
              id="qvNote" 
              className="form-input" 
              placeholder="e.g. Please add extra bow ribbon!" 
              value={customNote}
              onChange={(e) => setCustomNote(e.target.value)}
            />
          </div>

          <div className="qv-cta-row">
            <button className="btn btn-primary" onClick={handleAddToCart} style={{ flexGrow: 1, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}>
              <IconBag size={18} />
              <span>Add to Bag</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
