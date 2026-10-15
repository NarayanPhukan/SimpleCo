import React, { useState, useEffect } from 'react';
import { useCart } from '../context/CartContext';
import { IconBag, IconArrowRight, IconClose, IconGift } from './Icons';
import { api } from '../services/api';

export const CartDrawer = ({ isOpen, onClose, onProceedCheckout, onShowToast, onExplore }) => {
  const { 
    cart, 
    updateQuantity, 
    removeFromCart, 
    coupon, 
    applyCoupon, 
    removeCoupon, 
    getTotals, 
    formatPrice,
    giftPackaging,
    updateGiftPackaging
  } = useCart();
  const [couponCode, setCouponCode] = useState('');
  const [availableOffers, setAvailableOffers] = useState([]);
  const [showOffers, setShowOffers] = useState(false);
  const [loadingOffers, setLoadingOffers] = useState(false);

  useEffect(() => {
    if (!isOpen) return;
    const fetchOffers = async () => {
      try {
        setLoadingOffers(true);
        const res = await api.getCoupons();
        if (res && res.data) {
          // Filter only active, public, non-expired coupons
          const now = new Date();
          const publicCoupons = res.data.filter(c => 
            c.isActive !== false && 
            c.isPublic !== false &&
            (!c.expiryDate || new Date(c.expiryDate) > now)
          );
          setAvailableOffers(publicCoupons);
        }
      } catch (err) {
        // Fallback default offers
        setAvailableOffers([
          {
            code: 'BABYYGURLL',
            description: 'Store opening special welcome discount for all collectors',
            discountType: 'percentage',
            discountValue: 15,
            minOrderValue: 0,
            expiryDate: '2026-12-31'
          },
          {
            code: 'CUTIE100',
            description: 'Instant flat ₹100 deduction on orders above ₹500',
            discountType: 'flat',
            discountValue: 100,
            minOrderValue: 500,
            expiryDate: '2026-11-30'
          },
          {
            code: 'FREESHIP',
            description: 'Free pan-India tracked courier delivery across all states',
            discountType: 'free_shipping',
            discountValue: 0,
            minOrderValue: 0,
            expiryDate: '2026-12-31'
          }
        ]);
      } finally {
        setLoadingOffers(false);
      }
    };
    fetchOffers();
  }, [isOpen]);

  if (!isOpen) return null;

  const totals = getTotals();
  const threshold = totals.freeShippingThreshold;
  const subtotal = totals.subtotal;
  const pct = Math.min(100, Math.round((subtotal / threshold) * 100));

  const handleApplyCoupon = async () => {
    if (!couponCode.trim()) return;
    const res = await applyCoupon(couponCode);
    if (res.success) {
      onShowToast(`Promo "${res.coupon.code}" applied`, '✓');
      setCouponCode('');
    } else {
      onShowToast(res.message, '!');
    }
  };

  const handleApplyOffer = async (code, minSpend = 0) => {
    if (minSpend > 0 && subtotal < minSpend) {
      const diff = Math.round(minSpend - subtotal);
      onShowToast(`Add ${formatPrice(diff)} more to your cart to unlock "${code}"!`, '!');
      return;
    }
    const res = await applyCoupon(code);
    if (res.success) {
      onShowToast(`Promo "${code}" applied`, '✓');
      setShowOffers(false);
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
                ? "You have unlocked Free Pan-India Shipping" 
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
              <p>Explore our handcrafted plushies, everlasting bouquets, and bags.</p>
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
            {/* Luxury Gift Packaging Box */}
            <div className="gift-pack-card">
              <label className="gift-pack-toggle">
                <input 
                  type="checkbox" 
                  checked={giftPackaging.enabled}
                  onChange={(e) => {
                    updateGiftPackaging({ enabled: e.target.checked });
                    onShowToast(e.target.checked ? 'Gift packaging added (+₹99) 🎁' : 'Gift packaging removed', '✓');
                  }}
                />
                <div className="gift-pack-label-text">
                  <div className="gift-pack-title-row">
                    <span className="gift-pack-title">
                      <IconGift size={15} />
                      <strong>Luxury Gift Packaging</strong>
                    </span>
                    <span className="gift-pack-price">+{formatPrice(giftPackaging.price || 99)}</span>
                  </div>
                  <span className="gift-pack-sub">
                    Pastel keepsake box, silk ribbon & wax-sealed calligraphy card
                  </span>
                </div>
              </label>

              {giftPackaging.enabled && (
                <div className="gift-pack-fields">
                  <div className="form-group" style={{ marginBottom: '0.6rem' }}>
                    <input 
                      type="text" 
                      className="form-input gift-input"
                      placeholder="Recipient's Name (e.g. Maya)"
                      value={giftPackaging.recipientName || ''}
                      onChange={(e) => updateGiftPackaging({ recipientName: e.target.value })}
                    />
                  </div>
                  <div className="form-group" style={{ marginBottom: '0.6rem' }}>
                    <input 
                      type="text" 
                      className="form-input gift-input"
                      placeholder="Calligraphy Message (e.g. You make the world softer 🌸)"
                      value={giftPackaging.message || ''}
                      onChange={(e) => updateGiftPackaging({ message: e.target.value })}
                    />
                  </div>
                  <label className="gift-pack-hide-invoice">
                    <input 
                      type="checkbox" 
                      checked={giftPackaging.hideInvoice}
                      onChange={(e) => updateGiftPackaging({ hideInvoice: e.target.checked })}
                    />
                    <span>Do not include price invoice in parcel</span>
                  </label>
                </div>
              )}
            </div>

            <div id="couponArea" style={{ marginBottom: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.45rem' }}>
                <span style={{ fontSize: '0.82rem', fontWeight: 700, color: '#334155' }}>
                  Have a Promo Code?
                </span>
                {availableOffers.length > 0 && (
                  <button
                    type="button"
                    onClick={() => setShowOffers(!showOffers)}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: 'var(--primary)',
                      fontSize: '0.78rem',
                      fontWeight: 700,
                      cursor: 'pointer',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '0.25rem',
                      padding: '0.2rem 0'
                    }}
                  >
                    <span>🏷️ {showOffers ? 'Hide Offers' : `Available Offers (${availableOffers.length})`}</span>
                    <span style={{ fontSize: '0.65rem' }}>{showOffers ? '▲' : '▼'}</span>
                  </button>
                )}
              </div>

              <div className="coupon-box" style={{ marginBottom: '0.5rem' }}>
                <input 
                  type="text" 
                  className="coupon-input" 
                  placeholder="Enter code (e.g. CUTIE100)"
                  value={couponCode}
                  onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                  onKeyDown={(e) => { if (e.key === 'Enter') handleApplyCoupon(); }}
                />
                <button className="coupon-btn" onClick={handleApplyCoupon}>Apply</button>
              </div>

              {coupon && (
                <div className="active-coupon-tag" style={{ marginBottom: '0.75rem' }}>
                  <span>Coupon Applied: <strong>{coupon.code}</strong> ({coupon.label})</span>
                  <span className="remove-coupon-btn" onClick={handleRemoveCoupon} title="Remove coupon">&times;</span>
                </div>
              )}

              {/* AVAILABLE OFFERS LIST / ACCORDION */}
              {showOffers && (
                <div 
                  style={{
                    background: '#FFF5F8',
                    border: '1px solid #FECDD3',
                    borderRadius: '12px',
                    padding: '0.75rem',
                    marginBottom: '0.85rem',
                    maxHeight: '260px',
                    overflowY: 'auto'
                  }}
                >
                  <div style={{ fontSize: '0.75rem', fontWeight: 800, color: '#9F1239', textTransform: 'uppercase', letterSpacing: '0.04em', marginBottom: '0.5rem' }}>
                    🏷️ Available Boutique Offers & Coupons
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.55rem' }}>
                    {availableOffers.map(o => {
                      const isApplied = coupon && coupon.code === o.code;
                      const hasLimit = o.minOrderValue > 0;
                      const limitMet = !hasLimit || subtotal >= o.minOrderValue;
                      const diff = hasLimit ? Math.max(0, Math.round(o.minOrderValue - subtotal)) : 0;
                      const expiryStr = o.expiryDate 
                        ? new Date(o.expiryDate).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
                        : null;

                      return (
                        <div 
                          key={o.code}
                          style={{
                            background: '#FFFFFF',
                            borderRadius: '9px',
                            border: `1.5px ${isApplied ? 'solid #059669' : (limitMet ? 'dashed #FDA4AF' : 'solid #E2E8F0')}`,
                            padding: '0.65rem 0.8rem',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '0.35rem',
                            boxShadow: '0 1px 3px rgba(0,0,0,0.04)'
                          }}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                              <span style={{ 
                                background: isApplied ? '#ECFDF5' : '#FFF1F2',
                                color: isApplied ? '#047857' : '#BE123C',
                                fontWeight: 800,
                                fontSize: '0.85rem',
                                padding: '0.15rem 0.5rem',
                                borderRadius: '4px',
                                letterSpacing: '0.04em'
                              }}>
                                {o.code}
                              </span>
                            </div>

                            {isApplied ? (
                              <span style={{ fontSize: '0.74rem', color: '#059669', fontWeight: 800 }}>
                                Applied ✓
                              </span>
                            ) : (
                              <button
                                type="button"
                                onClick={() => handleApplyOffer(o.code, o.minOrderValue)}
                                style={{
                                  background: limitMet ? 'var(--primary)' : '#F1F5F9',
                                  color: limitMet ? '#FFFFFF' : '#64748B',
                                  border: limitMet ? 'none' : '1px solid #CBD5E1',
                                  borderRadius: '6px',
                                  padding: '0.22rem 0.65rem',
                                  fontSize: '0.74rem',
                                  fontWeight: 700,
                                  cursor: 'pointer'
                                }}
                              >
                                {limitMet ? 'Apply' : `Unlock at ₹${o.minOrderValue}`}
                              </button>
                            )}
                          </div>

                          <div style={{ fontSize: '0.8rem', color: '#1E293B', fontWeight: 600 }}>
                            {o.discountType === 'free_shipping' ? 'Free Nationwide Delivery' : (o.discountType === 'flat' ? `Flat ₹${o.discountValue} Off` : `${o.discountValue}% Off Total Order`)}
                          </div>

                          {o.description && (
                            <div style={{ fontSize: '0.74rem', color: '#64748B' }}>
                              {o.description}
                            </div>
                          )}

                          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem', marginTop: '0.15rem' }}>
                            {hasLimit ? (
                              <span style={{ 
                                fontSize: '0.68rem', 
                                padding: '0.1rem 0.4rem', 
                                borderRadius: '4px',
                                fontWeight: 600,
                                background: limitMet ? '#DCFCE7' : '#FEF3C7',
                                color: limitMet ? '#166534' : '#92400E'
                              }}>
                                {limitMet ? `✓ Min. spend ₹${o.minOrderValue} met` : `🛒 Min. spend ₹${o.minOrderValue} (Add ₹${diff} more)`}
                              </span>
                            ) : (
                              <span style={{ fontSize: '0.68rem', padding: '0.1rem 0.4rem', borderRadius: '4px', fontWeight: 600, background: '#F1F5F9', color: '#475569' }}>
                                ✓ No minimum spend
                              </span>
                            )}

                            {expiryStr && (
                              <span style={{ fontSize: '0.68rem', padding: '0.1rem 0.4rem', borderRadius: '4px', fontWeight: 600, background: '#F8FAFC', color: '#64748B', border: '1px solid #E2E8F0' }}>
                                📅 Valid till {expiryStr}
                              </span>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
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
              {totals.giftPackagingCost > 0 && (
                <div className="summary-row" style={{ color: 'var(--primary)' }}>
                  <span>Gift Keepsake Box</span>
                  <span>+{formatPrice(totals.giftPackagingCost)}</span>
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
