<script lang="ts">
	import type { TripBillResponse } from '$lib/api/types';
	export let show = false;
	export let bill: TripBillResponse | null = null;

	function close() {
		show = false;
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
			</div>
			<div class="mt-4 text-right">
				<button class="px-4 py-2 bg-blue-600 text-white rounded" on:click={close}>Close</button>
			</div>
		</div>
	</div>
{/if}
