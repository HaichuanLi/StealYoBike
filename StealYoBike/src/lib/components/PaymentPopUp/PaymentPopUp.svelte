<script lang="ts">
    import Button from '$lib/components/Button/Button.svelte';
    import Popup from '$lib/components/Popup/Popup.svelte';
    import { authApi } from '$lib/api/auth.api';
	import type { UserInfoResponse } from '$lib/api/types/auth.types';

	interface Props {
		showPaymentPopup: boolean;
		paymentTokenInput: string;
		savingPayment: boolean;
		closePaymentPopup: () => void;
		onUserUpdate: (user: UserInfoResponse) => void;
    }

	let { 
        showPaymentPopup = $bindable(),
        paymentTokenInput = $bindable(),
        savingPayment = $bindable(),
        closePaymentPopup,
        onUserUpdate
    }: Props = $props();
  
     async function savePaymentToken() {
        if (!paymentTokenInput.trim()) {
            alert('Please enter a payment token');
            return;
        }

        savingPayment = true;
        try {
            const response = await authApi.updatePaymentToken(paymentTokenInput.trim());
			onUserUpdate(response.data);
            
            console.log('Payment token saved successfully:', response.data.paymentToken);
            closePaymentPopup();
            
        } catch (error) {
            console.error('Failed to save payment token:', error);
            alert('Failed to save payment token. Please try again.');
        } finally {
            savingPayment = false;
        }
    }

    function handleKeydown(event: KeyboardEvent) {
        if (event.key === 'Enter') {
            savePaymentToken();
        } else if (event.key === 'Escape') {
            closePaymentPopup();
        }
    }
</script>


<Popup 
	isVisible={showPaymentPopup} 
	onClose={closePaymentPopup} 
>
	<div class="flex flex-col bg-white gap-4 p-6">
		<h2 class="text-2xl font-bold">Add Payment Method</h2>
		<p class="text-gray-700">Please enter your payment token to proceed with bike reservations and returns.</p>
		<input
			type="text"
			bind:value={paymentTokenInput}
			placeholder="Enter payment token"
			class="w-full rounded border border-gray-300 px-3 py-2 focus:border-lime-300 focus:outline-none"
			disabled={savingPayment}
			onkeydown={handleKeydown}
		/>
		<div class="flex justify-end gap-4">
			<Button 
				onclick={closePaymentPopup} 
				text="Cancel" 
				variant="red"
				disable={savingPayment}
			/>
			<Button
				onclick={savePaymentToken} 
				text={savingPayment ? 'Saving...' : 'Save'} 
				variant="green"
				disable={savingPayment}
			/>
		</div>
	</div>
</Popup>
