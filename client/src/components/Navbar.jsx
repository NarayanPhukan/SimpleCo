import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export const Navbar = ({ onOpenAccount, onOpenCart, onOpenAdmin, onSelectCategory }) => {
  const { user } = useAuth();
  const { getTotals } = useCart();
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
          <div className="brand-icon-wrapper" aria-hidden="true">🧶</div>
          <div className="brand-text">
            <span className="brand-title">BabyyGurll <span className="brand-accent">Crochet</span></span>
            <span className="brand-subtitle">Handmade with Love ✨</span>
          </div>
        </a>

        {/* Desktop Navigation Links (Clean & Spacious) */}
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
          </ul>
        </nav>

        {/* Right Action Controls (Account & Cart Logo Buttons) */}
        <div className="nav-actions">
          {/* Studio Admin Button */}
          <button 
            className="nav-icon-btn" 
            onClick={onOpenAdmin} 
            aria-label="Studio Admin Panel"
            title="Studio Admin Dashboard (Orders & Catalog)"
            style={{ fontSize: '1rem', background: '#F0F9FF', border: '1px solid #BAE6FD', color: '#0284C7' }}
          >
            ⚡
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
                ✨ Shop All
              </a>
            </li>
            <li>
              <a href="#catalog" onClick={(e) => handleNavCategory(e, 'plushies')}>
                🐰 Amigurumi Plushies
              </a>
            </li>
            <li>
              <a href="#catalog" onClick={(e) => handleNavCategory(e, 'floral')}>
                🌸 Floral & Home Decor
              </a>
            </li>
            <li>
              <a href="#catalog" onClick={(e) => handleNavCategory(e, 'bags')}>
                👜 Bags & Totes
              </a>
            </li>
            <li>
              <a href="#catalog" onClick={(e) => handleNavCategory(e, 'wearables')}>
                ☁️ Wearables & Cardigans
              </a>
            </li>
            <li style={{ borderTop: '1px solid var(--border-subtle)', marginTop: '0.5rem', paddingTop: '0.5rem' }}>
              <a href="#admin" onClick={(e) => { e.preventDefault(); setMobileMenuOpen(false); onOpenAdmin(); }} style={{ color: 'var(--primary)', fontWeight: 700 }}>
                ⚡ Studio Admin Panel
              </a>
            </li>
          </ul>
        </div>
      )}
    </header>
  );
};
