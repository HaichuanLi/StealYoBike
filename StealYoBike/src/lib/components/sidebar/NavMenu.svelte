<script lang="ts">
	interface Props {
		navItems: Array<{ href: string; label: string; icon: string; isLoggedInNecessary?: boolean }>;
		onNavigate: (href: string) => void;
		isActivePage: (href: string) => boolean;
		isUserLoggedIn: boolean;
	}

	let { navItems, onNavigate, isActivePage, isUserLoggedIn }: Props = $props();

	function isItemDisabled(item: { isLoggedInNecessary?: boolean }): boolean {
		return item.isLoggedInNecessary === true && !isUserLoggedIn;
	}

	function handleItemClick(item: { href: string; isLoggedInNecessary?: boolean }) {
		if (isItemDisabled(item)) {
			// Optionally show a message or redirect to login
			return;
		}
		onNavigate(item.href);
	}
</script>

<nav class="px-4">
	<ul class="space-y-2">
		{#each navItems as item}
			<li>
				<button
					onclick={() => handleItemClick(item)}
					disabled={isItemDisabled(item)}
					class="group flex w-full items-center gap-3 rounded-lg px-4 py-3 text-left transition-all duration-200
                    {isItemDisabled(item)
						? 'cursor-not-allowed text-gray-400 opacity-50'
						: isActivePage(item.href)
							? 'bg-emerald-600 text-white shadow-md'
							: 'text-emerald-700 hover:bg-emerald-200 hover:text-emerald-900 hover:shadow-sm'}"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="h-5 w-5 transition-colors
                        {isItemDisabled(item)
							? 'stroke-gray-400'
							: isActivePage(item.href)
								? 'stroke-white'
								: 'stroke-current'}"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
						stroke-width="1.5"
					>
						<path stroke-linecap="round" stroke-linejoin="round" d={item.icon} />
					</svg>
					<span class="font-medium">{item.label}</span>
					{#if isItemDisabled(item)}
						<svg
							xmlns="http://www.w3.org/2000/svg"
							class="ml-auto h-4 w-4 stroke-gray-400"
							fill="none"
							viewBox="0 0 24 24"
							stroke="currentColor"
							stroke-width="2"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
							/>
						</svg>
					{/if}
				</button>
			</li>
		{/each}
	</ul>
</nav>
