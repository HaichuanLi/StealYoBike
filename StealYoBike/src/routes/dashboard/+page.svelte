<script lang="ts">
    import type { StationSummary } from '$lib/api/types';
    import Button from '$lib/components/Button/Button.svelte';
    import DashboardBody from '$lib/components/DashboardBody/DashboardBody.svelte';
    import DashboardHeader from '$lib/components/DashboardHeader/DashboardHeader.svelte';
    import type { UserInfoResponse } from '$lib/api/types/auth.types';
    import { onMount } from 'svelte';
    import { authApi } from '$lib/api/auth.api';
    import Map from '$lib/components/Map/Map.svelte';
	import PaymentPopUp from '$lib/components/PaymentPopUp/PaymentPopUp.svelte';


    let selectedStation = $state<StationSummary | null>(null);
    let user = $state<UserInfoResponse | null>(null);
    let loading = $state(true);
    
    let showPaymentPopup = $state(false);
    let paymentTokenInput = $state('');
    let savingPayment = $state(false);

    let hasPaymentMethod = $derived(user?.paymentToken != null && user?.paymentToken !== "");

    onMount(async () => {
        try {
            const response = await authApi.getCurrentUser();
            user = response.data;
        } catch (error) {
            console.error('Failed to load user:', error);
            user = null;
        } finally {
            loading = false;
        }
    });

    function handleReserveBike() {
        if (!hasPaymentMethod) {
            showPaymentPopup = true;
            return;
        }
        console.log('Reserving bike...');
        // cook up reserve logic
    }

    function handleReturnBike() {
        if (!hasPaymentMethod) {
            showPaymentPopup = true;
            return;
        }
        console.log('Returning bike...');
        // cook return logic
    }

    function closePaymentPopup() {
        showPaymentPopup = false;
        paymentTokenInput = '';
        savingPayment = false;
    }

	function handleUserUpdate(updatedUser: UserInfoResponse) {
        user = updatedUser;
    }
</script>

<DashboardHeader />
<DashboardBody>
    <div>
        <Map bind:selectedStation />
    </div>
    <div>
        <div class="size-full rounded-xl bg-lime-50">
            {#if selectedStation}
                <div class="flex h-full flex-col items-center justify-center gap-2 sm:*:max-w-40 lg:flex-row">
                    <Button 
                        onclick={handleReserveBike} 
                        text="Reserve Bike" 
                        disable={!hasPaymentMethod}
                        variant="green"
                    />
                    <Button 
                        onclick={handleReturnBike} 
                        text="Return Bike"
                        disable={!hasPaymentMethod}
                        variant="teal"
                    />
                </div>
                {#if !hasPaymentMethod}
                    <div class="text-center mt-4 p-4">
                        <p class="text-orange-600 text-sm mb-2">⚠️ Payment method required to use bikes</p>
                        <Button 
                            onclick={() => showPaymentPopup = true} 
                            text="Add Payment Method" 
                            variant="red"
                        />
                    </div>
                {/if}
            {:else}
                <div class="h-full content-center text-center">Select a Station to get Started!</div>
            {/if}
        </div>
    </div>
</DashboardBody>

<PaymentPopUp 
	bind:showPaymentPopup
	bind:paymentTokenInput
	bind:savingPayment
	{closePaymentPopup}
	onUserUpdate={handleUserUpdate}
/>
