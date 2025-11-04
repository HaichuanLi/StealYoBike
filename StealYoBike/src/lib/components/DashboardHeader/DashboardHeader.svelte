<script lang="ts">
	import BurgerMenu from '../BurgerMenu/BurgerMenu.svelte';
</script>

<div class="h-25 relative top-0 w-full content-center justify-items-center bg-emerald-200">
	<BurgerMenu />
	<div class="relative w-fit">
		<a
			class="text-nowrap text-2xl font-black before:absolute before:-inset-1 before:text-emerald-500 before:transition-[inset] before:duration-300 before:content-['Steal_Your_Bike_Today!'] hover:before:inset-x-1 md:text-4xl lg:text-6xl"
			href="/"
		>
			Steal Your Bike Today!
		</a>
	</div>
</div>
