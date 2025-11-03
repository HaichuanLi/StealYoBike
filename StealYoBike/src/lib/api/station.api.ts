import type { StationDetailResponse, StationSummary } from '$lib/api/types';
import type { AxiosResponse } from 'axios';
import { api } from './index';

export const stationApi = {
	getStationDetails: (stationId: number): Promise<AxiosResponse<StationDetailResponse>> =>
		api.get(`/station/${stationId}`),
	getAllStations: (): Promise<AxiosResponse<StationSummary[]>> => api.get('/station/list')
};
