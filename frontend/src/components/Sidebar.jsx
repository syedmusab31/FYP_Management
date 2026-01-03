import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LayoutDashboard, FileText, CheckSquare, GraduationCap, LogOut, Users, Clock, Bell } from 'lucide-react';
import { cn } from '../utils/cn';
import NotificationBell from './NotificationBell';
import NotificationPanel from './NotificationPanel';

const Sidebar = () => {
    const { user, logout } = useAuth();
    const [isNotificationPanelOpen, setIsNotificationPanelOpen] = useState(false);

    if (!user) return null;

    const roleId = user.role?.id;

    const links = [
        { to: `/dashboard`, icon: LayoutDashboard, label: 'Dashboard', roles: [1, 2, 3, 4] },
        { to: '/documents', icon: FileText, label: 'Documents', roles: [1, 2, 3] },
        { to: '/groups', icon: Users, label: 'Groups', roles: [4] },
        { to: '/reviews', icon: CheckSquare, label: 'Reviews', roles: [2] },
        { to: '/grades', icon: GraduationCap, label: 'Grades', roles: [1, 3, 4] },
        { to: '/deadlines', icon: Clock, label: 'Deadlines', roles: [4] },
        { to: '/notifications', icon: Bell, label: 'Notifications', roles: [1, 2, 3, 4] },
    ];

    const filteredLinks = links.filter(link => link.roles.includes(roleId));

    return (
        <aside className="w-64 bg-slate-900 text-white min-h-screen flex flex-col shadow-xl fixed left-0 top-0 bottom-0 z-50">
            <div className="p-6 border-b border-slate-800 flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-wider text-blue-500">FYP MS</h1>
                    <p className="text-xs text-slate-400 mt-1 uppercase tracking-widest">{user.role?.name?.replace('_', ' ') || 'User'}</p>
                </div>
                <NotificationBell onOpenPanel={() => setIsNotificationPanelOpen(true)} />
            </div>

            <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
                {filteredLinks.map((link) => (
                    <NavLink
                        key={link.to}
                        to={link.to}
                        className={({ isActive }) => cn(
                            "flex items-center space-x-3 px-4 py-3 rounded-none transition-all duration-200 border-l-4 group",
                            isActive
                                ? "bg-slate-800 border-blue-500 text-blue-400"
                                : "border-transparent hover:bg-slate-800/50 text-slate-300 hover:text-white"
                        )}
                    >
                        <link.icon className="h-5 w-5 group-hover:scale-110 transition-transform" />
                        <span className="font-medium">{link.label}</span>
                    </NavLink>
                ))}
            </nav>

            <div className="p-4 border-t border-slate-800 bg-slate-900">
                <div className="flex items-center space-x-3 px-4 py-3 mb-2">
                    <div className="h-8 w-8 rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center font-bold text-sm text-white shadow-lg">
                        {user.fullName ? user.fullName.charAt(0) : 'U'}
                    </div>
                    <div className="flex-1 overflow-hidden">
                        <p className="text-sm font-medium truncate">{user.fullName}</p>
                        <p className="text-xs text-slate-400 truncate">{user.email}</p>
                    </div>
                </div>
                <button
                    onClick={logout}
                    className="w-full flex items-center space-x-3 px-4 py-2 text-slate-400 hover:text-red-400 hover:bg-slate-800/50 transition-colors rounded-none"
                >
                    <LogOut className="h-5 w-5" />
                    <span>Logout</span>
                </button>
            </div>

            <NotificationPanel 
                isOpen={isNotificationPanelOpen} 
                onClose={() => setIsNotificationPanelOpen(false)} 
            />
        </aside>
    );
};

export default Sidebar;
