<script lang="ts">
	import type { StationSummary } from '$lib/api/types';

	let {
		station
	}: {
		station: StationSummary;
	} = $props();
</script>

<div class="text-center font-bold">{station.name}</div>
<hr class="h-1 w-full bg-black" />
<div class="mt-2">
	<div>
		Status: {#if station.status === 'ACTIVE'}
			<span class="font-bold text-green-500">Open</span>
		{:else}
			<span class="font-bold text-red-600">Closed</span>
		{/if}
	</div>
	<div>Available Bikes: {station.availableBikes}</div>
	<div>Available Docks: {station.availableDocks}</div>
	<div>Capacity: {station.capacity}</div>
</div>
