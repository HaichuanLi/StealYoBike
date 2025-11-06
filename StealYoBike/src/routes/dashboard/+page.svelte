<script lang="ts">
	import { authApi } from '$lib/api/auth.api';
	import type { StationSummary } from '$lib/api/types';
	import type { UserInfoResponse } from '$lib/api/types/auth.types';
	import Button from '$lib/components/Button/Button.svelte';
	import DashboardBody from '$lib/components/DashboardBody/DashboardBody.svelte';
	import DashboardHeader from '$lib/components/DashboardHeader/DashboardHeader.svelte';
	import Map from '$lib/components/Map/Map.svelte';
	import PaymentPopUp from '$lib/components/PaymentPopUp/PaymentPopUp.svelte';
	import { onMount } from 'svelte';

	let selectedStation = $state<StationSummary | null>(null);
	let user = $state<UserInfoResponse | null>(null);
	let loading = $state(true);

	let showPaymentPopup = $state(false);
	let paymentTokenInput = $state('');
	let savingPayment = $state(false);
	// bike type toggle: false = O (default), true = I
	let isElectric = $state(false);

	let hasPaymentMethod = $derived(user?.paymentToken != null && user?.paymentToken !== '');

	onMount(async () => {
		try {
			const response = await authApi.getCurrentUser();
			user = response.data;
		} catch (error) {
			console.error('Failed to load user:', error);
			user = null;
		} finally {
			loading = false;
		}
	});

	function handleReserveBike() {
		if (!hasPaymentMethod) {
			showPaymentPopup = true;
			return;
		}
		console.log('Reserving bike...');
		// cook up reserve logic
	}

	function handleReturnBike() {
		if (!hasPaymentMethod) {
			showPaymentPopup = true;
			return;
		}
		console.log('Returning bike...');
		// cook return logic
	}

	function closePaymentPopup() {
		showPaymentPopup = false;
		paymentTokenInput = '';
		savingPayment = false;
	}

	function handleUserUpdate(updatedUser: UserInfoResponse) {
		user = updatedUser;
	}
</script>

<DashboardHeader />
<DashboardBody>
	<div>
		<Map bind:selectedStation />
	</div>
	<div>
		<div class="size-full rounded-xl bg-lime-50">
			{#if selectedStation}
				<div
					class="h-[15%] w-full content-center rounded-t-xl {isElectric
						? 'bg-cyan-300'
						: 'bg-lime-300'} p-4 text-center"
				>
					<h2 class="text-lg font-semibold">{selectedStation.name}</h2>
					<p class="text-sm">{selectedStation.streetAddress}</p>
				</div>
				<!-- switch toggle for bike type -->
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
				<div
					class="flex h-full flex-col items-center justify-center gap-2 sm:*:max-w-40 lg:flex-row"
				>
					<Button
						onclick={handleReserveBike}
						text="Reserve Bike"
						disable={!hasPaymentMethod}
						variant="green"
					/>
					<Button
						onclick={handleReturnBike}
						text="Return Bike"
						disable={!hasPaymentMethod}
						variant="teal"
					/>
				</div>
				{#if !hasPaymentMethod}
					<div class="mt-4 p-4 text-center">
						<p class="mb-2 text-sm text-orange-600">⚠️ Payment method required to use bikes</p>
						<Button
							onclick={() => (showPaymentPopup = true)}
							text="Add Payment Method"
							variant="red"
						/>
					</div>
				{/if}
			{:else}
				<div class="h-full content-center text-center">Select a Station to get Started!</div>
			{/if}
		</div>
	</div>
</DashboardBody>

<PaymentPopUp
	bind:showPaymentPopup
	bind:paymentTokenInput
	bind:savingPayment
	{closePaymentPopup}
	onUserUpdate={handleUserUpdate}
/>
