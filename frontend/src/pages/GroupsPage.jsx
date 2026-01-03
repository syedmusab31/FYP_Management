import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { Users, Plus, Edit2, Trash2, UserPlus, UserMinus, Shield, AlertCircle } from 'lucide-react';

const GroupsPage = () => {
    const [groups, setGroups] = useState([]);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false);

    // Lists for dropdowns
    const [supervisors, setSupervisors] = useState([]);
    const [availableStudents, setAvailableStudents] = useState([]);

    // Modals state
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showMemberModal, setShowMemberModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);

    // Form data
    const [selectedGroup, setSelectedGroup] = useState(null);
    const [groupToDelete, setGroupToDelete] = useState(null);
    const [formData, setFormData] = useState({ groupName: '', projectTitle: '', projectDescription: '', supervisorId: '' });
    const [memberData, setMemberData] = useState({ userId: '' });

    useEffect(() => {
        fetchGroups();
    }, []);

    // Fetch lists when modals are opened
    useEffect(() => {
        if (showCreateModal || showEditModal) {
            fetchSupervisors();
        }
    }, [showCreateModal, showEditModal]);

    useEffect(() => {
        if (showMemberModal) {
            fetchAvailableStudents();
        }
    }, [showMemberModal]);

    const fetchGroups = async () => {
        try {
            const res = await api.get('/groups/details'); // Using /details to get full object including members
            setGroups(res.data);
        } catch (error) {
            console.error("Failed to fetch groups", error);
        } finally {
            setLoading(false);
        }
    };

    const fetchSupervisors = async () => {
        try {
            const res = await api.get('/users/supervisors');
            setSupervisors(res.data);
        } catch (error) {
            console.error("Failed to fetch supervisors", error);
        }
    };

    const fetchAvailableStudents = async () => {
        try {
            const res = await api.get('/users/students/available');
            setAvailableStudents(res.data);
        } catch (error) {
            console.error("Failed to fetch students", error);
        }
    };

    const handleCreateGroup = async (e) => {
        e.preventDefault();
        if (!formData.groupName || !formData.projectTitle || !formData.projectDescription || !formData.supervisorId) {
            alert("All fields are required, including Supervisor.");
            return;
        }
        if(formData.projectTitle.length < 5) {
            alert("Project Title must be at least 5 characters long.");
            return;
        }
        setActionLoading(true);
        try {
            await api.post('/groups', {
                ...formData,
                supervisorId: Number(formData.supervisorId)
            });
            setShowCreateModal(false);
            setFormData({ groupName: '', projectTitle: '', projectDescription: '', supervisorId: '' });
            fetchGroups();
        } catch (error) {
            alert(error.response?.data?.message || 'Failed to create group');
        } finally {
            setActionLoading(false);
        }
    };

    const handleUpdateGroup = async (e) => {
        e.preventDefault();
        setActionLoading(true);
        try {
            await api.put(`/groups/${selectedGroup.id}`, {
                ...formData,
                supervisorId: formData.supervisorId ? Number(formData.supervisorId) : null
            });
            setShowEditModal(false);
            fetchGroups();
        } catch (error) {
            alert(error.response?.data?.message || 'Failed to update group');
        } finally {
            setActionLoading(false);
        }
    };

    const handleDeleteGroup = async (group) => {
        setGroupToDelete(group);
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        if (!groupToDelete) return;

        setActionLoading(true);
        try {
            await api.delete(`/groups/${groupToDelete.id}`);
            setShowDeleteModal(false);
            setGroupToDelete(null);
            fetchGroups();
        } catch (error) {
            alert(error.response?.data?.message || 'Failed to delete group');
        } finally {
            setActionLoading(false);
        }
    };

    const cancelDelete = () => {
        setShowDeleteModal(false);
        setGroupToDelete(null);
    };

    const handleAddMember = async (e) => {
        e.preventDefault();
        if (!memberData.userId) return;

        // Frontend limit check (also enforce 4)
        if (selectedGroup && selectedGroup.members && selectedGroup.members.length >= 4) {
            alert("Maximum 4 students allowed per group.");
            return;
        }

        setActionLoading(true);
        try {
            await api.post(`/groups/${selectedGroup.id}/members`, { userId: Number(memberData.userId) });
            setMemberData({ userId: '' });

            // Re-fetch groups to get updated list
            const res = await api.get('/groups/details');
            setGroups(res.data);

            // Update selected group in place to reflect changes immediately in modal if needed, 
            // though re-fetching available students is also wise
            const updatedGroup = res.data.find(g => g.id === selectedGroup.id);
            setSelectedGroup(updatedGroup);
            fetchAvailableStudents(); // Refresh dropdown

        } catch (error) {
            alert(error.response?.data?.message || 'Failed to add member');
        } finally {
            setActionLoading(false);
        }
    };

    const handleRemoveMember = async (groupId, userId) => {
        if (!window.confirm("Remove this member?")) return;
        try {
            await api.delete(`/groups/${groupId}/members/${userId}`);

            // Re-fetch groups
            const res = await api.get('/groups/details');
            setGroups(res.data);

            // Update selected group if modal is open
            if (selectedGroup && selectedGroup.id === groupId) {
                const updatedGroup = res.data.find(g => g.id === groupId);
                setSelectedGroup(updatedGroup);
            }
            fetchAvailableStudents(); // Make them available again

        } catch (error) {
            alert(error.response?.data?.message || 'Failed to remove member');
        }
    };

    const openEditModal = (group) => {
        setSelectedGroup(group);
        setFormData({
            groupName: group.groupName,
            projectTitle: group.projectTitle,
            projectDescription: group.projectDescription,
            supervisorId: group.supervisor ? group.supervisor.id : ''
        });
        setShowEditModal(true);
    };

    const openMemberModal = (group) => {
        setSelectedGroup(group);
        setShowMemberModal(true);
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-3xl font-bold text-slate-800">Group Management</h2>
                <Button onClick={() => setShowCreateModal(true)}>
                    <Plus className="mr-2 h-4 w-4" /> Create Group
                </Button>
            </div>

            <div className="grid gap-6">
                {groups.map(group => (
                    <Card key={group.id} className="relative overflow-hidden">
                        <div className="absolute top-0 left-0 w-1 h-full bg-blue-500"></div>
                        <CardHeader className="flex flex-row items-start justify-between pb-2">
                            <div>
                                <CardTitle className="text-xl font-bold">{group.groupName}</CardTitle>
                                <p className="text-slate-500 text-sm mt-1">{group.projectTitle}</p>
                            </div>
                            <div className="flex space-x-2">
                                <Button size="icon" variant="ghost" onClick={() => openEditModal(group)}><Edit2 className="h-4 w-4 text-slate-600" /></Button>
                                <Button size="icon" variant="ghost" onClick={() => handleDeleteGroup(group)}><Trash2 className="h-4 w-4 text-red-500" /></Button>
                            </div>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <p className="text-sm text-slate-600 line-clamp-2">{group.projectDescription}</p>

                            <div className="flex items-center justify-between bg-slate-50 p-3 border border-slate-100">
                                <div className="flex items-center space-x-2">
                                    <Shield className="h-4 w-4 text-indigo-500" />
                                    <span className="text-sm font-medium text-slate-700">Supervisor:</span>
                                    <span className="text-sm text-slate-600">{group.supervisor ? group.supervisor.fullName : 'Not Assigned'}</span>
                                </div>
                            </div>

                            <div>
                                <div className="flex justify-between items-center mb-2">
                                    <h4 className="text-sm font-semibold text-slate-700 flex items-center"><Users className="h-4 w-4 mr-1" /> Members ({group.members ? group.members.length : 0}/4)</h4>
                                    <Button size="sm" variant="outline" onClick={() => openMemberModal(group)}><UserPlus className="h-3 w-3 mr-1" /> Manage</Button>
                                </div>
                                <div className="space-y-1">
                                    {group.members && group.members.length > 0 ? (
                                        group.members.map(member => (
                                            <div key={member.id} className="flex justify-between items-center text-sm p-2 hover:bg-slate-50 border border-transparent hover:border-slate-100 transition-colors">
                                                <span>{member.fullName} <span className="text-slate-400 text-xs">({member.email})</span></span>
                                            </div>
                                        ))
                                    ) : <p className="text-xs text-slate-400 italic">No members assigned</p>}
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>

            {/* Create/Edit Modal Overlay */}
            {(showCreateModal || showEditModal) && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-6 max-w-lg w-full shadow-2xl animate-fade-in border border-slate-200">
                        <h3 className="text-xl font-bold mb-4">{showCreateModal ? 'Create New Group' : 'Edit Group'}</h3>
                        <form onSubmit={showCreateModal ? handleCreateGroup : handleUpdateGroup} className="space-y-4">
                            <Input placeholder="Group Name" value={formData.groupName} onChange={e => setFormData({ ...formData, groupName: e.target.value })} required />
                            {/* project title should be greater than 5 chars */}

                            <Input placeholder="Project Title" value={formData.projectTitle} onChange={e => setFormData({ ...formData, projectTitle: e.target.value })} required />
                            <textarea
                                className="w-full h-24 p-2 border border-slate-300 focus:ring-2 focus:ring-blue-500 focus:outline-none text-sm"
                                placeholder="Project Description"
                                required
                                value={formData.projectDescription}
                                onChange={e => setFormData({ ...formData, projectDescription: e.target.value })}
                            ></textarea>

                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Supervisor (Required)</label>
                                <select
                                    className="w-full h-11 px-3 border border-slate-300 bg-transparent text-sm shadow-sm transition-colors file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-slate-400 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-slate-950 disabled:cursor-not-allowed disabled:opacity-50"
                                    value={formData.supervisorId}
                                    onChange={e => setFormData({ ...formData, supervisorId: e.target.value })}
                                    required
                                >
                                    <option value="">Select Supervisor</option>
                                    {supervisors.map(sup => (
                                        <option key={sup.id} value={sup.id}>{sup.fullName} ({sup.email})</option>
                                    ))}
                                </select>
                            </div>

                            <div className="flex justify-end space-x-2 pt-2">
                                <Button type="button" variant="ghost" onClick={() => { setShowCreateModal(false); setShowEditModal(false); }}>Cancel</Button>
                                <Button type="submit" isLoading={actionLoading}>Save Group</Button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Member Modal Overlay */}
            {showMemberModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-6 max-w-md w-full shadow-2xl animate-fade-in border border-slate-200">
                        <h3 className="text-xl font-bold mb-4">Manage Members: {selectedGroup?.groupName}</h3>

                        <div className="mb-6">
                            <h4 className="text-sm font-semibold mb-2">Current Members ({selectedGroup?.members?.length || 0}/4)</h4>
                            <div className="bg-slate-50 p-4 max-h-40 overflow-y-auto space-y-2 border border-slate-200">
                                {selectedGroup?.members?.map(member => (
                                    <div key={member.id} className="flex justify-between items-center text-sm">
                                        <span>{member.fullName}</span>
                                        <button onClick={() => handleRemoveMember(selectedGroup.id, member.id)} className="text-red-500 hover:text-red-700"><UserMinus className="h-4 w-4" /></button>
                                    </div>
                                ))}
                                {(!selectedGroup?.members || selectedGroup?.members.length === 0) && <p className="text-xs text-slate-400">No members</p>}
                            </div>
                        </div>

                        {selectedGroup?.members?.length >= 4 ? (
                            <div className="p-3 bg-red-50 text-red-700 text-sm border border-red-200 flex items-center">
                                <AlertCircle className="w-4 h-4 inline mr-2 flex-shrink-0" />
                                <span>Group is full (Max 4 students). Remove a member to add a new one.</span>
                            </div>
                        ) : (
                            <form onSubmit={handleAddMember} className="space-y-4 border-t pt-4">
                                <label className="block text-sm font-medium">Add Student</label>
                                <div className="flex space-x-2">
                                    <select
                                        className="w-full h-11 px-3 border border-slate-300 bg-transparent text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-slate-950"
                                        value={memberData.userId}
                                        onChange={e => setMemberData({ userId: e.target.value })}
                                        required
                                    >
                                        <option value="">Select Student</option>
                                        {availableStudents.map(std => (
                                            <option key={std.id} value={std.id}>{std.fullName} ({std.email})</option>
                                        ))}
                                    </select>
                                    <Button type="submit" isLoading={actionLoading}>Add</Button>
                                </div>
                                {availableStudents.length === 0 && <p className="text-xs text-orange-500">No available students found.</p>}
                            </form>
                        )}

                        <div className="flex justify-end pt-4">
                            <Button type="button" variant="ghost" onClick={() => setShowMemberModal(false)}>Close</Button>
                        </div>
                    </div>
                </div>
            )}

            {/* Delete Confirmation Modal */}
            {showDeleteModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-6 max-w-md w-full shadow-2xl animate-fade-in border border-slate-200">
                        <div className="flex items-start space-x-4">
                            <div className="flex-shrink-0">
                                <div className="w-12 h-12 bg-red-100 flex items-center justify-center">
                                    <Trash2 className="h-6 w-6 text-red-600" />
                                </div>
                            </div>
                            <div className="flex-1">
                                <h3 className="text-lg font-bold text-slate-900 mb-2">Delete Group</h3>
                                <p className="text-sm text-slate-600 mb-1">
                                    Are you sure you want to delete <span className="font-semibold text-slate-900">{groupToDelete?.groupName}</span>?
                                </p>
                                <p className="text-sm text-slate-500">
                                    This action cannot be undone.
                                </p>
                            </div>
                        </div>

                        <div className="flex justify-end space-x-3 mt-6">
                            <Button
                                type="button"
                                variant="ghost"
                                onClick={cancelDelete}
                                disabled={actionLoading}
                            >
                                Cancel
                            </Button>
                            <Button
                                type="button"
                                onClick={confirmDelete}
                                isLoading={actionLoading}
                                className="bg-red-600 hover:bg-red-700 text-white"
                            >
                                Delete
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default GroupsPage;
