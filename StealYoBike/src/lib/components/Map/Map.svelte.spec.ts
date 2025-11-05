import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render } from 'vitest-browser-svelte';

// Component under test
import Map from './Map.svelte';

// Mock the station API module used by the component
vi.mock('$lib/api/station.api', () => {
	return {
		stationApi: {
			getAllStations: async () => ({
				data: {
					stations: [
						{
							stationId: 1,
							name: 'A',
							status: 'ACTIVE',
							latitude: 45,
							longitude: -73,
							streetAddress: 'addr',
							availableBikes: 2,
							availableDocks: 3,
							capacity: 5
						}
					]
				}
			})
		}
	};
});

// Simple Leaflet mock exposed on globalThis as L
class FakeLayerGroup {
	layers: unknown[] = [];
	addTo(): this {
		return this;
	}
	clearLayers(): void {
		this.layers = [];
	}
	addLayer(_l: unknown): void {
		this.layers.push(_l);
	}
	removeLayer(_l: unknown): void {
		this.layers = this.layers.filter((x) => x !== _l);
		// also try to remove from the global LMock marker list if present
		const idx = LMock._markers.indexOf(_l as unknown as FakeMarker);
		if (idx >= 0) LMock._markers.splice(idx, 1);
	}
}

let globalOpenCount = 0;

const LMock = {
	map: () => ({
		setView: () => ({ fitBounds: () => ({}), invalidateSize: () => ({}) }),
		invalidateSize: () => ({})
	}),
	tileLayer: () => ({ addTo: () => ({}) }),
	layerGroup: () => new FakeLayerGroup(),
	icon: () => ({}),
	latLngBounds: (arr: Array<[number, number]>) => ({
		// simple stub matching Leaflet's bounds interface — tests only need a passthrough
		extend() {
			return this;
		},
		getCenter() {
			const first = arr && arr.length > 0 ? arr[0] : [0, 0];
			return { lat: first[0], lng: first[1] };
		}
	}),
	_markers: [] as FakeMarker[],
	marker: (coords: [number, number], _opts: unknown) => {
		void _opts;
		const m: FakeMarker = {
			coords,
			popup: undefined,
			_popupopen: [],
			_popupclose: [],
			bindPopup(container: unknown) {
				m.popup = container;
				return m;
			},
			on(event: string, cb: () => void) {
				if (event === 'popupopen') m._popupopen.push(cb);
				if (event === 'popupclose') m._popupclose.push(cb);
				return m;
			},
			openPopup() {
				// count every time openPopup is called
				globalOpenCount++;
				m._popupopen.forEach((cb) => cb());
			}
		};
		m.setLatLng = (coords2: [number, number]) => {
			m.coords = coords2;
		};
		LMock._markers.push(m);
		return m;
	}
};

type FakeMarker = {
	coords: [number, number];
	popup?: unknown;
	_popupopen: Array<() => void>;
	_popupclose: Array<() => void>;
	bindPopup: (c: unknown) => FakeMarker;
	on: (event: string, cb: () => void) => FakeMarker;
	openPopup: () => void;
	setLatLng?: (coords: [number, number]) => void;
};

// Minimal fake EventSource so tests can emit events
class FakeEventSource {
	static instances: FakeEventSource[] = [];
	listeners: Record<string, Array<(e: { data: string }) => void>> = {};
	url: string;
	constructor(url: string) {
		this.url = url;
		FakeEventSource.instances.push(this);
	}
	addEventListener(name: string, cb: (e: { data: string }) => void) {
		(this.listeners[name] ||= []).push(cb);
	}
	emit(name: string, data: unknown) {
		const handlers = this.listeners[name] || [];
		const event = { data: JSON.stringify(data) };
		handlers.forEach((h) => h(event));
	}
	close() {}
}

describe('Map.svelte SSE/popup loop', () => {
	beforeEach(() => {
		// reset globals
		(globalThis as unknown as { L?: typeof LMock; EventSource?: unknown }).L = LMock;
		(globalThis as unknown as { L?: typeof LMock; EventSource?: unknown }).EventSource =
			FakeEventSource as unknown;
		FakeEventSource.instances = [];
		LMock._markers = [];
		globalOpenCount = 0;
	});

	it('does not repeatedly re-open popup on repeated snapshots', async () => {
		render(Map);

		// wait a tick so onMount runs
		await new Promise((r) => setTimeout(r, 50));

		// Expect an EventSource to have been created
		expect(FakeEventSource.instances.length).toBeGreaterThan(0);
		const es = FakeEventSource.instances[0];

		// There should be at least one marker created by initial load
		expect(LMock._markers.length).toBeGreaterThan(0);
		const marker = LMock._markers[0];

		// simulate user opening popup once
		marker.openPopup();
		expect(globalOpenCount).toBe(1);

		// now emit several identical snapshots (same stationId) to simulate live updates
		for (let i = 0; i < 5; i++) {
			es.emit('stations-snapshot', {
				stations: [
					{
						stationId: 1,
						name: 'A' + i,
						status: 'ACTIVE',
						latitude: 45,
						longitude: -73,
						streetAddress: 'addr',
						availableBikes: 2 + i,
						availableDocks: 3,
						capacity: 5
					}
				]
			});
			// allow component to process
			await new Promise((r) => setTimeout(r, 20));
		}

		// openPopup should NOT have been called more than once if the loop is prevented
		expect(globalOpenCount).toBe(1);
	});
});
