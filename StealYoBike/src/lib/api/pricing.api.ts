import type { EBikePricing, StandardBikePricing } from '$lib/api/types/pricing.types';
import type { AxiosResponse } from 'axios';
import { api } from './index';

export interface PricingResponse {
	standardBike: StandardBikePricing;
	eBike: EBikePricing;
}

export const pricingApi = {
	getCurrentPricing: async (): Promise<AxiosResponse<PricingResponse>> => {
		return await api.get<PricingResponse>('/pricing');
	}
};
