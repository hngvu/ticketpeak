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

	// Format start date beautifully
	const formattedDate = $derived(() => {
		if (!event.startAt) return null;
		const d = new Date(event.startAt);

		const weekday = d.toLocaleDateString('en-US', { weekday: 'short' });
		const month = d.toLocaleDateString('en-US', { month: 'short' }).toUpperCase();
		const day = d.getDate();
		const time = d.toLocaleTimeString('en-US', {
			hour: 'numeric',
			minute: '2-digit',
			hour12: true
		});

		return { weekday, month, day, time };
	});

	const dateParts = $derived(formattedDate());

	const displayVenue = $derived(venueName || event.venueName || 'Venue TBD');
	const displayCity = $derived(cityName || event.cityName || 'City TBD');
</script>

<div class="group flex flex-col sm:flex-row items-start sm:items-center w-full border-b border-hairline bg-canvas hover:bg-canvas-soft transition-colors py-4 px-2 sm:px-4 gap-4 sm:gap-6">
	
	<!-- Date Block -->
	<div class="flex-shrink-0 w-16 h-16 bg-[#f2f2f2] flex flex-col items-center justify-center rounded-sm">
		{#if dateParts}
			<span class="text-[13px] leading-tight font-semibold text-[#1f262d] uppercase tracking-wide">{dateParts.month}</span>
			<span class="text-[22px] leading-tight font-normal text-[#1f262d]">{dateParts.day}</span>
		{:else}
			<span class="text-[13px] leading-tight font-semibold text-[#1f262d] uppercase tracking-wide">TBD</span>
		{/if}
	</div>

	<!-- Info Block -->
	<div class="flex-grow min-w-0 flex flex-col justify-center gap-0.5">
		<div class="flex items-center gap-1.5 text-[15px] text-[#767676]">
			{#if dateParts}
				<span>{dateParts.weekday} • {dateParts.time}</span>
			{:else}
				<span>Date TBA</span>
			{/if}
			<!-- Info Icon Placeholder -->
			<svg class="w-4 h-4 text-[#767676]" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
			</svg>
		</div>
		
		<h3 class="text-[17px] font-semibold text-[#1f262d] leading-snug truncate">
			{event.title}
		</h3>
		
		<div class="text-[15px] text-[#767676] truncate">
			{displayCity} • {displayVenue}
		</div>
	</div>

	<!-- Action Button -->
	<div class="flex-shrink-0 w-full sm:w-auto mt-2 sm:mt-0">
		<a
			href="/{event.slug}/event/{encodeUuidToBase62(event.id)}"
			class="flex items-center justify-center sm:justify-between w-full sm:w-[150px] bg-[#026cdf] hover:bg-[#0256b2] text-white px-4 py-2.5 rounded-sm transition-colors"
		>
			<span class="font-semibold text-[15px]">Find Tickets</span>
			<svg class="w-5 h-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
			</svg>
		</a>
	</div>
</div>
