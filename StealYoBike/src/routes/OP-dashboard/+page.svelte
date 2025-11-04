<script lang="ts">
	import type { StationSummary } from '$lib/api/types';
	import Button from '$lib/components/Button/Button.svelte';
	import DashboardBody from '$lib/components/DashboardBody/DashboardBody.svelte';
	import DashboardHeader from '$lib/components/DashboardHeader/DashboardHeader.svelte';
	import Map from '$lib/components/Map/Map.svelte';
	import StationDetails from '$lib/components/StationDetails/StationDetails.svelte';

	let selectedStation = $state<StationSummary | null>(null);
</script>

<DashboardHeader />
<DashboardBody>
	<div>
		<Map bind:selectedStation />
	</div>
	<div>
		<div class="size-full overflow-hidden rounded-xl bg-lime-50">
			{#if selectedStation}
				<StationDetails selectedStation={selectedStation.stationId} />
			{:else}
				<div class="h-full content-center text-center">Select a Station to get Started!</div>
			{/if}
		</div>
	</div>
</DashboardBody>
