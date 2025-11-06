<script lang="ts">
	import type { StationSummary } from '$lib/api/types';
	import Button from '$lib/components/Button/Button.svelte';
	import DashboardBody from '$lib/components/DashboardBody/DashboardBody.svelte';
	import DashboardHeader from '$lib/components/DashboardHeader/DashboardHeader.svelte';
	import Map from '$lib/components/Map/Map.svelte';
	import RebalanceBikes from '$lib/components/RebalanceBikes/RebalanceBikes.svelte';
	import RestoreInitialState from '$lib/components/RestoreInitialState/RestoreInitialState.svelte';
	import StationDetails from '$lib/components/StationDetails/StationDetails.svelte';
	import { stationsSnapshot } from '$lib/stores/stations';

	let selectedStation = $state<StationSummary | null>(null);
	let activeView = $state<'station' | 'rebalance' | 'restore'>('station');

	// Get all stations for the rebalance component
	let allStations = $derived($stationsSnapshot || []);
</script>

<DashboardHeader />
<DashboardBody>
	<div>
		<Map bind:selectedStation />
	</div>
	<div>
		<div class="size-full rounded-xl bg-lime-50">
			<!-- View Toggle Buttons -->
			<p class="justify-center p-4 text-center font-semibold text-gray-700">Select a view:</p>
			<div class="flex justify-center gap-2">
				<Button
					text="Station Details"
					variant={activeView === 'station' ? 'teal' : 'green'}
					onclick={() => (activeView = 'station')}
				/>
				<Button
					text="Rebalance Bikes"
					variant={activeView === 'rebalance' ? 'teal' : 'green'}
					onclick={() => (activeView = 'rebalance')}
				/>
				<Button
					text="Restore State"
					variant={activeView === 'restore' ? 'teal' : 'green'}
					onclick={() => (activeView = 'restore')}
				/>
			</div>

			<!-- Content Area -->
			<div class="p-4">
				{#if activeView === 'station'}
					{#if selectedStation}
						<StationDetails selectedStation={selectedStation.stationId} />
					{:else}
						<div class="h-full content-center text-center">Select a Station to get Started!</div>
					{/if}
				{:else if activeView === 'rebalance'}
					<RebalanceBikes
						stations={allStations}
						onRebalanceComplete={() => {
							// The map will automatically update via SSE
							console.log('Rebalance completed successfully');
						}}
					/>
				{:else if activeView === 'restore'}
					<RestoreInitialState
						onRestoreComplete={() => {
							// The map will automatically update via SSE
							console.log('Initial state restored successfully');
						}}
					/>
				{/if}
			</div>
		</div>
	</div>
</DashboardBody>
