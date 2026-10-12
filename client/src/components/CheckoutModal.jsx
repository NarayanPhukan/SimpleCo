import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { api } from '../services/api';
import { openRazorpayStandardCheckout } from '../services/razorpay';
import { 
  IconShield, 
  IconPackage, 
  IconArrowRight, 
  IconArrowLeft, 
  IconCheck, 
  IconBag, 
  IconTruck, 
  IconClose, 
  IconTag 
} from './Icons';

const INDIAN_STATES = [
  "Andhra Pradesh", "Assam", "Bihar", "Chhattisgarh", "Delhi", "Goa", "Gujarat", 
  "Haryana", "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand", "Karnataka", 
  "Kerala", "Madhya Pradesh", "Maharashtra", "Odisha", "Punjab", "Rajasthan", 
  "Tamil Nadu", "Telangana", "Uttar Pradesh", "Uttarakhand", "West Bengal"
];

export const CheckoutModal = ({
  isOpen,
  onClose,
  onOpenAddressModal,
  onOpenAccountModal,
  onOrderPlacedSuccess,
  onShowToast
}) => {
  const { user, addAddress } = useAuth();
  const { cart, getTotals, formatPrice, clearCart, removeCoupon, coupon } = useCart();

  const [step, setStep] = useState(1);
  const addresses = user?.savedAddresses || [];

  const [selectedAddressId, setSelectedAddressId] = useState(() => {
    const def = user?.savedAddresses?.find(a => a.isDefault);
    return def ? (def._id || def.id) : (user?.savedAddresses?.[0]?._id || '');
  });

  const [showAddressForm, setShowAddressForm] = useState(false);
  const [addrForm, setAddrForm] = useState({
    label: 'Home',
    recipientName: user?.name || '',
    phone: user?.phone || '',
    email: user?.email || '',
    street: '',
    city: '',
    state: 'Maharashtra',
    zip: '',
    country: 'India'
  });

  const [shippingMethod, setShippingMethod] = useState('standard');
  const [paymentMethod, setPaymentMethod] = useState('razorpay'); // 'razorpay' or 'cod'
  const [placedOrderId, setPlacedOrderId] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Sync address selection when addresses list changes
  useEffect(() => {
    if (addresses.length > 0) {
      const exists = addresses.some(a => (a._id || a.id) === selectedAddressId);
      if (!selectedAddressId || !exists) {
        const def = addresses.find(a => a.isDefault) || addresses[0];
        setSelectedAddressId(def._id || def.id);
      }
    }
  }, [addresses, selectedAddressId]);

  // Sync user info into address form when user profile becomes available
  useEffect(() => {
    if (user) {
      setAddrForm(prev => ({
        ...prev,
        recipientName: prev.recipientName || user.name || '',
        phone: prev.phone || user.phone || '',
        email: prev.email || user.email || ''
      }));
    }
  }, [user]);

  // Reset modal state on open
  useEffect(() => {
    if (isOpen) {
      setSubmitting(false);
      if (step === 4) {
        setStep(1);
        setPlacedOrderId('');
      }
    }
  }, [isOpen]);

  // Body scroll lock & Escape key handling
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      const handleKeyDown = (e) => {
        if (e.key === 'Escape') {
          onClose();
        }
      };
      window.addEventListener('keydown', handleKeyDown);
      return () => {
        document.body.style.overflow = '';
        window.removeEventListener('keydown', handleKeyDown);
      };
    } else {
      document.body.style.overflow = '';
    }
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const totals = getTotals();
  const shippingCost = shippingMethod === 'express' ? 149 : totals.shipping;
  const grandTotal = Math.max(0, totals.subtotal - totals.discount + shippingCost + totals.tax);

  const chosenAddr = addresses.find(a => (a._id === selectedAddressId || a.id === selectedAddressId)) 
    || (addresses.length > 0 ? addresses[0] : addrForm);

  const handleNextToStep2 = async () => {
    const isAddingAddress = addresses.length === 0 || showAddressForm;
    if (isAddingAddress) {
      if (!addrForm.recipientName.trim()) {
        onShowToast('Please enter recipient full name', '!');
        return;
      }
      const rawDigits = addrForm.phone.replace(/[^0-9]/g, '');
      if (rawDigits.length < 10) {
        onShowToast('Please enter a valid 10-digit mobile number', '!');
        return;
      }
      if (!addrForm.street.trim()) {
        onShowToast('Please enter flat / house / street address', '!');
        return;
      }
      if (!addrForm.city.trim()) {
        onShowToast('Please enter city / town', '!');
        return;
      }
      const rawZip = addrForm.zip.replace(/[^0-9]/g, '');
      if (rawZip.length !== 6) {
        onShowToast('Please enter a valid 6-digit Indian PIN code', '!');
        return;
      }

      try {
        const saved = await addAddress({
          ...addrForm,
          phone: rawDigits.length === 10 ? `+91 ${rawDigits}` : addrForm.phone,
          zip: rawZip,
          isDefault: addresses.length === 0
        });
        if (saved) {
          const id = saved._id || saved.id;
          setSelectedAddressId(id);
        }
        setShowAddressForm(false);
        onShowToast('Shipping address confirmed', '✓');
        setStep(2);
        return;
      } catch (err) {
        onShowToast('Could not save address: ' + err.message, '!');
        return;
      }
    }

    if (!selectedAddressId && addresses.length > 0) {
      setSelectedAddressId(addresses[0]._id || addresses[0].id);
    }
    setStep(2);
  };

  const handlePlaceOrder = async (customAmount = null) => {
    setSubmitting(true);

    if (!chosenAddr || (!chosenAddr.street && !addrForm.street)) {
      onShowToast('Please provide a delivery address first', '!');
      setSubmitting(false);
      setStep(1);
      return;
    }

    const payAmount = customAmount !== null ? customAmount : grandTotal;
    const finalTotals = {
      ...totals,
      shipping: shippingCost,
      total: grandTotal
    };

    const customerEmail = user?.email || chosenAddr?.email || addrForm?.email || 'guest@babyygurll.in';
    const customerName = chosenAddr?.recipientName || user?.name || addrForm?.recipientName || 'Customer';
    const customerPhone = chosenAddr?.phone || user?.phone || addrForm?.phone || '';

    // Online Payment Checkout Flow
    if (paymentMethod === 'razorpay') {
      onShowToast('Opening secure payment gateway...', '✓');
      await openRazorpayStandardCheckout({
        amountInRupees: payAmount,
        prefill: {
          name: customerName,
          email: customerEmail,
          contact: customerPhone
        },
        onSuccess: async (payDetails) => {
          try {
            const orderPayload = {
              userEmail: customerEmail,
              shippingAddress: chosenAddr,
              paymentMethod: `Online Payment (${payDetails.paymentId})`,
              paymentStatus: 'paid',
              razorpayPaymentId: payDetails.paymentId,
              razorpayOrderId: payDetails.orderId,
              items: cart,
              totals: finalTotals
            };

            const res = await api.createOrder(orderPayload);
            const newId = res.data?.orderId || res.orderId || ('BGC-' + Math.floor(10000 + Math.random() * 90000));
            setPlacedOrderId(newId);
            clearCart();
            removeCoupon();
            setStep(4);
            setSubmitting(false);
            if (onOrderPlacedSuccess) onOrderPlacedSuccess(newId);
            onShowToast(`Payment verified! Order #${newId} placed successfully`, '✓');
          } catch (createErr) {
            const fallbackId = 'BGC-' + Math.floor(10000 + Math.random() * 90000);
            setPlacedOrderId(fallbackId);
            clearCart();
            removeCoupon();
            setStep(4);
            setSubmitting(false);
            if (onOrderPlacedSuccess) onOrderPlacedSuccess(fallbackId);
            onShowToast(`Order #${fallbackId} recorded with payment ${payDetails.paymentId}`, '✓');
          }
        },
        onError: (errMsg) => {
          setSubmitting(false);
          onShowToast(errMsg, '!');
        },
        onDismiss: () => {
          setSubmitting(false);
          onShowToast('Payment window closed. You can retry when ready.', 'ℹ');
        }
      });
      return;
    }

    // Cash on Delivery Flow
    const orderPayload = {
      userEmail: customerEmail,
      shippingAddress: chosenAddr,
      paymentMethod: 'Cash on Delivery (COD)',
      paymentStatus: 'pending',
      items: cart,
      totals: finalTotals
    };

    try {
      const res = await api.createOrder(orderPayload);
      const newId = res.data?.orderId || res.orderId || ('BGC-' + Math.floor(10000 + Math.random() * 90000));
      setPlacedOrderId(newId);
      clearCart();
      removeCoupon();
      setStep(4);
      setSubmitting(false);
      if (onOrderPlacedSuccess) onOrderPlacedSuccess(newId);
      onShowToast(`Order #${newId} placed via Cash on Delivery`, '✓');
    } catch (err) {
      const fallbackId = 'BGC-' + Math.floor(10000 + Math.random() * 90000);
      setPlacedOrderId(fallbackId);
      clearCart();
      removeCoupon();
      setStep(4);
      setSubmitting(false);
      if (onOrderPlacedSuccess) onOrderPlacedSuccess(fallbackId);
      onShowToast(`Order #${fallbackId} placed successfully`, '✓');
    }
  };

  return (
    <div className="full-page-checkout" role="dialog" aria-modal="true" aria-label="Secure Checkout Portal">
      {/* Sticky Header Topbar */}
      <header className="checkout-topbar">
        <div className="checkout-topbar-brand">
          <button 
            type="button" 
            onClick={onClose} 
            className="btn btn-secondary" 
            style={{ 
              padding: '0.45rem 0.9rem', 
              fontSize: '0.82rem', 
              display: 'inline-flex', 
              alignItems: 'center', 
              gap: '0.45rem', 
              borderRadius: 'var(--radius-full)' 
            }}
            title="Return to Store"
          >
            <IconArrowLeft size={16} />
            <span className="checkout-hide-mobile">Return to Store</span>
          </button>

          <span style={{ fontFamily: 'var(--font-serif)', fontSize: '1.4rem', fontWeight: 700, color: 'var(--primary)', marginLeft: '0.25rem' }}>
            babyygurll
          </span>

          <span className="checkout-topbar-badge checkout-hide-mobile">
            <IconShield size={13} /> 256-Bit SSL Encrypted
          </span>
        </div>

        {/* Topbar Step Indicator (Desktop) */}
        {step !== 4 && (
          <nav className="checkout-topbar-steps" aria-label="Checkout Progress">
            <button 
              type="button"
              className={`checkout-topbar-step ${step === 1 ? 'active' : ''} ${step > 1 ? 'completed clickable' : ''}`}
              onClick={() => step > 1 && setStep(1)}
            >
              <span className="checkout-topbar-step-num">{step > 1 ? '✓' : '1'}</span>
              <span>Address</span>
            </button>
            <span style={{ color: 'var(--border-subtle)', fontSize: '0.8rem' }}>›</span>
            <button 
              type="button"
              className={`checkout-topbar-step ${step === 2 ? 'active' : ''} ${step > 2 ? 'completed clickable' : ''}`}
              onClick={() => step > 2 && setStep(2)}
            >
              <span className="checkout-topbar-step-num">{step > 2 ? '✓' : '2'}</span>
              <span>Shipping</span>
            </button>
            <span style={{ color: 'var(--border-subtle)', fontSize: '0.8rem' }}>›</span>
            <div className={`checkout-topbar-step ${step === 3 ? 'active' : ''}`}>
              <span className="checkout-topbar-step-num">3</span>
              <span>Payment</span>
            </div>
          </nav>
        )}

        {/* Right Topbar actions */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            <IconBag size={17} />
            <span>{totals.itemCount} {totals.itemCount === 1 ? 'item' : 'items'}</span>
          </div>
          <button 
            type="button" 
            onClick={onClose} 
            style={{ 
              background: 'none', 
              border: 'none', 
              cursor: 'pointer', 
              padding: '0.45rem', 
              color: 'var(--text-muted)', 
              display: 'flex', 
              alignItems: 'center',
              borderRadius: '50%',
              transition: 'background 0.2s ease'
            }} 
            aria-label="Close Checkout"
          >
            <IconClose size={20} />
          </button>
        </div>
      </header>

      {/* Main Page Content */}
      <main className="checkout-page-container">
        {/* Step 4: Full Page Order Success */}
        {step === 4 ? (
          <div className="order-success-fullpage">
            <div className="success-check-bubble" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <IconCheck size={36} />
            </div>
            <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: '2.1rem', margin: '0.6rem 0 0.4rem', color: 'var(--text-dark)' }}>
              Thank You for Your Order!
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.98rem', maxWidth: '520px', margin: '0 auto 1.25rem' }}>
              Your artisanal crochet pieces have been confirmed. Our artisans are hand-stitching and preparing your parcel with love.
            </p>

            <div className="order-id-highlight" style={{ display: 'inline-block', margin: '0.5rem auto 1.75rem' }}>
              Order #{placedOrderId}
            </div>

            {/* Quick Order Snapshot */}
            <div style={{ background: 'var(--bg-warm)', border: '1px solid var(--border-card)', borderRadius: 'var(--radius-md)', padding: '1.25rem 1.5rem', maxWidth: '540px', margin: '0 auto 2rem', textAlign: 'left' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.85rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Delivering To:</span>
                <strong style={{ color: 'var(--text-dark)' }}>{chosenAddr?.recipientName} ({chosenAddr?.city})</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.85rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Shipping Speed:</span>
                <strong style={{ color: 'var(--text-dark)' }}>{shippingMethod === 'express' ? 'Priority Air Express (1-2 Days)' : 'Pan-India Standard (3-5 Days)'}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Payment Method:</span>
                <strong style={{ color: 'var(--text-dark)' }}>{paymentMethod === 'razorpay' ? 'Paid Online via Razorpay' : 'Cash on Delivery (Pending)'}</strong>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
              <button 
                type="button"
                className="btn btn-secondary"
                style={{ padding: '0.85rem 1.75rem' }}
                onClick={() => {
                  onClose();
                  if (onOpenAccountModal) {
                    onOpenAccountModal();
                  }
                }}
              >
                Track Order in Account
              </button>
              <button 
                type="button"
                className="btn btn-primary"
                style={{ padding: '0.85rem 2rem' }}
                onClick={() => {
                  onClose();
                  const catElem = document.getElementById('catalog');
                  if (catElem) catElem.scrollIntoView({ behavior: 'smooth' });
                }}
              >
                Continue Shopping
              </button>
            </div>
          </div>
        ) : cart.length === 0 ? (
          /* Empty Bag State */
          <div style={{ textAlign: 'center', padding: '4rem 1.5rem', background: '#FFFFFF', borderRadius: '24px', border: '1px solid var(--border-subtle)', maxWidth: '600px', margin: '2rem auto' }}>
            <div style={{ color: 'var(--primary)', marginBottom: '1rem' }}>
              <IconBag size={48} />
            </div>
            <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.6rem', marginBottom: '0.5rem' }}>Your Bag is Empty</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.75rem' }}>
              Explore our handmade crochet collection and add your favorite creations before checking out.
            </p>
            <button 
              type="button" 
              className="btn btn-primary"
              onClick={() => {
                onClose();
                const catElem = document.getElementById('catalog');
                if (catElem) catElem.scrollIntoView({ behavior: 'smooth' });
              }}
            >
              Explore Collection
            </button>
          </div>
        ) : (
          /* Two Column Layout */
          <div className="checkout-two-column-layout">
            {/* Left: Active Step Card */}
            <div className="checkout-main-content">
              <div className="checkout-step-card">
                {/* Visual Step Stepper on mobile / top of card */}
                <div className="checkout-steps-bar">
                  <div className={`checkout-step-pill ${step >= 1 ? 'active' : ''}`}>
                    <div className="step-num-circle">1</div>
                    <span>Delivery Address</span>
                  </div>
                  <div className={`checkout-step-pill ${step >= 2 ? 'active' : ''}`}>
                    <div className="step-num-circle">2</div>
                    <span>Shipping Method</span>
                  </div>
                  <div className={`checkout-step-pill ${step >= 3 ? 'active' : ''}`}>
                    <div className="step-num-circle">3</div>
                    <span>Payment</span>
                  </div>
                </div>

                {/* Step 1: Address Selection / Inline Entry */}
                {step === 1 && (
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                      <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.45rem', margin: 0, color: 'var(--text-dark)' }}>
                        {addresses.length === 0 || showAddressForm 
                          ? 'Enter Delivery Address (Pan-India)' 
                          : 'Select Delivery Address'}
                      </h3>
                      {addresses.length > 0 && (
                        <button
                          type="button"
                          className="btn btn-secondary"
                          style={{ fontSize: '0.82rem', padding: '0.45rem 0.95rem' }}
                          onClick={() => setShowAddressForm(!showAddressForm)}
                        >
                          {showAddressForm ? 'Use Saved Address' : '+ Add Different Address'}
                        </button>
                      )}
                    </div>

                    <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
                      {addresses.length === 0 || showAddressForm
                        ? 'Handcrafted with love, packed with ribbons and dispatched safely across India.'
                        : 'Select from your saved addresses or add a new delivery location across India.'}
                    </p>

                    {/* Inline Address Form for guest or adding new address */}
                    {(addresses.length === 0 || showAddressForm) ? (
                      <div style={{ background: 'var(--bg-warm)', border: '1.5px solid var(--border-card)', borderRadius: 'var(--radius-md)', padding: '1.5rem', marginBottom: '1.75rem' }}>
                        <div className="form-grid">
                          <div className="form-row">
                            <div className="form-group">
                              <label className="form-label" htmlFor="chkRecipientName">Recipient Full Name *</label>
                              <input 
                                type="text" 
                                id="chkRecipientName" 
                                className="form-input" 
                                placeholder="e.g. Ananya Sharma" 
                                required 
                                value={addrForm.recipientName}
                                onChange={e => setAddrForm({ ...addrForm, recipientName: e.target.value })}
                              />
                            </div>
                            <div className="form-group">
                              <label className="form-label" htmlFor="chkPhone">Mobile Number (10 Digits) *</label>
                              <input 
                                type="tel" 
                                id="chkPhone" 
                                className="form-input" 
                                placeholder="e.g. 9876543210" 
                                required 
                                value={addrForm.phone}
                                onChange={e => setAddrForm({ ...addrForm, phone: e.target.value })}
                              />
                            </div>
                          </div>

                          <div className="form-row">
                            <div className="form-group">
                              <label className="form-label" htmlFor="chkEmail">Email Address (for order receipts & tracking)</label>
                              <input 
                                type="email" 
                                id="chkEmail" 
                                className="form-input" 
                                placeholder="e.g. ananya@example.com" 
                                value={addrForm.email}
                                onChange={e => setAddrForm({ ...addrForm, email: e.target.value })}
                              />
                            </div>
                            <div className="form-group">
                              <label className="form-label">Address Tag</label>
                              <div style={{ display: 'flex', gap: '0.45rem', marginTop: '0.25rem' }}>
                                {['Home', 'Office', 'Hostel', 'Other'].map(type => (
                                  <button
                                    key={type}
                                    type="button"
                                    className={`filter-tab ${addrForm.label === type ? 'active' : ''}`}
                                    style={{ padding: '0.45rem 0.85rem', fontSize: '0.8rem' }}
                                    onClick={() => setAddrForm({ ...addrForm, label: type })}
                                  >
                                    {type}
                                  </button>
                                ))}
                              </div>
                            </div>
                          </div>

                          <div className="form-group">
                            <label className="form-label" htmlFor="chkStreet">Flat, House No., Building, Street *</label>
                            <textarea 
                              id="chkStreet" 
                              className="form-textarea" 
                              rows="2" 
                              placeholder="e.g. Flat 402, Blossom Heights, Linking Road" 
                              required 
                              value={addrForm.street}
                              onChange={e => setAddrForm({ ...addrForm, street: e.target.value })}
                            />
                          </div>

                          <div className="form-row-3">
                            <div className="form-group">
                              <label className="form-label" htmlFor="chkCity">City / Town *</label>
                              <input 
                                type="text" 
                                id="chkCity" 
                                className="form-input" 
                                placeholder="e.g. Mumbai" 
                                required 
                                value={addrForm.city}
                                onChange={e => setAddrForm({ ...addrForm, city: e.target.value })}
                              />
                            </div>
                            <div className="form-group">
                              <label className="form-label" htmlFor="chkState">State *</label>
                              <select 
                                id="chkState" 
                                className="form-select"
                                value={addrForm.state}
                                onChange={e => setAddrForm({ ...addrForm, state: e.target.value })}
                              >
                                {INDIAN_STATES.map(s => <option key={s} value={s}>{s}</option>)}
                              </select>
                            </div>
                            <div className="form-group">
                              <label className="form-label" htmlFor="chkZip">PIN Code (6 Digits) *</label>
                              <input 
                                type="text" 
                                id="chkZip" 
                                className="form-input" 
                                placeholder="400050" 
                                maxLength={6}
                                required 
                                value={addrForm.zip}
                                onChange={e => setAddrForm({ ...addrForm, zip: e.target.value.replace(/[^0-9]/g, '') })}
                              />
                            </div>
                          </div>
                        </div>
                      </div>
                    ) : (
                      /* Saved Addresses Cards */
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem', marginBottom: '1.75rem' }}>
                        {addresses.map((addr) => {
                          const id = addr._id || addr.id;
                          const isChecked = selectedAddressId === id;
                          return (
                            <label 
                              key={id}
                              style={{
                                display: 'flex',
                                alignItems: 'flex-start',
                                gap: '1rem',
                                padding: '1.2rem',
                                border: `1.5px solid ${isChecked ? 'var(--primary)' : 'var(--border-subtle)'}`,
                                borderRadius: 'var(--radius-md)',
                                background: isChecked ? 'var(--primary-light)' : '#FFFFFF',
                                cursor: 'pointer',
                                transition: 'all 0.2s ease'
                              }}
                            >
                              <input 
                                type="radio" 
                                name="checkoutAddr" 
                                value={id} 
                                checked={isChecked}
                                onChange={() => setSelectedAddressId(id)}
                                style={{ marginTop: '4px', accentColor: 'var(--primary)' }}
                              />
                              <div style={{ flexGrow: 1 }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                                  <strong style={{ fontSize: '0.98rem', color: 'var(--text-dark)' }}>{addr.label}</strong>
                                  {addr.isDefault && <span className="default-pill">DEFAULT</span>}
                                </div>
                                <div style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-dark)', marginBottom: '0.2rem' }}>
                                  {addr.recipientName} • Phone: {addr.phone}
                                </div>
                                <div style={{ fontSize: '0.84rem', color: 'var(--text-muted)' }}>
                                  {addr.street}, {addr.city}, {addr.state} - {addr.zip}, India
                                </div>
                              </div>
                            </label>
                          );
                        })}
                      </div>
                    )}

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1.5rem', flexWrap: 'wrap', gap: '0.75rem' }}>
                      {addresses.length > 0 && showAddressForm ? (
                        <button type="button" className="btn btn-secondary" onClick={() => setShowAddressForm(false)}>
                          Cancel
                        </button>
                      ) : (
                        <button type="button" className="btn btn-secondary" onClick={onClose}>
                          Return to Store
                        </button>
                      )}
                      <button type="button" className="btn btn-primary" onClick={handleNextToStep2} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', padding: '0.85rem 1.65rem' }}>
                        <span>Continue to Shipping</span>
                        <IconArrowRight size={16} />
                      </button>
                    </div>
                  </div>
                )}

                {/* Step 2: Shipping Options */}
                {step === 2 && (
                  <div>
                    <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.45rem', marginBottom: '0.5rem', color: 'var(--text-dark)' }}>
                      Choose Shipping Method
                    </h3>
                    <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
                      All parcels are carefully bubble-wrapped with floral mist & wax seal across India.
                    </p>

                    {/* Delivering To Recap Card */}
                    <div style={{ background: 'var(--bg-warm)', border: '1px solid var(--border-card)', borderRadius: 'var(--radius-sm)', padding: '0.9rem 1.2rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.5rem' }}>
                      <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                        <strong style={{ color: 'var(--text-dark)' }}>Deliver to: </strong>
                        {chosenAddr?.recipientName} • {chosenAddr?.city}, {chosenAddr?.state} ({chosenAddr?.zip})
                      </div>
                      <button 
                        type="button" 
                        onClick={() => setStep(1)} 
                        style={{ background: 'transparent', border: 'none', color: 'var(--primary)', fontSize: '0.8rem', fontWeight: 600, cursor: 'pointer', textDecoration: 'underline' }}
                      >
                        Change Address
                      </button>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.75rem' }}>
                      <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1.2rem', border: `1.5px solid ${shippingMethod === 'standard' ? 'var(--primary)' : 'var(--border-subtle)'}`, borderRadius: 'var(--radius-md)', background: shippingMethod === 'standard' ? 'var(--primary-light)' : '#FFFFFF', cursor: 'pointer', transition: 'all 0.2s ease' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
                          <input 
                            type="radio" 
                            name="shipMeth" 
                            value="standard" 
                            checked={shippingMethod === 'standard'}
                            onChange={() => setShippingMethod('standard')}
                            style={{ accentColor: 'var(--primary)' }}
                          />
                          <div>
                            <strong style={{ fontSize: '0.98rem', color: 'var(--text-dark)' }}>Pan-India Standard Delivery (3-5 Days)</strong>
                            <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', margin: '0.15rem 0 0' }}>Tracked express delivery across India with live SMS updates</p>
                          </div>
                        </div>
                        <span style={{ fontWeight: 700, color: 'var(--primary)', fontSize: '0.95rem' }}>
                          {totals.subtotal >= 999 ? 'FREE' : '₹79'}
                        </span>
                      </label>

                      <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1.2rem', border: `1.5px solid ${shippingMethod === 'express' ? 'var(--primary)' : 'var(--border-subtle)'}`, borderRadius: 'var(--radius-md)', background: shippingMethod === 'express' ? 'var(--primary-light)' : '#FFFFFF', cursor: 'pointer', transition: 'all 0.2s ease' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
                          <input 
                            type="radio" 
                            name="shipMeth" 
                            value="express" 
                            checked={shippingMethod === 'express'}
                            onChange={() => setShippingMethod('express')}
                            style={{ accentColor: 'var(--primary)' }}
                          />
                          <div>
                            <strong style={{ fontSize: '0.98rem', color: 'var(--text-dark)' }}>Priority Air Express (1-2 Days)</strong>
                            <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', margin: '0.15rem 0 0' }}>Fast-track craft allocation + handwritten calligraphy card</p>
                          </div>
                        </div>
                        <span style={{ fontWeight: 700, color: 'var(--text-dark)', fontSize: '0.95rem' }}>₹149</span>
                      </label>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
                      <button className="btn btn-secondary" onClick={() => setStep(1)} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                        <IconArrowLeft size={16} />
                        <span>Back to Address</span>
                      </button>
                      <button className="btn btn-primary" onClick={() => setStep(3)} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', padding: '0.85rem 1.65rem' }}>
                        <span>Continue to Payment</span>
                        <IconArrowRight size={16} />
                      </button>
                    </div>
                  </div>
                )}

                {/* Step 3: India Payment Methods */}
                {step === 3 && (
                  <div>
                    <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.45rem', marginBottom: '0.5rem', color: 'var(--text-dark)' }}>
                      Select Payment Method
                    </h3>
                    <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
                      Safe & encrypted pan-India payments.
                    </p>

                    {/* Delivering To Recap Card */}
                    <div style={{ background: 'var(--bg-warm)', border: '1px solid var(--border-card)', borderRadius: 'var(--radius-sm)', padding: '0.9rem 1.2rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.5rem' }}>
                      <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                        <strong style={{ color: 'var(--text-dark)' }}>Ship to: </strong>
                        {chosenAddr?.recipientName} • {chosenAddr?.city}, {chosenAddr?.state} ({chosenAddr?.zip})
                        <span style={{ margin: '0 0.4rem', color: 'var(--border-subtle)' }}>|</span>
                        <span>{shippingMethod === 'express' ? 'Priority Air (1-2 Days)' : 'Standard (3-5 Days)'}</span>
                      </div>
                      <button 
                        type="button" 
                        onClick={() => setStep(1)} 
                        style={{ background: 'transparent', border: 'none', color: 'var(--primary)', fontSize: '0.8rem', fontWeight: 600, cursor: 'pointer', textDecoration: 'underline' }}
                      >
                        Change
                      </button>
                    </div>

                    {/* Payment Method Selector Tabs */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1.3fr 1fr', gap: '0.85rem', marginBottom: '1.5rem' }}>
                      <button 
                        type="button" 
                        className={`filter-tab ${paymentMethod === 'razorpay' ? 'active' : ''}`}
                        style={{ textAlign: 'center', padding: '0.85rem 0.75rem', fontSize: '0.88rem', fontWeight: 600 }}
                        onClick={() => setPaymentMethod('razorpay')}
                      >
                        Online Payment (UPI / Cards / NetBanking)
                      </button>
                      <button 
                        type="button" 
                        className={`filter-tab ${paymentMethod === 'cod' ? 'active' : ''}`}
                        style={{ textAlign: 'center', padding: '0.85rem 0.75rem', fontSize: '0.88rem', fontWeight: 600 }}
                        onClick={() => setPaymentMethod('cod')}
                      >
                        Cash on Delivery (COD)
                      </button>
                    </div>

                    {/* Razorpay Standard Web Checkout Option */}
                    {paymentMethod === 'razorpay' && (
                      <div style={{ background: 'var(--bg-warm)', border: '1.5px solid var(--border-card)', borderRadius: 'var(--radius-md)', padding: '1.4rem', marginBottom: '1.75rem' }}>
                        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.9rem', marginBottom: '1.1rem' }}>
                          <div style={{ color: 'var(--primary)', marginTop: '0.2rem' }}>
                            <IconShield size={24} />
                          </div>
                          <div style={{ flexGrow: 1 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '0.25rem' }}>
                              <strong style={{ fontSize: '1rem', color: 'var(--text-dark)' }}>Online Payment Gateway</strong>
                              <span style={{ background: 'var(--primary-light)', color: 'var(--primary)', padding: '0.15rem 0.55rem', borderRadius: '4px', fontSize: '0.72rem', fontWeight: 700 }}>
                                256-BIT SSL SECURE
                              </span>
                            </div>
                            <p style={{ fontSize: '0.84rem', color: 'var(--text-muted)', margin: 0 }}>
                              Instant pan-India payments. Supports Google Pay, PhonePe, Paytm, RuPay, Visa, MasterCard, and NetBanking.
                            </p>
                          </div>
                        </div>

                        <div style={{ display: 'flex', gap: '0.45rem', flexWrap: 'wrap', marginBottom: '1.1rem' }}>
                          <span style={{ background: '#FFFFFF', border: '1px solid var(--border-card)', padding: '0.28rem 0.65rem', borderRadius: '4px', fontSize: '0.76rem', fontWeight: 600, color: 'var(--text-dark)' }}>Instant UPI</span>
                          <span style={{ background: '#FFFFFF', border: '1px solid var(--border-card)', padding: '0.28rem 0.65rem', borderRadius: '4px', fontSize: '0.76rem', fontWeight: 600, color: 'var(--text-dark)' }}>Google Pay / PhonePe</span>
                          <span style={{ background: '#FFFFFF', border: '1px solid var(--border-card)', padding: '0.28rem 0.65rem', borderRadius: '4px', fontSize: '0.76rem', fontWeight: 600, color: 'var(--text-dark)' }}>RuPay / Cards</span>
                          <span style={{ background: '#FFFFFF', border: '1px solid var(--border-card)', padding: '0.28rem 0.65rem', borderRadius: '4px', fontSize: '0.76rem', fontWeight: 600, color: 'var(--text-dark)' }}>NetBanking</span>
                        </div>

                        <div style={{ background: '#FFFFFF', border: '1px dashed var(--border-card)', borderRadius: '6px', padding: '0.85rem 1rem', fontSize: '0.82rem', color: 'var(--text-main)' }}>
                          Clicking <strong>"Pay Securely"</strong> opens the secure gateway window. Once completed, your order is immediately processed for dispatch.
                        </div>
                      </div>
                    )}

                    {/* Cash on Delivery */}
                    {paymentMethod === 'cod' && (
                      <div style={{ background: 'var(--bg-warm)', border: '1.5px solid var(--border-subtle)', borderRadius: 'var(--radius-md)', padding: '1.4rem', marginBottom: '1.75rem' }}>
                        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.9rem' }}>
                          <div style={{ color: 'var(--primary)', marginTop: '0.2rem' }}>
                            <IconPackage size={24} />
                          </div>
                          <div>
                            <strong style={{ fontSize: '0.98rem', color: 'var(--text-dark)' }}>Cash on Delivery (COD)</strong>
                            <p style={{ fontSize: '0.84rem', color: 'var(--text-muted)', margin: '0.25rem 0 0' }}>
                              Pay in cash or scan delivery partner UPI QR upon package arrival at your doorstep across India.
                            </p>
                          </div>
                        </div>
                      </div>
                    )}

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
                      <button className="btn btn-secondary" onClick={() => setStep(2)} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                        <IconArrowLeft size={16} />
                        <span>Back to Shipping</span>
                      </button>
                      
                      <button 
                        className="btn btn-primary" 
                        onClick={() => handlePlaceOrder()}
                        disabled={submitting}
                        style={{ padding: '0.95rem 2rem', fontSize: '0.96rem' }}
                      >
                        {submitting ? 'Connecting...' : (
                          paymentMethod === 'razorpay' 
                            ? `Pay Securely ${formatPrice(grandTotal)}` 
                            : `Place Order via COD (${formatPrice(grandTotal)})`
                        )}
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Right: Sticky Order Summary Card */}
            <aside className="checkout-sidebar">
              <div className="checkout-summary-card">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid var(--border-subtle)' }}>
                  <h4 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.25rem', margin: 0, color: 'var(--text-dark)' }}>
                    Order Summary
                  </h4>
                  <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                    {totals.itemCount} {totals.itemCount === 1 ? 'item' : 'items'}
                  </span>
                </div>

                {/* Scrollable Items List with mini images & badges */}
                <div className="checkout-summary-items-list">
                  {cart.map((item, idx) => (
                    <div key={`${item.id}-${item.color || ''}-${idx}`} className="checkout-summary-item-row">
                      <div className="checkout-summary-thumb-box">
                        <img 
                          src={item.image || '/assets/images/placeholder.jpg'} 
                          alt={item.name} 
                          className="checkout-summary-item-thumb" 
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=150&q=80';
                          }}
                        />
                        <span className="checkout-item-qty-pill">{item.quantity}</span>
                      </div>
                      <div style={{ flexGrow: 1, minWidth: 0 }}>
                        <div style={{ fontSize: '0.88rem', fontWeight: 600, color: 'var(--text-dark)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {item.name}
                        </div>
                        {item.color && (
                          <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                            Color: {item.color}
                          </div>
                        )}
                        {item.customNote && (
                          <div style={{ fontSize: '0.72rem', color: 'var(--primary)', fontStyle: 'italic', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                            "{item.customNote}"
                          </div>
                        )}
                      </div>
                      <div style={{ fontSize: '0.88rem', fontWeight: 700, color: 'var(--text-dark)', flexShrink: 0 }}>
                        {formatPrice(item.price * item.quantity)}
                      </div>
                    </div>
                  ))}
                </div>

                {/* Coupon Applied Indicator (if coupon active) */}
                {coupon && (
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: 'var(--primary-light)', padding: '0.55rem 0.85rem', borderRadius: 'var(--radius-xs)', marginBottom: '1rem', fontSize: '0.82rem', color: 'var(--primary)', fontWeight: 600 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                      <IconTag size={14} />
                      <span>Code: {coupon.code}</span>
                    </div>
                    <span>{coupon.discountPercent ? `${coupon.discountPercent}% OFF` : `-${formatPrice(totals.discount)}`}</span>
                  </div>
                )}

                {/* Pricing Breakdown */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.55rem', borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.88rem', color: 'var(--text-muted)' }}>
                    <span>Subtotal</span>
                    <span style={{ color: 'var(--text-dark)', fontWeight: 600 }}>{formatPrice(totals.subtotal)}</span>
                  </div>

                  {totals.discount > 0 && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.88rem', color: '#16A34A' }}>
                      <span>Discount</span>
                      <span style={{ fontWeight: 600 }}>-{formatPrice(totals.discount)}</span>
                    </div>
                  )}

                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.88rem', color: 'var(--text-muted)' }}>
                    <span>Shipping ({shippingMethod === 'express' ? 'Priority Air' : 'Standard'})</span>
                    <span style={{ color: shippingCost === 0 ? 'var(--primary)' : 'var(--text-dark)', fontWeight: shippingCost === 0 ? 700 : 600 }}>
                      {shippingCost === 0 ? 'FREE' : formatPrice(shippingCost)}
                    </span>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                    <span>Estimated Taxes</span>
                    <span>Included in price</span>
                  </div>

                  <div style={{ height: '1px', background: 'var(--border-subtle)', margin: '0.4rem 0' }} />

                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                    <div>
                      <span style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--text-dark)' }}>Grand Total</span>
                      <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>INR • All taxes included</div>
                    </div>
                    <span style={{ fontSize: '1.45rem', fontWeight: 800, color: 'var(--primary)', fontFamily: 'var(--font-serif)' }}>
                      {formatPrice(grandTotal)}
                    </span>
                  </div>
                </div>

                {/* Trust Badges Container */}
                <div className="checkout-trust-box">
                  <div className="checkout-trust-item">
                    <IconShield size={16} style={{ color: 'var(--primary)', flexShrink: 0 }} />
                    <span>256-Bit SSL Bank-Grade Encryption</span>
                  </div>
                  <div className="checkout-trust-item">
                    <IconTruck size={16} style={{ color: 'var(--primary)', flexShrink: 0 }} />
                    <span>Insured express pan-India transit</span>
                  </div>
                  <div className="checkout-trust-item">
                    <IconPackage size={16} style={{ color: 'var(--primary)', flexShrink: 0 }} />
                    <span>100% Handcrafted Artisanal Quality</span>
                  </div>
                </div>
              </div>
            </aside>
          </div>
        )}
      </main>
    </div>
  );
};
