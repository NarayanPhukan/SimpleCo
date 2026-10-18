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
              <p>Zero factory molds. Hand-hooked stitch by stitch with 100% natural milk-cotton yarn.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">
              <IconAward size={22} />
            </div>
            <div className="highlight-text">
              <h4>Keepsake Gift Unboxing</h4>
              <p>Nestled in butter paper, satin ribbons, and a personalized calligraphy message card.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">
              <IconPackage size={22} />
            </div>
            <div className="highlight-text">
              <h4>Live Pan-India Tracking</h4>
              <p>Follow your parcel step-by-step from our studio workshop to your doorstep.</p>
            </div>
          </div>
          <div className="highlight-box">
            <div className="highlight-icon">
              <IconTag size={22} />
            </div>
            <div className="highlight-text">
              <h4>Custom Colors & Notes</h4>
              <p>Choose custom yarn hues, personalize bouquet arrangements, or add free gift notes.</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
