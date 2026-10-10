import React, { createContext, useContext, useState, useEffect } from 'react';

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
        customNote: 'Extra cute please! 🍓'
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

  const isInWishlist = (productId) => wishlist.includes(productId);

  // Coupon
  const applyCoupon = (code) => {
    const upper = code.trim().toUpperCase();
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
    return { success: false, message: 'Invalid coupon! Try BABYYGURLL or CUTIE100' };
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

    const discountedSubtotal = Math.max(0, subtotal - discount);
    // GST included or 0 estimated tax for consumer handmade goods
    const tax = 0;
    const total = discountedSubtotal + shipping + tax;

    return {
      subtotal,
      discount,
      shipping,
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
      currency: 'INR',
      formatPrice,
      addToCart,
      updateQuantity,
      removeFromCart,
      clearCart,
      toggleWishlist,
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
