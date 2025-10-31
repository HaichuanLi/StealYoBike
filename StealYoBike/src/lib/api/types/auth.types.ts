export interface LoginRequest {
	usernameOrEmail: string;
	password: string;
}

export interface LoginResponse {
	token: string;
	userId: number;
	role: string;
}

export interface RegisterRequest {
	fullName: string;
	address: string;
	email: string;
	username: string;
	password: string;
	paymentToken: string;
}

export interface RegisterResponse {
	userId: number;
	email: string;
	username: string;
	role: string;
}
