import React from 'react';
import { useCart } from '../context/CartContext';

export const ProductCard = ({ product, onQuickView, onShowToast }) => {
  const { formatPrice, addToCart, isInWishlist, toggleWishlist } = useCart();
  const wishlisted = isInWishlist(product.id);

  const handleQuickAdd = () => {
    const defaultColor = product.colors && product.colors.length > 0 ? product.colors[0].name : 'Default';
    addToCart(product, defaultColor, 1);
    onShowToast(`Added ${product.name} (${defaultColor}) to bag! 🛍️`, '🍓');
  };

  const handleWishlist = () => {
    const added = toggleWishlist(product.id);
    onShowToast(added ? `Saved ${product.name} to Wishlist! 💕` : `Removed from Wishlist`, added ? '💖' : '🤍');
  };

  return (
    <article className="product-card">
      <div className="card-media">
        <span className="card-badge">{product.badge}</span>
        <button 
          className={`wishlist-btn ${wishlisted ? 'active' : ''}`}
          onClick={handleWishlist}
          aria-label="Save to Wishlist"
        >
          {wishlisted ? '❤️' : '🤍'}
        </button>
        <img src={product.image} alt={product.name} loading="lazy" width="400" height="400" />
        <button 
          className="quick-view-overlay-btn"
          onClick={() => onQuickView(product)}
        >
          Quick View 👁️
        </button>
      </div>

      <div className="card-body">
        <span className="card-category">{product.categoryName}</span>
        <h3 className="card-title">{product.name}</h3>
        
        <div className="card-rating">
          <span>★</span>
          <span>{Number(product.rating).toFixed(1)}</span>
          <span className="rating-count">({product.reviewsCount} reviews)</span>
        </div>

        <div className="card-swatches">
          {product.colors && product.colors.map((c, i) => (
            <span key={i} className="swatch-circle" style={{ backgroundColor: c.hex }} title={c.name} />
          ))}
          <span className="swatch-more">+{product.colors?.length || 0} colors</span>
        </div>

        <div className="card-footer">
          <div className="price-wrap">
            <span className="current-price">{formatPrice(product.price)}</span>
            {product.originalPrice && (
              <span className="original-price">{formatPrice(product.originalPrice)}</span>
            )}
          </div>
          <button 
            className="card-add-btn" 
            onClick={handleQuickAdd}
            aria-label={`Add ${product.name} to cart`}
          >
            +
          </button>
        </div>
      </div>
    </article>
  );
};
