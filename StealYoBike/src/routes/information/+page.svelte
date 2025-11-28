<script lang="ts">
	import BurgerMenu from '$lib/components/BurgerMenu/BurgerMenu.svelte';
	import PricingDetails from '$lib/components/PricingDetails/pricingDetails.svelte';
</script>

<BurgerMenu />

<div class="min-h-screen bg-gradient-to-br from-emerald-50 to-teal-100 p-6">
	<div class="mx-auto max-w-6xl">
		<!-- Header -->
		<div class="mb-12 hidden text-center md:flex md:flex-col">
			<div class="relative mb-4">
				<a
					class="text-wrap text-8xl font-black before:absolute before:-inset-1 before:mr-[8px] before:text-emerald-500 before:transition-all before:duration-300 before:content-['Pricing_Information'] hover:before:inset-x-1 hover:before:-mr-[8px]"
					href="/"
				>
					Pricing Information
				</a>
			</div>
			<p class="text-xl text-emerald-600">
				Simple, transparent pricing for your bike sharing needs
			</p>
		</div>

		<!-- How It Works Section -->
		<div class=" mt-15 mb-16 rounded-2xl bg-white p-8 shadow-lg md:mt-0">
			<h2 class="mb-6 text-3xl font-bold text-emerald-700">How It Works</h2>
			<div class="grid gap-6 md:grid-cols-3">
				<div class="text-center">
					<div
						class="mb-4 inline-flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100"
					>
						<span class="text-2xl">🔒</span>
					</div>
					<h3 class="mb-2 text-lg font-semibold text-gray-800">Reserve for Free</h3>
					<p class="text-gray-600">Find and reserve any bike in our network at no cost</p>
				</div>
				<div class="text-center">
					<div
						class="mb-4 inline-flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100"
					>
						<span class="text-2xl">🚴</span>
					</div>
					<h3 class="mb-2 text-lg font-semibold text-gray-800">Start Your Trip</h3>
					<p class="text-gray-600">Unlock the bike and the base fee is applied immediately</p>
				</div>
				<div class="text-center">
					<div
						class="mb-4 inline-flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100"
					>
						<span class="text-2xl">💰</span>
					</div>
					<h3 class="mb-2 text-lg font-semibold text-gray-800">Pay Per Minute</h3>
					<p class="text-gray-600">Simple per-minute billing while you ride</p>
				</div>
			</div>
		</div>

		<div class="mb-16 rounded-2xl bg-white p-8 shadow-xl md:mt-0">
			<div class="mb-8 flex items-center justify-center gap-3">
				<div class="rounded-full bg-emerald-100 p-3">
					<span class="text-4xl">💵</span>
				</div>
				<div class="text-center">
					<h3 class="text-4xl font-black text-emerald-700 drop-shadow-md">Flex Dollars</h3>
					<p class="text-lg text-emerald-600">Our way of thanking you!</p>
				</div>
				<div class="rounded-full bg-emerald-100 p-3">
					<span class="bg-emerald-100 text-4xl">💵</span>
				</div>
			</div>

			<div class="mb-8 rounded-xl bg-emerald-50 p-6 backdrop-blur-sm">
				<p class="text-center text-lg text-gray-700">
					Earn Flex Dollars with every ride! Use them to get discounts on future trips or apply them
					towards membership plans. <span class="font-bold text-emerald-700"
						>The more you ride, the more you save!</span
					>
				</p>
			</div>

			<div class="mb-6 flex items-center justify-center gap-3">
				<div class="rounded-full bg-emerald-100 p-3 backdrop-blur-sm">
					<span class="text-3xl">💪</span>
				</div>
				<h3 class="text-3xl font-bold text-emerald-700 drop-shadow-md">How to Flex?</h3>
				<div class="rounded-full bg-emerald-100 p-3 backdrop-blur-sm">
					<span class="inline-block scale-x-[-1] text-3xl">💪</span>
				</div>
			</div>

			<div class="rounded-xl bg-emerald-50 p-6 shadow-lg">
				<div class="flex items-start gap-4">
					<div class="flex-shrink-0">
						<div class="rounded-full bg-white p-3">
							<span class="text-3xl">🎯</span>
						</div>
					</div>
					<div>
						<h4 class="mb-2 text-xl font-bold text-emerald-700">Easy Steps to Earn</h4>
						<p class="leading-relaxed text-gray-700">
							Return your bike at a station that has <span class="font-bold text-emerald-600"
								>less than 25% available docks</span
							>
							and earn Flex Dollars based on your
							<span class="font-bold text-emerald-600">trip duration</span>. It's our way of
							encouraging balanced bike distribution across the city!
						</p>
					</div>
				</div>

				<div class="mt-6 grid gap-4 md:grid-cols-3">
					<div class="rounded-lg bg-white p-4 text-center">
						<div class="mb-2 text-2xl">⚡</div>
						<div class="text-sm font-semibold text-emerald-700">Instant Rewards</div>
						<div class="text-xs text-gray-600">Credits added immediately</div>
					</div>
					<div class="rounded-lg bg-white p-4 text-center">
						<div class="mb-2 text-2xl">🎁</div>
						<div class="text-sm font-semibold text-emerald-700">No Expiration</div>
						<div class="text-xs text-gray-600">Use them anytime</div>
					</div>
					<div class="rounded-lg bg-white p-4 text-center">
						<div class="mb-2 text-2xl">📈</div>
						<div class="text-sm font-semibold text-emerald-700">Stack Up</div>
						<div class="text-xs text-gray-600">Unlimited earning potential</div>
					</div>
				</div>
			</div>
		</div>

		<PricingDetails />
	</div>
</div>
