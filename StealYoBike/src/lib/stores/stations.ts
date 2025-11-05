import type { StationSummary } from '$lib/api/types';
import { writable } from 'svelte/store';

// Holds the latest snapshot of station summaries (updated by Map.svelte SSE)
export const stationsSnapshot = writable<StationSummary[]>([]);
