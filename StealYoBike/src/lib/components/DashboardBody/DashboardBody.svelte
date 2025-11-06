<script lang="ts">
	let { children } = $props();
</script>

<div
	class="relative flex h-[calc(100%-6.25rem)] min-h-fit w-full flex-col items-center bg-teal-800 *:size-full *:p-10 sm:flex-row"
>
	{@render children?.()}
</div>
