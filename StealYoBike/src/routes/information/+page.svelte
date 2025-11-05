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

		<PricingDetails />
	</div>
</div>
