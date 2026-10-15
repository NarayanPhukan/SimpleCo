import React from 'react';
import { useCart } from '../context/CartContext';
import { IconClock, IconTrash, IconEye } from './Icons';

export const RecentlyViewedBar = ({ onQuickView, onShowToast }) => {
  const { recentlyViewed, clearRecentlyViewed, formatPrice } = useCart();

  if (!recentlyViewed || recentlyViewed.length === 0) return null;

  return (
    <section className="recently-viewed-section">
      <div className="container">
        <div className="recently-viewed-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ color: 'var(--primary)', display: 'inline-flex' }}>
              <IconClock size={18} />
            </span>
            <h3 className="recently-viewed-title">Recently Viewed Pieces</h3>
            <span className="recently-viewed-count">({recentlyViewed.length})</span>
          </div>
          <button 
            className="clear-recent-btn" 
            onClick={() => {
              clearRecentlyViewed();
              if (onShowToast) onShowToast('Browsing history cleared', '✓');
            }}
            title="Clear recently viewed history"
          >
            <IconTrash size={14} />
            <span>Clear</span>
          </button>
        </div>

        <div className="recently-viewed-row">
          {recentlyViewed.map(item => (
            <div 
              key={item.id} 
              className="recent-item-card"
              onClick={() => onQuickView(item)}
              role="button"
              tabIndex={0}
              title={`Quick view ${item.name}`}
            >
              <div className="recent-item-thumb-box">
                <img src={item.image} alt={item.name} loading="lazy" />
                <span className="recent-item-hover-icon">
                  <IconEye size={16} />
                </span>
              </div>
              <div className="recent-item-info">
                <span className="recent-item-cat">{item.categoryName || 'Crochet'}</span>
                <h5 className="recent-item-name">{item.name}</h5>
                <span className="recent-item-price">{formatPrice(item.price)}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};
