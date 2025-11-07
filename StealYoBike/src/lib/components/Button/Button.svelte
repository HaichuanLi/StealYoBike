<script lang="ts">
	type Variant = 'teal' | 'green' | 'red' | 'blue' | 'cyan' | 'gray';

	let { onclick = null, variant = 'teal', text, disable = false } = $props();

	const variants: Record<Variant, { border: string; bg: string; text: string; shadow: string }> = {
		teal: {
			border: 'border-teal-600',
			bg: 'bg-teal-400',
			text: 'text-gray-700',
			shadow: 'shadow-teal-500'
		},
		green: {
			border: 'border-green-600',
			bg: 'bg-green-400',
			text: 'text-gray-600',
			shadow: 'shadow-green-500'
		},
		red: {
			border: 'border-red-600',
			bg: 'bg-red-400',
			text: 'text-gray-600',
			shadow: 'shadow-red-500'
		},
		blue: {
			border: 'border-blue-500',
			bg: 'bg-blue-300',
			text: 'text-gray-600',
			shadow: 'shadow-blue-400'
		},
		cyan: {
			border: 'border-cyan-500',
			bg: 'bg-cyan-300',
			text: 'text-gray-600',
			shadow: 'shadow-cyan-400'
		},
		gray: {
			border: 'border-gray-400',
			bg: 'bg-gray-200',
			text: 'text-gray-700',
			shadow: 'shadow-gray-300'
		}
	};

	let colors = $derived(variants[variant as Variant] || variants.teal);
</script>

<button class="relative" {onclick} type="button" disabled={disable}>
	<div
		class="rounded border-2 {colors.border} {colors.bg} px-4 py-2 font-bold {colors.text} {colors.shadow} transition-all duration-200 hover:-translate-x-[5px] hover:-translate-y-[5px] hover:shadow-[5px_5px_0px_0px] active:translate-x-0 active:translate-y-0 active:shadow-[0px_0px_0px_0px]"
	>
		{text}
	</div>
</button>
