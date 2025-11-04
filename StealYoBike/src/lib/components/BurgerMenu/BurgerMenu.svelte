<script lang="ts">
	import Sidebar from '$lib/components/Sidebar/Sidebar.svelte';
	let showSidebar = $state(false);
</script>

<button class="absolute left-5 top-5" onclick={() => (showSidebar = true)} aria-label="Toggle Menu">
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
