export interface StationDetailResponse {
	stationId: number;
	name: string;
	status: 'ACTIVE' | 'OUT_OF_SERVICE';
	latitude: number;
	longitude: number;
	streetAddress: string;
	capacity: number;
	expiresAfterMinutes: number;
	docks: {
		id: number;
		status: 'EMPTY' | 'OCCUPIED' | 'OUT_OF_SERVICE';
		bike: {
			id: number;
			type: 'REGULAR' | 'ELECTRIC';
			status: 'Available' | 'Maintenance' | 'On Trip' | 'Reserved';
			reservationExpirationTime: string | null;
		} | null;
	}[];
}

export interface StationSummary {
	stationId: number;
	name: string;
	status: 'ACTIVE' | 'OUT_OF_SERVICE';
	latitude: number;
	longitude: number;
	streetAddress: string;
	availableBikes: number;
	availableDocks: number;
	capacity: number;
}
