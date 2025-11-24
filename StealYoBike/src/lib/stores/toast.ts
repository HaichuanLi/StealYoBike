import { writable } from 'svelte/store';

export type ToastType = 'info' | 'success' | 'error' | 'warning';

export const toast = writable<{ message: string; type: ToastType; visible: boolean }>({
	message: '',
	type: 'info',
	visible: false
});

export function showToast(message: string, type: ToastType = 'info', duration = 3000) {
	toast.set({ message, type, visible: true });
	setTimeout(() => toast.update((t) => ({ ...t, visible: false })), duration);
}
