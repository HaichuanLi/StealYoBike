<script lang="ts">
	import { operatorApi } from '$lib/api/operator.api';
	import type { StationSummary } from '$lib/api/types';
	import type { BikeType, RebalanceResponse } from '$lib/api/types/operator.type';
	import Button from '../Button/Button.svelte';

	let {
		stations,
		onRebalanceComplete
	}: {
		stations: StationSummary[];
		onRebalanceComplete?: (response: RebalanceResponse) => void;
	} = $props();

	let fromStationId = $state<number | null>(null);
	let toStationId = $state<number | null>(null);
	let bikeType = $state<BikeType>('REGULAR');
	let count = $state<number>(1);
	let isLoading = $state<boolean>(false);
	let errorMessage = $state<string | null>(null);
	let successMessage = $state<string | null>(null);

	const handleRebalance = async () => {
		if (!fromStationId || !toStationId) {
			errorMessage = 'Please select both source and destination stations';
			return;
		}

		if (fromStationId === toStationId) {
			errorMessage = 'Source and destination stations must be different';
			return;
		}

		if (count < 1) {
			errorMessage = 'Count must be at least 1';
			return;
		}

		isLoading = true;
		errorMessage = null;
		successMessage = null;

		try {
			const response = await operatorApi.rebalanceBikes({
				fromStationId,
				toStationId,
				bikeType,
				count
			});

			const result = response.data;
			successMessage = `Successfully moved ${result.moved} ${bikeType.toLowerCase()} bike(s) from Station ${result.fromStationId} to Station ${result.toStationId}`;

			if (result.moved < count) {
				successMessage += ` (Requested ${count}, but only ${result.moved} could be moved)`;
			}

			if (onRebalanceComplete) {
				onRebalanceComplete(result);
			}

			// Reset form on success
			count = 1;
		} catch (error: any) {
			console.error('Rebalance error:', error);
			errorMessage = error?.response?.data?.detail || error?.message || 'Failed to rebalance bikes';
		} finally {
			isLoading = false;
		}
	};

	const activeStations = $derived(stations.filter((s) => s.status === 'ACTIVE'));
</script>

<div class="flex flex-col gap-4 rounded-xl bg-white p-6 shadow-md">
	<h2 class="text-2xl font-bold text-gray-800">Rebalance Bikes</h2>

	<div class="flex flex-col gap-4">
		<!-- From Station -->
		<div class="flex flex-col gap-2">
			<label for="from-station" class="font-semibold text-gray-700">From Station</label>
			<select
				id="from-station"
				bind:value={fromStationId}
				class="rounded-lg border-2 border-gray-300 px-3 py-2 focus:border-teal-500 focus:outline-none"
			>
				<option value={null}>Select source station</option>
				{#each activeStations as station}
					<option value={station.stationId}>
						{station.name} (Available: {station.availableBikes})
					</option>
				{/each}
			</select>
		</div>

		<!-- To Station -->
		<div class="flex flex-col gap-2">
			<label for="to-station" class="font-semibold text-gray-700">To Station</label>
			<select
				id="to-station"
				bind:value={toStationId}
				class="rounded-lg border-2 border-gray-300 px-3 py-2 focus:border-teal-500 focus:outline-none"
			>
				<option value={null}>Select destination station</option>
				{#each activeStations as station}
					<option value={station.stationId}>
						{station.name} (Empty docks: {station.availableDocks})
					</option>
				{/each}
			</select>
		</div>

		<!-- Bike Type -->
		<div class="flex flex-col gap-2">
			<label for="bike-type" class="font-semibold text-gray-700">Bike Type</label>
			<select
				id="bike-type"
				bind:value={bikeType}
				class="rounded-lg border-2 border-gray-300 px-3 py-2 focus:border-teal-500 focus:outline-none"
			>
				<option value="REGULAR">Regular</option>
				<option value="ELECTRIC">Electric</option>
			</select>
		</div>

		<!-- Count -->
		<div class="flex flex-col gap-2">
			<label for="count" class="font-semibold text-gray-700">Number of Bikes</label>
			<input
				id="count"
				type="number"
				min="1"
				bind:value={count}
				class="rounded-lg border-2 border-gray-300 px-3 py-2 focus:border-teal-500 focus:outline-none"
			/>
		</div>

		<!-- Error Message -->
		{#if errorMessage}
			<div class="rounded-lg bg-red-100 p-3 text-red-700">
				{errorMessage}
			</div>
		{/if}

		<!-- Success Message -->
		{#if successMessage}
			<div class="rounded-lg bg-green-100 p-3 text-green-700">
				{successMessage}
			</div>
		{/if}

		<!-- Action Button -->
		<div class="flex justify-center">
			<Button
				text={isLoading ? 'Rebalancing...' : 'Rebalance Bikes'}
				variant="teal"
				onclick={handleRebalance}
				disable={isLoading}
			/>
		</div>
	</div>
</div>
