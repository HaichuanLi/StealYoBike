<script lang="ts">
	import type { TripBillResponse } from '$lib/api/types';
	import { riderApi } from '$lib/api/rider.api';
	import { authApi } from '$lib/api/auth.api';
	import { createEventDispatcher } from 'svelte';
	import { onMount } from 'svelte';
	import { showToast } from '$lib/stores/toast';

	export let show = false;
	export let bill: TripBillResponse | null = null;

	let isPaying = false;
	let userPaymentToken: string | null = null;
	let message: string | null = null;

	async function loadUserPaymentToken() {
		try {
			const resp = await authApi.getCurrentUser();
			userPaymentToken = resp.data?.paymentToken ?? null;
		} catch (err) {
			userPaymentToken = null;
		}
	}

	onMount(() => {
		loadUserPaymentToken();
	});

	// If the modal becomes visible later (component may be mounted already),
	// refresh the user's payment token each time the modal is shown so the
	// displayed "payment token on file" stays up-to-date.
	$: if (show) {
		loadUserPaymentToken();
	}

	const dispatch = createEventDispatcher();

	async function pay() {
		if (!bill) return;
		isPaying = true;
		message = null;
		try {
			const tokenToUse = userPaymentToken ?? '';
			const resp = await riderApi.payTrip(bill.tripId, { paymentToken: tokenToUse });
			bill = resp.data;
			message = 'Payment recorded.';
			// inform parent that payment occurred so it can refresh lists
			dispatch('paid', { tripId: bill.tripId });
			// show toast
			showToast('Payment successful', 'success');
		} catch (err) {
			console.error('Payment failed', err);
			message = 'Payment failed. Try again.';
			showToast('Payment failed', 'error');
		} finally {
			isPaying = false;
		}
	}

	function close() {
		show = false;
		message = null;
	}
</script>

{#if show && bill}
	<div class="fixed inset-0 bg-transparent flex items-center justify-center z-50">
		<div class="bg-white/95 backdrop-blur-sm rounded-lg p-6 w-11/12 max-w-md">
			<h3 class="text-xl font-semibold mb-2">Trip Bill</h3>
			<p class="text-sm text-gray-600">Bill ID: {bill.billId}</p>
			<p class="mt-2">Total: ${bill.totalAmount.toFixed(2)}</p>
			{#if bill.createdAt}
				<p class="text-sm text-gray-500">Created: {new Date(bill.createdAt).toLocaleString()}</p>
			{/if}
			<hr class="my-4" />
			<div class="text-sm">
				<p>Trip ID: {bill.tripId}</p>
				<p>Bike ID: {bill.trip?.bikeId}</p>
				<p>Start Station: {bill.trip?.startStationName}</p>
				<p>End Station: {bill.endStationName ?? '—'}</p>
				<p>Start Time: {bill.startTime ? new Date(bill.startTime).toLocaleString() : bill.trip?.startTime}</p>
				<p>End Time: {bill.endTime ? new Date(bill.endTime).toLocaleString() : '—'}</p>
				<p>Duration: {bill.durationMinutes} minutes</p>

				<h4 class="mt-3 font-medium">Price breakdown</h4>
				<div class="ml-2">
					<p>Base fee: ${bill.baseFee.toFixed(2)}</p>
					<p>Usage cost: ${bill.usageCost.toFixed(2)}</p>
					<p>Electric surcharge: ${bill.electricCharge.toFixed(2)}</p>
					<p>Discount: -${bill.discountAmount.toFixed(2)}</p>
					<p class="mt-2 font-semibold">Total: ${bill.totalAmount.toFixed(2)}</p>
				</div>

				<div class="mt-3">
					<p class="text-sm">Payment token on file: {userPaymentToken ?? '—'}</p>
					{#if bill.paid}
						<p class="text-sm text-green-700 font-semibold">Paid at {bill.paidAt ? new Date(bill.paidAt).toLocaleString() : ''}</p>
						<p class="text-sm">Payment token used: {bill.paymentTokenUsed ?? '—'}</p>
					{/if}
				</div>
			</div>
			<div class="mt-4 text-right space-x-2">
				<button class="px-4 py-2 bg-gray-300 text-black rounded" on:click={close}>Close</button>
				{#if !bill.paid}
					<button class="px-4 py-2 bg-blue-600 text-white rounded" on:click={pay} disabled={isPaying}>
						{#if isPaying}Paying...{/if}
						{#if !isPaying}Pay{/if}
					</button>
				{/if}
			</div>
			{#if message}
				<p class="mt-2 text-sm">{message}</p>
			{/if}
		</div>
	</div>
{/if}
