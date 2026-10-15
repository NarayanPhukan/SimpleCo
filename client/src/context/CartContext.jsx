import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../services/api';

const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState(() => {
    const saved = localStorage.getItem('bgc_cart_inr_v1');
    return saved ? JSON.parse(saved) : [
      {
        id: 'bgc-cow-01',
        name: 'Strawberry Cow Amigurumi Plush',
        color: 'Strawberry Pink',
        quantity: 1,
        price: 1299,
        image: '/assets/images/strawberry-cow.jpg',
        customNote: 'Gift packaging requested'
      }
    ];
  });

  const [wishlist, setWishlist] = useState(() => {
    const saved = localStorage.getItem('bgc_wishlist_inr_v1');
    return saved ? JSON.parse(saved) : ['bgc-cow-01', 'bgc-tulip-02'];
  });

  const [coupon, setCoupon] = useState(() => {
    const saved = localStorage.getItem('bgc_coupon_inr_v1');
    return saved ? JSON.parse(saved) : null;
  });

  const [giftPackaging, setGiftPackaging] = useState(() => {
    const saved = localStorage.getItem('bgc_gift_pack_v1');
    return saved ? JSON.parse(saved) : {
      enabled: false,
      recipientName: '',
      message: '',
      hideInvoice: true,
      ribbonColor: 'Rose Gold',
      price: 99
    };
  });

  const [yarnPoints, setYarnPoints] = useState(() => {
    const saved = localStorage.getItem('bgc_yarn_points_v1');
    return saved !== null ? Number(saved) : 280;
  });

  const [recentlyViewed, setRecentlyViewed] = useState(() => {
    const saved = localStorage.getItem('bgc_recent_views_v1');
    return saved ? JSON.parse(saved) : [];
  });

  const [theme, setTheme] = useState(() => {
    const saved = localStorage.getItem('bgc_theme_v1');
    return saved || 'light';
  });

  useEffect(() => {
    localStorage.setItem('bgc_cart_inr_v1', JSON.stringify(cart));
  }, [cart]);

  useEffect(() => {
    localStorage.setItem('bgc_wishlist_inr_v1', JSON.stringify(wishlist));
  }, [wishlist]);

  useEffect(() => {
    if (coupon) {
      localStorage.setItem('bgc_coupon_inr_v1', JSON.stringify(coupon));
    } else {
      localStorage.removeItem('bgc_coupon_inr_v1');
    }
  }, [coupon]);

  useEffect(() => {
    localStorage.setItem('bgc_gift_pack_v1', JSON.stringify(giftPackaging));
  }, [giftPackaging]);

  useEffect(() => {
    localStorage.setItem('bgc_yarn_points_v1', String(yarnPoints));
  }, [yarnPoints]);

  useEffect(() => {
    localStorage.setItem('bgc_recent_views_v1', JSON.stringify(recentlyViewed));
  }, [recentlyViewed]);

  useEffect(() => {
    localStorage.setItem('bgc_theme_v1', theme);
    if (theme === 'candlelight') {
      document.documentElement.setAttribute('data-theme', 'candlelight');
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prev => prev === 'candlelight' ? 'light' : 'candlelight');
  };

  const updateGiftPackaging = (updates) => {
    setGiftPackaging(prev => ({ ...prev, ...updates }));
  };

  const addYarnPoints = (amount) => {
    setYarnPoints(prev => Math.max(0, prev + amount));
  };

  const redeemYarnPoints = (cost, rewardCoupon) => {
    if (yarnPoints < cost) return false;
    setYarnPoints(prev => prev - cost);
    if (rewardCoupon) {
      setCoupon(rewardCoupon);
    }
    return true;
  };

  const addRecentlyViewed = (product) => {
    if (!product || !product.id) return;
    setRecentlyViewed(prev => {
      const filtered = prev.filter(p => p.id !== product.id);
      return [
        {
          id: product.id,
          name: product.name,
          price: product.price,
          originalPrice: product.originalPrice,
          image: product.image,
          badge: product.badge,
          categoryName: product.categoryName
        },
        ...filtered
      ].slice(0, 8);
    });
  };

  const clearRecentlyViewed = () => {
    setRecentlyViewed([]);
  };

  // India INR Formatter
  const formatPrice = (amount) => {
    if (amount === undefined || amount === null) return '₹0';
    return `₹${Math.round(amount).toLocaleString('en-IN')}`;
  };

  // Cart operations
  const addToCart = (product, color, quantity = 1, customNote = '') => {
    setCart(prev => {
      const idx = prev.findIndex(item => item.id === product.id && item.color === color);
      if (idx > -1) {
        const next = [...prev];
        next[idx] = {
          ...next[idx],
          quantity: next[idx].quantity + quantity,
          customNote: customNote || next[idx].customNote
        };
        return next;
      }
      return [...prev, {
        id: product.id,
        name: product.name,
        color,
        quantity,
        price: product.price,
        image: product.image,
        customNote
      }];
    });
  };

  const updateQuantity = (index, newQty) => {
    if (newQty <= 0) {
      removeFromCart(index);
    } else {
      setCart(prev => {
        const next = [...prev];
        next[index] = { ...next[index], quantity: newQty };
        return next;
      });
    }
  };

  const removeFromCart = (index) => {
    setCart(prev => prev.filter((_, i) => i !== index));
  };

  const clearCart = () => {
    setCart([]);
  };

  // Wishlist operations
  const toggleWishlist = (productId) => {
    let added = false;
    setWishlist(prev => {
      if (prev.includes(productId)) {
        added = false;
        return prev.filter(id => id !== productId);
      }
      added = true;
      return [...prev, productId];
    });
    return added;
  };

  const removeFromWishlist = (productId) => {
    setWishlist(prev => prev.filter(id => id !== productId));
  };

  const clearWishlist = () => {
    setWishlist([]);
  };

  const moveToCart = (product, color) => {
    if (!product) return;
    const chosenColor = color || (product.colors && product.colors[0]?.name) || 'Default';
    addToCart(product, chosenColor, 1);
    removeFromWishlist(product.id || product._id);
  };

  const isInWishlist = (productId) => wishlist.includes(productId);

  // Coupon validation
  const applyCoupon = async (code) => {
    if (!code || !code.trim()) {
      return { success: false, message: 'Please enter a coupon code' };
    }
    const upper = code.trim().toUpperCase();
    const currentSubtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    try {
      const res = await api.validateCoupon(upper, currentSubtotal);
      if (res.success && res.data) {
        const c = {
          code: res.data.code,
          discountPercent: res.data.discountType === 'percentage' ? res.data.discountValue : null,
          discountFlat: res.data.discountType === 'flat' ? res.data.discountValue : null,
          freeShipping: res.data.freeShipping || res.data.discountType === 'free_shipping',
          label: res.data.description || `${res.data.code} Applied`
        };
        setCoupon(c);
        return { success: true, coupon: c };
      }
    } catch (err) {
      // Local fallback for default codes if offline
      if (upper === 'BABYYGURLL') {
        const c = { code: upper, discountPercent: 15, label: '15% Off Special Welcome' };
        setCoupon(c);
        return { success: true, coupon: c };
      } else if (upper === 'CUTIE100' || upper === 'CUTIE10') {
        const c = { code: upper, discountFlat: 100, label: '₹100 Off Order' };
        setCoupon(c);
        return { success: true, coupon: c };
      } else if (upper === 'FREESHIP') {
        const c = { code: upper, freeShipping: true, label: 'Free Delivery Across India' };
        setCoupon(c);
        return { success: true, coupon: c };
      }
      return { success: false, message: err.message || 'Invalid coupon! Try BABYYGURLL or CUTIE100' };
    }
    return { success: false, message: 'Invalid coupon code' };
  };

  const removeCoupon = () => {
    setCoupon(null);
  };

  // Calculations for India
  const getTotals = () => {
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const FREE_SHIPPING_THRESHOLD = 999;
    let discount = 0;
    let shipping = subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : (subtotal > 0 ? 79 : 0);

    if (coupon) {
      if (coupon.discountPercent) {
        discount = Math.round(subtotal * (coupon.discountPercent / 100));
      } else if (coupon.discountFlat) {
        discount = Math.min(subtotal, coupon.discountFlat);
      }
      if (coupon.freeShipping) {
        shipping = 0;
      }
    }

    const giftPackagingCost = (cart.length > 0 && giftPackaging.enabled) ? (giftPackaging.price || 99) : 0;
    const discountedSubtotal = Math.max(0, subtotal - discount);
    // GST included or 0 estimated tax for consumer handmade goods
    const tax = 0;
    const total = discountedSubtotal + shipping + giftPackagingCost + tax;

    return {
      subtotal,
      discount,
      shipping,
      giftPackagingCost,
      tax,
      total,
      freeShippingThreshold: FREE_SHIPPING_THRESHOLD,
      remainingForFreeShipping: Math.max(0, FREE_SHIPPING_THRESHOLD - subtotal),
      itemCount: cart.reduce((c, item) => c + item.quantity, 0)
    };
  };

  return (
    <CartContext.Provider value={{
      cart,
      wishlist,
      coupon,
      giftPackaging,
      updateGiftPackaging,
      yarnPoints,
      addYarnPoints,
      redeemYarnPoints,
      recentlyViewed,
      addRecentlyViewed,
      clearRecentlyViewed,
      theme,
      toggleTheme,
      currency: 'INR',
      formatPrice,
      addToCart,
      updateQuantity,
      removeFromCart,
      clearCart,
      toggleWishlist,
      removeFromWishlist,
      clearWishlist,
      moveToCart,
      isInWishlist,
      applyCoupon,
      removeCoupon,
      getTotals
    }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => useContext(CartContext);
