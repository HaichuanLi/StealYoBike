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

export type RestoreInitialStateRequest = Record<string, never>;

export interface RestoreInitialStateResponse {
	stations: number;
	docks: number;
	bikes: number;
	message: string;
	restoredAt: string; // ISO 8601 timestamp
}
