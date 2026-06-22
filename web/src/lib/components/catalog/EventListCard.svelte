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

<div
	class="group flex w-full flex-col items-start gap-4 border-b border-hairline bg-canvas px-2 py-4 transition-colors hover:bg-canvas-soft sm:flex-row sm:items-center sm:gap-6 sm:px-4"
>
	<!-- Date Block -->
	<div
		class="flex h-16 w-16 flex-shrink-0 flex-col items-center justify-center rounded-sm bg-[#f2f2f2]"
	>
		{#if dateParts}
			<span class="text-[13px] leading-tight font-semibold tracking-wide text-[#1f262d] uppercase"
				>{dateParts.month}</span
			>
			<span class="text-[22px] leading-tight font-normal text-[#1f262d]">{dateParts.day}</span>
		{:else}
			<span class="text-[13px] leading-tight font-semibold tracking-wide text-[#1f262d] uppercase"
				>TBD</span
			>
		{/if}
	</div>

	<!-- Info Block -->
	<div class="flex min-w-0 flex-grow flex-col justify-center gap-0.5">
		<div class="flex items-center gap-1.5 text-[15px] text-[#767676]">
			{#if dateParts}
				<span>{dateParts.weekday} • {dateParts.time}</span>
			{:else}
				<span>Date TBA</span>
			{/if}
			<!-- Info Icon Placeholder -->
			<svg
				class="h-4 w-4 text-[#767676]"
				fill="none"
				stroke="currentColor"
				viewBox="0 0 24 24"
				xmlns="http://www.w3.org/2000/svg"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
				></path>
			</svg>
		</div>

		<h3 class="truncate text-[17px] leading-snug font-semibold text-[#1f262d]">
			{event.title}
		</h3>

		<div class="truncate text-[15px] text-[#767676]">
			{displayCity} • {displayVenue}
		</div>
	</div>

	<!-- Action Button -->
	<div class="mt-2 w-full flex-shrink-0 sm:mt-0 sm:w-auto">
		<a
			href="/{event.slug}/event/{encodeUuidToBase62(event.id)}"
			class="flex w-full items-center justify-center rounded-sm bg-[#026cdf] px-4 py-2.5 text-white transition-colors hover:bg-[#0256b2] sm:w-[150px] sm:justify-between"
		>
			<span class="text-[15px] font-semibold">Find Tickets</span>
			<svg
				class="ml-2 h-5 w-5"
				fill="none"
				stroke="currentColor"
				viewBox="0 0 24 24"
				xmlns="http://www.w3.org/2000/svg"
			>
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"
				></path>
			</svg>
		</a>
	</div>
</div>
