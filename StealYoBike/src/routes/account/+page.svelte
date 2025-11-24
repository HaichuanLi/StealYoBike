<script lang="ts">
    import BurgerMenu from '$lib/components/BurgerMenu/BurgerMenu.svelte';
    import { authApi } from '$lib/api/auth.api';
    import { api } from '$lib/api';
    import { onMount } from 'svelte';
    import type { UserInfoResponse } from '$lib/api/types/auth.types';

    const defaultUser: UserInfoResponse = {
        id: 0,
        email: '',
        username: '',
        fullName: '',
        role: 'RIDER',
        activeRole: 'RIDER',
        dualRole: false,
        paymentToken: null,
        plan: null,
        tier: 'REGULAR',
        flexDollar: 0,
        tripsLastYear: 0,
        tripsLast3Months: 0,
        tripsLast12Weeks: 0
    };

    let user: UserInfoResponse | null = null;
    let publicMode = false;

    // ---------- Fetch Logged-in User ----------
    onMount(async () => {
        try {
            const res = await authApi.getCurrentUser();
            user = res.data;
        } catch {
            publicMode = true;
            user = defaultUser;
        }
    });

    // ---------- Tier Badge Colors ----------
    function tierColor(tier: string) {
        switch (tier) {
            case 'BRONZE': return 'bg-amber-600 text-white';
            case 'SILVER': return 'bg-gray-300 text-black';
            case 'GOLD':   return 'bg-yellow-400 text-black';
            default:       return 'bg-gray-200 text-gray-700';
        }
    }

    // ---------- Reactive Tier & Progress ----------
    $: currentTier = user?.tier || 'REGULAR';

    $: nextTier =
        currentTier === 'REGULAR'
            ? 'BRONZE'
            : currentTier === 'BRONZE'
                ? 'SILVER'
                : currentTier === 'SILVER'
                    ? 'GOLD'
                    : null;

    // reactive trip counters
    $: tripsLastYear = user?.tripsLastYear ?? 0;
    $: tripsLast3Months = user?.tripsLast3Months ?? 0;
    $: tripsLast12Weeks = user?.tripsLast12Weeks ?? 0;

    // calculate progress
    $: progress = 0;

    if (nextTier === 'BRONZE') {
        progress = Math.min((tripsLastYear / 10) * 100, 100);
    } else if (nextTier === 'SILVER') {
        progress = Math.min((tripsLast3Months / 5) * 100, 100);
    } else if (nextTier === 'GOLD') {
        progress = Math.min((tripsLast12Weeks / 5) * 100, 100);
    }

    // ---------- Dual-role active role switch ----------
    async function setActiveRole(role: 'RIDER' | 'OPERATOR') {
        if (!user) return;
        if (user.activeRole === role) return;

        const res = await api.put<UserInfoResponse>('/auth/active-role', { role });
        user = { ...user, ...res.data };
    }
</script>

<BurgerMenu />

<div class="min-h-screen bg-gradient-to-br from-emerald-50 to-teal-100 p-6 pb-20">
    <div class="mx-auto max-w-5xl">

        <!-- PAGE HEADER -->
        <h1 class="text-6xl font-black text-center text-emerald-700 drop-shadow mb-2">
            Account Information
        </h1>
        <p class="text-center text-lg text-emerald-600 mb-10">
            View your loyalty status, flex balance, and account details
        </p>

        <!-- MAIN CONTENT CARD -->
        <div class="bg-white rounded-3xl shadow-2xl p-10 space-y-12">

            <!-- Current Tier + Role Toggle -->
            <section class="space-y-4">
                <h2 class="text-2xl font-bold text-emerald-700">Your Current Tier</h2>

                <div class="flex flex-wrap items-center gap-4">

                    <!-- Tier Badge -->
                    <span class={`px-6 py-2 rounded-full text-lg font-bold shadow ${tierColor(currentTier)}`}>
                        {currentTier}
                    </span>

                    <!-- Dual-role toggle -->
                    {#if user && user.dualRole}
                        <div class="flex gap-2 bg-emerald-100 rounded-full p-1 shadow-inner">
                            <button
                                    class={`px-4 py-1 rounded-full text-xs font-semibold transition ${
                                    user.activeRole === 'RIDER'
                                        ? 'bg-emerald-600 text-white shadow'
                                        : 'text-emerald-800'
                                }`}
                                    on:click={() => setActiveRole('RIDER')}
                            >
                                Rider View
                            </button>

                            <button
                                    class={`px-4 py-1 rounded-full text-xs font-semibold transition ${
                                    user.activeRole === 'OPERATOR'
                                        ? 'bg-emerald-600 text-white shadow'
                                        : 'text-emerald-800'
                                }`}
                                    on:click={() => setActiveRole('OPERATOR')}
                            >
                                Operator View
                            </button>
                        </div>
                    {/if}
                </div>
            </section>

            <!-- Flex Dollars -->
            <section class="bg-emerald-50 rounded-2xl p-6 shadow">
                <h2 class="text-xl font-bold text-emerald-700 mb-1">Flex Dollar Balance</h2>

                <p class="text-5xl font-black text-emerald-900 leading-none">
                    ${user?.flexDollar?.toFixed(2) ?? '0.00'}
                </p>

                <p class="text-sm text-emerald-600 mt-2 font-medium">
                    Earn flex dollars by completing trips!
                </p>
            </section>

            <!-- Loyalty Tier Cards -->
            <section>
                <h3 class="text-2xl font-bold text-gray-800 mb-6">Loyalty Tiers</h3>

                <div class="grid md:grid-cols-3 gap-6">

                    <!-- Regular -->
                    <div class="rounded-xl border bg-gray-50 p-6 text-center shadow-sm">
                        <h4 class="text-xl font-bold text-gray-700 mb-1">REGULAR</h4>
                        <p class="text-sm text-gray-600">No perks</p>
                        <span class="mt-3 inline-block text-xs px-3 py-1 rounded-full bg-gray-200 text-gray-700">
                            Default Tier
                        </span>
                    </div>

                    <!-- Bronze -->
                    <div class={`rounded-xl p-6 text-center shadow-sm ${
                        currentTier === 'BRONZE'
                            ? 'ring-4 ring-amber-400'
                            : 'bg-amber-50'
                    }`}>
                        <h4 class="text-xl font-bold text-amber-700 mb-1">BRONZE</h4>
                        <p class="text-sm text-gray-700">5% discount</p>
                        <p class="text-sm text-gray-700">10+ yearly trips</p>
                    </div>

                    <!-- Silver -->
                    <div class={`rounded-xl p-6 text-center shadow-sm ${
                        currentTier === 'SILVER'
                            ? 'ring-4 ring-gray-300'
                            : 'bg-gray-100'
                    }`}>
                        <h4 class="text-xl font-bold text-gray-700 mb-1">SILVER</h4>
                        <p class="text-sm text-gray-700">10% discount</p>
                        <p class="text-sm text-gray-700">Monthly consistency</p>
                    </div>

                    <!-- Gold -->
                    <div class={`rounded-xl p-6 text-center shadow-sm md:col-span-3 max-w-xs mx-auto ${
                        currentTier === 'GOLD'
                            ? 'ring-4 ring-yellow-300'
                            : 'bg-yellow-50'
                    }`}>
                        <h4 class="text-xl font-bold text-yellow-600 mb-1">GOLD</h4>
                        <p class="text-sm text-gray-700">15% discount</p>
                        <p class="text-sm text-gray-700">Weekly consistency</p>
                    </div>
                </div>
            </section>

            <!-- Progress Bar -->
            {#if nextTier}
                <section>
                    <h3 class="text-2xl font-bold text-gray-800 mb-3 text-center">
                        Progress Toward {nextTier} Tier
                    </h3>

                    <div class="w-full bg-gray-200 h-4 rounded-full shadow-inner overflow-hidden">
                        <div
                                class={`h-full transition-all duration-700 ${
                                nextTier === 'BRONZE'
                                    ? 'bg-amber-600'
                                    : nextTier === 'SILVER'
                                        ? 'bg-gray-400'
                                        : 'bg-yellow-400'
                            }`}
                                style={`width: ${progress}%`}
                        ></div>
                    </div>

                    <p class="text-sm text-gray-600 mt-2 text-center font-medium">
                        {Math.floor(progress)}% complete
                    </p>
                </section>
            {/if}

        </div>
    </div>
</div>
