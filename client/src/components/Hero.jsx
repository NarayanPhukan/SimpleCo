import React from 'react';
import { IconArrowRight, IconAward, IconTruck, IconShield, IconSparkles } from './Icons';

export const Hero = ({ onExplore }) => {
  return (
    <section className="hero-section" id="hero">
      <div className="container hero-grid">
        <div className="hero-content">
          <div className="hero-pill">
            <span className="pulse-dot"></span>
            <span>✨ Small-Batch Artisanal Crochet • 100% Milk Cotton • India</span>
          </div>

          <h1 className="hero-title">
            Heirloom Handcrafted Crochet for <span className="italic-sparkle">Cozy Spaces</span> & Gentle Hearts.
          </h1>

          <p className="hero-description">
            Each creation is patiently hand-hooked stitch-by-stitch using pure, ultra-soft milk cotton yarn. Zero assembly lines or factory shortcuts — just mindful, heirloom-quality pieces crafted to bring lasting warmth to your everyday moments.
          </p>

          <div className="hero-cta-group">
            <button className="btn btn-primary" onClick={onExplore} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
              <span>Browse Studio Catalog</span>
              <IconArrowRight size={16} />
            </button>
            <a 
              href="#studio" 
              className="btn btn-secondary" 
              onClick={(e) => {
                e.preventDefault();
                window.location.hash = '#studio';
              }}
              style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem' }}
            >
              <IconSparkles size={15} />
              <span>Bespoke Bouquet Studio</span>
            </a>
          </div>

          <div className="hero-trust-badges">
            <div className="trust-item">
              <span className="icon" style={{ display: 'inline-flex', alignItems: 'center' }}><IconAward size={15} /></span>
              <span>100% Hand-Hooked</span>
            </div>
            <div className="trust-item">
              <span className="icon" style={{ display: 'inline-flex', alignItems: 'center' }}><IconTruck size={15} /></span>
              <span>Delivered Pan-India</span>
            </div>
            <div className="trust-item">
              <span className="icon" style={{ display: 'inline-flex', alignItems: 'center' }}><IconShield size={15} /></span>
              <span>Plastic-Free Keepsake Boxes</span>
            </div>
          </div>
        </div>

        {/* Hero Visual Card Stack */}
        <div className="hero-visual">
          <div className="hero-card-stack">
            <div className="floating-glass-card card-top-right">
              <div className="floating-icon" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                <IconAward size={18} />
              </div>
              <div className="floating-meta">
                <h4>Strawberry Cow</h4>
                <p>Artisanal Studio Signature</p>
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
              <div className="hero-maker-seal">
                <span>100% Handcrafted</span>
                <small>Assam Studio • India</small>
              </div>
            </div>

            <div className="floating-glass-card card-bottom-left">
              <div className="floating-icon" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                <IconShield size={18} />
              </div>
              <div className="floating-meta">
                <h4>Pure Milk-Cotton</h4>
                <p>Hypoallergenic & Ultra-Soft</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
