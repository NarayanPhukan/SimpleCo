import React, { useState } from 'react';

export const LegalPolicyModal = ({ isOpen, onClose, initialTab = 'terms' }) => {
  const [activeTab, setActiveTab] = useState(initialTab);

  if (!isOpen) return null;

  return (
    <div className="modal-overlay active" style={{ zIndex: 100000 }} onClick={onClose}>
      <div 
        className="modal-card" 
        style={{ maxWidth: '780px', width: '92%', maxHeight: '88vh', display: 'flex', flexDirection: 'column', padding: 0, overflow: 'hidden' }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div style={{ padding: '1.25rem 1.5rem', borderBottom: '1px solid #E2E8F0', display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: '#F8FAFC' }}>
          <div>
            <h3 style={{ margin: 0, fontSize: '1.2rem', fontWeight: 800, color: '#0F172A' }}>
              BabyyGurll Boutique Policies & Compliance
            </h3>
            <p style={{ margin: '0.2rem 0 0', fontSize: '0.8rem', color: '#64748B' }}>
              Razorpay merchant verified • Slow-craft handmade atelier in India
            </p>
          </div>
          <button 
            type="button"
            onClick={onClose}
            style={{ background: 'none', border: 'none', fontSize: '1.5rem', color: '#64748B', cursor: 'pointer', lineHeight: 1 }}
          >
            ×
          </button>
        </div>

        {/* Tab Navigation */}
        <div style={{ display: 'flex', borderBottom: '1px solid #E2E8F0', background: '#FFFFFF', overflowX: 'auto', padding: '0 1rem' }}>
          {[
            { id: 'refund', label: 'Refund & Cancellation' },
            { id: 'shipping', label: 'Shipping & Delivery' },
            { id: 'terms', label: 'Terms & Conditions' },
            { id: 'privacy', label: 'Privacy Policy' },
            { id: 'contact', label: 'Contact Us' }
          ].map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              style={{
                padding: '0.85rem 1rem',
                border: 'none',
                background: 'none',
                fontSize: '0.85rem',
                fontWeight: activeTab === tab.id ? 700 : 500,
                color: activeTab === tab.id ? '#2563EB' : '#64748B',
                borderBottom: activeTab === tab.id ? '2px solid #2563EB' : '2px solid transparent',
                cursor: 'pointer',
                whiteSpace: 'nowrap'
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Tab Content */}
        <div style={{ padding: '1.5rem', overflowY: 'auto', flex: 1, fontSize: '0.88rem', color: '#334155', lineHeight: '1.6' }}>
          {activeTab === 'refund' && (
            <div>
              <h4 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0F172A', marginBottom: '0.75rem' }}>
                Cancellation & Refund Policy
              </h4>
              <p>
                At <strong>BabyyGurll Boutique</strong>, every bouquet, amigurumi plushie, and handbag is meticulously crafted by hand with premium milk cotton yarn. We value transparency and fairness in our customer service.
              </p>
              
              <h5 style={{ fontWeight: 700, color: '#0F172A', marginTop: '1rem', marginBottom: '0.3rem' }}>1. Order Cancellation Window</h5>
              <p>
                You may cancel your order at any time before it has been dispatched by visiting <strong>My Account &gt; My Orders &gt; Order Details</strong> in our mobile app or web storefront, or by contacting our concierge via WhatsApp/Email within 24 hours of placing the order.
              </p>

              <h5 style={{ fontWeight: 700, color: '#0F172A', marginTop: '1rem', marginBottom: '0.3rem' }}>2. 100% Full Refund via Razorpay</h5>
              <p>
                For all prepaid orders paid via <strong>Razorpay (UPI, Debit/Credit Card, Netbanking)</strong>, upon cancellation, an automated 100% full refund is instantly issued back to your original source payment method or UPI ID.
              </p>
              <ul>
                <li><strong>Instant UPI Refunds:</strong> Typically settled within 10 to 30 minutes via Razorpay Optimum.</li>
                <li><strong>Card & Netbanking:</strong> Credited to your bank account within 2 to 3 business days, subject to your bank's settlement cycle.</li>
              </ul>

              <h5 style={{ fontWeight: 700, color: '#0F172A', marginTop: '1rem', marginBottom: '0.3rem' }}>3. Damaged or Incorrect Pieces</h5>
              <p>
                Because our pieces are slow-crafted and handmade, we do not accept returns for change of mind after delivery. However, if your keepsake arrives damaged in transit or is not as described, report it to us within <strong>48 hours of delivery</strong> with unboxing photos, and we will happily remake the piece or issue an immediate full refund.
              </p>
            </div>
          )}

          {activeTab === 'shipping' && (
            <div>
              <h4 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0F172A', marginBottom: '0.75rem' }}>
                Shipping & Delivery Policy
              </h4>
              <p>
                We dispatch packages across all serviceable pincodes in India through verified logistics partners including <strong>BlueDart, Delhivery, DTDC, and India Post Speed Post</strong>.
              </p>

              <h5 style={{ fontWeight: 700, color: '#0F172A', marginTop: '1rem', marginBottom: '0.3rem' }}>1. Crafting & Processing Timelines</h5>
              <ul>
                <li><strong>Ready-to-Ship Items:</strong> Dispatched within 24–48 hours.</li>
                <li><strong>Custom / Made-to-Order Pieces:</strong> Require 3–5 business days for hand-crocheting and quality inspection prior to packaging.</li>
              </ul>

              <h5 style={{ fontWeight: 700, color: '#0F172A', marginTop: '1rem', marginBottom: '0.3rem' }}>2. Shipping Charges</h5>
              <p>
                We currently offer <strong>FREE tracked doorstep delivery</strong> pan-India on all orders with no minimum order value required.
              </p>

              <h5 style={{ fontWeight: 700, color: '#0F172A', marginTop: '1rem', marginBottom: '0.3rem' }}>3. Live Tracking</h5>
              <p>
                Once handed over to the logistics courier, you will receive a tracking Airway Bill (AWB) number via email/SMS. You can track your parcel live anytime in the <strong>Track Order</strong> section of our website or mobile app.
              </p>
            </div>
          )}

          {activeTab === 'terms' && (
            <div>
              <h4 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0F172A', marginBottom: '0.75rem' }}>
                Terms and Conditions
              </h4>
              <p>
                Welcome to <strong>BabyyGurll Boutique</strong> (accessible at <a href="https://babyygurlcrochet.vercel.app/" target="_blank" rel="noopener noreferrer">https://babyygurlcrochet.vercel.app/</a> and the BabyyGurll Boutique Android App). By placing an order, browsing our collection, or making a payment, you agree to the following terms:
              </p>
              <ul>
                <li>All products displayed are handmade; slight variations in yarn dye lots, stitching tension, and handcrafted nuances are inherent characteristics of artisan crochet.</li>
                <li>Prices are in Indian Rupees (INR) and are inclusive of applicable taxes.</li>
                <li>Payments are processed securely via RBI-licensed payment aggregators (Razorpay). We do not store your banking credentials, UPI PINs, or raw CVVs.</li>
                <li>We reserve the right to cancel orders in case of stock unavailability or technical errors, with immediate 100% refund.</li>
              </ul>
            </div>
          )}

          {activeTab === 'privacy' && (
            <div>
              <h4 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0F172A', marginBottom: '0.75rem' }}>
                Privacy & Data Protection Policy
              </h4>
              <p>
                Your privacy is paramount to us. This policy details how <strong>BabyyGurll Boutique</strong> collects and protects your personal information:
              </p>
              <ul>
                <li><strong>Information Collected:</strong> Name, contact telephone number, shipping address, and email address strictly for order fulfillment and dispatch notification.</li>
                <li><strong>Payment Security:</strong> Card and UPI data is encrypted end-to-end and handled directly by Razorpay under PCI-DSS Level 1 compliance.</li>
                <li><strong>Third-Party Sharing:</strong> We never sell, rent, or lease your personal data. Shipping details are shared solely with authorized courier carriers (Shiprocket, Delhivery, BlueDart) to enable physical doorstep delivery.</li>
              </ul>
            </div>
          )}

          {activeTab === 'contact' && (
            <div>
              <h4 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0F172A', marginBottom: '0.75rem' }}>
                Contact & Merchant Information
              </h4>
              <p>
                For order inquiries, custom commission requests, payment support, or refunds, please reach out to our team:
              </p>
              
              <div style={{ background: '#F8FAFC', padding: '1.25rem', borderRadius: '12px', border: '1px solid #E2E8F0', marginTop: '1rem' }}>
                <p style={{ margin: '0.4rem 0' }}><strong>Business Name:</strong> BabyyGurll Boutique</p>
                <p style={{ margin: '0.4rem 0' }}><strong>Registered Domain:</strong> <a href="https://babyygurlcrochet.vercel.app/" target="_blank" rel="noopener noreferrer">https://babyygurlcrochet.vercel.app/</a></p>
                <p style={{ margin: '0.4rem 0' }}><strong>Customer Support Email:</strong> <a href="mailto:hello@babyygurll.in">hello@babyygurll.in</a> / <a href="mailto:tanayaburagohain2244@gmail.com">tanayaburagohain2244@gmail.com</a></p>
                <p style={{ margin: '0.4rem 0' }}><strong>Customer Support Phone:</strong> +91 98200 12345 / +91 98640 12345</p>
                <p style={{ margin: '0.4rem 0' }}><strong>Atelier Address:</strong> Studio 4, Bandra West, Mumbai, Maharashtra 400050 &amp; Guwahati, Assam 781001, India</p>
                <p style={{ margin: '0.4rem 0' }}><strong>Support Hours:</strong> Monday – Saturday, 9:00 AM – 7:00 PM IST</p>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div style={{ padding: '0.85rem 1.5rem', borderTop: '1px solid #E2E8F0', background: '#F8FAFC', display: 'flex', justifyContent: 'flex-end' }}>
          <button 
            type="button" 
            className="btn btn-primary" 
            onClick={onClose}
            style={{ padding: '0.5rem 1.25rem', fontSize: '0.84rem' }}
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

