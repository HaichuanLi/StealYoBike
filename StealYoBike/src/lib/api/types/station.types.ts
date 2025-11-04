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
		dockId: number;
		status: 'EMPTY' | 'OCCUPIED' | 'OUT_OF_SERVICE';
		bike: {
			bikeId: number;
			type: 'REGULAR' | 'ELECTRIC';
			status: 'Available' | 'Maintenance' | 'On Trip' | 'Reserved';
			reservationExpiration: string | null;
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
