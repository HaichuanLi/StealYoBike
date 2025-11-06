<script lang="ts">
	import { authApi } from '$lib/api/auth.api';
	import { riderApi } from '$lib/api/rider.api';
	import type { StationSummary } from '$lib/api/types';
	import type { UserInfoResponse } from '$lib/api/types/auth.types';
	import type { ReserveBikeResponse } from '$lib/api/types/rider.types';
	import DashboardBody from '$lib/components/DashboardBody/DashboardBody.svelte';
	import DashboardHeader from '$lib/components/DashboardHeader/DashboardHeader.svelte';
	import Map from '$lib/components/Map/Map.svelte';
	import PaymentPopUp from '$lib/components/PaymentPopUp/PaymentPopUp.svelte';
	import PinPopup from '$lib/components/PinPopup/PinPopup.svelte';
	import ReservationCard from '$lib/components/ReservationCard/ReservationCard.svelte';
	import StationCard from '$lib/components/StationCard/StationCard.svelte';
	import { onMount } from 'svelte';

	let selectedStation = $state<StationSummary | null>(null);
	let user = $state<UserInfoResponse | null>(null);
	let loading = $state(true);
	let reservation = $state<ReserveBikeResponse | null>(null);

	let showPaymentPopup = $state(false);
	let paymentTokenInput = $state('');
	let savingPayment = $state(false);

	let showPinPopup = $state(false);
	let pinInput = $state('');
	let submittingPin = $state(false);

	let isElectric = $state(false);

	let hasPaymentMethod = $derived(
		user !== null && user.paymentToken !== null && user.paymentToken.trim() !== ''
	);
	let isReservationExpired = $derived(
		reservation !== null && new Date(reservation.expiresAt) < new Date()
	);
	// TODO: Add currentTrip state when trip functionality is implemented
	let hasActiveReservationOrTrip = $derived(reservation !== null);

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

	function handleAddPayment() {
		showPaymentPopup = true;
	}
</script>

<DashboardHeader />
<DashboardBody>
	<div>
		<Map bind:selectedStation />
	</div>
	<div class="flex flex-col gap-4">
		<StationCard
			station={selectedStation}
			{hasPaymentMethod}
			{hasActiveReservationOrTrip}
			bind:isElectric
			onReserveBike={handleReserveBike}
			onReturnBike={handleReturnBike}
			onAddPayment={handleAddPayment}
		/>
		<ReservationCard
			{reservation}
			{loading}
			{hasPaymentMethod}
			onCancel={handleCancelReservation}
			onCheckout={handleCheckoutBike}
		/>
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
