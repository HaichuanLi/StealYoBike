<script lang='ts'>
    import { onMount } from 'svelte';
    import { pricingApi, type PricingResponse } from '$lib/api/pricing.api';

    let pricing = $state<PricingResponse | null>(null);
    let loading = $state(true);
    let error = $state<string | null>(null);

    onMount(async () => {
        try {
            const response = await pricingApi.getCurrentPricing();
            pricing = response.data;
        } catch (err) {
            console.error('Failed to load pricing:', err);
            error = 'Failed to load pricing information';
            // Fallback to default pricing
            pricing = {
                standardBike: {
                    baseFee: 1.50,
                    perMinuteRate: 0.15
                },
                eBike: {
                    baseFee: 2.50,
                    perMinuteRate: 0.25,
                    surcharge: 1.00
                }
            };
        } finally {
            loading = false;
        }
    });

    // Helper functions for calculations
    function formatPrice(price: number): string {
        return `$${price.toFixed(2)}`;
    }

    function calculateStandardTrip(minutes: number): number {
        if (!pricing) return 0;
        return pricing.standardBike.baseFee + (minutes * pricing.standardBike.perMinuteRate);
    }

    function calculateEBikeTrip(minutes: number): number {
        if (!pricing) return 0;
        return pricing.eBike.baseFee + (minutes * pricing.eBike.perMinuteRate) + pricing.eBike.surcharge;
    }
</script>

{#if loading}
    <div class="flex justify-center items-center py-16">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-500"></div>
    </div>
{:else if error}
    <div class="mb-16 rounded-2xl bg-red-50 p-8 text-center">
        <p class="text-red-600 mb-4">{error}</p>
        <p class="text-sm text-gray-600">Using default pricing shown below</p>
    </div>
{/if}

{#if pricing}
    <div class="mb-16 grid gap-8 md:grid-cols-2">
        <!-- Standard Bikes -->
        <div class="rounded-2xl bg-white p-8 shadow-lg">
            <div class="mb-6 flex items-center">
                <div class="mr-4 rounded-full bg-emerald-100 p-3">
                    <span class="text-3xl">🚲</span>
                </div>
                <div>
                    <h3 class="text-2xl font-bold text-emerald-700">Standard Bikes</h3>
                    <p class="text-emerald-600">Classic pedal-powered bikes</p>
                </div>
            </div>
            <div class="space-y-4">
                <div class="flex justify-between items-center py-3 border-b border-gray-100">
                    <span class="text-gray-700">Base Fee</span>
                    <span class="text-2xl font-bold text-emerald-600">{formatPrice(pricing.standardBike.baseFee)}</span>
                </div>
                <div class="flex justify-between items-center py-3 border-b border-gray-100">
                    <span class="text-gray-700">Per Minute Rate</span>
                    <span class="text-2xl font-bold text-emerald-600">{formatPrice(pricing.standardBike.perMinuteRate)}</span>
                </div>
                <div class="flex justify-between items-center py-3">
                    <span class="text-gray-700">Reservation</span>
                    <span class="text-2xl font-bold text-green-500">FREE</span>
                </div>
            </div>
        </div>

        <!-- E-Bikes -->
        <div class="rounded-2xl bg-white p-8 shadow-lg border-2 border-emerald-200">
            <div class="mb-2 text-center">
                <span class="inline-block rounded-full bg-emerald-500 px-3 py-1 text-sm font-semibold text-white">
                    PREMIUM
                </span>
            </div>
            <div class="mb-6 flex items-center">
                <div class="mr-4 rounded-full bg-emerald-100 p-3">
                    <span class="text-3xl">⚡</span>
                </div>
                <div>
                    <h3 class="text-2xl font-bold text-emerald-700">E-Bikes</h3>
                    <p class="text-emerald-600">Electric-powered assistance</p>
                </div>
            </div>
            <div class="space-y-4">
                <div class="flex justify-between items-center py-3 border-b border-gray-100">
                    <span class="text-gray-700">Base Fee</span>
                    <span class="text-2xl font-bold text-emerald-600">{formatPrice(pricing.eBike.baseFee)}</span>
                </div>
                <div class="flex justify-between items-center py-3 border-b border-gray-100">
                    <span class="text-gray-700">Per Minute Rate</span>
                    <span class="text-2xl font-bold text-emerald-600">{formatPrice(pricing.eBike.perMinuteRate)}</span>
                </div>
                <div class="flex justify-between items-center py-3 border-b border-gray-100">
                    <span class="text-gray-700">E-Bike Surcharge</span>
                    <span class="text-2xl font-bold text-orange-500">+{formatPrice(pricing.eBike.surcharge)}</span>
                </div>
                <div class="flex justify-between items-center py-3">
                    <span class="text-gray-700">Reservation</span>
                    <span class="text-2xl font-bold text-green-500">FREE</span>
                </div>
            </div>
        </div>
    </div>

    <!-- Pricing Plans -->
    <div class="mb-16">
        <h2 class="mb-8 text-center text-4xl font-bold text-emerald-700">Choose Your Plan</h2>
        <div class="grid gap-8 md:grid-cols-3">
            <!-- Pay Per Ride -->
            <div class="rounded-2xl bg-white p-8 shadow-lg">
                <h3 class="mb-4 text-2xl font-bold text-gray-800">Pay Per Ride</h3>
                <div class="mb-6">
                    <span class="text-4xl font-bold text-emerald-600">$0</span>
                    <span class="text-gray-600">/month</span>
                </div>
                <ul class="mb-8 space-y-3">
                    <li class="flex items-center">
                        <span class="mr-3 text-green-500">✓</span>
                        <span>Pay only when you ride</span>
                    </li>
                    <li class="flex items-center">
                        <span class="mr-3 text-green-500">✓</span>
                        <span>Free reservations</span>
                    </li>
                    <li class="flex items-center">
                        <span class="mr-3 text-green-500">✓</span>
                        <span>Standard & E-bike access</span>
                    </li>
                </ul>
                <button class="w-full rounded-lg bg-emerald-600 py-3 font-semibold text-white hover:bg-emerald-700 transition-colors">
                    Get Started
                </button>
            </div>

            <!-- Monthly Plan -->
            <div class="rounded-2xl bg-white p-8 shadow-lg border-2 border-emerald-500 transform scale-105">
                <div class="mb-4 text-center">
                    <span class="inline-block rounded-full bg-emerald-500 px-3 py-1 text-sm font-semibold text-white">
                        MOST POPULAR
                    </span>
                </div>
                <h3 class="mb-4 text-2xl font-bold text-gray-800">Monthly Pass</h3>
                <div class="mb-6">
                    <span class="text-4xl font-bold text-emerald-600">$29</span>
                    <span class="text-gray-600">/month</span>
                </div>
                <ul class="mb-8 space-y-3">
                    <li class="flex items-center">
                        <span class="mr-3 text-green-500">✓</span>
                        <span>Unlimited 30-min rides</span>
                    </li>
                    <li class="flex items-center">
                        <span class="mr-3 text-green-500">✓</span>
                        <span>50% off E-bike surcharge</span>
                    </li>
                    <li class="flex items-center">
                        <span class="mr-3 text-green-500">✓</span>
                        <span>Priority bike access</span>
                    </li>
                </ul>
                <button class="w-full rounded-lg bg-emerald-600 py-3 font-semibold text-white hover:bg-emerald-700 transition-colors">
                    Choose Plan
                </button>
            </div>

            <!-- Annual Plan -->
            <div class="rounded-2xl bg-white p-8 shadow-lg">
                <h3 class="mb-4 text-2xl font-bold text-gray-800">Annual Pass</h3>
                <div class="mb-6">
                    <span class="text-4xl font-bold text-emerald-600">$199</span>
                    <span class="text-gray-600">/year</span>
                    <div class="text-sm text-green-600 font-semibold">Save $149!</div>
                </div>
                <ul class="mb-8 space-y-3">
                    <li class="flex items-center">
                        <span class="mr-3 text-green-500">✓</span>
                        <span>Unlimited 45-min rides</span>
                    </li>
                    <li class="flex items-center">
                        <span class="mr-3 text-green-500">✓</span>
                        <span>Free E-bike access</span>
                    </li>
                    <li class="flex items-center">
                        <span class="mr-3 text-green-500">✓</span>
                        <span>Premium support</span>
                    </li>
                </ul>
                <button class="w-full rounded-lg bg-emerald-600 py-3 font-semibold text-white hover:bg-emerald-700 transition-colors">
                    Best Value
                </button>
            </div>
        </div>
    </div>

    <!-- Example Calculation -->
    <div class="rounded-2xl bg-emerald-50 p-8">
        <h3 class="mb-6 text-2xl font-bold text-emerald-700">Example Trip Cost</h3>
        <div class="grid gap-6 md:grid-cols-2">
            <div class="rounded-lg bg-white p-6">
                <h4 class="mb-4 text-lg font-semibold text-gray-800">15-minute Standard Bike Trip</h4>
                <div class="space-y-2 text-sm">
                    <div class="flex justify-between">
                        <span>Base Fee:</span>
                        <span>{formatPrice(pricing.standardBike.baseFee)}</span>
                    </div>
                    <div class="flex justify-between">
                        <span>15 minutes × {formatPrice(pricing.standardBike.perMinuteRate)}:</span>
                        <span>{formatPrice(15 * pricing.standardBike.perMinuteRate)}</span>
                    </div>
                    <hr class="my-3">
                    <div class="flex justify-between font-bold text-lg">
                        <span>Total:</span>
                        <span class="text-emerald-600">{formatPrice(calculateStandardTrip(15))}</span>
                    </div>
                </div>
            </div>
            <div class="rounded-lg bg-white p-6">
                <h4 class="mb-4 text-lg font-semibold text-gray-800">15-minute E-Bike Trip</h4>
                <div class="space-y-2 text-sm">
                    <div class="flex justify-between">
                        <span>Base Fee:</span>
                        <span>{formatPrice(pricing.eBike.baseFee)}</span>
                    </div>
                    <div class="flex justify-between">
                        <span>15 minutes × {formatPrice(pricing.eBike.perMinuteRate)}:</span>
                        <span>{formatPrice(15 * pricing.eBike.perMinuteRate)}</span>
                    </div>
                    <div class="flex justify-between">
                        <span>E-bike Surcharge:</span>
                        <span>{formatPrice(pricing.eBike.surcharge)}</span>
                    </div>
                    <hr class="my-3">
                    <div class="flex justify-between font-bold text-lg">
                        <span>Total:</span>
                        <span class="text-emerald-600">{formatPrice(calculateEBikeTrip(15))}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
{/if}


