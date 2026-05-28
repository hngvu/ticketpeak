<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable svelte/require-each-key */
	/* eslint-disable svelte/prefer-svelte-reactivity */
	import { page } from '$app/state';

	export interface PaginationBarProps {
		currentPage: number; // 0-indexed
		totalPages: number;
	}

	let { currentPage, totalPages }: PaginationBarProps = $props();

	// Construct pagination URLs
	function getPageUrl(pageNum: number) {
		const newParams = new URLSearchParams(page.url.searchParams);
		if (pageNum === 0) {
			newParams.delete('page');
		} else {
			newParams.set('page', String(pageNum));
		}
		return `${page.url.pathname}?${newParams.toString()}`;
	}

	// Generate list of page numbers to display
	const pageList = $derived(() => {
		const pages: Array<number | string> = [];
		const current = currentPage; // e.g. 0
		const total = totalPages;

		if (total <= 5) {
			for (let i = 0; i < total; i++) pages.push(i);
		} else {
			// Always show first, last, current, and adjacent pages
			pages.push(0);

			if (current > 2) {
				pages.push('...');
			}

			const start = Math.max(1, current - 1);
			const end = Math.min(total - 2, current + 1);

			for (let i = start; i <= end; i++) {
				pages.push(i);
			}

			if (current < total - 3) {
				pages.push('...');
			}

			pages.push(total - 1);
		}

		return pages;
	});
</script>

{#if totalPages > 1}
	<nav class="mt-8 flex items-center justify-center gap-1.5 py-6" aria-label="Pagination">
		<!-- Previous Page Button -->
		{#if currentPage > 0}
			<a
				href={getPageUrl(currentPage - 1)}
				class="flex h-9 w-9 items-center justify-center rounded-md border border-hairline bg-canvas text-body transition-colors hover:bg-canvas-soft hover:text-ink"
				aria-label="Previous Page"
			>
				<svg
					class="h-4 w-4"
					fill="none"
					stroke="currentColor"
					stroke-width="2.5"
					viewBox="0 0 24 24"
					xmlns="http://www.w3.org/2000/svg"
				>
					<path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
				</svg>
			</a>
		{:else}
			<span
				class="flex h-9 w-9 cursor-not-allowed items-center justify-center rounded-md border border-hairline bg-canvas-soft text-mute opacity-50"
			>
				<svg
					class="h-4 w-4"
					fill="none"
					stroke="currentColor"
					stroke-width="2.5"
					viewBox="0 0 24 24"
					xmlns="http://www.w3.org/2000/svg"
				>
					<path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
				</svg>
			</span>
		{/if}

		<!-- Numeric Pages -->
		{#each pageList() as pg}
			{#if pg === '...'}
				<span class="flex h-9 w-9 items-center justify-center text-xs text-mute select-none"
					>...</span
				>
			{:else}
				<a
					href={getPageUrl(Number(pg))}
					class="flex h-9 w-9 items-center justify-center rounded-md font-mono text-xs font-bold transition-colors {currentPage ===
					pg
						? 'bg-primary text-on-primary shadow-sm'
						: 'border border-hairline bg-canvas text-body hover:bg-canvas-soft hover:text-ink'}"
					aria-current={currentPage === pg ? 'page' : undefined}
				>
					{Number(pg) + 1}
				</a>
			{/if}
		{/each}

		<!-- Next Page Button -->
		{#if currentPage < totalPages - 1}
			<a
				href={getPageUrl(currentPage + 1)}
				class="flex h-9 w-9 items-center justify-center rounded-md border border-hairline bg-canvas text-body transition-colors hover:bg-canvas-soft hover:text-ink"
				aria-label="Next Page"
			>
				<svg
					class="h-4 w-4"
					fill="none"
					stroke="currentColor"
					stroke-width="2.5"
					viewBox="0 0 24 24"
					xmlns="http://www.w3.org/2000/svg"
				>
					<path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
				</svg>
			</a>
		{:else}
			<span
				class="flex h-9 w-9 cursor-not-allowed items-center justify-center rounded-md border border-hairline bg-canvas-soft text-mute opacity-50"
			>
				<svg
					class="h-4 w-4"
					fill="none"
					stroke="currentColor"
					stroke-width="2.5"
					viewBox="0 0 24 24"
					xmlns="http://www.w3.org/2000/svg"
				>
					<path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
				</svg>
			</span>
		{/if}
	</nav>
{/if}
