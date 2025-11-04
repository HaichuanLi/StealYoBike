<script lang="ts">
	let { children = null, status } = $props();
</script>

<svg width="60" height="60" viewBox="0 0 60 60" xmlns="http://www.w3.org/2000/svg">
	<g>
		<path
			d="M 15 20 L 15 45 Q 15 50 20 50 L 40 50 Q 45 50 45 45 L 45 20"
			fill="none"
			stroke={status === 'OUT_OF_SERVICE' ? '#ef4444' : '#333333'}
			stroke-width="3"
			stroke-linecap="round"
		/>
		{@render children?.()}
	</g>
</svg>
