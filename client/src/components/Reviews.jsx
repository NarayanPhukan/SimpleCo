import React from 'react';

export const Reviews = () => {
  return (
    <section className="testimonials-section">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">Customer Love</span>
          <h2 className="section-title">Loved by 2,400+ Cozy Souls</h2>
          <p className="section-subtitle">Real words from our lovely community of crochet collectors.</p>
        </div>

        <div className="reviews-grid">
          <div className="review-card">
            <div>
              <div className="review-stars">★★★★★</div>
              <p className="review-quote">
                "Literally the softest plushie I have ever owned! The stitching is so neat, and the packaging smelled like sweet vanilla. 100/10 recommend BabyyGurll Crochet!"
              </p>
            </div>
            <div className="review-author">
              <div className="avatar-circle">CM</div>
              <div className="author-meta">
                <h5>Chloe M.</h5>
                <p>Verified Buyer • Strawberry Cow</p>
              </div>
            </div>
          </div>

          <div className="review-card">
            <div>
              <div className="review-stars">★★★★★</div>
              <p className="review-quote">
                "Ordered this for my bestie's dorm room warming gift. She cried happy tears! They look stunning on her desk and won't ever wilt. The wrapping paper and satin ribbon are perfection."
              </p>
            </div>
            <div className="review-author">
              <div className="avatar-circle">JP</div>
              <div className="author-meta">
                <h5>Jessica P.</h5>
                <p>Verified Buyer • Pastel Tulip Bouquet</p>
              </div>
            </div>
          </div>

          <div className="review-card">
            <div>
              <div className="review-stars">★★★★★</div>
              <p className="review-quote">
                "I take this bag everywhere. Super sturdy, straps don't stretch out even when loaded with my iPad and water bottle. Constant compliments whenever I go to coffee shops!"
              </p>
            </div>
            <div className="review-author">
              <div className="avatar-circle">AK</div>
              <div className="author-meta">
                <h5>Aaliyah K.</h5>
                <p>Verified Buyer • Daisy Granny Square Tote</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
