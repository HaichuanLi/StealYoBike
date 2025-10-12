<script lang="ts">
	import BadgeLink from '$lib/components/BadgeLink/BadgeLink.svelte';
</script>

<div class="flex size-full flex-col items-center justify-center bg-teal-100">
	<h1 class="mb-4 text-4xl font-bold">
		Login to <BadgeLink href="/" text="StealYoBike" variant="emerald" showUnderline={true} />
	</h1>
	<form class="w-full max-w-sm rounded bg-white p-6 shadow-md">
		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="username">Username</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-teal-300 focus:outline-none"
				id="username"
				type="text"
				placeholder="Enter your username"
			/>
		</div>
		<div class="mb-6">
			<label class="mb-2 block font-bold text-gray-700" for="password">Password</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-teal-300 focus:outline-none"
				id="password"
				type="password"
				placeholder="Enter your password"
			/>
		</div>
		<button
			class="w-full rounded border-2 border-teal-600 bg-teal-400 px-4 py-2 font-bold text-gray-700 shadow-teal-500 transition-all duration-200 hover:-translate-x-[5px] hover:-translate-y-[5px] hover:shadow-[5px_5px_0px_0px] active:translate-x-0 active:translate-y-0 active:shadow-[0px_0px_0px_0px]"
			type="submit">Login</button
		>
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
