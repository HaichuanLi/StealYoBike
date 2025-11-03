<script lang="ts">
	import { browser } from '$app/environment';
	import { stationApi } from '$lib/api/station.api';
	import type { StationSummary } from '$lib/api/types/station.types';
	import type { AxiosResponse } from 'axios';
	import type { Icon, LayerGroup, Map, Marker } from 'leaflet';
	import { onDestroy, onMount } from 'svelte';

	interface Props {
		locations?: [number, number][];
	}

	let { locations: initialLocations = [] }: Props = $props();

	let locations = $state<[number, number][]>(initialLocations);
	let mapElement = $state<HTMLDivElement>();

	let map: Map | undefined = $state();
	let markerLayers: LayerGroup | undefined;
	let icon: Icon | undefined;
	let mapInitialized = $state(false);
	let leafletLoaded = false;

	$effect(() => {
		if (initialLocations && initialLocations.length > 0) {
			locations = initialLocations;
		}
	});

	$effect(() => {
		if (mapInitialized && locations) {
			updateMarkers();

			// Fit map to show all markers
			if (locations.length > 0 && map) {
				const L = window.L;
				const bounds = L.latLngBounds(locations);
				map.fitBounds(bounds, { padding: [50, 50] });
			}
		}
	});

	async function waitForLeaflet(): Promise<typeof window.L> {
		// Wait for Leaflet to be available (max 5 seconds)
		let attempts = 0;
		while (!window.L && attempts < 50) {
			await new Promise((resolve) => setTimeout(resolve, 100));
			attempts++;
		}

		if (!window.L) {
			throw new Error('Leaflet failed to load');
		}

		return window.L;
	}

	async function initializeMap() {
		if (!browser || !mapElement || mapInitialized) return;

		try {
			const L = await waitForLeaflet();
			leafletLoaded = true;

			// Create the map centered on the first location or a default
			const centerLocation = locations[0] || [45.4972159, -73.6103642];
			map = L.map(mapElement, { preferCanvas: true }).setView(centerLocation, 13);

			// Add tile layer
			L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
				attribution:
					'&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
			}).addTo(map);

			// Create marker layer group
			markerLayers = L.layerGroup();
			markerLayers.addTo(map);

			// Create custom icon
			icon = L.icon({
				iconUrl: '/stationIcon.svg',
				iconSize: [50, 50],
				iconAnchor: [25, 50],
				popupAnchor: [0, -50]
			});

			mapInitialized = true;

			// Add initial markers
			updateMarkers();

			// Fit map to show all markers
			if (locations.length > 0) {
				const bounds = L.latLngBounds(locations);
				map.fitBounds(bounds, { padding: [50, 50] });
			}
		} catch (error) {
			console.error('Error initializing map:', error);
		}
	}

	function createMarker(loc: [number, number]): Marker | null {
		if (!leafletLoaded || !icon) return null;

		const L = window.L;
		const [lat, lng] = loc;
		const marker = L.marker([lat, lng], { icon: icon }).bindPopup(
			`<strong>Location:</strong><br/>Latitude: ${lat.toFixed(6)}<br/>Longitude: ${lng.toFixed(6)}`
		);
		return marker;
	}

	function updateMarkers() {
		if (!map || !markerLayers || !mapInitialized) return;

		// Clear existing markers
		markerLayers.clearLayers();

		// Add new markers
		locations.forEach((loc) => {
			const marker = createMarker(loc);
			if (marker && markerLayers) {
				markerLayers.addLayer(marker);
			}
		});
	}

	function resizeMap() {
		if (map && mapInitialized) {
			requestAnimationFrame(() => {
				map?.invalidateSize();
			});
		}
	}

	async function loadStations() {
		try {
			const response: AxiosResponse<any> = await stationApi.getAllStations();
			const payload = response.data;
			let stationArray: StationSummary[] = [];
			stationArray = (payload as any).stations;

			if (stationArray.length > 0) {
				locations = stationArray.map((station) => [station.latitude, station.longitude]);
			} else {
				locations = [];
			}
		} catch (error) {
			console.error('Failed to load stations:', error);
		}
	}

	onMount(async () => {
		if (browser) {
			// Load stations data
			await loadStations();

			// Initialize map after data is loaded
			await initializeMap();
		}
	});

	onDestroy(() => {
		if (browser && map) {
			try {
				markerLayers?.clearLayers();
				map.remove();
				map = undefined;
				mapInitialized = false;
			} catch (error) {
				console.error('Error cleaning up map:', error);
			}
		}
	});
</script>

<svelte:window on:resize={resizeMap} />

<svelte:head>
	<link
		rel="stylesheet"
		href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
		integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
		crossorigin=""
	/>
	<script
		src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
		integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
		crossorigin=""
	></script>
</svelte:head>

<div class="map-container">
	{#if !mapInitialized}
		<div class="loading-overlay">
			<div class="loading-spinner"></div>
			<p>Loading map...</p>
		</div>
	{/if}
	<div class="map" bind:this={mapElement}></div>
</div>

<style>
	.map-container {
		width: 100%;
		height: 500px;
		position: relative;
	}

	.map {
		width: 100%;
		height: 100%;
		border-radius: 0.5rem;
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
	}

	.loading-overlay {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		background: rgba(255, 255, 255, 0.95);
		z-index: 1000;
		border-radius: 0.5rem;
	}

	.loading-spinner {
		width: 40px;
		height: 40px;
		border: 4px solid #f3f3f3;
		border-top: 4px solid #3498db;
		border-radius: 50%;
		animation: spin 0.15s linear infinite;
	}

	@keyframes spin {
		0% {
			transform: rotate(0deg);
		}
		100% {
			transform: rotate(360deg);
		}
	}

	.loading-overlay p {
		margin-top: 1rem;
		color: #666;
		font-size: 14px;
	}

	/* Custom popup styles */
	.map :global(.leaflet-popup-content-wrapper) {
		background: rgba(255, 255, 255, 0.95);
		box-shadow: 0 3px 14px rgba(0, 0, 0, 0.4);
		border-radius: 8px;
	}

	.map :global(.leaflet-popup-content) {
		margin: 13px 19px;
		font-size: 14px;
	}

	.map :global(.leaflet-popup-tip) {
		background: rgba(255, 255, 255, 0.95);
	}

	.map :global(.marker-cluster) {
		background-clip: padding-box;
		border-radius: 20px;
	}
</style>
