<script lang="ts">
	import Button from '$lib/components/Button/Button.svelte';
	import Popup from '$lib/components/Popup/Popup.svelte';

	interface Props {
		showPinPopup: boolean;
		pinInput: string;
		submittingPin: boolean;
		closePinPopup: () => void;
		onPinSubmit: (pin: string) => Promise<void>;
	}

	let {
		showPinPopup = $bindable(),
		pinInput = $bindable(),
		submittingPin = $bindable(),
		closePinPopup,
		onPinSubmit
	}: Props = $props();

	async function handleSubmit() {
		if (!pinInput.trim()) {
			alert('Please enter your reservation PIN');
			return;
		}

		submittingPin = true;
		try {
			await onPinSubmit(pinInput.trim());
		} catch (error) {
			console.error('Failed to submit PIN:', error);
			alert('Failed to checkout bike. Please try again.');
		} finally {
			submittingPin = false;
		}
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			handleSubmit();
		} else if (event.key === 'Escape') {
			closePinPopup();
		}
	}
</script>

<Popup isVisible={showPinPopup} onClose={closePinPopup}>
	<div class="flex flex-col gap-4 rounded-xl bg-white p-6">
		<h2 class="text-2xl font-bold">Enter Reservation PIN</h2>
		<p class="text-gray-700">Please enter your reservation PIN to checkout the bike.</p>
		<input
			type="text"
			bind:value={pinInput}
			placeholder="Enter PIN"
			class="w-full rounded border border-gray-300 px-3 py-2 focus:border-lime-300 focus:outline-none"
			disabled={submittingPin}
			onkeydown={handleKeydown}
		/>
		<div class="flex justify-end gap-4">
			<Button onclick={closePinPopup} text="Cancel" variant="red" disable={submittingPin} />
			<Button
				onclick={handleSubmit}
				text={submittingPin ? 'Checking out...' : 'Checkout'}
				variant="green"
				disable={submittingPin}
			/>
		</div>
	</div>
</Popup>
