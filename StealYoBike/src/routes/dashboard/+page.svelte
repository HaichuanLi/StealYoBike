<script lang="ts">
	import { authApi } from '$lib/api/auth.api';
	import { riderApi } from '$lib/api/rider.api';
	import type { StationSummary } from '$lib/api/types';
	import type { UserInfoResponse } from '$lib/api/types/auth.types';
	import type { ReserveBikeResponse } from '$lib/api/types/rider.types';
	import Button from '$lib/components/Button/Button.svelte';
	import DashboardBody from '$lib/components/DashboardBody/DashboardBody.svelte';
	import DashboardHeader from '$lib/components/DashboardHeader/DashboardHeader.svelte';
	import Map from '$lib/components/Map/Map.svelte';
	import PaymentPopUp from '$lib/components/PaymentPopUp/PaymentPopUp.svelte';
	import PinPopup from '$lib/components/PinPopup/PinPopup.svelte';
	import { onMount } from 'svelte';

	let selectedStation = $state<StationSummary | null>(null);
	let user = $state<UserInfoResponse | null>(null);
	let loading = $state(true);

	let showPaymentPopup = $state(false);
	let paymentTokenInput = $state('');
	let savingPayment = $state(false);
	let isElectric = $state(false);

	let showPinPopup = $state(false);
	let pinInput = $state('');
	let submittingPin = $state(false);

	let hasPaymentMethod = $derived(
		user !== null && user.paymentToken !== null && user.paymentToken.trim() !== ''
	);
	let reservation = $state<ReserveBikeResponse | null>(null);
	let isReservationExpired = $derived(
		reservation !== null && new Date(reservation.expiresAt) < new Date()
	);

	onMount(async () => {
		try {
			const response = await authApi.getCurrentUser();
			user = response.data;
			const reservationResponse = await riderApi.getCurrentReservation();
			reservation = reservationResponse.data;
		} catch (error) {
			console.error('Failed to load user:', error);
			user = null;
		} finally {
			loading = false;
		}
	});

	async function handleReserveBike() {
		if (!hasPaymentMethod) {
			showPaymentPopup = true;
			return;
		}
		const response = await riderApi.reserveBike({
			stationId: selectedStation?.stationId!,
			bikeType: isElectric ? 'ELECTRIC' : 'REGULAR'
		});
		reservation = response.data;
	}

	function handleReturnBike() {
		if (!hasPaymentMethod) {
			showPaymentPopup = true;
			return;
		}
		console.log('Returning bike...');
		// cook return logic
	}

	async function handleCancelReservation() {
		try {
			const response = await riderApi.cancelCurrentReservation();
			reservation = null;
		} catch (error) {
			console.error('Failed to cancel reservation:', error);
		}
	}

	async function handleCheckoutBike() {
		if (!hasPaymentMethod) {
			showPaymentPopup = true;
			return;
		}
		if (isReservationExpired) {
			alert('Your reservation has expired. Please cancel it and make a new reservation.');
			return;
		}
		showPinPopup = true;
	}

	async function handlePinSubmit(pin: string) {
		try {
			const response = await riderApi.checkoutBike({
				reservationId: reservation!.reservationId,
				pin: pin
			});
			reservation = null;
			closePinPopup();
			alert('Bike checked out successfully!');
		} catch (error) {
			console.error('Failed to checkout bike:', error);
			throw error;
		}
	}

	function closePaymentPopup() {
		showPaymentPopup = false;
		paymentTokenInput = '';
		savingPayment = false;
	}

	function closePinPopup() {
		showPinPopup = false;
		pinInput = '';
		submittingPin = false;
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
	<div class="flex flex-col gap-4">
		<div class="grow rounded-xl bg-lime-50">
			{#if selectedStation}
				<div
					class="h-[15%] min-h-fit w-full content-center rounded-t-xl {isElectric
						? 'bg-cyan-300'
						: 'bg-lime-300'} p-4 text-center"
				>
					<h2 class="text-lg font-semibold">
						{selectedStation.name}, ID: {selectedStation.stationId}
					</h2>
					<p class="text-sm">{selectedStation.streetAddress}</p>
				</div>
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
						<p class="mb-2 text-sm text-orange-600">Payment method required to use bikes</p>
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
		{#if reservation}
			<div class="mt-4 rounded-xl bg-lime-50 p-4">
				<h3 class="mb-2 text-lg font-semibold">Reservation Details</h3>
				{#if isReservationExpired}
					<div class="mb-3 rounded bg-red-100 p-3 text-center">
						<p class="font-semibold text-red-700">This reservation has expired!</p>
						<p class="text-sm text-red-600">Please cancel it and make a new reservation.</p>
					</div>
				{/if}
				<p>Reservation ID: {reservation.reservationId}</p>
				<p>Bike ID: {reservation.bikeId}</p>
				<p>Station ID: {reservation.stationId}</p>
				<p class={isReservationExpired ? 'font-semibold text-red-600' : ''}>
					Expires At: {new Date(reservation.expiresAt).toLocaleString()}
				</p>
				<p>PIN: {reservation.pin}</p>
				<div class="my-2 border-t border-lime-200"></div>
				<div class="flex flex-row justify-center gap-2">
					<Button
						onclick={handleCancelReservation}
						text="Cancel Reservation"
						disable={!hasPaymentMethod}
						variant="teal"
					/>
					<Button
						onclick={handleCheckoutBike}
						text="Checkout Bike"
						disable={!hasPaymentMethod || isReservationExpired}
						variant="green"
					/>
				</div>
			</div>
		{:else if !loading}
			<div class="mt-4 rounded-xl bg-lime-50 p-4 text-center">
				<p>No active reservations.</p>
			</div>
		{/if}
	</div>
</DashboardBody>

<PaymentPopUp
	bind:showPaymentPopup
	bind:paymentTokenInput
	bind:savingPayment
	{closePaymentPopup}
	onUserUpdate={handleUserUpdate}
/>

<PinPopup
	bind:showPinPopup
	bind:pinInput
	bind:submittingPin
	{closePinPopup}
	onPinSubmit={handlePinSubmit}
/>
