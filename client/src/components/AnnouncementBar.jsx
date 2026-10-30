import React, { useState, useEffect, useMemo } from 'react';
import { api } from '../services/api';

export const AnnouncementBar = () => {
  const [announcement, setAnnouncement] = useState({
    active: true,
    text: 'Free delivery across India on orders over ₹999 • Handcrafted with 100% milk cotton yarn',
    badge: 'Code: BABYYGURLL for 15% OFF'
  });

  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const [touchStartX, setTouchStartX] = useState(null);

  useEffect(() => {
    let isMounted = true;
    api.getSettings()
      .then(res => {
        if (isMounted && res.data) {
          setAnnouncement({
            active: res.data.announcementActive !== false,
            text: res.data.announcementText || 'Free delivery across India on orders over ₹999 • Handcrafted with 100% milk cotton yarn',
            badge: res.data.announcementBadge || 'Code: BABYYGURLL for 15% OFF'
          });
        }
      })
      .catch(() => {});
    return () => { isMounted = false; };
  }, []);

  // Build clean, thin-line slides from announcement settings
  const slides = useMemo(() => {
    const list = [];
    const rawText = announcement.text || '';
    
    if (rawText.includes('•')) {
      const parts = rawText.split('•').map(s => s.trim()).filter(Boolean);
      parts.forEach((p, idx) => {
        list.push({
          id: `text-${idx}`,
          text: p,
          badge: null,
          icon: idx === 0 ? '✨' : '🧶'
        });
      });
    } else if (rawText) {
      list.push({
        id: 'text-main',
        text: rawText,
        badge: null,
        icon: '✨'
      });
    }

    if (announcement.badge) {
      list.push({
        id: 'badge-slide',
        text: 'Limited Boutique Offer:',
        badge: announcement.badge,
        icon: '🏷️'
      });
    }

    // Default curated slides if only 1-2 exist
    if (list.length < 3) {
      list.push({
        id: 'handcrafted-slide',
        text: 'Handcrafted with 100% Hypoallergenic Milk Cotton Yarn',
        badge: null,
        icon: '🧶'
      });
      list.push({
        id: 'shipping-slide',
        text: 'Plastic-Free Keepsake Packaging & Tracked Pan-India Delivery',
        badge: null,
        icon: '🌿'
      });
    }

    return list;
  }, [announcement.text, announcement.badge]);

  // Slideshow auto-advance every 3.8s
  useEffect(() => {
    if (isPaused || slides.length <= 1) return;
    const timer = setInterval(() => {
      setCurrentIndex(prev => (prev + 1) % slides.length);
    }, 3800);
    return () => clearInterval(timer);
  }, [isPaused, slides.length]);

  const handlePrev = (e) => {
    e?.stopPropagation();
    setCurrentIndex(prev => (prev - 1 + slides.length) % slides.length);
  };

  const handleNext = (e) => {
    e?.stopPropagation();
    setCurrentIndex(prev => (prev + 1) % slides.length);
  };

  const handleTouchStart = (e) => {
    setTouchStartX(e.touches[0].clientX);
  };

  const handleTouchEnd = (e) => {
    if (touchStartX === null) return;
    const diff = touchStartX - e.changedTouches[0].clientX;
    if (Math.abs(diff) > 40) {
      if (diff > 0) handleNext();
      else handlePrev();
    }
    setTouchStartX(null);
  };

  if (!announcement.active || slides.length === 0) return null;

  const currentSlide = slides[currentIndex] || slides[0];

  return (
    <aside 
      className="announcement-bar" 
      id="announcementBar"
      role="region"
      aria-label="Announcements Slideshow"
      onMouseEnter={() => setIsPaused(true)}
      onMouseLeave={() => setIsPaused(false)}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      {slides.length > 1 && (
        <button 
          type="button"
          className="announcement-arrow announcement-arrow-prev" 
          onClick={handlePrev}
          aria-label="Previous announcement"
          title="Previous slide"
        >
          &#8249;
        </button>
      )}

      <div className="announcement-slides-viewport" aria-live="polite">
        <div key={currentIndex} className="announcement-slide-item">
          {currentSlide.icon && <span className="announcement-icon" aria-hidden="true">{currentSlide.icon}</span>}
          <span className="announcement-text">{currentSlide.text}</span>
          {currentSlide.badge && <span className="badge announcement-badge">{currentSlide.badge}</span>}
        </div>
      </div>

      {slides.length > 1 && (
        <button 
          type="button"
          className="announcement-arrow announcement-arrow-next" 
          onClick={handleNext}
          aria-label="Next announcement"
          title="Next slide"
        >
          &#8250;
        </button>
      )}
    </aside>
  );
};
