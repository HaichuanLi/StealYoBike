<script lang="ts">
	import Map from '$lib/components/Map/Map.svelte';
</script>

<div class="relative top-0 h-20 w-full bg-red-300"></div>
<div class="relative flex h-[calc(100%-5rem)] w-full bg-blue-300">
	<div class="h-[50%] w-[50%] overflow-hidden rounded-2xl *:size-full">
		<Map />
	</div>
</div>
<div class="h-40 w-full"></div>
