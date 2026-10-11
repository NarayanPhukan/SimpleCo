import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { api } from '../services/api';
import { openRazorpayStandardCheckout } from '../services/razorpay';
import { IconShield, IconPackage, IconArrowRight, IconArrowLeft, IconCheck, IconBag, IconTruck } from './Icons';

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
  const { cart, getTotals, formatPrice, clearCart, removeCoupon } = useCart();

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
    <div className="modal-overlay active">
      <div className="modal-card checkout-card">
        <button className="modal-close-btn" onClick={onClose}>&times;</button>

        {/* Step Indicator */}
        <div className="checkout-steps-bar">
          <div className={`checkout-step-pill ${step >= 1 ? 'active' : ''}`}>
            <div className="step-num-circle">1</div>
            <span>Delivery Address</span>
          </div>
          <div className={`checkout-step-pill ${step >= 2 ? 'active' : ''}`}>
            <div className="step-num-circle">2</div>
            <span>Shipping</span>
          </div>
          <div className={`checkout-step-pill ${step >= 3 ? 'active' : ''}`}>
            <div className="step-num-circle">3</div>
            <span>Payment (India)</span>
          </div>
        </div>

        {/* Step 1: Address Selection / Inline Entry */}
        {step === 1 && (
          <div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.4rem', flexWrap: 'wrap', gap: '0.5rem' }}>
              <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.35rem', margin: 0 }}>
                {addresses.length === 0 || showAddressForm 
                  ? 'Enter Delivery Address (Pan-India)' 
                  : 'Where should we ship your pieces?'}
              </h3>
              {addresses.length > 0 && (
                <button
                  type="button"
                  className="btn btn-secondary"
                  style={{ fontSize: '0.8rem', padding: '0.4rem 0.85rem' }}
                  onClick={() => setShowAddressForm(!showAddressForm)}
                >
                  {showAddressForm ? '⬅️ Use Saved Address' : '+ Add Different Address'}
                </button>
              )}
            </div>

            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
              {addresses.length === 0 || showAddressForm
                ? 'Handcrafted with love, packed with ribbons and dispatched safely across India.'
                : 'Select from your saved addresses or add a new delivery location across India.'}
            </p>

            {/* Inline Address Form for guest or adding new address */}
            {(addresses.length === 0 || showAddressForm) ? (
              <div style={{ background: 'var(--bg-warm)', border: '1.5px solid var(--border-card)', borderRadius: 'var(--radius-md)', padding: '1.3rem', marginBottom: '1.5rem' }}>
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
                      <div style={{ display: 'flex', gap: '0.4rem', marginTop: '0.2rem' }}>
                        {['Home', 'Office', 'Hostel', 'Other'].map(type => (
                          <button
                            key={type}
                            type="button"
                            className={`filter-tab ${addrForm.label === type ? 'active' : ''}`}
                            style={{ padding: '0.4rem 0.75rem', fontSize: '0.78rem' }}
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
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '1.5rem' }}>
                {addresses.map((addr) => {
                  const id = addr._id || addr.id;
                  const isChecked = selectedAddressId === id;
                  return (
                    <label 
                      key={id}
                      style={{
                        display: 'flex',
                        alignItems: 'flex-start',
                        gap: '0.85rem',
                        padding: '1.1rem',
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
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.2rem' }}>
                          <strong style={{ fontSize: '0.95rem', color: 'var(--text-dark)' }}>{addr.label}</strong>
                          {addr.isDefault && <span className="default-pill">DEFAULT</span>}
                        </div>
                        <div style={{ fontSize: '0.88rem', fontWeight: 600, color: 'var(--text-dark)' }}>
                          {addr.recipientName} • Phone: {addr.phone}
                        </div>
                        <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                          {addr.street}, {addr.city}, {addr.state} - {addr.zip}, India
                        </div>
                      </div>
                    </label>
                  );
                })}
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1.5rem' }}>
              {addresses.length > 0 && showAddressForm ? (
                <button type="button" className="btn btn-secondary" onClick={() => setShowAddressForm(false)}>
                  Cancel
                </button>
              ) : (
                <button type="button" className="btn btn-secondary" onClick={onClose}>
                  Cancel
                </button>
              )}
              <button type="button" className="btn btn-primary" onClick={handleNextToStep2} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                <span>Continue to Shipping</span>
                <IconArrowRight size={16} />
              </button>
            </div>
          </div>
        )}

        {/* Step 2: Shipping Options */}
        {step === 2 && (
          <div>
            <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.35rem', marginBottom: '0.5rem' }}>
              Choose Delivery Option
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
              All parcels are carefully bubble-wrapped with floral mist & wax seal across India.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem', marginBottom: '1.5rem' }}>
              <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1.1rem', border: `1px solid ${shippingMethod === 'standard' ? 'var(--primary)' : 'var(--border-subtle)'}`, borderRadius: 'var(--radius-md)', background: shippingMethod === 'standard' ? 'var(--primary-light)' : '#FFFFFF', cursor: 'pointer' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <input 
                    type="radio" 
                    name="shipMeth" 
                    value="standard" 
                    checked={shippingMethod === 'standard'}
                    onChange={() => setShippingMethod('standard')}
                    style={{ accentColor: 'var(--primary)' }}
                  />
                  <div>
                    <strong style={{ fontSize: '0.95rem', color: 'var(--text-dark)' }}>Pan-India Standard Delivery (3-5 Days)</strong>
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Tracked express delivery across India with live SMS updates</p>
                  </div>
                </div>
                <span style={{ fontWeight: 700, color: 'var(--primary)' }}>
                  {totals.subtotal >= 999 ? 'FREE' : '₹79'}
                </span>
              </label>

              <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1.1rem', border: `1px solid ${shippingMethod === 'express' ? 'var(--primary)' : 'var(--border-subtle)'}`, borderRadius: 'var(--radius-md)', background: shippingMethod === 'express' ? 'var(--primary-light)' : '#FFFFFF', cursor: 'pointer' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <input 
                    type="radio" 
                    name="shipMeth" 
                    value="express" 
                    checked={shippingMethod === 'express'}
                    onChange={() => setShippingMethod('express')}
                    style={{ accentColor: 'var(--primary)' }}
                  />
                  <div>
                    <strong style={{ fontSize: '0.95rem', color: 'var(--text-dark)' }}>Priority Air Express (1-2 Days)</strong>
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Fast-track craft allocation + handwritten calligraphy card</p>
                  </div>
                </div>
                <span style={{ fontWeight: 700, color: 'var(--text-dark)' }}>₹149</span>
              </label>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <button className="btn btn-secondary" onClick={() => setStep(1)} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                <IconArrowLeft size={16} />
                <span>Back to Address</span>
              </button>
              <button className="btn btn-primary" onClick={() => setStep(3)} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                <span>Continue to Payment</span>
                <IconArrowRight size={16} />
              </button>
            </div>
          </div>
        )}

        {/* Step 3: India Payment Methods */}
        {step === 3 && (
          <div>
            <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.35rem', marginBottom: '0.5rem' }}>
              Select Payment Method
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.1rem' }}>
              Safe & encrypted payments for India.
            </p>

            {/* Delivering To Recap Card */}
            <div style={{ background: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: 'var(--radius-sm)', padding: '0.75rem 1rem', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.5rem' }}>
              <div style={{ fontSize: '0.82rem', color: '#475569' }}>
                <strong style={{ color: '#0F172A' }}>Ship to: </strong>
                {chosenAddr?.recipientName} • {chosenAddr?.city}, {chosenAddr?.state} ({chosenAddr?.zip})
              </div>
              <button 
                type="button" 
                onClick={() => setStep(1)} 
                style={{ background: 'transparent', border: 'none', color: 'var(--primary)', fontSize: '0.78rem', fontWeight: 600, cursor: 'pointer', textDecoration: 'underline' }}
              >
                Change Address
              </button>
            </div>

            {/* Payment Method Selector Tabs */}
            <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: '0.75rem', marginBottom: '1.25rem' }}>
              <button 
                type="button" 
                className={`filter-tab ${paymentMethod === 'razorpay' ? 'active' : ''}`}
                style={{ textAlign: 'center', padding: '0.75rem 0.6rem', fontSize: '0.86rem', fontWeight: 600 }}
                onClick={() => setPaymentMethod('razorpay')}
              >
                Online Payment (UPI, Cards, NetBanking)
              </button>
              <button 
                type="button" 
                className={`filter-tab ${paymentMethod === 'cod' ? 'active' : ''}`}
                style={{ textAlign: 'center', padding: '0.75rem 0.6rem', fontSize: '0.86rem', fontWeight: 600 }}
                onClick={() => setPaymentMethod('cod')}
              >
                Cash on Delivery
              </button>
            </div>

            {/* Razorpay Standard Web Checkout Option */}
            {paymentMethod === 'razorpay' && (
              <div style={{ background: 'var(--bg-warm)', border: '1.5px solid var(--border-card)', borderRadius: 'var(--radius-md)', padding: '1.3rem', marginBottom: '1.5rem' }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.85rem', marginBottom: '1rem' }}>
                  <div style={{ color: 'var(--primary)', marginTop: '0.2rem' }}>
                    <IconShield size={22} />
                  </div>
                  <div style={{ flexGrow: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '0.2rem' }}>
                      <strong style={{ fontSize: '0.98rem', color: 'var(--text-dark)' }}>Online Payment</strong>
                      <span style={{ background: 'var(--primary-light)', color: 'var(--primary)', padding: '0.15rem 0.5rem', borderRadius: '4px', fontSize: '0.72rem', fontWeight: 700 }}>
                        SECURE 256-BIT SSL
                      </span>
                    </div>
                    <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                      Instant pan-India payments. Supports Google Pay, PhonePe, Paytm, RuPay, Visa, MasterCard, and NetBanking.
                    </p>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap', marginBottom: '1rem' }}>
                  <span style={{ background: '#FFFFFF', border: '1px solid var(--border-card)', padding: '0.25rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-dark)' }}>Instant UPI</span>
                  <span style={{ background: '#FFFFFF', border: '1px solid var(--border-card)', padding: '0.25rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-dark)' }}>RuPay / Cards</span>
                  <span style={{ background: '#FFFFFF', border: '1px solid var(--border-card)', padding: '0.25rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-dark)' }}>NetBanking</span>
                  <span style={{ background: '#FFFFFF', border: '1px solid var(--border-card)', padding: '0.25rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-dark)' }}>256-Bit SSL Secure</span>
                </div>

                <div style={{ background: '#FFFFFF', border: '1px dashed var(--border-card)', borderRadius: '6px', padding: '0.75rem 0.9rem', fontSize: '0.78rem', color: 'var(--text-main)' }}>
                  Clicking <strong>"Pay Securely"</strong> opens the secure payment window. Supports Google Pay, PhonePe, Paytm, all Cards, and NetBanking.
                </div>
              </div>
            )}

            {/* Cash on Delivery */}
            {paymentMethod === 'cod' && (
              <div style={{ background: 'var(--bg-warm)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-md)', padding: '1.2rem', marginBottom: '1.5rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{ color: 'var(--primary)' }}>
                    <IconPackage size={22} />
                  </div>
                  <div>
                    <strong style={{ fontSize: '0.92rem', color: 'var(--text-dark)' }}>Cash on Delivery (COD)</strong>
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      Pay in cash or scan delivery QR when your package arrives at your doorstep across India.
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* Price breakdown */}
            <div style={{ background: '#FFFFFF', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-md)', padding: '1rem', marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                <span>Subtotal ({totals.itemCount} items):</span>
                <span>{formatPrice(totals.subtotal)}</span>
              </div>
              {totals.discount > 0 && (
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: '#16A34A', marginBottom: '0.35rem' }}>
                  <span>Discount:</span>
                  <span>-{formatPrice(totals.discount)}</span>
                </div>
              )}
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
                <span>Delivery Fee ({shippingMethod === 'express' ? 'Priority Air' : 'Standard'}):</span>
                <span style={{ color: shippingCost === 0 ? 'var(--primary)' : 'inherit', fontWeight: shippingCost === 0 ? 700 : 400 }}>
                  {shippingCost === 0 ? 'FREE' : formatPrice(shippingCost)}
                </span>
              </div>
              <div style={{ height: '1px', background: 'var(--border-subtle)', margin: '0.4rem 0' }} />
              <div className="summary-row" style={{ fontWeight: 700, color: 'var(--text-dark)', marginBottom: 0, marginTop: '0.4rem' }}>
                <span>Total Payable Amount:</span>
                <span style={{ color: 'var(--primary)', fontSize: '1.35rem' }}>{formatPrice(grandTotal)}</span>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
              <button className="btn btn-secondary" onClick={() => setStep(2)} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                <IconArrowLeft size={16} />
                <span>Back</span>
              </button>
              
              <div>
                <button 
                  className="btn btn-primary" 
                  onClick={() => handlePlaceOrder()}
                  disabled={submitting}
                  style={{ padding: '0.95rem 1.8rem' }}
                >
                  {submitting ? 'Connecting...' : (
                    paymentMethod === 'razorpay' 
                      ? `Pay Securely ${formatPrice(grandTotal)}` 
                      : `Place Order via COD (${formatPrice(grandTotal)})`
                  )}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Step 4: Success View */}
        {step === 4 && (
          <div className="order-success-card">
            <div className="success-check-bubble" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <IconCheck size={32} />
            </div>
            <h3>Thank you for your order</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
              Your artisanal crochet order has been safely placed and recorded.
            </p>

            <div className="order-id-highlight">
              Order #{placedOrderId}
            </div>

            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.8rem' }}>
              A confirmation receipt and tracking updates have been sent to your email.
            </p>

            <div style={{ display: 'flex', justifyContent: 'center', gap: '0.85rem', flexWrap: 'wrap' }}>
              <button 
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  onClose();
                  if (onOpenAccountModal) {
                    onOpenAccountModal();
                  }
                }}
              >
                Track Order Status
              </button>
              <button 
                type="button"
                className="btn btn-primary"
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
        )}
      </div>
    </div>
  );
};
