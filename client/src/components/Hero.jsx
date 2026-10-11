import React from 'react';
import { IconArrowRight, IconAward, IconTruck, IconShield } from './Icons';

export const Hero = ({ onExplore }) => {
  return (
    <section className="hero-section" id="hero">
      <div className="container hero-grid">
        <div className="hero-content">
          <div className="hero-pill">
            <span className="pulse-dot"></span>
            <span>Handmade in India • Limited Artisanal Batches</span>
          </div>
          <h1 className="hero-title">
            Hand-Stitched Magic for <span className="italic-sparkle">Soft Souls</span> & Cozy Hearts.
          </h1>
          <p className="hero-description">
            Each bespoke piece is lovingly crocheted by hand using premium, hypoallergenic milk cotton and cloudfill chenille. Designed to bring gentle whimsy, warmth, and aesthetic joy into your daily rituals.
          </p>

          <div className="hero-cta-group">
            <button className="btn btn-primary" onClick={onExplore} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
              <span>Explore Collection</span>
              <IconArrowRight size={16} />
            </button>
            <a href="#catalog" className="btn btn-secondary">
              View Bestsellers
            </a>
          </div>

          <div className="hero-trust-badges">
            <div className="trust-item">
              <span className="icon" style={{ display: 'inline-flex', alignItems: 'center' }}><IconAward size={16} /></span>
              <span>100% Hand-Crocheted</span>
            </div>
            <div className="trust-item">
              <span className="icon" style={{ display: 'inline-flex', alignItems: 'center' }}><IconTruck size={16} /></span>
              <span>Pan-India Delivery</span>
            </div>
            <div className="trust-item">
              <span className="icon" style={{ display: 'inline-flex', alignItems: 'center' }}><IconShield size={16} /></span>
              <span>4.9/5 Rating (2,400+ Orders)</span>
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
                <p>#1 Bestseller • Verified Craft</p>
              </div>
            </div>

            <div className="hero-image-frame">
              <img 
                src="/assets/images/hero-banner.jpg" 
                alt="Artisanal crochet workshop with pastel pink yarn and handmade plushies" 
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
                <h4>Artisanal Quality</h4>
                <p>Loop-by-loop craftsmanship</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
