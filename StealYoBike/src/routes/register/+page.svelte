<script lang="ts">
	import { goto } from '$app/navigation';
	import { authApi } from '$lib/api/index';
	import BadgeLink from '$lib/components/BadgeLink/BadgeLink.svelte';

	let errorMessage = '';
	let isLoading = false;

	async function handleRegister(event: Event) {
		event.preventDefault();
		const form = event.target as HTMLFormElement;
		const formData = new FormData(form);

		const fullName = formData.get('fullName') as string;
		const address = formData.get('address') as string;
		const email = formData.get('email') as string;
		const username = formData.get('username') as string;
		const password = formData.get('password') as string;
		const paymentToken = formData.get('paymentToken') as string;
		const plan = ((formData.get('plan') as string) || 'PAYPERRIDE') as
			| 'PAYPERRIDE'
			| 'MONTHLY'
			| 'ANNUAL';
		const tier = ((formData.get('tier') as string) || 'REGULAR') as
			| 'REGULAR'
			| 'BRONZE'
			| 'SILVER'
			| 'GOLD';

		isLoading = true;
		errorMessage = '';

		try {
			await authApi.register({
				fullName,
				address,
				email,
				username,
				password,
				paymentToken,
				plan
			});
			// Redirect on success
			goto('/login');
		} catch (error) {
			console.error('Registration failed', error);
			errorMessage = 'Registration failed. Please try again.';
		} finally {
			isLoading = false;
		}
	}
</script>

<div class="flex size-full flex-col items-center justify-center bg-green-100">
	<h1 class="mb-4 text-4xl font-bold">
		Register for <BadgeLink href="/" text="StealYoBike" variant="emerald" showUnderline={true} />
	</h1>
	<form class="w-full max-w-sm rounded bg-white p-6 shadow-md" on:submit={handleRegister}>
		{#if errorMessage}
			<div class="mb-4 rounded bg-red-100 p-3 text-red-700">
				{errorMessage}
			</div>
		{/if}

		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="fullName">Full Name</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-green-300 focus:outline-none"
				id="fullName"
				name="fullName"
				type="text"
				placeholder="Enter your full name"
				required
			/>
		</div>
		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="address">Address</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-green-300 focus:outline-none"
				id="address"
				name="address"
				type="text"
				placeholder="Enter your address"
				required
			/>
		</div>
		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="email">Email</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-green-300 focus:outline-none"
				id="email"
				name="email"
				type="email"
				placeholder="Enter your email"
				required
			/>
		</div>
		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="username">Username</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-green-300 focus:outline-none"
				id="username"
				name="username"
				type="text"
				placeholder="Enter your username"
				required
			/>
		</div>
		<div class="mb-6">
			<label class="mb-2 block font-bold text-gray-700" for="password">Password</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-green-300 focus:outline-none"
				id="password"
				name="password"
				type="password"
				placeholder="Enter your password"
				required
			/>
		</div>
		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="paymentToken">Payment Token</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-green-300 focus:outline-none"
				id="paymentToken"
				name="paymentToken"
				type="text"
				placeholder="Enter your payment token (can do later)"
			/>
		</div>
		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="plan">Plan</label>
			<select
				id="plan"
				name="plan"
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-green-300 focus:outline-none"
			>
				<option value="PAYPERRIDE">Pay Per Ride (default)</option>
				<option value="MONTHLY">Monthly (30 min free, 50% e-bike surcharge)</option>
				<option value="ANNUAL">Annual (45 min free, free e-bike)</option>
			</select>
		</div>
		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="tier">Tier</label>
			<select
				id="tier"
				name="tier"
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-green-300 focus:outline-none"
			>
				<option value="REGULAR">Regular (default)</option>
				<option value="BRONZE">Bronze (5% discount)</option>
				<option value="SILVER">Silver (10% discount)</option>
				<option value="GOLD">Gold (15% discount)</option>
			</select>
		</div>
		<button
			class="w-full rounded border-2 border-green-600 bg-green-400 px-4 py-2 font-bold text-gray-700 shadow-green-500 transition-all duration-200 hover:-translate-x-[5px] hover:-translate-y-[5px] hover:shadow-[5px_5px_0px_0px] active:translate-x-0 active:translate-y-0 active:shadow-[0px_0px_0px_0px] disabled:cursor-not-allowed disabled:opacity-50"
			type="submit"
			disabled={isLoading}
		>
			{isLoading ? 'Registering...' : 'Register'}
		</button>
	</form>
	<p class="mt-4 text-gray-600">
		Already have an account?
		<BadgeLink
			href="/login"
			text="Login here"
			variant="teal"
			showUnderline={true}
			className="font-bold"
		/>
	</p>
</div>
