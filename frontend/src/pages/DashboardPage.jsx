import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import { Loader2, CheckCircle, Clock, AlertCircle, Users } from 'lucide-react';

const DashboardPage = () => {
    const { user } = useAuth();
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDashboard = async () => {
            try {
                let endpoint = '';
                const roleName = user.role?.name;
                if (roleName === 'STUDENT') endpoint = '/dashboard/student';
                else if (roleName === 'SUPERVISOR') endpoint = '/dashboard/supervisor';
                else if (roleName === 'COMMITTEE_MEMBER') endpoint = '/dashboard/committee';
                else if (roleName === 'FYP_COMMITTEE') endpoint = '/dashboard/fyp-committee';
                else throw new Error("Unknown role");

                const res = await api.get(endpoint);
                setData(res.data);
            } catch (err) {
                console.error(err);
                setData({});
            } finally {
                setLoading(false);
            }
        };

        if (user) fetchDashboard();
    }, [user]);

    if (loading) return <div className="flex justify-center p-10"><Loader2 className="animate-spin h-8 w-8 text-blue-600" /></div>;

    // Render logic based on role
    const renderStudentDashboard = () => (
        <div className="space-y-6">
            <h2 className="text-3xl font-bold text-slate-800">Hello, {user.fullName}</h2>

            {/* Group & Project Info */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <Card className="border-l-4 border-l-blue-500 shadow-sm">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase tracking-wider">Group</CardTitle></CardHeader>
                    <CardContent><p className="text-xl font-bold">{data.groupInfo?.groupName || 'No Group'}</p></CardContent>
                </Card>
                <Card className="border-l-4 border-l-indigo-500 shadow-sm lg:col-span-2">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase tracking-wider">Project</CardTitle></CardHeader>
                    <CardContent><p className="text-xl font-semibold truncate" title={data.groupInfo?.projectTitle}>{data.groupInfo?.projectTitle || 'Not Assigned'}</p></CardContent>
                </Card>
                <Card className="border-l-4 border-l-green-500 shadow-sm">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase tracking-wider">Status</CardTitle></CardHeader>
                    <CardContent>
                        <div className="flex items-center space-x-2 text-green-600 font-medium">
                            <CheckCircle className="h-5 w-5" /> <span>Active</span>
                        </div>
                    </CardContent>
                </Card>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Members List */}
                <div className="lg:col-span-1 space-y-4">
                    <Card className="h-full border-slate-200 shadow-sm">
                        <CardHeader><CardTitle className="flex items-center"><Users className="mr-2 h-5 w-5 text-blue-500" /> Group Members</CardTitle></CardHeader>
                        <CardContent>
                            {data.groupMembers && data.groupMembers.length > 0 ? (
                                <ul className="space-y-3">
                                    {data.groupMembers.map(member => (
                                        <li key={member.id} className="flex items-center space-x-3 p-2 bg-slate-50 rounded border border-slate-100">
                                            <div className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-xs">{member.fullName.charAt(0)}</div>
                                            <div>
                                                <p className="text-sm font-medium text-slate-800">{member.fullName}</p>
                                                <p className="text-xs text-slate-500">{member.email}</p>
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                            ) : <p className="text-slate-500 italic text-sm">No members found.</p>}
                        </CardContent>
                    </Card>
                </div>

                {/* Deadlines */}
                <div className="lg:col-span-2 space-y-4">
                    <Card className="h-full border-slate-200 shadow-sm">
                        <CardHeader><CardTitle className="flex items-center"><Clock className="mr-2 h-5 w-5 text-orange-500" /> Upcoming Deadlines</CardTitle></CardHeader>
                        <CardContent>
                            {data.upcomingDeadlines && data.upcomingDeadlines.length > 0 ? (
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    {data.upcomingDeadlines.map(deadline => (
                                        <div key={deadline.id} className="p-4 border-l-4 border-l-orange-400 bg-orange-50 rounded-r border-t border-b border-r border-slate-200">
                                            <h4 className="font-bold text-slate-800">{deadline.title}</h4>
                                            <p className="text-sm text-slate-600 mt-1">{new Date(deadline.dueDate).toLocaleDateString()} {new Date(deadline.dueDate).toLocaleTimeString()}</p>
                                            <span className="inline-block mt-2 text-xs font-bold uppercase tracking-wide text-orange-700 bg-orange-200 px-2 py-0.5 rounded">{deadline.documentType}</span>
                                        </div>
                                    ))}
                                </div>
                            ) : <p className="text-slate-500 italic p-4 text-center">No upcoming deadlines.</p>}
                        </CardContent>
                    </Card>
                </div>
            </div>

            <h3 className="text-xl font-bold mt-4 mb-4">Your Documents</h3>
            <div className="bg-white border border-slate-200 shadow-sm overflow-hidden mb-8">
                <table className="w-full text-sm text-left">
                    <thead className="bg-slate-50 text-slate-500 border-b border-slate-200">
                        <tr>
                            <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Title</th>
                            <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Type</th>
                            <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Status</th>
                            <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Submitted</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {data.documents && data.documents.map(doc => (
                            <tr key={doc.id} className="hover:bg-slate-50 transition-colors">
                                <td className="px-6 py-4 font-medium text-slate-900">{doc.title}</td>
                                <td className="px-6 py-4 text-slate-600">{doc.type}</td>
                                <td className="px-6 py-4">
                                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border
                                        ${doc.status === 'APPROVED' ? 'bg-green-50 text-green-700 border-green-200' :
                                            doc.status === 'REVISION_REQUESTED' ? 'bg-orange-50 text-orange-700 border-orange-200' :
                                                doc.status === 'REJECTED' ? 'bg-red-50 text-red-700 border-red-200' : 'bg-blue-50 text-blue-700 border-blue-200'}`}>
                                        {doc.status}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-slate-500">{doc.submittedAt ? new Date(doc.submittedAt).toLocaleDateString() : '-'}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {(!data.documents || data.documents.length === 0) && <div className="p-8 text-center text-slate-500">No documents found. Start by uploading one!</div>}
            </div>
        </div>
    );

    const renderSupervisorDashboard = () => (
        <div className="space-y-6">
            <h2 className="text-3xl font-bold text-slate-800">Supervisor Dashboard</h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <Card className="border-t-4 border-t-purple-500 shadow-sm">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">Supervised Groups</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold">{data.statistics?.total_groups || 0}</p></CardContent>
                </Card>
                <Card className="border-t-4 border-t-orange-500 shadow-sm">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">Pending Reviews</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-orange-600">{data.statistics?.pending_review || 0}</p></CardContent>
                </Card>
                <Card className="border-t-4 border-t-green-500 shadow-sm">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">Approved By Me</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-green-600">{data.statistics?.approved || 0}</p></CardContent>
                </Card>
                <Card className="border-t-4 border-t-blue-500 shadow-sm">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">Approved By Committee</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-blue-600">{data.statistics?.approved_by_committee || 0}</p></CardContent>
                </Card>
            </div>

            <div className="mt-8">
                <h3 className="text-xl font-bold mb-4">Pending Review Documents</h3>
                <div className="bg-white border border-slate-200 shadow-sm overflow-hidden">
                    <table className="w-full text-sm text-left">
                        <thead className="bg-slate-50 text-slate-500 border-b border-slate-200">
                            <tr>
                                <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Title</th>
                                <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Group</th>
                                <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Submitted At</th>
                                <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Status</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                            {data.pendingReviewDocuments && data.pendingReviewDocuments.length > 0 ?
                                data.pendingReviewDocuments.map(doc => (
                                    <tr key={doc.id} className="hover:bg-slate-50 transition-colors">
                                        <td className="px-6 py-4 font-medium text-slate-900">{doc.title}</td>
                                        <td className="px-6 py-4 text-slate-600">{doc.groupName || 'Unknown Group'}</td>
                                        <td className="px-6 py-4 text-slate-500">{new Date(doc.submittedAt).toLocaleDateString()}</td>
                                        <td className="px-6 py-4"><span className="px-2 py-0.5 bg-orange-100 text-orange-700 rounded text-xs font-bold">{doc.status}</span></td>
                                    </tr>
                                )) : <tr><td colSpan="4" className="p-8 text-center text-slate-500">No pending reviews.</td></tr>}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );

    const renderCommitteeDashboard = () => (
        <div className="space-y-6">
            <h2 className="text-3xl font-bold text-slate-800">Committee Dashboard</h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <Card className="border-l-4 border-l-slate-500 shadow-sm">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">Total Groups</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-slate-800">{data.statistics?.total_groups || 0}</p></CardContent>
                </Card>
                <Card className="border-l-4 border-l-blue-500 shadow-sm">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">Docs to Review</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-blue-600">{data.statistics?.approved || 0}</p></CardContent>
                </Card>
                <Card className="border-l-4 border-l-green-500 shadow-sm">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">Graded</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-green-600">{data.statistics?.graded || 0}</p></CardContent>
                </Card>
                <Card className="border-l-4 border-l-indigo-500 shadow-sm">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">Total Docs</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-indigo-600">{data.statistics?.total_documents || 0}</p></CardContent>
                </Card>
            </div>

            <Card className="mt-6">
                <CardHeader><CardTitle>Documents Ready for Review (Approved by Supervisor)</CardTitle></CardHeader>
                <CardContent>
                    {data.approvedDocuments && data.approvedDocuments.length > 0 ? (
                        <div className="bg-white border border-slate-200 overflow-hidden">
                            <table className="w-full text-sm text-left">
                                <thead className="bg-slate-50 text-slate-500 border-b border-slate-200">
                                    <tr>
                                        <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Title</th>
                                        <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Group</th>
                                        <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Project</th>
                                        <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Supervisor</th>
                                        <th className="px-6 py-3 font-medium uppercase tracking-wider text-xs">Date Approved</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-100">
                                    {data.approvedDocuments.map(doc => (
                                        <tr key={doc.id} className="hover:bg-slate-50">
                                            <td className="px-6 py-4 font-medium text-slate-900">{doc.title}</td>
                                            <td className="px-6 py-4 text-slate-600">{doc.groupName}</td>
                                            <td className="px-6 py-4 text-slate-600">{doc.projectTitle || '-'}</td>
                                            <td className="px-6 py-4 text-slate-500">{doc.supervisorName || '-'}</td>
                                            <td className="px-6 py-4 text-slate-500">{new Date(doc.createdAt).toLocaleDateString()}</td>
                                            {/* Note: createdAt is creation date. Ideally we want approval date (reviewedAt) but doc doesn't have it easily available on list. CreatedAt is proxy. */}
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <div className="text-center py-8 text-slate-500 border-2 border-dashed border-slate-100 italic">
                            No documents currently waiting for committee review.
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );

    const renderFypCommitteeDashboard = () => (
        <div className="space-y-6">
            <h2 className="text-3xl font-bold text-slate-800">FYP Committee Overview</h2>

            {/* Statistics Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                <Card className="border-l-4 border-l-blue-600 shadow-sm hover:shadow-md transition-shadow">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">No. of Students</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-slate-800">{data.totalStudents || 0}</p></CardContent>
                </Card>
                <Card className="border-l-4 border-l-orange-600 shadow-sm hover:shadow-md transition-shadow">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">No. of Groups</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-slate-800">{data.statistics?.total_groups || 0}</p></CardContent>
                </Card>
                <Card className="border-l-4 border-l-purple-600 shadow-sm hover:shadow-md transition-shadow">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">No. of Supervisors</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-purple-600">{data.totalSupervisors || 0}</p></CardContent>
                </Card>
                <Card className="border-l-4 border-l-indigo-600 shadow-sm hover:shadow-md transition-shadow">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">No. of Committee</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-indigo-600">{data.totalCommitteeMembers || 0}</p></CardContent>
                </Card>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                <Card className="border-l-4 border-l-green-600 shadow-sm hover:shadow-md transition-shadow">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">Total Docs</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-slate-800">{data.statistics?.total_documents || 0}</p></CardContent>
                </Card>
                <Card className="border-l-4 border-l-teal-600 shadow-sm hover:shadow-md transition-shadow">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">Approved Docs</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-teal-600">{data.statistics?.approved_documents || 0}</p></CardContent>
                </Card>
                <Card className="border-l-4 border-l-rose-600 shadow-sm hover:shadow-md transition-shadow">
                    <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-slate-500 uppercase">Final Grades</CardTitle></CardHeader>
                    <CardContent><p className="text-3xl font-bold text-rose-600">{data.statistics?.final_grades || 0}</p></CardContent>
                </Card>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Recent Documents */}
                <div className="space-y-4">
                    <h3 className="text-xl font-bold text-slate-800">Recent Documents</h3>
                    <div className="bg-white border border-slate-200 shadow-sm overflow-hidden">
                        <table className="w-full text-sm text-left">
                            <thead className="bg-slate-50 text-slate-500 border-b border-slate-200">
                                <tr>
                                    <th className="px-4 py-3 font-medium uppercase tracking-wider text-xs">Title</th>
                                    <th className="px-4 py-3 font-medium uppercase tracking-wider text-xs">Group</th>
                                    <th className="px-4 py-3 font-medium uppercase tracking-wider text-xs">Status</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                                {data.allDocuments?.slice(0, 5).map(doc => (
                                    <tr key={doc.id} className="hover:bg-slate-50">
                                        <td className="px-4 py-3 font-medium text-slate-900 truncate max-w-[150px]" title={doc.title}>{doc.title}</td>
                                        <td className="px-4 py-3 text-slate-600 truncate max-w-[150px]">{doc.groupName}</td>
                                        <td className="px-4 py-3">
                                            <span className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-medium uppercase border
                                                ${doc.status === 'APPROVED' ? 'bg-green-50 text-green-700 border-green-200' :
                                                    doc.status === 'REVISION_REQUESTED' ? 'bg-orange-50 text-orange-700 border-orange-200' :
                                                        'bg-blue-50 text-blue-700 border-blue-200'}`}>
                                                {doc.status}
                                            </span>
                                        </td>
                                    </tr>
                                )) || <tr><td colSpan="3" className="p-4 text-center text-slate-500">No documents found</td></tr>}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Deadlines or Other Info */}
                <div className="space-y-4">
                    <h3 className="text-xl font-bold text-slate-800">System Status</h3>
                    <div className="grid grid-cols-2 gap-4">
                        <Card className="bg-slate-50 border-slate-200">
                            <CardContent className="p-4 flex items-center justify-between">
                                <div>
                                    <p className="text-xs font-bold text-slate-500 uppercase">Pending Review</p>
                                    <p className="text-2xl font-bold text-orange-600">{data.statistics?.under_review_documents || 0}</p>
                                </div>
                                <Clock className="h-8 w-8 text-orange-200" />
                            </CardContent>
                        </Card>
                        <Card className="bg-slate-50 border-slate-200">
                            <CardContent className="p-4 flex items-center justify-between">
                                <div>
                                    <p className="text-xs font-bold text-slate-500 uppercase">Late Submissions</p>
                                    <p className="text-2xl font-bold text-red-600">{data.statistics?.late_submissions || 0}</p>
                                </div>
                                <AlertCircle className="h-8 w-8 text-red-200" />
                            </CardContent>
                        </Card>
                        <Card className="bg-slate-50 border-slate-200 col-span-2">
                            <CardContent className="p-4">
                                <p className="text-xs font-bold text-slate-500 uppercase mb-2">Grading Progress</p>
                                <div className="w-full bg-slate-200 rounded-full h-2.5 mb-1">
                                    <div
                                        className="bg-blue-600 h-2.5 rounded-full"
                                        style={{ width: `${data.statistics?.total_documents ? ((data.statistics.graded_documents / data.statistics.total_documents) * 100) : 0}%` }}>
                                    </div>
                                </div>
                                <p className="text-xs text-right text-slate-600">
                                    {data.statistics?.graded_documents || 0} / {data.statistics?.total_documents || 0} Graded
                                </p>
                            </CardContent>
                        </Card>
                    </div>
                </div>
            </div>

            <div className="space-y-6">
                <h3 className="text-xl font-bold text-slate-800 border-b pb-2">User Details</h3>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* Supervisors List */}
                    <Card className="max-h-96 overflow-y-auto">
                        <CardHeader className="sticky top-0 bg-white z-10 border-b"><CardTitle>Supervisors</CardTitle></CardHeader>
                        <CardContent className="pt-4">
                            {data.supervisors && data.supervisors.length > 0 ? (
                                <ul className="space-y-3">
                                    {data.supervisors.map(user => (
                                        <li key={user.id} className="flex flex-col p-2 bg-slate-50 rounded border border-slate-100">
                                            <span className="font-medium text-slate-800">{user.fullName}</span>
                                            <span className="text-xs text-slate-500">{user.email}</span>
                                        </li>
                                    ))}
                                </ul>
                            ) : <p className="text-slate-500 italic">No supervisors found.</p>}
                        </CardContent>
                    </Card>

                    {/* Committee List */}
                    <Card className="max-h-96 overflow-y-auto">
                        <CardHeader className="sticky top-0 bg-white z-10 border-b"><CardTitle>Committee Members</CardTitle></CardHeader>
                        <CardContent className="pt-4">
                            {data.committeeMembers && data.committeeMembers.length > 0 ? (
                                <ul className="space-y-3">
                                    {data.committeeMembers.map(user => (
                                        <li key={user.id} className="flex flex-col p-2 bg-slate-50 rounded border border-slate-100">
                                            <span className="font-medium text-slate-800">{user.fullName}</span>
                                            <span className="text-xs text-slate-500">{user.email}</span>
                                        </li>
                                    ))}
                                </ul>
                            ) : <p className="text-slate-500 italic">No committee members found.</p>}
                        </CardContent>
                    </Card>

                    {/* Students List */}
                    <Card className="max-h-96 overflow-y-auto">
                        <CardHeader className="sticky top-0 bg-white z-10 border-b"><CardTitle>Students</CardTitle></CardHeader>
                        <CardContent className="pt-4">
                            {data.students && data.students.length > 0 ? (
                                <ul className="space-y-3">
                                    {data.students.map(user => (
                                        <li key={user.id} className="flex flex-col p-2 bg-slate-50 rounded border border-slate-100">
                                            <span className="font-medium text-slate-800">{user.fullName}</span>
                                            <span className="text-xs text-slate-500">{user.email}</span>
                                        </li>
                                    ))}
                                </ul>
                            ) : <p className="text-slate-500 italic">No students found.</p>}
                        </CardContent>
                    </Card>
                </div>
            </div>
        </div>
    );

    return (
        <div className="animate-fade-in pb-10">
            {user.role?.name === 'STUDENT' && renderStudentDashboard()}
            {user.role?.name === 'SUPERVISOR' && renderSupervisorDashboard()}
            {user.role?.name === 'COMMITTEE_MEMBER' && renderCommitteeDashboard()}
            {user.role?.name === 'FYP_COMMITTEE' && renderFypCommitteeDashboard()}
        </div>
    );
};
export default DashboardPage;
