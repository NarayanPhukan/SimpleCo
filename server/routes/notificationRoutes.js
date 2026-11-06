const express = require('express');
const router = express.Router();
const notificationController = require('../controllers/notificationController');

// List notifications (with role/userEmail query parameters)
router.get('/', notificationController.getNotifications);

// Create / broadcast notification
router.post('/', notificationController.createNotification);

// Mark all as read
router.post('/read-all', notificationController.markAllAsRead);
router.put('/read-all', notificationController.markAllAsRead);

// Mark individual notification as read
router.patch('/:id/read', notificationController.markAsRead);
router.put('/:id/read', notificationController.markAsRead);

// Delete notification
router.delete('/:id', notificationController.deleteNotification);

module.exports = router;
