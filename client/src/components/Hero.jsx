import React from 'react';

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
            <button className="btn btn-primary" onClick={onExplore}>
              Explore Collection 🍓
            </button>
            <a href="#catalog" className="btn btn-secondary">
              View Bestsellers ✨
            </a>
          </div>

          <div className="hero-trust-badges">
            <div className="trust-item">
              <span className="icon">🧶</span>
              <span>100% Hand-Crocheted</span>
            </div>
            <div className="trust-item">
              <span className="icon">🚚</span>
              <span>Pan-India Delivery</span>
            </div>
            <div className="trust-item">
              <span className="icon">⭐</span>
              <span>4.9/5 (2,400+ Reviews)</span>
            </div>
          </div>
        </div>

        {/* Hero Visual Card */}
        <div className="hero-visual">
          <div className="hero-card-stack">
            <div className="floating-glass-card card-top-right">
              <div className="floating-icon">🍓</div>
              <div className="floating-meta">
                <h4>Strawberry Cow</h4>
                <p>#1 Bestseller • 184 Loved</p>
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
              <div className="floating-icon">✨</div>
              <div className="floating-meta">
                <h4>Made with Love</h4>
                <p>Loop-by-loop craftsmanship</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
