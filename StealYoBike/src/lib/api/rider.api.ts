import type { AxiosResponse } from 'axios';
import { api } from './index';
import type {
	CheckoutRequest,
	CheckoutResponse,
	ReservationInfoResponse,
	ReserveBikeRequest,
	ReserveBikeResponse,
	ReturnBikeRequest,
	ReturnBikeResponse
} from './types/rider.types';

export const riderApi = {
	reserveBike: async (request: ReserveBikeRequest): Promise<AxiosResponse<ReserveBikeResponse>> => {
		return await api.post<ReserveBikeResponse>('/rider/reserve', request);
	},
	checkoutBike: async (request: CheckoutRequest): Promise<AxiosResponse<CheckoutResponse>> => {
		return await api.post<CheckoutResponse>('/rider/checkout', request);
	},
	returnBike: async (request: ReturnBikeRequest): Promise<AxiosResponse<ReturnBikeResponse>> => {
		return await api.post<ReturnBikeResponse>('/rider/return', request);
	},
	getCurrentReservation: async (): Promise<AxiosResponse<ReservationInfoResponse>> => {
		return await api.get<ReservationInfoResponse>(`/rider/current-reservation`);
	}
};
