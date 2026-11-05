import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { IconHeart, IconMoon, IconSun, IconSparkles, IconUser, IconClose, IconPackage, IconArrowRight } from './Icons';

export const Navbar = ({ 
  onOpenAccount, 
  onOpenCart, 
  onOpenAdmin, 
  onSelectCategory,
  onOpenWishlist,
  onOpenCustomStudio 
}) => {
  const { user } = useAuth();
  const { getTotals, wishlist, theme, toggleTheme } = useCart();
  const { itemCount } = getTotals();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  // Lock body scroll while mobile drawer is open
  useEffect(() => {
    if (mobileMenuOpen) {
      const prevOverflow = document.body.style.overflow;
      document.body.style.overflow = 'hidden';
      const handleEsc = (e) => {
        if (e.key === 'Escape') setMobileMenuOpen(false);
      };
      window.addEventListener('keydown', handleEsc);
      return () => {
        document.body.style.overflow = prevOverflow;
        window.removeEventListener('keydown', handleEsc);
      };
    }
  }, [mobileMenuOpen]);

  const handleNavCategory = (e, cat) => {
    e.preventDefault();
    onSelectCategory(cat);
    setMobileMenuOpen(false);
    const catElem = document.getElementById('catalog');
    if (catElem) catElem.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <header className="site-header" id="siteHeader">
      <div className="nav-container">
        {/* Brand Logo */}
        <a href="#hero" className="brand-logo" id="brandLogo">
          <div className="brand-icon-wrapper" aria-hidden="true">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="11" cy="13" r="8" />
              <path d="M5.5 8.5C8 10 13 10.5 16.5 14" />
              <path d="M7 16c2.5-.5 6.5.5 9.5 3" />
              <path d="M8 6c2.5 1 5 4 6 7.5" />
              <path d="M15 5l6-3c.5-.2 1 .3.8.8l-3 6" />
              <circle cx="19" cy="4" r="1" fill="currentColor" />
            </svg>
          </div>
          <div className="brand-text">
            <span className="brand-title">BabyyGurll <span className="brand-accent">Crochet</span></span>
            <span className="brand-subtitle">Artisanal Studio • India</span>
          </div>
        </a>

        {/* Desktop Navigation Links */}
        <nav className="desktop-nav" aria-label="Main Navigation">
          <ul className="nav-links">
            <li>
              <a href="#catalog" className="nav-link" onClick={(e) => handleNavCategory(e, 'all')}>
                Shop All
              </a>
            </li>
            <li>
              <a href="#catalog" className="nav-link" onClick={(e) => handleNavCategory(e, 'plushies')}>
                Plushies
              </a>
            </li>
            <li>
              <a href="#catalog" className="nav-link" onClick={(e) => handleNavCategory(e, 'floral')}>
                Floral & Home
              </a>
            </li>
            <li>
              <a href="#catalog" className="nav-link" onClick={(e) => handleNavCategory(e, 'bags')}>
                Bags & Totes
              </a>
            </li>
            <li>
              <a href="#catalog" className="nav-link" onClick={(e) => handleNavCategory(e, 'wearables')}>
                Wearables
              </a>
            </li>
            <li>
              <button 
                type="button"
                className="nav-link nav-studio-pill-btn"
                onClick={() => onOpenCustomStudio && onOpenCustomStudio()}
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
              >
                <IconSparkles size={14} />
                <span>Custom Studio</span>
              </button>
            </li>
          </ul>
        </nav>

        {/* Right Action Controls (Theme Toggle, Wishlist, Account & Cart) */}
        <div className="nav-actions">
          {/* Candlelight / Cozy Dark Theme Toggle */}
          <button 
            className="nav-icon-btn nav-theme-btn" 
            onClick={toggleTheme}
            aria-label={theme === 'candlelight' ? 'Switch to daylight palette' : 'Switch to cozy candlelight mode'}
            title={theme === 'candlelight' ? 'Daylight Palette' : 'Cozy Candlelight Mode'}
          >
            {theme === 'candlelight' ? <IconSun size={19} /> : <IconMoon size={19} />}
          </button>

          {/* Wishlist Button */}
          <button 
            className="nav-icon-btn nav-wishlist-btn" 
            onClick={onOpenWishlist}
            aria-label={`My Wishlist with ${wishlist.length} saved items`}
            title={`Wishlist (${wishlist.length} saved)`}
          >
            <span className="wishlist-icon-svg" aria-hidden="true" style={{ display: 'inline-flex' }}>
              <IconHeart size={20} fill={wishlist.length > 0 ? '#DE1110' : 'none'} stroke={wishlist.length > 0 ? '#DE1110' : 'currentColor'} />
            </span>
            {wishlist.length > 0 && (
              <span className="nav-cart-badge nav-wishlist-badge">{wishlist.length}</span>
            )}
          </button>

          {/* Account Logo Button */}
          <button 
            className="nav-icon-btn nav-account-btn" 
            onClick={onOpenAccount} 
            aria-label={user?.isLoggedIn ? `Account (${user.name})` : 'My Account'}
            title={user?.isLoggedIn ? `Account: ${user.name}` : 'Sign In / Account'}
          >
            <span className="account-icon-svg" aria-hidden="true">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </span>
            {user?.isLoggedIn && (
              <span className="nav-user-status-dot" title={`Logged in as ${user.name}`}></span>
            )}
          </button>

          {/* Cart Logo Button */}
          <button 
            className="nav-icon-btn nav-cart-btn" 
            onClick={onOpenCart} 
            aria-label={`Shopping Cart with ${itemCount} items`}
            title={`Shopping Cart (${itemCount} items)`}
          >
            <span className="cart-icon-svg" aria-hidden="true">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <path d="M16 10a4 4 0 0 1-8 0"></path>
              </svg>
            </span>
            <span className="nav-cart-badge">{itemCount}</span>
          </button>

          {/* Mobile Hamburger Button */}
          <button 
            className="mobile-hamburger-btn" 
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label="Toggle navigation menu"
            aria-expanded={mobileMenuOpen}
          >
            <span className={`hamburger-bar ${mobileMenuOpen ? 'open' : ''}`}></span>
          </button>
        </div>
      </div>

      {/* Off-Canvas Mobile Navigation Backdrop */}
      <div 
        className={`mobile-drawer-backdrop ${mobileMenuOpen ? 'open' : ''}`}
        onClick={() => setMobileMenuOpen(false)}
        aria-hidden="true"
      />

      {/* Off-Canvas Mobile Navigation Drawer */}
      <aside 
        className={`mobile-nav-drawer-panel ${mobileMenuOpen ? 'open' : ''}`}
        aria-label="Mobile Navigation Menu"
      >
        {/* Drawer Header */}
        <div className="mobile-drawer-header">
          <div className="mobile-drawer-brand">
            <div className="drawer-brand-icon-wrapper" aria-hidden="true">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="13" r="8" />
                <path d="M5.5 8.5C8 10 13 10.5 16.5 14" />
                <path d="M7 16c2.5-.5 6.5.5 9.5 3" />
                <path d="M8 6c2.5 1 5 4 6 7.5" />
              </svg>
            </div>
            <div>
              <span className="drawer-brand-title">BabyyGurll <span style={{ color: 'var(--primary)' }}>Crochet</span></span>
              <span className="drawer-brand-subtitle">Artisanal Studio • India</span>
            </div>
          </div>
          <button 
            type="button" 
            className="mobile-drawer-close-btn" 
            onClick={() => setMobileMenuOpen(false)}
            aria-label="Close navigation menu"
          >
            <IconClose size={20} />
          </button>
        </div>

        <div className="mobile-drawer-body">
          {/* User Account / Sign In Banner */}
          {user?.isLoggedIn ? (
            <div 
              className="drawer-auth-card drawer-logged-in"
              onClick={() => {
                setMobileMenuOpen(false);
                if (onOpenAccount) onOpenAccount();
              }}
              role="button"
              tabIndex={0}
            >
              <div className="drawer-user-avatar">
                {user.name ? user.name.charAt(0).toUpperCase() : 'U'}
              </div>
              <div className="drawer-user-info">
                <div className="drawer-user-name">{user.name}</div>
                <div className="drawer-user-sub">
                  <span className="drawer-tier-pill">{user.memberTier || 'VIP Member'}</span>
                  <span>My Orders & Addresses &rarr;</span>
                </div>
              </div>
            </div>
          ) : (
            <div 
              className="drawer-auth-card drawer-guest"
              onClick={() => {
                setMobileMenuOpen(false);
                if (onOpenAccount) onOpenAccount();
              }}
              role="button"
              tabIndex={0}
            >
              <div className="drawer-guest-icon">
                <IconUser size={22} />
              </div>
              <div className="drawer-guest-info">
                <div className="drawer-guest-title">Sign In / Create Account</div>
                <div className="drawer-guest-sub">Track orders & earn VIP Points &rarr;</div>
              </div>
            </div>
          )}

          {/* Custom Studio Callout Pill */}
          <div style={{ margin: '0.85rem 0' }}>
            <button 
              type="button"
              className="drawer-studio-card-btn"
              onClick={() => {
                setMobileMenuOpen(false);
                if (onOpenCustomStudio) onOpenCustomStudio();
              }}
            >
              <span className="drawer-studio-icon">✨</span>
              <div style={{ textAlign: 'left', flex: 1 }}>
                <div style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--primary)' }}>Custom Studio & Bouquets</div>
                <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>Build your bespoke crochet piece</div>
              </div>
              <IconArrowRight size={15} />
            </button>
          </div>

          {/* Navigation Categories */}
          <div className="drawer-section-label">Boutique Collections</div>
          <nav className="mobile-drawer-nav">
            <a href="#catalog" className="drawer-nav-item" onClick={(e) => handleNavCategory(e, 'all')}>
              <span className="drawer-item-icon">✨</span>
              <span>All Handcrafted Pieces</span>
            </a>
            <a href="#catalog" className="drawer-nav-item" onClick={(e) => handleNavCategory(e, 'plushies')}>
              <span className="drawer-item-icon">🧸</span>
              <span>Amigurumi Plushies</span>
            </a>
            <a href="#catalog" className="drawer-nav-item" onClick={(e) => handleNavCategory(e, 'floral')}>
              <span className="drawer-item-icon">🌸</span>
              <span>Floral & Home Decor</span>
            </a>
            <a href="#catalog" className="drawer-nav-item" onClick={(e) => handleNavCategory(e, 'bags')}>
              <span className="drawer-item-icon">👜</span>
              <span>Bags, Totes & Clutches</span>
            </a>
            <a href="#catalog" className="drawer-nav-item" onClick={(e) => handleNavCategory(e, 'wearables')}>
              <span className="drawer-item-icon">🧶</span>
              <span>Wearables & Cardigans</span>
            </a>
            <a href="#catalog" className="drawer-nav-item" onClick={(e) => handleNavCategory(e, 'accessories')}>
              <span className="drawer-item-icon">🎀</span>
              <span>Accessories & Scrunchies</span>
            </a>
          </nav>

          {/* Quick Shortcuts */}
          <div className="drawer-section-label">Quick Actions</div>
          <div className="drawer-shortcuts-grid">
            <button
              type="button"
              className="drawer-shortcut-chip"
              onClick={() => {
                setMobileMenuOpen(false);
                if (onOpenWishlist) onOpenWishlist();
              }}
            >
              <IconHeart size={16} fill={wishlist.length > 0 ? '#DE1110' : 'none'} stroke={wishlist.length > 0 ? '#DE1110' : 'currentColor'} />
              <span>Wishlist ({wishlist.length})</span>
            </button>

            <button
              type="button"
              className="drawer-shortcut-chip"
              onClick={() => {
                setMobileMenuOpen(false);
                if (onOpenAccount) onOpenAccount();
              }}
            >
              <IconPackage size={16} />
              <span>Track Orders</span>
            </button>
          </div>

          {/* Theme Palette Switcher */}
          <div className="drawer-theme-section">
            <button
              type="button"
              className="drawer-theme-toggle-btn"
              onClick={() => toggleTheme()}
            >
              {theme === 'candlelight' ? <IconSun size={18} /> : <IconMoon size={18} />}
              <span>{theme === 'candlelight' ? 'Switch to Daylight Palette ☀️' : 'Cozy Candlelight Mode 🌙'}</span>
            </button>
          </div>

          {/* Footer Quality Promise */}
          <div className="drawer-footer-note">
            Handcrafted with 100% Milk Cotton Yarn<br />
            <span>Free Delivery Across India</span>
          </div>
        </div>
      </aside>
    </header>
  );
};
