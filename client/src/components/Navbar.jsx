import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { IconHeart, IconMoon, IconSun, IconSparkles } from './Icons';

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
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 3c-4.97 0-9 4.03-9 9 0 2.12.74 4.07 1.97 5.61L12 21l7.03-3.39A8.97 8.97 0 0 0 21 12c0-4.97-4.03-9-9-9z"></path>
              <path d="M12 7a5 5 0 0 0-5 5c0 1.93.98 3.63 2.47 4.63"></path>
            </svg>
          </div>
          <div className="brand-text">
            <span className="brand-title">BabyyGurll <span className="brand-accent">Crochet</span></span>
            <span className="brand-subtitle">Handmade with love • India</span>
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
              <IconHeart size={20} fill={wishlist.length > 0 ? 'var(--primary)' : 'none'} stroke="currentColor" />
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

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="mobile-nav-drawer">
          <ul className="mobile-nav-links">
            <li>
              <a href="#catalog" onClick={(e) => handleNavCategory(e, 'all')}>
                Shop All
              </a>
            </li>
            <li>
              <a href="#catalog" onClick={(e) => handleNavCategory(e, 'plushies')}>
                Amigurumi Plushies
              </a>
            </li>
            <li>
              <a href="#catalog" onClick={(e) => handleNavCategory(e, 'floral')}>
                Floral & Home Decor
              </a>
            </li>
            <li>
              <a href="#catalog" onClick={(e) => handleNavCategory(e, 'bags')}>
                Bags & Totes
              </a>
            </li>
            <li>
              <a href="#catalog" onClick={(e) => handleNavCategory(e, 'wearables')}>
                Wearables & Cardigans
              </a>
            </li>
            <li>
              <a 
                href="#studio" 
                onClick={(e) => {
                  e.preventDefault();
                  setMobileMenuOpen(false);
                  if (onOpenCustomStudio) onOpenCustomStudio();
                }}
                style={{ color: 'var(--primary)', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.4rem' }}
              >
                <IconSparkles size={16} />
                <span>Custom Studio & Bouquet Builder</span>
              </a>
            </li>
            <li>
              <a 
                href="#wishlist" 
                onClick={(e) => {
                  e.preventDefault();
                  setMobileMenuOpen(false);
                  if (onOpenWishlist) onOpenWishlist();
                }}
                style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}
              >
                <IconHeart size={16} />
                <span>My Wishlist ({wishlist.length})</span>
              </a>
            </li>
          </ul>
        </div>
      )}
    </header>
  );
};
