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
			venueName?: string; // Loaded or passed
			cityName?: string; // Loaded or passed
		};
		// Fallback details if nested objects aren't fully resolved
		venueName?: string;
		cityName?: string;
	}

	let { event, venueName = '', cityName = '' }: EventCardProps = $props();

	// Format start date beautifully
	const formattedDate = $derived(() => {
		if (!event.startAt) return '';
		const d = new Date(event.startAt);

		// e.g. "SAT, JUN 14, 2026"
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

	const dateParts = $derived(formattedDate());

	// Fallback image url based on category or attraction
	const imageSrc = $derived(() => {
		if (event.attractions && event.attractions.length > 0 && event.attractions[0].imageUrl) {
			return event.attractions[0].imageUrl;
		}

		// Fallback placeholder based on event categories
		const firstClass = event.classifications?.[0]?.slug || 'concerts';
		if (firstClass.includes('sport')) {
			return 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=800&q=80';
		} else if (firstClass.includes('art') || firstClass.includes('theater')) {
			return 'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=800&q=80';
		} else if (firstClass.includes('family')) {
			return 'https://images.unsplash.com/photo-1472712739516-7ad2b786e1f7?auto=format&fit=crop&w=800&q=80';
		}

		// Default Concerts image
		return 'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=800&q=80';
	});

	const displayVenue = $derived(venueName || event.venueName || 'Venue TBD');
	const displayCity = $derived(cityName || event.cityName || 'City TBD');
</script>

<a
	href="/{event.slug}/event/{encodeUuidToBase62(event.id)}"
	class="group flex h-full transform flex-col overflow-hidden rounded-lg border border-hairline bg-canvas shadow-sm transition-all duration-300 hover:-translate-y-1 hover:border-hairline-strong hover:shadow-md"
>
	<!-- Thumbnail Aspect 16:9 -->
	<div class="relative aspect-[16/9] w-full overflow-hidden bg-canvas-soft">
		<img
			src={imageSrc()}
			alt={event.title}
			loading="lazy"
			class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
		/>

		<!-- Genre/Category Badge -->
		{#if event.classifications && event.classifications.length > 0}
			<span
				class="absolute top-3 left-3 rounded-full border border-hairline bg-canvas/80 px-2 py-0.5 text-[10px] font-bold tracking-wider text-primary uppercase shadow-sm backdrop-blur-md"
			>
				{event.classifications[0].name}
			</span>
		{/if}

		<!-- Absolute Date Overlay for high impact -->
		{#if dateParts}
			<div
				class="absolute right-3 bottom-3 flex items-center gap-1.5 rounded bg-ink/90 px-2.5 py-1 text-center text-on-primary shadow-md backdrop-blur-sm"
			>
				<span class="font-mono text-xs font-bold text-primary">{dateParts.weekday}</span>
				<span class="h-3 w-px bg-mute/40"></span>
				<span class="font-mono text-[10px] font-medium text-on-primary/95"
					>{dateParts.month} {dateParts.day}</span
				>
			</div>
		{/if}
	</div>

	<!-- Info Details -->
	<div class="flex flex-grow flex-col p-4 md:p-5">
		<!-- Date / Time Metadata -->
		{#if dateParts}
			<div
				class="mb-1.5 flex items-center gap-1.5 font-mono text-[10px] font-bold tracking-wider text-primary uppercase"
			>
				<span>{dateParts.weekday}, {dateParts.month} {dateParts.day}</span>
				<span>·</span>
				<span>{dateParts.time}</span>
			</div>
		{/if}

		<!-- Event Title -->
		<h3
			class="mb-2 line-clamp-2 flex-grow text-base leading-snug font-bold tracking-tight text-ink transition-colors group-hover:text-primary"
		>
			{event.title}
		</h3>

		<!-- Venue and City -->
		<div class="mb-4 flex items-start gap-1.5 text-xs font-medium text-mute">
			<svg
				class="mt-0.5 h-3.5 w-3.5 shrink-0 text-mute/80"
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
			<span class="truncate"
				>{displayVenue} · <span class="font-semibold text-body">{displayCity}</span></span
			>
		</div>

		<!-- Action CTA -->
		<div class="mt-auto flex w-full items-center justify-between border-t border-hairline pt-3">
			<span class="font-mono text-xs font-semibold text-body"
				>From <span class="text-sm font-bold text-ink">$25.00</span></span
			>
			<span
				class="inline-flex items-center gap-1 text-xs font-bold text-primary group-hover:underline"
			>
				<span>Get Tickets</span>
				<svg
					class="h-3 w-3 transition-transform duration-200 group-hover:translate-x-0.5"
					fill="none"
					stroke="currentColor"
					stroke-width="2.5"
					viewBox="0 0 24 24"
					xmlns="http://www.w3.org/2000/svg"
				>
					<path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
				</svg>
			</span>
		</div>
	</div>
</a>
