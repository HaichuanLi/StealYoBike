<script lang="ts">
	let { showSidebar = $bindable() } = $props();
</script>

<button
	type="button"
	class="fixed top-0 left-0 z-40 h-full w-full bg-gray-100/50"
	aria-label="Close Sidebar Overlay"
	onclick={() => (showSidebar = false)}
></button>
<div class="fixed top-0 left-0 z-50 h-full w-80 bg-emerald-100">
	<button
		class="absolute top-5 right-5"
		onclick={() => (showSidebar = false)}
		aria-label="Close Menu"
	>
		<svg
			xmlns="http://www.w3.org/2000/svg"
			class="h-10 w-10 stroke-[1.5] transition-all duration-300 hover:stroke-2 active:stroke-1"
			viewBox="0 0 24 24"
			fill="none"
		>
			<path d="M18 6L6 18" stroke="#1C274C" stroke-linecap="round" />
			<path d="M6 6L18 18" stroke="#1C274C" stroke-linecap="round" />
		</svg>
	</button>
</div>
