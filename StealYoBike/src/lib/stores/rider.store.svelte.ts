import { authApi } from '$lib/api/auth.api';
import { riderApi } from '$lib/api/rider.api';
import type { StationSummary } from '$lib/api/types';
import type { UserInfoResponse } from '$lib/api/types/auth.types';
import type {
	ReserveBikeResponse,
	TripBillResponse,
	TripInfoResponse
} from '$lib/api/types/rider.types';
import { showToast } from '$lib/stores/toast';

class RiderStore {
	user = $state<UserInfoResponse | null>(null);
	selectedStation = $state<StationSummary | null>(null);
	reservation = $state<ReserveBikeResponse | null>(null);
	currentTrip = $state<TripInfoResponse | null>(null);
	isElectric = $state(false);

	loading = $state(true);
	reserving = $state(false);
	returning = $state(false);

	paymentModalShow = $state(false);
	paymentModalToken = $state('');
	paymentModalSaving = $state(false);

	pinModalShow = $state(false);
	pinModalPin = $state('');
	pinModalSubmitting = $state(false);

	billModalShow = $state(false);
	billModalData = $state<TripBillResponse | null>(null);

	get hasPaymentMethod(): boolean {
		return (
			this.user !== null && this.user.paymentToken !== null && this.user.paymentToken.trim() !== ''
		);
	}

	get isReservationExpired(): boolean {
		return this.reservation !== null && new Date(this.reservation.expiresAt) < new Date();
	}

	get hasActiveReservationOrTrip(): boolean {
		return this.reservation !== null || this.currentTrip !== null || this.returning;
	}

	async initialize() {
		this.loading = true;
		try {
			const response = await authApi.getCurrentUser();
			this.user = response.data;

			const [reservationResult, tripResult] = await Promise.allSettled([
				riderApi.getCurrentReservation(),
				riderApi.getCurrentTrip()
			]);

			this.reservation =
				reservationResult.status === 'fulfilled' ? reservationResult.value.data : null;
			this.currentTrip = tripResult.status === 'fulfilled' ? tripResult.value.data : null;
		} catch (error) {
			console.error('Failed to load user:', error);
			this.user = null;
		} finally {
			this.loading = false;
		}
	}

	async reserveBike() {
		if (!this.hasPaymentMethod) {
			this.showPaymentModal();
			return;
		}

		if (this.reserving || !this.selectedStation) {
			return;
		}

		this.reserving = true;
		try {
			const response = await riderApi.reserveBike({
				stationId: this.selectedStation.stationId,
				bikeType: this.isElectric ? 'ELECTRIC' : 'REGULAR'
			});

			if (!response || !response.data) {
				throw new Error('Invalid response from server');
			}

			this.closePaymentModal();
			this.currentTrip = null;
			this.closeBillModal();
			this.reservation = response.data;

			showToast('Bike reserved successfully', 'success');
		} catch (error) {
			console.error('Failed to reserve bike:', error);
			showToast('Failed to reserve bike', 'error');
		} finally {
			this.reserving = false;
		}
	}

	async checkoutBike() {
		if (!this.hasPaymentMethod) {
			this.showPaymentModal();
			return;
		}

		if (this.isReservationExpired) {
			showToast(
				'Your reservation has expired. Please cancel it and make a new reservation.',
				'error'
			);
			return;
		}

		this.closePaymentModal();
		this.closeBillModal();
		this.showPinModal();
	}

	async submitPin(pin: string) {
		if (!this.reservation) {
			return;
		}

		try {
			const response = await riderApi.checkoutBike({
				reservationId: this.reservation.reservationId,
				pin: pin
			});

			this.reservation = null;
			this.currentTrip = response.data;
			this.closePinModal();

			showToast('Bike checked out successfully', 'success');
		} catch (error) {
			console.error('Failed to checkout bike:', error);
			showToast('Failed to checkout bike. Please check your PIN.', 'error');
			throw error;
		}
	}

	async returnBike() {
		if (!this.hasPaymentMethod) {
			this.showPaymentModal();
			return;
		}

		if (!this.currentTrip || !this.selectedStation) {
			return;
		}

		this.returning = true;
		try {
			const response = await riderApi.returnBike({
				tripId: this.currentTrip.tripId,
				stationId: this.selectedStation.stationId
			});

			if (response.status === 200) {
				const tripId = response.data.tripId;

				try {
					const billResp = await riderApi.getTripBill(tripId);

					this.currentTrip = null;
					this.reservation = null;
					this.returning = false;
					this.closePaymentModal();
					this.closePinModal();

					this.showBillModal(billResp.data);
					showToast('Bike returned successfully', 'success');
				} catch (err) {
					console.error('Failed to fetch trip bill:', err);
					this.currentTrip = null;
					this.reservation = null;
					this.returning = false;
					this.closePaymentModal();
					this.closePinModal();
					showToast('Bike returned but failed to load bill', 'error');
				}
			}
		} catch (error) {
			console.error('Failed to return bike:', error);
			showToast('Failed to return bike', 'error');
			this.returning = false;
		}
	}

	async cancelReservation() {
		try {
			await riderApi.cancelCurrentReservation();
			this.reservation = null;
			this.closePinModal();
			showToast('Reservation cancelled', 'info');
		} catch (error) {
			console.error('Failed to cancel reservation:', error);
			showToast('Failed to cancel reservation', 'error');
		}
	}

	async savePaymentToken(token: string) {
		if (!token.trim()) {
			showToast('Please enter a payment token', 'error');
			return;
		}

		this.paymentModalSaving = true;

		try {
			if (!authApi.isAuthenticated()) {
				showToast('You must be signed in to save a payment method. Please sign in first.', 'error');
				this.closePaymentModal();
				return;
			}

			const response = await authApi.updatePaymentToken(token.trim());
			this.user = response.data;

			showToast('Payment method saved successfully', 'success');
			this.closePaymentModal();
		} catch (error) {
			console.error('Failed to save payment token:', error);
			showToast('Failed to save payment token. Please try again.', 'error');
		} finally {
			this.paymentModalSaving = false;
		}
	}

	showPaymentModal() {
		this.paymentModalShow = true;
		this.paymentModalToken = '';
		this.paymentModalSaving = false;
	}

	closePaymentModal() {
		this.paymentModalShow = false;
		this.paymentModalToken = '';
		this.paymentModalSaving = false;
	}

	showPinModal() {
		this.pinModalShow = true;
		this.pinModalPin = '';
		this.pinModalSubmitting = false;
	}

	closePinModal() {
		this.pinModalShow = false;
		this.pinModalPin = '';
		this.pinModalSubmitting = false;
	}

	showBillModal(bill: TripBillResponse) {
		this.billModalShow = true;
		this.billModalData = bill;
	}

	closeBillModal() {
		this.billModalShow = false;
		this.billModalData = null;
	}

	updateBill(bill: TripBillResponse) {
		this.billModalData = bill;
	}

	setSelectedStation(station: StationSummary | null) {
		this.selectedStation = station;
	}

	toggleBikeType(isElectric: boolean) {
		this.isElectric = isElectric;
	}
}

export const riderStore = new RiderStore();
