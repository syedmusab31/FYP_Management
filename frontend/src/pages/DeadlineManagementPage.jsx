import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import { Plus, Trash, Calendar } from 'lucide-react';
import Button from '../components/ui/Button';

const DeadlineManagementPage = () => {
    const [deadlines, setDeadlines] = useState([]);
    const [title, setTitle] = useState('');
    const [documentType, setDocumentType] = useState('PROPOSAL');
    const [dueDate, setDueDate] = useState('');
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);

    const docTypes = [
        'PROPOSAL', 'PROGRESS_REPORT', 'FINAL_REPORT', 'PRESENTATION'
    ];

    const fetchDeadlines = async () => {
        try {
            const res = await api.get('/deadlines');
            setDeadlines(res.data);
        } catch (error) {
            console.error("Failed to fetch deadlines", error);
        }
    };

    useEffect(() => {
        fetchDeadlines();
    }, []);

    const handleCreate = async (e) => {
        e.preventDefault();
        if (!title || !dueDate) return;
        setLoading(true);
        try {
            // Convert local datetime to ISO string (Instant format)
            const dateObj = new Date(dueDate);
            await api.post('/deadlines', {
                title,
                description,
                documentType,
                dueDate: dateObj.toISOString()
            });
            setTitle('');
            setDescription('');
            setDueDate('');
            fetchDeadlines();
            alert("Deadline created successfully!");
        } catch (error) {
            console.error(error);
            alert("Failed to create deadline: " + (error.response?.data?.message || error.message));
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Are you sure you want to delete this deadline?")) return;
        try {
            await api.delete(`/deadlines/${id}`);
            setDeadlines(deadlines.filter(d => d.id !== id));
        } catch (error) {
            alert("Failed to delete deadline");
        }
    };

    return (
        <div className="space-y-6 animate-fade-in p-6">
            <h2 className="text-3xl font-bold text-slate-800">Deadline Management</h2>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Create Form */}
                <div className="lg:col-span-1">
                    <Card>
                        <CardHeader><CardTitle>Create New Deadline</CardTitle></CardHeader>
                        <CardContent>
                            <form onSubmit={handleCreate} className="space-y-4">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700">Title</label>
                                    <input
                                        type="text"
                                        className="mt-1 w-full border border-slate-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none"
                                        value={title}
                                        onChange={e => setTitle(e.target.value)}
                                        required
                                        placeholder="e.g. Project Proposal Submission"
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700">Document Type</label>
                                    <select
                                        className="mt-1 w-full border border-slate-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none"
                                        value={documentType}
                                        onChange={e => setDocumentType(e.target.value)}
                                    >
                                        {docTypes.map(t => (
                                            <option key={t} value={t}>
                                                {t.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700">Due Date</label>
                                    <input
                                        type="datetime-local"
                                        className="mt-1 w-full border border-slate-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none"
                                        value={dueDate}
                                        onChange={e => setDueDate(e.target.value)}
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700">Description (Optional)</label>
                                    <textarea
                                        className="mt-1 w-full border border-slate-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none"
                                        value={description}
                                        onChange={e => setDescription(e.target.value)}
                                        rows="3"
                                    />
                                </div>
                                <Button type="submit" className="w-full" disabled={loading}>
                                    {loading ? 'Creating...' : <><Plus className="h-4 w-4 mr-2" /> Create Deadline</>}
                                </Button>
                            </form>
                        </CardContent>
                    </Card>
                </div>

                {/* List */}
                <div className="lg:col-span-2 space-y-4">
                    <h3 className="text-xl font-bold text-slate-800">Existing Deadlines</h3>
                    {deadlines.length === 0 ? (
                        <div className="text-center py-10 text-slate-400 border-2 border-dashed border-slate-200 rounded">
                            No deadlines configured.
                        </div>
                    ) : (
                        <div className="grid gap-4">
                            {deadlines.map(deadline => (
                                <div key={deadline.id} className="bg-white p-4 rounded shadow-sm border border-slate-200 flex justify-between items-start">
                                    <div className="flex items-start space-x-4">
                                        <div className="bg-orange-100 p-3 rounded-full text-orange-600">
                                            <Calendar className="h-6 w-6" />
                                        </div>
                                        <div>
                                            <h4 className="font-bold text-lg text-slate-800">{deadline.title}</h4>
                                            <span className="text-xs font-bold uppercase bg-slate-100 text-slate-600 px-2 py-0.5 rounded">{deadline.documentType}</span>
                                            <p className="text-sm text-slate-600 mt-1">{new Date(deadline.dueDate).toLocaleString()}</p>
                                            {deadline.description && <p className="text-sm text-slate-500 mt-2 italic">{deadline.description}</p>}
                                        </div>
                                    </div>
                                    <button
                                        onClick={() => handleDelete(deadline.id)}
                                        className="text-slate-400 hover:text-red-500 transition-colors p-2"
                                        title="Delete Deadline"
                                    >
                                        <Trash className="h-5 w-5" />
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default DeadlineManagementPage;
