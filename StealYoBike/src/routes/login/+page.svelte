<div class="flex min-h-screen flex-col items-center justify-center bg-gray-100">
	<h1 class="mb-4 text-4xl font-bold">Login to StealYoBike</h1>
	<form class="w-full max-w-sm rounded bg-white p-6 shadow-md">
		<div class="mb-4">
			<label class="mb-2 block font-bold text-gray-700" for="username">Username</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
				id="username"
				type="text"
				placeholder="Enter your username"
			/>
		</div>
		<div class="mb-6">
			<label class="mb-2 block font-bold text-gray-700" for="password">Password</label>
			<input
				class="w-full rounded border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
				id="password"
				type="password"
				placeholder="Enter your password"
			/>
		</div>
		<button
			class="w-full rounded bg-blue-500 px-4 py-2 font-bold text-white hover:bg-blue-600"
			type="submit">Login</button
		>
	</form>
	<p class="mt-4 text-gray-600">
		Don't have an account?
		<a href="/register" class="text-green-500 hover:underline">Register here</a>
	</p>
</div>
