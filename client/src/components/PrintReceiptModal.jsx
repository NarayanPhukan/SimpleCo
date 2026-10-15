import React from 'react';
import { useCart } from '../context/CartContext';
import { IconClose, IconPrinter } from './Icons';

export const PrintReceiptModal = ({ order, isOpen, onClose }) => {
  const { formatPrice } = useCart();

  if (!isOpen || !order) return null;

  const handlePrint = () => {
    window.print();
  };

  const orderDate = order.createdAt ? new Date(order.createdAt).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }) : new Date().toLocaleDateString('en-IN');

  const addr = order.shippingAddress || {};
  const items = order.items || [];
  const totals = order.totals || {
    subtotal: order.totalAmount || 0,
    shipping: 0,
    discount: 0,
    total: order.totalAmount || 0
  };

  return (
    <div 
      className="modal-overlay active print-receipt-overlay" 
      onClick={(e) => {
        if (e.target.classList.contains('modal-overlay')) onClose();
      }}
    >
      <div className="modal-card print-receipt-card" role="dialog" aria-modal="true" aria-label="Order Receipt">
        {/* Screen Toolbar (Hidden on Print) */}
        <div className="receipt-screen-toolbar no-print">
          <button className="btn btn-primary" onClick={handlePrint} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
            <IconPrinter size={16} />
            <span>Print / Save PDF</span>
          </button>
          <button className="modal-close-btn" onClick={onClose} aria-label="Close Receipt">&times;</button>
        </div>

        {/* Printable Receipt Paper Container */}
        <div className="receipt-paper" id="printableInvoice">
          {/* Brand Header */}
          <div className="receipt-header">
            <div>
              <h2 className="receipt-brand-title">BabyyGurll <span style={{ color: 'var(--primary)' }}>Crochet</span></h2>
              <p className="receipt-brand-sub">Handcrafted Artisanal Studio • Pan-India Delivery</p>
              <p className="receipt-brand-contact">orders@babyygurll.in • www.babyygurll.in</p>
            </div>
            <div className="receipt-badge-box">
              <span className="receipt-doc-tag">Tax Invoice / Receipt</span>
              <h3 className="receipt-order-num">{order.orderId || 'BGC-ORDER'}</h3>
              <span className="receipt-date">{orderDate}</span>
            </div>
          </div>

          <div className="receipt-divider" />

          {/* Customer & Shipping Details Grid */}
          <div className="receipt-meta-grid">
            <div className="receipt-meta-col">
              <h5>Billed & Shipped To:</h5>
              <strong>{addr.recipientName || order.userEmail || 'Valued Customer'}</strong>
              <p>{addr.street || 'Address on file'}</p>
              <p>{addr.city ? `${addr.city}, ${addr.state || ''} - ${addr.zip || ''}` : 'India'}</p>
              <p>Phone: {addr.phone || 'N/A'}</p>
            </div>
            <div className="receipt-meta-col">
              <h5>Order Information:</h5>
              <p>Payment Method: <strong>{order.paymentMethod?.toUpperCase() || 'UPI / ONLINE'}</strong></p>
              <p>Payment Status: <strong style={{ color: 'var(--success)' }}>{order.paymentStatus || 'Completed'}</strong></p>
              <p>Fulfillment: <strong>Shiprocket Express Air</strong></p>
              {order.shipmentDetails?.awbCode && (
                <p>Tracking AWB: <strong>{order.shipmentDetails.awbCode}</strong></p>
              )}
            </div>
          </div>

          {/* Items Table */}
          <table className="receipt-items-table">
            <thead>
              <tr>
                <th style={{ textAlign: 'left' }}>Item Description</th>
                <th style={{ textAlign: 'center' }}>Qty</th>
                <th style={{ textAlign: 'right' }}>Unit Price</th>
                <th style={{ textAlign: 'right' }}>Amount</th>
              </tr>
            </thead>
            <tbody>
              {items.map((it, idx) => (
                <tr key={idx}>
                  <td>
                    <strong>{it.name}</strong>
                    <div style={{ fontSize: '0.8rem', color: '#666' }}>
                      Color/Variant: {it.color || 'Default'}
                      {it.customNote && <span> • Note: "{it.customNote}"</span>}
                    </div>
                  </td>
                  <td style={{ textAlign: 'center' }}>{it.quantity}</td>
                  <td style={{ textAlign: 'right' }}>{formatPrice(it.price)}</td>
                  <td style={{ textAlign: 'right' }}>{formatPrice(it.price * it.quantity)}</td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Totals Summary */}
          <div className="receipt-totals-wrap">
            <div className="receipt-totals-table">
              <div className="receipt-sum-row">
                <span>Subtotal:</span>
                <span>{formatPrice(totals.subtotal)}</span>
              </div>
              {totals.discount > 0 && (
                <div className="receipt-sum-row" style={{ color: 'var(--success)' }}>
                  <span>Discount:</span>
                  <span>-{formatPrice(totals.discount)}</span>
                </div>
              )}
              {totals.giftPackagingCost > 0 && (
                <div className="receipt-sum-row">
                  <span>Luxury Keepsake Gift Box:</span>
                  <span>{formatPrice(totals.giftPackagingCost)}</span>
                </div>
              )}
              <div className="receipt-sum-row">
                <span>Shipping & Handling:</span>
                <span>{totals.shipping === 0 ? 'FREE' : formatPrice(totals.shipping)}</span>
              </div>
              <div className="receipt-sum-row receipt-grand-total">
                <span>Total Paid:</span>
                <span>{formatPrice(totals.total || order.totalAmount)}</span>
              </div>
            </div>
          </div>

          {/* Artisanal Milk-Cotton Care Guide */}
          <div className="receipt-care-guide">
            <h4 className="care-guide-title">🌸 Handcrafted Milk-Cotton Care Guide</h4>
            <div className="care-guide-grid">
              <div className="care-point">
                <strong>1. Hand Wash Cold (30°C)</strong>
                <p>Never machine spin. Soak in cold water with gentle wool or baby shampoo for 5-10 minutes.</p>
              </div>
              <div className="care-point">
                <strong>2. Never Wring or Twist</strong>
                <p>Gently press out water by rolling inside a clean, dry towel to preserve structural stitches.</p>
              </div>
              <div className="care-point">
                <strong>3. Lay Flat in Shade</strong>
                <p>Air-dry horizontally on a mesh or towel away from harsh direct sunlight to keep pastel hues vibrant.</p>
              </div>
              <div className="care-point">
                <strong>4. Fluff with Love</strong>
                <p>Give your plushie a gentle squeeze to redistribute the hypoallergenic polyfill stuffing!</p>
              </div>
            </div>
          </div>

          <div className="receipt-footer-msg">
            <p>Thank you for supporting an independent handmade artisan in India. Each loop and stitch was made for you!</p>
          </div>
        </div>
      </div>
    </div>
  );
};
