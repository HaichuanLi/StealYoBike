import axios, { type AxiosResponse } from 'axios';

const api = axios.create({
	baseURL: 'http://localhost:8080/api',
	headers: {
		'Content-Type': 'application/json'
	},
	withCredentials: true
});

export type RegisterReq = {
	fullName: string;
	address: string;
	email: string;
	username: string;
	password: string;
	paymentToken: string;
};

export type LoginReq = {
	username: string;
	password: string;
};

export type AuthResponse = {
	token?: string;
	userId: number;
	username: string;
	message?: string;
};

export type ErrorResponse = {
	message: string;
	status: number;
};

api.interceptors.response.use(
	(response) => response,
	(error) => {
		if (error.response) {
			console.error('API Error:', error.response.data);
		} else if (error.request) {
			console.error('Network Error:', error.message);
		}
		return Promise.reject(error);
	}
);

export const userApi = {
	login: (credentials: LoginReq): Promise<AxiosResponse<AuthResponse>> =>
		api.post('/auth/login', credentials),

	register: (userInfo: RegisterReq): Promise<AxiosResponse<AuthResponse>> =>
		api.post('/auth/register', userInfo),

	logout: (): Promise<AxiosResponse<void>> => api.post('/auth/logout')
};

export default api;
