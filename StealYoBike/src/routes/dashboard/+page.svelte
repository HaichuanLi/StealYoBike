<script lang="ts">
	import BillModal from '$lib/components/BillModal/BillModal.svelte';
	import DashboardBody from '$lib/components/DashboardBody/DashboardBody.svelte';
	import DashboardHeader from '$lib/components/DashboardHeader/DashboardHeader.svelte';
	import Map from '$lib/components/Map/Map.svelte';
	import PaymentPopUp from '$lib/components/PaymentPopUp/PaymentPopUp.svelte';
	import PinPopup from '$lib/components/PinPopup/PinPopup.svelte';
	import ReservationCard from '$lib/components/ReservationCard/ReservationCard.svelte';
	import StationCard from '$lib/components/StationCard/StationCard.svelte';
	import Toast from '$lib/components/Toast/Toast.svelte';
	import TripCard from '$lib/components/TripCard/TripCard.svelte';
	import { riderStore } from '$lib/stores/rider.store.svelte';
	import { onMount } from 'svelte';

	onMount(() => {
		riderStore.initialize();
	});
</script>

<DashboardHeader />
<DashboardBody>
	<div>
		<Map bind:selectedStation={riderStore.selectedStation} />
	</div>
	<div class="flex min-h-fit flex-col gap-4">
		<StationCard />
		{#if riderStore.currentTrip}
			<TripCard
				trip={riderStore.currentTrip}
				loading={riderStore.loading}
				selectedStation={riderStore.selectedStation}
			/>
		{:else}
			<ReservationCard />
		{/if}
	</div>
</DashboardBody>

<PaymentPopUp />
<PinPopup />
<BillModal />

<Toast />
