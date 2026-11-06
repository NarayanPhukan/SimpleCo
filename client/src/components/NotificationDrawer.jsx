import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { IconClose } from './Icons';

export const NotificationDrawer = ({ isOpen, onClose, onShowToast, onOpenOrder }) => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [activeFilter, setActiveFilter] = useState('all');

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const email = user?.email || 'guest@babyygurll.in';
      const res = await api.getNotifications({ userEmail: email });
      if (res.success) {
        setNotifications(res.data || []);
        setUnreadCount(res.unreadCount || 0);
      }
    } catch (e) {
      console.warn('Failed to load notifications:', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchNotifications();
    }
  }, [isOpen, user]);

  const handleMarkRead = async (id) => {
    try {
      await api.markNotificationRead(id);
      setNotifications(prev => prev.map(n => n._id === id || n.id === id ? { ...n, isRead: true } : n));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (e) {
      console.warn(e);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      const email = user?.email || 'guest@babyygurll.in';
      await api.markAllNotificationsRead({ userEmail: email, role: 'customer' });
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      setUnreadCount(0);
      onShowToast('All notifications marked as read ✓');
    } catch (e) {
      onShowToast('Failed to mark all as read', '!');
    }
  };

  if (!isOpen) return null;

  const filteredList = notifications.filter(n => {
    if (activeFilter === 'orders') return n.type === 'order' || n.type === 'shipping';
    if (activeFilter === 'news') return n.type === 'promotion' || n.type === 'broadcast' || n.type === 'general';
    return true;
  });

  const formatRelativeTime = (dateStr) => {
    if (!dateStr) return 'Just now';
    try {
      const date = new Date(dateStr);
      const diffSec = Math.floor((Date.now() - date.getTime()) / 1000);
      if (diffSec < 60) return 'Just now';
      if (diffSec < 3600) return `${Math.floor(diffSec / 60)}m ago`;
      if (diffSec < 86400) return `${Math.floor(diffSec / 3600)}h ago`;
      return date.toLocaleDateString('en-IN', { month: 'short', day: 'numeric' });
    } catch (e) {
      return 'Recent';
    }
  };

  return (
    <div 
      className={`drawer-backdrop ${isOpen ? 'active' : ''}`}
      onClick={(e) => {
        if (e.target.classList.contains('drawer-backdrop')) onClose();
      }}
      style={{ zIndex: 100000 }}
    >
      <div className="cart-drawer" role="dialog" aria-modal="true" aria-label="Notifications" style={{ width: '420px', maxWidth: '92vw', display: 'flex', flexDirection: 'column' }}>
        {/* Header */}
        <div style={{ padding: '1.25rem 1.5rem', borderBottom: '1px solid #E2E8F0', display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: '#FFFFFF' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <h3 style={{ margin: 0, fontSize: '1.15rem', fontWeight: 800, color: '#0F172A' }}>
                Boutique Alerts
              </h3>
              {unreadCount > 0 && (
                <span style={{ background: '#2563EB', color: '#FFFFFF', fontSize: '0.72rem', fontWeight: 700, padding: '2px 8px', borderRadius: '12px' }}>
                  {unreadCount} new
                </span>
              )}
            </div>
            <p style={{ margin: '0.15rem 0 0', fontSize: '0.78rem', color: '#64748B' }}>
              Order progress, dispatch updates &amp; drops
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            {unreadCount > 0 && (
              <button
                type="button"
                onClick={handleMarkAllRead}
                style={{ background: 'none', border: 'none', fontSize: '0.78rem', fontWeight: 700, color: '#2563EB', cursor: 'pointer', padding: '4px 8px' }}
                title="Mark all as read"
              >
                Mark read
              </button>
            )}
            <button
              type="button"
              onClick={onClose}
              style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748B', display: 'flex', alignItems: 'center', padding: '4px' }}
              aria-label="Close"
            >
              <IconClose size={20} />
            </button>
          </div>
        </div>

        {/* Filter Pills */}
        <div style={{ display: 'flex', gap: '8px', padding: '0.75rem 1.5rem', background: '#F8FAFC', borderBottom: '1px solid #E2E8F0' }}>
          {['all', 'orders', 'news'].map(f => (
            <button
              key={f}
              type="button"
              onClick={() => setActiveFilter(f)}
              style={{
                border: 'none',
                background: activeFilter === f ? '#0F172A' : '#FFFFFF',
                color: activeFilter === f ? '#FFFFFF' : '#64748B',
                fontSize: '0.78rem',
                fontWeight: activeFilter === f ? 700 : 500,
                padding: '4px 12px',
                borderRadius: '16px',
                cursor: 'pointer',
                boxShadow: activeFilter === f ? 'none' : '0 1px 2px rgba(0,0,0,0.05)'
              }}
            >
              {f === 'all' ? `All (${notifications.length})` :
               f === 'orders' ? `Orders (${notifications.filter(n => n.type === 'order' || n.type === 'shipping').length})` :
               `News (${notifications.filter(n => n.type === 'promotion' || n.type === 'broadcast' || n.type === 'general').length})`}
            </button>
          ))}
        </div>

        {/* Notification List */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '1rem 1.25rem', display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {loading && notifications.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3rem 1rem', color: '#94A3B8', fontSize: '0.88rem' }}>
              Loading alerts...
            </div>
          ) : filteredList.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3.5rem 1.5rem' }}>
              <div style={{ width: '56px', height: '56px', background: '#EEF2FF', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1rem', fontSize: '1.6rem' }}>
                🌸
              </div>
              <h4 style={{ margin: 0, fontSize: '1.05rem', fontWeight: 700, color: '#0F172A' }}>
                All caught up!
              </h4>
              <p style={{ margin: '0.4rem 0 0', fontSize: '0.82rem', color: '#64748B', lineHeight: '1.5' }}>
                You have no notifications right now. Order stage progress and boutique drops will appear here.
              </p>
            </div>
          ) : (
            filteredList.map(notif => {
              const id = notif._id || notif.id;
              const isOrder = notif.type === 'order' || notif.type === 'shipping';
              const orderId = notif.data?.orderId || notif.data?.orderNumber;

              return (
                <div
                  key={id}
                  onClick={() => !notif.isRead && handleMarkRead(id)}
                  style={{
                    background: notif.isRead ? '#FFFFFF' : '#F0F9FF',
                    border: `1px solid ${notif.isRead ? '#E2E8F0' : '#BAE6FD'}`,
                    borderRadius: '12px',
                    padding: '12px 14px',
                    position: 'relative',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '8px', marginBottom: '4px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{
                        fontSize: '0.68rem',
                        fontWeight: 700,
                        textTransform: 'uppercase',
                        padding: '2px 6px',
                        borderRadius: '6px',
                        background: isOrder ? '#DCFCE7' : '#FDF2F8',
                        color: isOrder ? '#166534' : '#9D174D'
                      }}>
                        {isOrder ? 'Order' : 'News'}
                      </span>
                      <span style={{ fontSize: '0.72rem', color: '#94A3B8' }}>
                        {formatRelativeTime(notif.createdAt)}
                      </span>
                    </div>

                    {!notif.isRead && (
                      <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#2563EB', display: 'inline-block' }} />
                    )}
                  </div>

                  <h5 style={{ margin: '0 0 3px', fontSize: '0.92rem', fontWeight: 700, color: '#0F172A' }}>
                    {notif.title}
                  </h5>

                  <p style={{ margin: 0, fontSize: '0.82rem', color: '#475569', lineHeight: '1.45' }}>
                    {notif.message}
                  </p>

                  {orderId && (
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        if (onOpenOrder) onOpenOrder(orderId);
                        onClose();
                      }}
                      style={{
                        marginTop: '8px',
                        background: '#EEF2FF',
                        border: 'none',
                        color: '#2563EB',
                        fontSize: '0.78rem',
                        fontWeight: 700,
                        padding: '4px 10px',
                        borderRadius: '6px',
                        cursor: 'pointer',
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: '4px'
                      }}
                    >
                      Track Order #{orderId} →
                    </button>
                  )}
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
};
