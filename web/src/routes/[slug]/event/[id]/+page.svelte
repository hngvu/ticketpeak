<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { goto } from '$app/navigation';
	import { SvelteMap } from 'svelte/reactivity';
	import { encodeUuidToBase62 } from '$lib/base62';
	import EventHero from '$lib/components/event/EventHero.svelte';
	import EventInfoBand from '$lib/components/event/EventInfoBand.svelte';
	import EventStatusBanner from '$lib/components/event/EventStatusBanner.svelte';
	import OfferRow from '$lib/components/event/OfferRow.svelte';
	import EventLineup from '$lib/components/event/EventLineup.svelte';
	import OrderSummaryCard from '$lib/components/event/OrderSummaryCard.svelte';
	import MobileOrderBar from '$lib/components/event/MobileOrderBar.svelte';

	let { data } = $props<{ data: any }>();

	// Reactive selection map: offerId -> qty
	const selection = new SvelteMap<string, number>();

	// UI States
	let bioExpanded = $state(false);
	let expandedOfferId = $state<string | null>(null);
	let activeTab = $state<'all' | 'standard' | 'vip'>('all');
	let reserving = $state(false);
	let reservationError = $state<string | null>(null);

	// Derived lists and pricing
	const selectedItems = $derived(
		[...selection.entries()]
			.filter(([, qty]) => qty > 0)
			.map(([offerId, qty]) => ({
				offer: data.offers.find((o: any) => o.id === offerId)!,
				qty
			}))
	);

	const orderTotal = $derived(
		selectedItems.reduce((acc, { offer, qty }) => {
			const chargesPerTicket = (offer.charges || []).reduce((s: number, c: any) => {
				const amt = c.isPercentage ? (offer.faceValue * c.amount) / 100 : c.amount;
				return s + amt;
			}, 0);
			return acc + (offer.faceValue + chargesPerTicket) * qty;
		}, 0)
	);

	const totalQty = $derived(selectedItems.reduce((sum, { qty }) => sum + qty, 0));

	const filteredOffers = $derived(
		data.offers.filter((o: any) => {
			if (activeTab === 'all') return true;
			if (activeTab === 'standard') return o.ticketTypeId === 'type-standard';
			if (activeTab === 'vip') return o.ticketTypeId === 'type-vip';
			return true;
		})
	);

	// Date formatted for SEO
	const eventDate = $derived(new Date(data.event.startAt));
	const formattedDate = $derived(
		eventDate.toLocaleDateString('en-US', {
			weekday: 'short',
			month: 'short',
			day: 'numeric',
			year: 'numeric'
		})
	);

	// Helper to update selection quantity
	function handleQtyChange(offerId: string, qty: number) {
		if (qty === 0) {
			selection.delete(offerId);
		} else {
			selection.set(offerId, qty);
		}
	}

	function handleToggleOffer(offerId: string) {
		if (expandedOfferId === offerId) {
			expandedOfferId = null;
		} else {
			expandedOfferId = offerId;
		}
	}

	// Create reservation flow
	async function handleGetTickets() {
		if (totalQty === 0) return;
		reserving = true;
		reservationError = null;

		try {
			const items = selectedItems.map(({ offer, qty }) => {
				const invItem = data.inventory.gaInventory.find((g: any) => g.offerId === offer.id);
				return {
					offerId: offer.id,
					seatingMode: offer.seatingMode || 'GA',
					areaId: invItem ? invItem.areaId : 'area-ga',
					qty
				};
			});

			const res = await fetch('/api/reservations', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					eventId: data.event.id,
					items
				})
			});

			const json = await res.json();

			if (!res.ok) {
				if (res.status === 401) {
					// Client is unauthorized, redirect to authentication route
					goto(`/auth?redirect=${encodeURIComponent(window.location.pathname)}`);
					return;
				}
				throw new Error(json.message || 'Failed to complete ticket reservation.');
			}

			// Successfully created reservation, forward to checkout screen (Plan 023)
			goto(`/checkout/${json.data.id}`);
		} catch (err: any) {
			console.error('[CLIENT GET TICKETS FAILURE]:', err);
			reservationError = err.message || 'Could not complete tickets reservation. Please try again.';
			reserving = false;
		}
	}
</script>

<svelte:head>
	<title>{data.event.title} | {formattedDate} | Ticketpeak</title>
	<meta
		name="description"
		content="Get tickets for {data.event.title} at {data.event
			.venueName} on {formattedDate}. Buy now on Ticketpeak."
	/>
	<link rel="canonical" href="/{data.event.slug}/event/{encodeUuidToBase62(data.event.id)}" />

	<!-- Open Graph metadata -->
	<meta property="og:title" content="{data.event.title} | Ticketpeak" />
	<meta
		property="og:description"
		content="Get tickets for {data.event.title} at {data.event
			.venueName} on {formattedDate}. Buy now on Ticketpeak."
	/>
	<meta property="og:image" content={data.event.imageUrl} />
	<meta property="og:type" content="website" />
</svelte:head>

<div class="min-h-screen bg-canvas-soft pb-24 select-none lg:pb-16">
	<!-- 1. Full-width Hero Banner -->
	<EventHero event={data.event} />

	<!-- 2. Event Info Band (Dates + Venue Locations) -->
	<EventInfoBand event={data.event} currentUser={data.currentUser} />

	<!-- 3. Page Columns Frame -->
	<div class="mx-auto max-w-[1400px] px-4 py-8 md:px-6">
		<div class="grid grid-cols-1 gap-8 lg:grid-cols-[1fr_360px]">
			<!-- Left Column: Details & Ticket selections -->
			<div class="space-y-8">
				<!-- Alerts / Warning status banners -->
				<EventStatusBanner status={data.event.status} />

				<!-- Ticket Selection options list -->
				<section class="space-y-5 rounded-2xl border border-hairline bg-canvas p-6 shadow-2xs">
					<div
						class="flex flex-col gap-4 border-b border-hairline pb-4 sm:flex-row sm:items-center sm:justify-between"
					>
						<h2 class="font-sans text-lg font-extrabold text-ink sm:text-xl">Ticket Options</h2>

						<!-- Elegant filter tabs -->
						{#if data.offers.length > 1}
							<div
								class="flex items-center gap-1.5 rounded-lg border border-hairline/60 bg-canvas-soft p-1"
							>
								<button
									type="button"
									onclick={() => (activeTab = 'all')}
									class="cursor-pointer rounded-md px-3 py-1 font-sans text-xs font-bold transition-all
										{activeTab === 'all' ? 'shadow-3xs bg-canvas text-ink' : 'text-mute hover:text-ink'}"
								>
									All
								</button>
								<button
									type="button"
									onclick={() => (activeTab = 'standard')}
									class="cursor-pointer rounded-md px-3 py-1 font-sans text-xs font-bold transition-all
										{activeTab === 'standard' ? 'shadow-3xs bg-canvas text-ink' : 'text-mute hover:text-ink'}"
								>
									Standard
								</button>
								<button
									type="button"
									onclick={() => (activeTab = 'vip')}
									class="cursor-pointer rounded-md px-3 py-1 font-sans text-xs font-bold transition-all
										{activeTab === 'vip' ? 'shadow-3xs bg-canvas text-ink' : 'text-mute hover:text-ink'}"
								>
									VIP
								</button>
							</div>
						{/if}
					</div>

					<!-- Offers accordion list -->
					{#if filteredOffers.length > 0}
						<div class="space-y-1">
							{#each filteredOffers as offer (offer.id)}
								<OfferRow
									{offer}
									inventory={data.inventory}
									selectedQty={selection.get(offer.id) || 0}
									isExpanded={expandedOfferId === offer.id}
									onToggle={() => handleToggleOffer(offer.id)}
									onQtyChange={(qty) => handleQtyChange(offer.id, qty)}
								/>
							{/each}
						</div>
					{:else}
						<p class="py-10 text-center font-sans text-sm font-medium text-mute">
							No ticket offers available matching this category.
						</p>
					{/if}
				</section>

				<!-- About details section -->
				{#if data.event.description}
					<section class="space-y-4 rounded-2xl border border-hairline bg-canvas p-6 shadow-2xs">
						<h3 class="font-sans text-base font-extrabold text-ink sm:text-lg">About this event</h3>
						<div class="border-t border-hairline pt-4">
							<p
								class="font-sans text-sm leading-relaxed whitespace-pre-line text-body
									{bioExpanded ? '' : 'line-clamp-4'}"
							>
								{data.event.description}
							</p>

							<button
								type="button"
								onclick={() => (bioExpanded = !bioExpanded)}
								class="hover:text-link-deep mt-3 cursor-pointer font-sans text-xs font-bold text-primary outline-none"
							>
								{bioExpanded ? 'Read less ▴' : 'Read more ▾'}
							</button>
						</div>
					</section>
				{/if}

				<!-- Attraction Lineup section -->
				<EventLineup attractions={data.event.attractions} />
			</div>

			<!-- Right Column: Desktop order card sidebar -->
			<div class="hidden self-start lg:sticky lg:top-20 lg:block">
				<OrderSummaryCard
					event={data.event}
					{selectedItems}
					{orderTotal}
					{totalQty}
					{reserving}
					{reservationError}
					onGetTickets={handleGetTickets}
				/>
			</div>
		</div>
	</div>

	<!-- 4. Mobile Sticky Bottom checkout drawer -->
	<MobileOrderBar
		{orderTotal}
		{totalQty}
		{reserving}
		currency={selectedItems[0]?.offer?.currency || 'VND'}
		onGetTickets={handleGetTickets}
	/>
</div>
