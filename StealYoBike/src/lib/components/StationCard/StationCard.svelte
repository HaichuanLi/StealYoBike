<script lang="ts">
	import BikeTypeToggle from '$lib/components/BikeTypeToggle/BikeTypeToggle.svelte';
	import Button from '$lib/components/Button/Button.svelte';
	import StationDockList from '$lib/components/StationDetails/StationDockList.svelte';
	import { useStationDetails } from '$lib/composables/useStationDetails.svelte';
	import { riderStore } from '$lib/stores/rider.store.svelte';

	// Always fetch station details when a station is selected
	const stationState = useStationDetails(() => riderStore.selectedStation?.stationId ?? null);
</script>

<div class="grow rounded-xl bg-lime-50">
	{#if riderStore.selectedStation}
		<div
			class="h-[15%] min-h-fit w-full content-center rounded-t-xl {riderStore.isElectric
				? 'bg-cyan-300'
				: 'bg-lime-300'} p-4 text-center"
		>
			<h2 class="text-lg font-semibold">
				{riderStore.selectedStation.name}, ID: {riderStore.selectedStation.stationId}
			</h2>
			<p class="text-sm">{riderStore.selectedStation.streetAddress}</p>

			<div class="mt-2 flex items-center justify-center gap-2">
                <Button
                    onclick={() => riderStore.toggleStationSubscription()}
                    text={riderStore.subscriptionLoading 
                        ? 'Loading...' 
                        : riderStore.isCurrentStationSubscribed 
                            ? '🔔 Subscribed' 
                            : '🔕 Subscribe to Alerts'}
                    variant={riderStore.isCurrentStationSubscribed ? 'teal' : 'gray'}
                    disable={riderStore.subscriptionLoading}
                />
            </div>
            
            {#if riderStore.isCurrentStationSubscribed}
                <p class="mt-1 text-xs text-gray-700">
                    You'll be notified when availability drops below 10%
                </p>
            {/if}
			
		</div>
		{#if riderStore.selectedStation.status === 'OUT_OF_SERVICE'}
			<div class="h-full content-center text-center">
				<p class="mt-4 text-red-600">This station is currently inactive.</p>
			</div>
		{:else if riderStore.hasActiveReservationOrTrip && stationState.stationDetails}
			<StationDockList
				station={stationState.stationDetails}
				bind:selectedDock={stationState.selectedDock}
			/>
			{#if riderStore.currentTrip}
				<div class="my-4 flex flex-col items-center justify-center">
					{#if stationState.stationDetails.docks.filter((dock) => dock.status === 'EMPTY').length === 0}
						<p class="mb-2 text-sm text-gray-600">
							No dock is currently available at this station.
						</p>
					{:else}
						<Button
							onclick={() => riderStore.returnBike()}
							text={riderStore.returning ? 'Returning...' : 'Return Bike'}
							disable={!riderStore.hasPaymentMethod || riderStore.returning}
							variant="blue"
						/>
					{/if}
				</div>
			{/if}
		{:else}
			<BikeTypeToggle bind:isElectric={riderStore.isElectric} />
			<div class="flex flex-col items-center justify-center">
				<Button
					onclick={() => riderStore.reserveBike()}
					text={riderStore.reserving ? 'Reserving...' : 'Reserve Bike'}
					disable={!riderStore.hasPaymentMethod || riderStore.reserving}
					variant="green"
				/>
			</div>
			{#if !riderStore.hasPaymentMethod}
				<div class="mt-4 p-4 text-center">
					<p class="mb-2 text-sm text-orange-600">Payment method required to use bikes</p>
					<Button
						onclick={() => riderStore.showPaymentModal()}
						text="Add Payment Method"
						variant="red"
					/>
				</div>
			{/if}
		{/if}
	{:else}
		<div class="h-full content-center text-center">Select a Station to get Started!</div>
	{/if}
</div>
