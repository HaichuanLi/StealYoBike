<script lang="ts">
    import type { TripResponse } from '$lib/api/types/rider.types';
    import Button from '$lib/components/Button/Button.svelte';

    interface Props {
        show: boolean;
        details: TripResponse | null;
        onClose: () => void;
    }
    let { show, details, onClose }: Props = $props();

    const money = (v?: number) => typeof v === 'number' ? `$${v.toFixed(2)}` : '$0.00';
</script>

{#if show && details}
    <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
        <div class="w-[680px] max-w-[95vw] rounded-xl bg-white p-5 shadow-lg">
            <div class="mb-3 flex items-center justify-between">
                <h3 class="text-lg font-semibold">Ride Details — Trip #{details.tripId}</h3>
                <button class="text-gray-600" on:click={onClose}>✕</button>
            </div>

            <div class="grid grid-cols-2 gap-3 text-sm">
                <div><span class="font-medium">Rider:</span> {details.riderName ?? '—'}</div>
                <div><span class="font-medium">Plan:</span> {details.plan ?? '—'}</div>
                <div><span class="font-medium">Bike Type:</span> {details.bikeType ?? '—'}</div>
                <div><span class="font-medium">Start Station:</span> {details.startStation ?? '—'}</div>
                <div><span class="font-medium">End Station:</span> {details.endStation ?? '—'}</div>
                <div><span class="font-medium">Start Time:</span> {details.startTime ? new Date(details.startTime).toLocaleString() : '—'}</div>
                <div><span class="font-medium">End Time:</span> {details.endTime ? new Date(details.endTime).toLocaleString() : '—'}</div>
                <div><span class="font-medium">Duration:</span> {details.durationMinutes} min</div>
            </div>

            <div class="my-4 rounded-lg border p-3">
                <div class="mb-2 font-semibold">Cost breakdown</div>
                <div class="grid grid-cols-2 gap-2 text-sm">
                    <div>Base fee:</div> <div class="text-right">{money(details.baseFee)}</div>
                    <div>Per-minute rate:</div> <div class="text-right">{money(details.perMinuteFee)}</div>
                    <div>E-bike surcharge:</div> <div class="text-right">{money(details.eBikeSurcharge)}</div>
                    <div>Discount:</div> <div class="text-right">{money(details.discountAmount)}</div>
                    <div>Tier Discount:</div> <div class="text-right">{money(details.tierDiscountAmount)}</div>
                    {#if details.flexDollarUsed > 0}
                        <div>Flex Dollar Used:</div> <div class="text-right">{money(details.flexDollarUsed)}</div>
                    {/if}
                    <div class="font-semibold">Total:</div> <div class="text-right font-semibold">{money(details.totalCost)}</div>
                </div>
            </div>

            <div class="my-3">
                <p class="mb-2 text-sm text-gray-700">User Tier: <span class="font-semibold text-emerald-700">{details.tier}</span></p>
            </div>

            <div class="my-3">
                <div class="mb-2 font-semibold">Timeline</div>
                <div class="rounded bg-gray-50 p-2 text-sm">{details.timeline}</div>
            </div>

            <div class="mt-4 flex justify-end">
                <Button text="Close" variant="teal" onclick={onClose} />
            </div>
        </div>
    </div>
{/if}
