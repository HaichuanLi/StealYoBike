<script lang="ts">
	import Button from '$lib/components/Button/Button.svelte';
	import Popup from '$lib/components/Popup/Popup.svelte';
	import { riderStore } from '$lib/stores/rider.store.svelte';

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			riderStore.savePaymentToken(riderStore.paymentModalToken);
		} else if (event.key === 'Escape') {
			riderStore.closePaymentModal();
		}
	}
</script>

<Popup isVisible={riderStore.paymentModalShow} onClose={() => riderStore.closePaymentModal()}>
	<div class="flex flex-col gap-4 bg-white p-6">
		<h2 class="text-2xl font-bold">Add Payment Method</h2>
		<p class="text-gray-700">
			Please enter your payment token to proceed with bike reservations and returns.
		</p>
		<input
			type="text"
			bind:value={riderStore.paymentModalToken}
			placeholder="Enter payment token"
			class="w-full rounded border border-gray-300 px-3 py-2 focus:border-lime-300 focus:outline-none"
			disabled={riderStore.paymentModalSaving}
			onkeydown={handleKeydown}
		/>
		<div class="flex justify-end gap-4">
			<Button
				onclick={() => riderStore.closePaymentModal()}
				text="Cancel"
				variant="red"
				disable={riderStore.paymentModalSaving}
			/>
			<Button
				onclick={() => riderStore.savePaymentToken(riderStore.paymentModalToken)}
				text={riderStore.paymentModalSaving ? 'Saving...' : 'Save'}
				variant="green"
				disable={riderStore.paymentModalSaving}
			/>
		</div>
	</div>
</Popup>
