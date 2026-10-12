import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../services/api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    // Purge legacy dummy user cache
    const legacy = localStorage.getItem('bgc_auth_user_inr_v1');
    if (legacy) {
      localStorage.removeItem('bgc_auth_user_inr_v1');
    }
    const saved = localStorage.getItem('bgc_auth_user_v2');
    return saved ? JSON.parse(saved) : null;
  });

  const [loading, setLoading] = useState(false);

  // Sync with MongoDB backend if a valid logged-in user exists
  useEffect(() => {
    if (user && user.email) {
      api.getMe(user.email)
        .then(res => {
          if (res.data) {
            const updated = { ...res.data, isLoggedIn: true };
            setUser(updated);
            localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
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
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(userData));
      setLoading(false);
      return { success: true, user: userData };
    } catch (err) {
      setLoading(false);
      return { success: false, message: err.message || 'Invalid email or password' };
    }
  };

  const register = async (userData) => {
    setLoading(true);
    try {
      const res = await api.register({ ...userData, country: 'India' });
      const created = { ...res.data, isLoggedIn: true };
      setUser(created);
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(created));
      setLoading(false);
      return { success: true, user: created };
    } catch (err) {
      setLoading(false);
      return { success: false, message: err.message || 'Registration failed' };
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('bgc_auth_user_v2');
    localStorage.removeItem('bgc_auth_user_inr_v1');
  };

  const forgotPassword = async (email, newPassword) => {
    setLoading(true);
    try {
      const res = await api.forgotPassword(email, newPassword);
      setLoading(false);
      return { success: true, message: res.message || 'Password reset successfully' };
    } catch (err) {
      setLoading(false);
      return { success: false, message: err.message || 'Password reset failed' };
    }
  };

  // Addresses CRUD (supports both logged-in users and guests)
  const addAddress = async (addressData) => {
    const formatted = { ...addressData, country: 'India' };

    // Guest checkout address handling
    if (!user || !user.email) {
      const newAddr = {
        _id: 'addr_' + Date.now(),
        ...formatted,
        isDefault: true
      };
      const guestUser = {
        ...(user || {}),
        isLoggedIn: false,
        name: addressData.recipientName || 'Customer',
        email: addressData.email || user?.email || '',
        phone: addressData.phone || '',
        savedAddresses: user?.savedAddresses ? [...user.savedAddresses, newAddr] : [newAddr]
      };
      setUser(guestUser);
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(guestUser));
      return newAddr;
    }

    try {
      const res = await api.addAddress(user.email, formatted);
      const updated = { ...user, savedAddresses: res.data };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
      return res.data[res.data.length - 1];
    } catch (err) {
      const newAddr = {
        _id: 'addr_' + Date.now(),
        ...formatted,
        isDefault: formatted.isDefault || (user.savedAddresses?.length || 0) === 0
      };
      let list = user.savedAddresses ? [...user.savedAddresses] : [];
      if (newAddr.isDefault) {
        list = list.map(a => ({ ...a, isDefault: false }));
      }
      list.push(newAddr);
      const updated = { ...user, savedAddresses: list };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
      return newAddr;
    }
  };

  const updateAddress = async (addressId, addressData) => {
    const formatted = { ...addressData, country: 'India' };
    if (!user?.savedAddresses) return;

    if (user.email && user.isLoggedIn) {
      try {
        const res = await api.updateAddress(user.email, addressId, formatted);
        const updated = { ...user, savedAddresses: res.data };
        setUser(updated);
        localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
        return;
      } catch (err) {}
    }

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
    localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
  };

  const deleteAddress = async (addressId) => {
    if (!user?.savedAddresses) return;

    if (user.email && user.isLoggedIn) {
      try {
        const res = await api.deleteAddress(user.email, addressId);
        const updated = { ...user, savedAddresses: res.data };
        setUser(updated);
        localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
        return;
      } catch (err) {}
    }

    const list = user.savedAddresses.filter(a => (a._id !== addressId && a.id !== addressId));
    if (list.length > 0 && !list.some(a => a.isDefault)) {
      list[0].isDefault = true;
    }
    const updated = { ...user, savedAddresses: list };
    setUser(updated);
    localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
  };

  const setDefaultAddress = async (addressId) => {
    if (!user?.savedAddresses) return;

    if (user.email && user.isLoggedIn) {
      try {
        const res = await api.setDefaultAddress(user.email, addressId);
        const updated = { ...user, savedAddresses: res.data };
        setUser(updated);
        localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
        return;
      } catch (err) {}
    }

    const list = user.savedAddresses.map(a => ({
      ...a,
      isDefault: (a._id === addressId || a.id === addressId)
    }));
    const updated = { ...user, savedAddresses: list };
    setUser(updated);
    localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
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
      forgotPassword,
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
