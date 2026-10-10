import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

const INDIAN_STATES = [
  "Andhra Pradesh", "Assam", "Bihar", "Chhattisgarh", "Delhi", "Goa", "Gujarat", 
  "Haryana", "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand", "Karnataka", 
  "Kerala", "Madhya Pradesh", "Maharashtra", "Odisha", "Punjab", "Rajasthan", 
  "Tamil Nadu", "Telangana", "Uttar Pradesh", "Uttarakhand", "West Bengal"
];

export const AddressModal = ({ isOpen, editAddress, onClose, onShowToast }) => {
  const { user, addAddress, updateAddress } = useAuth();
  const [formData, setFormData] = useState({
    label: 'Home',
    recipientName: '',
    phone: '',
    street: '',
    city: '',
    state: 'Maharashtra',
    zip: '',
    country: 'India',
    isDefault: false
  });

  useEffect(() => {
    if (editAddress) {
      setFormData({
        label: editAddress.label || 'Home',
        recipientName: editAddress.recipientName || '',
        phone: editAddress.phone || '',
        street: editAddress.street || '',
        city: editAddress.city || '',
        state: editAddress.state || 'Maharashtra',
        zip: editAddress.zip || '',
        country: 'India',
        isDefault: Boolean(editAddress.isDefault)
      });
    } else {
      setFormData({
        label: 'Home',
        recipientName: user?.name || '',
        phone: user?.phone || '+91 ',
        street: '',
        city: '',
        state: 'Maharashtra',
        zip: '',
        country: 'India',
        isDefault: user?.savedAddresses?.length === 0
      });
    }
  }, [editAddress, user]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (editAddress) {
      await updateAddress(editAddress._id || editAddress.id, formData);
      onShowToast('Address updated in MongoDB! 💾', '📍');
    } else {
      await addAddress(formData);
      onShowToast('New address saved to MongoDB! 🏡', '📍');
    }
    onClose();
  };

  return (
    <div className="modal-overlay active">
      <div className="modal-card" style={{ maxWidth: '540px', padding: '2.2rem' }}>
        <button className="modal-close-btn" onClick={onClose}>&times;</button>
        <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.35rem', marginBottom: '1.25rem' }}>
          {editAddress ? 'Edit Saved Address' : 'Add Delivery Address (India)'}
        </h3>

        <form onSubmit={handleSubmit} className="form-grid">
          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="addrLabel">Address Label</label>
              <input 
                type="text" 
                id="addrLabel" 
                className="form-input" 
                placeholder="e.g. Home, Hostel, Office" 
                required 
                value={formData.label}
                onChange={(e) => setFormData({ ...formData, label: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="addrFullName">Recipient Full Name</label>
              <input 
                type="text" 
                id="addrFullName" 
                className="form-input" 
                autoComplete="name" 
                required 
                placeholder="Recipient Full Name"
                value={formData.recipientName}
                onChange={(e) => setFormData({ ...formData, recipientName: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="addrPhone">Mobile Number (10 Digits)</label>
            <input 
              type="tel" 
              id="addrPhone" 
              className="form-input" 
              autoComplete="tel" 
              placeholder="+91 98765 43210" 
              required 
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="addrStreet">Flat, House No., Building, Street</label>
            <textarea 
              id="addrStreet" 
              className="form-textarea" 
              autoComplete="street-address" 
              rows="2" 
              placeholder="e.g. Flat 402, Blossom Heights, Linking Road" 
              required 
              value={formData.street}
              onChange={(e) => setFormData({ ...formData, street: e.target.value })}
            />
          </div>

          <div className="form-row-3">
            <div className="form-group">
              <label className="form-label" htmlFor="addrCity">City / Town</label>
              <input 
                type="text" 
                id="addrCity" 
                className="form-input" 
                autoComplete="address-level2" 
                required 
                placeholder="e.g. Mumbai"
                value={formData.city}
                onChange={(e) => setFormData({ ...formData, city: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="addrState">State</label>
              <select 
                id="addrState" 
                className="form-select"
                value={formData.state}
                onChange={(e) => setFormData({ ...formData, state: e.target.value })}
              >
                {INDIAN_STATES.map((st) => (
                  <option key={st} value={st}>{st}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="addrZip">PIN Code</label>
              <input 
                type="text" 
                id="addrZip" 
                className="form-input" 
                autoComplete="postal-code" 
                required 
                maxLength="6"
                placeholder="400050"
                value={formData.zip}
                onChange={(e) => setFormData({ ...formData, zip: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Country</label>
            <input 
              type="text" 
              className="form-input" 
              value="🇮🇳 India" 
              disabled 
              style={{ background: '#E5F4FF', color: 'var(--text-dark)', fontWeight: 600 }}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', margin: '0.5rem 0' }}>
            <input 
              type="checkbox" 
              id="addrDefault" 
              checked={formData.isDefault}
              onChange={(e) => setFormData({ ...formData, isDefault: e.target.checked })}
              style={{ width: '18px', height: '18px', accentColor: 'var(--primary)' }}
            />
            <label htmlFor="addrDefault" style={{ fontSize: '0.85rem', fontWeight: 600, cursor: 'pointer' }}>
              Set as default shipping address
            </label>
          </div>

          <button type="submit" className="btn btn-primary">Save Delivery Address 💾</button>
        </form>
      </div>
    </div>
  );
};
