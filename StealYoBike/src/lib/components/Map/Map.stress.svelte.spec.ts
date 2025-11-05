import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render } from 'vitest-browser-svelte';
import Map from './Map.svelte';

// Lightweight mocks similar to the main spec
class FakeLayerGroup {
	layers: unknown[] = [];
	addTo(): this {
		return this;
	}
	clearLayers(): void {
		this.layers = [];
	}
	addLayer(l: unknown): void {
		this.layers.push(l);
	}
	removeLayer(l: unknown): void {
		this.layers = this.layers.filter((x) => x !== l);
	}
}

let globalOpenCount = 0;

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

const LMock = {
	map: () => ({
		setView: () => ({ fitBounds: () => ({}), invalidateSize: () => ({}) }),
		invalidateSize: () => ({})
	}),
	tileLayer: () => ({ addTo: () => ({}) }),
	layerGroup: () => new FakeLayerGroup(),
	icon: () => ({}),
	latLngBounds: (arr: Array<[number, number]>) => ({
		extend() {
			return this;
		},
		getCenter() {
			const first = arr && arr.length > 0 ? arr[0] : [0, 0];
			return { lat: first[0], lng: first[1] };
		}
	}),
	_markers: [] as Array<FakeMarker>,
	marker: (coords: [number, number]) => {
		const m: FakeMarker = {
			coords,
			popup: undefined,
			_popupopen: [] as Array<() => void>,
			_popupclose: [] as Array<() => void>,
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
				globalOpenCount++;
				for (const cb of m._popupopen) {
					cb();
				}
			},
			setLatLng(coords2: [number, number]) {
				m.coords = coords2;
			}
		};
		LMock._markers.push(m);
		return m;
	}
};

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

vi.mock('$lib/api/station.api', () => ({
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
}));

describe('Map.svelte stress test', () => {
	beforeEach(() => {
		(globalThis as unknown as { L?: typeof LMock }).L = LMock;
		(globalThis as unknown as { EventSource?: unknown }).EventSource = FakeEventSource;
		FakeEventSource.instances = [];
		LMock._markers = [];
		globalOpenCount = 0;
	});

	it('does not re-open popup under rapid updates', async () => {
		render(Map);
		await new Promise((r) => setTimeout(r, 50));
		expect(FakeEventSource.instances.length).toBeGreaterThan(0);
		const es = FakeEventSource.instances[0];
		expect(LMock._markers.length).toBeGreaterThan(0);
		const marker = LMock._markers[0];

		// user opens popup
		marker.openPopup();
		expect(globalOpenCount).toBe(1);

		// emit many updates quickly
		for (let i = 0; i < 200; i++) {
			es.emit('stations-snapshot', {
				stations: [
					{
						stationId: 1,
						name: 'A' + i,
						status: 'ACTIVE',
						latitude: 45 + (i % 5) * 0.0001,
						longitude: -73 + (i % 5) * 0.0001,
						streetAddress: 'addr',
						availableBikes: 2 + i,
						availableDocks: 3,
						capacity: 5
					}
				]
			});
		}

		// give component time to process
		await new Promise((r) => setTimeout(r, 200));

		// still only the original open should have occurred (no repeated re-opens)
		expect(globalOpenCount).toBe(1);
	});
});
