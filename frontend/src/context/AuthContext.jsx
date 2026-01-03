import { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';
// import { jwtDecode } from 'jwt-decode'; // Not strictly needed if we hit /me

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const normalizeUser = (userData) => {
        // Transform UserDTO flat fields to nested objects expected by components
        return {
            ...userData,
            role: {
                id: userData.roleId,
                name: userData.roleName,
            },
            group: userData.groupId ? {
                id: userData.groupId,
                name: userData.groupName
            } : null
        };
    };

    useEffect(() => {
        const checkAuth = async () => {
            const token = localStorage.getItem('token');
            if (token) {
                try {
                    const res = await api.get('/auth/me');
                    setUser(normalizeUser(res.data));
                } catch (error) {
                    console.error("Auth check failed", error);
                    localStorage.removeItem('token');
                    setUser(null);
                }
            }
            setLoading(false);
        };
        checkAuth();
    }, []);

    const login = async (email, password) => {
        const response = await api.post('/auth/login', { email, password });
        // response.data is JwtResponse: { token, type, id, email, fullName, role (string), groupId }
        const { token } = response.data;
        if (token) {
            localStorage.setItem('token', token);
            // Fetch full user details (UserDTO) to get roleId which is missing in login response
            const meRes = await api.get('/auth/me');
            const userData = normalizeUser(meRes.data);
            setUser(userData);
            return userData;
        }
    };

    const register = async (data) => {
        const response = await api.post('/auth/register', data);
        return response.data;
    };

    const logout = () => {
        localStorage.removeItem('token');
        setUser(null);
        window.location.href = '/login';
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, register, loading }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
