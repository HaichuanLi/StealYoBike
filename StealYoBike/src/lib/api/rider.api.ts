import type { AxiosResponse } from 'axios';
import { api } from './index';
import type { UserInfoResponse } from './types/auth.types';
import type {
	CheckoutRequest,
	CheckoutResponse,
	PastTripResponse,
	ReservationCancelResponse,
	ReservationInfoResponse,
	ReserveBikeRequest,
	ReserveBikeResponse,
	ReturnBikeRequest,
	ReturnBikeResponse,
	StationSubscriptionResponse,
	SubscriptionResponse,
	TripBillResponse,
	TripInfoResponse,
	TripResponse,
	UpdatePlanRequest
} from './types/rider.types';

export type TripFilterParams = {
	fromDate?: string;
	toDate?: string;
	type?: 'REGULAR' | 'ELECTRIC';
};

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
	},
	getTripBill: async (tripId: number): Promise<AxiosResponse<TripBillResponse>> => {
		return await api.get<TripBillResponse>(`/rider/trips/${tripId}/bill`);
	},
	payTrip: async (
		tripId: number,
		body: { paymentToken: string }
	): Promise<AxiosResponse<TripBillResponse>> => {
		return await api.post<TripBillResponse>(`/rider/trips/${tripId}/pay`, body);
	},
	getPastTrips: async (params?: TripFilterParams): Promise<AxiosResponse<PastTripResponse[]>> => {
		return await api.get<PastTripResponse[]>('/rider/trips/history', { params });
	},

	getTripDetails: async (tripId: number) => {
		return await api.get<import('./types/rider.types').TripResponse>(
			`/rider/trips/${tripId}/details`
		);
	},
	searchTripById: async (tripId: number): Promise<AxiosResponse<TripResponse>> => {
		return await api.get<TripResponse>(`/rider/trips/search`, { params: { tripId } });
	},
	subscribeToStation: async (stationId: number): Promise<AxiosResponse<SubscriptionResponse>> => {
		return await api.post<SubscriptionResponse>(`/rider/stations/${stationId}/subscribe`);
	},

	unsubscribeFromStation: async (
		stationId: number
	): Promise<AxiosResponse<SubscriptionResponse>> => {
		return await api.delete<SubscriptionResponse>(`/rider/stations/${stationId}/unsubscribe`);
	},

	getMySubscriptions: async (): Promise<AxiosResponse<StationSubscriptionResponse[]>> => {
		return await api.get<StationSubscriptionResponse[]>('/rider/subscriptions');
	},

	isSubscribed: async (stationId: number): Promise<AxiosResponse<boolean>> => {
		return await api.get<boolean>(`/rider/stations/${stationId}/is-subscribed`);
	},

	updatePlan: async (request: UpdatePlanRequest): Promise<AxiosResponse<UserInfoResponse>> => {
		return await api.put<UserInfoResponse>('/rider/me/plan', request);
	}
};
