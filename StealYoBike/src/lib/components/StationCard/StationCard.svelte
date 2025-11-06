<script lang="ts">
	import type { StationSummary } from '$lib/api/types';
	import BikeTypeToggle from '$lib/components/BikeTypeToggle/BikeTypeToggle.svelte';
	import Button from '$lib/components/Button/Button.svelte';
	import StationDockList from '$lib/components/StationDetails/StationDockList.svelte';
	import { useStationDetails } from '$lib/composables/useStationDetails.svelte';

	interface Props {
		station: StationSummary | null;
		hasPaymentMethod: boolean;
		hasActiveReservationOrTrip: boolean;
		isElectric?: boolean;
		onReserveBike: () => void;
		onReturnBike: () => void;
		onAddPayment: () => void;
	}

	let {
		station,
		hasPaymentMethod,
		hasActiveReservationOrTrip,
		isElectric = $bindable(false),
		onReserveBike,
		onReturnBike,
		onAddPayment
	}: Props = $props();

	const stationState = useStationDetails(
		() => station?.stationId ?? null,
		() => hasActiveReservationOrTrip
	);
</script>

<div class="grow rounded-xl bg-lime-50">
	{#if station}
		<div
			class="h-[15%] min-h-fit w-full content-center rounded-t-xl {isElectric
				? 'bg-cyan-300'
				: 'bg-lime-300'} p-4 text-center"
		>
			<h2 class="text-lg font-semibold">
				{station.name}, ID: {station.stationId}
			</h2>
			<p class="text-sm">{station.streetAddress}</p>
		</div>
		{#if station.status === 'OUT_OF_SERVICE'}
			<div class="h-full content-center text-center">
				<p class="mt-4 text-red-600">This station is currently inactive.</p>
			</div>
		{:else if hasActiveReservationOrTrip && stationState.stationDetails}
			<StationDockList
				station={stationState.stationDetails}
				bind:selectedDock={stationState.selectedDock}
			/>
		{:else}
			<BikeTypeToggle bind:isElectric />
			<div class="flex flex-col items-center justify-center">
				<Button
					onclick={onReserveBike}
					text="Reserve Bike"
					disable={!hasPaymentMethod}
					variant="green"
				/>
			</div>
			{#if !hasPaymentMethod}
				<div class="mt-4 p-4 text-center">
					<p class="mb-2 text-sm text-orange-600">Payment method required to use bikes</p>
					<Button onclick={onAddPayment} text="Add Payment Method" variant="red" />
				</div>
			{/if}
		{/if}
	{:else}
		<div class="h-full content-center text-center">Select a Station to get Started!</div>
	{/if}
</div>
