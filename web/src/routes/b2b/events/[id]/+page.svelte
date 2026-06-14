<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';
	import {
		IconAdjustmentsHorizontal,
		IconTicket,
		IconMap,
		IconLock,
		IconPlus,
		IconChevronLeft,
		IconQrcode
	} from '@tabler/icons-svelte';

	let { data, form } = $props();

	let loading = $state(false);
	let activeTab = $state('general');
	let activeCanvasTool = $state<'select' | 'pan'>('select');
	let canvasZoom = $state(1);

	const event = $derived(form?.event || data.event);
	const offers = $derived(data.offers || []);
	const venueName = $derived(data.venues?.find((v: any) => v.id === event.venueId)?.name || '');

	let localOffers = $state<any[]>([]);
	$effect(() => {
		if (offers && localOffers.length === 0) {
			localOffers = [...offers];
		}
	});

	let isAddOfferModalOpen = $state(false);
	let newOfferName = $state('');
	let newOfferPrice = $state<number>(200000);
	let newOfferLimit = $state<number>(300);
	let newOfferActive = $state(true);

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

	let selectedSeatId = $state<string | null>(null);
	const seatR = 6;
	function seatColor(status: string) {
		if (status === 'Sold') return '#059669';
		if (status === 'Reserved') return '#D97706';
		if (status === 'Held') return '#DC2626';
		return '#94A3B8';
	}
	const seats = $derived.by(() => {
		const result: { id: string; rowLetter: string; seatNum: number; status: string; x: number; y: number }[] = [];
		for (let r = 0; r < 4; r++) {
			const rowLetter = String.fromCharCode(65 + r);
			for (let c = 0; c < 10; c++) {
				const seatNum = c + 1;
				const id = `${rowLetter}-${seatNum}`;
				const isLeftBlock = c < 5;
				const localIndex = isLeftBlock ? c : c - 5;
				const aisleOffset = isLeftBlock ? 0 : 44;
				const x = 75 + localIndex * 32 + aisleOffset;
				const y = 80 + r * 42;
				let status: string = 'Available';
				if ((r === 0 && c < 3) || (r === 1 && c === 4)) status = 'Sold';
				else if (r === 2 && c > 7) status = 'Held';
				else if (r === 3 && c === 2) status = 'Reserved';
				result.push({ id, rowLetter, seatNum, status, x, y });
			}
		}
		return result;
	});
	const selectedSeatDetail = $derived(seats.find((s) => s.id === selectedSeatId));
	const totalSeats = $derived(seats.length);
	const totalSold = $derived(seats.filter((s) => s.status === 'Sold').length);
	const totalReserved = $derived(seats.filter((s) => s.status === 'Reserved' || s.status === 'Held').length);
	const priceLevels = $derived.by(() => {
		const vip = seats.filter((s) => s.rowLetter < 'C');
		const ga = seats.filter((s) => s.rowLetter >= 'C');
		return [
			{ id: 'VIP', color: '#7c3aed', label: 'VIP', count: vip.length, avail: vip.filter((s) => s.status === 'Available').length },
			{ id: 'GA', color: '#059669', label: 'General Admission', count: ga.length, avail: ga.filter((s) => s.status === 'Available').length }
		];
	});

	function zoomIn() { canvasZoom = Math.min(3, canvasZoom + 0.25); }
	function zoomOut() { canvasZoom = Math.max(0.25, canvasZoom - 0.25); }
	function fitToView() { canvasZoom = 1; }

	function toDateTimeLocalString(isoString: string) {
		if (!isoString) return '';
		const d = new Date(isoString);
		const pad = (n: number) => n.toString().padStart(2, '0');
		return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
	}

	function formatCurrency(amount: number) {
		return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
	}

	function formatDateShort(iso: string) {
		if (!iso) return '';
		const d = new Date(iso);
		return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
	}

	function handleAddOffer(e: Event) {
		e.preventDefault();
		if (!newOfferName.trim()) return;
		const mockId = `offer-${Date.now()}`;
		localOffers = [
			...localOffers,
			{
				id: mockId,
				name: newOfferName,
				price: newOfferPrice,
				limitQuantity: newOfferLimit,
				quantitySold: 0,
				active: newOfferActive
			}
		];
		newOfferName = '';
		newOfferPrice = 200000;
		newOfferLimit = 300;
		newOfferActive = true;
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

	function releaseHold(id: string) {
		holds = holds.filter((h) => h.id !== id);
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
				<IconAdjustmentsHorizontal size={16} />
				<span>General</span>
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'seats')}
				class="tab-btn"
				class:active={activeTab === 'seats'}
			>
				<IconMap size={16} />
				<span>Seat Map & Inventory</span>
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'offers')}
				class="tab-btn"
				class:active={activeTab === 'offers'}
			>
				<IconTicket size={16} />
				<span>Offers & Presales</span>
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'holds')}
				class="tab-btn"
				class:active={activeTab === 'holds'}
			>
				<IconLock size={16} />
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
						<label for="edit-slug" class="label">Slug <span class="required">*</span></label>
						<input
							type="text"
							id="edit-slug"
							name="slug"
							required
							value={event.slug}
							class="input"
						/>
					</div>
					<div class="field">
						<label for="edit-category" class="label">Classification</label>
						<select id="edit-category" name="classificationId" class="input">
							<option value="">-- Select a Category --</option>
							{#each data.classifications as cat (cat.id)}
								<option
									value={cat.id}
									selected={event.classifications?.some((c: { id: string }) => c.id === cat.id)}
								>
									{cat.name}
								</option>
							{/each}
						</select>
					</div>
					<div class="field">
						<label for="edit-start" class="label"
							>Date<span class="required">*</span></label
						>
						<input
							type="datetime-local"
							id="edit-start"
							name="startAt"
							required
							value={toDateTimeLocalString(event.startAt)}
							class="input"
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
		{:else if activeTab === 'seats'}
			<div class="venue-bar">
				<label for="seat-venue" class="venue-label">Venue</label>
				<select id="seat-venue" name="venueId" class="input venue-select">
					{#each data.venues as venue (venue.id)}
						<option value={venue.id} selected={venue.id === event.venueId}>
							{venue.name} ({venue.city}, {venue.countryCode})
						</option>
					{/each}
				</select>
				<span class="manifest-badge">{venueName || 'No manifest'}</span>
			</div>

			<div class="card canvas-editor">
				<div class="canvas-toolbar">
					<div class="toolbar-group">
						<span class="toolbar-mode active">Scaling</span>
					</div>
					<div class="toolbar-group">
						<button type="button" onclick={zoomOut} class="tool-icon-btn" title="Zoom Out">−</button>
						<span class="zoom-label">{Math.round(canvasZoom * 100)}%</span>
						<button type="button" onclick={zoomIn} class="tool-icon-btn" title="Zoom In">+</button>
					</div>
					<div class="toolbar-group">
						<button type="button" onclick={fitToView} class="tool-icon-btn" title="Fit to View">⌂</button>
					</div>
					<div class="toolbar-spacer"></div>
					<button type="button" class="tool-btn-save">Save</button>
				</div>

				<div class="canvas-body">
					<aside class="canvas-sidebar">
						<div class="sidebar-section-header">
							<h3 class="sidebar-title">Price Levels</h3>
						</div>
						<div class="sidebar-list">
							<div class="pl-item pl-unassigned">
								<span class="pl-dot"></span>
								<span class="pl-name">Unassigned</span>
								<span class="pl-count">0</span>
							</div>
							{#each priceLevels as pl}
								<div class="pl-item">
									<span class="pl-dot" style="background:{pl.color}"></span>
									<div class="pl-info">
										<span class="pl-name">{pl.label}</span>
										<div class="pl-meta">
											<span class="pl-count">{pl.avail} avail</span>
											<span class="pl-sep">·</span>
											<span class="pl-count">{pl.count} total</span>
										</div>
									</div>
								</div>
							{/each}
						</div>

						<div class="sidebar-section-header">
							<h3 class="sidebar-title sidebar-title-sm">Financial Information</h3>
						</div>
						<div class="fin-row">
							<span class="fin-label">Total Capacity</span>
							<span class="fin-value">{totalSeats}</span>
						</div>
						<div class="fin-row">
							<span class="fin-label">Sold</span>
							<span class="fin-value fin-value-green">{totalSold}</span>
						</div>
						<div class="fin-row">
							<span class="fin-label">Reserved / Held</span>
							<span class="fin-value fin-value-amber">{totalReserved}</span>
						</div>
					</aside>

					<main class="canvas-main">
						<div class="canvas-viewport">
							<div class="canvas-stage" style="transform: scale({canvasZoom})">
								<svg viewBox="0 0 420 300" class="seating-canvas">
									<rect x="135" y="12" width="150" height="36" rx="4" fill="#e2e8f0" stroke="#cbd5e1"/>
									<text x="210" y="35" text-anchor="middle" fill="#64748b" font-size="11" font-weight="700" letter-spacing="2">STAGE</text>

									<rect x="55" y="62" width="310" height="90" rx="6" fill="#f5f3ff" stroke="#e9d5ff" stroke-width="1"/>
									<rect x="55" y="165" width="310" height="90" rx="6" fill="#ecfdf5" stroke="#a7f3d0" stroke-width="1"/>

									<text x="65" y="77" fill="#7c3aed" font-size="8" font-weight="700" letter-spacing="1">VIP</text>
									<text x="65" y="180" fill="#059669" font-size="8" font-weight="700" letter-spacing="1">GA</text>

									{#each ['A', 'B', 'C', 'D'] as letter, i}
										<text x="70" y={86 + i * 42 + 5} text-anchor="end" fill="#94a3b8" font-size="10" font-weight="700">{letter}</text>
									{/each}

									{#each seats as seat (seat.id)}
										<circle
											cx={seat.x}
											cy={seat.y}
											r={selectedSeatId === seat.id ? seatR + 2 : seatR}
											fill={seatColor(seat.status)}
											stroke={selectedSeatId === seat.id ? '#3B82F6' : 'none'}
											stroke-width={selectedSeatId === seat.id ? '2' : '0'}
											onclick={() => (selectedSeatId = seat.id)}
											role="button"
											tabindex="0"
											onkeydown={(e) => e.key === 'Enter' && (selectedSeatId = seat.id)}
											aria-label="Seat {seat.id} ({seat.status})"
										/>
									{/each}
								</svg>
							</div>

							<div class="zoom-controls">
								<button type="button" onclick={zoomIn} class="zoom-btn" title="Zoom In">+</button>
								<button type="button" onclick={zoomOut} class="zoom-btn" title="Zoom Out">−</button>
								<div class="zoom-divider"></div>
								<button type="button" onclick={fitToView} class="zoom-btn" title="Fit to View">⊞</button>
							</div>

							<div class="minimap">
								<div class="minimap-header">
									<span>Map</span>
									<span class="minimap-pct">{Math.round(canvasZoom * 100)}%</span>
								</div>
								<div class="minimap-body">
									<svg viewBox="0 0 420 300" class="minimap-svg">
										<rect x="55" y="62" width="310" height="90" rx="6" fill="#e9d5ff" fill-opacity="0.3" stroke="#7c3aed" stroke-width="2"/>
										<rect x="55" y="165" width="310" height="90" rx="6" fill="#a7f3d0" fill-opacity="0.3" stroke="#059669" stroke-width="2"/>
										{#each seats as seat (seat.id)}
											<circle cx={seat.x} cy={seat.y} r="2" fill={seat.status === 'Available' ? '#94a3b8' : seat.status === 'Sold' ? '#059669' : seat.status === 'Reserved' ? '#d97706' : '#dc2626'}/>
										{/each}
									</svg>
								</div>
							</div>

							{#if selectedSeatDetail}
								<div class="selected-bar">
									<div class="selected-bar-icon">
										<svg class="icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z"/></svg>
									</div>
									<span class="selected-bar-text">Seat {selectedSeatDetail.id} · {selectedSeatDetail.status}</span>
									{#if selectedSeatDetail.status === 'Available'}
										<button type="button" onclick={() => { selectedSeatDetail.status = 'Held'; selectedSeatId = null; }} class="selected-btn selected-btn-red">Hold</button>
										<button type="button" onclick={() => { selectedSeatDetail.status = 'Reserved'; selectedSeatId = null; }} class="selected-btn selected-btn-amber">Reserve</button>
									{:else}
										<button type="button" onclick={() => { selectedSeatDetail.status = 'Available'; selectedSeatId = null; }} class="selected-btn selected-btn-outline">Release</button>
									{/if}
									<button type="button" onclick={() => (selectedSeatId = null)} class="selected-btn selected-btn-ghost">Clear</button>
								</div>
							{/if}
						</div>
					</main>

					<aside class="canvas-palette">
						<button
							type="button"
							onclick={() => (activeCanvasTool = 'select')}
							class="palette-btn"
							class:palette-active={activeCanvasTool === 'select'}
							title="Select"
						>
							<svg class="icon-md" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 3l7 7-7 7M5 3l7 7-7 7"/></svg>
						</button>
						<button
							type="button"
							onclick={() => (activeCanvasTool = 'pan')}
							class="palette-btn"
							class:palette-active={activeCanvasTool === 'pan'}
							title="Pan"
						>
							<svg class="icon-md" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7"/></svg>
						</button>
						<div class="palette-divider"></div>
						<div class="palette-label">Zoom</div>
						<button type="button" onclick={zoomIn} class="palette-btn" title="Zoom In">
							<svg class="icon-md" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 4v16m8-8H4"/></svg>
						</button>
						<button type="button" onclick={zoomOut} class="palette-btn" title="Zoom Out">
							<svg class="icon-md" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M20 12H4"/></svg>
						</button>
					</aside>
				</div>

				<div class="canvas-legend">
					<span><span class="dot-swatch swatch-avail"></span>Available</span>
					<span><span class="dot-swatch swatch-sold"></span>Sold</span>
					<span><span class="dot-swatch swatch-reserved"></span>Reserved</span>
					<span><span class="dot-swatch swatch-held"></span>Held</span>
				</div>
			</div>
		{:else if activeTab === 'offers'}
			<div class="section-header">
				<div>
					<h2 class="card-title">Active Pricing Offers</h2>
					<p class="section-desc">Create and configure pricing tiers, limits, and pre-sales.</p>
				</div>
				<button type="button" onclick={() => (isAddOfferModalOpen = true)} class="btn-secondary">
					<IconPlus size={14} />
					<span>Add Offer Tier</span>
				</button>
			</div>

			<div class="offer-grid">
				{#each localOffers as offer (offer.id)}
					<div class="card">
						<div class="offer-top">
							<div>
								<h3 class="offer-name">{offer.name}</h3>
							</div>
							<div class="offer-right">
								<span class="offer-price">{formatCurrency(offer.price)}</span>
								{#if offer.active}
									<span class="badge badge-green">Active</span>
								{:else}
									<span class="badge">Inactive</span>
								{/if}
							</div>
						</div>
						<div class="progress-section">
							<div class="progress-label">
								<span>Sales Limit</span>
								<span>{offer.quantitySold || 0} / {offer.limitQuantity || 'Unlimited'}</span>
							</div>
							<div class="progress-bar">
								<div
									class="progress-fill"
									style="width: {offer.limitQuantity > 0
										? Math.round(((offer.quantitySold || 0) / offer.limitQuantity) * 100)
										: 0}%"
								></div>
							</div>
						</div>
					</div>
				{:else}
					<div class="empty-state">
						No pricing offer tiers created yet. Tap "Add Offer Tier" to schedule VIP or GA tickets.
					</div>
				{/each}
			</div>
		{:else if activeTab === 'holds'}
			<div class="section-header">
				<div>
					<h2 class="card-title">Held & Locked Blocks</h2>
					<p class="section-desc">Reserve seat blocks for sponsors, media, or equipment.</p>
				</div>
				<button type="button" onclick={() => (isAddHoldModalOpen = true)} class="btn-secondary">
					<IconPlus size={14} />
					<span>Allocate Block</span>
				</button>
			</div>

			<div class="table-wrap">
				<table class="table">
					<thead>
						<tr>
							<th>Block / Group</th>
							<th>Held Count</th>
							<th>Reason</th>
							<th>Status</th>
							<th class="th-right">Actions</th>
						</tr>
					</thead>
					<tbody>
						{#each holds as h (h.id)}
							<tr>
								<td class="td-name">{h.name}</td>
								<td>{h.count} seats</td>
								<td class="td-muted">{h.reason}</td>
								<td>
									{#if h.status === 'LOCKED'}
										<span class="badge badge-amber">Locked</span>
									{:else}
										<span class="badge badge-red">Killed</span>
									{/if}
								</td>
								<td class="td-actions">
									<button type="button" onclick={() => releaseHold(h.id)} class="link-btn"
										>Release</button
									>
								</td>
							</tr>
						{:else}
							<tr>
								<td colspan="5" class="empty-cell">No held seating blocks recorded.</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		{/if}
	</div>
</div>

{#if isAddOfferModalOpen}
	<div class="modal-overlay" role="dialog">
		<div class="modal">
			<div class="modal-header">
				<h3 class="modal-title">Add New Pricing Tier</h3>
				<button type="button" onclick={() => (isAddOfferModalOpen = false)} class="modal-close"
					>&times;</button
				>
			</div>
			<form onsubmit={handleAddOffer} class="modal-form">
				<div class="field">
					<label for="offer-name" class="label">Tier Name <span class="required">*</span></label>
					<input
						type="text"
						id="offer-name"
						bind:value={newOfferName}
						required
						placeholder="e.g. VIP Front Row"
						class="input"
					/>
				</div>
				<div class="field-row">
					<div class="field">
						<label for="offer-price" class="label"
							>Price (VND) <span class="required">*</span></label
						>
						<input
							type="number"
							id="offer-price"
							bind:value={newOfferPrice}
							required
							min="0"
							class="input"
						/>
					</div>
					<div class="field">
						<label for="offer-limit" class="label">Max Limit <span class="required">*</span></label>
						<input
							type="number"
							id="offer-limit"
							bind:value={newOfferLimit}
							required
							min="1"
							class="input"
						/>
					</div>
				</div>
				<label class="checkbox-label">
					<input type="checkbox" bind:checked={newOfferActive} class="checkbox" />
					<span>Mark tier as active for sales</span>
				</label>
				<div class="modal-actions">
					<button type="button" onclick={() => (isAddOfferModalOpen = false)} class="btn-ghost"
						>Cancel</button
					>
					<button type="submit" class="btn-primary">Add Tier</button>
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
		color: var(--accent);
		box-shadow: 0 2px 0 0 var(--accent);
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
	select.input {
		appearance: none;
		background-image: url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1L5 5L9 1' stroke='%23808080' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
		background-repeat: no-repeat;
		background-position: right 12px center;
		padding-right: 32px;
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

	.canvas-editor {
		display: flex;
		flex-direction: column;
		gap: 0;
		padding: 0;
		overflow: hidden;
	}
	.canvas-toolbar {
		display: flex;
		align-items: center;
		gap: 8px;
		padding: 8px 12px;
		border-bottom: 1px solid var(--border);
	}
	.toolbar-group {
		display: flex;
		align-items: center;
		gap: 4px;
	}
	.toolbar-group + .toolbar-group {
		margin-left: 8px;
		padding-left: 8px;
		border-left: 1px solid var(--border);
	}
	.toolbar-spacer { flex: 1; }
	.toolbar-mode {
		padding: 3px 10px;
		border-radius: var(--radius-sm);
		font-size: 10px;
		font-weight: 700;
		color: var(--muted);
		background: transparent;
	}
	.toolbar-mode.active { background: #fff; color: var(--fg); box-shadow: var(--elev-ring); }
	.tool-btn-save {
		padding: 4px 14px;
		border-radius: var(--radius-sm);
		font-size: 10px;
		font-weight: 700;
		color: #fff;
		background: var(--fg);
		border: none;
		cursor: pointer;
	}
	.tool-btn-save:hover { opacity: 0.85; }
	.icon-sm { width: 12px; height: 12px; }
	.icon-md { width: 16px; height: 16px; }
	.tool-icon-btn { display: inline-flex; align-items: center; justify-content: center; width: 24px; height: 24px; border-radius: var(--radius-sm); font-size: 13px; font-weight: 600; color: var(--muted); background: transparent; border: none; cursor: pointer; transition: all var(--motion-fast) var(--ease-standard); }
	.tool-icon-btn:hover { background: #f5f5f5; color: var(--fg); }
	.zoom-label { font-size: 10px; font-weight: 600; color: var(--muted); min-width: 32px; text-align: center; font-variant-numeric: tabular-nums; }

	.canvas-body { display: flex; flex: 1; min-height: 400px; }
	.canvas-sidebar { width: 220px; flex-shrink: 0; border-right: 1px solid var(--border); padding: 16px; overflow-y: auto; background: #fafafa; }
	.sidebar-section-header { margin-bottom: 10px; }
	.sidebar-title { font-size: 10px; font-weight: 800; color: var(--muted); text-transform: uppercase; letter-spacing: 0.08em; margin: 0; }
	.sidebar-title-sm { font-size: 9px; margin-top: 20px; }
	.sidebar-list { display: flex; flex-direction: column; gap: 2px; }
	.pl-item { display: flex; align-items: center; gap: 8px; padding: 8px; border-radius: var(--radius-sm); cursor: default; }
	.pl-item:hover { background: #f0f0f0; }
	.pl-unassigned { opacity: 0.6; }
	.pl-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; border: 1px solid rgba(0,0,0,0.08); }
	.pl-unassigned .pl-dot { background: #fff; border-color: #cbd5e1; }
	.pl-info { flex: 1; min-width: 0; }
	.pl-name { display: block; font-size: 11px; font-weight: 600; color: var(--fg); }
	.pl-meta { display: flex; align-items: center; gap: 4px; font-size: 10px; color: var(--muted); margin-top: 1px; }
	.pl-sep { opacity: 0.4; }
	.pl-count { font-variant-numeric: tabular-nums; }
	.fin-row { display: flex; justify-content: space-between; align-items: center; padding: 5px 0; font-size: 11px; }
	.fin-label { color: var(--muted); font-weight: 500; }
	.fin-value { font-weight: 600; color: var(--fg); font-variant-numeric: tabular-nums; }
	.fin-value-green { color: #059669; }
	.fin-value-amber { color: #d97706; }

	.canvas-main { flex: 1; position: relative; overflow: hidden; }
	.canvas-viewport { position: relative; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #fafafa; }
	.canvas-stage { transform-origin: center center; transition: transform 0.15s ease; }
	.seating-canvas { width: 420px; height: 300px; display: block; border-radius: var(--radius-sm); box-shadow: var(--elev-ring); background: #fff; }
	circle { cursor: pointer; }
	circle:hover { filter: brightness(0.85); }

	.zoom-controls { position: absolute; right: 12px; bottom: 12px; z-index: 20; display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 3px; border-radius: 10px; border: 1px solid rgba(0,0,0,0.06); background: rgba(255,255,255,0.95); box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
	.zoom-btn { display: flex; align-items: center; justify-content: center; width: 26px; height: 26px; border-radius: 6px; font-size: 15px; font-weight: 600; color: #64748b; background: transparent; border: none; cursor: pointer; transition: all 0.12s ease; }
	.zoom-btn:hover { background: #f1f5f9; color: #1e293b; }
	.zoom-divider { width: 16px; height: 1px; background: #e2e8f0; margin: 2px 0; }

	.minimap { position: absolute; right: 12px; bottom: 100px; z-index: 20; width: 160px; height: 120px; border-radius: 10px; border: 1px solid rgba(0,0,0,0.06); background: rgba(255,255,255,0.95); box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 8px; }
	.minimap-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 4px; font-size: 8px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; }
	.minimap-pct { padding: 1px 5px; border-radius: 3px; background: #f1f5f9; font-size: 7px; color: #64748b; }
	.minimap-body { width: 100%; height: calc(100% - 14px); overflow: hidden; border-radius: 4px; border: 1px solid #e2e8f0; background: #f8fafc; }
	.minimap-svg { width: 100%; height: 100%; display: block; }

	.selected-bar { position: absolute; top: 12px; left: 50%; transform: translateX(-50%); z-index: 20; display: flex; align-items: center; gap: 10px; padding: 8px 16px; border-radius: 10px; border: 1px solid rgba(0,0,0,0.06); background: rgba(255,255,255,0.96); box-shadow: 0 2px 12px rgba(0,0,0,0.1); font-size: 11px; }
	.selected-bar-icon { display: flex; align-items: center; justify-content: center; width: 28px; height: 28px; border-radius: 6px; background: #eff6ff; color: #2563eb; }
	.selected-bar-text { font-weight: 600; color: var(--fg); white-space: nowrap; }
	.selected-btn { padding: 5px 12px; border-radius: 6px; font-size: 10px; font-weight: 600; border: none; cursor: pointer; }
	.selected-btn-red { background: #fef2f2; color: #dc2626; }
	.selected-btn-red:hover { opacity: 0.8; }
	.selected-btn-amber { background: #fffbeb; color: #d97706; }
	.selected-btn-amber:hover { opacity: 0.8; }
	.selected-btn-outline { background: transparent; box-shadow: 0 0 0 1px var(--border); color: var(--fg-2); }
	.selected-btn-outline:hover { background: #f5f5f5; }
	.selected-btn-ghost { background: transparent; color: var(--muted); }
	.selected-btn-ghost:hover { background: #f5f5f5; }

	.canvas-palette { width: 44px; flex-shrink: 0; border-left: 1px solid var(--border); display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 8px 2px; background: #fafafa; }
	.palette-btn { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 6px; color: #94a3b8; background: transparent; border: none; cursor: pointer; transition: all 0.12s ease; }
	.palette-btn:hover { background: #f1f5f9; color: #334155; }
	.palette-btn.palette-active { background: #1e293b; color: #fff; }
	.palette-divider { width: 20px; height: 1px; background: #e2e8f0; margin: 4px 0; }
	.palette-label { font-size: 7px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; margin: 2px 0; }

	.canvas-legend { display: flex; justify-content: center; gap: 18px; padding: 10px 12px; border-top: 1px solid var(--border); font-size: 10px; color: var(--muted); }
	.dot-swatch { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 4px; vertical-align: middle; }
	.swatch-avail { background: #94a3b8; }
	.swatch-sold { background: #059669; }
	.swatch-reserved { background: #d97706; }
	.swatch-held { background: #dc2626; }

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
