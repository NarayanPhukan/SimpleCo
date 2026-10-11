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
        <div className="footer-grid">
          <div className="footer-brand">
            <h3>BabyyGurll Crochet</h3>
            <p>
              Hand-stitched bespoke crafts made with gentle intention, premium milk cotton, and heirloom quality.
            </p>
            <div className="social-links">
              <a href="#" className="social-icon" aria-label="Instagram" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                <IconInstagram size={18} />
              </a>
              <a href="#" className="social-icon" aria-label="Pinterest" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                <IconPinterest size={18} />
              </a>
            </div>
          </div>

          <div className="footer-col">
            <h4>Explore</h4>
            <ul className="footer-links">
              <li><a href="#catalog" onClick={(e) => handleCategoryClick(e, 'all')}>Shop All</a></li>
              <li><a href="#catalog" onClick={(e) => handleCategoryClick(e, 'plushies')}>Amigurumi Plushies</a></li>
              <li><a href="#catalog" onClick={(e) => handleCategoryClick(e, 'floral')}>Floral Bouquets</a></li>
              <li><a href="#catalog" onClick={(e) => handleCategoryClick(e, 'wearables')}>Bags & Wearables</a></li>
              <li><a href="#orderTracking" onClick={(e) => { e.preventDefault(); onScrollToTrack(); }}>Track an Order</a></li>
            </ul>
          </div>

          <div className="footer-col">
            <h4>Customer Care</h4>
            <ul className="footer-links">
              <li><a href="#orderTracking" onClick={(e) => { e.preventDefault(); onScrollToTrack(); }}>Order Tracking</a></li>
              <li><a href="#account" onClick={(e) => { e.preventDefault(); onOpenAccount(); }}>Saved Addresses</a></li>
              <li><a href="#account" onClick={(e) => { e.preventDefault(); onOpenAccount(); }}>My Account</a></li>
              <li><a href="#">Shipping & Returns</a></li>
              <li><a href="#">Yarn Care Guide</a></li>
            </ul>
          </div>

          <div className="footer-col">
            <h4>Stitch Club Newsletter</h4>
            <p style={{ color: '#99D5FF', fontSize: '0.85rem' }}>
              Get early access to secret drops and 15% off your first handcrafted piece.
            </p>
            <form className="newsletter-form" onSubmit={handleSubscribe}>
              <input 
                type="email" 
                className="newsletter-input" 
                placeholder="Your email address" 
                autoComplete="email" 
                required 
                value={newsletterEmail}
                onChange={(e) => setNewsletterEmail(e.target.value)}
              />
              <button type="submit" className="btn btn-primary" style={{ padding: '0.65rem 1.2rem', fontSize: '0.85rem' }}>
                Join
              </button>
            </form>
          </div>
        </div>

        <div className="footer-bottom">
          <p>&copy; 2026 BabyyGurll Crochet Studio India. All rights reserved. Handcrafted with love.</p>
          <p>
            Nationwide Express Tracked Dispatch • 
            <a 
              href="#admin" 
              onClick={(e) => { e.preventDefault(); if (onOpenAdmin) onOpenAdmin(); }} 
              style={{ marginLeft: '0.5rem', color: 'var(--text-muted)', textDecoration: 'none', fontSize: '0.75rem' }}
            >
              Staff Portal
            </a>
          </p>
        </div>
      </div>
    </footer>
  );
};
