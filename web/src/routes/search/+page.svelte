<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import EventListCard from '$lib/components/catalog/EventListCard.svelte';
	import PaginationBar from '$lib/components/catalog/PaginationBar.svelte';
	import EmptyState from '$lib/components/common/EmptyState.svelte';
	import HorizontalScroll from '$lib/components/catalog/HorizontalScroll.svelte';

	let { data } = $props();
</script>

<svelte:head>
	<title>Search Events & Find Tickets — Ticketpeak</title>
	<meta
		name="description"
		content="Search upcoming concerts, sports games, arts, theater, and family attractions on Ticketpeak."
	/>
</svelte:head>

<div class="min-h-screen" style="background:#f6f6f6">
	<div class="mx-auto max-w-[1600px] px-4 py-8 md:px-8">
		<div class="grid grid-cols-12 gap-4">
			<main class="col-span-12 rounded-md border border-hairline bg-white p-5 md:col-span-8">
				{#if data.query && data.suggestions.length > 0}
					<div class="mb-6">
						<div class="mb-2 h-1 w-8 rounded-full bg-ink"></div>
						<h2 class="mb-4 text-lg font-bold tracking-[0.1em] text-ink uppercase">
							Top Suggestions
						</h2>
						<HorizontalScroll>
							{#each data.suggestions as suggestion}
								<a
									href={`/search?q=${encodeURIComponent(suggestion.name)}`}
									class="group flex w-44 shrink-0 snap-start flex-col gap-2.5"
								>
									<div class="aspect-[4/3] w-full overflow-hidden rounded-md bg-canvas-soft">
										<img
											src={suggestion.imageUrl || '/placeholder-event.jpg'}
											alt={suggestion.name}
											class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
										/>
									</div>
									<div>
										<span class="block text-[11px] font-bold tracking-[0.1em] text-mute uppercase">
											{suggestion.type || 'Attraction'}
										</span>
										<span class="block truncate text-sm font-semibold text-ink"
											>{suggestion.name}</span
										>
									</div>
								</a>
							{/each}
						</HorizontalScroll>
					</div>
				{/if}

				<div class="mb-6 border-b border-hairline pb-4">
					<div class="mb-2 h-1 w-8 rounded-full bg-ink"></div>
					<h1 class="flex items-center gap-1 text-lg font-bold tracking-[0.1em] text-ink uppercase">
						<span>Events</span>
						<span class="mx-1.5 text-3xl leading-none font-black tracking-normal">&middot;</span>
						<span class="font-normal tracking-normal"
							>{data.totalResults} Result{data.totalResults !== 1 ? 's' : ''}</span
						>
					</h1>
				</div>

				{#if data.results.length > 0}
					<div class="flex flex-col border-t border-hairline">
						{#each data.results as event}
							<EventListCard {event} />
						{/each}
					</div>

					<div class="mt-8">
						<PaginationBar currentPage={data.currentPage} totalPages={data.totalPages} />
					</div>
				{:else}
					<EmptyState
						title="No results found"
						message="We couldn't find any events that match your search filters. Try searching for a different artist."
						actionText="Show All Upcoming Events"
						actionHref="/search"
					/>
				{/if}
			</main>

			<aside class="col-span-12 rounded-md border border-hairline bg-white p-5 md:col-span-4">
				<h3 class="mb-3 text-sm font-bold text-ink uppercase">Advertisement</h3>
				<p class="text-sm text-mute">Ad space coming soon</p>
			</aside>
		</div>
	</div>
</div>
