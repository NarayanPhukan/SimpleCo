import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { api } from '../services/api';
import { IconPackage, IconUser, IconRefresh, IconArrowRight, IconCheck, IconTruck } from './Icons';

export const AccountModal = ({ isOpen, onClose, onOpenAddressModal, onShowToast }) => {
  const { user, login, register, logout, forgotPassword, deleteAddress, setDefaultAddress } = useAuth();
  const { formatPrice } = useCart();

  // Top-level account contents: 'orders', 'address', 'logout'
  const [activeNav, setActiveNav] = useState('orders');

  // Sub-categories inside orders: 'in-progress', 'delivered', 'return', 'cancelled'
  const [orderSubtab, setOrderSubtab] = useState('in-progress');

  // ID of the in-progress order currently displaying expanded live tracking
  const [expandedTrackId, setExpandedTrackId] = useState(null);

  // Auth mode if user is not logged in: 'login', 'register', or 'forgot'
  const [authMode, setAuthMode] = useState('login');
  const [orders, setOrders] = useState([]);
  const [loadingOrders, setLoadingOrders] = useState(false);

  // Login form state
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Register form state
  const [regName, setRegName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPhone, setRegPhone] = useState('');
  const [regPassword, setRegPassword] = useState('');

  // Forgot password form state
  const [forgotEmail, setForgotEmail] = useState('');
  const [forgotNewPassword, setForgotNewPassword] = useState('');
  const [forgotConfirmPassword, setForgotConfirmPassword] = useState('');
  const [forgotLoading, setForgotLoading] = useState(false);

  // Fetch orders whenever user is logged in and modal opens
  const fetchUserOrders = async () => {
    if (!user?.email) return;
    try {
      setLoadingOrders(true);
      const res = await api.getOrders(user.email);
      const list = res.data || [];
      setOrders(list);
      setLoadingOrders(false);

      // Auto-expand first in-progress order if none selected
      const inProg = list.filter(o => o.statusCode !== 'delivered' && o.statusCode !== 'returned' && o.statusCode !== 'cancelled');
      if (inProg.length > 0 && !expandedTrackId) {
        setExpandedTrackId(inProg[0].orderId);
      }
    } catch (err) {
      setLoadingOrders(false);
    }
  };

  useEffect(() => {
    if (user && user.isLoggedIn) {
      fetchUserOrders();
    }
  }, [user, isOpen]);

  // Lock body scroll and handle Escape key for full-screen portal
  useEffect(() => {
    if (isOpen) {
      const prevOverflow = document.body.style.overflow;
      document.body.style.overflow = 'hidden';
      const handleKeyDown = (e) => {
        if (e.key === 'Escape') onClose();
      };
      window.addEventListener('keydown', handleKeyDown);
      return () => {
        document.body.style.overflow = prevOverflow;
        window.removeEventListener('keydown', handleKeyDown);
      };
    }
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    const res = await login(loginEmail, loginPassword);
    if (res.success) {
      onShowToast(`Welcome back, ${res.user.name}`, '✓');
      setActiveNav('orders');
    } else {
      onShowToast(res.message, '!');
    }
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    const res = await register({
      name: regName,
      email: regEmail,
      phone: regPhone,
      password: regPassword
    });
    if (res.success) {
      onShowToast('Account created successfully', '✓');
      setActiveNav('orders');
    } else {
      onShowToast(res.message, '!');
    }
  };

  const handleLogout = () => {
    logout();
    onShowToast('Signed out successfully', '✓');
    setActiveNav('orders');
  };

  const handleAdvanceStage = async (orderId) => {
    try {
      const res = await api.advanceOrderStage(orderId);
      if (res.data) {
        setOrders(prev => prev.map(o => o.orderId === orderId ? res.data : o));
        if (res.data.statusStage === 4) {
          onShowToast(`Order #${orderId} marked as delivered`, '✓');
          // Switch to delivered subtab so user can see it right away
          setOrderSubtab('delivered');
        } else {
          onShowToast(`Order #${orderId} updated: ${res.data.statusLabel}`, '✓');
        }
      }
    } catch (err) {
      onShowToast('Could not advance order stage', '!');
    }
  };

  const handleInitiateReturn = async (orderId) => {
    const reason = window.prompt(
      `Request Return for Order #${orderId}:\nPlease state your reason (e.g. Size/Color exchange, Changed mind):`,
      'Exchange for Sage Green colorway'
    );
    if (!reason || !reason.trim()) return;

    try {
      const res = await api.requestReturn(orderId, reason.trim());
      if (res.data) {
        setOrders(prev => prev.map(o => o.orderId === orderId ? res.data : o));
        onShowToast(`Return request submitted for #${orderId}`, '✓');
        setOrderSubtab('return');
      }
    } catch (err) {
      onShowToast(err.message || 'Could not process return request', '!');
    }
  };

  const handleCancelOrder = async (orderId) => {
    const reason = window.prompt(
      `Cancel Order #${orderId}?\nPlease state your cancellation reason (e.g. Changed mind, Placed duplicate order, Need alternate size):`,
      'Changed mind / Placed by mistake'
    );
    if (!reason || !reason.trim()) return;

    try {
      const res = await api.cancelOrder(orderId, reason.trim());
      if (res.data) {
        setOrders(prev => prev.map(o => o.orderId === orderId ? res.data : o));
        onShowToast(`Order #${orderId} has been cancelled`, '✓');
        setOrderSubtab('cancelled');
      }
    } catch (err) {
      onShowToast(err.message || 'Could not cancel order', '!');
    }
  };

  const handleForgotSubmit = async (e) => {
    e.preventDefault();
    if (!forgotEmail || !forgotEmail.trim()) {
      onShowToast('Please enter your registered email address', '!');
      return;
    }
    if (forgotNewPassword.length < 6) {
      onShowToast('New password must be at least 6 characters', '!');
      return;
    }
    if (forgotNewPassword !== forgotConfirmPassword) {
      onShowToast('Passwords do not match. Please verify.', '!');
      return;
    }

    try {
      setForgotLoading(true);
      const res = await forgotPassword(forgotEmail.trim(), forgotNewPassword);
      setForgotLoading(false);
      if (res.success) {
        onShowToast('Password reset successfully! Please sign in.', '✓');
        setLoginEmail(forgotEmail.trim());
        setLoginPassword('');
        setForgotNewPassword('');
        setForgotConfirmPassword('');
        setAuthMode('login');
      } else {
        onShowToast(res.message || 'Password reset failed', '!');
      }
    } catch (err) {
      setForgotLoading(false);
      onShowToast(err.message || 'Password reset failed', '!');
    }
  };

  // Filter orders by sub-category
  const inProgressOrders = orders.filter(o => o.statusCode !== 'delivered' && o.statusCode !== 'returned' && o.statusCode !== 'cancelled');
  const returnOrders = orders.filter(o => o.statusCode === 'returned');
  const deliveredOrders = orders.filter(o => o.statusCode === 'delivered');
  const cancelledOrders = orders.filter(o => o.statusCode === 'cancelled');

  const initials = user?.name ? user.name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2) : 'BG';

  return (
    <div className="full-screen-account-page" role="dialog" aria-modal="true" aria-label="Customer Account Portal">
      {/* Top Sticky Header */}
      <header className="account-page-topbar">
        <div className="account-page-brand">
          <div 
            aria-hidden="true" 
            style={{ 
              width: 38, 
              height: 38, 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center', 
              background: 'var(--primary-light)', 
              color: 'var(--primary)', 
              borderRadius: '50%',
              boxShadow: 'var(--shadow-xs)' 
            }}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 3c-4.97 0-9 4.03-9 9 0 2.12.74 4.07 1.97 5.61L12 21l7.03-3.39A8.97 8.97 0 0 0 21 12c0-4.97-4.03-9-9-9z"></path>
              <path d="M12 7a5 5 0 0 0-5 5c0 1.93.98 3.63 2.47 4.63"></path>
            </svg>
          </div>
          <div>
            <span style={{ fontFamily: 'var(--font-serif)', fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-dark)', letterSpacing: '-0.02em', display: 'block', lineHeight: 1.15 }}>
              BabyyGurll <span style={{ color: 'var(--primary)' }}>Crochet</span>
            </span>
            <span style={{ fontSize: '0.74rem', color: 'var(--text-muted)', fontWeight: 500 }}>Customer Account & Orders</span>
          </div>
        </div>

        <button 
          className="btn-back-to-store" 
          onClick={onClose}
          aria-label="Back to Storefront"
        >
          <span style={{ display: 'inline-flex', transform: 'rotate(180deg)' }}>
            <IconArrowRight size={14} />
          </span>
          <span>Back to Store</span>
        </button>
      </header>

      {/* Main Account Portal Container */}
      <main className="account-page-container">
        {/* Logged In View */}
        {user?.isLoggedIn ? (
          <div>
            {/* User Profile Header Banner */}
            <div className="profile-hero">
              <div className="profile-hero-left">
                <div className="profile-avatar-large">{initials}</div>
                <div className="profile-info">
                  <h3>{user.name}</h3>
                  <p>{user.email} {user.phone ? `• Mobile: ${user.phone}` : ''}</p>
                  <span className="tier-badge">{user.memberTier || 'VIP Member'}</span>
                </div>
              </div>

              {/* Profile Quick Stats */}
              <div className="profile-quick-stats">
                <div className="quick-stat-box">
                  <div className="quick-stat-val">{orders.length}</div>
                  <div className="quick-stat-label">Total Orders</div>
                </div>
                <div className="quick-stat-box">
                  <div className="quick-stat-val" style={{ color: 'var(--primary)' }}>{inProgressOrders.length}</div>
                  <div className="quick-stat-label">In-Progress</div>
                </div>
                <div className="quick-stat-box">
                  <div className="quick-stat-val">{user.savedAddresses ? user.savedAddresses.length : 0}</div>
                  <div className="quick-stat-label">Saved Addresses</div>
                </div>
              </div>
            </div>

            {/* Account Page Contents Navigation: Orders, Address, Logout */}
            <div className="account-main-nav" role="tablist">
              <button 
                className={`account-nav-btn ${activeNav === 'orders' ? 'active' : ''}`}
                onClick={() => setActiveNav('orders')}
                role="tab"
                aria-selected={activeNav === 'orders'}
                style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.45rem' }}
              >
                <IconPackage size={16} />
                <span>Orders</span>
              </button>
              <button 
                className={`account-nav-btn ${activeNav === 'address' ? 'active' : ''}`}
                onClick={() => setActiveNav('address')}
                role="tab"
                aria-selected={activeNav === 'address'}
                style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.45rem' }}
              >
                <IconUser size={16} />
                <span>Addresses</span>
              </button>
              <button 
                className="account-nav-btn logout-nav-btn"
                onClick={handleLogout}
                title="Log out of your account"
                style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.45rem' }}
              >
                <IconArrowRight size={16} />
                <span>Sign Out</span>
              </button>
            </div>

            {/* ========================================================
                TAB 1: ORDERS (in-progress, return, delivered)
               ======================================================== */}
            {activeNav === 'orders' && (
              <div className="account-orders-section">
                {/* Order Sub-tabs */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem', marginBottom: '1.25rem' }}>
                  <div className="order-subtabs-bar" style={{ marginBottom: 0 }}>
                    <button 
                      className={`order-subtab-pill ${orderSubtab === 'all' ? 'active' : ''}`}
                      onClick={() => setOrderSubtab('all')}
                    >
                      <span>All</span>
                      <span className="subtab-count-badge">{orders.length}</span>
                    </button>
                    <button 
                      className={`order-subtab-pill ${orderSubtab === 'in-progress' ? 'active' : ''}`}
                      onClick={() => setOrderSubtab('in-progress')}
                    >
                      <span>In-Progress</span>
                      <span className="subtab-count-badge">{inProgressOrders.length}</span>
                    </button>
                    <button 
                      className={`order-subtab-pill ${orderSubtab === 'delivered' ? 'active' : ''}`}
                      onClick={() => setOrderSubtab('delivered')}
                    >
                      <span>Delivered</span>
                      <span className="subtab-count-badge">{deliveredOrders.length}</span>
                    </button>
                    <button 
                      className={`order-subtab-pill ${orderSubtab === 'return' ? 'active' : ''}`}
                      onClick={() => setOrderSubtab('return')}
                    >
                      <span>Return</span>
                      <span className="subtab-count-badge">{returnOrders.length}</span>
                    </button>
                    <button 
                      className={`order-subtab-pill ${orderSubtab === 'cancelled' ? 'active' : ''}`}
                      onClick={() => setOrderSubtab('cancelled')}
                    >
                      <span>Cancelled</span>
                      <span className="subtab-count-badge">{cancelledOrders.length}</span>
                    </button>
                  </div>

                  <button
                    type="button"
                    onClick={() => {
                      fetchUserOrders();
                      onShowToast('Orders list refreshed', '✓');
                    }}
                    style={{ background: '#F8FAFC', border: '1px solid #CBD5E1', borderRadius: '20px', padding: '0.4rem 0.85rem', fontSize: '0.78rem', color: '#475569', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: '0.35rem', fontWeight: 600 }}
                  >
                    <IconRefresh size={13} />
                    <span>Refresh</span>
                  </button>
                </div>

                {loadingOrders && (
                  <div style={{ textAlign: 'center', padding: '2rem 0', color: 'var(--text-muted)' }}>
                    <div style={{ width: '28px', height: '28px', border: '2px solid #E2E8F0', borderTopColor: 'var(--primary)', borderRadius: '50%', margin: '0 auto', animation: 'spin 0.8s linear infinite' }}></div>
                    <p style={{ marginTop: '0.75rem', fontSize: '0.88rem' }}>Loading your orders...</p>
                  </div>
                )}

                {/* Subtab Content: ALL / IN-PROGRESS */}
                {(orderSubtab === 'all' || orderSubtab === 'in-progress') && (
                  <div className="orders-list-container">
                    {(orderSubtab === 'all' ? orders : inProgressOrders).length === 0 ? (
                      <div className="empty-subtab-box">
                        <div className="empty-icon" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                          <IconPackage size={36} />
                        </div>
                        <h4>{orderSubtab === 'all' ? 'No Orders Found' : 'No In-Progress Orders'}</h4>
                        <p>{orderSubtab === 'all' ? 'You have not placed any orders yet. Discover our handmade crochet collection!' : "You don't have any orders currently being crafted or in transit."}</p>
                      </div>
                    ) : (
                      (orderSubtab === 'all' ? orders : inProgressOrders).map((ord) => {
                        const isExpanded = expandedTrackId === ord.orderId;
                        const totalSteps = ord.timeline?.length || 5;
                        const progressWidth = Math.min(84, (ord.statusStage / (totalSteps - 1)) * 84);

                        return (
                          <div key={ord.orderId} className="in-progress-order-block">
                            <div className="order-header-meta">
                              <div>
                                <span className="order-number-badge">Order #{ord.orderId}</span>
                                <span className="order-placed-date">Placed {ord.dateFormatted}</span>
                              </div>
                              <span className={`tracker-status-pill status-pill-${ord.statusCode}`}>
                                {ord.statusLabel}
                              </span>
                            </div>

                            {/* Items List */}
                            <div className="order-items-snippet">
                              {ord.items && ord.items.map((item, i) => (
                                <div key={i} className="order-item-mini-row">
                                  <img src={item.image} alt={item.name} className="order-item-mini-img" />
                                  <div className="order-item-mini-info">
                                    <div className="mini-name">{item.name}</div>
                                    <div className="mini-sub">{item.color} • Qty {item.quantity}</div>
                                  </div>
                                  <div className="mini-price">{formatPrice(item.price * item.quantity)}</div>
                                </div>
                              ))}
                            </div>

                            <div className="order-meta-footer">
                              <div className="est-delivery-text">
                                Est. Delivery: <strong>{ord.estimatedDelivery}</strong>
                              </div>
                              <div className="order-grand-total">
                                Total: <strong>{formatPrice(ord.total)}</strong>
                              </div>
                            </div>

                            {/* ========================================================
                                BUTTON UNDER THE IN-PROGRESS ORDER
                               ======================================================== */}
                            <div className="in-progress-btn-wrapper" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
                              <button 
                                className={`btn-track-in-progress ${isExpanded ? 'active' : ''}`}
                                onClick={() => setExpandedTrackId(isExpanded ? null : ord.orderId)}
                                aria-expanded={isExpanded}
                                style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.45rem' }}
                              >
                                <IconTruck size={15} />
                                <span>{isExpanded ? 'Collapse Tracking' : 'Track Order'}</span>
                              </button>
                              <button 
                                type="button"
                                className="btn-cancel-order"
                                onClick={() => handleCancelOrder(ord.orderId)}
                                title={`Cancel Order #${ord.orderId}`}
                              >
                                Cancel Order
                              </button>
                            </div>

                            {/* EXPANDED LIVE TRACKING CONTAINER */}
                            {isExpanded && (
                              <div className="inline-live-tracking-panel">
                                <div className="tracking-courier-banner">
                                  <div>
                                    <span className="courier-badge">Delivery Details</span>
                                    <div className="courier-name">Tracked Express Delivery</div>
                                    <div className="waybill-code">Waybill / Tracking: <code>{ord.trackingNumber}</code></div>
                                  </div>
                                  <button 
                                    className="btn-sim-advance-inline"
                                    onClick={() => handleAdvanceStage(ord.orderId)}
                                    title="Simulate next tracking stage"
                                  >
                                    Advance Stage
                                  </button>
                                </div>

                                {/* Step-by-Step Progress Timeline */}
                                <div className="inline-stepper">
                                  <div className="inline-stepper-line-bg"></div>
                                  <div 
                                    className="inline-stepper-line-fill" 
                                    style={{ width: `${progressWidth}%` }}
                                  ></div>

                                  {ord.timeline && ord.timeline.map((step, idx) => {
                                    const isDone = step.completed;
                                    const isCurrent = step.current;
                                    return (
                                      <div 
                                        key={idx} 
                                        className={`inline-step-node ${isDone ? 'completed' : ''} ${isCurrent ? 'current' : ''}`}
                                      >
                                        <div className="step-node-bubble">
                                          {isDone && !isCurrent && idx < ord.statusStage ? '✓' : (idx + 1)}
                                        </div>
                                        <div className="step-node-label">{step.title}</div>
                                        <div className="step-node-time">{step.time}</div>
                                      </div>
                                    );
                                  })}
                                </div>

                                <div className="tracking-destination-strip">
                                  <span><strong>Delivery Destination:</strong> {ord.shippingAddress?.recipientName}, {ord.shippingAddress?.street}, {ord.shippingAddress?.city}, {ord.shippingAddress?.state} {ord.shippingAddress?.zip}</span>
                                </div>
                              </div>
                            )}
                          </div>
                        );
                      })
                    )}
                  </div>
                )}

                {/* Subtab Content: RETURN */}
                {orderSubtab === 'return' && (
                  <div className="orders-list-container">
                    {returnOrders.length === 0 ? (
                      <div className="empty-subtab-box">
                        <div className="empty-icon" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                          <IconRefresh size={36} />
                        </div>
                        <h4>No Return or Exchange Requests</h4>
                        <p>All your crochet treasures are happily loved! If you ever need to exchange an item, you can initiate a return from the Delivered tab.</p>
                      </div>
                    ) : (
                      returnOrders.map((ord) => (
                        <div key={ord.orderId} className="return-order-block">
                          <div className="order-header-meta">
                            <div>
                              <span className="order-number-badge">Order #{ord.orderId}</span>
                              <span className="order-placed-date">Return Logged: {ord.returnDate || ord.dateFormatted}</span>
                            </div>
                            <span className="return-status-badge">
                              {ord.statusLabel || 'Return In Progress'}
                            </span>
                          </div>

                          <div className="order-items-snippet">
                            {ord.items && ord.items.map((item, i) => (
                              <div key={i} className="order-item-mini-row">
                                <img src={item.image} alt={item.name} className="order-item-mini-img" />
                                <div className="order-item-mini-info">
                                  <div className="mini-name">{item.name}</div>
                                  <div className="mini-sub">{item.color} • Qty {item.quantity}</div>
                                </div>
                                <div className="mini-price">{formatPrice(item.price * item.quantity)}</div>
                              </div>
                            ))}
                          </div>

                          <div className="return-spec-box">
                            <div className="spec-row">
                              <span className="spec-title">Reason for Return:</span>
                              <span className="spec-value">{ord.returnReason || 'Exchange for alternate crochet design'}</span>
                            </div>
                            <div className="spec-row">
                              <span className="spec-title">Refund Settlement:</span>
                              <span className="spec-value refund-highlight">{ord.returnStatus || `Full refund of ${formatPrice(ord.total)} credited to UPI`}</span>
                            </div>
                            <div className="spec-row">
                              <span className="spec-title">Reverse Logistics:</span>
                              <span className="spec-value">Doorstep Reverse Pickup (Sealed parcel collected)</span>
                            </div>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                )}

                {/* Subtab Content: DELIVERED */}
                {orderSubtab === 'delivered' && (
                  <div className="orders-list-container">
                    {deliveredOrders.length === 0 ? (
                      <div className="empty-subtab-box">
                        <div className="empty-icon" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                          <IconPackage size={36} />
                        </div>
                        <h4>No Delivered Orders Yet</h4>
                        <p>When in-progress packages arrive at your doorstep, they will appear here with invoice receipts and return options.</p>
                      </div>
                    ) : (
                      deliveredOrders.map((ord) => (
                        <div key={ord.orderId} className="delivered-order-block">
                          <div className="order-header-meta">
                            <div>
                              <span className="order-number-badge">Order #{ord.orderId}</span>
                              <span className="order-placed-date">{ord.estimatedDelivery || `Delivered ${ord.dateFormatted}`}</span>
                            </div>
                            <span className="delivered-status-badge">
                              Delivered to Doorstep
                            </span>
                          </div>

                          <div className="order-items-snippet">
                            {ord.items && ord.items.map((item, i) => (
                              <div key={i} className="order-item-mini-row">
                                <img src={item.image} alt={item.name} className="order-item-mini-img" />
                                <div className="order-item-mini-info">
                                  <div className="mini-name">{item.name}</div>
                                  <div className="mini-sub">{item.color} • Qty {item.quantity}</div>
                                </div>
                                <div className="mini-price">{formatPrice(item.price * item.quantity)}</div>
                              </div>
                            ))}
                          </div>

                          <div className="delivered-actions-footer">
                            <div className="order-grand-total">
                              Paid Total: <strong>{formatPrice(ord.total)}</strong>
                            </div>
                            <div className="delivered-btns">
                              <button 
                                className="btn-delivered-action invoice"
                                onClick={() => onShowToast(`Tax Invoice for Order #${ord.orderId} sent to email`, '✓')}
                              >
                                Download Invoice
                              </button>
                              <button 
                                className="btn-delivered-action return"
                                onClick={() => handleInitiateReturn(ord.orderId)}
                              >
                                Request Return
                              </button>
                            </div>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                )}

                {/* Subtab Content: CANCELLED */}
                {orderSubtab === 'cancelled' && (
                  <div className="orders-list-container">
                    {cancelledOrders.length === 0 ? (
                      <div className="empty-subtab-box">
                        <div className="empty-icon" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                          <IconPackage size={36} />
                        </div>
                        <h4>No Cancelled Orders</h4>
                        <p>You do not have any cancelled orders. All your handmade crochet orders are active or delivered.</p>
                      </div>
                    ) : (
                      cancelledOrders.map((ord) => (
                        <div key={ord.orderId} className="cancelled-order-block">
                          <div className="order-header-meta">
                            <div>
                              <span className="order-number-badge">Order #{ord.orderId}</span>
                              <span className="order-placed-date">Placed {ord.dateFormatted} • Cancelled {ord.cancelledAt || 'Recently'}</span>
                            </div>
                            <span className="cancelled-status-badge">
                              Order Cancelled
                            </span>
                          </div>

                          <div className="order-items-snippet">
                            {ord.items && ord.items.map((item, i) => (
                              <div key={i} className="order-item-mini-row">
                                <img src={item.image} alt={item.name} className="order-item-mini-img" />
                                <div className="order-item-mini-info">
                                  <div className="mini-name">{item.name}</div>
                                  <div className="mini-sub">{item.color} • Qty {item.quantity}</div>
                                </div>
                                <div className="mini-price">{formatPrice(item.price * item.quantity)}</div>
                              </div>
                            ))}
                          </div>

                          <div className="cancelled-spec-box">
                            <div className="spec-row">
                              <span className="spec-title">Reason for Cancellation:</span>
                              <span className="spec-value">{ord.cancelReason || 'Customer requested order cancellation'}</span>
                            </div>
                            <div className="spec-row">
                              <span className="spec-title">Settlement & Refund:</span>
                              <span className="spec-value refund-highlight">
                                {ord.paymentStatus === 'refund_pending'
                                  ? `Full refund of ${formatPrice(ord.total)} initiated to original payment source (UPI / Card settlement in 2-3 business days)`
                                  : 'No payment charged (Cash on Delivery)'}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                )}
              </div>
            )}

            {/* ========================================================
                TAB 2: ADDRESS (Saved Addresses Manager)
               ======================================================== */}
            {activeNav === 'address' && (
              <div className="account-address-section">
                <div className="addresses-header">
                  <h4>Saved Delivery Addresses</h4>
                  <button className="btn-add-address" onClick={() => onOpenAddressModal(null)}>
                    + Add New Address
                  </button>
                </div>

                <div className="addresses-grid">
                  {(!user.savedAddresses || user.savedAddresses.length === 0) ? (
                    <div style={{ gridColumn: '1 / -1', padding: '2.5rem', textAlign: 'center', color: 'var(--text-muted)', background: 'var(--bg-warm)', borderRadius: 'var(--radius-md)', border: '1px dashed var(--border-card)' }}>
                      No saved addresses found. Click "+ Add New Address" above to save shipping info!
                    </div>
                  ) : (
                    user.savedAddresses.map((addr) => {
                      const id = addr._id || addr.id;
                      return (
                        <div key={id} className={`address-card ${addr.isDefault ? 'is-default' : ''}`}>
                          <div>
                            <div className="address-card-top">
                              <span className="addr-label-badge">{addr.label || 'Home'}</span>
                              {addr.isDefault && <span className="default-pill">DEFAULT</span>}
                            </div>
                            <div className="addr-recipient">{addr.recipientName}</div>
                            <div className="addr-street">
                              {addr.street}<br />
                              {addr.city}, {addr.state} {addr.zip}, {addr.country}
                            </div>
                            <div className="addr-phone">Phone: {addr.phone}</div>
                          </div>
                          <div className="addr-actions">
                            {!addr.isDefault && (
                              <button 
                                className="addr-action-btn" 
                                onClick={async () => {
                                  await setDefaultAddress(id);
                                  onShowToast('Default address updated', '✓');
                                }}
                              >
                                Make Default
                              </button>
                            )}
                            <button className="addr-action-btn" onClick={() => onOpenAddressModal(addr)}>
                              Edit
                            </button>
                            <button 
                              className="addr-action-btn delete" 
                              onClick={async () => {
                                if (window.confirm('Are you sure you want to delete this address?')) {
                                  await deleteAddress(id);
                                  onShowToast('Address deleted', '✓');
                                }
                              }}
                            >
                              Delete
                            </button>
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>
              </div>
            )}
          </div>
        ) : (
          /* Authentication Screen (if not logged in) */
          <div className="auth-portal-card">
            <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
              <div style={{ width: '56px', height: '56px', margin: '0 auto 1rem', background: 'var(--primary-light)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary)', boxShadow: 'var(--shadow-xs)' }}>
                {authMode === 'forgot' ? <IconRefresh size={26} /> : <IconUser size={28} />}
              </div>
              <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.65rem', color: 'var(--text-dark)', marginBottom: '0.35rem' }}>
                {authMode === 'forgot' ? 'Reset Account Password' : 'Customer Account Portal'}
              </h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', maxWidth: '360px', margin: '0 auto' }}>
                {authMode === 'forgot'
                  ? 'Enter your registered email address and create a new secure password to regain access.'
                  : 'Sign in to manage your orders, track handmade crochet packages, and manage saved delivery addresses.'}
              </p>
            </div>

            {authMode !== 'forgot' && (
              <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.75rem', justifyContent: 'center', background: 'var(--bg-warm)', padding: '0.35rem', borderRadius: 'var(--radius-full)', border: '1px solid var(--border-subtle)' }}>
                <button 
                  className={`filter-tab ${authMode === 'login' ? 'active' : ''}`}
                  onClick={() => setAuthMode('login')}
                  style={{ flex: 1, padding: '0.5rem 1rem' }}
                >
                  Sign In
                </button>
                <button 
                  className={`filter-tab ${authMode === 'register' ? 'active' : ''}`}
                  onClick={() => setAuthMode('register')}
                  style={{ flex: 1, padding: '0.5rem 1rem' }}
                >
                  Create Account
                </button>
              </div>
            )}

            {authMode === 'forgot' ? (
              <form onSubmit={handleForgotSubmit} className="form-grid">
                <div className="form-group">
                  <label className="form-label" htmlFor="forgotEmail">Registered Email Address</label>
                  <input 
                    type="email" 
                    id="forgotEmail" 
                    className="form-input" 
                    autoComplete="email" 
                    required 
                    placeholder="name@example.com"
                    value={forgotEmail}
                    onChange={(e) => setForgotEmail(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="forgotNewPass">New Password (min. 6 characters)</label>
                  <input 
                    type="password" 
                    id="forgotNewPass" 
                    className="form-input" 
                    autoComplete="new-password" 
                    required 
                    placeholder="••••••••"
                    value={forgotNewPassword}
                    onChange={(e) => setForgotNewPassword(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="forgotConfirmPass">Confirm New Password</label>
                  <input 
                    type="password" 
                    id="forgotConfirmPass" 
                    className="form-input" 
                    autoComplete="new-password" 
                    required 
                    placeholder="••••••••"
                    value={forgotConfirmPassword}
                    onChange={(e) => setForgotConfirmPassword(e.target.value)}
                  />
                </div>
                <button 
                  type="submit" 
                  className="btn btn-primary" 
                  disabled={forgotLoading}
                  style={{ marginTop: '0.75rem', width: '100%', padding: '0.85rem' }}
                >
                  {forgotLoading ? 'Updating Password...' : 'Reset Password & Sign In'}
                </button>
                <div style={{ textAlign: 'center', marginTop: '1rem' }}>
                  <button 
                    type="button" 
                    className="auth-back-btn"
                    onClick={() => setAuthMode('login')}
                  >
                    ← Back to Sign In
                  </button>
                </div>
              </form>
            ) : authMode === 'login' ? (
              <form onSubmit={handleLoginSubmit} className="form-grid">
                <div className="form-group">
                  <label className="form-label" htmlFor="accEmail">Email Address</label>
                  <input 
                    type="email" 
                    id="accEmail" 
                    className="form-input" 
                    autoComplete="email" 
                    required 
                    placeholder="name@example.com"
                    value={loginEmail}
                    onChange={(e) => setLoginEmail(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                    <label className="form-label" htmlFor="accPass" style={{ marginBottom: 0 }}>Password</label>
                    <button 
                      type="button" 
                      className="auth-forgot-link" 
                      onClick={() => {
                        setForgotEmail(loginEmail);
                        setAuthMode('forgot');
                      }}
                    >
                      Forgot password?
                    </button>
                  </div>
                  <input 
                    type="password" 
                    id="accPass" 
                    className="form-input" 
                    autoComplete="current-password" 
                    required 
                    placeholder="••••••••"
                    value={loginPassword}
                    onChange={(e) => setLoginPassword(e.target.value)}
                  />
                </div>
                <button type="submit" className="btn btn-primary" style={{ marginTop: '0.75rem', width: '100%', padding: '0.85rem' }}>
                  Sign In to Account
                </button>
              </form>
            ) : (
              <form onSubmit={handleRegisterSubmit} className="form-grid">
                <div className="form-group">
                  <label className="form-label" htmlFor="regName">Full Name</label>
                  <input 
                    type="text" 
                    id="regName" 
                    className="form-input" 
                    autoComplete="name" 
                    required 
                    placeholder="e.g. Priya Sharma"
                    value={regName}
                    onChange={(e) => setRegName(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="regEmail">Email Address</label>
                  <input 
                    type="email" 
                    id="regEmail" 
                    className="form-input" 
                    autoComplete="email" 
                    required 
                    placeholder="priya@example.com"
                    value={regEmail}
                    onChange={(e) => setRegEmail(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="regPhone">Mobile Number (India +91)</label>
                  <input 
                    type="tel" 
                    id="regPhone" 
                    className="form-input" 
                    autoComplete="tel" 
                    required 
                    placeholder="+91 98765 43210"
                    value={regPhone}
                    onChange={(e) => setRegPhone(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="regPass">Create Password</label>
                  <input 
                    type="password" 
                    id="regPass" 
                    className="form-input" 
                    autoComplete="new-password" 
                    required 
                    placeholder="••••••••"
                    value={regPassword}
                    onChange={(e) => setRegPassword(e.target.value)}
                  />
                </div>
                <button type="submit" className="btn btn-primary" style={{ marginTop: '0.75rem', width: '100%', padding: '0.85rem' }}>
                  Create Account & Join VIP Club
                </button>
              </form>
            )}
          </div>
        )}
      </main>
    </div>
  );
};
