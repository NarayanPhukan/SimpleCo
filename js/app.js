// BabyyGurll Crochet - Main Application Controller
// Orchestrates Catalog, Cart, User Account & Addresses, Order Tracker, and Checkout

document.addEventListener("DOMContentLoaded", () => {
  // Application State
  const state = {
    activeCategory: "all",
    searchQuery: "",
    sortOrder: "featured",
    quickViewProduct: null,
    selectedColor: null,
    checkoutSelectedAddressId: null,
    checkoutShippingMethod: "standard",
    checkoutSelectedOrderId: null
  };

  // DOM Elements Cache
  const el = {
    // Navigation & Header
    cartCountBadge: document.getElementById("cartCountBadge"),
    accountBtn: document.getElementById("accountBtn"),
    accountBtnLabel: document.getElementById("accountBtnLabel"),
    cartTriggerBtn: document.getElementById("cartTriggerBtn"),
    currencySelector: document.getElementById("currencySelector"),
    
    // Catalog
    productsGrid: document.getElementById("productsGrid"),
    filterTabs: document.getElementById("categoryFilterTabs"),
    searchInput: document.getElementById("productSearchInput"),
    sortSelect: document.getElementById("catalogSortSelect"),

    // Cart Drawer
    cartDrawerBackdrop: document.getElementById("cartDrawerBackdrop"),
    cartDrawer: document.getElementById("cartDrawer"),
    closeCartBtn: document.getElementById("closeCartBtn"),
    cartDrawerCount: document.getElementById("cartDrawerCount"),
    cartItemsContainer: document.getElementById("cartItemsContainer"),
    cartFooterArea: document.getElementById("cartFooterArea"),
    shippingMeterLabel: document.getElementById("shippingMeterLabel"),
    shippingMeterPercent: document.getElementById("shippingMeterPercent"),
    shippingMeterFill: document.getElementById("shippingMeterFill"),
    couponInput: document.getElementById("couponInput"),
    applyCouponBtn: document.getElementById("applyCouponBtn"),
    activeCouponTagContainer: document.getElementById("activeCouponTagContainer"),
    summarySubtotal: document.getElementById("summarySubtotal"),
    summaryDiscountRow: document.getElementById("summaryDiscountRow"),
    summaryDiscount: document.getElementById("summaryDiscount"),
    summaryShipping: document.getElementById("summaryShipping"),
    summaryTax: document.getElementById("summaryTax"),
    summaryTotal: document.getElementById("summaryTotal"),
    proceedCheckoutBtn: document.getElementById("proceedCheckoutBtn"),

    // Quick View Modal
    quickViewModal: document.getElementById("quickViewModal"),
    closeQuickViewBtn: document.getElementById("closeQuickViewBtn"),
    qvImage: document.getElementById("qvImage"),
    qvBadge: document.getElementById("qvBadge"),
    qvTitle: document.getElementById("qvTitle"),
    qvPrice: document.getElementById("qvPrice"),
    qvOriginalPrice: document.getElementById("qvOriginalPrice"),
    qvDescription: document.getElementById("qvDescription"),
    qvSpecs: document.getElementById("qvSpecs"),
    qvColorOptions: document.getElementById("qvColorOptions"),
    qvCustomNote: document.getElementById("qvCustomNote"),
    qvAddToCartBtn: document.getElementById("qvAddToCartBtn"),

    // Account Modal
    accountModal: document.getElementById("accountModal"),
    closeAccountBtn: document.getElementById("closeAccountBtn"),
    tabProfileBtn: document.getElementById("tabProfileBtn"),
    tabAuthBtn: document.getElementById("tabAuthBtn"),
    profileViewPanel: document.getElementById("profileViewPanel"),
    authViewPanel: document.getElementById("authViewPanel"),
    profileInitials: document.getElementById("profileInitials"),
    profileNameDisplay: document.getElementById("profileNameDisplay"),
    profileEmailDisplay: document.getElementById("profileEmailDisplay"),
    profilePhoneDisplay: document.getElementById("profilePhoneDisplay"),
    profileTierDisplay: document.getElementById("profileTierDisplay"),
    savedAddressesList: document.getElementById("savedAddressesList"),
    btnOpenAddAddress: document.getElementById("btnOpenAddAddress"),
    accountOrderHistoryList: document.getElementById("accountOrderHistoryList"),
    logoutBtn: document.getElementById("logoutBtn"),
    authModeLoginBtn: document.getElementById("authModeLoginBtn"),
    authModeRegisterBtn: document.getElementById("authModeRegisterBtn"),
    loginForm: document.getElementById("loginForm"),
    registerForm: document.getElementById("registerForm"),

    // Address Modal Form
    addressModal: document.getElementById("addressModal"),
    closeAddressModalBtn: document.getElementById("closeAddressModalBtn"),
    addressModalTitle: document.getElementById("addressModalTitle"),
    addressForm: document.getElementById("addressForm"),
    addrFormEditId: document.getElementById("addrFormEditId"),
    addrLabel: document.getElementById("addrLabel"),
    addrFullName: document.getElementById("addrFullName"),
    addrPhone: document.getElementById("addrPhone"),
    addrStreet: document.getElementById("addrStreet"),
    addrCity: document.getElementById("addrCity"),
    addrState: document.getElementById("addrState"),
    addrZip: document.getElementById("addrZip"),
    addrCountry: document.getElementById("addrCountry"),
    addrIsDefault: document.getElementById("addrIsDefault"),

    // Order Tracking Section
    orderTrackInput: document.getElementById("orderTrackInput"),
    trackOrderSubmitBtn: document.getElementById("trackOrderSubmitBtn"),
    orderTrackingResult: document.getElementById("orderTrackingResult"),
    trackerOrderIdDisplay: document.getElementById("trackerOrderIdDisplay"),
    trackerOrderDateDisplay: document.getElementById("trackerOrderDateDisplay"),
    trackerDeliveryDateDisplay: document.getElementById("trackerDeliveryDateDisplay"),
    trackerCarrierDisplay: document.getElementById("trackerCarrierDisplay"),
    trackerWaybillDisplay: document.getElementById("trackerWaybillDisplay"),
    trackerStatusPill: document.getElementById("trackerStatusPill"),
    trackingTimelineContainer: document.getElementById("trackingTimelineContainer"),
    trackerItemsList: document.getElementById("trackerItemsList"),
    trackerRecipientDisplay: document.getElementById("trackerRecipientDisplay"),
    trackerAddressDisplay: document.getElementById("trackerAddressDisplay"),
    trackerPhoneDisplay: document.getElementById("trackerPhoneDisplay"),
    trackerPaymentDisplay: document.getElementById("trackerPaymentDisplay"),
    btnAdvanceStage: document.getElementById("btnAdvanceStage"),

    // Checkout Modal
    checkoutModal: document.getElementById("checkoutModal"),
    closeCheckoutBtn: document.getElementById("closeCheckoutBtn"),
    pillStep1: document.getElementById("pillStep1"),
    pillStep2: document.getElementById("pillStep2"),
    pillStep3: document.getElementById("pillStep3"),
    checkoutStep1: document.getElementById("checkoutStep1"),
    checkoutStep2: document.getElementById("checkoutStep2"),
    checkoutStep3: document.getElementById("checkoutStep3"),
    checkoutStepSuccess: document.getElementById("checkoutStepSuccess"),
    checkoutSavedAddressesList: document.getElementById("checkoutSavedAddressesList"),
    checkoutAddAddressInlineBtn: document.getElementById("checkoutAddAddressInlineBtn"),
    btnNextToStep2: document.getElementById("btnNextToStep2"),
    btnBackToStep1: document.getElementById("btnBackToStep1"),
    btnNextToStep3: document.getElementById("btnNextToStep3"),
    btnBackToStep2: document.getElementById("btnBackToStep2"),
    btnPlaceOrderFinal: document.getElementById("btnPlaceOrderFinal"),
    checkoutFinalTotalDisplay: document.getElementById("checkoutFinalTotalDisplay"),
    cardHolderPreview: document.getElementById("cardHolderPreview"),
    newPlacedOrderId: document.getElementById("newPlacedOrderId"),
    btnTrackNewlyPlacedOrder: document.getElementById("btnTrackNewlyPlacedOrder"),
    btnContinueShoppingAfterSuccess: document.getElementById("btnContinueShoppingAfterSuccess"),

    // Toast & Utilities
    toastContainer: document.getElementById("toastContainer"),
    confettiCanvas: document.getElementById("confettiCanvas"),
    footerSavedAddressesLink: document.getElementById("footerSavedAddressesLink"),
    footerAccountLink: document.getElementById("footerAccountLink"),
    navOrderTracker: document.getElementById("navOrderTracker")
  };

  // ==========================================================================
  // Helper: Toast Notifications
  // ==========================================================================
  function showToast(message, icon = "🌸") {
    const toast = document.createElement("div");
    toast.className = "toast";
    toast.innerHTML = `<span class="toast-icon">${icon}</span><span>${message}</span>`;
    el.toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.classList.add("hiding");
      setTimeout(() => { toast.remove(); }, 350);
    }, 3200);
  }

  // ==========================================================================
  // 1. Catalog Filtering, Searching, & Rendering
  // ==========================================================================
  function getFilteredProducts() {
    let list = PRODUCTS_DATA.slice();

    // Category filter
    if (state.activeCategory !== "all") {
      list = list.filter(p => p.category === state.activeCategory);
    }

    // Search query filter
    if (state.searchQuery.trim() !== "") {
      const q = state.searchQuery.toLowerCase().trim();
      list = list.filter(p => 
        p.name.toLowerCase().includes(q) ||
        p.description.toLowerCase().includes(q) ||
        p.categoryName.toLowerCase().includes(q)
      );
    }

    // Sort order
    if (state.sortOrder === "price-asc") {
      list.sort((a, b) => a.price - b.price);
    } else if (state.sortOrder === "price-desc") {
      list.sort((a, b) => b.price - a.price);
    } else if (state.sortOrder === "rating") {
      list.sort((a, b) => b.rating - a.rating);
    }

    return list;
  }

  function renderCatalog() {
    const products = getFilteredProducts();
    if (!products.length) {
      el.productsGrid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 4rem 1rem;">
          <div style="font-size: 3rem; margin-bottom: 1rem;">🧶</div>
          <h3 style="font-family: var(--font-serif); font-size: 1.4rem; color: var(--text-dark);">No crochet pieces match your search</h3>
          <p style="color: var(--text-muted); margin-top: 0.5rem;">Try searching for another keyword like "cow", "tulip", "bag" or view all categories.</p>
        </div>
      `;
      return;
    }

    el.productsGrid.innerHTML = products.map(product => {
      const isWishlisted = store.isInWishlist(product.id);
      return `
        <article class="product-card" data-id="${product.id}">
          <div class="card-media">
            <span class="card-badge">${product.badge}</span>
            <button class="wishlist-btn ${isWishlisted ? 'active' : ''}" 
                    aria-label="Save to Wishlist" 
                    data-action="wishlist" 
                    data-id="${product.id}">
              ${isWishlisted ? '❤️' : '🤍'}
            </button>
            <img src="${product.image}" alt="${product.name}" loading="lazy" width="400" height="400">
            <button class="quick-view-overlay-btn" data-action="quick-view" data-id="${product.id}">
              Quick View 👁️
            </button>
          </div>
          <div class="card-body">
            <span class="card-category">${product.categoryName}</span>
            <h3 class="card-title">${product.name}</h3>
            <div class="card-rating">
              <span>★</span>
              <span>${product.rating.toFixed(1)}</span>
              <span class="rating-count">(${product.reviewsCount} reviews)</span>
            </div>
            <div class="card-swatches">
              ${product.colors.map(c => `
                <span class="swatch-circle" style="background-color: ${c.hex};" title="${c.name}"></span>
              `).join('')}
              <span class="swatch-more">+${product.colors.length} colors</span>
            </div>
            <div class="card-footer">
              <div class="price-wrap">
                <span class="current-price">${store.formatPrice(product.price)}</span>
                ${product.originalPrice ? `<span class="original-price">${store.formatPrice(product.originalPrice)}</span>` : ''}
              </div>
              <button class="card-add-btn" 
                      aria-label="Add ${product.name} to cart" 
                      data-action="quick-add" 
                      data-id="${product.id}">
                +
              </button>
            </div>
          </div>
        </article>
      `;
    }).join('');
  }

  // Catalog Event Listeners
  if (el.filterTabs) {
    el.filterTabs.addEventListener("click", (e) => {
      const tab = e.target.closest(".filter-tab");
      if (!tab) return;
      el.filterTabs.querySelectorAll(".filter-tab").forEach(t => t.classList.remove("active"));
      tab.classList.add("active");
      state.activeCategory = tab.dataset.category;
      renderCatalog();
    });
  }

  // Nav filter links
  document.querySelectorAll("[data-filter]").forEach(link => {
    link.addEventListener("click", (e) => {
      e.preventDefault();
      const cat = link.dataset.filter;
      state.activeCategory = cat;
      const targetTab = el.filterTabs.querySelector(`[data-category="${cat}"]`);
      if (targetTab) {
        el.filterTabs.querySelectorAll(".filter-tab").forEach(t => t.classList.remove("active"));
        targetTab.classList.add("active");
      }
      renderCatalog();
      document.getElementById("catalog").scrollIntoView({ behavior: "smooth" });
    });
  });

  if (el.searchInput) {
    el.searchInput.addEventListener("input", (e) => {
      state.searchQuery = e.target.value;
      renderCatalog();
    });
  }

  if (el.sortSelect) {
    el.sortSelect.addEventListener("change", (e) => {
      state.sortOrder = e.target.value;
      renderCatalog();
    });
  }

  // Delegated clicks on Product Grid (Quick Add, Wishlist, Quick View)
  if (el.productsGrid) {
    el.productsGrid.addEventListener("click", (e) => {
      const quickAddBtn = e.target.closest('[data-action="quick-add"]');
      if (quickAddBtn) {
        const prodId = quickAddBtn.dataset.id;
        const prod = PRODUCTS_DATA.find(p => p.id === prodId);
        if (prod) {
          const defaultColor = prod.colors[0].name;
          store.addToCart(prod.id, defaultColor, 1);
          updateCartUI();
          showToast(`Added ${prod.name} (${defaultColor}) to bag! 🛍️`, "🍓");
          bumpCartBadge();
        }
        return;
      }

      const wishlistBtn = e.target.closest('[data-action="wishlist"]');
      if (wishlistBtn) {
        const prodId = wishlistBtn.dataset.id;
        const prod = PRODUCTS_DATA.find(p => p.id === prodId);
        const added = store.toggleWishlist(prodId);
        wishlistBtn.classList.toggle("active", added);
        wishlistBtn.innerHTML = added ? "❤️" : "🤍";
        showToast(added ? `Saved ${prod.name} to Wishlist! 💕` : `Removed from Wishlist`, added ? "💖" : "🤍");
        return;
      }

      const quickViewBtn = e.target.closest('[data-action="quick-view"]');
      if (quickViewBtn) {
        const prodId = quickViewBtn.dataset.id;
        openQuickView(prodId);
        return;
      }
    });
  }

  // ==========================================================================
  // 2. Quick View Modal
  // ==========================================================================
  function openQuickView(productId) {
    const prod = PRODUCTS_DATA.find(p => p.id === productId);
    if (!prod) return;

    state.quickViewProduct = prod;
    state.selectedColor = prod.colors[0].name;

    el.qvImage.src = prod.image;
    el.qvImage.alt = prod.name;
    el.qvBadge.textContent = prod.badge;
    el.qvTitle.textContent = prod.name;
    el.qvPrice.textContent = store.formatPrice(prod.price);
    el.qvOriginalPrice.textContent = prod.originalPrice ? store.formatPrice(prod.originalPrice) : "";
    el.qvDescription.textContent = prod.description;

    el.qvSpecs.innerHTML = `
      <strong>Artisan Specifications:</strong><br>
      • Yarn: ${prod.specs.yarn}<br>
      • Dimensions: ${prod.specs.height || prod.specs.dimensions || 'Handmade scale'}<br>
      • Lead Time: ${prod.leadTime}<br>
      • Care: ${prod.specs.care || 'Hand wash cold'}
    `;

    el.qvColorOptions.innerHTML = prod.colors.map((c, idx) => `
      <button class="swatch-btn ${idx === 0 ? 'selected' : ''}" data-color="${c.name}">
        <span class="swatch-circle" style="background-color: ${c.hex};"></span>
        <span>${c.name}</span>
      </button>
    `).join('');

    el.qvCustomNote.value = "";
    el.quickViewModal.classList.add("active");
  }

  el.qvColorOptions.addEventListener("click", (e) => {
    const btn = e.target.closest(".swatch-btn");
    if (!btn) return;
    el.qvColorOptions.querySelectorAll(".swatch-btn").forEach(b => b.classList.remove("selected"));
    btn.classList.add("selected");
    state.selectedColor = btn.dataset.color;
  });

  el.qvAddToCartBtn.addEventListener("click", () => {
    if (!state.quickViewProduct) return;
    const note = el.qvCustomNote.value.trim();
    store.addToCart(state.quickViewProduct.id, state.selectedColor, 1, note);
    updateCartUI();
    showToast(`Added ${state.quickViewProduct.name} (${state.selectedColor}) to bag! 🛍️`, "🌸");
    bumpCartBadge();
    el.quickViewModal.classList.remove("active");
    openCartDrawer();
  });

  el.closeQuickViewBtn.addEventListener("click", () => {
    el.quickViewModal.classList.remove("active");
  });

  // ==========================================================================
  // 3. Cart Drawer & Calculations
  // ==========================================================================
  function bumpCartBadge() {
    el.cartCountBadge.style.transform = "scale(1.35)";
    setTimeout(() => {
      el.cartCountBadge.style.transform = "scale(1)";
    }, 250);
  }

  function updateCartUI() {
    const cart = store.getCart();
    const count = store.getCartItemCount();
    const totals = store.getCartTotals();

    el.cartCountBadge.textContent = count;
    el.cartDrawerCount.textContent = count;

    // Free Shipping Progress Meter
    const threshold = totals.freeShippingThreshold;
    const subtotal = totals.subtotal;
    const pct = Math.min(100, Math.round((subtotal / threshold) * 100));

    el.shippingMeterFill.style.width = `${pct}%`;
    el.shippingMeterPercent.textContent = `${pct}%`;

    if (totals.remainingForFreeShipping <= 0) {
      el.shippingMeterLabel.innerHTML = `🎉 You've unlocked <strong>Free Standard Shipping!</strong>`;
    } else {
      el.shippingMeterLabel.innerHTML = `Add <strong>${store.formatPrice(totals.remainingForFreeShipping)}</strong> more for Free Shipping`;
    }

    // Items list
    if (cart.length === 0) {
      el.cartItemsContainer.innerHTML = `
        <div class="cart-empty-state">
          <span class="cart-empty-icon">🧺</span>
          <h4>Your basket is feeling empty</h4>
          <p>Explore our soft pastel plushies, bouquets, and heirloom totes.</p>
          <button class="btn btn-primary" id="btnEmptyShopNow">Start Browsing 🍓</button>
        </div>
      `;
      el.cartFooterArea.style.display = "none";
      const shopBtn = document.getElementById("btnEmptyShopNow");
      if (shopBtn) {
        shopBtn.addEventListener("click", () => {
          closeCartDrawer();
          document.getElementById("catalog").scrollIntoView({ behavior: "smooth" });
        });
      }
      return;
    }

    el.cartFooterArea.style.display = "block";

    el.cartItemsContainer.innerHTML = cart.map((item, index) => {
      const prod = PRODUCTS_DATA.find(p => p.id === item.productId);
      if (!prod) return "";
      const itemSubtotal = prod.price * item.quantity;

      return `
        <div class="cart-item-card" data-index="${index}">
          <img src="${prod.image}" alt="${prod.name}" class="cart-item-thumb">
          <div class="cart-item-details">
            <h4 class="cart-item-name">${prod.name}</h4>
            <div class="cart-item-variant">Color: <strong>${item.color}</strong></div>
            ${item.customNote ? `<div style="font-size: 0.74rem; color: var(--secondary); margin-bottom: 0.35rem;">Note: "${item.customNote}"</div>` : ''}
            <div class="cart-item-bottom">
              <div class="qty-stepper">
                <button class="qty-btn" data-action="qty-minus" data-index="${index}">-</button>
                <span class="qty-value">${item.quantity}</span>
                <button class="qty-btn" data-action="qty-plus" data-index="${index}">+</button>
              </div>
              <span class="cart-item-price">${store.formatPrice(itemSubtotal)}</span>
              <button class="cart-remove-btn" data-action="remove" data-index="${index}" title="Remove item">✕</button>
            </div>
          </div>
        </div>
      `;
    }).join('');

    // Active Coupon display
    if (store.coupon) {
      el.activeCouponTagContainer.innerHTML = `
        <div class="active-coupon-tag">
          <span>🏷️ Coupon Applied: <strong>${store.coupon.code}</strong> (${store.coupon.label})</span>
          <span class="remove-coupon-btn" id="btnRemoveCoupon" title="Remove coupon">&times;</span>
        </div>
      `;
      const btnRemove = document.getElementById("btnRemoveCoupon");
      if (btnRemove) {
        btnRemove.addEventListener("click", () => {
          store.removeCoupon();
          updateCartUI();
          showToast("Coupon removed", "🏷️");
        });
      }
    } else {
      el.activeCouponTagContainer.innerHTML = "";
    }

    // Totals Summary
    el.summarySubtotal.textContent = store.formatPrice(totals.subtotal);
    if (totals.discount > 0) {
      el.summaryDiscountRow.style.display = "flex";
      el.summaryDiscount.textContent = `-${store.formatPrice(totals.discount)}`;
    } else {
      el.summaryDiscountRow.style.display = "none";
    }

    el.summaryShipping.textContent = totals.shipping === 0 ? "FREE" : store.formatPrice(totals.shipping);
    el.summaryTax.textContent = store.formatPrice(totals.tax);
    el.summaryTotal.textContent = store.formatPrice(totals.total);
  }

  // Delegated events in cart items container (steppers & remove)
  el.cartItemsContainer.addEventListener("click", (e) => {
    const minusBtn = e.target.closest('[data-action="qty-minus"]');
    if (minusBtn) {
      const idx = parseInt(minusBtn.dataset.index, 10);
      const item = store.getCart()[idx];
      if (item) {
        store.updateQuantity(idx, item.quantity - 1);
        updateCartUI();
      }
      return;
    }

    const plusBtn = e.target.closest('[data-action="qty-plus"]');
    if (plusBtn) {
      const idx = parseInt(plusBtn.dataset.index, 10);
      const item = store.getCart()[idx];
      if (item) {
        store.updateQuantity(idx, item.quantity + 1);
        updateCartUI();
      }
      return;
    }

    const removeBtn = e.target.closest('[data-action="remove"]');
    if (removeBtn) {
      const idx = parseInt(removeBtn.dataset.index, 10);
      store.removeFromCart(idx);
      updateCartUI();
      showToast("Item removed from basket", "🧺");
      return;
    }
  });

  // Coupon application
  el.applyCouponBtn.addEventListener("click", () => {
    const code = el.couponInput.value.trim();
    if (!code) return;
    const res = store.applyCoupon(code);
    if (res.success) {
      showToast(`Promo "${res.coupon.code}" applied! 🎉`, "🏷️");
      el.couponInput.value = "";
      updateCartUI();
    } else {
      showToast(res.message, "⚠️");
    }
  });

  function openCartDrawer() {
    updateCartUI();
    el.cartDrawerBackdrop.classList.add("active");
  }

  function closeCartDrawer() {
    el.cartDrawerBackdrop.classList.remove("active");
  }

  el.cartTriggerBtn.addEventListener("click", openCartDrawer);
  el.closeCartBtn.addEventListener("click", closeCartDrawer);
  el.cartDrawerBackdrop.addEventListener("click", (e) => {
    if (e.target === el.cartDrawerBackdrop) closeCartDrawer();
  });

  // ==========================================================================
  // 4. Currency Switcher
  // ==========================================================================
  el.currencySelector.value = store.currency;
  el.currencySelector.addEventListener("change", (e) => {
    store.setCurrency(e.target.value);
    renderCatalog();
    updateCartUI();
    renderTrackingOrder(state.checkoutSelectedOrderId || "BGC-89241");
    showToast(`Currency switched to ${e.target.value}`, "💱");
  });

  // ==========================================================================
  // 5. User Account & Saved Addresses Manager
  // ==========================================================================
  function updateAccountHeaderButton() {
    const user = store.getUser();
    if (user && user.isLoggedIn) {
      el.accountBtnLabel.textContent = user.name.split(" ")[0];
    } else {
      el.accountBtnLabel.textContent = "Sign In";
    }
  }

  function renderAccountProfile() {
    const user = store.getUser();
    if (!user || !user.isLoggedIn) {
      // Switch to auth tab if not logged in
      el.tabProfileBtn.classList.remove("active");
      el.tabAuthBtn.classList.add("active");
      el.profileViewPanel.style.display = "none";
      el.authViewPanel.style.display = "block";
      return;
    }

    el.tabProfileBtn.classList.add("active");
    el.tabAuthBtn.classList.remove("active");
    el.profileViewPanel.style.display = "block";
    el.authViewPanel.style.display = "none";

    // Set Header
    const initials = user.name.split(" ").map(w => w[0]).join("").toUpperCase().slice(0, 2);
    el.profileInitials.textContent = initials;
    el.profileNameDisplay.textContent = user.name;
    el.profileEmailDisplay.textContent = user.email;
    el.profilePhoneDisplay.textContent = `Mobile: ${user.phone || 'Not added'}`;
    el.profileTierDisplay.textContent = user.memberTier || "VIP Stitcher Club 🧶";

    // Saved Addresses
    const addresses = store.getAddresses();
    if (addresses.length === 0) {
      el.savedAddressesList.innerHTML = `
        <div style="grid-column: 1 / -1; padding: 1.5rem; text-align: center; color: var(--text-muted); background: #FAF7F9; border-radius: var(--radius-md);">
          No saved addresses found. Click "+ Add New Address" above to save shipping info!
        </div>
      `;
    } else {
      el.savedAddressesList.innerHTML = addresses.map(addr => `
        <div class="address-card ${addr.isDefault ? 'is-default' : ''}">
          <div>
            <div class="address-card-top">
              <span class="addr-label-badge">${addr.label || 'Home'}</span>
              ${addr.isDefault ? '<span class="default-pill">DEFAULT</span>' : ''}
            </div>
            <div class="addr-recipient">${addr.recipientName}</div>
            <div class="addr-street">${addr.street}<br>${addr.city}, ${addr.state} ${addr.zip}, ${addr.country}</div>
            <div class="addr-phone">📞 ${addr.phone}</div>
          </div>
          <div class="addr-actions">
            ${!addr.isDefault ? `<button class="addr-action-btn" data-action="set-default" data-id="${addr.id}">Make Default</button>` : ''}
            <button class="addr-action-btn" data-action="edit-addr" data-id="${addr.id}">Edit</button>
            <button class="addr-action-btn delete" data-action="delete-addr" data-id="${addr.id}">Delete</button>
          </div>
        </div>
      `).join('');
    }

    // Orders History in Account
    const orders = store.getOrders();
    if (orders.length === 0) {
      el.accountOrderHistoryList.innerHTML = `
        <p style="font-size: 0.85rem; color: var(--text-muted); padding: 0.5rem 0;">No past orders yet.</p>
      `;
    } else {
      el.accountOrderHistoryList.innerHTML = orders.map(ord => `
        <div style="display: flex; align-items: center; justify-content: space-between; background: #FAF7F9; border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); padding: 0.85rem 1rem;">
          <div>
            <strong style="font-size: 0.92rem; color: var(--text-dark);">${ord.orderId}</strong>
            <span style="font-size: 0.78rem; color: var(--text-muted); margin-left: 0.5rem;">${ord.dateFormatted}</span>
            <div style="font-size: 0.78rem; color: var(--secondary); margin-top: 0.2rem;">${ord.items.length} item(s) • ${store.formatPrice(ord.total)}</div>
          </div>
          <button class="btn btn-secondary" style="padding: 0.35rem 0.85rem; font-size: 0.78rem;" data-action="account-track-order" data-id="${ord.orderId}">
            Track Live 🚚
          </button>
        </div>
      `).join('');
    }
  }

  // Account Modal Open / Close
  function openAccountModal() {
    renderAccountProfile();
    updateAccountHeaderButton();
    el.accountModal.classList.add("active");
  }

  el.accountBtn.addEventListener("click", openAccountModal);
  el.closeAccountBtn.addEventListener("click", () => el.accountModal.classList.remove("active"));
  if (el.footerAccountLink) el.footerAccountLink.addEventListener("click", (e) => { e.preventDefault(); openAccountModal(); });
  if (el.footerSavedAddressesLink) el.footerSavedAddressesLink.addEventListener("click", (e) => { e.preventDefault(); openAccountModal(); });

  // Account Tabs
  el.tabProfileBtn.addEventListener("click", () => {
    el.tabProfileBtn.classList.add("active");
    el.tabAuthBtn.classList.remove("active");
    el.profileViewPanel.style.display = "block";
    el.authViewPanel.style.display = "none";
  });

  el.tabAuthBtn.addEventListener("click", () => {
    el.tabAuthBtn.classList.add("active");
    el.tabProfileBtn.classList.remove("active");
    el.profileViewPanel.style.display = "none";
    el.authViewPanel.style.display = "block";
  });

  // Auth Mode: Sign In vs Register
  el.authModeLoginBtn.addEventListener("click", () => {
    el.authModeLoginBtn.classList.add("active");
    el.authModeRegisterBtn.classList.remove("active");
    el.loginForm.style.display = "flex";
    el.registerForm.style.display = "none";
  });

  el.authModeRegisterBtn.addEventListener("click", () => {
    el.authModeRegisterBtn.classList.add("active");
    el.authModeLoginBtn.classList.remove("active");
    el.registerForm.style.display = "flex";
    el.loginForm.style.display = "none";
  });

  // Login Form Submission
  el.loginForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const email = document.getElementById("loginEmail").value.trim();
    const pass = document.getElementById("loginPassword").value.trim();
    store.login(email, pass);
    updateAccountHeaderButton();
    renderAccountProfile();
    showToast(`Welcome back, ${store.getUser().name}! 🌸`, "👤");
  });

  // Register Form Submission
  el.registerForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const name = document.getElementById("regName").value.trim();
    const email = document.getElementById("regEmail").value.trim();
    const phone = document.getElementById("regPhone").value.trim();
    const pass = document.getElementById("regPassword").value.trim();
    store.register({ name, email, phone, password: pass });
    updateAccountHeaderButton();
    renderAccountProfile();
    showToast(`Account created! Welcome to BabyyGurll VIP Club 💕`, "🎉");
  });

  // Logout
  el.logoutBtn.addEventListener("click", () => {
    store.logout();
    updateAccountHeaderButton();
    renderAccountProfile();
    showToast("Signed out safely", "👋");
  });

  // Delegated Address Actions (Set Default, Edit, Delete, Track Order)
  el.profileViewPanel.addEventListener("click", (e) => {
    const setDefaultBtn = e.target.closest('[data-action="set-default"]');
    if (setDefaultBtn) {
      store.setDefaultAddress(setDefaultBtn.dataset.id);
      renderAccountProfile();
      showToast("Default address updated", "📍");
      return;
    }

    const deleteBtn = e.target.closest('[data-action="delete-addr"]');
    if (deleteBtn) {
      if (confirm("Are you sure you want to remove this saved address?")) {
        store.deleteAddress(deleteBtn.dataset.id);
        renderAccountProfile();
        showToast("Address deleted", "🗑️");
      }
      return;
    }

    const editBtn = e.target.closest('[data-action="edit-addr"]');
    if (editBtn) {
      const addrId = editBtn.dataset.id;
      const addr = store.getAddresses().find(a => a.id === addrId);
      if (addr) {
        openAddressModal(addr);
      }
      return;
    }

    const trackOrdBtn = e.target.closest('[data-action="account-track-order"]');
    if (trackOrdBtn) {
      const ordId = trackOrdBtn.dataset.id;
      el.accountModal.classList.remove("active");
      renderTrackingOrder(ordId);
      document.getElementById("orderTracking").scrollIntoView({ behavior: "smooth" });
      return;
    }
  });

  // ==========================================================================
  // 6. Add / Edit Address Form Modal
  // ==========================================================================
  function openAddressModal(editAddr = null) {
    if (editAddr) {
      el.addressModalTitle.textContent = "Edit Saved Address";
      el.addrFormEditId.value = editAddr.id;
      el.addrLabel.value = editAddr.label || "Home";
      el.addrFullName.value = editAddr.recipientName || "";
      el.addrPhone.value = editAddr.phone || "";
      el.addrStreet.value = editAddr.street || "";
      el.addrCity.value = editAddr.city || "";
      el.addrState.value = editAddr.state || "";
      el.addrZip.value = editAddr.zip || "";
      el.addrCountry.value = editAddr.country || "United States";
      el.addrIsDefault.checked = Boolean(editAddr.isDefault);
    } else {
      const u = store.getUser();
      el.addressModalTitle.textContent = "Add Saved Address";
      el.addrFormEditId.value = "";
      el.addrLabel.value = "Home";
      el.addrFullName.value = u ? u.name : "";
      el.addrPhone.value = u ? u.phone : "";
      el.addrStreet.value = "";
      el.addrCity.value = "";
      el.addrState.value = "";
      el.addrZip.value = "";
      el.addrCountry.value = "United States";
      el.addrIsDefault.checked = store.getAddresses().length === 0;
    }
    el.addressModal.classList.add("active");
  }

  el.btnOpenAddAddress.addEventListener("click", () => openAddressModal());
  el.closeAddressModalBtn.addEventListener("click", () => el.addressModal.classList.remove("active"));

  el.addressForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const editId = el.addrFormEditId.value;
    const addrData = {
      label: el.addrLabel.value.trim(),
      recipientName: el.addrFullName.value.trim(),
      phone: el.addrPhone.value.trim(),
      street: el.addrStreet.value.trim(),
      city: el.addrCity.value.trim(),
      state: el.addrState.value.trim(),
      zip: el.addrZip.value.trim(),
      country: el.addrCountry.value,
      isDefault: el.addrIsDefault.checked
    };

    if (editId) {
      store.updateAddress(editId, addrData);
      showToast("Address updated successfully! 💾", "📍");
    } else {
      store.addAddress(addrData);
      showToast("New address saved! 🏡", "📍");
    }

    el.addressModal.classList.remove("active");
    renderAccountProfile();
    renderCheckoutSavedAddresses();
  });

  // ==========================================================================
  // 7. Dedicated Order Tracking System
  // ==========================================================================
  function renderTrackingOrder(orderId) {
    const order = store.getOrderById(orderId);
    if (!order) {
      showToast(`Order "${orderId}" not found. Try sample order BGC-89241!`, "⚠️");
      return;
    }

    state.checkoutSelectedOrderId = order.orderId;
    el.orderTrackInput.value = order.orderId;
    el.orderTrackingResult.classList.add("active");

    // Header Display
    el.trackerOrderIdDisplay.textContent = `Order #${order.orderId}`;
    el.trackerOrderDateDisplay.textContent = order.dateFormatted;
    el.trackerDeliveryDateDisplay.textContent = order.estimatedDelivery;
    el.trackerCarrierDisplay.textContent = order.carrier;
    el.trackerWaybillDisplay.textContent = order.trackingNumber;

    // Status Pill
    el.trackerStatusPill.className = `tracker-status-pill status-pill-${order.statusCode}`;
    el.trackerStatusPill.textContent = order.statusLabel;

    // Timeline Stepper (5 milestones)
    const stageIcons = {
      received: "📦",
      crafted: "🪡",
      packed: "🎀",
      shipped: "🚚",
      delivered: "🏡"
    };

    const totalSteps = order.timeline.length;
    // Calculate progress bar line percentage (from 10% to 90%)
    const progressWidth = Math.min(80, (order.statusStage / (totalSteps - 1)) * 80);

    el.trackingTimelineContainer.innerHTML = `
      <div class="timeline-progress-bar" style="width: ${progressWidth}%;"></div>
      ${order.timeline.map((step, idx) => {
        const isDone = step.completed;
        const isCurrent = step.current;
        return `
          <div class="timeline-step ${isDone ? 'completed' : ''} ${isCurrent ? 'current' : ''}">
            <div class="step-icon-bubble">
              ${isDone && !isCurrent && idx < order.statusStage ? '✓' : (stageIcons[step.stage] || '✨')}
            </div>
            <div class="step-content">
              <h4>${step.title}</h4>
              <div class="step-time">${step.time}</div>
              <div class="step-desc">${step.desc}</div>
            </div>
          </div>
        `;
      }).join('')}
    `;

    // Package Items list
    el.trackerItemsList.innerHTML = order.items.map(item => `
      <div class="tracking-item-row">
        <img src="${item.image}" alt="${item.name}" class="tracking-item-thumb">
        <div class="tracking-item-info">
          <h5>${item.name}</h5>
          <p>Color: ${item.color} • Qty: ${item.quantity}</p>
        </div>
        <div class="tracking-item-price">${store.formatPrice(item.price * item.quantity)}</div>
      </div>
    `).join('');

    // Destination & Payment info
    const addr = order.shippingAddress;
    el.trackerRecipientDisplay.innerHTML = `<strong>${addr.recipientName || 'Valued Customer'}</strong>`;
    el.trackerAddressDisplay.innerHTML = `${addr.street}<br>${addr.city}, ${addr.state} ${addr.zip}, ${addr.country}`;
    el.trackerPhoneDisplay.textContent = `Phone: ${addr.phone || 'N/A'}`;
    el.trackerPaymentDisplay.textContent = `Payment: ${order.paymentMethod || 'Credit Card'} • Total: ${store.formatPrice(order.total)}`;
  }

  // Tracking Search Submission
  el.trackOrderSubmitBtn.addEventListener("click", () => {
    const val = el.orderTrackInput.value.trim();
    if (!val) {
      showToast("Please enter an Order ID", "🔍");
      return;
    }
    renderTrackingOrder(val);
  });

  el.orderTrackInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      el.trackOrderSubmitBtn.click();
    }
  });

  // Sample Chips Click
  document.querySelectorAll(".sample-chip").forEach(chip => {
    chip.addEventListener("click", () => {
      const id = chip.dataset.orderId;
      renderTrackingOrder(id);
      showToast(`Loaded sample order ${id}! 📦`, "🚚");
    });
  });

  // Interactive Stage Advance Simulator
  el.btnAdvanceStage.addEventListener("click", () => {
    const currentId = state.checkoutSelectedOrderId || "BGC-89241";
    const updated = store.advanceOrderStage(currentId);
    if (updated) {
      renderTrackingOrder(currentId);
      if (updated.statusStage === 4) {
        showToast(`Order ${updated.orderId} is DELIVERED to your doorstep! 🏡🎉`, "🌸");
        triggerConfetti();
      } else {
        showToast(`Advanced to: ${updated.statusLabel}! ⚡`, "✨");
      }
    }
  });

  // Top Nav "Track Order" link
  el.navOrderTracker.addEventListener("click", (e) => {
    e.preventDefault();
    renderTrackingOrder("BGC-89241");
    document.getElementById("orderTracking").scrollIntoView({ behavior: "smooth" });
  });

  // ==========================================================================
  // 8. Multi-Step Checkout Flow
  // ==========================================================================
  function renderCheckoutSavedAddresses() {
    const addresses = store.getAddresses();
    if (addresses.length === 0) {
      el.checkoutSavedAddressesList.innerHTML = `
        <p style="font-size: 0.85rem; color: var(--text-muted);">No addresses saved. Please add an address to continue!</p>
      `;
      state.checkoutSelectedAddressId = null;
      return;
    }

    if (!state.checkoutSelectedAddressId || !addresses.find(a => a.id === state.checkoutSelectedAddressId)) {
      const def = store.getDefaultAddress();
      state.checkoutSelectedAddressId = def ? def.id : addresses[0].id;
    }

    el.checkoutSavedAddressesList.innerHTML = addresses.map(addr => {
      const isSelected = addr.id === state.checkoutSelectedAddressId;
      return `
        <label style="display: flex; align-items: flex-start; gap: 0.85rem; padding: 1.1rem; border: 1px solid ${isSelected ? 'var(--primary)' : 'var(--border-subtle)'}; border-radius: var(--radius-md); background: ${isSelected ? '#FFF9FA' : '#FFFFFF'}; cursor: pointer;">
          <input type="radio" name="checkoutAddressSelect" value="${addr.id}" ${isSelected ? 'checked' : ''} style="margin-top: 3px; accent-color: var(--primary);">
          <div style="flex-grow: 1;">
            <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.2rem;">
              <strong style="font-size: 0.95rem; color: var(--text-dark);">${addr.label}</strong>
              ${addr.isDefault ? '<span class="default-pill">DEFAULT</span>' : ''}
            </div>
            <div style="font-size: 0.88rem; font-weight: 600; color: var(--text-dark);">${addr.recipientName} • ${addr.phone}</div>
            <div style="font-size: 0.82rem; color: var(--text-muted);">${addr.street}, ${addr.city}, ${addr.state} ${addr.zip}, ${addr.country}</div>
          </div>
        </label>
      `;
    }).join('');

    // Attach listeners
    el.checkoutSavedAddressesList.querySelectorAll('input[name="checkoutAddressSelect"]').forEach(radio => {
      radio.addEventListener("change", (e) => {
        state.checkoutSelectedAddressId = e.target.value;
        renderCheckoutSavedAddresses();
      });
    });
  }

  function openCheckout() {
    const cart = store.getCart();
    if (cart.length === 0) {
      showToast("Your cart is empty! Add some crochet pieces first 🍓", "🧺");
      return;
    }

    closeCartDrawer();
    setCheckoutStep(1);
    renderCheckoutSavedAddresses();
    el.checkoutModal.classList.add("active");
  }

  function setCheckoutStep(stepNumber) {
    el.pillStep1.classList.toggle("active", stepNumber >= 1);
    el.pillStep2.classList.toggle("active", stepNumber >= 2);
    el.pillStep3.classList.toggle("active", stepNumber >= 3);

    el.checkoutStep1.style.display = stepNumber === 1 ? "block" : "none";
    el.checkoutStep2.style.display = stepNumber === 2 ? "block" : "none";
    el.checkoutStep3.style.display = stepNumber === 3 ? "block" : "none";
    el.checkoutStepSuccess.style.display = stepNumber === 4 ? "block" : "none";

    if (stepNumber === 3) {
      const totals = store.getCartTotals();
      el.checkoutFinalTotalDisplay.textContent = store.formatPrice(totals.total);
      const user = store.getUser();
      el.cardHolderPreview.textContent = (user && user.name) ? user.name.toUpperCase() : "CHLOE BENNETT";
    }
  }

  el.proceedCheckoutBtn.addEventListener("click", openCheckout);
  el.closeCheckoutBtn.addEventListener("click", () => el.checkoutModal.classList.remove("active"));

  el.checkoutAddAddressInlineBtn.addEventListener("click", () => {
    openAddressModal();
  });

  // Step Navigations
  el.btnNextToStep2.addEventListener("click", () => {
    if (!state.checkoutSelectedAddressId) {
      showToast("Please choose or add a shipping address", "⚠️");
      return;
    }
    setCheckoutStep(2);
  });

  el.btnBackToStep1.addEventListener("click", () => setCheckoutStep(1));
  el.btnNextToStep3.addEventListener("click", () => setCheckoutStep(3));
  el.btnBackToStep2.addEventListener("click", () => setCheckoutStep(2));

  // Place Order Final Action
  el.btnPlaceOrderFinal.addEventListener("click", () => {
    const selectedAddr = store.getAddresses().find(a => a.id === state.checkoutSelectedAddressId) || store.getDefaultAddress();
    const totals = store.getCartTotals();
    const cartItems = store.getCart();

    const newOrder = store.createOrder({
      shippingAddress: selectedAddr,
      paymentMethod: "Visa ending in •••• 4242",
      items: cartItems,
      totals: totals
    });

    state.checkoutSelectedOrderId = newOrder.orderId;
    el.newPlacedOrderId.textContent = `Order #${newOrder.orderId}`;
    updateCartUI();
    setCheckoutStep(4);
    triggerConfetti();
    showToast(`Order #${newOrder.orderId} placed successfully! 🌸`, "🎉");
  });

  el.btnTrackNewlyPlacedOrder.addEventListener("click", () => {
    el.checkoutModal.classList.remove("active");
    renderTrackingOrder(state.checkoutSelectedOrderId);
    document.getElementById("orderTracking").scrollIntoView({ behavior: "smooth" });
  });

  el.btnContinueShoppingAfterSuccess.addEventListener("click", () => {
    el.checkoutModal.classList.remove("active");
    document.getElementById("catalog").scrollIntoView({ behavior: "smooth" });
  });

  // ==========================================================================
  // 9. FAQ Accordion
  // ==========================================================================
  document.querySelectorAll(".faq-item").forEach(item => {
    const btn = item.querySelector(".faq-question");
    btn.addEventListener("click", () => {
      const isOpen = item.classList.contains("open");
      document.querySelectorAll(".faq-item").forEach(i => i.classList.remove("open"));
      if (!isOpen) {
        item.classList.add("open");
      }
    });
  });

  // ==========================================================================
  // 10. Festive Confetti Cannon
  // ==========================================================================
  function triggerConfetti() {
    const canvas = el.confettiCanvas;
    if (!canvas) return;
    canvas.style.display = "block";
    const ctx = canvas.getContext("2d");
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    const pieces = [];
    const colors = ["#FF6584", "#9B72CF", "#FFCCD7", "#E9D5FF", "#7AA885", "#FFD166"];
    for (let i = 0; i < 90; i++) {
      pieces.push({
        x: canvas.width / 2,
        y: canvas.height / 2,
        vx: (Math.random() - 0.5) * 16,
        vy: (Math.random() - 0.8) * 18,
        size: Math.random() * 8 + 4,
        color: colors[Math.floor(Math.random() * colors.length)],
        rotation: Math.random() * 360,
        vr: (Math.random() - 0.5) * 10,
        alpha: 1
      });
    }

    let frame = 0;
    function animate() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      frame++;
      let alive = false;
      pieces.forEach(p => {
        p.x += p.vx;
        p.y += p.vy;
        p.vy += 0.35; // gravity
        p.rotation += p.vr;
        p.alpha -= 0.012;

        if (p.alpha > 0) {
          alive = true;
          ctx.save();
          ctx.globalAlpha = p.alpha;
          ctx.translate(p.x, p.y);
          ctx.rotate((p.rotation * Math.PI) / 180);
          ctx.fillStyle = p.color;
          ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size);
          ctx.restore();
        }
      });

      if (alive && frame < 100) {
        requestAnimationFrame(animate);
      } else {
        canvas.style.display = "none";
      }
    }
    requestAnimationFrame(animate);
  }

  // ==========================================================================
  // Initial Boot
  // ==========================================================================
  renderCatalog();
  updateCartUI();
  updateAccountHeaderButton();
  // Pre-render sample order into tracking section so it's populated and ready
  renderTrackingOrder("BGC-89241");
});
