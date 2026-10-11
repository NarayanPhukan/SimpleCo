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

export const App = () => {
  const [products, setProducts] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [activeCategory, setActiveCategory] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortOrder, setSortOrder] = useState('featured');

  // Modals state
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isAccountOpen, setIsAccountOpen] = useState(false);
  const [isAddressModalOpen, setIsAddressModalOpen] = useState(false);
  const [editingAddress, setEditingAddress] = useState(null);
  const [isCheckoutOpen, setIsCheckoutOpen] = useState(false);
  const [quickViewProduct, setQuickViewProduct] = useState(null);
  const [isAdminOpen, setIsAdminOpen] = useState(false);

  // Toasts
  const [toasts, setToasts] = useState([]);
  const confettiCanvasRef = useRef(null);

  const showToast = (message, icon = '🌸') => {
    const id = Date.now() + Math.random();
    setToasts(prev => [...prev, { id, message, icon }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 3200);
  };

  // Fetch products from MongoDB Backend
  const loadProducts = async () => {
    try {
      setLoadingProducts(true);
      const res = await api.getProducts({
        category: activeCategory,
        search: searchQuery,
        sort: sortOrder
      });
      setProducts(res.data || []);
      setLoadingProducts(false);
    } catch (err) {
      console.warn('API fetch products error, using fallback:', err.message);
      setLoadingProducts(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, [activeCategory, searchQuery, sortOrder]);

  // Listen for #admin or #studio hash in URL
  useEffect(() => {
    const handleHash = () => {
      if (window.location.hash === '#admin' || window.location.hash === '#studio') {
        setIsAdminOpen(true);
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

    const pieces = [];
    const colors = ['#0095FF', '#33AAFF', '#66BFFF', '#99D5FF', '#CCEAFF', '#0077CC'];
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
        onOpenAccount={() => setIsAccountOpen(true)}
        onOpenCart={() => setIsCartOpen(true)}
        onOpenAdmin={() => setIsAdminOpen(true)}
        onSelectCategory={(cat) => setActiveCategory(cat)}
      />

      <main>
        <Hero 
          onExplore={handleExplore}
        />

        <Highlights />

        {/* Catalog Section */}
        <section className="catalog-section" id="catalog">
          <div className="container">
            <div className="section-header">
              <span className="section-tag">Curated MERN Boutique</span>
              <h2 className="section-title">Handmade Crochet Creations</h2>
              <p className="section-subtitle">
                Discover our cutest amigurumi plushies, everlasting bouquets, and trendy everyday totes stored in MongoDB.
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
                  Amigurumi Plushies 🐰
                </button>
                <button 
                  className={`filter-tab ${activeCategory === 'floral' ? 'active' : ''}`}
                  onClick={() => setActiveCategory('floral')}
                >
                  Floral & Home 🌸
                </button>
                <button 
                  className={`filter-tab ${activeCategory === 'bags' ? 'active' : ''}`}
                  onClick={() => setActiveCategory('bags')}
                >
                  Bags & Totes 👜
                </button>
                <button 
                  className={`filter-tab ${activeCategory === 'wearables' ? 'active' : ''}`}
                  onClick={() => setActiveCategory('wearables')}
                >
                  Wearables ☁️
                </button>
                <button 
                  className={`filter-tab ${activeCategory === 'accessories' ? 'active' : ''}`}
                  onClick={() => setActiveCategory('accessories')}
                >
                  Accessories 🍓
                </button>
              </div>

              <div className="toolbar-right">
                <div className="search-input-wrapper">
                  <span className="search-icon">🔍</span>
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
              {loadingProducts ? (
                <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '3rem 0' }}>
                  <div style={{ fontSize: '2.5rem', animation: 'spin 1s infinite' }}>🧶</div>
                  <p style={{ marginTop: '0.5rem', color: 'var(--text-muted)' }}>Loading crochet creations from MongoDB...</p>
                </div>
              ) : products.length === 0 ? (
                <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '4rem 1rem' }}>
                  <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🧶</div>
                  <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '1.4rem', color: 'var(--text-dark)' }}>
                    No crochet pieces match your search
                  </h3>
                  <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                    Try searching for another keyword or view all categories.
                  </p>
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

        <Reviews />

        <FAQ />
      </main>

      <Footer 
        onOpenAccount={() => setIsAccountOpen(true)}
        onScrollToTrack={() => setIsAccountOpen(true)}
        onSelectCategory={(cat) => setActiveCategory(cat)}
        onOpenAdmin={() => setIsAdminOpen(true)}
        onShowToast={showToast}
      />

      {/* Modals & Drawers */}
      <CartDrawer 
        isOpen={isCartOpen}
        onClose={() => setIsCartOpen(false)}
        onProceedCheckout={() => {
          setIsCartOpen(false);
          setIsCheckoutOpen(true);
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
        onClose={() => setIsAccountOpen(false)}
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
        onClose={() => setIsCheckoutOpen(false)}
        onOpenAddressModal={(addr) => {
          setEditingAddress(addr);
          setIsAddressModalOpen(true);
        }}
        onOpenAccountModal={() => {
          setIsCheckoutOpen(false);
          setIsAccountOpen(true);
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
          if (window.location.hash === '#admin' || window.location.hash === '#studio') {
            window.history.pushState(null, '', window.location.pathname);
          }
        }}
        onShowToast={showToast}
        onProductCatalogUpdated={loadProducts}
      />

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
