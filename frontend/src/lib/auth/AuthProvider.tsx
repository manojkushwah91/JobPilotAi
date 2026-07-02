'use client';

import { createContext, useContext, useCallback, type ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { apiPost } from '@/lib/api/client';
import { API } from '@/lib/api/endpoints';
import type { User, AuthTokens, LoginRequest, RegisterRequest } from '@/types';
import { create } from 'zustand';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setUser: (user: User | null) => void;
  setLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: true,
  setUser: (user) => set({ user, isAuthenticated: user !== null, isLoading: false }),
  setLoading: (isLoading) => set({ isLoading }),
}));

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const router = useRouter();
  const { user, isAuthenticated, isLoading, setUser, setLoading } = useAuthStore();

  const login = useCallback(async (credentials: LoginRequest) => {
    const { data: tokens } = await apiPost<AuthTokens>(API.auth.login, credentials);
    localStorage.setItem('accessToken', tokens.accessToken);
    localStorage.setItem('refreshToken', tokens.refreshToken);
    const { data: userData } = await apiPost<User>('/auth/me');
    setUser(userData);
    router.push('/dashboard');
  }, [router, setUser]);

  const register = useCallback(async (data: RegisterRequest) => {
    const { data: tokens } = await apiPost<AuthTokens>(API.auth.register, data);
    localStorage.setItem('accessToken', tokens.accessToken);
    localStorage.setItem('refreshToken', tokens.refreshToken);
    const { data: userData } = await apiPost<User>('/auth/me');
    setUser(userData);
    router.push('/dashboard');
  }, [router, setUser]);

  const logout = useCallback(async () => {
    try { await apiPost(API.auth.logout); } catch { /* ignore */ }
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUser(null);
    router.push('/login');
  }, [router, setUser]);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

export function useUser() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useUser must be used within AuthProvider');
  return ctx.user;
}
