<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';
	import { IconPlus } from '@tabler/icons-svelte';
	import Combobox from '$lib/components/ui/combobox.svelte';
	import DateTimePicker from '$lib/components/common/DateTimePicker.svelte';

	function cleanVietnamese(text: string): string {
		return text
			.normalize('NFD')
			.replace(/[\u0300-\u036f]/g, '')
			.replace(/đ/g, 'd')
			.replace(/Đ/g, 'D');
	}

	let { data, form } = $props();

	let loading = $state(false);
	let isAddOfferModalOpen = $state(false);

	// Canvas Tools
	let activeCanvasTool = $state<'select' | 'pan'>('select');
	let selectionTool = $state<'seat' | 'row' | 'section'>('seat');
	let showFilterDialog = $state(false);
	let showAssistantDialog = $state(false);
	let panX = $state(0);
	let panY = $state(0);
	let isPanning = $state(false);
	let panStartX = $state(0);
	let panStartY = $state(0);

	function handleWheel(e: WheelEvent) {
		e.preventDefault();
		const oldZoom = canvasZoom;
		let newZoom = e.deltaY < 0 ? oldZoom * 1.15 : oldZoom / 1.15;
		canvasZoom = Math.max(0.1, Math.min(newZoom, 5));
	}

	function handlePointerDown(e: PointerEvent) {
		if (e.button === 0 || e.button === 1) {
			// Left or middle click pans
			isPanning = true;
			panStartX = e.clientX - panX;
			panStartY = e.clientY - panY;
			(e.currentTarget as HTMLElement).setPointerCapture(e.pointerId);
		}
	}
	function handlePointerMove(e: PointerEvent) {
		if (isPanning) {
			panX = e.clientX - panStartX;
			panY = e.clientY - panStartY;
		}
	}
	function handlePointerUp(e: PointerEvent) {
		isPanning = false;
		(e.currentTarget as HTMLElement).releasePointerCapture(e.pointerId);
	}
	let canvasZoom = $state(1);

	let activeTab = $state('general');

	const event = $derived(form?.event || data.event);
	const offers = $derived(data.offers || []);
	const venueName = $derived(data.venues?.find((v: any) => v.id === event.venueId)?.name || '');
	const attractions = $derived(data.attractions || []);

	let localOffers = $state<any[]>([]);
	$effect(() => {
		if (offers && localOffers.length === 0) {
			localOffers = [...offers];
		}
	});

	let classificationId = $state(
		data.classifications?.find((c: any) => event.classifications?.some((ec: any) => ec.id === c.id))
			?.id || ''
	);
	let venueId = $state(event.venueId || '');
	let selectedAttractionIds = $state<string[]>(event.attractionIds || []);
	let startAt = $state(event.startAt || '');

	const sortedClassifications = $derived.by(() => {
		const raw = data.classifications || [];
		const parents = raw.filter((c: any) => !c.parentId);
		const result: any[] = [];
		for (const p of parents) {
			result.push(p);
			const children = raw.filter((c: any) => c.parentId === p.id);
			result.push(...children);
		}
		const inResult = new Set(result.map((c) => c.id));
		const orphans = raw.filter((c: any) => !inResult.has(c.id));
		result.push(...orphans);
		return result;
	});

	let editingOffer = $state<any>(null);

	let holds = $state([
		{
			id: 'hold-1',
			name: 'Sponsor Block A (VIP)',
			count: 20,
			reason: 'Gold Sponsor Allocation',
			status: 'LOCKED'
		},
		{
			id: 'hold-2',
			name: 'Press & Media Tier',
			count: 10,
			reason: 'Journalist Review',
			status: 'LOCKED'
		},
		{
			id: 'hold-3',
			name: 'Technical Camera Hold',
			count: 6,
			reason: 'Camera Obstruction',
			status: 'KILLED'
		}
	]);

	let isAddHoldModalOpen = $state(false);
	let newHoldName = $state('');
	let newHoldCount = $state<number>(15);
	let newHoldReason = $state('');
	let newHoldType = $state<'LOCKED' | 'KILLED'>('LOCKED');

	let selectedSeatIds = $state<string[]>([]);
	let selectedManifestId = $state(
		data.manifests.length > 0
			? data.manifests.find((m) => m.status === 'PUBLISHED')?.id || data.manifests[0].id
			: ''
	);
	const selectedManifest = $derived(data.manifests.find((m: any) => m.id === selectedManifestId));

	let manifestDetail = $state<any>(null);
	$effect(() => {
		const mid = selectedManifestId;
		const vid = event.venueId;
		if (!mid || !vid) {
			manifestDetail = null;
			return;
		}
		manifestDetail = null;
		fetch(`/api/partner/venues/${vid}/manifests/${mid}`)
			.then((r) => (r.ok ? r.json() : null))
			.then((json) => {
				manifestDetail = json?.data || null;
			})
			.catch(() => {
				manifestDetail = null;
			});
	});

	const hasLayoutData = $derived(
		manifestDetail && (manifestDetail.gaAreas?.length > 0 || manifestDetail.rsAreas?.length > 0)
	);

	const seatR = 6;
	function seatColor(status: string) {
		if (status === 'Sold') return '#059669';
		if (status === 'Reserved') return '#D97706';
		if (status === 'Held') return '#F59E0B';
		if (status === 'Killed') return '#EF4444';
		return '#94A3B8';
	}
	
	function getSeatFill(seat: any, currentTab: string) {
		if (currentTab === 'holds') {
			// In Holds mode, emphasize ONLY holds and kills. Fade out Sold and Available.
			if (seat.status === 'Held') return '#F59E0B';
			if (seat.status === 'Killed') return '#EF4444';
			if (seat.status === 'Sold' || seat.status === 'Reserved') return '#CBD5E1'; // darker grey for unavailable seats
			return '#E2E8F0'; // light grey for available seats
		} else {
			// In Seats mode (Scaling), emphasize Price Level assignments
			const plId = seatPriceLevels[seat.id] || (seat.rowLetter < 'C' ? 'VIP' : 'GA');
			return basePriceLevels.find((p) => p.id === plId)?.color || '#e2e8f0';
		}
	}
	const seats = $derived.by(() => {
		if (hasLayoutData) return [];
		const result: {
			id: string;
			rowLetter: string;
			seatNum: number;
			status: string;
			x: number;
			y: number;
		}[] = [];
		const cap = selectedManifest?.totalCapacity || 500;
		const isLarge = cap > 5000;
		const rowCount = isLarge ? 6 : 4;
		const colsPerRow = isLarge ? 14 : 10;
		for (let r = 0; r < rowCount; r++) {
			const rowLetter = String.fromCharCode(65 + r);
			for (let c = 0; c < colsPerRow; c++) {
				const seatNum = c + 1;
				const id = `${rowLetter}-${seatNum}`;
				const isLeftBlock = c < colsPerRow / 2;
				const localIndex = isLeftBlock ? c : c - colsPerRow / 2;
				const aisleOffset = isLeftBlock ? 0 : isLarge ? 60 : 44;
				const x = 75 + localIndex * 32 + aisleOffset;
				const y = 80 + r * (isLarge ? 50 : 42);
				let status: string = 'Available';
				if (!isLarge) {
					if (r === 2 && c > 7) status = 'Held';
				} else {
					if (r === 4 && c < 2) status = 'Held';
				}
				status = seatStatuses[id] || status;
				result.push({ id, rowLetter, seatNum, status, x, y });
			}
		}
		return result;
	});
	const selectedSeatDetails = $derived(seats.filter((s) => selectedSeatIds.includes(s.id)));
	let seatPriceLevels = $state<Record<string, string>>({});
	let seatStatuses = $state<Record<string, string>>({});
	let seatHoldIds = $state<Record<string, string>>({});

	const selectedSeatsByPriceLevel = $derived.by(() => {
		const counts: Record<string, number> = {};
		selectedSeatDetails.forEach((s) => {
			const plId = seatPriceLevels[s.id] || (s.rowLetter < 'C' ? 'VIP' : 'GA');
			counts[plId] = (counts[plId] || 0) + 1;
		});
		return Object.entries(counts).map(([plId, count]) => ({
			pl: basePriceLevels.find((p) => p.id === plId) || basePriceLevels[0],
			count
		}));
	});
	const totalSeats = $derived(seats.length);
	const totalSold = $derived(seats.filter((s) => s.status === 'Sold').length);
	const totalReserved = $derived(
		seats.filter((s) => s.status === 'Reserved' || s.status === 'Held').length
	);
	const totalKilled = $derived(seats.filter((s) => s.status === 'Killed').length);
	let basePriceLevels = $state([
		{ id: 'VIP', color: '#7c3aed', label: 'VIP' },
		{ id: 'GA', color: '#059669', label: 'General Admission' }
	]);
	const PRESET_COLORS = [
		'#2563eb', // blue-600
		'#db2777', // pink-600
		'#0891b2', // cyan-600
		'#4f46e5', // indigo-600
		'#65a30d', // lime-600
		'#0d9488', // teal-600
		'#c026d3', // fuchsia-600
		'#0284c7', // sky-600
		'#047857', // emerald-700
		'#6d28d9'  // violet-700
	];
	function getNextPriceLevelColor() {
		const used = basePriceLevels.map(p => (p.color || '').toLowerCase());
		for (const color of PRESET_COLORS) {
			if (!used.includes(color.toLowerCase())) return color;
		}
		return PRESET_COLORS[basePriceLevels.length % PRESET_COLORS.length];
	}
	const priceLevels = $derived.by(() => {
		return basePriceLevels.map((pl) => {
			const plSeats = seats.filter(
				(s) => (seatPriceLevels[s.id] || (s.rowLetter < 'C' ? 'VIP' : 'GA')) === pl.id
			);
			return {
				...pl,
				avail: plSeats.filter((s) => s.status === 'Available').length,
				count: plSeats.length
			};
		});
	});

	function zoomIn() {
		canvasZoom = Math.min(3, canvasZoom + 0.25);
	}
	function zoomOut() {
		canvasZoom = Math.max(0.25, canvasZoom - 0.25);
	}
	function fitToView() {
		canvasZoom = 1;
	}

	function formatCurrency(amount: number) {
		if (amount === null || amount === undefined) return '';
		return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
	}

	function formatDateTime(isoString: string) {
		if (!isoString) return 'TBA';
		try {
			return new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(isoString));
		} catch (e) {
			return 'Invalid Date';
		}
	}

	function openAddOfferModal() {
		editingOffer = {
			name: '',
			description: '',
			ticketTypeId: 'tt-1', // default mock
			saleWindows: [{ type: 'GENERAL_SALE', startAt: '', endAt: '' }],
			currency: 'VND',
			faceValue: 200000,
			capacityCap: 300,
			eventTicketMinimum: 1,
			seatingMode: 'GA',
			priceLevelId: 'P1',
			restrictedPayment: false,
			active: true
		};
		isAddOfferModalOpen = true;
	}

	function openUpdateOfferModal(offer: any) {
		editingOffer = { ...offer };
		isAddOfferModalOpen = true;
	}

	function handleSaveOffer(e: Event) {
		e.preventDefault();
		if (!editingOffer?.name?.trim()) return;

		if (editingOffer.id) {
			localOffers = localOffers.map(o => o.id === editingOffer.id ? { ...editingOffer } : o);
		} else {
			const mockId = `offer-${Date.now()}`;
			localOffers = [
				...localOffers,
				{
					...editingOffer,
					id: mockId,
					quantitySold: 0
				}
			];
		}
		isAddOfferModalOpen = false;
	}

	function handleAddHold(e: Event) {
		e.preventDefault();
		if (!newHoldName.trim()) return;
		holds = [
			...holds,
			{
				id: `hold-${Date.now()}`,
				name: newHoldName,
				count: newHoldCount,
				reason: newHoldReason,
				status: newHoldType
			}
		];
		newHoldName = '';
		newHoldCount = 15;
		newHoldReason = '';
		newHoldType = 'LOCKED';
		isAddHoldModalOpen = false;
	}
</script>

<svelte:head>
	<title>{event.title} — Event Workspace</title>
</svelte:head>

{#if form?.error}
	<div class="alert alert-error">{form.error}</div>
{/if}
{#if form?.success}
	<div class="alert alert-success">Changes saved successfully.</div>
{/if}

<div class="page">
	<div class="tabs">
		<nav class="tab-nav">
			<button
				type="button"
				onclick={() => (activeTab = 'general')}
				class="tab-btn"
				class:active={activeTab === 'general'}
			>
				<span>General</span>
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'seats')}
				class="tab-btn"
				class:active={activeTab === 'seats'}
			>
				<span>Seat Map</span>
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'offers')}
				class="tab-btn"
				class:active={activeTab === 'offers'}
			>
				<span>Offers</span>
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'holds')}
				class="tab-btn"
				class:active={activeTab === 'holds'}
			>
				<span>Holds & Kills</span>
			</button>
		</nav>
	</div>

	<div class="content">
		{#if activeTab === 'general'}
			<div class="card">
				<form
					method="POST"
					action="?/updateEvent"
					use:enhance={() => {
						loading = true;
						return async ({ update }) => {
							await update();
							loading = false;
						};
					}}
					class="form"
				>
					<div class="field">
						<label for="edit-title" class="label">Event Title <span class="required">*</span></label
						>
						<input
							type="text"
							id="edit-title"
							name="title"
							required
							value={event.title}
							class="input"
						/>
					</div>
					<div class="field">
						<label for="edit-category" class="label">Classification</label>
						<Combobox
							items={sortedClassifications}
							bind:value={classificationId}
							name="classificationId"
							placeholder="Select a Category"
							searchPlaceholder="Search classification..."
							displayFn={(c) => {
								if (!c) return '';
								if (c.parentId) {
									const parent = sortedClassifications.find((p: any) => p.id === c.parentId);
									return parent ? `${parent.name} > ${c.name}` : c.name;
								}
								return c.name;
							}}
						>
							{#snippet itemSnippet(item)}
								<div class="flex w-full items-center">
									<span class={item?.parentId ? 'text-slate-600' : 'font-semibold text-slate-900'}>
										{#if item?.parentId}
											{@const parent = sortedClassifications.find((p: any) => p.id === item.parentId)}
											{parent ? `${parent.name} > ${item.name}` : item.name}
										{:else}
											{item?.name}
										{/if}
									</span>
								</div>
							{/snippet}
						</Combobox>
					</div>
					<div class="field">
						<label for="edit-attraction" class="label">Attraction</label>
						<Combobox
							items={attractions}
							bind:values={selectedAttractionIds}
							multiple={true}
							name="attractionIds"
							placeholder="Select an Attraction"
							searchPlaceholder="Search attraction..."
						>
							{#snippet itemSnippet(item)}
								<div class="flex items-center gap-2">
									<img
										src={item?.thumbnailUrl ||
											item?.imageUrl ||
											`https://ui-avatars.com/api/?name=${encodeURIComponent(item?.name || 'Unknown')}&background=random`}
										alt={item?.name || ''}
										class="h-6 w-6 rounded-full object-cover"
									/>
									<span>{item?.name || ''}</span>
								</div>
							{/snippet}
						</Combobox>
					</div>
					<div class="field">
						<label for="edit-venue" class="label">Venue</label>
						<Combobox
							items={data.venues || []}
							bind:value={venueId}
							name="venueId"
							placeholder="Select a Venue"
							searchPlaceholder="Search venue..."
							displayFn={(v) => v?.name || ''}
							searchFn={(items, query) => {
								const q = cleanVietnamese(query).toLowerCase();
								return items.filter(
									(v: any) =>
										!q ||
										cleanVietnamese(v.name || '')
											.toLowerCase()
											.includes(q)
								);
							}}
						>
							{#snippet itemSnippet(item)}
								<div class="flex items-center gap-2">
									<img
										src={item?.thumbnailUrl ||
											`https://ui-avatars.com/api/?name=${encodeURIComponent(item?.name || 'Unknown')}&background=random`}
										alt={item?.name || ''}
										class="h-6 w-6 rounded-full object-cover"
									/>
									<span>{item?.name || ''}</span>
								</div>
							{/snippet}
						</Combobox>
					</div>
					<div class="field-row">
						<div class="field">
							<label for="edit-start" class="label">Date<span class="required">*</span></label>
							<DateTimePicker
								name="startAt"
								bind:value={startAt}
								required={true}
								placeholder="Select date & time"
							/>
						</div>
						<div class="field">
							<label for="edit-tz" class="label">Timezone</label>
							<input
								type="text"
								id="edit-tz"
								name="timezone"
								value={event.timezone || 'Asia/Ho_Chi_Minh'}
								readonly
								class="input input-readonly"
							/>
						</div>
					</div>
					<div class="form-actions">
						<button type="submit" disabled={loading} class="btn-primary">
							{#if loading}
								<span class="spinner"></span>
							{/if}
							<span>Save Changes</span>
						</button>
					</div>
				</form>
			</div>
		{:else if activeTab === 'seats' || activeTab === 'holds'}
			<div class="venue-bar">
				<label for="seat-manifest" class="venue-label">Manifest</label>
				<select
					id="seat-manifest"
					name="manifestId"
					class="input venue-select"
					bind:value={selectedManifestId}
				>
					{#each data.manifests as manifest (manifest.id)}
						<option value={manifest.id}>
							{manifest.name}
						</option>
					{/each}
				</select>
				<div class="venue-actions">
					<span class="manifest-badge">{venueName || 'No manifest'}</span>
				</div>
			</div>

			<div
				class="relative flex min-h-[600px] flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"
			>
				<div class="relative flex min-h-0 flex-1">
					<aside class="flex w-[280px] shrink-0 flex-col border-r border-slate-200 bg-white">
						{#if activeTab === 'seats'}
							<div class="flex items-center justify-between border-b border-slate-200 px-4 py-2.5">
								<h3 class="text-[10px] font-black tracking-widest text-slate-400 uppercase">
									Price Levels
								</h3>
								<button
									type="button"
									onclick={() =>
										basePriceLevels.push({
											id: `PL-${Date.now()}`,
											color: getNextPriceLevelColor(),
											label: 'New Level'
										})}
									class="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
									aria-label="Add price level"
								>
									<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
										><path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2.5"
											d="M12 4v16m8-8H4"
										/></svg
									>
								</button>
							</div>
							<div class="flex-1 space-y-px overflow-y-auto px-3 py-2">
								<div class="flex items-center gap-2 rounded-lg px-2.5 py-2">
									<span
										class="flex h-4 w-4 shrink-0 items-center justify-center rounded-full border-2 border-slate-300 bg-white"
									></span>
									<span class="flex-1 text-sm font-medium text-slate-500">Unassigned</span>
									<span class="text-sm text-slate-500">0</span>
								</div>
								{#each basePriceLevels as bpl, i (bpl.id)}
									{@const pl = priceLevels[i]}
									<div
										class="group flex items-center gap-2 rounded-lg px-2.5 py-2 text-[11px] transition-colors hover:bg-slate-50"
										role="listitem"
										ondragover={(e) => {
											e.preventDefault();
											if (e.dataTransfer) e.dataTransfer.dropEffect = 'move';
										}}
										ondrop={(e) => {
											e.preventDefault();
											const data = e.dataTransfer?.getData('text/plain');
											if (data === 'selected_seats' && selectedSeatIds.length > 0) {
												const updated = { ...seatPriceLevels };
												selectedSeatIds.forEach((id) => {
													updated[id] = bpl.id;
												});
												seatPriceLevels = updated;
												selectedSeatIds = []; // clear selection after assign
											}
										}}
									>
										<label
											class="relative flex h-4 w-4 shrink-0 cursor-pointer items-center justify-center overflow-hidden rounded-full border border-white/50"
											style="background:{bpl.color}"
										>
											<input
												type="color"
												bind:value={bpl.color}
												class="absolute -inset-2 h-8 w-8 cursor-pointer opacity-0"
											/>
										</label>
										<div class="flex flex-1 items-center justify-between gap-2">
											<input
												type="text"
												bind:value={bpl.label}
												class="w-full border-none bg-transparent p-0 text-sm font-medium text-slate-500 outline-none focus:border-none focus:ring-0"
											/>
											<div class="text-sm text-slate-500">
												<span>{pl.count}</span>
											</div>
										</div>
										<button
											type="button"
											onclick={() => {
												basePriceLevels = basePriceLevels.filter((p) => p.id !== bpl.id);
											}}
											class="hidden h-5 w-5 items-center justify-center rounded text-slate-400 group-hover:flex hover:bg-slate-200 hover:text-red-500"
											aria-label="Remove {bpl.label}"
										>
											<svg class="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"
												><path
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M6 18L18 6M6 6l12 12"
												/></svg
											>
										</button>
									</div>
								{/each}
							</div>
						{/if}

						{#if activeTab === 'holds'}
							{#snippet holdItem(hold)}
								<div
									class="group flex items-center gap-2 rounded-lg px-2.5 py-2 transition-colors hover:bg-slate-50"
									ondragover={(e) => {
										e.preventDefault();
										if (e.dataTransfer) e.dataTransfer.dropEffect = 'move';
									}}
									ondrop={(e) => {
										e.preventDefault();
										const data = e.dataTransfer?.getData('text/plain');
										if (data === 'selected_seats' && selectedSeatIds.length > 0) {
											const updatedStatuses = { ...seatStatuses };
											const updatedHoldIds = { ...seatHoldIds };
											selectedSeatIds.forEach((id) => {
												updatedStatuses[id] = hold.status === 'LOCKED' ? 'Held' : 'Killed';
												updatedHoldIds[id] = hold.id;
											});
											seatStatuses = updatedStatuses;
											seatHoldIds = updatedHoldIds;
											selectedSeatIds = [];
										}
									}}
								>
									<button
										type="button"
										class="flex h-5 w-5 shrink-0 items-center justify-center rounded-full hover:bg-slate-200"
										onclick={(e) => {
											e.stopPropagation();
											selectedSeatIds = seats.filter(s => seatHoldIds[s.id] === hold.id).map(s => s.id);
										}}
										aria-label="Select seats in {hold.name}"
									>
										<span class="block h-3 w-3 rounded-full {hold.status === 'LOCKED' ? 'bg-amber-500' : 'bg-red-500'}"></span>
									</button>
									<div class="flex flex-1 items-center justify-between gap-2">
										<input
											type="text"
											bind:value={hold.name}
											class="w-full min-w-0 border-none bg-transparent p-0 text-sm font-medium text-slate-500 outline-none hover:text-slate-700 focus:text-slate-900 focus:ring-0"
										/>
										<div class="text-sm text-slate-500">
											<span>{hold.count === 0 ? 0 : hold.count + seats.filter(s => seatHoldIds[s.id] === hold.id).length}</span>
										</div>
									</div>
									<button
										type="button"
										onclick={() => {
											holds = holds.filter((h) => h.id !== hold.id);
										}}
										class="hidden h-5 w-5 items-center justify-center rounded text-slate-400 group-hover:flex hover:bg-slate-200 hover:text-red-500"
										aria-label="Remove {hold.name}"
									>
										<svg class="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"
											><path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M6 18L18 6M6 6l12 12"
											/></svg
										>
									</button>
								</div>
							{/snippet}

							<div class="flex items-center justify-between border-b border-slate-200 px-4 py-2.5">
								<h3 class="text-[10px] font-black tracking-widest text-slate-400 uppercase">
									Holds & Kills
								</h3>
							</div>
							<div class="flex-1 overflow-y-auto px-3 py-2">
								<div class="flex items-center justify-between px-2.5 py-1.5">
									<div class="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
										Holds
									</div>
									<button
										type="button"
										onclick={() => holds.push({ id: `hold_${Date.now()}`, name: 'New Hold', status: 'LOCKED', count: 0 })}
										class="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
										aria-label="Add hold"
									>
										<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
											><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 4v16m8-8H4" /></svg>
									</button>
								</div>
								<div class="space-y-px mb-2">
									{#each holds.filter(h => h.status === 'LOCKED') as hold (hold.id)}
										{@render holdItem(hold)}
									{/each}
								</div>

								<div class="flex items-center justify-between px-2.5 py-1.5 mt-2">
									<div class="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
										Kills
									</div>
									<button
										type="button"
										onclick={() => holds.push({ id: `kill_${Date.now()}`, name: 'New Kill', status: 'KILLED', count: 0 })}
										class="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
										aria-label="Add kill"
									>
										<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
											><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 4v16m8-8H4" /></svg>
									</button>
								</div>
								<div class="space-y-px">
									{#each holds.filter(h => h.status === 'KILLED') as hold (hold.id)}
										{@render holdItem(hold)}
									{/each}
								</div>
							</div>
						{/if}
						<div class="border-t border-slate-200 bg-slate-50/50 px-4 py-3">
							<h4 class="mb-2 text-[9px] font-black tracking-widest text-slate-400 uppercase">
								Financial Information
							</h4>
							<div class="space-y-1.5 text-[11px]">
								<div class="flex items-center justify-between">
									<span class="font-medium text-slate-500">Total Capacity</span>
									<span class="font-bold text-slate-800">{totalSeats}</span>
								</div>
								<div class="flex items-center justify-between">
									<span class="font-medium text-slate-500">Sold</span>
									<span class="font-bold text-emerald-700">{totalSold}</span>
								</div>
								<div class="flex items-center justify-between">
									<span class="font-medium text-slate-500">Reserved / Held</span>
									<span class="font-bold text-amber-600">{totalReserved}</span>
								</div>
								<div class="flex items-center justify-between">
									<span class="font-medium text-slate-500">Killed</span>
									<span class="font-bold text-slate-800">{totalKilled}</span>
								</div>
							</div>
						</div>
					</aside>

					<main
						class="relative flex flex-1 flex-col overflow-hidden bg-[#FAFAFA] select-none"
						style="cursor: {activeCanvasTool === 'pan' || isPanning ? 'grabbing' : 'default'}"
					>
						<div
							class="relative flex w-full flex-1 items-center justify-center overflow-hidden"
							role="application"
							aria-label="Seat map canvas"
							onwheel={handleWheel}
							onpointerdown={handlePointerDown}
							onpointermove={handlePointerMove}
							onpointerup={handlePointerUp}
							onpointercancel={handlePointerUp}
							oncontextmenu={(e) => e.preventDefault()}
						>
							<div
								style="transform: translate({panX}px, {panY}px) scale({canvasZoom}); transform-origin: center; pointer-events: {activeCanvasTool ===
								'pan'
									? 'none'
									: 'auto'};"
							>
								<svg viewBox="0 0 420 300" class="seating-canvas h-[300px] w-[420px]">
									<!-- Stage -->
									{#if selectedManifest?.objects}
										{#each selectedManifest.objects.filter((o: any) => o.type === 'stage') as obj (obj.type)}
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
												letter-spacing="2">{obj.text || 'STAGE'}</text
											>
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
											letter-spacing="2">STAGE</text
										>
									{/if}

									{#if hasLayoutData}
										<!-- GA Areas as colored blocks -->
										{#each manifestDetail.gaAreas || [] as ga (ga.id ?? ga.sectionId)}
											<rect
												x={ga.x || 55}
												y={ga.y || 60}
												width={ga.width || 310}
												height={ga.height || 180}
												rx="8"
												fill={manifestDetail.sections?.find((s: any) => s.id === ga.sectionId)
													?.color || '#EF4444'}
												opacity="0.15"
											/>
											<rect
												x={ga.x || 55}
												y={ga.y || 60}
												width={ga.width || 310}
												height={ga.height || 180}
												rx="8"
												fill="none"
												stroke={manifestDetail.sections?.find((s: any) => s.id === ga.sectionId)
													?.color || '#EF4444'}
												stroke-width="2"
												stroke-dasharray="6 3"
											/>
											<text
												x={(ga.x || 55) + (ga.width || 310) / 2}
												y={(ga.y || 60) + (ga.height || 180) / 2 - 8}
												text-anchor="middle"
												fill={manifestDetail.sections?.find((s: any) => s.id === ga.sectionId)
													?.color || '#EF4444'}
												font-size="12"
												font-weight="800"
												letter-spacing="1"
											>
												{manifestDetail.sections?.find((s: any) => s.id === ga.sectionId)?.name ||
													'GA'}
											</text>
											<text
												x={(ga.x || 55) + (ga.width || 310) / 2}
												y={(ga.y || 60) + (ga.height || 180) / 2 + 12}
												text-anchor="middle"
												fill={manifestDetail.sections?.find((s: any) => s.id === ga.sectionId)
													?.color || '#EF4444'}
												font-size="10"
												font-weight="600"
												opacity="0.7"
											>
												{(ga.capacity || 0).toLocaleString()} standing
											</text>
										{/each}

										<!-- RS Areas as bordered sections -->
										{#each manifestDetail.rsAreas || [] as rs (rs.id ?? rs.sectionId)}
											<rect
												x={rs.x || 55}
												y={rs.y || 60}
												width={rs.width || 310}
												height={rs.height || 90}
												rx="6"
												fill={manifestDetail.sections?.find((s: any) => s.id === rs.sectionId)
													?.color
													? manifestDetail.sections.find((s: any) => s.id === rs.sectionId).color +
														'15'
													: '#f5f3ff'}
												stroke={manifestDetail.sections?.find((s: any) => s.id === rs.sectionId)
													?.color || '#e9d5ff'}
												stroke-width="1"
											/>
											<text
												x={(rs.x || 55) + 10}
												y={(rs.y || 60) + 15}
												fill={manifestDetail.sections?.find((s: any) => s.id === rs.sectionId)
													?.color || '#7c3aed'}
												font-size="8"
												font-weight="700"
												letter-spacing="1"
											>
												{manifestDetail.sections?.find((s: any) => s.id === rs.sectionId)?.name ||
													'RS'}
											</text>
											<!-- Render seats within RS area -->
											{#each rs.rows || [] as row (row.id ?? row.label)}
												{#each row.seats || [] as seat (seat.id)}
													<circle
														cx={seat.positionX || 0}
														cy={seat.positionY || 0}
														r={seatR}
														fill={getSeatFill(seat, activeTab)}
														stroke="none"
														class="cursor-pointer transition-all hover:opacity-80"
													/>
													{#if selectedSeatIds.includes(seat.id)}
														<circle
															cx={seat.positionX || 0}
															cy={seat.positionY || 0}
															r={seatR - 0.75}
															fill="none"
															stroke="#000000"
															stroke-width="1.5"
															class="pointer-events-none"
														/>
													{/if}
												{/each}
											{/each}
										{/each}

										<!-- Fallback: no areas at all, show empty state -->
										{#if (manifestDetail.gaAreas || []).length === 0 && (manifestDetail.rsAreas || []).length === 0}
											<text x="210" y="160" text-anchor="middle" fill="#94a3b8" font-size="12"
												>No layout areas defined</text
											>
										{/if}
									{:else}
										<!-- Fallback: no manifest detail, show hardcoded grid -->
										<rect
											x="55"
											y="62"
											width="310"
											height="90"
											rx="6"
											fill="#f5f3ff"
											stroke="#e9d5ff"
											stroke-width="1"
										/>
										<rect
											x="55"
											y="165"
											width="310"
											height="90"
											rx="6"
											fill="#ecfdf5"
											stroke="#a7f3d0"
											stroke-width="1"
										/>
										<text
											x="65"
											y="77"
											fill="#7c3aed"
											font-size="8"
											font-weight="700"
											letter-spacing="1">VIP</text
										>
										<text
											x="65"
											y="180"
											fill="#059669"
											font-size="8"
											font-weight="700"
											letter-spacing="1">GA</text
										>
										{#each ['A', 'B', 'C', 'D'] as letter, i (letter)}
											<text
												x="70"
												y={86 + i * 42 + 5}
												text-anchor="end"
												fill="#94a3b8"
												font-size="10"
												font-weight="700">{letter}</text
											>
										{/each}
										{#each seats as seat (seat.id)}
											<circle
												cx={seat.x}
												cy={seat.y}
												r={seatR}
												fill={getSeatFill(seat, activeTab)}
												stroke="none"
												oncontextmenu={(e) => {
													e.preventDefault();
													e.stopPropagation();
													let idsToToggle = [seat.id];
													if (selectionTool === 'row')
														idsToToggle = seats
															.filter((s) => s.rowLetter === seat.rowLetter)
															.map((s) => s.id);
													else if (selectionTool === 'section')
														idsToToggle = seats
															.filter((s) => s.rowLetter < 'C' === seat.rowLetter < 'C')
															.map((s) => s.id);

													const allSelected = idsToToggle.every((id) =>
														selectedSeatIds.includes(id)
													);
													if (allSelected) {
														selectedSeatIds = selectedSeatIds.filter(
															(id) => !idsToToggle.includes(id)
														);
													} else {
														selectedSeatIds = [...new Set([...selectedSeatIds, ...idsToToggle])];
													}
												}}
												role="button"
												tabindex="0"
												onkeydown={(e) => e.key === 'Enter' && (selectedSeatIds = [seat.id])}
												aria-label="Seat {seat.id} ({seat.status})"
												class="cursor-pointer transition-all outline-none hover:opacity-80 focus:outline-none"
											/>
											{#if selectedSeatIds.includes(seat.id)}
												<circle
													cx={seat.x}
													cy={seat.y}
													r={seatR - 0.75}
													fill="none"
													stroke="#000000"
													stroke-width="1.5"
													class="pointer-events-none"
												/>
											{/if}
										{/each}
									{/if}
								</svg>
							</div>
						</div>

						<div
							class="absolute right-4 bottom-4 z-30 flex flex-col items-center gap-0.5 rounded-xl border border-slate-200/80 bg-white/90 p-0.5 shadow-lg backdrop-blur-md"
						>
							<button
								type="button"
								onclick={zoomIn}
								class="flex h-7 w-7 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100"
								title="Zoom In"
							>
								<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
									><path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2.5"
										d="M12 4v16m8-8H4"
									/></svg
								>
							</button>
							<button
								type="button"
								onclick={zoomOut}
								class="flex h-7 w-7 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100"
								title="Zoom Out"
							>
								<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
									><path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2.5"
										d="M20 12H4"
									/></svg
								>
							</button>
							<div class="h-px w-4 bg-slate-200"></div>
							<button
								type="button"
								onclick={fitToView}
								class="flex h-7 w-7 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100"
								title="Fit to View"
							>
								<svg class="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"
									><path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4"
									/></svg
								>
							</button>
						</div>

						<!-- Mini-map -->
						<div
							class="absolute right-16 bottom-4 z-30 flex h-32 w-44 flex-col overflow-hidden rounded-xl border border-slate-200/80 bg-white/90 p-2 shadow-lg backdrop-blur-md"
						>
							<div
								class="flex items-center justify-between pb-1 text-[8px] font-bold tracking-wider text-slate-400 uppercase"
							>
								<span>Map</span>
								<span class="rounded bg-slate-100 px-1 py-0.5 font-mono text-[7px] text-slate-500"
									>{Math.round(canvasZoom * 100)}%</span
								>
							</div>
							<div
								class="relative w-full flex-1 overflow-hidden rounded-lg border border-slate-200/60 bg-slate-50"
							>
								<svg viewBox="0 0 420 300" class="pointer-events-none h-full w-full">
									<rect
										x="55"
										y="62"
										width="310"
										height="90"
										rx="6"
										fill="#e9d5ff"
										fill-opacity="0.3"
										stroke="#7c3aed"
										stroke-width="2"
									/>
									<rect
										x="55"
										y="165"
										width="310"
										height="90"
										rx="6"
										fill="#a7f3d0"
										fill-opacity="0.3"
										stroke="#059669"
										stroke-width="2"
									/>
									{#each seats as seat (seat.id)}
										<circle
											cx={seat.x}
											cy={seat.y}
											r="2"
											fill={seat.status === 'Available'
												? '#94a3b8'
												: seat.status === 'Sold'
													? '#059669'
													: seat.status === 'Reserved'
														? '#d97706'
														: '#dc2626'}
										/>
									{/each}
								</svg>
							</div>
						</div>

						{#if selectedSeatDetails.length > 0}
							<div
								draggable="true"
								ondragstart={(e) => {
									e.dataTransfer?.setData('text/plain', 'selected_seats');
									if (e.dataTransfer) e.dataTransfer.effectAllowed = 'move';
								}}
								role="region"
								aria-label="Selected seats"
								class="absolute top-3 left-3 z-30 flex min-w-[200px] cursor-grab flex-col rounded-xl border border-slate-200 bg-white/95 shadow-lg backdrop-blur-md active:cursor-grabbing"
							>
								<div class="flex items-center justify-between border-b border-slate-100 px-3 py-2">
									<div class="flex items-center gap-2.5">
										<div
											class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[#0070F3] text-white shadow-sm"
										>
											<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
												><path
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z"
												/></svg
											>
										</div>
										<span class="text-xl font-semibold text-slate-700"
											>{selectedSeatDetails.length}</span
										>
									</div>
									<button
										type="button"
										onclick={() => (selectedSeatIds = [])}
										class="text-xs font-medium text-[#0070F3] transition-colors hover:text-blue-700"
										>Clear</button
									>
								</div>

								<div class="flex flex-col gap-1.5 p-2 text-xs">
									{#each selectedSeatsByPriceLevel as item (item.pl?.id)}
										<div class="flex items-center justify-between text-slate-600">
											<div class="flex items-center gap-2">
												<span
													class="flex h-3.5 w-3.5 shrink-0 items-center justify-center rounded-full text-white"
													style="background:{item.pl?.color}"
												>
													<svg
														class="ml-px h-2 w-2"
														fill="none"
														stroke="currentColor"
														viewBox="0 0 24 24"
														><path
															stroke-linecap="round"
															stroke-linejoin="round"
															stroke-width="3.5"
															d="M9 5l7 7-7 7"
														/></svg
													>
												</span>
												<span class="text-[11px] font-medium text-slate-500"
													>{item.pl?.label || item.pl?.id}</span
												>
											</div>
											<span class="text-[11px] font-bold text-slate-500">{item.count}</span>
										</div>
									{/each}
								</div>
							</div>
						{/if}
					</main>

					<aside
						class="z-10 flex w-[56px] shrink-0 flex-col items-center gap-0.5 border-l border-slate-200 bg-white py-3 shadow-sm"
					>
						<button
							onclick={() => {
								activeCanvasTool = 'select';
								selectionTool = 'seat';
							}}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold transition-colors {activeCanvasTool ===
								'select' && selectionTool === 'seat'
								? 'bg-slate-100 text-slate-900'
								: 'text-slate-400 hover:bg-slate-50 hover:text-slate-700'}"
						>
							<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="1.5"
									d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5"
								/></svg
							>
							Seat
						</button>
						<button
							onclick={() => {
								activeCanvasTool = 'select';
								selectionTool = 'row';
							}}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold transition-colors {activeCanvasTool ===
								'select' && selectionTool === 'row'
								? 'bg-slate-100 text-slate-900'
								: 'text-slate-400 hover:bg-slate-50 hover:text-slate-700'}"
						>
							<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="1.5"
									d="M4 6h16M4 12h16M4 18h16"
								/></svg
							>
							Row
						</button>
						<button
							onclick={() => {
								activeCanvasTool = 'select';
								selectionTool = 'section';
							}}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold transition-colors {activeCanvasTool ===
								'select' && selectionTool === 'section'
								? 'bg-slate-100 text-slate-900'
								: 'text-slate-400 hover:bg-slate-50 hover:text-slate-700'}"
						>
							<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="1.5"
									d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"
								/></svg
							>
							Section
						</button>
						<button
							onclick={() => {
								activeCanvasTool = 'pan';
							}}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold transition-colors {activeCanvasTool ===
							'pan'
								? 'bg-slate-100 text-slate-900'
								: 'text-slate-400 hover:bg-slate-50 hover:text-slate-700'}"
						>
							<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="1.5"
									d="M5 15l7-7 7 7"
								/></svg
							>
							Pan
						</button>
						<div class="my-1.5 h-px w-8 bg-slate-100"></div>
						<button
							onclick={() => (showFilterDialog = !showFilterDialog)}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold text-slate-400 transition-colors hover:bg-slate-50 hover:text-slate-700"
						>
							<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="1.5"
									d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
								/></svg
							>
							Filters
						</button>
						<button
							onclick={() => (showAssistantDialog = !showAssistantDialog)}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold text-slate-400 transition-colors hover:bg-slate-50 hover:text-slate-700"
						>
							<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="1.5"
									d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
								/></svg
							>
							Assist
						</button>
					</aside>
				</div>
			</div>
		{:else if activeTab === 'offers'}
			<div class="card">
				<div class="flex items-center justify-end">
					<button
						type="button"
						onclick={openAddOfferModal}
						class="flex items-center gap-2 rounded-none bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-all duration-200 hover:bg-blue-500 hover:shadow-md"
					>
						<IconPlus size={16} />
						<span>Add Offer Tier</span>
					</button>
				</div>

				<div class="mt-6 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
					<table class="min-w-full divide-y divide-slate-200 text-left text-sm">
						<thead class="bg-slate-50">
							<tr>
								<th scope="col" class="px-6 py-4 font-semibold text-slate-900">Offer Name</th>
								<th scope="col" class="px-6 py-4 font-semibold text-slate-900">Ticket Type</th>
								<th scope="col" class="px-6 py-4 font-semibold text-slate-900">Price (VND)</th>
								<th scope="col" class="px-6 py-4 font-semibold text-slate-900">Config</th>
								<th scope="col" class="px-6 py-4 font-semibold text-slate-900">Sale Window</th>
								<th scope="col" class="px-6 py-4 font-semibold text-slate-900">Status</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-slate-200 bg-white">
							{#each localOffers as offer (offer.id)}
								<tr class="transition-colors hover:bg-slate-50">
									<td class="whitespace-nowrap px-6 py-4 font-bold text-slate-900">
										<button type="button" class="hover:text-blue-600 hover:underline" onclick={() => openUpdateOfferModal(offer)}>
											{offer.name}
										</button>
										{#if offer.description}
											<div class="text-xs font-normal text-slate-500 max-w-[200px] truncate">{offer.description}</div>
										{/if}
									</td>
									<td class="whitespace-nowrap px-6 py-4 text-sm font-medium text-slate-700">
										<span class="inline-flex rounded-md bg-slate-100 px-2 py-1 text-xs font-semibold text-slate-700 ring-1 ring-inset ring-slate-200">{offer.ticketTypeId === 'tt-1' ? 'General Admission' : (offer.ticketTypeId === 'tt-2' ? 'VIP' : 'Standard')}</span>
									</td>
									<td class="whitespace-nowrap px-6 py-4 font-black tracking-tight text-blue-600">{formatCurrency(offer.faceValue ?? offer.price)}</td>
									<td class="whitespace-nowrap px-6 py-4 text-sm text-slate-600">
										<div class="font-semibold text-slate-900">{offer.seatingMode === 'RESERVED' ? 'Reserved Seating' : 'General Admission'}</div>
										<div class="text-xs">
											Price Level:
											{#if offer.priceLevelId}
												<span class="inline-flex items-center gap-1.5 rounded-md px-1.5 py-0.5 text-[10px] font-bold ring-1 ring-inset ring-slate-200" style="color: {basePriceLevels.find(p => p.id === offer.priceLevelId)?.color || 'inherit'}">
													{basePriceLevels.find(p => p.id === offer.priceLevelId)?.label || offer.priceLevelId}
												</span>
											{:else}
												None
											{/if}
										</div>
										<div class="text-xs mt-0.5">Min/Max: {offer.eventTicketMinimum || 1}/{offer.capacityCap || '∞'}</div>
									</td>
									<td class="whitespace-nowrap px-6 py-4 text-sm text-slate-600">
										{#if offer.saleWindows && offer.saleWindows.length > 0}
											<div class="font-semibold text-slate-900">{offer.saleWindows[0].type === 'PRESALE' ? 'Pre-Sale' : 'General Sale'}</div>
											<div class="text-xs">{formatDateTime(offer.saleWindows[0].startAt)} - {formatDateTime(offer.saleWindows[0].endAt)}</div>
										{:else}
											<span class="text-xs text-slate-400">Not configured</span>
										{/if}
									</td>
									<td class="whitespace-nowrap px-6 py-4">
										{#if offer.active ?? true}
											<span class="inline-flex items-center rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-600 ring-1 ring-emerald-500/20">Active</span>
										{:else}
											<span class="inline-flex items-center rounded-full bg-slate-50 px-2 py-0.5 text-xs font-medium text-slate-500 ring-1 ring-slate-200">Inactive</span>
										{/if}
									</td>
								</tr>
							{:else}
								<tr>
									<td colspan="4" class="px-6 py-16 text-center">
										<div class="mx-auto mb-3 w-12 rounded-full bg-slate-100 p-3 text-slate-400">
											<IconPlus size={24} />
										</div>
										<h3 class="text-sm font-bold text-slate-900">No offers yet</h3>
										<p class="mt-1 text-sm text-slate-500">Create your first pricing tier to start selling tickets.</p>
										<button type="button" onclick={openAddOfferModal} class="mt-4 text-sm font-semibold text-blue-600 hover:text-blue-700">
											+ Add Offer Tier
										</button>
									</td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			</div>
		{/if}
	</div>
</div>

{#if isAddOfferModalOpen}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4 transition-all backdrop-blur-sm" role="dialog">
		<div class="w-full max-w-md overflow-hidden rounded-2xl bg-white shadow-2xl ring-1 ring-slate-200">
			<div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
				<h3 class="text-lg font-bold tracking-tight text-slate-900">{editingOffer?.id ? 'Update Pricing Tier' : 'Add New Pricing Tier'}</h3>
				<button type="button" onclick={() => (isAddOfferModalOpen = false)} class="rounded-full p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600">
					<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
				</button>
			</div>
			<form onsubmit={handleSaveOffer} class="p-6">
				<div class="space-y-5">
					<div class="grid grid-cols-2 gap-4">
						<div>
							<label for="offer-name" class="mb-1.5 block text-sm font-semibold text-slate-700">Offer Name <span class="text-red-500">*</span></label>
							<input
								type="text"
								id="offer-name"
								bind:value={editingOffer.name}
								required
								placeholder="e.g. VIP Early Bird"
								class="block w-full rounded-lg border-0 px-3.5 py-2.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm"
							/>
						</div>
						<div>
							<label for="offer-desc" class="mb-1.5 block text-sm font-semibold text-slate-700">Description</label>
							<input
								type="text"
								id="offer-desc"
								bind:value={editingOffer.description}
								placeholder="Optional description"
								class="block w-full rounded-lg border-0 px-3.5 py-2.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm"
							/>
						</div>
					</div>
					<div class="grid grid-cols-2 gap-4">
						<div>
							<label for="ticket-type" class="mb-1.5 block text-sm font-semibold text-slate-700">Ticket Type <span class="text-red-500">*</span></label>
							<select
								id="ticket-type"
								bind:value={editingOffer.ticketTypeId}
								required
								class="block w-full rounded-lg border-0 px-3.5 py-2.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm"
							>
								<option value="tt-1">General Admission</option>
								<option value="tt-2">VIP</option>
							</select>
						</div>
						<div>
							<label for="price-level" class="mb-1.5 block text-sm font-semibold text-slate-700">Price Level</label>
							<select
								id="price-level"
								bind:value={editingOffer.priceLevelId}
								class="block w-full rounded-lg border-0 px-3.5 py-2.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm"
							>
								<option value="">None (Applies to all)</option>
								{#each basePriceLevels as pl}
									<option value={pl.id}>{pl.label}</option>
								{/each}
							</select>
						</div>
					</div>
					<div class="grid grid-cols-2 gap-4">
						<div>
							<label for="offer-price" class="mb-1.5 block text-sm font-semibold text-slate-700">Price (VND) <span class="text-red-500">*</span></label>
							<input
								type="number"
								id="offer-price"
								bind:value={editingOffer.faceValue}
								required
								min="0"
								class="block w-full rounded-lg border-0 px-3.5 py-2.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm"
							/>
						</div>
						<div>
							<label for="offer-limit" class="mb-1.5 block text-sm font-semibold text-slate-700">Capacity Cap <span class="text-red-500">*</span></label>
							<input
								type="number"
								id="offer-limit"
								bind:value={editingOffer.capacityCap}
								required
								min="1"
								class="block w-full rounded-lg border-0 px-3.5 py-2.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm"
							/>
						</div>
					</div>
					<div class="grid grid-cols-2 gap-4">
						<div>
							<label for="offer-min" class="mb-1.5 block text-sm font-semibold text-slate-700">Min Tickets/Order</label>
							<input
								type="number"
								id="offer-min"
								bind:value={editingOffer.eventTicketMinimum}
								min="1"
								class="block w-full rounded-lg border-0 px-3.5 py-2.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm"
							/>
						</div>
						<div>
							<label for="offer-mode" class="mb-1.5 block text-sm font-semibold text-slate-700">Seating Mode</label>
							<select
								id="offer-mode"
								bind:value={editingOffer.seatingMode}
								class="block w-full rounded-lg border-0 px-3.5 py-2.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm"
							>
								<option value="GA">General Admission</option>
								<option value="RESERVED">Reserved Seating</option>
							</select>
						</div>
					</div>
					
					<div class="rounded-xl border border-slate-200 bg-slate-50 p-4">
						<h4 class="mb-3 text-sm font-bold text-slate-900">Sale Window</h4>
						<div class="space-y-4">
							{#if editingOffer.saleWindows && editingOffer.saleWindows.length > 0}
								<div>
									<label for="sale-type" class="mb-1.5 block text-xs font-semibold text-slate-700">Window Type</label>
									<select
										id="sale-type"
										bind:value={editingOffer.saleWindows[0].type}
										class="block w-full rounded-md border-0 px-3 py-2 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm"
									>
										<option value="PRESALE">Pre-Sale</option>
										<option value="GENERAL_SALE">General Sale</option>
									</select>
								</div>
								<div class="grid grid-cols-2 gap-3">
									<div>
										<label for="sale-start" class="mb-1.5 block text-xs font-semibold text-slate-700">Start Time</label>
										<input type="datetime-local" id="sale-start" bind:value={editingOffer.saleWindows[0].startAt} class="block w-full rounded-md border-0 px-3 py-2 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm" />
									</div>
									<div>
										<label for="sale-end" class="mb-1.5 block text-xs font-semibold text-slate-700">End Time</label>
										<input type="datetime-local" id="sale-end" bind:value={editingOffer.saleWindows[0].endAt} class="block w-full rounded-md border-0 px-3 py-2 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm" />
									</div>
								</div>
							{:else}
								<button type="button" class="text-sm font-semibold text-blue-600 hover:text-blue-700" onclick={() => editingOffer.saleWindows = [{ type: 'GENERAL_SALE', startAt: '', endAt: '' }]}>
									+ Add Sale Window
								</button>
							{/if}
						</div>
					</div>

					<div class="flex items-center gap-6">
						<label class="flex cursor-pointer items-center gap-3 transition-colors hover:opacity-80">
							<input type="checkbox" bind:checked={editingOffer.active} class="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-600" />
							<span class="text-sm font-medium text-slate-700">Active for sales</span>
						</label>
						<label class="flex cursor-pointer items-center gap-3 transition-colors hover:opacity-80">
							<input type="checkbox" bind:checked={editingOffer.restrictedPayment} class="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-600" />
							<span class="text-sm font-medium text-slate-700">Restricted Payment</span>
						</label>
					</div>
				</div>
				<div class="mt-8 flex items-center justify-end gap-3">
					<button type="button" onclick={() => (isAddOfferModalOpen = false)} class="rounded-lg px-4 py-2.5 text-sm font-semibold text-slate-600 transition-colors hover:bg-slate-100">
						Cancel
					</button>
					<button type="submit" class="rounded-lg bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-all hover:bg-blue-500 hover:shadow-md">
						{editingOffer?.id ? 'Save Changes' : 'Add Tier'}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

{#if isAddHoldModalOpen}
	<div class="modal-overlay" role="dialog">
		<div class="modal">
			<div class="modal-header">
				<h3 class="modal-title">Allocate Locked Seats Block</h3>
				<button type="button" onclick={() => (isAddHoldModalOpen = false)} class="modal-close"
					>&times;</button
				>
			</div>
			<form onsubmit={handleAddHold} class="modal-form">
				<div class="field">
					<label for="hold-name" class="label">Block Name <span class="required">*</span></label>
					<input
						type="text"
						id="hold-name"
						bind:value={newHoldName}
						required
						placeholder="e.g. VIP Sponsor Allocation"
						class="input"
					/>
				</div>
				<div class="field-row">
					<div class="field">
						<label for="hold-cnt" class="label">Seat Qty <span class="required">*</span></label>
						<input
							type="number"
							id="hold-cnt"
							bind:value={newHoldCount}
							required
							min="1"
							class="input"
						/>
					</div>
					<div class="field">
						<label for="hold-type" class="label">Lock Type <span class="required">*</span></label>
						<select id="hold-type" bind:value={newHoldType} class="input">
							<option value="LOCKED">Held (Locked)</option>
							<option value="KILLED">Killed (Obstruction)</option>
						</select>
					</div>
				</div>
				<div class="field">
					<label for="hold-reason" class="label">Reason</label>
					<input
						type="text"
						id="hold-reason"
						bind:value={newHoldReason}
						placeholder="e.g. Partner VIP list block allocation"
						class="input"
					/>
				</div>
				<div class="modal-actions">
					<button type="button" onclick={() => (isAddHoldModalOpen = false)} class="btn-ghost"
						>Cancel</button
					>
					<button type="submit" class="btn-primary">Allocate Block</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<style>
	:root {
		--bg: #ffffff;
		--surface: #ffffff;
		--fg: #171717;
		--fg-2: #4d4d4d;
		--muted: #666666;
		--meta: #808080;
		--border: rgba(0, 0, 0, 0.08);
		--border-soft: rgba(0, 0, 0, 0.04);
		--accent: #0070f3;
		--accent-on: #ffffff;
		--accent-hover: #005bb5;
		--font-display: 'Geist', 'Geist Sans', -apple-system, 'Segoe UI', Arial, sans-serif;
		--font-body: 'Geist', 'Geist Sans', -apple-system, 'Segoe UI', Arial, sans-serif;
		--font-mono:
			'Geist Mono', ui-monospace, 'SF Mono', 'Roboto Mono', Menlo, Monaco, 'Liberation Mono',
			'DejaVu Sans Mono', 'Courier New', monospace;
		--text-xs: 12px;
		--text-sm: 14px;
		--text-base: 16px;
		--text-lg: 20px;
		--text-xl: 24px;
		--text-2xl: 32px;
		--text-3xl: 40px;
		--text-4xl: 48px;
		--leading-body: 1.5;
		--leading-tight: 1.1;
		--tracking-display: -0.05em;
		--radius-sm: 6px;
		--radius-md: 8px;
		--radius-lg: 12px;
		--radius-pill: 9999px;
		--elev-flat: none;
		--elev-ring: 0 0 0 1px rgba(0, 0, 0, 0.08);
		--elev-raised:
			0 0 0 1px rgba(0, 0, 0, 0.08), 0 2px 2px rgba(0, 0, 0, 0.04),
			0 8px 8px -8px rgba(0, 0, 0, 0.04), 0 0 0 1px #fafafa;
		--focus-ring: 0 0 0 2px var(--accent);
		--motion-fast: 150ms;
		--motion-base: 200ms;
		--ease-standard: cubic-bezier(0.2, 0, 0, 1);
		--container-max: 1200px;
		--container-gutter: 24px;
	}

	.page {
		max-width: var(--container-max);
		margin: 0 auto;
		padding: 32px var(--container-gutter);
		display: flex;
		flex-direction: column;
		gap: var(--space-6, 24px);
	}

	.badge {
		display: inline-flex;
		align-items: center;
		padding: 2px 10px;
		border-radius: var(--radius-pill);
		font-size: 11px;
		font-weight: 500;
		background: #ebebeb;
		color: var(--fg);
	}
	.badge-green {
		background: #ecfdf5;
		color: #059669;
	}
	.badge-red {
		background: #fef2f2;
		color: #dc2626;
	}
	.badge-amber {
		background: #fffbeb;
		color: #d97706;
	}
	.btn-primary {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		padding: 8px 18px;
		border-radius: var(--radius-sm);
		background: var(--fg);
		color: #ffffff;
		font-size: var(--text-sm);
		font-weight: 500;
		font-family: var(--font-body);
		text-decoration: none;
		border: none;
		cursor: pointer;
		transition: opacity var(--motion-fast) var(--ease-standard);
		white-space: nowrap;
	}
	.btn-primary:hover {
		opacity: 0.85;
	}
	.btn-primary:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	.btn-secondary {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		padding: 7px 16px;
		border-radius: var(--radius-sm);
		background: var(--bg);
		box-shadow: var(--elev-ring);
		color: var(--fg);
		font-size: var(--text-sm);
		font-weight: 500;
		font-family: var(--font-body);
		border: none;
		cursor: pointer;
		transition: background var(--motion-fast) var(--ease-standard);
	}
	.btn-secondary:hover {
		background: #f5f5f5;
	}

	.btn-ghost {
		display: inline-flex;
		align-items: center;
		padding: 8px 20px;
		border-radius: var(--radius-sm);
		background: transparent;
		box-shadow: var(--elev-ring);
		color: var(--fg-2);
		font-size: var(--text-sm);
		font-weight: 500;
		font-family: var(--font-body);
		border: none;
		cursor: pointer;
		transition: background var(--motion-fast) var(--ease-standard);
	}
	.btn-ghost:hover {
		background: #f5f5f5;
	}

	.spinner {
		display: inline-block;
		width: 14px;
		height: 14px;
		border: 2px solid rgba(255, 255, 255, 0.3);
		border-top-color: #fff;
		border-radius: 50%;
		animation: spin 0.5s linear infinite;
	}
	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}

	.alert {
		padding: 10px 14px;
		border-radius: var(--radius-sm);
		font-size: var(--text-sm);
		font-weight: 500;
		box-shadow: var(--elev-ring);
	}
	.alert-error {
		background: #fef2f2;
		color: #dc2626;
	}
	.alert-success {
		background: #ecfdf5;
		color: #059669;
	}

	.tabs {
		box-shadow: 0 1px 0 0 var(--border);
	}
	.tab-nav {
		display: flex;
		overflow: hidden;
		flex-wrap: wrap;
	}
	.tab-btn {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		padding: 10px 16px;
		margin-right: 2px;
		font-size: var(--text-sm);
		font-weight: 500;
		color: var(--muted);
		background: transparent;
		border: none;
		box-shadow: 0 2px 0 0 transparent;
		cursor: pointer;
		transition:
			color var(--motion-fast) var(--ease-standard),
			box-shadow var(--motion-fast) var(--ease-standard);
		white-space: nowrap;
		font-family: var(--font-body);
	}
	.tab-btn:hover {
		color: var(--fg);
	}
	.tab-btn.active {
		color: #2563eb;
		box-shadow: 0 2px 0 0 #2563eb;
	}

	.content {
		display: flex;
		flex-direction: column;
		gap: 24px;
	}

	.card {
		background: var(--surface);
		border-radius: var(--radius-md);
		padding: 24px;
		box-shadow: var(--elev-ring);
	}
	.card-title {
		font-size: var(--text-base);
		font-weight: 600;
		color: var(--fg);
		margin: 0 0 20px;
		letter-spacing: -0.02em;
		font-family: var(--font-display);
	}

	.form {
		display: flex;
		flex-direction: column;
		gap: 16px;
	}
	.field {
		display: flex;
		flex-direction: column;
		gap: 6px;
	}
	.field-row {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 16px;
	}

	@media (max-width: 640px) {
		.field-row {
			grid-template-columns: 1fr;
		}
	}
	.label {
		font-size: var(--text-xs);
		font-weight: 600;
		color: var(--fg);
	}
	.required {
		color: #dc2626;
	}
	.input {
		width: 100%;
		padding: 8px 12px;
		border-radius: var(--radius-sm);
		background: var(--bg);
		box-shadow: 0 0 0 1px var(--border);
		font-size: var(--text-sm);
		color: var(--fg);
		outline: none;
		transition: box-shadow var(--motion-fast) var(--ease-standard);
		box-sizing: border-box;
		font-family: var(--font-body);
		border: none;
	}
	.input:focus {
		box-shadow: 0 0 0 2px var(--accent);
	}
	.input-readonly {
		background: #f5f5f5;
		color: var(--muted);
		cursor: not-allowed;
	}
	.dropdown-trigger {
		display: flex !important;
		align-items: center;
		justify-content: space-between;
		gap: 8px;
		cursor: pointer;
		text-align: left;
		font-family: var(--font-body);
	}
	.dropdown-trigger .placeholder {
		color: var(--muted);
		font-weight: 400;
	}
	.chevron {
		flex-shrink: 0;
		color: var(--meta);
		transition: transform var(--motion-fast) var(--ease-standard);
	}
	.chevron.rotated {
		transform: rotate(180deg);
	}
	.dropdown-backdrop {
		position: fixed;
		inset: 0;
		z-index: 45;
		background: transparent;
		border: none;
		padding: 0;
		cursor: default;
	}
	.dropdown-panel {
		position: absolute;
		left: 0;
		right: 0;
		z-index: 50;
		margin-top: 4px;
		border-radius: var(--radius-md);
		background: #fff;
		box-shadow: var(--elev-raised);
		overflow: hidden;
	}
	.dropdown-search {
		display: flex;
		align-items: center;
		gap: 8px;
		padding: 8px 10px;
		border-bottom: 1px solid var(--border);
	}
	.search-icon {
		flex-shrink: 0;
		color: var(--meta);
	}
	.search-input {
		flex: 1;
		border: none;
		outline: none;
		font-size: var(--text-sm);
		color: var(--fg);
		background: transparent;
		font-family: var(--font-body);
	}
	.search-input::placeholder {
		color: var(--meta);
	}
	.dropdown-list {
		max-height: 192px;
		overflow-y: auto;
		padding: 4px;
	}
	.dropdown-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		width: 100%;
		padding: 8px 10px;
		border-radius: var(--radius-sm);
		border: none;
		background: transparent;
		font-size: var(--text-sm);
		color: var(--fg);
		cursor: pointer;
		text-align: left;
		font-family: var(--font-body);
		transition: background var(--motion-fast) var(--ease-standard);
	}
	.dropdown-item:hover {
		background: #f5f5f5;
	}
	.dropdown-item.selected {
		background: #f0f0f0;
	}
	.check-icon {
		flex-shrink: 0;
		color: #2563eb;
	}
	.dropdown-empty {
		padding: 24px;
		text-align: center;
		font-size: var(--text-sm);
		color: var(--meta);
	}
	.chip-wrap {
		display: flex;
		flex-wrap: wrap;
		gap: 4px;
		flex: 1;
		min-width: 0;
	}
	.chip {
		display: inline-flex;
		align-items: center;
		padding: 2px 8px;
		font-size: var(--text-xs);
		font-weight: 500;
		color: var(--fg);
		background: var(--surface);
		border-radius: var(--radius-sm);
		box-shadow: 0 0 0 1px var(--border);
		white-space: nowrap;
	}
	.venue-item {
		display: flex;
		flex-direction: column;
		gap: 1px;
	}
	.venue-name {
		font-weight: 500;
	}
	.venue-loc {
		font-size: 11px;
		color: var(--meta);
	}
	.form-actions {
		display: flex;
		justify-content: flex-end;
		padding-top: 12px;
		box-shadow: 0 -1px 0 0 var(--border);
	}

	.section-header {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16px;
	}
	.section-desc {
		font-size: var(--text-sm);
		color: var(--muted);
		margin: 4px 0 0;
	}

	.offer-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
		gap: 16px;
	}
	.offer-grid > .card {
		transition: box-shadow var(--motion-base) var(--ease-standard);
	}
	.offer-grid > .card:hover {
		box-shadow: var(--elev-raised);
	}
	.offer-top {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		gap: 12px;
	}
	.offer-name {
		font-size: var(--text-base);
		font-weight: 600;
		color: var(--fg);
		margin: 0;
	}
	.offer-right {
		text-align: right;
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: 4px;
	}
	.offer-price {
		font-size: var(--text-xl);
		font-weight: 600;
		color: var(--accent);
		font-variant-numeric: tabular-nums;
	}

	.progress-section {
		margin-top: 16px;
	}
	.progress-label {
		display: flex;
		justify-content: space-between;
		font-size: var(--text-xs);
		color: var(--muted);
		margin-bottom: 6px;
	}
	.progress-bar {
		height: 6px;
		border-radius: var(--radius-pill);
		background: #ebebeb;
		overflow: hidden;
	}
	.progress-fill {
		height: 100%;
		border-radius: var(--radius-pill);
		background: var(--accent);
		transition: width var(--motion-base) var(--ease-standard);
	}

	.empty-state {
		grid-column: 1 / -1;
		padding: 40px;
		text-align: center;
		font-size: var(--text-sm);
		color: var(--muted);
		box-shadow: 0 0 0 1px var(--border-soft);
		border-radius: var(--radius-md);
		background: var(--surface);
	}

	.venue-bar {
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 12px 16px;
		border-radius: var(--radius-md);
		background: var(--surface);
		box-shadow: var(--elev-ring);
	}
	.venue-label {
		font-size: var(--text-xs);
		font-weight: 600;
		color: var(--fg);
		white-space: nowrap;
	}
	.venue-select {
		max-width: 320px;
	}
	.manifest-badge {
		padding: 2px 10px;
		border-radius: var(--radius-pill);
		background: #ebf5ff;
		color: #0068d6;
		font-size: 10px;
		font-weight: 600;
		margin-left: auto;
	}

	.table-wrap {
		border-radius: var(--radius-md);
		overflow: hidden;
		background: var(--surface);
		box-shadow: var(--elev-ring);
	}
	.table {
		width: 100%;
		border-collapse: collapse;
		font-size: var(--text-sm);
	}
	.table th {
		padding: 12px 16px;
		text-align: left;
		font-size: 10px;
		font-weight: 600;
		color: var(--muted);
		text-transform: uppercase;
		letter-spacing: 0.04em;
		background: #f5f5f5;
		box-shadow: 0 1px 0 0 var(--border);
	}
	.th-right {
		text-align: right;
	}
	.table td {
		padding: 12px 16px;
		box-shadow: 0 1px 0 0 var(--border-soft);
		color: var(--fg-2);
	}
	.table tr:last-child td {
		box-shadow: none;
	}
	.td-name {
		font-weight: 600;
		color: var(--fg);
	}
	.td-muted {
		color: var(--muted);
	}
	.td-actions {
		text-align: right;
	}
	.link-btn {
		background: none;
		border: none;
		font-size: var(--text-xs);
		font-weight: 600;
		color: var(--accent);
		cursor: pointer;
		padding: 0;
		font-family: var(--font-body);
		transition: opacity var(--motion-fast) var(--ease-standard);
	}
	.link-btn:hover {
		opacity: 0.8;
	}
	.empty-cell {
		padding: 40px !important;
		text-align: center;
		font-style: italic;
		color: var(--muted);
	}

	.modal-overlay {
		position: fixed;
		inset: 0;
		z-index: 50;
		display: flex;
		align-items: center;
		justify-content: center;
		background: rgba(0, 0, 0, 0.4);
	}
	.modal {
		width: 100%;
		max-width: 460px;
		padding: 24px;
		border-radius: var(--radius-md);
		background: var(--surface);
		box-shadow: var(--elev-raised);
	}
	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20px;
		padding-bottom: 12px;
		box-shadow: 0 1px 0 0 var(--border);
	}
	.modal-title {
		font-size: var(--text-base);
		font-weight: 600;
		color: var(--fg);
		margin: 0;
	}
	.modal-close {
		background: none;
		border: none;
		font-size: 20px;
		color: var(--muted);
		cursor: pointer;
		padding: 0;
		line-height: 1;
	}
	.modal-close:hover {
		color: var(--fg);
	}
	.modal-form {
		display: flex;
		flex-direction: column;
		gap: 16px;
	}
	.modal-actions {
		display: flex;
		justify-content: flex-end;
		gap: 8px;
		padding-top: 12px;
		box-shadow: 0 -1px 0 0 var(--border);
	}
	.checkbox-label {
		display: flex;
		align-items: center;
		gap: 8px;
		font-size: var(--text-sm);
		color: var(--fg-2);
		cursor: pointer;
	}
	.checkbox {
		width: 16px;
		height: 16px;
		accent-color: var(--accent);
	}
</style>
