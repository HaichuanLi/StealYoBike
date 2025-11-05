<script lang="ts">
	import BurgerMenu from '$lib/components/BurgerMenu/BurgerMenu.svelte';
	import ButtonLink from '$lib/components/ButtonLink/ButtonLink.svelte';
	import { fade } from 'svelte/transition';

	// tooltip visibility state
	// initialize as a plain boolean — accidental `$state` slipped in earlier
	let showTooltip: boolean = false;
</script>

<BurgerMenu />

<div class="flex size-full flex-col items-center justify-center">
	<div class="relative mb-4">
		<a
			class="text-wrap text-8xl font-black before:absolute before:-inset-1 before:mr-[8px] before:text-emerald-500 before:transition-all before:duration-300 before:content-['Steal_Your_Bike_Today!'] hover:before:inset-x-1 hover:before:-mr-[8px]"
			href="/information"
			onmouseenter={() => (showTooltip = true)}
			onmouseleave={() => (showTooltip = false)}
			onfocus={() => (showTooltip = true)}
			onblur={() => (showTooltip = false)}
			aria-describedby={showTooltip ? 'pricing-tooltip' : undefined}
		>
			Steal Your Bike Today!
		</a>
		{#if showTooltip}
			<div
				id="pricing-tooltip"
				role="tooltip"
				aria-hidden={!showTooltip}
				class="rounded-4xl absolute bottom-full left-1/2 z-10 mb-2 -translate-x-1/2 animate-bounce border-2 border-black bg-green-200 px-4 py-2 text-sm font-semibold text-black shadow-lg"
				in:fade={{ duration: 120 }}
			>
				<div class="whitespace-nowrap">Click to view our BEST offers!</div>
				<!-- comic-style arrow pointing down (matches bubble color, with black stroke) -->
				<svg
					class="absolute -bottom-2 left-1/2 -translate-x-1/2"
					width="22"
					height="12"
					viewBox="0 0 22 12"
					fill="none"
					xmlns="http://www.w3.org/2000/svg"
					aria-hidden="true"
				>
					<path d="M1 1 L11 11 L21 1 Z" fill="#bbf7d0" stroke="#000" stroke-width="1.6" />
				</svg>
			</div>
		{/if}
	</div>
	<p class="text-lg text-gray-700">Your one-stop solution for bike sharing.</p>
	<div class="mt-6 flex flex-row items-center gap-6">
		<ButtonLink href="/login" variant="teal" text="Login" />
		<ButtonLink href="/register" variant="green" text="Register" />
	</div>
</div>
