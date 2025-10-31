export interface CheckoutRequest {
	reservationId: number;
	pin: string;
}
export interface CheckoutResponse {
	tripId: number;
	bikeId: number;
	startStationId: number;
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
	expirationTime: string;
	pin: string;
}
export interface ReturnBikeRequest {
	tripId: number;
	endStationId: number;
}
export interface ReturnBikeResponse {
	tripId: number;
	bikeId: number;
	endStationId: number;
	endTime: string;
	totalCost: number;
	status: string;
}
