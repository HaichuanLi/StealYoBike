<script lang="ts">
	import { stationApi } from '$lib/api';
	import { operatorApi } from '$lib/api/operator.api';
	import type { StationDetailResponse } from '$lib/api/types';
	import { stationsSnapshot } from '$lib/stores/stations';
	import { onDestroy } from 'svelte';
	import Bike from '../Bike/Bike.svelte';
	import Button from '../Button/Button.svelte';
	import Dock from '../Dock/Dock.svelte';
	let { selectedStation }: { selectedStation: number } = $props();

	let station = $state<StationDetailResponse | null>(null);
	let hovered = $state<number | null>(null);
	// Remember the last stationId we fetched to avoid duplicate network calls
	let lastFetchedStationId = $state<number | null>(null);

	$effect(() => {
		// If no station selected, clear state
		if (!selectedStation) {
			station = null;
			lastFetchedStationId = null;
			return;
		}

		// Avoid refetch if the selected station id hasn't changed
		if (lastFetchedStationId === selectedStation) return;

		lastFetchedStationId = selectedStation;

		(async () => {
			try {
				const stationDetails = await stationApi.getStationDetails(selectedStation);
				if (stationDetails) station = stationDetails.data;
			} catch (err) {
				console.error('Failed to load station details', err);
				// keep previous station data if fetch fails
			}
		})();
	});

	// Subscribe to station snapshot updates (published by Map.svelte). When a snapshot
	// arrives and there is an already-selected station, force a refresh of the
	// station details so the detail view stays in sync with SSE updates.
	const unsubscribe = stationsSnapshot.subscribe((snap) => {
		try {
			if (!selectedStation) return;
			// If we were already showing this station, refresh its details
			if (lastFetchedStationId === selectedStation) {
				(async () => {
					try {
						const stationDetails = await stationApi.getStationDetails(selectedStation);
						if (stationDetails) station = stationDetails.data;
					} catch (err) {
						console.error('Failed to refresh station details from snapshot', err);
					}
				})();
			}
		} catch (err) {
			// ignore
		}
	});

	onDestroy(() => {
		unsubscribe();
	});
</script>

{#if station}
	<div class="flex size-full flex-col gap-4 rounded-xl">
		<div class="h-[15%] w-full content-center rounded-t-xl bg-lime-300 text-center">
			<span class="text-4xl font-bold">{station.name}</span>
		</div>
		<div class="content-center place-self-center">
			<Button
				text={station.status}
				variant={station.status === 'ACTIVE' ? 'green' : 'red'}
				onclick={async () => await operatorApi.toggleStationStatus(station?.stationId!)}
			/>
		</div>
		<div class="flex flex-wrap justify-center">
			{#each station.docks as dock, i}
				<div
					onmouseenter={() => (hovered = i)}
					onmouseleave={() => (hovered = null)}
					role="button"
					tabindex="0"
					class="relative"
				>
					<Dock status={dock.status}>
						{#if dock.bike}
							<Bike status={dock.bike.status} />
						{/if}
					</Dock>

					{#if hovered === i}
						<div
							role="tooltip"
							class="absolute left-1/2 top-full z-10 mt-2 -translate-x-1/2 rounded-lg bg-gray-900 px-3 py-2 text-sm font-medium text-white shadow-lg"
						>
							{#if dock.bike?.status}
								{dock.bike.status}
							{:else}
								{dock.status}
							{/if}
							<div class="tooltip-arrow" data-popper-arrow></div>
						</div>
					{/if}
				</div>
			{/each}
		</div>
	</div>
{/if}
