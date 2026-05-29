<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	let {
		event,
		isExpanded = false,
		onToggle
	} = $props<{
		event: any;
		isExpanded: boolean;
		onToggle: () => void;
	}>();

	// Parse date details dynamically
	const startDate = $derived(new Date(event.startAt));
	const weekday = $derived(
		startDate.toLocaleDateString('en-US', { weekday: 'short' }).toUpperCase()
	);
	const dayNum = $derived(startDate.getDate());
	const month = $derived(startDate.toLocaleDateString('en-US', { month: 'short' }).toUpperCase());
	const timeLabel = $derived(
		startDate.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })
	);
	const formattedFullDate = $derived(
		startDate
			.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })
			.toUpperCase()
	);

	// Generate price display if not available (Standard ADP pricing simulation)
	const priceLabel = $derived(
		event.priceRange ||
			(event.id === 'event-taylor-swift-hn'
				? '$120 - $450'
				: event.id === 'event-sontung-hcm'
					? '$45 - $180'
					: event.id === 'event-denvau-hn'
						? '$35 - $120'
						: '$25 - $90')
	);
</script>

<div class="border-b border-hairline bg-canvas transition-colors duration-200">
	<!-- Collapsed Header Row -->
	<button
		type="button"
		onclick={onToggle}
		class="group flex w-full cursor-pointer items-center gap-4 px-4 py-4.5 text-left transition hover:bg-canvas-soft/50"
	>
		<!-- 1. Date block (Fixed 56px) -->
		<div
			class="flex h-14 w-14 shrink-0 flex-col items-center justify-center rounded-md border border-hairline bg-canvas-soft/70 font-mono shadow-2xs select-none"
		>
			<span class="text-[10px] font-bold tracking-wider text-primary">{weekday}</span>
			<span class="py-0.5 text-xl leading-none font-black text-ink">{dayNum}</span>
			<span class="text-[9px] font-bold tracking-wider text-mute">{month}</span>
		</div>

		<!-- 2. Main Title and Venue -->
		<div class="min-w-0 flex-grow pr-2">
			<h4
				class="truncate font-sans text-sm leading-snug font-bold text-ink transition-colors group-hover:text-primary"
			>
				{event.title}
			</h4>
			<p class="mt-1 truncate text-xs font-medium text-mute">
				{event.venueName || 'Venue'} · {event.cityName || 'City'}
			</p>
			{#if event.isExternal}
				<span
					class="mt-1.5 inline-block rounded-full bg-canvas-soft-2 px-2.5 py-0.5 font-mono text-[9px] font-bold text-mute uppercase"
				>
					On partner site
				</span>
			{/if}
		</div>

		<!-- 3. Actions / CTA -->
		<div class="flex shrink-0 items-center gap-4">
			<a
				href="/discover"
				onclick={(e) => {
					e.stopPropagation();
					// In a real purchase journey, this links to the ticketing page
				}}
				class="hover:bg-primary-hover hidden h-9 min-w-[110px] cursor-pointer items-center justify-center rounded-full bg-primary px-5 text-xs font-bold text-white shadow-xs transition active:scale-[0.98] sm:inline-flex"
			>
				Find Tickets
			</a>

			<!-- Chevron icon toggling -->
			<div class="pr-1 text-mute transition-colors group-hover:text-ink">
				<svg
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2.5"
					class="h-4.5 w-4.5 transition-transform duration-200 {isExpanded ? 'rotate-180' : ''}"
				>
					<path d="m6 9 6 6 6-6" />
				</svg>
			</div>
		</div>
	</button>

	<!-- Expanded Details Panel -->
	{#if isExpanded}
		<div
			class="grid grid-cols-1 items-center gap-5 border-t border-hairline/60 bg-canvas-soft px-6 py-6.5 select-none sm:grid-cols-[auto_1fr_auto]"
		>
			<!-- Image thumbnail -->
			<div class="mx-auto shrink-0 sm:mx-0">
				<div
					class="relative h-20 w-32 overflow-hidden rounded-md border border-hairline shadow-xs md:h-24 md:w-36"
				>
					<img
						src="https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=400&q=80"
						alt={event.title}
						class="h-full w-full object-cover"
					/>
				</div>
			</div>

			<!-- Secondary details -->
			<div class="space-y-1.5 text-center sm:text-left">
				<h5 class="text-sm leading-snug font-extrabold text-ink">{event.title}</h5>
				<div class="space-y-1 text-xs font-semibold text-mute">
					<p class="font-mono text-primary">{formattedFullDate} · {timeLabel}</p>
					<p>{event.venueName || 'UBS Arena'} · {event.cityName || 'City'}</p>
					<p class="font-sans font-bold text-ink">Tickets: {priceLabel}</p>
				</div>
			</div>

			<!-- Expand Find Tickets Button -->
			<div class="w-full shrink-0 self-center sm:w-auto">
				<a
					href="/discover"
					class="hover:bg-primary-hover flex h-11 w-full cursor-pointer items-center justify-center rounded-full bg-primary text-xs font-bold text-white shadow-md transition active:scale-[0.98] sm:w-44"
				>
					Find Tickets →
				</a>
			</div>
		</div>
	{/if}
</div>
