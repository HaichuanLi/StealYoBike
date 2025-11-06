import type { AxiosResponse } from 'axios';
import { api } from './index';
import type {
	CheckoutRequest,
	CheckoutResponse,
	ReservationCancelResponse,
	ReservationInfoResponse,
	ReserveBikeRequest,
	ReserveBikeResponse,
	ReturnBikeRequest,
	ReturnBikeResponse,
	TripInfoResponse
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
	},
	cancelCurrentReservation: async (): Promise<AxiosResponse<ReservationCancelResponse>> => {
		return await api.post<ReservationCancelResponse>(`/rider/cancel-reservation`);
	},
	getCurrentTrip: async (): Promise<AxiosResponse<TripInfoResponse>> => {
		return await api.get<TripInfoResponse>(`/rider/current-trip`);
	}
};
