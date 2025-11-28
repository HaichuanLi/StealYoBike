<script lang="ts">
	import { api } from '$lib/api';
	import { authApi } from '$lib/api/auth.api';
	import type { UserInfoResponse } from '$lib/api/types/auth.types';
	import BurgerMenu from '$lib/components/BurgerMenu/BurgerMenu.svelte';
	import { onMount } from 'svelte';

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

	let user = $state<UserInfoResponse | null>(null);
	let publicMode = $state(false);

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
			case 'BRONZE':
				return 'bg-amber-600 text-white';
			case 'SILVER':
				return 'bg-gray-300 text-black';
			case 'GOLD':
				return 'bg-yellow-400 text-black';
			default:
				return 'bg-gray-200 text-gray-700';
		}
	}

	// ---------- Reactive Tier & Progress ----------
	let currentTier = $derived(user?.tier || 'REGULAR');

	let nextTier = $derived(
		currentTier === 'REGULAR'
			? 'BRONZE'
			: currentTier === 'BRONZE'
				? 'SILVER'
				: currentTier === 'SILVER'
					? 'GOLD'
					: null
	);

	// reactive trip counters
	let tripsLastYear = $derived(user?.tripsLastYear ?? 0);
	let tripsLast3Months = $derived(user?.tripsLast3Months ?? 0);
	let tripsLast12Weeks = $derived(user?.tripsLast12Weeks ?? 0);

	// calculate progress
	let progress = $derived.by(() => {
		if (nextTier === 'BRONZE') {
			return Math.min((tripsLastYear / 10) * 100, 100);
		} else if (nextTier === 'SILVER') {
			return Math.min((tripsLast3Months / 15) * 100, 100);
		} else if (nextTier === 'GOLD') {
			return Math.min((tripsLast12Weeks / 60) * 100, 100);
		}
		return 0;
	});

	// Check if requirement is met but tier hasn't upgraded yet
	let requirementMet = $derived.by(() => {
		if (nextTier === 'BRONZE') {
			return tripsLastYear >= 10;
		} else if (nextTier === 'SILVER') {
			return tripsLast3Months >= 15;
		} else if (nextTier === 'GOLD') {
			return tripsLast12Weeks >= 60;
		}
		return false;
	});

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
		<h1 class="mb-2 text-center text-6xl font-black text-emerald-700 drop-shadow">
			Account Information
		</h1>
		<p class="mb-10 text-center text-lg text-emerald-600">
			View your loyalty status, flex balance, and account details
		</p>

		<!-- MAIN CONTENT CARD -->
		<div class="space-y-12 rounded-3xl bg-white p-10 shadow-2xl">
			<!-- Current Tier + Role Toggle -->
			<section class="space-y-4">
				<h2 class="text-2xl font-bold text-emerald-700">Your Current Tier</h2>

				<div class="flex flex-wrap items-center gap-4">
					<!-- Tier Badge -->
					<span class={`rounded-full px-6 py-2 text-lg font-bold shadow ${tierColor(currentTier)}`}>
						{currentTier}
					</span>

					<!-- Dual-role toggle -->
					{#if user && user.dualRole}
						<div class="flex gap-2 rounded-full bg-emerald-100 p-1 shadow-inner">
							<button
								class={`rounded-full px-4 py-1 text-xs font-semibold transition ${
									user.activeRole === 'RIDER'
										? 'bg-emerald-600 text-white shadow'
										: 'text-emerald-800'
								}`}
								onclick={() => setActiveRole('RIDER')}
							>
								Rider View
							</button>

							<button
								class={`rounded-full px-4 py-1 text-xs font-semibold transition ${
									user.activeRole === 'OPERATOR'
										? 'bg-emerald-600 text-white shadow'
										: 'text-emerald-800'
								}`}
								onclick={() => setActiveRole('OPERATOR')}
							>
								Operator View
							</button>
						</div>
					{/if}
				</div>
			</section>

			<!-- Flex Dollars -->
			<section class="rounded-2xl bg-emerald-50 p-6 shadow">
				<h2 class="mb-1 text-xl font-bold text-emerald-700">Flex Dollar Balance</h2>

				<p class="text-5xl font-black leading-none text-emerald-900">
					${user?.flexDollar?.toFixed(2) ?? '0.00'}
				</p>

				<p class="mt-2 text-sm font-medium text-emerald-600">
					Earn flex dollars by completing trips!
				</p>
			</section>

			<!-- Loyalty Tier Cards -->
			<section>
				<h3 class="mb-6 text-2xl font-bold text-gray-800">Loyalty Tiers</h3>

				<div class="grid gap-6 md:grid-cols-3">
					<!-- Regular -->
					<div class="rounded-xl border bg-gray-50 p-6 text-center shadow-sm">
						<h4 class="mb-1 text-xl font-bold text-gray-700">REGULAR</h4>
						<p class="text-sm text-gray-600">No perks</p>
						<span
							class="mt-3 inline-block rounded-full bg-gray-200 px-3 py-1 text-xs text-gray-700"
						>
							Default Tier
						</span>
					</div>

					<!-- Bronze -->
					<div
						class={`rounded-xl p-6 text-center shadow-sm ${
							currentTier === 'BRONZE' ? 'ring-4 ring-amber-400' : 'bg-amber-50'
						}`}
					>
						<h4 class="mb-1 text-xl font-bold text-amber-700">BRONZE</h4>
						<p class="text-sm text-gray-700">5% discount</p>
						<p class="text-sm text-gray-700">&gt;10 trips/year</p>
						<p class="text-sm text-gray-700">No missed reservations</p>
						<p class="text-sm text-gray-700">No unreturned bikes</p>
					</div>

					<!-- Silver -->
					<div
						class={`rounded-xl p-6 text-center shadow-sm ${
							currentTier === 'SILVER' ? 'ring-4 ring-gray-300' : 'bg-gray-100'
						}`}
					>
						<h4 class="mb-1 text-xl font-bold text-gray-700">SILVER</h4>
						<p class="text-sm text-gray-700">10% discount</p>
						<p class="text-sm text-gray-700">Bronze + 5 trips/month</p>
						<p class="text-sm text-gray-700">5+ successful reservations</p>
					</div>

					<!-- Gold -->
					<div
						class={`mx-auto max-w-xs rounded-xl p-6 text-center shadow-sm md:col-span-3 ${
							currentTier === 'GOLD' ? 'ring-4 ring-yellow-300' : 'bg-yellow-50'
						}`}
					>
						<h4 class="mb-1 text-xl font-bold text-yellow-600">GOLD</h4>
						<p class="text-sm text-gray-700">15% discount</p>
						<p class="text-sm text-gray-700">Silver + 5 trips/week</p>
						<p class="text-sm text-gray-700">(60 trips in 12 weeks)</p>
					</div>
				</div>
			</section>

			<!-- Progress Bar -->
			{#if nextTier}
				<section>
					<h3 class="mb-3 text-center text-2xl font-bold text-gray-800">
						Progress Toward {nextTier} Tier
					</h3>

					{#if requirementMet}
						<div class="rounded-lg border-2 border-emerald-200 bg-emerald-50 p-6 text-center">
							<p class="mb-2 text-lg font-bold text-emerald-700">✓ Requirement Met!</p>
							<p class="text-sm text-emerald-600">
								You've completed the required trips for {nextTier} tier. Your tier will be updated automatically
								on your next trip.
							</p>
						</div>
					{:else}
						<div class="h-4 w-full overflow-hidden rounded-full bg-gray-200 shadow-inner">
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

						<p class="mt-2 text-center text-sm font-medium text-gray-600">
							{Math.floor(progress)}% complete
						</p>
					{/if}
				</section>
			{/if}
		</div>
	</div>
</div>
