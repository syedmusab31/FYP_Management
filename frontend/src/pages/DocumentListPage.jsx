import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { Plus, FileText, Download, Clock, UploadCloud } from 'lucide-react';
import { cn } from '../utils/cn';

const DocumentListPage = () => {
    const { user } = useAuth();
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isUploading, setIsUploading] = useState(false);

    // Upload state
    const [uploadFile, setUploadFile] = useState(null);
    const [uploadTitle, setUploadTitle] = useState('');
    const [uploadType, setUploadType] = useState('PROPOSAL');

    // Review state
    const [reviewDoc, setReviewDoc] = useState(null);
    const [reviewAction, setReviewAction] = useState('APPROVE'); // APPROVE or REVISION
    const [reviewComments, setReviewComments] = useState('');

    // Feedback state
    const [feedbackDoc, setFeedbackDoc] = useState(null);
    const [reviews, setReviews] = useState([]);

    const fetchDocuments = async () => {
        setLoading(true);
        try {
            let res;
            if (user.role?.name === 'STUDENT') {
                const groupId = user.group?.id || user.groupId;
                if (groupId) {
                    res = await api.get(`/documents/group/${groupId}`);
                } else {
                    const me = await api.get('/auth/me');
                    if (me.data.groupId) {
                        res = await api.get(`/documents/group/${me.data.groupId}`);
                    } else {
                        res = { data: [] };
                    }
                }
            } else if (user.role?.name === 'SUPERVISOR') {
                res = await api.get(`/documents/supervisor/${user.id}`);
            } else if (user.role?.name === 'COMMITTEE_MEMBER') {
                // Committee sees APPROVED documents to Grade or Request Revision
                res = await api.get('/documents/status/APPROVED');
            } else {
                res = { data: [] };
            }
            if (res && res.data) setDocuments(res.data);
        } catch (error) {
            console.error("Failed to fetch docs", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchDocuments();
    }, [user]);

    const handleUpload = async (e) => {
        e.preventDefault();
        if (!uploadFile) return;

        const formData = new FormData();
        formData.append('file', uploadFile);

        let groupId = user.group?.id || user.groupId;
        if (!groupId) {
            const me = await api.get('/auth/me');
            groupId = me.data.groupId;
        }

        formData.append('groupId', groupId);
        formData.append('title', uploadTitle);
        formData.append('type', uploadType);

        try {
            await api.post('/documents/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            setIsUploading(false);
            setUploadFile(null);
            setUploadTitle('');
            fetchDocuments();
        } catch (error) {
            console.error("Upload failed", error);
            alert("Upload failed: " + (error.response?.data?.message || error.message));
        }
    };

    const handleSubmitDocument = async (docId) => {
        if (!window.confirm("Are you sure you want to submit this document? It will be locked for review.")) return;
        try {
            await api.put(`/documents/${docId}/submit`);
            fetchDocuments();
        } catch (error) {
            console.error("Submission failed", error);
            alert("Submission failed: " + (error.response?.data?.message || error.message));
        }
    };

    const openReviewModal = (doc) => {
        setReviewDoc(doc);
        // Default action based on role
        if (user.role?.name === 'COMMITTEE_MEMBER') {
            setReviewAction('REVISION');
        } else {
            setReviewAction('APPROVE');
        }
        setReviewComments('');
    };

    const handleReviewSubmit = async (e) => {
        e.preventDefault();
        if (!reviewDoc) return;

        try {
            await api.put(`/documents/${reviewDoc.id}/review`, {
                action: reviewAction,
                comments: reviewComments
            });
            setReviewDoc(null);
            fetchDocuments();
        } catch (error) {
            console.error("Review failed", error);
            alert("Review failed: " + (error.response?.data?.message || error.message));
        }
    };

    const handleViewFeedback = async (doc) => {
        try {
            const res = await api.get(`/documents/${doc.id}/reviews`);
            setReviews(res.data);
            setFeedbackDoc(doc);
        } catch (error) {
            console.error("Failed to fetch reviews", error);
            alert("Could not load feedback");
        }
    };

    return (
        <div className="space-y-6 animate-fade-in relative">
            <div className="flex justify-between items-center bg-white p-6 border border-slate-200 shadow-sm">
                <div>
                    <h2 className="text-2xl font-bold text-slate-800">Documents</h2>
                    <p className="text-sm text-slate-500">Manage and track your project documentation</p>
                </div>
                {user.role?.name === 'STUDENT' && (
                    <Button onClick={() => setIsUploading(!isUploading)}>
                        <Plus className="mr-2 h-4 w-4" /> Upload New
                    </Button>
                )}
            </div>

            {/* Upload Form */}
            {isUploading && (
                <div className="bg-slate-50 border border-dashed border-slate-300 p-8 rounded-none animate-slide-up">
                    <h3 className="text-lg font-semibold mb-4 flex items-center"><UploadCloud className="mr-2 h-5 w-5 text-blue-500" /> Upload Document</h3>
                    <form onSubmit={handleUpload} className="space-y-4 max-w-xl">
                        <Input
                            placeholder="Document Title"
                            value={uploadTitle}
                            onChange={e => setUploadTitle(e.target.value)}
                            required
                        />
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <select
                                className="w-full h-10 px-3 border border-slate-300 rounded-none bg-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                value={uploadType}
                                onChange={e => setUploadType(e.target.value)}
                            >
                                <option value="PROPOSAL">Proposal</option>
                                <option value="PROGRESS_REPORT">Progress Report</option>
                                <option value="FINAL_REPORT">Final Report</option>
                                <option value="PRESENTATION">Presentation</option>
                            </select>
                            <input
                                type="file"
                                onChange={e => setUploadFile(e.target.files[0])}
                                className="block w-full text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-none file:border-0 file:text-sm file:font-semibold file:bg-blue-600 file:text-white hover:file:bg-blue-700 cursor-pointer"
                                required
                            />
                        </div>
                        <div className="flex justify-end space-x-2 pt-4">
                            <Button type="button" variant="ghost" onClick={() => setIsUploading(false)}>Cancel</Button>
                            <Button type="submit">Start Upload</Button>
                        </div>
                    </form>
                </div>
            )}

            {/* Review Modal */}
            {reviewDoc && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
                    <div className="bg-white p-6 max-w-md w-full shadow-2xl animate-fade-in border border-slate-200">
                        <h3 className="text-xl font-bold mb-4">Review Document: {reviewDoc.title}</h3>
                        <form onSubmit={handleReviewSubmit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Decision</label>
                                <div className="flex space-x-4">
                                    {/* Supervisor can Approve or Revision */}
                                    {user.role?.name === 'SUPERVISOR' && (
                                        <label className="flex items-center space-x-2 cursor-pointer">
                                            <input type="radio" name="action" value="APPROVE" checked={reviewAction === 'APPROVE'} onChange={e => setReviewAction(e.target.value)} />
                                            <span className="text-green-700 font-medium">Approve</span>
                                        </label>
                                    )}
                                    {/* Both can Request Revision */}
                                    <label className="flex items-center space-x-2 cursor-pointer">
                                        <input type="radio" name="action" value="REVISION" checked={reviewAction === 'REVISION'} onChange={e => setReviewAction(e.target.value)} />
                                        <span className="text-orange-700 font-medium">Request Revision</span>
                                    </label>
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Comments</label>
                                <textarea
                                    className="w-full h-24 px-3 py-2 border border-slate-300 rounded-none focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    placeholder="Enter your feedback here..."
                                    value={reviewComments}
                                    onChange={e => setReviewComments(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="flex justify-end space-x-2 pt-2">
                                <Button type="button" variant="ghost" onClick={() => setReviewDoc(null)}>Cancel</Button>
                                <Button type="submit">Submit Review</Button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* View Feedback Modal */}
            {feedbackDoc && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
                    <div className="bg-white p-6 max-w-lg w-full shadow-2xl animate-fade-in border border-slate-200 max-h-[80vh] overflow-y-auto">
                        <h3 className="text-xl font-bold mb-4">Feedback for: {feedbackDoc.title}</h3>
                        <div className="space-y-4">
                            {reviews.length > 0 ? reviews.map((review, idx) => (
                                <div key={idx} className="bg-slate-50 p-4 border border-slate-200">
                                    <div className="flex justify-between items-start mb-2">
                                        <span className={cn(
                                            "text-xs font-bold px-2 py-0.5 uppercase tracking-wide",
                                            review.status === 'APPROVED' ? 'bg-green-100 text-green-700' : 'bg-orange-100 text-orange-700'
                                        )}>{review.status}</span>
                                        <span className="text-xs text-slate-400">{new Date(review.reviewedAt).toLocaleDateString()}</span>
                                    </div>
                                    <p className="text-slate-700 text-sm mb-2">{review.comments}</p>
                                    <p className="text-xs text-slate-500 italic">- {review.reviewerName}</p>
                                </div>
                            )) : (
                                <p className="text-slate-500 italic">No feedback found.</p>
                            )}
                        </div>
                        <div className="flex justify-end pt-4">
                            <Button type="button" onClick={() => setFeedbackDoc(null)}>Close</Button>
                        </div>
                    </div>
                </div>
            )}

            <div className="grid grid-cols-1 gap-4">
                {documents.map(doc => (
                    <div key={doc.id} className="bg-white border border-slate-200 p-6 flex items-center justify-between hover:shadow-md transition-all duration-200 group">
                        <div className="flex items-center space-x-4">
                            <div className="h-12 w-12 bg-slate-50 text-slate-400 group-hover:bg-blue-50 group-hover:text-blue-600 flex items-center justify-center rounded-none transition-colors">
                                <FileText className="h-6 w-6" />
                            </div>
                            <div>
                                <h3 className="font-bold text-lg text-slate-800 group-hover:text-blue-600 transition-colors">{doc.title}</h3>
                                <div className="flex items-center space-x-3 text-sm text-slate-500 mt-1">
                                    <span className="font-mono bg-slate-100 px-2 py-0.5 text-xs text-slate-600 uppercase tracking-wide">{doc.type}</span>
                                    <span className="flex items-center"><Clock className="h-3 w-3 mr-1" /> {new Date(doc.createdAt).toLocaleDateString()}</span>
                                    <span>v{doc.version}</span>
                                    <span className="text-xs text-slate-400">By: {doc.uploadedByName}</span>
                                </div>
                                {doc.deadlineDate && (
                                    <div className="mt-1 text-xs text-red-600 font-medium">
                                        Deadline: {new Date(doc.deadlineDate).toLocaleDateString()}
                                    </div>
                                )}
                            </div>
                        </div>
                        <div className="flex items-center space-x-4">
                            <span className={cn(
                                "px-3 py-1 text-xs font-bold uppercase tracking-wider border",
                                doc.status === 'APPROVED' ? 'bg-green-50 text-green-700 border-green-200' :
                                    doc.status === 'REVISION_REQUESTED' ? 'bg-orange-50 text-orange-700 border-orange-200' :
                                        doc.status === 'REJECTED' ? 'bg-red-50 text-red-700 border-red-200' :
                                            'bg-slate-50 text-slate-700 border-slate-200'
                            )}>
                                {doc.status}
                            </span>

                            {doc.isLate && (
                                <span className="px-3 py-1 text-xs font-bold uppercase tracking-wider border bg-red-100 text-red-700 border-red-200">
                                    LATE
                                </span>
                            )}

                            {/* Student Actions */}
                            {user.role?.name === 'STUDENT' && doc.status === 'DRAFT' && (
                                <Button size="sm" onClick={() => handleSubmitDocument(doc.id)} className="bg-indigo-600 hover:bg-indigo-700">
                                    Submit
                                </Button>
                            )}

                            {/* Student Re-submit Action (if Revision Requested, they must upload new version first, which sets status to DRAFT.
                                 However, if they just uploaded over it, it becomes DRAFT. So just DRAFT check is likely enough,
                                 unless they can see REVISION_REQUESTED state and need to know they can't submit yet.)
                                 Actually backend says: "Can only submit if in DRAFT status".
                                 So we only show submit if DRAFT.
                             */}
                            {/* Student: View Feedback if Revision Requested */}
                            {user.role?.name === 'STUDENT' && doc.status === 'REVISION_REQUESTED' && (
                                <Button size="sm" variant="outline" onClick={() => handleViewFeedback(doc)}>
                                    View Feedback
                                </Button>
                            )}

                            {/* Supervisor Actions */}
                            {user.role?.name === 'SUPERVISOR' && doc.status === 'SUBMITTED' && (
                                <Button size="sm" onClick={() => openReviewModal(doc)} className="bg-purple-600 hover:bg-purple-700">
                                    Review
                                </Button>
                            )}

                            {/* Committee Actions */}
                            {user.role?.name === 'COMMITTEE_MEMBER' && doc.status === 'APPROVED' && (
                                <Button size="sm" onClick={() => openReviewModal(doc)} className="bg-purple-600 hover:bg-purple-700">
                                    Request Revision
                                </Button>
                            )}

                            {/* Only show download if path exists. normalize path for windows backslashes */}
                            {doc.filePath && (
                                <Button variant="outline" size="icon" onClick={() => window.open(`/${doc.filePath.replace(/\\/g, '/')}`, '_blank')} title="Download">
                                    <Download className="h-4 w-4" />
                                </Button>
                            )}
                        </div>
                    </div>
                ))}
            </div>

            {documents.length === 0 && !loading && (
                <div className="text-center py-20 bg-slate-50 border border-dashed border-slate-300">
                    <FileText className="h-10 w-10 text-slate-300 mx-auto mb-4" />
                    <p className="text-slate-500">No documents found.</p>
                    {user.role?.name === 'STUDENT' && <p className="text-slate-400 text-sm mt-2">Upload your first document to get started.</p>}
                    {user.role?.name === 'COMMITTEE_MEMBER' && <p className="text-slate-400 text-sm mt-2">No documents currently approved for review.</p>}
                </div>
            )}
        </div>
    );
};

export default DocumentListPage;
