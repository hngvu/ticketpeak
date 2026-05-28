<script lang="ts">
	/* eslint-disable svelte/require-each-key */
	import EventCardHorizontal from '$lib/components/catalog/EventCardHorizontal.svelte';
	import PaginationBar from '$lib/components/catalog/PaginationBar.svelte';
	import EmptyState from '$lib/components/common/EmptyState.svelte';

	let { data } = $props();

	let isFollowed = $state(false);

	function toggleFollow() {
		isFollowed = !isFollowed;
	}

	const fallbackAvatar =
		'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=400&q=80';
</script>

<svelte:head>
	<title>{data.attraction.name} Tickets & Tour Dates — Ticketpeak</title>
	<meta
		name="description"
		content="Get upcoming tour schedules and secure tickets for {data.attraction
			.name}. Book online on Ticketpeak."
	/>
</svelte:head>

<div class="space-y-8 pb-16">
	<!-- ARTIST HERO (canvas-soft-2 / premium glass card) -->
	<section
		class="relative overflow-hidden bg-ink px-4 py-12 text-on-primary shadow-md md:px-6 md:py-16"
	>
		<!-- Mesh background accent -->
		<div
			class="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_20%_-20%,rgba(33,133,213,0.35),transparent)]"
		></div>

		<div
			class="relative mx-auto flex max-w-[1400px] flex-col items-center gap-6 md:flex-row md:gap-8"
		>
			<!-- Artist Avatar Image -->
			<div
				class="h-24 w-24 shrink-0 overflow-hidden rounded-full border-2 border-primary/45 bg-canvas-soft shadow-lg md:h-32 md:w-32"
			>
				<img
					src={data.attraction.imageUrl || fallbackAvatar}
					alt={data.attraction.name}
					class="h-full w-full object-cover"
				/>
			</div>

			<!-- Profile Details -->
			<div class="min-w-0 flex-grow space-y-3 text-center md:text-left">
				<div
					class="flex items-center justify-center gap-2 font-mono text-xs font-extrabold tracking-widest text-primary uppercase md:justify-start"
				>
					<span>Attraction</span>
					<span>·</span>
					<span>{data.attraction.type || 'Artist'}</span>
				</div>

				<h1
					class="text-3xl leading-none font-extrabold tracking-tight text-on-primary md:text-4xl lg:text-5xl"
				>
					{data.attraction.name}
				</h1>

				<p class="max-w-2xl text-xs leading-relaxed font-medium text-mute">
					{data.attraction.description ||
						'No description listed for this attraction. Stay tuned for upcoming tours!'}
				</p>
			</div>

			<!-- Interactions Panel -->
			<div class="flex shrink-0 items-center gap-2">
				<button
					onclick={toggleFollow}
					class="inline-flex h-10 cursor-pointer items-center justify-center rounded-full border px-5 text-xs font-semibold transition-all {isFollowed
						? 'border-primary bg-primary text-on-primary'
						: 'border-on-primary/10 bg-canvas/10 text-on-primary hover:border-on-primary/30 hover:bg-canvas/15'}"
				>
					<svg
						class="mr-1.5 h-4 w-4 {isFollowed ? 'fill-current' : 'fill-none'}"
						stroke="currentColor"
						stroke-width="2.5"
						viewBox="0 0 24 24"
						xmlns="http://www.w3.org/2000/svg"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12z"
						/>
					</svg>
					<span>{isFollowed ? 'Following' : 'Add to Favorites'}</span>
				</button>
			</div>
		</div>
	</section>

	<!-- UPCOMING SHOWS -->
	<section class="mx-auto max-w-[1400px] space-y-6 px-4 md:px-6">
		<div class="flex items-center justify-between border-b border-hairline pb-3.5">
			<h2 class="font-mono text-xl font-bold tracking-tight text-ink uppercase">Upcoming Shows</h2>
			<span class="text-xs font-semibold text-mute"
				>{data.totalShows} Schedule{data.totalShows !== 1 ? 's' : ''} listed</span
			>
		</div>

		{#if data.shows.length > 0}
			<div class="max-w-4xl space-y-3.5">
				{#each data.shows as event}
					<EventCardHorizontal {event} />
				{/each}
			</div>

			<!-- Pagination -->
			<PaginationBar currentPage={data.currentPage} totalPages={data.totalPages} />
		{:else}
			<EmptyState
				title="No upcoming dates"
				message="We couldn't find any scheduled events for {data.attraction
					.name} right now. Sign up to get notified as soon as new schedules are published."
				actionText="Discover Other Artists"
				actionHref="/discover"
			/>
		{/if}
	</section>
</div>
