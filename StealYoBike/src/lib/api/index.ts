import axios, { type AxiosError, type AxiosInstance } from 'axios';

// Base axios instance
export const api: AxiosInstance = axios.create({
	baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
	headers: {
		'Content-Type': 'application/json'
	},
	withCredentials: true,
	timeout: 10000
});

// Request interceptor (add auth token)
api.interceptors.request.use(
	(config) => {
		const token = localStorage.getItem('authToken');
		if (token) {
			config.headers.Authorization = `Bearer ${token}`;
		}
		return config;
	},
	(error) => Promise.reject(error)
);

// Response interceptor (handle errors globally)
api.interceptors.response.use(
	(response) => response,
	(error: AxiosError) => {
		if (error.response?.status === 401) {
			// Redirect to login on unauthorized
			localStorage.removeItem('authToken');
			window.location.href = '/login';
		}
		return Promise.reject(error);
	}
);

// Export everything from specific API modules
export * from './auth.api';
