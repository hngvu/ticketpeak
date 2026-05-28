<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable svelte/require-each-key */
	import EventCardHorizontal from '$lib/components/catalog/EventCardHorizontal.svelte';
	import PaginationBar from '$lib/components/catalog/PaginationBar.svelte';
	import EmptyState from '$lib/components/common/EmptyState.svelte';

	let { data } = $props();

	// Tabs state
	let activeTab = $state('events'); // events, info, seating

	const fallbackBanner =
		'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=1200&q=80';
</script>

<svelte:head>
	<title>{data.venue.name} Tickets & Event Schedule — Ticketpeak</title>
	<meta
		name="description"
		content="View seating charts, directions, and buy tickets for all upcoming shows at {data.venue
			.name} on Ticketpeak."
	/>
</svelte:head>

<div class="space-y-8 pb-16">
	<!-- VENUE HERO (banner layout) -->
	<section
		class="relative flex min-h-[220px] items-end overflow-hidden bg-ink text-on-primary shadow-md md:min-h-[280px]"
	>
		<!-- Backdrop banner image with dark overlay -->
		<div class="absolute inset-0">
			<img
				src={data.venue.imageUrl || fallbackBanner}
				alt={data.venue.name}
				class="h-full w-full object-cover opacity-35 blur-xs filter"
			/>
			<div class="absolute inset-0 bg-gradient-to-t from-ink via-ink/65 to-transparent"></div>
		</div>

		<div class="relative mx-auto w-full max-w-[1400px] space-y-4 px-4 pb-8 md:px-6 md:pb-12">
			<!-- Breadcrumbs -->
			<div class="flex items-center gap-1.5 font-mono text-xs font-bold text-primary uppercase">
				<a href="/discover" class="hover:underline">Discover</a>
				<span>/</span>
				<span class="text-on-primary">Venues</span>
			</div>

			<div class="space-y-2.5">
				<span
					class="inline-flex items-center rounded-full bg-primary px-2.5 py-0.5 font-mono text-[10px] font-extrabold tracking-wider text-on-primary uppercase shadow-sm"
				>
					📍 Venue Partner
				</span>
				<h1
					class="text-3xl leading-none font-extrabold tracking-tight text-on-primary md:text-4xl lg:text-5xl"
				>
					{data.venue.name}
				</h1>
				<p class="max-w-xl text-sm font-medium text-on-primary/90">
					{data.venue.address || 'Address not listed'} ·
					<span class="font-bold text-primary">{data.venue.city}</span>
				</p>
			</div>
		</div>
	</section>

	<div class="mx-auto max-w-[1400px] space-y-6 px-4 md:px-6">
		<!-- TAB BAR -->
		<div
			class="flex items-center gap-1 overflow-x-auto border-b border-hairline pb-px"
			style="scrollbar-width: none;"
		>
			<button
				onclick={() => (activeTab = 'events')}
				class="cursor-pointer border-b-2 px-5 py-3 text-xs font-bold tracking-wider whitespace-nowrap uppercase transition-all md:text-sm {activeTab ===
				'events'
					? 'border-primary text-primary'
					: 'border-transparent text-body hover:border-hairline-strong hover:text-ink'}"
			>
				Upcoming Events ({data.totalShows})
			</button>
			<button
				onclick={() => (activeTab = 'info')}
				class="cursor-pointer border-b-2 px-5 py-3 text-xs font-bold tracking-wider whitespace-nowrap uppercase transition-all md:text-sm {activeTab ===
				'info'
					? 'border-primary text-primary'
					: 'border-transparent text-body hover:border-hairline-strong hover:text-ink'}"
			>
				Venue Info & Directions
			</button>
			<button
				onclick={() => (activeTab = 'seating')}
				class="cursor-pointer border-b-2 px-5 py-3 text-xs font-bold tracking-wider whitespace-nowrap uppercase transition-all md:text-sm {activeTab ===
				'seating'
					? 'border-primary text-primary'
					: 'border-transparent text-body hover:border-hairline-strong hover:text-ink'}"
			>
				Interactive Seating Chart
			</button>
		</div>

		<!-- TABS INNER CONTENT -->
		<div class="pt-2">
			<!-- EVENTS TAB -->
			{#if activeTab === 'events'}
				<div class="space-y-6">
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
							title="No scheduled events"
							message="There are no upcoming events listed at {data.venue
								.name} right now. Sign up to get notified as soon as new schedules are published."
							actionText="Explore Trending Shows"
							actionHref="/discover"
						/>
					{/if}
				</div>
			{/if}

			<!-- INFO TAB -->
			{#if activeTab === 'info'}
				<div class="grid max-w-5xl grid-cols-1 gap-6 md:grid-cols-3">
					<!-- Address & Capacity Card -->
					<div class="space-y-3 rounded-lg border border-hairline bg-canvas p-5 shadow-sm">
						<div
							class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-accent-soft text-primary shadow-inner"
						>
							<svg
								class="h-5 w-5"
								fill="none"
								stroke="currentColor"
								stroke-width="2.5"
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
						<h3 class="font-mono text-sm font-bold text-ink uppercase">Location Details</h3>
						<p class="text-xs leading-relaxed text-body">
							<strong>{data.venue.name}</strong><br />
							{data.venue.address || 'Address not listed'}<br />
							{data.venue.city}, Vietnam
						</p>
						{#if data.venue.capacity}
							<div
								class="border-t border-hairline pt-3 font-mono text-[10px] font-bold text-mute uppercase"
							>
								Maximum Seating Capacity: {data.venue.capacity.toLocaleString()} guests
							</div>
						{/if}
					</div>

					<!-- Public Transport Card -->
					<div class="space-y-3 rounded-lg border border-hairline bg-canvas p-5 shadow-sm">
						<div
							class="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 shadow-inner"
						>
							<svg
								class="h-5 w-5"
								fill="none"
								stroke="currentColor"
								stroke-width="2.5"
								viewBox="0 0 24 24"
								xmlns="http://www.w3.org/2000/svg"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									d="M12.25 12.25l-2.003 2.003a3.483 3.483 0 00-.775 1.567l-.586 2.343a.75.75 0 00.918.918l2.343-.586c.604-.151 1.144-.419 1.567-.775l2.003-2.003m-3.458-3.458c.96.6 2.133.518 3.012-.246L19.75 9.75M12.25 12.25L9.75 19.75M19.75 9.75a3.483 3.483 0 00-.775-1.567l-2.343-.586a.75.75 0 00-.918.918l.586 2.343c.151.604.419 1.144.775 1.567l2.003 2.003m-3.458-3.458A3.485 3.485 0 0112 12.25m8.25-3a9 9 0 11-18 0 9 9 0 0118 0z"
								/>
							</svg>
						</div>
						<h3 class="font-mono text-sm font-bold text-ink uppercase">Transit Directions</h3>
						<p class="text-xs leading-relaxed text-body">
							Easily accessible via local metro lines, shuttle buses, and private car-hailing
							networks. Main entry gates open approximately 90 minutes before show start time.
						</p>
					</div>

					<!-- Parking & Security Card -->
					<div class="space-y-3 rounded-lg border border-hairline bg-canvas p-5 shadow-sm">
						<div
							class="flex h-10 w-10 items-center justify-center rounded-full bg-amber-100 text-amber-600 shadow-inner"
						>
							<svg
								class="h-5 w-5"
								fill="none"
								stroke="currentColor"
								stroke-width="2.5"
								viewBox="0 0 24 24"
								xmlns="http://www.w3.org/2000/svg"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.57-.599-3.75A11.952 11.952 0 0112 2.714z"
								/>
							</svg>
						</div>
						<h3 class="font-mono text-sm font-bold text-ink uppercase">Parking & Safety</h3>
						<p class="text-xs leading-relaxed text-body">
							Valet parking and motorcycle parking decks are located adjacent to the South and East
							concourses. Security checkpoints require QR ticket validation before entry.
						</p>
					</div>
				</div>
			{/if}

			<!-- SEATING TAB -->
			{#if activeTab === 'seating'}
				<div
					class="max-w-4xl space-y-6 rounded-lg border border-hairline bg-canvas p-6 text-center shadow-sm"
				>
					<div class="space-y-2">
						<h3 class="font-mono text-base font-bold tracking-tight text-ink uppercase">
							Interactive Stadium Seating Map
						</h3>
						<p class="mx-auto max-w-md text-xs text-mute">
							View seat levels, VIP sections, and general admission standing zones. Click or pinch
							to zoom.
						</p>
					</div>

					<!-- Premium Stylized SVG Seating Map -->
					<div
						class="relative mx-auto flex aspect-[16/10] max-w-2xl items-center justify-center overflow-hidden rounded-lg border border-hairline bg-canvas-soft p-6 shadow-inner"
					>
						<svg
							viewBox="0 0 800 500"
							class="h-full max-h-[450px] w-full"
							xmlns="http://www.w3.org/2000/svg"
						>
							<!-- Main Stage -->
							<rect
								x="250"
								y="30"
								width="300"
								height="70"
								rx="10"
								fill="#303841"
								stroke="#2185d5"
								stroke-width="3"
							/>
							<text
								x="400"
								y="72"
								fill="#ffffff"
								font-family="monospace"
								font-size="16"
								font-weight="bold"
								text-anchor="middle"
								letter-spacing="4">★ STAGE ★</text
							>

							<!-- Standing Zone (General Admission) -->
							<path
								d="M 230 140 L 570 140 L 600 240 L 200 240 Z"
								fill="#2185d5"
								fill-opacity="0.1"
								stroke="#2185d5"
								stroke-dasharray="4 4"
								stroke-width="2"
							/>
							<text
								x="400"
								y="195"
								fill="#2185d5"
								font-family="sans-serif"
								font-size="13"
								font-weight="bold"
								text-anchor="middle">STANDING GA ZONE</text
							>

							<!-- VIP Floor Left / Right -->
							<rect
								x="80"
								y="140"
								width="120"
								height="90"
								rx="6"
								fill="#f59e0b"
								fill-opacity="0.12"
								stroke="#f59e0b"
								stroke-width="2"
							/>
							<text
								x="140"
								y="190"
								fill="#f59e0b"
								font-family="monospace"
								font-size="12"
								font-weight="bold"
								text-anchor="middle">VIP FLOOR L</text
							>

							<rect
								x="600"
								y="140"
								width="120"
								height="90"
								rx="6"
								fill="#f59e0b"
								fill-opacity="0.12"
								stroke="#f59e0b"
								stroke-width="2"
							/>
							<text
								x="660"
								y="190"
								fill="#f59e0b"
								font-family="monospace"
								font-size="12"
								font-weight="bold"
								text-anchor="middle">VIP FLOOR R</text
							>

							<!-- Lower Tier Seats (Semi-Circle) -->
							<path
								d="M 120 270 Q 400 370 680 270"
								fill="none"
								stroke="#3a4750"
								stroke-width="16"
								stroke-linecap="round"
								stroke-dasharray="32 12"
							/>
							<text
								x="400"
								y="340"
								fill="#3a4750"
								font-family="sans-serif"
								font-size="12"
								font-weight="bold"
								text-anchor="middle">LOWER TIER SEATING - CAT 1</text
							>

							<!-- Upper Tier Seats (Semi-Circle) -->
							<path
								d="M 60 310 Q 400 460 740 310"
								fill="none"
								stroke="#7a8894"
								stroke-width="24"
								stroke-linecap="round"
								stroke-dasharray="40 16"
							/>
							<text
								x="400"
								y="420"
								fill="#7a8894"
								font-family="sans-serif"
								font-size="12"
								font-weight="bold"
								text-anchor="middle">UPPER BALCONY - CAT 2</text
							>
						</svg>

						<!-- Seating Legends -->
						<div class="absolute bottom-4 left-4 flex gap-3 font-mono text-[10px] font-bold">
							<div class="flex items-center gap-1.5">
								<span class="bg-opacity-20 h-3 w-3 rounded border border-[#f59e0b] bg-[#f59e0b]"
								></span>
								<span>VIP Floor</span>
							</div>
							<div class="flex items-center gap-1.5">
								<span class="bg-opacity-20 h-3 w-3 rounded border border-[#2185d5] bg-[#2185d5]"
								></span>
								<span>Standing</span>
							</div>
							<div class="flex items-center gap-1.5">
								<span class="h-3 w-3 rounded bg-[#3a4750]"></span>
								<span>CAT 1 Seat</span>
							</div>
							<div class="flex items-center gap-1.5">
								<span class="h-3 w-3 rounded bg-[#7a8894]"></span>
								<span>CAT 2 Seat</span>
							</div>
						</div>
					</div>
				</div>
			{/if}
		</div>
	</div>
</div>
