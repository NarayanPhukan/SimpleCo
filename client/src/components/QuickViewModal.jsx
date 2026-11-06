import React, { useState, useEffect } from 'react';
import { useCart } from '../context/CartContext';
import { IconBag, IconTruck, IconCheck, IconPlus, IconSparkles } from './Icons';

// Curated Bundle Matches
const BUNDLE_COMPANIONS = {
  plushies: {
    id: 'bgc-charm-bundle',
    name: 'Mini Sweet Berry Charm Keychain',
    price: 349,
    originalPrice: 449,
    color: 'Berry Red',
    image: '/assets/images/strawberry-keychain.jpg',
    categoryName: 'Accessories'
  },
  floral: {
    id: 'bgc-coasters-bundle',
    name: 'Pastel Blossom Coaster Set (2 pcs)',
    price: 299,
    originalPrice: 399,
    color: 'Blossom Mix',
    image: '/assets/images/crochet-coasters.jpg',
    categoryName: 'Home'
  },
  default: {
    id: 'bgc-charm-bundle',
    name: 'Mini Sweet Berry Charm Keychain',
    price: 349,
    originalPrice: 449,
    color: 'Berry Red',
    image: '/assets/images/strawberry-keychain.jpg',
    categoryName: 'Accessories'
  }
};

export const QuickViewModal = ({ product, onClose, onShowToast, onOpenCart }) => {
  const { formatPrice, addToCart, addRecentlyViewed } = useCart();
  const [selectedColor, setSelectedColor] = useState('');
  const [customNote, setCustomNote] = useState('');
  const [activeImageIdx, setActiveImageIdx] = useState(0);

  // Compute all available product images for slideshow
  const allImages = React.useMemo(() => {
    if (!product) return [];
    const list = [];
    if (product.image && !product.image.startsWith('/assets/images/granny-bag.jpg')) {
      list.push(product.image);
    }
    if (Array.isArray(product.images)) {
      product.images.forEach(img => {
        if (img && !img.startsWith('/assets/images/granny-bag.jpg') && !list.includes(img)) {
          list.push(img);
        }
      });
    }
    if (list.length === 0 && product.image) list.push(product.image);
    return list.length > 0 ? list : ['/assets/images/placeholder.jpg'];
  }, [product]);

  // PIN Code Delivery Checker State
  const [pinCode, setPinCode] = useState(() => {
    const saved = localStorage.getItem('bgc_user_pincode');
    return (saved && saved !== '560001') ? saved : '';
  });
  const [pinResult, setPinResult] = useState(null);
  const [pinLoading, setPinLoading] = useState(false);

  useEffect(() => {
    if (product) {
      setActiveImageIdx(0);
      if (product.colors && product.colors.length > 0) {
        setSelectedColor(product.colors[0].name);
      }
      setCustomNote('');
      // Track into recently viewed
      addRecentlyViewed(product);
    }
  }, [product]);

  if (!product) return null;

  // Determine bundle companion based on product category
  const companion = (product.category === 'floral' ? BUNDLE_COMPANIONS.floral : BUNDLE_COMPANIONS.plushies);
  const bundleSavings = 100;
  const bundleTotalPrice = (product.price + companion.price) - bundleSavings;

  const handleAddToCart = () => {
    addToCart(product, selectedColor, 1, customNote);
    onShowToast(`Added ${product.name} (${selectedColor}) to bag`, '✓');
    onClose();
    if (onOpenCart) onOpenCart();
  };

  const handleAddBundleToCart = () => {
    // Add primary product
    addToCart(product, selectedColor, 1, customNote);
    // Add companion with bundle discount
    addToCart({
      id: companion.id,
      name: `${companion.name} (Bundle Deal)`,
      price: companion.price - bundleSavings,
      image: companion.image,
      categoryName: companion.categoryName
    }, companion.color, 1, 'Frequently Bought Together Bundle Savings Applied');

    onShowToast(`Bundle added! Saved ${formatPrice(bundleSavings)} 🎉`, '✓');
    onClose();
    if (onOpenCart) onOpenCart();
  };

  const handleCheckPincode = (e) => {
    e.preventDefault();
    if (!/^\d{6}$/.test(pinCode.trim())) {
      setPinResult({
        success: false,
        message: 'Please enter a valid 6-digit Indian PIN code.'
      });
      return;
    }
    setPinLoading(true);
    localStorage.setItem('bgc_user_pincode', pinCode.trim());

    // Intelligent estimate calculation
    setTimeout(() => {
      const today = new Date();
      const deliveryDate = new Date(today);
      deliveryDate.setDate(today.getDate() + 3);

      const formattedDate = deliveryDate.toLocaleDateString('en-IN', {
        weekday: 'short',
        month: 'short',
        day: 'numeric'
      });

      setPinResult({
        success: true,
        date: formattedDate,
        courier: 'Shiprocket Air Express',
        codAvailable: true,
        freeShip: product.price >= 999
      });
      setPinLoading(false);
    }, 350);
  };

  return (
    <div 
      className="modal-overlay active" 
      onClick={(e) => {
        if (e.target.classList.contains('modal-overlay')) onClose();
      }}
    >
      <div className="modal-card quick-view-card" role="dialog" aria-modal="true" aria-label={product.name}>
        <button className="modal-close-btn" onClick={onClose} aria-label="Close modal">&times;</button>
        
        <div className="qv-media" style={{ position: 'relative' }}>
          <img src={allImages[activeImageIdx] || product.image} alt={`${product.name} - slide ${activeImageIdx + 1}`} />
          {allImages.length > 1 && (
            <>
              <button
                type="button"
                className="qv-nav-btn qv-nav-prev"
                onClick={(e) => {
                  e.stopPropagation();
                  setActiveImageIdx(prev => (prev === 0 ? allImages.length - 1 : prev - 1));
                }}
                aria-label="Previous slide"
              >
                ‹
              </button>
              <button
                type="button"
                className="qv-nav-btn qv-nav-next"
                onClick={(e) => {
                  e.stopPropagation();
                  setActiveImageIdx(prev => (prev === allImages.length - 1 ? 0 : prev + 1));
                }}
                aria-label="Next slide"
              >
                ›
              </button>
              <div className="qv-thumbs-row">
                {allImages.map((img, idx) => (
                  <button
                    key={idx}
                    type="button"
                    className={`qv-thumb-btn ${idx === activeImageIdx ? 'active' : ''}`}
                    onClick={(e) => {
                      e.stopPropagation();
                      setActiveImageIdx(idx);
                    }}
                    aria-label={`View photo ${idx + 1}`}
                  >
                    <img src={img} alt="" />
                  </button>
                ))}
              </div>
            </>
          )}
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
            <strong>Details & Care:</strong><br />
            • Yarn: {product.specs?.yarn || '100% Milk Cotton'}<br />
            • Size: {product.specs?.height || product.specs?.dimensions || 'Handmade scale'}<br />
            • Making time: {product.leadTime}<br />
            • Care: {product.specs?.care || 'Hand wash cold, air dry'}
          </div>

          {/* Color Swatch Picker */}
          <div className="color-picker-section">
            <h5>Choose your color:</h5>
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

          {/* PIN Code Delivery Estimator */}
          <div className="qv-pincode-box">
            <div className="pincode-header">
              <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}>
                <IconTruck size={16} />
                <strong>Check Pan-India Delivery:</strong>
              </span>
            </div>
            <form onSubmit={handleCheckPincode} className="pincode-input-row">
              <input
                type="text"
                maxLength={6}
                placeholder="Enter 6-digit PIN"
                value={pinCode}
                onChange={(e) => setPinCode(e.target.value)}
                className="pincode-input"
              />
              <button type="submit" className="pincode-btn" disabled={pinLoading}>
                {pinLoading ? 'Checking...' : 'Check'}
              </button>
            </form>

            {pinResult && pinResult.success && (
              <div className="pincode-result-success">
                <div style={{ color: 'var(--success)', display: 'flex', alignItems: 'center', gap: '0.35rem', fontWeight: 600 }}>
                  <IconCheck size={14} />
                  <span>Delivery to {pinCode} by <strong>{pinResult.date}</strong></span>
                </div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                  Via {pinResult.courier} • Prepaid via Razorpay • Free Nationwide Delivery
                </div>
              </div>
            )}
            {pinResult && !pinResult.success && (
              <div className="pincode-result-error" style={{ color: 'var(--danger)', fontSize: '0.8rem', marginTop: '0.3rem' }}>
                {pinResult.message}
              </div>
            )}
          </div>

          {/* Gift Note Input */}
          <div className="form-group" style={{ marginBottom: '1.2rem' }}>
            <label className="form-label" htmlFor="qvNote">Gift note or custom color request (Optional):</label>
            <input 
              type="text" 
              id="qvNote" 
              className="form-input" 
              placeholder="e.g. Can you add a handwritten birthday note?" 
              value={customNote}
              onChange={(e) => setCustomNote(e.target.value)}
            />
          </div>

          {/* Primary Add to Bag */}
          <div className="qv-cta-row">
            <button className="btn btn-primary" onClick={handleAddToCart} style={{ flexGrow: 1, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}>
              <IconBag size={18} />
              <span>Add to Bag • {formatPrice(product.price)}</span>
            </button>
          </div>

          {/* Frequently Bought Together Bundle */}
          <div className="qv-bundle-box">
            <div className="qv-bundle-header">
              <IconSparkles size={14} />
              <span>Frequently Bought Together</span>
              <span className="qv-bundle-badge">Save {formatPrice(bundleSavings)}</span>
            </div>

            <div className="qv-bundle-items">
              <div className="bundle-thumb-pair">
                <img src={product.image} alt={product.name} className="bundle-thumb" />
                <span className="bundle-plus-icon">+</span>
                <img src={companion.image} alt={companion.name} className="bundle-thumb" />
              </div>
              <div className="bundle-details">
                <div className="bundle-name">
                  <strong>{product.name}</strong> + {companion.name}
                </div>
                <div className="bundle-pricing">
                  <strong className="bundle-total">{formatPrice(bundleTotalPrice)}</strong>
                  <span className="bundle-original">{formatPrice(product.price + companion.price)}</span>
                </div>
              </div>
            </div>

            <button 
              className="btn btn-secondary btn-sm bundle-add-btn" 
              onClick={handleAddBundleToCart}
              style={{ width: '100%', marginTop: '0.75rem', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.4rem' }}
            >
              <IconPlus size={14} />
              <span>Add Both to Bag ({formatPrice(bundleTotalPrice)})</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
