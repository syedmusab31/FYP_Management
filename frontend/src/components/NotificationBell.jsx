import React, { useState, useEffect } from 'react';
import { Bell } from 'lucide-react';
import notificationService from '../services/notificationService';

const NotificationBell = ({ onOpenPanel }) => {
    const [unreadCount, setUnreadCount] = useState(0);

    useEffect(() => {
        const fetchUnreadCount = async () => {
            try {
                const count = await notificationService.getUnreadCount();
                setUnreadCount(count || 0);
            } catch (error) {
                console.error('Error fetching unread count:', error);
            }
        };

        fetchUnreadCount();

        // Refresh unread count every 30 seconds
        const interval = setInterval(fetchUnreadCount, 30000);
        return () => clearInterval(interval);
    }, []);

    return (
        <button
            onClick={onOpenPanel}
            className="relative p-2 text-slate-400 hover:text-white transition-colors focus:outline-none"
            aria-label="Notifications"
        >
            <Bell className="h-6 w-6" />
            {unreadCount > 0 && (
                <span className="absolute top-1 right-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-600 text-[10px] font-bold text-white shadow-sm ring-2 ring-slate-900 animate-pulse">
                    {unreadCount > 9 ? '9+' : unreadCount}
                </span>
            )}
        </button>
    );
};

export default NotificationBell;
