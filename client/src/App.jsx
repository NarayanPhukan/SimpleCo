import React, { useState, useEffect, useRef } from 'react';
import { api } from './services/api';
import { useAuth } from './context/AuthContext';
import { useCart } from './context/CartContext';

import { AnnouncementBar } from './components/AnnouncementBar';
import { Navbar } from './components/Navbar';
import { Hero } from './components/Hero';
import { Highlights } from './components/Highlights';
import { ProductCard } from './components/ProductCard';
import { QuickViewModal } from './components/QuickViewModal';
import { CartDrawer } from './components/CartDrawer';
import { AccountModal } from './components/AccountModal';
import { AddressModal } from './components/AddressModal';
import { CheckoutModal } from './components/CheckoutModal';
import { Reviews } from './components/Reviews';
import { FAQ } from './components/FAQ';
import { Footer } from './components/Footer';
import { AdminPanel } from './components/AdminPanel';
import { WishlistDrawer } from './components/WishlistDrawer';
import { CustomStudioModal } from './components/CustomStudioModal';
import { WhatsAppConcierge } from './components/WhatsAppConcierge';
import { RecentlyViewedBar } from './components/RecentlyViewedBar';
import { LegalPolicyModal } from './components/LegalPolicyModal';
import { AppDownloadModal } from './components/AppDownloadModal';
import { IconSearch, IconPackage, IconSparkles } from './components/Icons';

export const App = () => {
  const [products, setProducts] = useState(() => {
    try {
      const cached = localStorage.getItem('bgc_cached_catalog');
      if (cached) {
        const parsed = JSON.parse(cached);
        if (Array.isArray(parsed) && parsed.length > 0) return parsed;
      }
    } catch (e) {}
    return [];
  });
  const [loadingProducts, setLoadingProducts] = useState(false);
  const [catalogError, setCatalogError] = useState(false);
  const [activeCategory, setActiveCategory] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortOrder, setSortOrder] = useState('featured');

  // Modals state
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isWishlistOpen, setIsWishlistOpen] = useState(false);
  const [isCustomStudioOpen, setIsCustomStudioOpen] = useState(false);
  const [isAccountOpen, setIsAccountOpen] = useState(false);
  const [isAddressModalOpen, setIsAddressModalOpen] = useState(false);
  const [editingAddress, setEditingAddress] = useState(null);
  const [isCheckoutOpen, setIsCheckoutOpen] = useState(false);
  const [quickViewProduct, setQuickViewProduct] = useState(null);
  const [isAdminOpen, setIsAdminOpen] = useState(false);
  const [isPolicyModalOpen, setIsPolicyModalOpen] = useState(false);
  const [policyInitialTab, setPolicyInitialTab] = useState('terms');
  const [isAppDownloadOpen, setIsAppDownloadOpen] = useState(false);

  // Toasts
  const [toasts, setToasts] = useState([]);
  const confettiCanvasRef = useRef(null);

  const showToast = (message, icon = '✓') => {
    const id = Date.now() + Math.random();
    setToasts(prev => [...prev, { id, message, icon }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 3200);
  };

  // Fetch products from MongoDB Backend
  const loadProducts = async (silent = false) => {
    try {
      if (!silent && products.length === 0) setLoadingProducts(true);
      const res = await api.getProducts({
        category: activeCategory,
        search: searchQuery,
        sort: sortOrder
      });
      const list = res.data || [];
      setProducts(list);
      setCatalogError(false);
      if (!silent) setLoadingProducts(false);

      if (activeCategory === 'all' && !searchQuery && list.length > 0) {
        try {
          localStorage.setItem('bgc_cached_catalog', JSON.stringify(list));
        } catch (e) {}
      }

      // If empty on initial mount, retry once after 2 seconds in case backend was cold booting
      if (list.length === 0 && !searchQuery && activeCategory === 'all') {
        setTimeout(async () => {
          try {
            const retryRes = await api.getProducts({
              category: activeCategory,
              search: searchQuery,
              sort: sortOrder
            });
            if (retryRes?.data?.length > 0) {
              setProducts(retryRes.data);
              setCatalogError(false);
              try {
                localStorage.setItem('bgc_cached_catalog', JSON.stringify(retryRes.data));
              } catch (e) {}
            }
          } catch (e) {
            // Ignore retry error
          }
        }, 2000);
      }
    } catch (err) {
      console.warn('API fetch products error:', err.message);
      if (!silent) {
        setLoadingProducts(false);
        setCatalogError(true);
      }
      // Auto-retry in 2.5s if catalog is currently empty
      if (products.length === 0) {
        setTimeout(() => {
          loadProducts(true);
        }, 2500);
      }
    }
  };

  useEffect(() => {
    loadProducts();
  }, [activeCategory, searchQuery, sortOrder]);

  // Refresh catalog immediately when tab regains focus or custom event fires
  useEffect(() => {
    const handleFocus = () => {
      loadProducts(true);
    };
    window.addEventListener('focus', handleFocus);
    window.addEventListener('catalog_updated', handleFocus);
    return () => {
      window.removeEventListener('focus', handleFocus);
      window.removeEventListener('catalog_updated', handleFocus);
    };
  }, [activeCategory, searchQuery, sortOrder]);

  // Real-time periodic dynamic sync loop (updates catalog every 6 seconds)
  useEffect(() => {
    const timer = setInterval(() => {
      loadProducts(true);
    }, 6000);
    return () => clearInterval(timer);
  }, [activeCategory, searchQuery, sortOrder]);

  // Listen for #admin, #studio, #custom, #wishlist, #account, #orders, #checkout hash in URL
  useEffect(() => {
    const handleHash = () => {
      const hash = window.location.hash;
      if (hash === '#admin') {
        setIsAdminOpen(true);
      } else if (hash === '#studio' || hash === '#custom' || hash === '#builder') {
        setIsCustomStudioOpen(true);
      } else if (hash === '#wishlist') {
        setIsWishlistOpen(true);
      } else if (hash === '#account' || hash === '#orders') {
        setIsAccountOpen(true);
      } else if (hash === '#checkout') {
        setIsCheckoutOpen(true);
      } else if (hash === '#app' || hash === '#download') {
        setIsAppDownloadOpen(true);
      } else if (hash === '#refund-policy' || hash === '#refund' || hash === '#cancellation') {
        setPolicyInitialTab('refund');
        setIsPolicyModalOpen(true);
      } else if (hash === '#shipping-policy' || hash === '#shipping') {
        setPolicyInitialTab('shipping');
        setIsPolicyModalOpen(true);
      } else if (hash === '#terms' || hash === '#conditions') {
        setPolicyInitialTab('terms');
        setIsPolicyModalOpen(true);
      } else if (hash === '#privacy') {
        setPolicyInitialTab('privacy');
        setIsPolicyModalOpen(true);
      } else if (hash === '#contact' || hash === '#merchant') {
        setPolicyInitialTab('contact');
        setIsPolicyModalOpen(true);
      }
    };
    handleHash();
    window.addEventListener('hashchange', handleHash);
    return () => window.removeEventListener('hashchange', handleHash);
  }, []);

  // Confetti Animation
  const triggerConfetti = () => {
    const canvas = confettiCanvasRef.current;
    if (!canvas) return;
    canvas.style.display = 'block';
    const ctx = canvas.getContext('2d');
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    const colors = ['#021861', '#255DCE', '#467DF2', '#DE1110', '#85ACFA', '#FF5756'];
    const pieces = [];
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
        p.vy += 0.35;
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
        canvas.style.display = 'none';
      }
    }
    requestAnimationFrame(animate);
  };

  const handleExplore = () => {
    const elem = document.getElementById('catalog');
    if (elem) elem.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <>
      <AnnouncementBar />

      <Navbar 
        onOpenAccount={() => {
          setIsAccountOpen(true);
          window.history.pushState(null, '', '#account');
        }}
        onOpenCart={() => setIsCartOpen(true)}
        onOpenWishlist={() => {
          setIsWishlistOpen(true);
          window.history.pushState(null, '', '#wishlist');
        }}
        onOpenCustomStudio={() => {
          setIsCustomStudioOpen(true);
          window.history.pushState(null, '', '#studio');
        }}
        onOpenAdmin={() => {
          setIsAdminOpen(true);
          window.history.pushState(null, '', '#admin');
        }}
        onSelectCategory={(cat) => setActiveCategory(cat)}
      />

      <main>
        <Hero 
          onExplore={handleExplore}
        />

        <Highlights />

        {/* Custom Studio & Bouquet Builder Interactive Banner */}
        <section className="custom-studio-banner-section">
          <div className="container">
            <div className="custom-studio-banner-card">
              <div className="studio-banner-content">
                <span className="studio-banner-pill">
                  <IconSparkles size={13} />
                  <span>Artisanal Atelier Studio</span>
                </span>
                <h3 className="studio-banner-title">
                  Build Your Everlasting Bouquet or Commission a Bespoke Piece 🌸
                </h3>
                <p className="studio-banner-text">
                  Choose your favorite milk-cotton stems flower-by-flower, select premium wrapping paper and satin ribbons, or commission a personalized heirloom friend hand-hooked just for you.
                </p>
                <div className="studio-banner-chips">
                  <span className="studio-chip">🌷 Pink Tulips</span>
                  <span className="studio-chip">🌻 Sunflowers</span>
                  <span className="studio-chip">🌼 Daisies</span>
                  <span className="studio-chip">🌹 Velvet Roses</span>
                  <span className="studio-chip">🎀 Satin Ribbon</span>
                  <span className="studio-chip">📜 Calligraphy Note</span>
                </div>
                <div className="studio-banner-actions">
                  <button 
                    className="btn btn-primary"
                    onClick={() => {
                      setIsCustomStudioOpen(true);
                      window.history.pushState(null, '', '#studio');
                    }}
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem' }}
                  >
                    <IconSparkles size={16} />
                    <span>Launch Bouquet Studio</span>
                  </button>
                  <button 
                    className="btn btn-secondary"
                    onClick={() => {
                      setIsWishlistOpen(true);
                      window.history.pushState(null, '', '#wishlist');
                    }}
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem' }}
                  >
                    <span>View Saved Wishlist</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Catalog Section */}
        <section className="catalog-section" id="catalog">
          <div className="container">
            <div className="section-header">
              <span className="section-tag">Limited Studio Releases</span>
              <h2 className="section-title">Artisanal Hand-Hooked Collection</h2>
              <p className="section-subtitle">
                Mindfully crocheted in small batches. Everlasting botanicals, cuddly desk companions, and everyday heirloom totes crafted with 100% natural milk-cotton yarn.
              </p>
            </div>

            {/* Toolbar */}
            <div className="catalog-toolbar">
              <div className="filter-tabs">
                <button 
                  className={`filter-tab ${activeCategory === 'all' ? 'active' : ''}`}
                  onClick={() => setActiveCategory('all')}
                >
                  All Products
                </button>
                <button 
                  className={`filter-tab ${activeCategory === 'plushies' ? 'active' : ''}`}
                  onClick={() => setActiveCategory('plushies')}
                >
                  Amigurumi Plushies
                </button>
                <button 
                  className={`filter-tab ${activeCategory === 'floral' ? 'active' : ''}`}
                  onClick={() => setActiveCategory('floral')}
                >
                  Floral & Home
                </button>
                <button 
                  className={`filter-tab ${activeCategory === 'bags' ? 'active' : ''}`}
                  onClick={() => setActiveCategory('bags')}
                >
                  Bags & Totes
                </button>
                <button 
                  className={`filter-tab ${activeCategory === 'wearables' ? 'active' : ''}`}
                  onClick={() => setActiveCategory('wearables')}
                >
                  Wearables
                </button>
                <button 
                  className={`filter-tab ${activeCategory === 'accessories' ? 'active' : ''}`}
                  onClick={() => setActiveCategory('accessories')}
                >
                  Accessories
                </button>
              </div>

              <div className="toolbar-right">
                <div className="search-input-wrapper">
                  <span className="search-icon" style={{ display: 'inline-flex', alignItems: 'center' }}>
                    <IconSearch size={16} />
                  </span>
                  <input 
                    type="text" 
                    placeholder="Search products..." 
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                </div>

                <select 
                  className="sort-select"
                  value={sortOrder}
                  onChange={(e) => setSortOrder(e.target.value)}
                  aria-label="Sort products"
                >
                  <option value="featured">Featured</option>
                  <option value="price-asc">Price: Low to High</option>
                  <option value="price-desc">Price: High to Low</option>
                  <option value="rating">Highest Rated</option>
                </select>
              </div>
            </div>

            {/* Products Grid */}
            <div className="products-grid">
              {loadingProducts && products.length === 0 ? (
                <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '3.5rem 0' }}>
                  <div style={{ width: '36px', height: '36px', border: '3px solid #E2E8F0', borderTopColor: 'var(--primary)', borderRadius: '50%', margin: '0 auto', animation: 'spin 0.8s linear infinite' }}></div>
                  <p style={{ marginTop: '0.85rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>Loading handcrafted catalog...</p>
                </div>
              ) : catalogError && products.length === 0 ? (
                <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '3.5rem 1rem' }}>
                  <div style={{ color: 'var(--primary)', marginBottom: '1rem', display: 'flex', justifyContent: 'center' }}>
                    <IconPackage size={48} />
                  </div>
                  <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.35rem', color: 'var(--text-dark)' }}>
                    Connecting to Studio Catalog...
                  </h3>
                  <p style={{ color: 'var(--text-muted)', marginTop: '0.4rem', marginBottom: '1.25rem', fontSize: '0.9rem' }}>
                    Waking up cloud server. Retrying automatically...
                  </p>
                  <button 
                    className="btn btn-primary"
                    onClick={() => loadProducts(false)}
                    style={{ padding: '0.5rem 1.35rem', fontSize: '0.85rem' }}
                  >
                    ↻ Retry Now
                  </button>
                </div>
              ) : products.length === 0 ? (
                <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '4rem 1rem' }}>
                  <div style={{ color: '#94A3B8', marginBottom: '1rem', display: 'flex', justifyContent: 'center' }}>
                    <IconPackage size={48} />
                  </div>
                  <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.4rem', color: 'var(--text-dark)' }}>
                    No products found
                  </h3>
                  <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                    {searchQuery ? `No pieces found matching "${searchQuery}".` : 'Try selecting another category or view all.'}
                  </p>
                  {(activeCategory !== 'all' || searchQuery) && (
                    <button
                      className="btn btn-outline"
                      onClick={() => { setActiveCategory('all'); setSearchQuery(''); }}
                      style={{ marginTop: '1.1rem', padding: '0.45rem 1.2rem', fontSize: '0.85rem' }}
                    >
                      View All Products
                    </button>
                  )}
                </div>
              ) : (
                products.map(product => (
                  <ProductCard 
                    key={product.id || product._id}
                    product={product}
                    onQuickView={(p) => setQuickViewProduct(p)}
                    onShowToast={showToast}
                  />
                ))
              )}
            </div>
          </div>
        </section>

        {/* Recently Viewed Shelf */}
        <RecentlyViewedBar 
          onQuickView={(p) => setQuickViewProduct(p)} 
          onShowToast={showToast} 
        />

        <Reviews onShowToast={showToast} />

        <FAQ />
      </main>

      <Footer 
        onOpenAccount={() => {
          setIsAccountOpen(true);
          window.history.pushState(null, '', '#account');
        }}
        onScrollToTrack={() => {
          setIsAccountOpen(true);
          window.history.pushState(null, '', '#orders');
        }}
        onSelectCategory={(cat) => setActiveCategory(cat)}
        onOpenAdmin={() => {
          setIsAdminOpen(true);
          window.history.pushState(null, '', '#admin');
        }}
        onShowToast={showToast}
        onOpenPolicy={(tab) => {
          setPolicyInitialTab(tab);
          setIsPolicyModalOpen(true);
        }}
        onOpenAppDownload={() => setIsAppDownloadOpen(true)}
      />

      {/* Modals & Drawers */}
      <CartDrawer 
        isOpen={isCartOpen}
        onClose={() => setIsCartOpen(false)}
        onProceedCheckout={() => {
          setIsCartOpen(false);
          setIsCheckoutOpen(true);
          window.history.pushState(null, '', '#checkout');
        }}
        onShowToast={showToast}
        onExplore={handleExplore}
      />

      <QuickViewModal 
        product={quickViewProduct}
        onClose={() => setQuickViewProduct(null)}
        onShowToast={showToast}
        onOpenCart={() => setIsCartOpen(true)}
      />

      <AccountModal 
        isOpen={isAccountOpen}
        onClose={() => {
          setIsAccountOpen(false);
          if (window.location.hash === '#account' || window.location.hash === '#orders') {
            window.history.pushState(null, '', window.location.pathname);
          }
        }}
        onOpenAddressModal={(addr) => {
          setEditingAddress(addr);
          setIsAddressModalOpen(true);
        }}
        onShowToast={showToast}
      />

      <AddressModal 
        isOpen={isAddressModalOpen}
        editAddress={editingAddress}
        onClose={() => {
          setIsAddressModalOpen(false);
          setEditingAddress(null);
        }}
        onShowToast={showToast}
      />

      <CheckoutModal 
        isOpen={isCheckoutOpen}
        onClose={() => {
          setIsCheckoutOpen(false);
          if (window.location.hash === '#checkout') {
            window.history.pushState(null, '', window.location.pathname);
          }
        }}
        onOpenAddressModal={(addr) => {
          setEditingAddress(addr);
          setIsAddressModalOpen(true);
        }}
        onOpenAccountModal={() => {
          setIsCheckoutOpen(false);
          setIsAccountOpen(true);
          window.history.pushState(null, '', '#account');
        }}
        onOrderPlacedSuccess={() => {
          triggerConfetti();
        }}
        onShowToast={showToast}
      />

      <AdminPanel 
        isOpen={isAdminOpen}
        onClose={() => {
          setIsAdminOpen(false);
          if (window.location.hash === '#admin') {
            window.history.pushState(null, '', window.location.pathname);
          }
        }}
        onShowToast={showToast}
        onProductCatalogUpdated={loadProducts}
      />

      {/* Wishlist Drawer */}
      <WishlistDrawer 
        isOpen={isWishlistOpen}
        onClose={() => {
          setIsWishlistOpen(false);
          if (window.location.hash === '#wishlist') {
            window.history.pushState(null, '', window.location.pathname);
          }
        }}
        products={products}
        onShowToast={showToast}
        onOpenCart={() => setIsCartOpen(true)}
        onExplore={handleExplore}
      />

      {/* Custom Studio & Bouquet Builder Modal */}
      <CustomStudioModal 
        isOpen={isCustomStudioOpen}
        onClose={() => {
          setIsCustomStudioOpen(false);
          if (window.location.hash === '#studio' || window.location.hash === '#custom' || window.location.hash === '#builder') {
            window.history.pushState(null, '', window.location.pathname);
          }
        }}
        onShowToast={showToast}
        onOpenCart={() => setIsCartOpen(true)}
      />

      {/* Razorpay Compliance Policies Modal */}
      <LegalPolicyModal 
        isOpen={isPolicyModalOpen}
        onClose={() => {
          setIsPolicyModalOpen(false);
          if (['#refund-policy', '#refund', '#cancellation', '#shipping-policy', '#shipping', '#terms', '#privacy', '#contact'].includes(window.location.hash)) {
            window.history.pushState(null, '', window.location.pathname);
          }
        }}
        initialTab={policyInitialTab}
      />

      {/* Android Mobile App Showcase & Download Modal */}
      <AppDownloadModal 
        isOpen={isAppDownloadOpen}
        onClose={() => {
          setIsAppDownloadOpen(false);
          if (window.location.hash === '#app' || window.location.hash === '#download') {
            window.history.pushState(null, '', window.location.pathname);
          }
        }}
      />

      {/* Live WhatsApp Floating Concierge */}
      <WhatsAppConcierge />

      {/* Toast Notification Container */}
      <div className="toast-container" aria-live="polite">
        {toasts.map(t => (
          <div key={t.id} className="toast">
            <span className="toast-icon">{t.icon}</span>
            <span>{t.message}</span>
          </div>
        ))}
      </div>

      {/* Confetti Canvas */}
      <canvas 
        ref={confettiCanvasRef}
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100vw',
          height: '100vh',
          pointerEvents: 'none',
          zIndex: 999,
          display: 'none'
        }}
      />
    </>
  );
};
