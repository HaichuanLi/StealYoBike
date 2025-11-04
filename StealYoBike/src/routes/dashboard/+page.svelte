<script lang="ts">
	import type { StationSummary } from '$lib/api/types';
	import BurgerMenu from '$lib/components/BurgerMenu/BurgerMenu.svelte';
	import Button from '$lib/components/Button/Button.svelte';
	import Map from '$lib/components/Map/Map.svelte';

	let selectedStation = $state<StationSummary | null>(null);
</script>

<div class="h-25 relative top-0 w-full content-center justify-items-center bg-emerald-200">
	<BurgerMenu />
	<div class="relative w-fit">
		<a
			class="text-nowrap text-2xl font-black before:absolute before:-inset-1 before:text-emerald-500 before:transition-[inset] before:duration-300 before:content-['Steal_Your_Bike_Today!'] hover:before:inset-x-1 md:text-4xl lg:text-6xl"
			href="/"
		>
			Steal Your Bike Today!
		</a>
	</div>
</div>
<div
	class="relative flex h-[calc(100%-6.25rem)] w-full flex-col items-center bg-teal-800 sm:flex-row"
>
	<div class="size-full p-10">
		<Map bind:selectedStation />
	</div>
	<div class="size-full p-10">
		<div class="size-full rounded-xl bg-lime-50">
			{#if selectedStation}
				<div
					class="flex h-full flex-col items-center justify-center gap-2 sm:*:max-w-40 lg:flex-row"
				>
					<Button onclick={() => console.error('hey')} text="Reserve Bike" />
					<Button onclick={() => console.warn('hey')} text="Return Bike" />
				</div>
			{:else}
				<div class="h-full content-center text-center">Select a Station to get Started!</div>
			{/if}
		</div>
	</div>
</div>
