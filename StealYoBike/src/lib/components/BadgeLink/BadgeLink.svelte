<script lang="ts">
	let {
		href = '/',
		text = '',
		variant = 'emerald',
		showUnderline = false,
		className = ''
	} = $props();

	// include emerald in the variant union and provide the same full shape as other components
	type Variant = 'teal' | 'green' | 'emerald';

	const variants: Record<Variant, { border: string; bg: string; text: string; shadow: string }> = {
		teal: {
			border: 'border-teal-600',
			bg: 'bg-teal-200',
			text: 'text-teal-500',
			shadow: 'shadow-teal-300'
		},
		green: {
			border: 'border-green-600',
			bg: 'bg-green-200',
			text: 'text-green-500',
			shadow: 'shadow-green-300'
		},
		emerald: {
			border: 'border-emerald-600',
			bg: 'bg-emerald-200',
			text: 'text-emerald-600',
			shadow: 'shadow-emerald-300'
		}
	};

	let colors = $derived(variants[variant as Variant] || variants.emerald);
	let underlineClass = showUnderline ? 'hover:underline' : '';
</script>

<a
	{href}
	class="relative inline-block before:absolute before:-inset-1 before:block before:rounded-lg before:{colors.bg} {className}"
>
	<span class="relative {colors.text} {underlineClass} dark:text-gray-950">{text}</span>
</a>
