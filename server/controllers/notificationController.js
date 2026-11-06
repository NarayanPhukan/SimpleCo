const Notification = require('../models/Notification');

// Internal helper to create notifications directly from server-side events
exports.createNotificationInternal = async ({ recipient, role, title, message, type = 'general', data = {} }) => {
  try {
    const finalRole = role || (recipient === 'admin' ? 'admin' : 'customer');
    const finalRecipient = (recipient || (finalRole === 'admin' ? 'admin' : 'all')).trim().toLowerCase();

    const notif = await Notification.create({
      recipient: finalRecipient,
      role: finalRole,
      title,
      message,
      type,
      data,
      isRead: false,
      createdAt: new Date()
    });
    return notif;
  } catch (err) {
    console.warn('[Notification] Failed to create internal notification:', err.message);
    return null;
  }
};

// @desc    Get notifications for user or admin
// @route   GET /api/notifications
exports.getNotifications = async (req, res) => {
  try {
    const { role, userEmail, limit = 50 } = req.query;
    let query = {};

    if (role === 'admin') {
      query = {
        $or: [
          { role: 'admin' },
          { recipient: 'admin' }
        ]
      };
    } else if (userEmail) {
      const email = userEmail.trim().toLowerCase();
      query = {
        $or: [
          { recipient: email },
          { recipient: 'all' },
          { role: 'all' }
        ]
      };
    } else {
      query = {
        $or: [
          { recipient: 'all' },
          { role: 'customer' },
          { role: 'all' }
        ]
      };
    }

    const docs = await Notification.find(query);
    const sorted = (docs || []).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    const limited = sorted.slice(0, parseInt(limit, 10) || 50);
    const unreadCount = sorted.filter(n => !n.isRead).length;

    res.json({
      success: true,
      count: limited.length,
      unreadCount,
      data: limited
    });
  } catch (error) {
    console.error('Error fetching notifications:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Create a new notification (Admin broadcast or targeted)
// @route   POST /api/notifications
exports.createNotification = async (req, res) => {
  try {
    const { recipient, role, title, message, type = 'general', data = {} } = req.body;
    if (!title || !message) {
      return res.status(400).json({ success: false, message: 'Title and message are required' });
    }

    const finalRole = role || (recipient === 'admin' ? 'admin' : 'customer');
    const finalRecipient = (recipient || (finalRole === 'admin' ? 'admin' : 'all')).trim().toLowerCase();

    const notif = await Notification.create({
      recipient: finalRecipient,
      role: finalRole,
      title: title.trim(),
      message: message.trim(),
      type,
      data,
      isRead: false,
      createdAt: new Date()
    });

    res.status(201).json({ success: true, data: notif });
  } catch (error) {
    console.error('Error creating notification:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Mark single notification as read
// @route   PATCH /api/notifications/:id/read
exports.markAsRead = async (req, res) => {
  try {
    const { id } = req.params;
    let notif = await Notification.findById(id);
    if (!notif) {
      notif = await Notification.findOne({ _id: id });
    }
    if (!notif) {
      return res.status(404).json({ success: false, message: 'Notification not found' });
    }

    notif.isRead = true;
    notif.readAt = new Date();
    await notif.save();

    res.json({ success: true, data: notif });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Mark all notifications as read for current role / user
// @route   POST /api/notifications/read-all
exports.markAllAsRead = async (req, res) => {
  try {
    const { role, userEmail } = req.body;
    let query = { isRead: false };

    if (role === 'admin') {
      query.$or = [{ role: 'admin' }, { recipient: 'admin' }];
    } else if (userEmail) {
      const email = userEmail.trim().toLowerCase();
      query.$or = [{ recipient: email }, { recipient: 'all' }, { role: 'all' }];
    }

    const docs = await Notification.find(query);
    const now = new Date();
    for (const doc of docs) {
      doc.isRead = true;
      doc.readAt = now;
      await doc.save();
    }

    res.json({ success: true, message: 'All notifications marked as read', count: docs.length });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// @desc    Delete a notification
// @route   DELETE /api/notifications/:id
exports.deleteNotification = async (req, res) => {
  try {
    const { id } = req.params;
    let deleted = await Notification.findByIdAndDelete(id);
    if (!deleted) {
      deleted = await Notification.findOneAndDelete({ _id: id });
    }
    if (!deleted) {
      return res.status(404).json({ success: false, message: 'Notification not found' });
    }
    res.json({ success: true, message: 'Notification removed successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
