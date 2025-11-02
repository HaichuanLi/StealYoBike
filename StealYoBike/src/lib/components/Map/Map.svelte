<script lang="ts">
	import { browser } from '$app/environment';
	import { onDestroy, onMount } from 'svelte';

	const props = $props<{ locations?: [number, number][] }>();

	let locations: [number, number][] = props.locations ?? [
		[45.4972159, -73.6103642],
		[45.49621504158091, -73.57718419193809]
	];

	let map: any;
	let mapElement: HTMLDivElement;
	let L: any;
	let markerLayers: any;
	let icon: any;
	let mapInitialized = false;

	async function initializeMap() {
		if (!browser || !mapElement || mapInitialized) return;

		try {
			if (!window.L) {
				console.log('Waiting for Leaflet to load...');
				return;
			}

			L = window.L;

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

	function createMarker(loc: [number, number]) {
		if (!L || !icon) return null;

		const [lat, lng] = loc;
		const marker = L.marker([lat, lng], { icon: icon }).bindPopup(
			`<strong>Location:</strong><br/>Latitude: ${lat}<br/>Longitude: ${lng}`
		);
		return marker;
	}

	function updateMarkers() {
		if (!map || !markerLayers || !mapInitialized) return;

		markerLayers.clearLayers();

		locations.forEach((loc) => {
			const marker = createMarker(loc);
			if (marker) {
				markerLayers.addLayer(marker);
			}
		});
	}

	function resizeMap() {
		if (map && mapInitialized) {
			setTimeout(() => {
				map.invalidateSize();
			}, 100);
		}
	}

	$effect(() => {
		updateMarkers();
	});

	onMount(() => {
		if (browser) {
			if (window.L) {
				initializeMap();
			}
		}
	});

	onDestroy(() => {
		if (browser && map) {
			try {
				markerLayers?.clearLayers();
				map.remove();
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
		onload={initializeMap}
	></script>
</svelte:head>

<div class="map-container">
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
