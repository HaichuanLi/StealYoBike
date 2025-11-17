<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import { authApi } from '$lib/api/auth.api';
	import type { UserInfoResponse } from '$lib/api/types/auth.types';
	import { onMount } from 'svelte';
	import NavMenu from './NavMenu.svelte';

	let { showSidebar = $bindable() } = $props();

	let user = $state<UserInfoResponse | null>(null);
	let loading = $state(true);

	onMount(async () => {
		try {
			const response = await authApi.getCurrentUser();
			user = response.data;
		} catch (error) {
			console.error('Failed to load user:', error);
		} finally {
			loading = false;
		}
	});

	const navItems = [
		{
			href: '/information',
			label: 'Information',
			icon: 'M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z M15 11a3 3 0 11-6 0 3 3 0 016 0z',
			isLoggedInNecessary: false
		},
		{
			href: '/dashboard',
			label: 'Dashboard',
			icon: 'M3 3h7v7H3V3zm0 11h7v7H3v-7zm11-11h7v7h-7V3zm0 11h7v7h-7v-7z',
			isLoggedInNecessary: true
		},
		{
			href: '/trips',
			label: 'Trips',
			icon: 'M3 7h18M3 12h18M3 17h18',
			isLoggedInNecessary: true
		},
		{
			href: '/OP-dashboard',
			label: 'Operator Dashboard',
			icon: 'm19.588 15.492l-1.814-1.29a6.483 6.483 0 0 0-.005-3.421l1.82-1.274l-1.453-2.514l-2.024.926a6.484 6.484 0 0 0-2.966-1.706L12.953 4h-2.906l-.193 2.213A6.483 6.483 0 0 0 6.889 7.92l-2.025-.926l-1.452 2.514l1.82 1.274a6.483 6.483 0 0 0-.006 3.42l-1.814 1.29l1.452 2.502l2.025-.927a6.483 6.483 0 0 0 2.965 1.706l.193 2.213h2.906l.193-2.213a6.484 6.484 0 0 0 2.965-1.706l2.025.927l1.453-2.501ZM13.505 2.985a.5.5 0 0 1 .5.477l.178 2.035a7.45 7.45 0 0 1 2.043 1.178l1.85-.863a.5.5 0 0 1 .662.195l2.005 3.47a.5.5 0 0 1-.162.671l-1.674 1.172c.128.798.124 1.593.001 2.359l1.673 1.17a.5.5 0 0 1 .162.672l-2.005 3.457a.5.5 0 0 1-.662.195l-1.85-.863c-.602.49-1.288.89-2.043 1.179l-.178 2.035a.5.5 0 0 1-.5.476h-4.01a.5.5 0 0 1-.5-.476l-.178-2.035a7.453 7.453 0 0 1-2.043-1.179l-1.85.863a.5.5 0 0 1-.663-.194L2.257 15.52a.5.5 0 0 1 .162-.671l1.673-1.171a7.45 7.45 0 0 1 0-2.359L2.42 10.148a.5.5 0 0 1-.162-.67L4.26 6.007a.5.5 0 0 1 .663-.195l1.85.863a7.45 7.45 0 0 1 2.043-1.178l.178-2.035a.5.5 0 0 1 .5-.477h4.01ZM11.5 9a3.5 3.5 0 1 1 0 7a3.5 3.5 0 0 1 0-7Zm0 1a2.5 2.5 0 1 0 0 5a2.5 2.5 0 0 0 0-5Z',
			isLoggedInNecessary: true,
			roles: ['OPERATOR', 'ADMIN']
		}
	];

	function getUserRoles(u: UserInfoResponse | null): string[] {
		if (!u) return [];
		if (Array.isArray((u as any).role))
			return (u as any).role.map((r: string) => String(r).toUpperCase());
		return [String((u as any).role).toUpperCase()];
	}

	function canAccess(item: any, u: UserInfoResponse | null) {
		if (!item.isLoggedInNecessary) return true;
		if (!u) return false;
		if (item.roles && item.roles.length > 0) {
			const allowed = item.roles.map((r: string) => r.toUpperCase());
			const ur = getUserRoles(u);
			return ur.some((r) => allowed.includes(r));
		}
		return true;
	}

	function getVisibleNavItems() {
		if (loading) return navItems.filter((item) => !item.isLoggedInNecessary);
		return navItems.filter((item) => canAccess(item, user));
	}

	async function onNavigate(href: string) {
		if (href === '/logout') {
			try {
				authApi.logout();
			} catch (err) {
				console.error('Logout failed:', err);
			} finally {
				user = null;
				showSidebar = false;
				await goto('/login');
			}
			return;
		}

		const item = navItems.find((i) => i.href === href);
		if (item) {
			if (item.isLoggedInNecessary && !user) {
				showSidebar = false;
				await goto(`/login?next=${encodeURIComponent(href)}`);
				return;
			}
			if (item.roles && item.roles.length > 0 && (!user || !canAccess(item, user))) {
				showSidebar = false;
				await goto('/unauthorized');
				return;
			}
		}

		showSidebar = false;
		await goto(href);
	}

	function isActivePage(href: string): boolean {
		return page.url.pathname === href || page.url.pathname.startsWith(href + '/');
	}
</script>

<svelte:window onkeydown={(e) => (e.key === 'Escape' ? (showSidebar = false) : null)} />

<button
	type="button"
	class="z-1000 fixed left-0 top-0 h-full w-full bg-emerald-50/50"
	aria-label="Close Sidebar Overlay"
	onclick={() => (showSidebar = false)}
></button>
<div class="z-1000 fixed left-0 top-0 h-full w-80 bg-emerald-100">
	<button
		class="absolute right-5 top-5"
		onclick={() => (showSidebar = false)}
		aria-label="Close Menu"
	>
		<svg
			xmlns="http://www.w3.org/2000/svg"
			class="h-10 w-10 stroke-[1.5] transition-all duration-300 hover:stroke-2 active:stroke-1"
			viewBox="0 0 24 24"
			fill="none"
		>
			<path d="M18 6L6 18" stroke="#1C274C" stroke-linecap="round" />
			<path d="M6 6L18 18" stroke="#1C274C" stroke-linecap="round" />
		</svg>
	</button>

	<!-- Header -->
	<div class="px-6 py-8">
		<h2 class="text-2xl font-bold text-emerald-800">StealYoBike</h2>
		<p class="mt-1 text-sm text-emerald-600">Navigate your journey</p>
	</div>

	<!-- Navigation Menu -->
	<NavMenu navItems={getVisibleNavItems()} {onNavigate} {isActivePage} isUserLoggedIn={!!user} />

	<!-- User section -->
	<div class="absolute bottom-6 left-4 right-4">
		<div class="rounded-lg bg-emerald-200 p-4">
			{#if loading}
				<p class="text-sm text-emerald-600">Loading...</p>
			{:else if user}
				<p class="text-sm font-medium text-emerald-800">Welcome back!</p>
				<p class="text-xs text-emerald-600">{user.email}</p>
				<p class="text-xs text-emerald-500">{user.fullName}</p>
				<p class="mt-2 text-xs font-semibold text-emerald-700">
					Tier: <span class="font-bold text-emerald-800">{user.tier || 'REGULAR'}</span>
				</p>
				<p class="text-xs font-semibold text-emerald-700">
					Flex Dollars: <span class="font-bold text-emerald-800">${user.flexDollar.toFixed(2)}</span>
				</p>
				<button
					onclick={() => onNavigate('/logout')}
					class="mt-2 text-xs text-emerald-700 underline hover:text-emerald-900"
				>
					Sign out
				</button>
			{:else}
				<p class="text-sm font-medium text-emerald-800">Not logged in</p>
				<button
					onclick={() => onNavigate('/login')}
					class="mt-2 text-xs text-emerald-700 underline hover:text-emerald-900"
				>
					Sign in
				</button>
			{/if}
		</div>
	</div>
</div>
