<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable svelte/require-each-key */
	import EventCard from '$lib/components/catalog/EventCard.svelte';
	import VenueCard from '$lib/components/catalog/VenueCard.svelte';
	import HorizontalScroll from '$lib/components/catalog/HorizontalScroll.svelte';

	let { data } = $props();
</script>

<svelte:head>
	<title>Discover Live Experiences & Tickets — Ticketpeak</title>
	<meta
		name="description"
		content="Discover and book tickets for the best concerts, sporting events, arts, theater, and family attractions near you on Ticketpeak."
	/>
</svelte:head>

<div class="space-y-12 pb-16">
	<!-- HERO BAND (gradient-discover) -->
	<section
		class="from-gradient-discover-start to-gradient-discover-end relative overflow-hidden bg-gradient-to-r px-4 py-16 text-on-primary shadow-md md:px-6 md:py-24"
	>
		<!-- Aesthetic mesh effects -->
		<div
			class="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_70%_120%,rgba(255,255,255,0.15),transparent)]"
		></div>

		<div class="relative mx-auto max-w-[1400px] space-y-6 text-center md:space-y-8">
			<!-- Title Display XL -->
			<h1
				class="mx-auto max-w-4xl text-4xl leading-none font-extrabold tracking-tight text-on-primary md:text-5xl lg:text-6xl"
			>
				Find your next <br class="sm:hidden" /><span
					class="inline-block rounded-lg bg-canvas px-4 py-1 text-ink sm:inline sm:bg-transparent sm:p-0 sm:text-on-primary"
					>live experience.</span
				>
			</h1>
			<p class="mx-auto max-w-xl text-base font-medium text-on-primary/90 md:text-lg">
				Discover, book, and scan secure tickets for concerts, sports, theater, and local
				entertainment.
			</p>

			<!-- Category Pills Quick Navigation -->
			<div class="flex flex-wrap items-center justify-center gap-2 pt-2">
				<a
					href="/discover/concerts"
					class="rounded-full border border-on-primary/20 bg-canvas/15 px-5 py-2.5 text-xs font-bold backdrop-blur-sm transition-all hover:border-on-primary/45 hover:bg-canvas/25"
				>
					🎸 Concerts
				</a>
				<a
					href="/discover/sports"
					class="rounded-full border border-on-primary/20 bg-canvas/15 px-5 py-2.5 text-xs font-bold backdrop-blur-sm transition-all hover:border-on-primary/45 hover:bg-canvas/25"
				>
					⚽ Sports
				</a>
				<a
					href="/discover/arts"
					class="rounded-full border border-on-primary/20 bg-canvas/15 px-5 py-2.5 text-xs font-bold backdrop-blur-sm transition-all hover:border-on-primary/45 hover:bg-canvas/25"
				>
					🎭 Arts & Theater
				</a>
				<a
					href="/discover/family"
					class="rounded-full border border-on-primary/20 bg-canvas/15 px-5 py-2.5 text-xs font-bold backdrop-blur-sm transition-all hover:border-on-primary/45 hover:bg-canvas/25"
				>
					🍿 Family
				</a>
			</div>
		</div>
	</section>

	<!-- CONTENT BOUNDS -->
	<div class="mx-auto max-w-[1400px] space-y-12 px-4 md:px-6">
		<!-- SECTION: Trending Near You -->
		{#if data.trendingEvents.length > 0}
			<section class="space-y-4">
				<div class="flex items-end justify-between border-b border-hairline pb-2.5">
					<h2 class="text-xl font-bold tracking-tight text-ink md:text-2xl">Trending Near You</h2>
					<a
						href="/search"
						class="flex items-center gap-0.5 text-xs font-bold text-primary hover:underline"
					>
						<span>View all</span>
						<svg
							class="h-3 w-3"
							fill="none"
							stroke="currentColor"
							stroke-width="2.5"
							viewBox="0 0 24 24"
							xmlns="http://www.w3.org/2000/svg"
						>
							<path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
						</svg>
					</a>
				</div>

				<HorizontalScroll>
					{#each data.trendingEvents as event}
						<div class="w-[280px] shrink-0 snap-start sm:w-[320px]">
							<EventCard {event} />
						</div>
					{/each}
				</HorizontalScroll>
			</section>
		{/if}

		<!-- SECTION: This Weekend -->
		{#if data.weekendEvents.length > 0}
			<section class="space-y-4">
				<div class="flex items-end justify-between border-b border-hairline pb-2.5">
					<h2 class="text-xl font-bold tracking-tight text-ink md:text-2xl">This Weekend</h2>
					<a
						href="/search"
						class="flex items-center gap-0.5 text-xs font-bold text-primary hover:underline"
					>
						<span>View all</span>
						<svg
							class="h-3 w-3"
							fill="none"
							stroke="currentColor"
							stroke-width="2.5"
							viewBox="0 0 24 24"
							xmlns="http://www.w3.org/2000/svg"
						>
							<path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
						</svg>
					</a>
				</div>

				<HorizontalScroll>
					{#each data.weekendEvents as event}
						<div class="w-[280px] shrink-0 snap-start sm:w-[320px]">
							<EventCard {event} />
						</div>
					{/each}
				</HorizontalScroll>
			</section>
		{/if}

		<!-- SECTION: Local Concerts -->
		{#if data.concertEvents.length > 0}
			<section class="space-y-4">
				<div class="flex items-end justify-between border-b border-hairline pb-2.5">
					<h2 class="text-xl font-bold tracking-tight text-ink md:text-2xl">Top Concerts</h2>
					<a
						href="/discover/concerts"
						class="flex items-center gap-0.5 text-xs font-bold text-primary hover:underline"
					>
						<span>Browse concerts</span>
						<svg
							class="h-3 w-3"
							fill="none"
							stroke="currentColor"
							stroke-width="2.5"
							viewBox="0 0 24 24"
							xmlns="http://www.w3.org/2000/svg"
						>
							<path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
						</svg>
					</a>
				</div>

				<HorizontalScroll>
					{#each data.concertEvents as event}
						<div class="w-[280px] shrink-0 snap-start sm:w-[320px]">
							<EventCard {event} />
						</div>
					{/each}
				</HorizontalScroll>
			</section>
		{/if}

		<!-- SECTION: Popular Venues -->
		{#if data.venues.length > 0}
			<section class="space-y-4">
				<div class="flex items-end justify-between border-b border-hairline pb-2.5">
					<h2 class="text-xl font-bold tracking-tight text-ink md:text-2xl">Popular Venues</h2>
					<span class="text-xs font-medium text-mute">Explore upcoming events by venue</span>
				</div>

				<div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
					{#each data.venues as venue}
						<VenueCard {venue} />
					{/each}
				</div>
			</section>
		{/if}
	</div>
</div>
