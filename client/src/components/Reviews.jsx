import React from 'react';

export const Reviews = () => {
  return (
    <section className="testimonials-section">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">From Happy Homes 💌</span>
          <h2 className="section-title">Real notes from people who took us home</h2>
          <p className="section-subtitle">Honest words from people cuddling our plushies and carrying our bags across India.</p>
        </div>

        <div className="reviews-grid">
          <div className="review-card">
            <div>
              <div className="review-stars">★★★★★</div>
              <p className="review-quote">
                "Got the Strawberry Cow for my hostel desk and everyone in my wing keeps borrowing it! It’s surprisingly soft and the blush pink cheeks are so well done. Came packed with a handwritten note and cute stickers too."
              </p>
            </div>
            <div className="review-author">
              <div className="avatar-circle">AS</div>
              <div className="author-meta">
                <h5>Ananya S.</h5>
                <p>Pune • Strawberry Cow</p>
              </div>
            </div>
          </div>

          <div className="review-card">
            <div>
              <div className="review-stars">★★★★★</div>
              <p className="review-quote">
                "Ordered the pastel tulip bouquet for my mom's birthday because real flowers always die in 3 days. She literally keeps them on her dining table now. The stem wire is sturdy and bends nicely into any vase."
              </p>
            </div>
            <div className="review-author">
              <div className="avatar-circle">RK</div>
              <div className="author-meta">
                <h5>Rhea K.</h5>
                <p>Bengaluru • Pastel Tulip Bouquet</p>
              </div>
            </div>
          </div>

          <div className="review-card">
            <div>
              <div className="review-stars">★★★★☆</div>
              <p className="review-quote">
                "The Daisy Tote bag is spacious enough for my daily notebook and water bottle. Took about 4 days to stitch because of backorders, but totally worth the wait — the quality is far better than fast-fashion bags."
              </p>
            </div>
            <div className="review-author">
              <div className="avatar-circle">TM</div>
              <div className="author-meta">
                <h5>Tanvi M.</h5>
                <p>Mumbai • Daisy Granny Square Tote</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
