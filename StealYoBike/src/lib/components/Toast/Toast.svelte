<script lang="ts">
    import { toast } from '$lib/stores/toast';
    import { fly } from 'svelte/transition';

    // Use Svelte auto-subscription: $toast will track the `toast` store
    // $toast shape: { message, type, visible }
</script>

{#if $toast.visible}
    <div class="fixed right-4 bottom-6 z-50" transition:fly={{ y: 10 }}>
        <div class="max-w-xs px-4 py-3 rounded shadow-lg text-white" class:info={$toast.type === 'info'} class:success={$toast.type === 'success'} class:error={$toast.type === 'error'}>
            {$toast.message}
        </div>
    </div>
{/if}

<style>
    .info { background: rgba(14,165,233,0.95); }
    .success { background: rgba(16,185,129,0.95); }
    .error { background: rgba(239,68,68,0.95); }
</style>
