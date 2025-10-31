import type {
	LoginRequest,
	LoginResponse,
	RegisterRequest,
	RegisterResponse
} from '$lib/api/types';
import type { AxiosResponse } from 'axios';
import { api } from './index';

export const authApi = {
	login: (credentials: LoginRequest): Promise<AxiosResponse<LoginResponse>> =>
		api.post('/auth/login', credentials),

	register: (userInfo: RegisterRequest): Promise<AxiosResponse<RegisterResponse>> =>
		api.post('/auth/register', userInfo)
};
