import React, { useState } from 'react';
import { LegalPolicyModal } from './LegalPolicyModal';

export const AppDownloadPage = ({ onBackToStore }) => {
  const [isPolicyOpen, setIsPolicyOpen] = useState(false);
  const [policyTab, setPolicyTab] = useState('terms');

  const openPolicy = (tab) => {
    setPolicyTab(tab);
    setIsPolicyOpen(true);
  };

  const apkUrl = "https://raw.githubusercontent.com/NarayanPhukan/SimpleCo/main/android-customer/babyygurll-boutique.apk";

  return (
    <div style={{ minHeight: '100vh', background: '#F8FAFC', color: '#0F172A', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      {/* Top Navigation */}
      <header style={{ background: '#FFFFFF', borderBottom: '1px solid #E2E8F0', padding: '1rem 1.5rem', position: 'sticky', top: 0, zIndex: 50 }}>
        <div style={{ maxWidth: '1100px', margin: '0 auto', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ width: '40px', height: '40px', background: '#091E61', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#FFFFFF', fontFamily: "'Times New Roman', serif", fontWeight: 700, fontSize: '20px' }}>
              BG
            </div>
            <div>
              <h1 style={{ margin: 0, fontSize: '1.15rem', fontWeight: 800, color: '#091E61', letterSpacing: '0.5px' }}>
                BABYYGURLL BOUTIQUE
              </h1>
              <p style={{ margin: 0, fontSize: '0.75rem', color: '#64748B', letterSpacing: '1px' }}>
                OFFICIAL ANDROID APP
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onBackToStore}
            style={{ background: '#F1F5F9', border: '1px solid #CBD5E1', padding: '0.5rem 1rem', borderRadius: '8px', fontSize: '0.85rem', fontWeight: 600, color: '#334155', cursor: 'pointer' }}
          >
            ← Open Web Store
          </button>
        </div>
      </header>

      {/* Hero Section */}
      <main style={{ maxWidth: '960px', margin: '0 auto', padding: '3rem 1.5rem' }}>
        <div style={{ textAlign: 'center', maxWidth: '720px', margin: '0 auto 3rem' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', background: '#EEF2FF', border: '1px solid #C7D2FE', color: '#3730A3', padding: '0.35rem 0.9rem', borderRadius: '20px', fontSize: '0.82rem', fontWeight: 700, marginBottom: '1.25rem' }}>
            <span>📱</span> Official Release • Android APK v1.2.0
          </div>
          <h2 style={{ fontSize: '2.5rem', fontWeight: 900, color: '#091E61', lineHeight: '1.2', marginBottom: '1rem', letterSpacing: '-0.5px' }}>
            Bespoke Handmade Crochet on Android
          </h2>
          <p style={{ fontSize: '1.1rem', color: '#475569', lineHeight: '1.6', marginBottom: '2rem' }}>
            Browse slow-crafted amigurumi plushies, floral bouquets, and cozy wearables. Experience one-tap Razorpay UPI payments, interactive photo carousels, bespoke color requests, and live parcel tracking.
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', smDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: '1rem' }}>
            <a
              href={apkUrl}
              download="babyygurll-boutique.apk"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '10px',
                background: '#091E61',
                color: '#FFFFFF',
                padding: '1rem 2rem',
                borderRadius: '12px',
                fontSize: '1.05rem',
                fontWeight: 700,
                textDecoration: 'none',
                boxShadow: '0 10px 25px -5px rgba(9, 30, 97, 0.3)'
              }}
            >
              <span>⬇️</span> Download Android App (APK • 19.8 MB)
            </a>
          </div>
          <p style={{ fontSize: '0.8rem', color: '#64748B', marginTop: '0.75rem' }}>
            Compatible with Android 8.0 (Oreo) and above • Verified clean &amp; secure build
          </p>
        </div>

        {/* Feature Highlights Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem', marginBottom: '3.5rem' }}>
          <div style={{ background: '#FFFFFF', padding: '1.75rem', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)' }}>
            <div style={{ fontSize: '2rem', marginBottom: '0.75rem' }}>🔒</div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800, color: '#0F172A', marginBottom: '0.5rem' }}>
              Razorpay Secured Checkout
            </h3>
            <p style={{ fontSize: '0.88rem', color: '#64748B', lineHeight: '1.5', margin: 0 }}>
              Seamless instant payment integration powered by Razorpay. Pay with Google Pay, PhonePe, Paytm, BHIM UPI, debit/credit cards, and netbanking. 100% PCI-DSS compliant.
            </p>
          </div>

          <div style={{ background: '#FFFFFF', padding: '1.75rem', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)' }}>
            <div style={{ fontSize: '2rem', marginBottom: '0.75rem' }}>📦</div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800, color: '#0F172A', marginBottom: '0.5rem' }}>
              Real-Time Tracking &amp; Alerts
            </h3>
            <p style={{ fontSize: '0.88rem', color: '#64748B', lineHeight: '1.5', margin: 0 }}>
              Receive instant push notifications when artisans start stitching your piece, when it passes quality check, and when courier partners (BlueDart/Delhivery) dispatch it to your doorstep.
            </p>
          </div>

          <div style={{ background: '#FFFFFF', padding: '1.75rem', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)' }}>
            <div style={{ fontSize: '2rem', marginBottom: '0.75rem' }}>🎨</div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800, color: '#0F172A', marginBottom: '0.5rem' }}>
              Bespoke Artisan Studio
            </h3>
            <p style={{ fontSize: '0.88rem', color: '#64748B', lineHeight: '1.5', margin: 0 }}>
              Customize yarn colors, dimensions, and floral arrangements directly in the app. Submit personalized gift card messages for birthdays, anniversaries, and holidays.
            </p>
          </div>
        </div>

        {/* Business & Merchant Verification Details for Compliance */}
        <div style={{ background: '#FFFFFF', borderRadius: '16px', border: '1px solid #E2E8F0', padding: '2rem', marginBottom: '3rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '1rem' }}>
            <div style={{ background: '#DCFCE7', color: '#166534', width: '28px', height: '28px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold' }}>
              ✓
            </div>
            <h3 style={{ margin: 0, fontSize: '1.15rem', fontWeight: 800, color: '#0F172A' }}>
              Merchant Compliance &amp; Business Information
            </h3>
          </div>
          <p style={{ fontSize: '0.9rem', color: '#475569', lineHeight: '1.6', marginBottom: '1.25rem' }}>
            BabyyGurll Boutique is a slow-fashion atelier operating under registered compliance with Indian commercial e-commerce standards. All digital transactions are authorized and settled securely through Razorpay Software Private Limited.
          </p>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', background: '#F8FAFC', padding: '1.25rem', borderRadius: '12px', border: '1px solid #E2E8F0', fontSize: '0.85rem' }}>
            <div>
              <strong style={{ color: '#0F172A' }}>Brand Name:</strong>
              <div style={{ color: '#475569' }}>BabyyGurll Boutique</div>
            </div>
            <div>
              <strong style={{ color: '#0F172A' }}>Support Email:</strong>
              <div><a href="mailto:tanayaburagohain2244@gmail.com" style={{ color: '#2563EB', textDecoration: 'none' }}>tanayaburagohain2244@gmail.com</a></div>
            </div>
            <div>
              <strong style={{ color: '#0F172A' }}>Official Website:</strong>
              <div><a href="https://babyygurlcrochet.vercel.app/" style={{ color: '#2563EB', textDecoration: 'none' }}>babyygurlcrochet.vercel.app</a></div>
            </div>
            <div>
              <strong style={{ color: '#0F172A' }}>Fulfillment Partner:</strong>
              <div style={{ color: '#475569' }}>Shiprocket &amp; India Post</div>
            </div>
          </div>
        </div>

        {/* Mandatory Policy Buttons for Razorpay Compliance */}
        <div style={{ textAlign: 'center', padding: '2rem 1rem', borderTop: '1px solid #E2E8F0' }}>
          <h4 style={{ fontSize: '0.95rem', fontWeight: 700, color: '#64748B', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '1rem' }}>
            Consumer Protection &amp; Legal Policies
          </h4>
          <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '0.75rem' }}>
            {[
              { id: 'refund', label: 'Refund & Cancellation Policy' },
              { id: 'shipping', label: 'Shipping & Delivery Policy' },
              { id: 'terms', label: 'Terms & Conditions' },
              { id: 'privacy', label: 'Privacy Policy' },
              { id: 'contact', label: 'Contact Us / Merchant Grievance' }
            ].map(item => (
              <button
                key={item.id}
                type="button"
                onClick={() => openPolicy(item.id)}
                style={{
                  background: '#FFFFFF',
                  border: '1px solid #CBD5E1',
                  borderRadius: '8px',
                  padding: '0.5rem 1rem',
                  fontSize: '0.85rem',
                  fontWeight: 600,
                  color: '#1E293B',
                  cursor: 'pointer'
                }}
              >
                {item.label}
              </button>
            ))}
          </div>
          <p style={{ fontSize: '0.78rem', color: '#94A3B8', marginTop: '1.5rem' }}>
            &copy; 2026 BabyyGurll Boutique. All rights reserved. Powered securely by Razorpay.
          </p>
        </div>
      </main>

      <LegalPolicyModal 
        isOpen={isPolicyOpen} 
        onClose={() => setIsPolicyOpen(false)} 
        initialTab={policyTab} 
      />
    </div>
  );
};
