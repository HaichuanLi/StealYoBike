<script lang="ts">
	import { authApi } from '$lib/api/auth.api';
	import { riderApi } from '$lib/api/rider.api';
	import type { StationSummary } from '$lib/api/types';
	import type { UserInfoResponse } from '$lib/api/types/auth.types';
	import type { ReserveBikeResponse, TripInfoResponse, TripBillResponse } from '$lib/api/types/rider.types';
	import DashboardBody from '$lib/components/DashboardBody/DashboardBody.svelte';
	import DashboardHeader from '$lib/components/DashboardHeader/DashboardHeader.svelte';
	import Map from '$lib/components/Map/Map.svelte';
	import PaymentPopUp from '$lib/components/PaymentPopUp/PaymentPopUp.svelte';
	import PinPopup from '$lib/components/PinPopup/PinPopup.svelte';
	import ReservationCard from '$lib/components/ReservationCard/ReservationCard.svelte';
	import StationCard from '$lib/components/StationCard/StationCard.svelte';
	import TripCard from '$lib/components/TripCard/TripCard.svelte';
	import BillModal from '$lib/components/BillModal/BillModal.svelte';
	import { onMount } from 'svelte';

	let selectedStation = $state<StationSummary | null>(null);
	let user = $state<UserInfoResponse | null>(null);
	let loading = $state(true);
	let reservation = $state<ReserveBikeResponse | null>(null);
	let currentTrip = $state<TripInfoResponse | null>(null);

	let showPaymentPopup = $state(false);
	let paymentTokenInput = $state('');
	let savingPayment = $state(false);

	let showPinPopup = $state(false);
	let pinInput = $state('');
	let submittingPin = $state(false);

	let isElectric = $state(false);

	let tripBill = $state<TripBillResponse | null>(null);
	let showBillModal = $state(false);

	let hasPaymentMethod = $derived(
		user !== null && user.paymentToken !== null && user.paymentToken.trim() !== ''
	);
	let isReservationExpired = $derived(
		reservation !== null && new Date(reservation.expiresAt) < new Date()
	);
	let hasActiveReservationOrTrip = $derived(reservation !== null || currentTrip !== null);

	onMount(async () => {
		try {
			const response = await authApi.getCurrentUser();
			user = response.data;

			// Try to get current reservation
			try {
				const reservationResponse = await riderApi.getCurrentReservation();
				reservation = reservationResponse.data;
			} catch (error) {
				// No active reservation
				reservation = null;
			}

			// Try to get current trip
			try {
				const tripResponse = await riderApi.getCurrentTrip();
				currentTrip = tripResponse.data;
			} catch (error) {
				// No active trip
				currentTrip = null;
			}
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

	async function handleReturnBike() {
		if (!hasPaymentMethod) {
			showPaymentPopup = true;
			return;
		}
			const response = await riderApi.returnBike({
				tripId: currentTrip?.tripId!,
				stationId: selectedStation?.stationId!
			});
			if (response.status === 200) {
				console.log('Return bike response:', response);
				// attempt to fetch the computed bill for this trip and show it
				try {
					const billResp = await riderApi.getTripBill(response.data.tripId);
					tripBill = billResp.data;
					showBillModal = true;
				} catch (err) {
					console.warn('Failed to fetch trip bill:', err);
				}
				currentTrip = null;
			}
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
			currentTrip = response.data;
			closePinPopup();
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
	<div class="flex min-h-fit flex-col gap-4">
		<StationCard
			station={selectedStation}
			{hasPaymentMethod}
			{hasActiveReservationOrTrip}
			{currentTrip}
			bind:isElectric
			onReserveBike={handleReserveBike}
			onReturnBike={handleReturnBike}
			onAddPayment={handleAddPayment}
		/>
		{#if currentTrip}
			<TripCard trip={currentTrip} {loading} {selectedStation} />
		{:else}
			<ReservationCard
				{reservation}
				{loading}
				{hasPaymentMethod}
				onCancel={handleCancelReservation}
				onCheckout={handleCheckoutBike}
			/>
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

<BillModal bind:show={showBillModal} bind:bill={tripBill} />
