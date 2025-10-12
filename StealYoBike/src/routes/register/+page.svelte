<script lang="ts">
	import BadgeLink from '$lib/components/BadgeLink/BadgeLink.svelte';
</script>

<div class="flex size-full flex-col items-center justify-center bg-green-100">
	<h1 class="mb-4 text-4xl font-bold">
		Register for <BadgeLink href="/" text="StealYoBike" variant="emerald" showUnderline={true} />
	</h1>
	<form class="w-full max-w-sm rounded bg-white p-6 shadow-md">
		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="username">Username</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-green-300 focus:outline-none"
				id="username"
				type="text"
				placeholder="Enter your username"
			/>
		</div>
		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="email">Email</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-green-300 focus:outline-none"
				id="email"
				type="email"
				placeholder="Enter your email"
			/>
		</div>
		<div class="mb-6">
			<label class="mb-2 block font-bold text-gray-700" for="password">Password</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-green-300 focus:outline-none"
				id="password"
				type="password"
				placeholder="Enter your password"
			/>
		</div>
		<button
			class="w-full rounded border-2 border-green-600 bg-green-400 px-4 py-2 font-bold text-gray-700 shadow-green-500 transition-all duration-200 hover:-translate-x-[5px] hover:-translate-y-[5px] hover:shadow-[5px_5px_0px_0px] active:translate-x-0 active:translate-y-0 active:shadow-[0px_0px_0px_0px]"
			type="submit">Register</button
		>
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
