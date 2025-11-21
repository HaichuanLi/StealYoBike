import type {
	LoginRequest,
	LoginResponse,
	RegisterRequest,
	RegisterResponse,
	UserInfoResponse
} from '$lib/api/types';
import type { AxiosResponse } from 'axios';
import { api } from './index';

export const authApi = {
	login: async (credentials: LoginRequest): Promise<AxiosResponse<LoginResponse>> => {
		const response = await api.post<LoginResponse>('/auth/login', credentials);
		if (response.data.token) {
			localStorage.setItem('authToken', response.data.token);
		}

		const authHeader = response.headers['authorization'];
		if (authHeader && authHeader.startsWith('Bearer ')) {
			localStorage.setItem('authToken', authHeader.substring(7));
		}

		return response;
	},

	register: async (userInfo: RegisterRequest): Promise<AxiosResponse<RegisterResponse>> => {
		const response = await api.post<RegisterResponse>('/auth/register', userInfo);
		return response;
	},

	getCurrentUser: async (): Promise<AxiosResponse<UserInfoResponse>> => {
		const response = await api.get<UserInfoResponse>('/auth/me');
		return response;
	},

	logout: () => {
		localStorage.removeItem('authToken');
		// Clear any cookies if using cookie-based auth
		document.cookie.split(';').forEach((c) => {
			document.cookie = c
				.replace(/^ +/, '')
				.replace(/=.*/, '=;expires=' + new Date().toUTCString() + ';path=/');
		});
		//window.location.href = '/login';
	},

	isAuthenticated: (): boolean => {
		return !!localStorage.getItem('authToken');
	},

	getToken: (): string | null => {
		return localStorage.getItem('authToken');
	},

	updatePaymentToken: async (paymentToken: string): Promise<AxiosResponse<UserInfoResponse>> => {
		const response = await api.put<UserInfoResponse>('/auth/me/payment-token', {
			paymentToken
		});
		return response;
	}
};
