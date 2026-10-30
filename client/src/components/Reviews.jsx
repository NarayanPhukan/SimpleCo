import React, { useState, useEffect } from 'react';
import { useCart } from '../context/CartContext';
import { IconStar, IconCheck, IconClose, IconCamera, IconSparkles } from './Icons';

const INITIAL_REVIEWS = [];

export const Reviews = ({ onShowToast }) => {
  const { addYarnPoints } = useCart();
  const [reviews, setReviews] = useState(() => {
    const saved = localStorage.getItem('bgc_user_reviews_v1');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        return Array.isArray(parsed) 
          ? parsed.filter(r => !['rev-1', 'rev-2', 'rev-3'].includes(r.id))
          : [];
      } catch (e) {
        return [];
      }
    }
    return [];
  });

  const [isWriteModalOpen, setIsWriteModalOpen] = useState(false);
  const [formRating, setFormRating] = useState(5);
  const [formHoverRating, setFormHoverRating] = useState(0);
  const [formName, setFormName] = useState('');
  const [formCity, setFormCity] = useState('');
  const [formProduct, setFormProduct] = useState('Strawberry Cow Amigurumi Plush');
  const [formQuote, setFormQuote] = useState('');
  const [formPhoto, setFormPhoto] = useState('');

  const handleSubmitReview = (e) => {
    e.preventDefault();
    if (!formName.trim() || !formQuote.trim()) {
      if (onShowToast) onShowToast('Please share your name and your thoughts!', '!');
      return;
    }

    const initials = formName.trim().split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) || 'BG';
    const newRev = {
      id: `rev-${Date.now()}`,
      name: formName.trim(),
      city: formCity.trim() || 'India',
      product: formProduct,
      rating: formRating,
      quote: formQuote.trim(),
      verified: true,
      initials,
      photo: formPhoto || null,
      createdAt: new Date().toISOString()
    };

    const userOnlyReviews = (() => {
      const saved = localStorage.getItem('bgc_user_reviews_v1');
      return saved ? JSON.parse(saved) : [];
    })();

    const updatedUserReviews = [newRev, ...userOnlyReviews];
    localStorage.setItem('bgc_user_reviews_v1', JSON.stringify(updatedUserReviews));
    setReviews([newRev, ...reviews]);

    // Reward user with 50 Yarn Points
    if (addYarnPoints) addYarnPoints(50);
    if (onShowToast) onShowToast('Thank you! Review published + 50 Yarn Points earned 🧶✨', '✓');

    setIsWriteModalOpen(false);
    setFormName('');
    setFormCity('');
    setFormQuote('');
    setFormPhoto('');
  };

  return (
    <section className="testimonials-section" id="reviews">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">Verified Collector Reviews</span>
          <h2 className="section-title">Real notes from people who took us home</h2>
          <p className="section-subtitle">
            Honest words from people cuddling our plushies and carrying our bags across India.
          </p>
        </div>

        {/* Review Highlights & Write Review CTA */}
        {reviews.length > 0 ? (
          <>
            <div className="reviews-summary-bar">
              <div className="rating-score-box">
                <div className="rating-huge-number">
                  {(reviews.reduce((acc, r) => acc + (Number(r.rating) || 5), 0) / reviews.length).toFixed(1)}
                </div>
                <div className="rating-huge-stars">
                  <span style={{ color: '#F59E0B' }}>★★★★★</span>
                  <span className="rating-caption">
                    Based on {reviews.length} collector {reviews.length === 1 ? 'review' : 'reviews'}
                  </span>
                </div>
              </div>

              <div className="reviews-cta-wrap">
                <button 
                  className="btn btn-primary"
                  onClick={() => setIsWriteModalOpen(true)}
                  style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
                >
                  <IconSparkles size={16} />
                  <span>Share Your Note (+50 Pts)</span>
                </button>
              </div>
            </div>

            {/* Reviews Grid */}
            <div className="reviews-grid">
              {reviews.map((rev) => (
                <div key={rev.id} className="review-card">
                  <div>
                    <div className="review-card-top-row">
                      <div className="review-stars">
                        {'★'.repeat(rev.rating)}
                        {'☆'.repeat(Math.max(0, 5 - rev.rating))}
                      </div>
                      {rev.verified && (
                        <span className="verified-buyer-chip">
                          <IconCheck size={11} />
                          <span>Verified Adoption</span>
                        </span>
                      )}
                    </div>

                    <p className="review-quote">"{rev.quote}"</p>

                    {rev.photo && (
                      <div className="review-photo-preview">
                        <img src={rev.photo} alt={`Photo from ${rev.name}`} loading="lazy" />
                      </div>
                    )}
                  </div>

                  <div className="review-author">
                    <div className="avatar-circle">{rev.initials || 'BG'}</div>
                    <div className="author-meta">
                      <h5>{rev.name}</h5>
                      <p>{rev.city} • {rev.product}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </>
        ) : (
          <div className="reviews-empty-banner" style={{ textAlign: 'center', padding: '3rem 1.5rem', background: '#FFFFFF', borderRadius: 'var(--radius-lg)', border: '1px dashed var(--border-card)', margin: '1.5rem 0' }}>
            <span style={{ fontSize: '2.5rem', display: 'block', marginBottom: '0.75rem' }}>🧶✨</span>
            <h4 style={{ fontFamily: 'var(--font-serif)', color: 'var(--text-dark)', fontSize: '1.25rem', marginBottom: '0.5rem' }}>Be the First to Review</h4>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.92rem', maxWidth: '440px', margin: '0 auto 1.5rem', lineHeight: '1.5' }}>
              Received a handcrafted piece from our studio? Share your adoption story and yarn notes with our community!
            </p>
            <button 
              className="btn btn-primary"
              onClick={() => setIsWriteModalOpen(true)}
              style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
            >
              <IconSparkles size={16} />
              <span>Write the First Review (+50 Pts)</span>
            </button>
          </div>
        )}
      </div>

      {/* Write a Review Modal */}
      {isWriteModalOpen && (
        <div 
          className="modal-overlay active"
          onClick={(e) => {
            if (e.target.classList.contains('modal-overlay')) setIsWriteModalOpen(false);
          }}
        >
          <div className="modal-card review-modal-card" role="dialog" aria-modal="true" aria-label="Write a Review">
            <button className="modal-close-btn" onClick={() => setIsWriteModalOpen(false)} aria-label="Close modal">&times;</button>

            <div className="review-modal-header">
              <div className="studio-badge">
                <IconSparkles size={13} />
                <span>Collector Feedback</span>
              </div>
              <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.4rem', marginTop: '0.4rem', color: 'var(--text-dark)' }}>
                Share Your Stitch Story
              </h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                How does your handmade crochet piece feel in your home? Earn 50 Yarn Points on submission!
              </p>
            </div>

            <form onSubmit={handleSubmitReview} className="review-modal-form">
              {/* Star Rating Picker */}
              <div className="form-group" style={{ textAlign: 'center', margin: '1rem 0' }}>
                <label className="form-label">Your Overall Rating:</label>
                <div className="star-rating-picker">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      type="button"
                      key={star}
                      className="star-btn"
                      onClick={() => setFormRating(star)}
                      onMouseEnter={() => setFormHoverRating(star)}
                      onMouseLeave={() => setFormHoverRating(0)}
                    >
                      <IconStar 
                        size={28} 
                        fill={(formHoverRating || formRating) >= star ? '#F59E0B' : 'none'}
                        stroke={(formHoverRating || formRating) >= star ? '#F59E0B' : '#CBD5E1'}
                      />
                    </button>
                  ))}
                </div>
              </div>

              <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                <div className="form-group">
                  <label className="form-label" htmlFor="revName">Your Name:</label>
                  <input
                    type="text"
                    id="revName"
                    required
                    className="form-input"
                    placeholder="e.g. Diya Sharma"
                    value={formName}
                    onChange={(e) => setFormName(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="revCity">Your City:</label>
                  <input
                    type="text"
                    id="revCity"
                    className="form-input"
                    placeholder="e.g. Jaipur"
                    value={formCity}
                    onChange={(e) => setFormCity(e.target.value)}
                  />
                </div>
              </div>

              <div className="form-group" style={{ marginTop: '0.75rem' }}>
                <label className="form-label" htmlFor="revProd">Piece Adopted:</label>
                <select
                  id="revProd"
                  className="form-input"
                  value={formProduct}
                  onChange={(e) => setFormProduct(e.target.value)}
                >
                  <option value="Strawberry Cow Amigurumi Plush">Strawberry Cow Amigurumi Plush</option>
                  <option value="Everlasting Pastel Tulip Bouquet">Everlasting Pastel Tulip Bouquet</option>
                  <option value="Daisy Granny Square Tote Bag">Daisy Granny Square Tote Bag</option>
                  <option value="Pastel Cloud Cardigan">Pastel Cloud Cardigan</option>
                  <option value="Lavender Lop Bunny">Lavender Lop Bunny</option>
                  <option value="Custom Bouquet / Commission">Custom Bouquet / Commission</option>
                </select>
              </div>

              <div className="form-group" style={{ marginTop: '0.75rem' }}>
                <label className="form-label" htmlFor="revQuote">Your Honest Thoughts:</label>
                <textarea
                  id="revQuote"
                  required
                  rows={4}
                  className="form-input"
                  placeholder="Tell us about the yarn softness, the packaging, or who you gifted it to..."
                  value={formQuote}
                  onChange={(e) => setFormQuote(e.target.value)}
                />
              </div>

              {/* Photo Simulator */}
              <div className="form-group" style={{ marginTop: '0.75rem' }}>
                <label className="form-label">Attach Desk/Home Snapshot (Optional):</label>
                <div className="photo-picker-row">
                  {[
                    { label: 'Plushie Desk', url: '/assets/images/strawberry-cow.jpg' },
                    { label: 'Bouquet Vase', url: '/assets/images/tulip-bouquet.jpg' },
                    { label: 'Tote Outdoors', url: '/assets/images/granny-bag.jpg' }
                  ].map((preset, idx) => (
                    <button
                      type="button"
                      key={idx}
                      className={`photo-preset-chip ${formPhoto === preset.url ? 'active' : ''}`}
                      onClick={() => setFormPhoto(formPhoto === preset.url ? '' : preset.url)}
                    >
                      <IconCamera size={14} />
                      <span>{preset.label}</span>
                    </button>
                  ))}
                </div>
              </div>

              <button 
                type="submit" 
                className="btn btn-primary"
                style={{ width: '100%', marginTop: '1.2rem', padding: '0.8rem' }}
              >
                Post Review & Claim Points 🌸
              </button>
            </form>
          </div>
        </div>
      )}
    </section>
  );
};
