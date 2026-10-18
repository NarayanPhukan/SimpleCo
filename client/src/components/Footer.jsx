import React, { useState } from 'react';
import { IconInstagram, IconPinterest } from './Icons';

export const Footer = ({ onOpenAccount, onScrollToTrack, onSelectCategory, onOpenAdmin, onShowToast }) => {
  const [newsletterEmail, setNewsletterEmail] = useState('');

  const handleSubscribe = (e) => {
    e.preventDefault();
    if (newsletterEmail) {
      onShowToast('Subscribed successfully! Welcome to Stitch Club', '✓');
      setNewsletterEmail('');
    }
  };

  const handleCategoryClick = (e, cat) => {
    e.preventDefault();
    onSelectCategory(cat);
    const catElem = document.getElementById('catalog');
    if (catElem) catElem.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <footer className="site-footer">
      <div className="container">
        {/* Trust Badges Ribbon */}
        <div className="footer-trust-ribbon">
          <div className="footer-trust-item">
            <span className="footer-trust-icon">🌿</span>
            <div>
              <strong>100% Milk Cotton</strong>
              <small>Hypoallergenic & Heirloom Soft</small>
            </div>
          </div>
          <div className="footer-trust-item">
            <span className="footer-trust-icon">🎀</span>
            <div>
              <strong>Plastic-Free Packaging</strong>
              <small>Keepsake box with satin ribbons</small>
            </div>
          </div>
          <div className="footer-trust-item">
            <span className="footer-trust-icon">📦</span>
            <div>
              <strong>Tracked Courier Pan-India</strong>
              <small>Bluedart, Delhivery & DTDC</small>
            </div>
          </div>
          <div className="footer-trust-item">
            <span className="footer-trust-icon">🔒</span>
            <div>
              <strong>Secure UPI & Cards</strong>
              <small>Protected via 256-bit encryption</small>
            </div>
          </div>
        </div>

        <div className="footer-grid">
          <div className="footer-brand">
            <h3>BabyyGurll Crochet</h3>
            <p>
              An independent slow-craft studio creating everlasting floral bouquets, cuddly desk companions, and heirloom accessories. Every piece is hand-hooked one stitch at a time with pure milk cotton yarn.
            </p>
            <div className="social-links">
              <a href="https://instagram.com" target="_blank" rel="noopener noreferrer" className="social-icon" aria-label="Instagram" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                <IconInstagram size={18} />
              </a>
              <a href="https://pinterest.com" target="_blank" rel="noopener noreferrer" className="social-icon" aria-label="Pinterest" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                <IconPinterest size={18} />
              </a>
            </div>
          </div>

          <div className="footer-col">
            <h4>Boutique Catalog</h4>
            <ul className="footer-links">
              <li><a href="#catalog" onClick={(e) => handleCategoryClick(e, 'all')}>Complete Collection</a></li>
              <li><a href="#catalog" onClick={(e) => handleCategoryClick(e, 'plushies')}>Amigurumi Desk Friends</a></li>
              <li><a href="#catalog" onClick={(e) => handleCategoryClick(e, 'floral')}>Everlasting Floral Bouquets</a></li>
              <li><a href="#catalog" onClick={(e) => handleCategoryClick(e, 'bags')}>Bags & Granny Squares</a></li>
              <li><a href="#studio" onClick={(e) => { e.preventDefault(); window.location.hash = '#studio'; }}>Bouquet Builder Studio</a></li>
            </ul>
          </div>

          <div className="footer-col">
            <h4>Collector Care</h4>
            <ul className="footer-links">
              <li><a href="#orderTracking" onClick={(e) => { e.preventDefault(); onScrollToTrack(); }}>Track Your Parcel</a></li>
              <li><a href="#account" onClick={(e) => { e.preventDefault(); onOpenAccount(); }}>Stitch Club Yarn Points</a></li>
              <li><a href="#account" onClick={(e) => { e.preventDefault(); onOpenAccount(); }}>Saved Addresses & Orders</a></li>
              <li><a href="#reviews">Verified Collector Reviews</a></li>
              <li><a href="#faq">Yarn Wash & Care Guide</a></li>
            </ul>
          </div>

          <div className="footer-col">
            <h4>The Stitch Club</h4>
            <p style={{ color: '#D4BFC4', fontSize: '0.85rem', lineHeight: '1.5' }}>
              Join our cozy circle for private batch drops, seasonal pattern previews, and 15% off your first handmade piece.
            </p>
            <form className="newsletter-form" onSubmit={handleSubscribe}>
              <input 
                type="email" 
                className="newsletter-input" 
                placeholder="Enter your email address" 
                autoComplete="email" 
                required 
                value={newsletterEmail}
                onChange={(e) => setNewsletterEmail(e.target.value)}
              />
              <button type="submit" className="btn btn-primary" style={{ padding: '0.65rem 1.25rem', fontSize: '0.84rem', fontWeight: 700 }}>
                Join
              </button>
            </form>
          </div>
        </div>

        <div className="footer-bottom">
          <p>&copy; 2026 BabyyGurll Crochet. Handcrafted with love in Assam, India.</p>
          <p>
            All rights reserved • 
            <a 
              href="#admin" 
              onClick={(e) => { e.preventDefault(); if (onOpenAdmin) onOpenAdmin(); }} 
              style={{ marginLeft: '0.5rem', color: 'rgba(255,255,255,0.4)', textDecoration: 'none', fontSize: '0.75rem' }}
            >
              Atelier Portal
            </a>
          </p>
        </div>
      </div>
    </footer>
  );
};
