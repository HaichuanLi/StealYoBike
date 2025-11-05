import type { StationDetailResponse, StationSummary } from '$lib/api/types';
import type { AxiosResponse } from 'axios';
import { api } from './index';

export const stationApi = {
	getStationDetails: async (stationId: number): Promise<AxiosResponse<StationDetailResponse>> =>
		await api.get(`/station/${stationId}`),
	getAllStations: async (): Promise<AxiosResponse<StationSummary[]>> =>
		await api.get('/station/list')
};
