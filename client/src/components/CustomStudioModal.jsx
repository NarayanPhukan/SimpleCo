import React, { useState } from 'react';
import { useCart } from '../context/CartContext';
import { IconClose, IconFlower, IconSparkles, IconCheck, IconBag } from './Icons';

// Available Stem Choices
const BOUQUET_FLOWERS = [
  { id: 'tulip_pink', name: 'Soft Pink Milk Tulip', price: 199, emoji: '🌷', color: '#F7A8B8' },
  { id: 'daisy_cream', name: 'Cream White Daisy', price: 149, emoji: '🌼', color: '#FDFBF7' },
  { id: 'lavender_sprig', name: 'French Lavender Sprig', price: 129, emoji: '🪻', color: '#C8A2C8' },
  { id: 'sunshine_blossom', name: 'Golden Sunflower', price: 229, emoji: '🌻', color: '#F59E0B' },
  { id: 'velvet_rose', name: 'Crimson Velvet Rose', price: 249, emoji: '🌹', color: '#E11D48' },
  { id: 'eucalyptus_stem', name: 'Sage Eucalyptus Leaf', price: 99, emoji: '🌿', color: '#84A98C' }
];

const WRAPPING_PAPERS = [
  { id: 'kraft', name: 'Vintage Eco Kraft', color: '#D4B996' },
  { id: 'pink', name: 'Pastel Strawberry Pink', color: '#F7D6DE' },
  { id: 'lilac', name: 'Soft Provence Lilac', color: '#E5D4ED' },
  { id: 'sage', name: 'Sage Green Linen', color: '#CFE1D9' },
  { id: 'cream', name: 'Champagne Silk Cream', color: '#FBF5ED' }
];

const RIBBON_COLORS = [
  { id: 'rose_gold', name: 'Rose Gold Satin', color: '#B76E79' },
  { id: 'ivory', name: 'Ivory Cream', color: '#FFFDD0' },
  { id: 'wine', name: 'Wine Velvet', color: '#722F37' },
  { id: 'matcha', name: 'Matcha Sage', color: '#889C7B' }
];

const COMMISSION_CATEGORIES = [
  { id: 'plushie', name: 'Custom Amigurumi Plushie', basePrice: 1399, leadDays: '4-6 days', icon: '🧸' },
  { id: 'bag', name: 'Custom Granny Square Tote', basePrice: 1799, leadDays: '5-7 days', icon: '👜' },
  { id: 'cardigan', name: 'Artisanal Wearable / Vest', basePrice: 3499, leadDays: '8-12 days', icon: '🧶' },
  { id: 'charm', name: 'Personalized Charm / Keychain', basePrice: 449, leadDays: '2-3 days', icon: '✨' }
];

export const CustomStudioModal = ({ isOpen, onClose, onShowToast, onOpenCart }) => {
  const { addToCart, formatPrice } = useCart();

  // Active studio mode: 'bouquet' or 'commission'
  const [activeTab, setActiveTab] = useState('bouquet');

  // Bouquet Builder State
  const [flowerCounts, setFlowerCounts] = useState({
    tulip_pink: 2,
    daisy_cream: 2,
    lavender_sprig: 1,
    sunshine_blossom: 0,
    velvet_rose: 0,
    eucalyptus_stem: 1
  });
  const [selectedWrapping, setSelectedWrapping] = useState('pink');
  const [selectedRibbon, setSelectedRibbon] = useState('rose_gold');
  const [bouquetCardMessage, setBouquetCardMessage] = useState('');

  // Commission Builder State
  const [commType, setCommType] = useState('plushie');
  const [commTitle, setCommTitle] = useState('');
  const [commColors, setCommColors] = useState('Strawberry Pink & Cream');
  const [commNotes, setCommNotes] = useState('');
  const [commInitials, setCommInitials] = useState('');

  if (!isOpen) return null;

  // Bouquet Calculations
  const totalStems = Object.values(flowerCounts).reduce((a, b) => a + b, 0);
  const stemSubtotal = BOUQUET_FLOWERS.reduce((sum, f) => {
    return sum + (f.price * (flowerCounts[f.id] || 0));
  }, 0);
  const arrangementFee = totalStems > 0 ? 149 : 0;
  const bouquetTotal = stemSubtotal + arrangementFee;

  const updateFlowerCount = (id, delta) => {
    setFlowerCounts(prev => {
      const current = prev[id] || 0;
      const next = Math.max(0, current + delta);
      return { ...prev, [id]: next };
    });
  };

  const handleAddBouquetToCart = () => {
    if (totalStems === 0) {
      onShowToast('Please pick at least 1 flower stem for your bouquet!', '!');
      return;
    }

    // Build breakdown description
    const stemBreakdown = BOUQUET_FLOWERS
      .filter(f => flowerCounts[f.id] > 0)
      .map(f => `${flowerCounts[f.id]}x ${f.name}`)
      .join(', ');

    const wrapObj = WRAPPING_PAPERS.find(w => w.id === selectedWrapping);
    const ribObj = RIBBON_COLORS.find(r => r.id === selectedRibbon);

    const customBouquetProduct = {
      id: `custom-bouquet-${Date.now()}`,
      name: `Custom Bouquet (${totalStems} Stems)`,
      price: bouquetTotal,
      image: '/assets/images/tulip-bouquet.jpg',
      category: 'floral',
      categoryName: 'Custom Floral Studio',
      badge: 'Custom Made'
    };

    const customNote = `Composition: ${stemBreakdown} | Wrapping: ${wrapObj?.name || 'Pastel'} | Ribbon: ${ribObj?.name || 'Silk'}${bouquetCardMessage ? ` | Card: "${bouquetCardMessage}"` : ''}`;

    addToCart(customBouquetProduct, `${wrapObj?.name || 'Pastel'} Paper`, 1, customNote);
    onShowToast(`Custom bouquet added to bag! 💐`, '✓');
    onClose();
    if (onOpenCart) onOpenCart();
  };

  // Commission Calculations
  const selectedComm = COMMISSION_CATEGORIES.find(c => c.id === commType) || COMMISSION_CATEGORIES[0];
  const commissionTotal = selectedComm.basePrice + (commInitials.trim() ? 99 : 0);

  const handleAddCommissionToCart = () => {
    const itemTitle = commTitle.trim() || selectedComm.name;
    const customCommProduct = {
      id: `custom-comm-${Date.now()}`,
      name: `Artisanal Commission: ${itemTitle}`,
      price: commissionTotal,
      image: commType === 'cardigan' ? '/assets/images/crochet-cardigan.jpg' :
             commType === 'bag' ? '/assets/images/granny-bag.jpg' :
             commType === 'charm' ? '/assets/images/strawberry-keychain.jpg' :
             '/assets/images/strawberry-cow.jpg',
      category: 'accessories',
      categoryName: 'Bespoke Commission',
      badge: 'Made to Order'
    };

    const customNote = `Palette: ${commColors} | Initials: ${commInitials || 'None'} | Notes: ${commNotes || 'Standard studio design'} | Lead: ${selectedComm.leadDays}`;

    addToCart(customCommProduct, commColors, 1, customNote);
    onShowToast(`Custom commission reserved! ✨`, '✓');
    onClose();
    if (onOpenCart) onOpenCart();
  };

  return (
    <div 
      className="modal-overlay active" 
      onClick={(e) => {
        if (e.target.classList.contains('modal-overlay')) onClose();
      }}
    >
      <div className="modal-card custom-studio-modal" role="dialog" aria-modal="true" aria-label="Artisanal Maker Studio">
        <button className="modal-close-btn" onClick={onClose} aria-label="Close Studio">&times;</button>

        {/* Modal Header */}
        <div className="studio-header">
          <div className="studio-badge">
            <IconSparkles size={14} />
            <span>Artisanal Maker Studio</span>
          </div>
          <h2 className="studio-title">Design Your Handcrafted Piece</h2>
          <p className="studio-subtitle">
            Choose your blooms for an everlasting bouquet or commission a one-of-a-kind crochet friend.
          </p>

          {/* Mode Switcher Tabs */}
          <div className="studio-tabs">
            <button 
              className={`studio-tab-btn ${activeTab === 'bouquet' ? 'active' : ''}`}
              onClick={() => setActiveTab('bouquet')}
            >
              <IconFlower size={16} />
              <span>Bouquet Builder (Mix & Match)</span>
            </button>
            <button 
              className={`studio-tab-btn ${activeTab === 'commission' ? 'active' : ''}`}
              onClick={() => setActiveTab('commission')}
            >
              <IconSparkles size={16} />
              <span>Custom Commission Studio</span>
            </button>
          </div>
        </div>

        {/* TAB 1: BOUQUET BUILDER */}
        {activeTab === 'bouquet' && (
          <div className="studio-body">
            <div className="studio-columns">
              {/* Left Column: Stem Selector */}
              <div className="studio-col-left">
                <h4 className="studio-section-heading">1. Select Your Flower Stems</h4>
                <div className="stems-grid">
                  {BOUQUET_FLOWERS.map(flower => {
                    const count = flowerCounts[flower.id] || 0;
                    return (
                      <div key={flower.id} className={`stem-card ${count > 0 ? 'selected' : ''}`}>
                        <div className="stem-emoji-avatar" style={{ backgroundColor: `${flower.color}25` }}>
                          <span style={{ fontSize: '1.4rem' }}>{flower.emoji}</span>
                        </div>
                        <div className="stem-info">
                          <h5 className="stem-name">{flower.name}</h5>
                          <span className="stem-price">{formatPrice(flower.price)} / stem</span>
                        </div>
                        <div className="qty-stepper stem-stepper">
                          <button className="qty-btn" onClick={() => updateFlowerCount(flower.id, -1)}>-</button>
                          <span className="qty-value">{count}</span>
                          <button className="qty-btn" onClick={() => updateFlowerCount(flower.id, 1)}>+</button>
                        </div>
                      </div>
                    );
                  })}
                </div>

                {/* Wrapping Paper Selector */}
                <h4 className="studio-section-heading" style={{ marginTop: '1.5rem' }}>2. Choose Wrapping Paper</h4>
                <div className="swatch-options-row">
                  {WRAPPING_PAPERS.map(wrap => (
                    <button
                      key={wrap.id}
                      className={`studio-swatch-chip ${selectedWrapping === wrap.id ? 'active' : ''}`}
                      onClick={() => setSelectedWrapping(wrap.id)}
                    >
                      <span className="swatch-dot" style={{ backgroundColor: wrap.color }} />
                      <span>{wrap.name}</span>
                    </button>
                  ))}
                </div>

                {/* Ribbon Color Selector */}
                <h4 className="studio-section-heading" style={{ marginTop: '1.5rem' }}>3. Silk Ribbon Color</h4>
                <div className="swatch-options-row">
                  {RIBBON_COLORS.map(rib => (
                    <button
                      key={rib.id}
                      className={`studio-swatch-chip ${selectedRibbon === rib.id ? 'active' : ''}`}
                      onClick={() => setSelectedRibbon(rib.id)}
                    >
                      <span className="swatch-dot" style={{ backgroundColor: rib.color }} />
                      <span>{rib.name}</span>
                    </button>
                  ))}
                </div>

                {/* Card Message */}
                <div className="form-group" style={{ marginTop: '1.5rem' }}>
                  <label className="form-label" htmlFor="cardMsg">
                    4. Handwritten Gift Note (Optional):
                  </label>
                  <input
                    type="text"
                    id="cardMsg"
                    className="form-input"
                    placeholder="e.g. Happy Birthday sweet girl! Keep blooming 🌸"
                    value={bouquetCardMessage}
                    onChange={(e) => setBouquetCardMessage(e.target.value)}
                  />
                </div>
              </div>

              {/* Right Column: Live Bouquet Preview & Checkout */}
              <div className="studio-col-right">
                <div className="studio-summary-card">
                  <h4>Your Custom Bouquet</h4>
                  <div className="bouquet-visual-preview">
                    <div className="bouquet-stems-preview">
                      {totalStems === 0 ? (
                        <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '1.5rem 0' }}>
                          <span style={{ fontSize: '2.5rem' }}>💐</span>
                          <p style={{ fontSize: '0.85rem', marginTop: '0.5rem' }}>No flowers selected yet. Pick your stems!</p>
                        </div>
                      ) : (
                        <div className="stem-tags-list">
                          {BOUQUET_FLOWERS.filter(f => flowerCounts[f.id] > 0).map(f => (
                            <div key={f.id} className="stem-tag-pill">
                              <span>{f.emoji}</span>
                              <span>{flowerCounts[f.id]}x {f.name}</span>
                              <span style={{ marginLeft: 'auto', fontWeight: 600 }}>
                                {formatPrice(f.price * flowerCounts[f.id])}
                              </span>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>

                    <div className="bouquet-attributes-list">
                      <div className="attr-row">
                        <span>Total Stems:</span>
                        <strong>{totalStems} {totalStems === 1 ? 'stem' : 'stems'}</strong>
                      </div>
                      <div className="attr-row">
                        <span>Wrapping:</span>
                        <strong>{WRAPPING_PAPERS.find(w => w.id === selectedWrapping)?.name}</strong>
                      </div>
                      <div className="attr-row">
                        <span>Silk Ribbon:</span>
                        <strong>{RIBBON_COLORS.find(r => r.id === selectedRibbon)?.name}</strong>
                      </div>
                      {arrangementFee > 0 && (
                        <div className="attr-row">
                          <span>Studio Wrapping & Arrangement:</span>
                          <span style={{ color: 'var(--primary)' }}>{formatPrice(arrangementFee)}</span>
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="studio-total-row">
                    <div>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block' }}>Estimated Price</span>
                      <strong style={{ fontSize: '1.4rem', color: 'var(--text-dark)', fontFamily: 'var(--font-serif)' }}>
                        {formatPrice(bouquetTotal)}
                      </strong>
                    </div>
                    <button 
                      className="btn btn-primary"
                      disabled={totalStems === 0}
                      onClick={handleAddBouquetToCart}
                      style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', padding: '0.75rem 1.4rem' }}
                    >
                      <IconBag size={16} />
                      <span>Add Bouquet to Bag</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: CUSTOM COMMISSION STUDIO */}
        {activeTab === 'commission' && (
          <div className="studio-body">
            <div className="studio-columns">
              <div className="studio-col-left">
                <h4 className="studio-section-heading">1. Select Creation Category</h4>
                <div className="stems-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
                  {COMMISSION_CATEGORIES.map(cat => (
                    <div 
                      key={cat.id} 
                      className={`stem-card ${commType === cat.id ? 'selected' : ''}`}
                      onClick={() => setCommType(cat.id)}
                      style={{ cursor: 'pointer' }}
                    >
                      <div className="stem-emoji-avatar" style={{ backgroundColor: '#FFF0F4' }}>
                        <span style={{ fontSize: '1.5rem' }}>{cat.icon}</span>
                      </div>
                      <div className="stem-info">
                        <h5 className="stem-name">{cat.name}</h5>
                        <span className="stem-price">Starts at {formatPrice(cat.basePrice)} • {cat.leadDays}</span>
                      </div>
                      {commType === cat.id && (
                        <span style={{ color: 'var(--primary)', marginLeft: 'auto' }}>
                          <IconCheck size={18} />
                        </span>
                      )}
                    </div>
                  ))}
                </div>

                <div className="form-group" style={{ marginTop: '1.5rem' }}>
                  <label className="form-label" htmlFor="commTitle">2. Creation Name / Idea:</label>
                  <input
                    type="text"
                    id="commTitle"
                    className="form-input"
                    placeholder="e.g. Lavender Lop-Eared Bunny with Frog Hat"
                    value={commTitle}
                    onChange={(e) => setCommTitle(e.target.value)}
                  />
                </div>

                <div className="form-group" style={{ marginTop: '1rem' }}>
                  <label className="form-label" htmlFor="commColors">3. Preferred Yarn Color Palette:</label>
                  <input
                    type="text"
                    id="commColors"
                    className="form-input"
                    placeholder="e.g. Pastel Pink, Cream White & Forest Matcha"
                    value={commColors}
                    onChange={(e) => setCommColors(e.target.value)}
                  />
                </div>

                <div className="form-group" style={{ marginTop: '1rem' }}>
                  <label className="form-label" htmlFor="commInitials">4. Embroidered Monogram / Initial (+₹99, Optional):</label>
                  <input
                    type="text"
                    id="commInitials"
                    className="form-input"
                    placeholder="e.g. Initial 'A' stitched on front"
                    maxLength={10}
                    value={commInitials}
                    onChange={(e) => setCommInitials(e.target.value)}
                  />
                </div>

                <div className="form-group" style={{ marginTop: '1rem' }}>
                  <label className="form-label" htmlFor="commNotes">5. Special Instructions or Sizing Notes:</label>
                  <textarea
                    id="commNotes"
                    className="form-input"
                    rows={3}
                    placeholder="Tell us any specific dimensions, eye colors, or details you'd love!"
                    value={commNotes}
                    onChange={(e) => setCommNotes(e.target.value)}
                  />
                </div>
              </div>

              {/* Commission Summary */}
              <div className="studio-col-right">
                <div className="studio-summary-card">
                  <h4>Commission Summary</h4>
                  <div className="bouquet-visual-preview">
                    <div className="attr-row">
                      <span>Base Category:</span>
                      <strong>{selectedComm.name}</strong>
                    </div>
                    <div className="attr-row">
                      <span>Handmade Lead Time:</span>
                      <strong style={{ color: 'var(--primary)' }}>{selectedComm.leadDays}</strong>
                    </div>
                    <div className="attr-row">
                      <span>Palette:</span>
                      <strong>{commColors || 'Custom Pastel'}</strong>
                    </div>
                    {commInitials.trim() && (
                      <div className="attr-row">
                        <span>Personalized Monogram:</span>
                        <span>+₹99</span>
                      </div>
                    )}
                    <div className="attr-row">
                      <span>Artisanal Packaging:</span>
                      <span style={{ color: 'var(--success)' }}>Complimentary</span>
                    </div>
                  </div>

                  <div className="studio-total-row">
                    <div>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block' }}>Estimated Price</span>
                      <strong style={{ fontSize: '1.4rem', color: 'var(--text-dark)', fontFamily: 'var(--font-serif)' }}>
                        {formatPrice(commissionTotal)}
                      </strong>
                    </div>
                    <button 
                      className="btn btn-primary"
                      onClick={handleAddCommissionToCart}
                      style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', padding: '0.75rem 1.4rem' }}
                    >
                      <IconSparkles size={16} />
                      <span>Reserve Custom Slot</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
