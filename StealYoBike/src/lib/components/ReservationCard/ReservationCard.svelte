<script lang="ts">
	import Button from '$lib/components/Button/Button.svelte';
	import { riderStore } from '$lib/stores/rider.store.svelte';
</script>

{#if riderStore.reservation}
	<div class="mt-4 rounded-xl bg-lime-50 p-4">
		<h3 class="mb-2 text-lg font-semibold">Reservation Details</h3>
		{#if riderStore.isReservationExpired}
			<div class="mb-3 rounded bg-red-100 p-3 text-center">
				<p class="font-semibold text-red-700">This reservation has expired!</p>
				<p class="text-sm text-red-600">Please cancel it and make a new reservation.</p>
			</div>
		{/if}
		<p>Reservation ID: {riderStore.reservation.reservationId}</p>
		<p>Bike ID: {riderStore.reservation.bikeId}</p>
		<p>Station ID: {riderStore.reservation.stationId}</p>
		<p class={riderStore.isReservationExpired ? 'font-semibold text-red-600' : ''}>
			Expires At: {new Date(riderStore.reservation.expiresAt).toLocaleString()}
		</p>
		<p>PIN: {riderStore.reservation.pin}</p>
		<div class="my-2 border-t border-lime-200"></div>
		<div class="flex flex-row justify-center gap-2">
			<Button
				onclick={() => riderStore.cancelReservation()}
				text="Cancel Reservation"
				disable={!riderStore.hasPaymentMethod}
				variant="teal"
			/>
			<Button
				onclick={() => riderStore.checkoutBike()}
				text="Checkout Bike"
				disable={!riderStore.hasPaymentMethod || riderStore.isReservationExpired}
				variant="green"
			/>
		</div>
	</div>
{:else if !riderStore.loading}
	<div class="mt-4 rounded-xl bg-lime-50 p-4 text-center">
		<p>No active reservations.</p>
	</div>
{/if}
