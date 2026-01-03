import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import Button from '../components/ui/Button';
import { CheckCircle, Clock } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const GradesPage = () => {
    const { user } = useAuth();
    const [grades, setGrades] = useState([]);
    const [pendingDocs, setPendingDocs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [gradingDoc, setGradingDoc] = useState(null);
    const [score, setScore] = useState('');
    const [feedback, setFeedback] = useState('');

    const isFypCommittee = user.role?.name === 'FYP_COMMITTEE';
    const isCommittee = user.role?.name === 'COMMITTEE_MEMBER';

    useEffect(() => {
        fetchData();
    }, [user]);

    const fetchData = async () => {
        setLoading(true);
        try {
            if (isFypCommittee) {
                // Get all grades via dashboard for overview
                const res = await api.get('/dashboard/fyp-committee');
                setGrades(res.data.allGrades || []);
            } else if (isCommittee) {
                // Fetch approved and revision requested documents to grade
                const resDocs = await api.get('/documents/committee/gradable');
                // Ideally filtering out already graded ones requires fetching my grades
                // For now, showing all Approved and Revision Requested.
                //don't show docs that is requested for revision
                const filteredDocs = resDocs.data.filter(doc => doc.status !== 'REVISION_REQUESTED');
                setPendingDocs(filteredDocs);
            }
        } catch (error) {
            console.error("Failed to fetch data", error);
        } finally {
            setLoading(false);
        }
    };

    const handleFinalize = async (gradeId) => {
        if (!window.confirm("Are you sure you want to release this grade? It will be visible to students.")) return;
        try {
            await api.put(`/grades/${gradeId}/finalize`);
            fetchData();
            alert("Grade released successfully!");
        } catch (error) {
            alert(error.response?.data?.message || 'Failed to finalize grade');
        }
    };

    const handleGradeSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/grades', {
                groupId: gradingDoc.groupId,
                documentId: gradingDoc.id,
                score: Number(score),
                feedback: feedback,
                isFinal: false // Committee grades are provisional
            });
            setGradingDoc(null);
            setScore('');
            setFeedback('');
            fetchData();
            alert("Grade submitted successfully!");
            // Remove from pending locally
            setPendingDocs(pendingDocs.filter(d => d.id !== gradingDoc.id));
        } catch (error) {
            alert("Failed to submit grade: " + (error.response?.data?.message || error.message));
        }
    };

    if (loading) return <div className="flex justify-center p-10"><Clock className="animate-spin h-8 w-8 text-blue-600" /></div>;

    return (
        <div className="space-y-6 animate-fade-in p-6">
            <div className="flex justify-between items-center">
                <h2 className="text-3xl font-bold text-slate-800">
                    {isFypCommittee ? 'Grade Administration' : 'Grading & Evaluation'}
                </h2>
                {isFypCommittee && <span className="text-sm bg-purple-100 text-purple-700 px-3 py-1 rounded-full font-bold">FYP Access</span>}
            </div>

            {isCommittee && (
                <div className="space-y-4">
                    <h3 className="text-xl font-bold text-slate-800 border-b pb-2">Documents Pending Grading</h3>
                    {pendingDocs.length > 0 ? (
                        <div className="grid md:grid-cols-2 gap-4">
                            {pendingDocs.map(doc => (
                                <Card key={doc.id} className="border-l-4 border-l-orange-400">
                                    <CardContent className="p-4">
                                        <div className="flex justify-between items-start">
                                            <div>
                                                <h4 className="font-bold text-lg">{doc.title}</h4>
                                                <p className="text-sm text-slate-600">{doc.groupName}</p>
                                                <p className="text-xs text-slate-400 mt-1">{new Date(doc.createdAt).toLocaleDateString()}</p>
                                            </div>
                                            <Button size="sm" onClick={() => setGradingDoc(doc)}>Grade</Button>
                                        </div>
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
                    ) : (
                        <div className="p-8 text-center text-slate-500 border-2 border-dashed border-slate-200 rounded">
                            No approved documents pending grading.
                        </div>
                    )}
                </div>
            )}

            {isFypCommittee && (
                <div className="space-y-4">
                    <h3 className="text-xl font-bold text-slate-800 border-b pb-2">All Grades</h3>
                    <div className="grid gap-4">
                        {grades.length > 0 ? grades.map(grade => (
                            <Card key={grade.id} className={grade.isFinal ? "border-l-4 border-l-green-500" : "border-l-4 border-l-yellow-500"}>
                                <CardContent className="p-4 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                                    <div>
                                        <h4 className="font-bold text-lg">{grade.group?.groupName}</h4>
                                        <p className="text-sm font-semibold text-slate-700">{grade.document?.title}</p>
                                        <div className="flex items-center space-x-2 mt-1">
                                            <span className="text-2xl font-bold text-slate-800">{grade.score}/100</span>
                                            {grade.isFinal ?
                                                <span className="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded font-bold uppercase">Released</span> :
                                                <span className="text-xs bg-yellow-100 text-yellow-700 px-2 py-0.5 rounded font-bold uppercase">Provisional</span>
                                            }
                                        </div>
                                        <p className="text-sm text-slate-500 mt-1 italic">"{grade.feedback}"</p>
                                        <p className="text-xs text-slate-400 mt-1">Graded by {grade.gradedBy?.fullName}</p>
                                    </div>

                                    {!grade.isFinal && (
                                        <Button onClick={() => handleFinalize(grade.id)} className="bg-green-600 hover:bg-green-700">
                                            Release Result
                                        </Button>
                                    )}
                                </CardContent>
                            </Card>
                        )) : <p className="text-slate-500 italic">No grades recorded yet.</p>}
                    </div>
                </div>
            )}

            {/* Grading Modal */}
            {gradingDoc && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
                    <div className="bg-white p-6 max-w-md w-full rounded-lg shadow-xl animate-fade-in">
                        <h3 className="text-xl font-bold mb-4">Grade Document</h3>
                        <div className="mb-4">
                            <p className="font-semibold">{gradingDoc.title}</p>
                            <p className="text-sm text-slate-500">{gradingDoc.groupName}</p>
                        </div>
                        <form onSubmit={handleGradeSubmit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700">Score (0-10)</label>
                                <input
                                    type="number"
                                    min="0"
                                    max="10"
                                    step="0.1"
                                    className="mt-1 w-full border border-slate-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none"
                                    value={score}
                                    onChange={e => setScore(e.target.value)}
                                    required
                                />
                                <p className="text-xs text-slate-500 mt-1">Enter a score out of 10.</p>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-700">Feedback</label>
                                <textarea
                                    className="mt-1 w-full border border-slate-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none"
                                    rows="3"
                                    value={feedback}
                                    onChange={e => setFeedback(e.target.value)}
                                    required
                                    placeholder="Enter detailed feedback..."
                                />
                            </div>
                            <div className="flex justify-end space-x-3 pt-2">
                                <Button type="button" variant="ghost" onClick={() => setGradingDoc(null)}>Cancel</Button>
                                <Button type="submit">Submit Grade</Button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default GradesPage;
