<script lang="ts">
	import { authApi } from '$lib/api/auth.api';
	import { riderApi } from '$lib/api/rider.api';
	import { riderStore } from '$lib/stores/rider.store.svelte';
	import { showToast } from '$lib/stores/toast';
	import { onMount } from 'svelte';
	import Button from '../Button/Button.svelte';

	let paymentState = $state({
		isPaying: false,
		userToken: null as string | null,
		message: null as string | null
	});

	async function loadUserPaymentToken() {
		try {
			const resp = await authApi.getCurrentUser();
			paymentState.userToken = resp.data?.paymentToken ?? null;
		} catch (err) {
			paymentState.userToken = null;
		}
	}

	onMount(() => {
		loadUserPaymentToken();
	});

	async function pay() {
		const tripBill = riderStore.billModalData;
		if (!tripBill) return;
		paymentState.isPaying = true;
		paymentState.message = null;
		try {
			const tokenToUse = paymentState.userToken ?? '';
			const resp = await riderApi.payTrip(tripBill.tripId, { paymentToken: tokenToUse });

			riderStore.updateBill(resp.data);
			paymentState.message = 'Payment recorded.';
			showToast('Payment successful', 'success');
		} catch (err) {
			paymentState.message = 'Payment failed. Try again.';
			showToast('Payment failed', 'error');
		} finally {
			paymentState.isPaying = false;
		}
	}

	function close() {
		riderStore.closeBillModal();
		paymentState.message = null;
	}
</script>

{#if riderStore.billModalShow && riderStore.billModalData}
	{@const tripBill = riderStore.billModalData}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-transparent">
		<div class="w-11/12 max-w-md rounded-lg bg-white/95 p-6 backdrop-blur-sm">
			<h3 class="mb-2 text-xl font-semibold">Trip Bill</h3>
			<p class="text-sm text-gray-600">Bill ID: {tripBill.billId}</p>
			<p class="mt-2">Total: ${(tripBill.totalAmount ?? 0).toFixed(2)}</p>
			{#if tripBill.createdAt}
				<p class="text-sm text-gray-500">
					Created: {new Date(tripBill.createdAt).toLocaleString()}
				</p>
			{/if}
			<hr class="my-4" />
			<div class="text-sm">
				<p>Trip ID: {tripBill.tripId}</p>
				<p>Bike ID: {tripBill.trip?.bikeId ?? '—'}</p>
				<p>Start Station: {tripBill.trip?.startStationName ?? '—'}</p>
				<p>End Station: {tripBill.endStationName ?? '—'}</p>
				<p>
					Start Time: {tripBill.startTime
						? new Date(tripBill.startTime).toLocaleString()
						: tripBill.trip?.startTime
							? new Date(tripBill.trip.startTime).toLocaleString()
							: '—'}
				</p>
				<p>End Time: {tripBill.endTime ? new Date(tripBill.endTime).toLocaleString() : '—'}</p>
				<p>Duration: {tripBill.durationMinutes ?? 0} minutes</p>

				<h4 class="mt-3 font-medium">Price breakdown</h4>
				<div class="ml-2">
					<p>Base fee: ${(tripBill.baseFee ?? 0).toFixed(2)}</p>
					<p>Usage cost: ${(tripBill.usageCost ?? 0).toFixed(2)}</p>
					<p>Electric surcharge: ${(tripBill.elecCharge ?? 0).toFixed(2)}</p>
					<p>Discount: -${(tripBill.discountAmount ?? 0).toFixed(2)}</p>
					<p>Tier Discount: -${(tripBill.tierDiscountAmount ?? 0).toFixed(2)}</p>
					{#if (tripBill.flexDollarUsed ?? 0) > 0}
						<p>Flex Dollar Used: -${(tripBill.flexDollarUsed ?? 0).toFixed(2)}</p>
					{/if}
					<p class="mt-2 font-semibold">Total: ${(tripBill.totalAmount ?? 0).toFixed(2)}</p>
				</div>
				<p class="mt-3 text-sm text-gray-700">
					User Tier: <span class="font-semibold text-emerald-700">{tripBill.tier ?? 'REGULAR'}</span
					>
				</p>
				<div class="mt-3">
					<p class="text-sm">Payment token on file: {paymentState.userToken ?? '—'}</p>
					{#if tripBill.paid}
						<p class="text-sm font-semibold text-green-700">
							Paid at {tripBill.paidAt ? new Date(tripBill.paidAt).toLocaleString() : ''}
						</p>
						<p class="text-sm">Payment token used: {tripBill.paymentTokenUsed ?? '—'}</p>
					{/if}
				</div>
			</div>
			<div class="mt-4 space-x-2 text-right">
				<Button text="Close" variant="gray" onclick={close} />
				{#if !tripBill.paid}
					<Button text="Pay" variant="blue" onclick={pay} disable={paymentState.isPaying} />
				{/if}
			</div>
			{#if paymentState.message}
				<p class="mt-2 text-sm">{paymentState.message}</p>
			{/if}
		</div>
	</div>
{/if}
