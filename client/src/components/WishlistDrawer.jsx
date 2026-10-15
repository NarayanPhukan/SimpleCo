import React from 'react';
import { useCart } from '../context/CartContext';
import { IconHeart, IconBag, IconClose, IconArrowRight, IconShare, IconTrash } from './Icons';

export const WishlistDrawer = ({ isOpen, onClose, products = [], onShowToast, onOpenCart, onExplore }) => {
  const { wishlist, removeFromWishlist, clearWishlist, moveToCart, formatPrice } = useCart();

  if (!isOpen) return null;

  // Resolve wishlisted items from catalog products
  const wishlistedItems = wishlist.map(id => {
    const found = products.find(p => p.id === id || p._id === id);
    if (found) return found;
    // Fallback if catalog is still loading or product was customized
    return {
      id,
      name: id === 'bgc-cow-01' ? 'Strawberry Cow Amigurumi Plush' :
            id === 'bgc-tulip-02' ? 'Everlasting Pastel Tulip Bouquet' : 'Handmade Crochet Creation',
      price: id === 'bgc-cow-01' ? 1299 : id === 'bgc-tulip-02' ? 899 : 999,
      image: id === 'bgc-cow-01' ? '/assets/images/strawberry-cow.jpg' :
             id === 'bgc-tulip-02' ? '/assets/images/tulip-bouquet.jpg' : '/assets/images/strawberry-cow.jpg',
      categoryName: 'Artisanal Studio',
      badge: 'Bestseller'
    };
  });

  const handleShareWishlist = () => {
    if (wishlist.length === 0) {
      onShowToast('Wishlist is empty!', '!');
      return;
    }
    const shareUrl = `${window.location.origin}${window.location.pathname}#wishlist=${wishlist.join(',')}`;
    if (navigator.clipboard) {
      navigator.clipboard.writeText(shareUrl)
        .then(() => onShowToast('Wishlist link copied to clipboard! 💌', '✓'))
        .catch(() => onShowToast('Share link: ' + shareUrl, '✓'));
    } else {
      onShowToast('Share link: ' + shareUrl, '✓');
    }
  };

  const handleMoveAllToBag = () => {
    if (wishlistedItems.length === 0) return;
    wishlistedItems.forEach(item => {
      moveToCart(item);
    });
    onShowToast(`Moved ${wishlistedItems.length} items to bag! 🛍️`, '✓');
    onClose();
    if (onOpenCart) onOpenCart();
  };

  return (
    <div 
      className={`drawer-backdrop ${isOpen ? 'active' : ''}`}
      onClick={(e) => {
        if (e.target.classList.contains('drawer-backdrop')) onClose();
      }}
    >
      <div className="cart-drawer wishlist-drawer" role="dialog" aria-modal="true" aria-label="My Wishlist">
        {/* Header */}
        <div className="cart-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <span style={{ color: 'var(--primary)', display: 'inline-flex' }}>
              <IconHeart size={22} fill="var(--primary)" stroke="var(--primary)" />
            </span>
            <div>
              <h3 style={{ margin: 0, fontSize: '1.2rem', fontFamily: 'var(--font-serif)', color: 'var(--text-dark)' }}>
                My Wishlist ({wishlist.length})
              </h3>
              <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                Handmade treasures saved for later
              </span>
            </div>
          </div>
          <button className="drawer-close-btn" onClick={onClose} aria-label="Close Wishlist">&times;</button>
        </div>

        {/* Action Bar (Share & Clear) */}
        {wishlist.length > 0 && (
          <div className="wishlist-toolbar">
            <button 
              className="wishlist-tool-btn" 
              onClick={handleShareWishlist}
              title="Copy shareable wishlist link"
            >
              <IconShare size={14} />
              <span>Share Wishlist</span>
            </button>
            <button 
              className="wishlist-tool-btn danger" 
              onClick={() => { clearWishlist(); onShowToast('Wishlist cleared', '✓'); }}
              title="Clear all saved items"
            >
              <IconTrash size={14} />
              <span>Clear</span>
            </button>
          </div>
        )}

        {/* Wishlist Items List */}
        <div className="cart-items-body">
          {wishlist.length === 0 ? (
            <div className="cart-empty-state">
              <span className="cart-empty-icon" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary-glow)' }}>
                <IconHeart size={48} stroke="var(--primary)" />
              </span>
              <h4>Your wishlist is waiting for love</h4>
              <p>Tap the little heart on any crochet companion or bouquet to keep it close.</p>
              <button 
                className="btn btn-primary" 
                onClick={() => { onClose(); if (onExplore) onExplore(); }} 
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', margin: '0 auto' }}
              >
                <span>Discover Creations</span>
                <IconArrowRight size={16} />
              </button>
            </div>
          ) : (
            wishlistedItems.map((item) => (
              <div key={item.id} className="cart-item-card wishlist-item-card">
                <img src={item.image} alt={item.name} className="cart-item-thumb" />
                <div className="cart-item-details">
                  <span className="card-category" style={{ fontSize: '0.72rem', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                    {item.categoryName || 'Crochet Studio'}
                  </span>
                  <h4 className="cart-item-name" style={{ fontSize: '0.95rem', margin: '0.15rem 0' }}>{item.name}</h4>
                  
                  <div className="price-wrap" style={{ margin: '0.25rem 0 0.5rem 0' }}>
                    <span className="current-price" style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-dark)' }}>
                      {formatPrice(item.price)}
                    </span>
                    {item.originalPrice && (
                      <span className="original-price" style={{ fontSize: '0.82rem', marginLeft: '0.4rem', textDecoration: 'line-through', color: 'var(--text-light)' }}>
                        {formatPrice(item.originalPrice)}
                      </span>
                    )}
                  </div>

                  <div className="wishlist-item-actions">
                    <button 
                      className="btn btn-primary btn-sm"
                      onClick={() => {
                        moveToCart(item);
                        onShowToast(`Moved ${item.name} to bag! 🛍️`, '✓');
                      }}
                      style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', padding: '0.45rem 0.85rem', fontSize: '0.82rem' }}
                    >
                      <IconBag size={14} />
                      <span>Move to Bag</span>
                    </button>
                    <button 
                      className="cart-remove-btn" 
                      onClick={() => {
                        removeFromWishlist(item.id);
                        onShowToast(`Removed from wishlist`, '✓');
                      }}
                      title="Remove from wishlist"
                    >
                      <IconClose size={14} />
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Footer with Move All */}
        {wishlist.length > 0 && (
          <div className="cart-footer">
            <button 
              className="btn btn-primary btn-checkout" 
              onClick={handleMoveAllToBag}
              style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', width: '100%' }}
            >
              <IconBag size={16} />
              <span>Move All to Bag ({wishlist.length})</span>
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
