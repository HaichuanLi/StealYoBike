<script lang="ts">
	import type { StationDetailResponse } from '$lib/api/types';
	import Bike from '../Bike/Bike.svelte';
	import Dock from '../Dock/Dock.svelte';

	let {
		station,
		selectedDock = $bindable<StationDetailResponse['docks'][number] | null>(null)
	}: {
		station: StationDetailResponse;
		selectedDock: StationDetailResponse['docks'][number] | null;
	} = $props();

	let hovered = $state<number | null>(null);
</script>

<div class="flex flex-wrap justify-center">
	{#each station.docks as dock, i}
		<button
			onmouseenter={() => (hovered = i)}
			onmouseleave={() => (hovered = null)}
			tabindex="0"
			class="relative rounded-2xl {selectedDock?.dockId === dock.dockId ? 'bg-gray-400/30' : ''}"
			aria-pressed={selectedDock?.dockId === dock.dockId}
			onclick={() => (selectedDock = selectedDock?.dockId === dock.dockId ? null : dock)}
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
		</button>
	{/each}
</div>

<div class="flex w-full flex-row gap-2 p-2">
	<div class="h-full min-h-fit w-1/2 rounded-2xl bg-amber-200 p-4">
		{#if selectedDock}
			<div class="text-lg font-semibold">Dock ID: {selectedDock.dockId}</div>
			<div class="mt-2">Status: {selectedDock.status}</div>
		{:else}
			<div class="text-lg font-semibold">No dock selected</div>
			<div class="mt-2">Total docks: {station.docks.length}</div>
			<div>
				Available bikes: {station.docks.filter((d) => d.bike?.status === 'Available').length}
			</div>
		{/if}
	</div>
	<div class="h-full min-h-fit w-1/2 rounded-2xl bg-amber-200 p-4">
		{#if selectedDock?.bike}
			<div class="text-lg font-semibold">Bike details</div>
			<div class="mt-2">ID: {selectedDock.bike.bikeId}</div>
			<div>Status: {selectedDock.bike.status}</div>
			<div>Type: {selectedDock.bike.type}</div>
		{:else if selectedDock && !selectedDock.bike}
			<div class="text-lg font-semibold">No bike at this dock</div>
			<div class="mt-2">You can select another dock to view bike details.</div>
		{:else}
			<div class="text-lg font-semibold">Select a dock</div>
			<div class="mt-2">Click a dock above to view bike and dock information.</div>
		{/if}
	</div>
</div>
