import type {} from '$lib/api/types';
import type { AxiosResponse } from 'axios';
import { api } from './index';
import type { ChangeStationResponse } from './types/operator.type';

export const operatorApi = {
	toggleStationStatus: async (stationId: number): Promise<AxiosResponse<ChangeStationResponse>> => {
		try {
			const response = await api.post(`operator/stations/out-of-service`, {
				stationId: stationId
			});
			console.warn(response);
			return response;
		} catch (error) {
			console.error('Error toggling station status:', error);
			throw error;
		}
	}
};
