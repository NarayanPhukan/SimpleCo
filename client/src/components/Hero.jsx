import React from 'react';
import { IconArrowRight, IconAward, IconTruck, IconShield } from './Icons';

export const Hero = ({ onExplore }) => {
  return (
    <section className="hero-section" id="hero">
      <div className="container hero-grid">
        <div className="hero-content">
          <div className="hero-pill">
            <span className="pulse-dot"></span>
            <span>Handmade in small batches right here in India 🧶</span>
          </div>
          <h1 className="hero-title">
            Handmade with love for <span className="italic-sparkle">cozy days</span> & gentle hearts.
          </h1>
          <p className="hero-description">
            Hey there! We crochet every single piece by hand using soft, snuggly milk-cotton yarn. No factory machines, no rushing — just cute handmade companions made to make you smile every day.
          </p>

          <div className="hero-cta-group">
            <button className="btn btn-primary" onClick={onExplore} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
              <span>Browse Collection</span>
              <IconArrowRight size={16} />
            </button>
            <a href="#catalog" className="btn btn-secondary">
              See What's New
            </a>
          </div>

          <div className="hero-trust-badges">
            <div className="trust-item">
              <span className="icon" style={{ display: 'inline-flex', alignItems: 'center' }}><IconAward size={16} /></span>
              <span>Hand-crocheted with care</span>
            </div>
            <div className="trust-item">
              <span className="icon" style={{ display: 'inline-flex', alignItems: 'center' }}><IconTruck size={16} /></span>
              <span>Delivered across India</span>
            </div>
            <div className="trust-item">
              <span className="icon" style={{ display: 'inline-flex', alignItems: 'center' }}><IconShield size={16} /></span>
              <span>Over 2,000+ happy homes 💕</span>
            </div>
          </div>
        </div>

        {/* Hero Visual Card */}
        <div className="hero-visual">
          <div className="hero-card-stack">
            <div className="floating-glass-card card-top-right">
              <div className="floating-icon" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                <IconAward size={18} />
              </div>
              <div className="floating-meta">
                <h4>Strawberry Cow</h4>
                <p>Everyone's favorite little buddy 🍓</p>
              </div>
            </div>

            <div className="hero-image-frame">
              <img 
                src="/assets/images/hero-banner.jpg" 
                alt="Handmade crochet creations with pastel yarn and cute plushies" 
                width="700" 
                height="525" 
                fetchPriority="high" 
              />
            </div>

            <div className="floating-glass-card card-bottom-left">
              <div className="floating-icon" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                <IconShield size={18} />
              </div>
              <div className="floating-meta">
                <h4>Stitch by Stitch</h4>
                <p>Pure soft milk-cotton yarn 🌸</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
