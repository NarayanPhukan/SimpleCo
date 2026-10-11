import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useCart } from '../context/CartContext';

export const OrderTracker = ({ trackOrderId, onShowToast }) => {
  const { formatPrice } = useCart();
  const [orderIdInput, setOrderIdInput] = useState('BGC-89241');
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (trackOrderId) {
      setOrderIdInput(trackOrderId);
      fetchOrder(trackOrderId);
    } else {
      fetchOrder('BGC-89241');
    }
  }, [trackOrderId]);

  const fetchOrder = async (idToFetch) => {
    if (!idToFetch) return;
    setLoading(true);
    try {
      const res = await api.getOrderById(idToFetch);
      setOrder(res.data);
      setLoading(false);
    } catch (err) {
      setLoading(false);
      onShowToast(err.message, '⚠️');
    }
  };

  const handleAdvanceStage = async () => {
    if (!order) return;
    try {
      const res = await api.advanceOrderStage(order.orderId);
      setOrder(res.data);
      if (res.data.statusStage === 4) {
        onShowToast(`Order ${res.data.orderId} is DELIVERED to your doorstep! 🏡🎉`, '🌸');
      } else {
        onShowToast(`Advanced to: ${res.data.statusLabel}! ⚡`, '✨');
      }
    } catch (err) {
      onShowToast('Could not advance stage', '⚠️');
    }
  };

  const stageIcons = {
    received: '📦',
    crafted: '🪡',
    packed: '🎀',
    shipped: '🚚',
    delivered: '🏡'
  };

  const totalSteps = order?.timeline?.length || 5;
  const progressWidth = order ? Math.min(80, (order.statusStage / (totalSteps - 1)) * 80) : 0;

  return (
    <section className="order-tracking-section" id="orderTracking">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">Live Order Tracking</span>
          <h2 className="section-title">Track Your Crochet Package</h2>
          <p className="section-subtitle">
            Enter your Order ID (or select a sample below) to track real-time delivery status across India.
          </p>
        </div>

        <div className="tracking-search-card">
          <div className="track-input-group">
            <input 
              type="text" 
              className="track-input" 
              placeholder="Enter Order ID (e.g. BGC-89241)" 
              value={orderIdInput}
              onChange={(e) => setOrderIdInput(e.target.value)}
              onKeyDown={(e) => { if (e.key === 'Enter') fetchOrder(orderIdInput); }}
            />
            <button className="btn btn-primary" onClick={() => fetchOrder(orderIdInput)}>
              {loading ? 'Searching...' : 'Track Package 🔎'}
            </button>
          </div>

          <div className="quick-sample-chips">
            <span>Quick test orders:</span>
            <button className="sample-chip" onClick={() => { setOrderIdInput('BGC-89241'); fetchOrder('BGC-89241'); }}>
              BGC-89241 (In Transit)
            </button>
            <button className="sample-chip" onClick={() => { setOrderIdInput('BGC-91402'); fetchOrder('BGC-91402'); }}>
              BGC-91402 (Handcrafting)
            </button>
          </div>

          {/* Dynamic Order Result Area */}
          {order && (
            <div className="order-tracking-result active">
              <div className="tracker-header">
                <div className="tracker-order-meta">
                  <h3>Order #{order.orderId}</h3>
                  <p>
                    Placed on {order.dateFormatted} • Est. Delivery: <strong>{order.estimatedDelivery}</strong>
                  </p>
                  <p>
                    Waybill / Tracking: <code>{order.trackingNumber}</code>
                  </p>
                </div>

                <div>
                  <span className={`tracker-status-pill status-pill-${order.statusCode}`}>
                    {order.statusLabel}
                  </span>
                </div>
              </div>

              {/* Simulation Advance Bar */}
              <div className="simulation-control-bar">
                <div className="sim-info">
                  <span>⚡ Live Tracking Simulation: Test parcel status updates</span>
                </div>
                <button className="btn-sim-advance" onClick={handleAdvanceStage}>
                  Advance Next Stage ⏩
                </button>
              </div>

              {/* Visual Progress Stepper */}
              <div className="tracking-timeline">
                <div className="timeline-progress-bar" style={{ width: `${progressWidth}%` }}></div>
                {order.timeline && order.timeline.map((step, idx) => {
                  const isDone = step.completed;
                  const isCurrent = step.current;
                  return (
                    <div key={idx} className={`timeline-step ${isDone ? 'completed' : ''} ${isCurrent ? 'current' : ''}`}>
                      <div className="step-icon-bubble">
                        {isDone && !isCurrent && idx < order.statusStage ? '✓' : (stageIcons[step.stage] || '✨')}
                      </div>
                      <div className="step-content">
                        <h4>{step.title}</h4>
                        <div className="step-time">{step.time}</div>
                        <div className="step-desc">{step.desc}</div>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Order Overview Grid */}
              <div className="order-details-grid">
                <div>
                  <h5 style={{ marginBottom: '0.8rem', fontSize: '0.95rem', fontWeight: 700 }}>Package Items</h5>
                  <div className="order-items-list">
                    {order.items && order.items.map((item, i) => (
                      <div key={i} className="tracking-item-row">
                        <img src={item.image} alt={item.name} className="tracking-item-thumb" />
                        <div className="tracking-item-info">
                          <h5>{item.name}</h5>
                          <p>Color: {item.color} • Qty: {item.quantity}</p>
                        </div>
                        <div className="tracking-item-price">{formatPrice(item.price * item.quantity)}</div>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="shipping-recap-box">
                  <h5>Shipping Destination</h5>
                  <p><strong>{order.shippingAddress?.recipientName}</strong></p>
                  <p>
                    {order.shippingAddress?.street}<br />
                    {order.shippingAddress?.city}, {order.shippingAddress?.state} {order.shippingAddress?.zip}, {order.shippingAddress?.country}
                  </p>
                  <p>Phone: {order.shippingAddress?.phone}</p>
                  <p style={{ marginTop: '0.8rem', fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                    Paid with: {order.paymentMethod} • Total: {formatPrice(order.total)}
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </section>
  );
};
