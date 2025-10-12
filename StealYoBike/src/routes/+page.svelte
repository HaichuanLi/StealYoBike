<script lang="ts">
	import BurgerMenu from '$lib/components/BurgerMenu/BurgerMenu.svelte';
	import ButtonLink from '$lib/components/ButtonLink/ButtonLink.svelte';
</script>

<BurgerMenu />

<div class="flex size-full flex-col items-center justify-center">
	<div class="relative mb-4">
		<h1
			class="text-8xl font-black text-wrap before:absolute before:-inset-1 before:mr-[8px] before:text-emerald-500 before:transition-all before:duration-300 before:content-['Steal_Your_Bike_Today!'] hover:before:inset-x-1 hover:before:-mr-[8px]"
		>
			Steal Your Bike Today!
		</h1>
	</div>
	<p class="text-lg text-gray-700">Your one-stop solution for bike sharing.</p>
	<div class="mt-6 flex flex-row items-center gap-6">
		<ButtonLink href="/login" variant="teal" text="Login" />
		<ButtonLink href="/register" variant="green" text="Register" />
	</div>
</div>
