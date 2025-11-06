<script lang="ts">
	import { operatorApi } from '$lib/api/operator.api';
	import { useStationDetails } from '$lib/composables/useStationDetails.svelte';
	import Button from '../Button/Button.svelte';
	import StationDockList from './StationDockList.svelte';

	let { selectedStation }: { selectedStation: number } = $props();

	const stationState = useStationDetails(() => selectedStation || null);
</script>

{#if stationState.stationDetails}
	<div class="flex size-full flex-col gap-4 rounded-xl bg-amber-100">
		<div class="h-[15%] min-h-fit w-full content-center rounded-t-xl bg-lime-300 text-center">
			<span class="text-lg font-semibold">{stationState.stationDetails.name}</span>
			<p class="text-sm">{stationState.stationDetails.streetAddress}</p>
		</div>
		<div class="content-center place-self-center">
			<Button
				text={stationState.stationDetails.status}
				variant={stationState.stationDetails.status === 'ACTIVE' ? 'green' : 'red'}
				onclick={async () =>
					await operatorApi.toggleStationStatus(stationState.stationDetails?.stationId!)}
			/>
		</div>
		<StationDockList
			station={stationState.stationDetails}
			bind:selectedDock={stationState.selectedDock}
		/>
		{#if stationState.selectedDock?.bike}
			<div class="flex justify-center p-4">
				<Button
					text={stationState.selectedDock.bike.status === 'Available'
						? 'Send for Maintenance'
						: 'Activate Bike'}
					variant={stationState.selectedDock.bike.status === 'Available' ? 'red' : 'green'}
					onclick={async () =>
						await operatorApi.toggleBikeStatus(stationState.selectedDock!.bike!.bikeId!)}
				/>
			</div>
		{/if}
	</div>
{/if}
