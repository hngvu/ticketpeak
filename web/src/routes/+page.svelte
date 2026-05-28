<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable svelte/require-each-key */
	import EventCard from '$lib/components/catalog/EventCard.svelte';
	import VenueCard from '$lib/components/catalog/VenueCard.svelte';
	import HorizontalScroll from '$lib/components/catalog/HorizontalScroll.svelte';

	let { data } = $props();

	const heroImage =
		'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=2000&q=80';
	const heroTitle = 'The Wizard of Oz at Sphere';
	const heroSubtitle = 'Experience it like never before';
</script>

<svelte:head>
	<title>Ticketpeak - Discover live events</title>
	<meta
		name="description"
		content="Discover concerts, sports, theater, and venues on Ticketpeak with a Ticketmaster-style layout."
	/>
</svelte:head>

<div class="w-full">
	<section class="relative min-h-[560px] overflow-hidden bg-black">
		<img
			src={heroImage}
			alt={heroTitle}
			class="absolute inset-0 h-full w-full object-cover object-center"
		/>
		<div
			class="absolute inset-0 bg-[linear-gradient(180deg,rgba(0,0,0,0.1)_0%,rgba(0,0,0,0.2)_45%,rgba(0,0,0,0.86)_100%)]"
		></div>

		<div
			class="relative mx-auto flex min-h-[560px] max-w-[1600px] items-end px-4 pb-8 md:px-6 md:pb-10"
		>
			<div class="max-w-xl text-white">
				<span
					class="inline-flex items-center gap-1 rounded-sm bg-black/70 px-2 py-1 text-[10px] font-bold tracking-[0.12em] text-white uppercase"
				>
					<span class="inline-block h-2 w-2 rounded-full bg-white"></span>
					Promoted
				</span>
				<h1
					class="mt-3 text-3xl leading-none font-extrabold tracking-tight md:text-4xl lg:text-5xl"
				>
					{heroTitle}
				</h1>
				<p class="mt-2 text-sm font-semibold text-white/95 md:text-base">
					{heroSubtitle}
				</p>
				<a
					href="/search"
					class="mt-6 inline-flex h-10 items-center justify-center rounded-sm bg-[#0251d3] px-5 text-sm font-bold text-white transition-colors hover:bg-[#003fb5]"
				>
					Find Tickets
				</a>
			</div>
		</div>
	</section>

	<div class="mx-auto max-w-[1600px] px-4 py-8 md:px-6 lg:py-10">
		{#if data.featuredEvents.length > 0}
			<section class="space-y-4">
				<div class="flex items-end justify-between border-b border-hairline pb-2.5">
					<h2 class="text-xl font-bold tracking-tight text-ink md:text-2xl">Trending Near You</h2>
					<a href="/search" class="text-xs font-bold text-primary hover:underline">View all</a>
				</div>

				<HorizontalScroll>
					{#each data.featuredEvents as event}
						<div class="w-[280px] shrink-0 snap-start sm:w-[320px]">
							<EventCard {event} />
						</div>
					{/each}
				</HorizontalScroll>
			</section>
		{/if}

		{#if data.featuredVenues.length > 0}
			<section class="mt-8 space-y-4">
				<div class="flex items-end justify-between border-b border-hairline pb-2.5">
					<h2 class="text-xl font-bold tracking-tight text-ink md:text-2xl">Popular Venues</h2>
					<span class="text-xs font-medium text-mute">Explore upcoming shows by venue</span>
				</div>

				<div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
					{#each data.featuredVenues as venue}
						<VenueCard {venue} />
					{/each}
				</div>
			</section>
		{/if}
	</div>
</div>
