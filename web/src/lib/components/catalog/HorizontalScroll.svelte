<script lang="ts">
	import { type Snippet } from 'svelte';

	export interface HorizontalScrollProps {
		children?: Snippet;
	}

	let { children }: HorizontalScrollProps = $props();

	let containerRef: HTMLDivElement | undefined = $state();

	function scroll(direction: 'left' | 'right') {
		if (!containerRef) return;
		const width = containerRef.clientWidth;
		containerRef.scrollBy({
			left: direction === 'left' ? -width * 0.75 : width * 0.75,
			behavior: 'smooth'
		});
	}
</script>

<div class="group/scroll relative w-full">
	<!-- Left Scroll Handle -->
	<button
		onclick={() => scroll('left')}
		aria-label="Scroll Left"
		class="absolute top-1/2 left-4 z-10 hidden h-10 w-10 shrink-0 -translate-y-1/2 cursor-pointer items-center justify-center rounded-full border border-hairline bg-canvas/90 text-ink opacity-0 shadow-md transition-all group-hover/scroll:opacity-100 hover:bg-canvas hover:text-primary md:flex"
	>
		<svg
			class="h-5 w-5"
			fill="none"
			stroke="currentColor"
			stroke-width="2.5"
			viewBox="0 0 24 24"
			xmlns="http://www.w3.org/2000/svg"
		>
			<path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
		</svg>
	</button>

	<!-- Scrollable Container -->
	<div
		bind:this={containerRef}
		class="flex w-full snap-x snap-mandatory gap-4 overflow-x-auto scroll-smooth px-1 pt-1 pb-5 md:gap-5"
		style="scrollbar-width: thin; -ms-overflow-style: none;"
	>
		{@render children?.()}
	</div>

	<!-- Right Scroll Handle -->
	<button
		onclick={() => scroll('right')}
		aria-label="Scroll Right"
		class="absolute top-1/2 right-4 z-10 hidden h-10 w-10 shrink-0 -translate-y-1/2 cursor-pointer items-center justify-center rounded-full border border-hairline bg-canvas/90 text-ink opacity-0 shadow-md transition-all group-hover/scroll:opacity-100 hover:bg-canvas hover:text-primary md:flex"
	>
		<svg
			class="h-5 w-5"
			fill="none"
			stroke="currentColor"
			stroke-width="2.5"
			viewBox="0 0 24 24"
			xmlns="http://www.w3.org/2000/svg"
		>
			<path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
		</svg>
	</button>
</div>

<style>
	/* Hide standard scrollbars on mobile webkits but keep them scrollable */
	div::-webkit-scrollbar {
		height: 6px;
	}
	div::-webkit-scrollbar-track {
		background: transparent;
	}
	div::-webkit-scrollbar-thumb {
		background: var(--color-hairline-strong);
		border-radius: 9999px;
	}
</style>
