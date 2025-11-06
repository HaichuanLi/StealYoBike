<script lang="ts">
	import type { StationSummary } from '$lib/api/types';
	import type { TripInfoResponse } from '$lib/api/types/rider.types';
	import Button from '$lib/components/Button/Button.svelte';

	interface Props {
		trip: TripInfoResponse | null;
		loading: boolean;
		selectedStation: StationSummary | null;
	}

	let { trip, loading, selectedStation }: Props = $props();

	function formatBikeType(type: string) {
		return type === 'ELECTRIC' ? 'Electric' : 'Regular';
	}
</script>

{#if trip}
	<div class="mt-4 rounded-xl bg-blue-50 p-4">
		<h3 class="mb-2 text-lg font-semibold">Active Trip</h3>
		<div class="mb-3 rounded bg-blue-100 p-3 text-center">
			<p class="font-semibold text-blue-700">You're on a ride!</p>
			<p class="text-sm text-blue-600">Remember to return the bike to a station when done.</p>
		</div>
		<div class="flex flex-row justify-between">
			<div>
				<p><span class="font-medium">Trip ID:</span> {trip.tripId}</p>
				<p><span class="font-medium">Bike ID:</span> {trip.bikeId}</p>
				<p><span class="font-medium">Bike Type:</span> {formatBikeType(trip.bikeType)}</p>
				<p><span class="font-medium">Start Station:</span> {trip.startStationName}</p>
				<p>
					<span class="font-medium">Started At:</span>
					{new Date(trip.startTime).toLocaleString()}
				</p>
				<p>
					<span class="font-medium">Status:</span>
					<span class="font-semibold text-green-600">{trip.status}</span>
				</p>
			</div>
			{#if !selectedStation}
				<div class="my-2 border-l border-blue-200"></div>
				<div class="self-center p-3 text-center">
					<p class="font-semibold text-red-600">Select a station to return the bike.</p>
				</div>
			{/if}
		</div>
	</div>
{:else if !loading}
	<div class="mt-4 rounded-xl bg-gray-50 p-4 text-center">
		<p class="text-gray-600">No active trip.</p>
	</div>
{/if}
