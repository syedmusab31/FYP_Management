import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import { useAuth } from '../context/AuthContext';
import { Award, FileText, Clock } from 'lucide-react';

const StudentGradesPage = () => {
    const { user } = useAuth();
    const [grades, setGrades] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchGrades();
    }, [user]);

    const fetchGrades = async () => {
        setLoading(true);
        try {
            const groupId = user.group?.id || user.groupId;
            if (groupId) {
                const res = await api.get(`/grades/group/${groupId}`);
                // Filter to show only final (released) grades to students
                const finalGrades = res.data.filter(grade => grade.isFinal);
                setGrades(finalGrades);
            }
        } catch (error) {
            console.error("Failed to fetch grades", error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="flex justify-center p-10"><Clock className="animate-spin h-8 w-8 text-blue-600" /></div>;

    return (
        <div className="space-y-6 animate-fade-in p-6">
            <div className="flex justify-between items-center">
                <h2 className="text-3xl font-bold text-slate-800">My Grades</h2>
                <span className="text-sm bg-blue-100 text-blue-700 px-3 py-1 rounded-full font-bold">
                    {grades.length} Released
                </span>
            </div>

            {grades.length > 0 ? (
                <div className="grid gap-4">
                    {grades.map(grade => (
                        <Card key={grade.id} className="border-l-4 border-l-green-500 hover:shadow-lg transition-shadow">
                            <CardContent className="p-6">
                                <div className="flex justify-between items-start">
                                    <div className="flex-1">
                                        <div className="flex items-center space-x-3 mb-2">
                                            <FileText className="h-5 w-5 text-slate-500" />
                                            <h3 className="font-bold text-lg text-slate-800">
                                                {grade.document?.title || 'Document'}
                                            </h3>
                                        </div>
                                        <p className="text-sm text-slate-600 mb-3">
                                            Type: <span className="font-medium">{grade.document?.type}</span>
                                        </p>
                                        <div className="bg-slate-50 p-4 rounded border border-slate-200">
                                            <p className="text-sm font-medium text-slate-700 mb-2">Feedback:</p>
                                            <p className="text-sm text-slate-600 italic">"{grade.feedback}"</p>
                                        </div>
                                        <p className="text-xs text-slate-400 mt-3">
                                            Graded by {grade.gradedBy?.fullName} on {new Date(grade.createdAt).toLocaleDateString()}
                                        </p>
                                    </div>
                                    <div className="ml-6 text-center">
                                        <div className="flex items-center justify-center w-24 h-24 rounded-full bg-gradient-to-br from-green-400 to-green-600 text-white shadow-lg">
                                            <div>
                                                <Award className="h-6 w-6 mx-auto mb-1" />
                                                <p className="text-2xl font-bold">{grade.score}</p>
                                                <p className="text-xs">/ 10</p>
                                            </div>
                                        </div>
                                        <span className="inline-block mt-2 text-xs bg-green-100 text-green-700 px-2 py-1 rounded font-bold uppercase">
                                            Released
                                        </span>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            ) : (
                <div className="text-center py-20 bg-slate-50 border-2 border-dashed border-slate-200 rounded">
                    <Award className="h-12 w-12 text-slate-300 mx-auto mb-4" />
                    <p className="text-slate-500 text-lg font-medium">No grades released yet</p>
                    <p className="text-slate-400 text-sm mt-2">
                        Your grades will appear here once they are finalized by the FYP Committee
                    </p>
                </div>
            )}
        </div>
    );
};

export default StudentGradesPage;
