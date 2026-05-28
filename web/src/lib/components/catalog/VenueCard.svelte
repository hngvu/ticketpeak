<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { resolve } from '$app/paths';

	export interface VenueCardProps {
		venue: {
			id: string;
			name: string;
			slug: string;
			city: string;
			address?: string;
			capacity?: number;
			imageUrl?: string;
			eventCount?: number;
		};
	}

	let { venue }: VenueCardProps = $props();

	// Fallback image url for venues
	const imageSrc = $derived(() => {
		if (venue.imageUrl) return venue.imageUrl;

		// Fallbacks
		const names = venue.name.toLowerCase();
		if (names.includes('stadium') || names.includes('sân vận động')) {
			return 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=600&q=80';
		} else if (names.includes('theater') || names.includes('nhà hát') || names.includes('opera')) {
			return 'https://images.unsplash.com/photo-1503095396549-807759245b35?auto=format&fit=crop&w=600&q=80';
		}

		return 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=600&q=80';
	});

	const displayEventCount = $derived(venue.eventCount ?? Math.floor(Math.random() * 5) + 1);
</script>

<a
	href={resolve(`/venues/${venue.id}-${venue.slug}` as any)}
	class="group flex h-full transform flex-col overflow-hidden rounded-lg border border-hairline bg-canvas shadow-sm transition-all duration-300 hover:-translate-y-1 hover:border-hairline-strong hover:shadow-md"
>
	<!-- Thumbnail Aspect 16:9 -->
	<div class="relative aspect-[16/10] w-full overflow-hidden bg-canvas-soft">
		<img
			src={imageSrc()}
			alt={venue.name}
			loading="lazy"
			class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
		/>

		<!-- Total events count badge -->
		<span
			class="absolute right-3 bottom-3 rounded-full bg-primary px-2.5 py-1 font-mono text-[10px] font-bold tracking-wider text-on-primary uppercase shadow-md"
		>
			{displayEventCount} Show{displayEventCount !== 1 ? 's' : ''}
		</span>
	</div>

	<!-- Info Details -->
	<div class="flex flex-grow flex-col p-4 md:p-5">
		<!-- Venue Title -->
		<h3
			class="mb-1 line-clamp-1 text-sm leading-snug font-bold tracking-tight text-ink transition-colors group-hover:text-primary md:text-base"
		>
			{venue.name}
		</h3>

		<!-- City and Address -->
		<p class="mb-3 line-clamp-1 text-xs font-medium text-mute">
			{venue.address || 'Address not listed'} ·
			<span class="font-semibold text-body">{venue.city}</span>
		</p>

		<!-- Capacity / Metadata -->
		{#if venue.capacity}
			<div
				class="mt-auto flex items-center gap-1.5 border-t border-hairline pt-3 font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
			>
				<svg
					class="h-3.5 w-3.5 shrink-0 text-mute/80"
					fill="none"
					stroke="currentColor"
					stroke-width="2"
					viewBox="0 0 24 24"
					xmlns="http://www.w3.org/2000/svg"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.109A11.386 11.386 0 0110.089 20.08l-.014-.002c-.312-.012-.62-.03-.923-.056a11.378 11.378 0 01-5.188-1.584M10.09 20.08v-.003c0-1.113.285-2.16.786-3.07M10.09 20.08A11.32 11.32 0 0110 20c-1.332 0-2.602-.23-3.774-.652L6.11 19.13M6.11 19.13a12.114 12.114 0 01-3.486-5.632l-.004-.017a11.53 11.53 0 01-.117-2.378c0-5.14 4.03-9.33 9.105-9.48l.004-.001A11.575 11.575 0 0112 1.5c.348 0 .692.015 1.03.045M6.11 19.13v.109a11.383 11.383 0 004.858-1.14M12 1.5c3.278 0 6.042 2.18 6.942 5.176M12 1.5C9.722 1.5 7.73 2.766 6.702 4.63M18.942 6.676A8.97 8.97 0 0019 6c0-4.97-4.03-9-9-9a8.96 8.96 0 00-5.702 2.03M18.942 6.676C19.643 8.878 20 11.142 20 13.5M10.09 16.907c0-1.636 1.306-2.96 2.91-2.96H15M10.09 16.907c0-1.636-1.306-2.96-2.91-2.96H4.125M10.09 16.907v.109M4.125 11.018a4.125 4.125 0 000 8.236m0-8.236h3.055m-3.055 0c-.394 0-.77.115-1.083.313m3.055-.313a4.125 4.125 0 014.125 4.125M4.125 19.254h3.055m-3.055 0c-.394 0-.77-.115-1.083-.313m0 0a4.122 4.122 0 01-1.417-3.06m5.555 3.373a4.121 4.121 0 004.125-4.125m-4.125 4.125v-3.07"
					/>
				</svg>
				<span>Capacity: {venue.capacity.toLocaleString()} seats</span>
			</div>
		{/if}
	</div>
</a>
