import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { api } from '../services/api';
import { IconPackage, IconUser, IconRefresh, IconArrowRight, IconCheck, IconTruck } from './Icons';

export const AccountModal = ({ isOpen, onClose, onOpenAddressModal, onShowToast }) => {
  const { user, login, register, logout, deleteAddress, setDefaultAddress } = useAuth();
  const { formatPrice } = useCart();

  // Top-level account contents: 'orders', 'address', 'logout'
  const [activeNav, setActiveNav] = useState('orders');

  // Sub-categories inside orders: 'in-progress', 'return', 'delivered'
  const [orderSubtab, setOrderSubtab] = useState('in-progress');

  // ID of the in-progress order currently displaying expanded live tracking
  const [expandedTrackId, setExpandedTrackId] = useState(null);

  // Auth mode if user is not logged in
  const [authMode, setAuthMode] = useState('login'); // 'login' or 'register'
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
      const inProg = list.filter(o => o.statusCode !== 'delivered' && o.statusCode !== 'returned');
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

  // Filter orders by sub-category
  const inProgressOrders = orders.filter(o => o.statusCode !== 'delivered' && o.statusCode !== 'returned');
  const returnOrders = orders.filter(o => o.statusCode === 'returned');
  const deliveredOrders = orders.filter(o => o.statusCode === 'delivered');

  const initials = user?.name ? user.name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2) : 'BG';

  return (
    <div className="modal-overlay active" onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="modal-card account-modal-card">
        <button className="modal-close-btn" onClick={onClose} aria-label="Close Account modal">&times;</button>

        {/* Logged In View */}
        {user?.isLoggedIn ? (
          <div>
            {/* User Profile Header Card */}
            <div className="profile-hero">
              <div className="profile-avatar-large">{initials}</div>
              <div className="profile-info">
                <h3>{user.name}</h3>
                <p>{user.email} • Mobile: {user.phone}</p>
                <span className="tier-badge">{user.memberTier || 'VIP Member'}</span>
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
                <div className="order-subtabs-bar">
                  <button 
                    className={`order-subtab-pill ${orderSubtab === 'in-progress' ? 'active' : ''}`}
                    onClick={() => setOrderSubtab('in-progress')}
                  >
                    <span>In-Progress</span>
                    <span className="subtab-count-badge">{inProgressOrders.length}</span>
                  </button>
                  <button 
                    className={`order-subtab-pill ${orderSubtab === 'return' ? 'active' : ''}`}
                    onClick={() => setOrderSubtab('return')}
                  >
                    <span>Return</span>
                    <span className="subtab-count-badge">{returnOrders.length}</span>
                  </button>
                  <button 
                    className={`order-subtab-pill ${orderSubtab === 'delivered' ? 'active' : ''}`}
                    onClick={() => setOrderSubtab('delivered')}
                  >
                    <span>Delivered</span>
                    <span className="subtab-count-badge">{deliveredOrders.length}</span>
                  </button>
                </div>

                {loadingOrders && (
                  <div style={{ textAlign: 'center', padding: '2rem 0', color: 'var(--text-muted)' }}>
                    <div style={{ width: '28px', height: '28px', border: '2px solid #E2E8F0', borderTopColor: 'var(--primary)', borderRadius: '50%', margin: '0 auto', animation: 'spin 0.8s linear infinite' }}></div>
                    <p style={{ marginTop: '0.75rem', fontSize: '0.88rem' }}>Loading your orders...</p>
                  </div>
                )}

                {/* Subtab Content: IN-PROGRESS */}
                {orderSubtab === 'in-progress' && (
                  <div className="orders-list-container">
                    {inProgressOrders.length === 0 ? (
                      <div className="empty-subtab-box">
                        <div className="empty-icon" style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
                          <IconPackage size={36} />
                        </div>
                        <h4>No In-Progress Orders</h4>
                        <p>You don't have any orders currently being crafted or in transit.</p>
                      </div>
                    ) : (
                      inProgressOrders.map((ord) => {
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
                            <div className="in-progress-btn-wrapper">
                              <button 
                                className={`btn-track-in-progress ${isExpanded ? 'active' : ''}`}
                                onClick={() => setExpandedTrackId(isExpanded ? null : ord.orderId)}
                                aria-expanded={isExpanded}
                                style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.45rem' }}
                              >
                                <IconTruck size={15} />
                                <span>{isExpanded ? 'Collapse Tracking' : 'Track Order'}</span>
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
                    <div style={{ gridColumn: '1 / -1', padding: '2rem', textAlign: 'center', color: 'var(--text-muted)', background: '#F4FAFF', borderRadius: 'var(--radius-md)' }}>
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
          <div>
            <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
              <div style={{ width: '48px', height: '48px', margin: '0 auto 1rem', background: '#F1F5F9', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary)' }}>
                <IconUser size={24} />
              </div>
              <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.4rem', color: 'var(--text-dark)' }}>
                Account Sign In
              </h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem' }}>
                Sign in to manage your orders, track packages, and view saved addresses.
              </p>
            </div>

            <div style={{ display: 'flex', gap: '0.8rem', marginBottom: '1.5rem', justifyContent: 'center' }}>
              <button 
                className={`filter-tab ${authMode === 'login' ? 'active' : ''}`}
                onClick={() => setAuthMode('login')}
              >
                Sign In
              </button>
              <button 
                className={`filter-tab ${authMode === 'register' ? 'active' : ''}`}
                onClick={() => setAuthMode('register')}
              >
                Create Account
              </button>
            </div>

            {authMode === 'login' ? (
              <form onSubmit={handleLoginSubmit} className="form-grid">
                <div className="form-group">
                  <label className="form-label" htmlFor="accEmail">Email Address</label>
                  <input 
                    type="email" 
                    id="accEmail" 
                    className="form-input" 
                    autoComplete="email" 
                    required 
                    value={loginEmail}
                    onChange={(e) => setLoginEmail(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="accPass">Password</label>
                  <input 
                    type="password" 
                    id="accPass" 
                    className="form-input" 
                    autoComplete="current-password" 
                    required 
                    value={loginPassword}
                    onChange={(e) => setLoginPassword(e.target.value)}
                  />
                </div>
                <button type="submit" className="btn btn-primary" style={{ marginTop: '0.5rem' }}>
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
                    placeholder="Full Name"
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
                    placeholder="name@example.com"
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
                <button type="submit" className="btn btn-primary" style={{ marginTop: '0.5rem' }}>
                  Create Account & Join VIP Club
                </button>
              </form>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
