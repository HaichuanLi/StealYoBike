<script lang="ts">
	import { authApi } from '$lib/api/auth.api';
	import { operatorApi } from '$lib/api/operator.api';
	import { riderApi } from '$lib/api/rider.api';
	import type { PastTripResponse, TripResponse } from '$lib/api/types/rider.types';
	import type { WithUIState } from '$lib/api/types/ui.types';
	import Button from '$lib/components/Button/Button.svelte';
	import Sidebar from '$lib/components/Sidebar/Sidebar.svelte';
	import Toast from '$lib/components/Toast/Toast.svelte';
	import TripDetailsModal from '$lib/components/TripDetailsModal/TripDetailsModal.svelte';
	import { showToast } from '$lib/stores/toast';
	import { onMount } from 'svelte';

	let pastTrips: WithUIState<PastTripResponse>[] | null = null;
	let loading = false;
	let user: any = null;
	let showSidebar = false;
	let isOperator = false;

	// filters
	let fromDate = '';
	let toDate = '';
	let bikeType: '' | 'REGULAR' | 'ELECTRIC' = '';

	// UC15/UC16 modal state
	let showDetails = false;
	let details: TripResponse | null = null;

	// UC16: input state
	let searchTripId = '';

	async function load() {
		try {
			loading = true;
			const hasFilters = !!(fromDate || toDate || bikeType);
			const params = hasFilters
				? {
						...(fromDate ? { fromDate } : {}),
						...(toDate ? { toDate } : {}),
						...(bikeType ? { type: bikeType } : {})
					}
				: undefined;

			if (isOperator) {
				const resp = await operatorApi.getAllPastTrips(params);
				pastTrips = resp.data;
			} else {
				const resp = await riderApi.getPastTrips(params);
				pastTrips = resp.data;
			}
		} catch (err) {
			console.warn('Failed to load past trips', err);
			pastTrips = null;
		} finally {
			loading = false;
		}
	}

	onMount(async () => {
		try {
			const u = await authApi.getCurrentUser();
			user = u.data;
			isOperator = user?.role === 'OPERATOR';
		} catch {
			user = null;
		}
		await load();
	});

	async function payTrip(t: WithUIState<PastTripResponse>) {
		try {
			t._paying = true;
			await riderApi.payTrip(t.tripId, { paymentToken: user?.paymentToken ?? '' });
			await load();
			showToast('Payment successful', 'success');
		} catch (err) {
			console.error('Payment failed', err);
			showToast('Payment failed', 'error');
			alert('Payment failed');
		} finally {
			t._paying = false;
		}
	}

	function clearFilters() {
		fromDate = '';
		toDate = '';
		bikeType = '';
		load();
	}

	// UC15 (details)
	async function openDetails(tripId: number) {
		try {
			details = isOperator
				? (await operatorApi.getTripDetails(tripId)).data
				: (await riderApi.getTripDetails(tripId)).data;
			showDetails = true;
		} catch (e) {
			console.error(e);
			showToast('Failed to load trip details', 'error');
		}
	}
	function closeDetails() {
		showDetails = false;
		details = null;
	}

	// UC16 (search by ID → shows the same modal)
	async function searchByTripId() {
		const id = Number(searchTripId);
		if (!id || Number.isNaN(id)) {
			showToast('Enter a valid Trip ID', 'error');
			return;
		}
		try {
			// Try UC16 endpoints first
			details = isOperator
				? (await operatorApi.searchTripById(id)).data
				: (await riderApi.searchTripById(id)).data;
			showDetails = true;
		} catch (err) {
			// Fallback to UC15 details (in case only that route exists)
			try {
				details = isOperator
					? (await operatorApi.getTripDetails(id)).data
					: (await riderApi.getTripDetails(id)).data;
				showDetails = true;
			} catch (e2) {
				console.error('Search failed', err, e2);
				showToast('Trip not found', 'error');
			}
		}
	}
</script>

<section class="p-6">
	<!-- Menu button -->
	<button
		class="fixed left-4 top-4 z-50 p-2"
		onclick={() => (showSidebar = true)}
		aria-label="Open Menu"
	>
		<svg
			xmlns="http://www.w3.org/2000/svg"
			class="h-6 w-6"
			fill="none"
			viewBox="0 0 24 24"
			stroke="currentColor"
		>
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M4 6h16M4 12h16M4 18h16"
			/>
		</svg>
	</button>

	{#if showSidebar}
		<Sidebar bind:showSidebar />
	{/if}
	<h1 class="mb-4 ml-12 text-2xl font-semibold">{isOperator ? 'All User Trips' : 'My Trips'}</h1>

	<!-- UC16: Search row -->
	<div class="mb-4 ml-12 flex flex-wrap items-end gap-3">
		<div class="flex flex-col">
			<label for="searchTripId" class="text-xs text-gray-600">Search by Trip ID</label>
			<input
				id="searchTripId"
				type="number"
				bind:value={searchTripId}
				class="w-48 rounded border px-2 py-1"
				placeholder="e.g. 123"
			/>
		</div>
		<Button text="Search" onclick={searchByTripId} variant="teal" />
	</div>

	<!-- Filter row -->
	<div class="mb-4 ml-12 flex flex-wrap items-end gap-3">
		<div class="flex flex-col">
			<label for="fromDate" class="text-xs text-gray-600">From</label>
			<input id="fromDate" type="date" bind:value={fromDate} class="rounded border px-2 py-1" />
		</div>
		<div class="flex flex-col">
			<label for="toDate" class="text-xs text-gray-600">To</label>
			<input id="toDate" type="date" bind:value={toDate} class="rounded border px-2 py-1" />
		</div>
		<div class="flex flex-col">
			<label for="bikeType" class="text-xs text-gray-600">Bike</label>
			<select id="bikeType" bind:value={bikeType} class="rounded border px-2 py-1">
				<option value="">All</option>
				<option value="REGULAR">Regular</option>
				<option value="ELECTRIC">E-bike</option>
			</select>
		</div>
		<Button text="Apply" onclick={load} variant="blue" />
		<Button onclick={clearFilters} text="Clear" variant="cyan" />
	</div>

	{#if loading}
		<p>Loading...</p>
	{:else if pastTrips && pastTrips.length > 0}
		<ul class="space-y-3">
			{#each pastTrips as t}
				<li
					class="flex items-center justify-between rounded border p-4 {t.paid
						? ''
						: 'border-rose-500'}"
				>
					<div>
						<div class="font-medium">Trip #{t.tripId}</div>
						{#if isOperator && t.userName}
							<div class="text-sm font-medium text-blue-600">User: {t.userName}</div>
						{/if}
						{#if t.bikeId}
							<div class="text-sm">Bike #{t.bikeId} ({t.bikeType})</div>
						{/if}
						<div class="text-sm text-gray-600">
							{t.startStationName ?? '—'} → {t.endStationName ?? '—'}
						</div>
						<div class="text-xs text-gray-500">
							{t.startTime ? new Date(t.startTime).toLocaleString() : ''} — {t.endTime
								? new Date(t.endTime).toLocaleString()
								: ''} ({t.durationMinutes}m)
						</div>
					</div>
					<div class="space-y-1 text-right">
						<div class="font-semibold">${t.totalAmount.toFixed(2)}</div>
						<div class="flex justify-end gap-2">
							<Button onclick={() => openDetails(t.tripId)} text="Details" variant="gray" />
							{#if t.paid}
								<div class="text-sm text-green-700">Paid</div>
							{:else if !isOperator}
								<Button
									disable={t._paying}
									onclick={() => payTrip(t)}
									text={t._paying ? 'Paying...' : 'Pay'}
									variant="blue"
								/>
							{:else}
								<div class="text-sm text-yellow-600">Unpaid</div>
							{/if}
						</div>
					</div>
				</li>
			{/each}
		</ul>
	{:else}
		<p class="text-sm text-gray-600">No past trips found.</p>
	{/if}
</section>

<!-- Details popup (reused for UC15 & UC16) -->
<TripDetailsModal show={showDetails} {details} onClose={closeDetails} />

<!-- Toast -->
<Toast />
