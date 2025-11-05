<script lang="ts">
    import type { Snippet } from 'svelte';
    import { cubicIn, cubicOut } from 'svelte/easing';
    import { fade } from 'svelte/transition';
  
    interface Props {
      children: Snippet;
      isVisible: boolean;
      onClose: () => void;
    }
  
    const { children, isVisible, onClose }: Props = $props();
  
    // svelte-ignore non_reactive_update
    let contentElement: HTMLElement;
    let savedScrollY = 0;
    let originalBodyStyle = '';
    let canClose = $state(false);
  
    $effect(() => {
      if (isVisible) {
        canClose = false;
        setTimeout(() => {
          canClose = true;
        }, 300);
      }
    });
  
    $effect(() => {
      if (isVisible) {
        savedScrollY = window.scrollY;
        originalBodyStyle = document.body.getAttribute('style') || '';
  
        document.body.style.cssText = `
          ${originalBodyStyle};
          overflow: hidden;
          touch-action: pan-y;
          position: fixed;
          width: 100%;
          top: -${savedScrollY}px;
        `;
      }
  
      return () => {
        if (isVisible) {
          restoreBodyScroll();
        }
      };
    });
  
    function restoreBodyScroll() {
      if (originalBodyStyle) {
        document.body.setAttribute('style', originalBodyStyle);
      } else {
        document.body.removeAttribute('style');
      }
  
      window.scrollTo(0, savedScrollY);
    }
  
    function handleOutroEnd() {
      restoreBodyScroll();
    }
  
    function handleOverlayClick(event: MouseEvent | KeyboardEvent) {
      if (!canClose) return;
  
      const isValidKeyboardEvent =
        event instanceof KeyboardEvent && (event.key === 'Enter' || event.key === ' ');
      const isValidMouseEvent = event instanceof MouseEvent && event.target === event.currentTarget;
  
      if (isValidKeyboardEvent || isValidMouseEvent) {
        onClose();
      }
    }
  
    function handleKeyDown(event: KeyboardEvent) {
      if (isVisible && event.key === 'Escape') {
        onClose();
      }
    }
  
    function isScrollableElement(element: Element): boolean {
      const style = window.getComputedStyle(element);
      const overflowValues = [style.overflow, style.overflowY];
      return overflowValues.some((value) => value === 'auto' || value === 'scroll');
    }
  
    function preventScroll(event: TouchEvent | WheelEvent) {
      if (!isVisible || !contentElement) return;
  
      const target = event.target as Element;
  
      if (contentElement.contains(target)) {
        let element = target;
        while (element && element !== contentElement) {
          if (isScrollableElement(element)) return;
          element = element.parentElement as Element;
        }
        return;
      }
  
      event.preventDefault();
    }
  </script>
  
  <svelte:window onkeydown={handleKeyDown} ontouchmove={preventScroll} onwheel={preventScroll} />
  
  {#if isVisible}
    <div
      class="fixed inset-0 z-[9999] flex size-full items-center justify-center backdrop-blur-[2px]"
      in:fade={{ duration: 250, easing: cubicIn }}
      out:fade={{ duration: 250, easing: cubicOut }}
      onoutroend={handleOutroEnd}
      data-testid="popup-overlay"
      role="button"
      tabindex="0"
      onclick={handleOverlayClick}
      onkeydown={handleOverlayClick}
    >
      <div
        bind:this={contentElement}
        class="scrollbar-thin mx-4 flex size-full items-center justify-center"
        data-testid="popup"
        role="dialog"
        tabindex="-1"
        onclick={handleOverlayClick}
        onkeydown={handleOverlayClick}
      >
        {@render children?.()}
      </div>
    </div>
  {/if}
  