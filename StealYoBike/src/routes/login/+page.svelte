<script lang="ts">
	import { goto } from '$app/navigation';
	import { authApi } from '$lib/api/index';
	import BadgeLink from '$lib/components/BadgeLink/BadgeLink.svelte';
	import type { AxiosError } from 'axios';

	let errorMessage = '';
	let isLoading = false;

	async function handleLogin(event: Event) {
		event.preventDefault();
		const form = event.target as HTMLFormElement;
		const formData = new FormData(form);
		const usernameOrEmail = formData.get('usernameOrEmail') as string;
		const password = formData.get('password') as string;

		isLoading = true;
		errorMessage = '';

		try {
			const response = await authApi.login({ usernameOrEmail, password });

			if (response.data.token) {
				localStorage.setItem('authToken', response.data.token);
			}

			// Redirect on success
			if (response.data.role == 'OPERATOR') {
				goto('/OP-dashboard');
			} else goto('/dashboard');
		} catch (error) {
			const axiosError = error as AxiosError<{ message: string }>;

			if (axiosError.response) {
				errorMessage = axiosError.response.data.message || 'Invalid usernameOrEmail or password';
			} else {
				errorMessage = 'Network error. Please try again.';
			}

			console.error('Login failed', error);
		} finally {
			isLoading = false;
		}
	}
</script>

<div class="flex size-full flex-col items-center justify-center bg-teal-100">
	<h1 class="mb-4 text-4xl font-bold">
		Login to <BadgeLink href="/" text="StealYoBike" variant="emerald" showUnderline={true} />
	</h1>
	<form class="w-full max-w-sm rounded bg-white p-6 shadow-md" on:submit={handleLogin}>
		{#if errorMessage}
			<div class="mb-4 rounded bg-red-100 p-3 text-red-700">
				{errorMessage}
			</div>
		{/if}

		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="usernameOrEmail">usernameOrEmail</label
			>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-teal-300 focus:outline-none"
				id="usernameOrEmail"
				name="usernameOrEmail"
				type="text"
				placeholder="Enter your username or email"
				required
			/>
		</div>
		<div class="mb-6">
			<label class="mb-2 block font-bold text-gray-700" for="password">Password</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-teal-300 focus:outline-none"
				id="password"
				name="password"
				type="password"
				placeholder="Enter your password"
				required
			/>
		</div>
		<button
			class="w-full rounded border-2 border-teal-600 bg-teal-400 px-4 py-2 font-bold text-gray-700 shadow-teal-500 transition-all duration-200 hover:-translate-x-[5px] hover:-translate-y-[5px] hover:shadow-[5px_5px_0px_0px] active:translate-x-0 active:translate-y-0 active:shadow-[0px_0px_0px_0px] disabled:cursor-not-allowed disabled:opacity-50"
			type="submit"
			disabled={isLoading}
		>
			{isLoading ? 'Logging in...' : 'Login'}
		</button>
	</form>
	<p class="mt-4 text-gray-600">
		Don't have an account?
		<BadgeLink
			href="/register"
			text="Register here"
			variant="green"
			showUnderline={true}
			className="font-bold"
		/>
	</p>
</div>
