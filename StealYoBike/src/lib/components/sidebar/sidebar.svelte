<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import NavMenu from '$lib/components/sidebar/NavMenu.svelte';
	import { onMount } from 'svelte';
	import { authApi } from '$lib/api/auth.api';
	import type { UserInfoResponse } from '$lib/api/types/auth.types';

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
		{ href: '/information', label: 'Information', icon: 'M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z M15 11a3 3 0 11-6 0 3 3 0 016 0z', isLoggedInNecessary: false},
		{ href: '/dashboard', label: 'Dashboard', icon: 'M3 3h7v7H3V3zm0 11h7v7H3v-7zm11-11h7v7h-7V3zm0 11h7v7h-7v-7z', isLoggedInNecessary: true},
	]

	function onNavigate(href: string) {
        if (href === '/logout') {
            authApi.logout();
            user = null;
        } else {
            showSidebar = false;
            goto(href);
        }
    }

	function isActivePage(href: string): boolean {
		return $page.url.pathname === href;
	}
</script>

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
    <NavMenu {navItems} {onNavigate} {isActivePage} isUserLoggedIn={!!user} />
    
    <!-- User section -->
    <div class="absolute bottom-6 left-4 right-4">
        <div class="rounded-lg bg-emerald-200 p-4">
            {#if loading}
                <p class="text-sm text-emerald-600">Loading...</p>
            {:else if user}
                <p class="text-sm font-medium text-emerald-800">Welcome back!</p>
                <p class="text-xs text-emerald-600">{user.email}</p>
                <p class="text-xs text-emerald-500">{user.fullName}</p>
                <button 
                    onclick={() => onNavigate('/logout')}
                    class="mt-2 text-xs text-emerald-700 hover:text-emerald-900 underline"
                >
                    Sign out
                </button>
            {:else}
                <p class="text-sm font-medium text-emerald-800">Not logged in</p>
                <button 
                    onclick={() => onNavigate('/login')}
                    class="mt-2 text-xs text-emerald-700 hover:text-emerald-900 underline"
                >
                    Sign in
                </button>
            {/if}
        </div>
    </div>
</div>
