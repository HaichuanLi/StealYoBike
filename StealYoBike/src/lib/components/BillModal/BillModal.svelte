<script lang="ts">
	import { authApi } from '$lib/api/auth.api';
	import { riderApi } from '$lib/api/rider.api';
	import type { TripBillResponse } from '$lib/api/types';
	import { showToast } from '$lib/stores/toast';
	import { createEventDispatcher, onMount } from 'svelte';
	import Button from '../Button/Button.svelte';

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
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-transparent">
		<div class="w-11/12 max-w-md rounded-lg bg-white/95 p-6 backdrop-blur-sm">
			<h3 class="mb-2 text-xl font-semibold">Trip Bill</h3>
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
				<p>
					Start Time: {bill.startTime
						? new Date(bill.startTime).toLocaleString()
						: bill.trip?.startTime}
				</p>
				<p>End Time: {bill.endTime ? new Date(bill.endTime).toLocaleString() : '—'}</p>
				<p>Duration: {bill.durationMinutes} minutes</p>

			<h4 class="mt-3 font-medium">Price breakdown</h4>
			<div class="ml-2">
				<p>Base fee: ${bill.baseFee.toFixed(2)}</p>
				<p>Usage cost: ${bill.usageCost.toFixed(2)}</p>
				<p>Electric surcharge: ${bill.electricCharge.toFixed(2)}</p>
				<p>Discount: -${bill.discountAmount.toFixed(2)}</p>
				<p>Tier Discount: -${bill.tierDiscountAmount.toFixed(2)}</p>
				<p class="mt-2 font-semibold">Total: ${bill.totalAmount.toFixed(2)}</p>
			</div>
			<p class="mt-3 text-sm text-gray-700">User Tier: <span class="font-semibold text-emerald-700">{bill.tier}</span></p>				<div class="mt-3">
					<p class="text-sm">Payment token on file: {userPaymentToken ?? '—'}</p>
					{#if bill.paid}
						<p class="text-sm font-semibold text-green-700">
							Paid at {bill.paidAt ? new Date(bill.paidAt).toLocaleString() : ''}
						</p>
						<p class="text-sm">Payment token used: {bill.paymentTokenUsed ?? '—'}</p>
					{/if}
				</div>
			</div>
			<div class="mt-4 space-x-2 text-right">
				<Button text="Close" variant="gray" onclick={close} />
				{#if !bill.paid}
					<Button text="Pay" variant="blue" onclick={pay} disable={isPaying} />
				{/if}
			</div>
			{#if message}
				<p class="mt-2 text-sm">{message}</p>
			{/if}
		</div>
	</div>
{/if}
