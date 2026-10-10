import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../services/api';

const AuthContext = createContext();

const DEFAULT_USER = {
  isLoggedIn: true,
  name: 'Ananya Sharma',
  email: 'ananya@babyygurll.in',
  phone: '+91 98765 43210',
  memberTier: 'VIP Stitcher Club 🧶',
  savedAddresses: [
    {
      _id: 'addr_1',
      label: 'Home (Apartment)',
      recipientName: 'Ananya Sharma',
      phone: '+91 98765 43210',
      street: 'Flat 402, Blossom Heights, Linking Road, Bandra West',
      city: 'Mumbai',
      state: 'Maharashtra',
      zip: '400050',
      country: 'India',
      isDefault: true
    },
    {
      _id: 'addr_2',
      label: 'Design Studio',
      recipientName: 'Ananya Sharma',
      phone: '+91 98111 22334',
      street: 'Studio 12, Indiranagar 100ft Road',
      city: 'Bengaluru',
      state: 'Karnataka',
      zip: '560038',
      country: 'India',
      isDefault: false
    }
  ]
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('bgc_auth_user_inr_v1');
    return saved ? JSON.parse(saved) : DEFAULT_USER;
  });

  const [loading, setLoading] = useState(false);

  // Sync with MongoDB backend on mount
  useEffect(() => {
    if (user && user.email) {
      api.getMe(user.email)
        .then(res => {
          if (res.data) {
            const updated = { ...res.data, isLoggedIn: true };
            setUser(updated);
            localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(updated));
          }
        })
        .catch(() => {});
    }
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const res = await api.login(email, password);
      const userData = { ...res.data, isLoggedIn: true };
      setUser(userData);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(userData));
      setLoading(false);
      return { success: true, user: userData };
    } catch (err) {
      const fallback = {
        ...DEFAULT_USER,
        email,
        name: email.split('@')[0].replace(/[._]/g, ' ').replace(/\b\w/g, c => c.toUpperCase())
      };
      setUser(fallback);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(fallback));
      setLoading(false);
      return { success: true, user: fallback };
    }
  };

  const register = async (userData) => {
    setLoading(true);
    try {
      const res = await api.register({ ...userData, country: 'India' });
      const created = { ...res.data, isLoggedIn: true };
      setUser(created);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(created));
      setLoading(false);
      return { success: true, user: created };
    } catch (err) {
      const fallback = {
        isLoggedIn: true,
        name: userData.name,
        email: userData.email,
        phone: userData.phone || '+91 98765 00000',
        memberTier: 'VIP Stitcher Club 🧶',
        savedAddresses: [
          {
            _id: 'addr_' + Date.now(),
            label: 'Home',
            recipientName: userData.name,
            phone: userData.phone || '+91 98765 00000',
            street: '12 Garden Avenue',
            city: 'New Delhi',
            state: 'Delhi',
            zip: '110001',
            country: 'India',
            isDefault: true
          }
        ]
      };
      setUser(fallback);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(fallback));
      setLoading(false);
      return { success: true, user: fallback };
    }
  };

  const logout = () => {
    const loggedOut = { ...user, isLoggedIn: false };
    setUser(loggedOut);
    localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(loggedOut));
  };

  // Addresses CRUD
  const addAddress = async (addressData) => {
    const formatted = { ...addressData, country: 'India' };
    try {
      const res = await api.addAddress(user.email, formatted);
      const updated = { ...user, savedAddresses: res.data };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(updated));
    } catch (err) {
      const newAddr = {
        _id: 'addr_' + Date.now(),
        ...formatted,
        isDefault: formatted.isDefault || user.savedAddresses.length === 0
      };
      let list = [...user.savedAddresses];
      if (newAddr.isDefault) {
        list = list.map(a => ({ ...a, isDefault: false }));
      }
      list.push(newAddr);
      const updated = { ...user, savedAddresses: list };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(updated));
    }
  };

  const updateAddress = async (addressId, addressData) => {
    const formatted = { ...addressData, country: 'India' };
    try {
      const res = await api.updateAddress(user.email, addressId, formatted);
      const updated = { ...user, savedAddresses: res.data };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(updated));
    } catch (err) {
      let list = user.savedAddresses.map(a => {
        if (a._id === addressId || a.id === addressId) {
          return { ...a, ...formatted };
        }
        if (formatted.isDefault) {
          return { ...a, isDefault: false };
        }
        return a;
      });
      const updated = { ...user, savedAddresses: list };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(updated));
    }
  };

  const deleteAddress = async (addressId) => {
    try {
      const res = await api.deleteAddress(user.email, addressId);
      const updated = { ...user, savedAddresses: res.data };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(updated));
    } catch (err) {
      const list = user.savedAddresses.filter(a => (a._id !== addressId && a.id !== addressId));
      if (list.length > 0 && !list.some(a => a.isDefault)) {
        list[0].isDefault = true;
      }
      const updated = { ...user, savedAddresses: list };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(updated));
    }
  };

  const setDefaultAddress = async (addressId) => {
    try {
      const res = await api.setDefaultAddress(user.email, addressId);
      const updated = { ...user, savedAddresses: res.data };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(updated));
    } catch (err) {
      const list = user.savedAddresses.map(a => ({
        ...a,
        isDefault: (a._id === addressId || a.id === addressId)
      }));
      const updated = { ...user, savedAddresses: list };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_inr_v1', JSON.stringify(updated));
    }
  };

  const getDefaultAddress = () => {
    if (!user || !user.savedAddresses || user.savedAddresses.length === 0) return null;
    return user.savedAddresses.find(a => a.isDefault) || user.savedAddresses[0];
  };

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      login,
      register,
      logout,
      addAddress,
      updateAddress,
      deleteAddress,
      setDefaultAddress,
      getDefaultAddress
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
