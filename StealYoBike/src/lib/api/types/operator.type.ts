export interface ChangeStationResponse {
	stationId: number;
	status: 'ACTIVE' | 'OUT_OF_SERVICE';
}
export interface ChangeStationStateRequest {
	stationId: number;
}
