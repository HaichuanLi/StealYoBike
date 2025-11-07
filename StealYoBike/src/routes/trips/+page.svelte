<script lang="ts">
    import { onMount } from 'svelte';
    import { riderApi } from '$lib/api/rider.api';
    import { operatorApi } from '$lib/api/operator.api';
    import type { PastTripResponse } from '$lib/api/types/rider.types';
    import { authApi } from '$lib/api/auth.api';
    import Toast from '$lib/components/Toast/Toast.svelte';
    import Sidebar from '$lib/components/sidebar/sidebar.svelte';
    import { showToast } from '$lib/stores/toast';
    import type { WithUIState } from '$lib/api/types/ui.types';

    let pastTrips: WithUIState<PastTripResponse>[] | null = null;
    let loading = false;
    let user: any = null;
    let showSidebar = false;
    let isOperator = false;

    async function load() {
        try {
            loading = true;
            if (isOperator) {
                // Load all trips for operator
                const resp = await operatorApi.getAllPastTrips();
                pastTrips = resp.data;
            } else {
                // Load rider's own trips
                const resp = await riderApi.getPastTrips();
                pastTrips = resp.data;
            }
        } catch (err) {
            console.warn('Failed to load past trips', err);
            pastTrips = null;
        } finally {
            loading = false;
        }
    }

    onMount(async () => {
        try {
            const u = await authApi.getCurrentUser();
            user = u.data;
            isOperator = user?.role === 'OPERATOR';
        } catch (err) {
            user = null;
        }
        await load();
    });

    async function payTrip(t: WithUIState<PastTripResponse>) {
        try {
            // show spinner per trip by temporarily attaching paying flag
            t._paying = true;
            const resp = await riderApi.payTrip(t.tripId, { paymentToken: user?.paymentToken ?? '' });
            // refresh list
            await load();
            showToast('Payment successful', 'success');
        } catch (err) {
            console.error('Payment failed', err);
            showToast('Payment failed', 'error');
            alert('Payment failed');
        } finally {
            t._paying = false;
        }
    }
</script>

<section class="p-6">
    <!-- Menu button -->
    <button
        class="fixed top-4 left-4 z-50 p-2"
        on:click={() => showSidebar = true}
        aria-label="Open Menu"
    >
        <svg 
            xmlns="http://www.w3.org/2000/svg" 
            class="h-6 w-6" 
            fill="none" 
            viewBox="0 0 24 24" 
            stroke="currentColor"
        >
            <path 
                stroke-linecap="round" 
                stroke-linejoin="round" 
                stroke-width="2" 
                d="M4 6h16M4 12h16M4 18h16"
            />
        </svg>
    </button>

    {#if showSidebar}
        <Sidebar bind:showSidebar />
    {/if}
    <h1 class="text-2xl font-semibold mb-4 ml-12">{isOperator ? 'All User Trips' : 'My Trips'}</h1>
    {#if loading}
        <p>Loading...</p>
    {:else if pastTrips && pastTrips.length > 0}
        <ul class="space-y-3">
            {#each pastTrips as t}
                <li class="p-4 border rounded flex justify-between items-center">
                    <div>
                        <div class="font-medium">Trip #{t.tripId}</div>
                        {#if isOperator && t.userName}
                            <div class="text-sm font-medium text-blue-600">User: {t.userName}</div>
                        {/if}
                        {#if t.bikeId}
                            <div class="text-sm">Bike #{t.bikeId} ({t.bikeType})</div>
                        {/if}
                        <div class="text-sm text-gray-600">{t.startStationName ?? '—'} → {t.endStationName ?? '—'}</div>
                        <div class="text-xs text-gray-500">{t.startTime ? new Date(t.startTime).toLocaleString() : ''} — {t.endTime ? new Date(t.endTime).toLocaleString() : ''} ({t.durationMinutes}m)</div>
                    </div>
                    <div class="text-right">
                        <div class="font-semibold">${t.totalAmount.toFixed(2)}</div>
                        {#if t.paid}
                            <div class="text-sm text-green-700">Paid</div>
                        {:else if !isOperator} 
                            <button class="mt-1 px-3 py-1 bg-blue-600 text-white rounded" disabled={t._paying} on:click={() => payTrip(t)}>{t._paying ? 'Paying...' : 'Pay'}</button>
                        {:else}
                            <div class="text-sm text-yellow-600">Unpaid</div>
                        {/if}
                    </div>
                </li>
            {/each}
        </ul>
    {:else}
        <p class="text-sm text-gray-600">No past trips found.</p>
    {/if}
</section>

<!-- Toast so messages show on trips page too -->
<Toast />
