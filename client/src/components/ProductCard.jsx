import React, { useState } from 'react';
import { useCart } from '../context/CartContext';
import { IconHeart, IconEye, IconBag, IconCheck } from './Icons';

export const ProductCard = ({ product, onQuickView, onShowToast }) => {
  const { formatPrice, addToCart, isInWishlist, toggleWishlist } = useCart();
  const wishlisted = isInWishlist(product.id);
  
  const [selectedColor, setSelectedColor] = useState(
    product.colors && product.colors.length > 0 ? product.colors[0].name : 'Default'
  );
  const [isAdding, setIsAdding] = useState(false);

  const discountPct = product.originalPrice && product.originalPrice > product.price
    ? Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)
    : 0;

  const handleQuickAdd = () => {
    setIsAdding(true);
    addToCart(product, selectedColor, 1);
    onShowToast(`Added ${product.name} (${selectedColor}) to bag`, '✓');
    setTimeout(() => setIsAdding(false), 800);
  };

  const handleWishlist = () => {
    const added = toggleWishlist(product.id);
    onShowToast(added ? `Saved ${product.name} to Wishlist` : `Removed from Wishlist`, '✓');
  };

  return (
    <article className="product-card">
      <div className="card-media">
        <div className="card-badge-group">
          {product.badge && (
            <span className="card-badge">{product.badge}</span>
          )}
          {discountPct > 0 && (
            <span className="card-badge discount-badge">-{discountPct}%</span>
          )}
        </div>

        <button 
          className={`wishlist-btn ${wishlisted ? 'active' : ''}`}
          onClick={handleWishlist}
          aria-label={wishlisted ? "Remove from Wishlist" : "Save to Wishlist"}
          title={wishlisted ? "Remove from Wishlist" : "Save to Wishlist"}
          style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}
        >
          <IconHeart size={18} fill={wishlisted ? '#DE1110' : 'none'} stroke={wishlisted ? '#DE1110' : '#475569'} />
        </button>

        <img src={product.image} alt={product.name} loading="lazy" width="400" height="400" />

        <button 
          className="quick-view-overlay-btn"
          onClick={() => onQuickView(product)}
          style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.4rem' }}
        >
          <IconEye size={14} />
          <span>Quick View</span>
        </button>
      </div>

      <div className="card-body">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
          <span className="card-category">{product.categoryName}</span>
          <span className="card-artisan-seal">100% Hand-Hooked</span>
        </div>

        <h3 className="card-title" onClick={() => onQuickView(product)} style={{ cursor: 'pointer' }}>
          {product.name}
        </h3>
        
        <div className="card-rating">
          {product.reviewsCount > 0 ? (
            <>
              <span className="star-symbol">★</span>
              <span className="rating-num">{Number(product.rating || 5).toFixed(1)}</span>
              <span className="rating-count">({product.reviewsCount} {product.reviewsCount === 1 ? 'review' : 'reviews'})</span>
            </>
          ) : (
            <span className="rating-count" style={{ fontStyle: 'normal', color: 'var(--text-muted)' }}>
              ★ 5.0 • Fresh Studio Batch
            </span>
          )}
        </div>

        {product.colors && product.colors.length > 0 && (
          <div className="card-swatches">
            {product.colors.map((c, i) => {
              const isSelected = selectedColor === c.name;
              return (
                <button
                  type="button"
                  key={i}
                  className={`swatch-circle ${isSelected ? 'selected' : ''}`}
                  style={{ backgroundColor: c.hex }}
                  title={c.name}
                  onClick={(e) => {
                    e.stopPropagation();
                    setSelectedColor(c.name);
                  }}
                  aria-label={`Select color ${c.name}`}
                />
              );
            })}
            <span className="swatch-more">{selectedColor}</span>
          </div>
        )}

        <div className="card-footer">
          <div className="price-wrap">
            <span className="current-price">{formatPrice(product.price)}</span>
            {product.originalPrice && (
              <span className="original-price">{formatPrice(product.originalPrice)}</span>
            )}
          </div>
          
          <button 
            className={`card-add-btn ${isAdding ? 'added' : ''}`}
            onClick={handleQuickAdd}
            aria-label={`Add ${product.name} to cart`}
          >
            {isAdding ? (
              <>
                <IconCheck size={14} />
                <span>Added</span>
              </>
            ) : (
              <>
                <IconBag size={14} />
                <span>Add to Bag</span>
              </>
            )}
          </button>
        </div>
      </div>
    </article>
  );
};
