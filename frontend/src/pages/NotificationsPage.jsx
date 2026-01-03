import React, { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import { Trash2, Check, CheckCheck, Filter } from 'lucide-react';
import notificationService from '../services/notificationService';
import Button from '../components/ui/Button';

const NotificationsPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all'); // all, unread, read

  useEffect(() => {
    fetchNotifications();
    // Auto-refresh every 30 seconds
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, [filter]);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      let data;
      if (filter === 'unread') {
        data = await notificationService.getUnreadNotifications();
      } else if (filter === 'read') {
        const allNotifications = await notificationService.getNotifications();
        data = allNotifications.filter(n => n.isRead);
      } else {
        data = await notificationService.getNotifications();
      }
      setNotifications(data);
    } catch (error) {
      console.error('Error fetching notifications:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (notificationId) => {
    try {
      await notificationService.markAsRead(notificationId);
      setNotifications(notifications.map(n =>
        n.id === notificationId ? { ...n, isRead: true } : n
      ));
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  };

  const handleDelete = async (notificationId) => {
    try {
      await notificationService.deleteNotification(notificationId);
      setNotifications(notifications.filter(n => n.id !== notificationId));
    } catch (error) {
      console.error('Error deleting notification:', error);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications(notifications.map(n => ({ ...n, isRead: true })));
    } catch (error) {
      console.error('Error marking all as read:', error);
    }
  };

  const handleDeleteAll = async () => {
    if (window.confirm('Delete all notifications? This cannot be undone.')) {
      try {
        await notificationService.deleteAllNotifications();
        setNotifications([]);
      } catch (error) {
        console.error('Error deleting all notifications:', error);
      }
    }
  };

  const getNotificationIcon = (type) => {
    const icons = {
      GRADE_RELEASED: '🎓',
      DEADLINE_CREATED: '📅',
      DOCUMENT_APPROVED: '✅',
      REVISION_REQUESTED: '📝',
      DOCUMENT_UPLOADED: '📤',
      DOCUMENT_RESUBMITTED: '🔄',
      COMMITTEE_REVISION_REQUESTED: '⚠️',
      GRADES_RELEASED: '📊',
      DOCUMENT_RESUBMITTED_FOR_REVIEW: '📬',
      GRADES_COMPLETED: '🏆',
      GENERAL: '📢'
    };
    return icons[type] || '📬';
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;

    return date.toLocaleDateString();
  };

  const unreadCount = notifications.filter(n => !n.isRead).length;

  return (
    <div className="space-y-6 animate-fade-in p-6">
      <div className="flex items-center justify-between">
        <h2 className="text-3xl font-bold text-slate-800">Notifications</h2>
        {notifications.length > 0 && (
          <div className="flex gap-2">
            <Button
              onClick={handleMarkAllAsRead}
              variant="outline"
              className="flex items-center gap-2"
            >
              <CheckCheck className="h-4 w-4" />
              Mark all read
            </Button>
            <Button
              onClick={handleDeleteAll}
              variant="danger"
              className="flex items-center gap-2"
            >
              <Trash2 className="h-4 w-4" />
              Clear all
            </Button>
          </div>
        )}
      </div>

      {/* Filter Tabs */}
      <div className="flex gap-2 border-b border-slate-200">
        <button
          onClick={() => setFilter('all')}
          className={`px-4 py-3 font-medium border-b-2 transition-colors ${
            filter === 'all'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-slate-600 hover:text-slate-900'
          }`}
        >
          All
        </button>
        <button
          onClick={() => setFilter('unread')}
          className={`px-4 py-3 font-medium border-b-2 transition-colors ${
            filter === 'unread'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-slate-600 hover:text-slate-900'
          }`}
        >
          Unread {unreadCount > 0 && `(${unreadCount})`}
        </button>
        <button
          onClick={() => setFilter('read')}
          className={`px-4 py-3 font-medium border-b-2 transition-colors ${
            filter === 'read'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-slate-600 hover:text-slate-900'
          }`}
        >
          Read
        </button>
      </div>

      {/* Notifications List */}
      {loading ? (
        <Card>
          <CardContent className="flex items-center justify-center h-32">
            <p className="text-slate-600">Loading notifications...</p>
          </CardContent>
        </Card>
      ) : notifications.length === 0 ? (
        <Card>
          <CardContent className="flex items-center justify-center h-32">
            <p className="text-slate-500">No notifications</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {notifications.map((notification) => (
            <Card
              key={notification.id}
              className={`${
                !notification.isRead ? 'bg-blue-50 border-blue-200' : ''
              }`}
            >
              <CardContent className="p-4">
                <div className="flex gap-4 items-start">
                  {/* Icon */}
                  <div className="text-3xl flex-shrink-0">
                    {getNotificationIcon(notification.type)}
                  </div>

                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-slate-900">
                      {notification.message}
                    </p>
                    <div className="flex items-center gap-4 mt-2">
                      <p className="text-xs text-slate-500">
                        {formatDate(notification.createdAt)}
                      </p>
                      <span className={`text-xs px-2 py-1 rounded-full ${
                        !notification.isRead
                          ? 'bg-blue-100 text-blue-700'
                          : 'bg-slate-100 text-slate-600'
                      }`}>
                        {!notification.isRead ? 'Unread' : 'Read'}
                      </span>
                      <span className="text-xs text-slate-400 uppercase font-medium">
                        {notification.type.replace(/_/g, ' ')}
                      </span>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex gap-2 flex-shrink-0">
                    {!notification.isRead && (
                      <button
                        onClick={() => handleMarkAsRead(notification.id)}
                        className="p-2 text-blue-600 hover:bg-blue-100 rounded transition-colors"
                        title="Mark as read"
                      >
                        <Check className="h-5 w-5" />
                      </button>
                    )}
                    <button
                      onClick={() => handleDelete(notification.id)}
                      className="p-2 text-red-600 hover:bg-red-100 rounded transition-colors"
                      title="Delete"
                    >
                      <Trash2 className="h-5 w-5" />
                    </button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default NotificationsPage;
