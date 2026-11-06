import React from 'react';

export const AppDownloadModal = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay active" style={{ zIndex: 100000 }} onClick={onClose}>
      <div 
        className="modal-card" 
        style={{ maxWidth: '520px', width: '92%', padding: '2rem', textAlign: 'center', position: 'relative' }}
        onClick={(e) => e.stopPropagation()}
      >
        <button 
          type="button"
          onClick={onClose}
          style={{ position: 'absolute', top: '1rem', right: '1.25rem', background: 'none', border: 'none', fontSize: '1.5rem', color: '#64748B', cursor: 'pointer' }}
        >
          ×
        </button>

        <div style={{ width: '64px', height: '64px', background: '#EEF4FF', borderRadius: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1rem', fontSize: '2rem' }}>
          📱
        </div>

        <h3 style={{ fontSize: '1.35rem', fontWeight: 800, color: '#0F172A', marginBottom: '0.4rem' }}>
          BabyyGurll Boutique App
        </h3>
        
        <div style={{ display: 'inline-block', background: '#DCFCE7', color: '#166534', padding: '0.2rem 0.65rem', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 700, marginBottom: '1rem' }}>
          Official Android Release • v1.1.0
        </div>

        <p style={{ fontSize: '0.88rem', color: '#64748B', lineHeight: '1.6', marginBottom: '1.5rem' }}>
          Experience our slow-crafted crochet collection on your Android device. Features one-tap Razorpay UPI instant payments, live order stage tracking, and bespoke custom design commissions.
        </p>

        <div style={{ background: '#F8FAFC', borderRadius: '12px', padding: '1rem', textAlign: 'left', marginBottom: '1.5rem', border: '1px solid #E2E8F0', fontSize: '0.82rem', color: '#334155' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.5rem' }}>
            <span>🌸</span> <strong>100% Secure Checkout</strong> — Verified via Razorpay UPI &amp; Cards
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.5rem' }}>
            <span>📦</span> <strong>Live Stage Progress</strong> — Watch your piece go from crochet hook to doorstep
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>✨</span> <strong>Bespoke Studio</strong> — Request customized yarn colors and custom notes
          </div>
        </div>

        <a 
          href="https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-customer/babyygurll-boutique.apk" 
          download="babyygurll-boutique.apk"
          className="btn btn-primary"
          style={{ display: 'inline-block', width: '100%', padding: '0.85rem', fontSize: '0.95rem', fontWeight: 700, borderRadius: '10px', textDecoration: 'none' }}
        >
          Download Android APK (Direct Install)
        </a>

        <p style={{ fontSize: '0.72rem', color: '#94A3B8', marginTop: '0.75rem', marginBottom: 0 }}>
          Compatible with Android 8.0 (Oreo) and above • Safe &amp; verified APK
        </p>
      </div>
    </div>
  );
};
