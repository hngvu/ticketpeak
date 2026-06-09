<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { encodeUuidToBase62 } from '$lib/base62';

	export interface EventCardProps {
		event: {
			id: string;
			title: string;
			slug: string;
			startAt: string;
			classifications?: Array<{ name: string; slug: string }>;
			attractions?: Array<{ name: string; imageUrl?: string }>;
			venueName?: string;
			cityName?: string;
		};
		venueName?: string;
		cityName?: string;
	}

	let { event, venueName = '', cityName = '' }: EventCardProps = $props();

	// Format date beautifully
	const dateParts = $derived(() => {
		if (!event.startAt) return null;
		const d = new Date(event.startAt);

		const weekday = d.toLocaleDateString('en-US', { weekday: 'short' }).toUpperCase();
		const month = d.toLocaleDateString('en-US', { month: 'short' }).toUpperCase();
		const day = d.getDate();
		const time = d.toLocaleTimeString('en-US', {
			hour: '2-digit',
			minute: '2-digit',
			hour12: false
		});

		return { weekday, month, day, time };
	});

	const parts = $derived(dateParts());

	// Fallback image url
	const imageSrc = $derived(() => {
		if (event.attractions && event.attractions.length > 0 && event.attractions[0].imageUrl) {
			return event.attractions[0].imageUrl;
		}
		const firstClass = event.classifications?.[0]?.slug || 'concerts';
		if (firstClass.includes('sport')) {
			return 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=400&q=80';
		} else if (firstClass.includes('art') || firstClass.includes('theater')) {
			return 'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=400&q=80';
		}
		return 'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=400&q=80';
	});

	const displayVenue = $derived(venueName || event.venueName || 'Venue TBD');
	const displayCity = $derived(cityName || event.cityName || 'City TBD');
</script>

<div
	class="group flex flex-col items-center justify-between gap-4 overflow-hidden rounded-lg border border-hairline bg-canvas p-4 shadow-sm transition-all duration-300 hover:border-hairline-strong hover:shadow-md sm:flex-row"
>
	<div class="flex w-full items-center gap-4 sm:w-auto">
		<!-- Date block (high visibility) -->
		{#if parts}
			<div
				class="flex w-16 shrink-0 flex-col items-center justify-center rounded border border-hairline bg-canvas-soft px-3 py-2 text-center"
			>
				<span class="font-mono text-[10px] font-extrabold tracking-wider text-primary"
					>{parts.month}</span
				>
				<span class="my-0.5 font-mono text-lg leading-none font-black text-ink">{parts.day}</span>
				<span class="font-mono text-[9px] font-bold text-mute">{parts.weekday}</span>
			</div>
		{/if}

		<!-- Thumbnail -->
		<div class="hidden h-16 w-16 shrink-0 overflow-hidden rounded bg-canvas-soft md:block">
			<img
				src={imageSrc()}
				alt={event.title}
				loading="lazy"
				class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
			/>
		</div>

		<!-- Title and details -->
		<div class="min-w-0 flex-grow space-y-1">
			<h3
				class="line-clamp-1 text-sm leading-snug font-bold tracking-tight text-ink transition-colors group-hover:text-primary md:text-base"
			>
				<a href="/{event.slug}/event/{encodeUuidToBase62(event.id)}">{event.title}</a>
			</h3>
			<div class="flex flex-wrap items-center gap-x-2 gap-y-1 text-xs font-medium text-mute">
				<span>{parts?.time || ''}</span>
				<span class="hidden text-hairline-strong sm:inline">·</span>
				<span class="truncate"
					>{displayVenue} · <span class="font-semibold text-body">{displayCity}</span></span
				>
			</div>

			{#if event.classifications && event.classifications.length > 0}
				<div
					class="bg-blue-accent-soft inline-flex items-center rounded-full px-2 py-0.5 text-[9px] font-bold tracking-wider text-primary uppercase"
				>
					{event.classifications[0].name}
				</div>
			{/if}
		</div>
	</div>

	<!-- Tickets CTA -->
	<div
		class="mt-2 flex w-full shrink-0 items-center justify-between gap-6 border-t border-hairline pt-3 sm:mt-0 sm:w-auto sm:justify-end sm:border-t-0 sm:pt-0"
	>
		<div class="text-left sm:text-right">
			<span class="block font-mono text-[10px] font-semibold tracking-wider text-mute uppercase"
				>Prices from</span
			>
			<span class="font-mono text-sm font-bold text-ink">$25.00</span>
		</div>
		<a
			href="/{event.slug}/event/{encodeUuidToBase62(event.id)}"
			class="inline-flex h-9 cursor-pointer items-center justify-center rounded-full bg-primary px-5 text-xs font-semibold text-on-primary shadow-sm transition-colors group-hover:shadow hover:bg-primary/95"
		>
			<span>See Tickets</span>
			<svg
				class="ml-1 h-3.5 w-3.5 transition-transform duration-200 group-hover:translate-x-0.5"
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
</div>
