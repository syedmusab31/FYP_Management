import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import { Loader2, FileText, MessageSquare } from 'lucide-react';
import Button from '../components/ui/Button';
import { cn } from '../utils/cn';

const ReviewsPage = () => {
    const { user } = useAuth();
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [feedbackDoc, setFeedbackDoc] = useState(null);
    const [reviews, setReviews] = useState([]);

    useEffect(() => {
        const fetchLog = async () => {
            try {
                // Fetch all documents supervised
                // Note: The endpoint returns all docs for groups supervised by this user
                const res = await api.get(`/documents/supervisor/${user.id}`);
                // Filter for processed documents (Not Draft, Not Submitted - i.e. reviewed ones)
                // Actually, if status is SUBMITTED, it's pending.
                // We want APPROVED, REVISION_REQUESTED, REJECTED.
                const log = res.data.filter(d =>
                    d.status === 'APPROVED' ||
                    d.status === 'REVISION_REQUESTED' ||
                    d.status === 'REJECTED' ||
                    d.status === 'GRADED'
                );
                // Sort by date sort of? API doesn't guarantee order.
                setDocuments(log);
            } catch (error) {
                console.error("Failed to fetch review log", error);
            } finally {
                setLoading(false);
            }
        };

        if (user) fetchLog();
    }, [user]);

    const handleViewFeedback = async (doc) => {
        try {
            const res = await api.get(`/documents/${doc.id}/reviews`);
            // Filter reviews by me? Or all reviews?
            // "Show a log... by supervisor with feedback".
            // Showing all reviews is safer context.
            setReviews(res.data);
            setFeedbackDoc(doc);
        } catch (error) {
            alert("Could not load feedback");
        }
    };

    if (loading) return <div className="flex justify-center p-10"><Loader2 className="animate-spin h-8 w-8 text-blue-600" /></div>;

    return (
        <div className="space-y-6 animate-fade-in p-6">
            <h2 className="text-3xl font-bold text-slate-800">Review History</h2>
            <p className="text-slate-500">Log of documents you have approved or requested revision for.</p>

            <div className="bg-white border border-slate-200 shadow-sm overflow-hidden rounded-lg">
                <table className="w-full text-sm text-left">
                    <thead className="bg-slate-50 text-slate-500 border-b border-slate-200">
                        <tr>
                            <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Document</th>
                            <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Group</th>
                            <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Type</th>
                            <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Status</th>
                            <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {documents.length > 0 ? documents.map(doc => (
                            <tr key={doc.id} className="hover:bg-slate-50 transition-colors">
                                <td className="px-6 py-4 font-medium text-slate-900">{doc.title}</td>
                                <td className="px-6 py-4 text-slate-600">{doc.groupName}</td>
                                <td className="px-6 py-4 text-slate-500">{doc.type}</td>
                                <td className="px-6 py-4">
                                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border
                                        ${doc.status === 'APPROVED' ? 'bg-green-50 text-green-700 border-green-200' :
                                            doc.status === 'REVISION_REQUESTED' ? 'bg-orange-50 text-orange-700 border-orange-200' :
                                                doc.status === 'REJECTED' ? 'bg-red-50 text-red-700 border-red-200' : 'bg-blue-50 text-blue-700 border-blue-200'}`}>
                                        {doc.status}
                                    </span>
                                </td>
                                <td className="px-6 py-4">
                                    <Button size="sm" variant="outline" onClick={() => handleViewFeedback(doc)}>
                                        <MessageSquare className="h-4 w-4 mr-2" /> Log
                                    </Button>
                                </td>
                            </tr>
                        )) : <tr><td colSpan="5" className="p-8 text-center text-slate-500">No review history found.</td></tr>}
                    </tbody>
                </table>
            </div>

            {feedbackDoc && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
                    <div className="bg-white p-6 max-w-lg w-full shadow-2xl animate-fade-in border border-slate-200 max-h-[80vh] overflow-y-auto rounded-lg">
                        <h3 className="text-xl font-bold mb-4">Feedback Log: {feedbackDoc.title}</h3>
                        <div className="space-y-4">
                            {reviews.length > 0 ? reviews.map((review, idx) => (
                                <div key={idx} className="bg-slate-50 p-4 border border-slate-200 rounded">
                                    <div className="flex justify-between items-start mb-2">
                                        <span className={cn(
                                            "text-xs font-bold px-2 py-0.5 uppercase tracking-wide rounded",
                                            review.status === 'APPROVED' ? 'bg-green-100 text-green-700' : 'bg-orange-100 text-orange-700'
                                        )}>{review.status}</span>
                                        <span className="text-xs text-slate-400">{new Date(review.reviewedAt).toLocaleString()}</span>
                                    </div>
                                    <p className="text-slate-700 text-sm mb-2 whitespace-pre-wrap">{review.comments}</p>
                                    <p className="text-xs text-slate-500 italic">- {review.reviewerName}</p>
                                </div>
                            )) : (
                                <p className="text-slate-500 italic">No feedback entries found.</p>
                            )}
                        </div>
                        <div className="flex justify-end pt-4">
                            <Button type="button" onClick={() => setFeedbackDoc(null)}>Close</Button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ReviewsPage;
