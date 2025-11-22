<script lang="ts">
    import BurgerMenu from '$lib/components/BurgerMenu/BurgerMenu.svelte';
    import { authApi } from '$lib/api/auth.api';
    import { onMount } from 'svelte';

    type UserResponse = {
        tier?: string;
        tripsLastYear?: number;
        tripsLast3Months?: number;
        tripsLast12Weeks?: number;
        [key: string]: any;
    };

    let user: UserResponse | null = null;
    let publicMode = false;

    let tripsLastYear = 0;
    let tripsLast3Months = 0;
    let tripsLast12Weeks = 0;

    onMount(async () => {
        try {
            const res = await authApi.getCurrentUser();
            user = res.data;

            tripsLastYear = user.tripsLastYear ?? 0;
            tripsLast3Months = user.tripsLast3Months ?? 0;
            tripsLast12Weeks = user.tripsLast12Weeks ?? 0;
        } catch {
            publicMode = true;
            user = { tier: "REGULAR" };
        }
    });

    function tierColor(tier: string) {
        switch (tier) {
            case 'BRONZE': return 'bg-amber-600 text-white';
            case 'SILVER': return 'bg-gray-400 text-black';
            case 'GOLD': return 'bg-yellow-400 text-black';
            default: return 'bg-gray-200 text-gray-700';
        }
    }

    // Current and next tier logic
    $: currentTier = user?.tier || "REGULAR";

    $: nextTier =
        currentTier === "REGULAR" ? "BRONZE" :
            currentTier === "BRONZE"  ? "SILVER" :
                currentTier === "SILVER"  ? "GOLD"   :
                    null;

    // Progress calc
    $: progress = 0;

    if (nextTier === "BRONZE") {
        progress = Math.min((tripsLastYear / 10) * 100, 100);
    } else if (nextTier === "SILVER") {
        progress = Math.min((tripsLast3Months / 5) * 100, 100);
    } else if (nextTier === "GOLD") {
        progress = Math.min((tripsLast12Weeks / 5) * 100, 100);
    }
</script>

<BurgerMenu />

<div class="min-h-screen bg-gradient-to-br from-emerald-50 to-teal-100 p-6">
    <div class="mx-auto max-w-5xl">

        <!-- Header -->
        <h1 class="text-7xl font-black text-center text-emerald-700 drop-shadow-lg mb-4">
            Account Information
        </h1>
        <p class="text-center text-xl text-emerald-600 mb-12">
            View your loyalty tier and account details
        </p>

        <!-- Loyalty Program -->
        <div class="bg-white rounded-2xl shadow-xl p-10">

            <!-- Current Tier -->
            <p class="text-2xl font-bold text-emerald-700 mb-3">Your Current Tier</p>

            <span class={`px-5 py-2 rounded-full text-lg font-bold ${tierColor(currentTier)}`}>
                {currentTier}
            </span>

            {#if publicMode}
                <p class="mt-2 text-gray-600 text-sm">
                    You are viewing this page in public mode. Log in to see real data.
                </p>
            {/if}

            <!-- Tier Overview -->
            <h3 class="text-2xl font-bold text-gray-800 mt-12 mb-6">Loyalty Tiers</h3>

            <div class="grid md:grid-cols-3 gap-6">

                <!-- REGULAR -->
                <div class="p-5 border rounded-xl bg-gray-50 text-center shadow-sm">
                    <h4 class="text-xl font-bold text-gray-700 mb-2">REGULAR</h4>
                    <p class="text-sm text-gray-600">No perks</p>
                    <div class="mt-3">
                        <span class="text-xs px-3 py-1 rounded-full bg-gray-200 text-gray-700">
                            Default Tier
                        </span>
                    </div>
                </div>

                <!-- BRONZE -->
                <div class={`p-5 border rounded-xl text-center shadow-sm ${currentTier === 'BRONZE' ? 'ring-4 ring-amber-400' : 'bg-amber-50'}`}>
                    <h4 class="text-xl font-bold text-amber-700 mb-2">BRONZE</h4>
                    <p class="text-sm text-gray-700">5% discount on trips</p>
                    <p class="text-sm text-gray-700">10+ trips last year</p>
                    {#if currentTier === 'BRONZE'}
                        <p class="mt-3 text-sm text-amber-700 font-bold">Current Tier</p>
                    {/if}
                </div>

                <!-- SILVER -->
                <div class={`p-5 border rounded-xl text-center shadow-sm ${currentTier === 'SILVER' ? 'ring-4 ring-gray-300' : 'bg-gray-100'}`}>
                    <h4 class="text-xl font-bold text-gray-600 mb-2">SILVER</h4>
                    <p class="text-sm text-gray-700">10% discount + 2-min hold</p>
                    <p class="text-sm text-gray-700">5 successful reservations + 5 monthly trips</p>
                    {#if currentTier === 'SILVER'}
                        <p class="mt-3 text-sm text-gray-700 font-bold">Current Tier</p>
                    {/if}
                </div>

                <!-- GOLD -->
                <div class={`p-5 border rounded-xl text-center shadow-sm
                    ${currentTier === 'GOLD' ? 'ring-4 ring-yellow-300' : 'bg-yellow-50'}
                    md:col-start-2`}>
                    <h4 class="text-xl font-bold text-yellow-600 mb-2">GOLD</h4>
                    <p class="text-sm text-gray-700">15% discount + 5-min hold</p>
                    <p class="text-sm text-gray-700">5 weekly trips for 12 weeks</p>
                    {#if currentTier === 'GOLD'}
                        <p class="mt-3 text-sm text-yellow-700 font-bold">Current Tier</p>
                    {/if}
                </div>
            </div>

            <!-- Progress Section -->
            {#if nextTier}
                <h3 class="text-2xl font-bold text-gray-800 mt-12 mb-4">
                    Progress Toward {nextTier} Tier
                </h3>

                <div class="w-full bg-gray-200 h-4 rounded-full overflow-hidden">
                    <div
                            class={`h-full transition-all ${
                            nextTier === 'BRONZE'
                                ? 'bg-amber-600'
                                : nextTier === 'SILVER'
                                ? 'bg-gray-400'
                                : 'bg-yellow-400'
                        }`}
                            style={`width: ${progress}%`}
                    ></div>
                </div>

                <p class="text-sm text-gray-600 mt-1">
                    {Math.floor(progress)}% complete
                </p>
            {:else}
                <h3 class="text-2xl font-bold text-gray-800 mt-12 mb-4">🎉 You reached the highest tier!</h3>
            {/if}
        </div>
    </div>
</div>
