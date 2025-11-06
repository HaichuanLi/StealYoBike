<script lang="ts">
	import { operatorApi } from '$lib/api/operator.api';
	import Button from '$lib/components/Button/Button.svelte';

	interface Props {
		onRestoreComplete?: () => void;
	}

	let { onRestoreComplete }: Props = $props();

	let isLoading = $state(false);
	let successMessage = $state<string | null>(null);
	let errorMessage = $state<string | null>(null);

	async function handleRestore() {
		isLoading = true;
		successMessage = null;
		errorMessage = null;

		try {
			const response = await operatorApi.restoreInitialState({});
			successMessage = `Successfully restored: ${response.data.stations} stations, ${response.data.docks} docks, ${response.data.bikes} bikes. ${response.data.message}`;

			if (onRestoreComplete) {
				onRestoreComplete();
			}

			// Clear success message after 5 seconds
			setTimeout(() => {
				successMessage = null;
			}, 5000);
		} catch (error: any) {
			errorMessage = error.response?.data?.message || 'Failed to restore initial state';
			console.error('Error restoring initial state:', error);

			// Clear error message after 5 seconds
			setTimeout(() => {
				errorMessage = null;
			}, 5000);
		} finally {
			isLoading = false;
		}
	}
</script>

<div class="space-y-4">
	<div class="rounded-lg bg-white p-6 shadow-sm">
		<h3 class="mb-4 text-lg font-semibold text-gray-800">Restore Initial State</h3>

		<p class="mb-4 text-sm text-gray-600">
			This action will reset all stations, docks, and bikes to their initial state. All ongoing
			reservations and trips will be cleared.
		</p>

		<div class="mb-4">
			<Button
				text={isLoading ? 'Restoring...' : 'Restore Initial State'}
				variant="red"
				onclick={handleRestore}
				disable={isLoading}
			/>
		</div>

		{#if successMessage}
			<div class="rounded-md bg-green-50 p-4 text-sm text-green-800">
				<p class="font-medium">Success!</p>
				<p>{successMessage}</p>
			</div>
		{/if}

		{#if errorMessage}
			<div class="rounded-md bg-red-50 p-4 text-sm text-red-800">
				<p class="font-medium">Error</p>
				<p>{errorMessage}</p>
			</div>
		{/if}
	</div>
</div>
