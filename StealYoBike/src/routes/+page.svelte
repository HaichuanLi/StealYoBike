<script lang="ts">
	import Sidebar from '$lib/components/sidebar/sidebar.svelte';
	let showSidebar = $state(false);
</script>

<button class="absolute top-5 left-5" onclick={() => (showSidebar = true)} aria-label="Toggle Menu">
	<svg
		xmlns="http://www.w3.org/2000/svg"
		class="h-16 w-16 stroke-[1.5] transition-all duration-300 hover:stroke-2 active:stroke-1"
		viewBox="0 0 24 24"
		fill="none"
	>
		<path d="M20 7L4 7" stroke="#1C274C" stroke-linecap="round" />
		<path d="M20 12L4 12" stroke="#1C274C" stroke-linecap="round" />
		<path d="M20 17L4 17" stroke="#1C274C" stroke-linecap="round" />
	</svg>
</button>
{#if showSidebar}
	<Sidebar bind:showSidebar />
{/if}

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
		<a href="/login" class="relative"
			><div
				class="rounded border-2 border-teal-600 bg-teal-400 px-4 py-2 font-bold text-gray-700 shadow-teal-500 transition-all duration-200 hover:-translate-x-[5px] hover:-translate-y-[5px] hover:shadow-[5px_5px_0px_0px] active:translate-x-0 active:translate-y-0 active:shadow-[0px_0px_0px_0px]"
			>
				Login
			</div>
		</a>
		<a href="/register" class="relative"
			><div
				class="rounded border-2 border-green-600 bg-green-400 px-4 py-2 font-bold text-gray-600 shadow-green-500 transition-all duration-200 hover:-translate-x-[5px] hover:-translate-y-[5px] hover:shadow-[5px_5px_0px_0px] active:translate-x-0 active:translate-y-0 active:shadow-[0px_0px_0px_0px]"
			>
				Register
			</div>
		</a>
	</div>
</div>
