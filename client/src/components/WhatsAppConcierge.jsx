import React, { useState } from 'react';
import { IconWhatsApp, IconClose, IconArrowRight } from './Icons';

export const WhatsAppConcierge = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [message, setMessage] = useState('');

  const CRAFTER_PHONE = '919876543210'; // Studio WhatsApp Hotline

  const quickPrompts = [
    { label: '🎨 Custom Color Request', text: "Hi! I'd love to ask about custom yarn colors for a crochet piece." },
    { label: '⚡ Urgent Birthday Delivery', text: "Hey! Can this order reach me before this weekend for a birthday gift?" },
    { label: '🚚 Order Tracking Help', text: "Hello! I need assistance tracking my BabyyGurll crochet order." },
    { label: '🌸 Custom Bouquet Advice', text: "Hi! Could you recommend flower combinations for an anniversary bouquet?" }
  ];

  const handleSend = (textToSend) => {
    const finalMsg = textToSend || message.trim() || "Hi BabyyGurll Crochet! I have a question about your handmade creations 🌸";
    const encoded = encodeURIComponent(finalMsg);
    window.open(`https://wa.me/${CRAFTER_PHONE}?text=${encoded}`, '_blank', 'noopener,noreferrer');
    setMessage('');
    setIsOpen(false);
  };

  return (
    <aside className="whatsapp-concierge-root" aria-label="WhatsApp Concierge">
      {/* Floating Action Button */}
      <button 
        className={`whatsapp-fab ${isOpen ? 'active' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Chat with the Crafter on WhatsApp"
        title="Chat with the Crafter"
      >
        <span className="whatsapp-icon-wrap">
          <IconWhatsApp size={24} />
        </span>
        <span className="whatsapp-fab-label">Chat with Crafter</span>
        <span className="online-indicator-dot" />
      </button>

      {/* Concierge Popover Window */}
      {isOpen && (
        <div className="whatsapp-popover" role="dialog" aria-modal="false">
          {/* Header */}
          <div className="wa-popover-header">
            <div className="wa-avatar-box">
              <div className="wa-avatar">🧶</div>
              <div>
                <h5>BabyyGurll Studio</h5>
                <span className="wa-status-text">
                  <span className="online-dot-mini" /> Usually replies in ~15 mins
                </span>
              </div>
            </div>
            <button className="wa-close-btn" onClick={() => setIsOpen(false)} aria-label="Close Chat">&times;</button>
          </div>

          {/* Body */}
          <div className="wa-popover-body">
            <div className="wa-message-bubble">
              <p>
                Hello darling! 🌸 Handcrafting pieces in the studio today. How can I help make your crochet dream come true?
              </p>
              <span className="wa-message-time">Just now</span>
            </div>

            <div className="wa-quick-chips">
              <span className="wa-chips-title">Frequent questions:</span>
              {quickPrompts.map((p, i) => (
                <button 
                  key={i} 
                  className="wa-chip-btn"
                  onClick={() => handleSend(p.text)}
                >
                  {p.label}
                </button>
              ))}
            </div>
          </div>

          {/* Footer Input */}
          <div className="wa-popover-footer">
            <input 
              type="text" 
              className="wa-input"
              placeholder="Ask anything or request custom colors..."
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              onKeyDown={(e) => { if (e.key === 'Enter') handleSend(); }}
            />
            <button 
              className="wa-send-btn" 
              onClick={() => handleSend()}
              aria-label="Send WhatsApp Message"
            >
              <IconArrowRight size={16} />
            </button>
          </div>
        </div>
      )}
    </aside>
  );
};
