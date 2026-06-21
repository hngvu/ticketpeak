<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { goto } from '$app/navigation';
	import { SvelteMap } from 'svelte/reactivity';
	import { encodeUuidToBase62 } from '$lib/base62';
	import EventHeader from '$lib/components/event/EventHeader.svelte';
	import EventStatusBanner from '$lib/components/event/EventStatusBanner.svelte';
	import OfferRow from '$lib/components/event/OfferRow.svelte';
	import MobileOrderBar from '$lib/components/event/MobileOrderBar.svelte';

	let { data } = $props<{ data: any }>();

	const selection = new SvelteMap<string, number>();

	let activeTab = $state<'all' | 'standard' | 'vip'>('all');
	let reserving = $state(false);
	let reservationError = $state<string | null>(null);

	// Seating states (inline, not modal)
	let selectedSeats = $state<any[]>([]);
	let activeOfferForSeating = $state<any>(null);

	let canvasZoom = $state(1);
	let panX = $state(0);
	let panY = $state(0);
	let isPanning = $state(false);
	let startPanX = 0;
	let startPanY = 0;

	const seatR = 5;

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

	const eventDate = $derived(new Date(data.event.startAt));
	const formattedDate = $derived(
		eventDate.toLocaleDateString('en-US', {
			weekday: 'short',
			month: 'short',
			day: 'numeric',
			year: 'numeric'
		})
	);

	const hasManifest = $derived(
		data.manifestDetail && (
			(data.manifestDetail.gaAreas && data.manifestDetail.gaAreas.length > 0) ||
			(data.manifestDetail.rsAreas && data.manifestDetail.rsAreas.length > 0)
		)
	);

	function handleQtyChange(offerId: string, qty: number) {
		if (qty === 0) {
			selection.delete(offerId);
		} else {
			selection.set(offerId, qty);
		}
	}

	function handleChooseSeats(offer: any) {
		activeOfferForSeating = offer;
		selectedSeats = [];
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
		if (selectedSeats.some((s) => s.id === seat.id)) return '#2563EB';
		const pl = data.manifestDetail?.priceLevels?.find((p: any) => p.id === seat.priceLevelId);
		return pl ? pl.color : '#e2e8f0';
	}

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

	function resetSeating() {
		selectedSeats = [];
		activeOfferForSeating = null;
		if (activeOfferForSeating) {
			selection.delete(activeOfferForSeating.id);
		}
	}

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
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ eventId: data.event.id, items })
			});

			const json = await res.json();

			if (!res.ok) {
				if (res.status === 401) {
					goto(`/auth?redirect=${encodeURIComponent(window.location.pathname)}`);
					return;
				}
				throw new Error(json.message || 'Failed to complete ticket reservation.');
			}

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
	<meta name="description" content="Get tickets for {data.event.title} at {data.event.venueName} on {formattedDate}. Buy now on Ticketpeak." />
	<link rel="canonical" href="/{data.event.slug}/event/{encodeUuidToBase62(data.event.id)}" />
	<meta property="og:title" content="{data.event.title} | Ticketpeak" />
	<meta property="og:description" content="Get tickets for {data.event.title} at {data.event.venueName} on {formattedDate}. Buy now on Ticketpeak." />
	<meta property="og:image" content={data.event.imageUrl} />
	<meta property="og:type" content="website" />
</svelte:head>

<div class="flex h-screen flex-col bg-white select-none overflow-hidden">
	<EventHeader event={data.event} />
	<EventStatusBanner status={data.event.status} />

	<div class="flex flex-1 min-h-0 w-full max-w-[1600px] gap-4 px-4 py-4 md:px-6 mx-auto">
		<!-- Left: Manifest / Seating Map -->
		<div class="flex flex-1 flex-col rounded-md border border-hairline bg-white p-4 min-h-0">
			{#if hasManifest}
				<div class="mb-3 flex shrink-0 items-center gap-2">
					{#if activeOfferForSeating}
						<button type="button" onclick={resetSeating} class="cursor-pointer rounded-md border border-hairline bg-canvas-soft px-3 py-1.5 text-xs font-bold text-ink transition-colors hover:bg-canvas">← Back to Offers</button>
						<span class="text-xs text-mute">Selecting: <strong class="text-ink">{activeOfferForSeating.name}</strong></span>
					{:else}
						<button type="button" class="inline-flex cursor-pointer items-center gap-1.5 rounded-md border border-hairline bg-canvas-soft px-3 py-1.5 text-xs font-bold text-ink transition-colors hover:bg-canvas">
							<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" class="h-3.5 w-3.5"><path d="M3 6h18M3 12h18M3 18h18" /></svg>
							Filters
						</button>
						<button type="button" class="cursor-pointer rounded-md bg-ink px-3 py-1.5 text-xs font-bold text-white">{selectedItems.reduce((s, i) => s + i.qty, 0) || 0} Tickets</button>
						<button type="button" class="cursor-pointer rounded-md border border-hairline bg-canvas-soft px-3 py-1.5 text-xs font-bold text-ink transition-colors hover:bg-canvas">Ticket Types</button>
					{/if}
				</div>

				<!-- svelte-ignore a11y_no_static_element_interactions -->
				<div
					class="relative flex min-h-0 flex-1 items-center justify-center overflow-hidden rounded-lg border border-hairline/60 bg-canvas-soft cursor-grab active:cursor-grabbing"
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
						{#if data.manifestDetail?.manifest?.objects}
							{#each data.manifestDetail.manifest.objects.filter((o: any) => o.type === 'stage') as obj}
								<rect x={obj.x || 135} y={obj.y || 12} width={obj.width || 150} height={obj.height || 36} rx="4" fill="#e2e8f0" stroke="#cbd5e1" />
								<text x={(obj.x || 135) + (obj.width || 150) / 2} y={(obj.y || 12) + (obj.height || 36) / 2 + 4} text-anchor="middle" fill="#64748b" font-size="11" font-weight="700" letter-spacing="2">{obj.text || 'STAGE'}</text>
							{/each}
						{:else}
							<rect x="135" y="12" width="150" height="36" rx="4" fill="#e2e8f0" stroke="#cbd5e1" />
							<text x="210" y="35" text-anchor="middle" fill="#64748b" font-size="11" font-weight="700" letter-spacing="2">STAGE</text>
						{/if}

						{#each data.manifestDetail?.gaAreas || [] as ga}
							<rect x={ga.x || 55} y={ga.y || 60} width={ga.width || 310} height={ga.height || 180} rx="8" fill={data.manifestDetail?.sections?.find((s: any) => s.id === ga.sectionId)?.color || '#EF4444'} opacity="0.15" />
							<rect x={ga.x || 55} y={ga.y || 60} width={ga.width || 310} height={ga.height || 180} rx="8" fill="none" stroke={data.manifestDetail?.sections?.find((s: any) => s.id === ga.sectionId)?.color || '#EF4444'} stroke-width="2" stroke-dasharray="6 3" />
							<text x={(ga.x || 55) + (ga.width || 310) / 2} y={(ga.y || 60) + (ga.height || 180) / 2} text-anchor="middle" fill={data.manifestDetail?.sections?.find((s: any) => s.id === ga.sectionId)?.color || '#EF4444'} font-size="12" font-weight="800">{data.manifestDetail?.sections?.find((s: any) => s.id === ga.sectionId)?.name || 'GA'}</text>
						{/each}

						{#each data.manifestDetail?.rsAreas || [] as rs}
							<rect x={rs.x || 55} y={rs.y || 60} width={rs.width || 310} height={rs.height || 90} rx="6" fill={data.manifestDetail?.sections?.find((s: any) => s.id === rs.sectionId)?.color ? data.manifestDetail.sections.find((s: any) => s.id === rs.sectionId).color + '05' : '#f5f3ff'} stroke={data.manifestDetail?.sections?.find((s: any) => s.id === rs.sectionId)?.color || '#e9d5ff'} stroke-width="0.5" />
							{#each rs.rows || [] as row}
								{#each row.seats || [] as seat}
									<!-- svelte-ignore a11y_click_events_have_key_events -->
									<!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
									<circle cx={seat.positionX || 0} cy={seat.positionY || 0} r={seatR} fill={getSeatColor(seat)} stroke={selectedSeats.some((s) => s.id === seat.id) ? '#ffffff' : 'none'} stroke-width="1.5" class="transition-all hover:opacity-85 {seat.status === 'AVAILABLE' ? 'cursor-pointer' : 'cursor-not-allowed'}" onclick={() => seat.status === 'AVAILABLE' && toggleSeatSelection(seat, rs.sectionId)} />
								{/each}
							{/each}
						{/each}
					</svg>

					<div class="absolute bottom-3 right-3 flex items-center gap-1 rounded-lg border border-hairline bg-canvas/90 p-1 shadow-sm backdrop-blur-xs">
						<button type="button" onclick={zoomOut} class="flex h-7 w-7 cursor-pointer items-center justify-center rounded-md border border-hairline bg-canvas font-bold font-mono text-sm text-ink select-none hover:bg-canvas-soft">−</button>
						<span class="px-2 font-mono text-[10px] font-bold text-mute">{Math.round(canvasZoom * 100)}%</span>
						<button type="button" onclick={zoomIn} class="flex h-7 w-7 cursor-pointer items-center justify-center rounded-md border border-hairline bg-canvas font-bold font-mono text-sm text-ink select-none hover:bg-canvas-soft">+</button>
					</div>
				</div>

				{#if data.manifestDetail?.priceLevels?.length > 0}
					<div class="mt-3 flex shrink-0 flex-wrap items-center gap-3">
						{#each data.manifestDetail.priceLevels as pl}
							<div class="flex items-center gap-1.5">
								<span class="inline-block h-3 w-3 rounded-sm" style="background:{pl.color}"></span>
								<span class="text-[11px] font-semibold text-mute">{pl.name} — {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(pl.price)}</span>
							</div>
						{/each}
					</div>
				{/if}

				{#if selectedSeats.length > 0}
					<div class="mt-3 shrink-0 rounded-lg border border-hairline bg-canvas-soft p-3">
						<div class="mb-2 text-xs font-bold uppercase tracking-wider text-mute">Selected Seats ({selectedSeats.length})</div>
						<div class="flex flex-wrap gap-1.5">
							{#each selectedSeats as s}
								<span class="inline-flex items-center gap-1 rounded-md border border-primary/30 bg-primary/5 px-2 py-0.5 text-xs font-semibold text-primary">
									Row {s.rowLetter}·{s.seatNum}
									<button type="button" onclick={() => toggleSeatSelection({ id: s.id }, s.sectionId)} class="ml-0.5 cursor-pointer text-primary/60 hover:text-primary">✕</button>
								</span>
							{/each}
						</div>
					</div>
				{/if}
			{:else}
				<div class="flex flex-1 items-center justify-center text-sm text-mute">No seating map available for this event.</div>
			{/if}
		</div>

		<!-- Right: Offers Panel -->
		<div class="flex w-[360px] shrink-0 flex-col min-h-0">
			<div class="flex-1 flex flex-col rounded-md border border-hairline bg-white p-4 min-h-0">
				<div class="mb-3 flex shrink-0 items-center justify-between">
					<span class="text-sm font-bold text-ink">{filteredOffers.length} Result{filteredOffers.length !== 1 ? 's' : ''}</span>
					{#if data.offers.length > 1}
						<div class="flex items-center gap-1 rounded-md border border-hairline/60 bg-canvas-soft p-0.5">
							<button type="button" onclick={() => (activeTab = 'all')} class="cursor-pointer rounded px-2 py-0.5 text-[11px] font-bold transition-all {activeTab === 'all' ? 'bg-canvas text-ink shadow-xs' : 'text-mute hover:text-ink'}">All</button>
							<button type="button" onclick={() => (activeTab = 'standard')} class="cursor-pointer rounded px-2 py-0.5 text-[11px] font-bold transition-all {activeTab === 'standard' ? 'bg-canvas text-ink shadow-xs' : 'text-mute hover:text-ink'}">Standard</button>
							<button type="button" onclick={() => (activeTab = 'vip')} class="cursor-pointer rounded px-2 py-0.5 text-[11px] font-bold transition-all {activeTab === 'vip' ? 'bg-canvas text-ink shadow-xs' : 'text-mute hover:text-ink'}">VIP</button>
						</div>
					{/if}
				</div>

				<p class="mb-3 shrink-0 text-xs text-mute">Prices include fees (before taxes).</p>

				<div class="flex-1 min-h-0 overflow-y-auto space-y-1">
					{#if filteredOffers.length > 0}
						{#each filteredOffers as offer (offer.id)}
							<OfferRow {offer} inventory={data.inventory} selectedQty={selection.get(offer.id) || 0} onQtyChange={(qty) => handleQtyChange(offer.id, qty)} onChooseSeats={() => handleChooseSeats(offer)} />
						{/each}
					{:else}
						<p class="py-8 text-center text-sm text-mute">No offers available.</p>
					{/if}
				</div>
			</div>

			{#if totalQty > 0}
				<div class="mt-4 shrink-0 rounded-md border border-hairline bg-white p-4">
					<div class="mb-3 space-y-1">
						{#each selectedItems as item}
							<div class="flex items-center justify-between text-xs">
								<span class="text-mute">{item.qty}× {item.offer.name}</span>
								<span class="font-semibold text-ink">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.offer.faceValue * item.qty)}</span>
							</div>
						{/each}
					</div>
					<div class="flex items-center justify-between border-t border-hairline pt-3">
						<span class="text-sm font-bold text-ink">Total</span>
						<span class="text-base font-bold text-primary">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(orderTotal)}</span>
					</div>
					{#if reservationError}
						<p class="mt-2 text-xs text-error">{reservationError}</p>
					{/if}
					<button type="button" onclick={handleGetTickets} disabled={reserving || totalQty === 0} class="mt-3 w-full cursor-pointer rounded-full bg-primary py-3 text-sm font-bold text-white transition-colors hover:bg-primary/95 disabled:cursor-not-allowed disabled:opacity-60">
						{reserving ? 'Reserving...' : 'Get Tickets'}
					</button>
				</div>
			{/if}
		</div>
	</div>

	<MobileOrderBar {orderTotal} {totalQty} {reserving} currency={selectedItems[0]?.offer?.currency || 'VND'} onGetTickets={handleGetTickets} />
</div>
