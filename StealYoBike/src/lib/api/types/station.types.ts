export interface StationDetailResponse {
	stationId: number;
	name: string;
	status: 'ACTIVE' | 'OUT_OF_SERVICE';
	latitude: number;
	longitude: number;
	streetAddress: string;
	capacity: number;
	expiresAfterMinutes: number;
	docks: Dock[];
}

export interface Dock {
	dockId: number;
	status: 'EMPTY' | 'OCCUPIED' | 'OUT_OF_SERVICE';
	bike: Bike | null;
}

export interface Bike {
	bikeId: number;
	type: 'REGULAR' | 'ELECTRIC';
	status: 'Available' | 'Maintenance' | 'On Trip' | 'Reserved';
	reservationExpiration: string | null;
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
