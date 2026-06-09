<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable svelte/require-each-key */
	/* eslint-disable svelte/prefer-svelte-reactivity */
	/* eslint-disable @typescript-eslint/no-unused-vars */
	import EventCard from '$lib/components/catalog/EventCard.svelte';
	import PaginationBar from '$lib/components/catalog/PaginationBar.svelte';
	import EmptyState from '$lib/components/common/EmptyState.svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';

	let { data } = $props();

	// Search keyword bind
	let searchVal = $state(data.query);
	let locationVal = $state(data.location);

	// Date Range options
	let datePreset = $state('any'); // any, today, weekend, month, custom
	let customStartDate = $state(data.startDate);
	let customEndDate = $state(data.endDate);

	// Check if dates represent a preset on load
	$effect(() => {
		searchVal = data.query;
		locationVal = data.location;
		customStartDate = data.startDate;
		customEndDate = data.endDate;

		if (data.startDate || data.endDate) {
			datePreset = 'custom';
		} else {
			datePreset = 'any';
		}
	});

	function applyFilters() {
		const newParams = new URLSearchParams();
		newParams.delete('page'); // Reset pagination

		if (searchVal.trim()) {
			newParams.set('q', searchVal.trim());
		}
		if (locationVal.trim()) {
			newParams.set('location', locationVal.trim());
		}

		if (datePreset === 'custom') {
			if (customStartDate) newParams.set('startDate', customStartDate);
			if (customEndDate) newParams.set('endDate', customEndDate);
		} else if (datePreset === 'today') {
			const today = new Date().toISOString().split('T')[0];
			newParams.set('startDate', today);
			newParams.set('endDate', today);
		} else if (datePreset === 'weekend') {
			const today = new Date();
			const day = today.getDay();
			const fri = new Date(today);
			const sun = new Date(today);

			// next Friday
			fri.setDate(today.getDate() + ((5 - day + 7) % 7));
			// next Sunday
			sun.setDate(today.getDate() + ((7 - day + 7) % 7));

			newParams.set('startDate', fri.toISOString().split('T')[0]);
			newParams.set('endDate', sun.toISOString().split('T')[0]);
		}

		goto(`/search?${newParams.toString()}`);
	}

	function handlePresetChange(preset: string) {
		datePreset = preset;
		if (preset !== 'custom') {
			applyFilters();
		}
	}

	function clearAll() {
		searchVal = '';
		locationVal = '';
		datePreset = 'any';
		customStartDate = '';
		customEndDate = '';
		goto('/search');
	}
</script>

<svelte:head>
	<title>Search Events & Find Tickets — Ticketpeak</title>
	<meta
		name="description"
		content="Search upcoming concerts, sports games, arts, theater, and family attractions on Ticketpeak."
	/>
</svelte:head>

<div class="mx-auto max-w-[1400px] space-y-6 px-4 py-8 md:px-6">
	<!-- SEARCH BAR & FILTER ROW (inline, no sidebar) -->
	<section class="space-y-4 rounded-xl border border-hairline bg-canvas p-4 shadow-sm md:p-6">
		<!-- Search Form -->
		<form
			onsubmit={(e) => {
				e.preventDefault();
				applyFilters();
			}}
			class="flex flex-col gap-3 sm:flex-row"
		>
			<div class="relative flex-grow">
				<input
					type="text"
					placeholder="Search for artists, teams, concerts, venues..."
					bind:value={searchVal}
					class="h-11 w-full rounded-lg border border-hairline bg-canvas-soft pr-4 pl-11 text-sm text-ink placeholder-mute transition-colors focus:border-primary focus:bg-canvas focus:outline-none"
				/>
				<svg
					class="absolute top-3.5 left-4 h-4.5 w-4.5 text-mute"
					xmlns="http://www.w3.org/2000/svg"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
					stroke-width="2.5"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
					/>
				</svg>
			</div>

			<!-- Location filter -->
			<div class="relative w-full sm:max-w-[200px]">
				<input
					type="text"
					placeholder="📍 City / Location"
					bind:value={locationVal}
					class="h-11 w-full rounded-lg border border-hairline bg-canvas-soft pr-4 pl-10 text-sm text-ink placeholder-mute transition-colors focus:border-primary focus:bg-canvas focus:outline-none"
				/>
				<svg
					class="absolute top-3.5 left-3.5 h-4.5 w-4.5 text-mute"
					fill="none"
					stroke="currentColor"
					stroke-width="2"
					viewBox="0 0 24 24"
					xmlns="http://www.w3.org/2000/svg"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M15 10.5a3 3 0 11-6 0 3 3 0 016 0z"
					/>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25s-7.5-4.108-7.5-11.25a7.5 7.5 0 1115 0z"
					/>
				</svg>
			</div>

			<button
				type="submit"
				class="h-11 cursor-pointer rounded-lg bg-primary px-6 text-sm font-bold text-on-primary transition-colors hover:bg-primary/95"
			>
				Apply Filters
			</button>
		</form>

		<!-- Date range presets + actions row -->
		<div class="flex flex-wrap items-center justify-between gap-4 border-t border-hairline pt-3">
			<div class="flex flex-wrap items-center gap-2">
				<span class="font-mono text-xs font-bold tracking-wider text-mute uppercase"
					>Date Range:</span
				>

				<button
					onclick={() => handlePresetChange('any')}
					class="rounded-full px-3 py-1.5 text-xs font-bold transition-all {datePreset === 'any'
						? 'border border-primary/20 bg-primary/10 text-primary'
						: 'cursor-pointer border border-hairline bg-canvas text-body hover:bg-canvas-soft hover:text-ink'}"
				>
					Any Date
				</button>
				<button
					onclick={() => handlePresetChange('today')}
					class="rounded-full px-3 py-1.5 text-xs font-bold transition-all {datePreset === 'today'
						? 'border border-primary/20 bg-primary/10 text-primary'
						: 'cursor-pointer border border-hairline bg-canvas text-body hover:bg-canvas-soft hover:text-ink'}"
				>
					Today
				</button>
				<button
					onclick={() => handlePresetChange('weekend')}
					class="rounded-full px-3 py-1.5 text-xs font-bold transition-all {datePreset === 'weekend'
						? 'border border-primary/20 bg-primary/10 text-primary'
						: 'cursor-pointer border border-hairline bg-canvas text-body hover:bg-canvas-soft hover:text-ink'}"
				>
					This Weekend
				</button>
				<button
					onclick={() => handlePresetChange('custom')}
					class="rounded-full px-3 py-1.5 text-xs font-bold transition-all {datePreset === 'custom'
						? 'border border-primary/20 bg-primary/10 text-primary'
						: 'cursor-pointer border border-hairline bg-canvas text-body hover:bg-canvas-soft hover:text-ink'}"
				>
					Custom Range...
				</button>
			</div>

			<!-- Clear action -->
			{#if data.query || data.location || data.startDate || data.endDate}
				<button
					onclick={clearAll}
					class="hover:text-error-deep cursor-pointer text-xs font-bold text-error transition-colors"
				>
					Clear All Filters
				</button>
			{/if}
		</div>

		<!-- Custom date picker range panel -->
		{#if datePreset === 'custom'}
			<div
				class="animate-fadeIn grid max-w-md grid-cols-1 gap-4 rounded-lg border border-hairline bg-canvas-soft p-4 sm:grid-cols-2"
			>
				<div>
					<label
						class="mb-1.5 block font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
						for="start-date-input">Start Date</label
					>
					<input
						type="date"
						id="start-date-input"
						bind:value={customStartDate}
						onchange={applyFilters}
						class="h-10 w-full rounded border border-hairline bg-canvas px-3 text-xs focus:border-primary focus:outline-none"
					/>
				</div>
				<div>
					<label
						class="mb-1.5 block font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
						for="end-date-input">End Date</label
					>
					<input
						type="date"
						id="end-date-input"
						bind:value={customEndDate}
						onchange={applyFilters}
						class="h-10 w-full rounded border border-hairline bg-canvas px-3 text-xs focus:border-primary focus:outline-none"
					/>
				</div>
			</div>
		{/if}
	</section>

	<!-- SEARCH RESULT DATA -->
	<section class="space-y-4">
		<!-- Query Context Label -->
		<div class="flex items-center justify-between">
			<span class="text-xs font-semibold text-mute">
				{#if data.totalResults > 0}
					Found {data.totalResults} result{data.totalResults !== 1 ? 's' : ''}
					{#if data.query}for "<span class="font-bold text-ink">{data.query}</span>"{/if}
					{#if data.location}in <span class="font-bold text-ink">{data.location}</span>{/if}
				{:else}
					No listings matched your criteria
				{/if}
			</span>
		</div>

		{#if data.results.length > 0}
			<div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
				{#each data.results as event}
					<EventCard {event} />
				{/each}
			</div>

			<!-- Pagination -->
			<PaginationBar currentPage={data.currentPage} totalPages={data.totalPages} />
		{:else}
			<EmptyState
				title="No results found"
				message="We couldn't find any events that match your search filters. Try clearing some criteria or search for a different artist."
				actionText="Show All Upcoming Events"
				actionHref="/search"
			/>
		{/if}
	</section>
</div>
