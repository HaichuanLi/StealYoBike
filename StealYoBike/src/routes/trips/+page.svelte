<script lang="ts">
    import { onMount } from 'svelte';
    import { riderApi } from '$lib/api/rider.api';
    import type { PastTripResponse } from '$lib/api/types/rider.types';
    import { authApi } from '$lib/api/auth.api';
    import Toast from '$lib/components/Toast/Toast.svelte';
    import Sidebar from '$lib/components/sidebar/sidebar.svelte';
    import { showToast } from '$lib/stores/toast';

    let pastTrips: PastTripResponse[] | null = null;
    let loading = false;
    let user: any = null;

    async function load() {
        try {
            loading = true;
            const resp = await riderApi.getPastTrips();
            pastTrips = resp.data;
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
        } catch (err) {
            user = null;
        }
        await load();
    });

    async function payTrip(t: PastTripResponse) {
        try {
            // show spinner per trip by temporarily attaching paying flag
            t['_paying'] = true as any;
            const resp = await riderApi.payTrip(t.tripId, { paymentToken: user?.paymentToken ?? '' });
            // refresh list
            await load();
            showToast('Payment successful', 'success');
        } catch (err) {
            console.error('Payment failed', err);
            showToast('Payment failed', 'error');
            alert('Payment failed');
        } finally {
            delete t['_paying'];
        }
    }
</script>

<section class="p-6">
    <Sidebar />
    <h1 class="text-2xl font-semibold mb-4">My Trips</h1>
    {#if loading}
        <p>Loading...</p>
    {:else if pastTrips && pastTrips.length > 0}
        <ul class="space-y-3">
            {#each pastTrips as t}
                <li class="p-4 border rounded flex justify-between items-center">
                    <div>
                        <div class="font-medium">Trip #{t.tripId}</div>
                        <div class="text-sm text-gray-600">{t.startStationName ?? '—'} → {t.endStationName ?? '—'}</div>
                        <div class="text-xs text-gray-500">{t.startTime ? new Date(t.startTime).toLocaleString() : ''} — {t.endTime ? new Date(t.endTime).toLocaleString() : ''} ({t.durationMinutes}m)</div>
                    </div>
                    <div class="text-right">
                        <div class="font-semibold">${t.totalAmount.toFixed(2)}</div>
                        {#if t.paid}
                            <div class="text-sm text-green-700">Paid</div>
                        {:else}
                            <button class="mt-1 px-3 py-1 bg-blue-600 text-white rounded" disabled={t['_paying']} on:click={() => payTrip(t)}>{t['_paying'] ? 'Paying...' : 'Pay'}</button>
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
