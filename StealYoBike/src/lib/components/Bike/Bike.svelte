<script lang="ts">
	let { status } = $props();
</script>

<rect
	x="22"
	y="15"
	width="16"
	height="30"
	fill={status === 'Available' ? '#4CAF50' : status === 'Maintenance' ? '#ef4444' : '#fdba74'}
	rx="2"
	ry="2"
/>
