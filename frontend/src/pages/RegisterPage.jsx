import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import { Eye, EyeOff } from 'lucide-react';

const RegisterPage = () => {
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: '',
        roleId: 1, // Default to Student role
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const { register } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        // Convert roleId to number
        const finalValue = name === 'roleId' ? Number(value) : value;
        setFormData({ ...formData, [name]: finalValue });
    };
    const [showPassword, setShowPassword] = useState(false);
    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setIsLoading(true);
        try {
            const data = { ...formData };
            await register(data);
            setSuccess('Registration successful! Redirecting to login...');
            setTimeout(() => navigate('/login'), 2000);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to register');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8 bg-white p-10 shadow-xl border border-slate-100 animate-fade-in relative">
                <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-500 to-indigo-600"></div>
                <div>
                    <h2 className="mt-2 text-center text-3xl font-extrabold text-slate-900">Create your account</h2>
                    <p className="mt-2 text-center text-sm text-slate-600">Join the FYP Management System</p>
                </div>
                <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
                    {error && <div className="p-3 bg-red-50 text-red-700 text-sm border-l-4 border-red-500">{error}</div>}
                    {success && <div className="p-3 bg-green-50 text-green-700 text-sm border-l-4 border-green-500">{success}</div>}

                    <div className="space-y-4">
                        <Input name="fullName" placeholder="Full Name" required value={formData.fullName} onChange={handleChange} />
                        <Input name="email" type="email" placeholder="Email address" required value={formData.email} onChange={handleChange} />
                        {/* adding icon to password feild */}
                        <div className="relative flex items-center">
                            <input
                                name="password"
                                type={showPassword ? "text" : "password"}
                                placeholder="Password"
                                required
                                value={formData.password}
                                onChange={handleChange}
                                className="w-full pr-10 py-2 border focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                            <button
                                type="button"
                                onClick={togglePasswordVisibility}
                                className="absolute right-3 text-gray-400 hover:text-gray-600 focus:outline-none"
                            >
                                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                            </button>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Role</label>
                            <select
                                name="roleId"
                                value={formData.roleId || 1}
                                onChange={handleChange}
                                className="w-full h-10 border border-slate-300 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            >
                                <option value={1}>Student</option>
                                <option value={2}>Supervisor</option>
                                <option value={3}>Committee Member</option>
                                <option value={4}>FYP Committee</option>
                            </select>
                        </div>
                    </div>

                    <div>
                        <Button type="submit" className="w-full" isLoading={isLoading}>Register</Button>
                    </div>
                    <div className="text-center text-sm">
                        <Link to="/login" className="font-medium text-blue-600 hover:text-blue-500">
                            Already have an account? Sign in
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
};
export default RegisterPage;
