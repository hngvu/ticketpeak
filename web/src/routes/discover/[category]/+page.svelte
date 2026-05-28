<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable svelte/require-each-key */
	/* eslint-disable svelte/prefer-svelte-reactivity */
	import EventCard from '$lib/components/catalog/EventCard.svelte';
	import PaginationBar from '$lib/components/catalog/PaginationBar.svelte';
	import EmptyState from '$lib/components/common/EmptyState.svelte';
	import { page } from '$app/state';

	let { data } = $props();

	// Capitalize category name for display
	const displayCategoryName = $derived(
		data.category.charAt(0).toUpperCase() + data.category.slice(1)
	);

	// Category emojis/icons for premium hero aesthetics
	const heroStyles = $derived(() => {
		switch (data.category) {
			case 'concerts':
				return {
					emoji: '🎸',
					subtitle: 'Live music, festivals, global tours, and intimate gigs.',
					gradient: 'from-blue-600 to-indigo-500'
				};
			case 'sports':
				return {
					emoji: '⚽',
					subtitle: 'Football, basketball, tennis, races, and championship events.',
					gradient: 'from-emerald-600 to-teal-500'
				};
			case 'arts':
				return {
					emoji: '🎭',
					subtitle: 'Broadway musicals, plays, opera, stand-up comedy, and dance.',
					gradient: 'from-purple-600 to-pink-500'
				};
			case 'family':
				return {
					emoji: '🍿',
					subtitle: 'Fun family outings, theme parks, magic shows, and workshops.',
					gradient: 'from-amber-500 to-orange-500'
				};
			default:
				return {
					emoji: '🎉',
					subtitle: 'Discover amazing live activities and upcoming tickets.',
					gradient: 'from-primary to-blue-400'
				};
		}
	});

	const styles = $derived(heroStyles());

	// Construct subcategory link URL
	function getSubGenreUrl(genreId: string) {
		const newParams = new URLSearchParams(page.url.searchParams);
		newParams.delete('page'); // Reset to page 1 on filter change
		if (!genreId) {
			newParams.delete('classificationId');
		} else {
			newParams.set('classificationId', genreId);
		}
		const query = newParams.toString();
		return `${page.url.pathname}${query ? '?' + query : ''}`;
	}
</script>

<svelte:head>
	<title>Browse {displayCategoryName} Tickets & Events — Ticketpeak</title>
	<meta
		name="description"
		content="Find upcoming {data.category} tickets and event schedules. Book online securely on Ticketpeak."
	/>
</svelte:head>

<div class="space-y-8 pb-16">
	<!-- PAGE HERO (canvas-soft-2 / custom gradient) -->
	<section
		class="bg-gradient-to-r {styles.gradient} px-4 py-12 text-on-primary shadow-sm md:px-6 md:py-16"
	>
		<div
			class="mx-auto flex max-w-[1400px] flex-col justify-between gap-6 md:flex-row md:items-center"
		>
			<div>
				<!-- Breadcrumbs -->
				<div
					class="mb-2 flex items-center gap-1.5 font-mono text-xs font-semibold text-on-primary/75 uppercase"
				>
					<a href="/discover" class="transition-colors hover:text-on-primary">Discover</a>
					<span>/</span>
					<span class="font-bold text-on-primary">{data.category}</span>
				</div>
				<!-- Heading Display LG -->
				<h1 class="mb-2 flex items-center gap-2 text-3xl font-extrabold tracking-tight md:text-4xl">
					<span class="text-2xl md:text-3xl">{styles.emoji}</span>
					<span>{displayCategoryName}</span>
				</h1>
				<p class="max-w-xl text-sm font-medium text-on-primary/95">
					{styles.subtitle}
				</p>
			</div>

			<!-- Quick Info details -->
			{#if data.events.length > 0}
				<div
					class="shrink-0 space-y-1 self-start rounded-lg border border-on-primary/10 bg-canvas/10 p-4 font-mono text-xs font-semibold text-on-primary/90 backdrop-blur-md md:self-auto"
				>
					<div>City context: {page.url.searchParams.get('city') || 'Preferred City'}</div>
					<div>Shows found: {data.events.length} listings</div>
				</div>
			{/if}
		</div>
	</section>

	<div class="mx-auto max-w-[1400px] space-y-8 px-4 md:px-6">
		<!-- GENRE PILLS (horizontal list) -->
		{#if data.subGenres.length > 0}
			<div class="space-y-3">
				<span class="font-mono text-xs font-bold tracking-wider text-ink uppercase"
					>Filter by Genre</span
				>
				<div
					class="-mx-4 flex items-center gap-2 overflow-x-auto px-4 pb-2 sm:mx-0 sm:px-0"
					style="scrollbar-width: none;"
				>
					<!-- All Category Pill -->
					<a
						href={getSubGenreUrl('')}
						class="shrink-0 rounded-full border px-4 py-2 text-xs font-bold transition-all {!data.classificationId
							? 'border-primary bg-primary text-on-primary shadow-sm'
							: 'border-hairline bg-canvas text-body hover:bg-canvas-soft hover:text-ink'}"
					>
						All {displayCategoryName}
					</a>

					<!-- Genre Specific Pills -->
					{#each data.subGenres as genre}
						<a
							href={getSubGenreUrl(genre.id)}
							class="shrink-0 rounded-full border px-4 py-2 text-xs font-bold transition-all {data.classificationId ===
							genre.id
								? 'border-primary bg-primary text-on-primary shadow-sm'
								: 'border-hairline bg-canvas text-body hover:bg-canvas-soft hover:text-ink'}"
						>
							{genre.name}
						</a>
					{/each}
				</div>
			</div>
		{/if}

		<!-- EVENT GRID -->
		{#if data.events.length > 0}
			<div class="space-y-4">
				<div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
					{#each data.events as event}
						<EventCard {event} />
					{/each}
				</div>

				<!-- Pagination -->
				<PaginationBar currentPage={data.currentPage} totalPages={data.totalPages} />
			</div>
		{:else}
			<EmptyState
				title="No matches in {displayCategoryName}"
				message="We couldn't find any upcoming {data.category} events for this filter. Browse our trending feed to see what else is happening."
				actionHref="/discover"
				actionText="Browse Trending Experiences"
			/>
		{/if}
	</div>
</div>
