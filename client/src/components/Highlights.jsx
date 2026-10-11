import React from 'react';
import { IconAward, IconPackage, IconStudio, IconTag } from './Icons';

export const Highlights = () => {
  return (
    <section className="highlights-banner">
      <div className="container">
        <div className="highlights-grid">
          <div className="highlight-box">
            <div className="highlight-icon">
              <IconStudio size={22} />
            </div>
            <div className="highlight-text">
              <h4>Slow, Mindful Craft</h4>
              <p>Zero factory molds. Every piece is hand-hooked one stitch at a time.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">
              <IconAward size={22} />
            </div>
            <div className="highlight-text">
              <h4>Cute Gift Packaging</h4>
              <p>Wrapped in pretty paper with ribbons & a sweet handwritten note.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">
              <IconPackage size={22} />
            </div>
            <div className="highlight-text">
              <h4>Live Order Tracking</h4>
              <p>Follow your order step-by-step from yarn winding to your doorstep.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">
              <IconTag size={22} />
            </div>
            <div className="highlight-text">
              <h4>Custom Colors & Notes</h4>
              <p>Want a special shade or a gift message? Just tell us at checkout!</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
