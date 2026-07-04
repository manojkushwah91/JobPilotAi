import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse } from '@/types';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1',
  timeout: 30_000,
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Skip 401 handling for auth endpoints (login, register, forgot-password)
    const isAuthEndpoint = originalRequest.url?.includes('/auth/');
    if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      originalRequest._retry = true;
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) throw new Error('No refresh token');

        const { data } = await axios.post<ApiResponse<{ accessToken: string }>>(
          `${apiClient.defaults.baseURL}/auth/refresh`,
          { refreshToken }
        );

        if (data.data?.accessToken) {
          localStorage.setItem('accessToken', data.data.accessToken);
          document.cookie = `accessToken=${data.data.accessToken}; path=/; max-age=900; SameSite=Lax`;
          originalRequest.headers.Authorization = `Bearer ${data.data.accessToken}`;
          return apiClient(originalRequest);
        }
      } catch {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        document.cookie = 'accessToken=; path=/; max-age=0';
        document.cookie = 'refreshToken=; path=/; max-age=0';
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
      }
    }

    return Promise.reject(error);
  }
);

export async function apiGet<T>(url: string, params?: Record<string, unknown>): Promise<ApiResponse<T>> {
  const { data } = await apiClient.get<ApiResponse<T>>(url, { params });
  return data;
}

export async function apiPost<T>(url: string, body?: unknown): Promise<ApiResponse<T>> {
  const { data } = await apiClient.post<ApiResponse<T>>(url, body);
  return data;
}

export async function apiPut<T>(url: string, body?: unknown): Promise<ApiResponse<T>> {
  const { data } = await apiClient.put<ApiResponse<T>>(url, body);
  return data;
}

export async function apiDelete<T>(url: string): Promise<ApiResponse<T>> {
  const { data } = await apiClient.delete<ApiResponse<T>>(url);
  return data;
}

export default apiClient;
