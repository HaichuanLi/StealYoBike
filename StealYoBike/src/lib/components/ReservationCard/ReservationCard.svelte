<script lang="ts">
	import type { ReserveBikeResponse } from '$lib/api/types/rider.types';
	import Button from '$lib/components/Button/Button.svelte';

	interface Props {
		reservation: ReserveBikeResponse | null;
		loading: boolean;
		hasPaymentMethod: boolean;
		onCancel: () => void;
		onCheckout: () => void;
	}

	let { reservation, loading, hasPaymentMethod, onCancel, onCheckout }: Props = $props();

	let isReservationExpired = $derived(
		reservation !== null && new Date(reservation.expiresAt) < new Date()
	);
</script>

{#if reservation}
	<div class="mt-4 rounded-xl bg-lime-50 p-4">
		<h3 class="mb-2 text-lg font-semibold">Reservation Details</h3>
		{#if isReservationExpired}
			<div class="mb-3 rounded bg-red-100 p-3 text-center">
				<p class="font-semibold text-red-700">This reservation has expired!</p>
				<p class="text-sm text-red-600">Please cancel it and make a new reservation.</p>
			</div>
		{/if}
		<p>Reservation ID: {reservation.reservationId}</p>
		<p>Bike ID: {reservation.bikeId}</p>
		<p>Station ID: {reservation.stationId}</p>
		<p class={isReservationExpired ? 'font-semibold text-red-600' : ''}>
			Expires At: {new Date(reservation.expiresAt).toLocaleString()}
		</p>
		<p>PIN: {reservation.pin}</p>
		<div class="my-2 border-t border-lime-200"></div>
		<div class="flex flex-row justify-center gap-2">
			<Button
				onclick={onCancel}
				text="Cancel Reservation"
				disable={!hasPaymentMethod}
				variant="teal"
			/>
			<Button
				onclick={onCheckout}
				text="Checkout Bike"
				disable={!hasPaymentMethod || isReservationExpired}
				variant="green"
			/>
		</div>
	</div>
{:else if !loading}
	<div class="mt-4 rounded-xl bg-lime-50 p-4 text-center">
		<p>No active reservations.</p>
	</div>
{/if}
