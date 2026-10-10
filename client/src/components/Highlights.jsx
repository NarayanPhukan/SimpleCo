import React from 'react';

export const Highlights = () => {
  return (
    <section className="highlights-banner">
      <div className="container">
        <div className="highlights-grid">
          <div className="highlight-box">
            <div className="highlight-icon">🪡</div>
            <div className="highlight-text">
              <h4>Slow Fashion & Craft</h4>
              <p>Zero mass production. Each stitch made with love & intention.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">🎀</div>
            <div className="highlight-text">
              <h4>Aesthetic Gift Ready</h4>
              <p>Wrapped in Korean matte floral paper with satin ribbon & scented mist.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">📦</div>
            <div className="highlight-text">
              <h4>Live Order Tracking</h4>
              <p>Follow every stage from yarn winding to front doorstep delivery.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">💌</div>
            <div className="highlight-text">
              <h4>Custom Colorways</h4>
              <p>Personalized palette options and gift note cards for loved ones.</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
