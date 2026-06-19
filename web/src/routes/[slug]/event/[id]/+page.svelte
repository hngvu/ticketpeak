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

	// Seat Picker States
	let showSeatPickerModal = $state(false);
	let selectedSeats = $state<any[]>([]); // Array of { id, sectionId, seatNum, rowLetter, price, offerId }
	let activeOfferForSeating = $state<any>(null);

	// Zoom and Pan states for the SVG map
	let canvasZoom = $state(1);
	let panX = $state(0);
	let panY = $state(0);
	let isPanning = $state(false);
	let startPanX = 0;
	let startPanY = 0;

	const seatR = 5;

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

	function handleChooseSeats(offer: any) {
		activeOfferForSeating = offer;
		showSeatPickerModal = true;
		canvasZoom = 1;
		panX = 0;
		panY = 0;
	}

	function toggleSeatSelection(seat: any, sectionId: string) {
		const idx = selectedSeats.findIndex((s) => s.id === seat.id);
		if (idx > -1) {
			selectedSeats = selectedSeats.filter((s) => s.id !== seat.id);
			const currentQty = selection.get(activeOfferForSeating.id) || 0;
			handleQtyChange(activeOfferForSeating.id, Math.max(0, currentQty - 1));
		} else {
			if (selectedSeats.length >= 6) {
				alert('You can select a maximum of 6 seats.');
				return;
			}
			selectedSeats = [
				...selectedSeats,
				{
					id: seat.id,
					sectionId,
					seatNum: seat.seatNum,
					rowLetter: seat.rowLetter,
					price: activeOfferForSeating.faceValue,
					offerId: activeOfferForSeating.id
				}
			];
			const currentQty = selection.get(activeOfferForSeating.id) || 0;
			handleQtyChange(activeOfferForSeating.id, currentQty + 1);
		}
	}

	function getSeatColor(seat: any) {
		if (seat.status !== 'AVAILABLE') return '#94A3B8';
		if (selectedSeats.some((s) => s.id === seat.id)) return '#2563EB'; // primary selection color
		const pl = data.manifestDetail?.priceLevels?.find((p: any) => p.id === seat.priceLevelId);
		return pl ? pl.color : '#e2e8f0';
	}

	// Pan handlers
	function handleMouseDown(e: MouseEvent) {
		isPanning = true;
		startPanX = e.clientX - panX;
		startPanY = e.clientY - panY;
	}

	function handleMouseMove(e: MouseEvent) {
		if (!isPanning) return;
		panX = e.clientX - startPanX;
		panY = e.clientY - startPanY;
	}

	function handleMouseUp() {
		isPanning = false;
	}

	function zoomIn() {
		canvasZoom = Math.min(3, canvasZoom + 0.25);
	}

	function zoomOut() {
		canvasZoom = Math.max(0.5, canvasZoom - 0.25);
	}

	// Create reservation flow
	async function handleGetTickets() {
		if (totalQty === 0) return;
		reserving = true;
		reservationError = null;

		try {
			const items = selectedSeats.length > 0
				? selectedSeats.map((s) => ({
						offerId: s.offerId,
						seatingMode: 'SEATED' as any,
						sectionId: s.sectionId,
						seatId: s.id,
						qty: 1
					}))
				: selectedItems.map(({ offer, qty }) => {
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
									onChooseSeats={() => handleChooseSeats(offer)}
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

<!-- Interactive SVG Seat Picker Modal -->
{#if showSeatPickerModal}
	<!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs select-none">
		<div class="flex h-[80vh] w-full max-w-5xl flex-col rounded-2xl border border-hairline bg-canvas shadow-2xl overflow-hidden">
			<!-- Header -->
			<div class="flex items-center justify-between border-b border-hairline px-6 py-4 bg-canvas">
				<div>
					<h3 class="font-sans text-base font-extrabold text-ink">Choose Seats — {activeOfferForSeating?.name}</h3>
					<p class="font-sans text-xs text-mute mt-0.5">Click on available seats to add them to your selection.</p>
				</div>
				<button
					type="button"
					onclick={() => {
						showSeatPickerModal = false;
						selectedSeats = [];
						selection.delete(activeOfferForSeating.id);
					}}
					class="cursor-pointer text-mute hover:text-ink font-mono text-base font-semibold"
				>
					✕
				</button>
			</div>

			<!-- Main Workspace (SVG Map left, Selected Seats sidebar right) -->
			<div class="flex flex-1 min-h-0">
				<!-- Seating Map Canvas -->
				<div class="relative flex-1 bg-canvas-soft overflow-hidden flex items-center justify-center p-4">
					<!-- SVG Canvas wrapper with mouse drag-pan -->
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<div
						class="relative h-[400px] w-[500px] border border-hairline/60 bg-canvas rounded-lg shadow-inner overflow-hidden cursor-grab active:cursor-grabbing"
						onmousedown={handleMouseDown}
						onmousemove={handleMouseMove}
						onmouseup={handleMouseUp}
						onmouseleave={handleMouseUp}
					>
						<svg
							viewBox="0 0 420 300"
							class="h-full w-full select-none"
							style="transform: translate({panX}px, {panY}px) scale({canvasZoom}); transform-origin: center; pointer-events: {isPanning ? 'none' : 'auto'};"
						>
							<!-- Stage object -->
							{#if data.manifestDetail?.manifest?.objects}
								{#each data.manifestDetail.manifest.objects.filter((o: any) => o.type === 'stage') as obj}
									<rect
										x={obj.x || 135}
										y={obj.y || 12}
										width={obj.width || 150}
										height={obj.height || 36}
										rx="4"
										fill="#e2e8f0"
										stroke="#cbd5e1"
									/>
									<text
										x={(obj.x || 135) + (obj.width || 150) / 2}
										y={(obj.y || 12) + (obj.height || 36) / 2 + 4}
										text-anchor="middle"
										fill="#64748b"
										font-size="11"
										font-weight="700"
										letter-spacing="2"
									>{obj.text || 'STAGE'}</text>
								{/each}
							{:else}
								<rect
									x="135"
									y="12"
									width="150"
									height="36"
									rx="4"
									fill="#e2e8f0"
									stroke="#cbd5e1"
								/>
								<text
									x="210"
									y="35"
									text-anchor="middle"
									fill="#64748b"
									font-size="11"
									font-weight="700"
									letter-spacing="2"
								>STAGE</text>
							{/if}

							<!-- GA Areas -->
							{#each data.manifestDetail?.gaAreas || [] as ga}
								<rect
									x={ga.x || 55}
									y={ga.y || 60}
									width={ga.width || 310}
									height={ga.height || 180}
									rx="8"
									fill={data.manifestDetail?.sections?.find((s: any) => s.id === ga.sectionId)?.color || '#EF4444'}
									opacity="0.15"
								/>
								<rect
									x={ga.x || 55}
									y={ga.y || 60}
									width={ga.width || 310}
									height={ga.height || 180}
									rx="8"
									fill="none"
									stroke={data.manifestDetail?.sections?.find((s: any) => s.id === ga.sectionId)?.color || '#EF4444'}
									stroke-width="2"
									stroke-dasharray="6 3"
								/>
								<text
									x={(ga.x || 55) + (ga.width || 310) / 2}
									y={(ga.y || 60) + (ga.height || 180) / 2}
									text-anchor="middle"
									fill={data.manifestDetail?.sections?.find((s: any) => s.id === ga.sectionId)?.color || '#EF4444'}
									font-size="12"
									font-weight="800"
								>
									{data.manifestDetail?.sections?.find((s: any) => s.id === ga.sectionId)?.name || 'GA'}
								</text>
							{/each}

							<!-- RS Areas with Seats -->
							{#each data.manifestDetail?.rsAreas || [] as rs}
								<rect
									x={rs.x || 55}
									y={rs.y || 60}
									width={rs.width || 310}
									height={rs.height || 90}
									rx="6"
									fill={data.manifestDetail?.sections?.find((s: any) => s.id === rs.sectionId)?.color ? data.manifestDetail.sections.find((s: any) => s.id === rs.sectionId).color + '05' : '#f5f3ff'}
									stroke={data.manifestDetail?.sections?.find((s: any) => s.id === rs.sectionId)?.color || '#e9d5ff'}
									stroke-width="0.5"
								/>
								{#each rs.rows || [] as row}
									{#each row.seats || [] as seat}
										<!-- svelte-ignore a11y_click_events_have_key_events -->
										<!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
										<circle
											cx={seat.positionX || 0}
											cy={seat.positionY || 0}
											r={seatR}
											fill={getSeatColor(seat)}
											stroke={selectedSeats.some((s) => s.id === seat.id) ? '#ffffff' : 'none'}
											stroke-width="1.5"
											class="transition-all hover:opacity-85 {seat.status === 'AVAILABLE' ? 'cursor-pointer animate-pulse' : 'cursor-not-allowed'}"
											onclick={() => seat.status === 'AVAILABLE' && toggleSeatSelection(seat, rs.sectionId)}
										/>
									{/each}
								{/each}
							{/each}
						</svg>

						<!-- Zoom controls overlay -->
						<div class="absolute bottom-4 right-4 flex items-center gap-1 bg-canvas/90 backdrop-blur-xs border border-hairline rounded-lg p-1.5 shadow-sm">
							<button
								type="button"
								onclick={zoomOut}
								class="h-7 w-7 flex items-center justify-center rounded-md border border-hairline bg-canvas hover:bg-canvas-soft-2 font-bold font-mono text-sm cursor-pointer select-none text-ink"
							>
								−
							</button>
							<span class="px-2 font-mono text-[10px] font-bold text-mute">{Math.round(canvasZoom * 100)}%</span>
							<button
								type="button"
								onclick={zoomIn}
								class="h-7 w-7 flex items-center justify-center rounded-md border border-hairline bg-canvas hover:bg-canvas-soft-2 font-bold font-mono text-sm cursor-pointer select-none text-ink"
							>
								+
							</button>
						</div>
					</div>
				</div>

				<!-- Sidebar Selected Seats -->
				<div class="w-[300px] border-l border-hairline bg-canvas p-6 flex flex-col justify-between">
					<div class="space-y-6">
						<h4 class="font-sans text-xs font-bold uppercase tracking-wider text-mute">Selected Seats ({selectedSeats.length})</h4>
						
						{#if selectedSeats.length > 0}
							<div class="space-y-2 max-h-[250px] overflow-y-auto pr-1">
								{#each selectedSeats as s (s.id)}
									<div class="flex items-center justify-between p-3 rounded-lg border border-hairline bg-canvas-soft-2 animate-in slide-in-from-right-5 duration-200">
										<div>
											<div class="font-sans text-xs font-semibold text-ink">
												Row {s.rowLetter} — Seat {s.seatNum}
											</div>
											<p class="font-mono text-[9px] text-mute uppercase mt-0.5">
												Section: {data.manifestDetail?.sections?.find((sec: any) => sec.id === s.sectionId)?.name || 'Reserved'}
											</p>
										</div>
										<button
											type="button"
											onclick={() => toggleSeatSelection({ id: s.id }, s.sectionId)}
											class="text-error font-mono text-xs font-bold cursor-pointer"
										>
											Remove
										</button>
									</div>
								{/each}
							</div>
						{:else}
							<div class="h-40 flex flex-col items-center justify-center border border-dashed border-hairline rounded-lg text-center p-4">
								<span class="text-xs text-mute font-semibold">No seats selected yet.</span>
							</div>
						{/if}
					</div>

					<div class="border-t border-hairline pt-4 space-y-4">
						<div class="flex justify-between items-baseline">
							<span class="font-sans text-xs font-bold text-mute uppercase">Total Price</span>
							<span class="font-mono text-base font-bold text-primary">
								{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(selectedSeats.reduce((acc, s) => acc + s.price, 0))}
							</span>
						</div>

						<button
							type="button"
							onclick={() => (showSeatPickerModal = false)}
							disabled={selectedSeats.length === 0}
							class="w-full cursor-pointer rounded-full bg-primary py-2.5 font-sans text-xs font-bold text-on-primary hover:bg-primary/95 transition text-center disabled:opacity-60 disabled:cursor-not-allowed"
						>
							CONFIRM SEATING SELECTION
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>
{/if}
