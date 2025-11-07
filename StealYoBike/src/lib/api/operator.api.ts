import type {} from '$lib/api/types';
import type { AxiosResponse } from 'axios';
import { api } from './index';
import type {
	ChangeStationResponse,
	RebalanceRequest,
	RebalanceResponse,
	RestoreInitialStateRequest,
	RestoreInitialStateResponse
} from './types/operator.type';
import type { PastTripResponse } from './types/rider.types';

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
	},
	toggleBikeStatus: async (bikeId: number): Promise<AxiosResponse<ChangeStationResponse>> => {
		try {
			const response = await api.post(`operator/bikes/out-of-service`, {
				bikeId: bikeId
			});
			console.warn(response);
			return response;
		} catch (error) {
			console.error('Error toggling bike status:', error);
			throw error;
		}
	},
	rebalanceBikes: async (request: RebalanceRequest): Promise<AxiosResponse<RebalanceResponse>> => {
		try {
			const response = await api.post<RebalanceResponse>('operator/rebalance', request);
			console.log('Rebalance response:', response.data);
			return response;
		} catch (error) {
			console.error('Error rebalancing bikes:', error);
			throw error;
		}
	},
	restoreInitialState: async (
		request: RestoreInitialStateRequest = {}
	): Promise<AxiosResponse<RestoreInitialStateResponse>> => {
		try {
			const response = await api.post<RestoreInitialStateResponse>(
				'operator/restore-initial-state',
				request
			);
			console.log('Restore initial state response:', response.data);
			return response;
		} catch (error) {
			console.error('Error restoring initial state:', error);
			throw error;
		}
	},

    getAllPastTrips: async (): Promise<AxiosResponse<PastTripResponse[]>> => {
        return await api.get<PastTripResponse[]>('operator/trips/history');
    }
};
