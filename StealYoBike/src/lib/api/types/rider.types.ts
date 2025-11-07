export interface CheckoutRequest {
	reservationId: number;
	pin: string;
}

export interface CheckoutResponse {
	tripId: number;
	bikeId: number;
	bikeType: 'REGULAR' | 'ELECTRIC';
	startStationId: number;
	startStationName: string;
	startTime: string;
	status: string;
}

export interface ReserveBikeRequest {
	stationId: number;
	bikeType: 'REGULAR' | 'ELECTRIC';
}

export interface ReserveBikeResponse {
	reservationId: number;
	bikeId: number;
	stationId: number;
	expiresAt: string;
	pin: string;
}

export interface ReturnBikeRequest {
	tripId: number;
	stationId: number;
}

export interface ReturnBikeResponse {
	tripId: number;
	bikeId: number;
	stationId: number;
	endTime: string;
	totalCost: number;
	status: string;
}

export interface TripBillResponse {
	billId: number;
	tripId: number;
	totalAmount: number;
	createdAt: string | null;
	startTime: string | null;
	endTime: string | null;
	durationMinutes: number;
	baseFee: number;
	usageCost: number;
	electricCharge: number;
	discountAmount: number;
	endStationId: number | null;
	endStationName: string | null;
	trip: TripInfoResponse;
}

export interface ReservationInfoResponse {
	reservationId: number;
	bikeId: number;
	stationId: number;
	expiresAt: string;
	pin: string;
}

export interface ReservationCancelResponse {
	reservationId: number;
	status: string;
}

export interface TripInfoResponse {
	tripId: number;
	bikeId: number;
	bikeType: 'REGULAR' | 'ELECTRIC';
	startStationId: number;
	startStationName: string;
	startTime: string;
	status: string;
}
