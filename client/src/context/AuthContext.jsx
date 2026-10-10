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

  // Addresses CRUD
  const addAddress = async (addressData) => {
    if (!user?.email) return;
    const formatted = { ...addressData, country: 'India' };
    try {
      const res = await api.addAddress(user.email, formatted);
      const updated = { ...user, savedAddresses: res.data };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
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
    }
  };

  const updateAddress = async (addressId, addressData) => {
    if (!user?.email || !user.savedAddresses) return;
    const formatted = { ...addressData, country: 'India' };
    try {
      const res = await api.updateAddress(user.email, addressId, formatted);
      const updated = { ...user, savedAddresses: res.data };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
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
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
    }
  };

  const deleteAddress = async (addressId) => {
    if (!user?.email || !user.savedAddresses) return;
    try {
      const res = await api.deleteAddress(user.email, addressId);
      const updated = { ...user, savedAddresses: res.data };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
    } catch (err) {
      const list = user.savedAddresses.filter(a => (a._id !== addressId && a.id !== addressId));
      if (list.length > 0 && !list.some(a => a.isDefault)) {
        list[0].isDefault = true;
      }
      const updated = { ...user, savedAddresses: list };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
    }
  };

  const setDefaultAddress = async (addressId) => {
    if (!user?.email || !user.savedAddresses) return;
    try {
      const res = await api.setDefaultAddress(user.email, addressId);
      const updated = { ...user, savedAddresses: res.data };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
    } catch (err) {
      const list = user.savedAddresses.map(a => ({
        ...a,
        isDefault: (a._id === addressId || a.id === addressId)
      }));
      const updated = { ...user, savedAddresses: list };
      setUser(updated);
      localStorage.setItem('bgc_auth_user_v2', JSON.stringify(updated));
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
