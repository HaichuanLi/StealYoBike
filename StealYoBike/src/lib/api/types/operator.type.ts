export interface ChangeStationResponse {
	stationId: number;
	status: 'ACTIVE' | 'OUT_OF_SERVICE';
}
export interface ChangeStationStateRequest {
	stationId: number;
}

export type BikeType = 'REGULAR' | 'ELECTRIC';

export interface RebalanceRequest {
	fromStationId: number;
	toStationId: number;
	bikeType: BikeType;
	count: number;
}

export interface RebalanceResponse {
	moved: number;
	fromStationId: number;
	toStationId: number;
}
