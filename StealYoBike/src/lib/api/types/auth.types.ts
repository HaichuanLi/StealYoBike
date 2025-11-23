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
	plan?: 'PAYPERRIDE' | 'MONTHLY' | 'ANNUAL';
}

export interface RegisterResponse {
	userId: number;
	email: string;
	username: string;
	role: string;
}
export interface UserInfoResponse {
	id: number;
	email: string;
	username: string;
	fullName: string;
	role: string;
	activeRole: string;
	dualRole: boolean;
	paymentToken: string | null;
	plan: string | null;
	tier: string;
	flexDollar: number;
	tripsLastYear: number;
	tripsLast3Months: number;
	tripsLast12Weeks: number;
}

