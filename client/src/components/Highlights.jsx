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
              <h4>Slow Fashion & Craft</h4>
              <p>Zero mass production. Each stitch made with care and intention.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">
              <IconAward size={22} />
            </div>
            <div className="highlight-text">
              <h4>Gift-Ready Packaging</h4>
              <p>Wrapped in signature matte parchment with satin ribbon finishing.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">
              <IconPackage size={22} />
            </div>
            <div className="highlight-text">
              <h4>Live Order Tracking</h4>
              <p>Follow every stage from yarn winding to front doorstep delivery.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">
              <IconTag size={22} />
            </div>
            <div className="highlight-text">
              <h4>Custom Colorways</h4>
              <p>Personalized palette options and complimentary gift cards.</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
