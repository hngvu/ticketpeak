<script lang="ts">
	import AttractionHero from '$lib/components/catalog/AttractionHero.svelte';
	import EventAccordionRow from '$lib/components/catalog/EventAccordionRow.svelte';
	import LocationFilterDropdown from '$lib/components/catalog/LocationFilterDropdown.svelte';
	import AttractionBio from '$lib/components/catalog/AttractionBio.svelte';
	import EmptyState from '$lib/components/common/EmptyState.svelte';
	import { encodeUuidToBase62 } from '$lib/base62';

	let { data } = $props();

	// Reactive states matching Plan 021 spec
	let expandedEventId = $state<string | null>(null);
	let locationFilter = $state<string>('all');
	let events = $state(data.initialEvents);
	let pageNum = $state(0);
	let loading = $state(false);

	// Derived values for locations filter
	const uniqueCities = $derived([
		...new Set(events.map((e: { cityName?: string }) => e.cityName).filter(Boolean))
	] as string[]);

	const filteredEvents = $derived(
		locationFilter === 'all'
			? events
			: events.filter((e: { cityName?: string }) => e.cityName === locationFilter)
	);

	const totalEventsCount = $derived(
		locationFilter === 'all' ? data.totalEvents : filteredEvents.length
	);

	const showLoadMoreButton = $derived(
		locationFilter === 'all' && pageNum < data.totalPages - 1 && events.length > 0
	);

	// Load more action supporting dynamic API and graceful mock pagination fallback
	async function loadMore() {
		if (loading) return;
		loading = true;

		try {
			const res = await fetch(
				`/api/events?attractionId=${data.attraction.id}&page=${pageNum + 1}&size=10&sort=startAt,asc`
			);
			if (res.ok) {
				const json = await res.json();
				if (json.success && json.data?.content) {
					events = [...events, ...json.data.content];
					pageNum++;
				}
			}
		} catch (err) {
			console.error('[ADP CLIENT LOAD MORE ERROR]:', err);
		} finally {
			loading = false;
		}
	}

	function handleToggleRow(eventId: string) {
		if (expandedEventId === eventId) {
			expandedEventId = null;
		} else {
			expandedEventId = eventId;
		}
	}
</script>

<svelte:head>
	<title>{data.attraction.name} Tickets & Tour Dates | Ticketpeak</title>
	<meta
		name="description"
		content="Find {data.attraction
			.name} tickets for upcoming concerts and events. Browse all tour dates on Ticketpeak."
	/>
	<link rel="canonical" href="/{data.slug}/artist/{encodeUuidToBase62(data.attraction.id)}" />
</svelte:head>

<div class="min-h-screen space-y-8 bg-canvas-soft pb-16">
	<!-- 1. Artist Hero Section -->
	<AttractionHero attraction={data.attraction} />

	<!-- Content Bounds -->
	<div class="mx-auto grid max-w-[1400px] grid-cols-1 gap-8 px-4 md:px-6 lg:grid-cols-[1fr_380px]">
		<!-- Left: Events accordion list -->
		<div class="space-y-6">
			<!-- Header filter row -->
			<div class="flex items-center justify-between border-b border-hairline pb-4 select-none">
				<div>
					<h2 class="font-sans text-xl font-bold tracking-tight text-ink md:text-2xl">
						{totalEventsCount} Event{totalEventsCount !== 1 ? 's' : ''}
					</h2>
					{#if locationFilter !== 'all'}
						<p class="mt-1 text-xs font-medium text-mute">
							Filtered near <span class="font-bold text-ink">{locationFilter}</span>
						</p>
					{/if}
				</div>

				<!-- Location city filter dropdown -->
				{#if uniqueCities.length > 0}
					<LocationFilterDropdown cities={uniqueCities} bind:selectedLocation={locationFilter} />
				{/if}
			</div>

			<!-- Accordion list -->
			{#if filteredEvents.length > 0}
				<div
					class="divide-y divide-hairline overflow-hidden rounded-xl border border-hairline bg-canvas shadow-2xs"
				>
					{#each filteredEvents as event (event.id)}
						<EventAccordionRow
							{event}
							isExpanded={expandedEventId === event.id}
							onToggle={() => handleToggleRow(event.id)}
						/>
					{/each}
				</div>

				<!-- Load more button -->
				{#if showLoadMoreButton}
					<div class="flex justify-center pt-4 select-none">
						<button
							type="button"
							onclick={loadMore}
							disabled={loading}
							class="inline-flex h-11 cursor-pointer items-center justify-center gap-2 rounded-full border border-hairline bg-canvas px-8 text-sm font-bold text-slate-800 shadow-xs hover:bg-canvas-soft active:scale-[0.98] disabled:opacity-50"
						>
							{#if loading}
								<svg
									class="h-4.5 w-4.5 animate-spin text-gray-500"
									xmlns="http://www.w3.org/2000/svg"
									fill="none"
									viewBox="0 0 24 24"
								>
									<circle
										class="opacity-25"
										cx="12"
										cy="12"
										r="10"
										stroke="currentColor"
										stroke-width="4"
									></circle>
									<path
										class="opacity-75"
										fill="currentColor"
										d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
									></path>
								</svg>
							{/if}
							<span>Load More Events</span>
						</button>
					</div>
				{/if}
			{:else}
				<EmptyState
					title="No upcoming events"
					message="We couldn't find any events scheduled for this location. Check back later or browse other trending experiences near you."
					actionText="Browse Trending Experiences"
					actionHref="/discover"
				/>
			{/if}
		</div>

		<!-- Right: Biography section -->
		<div class="space-y-6 self-start lg:sticky lg:top-8">
			<AttractionBio name={data.attraction.name} description={data.attraction.description} />
		</div>
	</div>
</div>
