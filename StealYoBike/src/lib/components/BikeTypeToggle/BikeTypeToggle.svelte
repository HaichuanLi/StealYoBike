<script lang="ts">
	interface Props {
		isElectric: boolean;
	}

	let { isElectric = $bindable(false) }: Props = $props();
</script>

<div class="flex items-center py-3">
	<div class="relative m-auto h-[32px] w-[163px]">
		<input
			id="bikeTypeToggle"
			type="checkbox"
			class="peer absolute inset-0 h-full w-full cursor-pointer opacity-0"
			bind:checked={isElectric}
		/>
		<label
			for="bikeTypeToggle"
			class="block h-full w-full rounded-[20px] bg-[#c0e7e3] shadow-inner transition-colors duration-200 peer-checked:bg-cyan-300"
		></label>
		<span
			class="absolute left-[3px] top-[2px] z-10 flex h-[28px] w-[100px] items-center justify-center rounded-full bg-[#21bf73] text-[20px] font-bold text-white shadow-md transition-transform duration-200 peer-checked:translate-x-[57px] peer-checked:bg-sky-400"
			aria-hidden="true"
		>
			{isElectric ? 'Electric' : 'Regular'}
		</span>
	</div>
</div>
