import React, { createContext, useContext, useState, useEffect } from 'react';
import API from '../services/api';
import { adminAuthApi } from '../services/admin/api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    if (savedUser && token) {
      try {
        const parsedUser = JSON.parse(savedUser);
        const role = typeof parsedUser.role === 'string' ? parsedUser.role : parsedUser.role?.name || parsedUser.role?.authority || '';
        const isSavedAdmin = ['ADMIN', 'ROLE_ADMIN'].includes(role.toUpperCase());
        if (isSavedAdmin && !localStorage.getItem('adminToken')) {
          localStorage.removeItem('user');
          localStorage.removeItem('token');
        } else {
          setUser(parsedUser);
        }
      } catch (e) {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        localStorage.removeItem('adminToken');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, motDePasse) => {
    const response = await adminAuthApi.login({ email, motDePasse });
    const { token, ...userData } = response.data;
    localStorage.setItem('token', token);
    localStorage.setItem('adminToken', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const register = async (userData) => {
    const response = await API.post('/auth/register', userData);
    const { token, ...newUserData } = response.data;
    localStorage.removeItem('adminToken');
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(newUserData));
    setUser(newUserData);
    return newUserData;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('adminToken');
    localStorage.removeItem('user');
    setUser(null);
  };

  const isAdmin = () => {
    if (!user) return false;
    const role = typeof user.role === 'string' ? user.role : user.role?.name || user.role?.authority || '';
    return role.toUpperCase() === 'ROLE_ADMIN' || role.toUpperCase() === 'ADMIN';
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAdmin, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
