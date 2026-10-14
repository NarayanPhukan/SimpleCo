import React, { useState, useEffect } from 'react';
import { api } from '../services/api';

export const AnnouncementBar = () => {
  const [announcement, setAnnouncement] = useState({
    active: true,
    text: 'Free delivery across India on orders over ₹999 • Handcrafted with 100% milk cotton yarn',
    badge: 'Code: BABYYGURLL for 15% OFF'
  });

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

  if (!announcement.active) return null;

  return (
    <aside className="announcement-bar" id="announcementBar">
      <span>{announcement.text}</span>
      {announcement.badge && <span className="badge">{announcement.badge}</span>}
    </aside>
  );
};
