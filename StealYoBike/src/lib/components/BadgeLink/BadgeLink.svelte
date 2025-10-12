<script lang="ts">
	let {
		href = '/',
		text = '',
		variant = 'emerald',
		showUnderline = false,
		className = ''
	} = $props();

	type Variant = 'teal' | 'green';

	const variants: Record<Variant, { border: string; bg: string; text: string; shadow: string }> = {
		teal: {
			bg: 'bg-teal-200',
			text: 'text-teal-500'
		},
		green: {
			bg: 'bg-green-200',
			text: 'text-teal-500'
		},
		emerald: {
			bg: 'bg-emerald-200',
			text: 'text-emerald-600'
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
