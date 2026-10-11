import React, { useState } from 'react';
import { useCart } from '../context/CartContext';
import { IconBag, IconArrowRight, IconClose } from './Icons';

export const CartDrawer = ({ isOpen, onClose, onProceedCheckout, onShowToast, onExplore }) => {
  const { cart, updateQuantity, removeFromCart, coupon, applyCoupon, removeCoupon, getTotals, formatPrice } = useCart();
  const [couponCode, setCouponCode] = useState('');

  if (!isOpen) return null;

  const totals = getTotals();
  const threshold = totals.freeShippingThreshold;
  const subtotal = totals.subtotal;
  const pct = Math.min(100, Math.round((subtotal / threshold) * 100));

  const handleApplyCoupon = () => {
    if (!couponCode.trim()) return;
    const res = applyCoupon(couponCode);
    if (res.success) {
      onShowToast(`Promo "${res.coupon.code}" applied`, '✓');
      setCouponCode('');
    } else {
      onShowToast(res.message, '!');
    }
  };

  const handleRemoveCoupon = () => {
    removeCoupon();
    onShowToast('Coupon removed', '✓');
  };

  return (
    <div className={`drawer-backdrop ${isOpen ? 'active' : ''}`} onClick={(e) => {
      if (e.target.classList.contains('drawer-backdrop')) onClose();
    }}>
      <div className="cart-drawer" role="dialog" aria-modal="true" aria-label="Shopping Cart">
        <div className="cart-header">
          <h3>Shopping Bag ({totals.itemCount})</h3>
          <button className="drawer-close-btn" onClick={onClose} aria-label="Close cart">&times;</button>
        </div>

        {/* Free Shipping Meter */}
        <div className="shipping-meter-wrap">
          <div className="shipping-meter-text">
            <span>
              {totals.remainingForFreeShipping <= 0 
                ? "Yay! You've unlocked Free Shipping ✨" 
                : `Add ${formatPrice(totals.remainingForFreeShipping)} more for Free Shipping`}
            </span>
            <span>{pct}%</span>
          </div>
          <div className="meter-track">
            <div className="meter-fill" style={{ width: `${pct}%` }}></div>
          </div>
        </div>

        {/* Items Body */}
        <div className="cart-items-body">
          {cart.length === 0 ? (
            <div className="cart-empty-state">
              <span className="cart-empty-icon" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                <IconBag size={48} />
              </span>
              <h4>Your bag is currently empty</h4>
              <p>Find cute plushies, flowers, or totes to adopt! 🌸</p>
              <button className="btn btn-primary" onClick={() => { onClose(); onExplore(); }} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                <span>Start Browsing</span>
                <IconArrowRight size={16} />
              </button>
            </div>
          ) : (
            cart.map((item, index) => (
              <div key={`${item.id}-${item.color}-${index}`} className="cart-item-card">
                <img src={item.image} alt={item.name} className="cart-item-thumb" />
                <div className="cart-item-details">
                  <h4 className="cart-item-name">{item.name}</h4>
                  <div className="cart-item-variant">Color: <strong>{item.color}</strong></div>
                  {item.customNote && (
                    <div style={{ fontSize: '0.74rem', color: 'var(--secondary)', marginBottom: '0.35rem' }}>
                      Note: "{item.customNote}"
                    </div>
                  )}
                  <div className="cart-item-bottom">
                    <div className="qty-stepper">
                      <button className="qty-btn" onClick={() => updateQuantity(index, item.quantity - 1)}>-</button>
                      <span className="qty-value">{item.quantity}</span>
                      <button className="qty-btn" onClick={() => updateQuantity(index, item.quantity + 1)}>+</button>
                    </div>
                    <span className="cart-item-price">{formatPrice(item.price * item.quantity)}</span>
                    <button 
                      className="cart-remove-btn" 
                      onClick={() => { removeFromCart(index); onShowToast('Item removed from bag', '✓'); }}
                      title="Remove item"
                    >
                      <IconClose size={14} />
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Cart Footer */}
        {cart.length > 0 && (
          <div className="cart-footer">
            <div id="couponArea">
              <div className="coupon-box">
                <input 
                  type="text" 
                  className="coupon-input" 
                  placeholder="Promo code (BABYYGURLL)"
                  value={couponCode}
                  onChange={(e) => setCouponCode(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter') handleApplyCoupon(); }}
                />
                <button className="coupon-btn" onClick={handleApplyCoupon}>Apply</button>
              </div>

              {coupon && (
                <div className="active-coupon-tag">
                  <span>Coupon Applied: <strong>{coupon.code}</strong> ({coupon.label})</span>
                  <span className="remove-coupon-btn" onClick={handleRemoveCoupon} title="Remove coupon">&times;</span>
                </div>
              )}
            </div>

            <div className="cart-summary-table">
              <div className="summary-row">
                <span>Subtotal</span>
                <span>{formatPrice(totals.subtotal)}</span>
              </div>
              {totals.discount > 0 && (
                <div className="summary-row" style={{ color: 'var(--success)' }}>
                  <span>Promo Discount</span>
                  <span>-{formatPrice(totals.discount)}</span>
                </div>
              )}
              <div className="summary-row">
                <span>Estimated Shipping</span>
                <span>{totals.shipping === 0 ? 'FREE' : formatPrice(totals.shipping)}</span>
              </div>
              <div className="summary-row">
                <span>Estimated Tax (7%)</span>
                <span>{formatPrice(totals.tax)}</span>
              </div>
              <div className="summary-row total-row">
                <span>Estimated Total</span>
                <span>{formatPrice(totals.total)}</span>
              </div>
            </div>

            <button className="btn btn-primary btn-checkout" onClick={onProceedCheckout} style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}>
              <span>Proceed to Checkout</span>
              <IconArrowRight size={16} />
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
