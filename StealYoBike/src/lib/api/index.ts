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

api.interceptors.request.use(
	(config) => {
		const token = localStorage.getItem('authToken');

		if (token) {
			config.headers.Authorization = `Bearer ${token}`;
			console.debug(`[API Request] ${config.method?.toUpperCase()} ${config.url} with token`);
		} else {
			console.debug(`[API Request] ${config.method?.toUpperCase()} ${config.url} without token`);
		}

		if (import.meta.env.DEV) {
			console.debug('Request config:', {
				url: config.url,
				method: config.method,
				headers: config.headers,
				withCredentials: config.withCredentials
			});
		}

		return config;
	},
	(error) => {
		console.error('[API Request Error]', error);
		return Promise.reject(error);
	}
);

api.interceptors.response.use(
	(response) => {
		if (import.meta.env.DEV) {
			console.debug(
				`[API Response] ${response.config.method?.toUpperCase()} ${response.config.url}`,
				response.status
			);
		}

		const newToken = response.headers['authorization'];
		if (newToken && newToken.startsWith('Bearer ')) {
			localStorage.setItem('authToken', newToken.substring(7));
			console.debug('[API] Token refreshed from response header');
		}

		return response;
	},
	(error: AxiosError) => {
		const { response } = error;

		console.error(`[API Error] ${error.config?.method?.toUpperCase()} ${error.config?.url}`, {
			status: response?.status,
			data: response?.data,
			headers: response?.headers
		});

		if (response?.status === 401) {
			console.warn('[API] Received 401 Unauthorized, clearing auth and redirecting to login');
			// Clear auth token
			localStorage.removeItem('authToken');
		} else if (response?.status === 403) {
			console.error('[API] Received 403 Forbidden - check permissions or authentication');
		}

		return Promise.reject(error);
	}
);

// Export everything from specific API modules
export * from './auth.api';
export * from './station.api';
