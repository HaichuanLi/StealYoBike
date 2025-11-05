<script lang="ts">
	import { stationApi } from '$lib/api';
	import { operatorApi } from '$lib/api/operator.api';
	import type { StationDetailResponse } from '$lib/api/types';
	import { stationsSnapshot } from '$lib/stores/stations';
	import { onDestroy } from 'svelte';
	import Button from '../Button/Button.svelte';
	import StationDockList from './StationDockList.svelte';
	let { selectedStation }: { selectedStation: number } = $props();

	let station = $state<StationDetailResponse | null>(null);
	let selectedDock = $state<StationDetailResponse['docks'][number] | null>(null);
	// Remember the last stationId we fetched to avoid duplicate network calls
	let lastFetchedStationId = $state<number | null>(null);

	$effect(() => {
		// If no station selected, clear state
		if (!selectedStation) {
			station = null;
			lastFetchedStationId = null;
			// clear any selected dock when there is no station
			selectedDock = null;
			return;
		}

		// Avoid refetch if the selected station id hasn't changed
		if (lastFetchedStationId === selectedStation) return;

		lastFetchedStationId = selectedStation;

		(async () => {
			try {
				const stationDetails = await stationApi.getStationDetails(selectedStation);
				if (stationDetails) {
					station = stationDetails.data;
					// If a dock was previously selected, try to re-resolve it from the
					// fresh station data so the UI reflects latest dock/bike status.
					if (selectedDock) {
						const updated = stationDetails.data.docks.find(
							(d) => d.dockId === selectedDock?.dockId
						);
						selectedDock = updated ?? null;
					}
				}
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
						if (stationDetails) {
							station = stationDetails.data;
							// Keep selectedDock in sync with updated station details
							if (selectedDock) {
								const updated = stationDetails.data.docks.find(
									(d) => d.dockId === selectedDock?.dockId
								);
								selectedDock = updated ?? null;
							}
						}
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
		<StationDockList {station} bind:selectedDock />
	</div>
{/if}
