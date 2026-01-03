import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import { Construction } from 'lucide-react';

const PlaceholderPage = ({ title }) => {
    return (
        <Card className="max-w-2xl mx-auto mt-10 text-center py-10 border-dashed border-2 bg-slate-50/50">
            <CardContent>
                <Construction className="h-16 w-16 mx-auto text-slate-300 mb-6" />
                <h2 className="text-2xl font-bold text-slate-800 mb-2">{title}</h2>
                <p className="text-slate-500">This feature is currently under development.</p>
            </CardContent>
        </Card>
    );
};

export default PlaceholderPage;
