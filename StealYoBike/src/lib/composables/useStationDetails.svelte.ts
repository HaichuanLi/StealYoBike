import { stationApi } from '$lib/api';
import type { StationDetailResponse } from '$lib/api/types';
import { stationsSnapshot } from '$lib/stores/stations';
import { onDestroy } from 'svelte';

/**
 * Composable for managing station details with SSE updates and proper reactivity.
 * Prevents infinite loops and duplicate API calls.
 *
 * @param getStationId - A function that returns the current station ID (or null)
 * @param shouldFetch - A function that returns whether fetching should occur
 * @returns Reactive state for station details and selected dock
 */
export function useStationDetails(
	getStationId: () => number | null,
	shouldFetch: () => boolean = () => true
) {
	let stationDetails = $state<StationDetailResponse | null>(null);
	let selectedDock = $state<StationDetailResponse['docks'][number] | null>(null);
	let lastFetchedStationId = $state<number | null>(null);

	// Helper function to fetch station details
	async function fetchStationDetails(stationId: number) {
		try {
			const response = await stationApi.getStationDetails(stationId);
			if (response) {
				stationDetails = response.data;
				// If a dock was previously selected, try to re-resolve it from the
				// fresh station data so the UI reflects latest dock/bike status.
				if (selectedDock) {
					const updated = response.data.docks.find((d) => d.dockId === selectedDock?.dockId);
					selectedDock = updated ?? null;
				}
			}
		} catch (err) {
			console.error('Failed to load station details', err);
			// keep previous station data if fetch fails
		}
	}

	$effect(() => {
		const stationId = getStationId();
		const canFetch = shouldFetch();

		// If no station selected or shouldn't fetch, clear state
		if (!stationId || !canFetch) {
			stationDetails = null;
			lastFetchedStationId = null;
			selectedDock = null;
			return;
		}

		// Avoid refetch if the selected station id hasn't changed
		if (lastFetchedStationId === stationId) return;

		lastFetchedStationId = stationId;
		fetchStationDetails(stationId);
	});

	// Subscribe to station snapshot updates (published by Map.svelte). When a snapshot
	// arrives and there is an already-selected station, force a refresh of the
	// station details so the detail view stays in sync with SSE updates.
	const unsubscribe = stationsSnapshot.subscribe(() => {
		try {
			const stationId = getStationId();
			const canFetch = shouldFetch();

			if (!stationId || !canFetch) return;

			// If we were already showing this station, refresh its details
			if (lastFetchedStationId === stationId) {
				fetchStationDetails(stationId);
			}
		} catch {
			// ignore
		}
	});

	onDestroy(() => {
		unsubscribe();
	});

	return {
		get stationDetails() {
			return stationDetails;
		},
		get selectedDock() {
			return selectedDock;
		},
		set selectedDock(value) {
			selectedDock = value;
		}
	};
}
