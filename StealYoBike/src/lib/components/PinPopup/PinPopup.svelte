<script lang="ts">
	import Button from '$lib/components/Button/Button.svelte';
	import Popup from '$lib/components/Popup/Popup.svelte';
	import { riderStore } from '$lib/stores/rider.store.svelte';
	import { showToast } from '$lib/stores/toast';

	async function handleSubmit() {
		if (!riderStore.pinModalPin.trim()) {
			showToast('Please enter your reservation PIN', 'error');
			return;
		}

		riderStore.pinModalSubmitting = true;
		try {
			await riderStore.submitPin(riderStore.pinModalPin.trim());
			// Success toast is handled in the store
		} catch (error) {
			console.error('Failed to submit PIN:', error);
			// Error toast is handled in the store
		} finally {
			riderStore.pinModalSubmitting = false;
		}
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			handleSubmit();
		} else if (event.key === 'Escape') {
			riderStore.closePinModal();
		}
	}
</script>

<Popup isVisible={riderStore.pinModalShow} onClose={() => riderStore.closePinModal()}>
	<div class="flex flex-col gap-4 rounded-xl bg-white p-6">
		<h2 class="text-2xl font-bold">Enter Reservation PIN</h2>
		<p class="text-gray-700">Please enter your reservation PIN to checkout the bike.</p>
		<input
			type="text"
			bind:value={riderStore.pinModalPin}
			placeholder="Enter PIN"
			class="w-full rounded border border-gray-300 px-3 py-2 focus:border-lime-300 focus:outline-none"
			disabled={riderStore.pinModalSubmitting}
			onkeydown={handleKeydown}
		/>
		<div class="flex justify-end gap-4">
			<Button
				onclick={() => riderStore.closePinModal()}
				text="Cancel"
				variant="red"
				disable={riderStore.pinModalSubmitting}
			/>
			<Button
				onclick={handleSubmit}
				text={riderStore.pinModalSubmitting ? 'Checking out...' : 'Checkout'}
				variant="green"
				disable={riderStore.pinModalSubmitting}
			/>
		</div>
	</div>
</Popup>
