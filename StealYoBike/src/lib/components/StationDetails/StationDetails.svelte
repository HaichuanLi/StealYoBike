<script lang="ts">
	import { stationApi } from '$lib/api';
	import { operatorApi } from '$lib/api/operator.api';
	import type { StationDetailResponse } from '$lib/api/types';
	import Bike from '../Bike/Bike.svelte';
	import Button from '../Button/Button.svelte';
	import Dock from '../Dock/Dock.svelte';
	let { selectedStation }: { selectedStation: number } = $props();

	let station = $state<StationDetailResponse | null>(null);
	let hovered = $state<number | null>(null);

	$effect(() => {
		if (!selectedStation) return;

		(async () => {
			const stationDetails = await stationApi.getStationDetails(selectedStation);
			if (stationDetails) station = stationDetails.data;
		})();
	});
</script>

{#if station}
	<div class="flex size-full flex-col gap-4 rounded-xl">
		<div class="h-[15%] w-full content-center rounded-t-xl bg-lime-300 text-center">
			<span class="text-4xl font-bold">{station.name}</span>
		</div>
		<div class="content-center place-self-center">
			<Button
				text={station.status}
				variant={station.status === 'ACTIVE' ? 'green' : 'red'}
				onclick={async () => await operatorApi.toggleStationStatus(station?.stationId!)}
			/>
		</div>
		<div class="flex flex-wrap justify-center">
			{#each station.docks as dock, i}
				<div
					onmouseenter={() => (hovered = i)}
					onmouseleave={() => (hovered = null)}
					role="button"
					tabindex="0"
					class="relative"
				>
					<Dock status={dock.status}>
						{#if dock.bike}
							<Bike status={dock.bike.status} />
						{/if}
					</Dock>

					{#if hovered === i}
						<div
							role="tooltip"
							class="absolute left-1/2 top-full z-10 mt-2 -translate-x-1/2 rounded-lg bg-gray-900 px-3 py-2 text-sm font-medium text-white shadow-lg"
						>
							{#if dock.bike?.status}
								{dock.bike.status}
							{:else}
								{dock.status}
							{/if}
							<div class="tooltip-arrow" data-popper-arrow></div>
						</div>
					{/if}
				</div>
			{/each}
		</div>
	</div>
{/if}
