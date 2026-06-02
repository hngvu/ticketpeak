<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';
	import {
		IconSettings,
		IconTicket,
		IconMap,
		IconLock,
		IconUsers,
		IconPlus,
		IconTrash
	} from '@tabler/icons-svelte';

	let { data, form } = $props();

	let loading = $state(false);
	let activeTab = $state('general');

	// Derived values for stats
	const event = $derived(form?.event || data.event);
	const offers = $derived(data.offers || []);
	const inventory = $derived(data.inventory || null);

	// Compute inventory variables
	const totalCapacity = $derived(inventory?.totalCapacity || 1200);
	const totalSold = $derived(inventory?.soldCount || 240);
	const totalReserved = $derived(inventory?.reservedCount || 80);
	const soldPercent = $derived(
		totalCapacity > 0 ? Math.round((totalSold / totalCapacity) * 100) : 0
	);
	const reservedPercent = $derived(
		totalCapacity > 0 ? Math.round((totalReserved / totalCapacity) * 100) : 0
	);

	// Offers & pricing reactive state
	let localOffers = $state<any[]>([]);
	$effect(() => {
		if (offers && localOffers.length === 0) {
			localOffers = [...offers];
		}
	});

	// Mock Add Offer states
	let isAddOfferModalOpen = $state(false);
	let newOfferName = $state('');
	let newOfferPrice = $state<number>(200000);
	let newOfferLimit = $state<number>(300);
	let newOfferActive = $state(true);

	// Mock holds state
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

	// Mock Add Hold states
	let isAddHoldModalOpen = $state(false);
	let newHoldName = $state('');
	let newHoldCount = $state<number>(15);
	let newHoldReason = $state('');
	let newHoldType = $state<'LOCKED' | 'KILLED'>('LOCKED');

	// Mock collaborators state
	let collaborators = $state([
		{
			id: 'col-1',
			name: 'Hoàng Vũ',
			email: 'hoangvu@ticketpeak.com',
			role: 'Owner',
			initials: 'HV'
		},
		{
			id: 'col-2',
			name: 'Minh Đăng',
			email: 'minhdang@ticketpeak.com',
			role: 'Editor',
			initials: 'MD'
		},
		{
			id: 'col-3',
			name: 'Phương Nam',
			email: 'phuongnam@partner.com',
			role: 'Scanner',
			initials: 'PN'
		}
	]);

	// Mock Add Collab states
	let isAddCollabModalOpen = $state(false);
	let newCollabEmail = $state('');
	let newCollabName = $state('');
	let newCollabRole = $state<'Editor' | 'Scanner'>('Editor');

	// Simulated Seating Map grid
	let selectedSeatId = $state<string | null>(null);
	let seats = $state(
		Array.from({ length: 4 }, (_, r) =>
			Array.from({ length: 10 }, (_, c) => {
				const rowLetter = String.fromCharCode(65 + r); // A, B, C, D
				const seatNum = c + 1;
				const id = `${rowLetter}-${seatNum}`;
				// Randomize some statuses for premium visual impact
				let status = 'Available';
				if ((r === 0 && c < 3) || (r === 1 && c === 4)) status = 'Sold';
				else if (r === 2 && c > 7) status = 'Held';
				else if (r === 3 && c === 2) status = 'Reserved';
				return { id, rowLetter, seatNum, status };
			})
		).flat()
	);
	const selectedSeatDetail = $derived(seats.find((s) => s.id === selectedSeatId));

	// Date time input formats
	function toDateTimeLocalString(isoString: string) {
		if (!isoString) return '';
		const d = new Date(isoString);
		const pad = (n: number) => n.toString().padStart(2, '0');
		return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
	}

	function formatCurrency(amount: number) {
		return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
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
		// reset form and close
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
		// reset
		newHoldName = '';
		newHoldCount = 15;
		newHoldReason = '';
		newHoldType = 'LOCKED';
		isAddHoldModalOpen = false;
	}

	function handleAddCollab(e: Event) {
		e.preventDefault();
		if (!newCollabEmail.trim() || !newCollabName.trim()) return;
		const initials = newCollabName
			.split(' ')
			.map((n) => n[0])
			.join('')
			.toUpperCase()
			.slice(0, 2);
		collaborators = [
			...collaborators,
			{
				id: `col-${Date.now()}`,
				name: newCollabName,
				email: newCollabEmail,
				role: newCollabRole,
				initials
			}
		];
		// reset
		newCollabName = '';
		newCollabEmail = '';
		newCollabRole = 'Editor';
		isAddCollabModalOpen = false;
	}

	function releaseHold(id: string) {
		holds = holds.filter((h) => h.id !== id);
	}

	function removeCollab(id: string) {
		collaborators = collaborators.filter((c) => c.id !== id);
	}
</script>

<svelte:head>
	<title>{event.title} — Event Workspace</title>
</svelte:head>

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-6 p-6">
	<!-- Navigation & Title Header -->
	<div class="flex flex-col gap-4 border-b border-slate-100 pb-5">
		<a
			href="/b2b/events?organizationId={event.organizationId}"
			class="flex items-center gap-1.5 text-xs font-bold text-slate-400 transition hover:text-slate-600"
		>
			<span>←</span>
			<span>Back to Events</span>
		</a>

		<div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
			<div>
				<div class="flex items-center gap-3">
					<h1 class="text-2xl font-extrabold text-slate-900 md:text-3xl">
						{event.title}
					</h1>
					<!-- Status Badge -->
					{#if event.status === 'DRAFT'}
						<span
							class="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-bold text-slate-700 uppercase"
						>
							Draft
						</span>
					{:else if event.status === 'PUBLISHED'}
						<span
							class="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-bold text-blue-700 uppercase"
						>
							Published
						</span>
					{:else if event.status === 'SALES_ACTIVE'}
						<span
							class="inline-flex animate-pulse items-center rounded-full bg-green-50 px-2.5 py-0.5 text-xs font-bold text-green-700 uppercase"
						>
							Active
						</span>
					{:else if event.status === 'CANCELLED'}
						<span
							class="inline-flex items-center rounded-full bg-red-50 px-2.5 py-0.5 text-xs font-bold text-red-700 uppercase"
						>
							Cancelled
						</span>
					{:else if event.status === 'POSTPONED'}
						<span
							class="inline-flex items-center rounded-full bg-amber-50 px-2.5 py-0.5 text-xs font-bold text-amber-700 uppercase"
						>
							Postponed
						</span>
					{/if}
				</div>
				<p class="mt-1 text-xs font-medium text-slate-400">
					Event ID: {event.id} • Slug: {event.slug}
				</p>
			</div>

			<!-- Access Gate Scanner Button -->
			<a
				href="/b2b/check-in/{event.id}"
				class="inline-flex items-center justify-center gap-2 rounded-full bg-purple-600 px-5 py-2.5 text-xs font-bold text-white shadow-sm transition hover:bg-purple-700 active:scale-95"
			>
				<svg
					class="h-4 w-4"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
					stroke-width="2.5"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M3.75 4.875c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5A1.125 1.125 0 013.75 9.375v-4.5zM3.75 14.625c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5a1.125 1.125 0 01-1.125-1.125v-4.5zM13.5 4.875c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5A1.125 1.125 0 0113.5 9.375v-4.5z"
					/>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M15 15h.008v.008H15V15zm0 3h.008v.008H15V18zm3-3h.008v.008H18V15zm0 3h.008v.008H18V18zm-3-3h.008v.008H15V15zm0 3h.008v.008H15V18z"
					/>
				</svg>
				<span>Open Check-In Simulator</span>
			</a>
		</div>
	</div>

	<!-- Status Banners -->
	{#if form?.error}
		<div
			class="flex items-center gap-3 rounded-lg border border-red-200 bg-red-50 p-4 text-sm font-semibold text-red-700"
		>
			<svg
				class="h-5 w-5 shrink-0 text-red-500"
				fill="none"
				viewBox="0 0 24 24"
				stroke="currentColor"
				stroke-width="2"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
				/>
			</svg>
			<span>{form.error}</span>
		</div>
	{/if}

	{#if form?.success}
		<div
			class="flex items-center gap-3 rounded-lg border border-green-200 bg-green-50 p-4 text-sm font-semibold text-green-700"
		>
			<svg
				class="h-5 w-5 shrink-0 text-green-500"
				fill="none"
				viewBox="0 0 24 24"
				stroke="currentColor"
				stroke-width="2"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
				/>
			</svg>
			<span>Changes saved successfully!</span>
		</div>
	{/if}

	<!-- Horizontal Navigation Tabs Bar -->
	<div class="border-b border-slate-200">
		<nav class="-mb-px flex space-x-6 overflow-x-auto pb-1" aria-label="Tabs">
			<button
				type="button"
				onclick={() => (activeTab = 'general')}
				class="flex items-center gap-2 border-b-2 px-1 py-3 text-sm font-semibold transition-all duration-150 focus:outline-none {activeTab ===
				'general'
					? 'border-blue-600 text-blue-600'
					: 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'}"
			>
				<IconSettings size={16} />
				<span>General Info</span>
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'offers')}
				class="flex items-center gap-2 border-b-2 px-1 py-3 text-sm font-semibold transition-all duration-150 focus:outline-none {activeTab ===
				'offers'
					? 'border-blue-600 text-blue-600'
					: 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'}"
			>
				<IconTicket size={16} />
				<span>Offers & Presales</span>
				<span class="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-bold text-slate-600">
					{localOffers.length}
				</span>
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'seats')}
				class="flex items-center gap-2 border-b-2 px-1 py-3 text-sm font-semibold transition-all duration-150 focus:outline-none {activeTab ===
				'seats'
					? 'border-blue-600 text-blue-600'
					: 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'}"
			>
				<IconMap size={16} />
				<span>Seat Map & Inventory</span>
				<span class="rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-bold text-emerald-600">
					{soldPercent}% Sold
				</span>
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'holds')}
				class="flex items-center gap-2 border-b-2 px-1 py-3 text-sm font-semibold transition-all duration-150 focus:outline-none {activeTab ===
				'holds'
					? 'border-blue-600 text-blue-600'
					: 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'}"
			>
				<IconLock size={16} />
				<span>Holds & Kills</span>
				<span class="rounded-full bg-amber-50 px-2 py-0.5 text-[10px] font-bold text-amber-600">
					{holds.length}
				</span>
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'collaborators')}
				class="flex items-center gap-2 border-b-2 px-1 py-3 text-sm font-semibold transition-all duration-150 focus:outline-none {activeTab ===
				'collaborators'
					? 'border-blue-600 text-blue-600'
					: 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'}"
			>
				<IconUsers size={16} />
				<span>Collaborators</span>
				<span class="rounded-full bg-purple-50 px-2 py-0.5 text-[10px] font-bold text-purple-600">
					{collaborators.length}
				</span>
			</button>
		</nav>
	</div>

	<!-- Active Tab Panel Context -->
	<div class="py-4">
		<!-- TAB 1: GENERAL INFO PANEL -->
		{#if activeTab === 'general'}
			<div class="mx-auto max-w-3xl">
				<div class="rounded-xl border border-slate-200 bg-white p-6 shadow-xs">
					<h2 class="mb-5 text-base font-bold text-slate-900">Modify General Details</h2>

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
						class="space-y-5"
					>
						<!-- Title -->
						<div class="space-y-1">
							<label for="edit-title" class="block text-xs font-semibold text-slate-700">
								Event Title *
							</label>
							<input
								type="text"
								id="edit-title"
								name="title"
								required
								value={event.title}
								class="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2 text-sm text-slate-900 shadow-xs focus:border-blue-500 focus:outline-none"
							/>
						</div>

						<!-- Description -->
						<div class="space-y-1">
							<label for="edit-desc" class="block text-xs font-semibold text-slate-700">
								Description
							</label>
							<textarea
								id="edit-desc"
								name="description"
								rows="5"
								class="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2 text-sm text-slate-900 shadow-xs focus:border-blue-500 focus:outline-none"
								>{event.description || ''}</textarea
							>
						</div>

						<!-- Venue Selector -->
						<div class="space-y-1">
							<label for="edit-venue" class="block text-xs font-semibold text-slate-700">
								Venue *
							</label>
							<select
								id="edit-venue"
								name="venueId"
								required
								class="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2 text-sm text-slate-900 shadow-xs focus:border-blue-500 focus:outline-none"
							>
								{#each data.venues as venue (venue.id)}
									<option value={venue.id} selected={venue.id === event.venueId}>
										{venue.name} ({venue.city}, {venue.countryCode})
									</option>
								{/each}
							</select>
						</div>

						<!-- Category Classification -->
						<div class="space-y-1">
							<label for="edit-category" class="block text-xs font-semibold text-slate-700">
								Classification Category
							</label>
							<select
								id="edit-category"
								name="classificationId"
								class="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2 text-sm text-slate-900 shadow-xs focus:border-blue-500 focus:outline-none"
							>
								<option value="">-- Select a Category --</option>
								{#each data.classifications as cat (cat.id)}
									<option
										value={cat.id}
										selected={event.classifications &&
											event.classifications.some((c: { id: string }) => c.id === cat.id)}
									>
										{cat.name}
									</option>
								{/each}
							</select>
						</div>

						<!-- Date range fields -->
						<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
							<div class="space-y-1">
								<label for="edit-start" class="block text-xs font-semibold text-slate-700">
									Start Date & Time *
								</label>
								<input
									type="datetime-local"
									id="edit-start"
									name="startAt"
									required
									value={toDateTimeLocalString(event.startAt)}
									class="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2 text-sm text-slate-900 shadow-xs focus:border-blue-500 focus:outline-none"
								/>
							</div>
							<div class="space-y-1">
								<label for="edit-end" class="block text-xs font-semibold text-slate-700">
									End Date & Time
								</label>
								<input
									type="datetime-local"
									id="edit-end"
									name="endAt"
									value={toDateTimeLocalString(event.endAt)}
									class="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2 text-sm text-slate-900 shadow-xs focus:border-blue-500 focus:outline-none"
								/>
							</div>
						</div>

						<!-- Timezone display -->
						<div class="space-y-1">
							<label for="edit-tz" class="block text-xs font-semibold text-slate-700">
								Timezone
							</label>
							<input
								type="text"
								id="edit-tz"
								name="timezone"
								value={event.timezone || 'Asia/Ho_Chi_Minh'}
								readonly
								class="w-full cursor-not-allowed rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2 text-sm text-slate-400 outline-none"
							/>
						</div>

						<!-- Form Submit Button -->
						<div class="flex items-center justify-end border-t border-slate-100 pt-5">
							<button
								type="submit"
								disabled={loading}
								class="flex cursor-pointer items-center justify-center gap-1.5 rounded-full bg-slate-900 px-6 py-2.5 text-xs font-bold text-white transition hover:bg-slate-800 active:scale-95 disabled:opacity-50"
							>
								{#if loading}
									<span
										class="h-3.5 w-3.5 animate-spin rounded-full border-2 border-white border-t-transparent"
									></span>
								{/if}
								<span>Save Changes</span>
							</button>
						</div>
					</form>
				</div>
			</div>
		{/if}

		<!-- TAB 2: OFFERS & PRESALES PANEL -->
		{#if activeTab === 'offers'}
			<div class="mx-auto max-w-4xl space-y-6">
				<div class="flex items-center justify-between">
					<div>
						<h2 class="text-base font-bold text-slate-900">Active Pricing Offers</h2>
						<p class="text-xs text-slate-500">
							Create and configure pricing tiers, limits, and pre-sales.
						</p>
					</div>
					<button
						type="button"
						onclick={() => (isAddOfferModalOpen = true)}
						class="flex items-center gap-1.5 rounded-full bg-slate-900 px-4 py-2 text-xs font-bold text-white transition hover:bg-slate-800"
					>
						<IconPlus size={14} />
						<span>Add Offer Tier</span>
					</button>
				</div>

				<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
					{#each localOffers as offer (offer.id)}
						<div class="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
							<div class="flex items-start justify-between">
								<div>
									<h3 class="text-sm font-bold text-slate-900">{offer.name}</h3>
									<span
										class="mt-1 inline-flex items-center gap-1 rounded bg-slate-100 px-2 py-0.5 text-[9px] font-bold tracking-wide text-slate-600 uppercase"
									>
										ID: {offer.id.slice(0, 8)}
									</span>
								</div>
								<div class="text-right">
									<div class="text-base font-bold text-blue-600">{formatCurrency(offer.price)}</div>
									<div class="mt-1">
										{#if offer.active}
											<span
												class="inline-flex items-center rounded-full bg-emerald-50 px-2 py-0.5 text-[9px] font-bold text-emerald-700 uppercase"
											>
												Active
											</span>
										{:else}
											<span
												class="inline-flex items-center rounded-full bg-slate-50 px-2 py-0.5 text-[9px] font-bold text-slate-400 uppercase"
											>
												Inactive
											</span>
										{/if}
									</div>
								</div>
							</div>

							<!-- Progress Indicator -->
							<div class="mt-4 space-y-1.5">
								<div class="flex items-center justify-between text-xs font-semibold text-slate-400">
									<span>Sales Limit</span>
									<span class="text-slate-700"
										>{offer.quantitySold || 0} / {offer.limitQuantity || 'Unlimited'}</span
									>
								</div>
								<div class="h-2 w-full overflow-hidden rounded-full bg-slate-100">
									<div
										style="width: {offer.limitQuantity > 0
											? Math.round(((offer.quantitySold || 0) / offer.limitQuantity) * 100)
											: 0}%"
										class="h-full rounded-full bg-blue-500 transition-all duration-300"
									></div>
								</div>
							</div>
						</div>
					{:else}
						<div
							class="col-span-2 rounded-xl border border-dashed border-slate-200 p-12 text-center text-sm text-slate-400"
						>
							No pricing offer tiers created yet. Tap "Add Offer Tier" to schedule VIP or GA
							tickets.
						</div>
					{/each}
				</div>
			</div>
		{/if}

		<!-- TAB 3: SEAT MAP & INVENTORY PANEL -->
		{#if activeTab === 'seats'}
			<div class="mx-auto max-w-5xl space-y-6">
				<!-- Top Analytics Row -->
				<div class="grid grid-cols-1 gap-4 md:grid-cols-3">
					<div class="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
						<span class="block text-xs font-semibold tracking-wide text-slate-400 uppercase">
							Total Manifest Capacity
						</span>
						<span class="mt-1 block text-2xl font-extrabold text-slate-900"
							>{totalCapacity} seats</span
						>
					</div>
					<div class="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
						<span class="block text-xs font-semibold tracking-wide text-slate-400 uppercase">
							Sold Quota (Revenue Generated)
						</span>
						<span class="mt-1 block text-2xl font-extrabold text-emerald-600"
							>{totalSold} ({soldPercent}% Sold)</span
						>
					</div>
					<div class="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
						<span class="block text-xs font-semibold tracking-wide text-slate-400 uppercase">
							Reserved Blocks & Holds
						</span>
						<span class="mt-1 block text-2xl font-extrabold text-amber-500"
							>{totalReserved} ({reservedPercent}% Locked)</span
						>
					</div>
				</div>

				<div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
					<!-- Interactive seating grid grid-cols-2 -->
					<div class="rounded-xl border border-slate-200 bg-white p-6 shadow-xs lg:col-span-2">
						<h3 class="mb-2 text-sm font-bold text-slate-900">Seating Chart Manifest Layout</h3>
						<p class="mb-5 text-xs text-slate-400">
							Visual physical layout. Click any available seat to allocate holds or review lock
							details.
						</p>

						<!-- Map Layout Grid Container -->
						<div class="space-y-4">
							<!-- Stages Header -->
							<div
								class="mx-auto w-2/3 rounded border-b-4 border-slate-300 bg-slate-50 py-2 text-center text-xs font-bold tracking-widest text-slate-400 uppercase"
							>
								STAGE / MAIN PERFORMANCE
							</div>

							<!-- Grid Grid -->
							<div class="no-scrollbar overflow-x-auto py-4">
								<div class="mx-auto flex min-w-[450px] flex-col items-center justify-center gap-2">
									{#each [0, 1, 2, 3] as rowIndex (rowIndex)}
										{@const rowLetter = String.fromCharCode(65 + rowIndex)}
										<div class="flex items-center gap-2">
											<!-- Row label -->
											<span class="w-5 text-center text-xs font-bold text-slate-400"
												>{rowLetter}</span
											>

											<!-- Seats row -->
											<div class="flex gap-1.5">
												{#each [0, 1, 2, 3, 4, 5, 6, 7, 8, 9] as seatIndex (seatIndex)}
													{@const id = `${rowLetter}-${seatIndex + 1}`}
													{@const seat = seats.find((s) => s.id === id)}
													{#if seat}
														<button
															type="button"
															onclick={() => (selectedSeatId = id)}
															class="flex h-7 w-7 items-center justify-center rounded-sm border text-[9px] font-semibold transition-all
															{seat.status === 'Sold' ? 'cursor-not-allowed border-emerald-600 bg-emerald-500 text-white' : ''}
															{seat.status === 'Reserved' ? 'border-amber-600 bg-amber-500 text-white' : ''}
															{seat.status === 'Held' ? 'border-red-600 bg-red-500 text-white' : ''}
															{seat.status === 'Available' && selectedSeatId === id
																? 'border-blue-700 bg-blue-600 text-white ring-2 ring-blue-300'
																: ''}
															{seat.status === 'Available' && selectedSeatId !== id
																? 'border-slate-200 bg-slate-100 text-slate-700 hover:bg-slate-200'
																: ''}"
															title="Seat {id} ({seat.status})"
														>
															{seat.seatNum}
														</button>
													{/if}
												{/each}
											</div>
										</div>
									{/each}
								</div>
							</div>

							<!-- Seating Grid Legend -->
							<div
								class="flex flex-wrap items-center justify-center gap-4 border-t border-slate-100 pt-4 text-xs font-medium text-slate-500"
							>
								<span class="flex items-center gap-1.5">
									<span class="h-3.5 w-3.5 rounded-sm border border-slate-200 bg-slate-100"></span>
									<span>Available</span>
								</span>
								<span class="flex items-center gap-1.5">
									<span class="h-3.5 w-3.5 rounded-sm border border-emerald-600 bg-emerald-500"
									></span>
									<span>Sold</span>
								</span>
								<span class="flex items-center gap-1.5">
									<span class="h-3.5 w-3.5 rounded-sm border border-amber-600 bg-amber-500"></span>
									<span>Reserved</span>
								</span>
								<span class="flex items-center gap-1.5">
									<span class="h-3.5 w-3.5 rounded-sm border border-red-600 bg-red-500"></span>
									<span>Held / Killed</span>
								</span>
							</div>
						</div>
					</div>

					<!-- Seat Details Sidebar -->
					<div class="rounded-xl border border-slate-200 bg-white p-6 shadow-xs">
						<h3 class="text-sm font-bold text-slate-900">Seat Detail Inspector</h3>

						{#if selectedSeatDetail}
							<div class="mt-4 space-y-4">
								<div class="rounded-lg border border-slate-100 bg-slate-50 p-4">
									<span class="block text-[10px] font-bold tracking-wider text-slate-400 uppercase"
										>Seat Number</span
									>
									<span class="text-lg font-bold text-slate-900">{selectedSeatDetail.id}</span>

									<span
										class="mt-3 block text-[10px] font-bold tracking-wider text-slate-400 uppercase"
										>Pricing Level</span
									>
									<span class="text-xs font-semibold text-slate-700">VIP Zone (Standard Block)</span
									>

									<span
										class="mt-3 block text-[10px] font-bold tracking-wider text-slate-400 uppercase"
										>Status</span
									>
									<span
										class="mt-0.5 inline-flex items-center rounded-full px-2 py-0.5 text-[9px] font-bold uppercase
									{selectedSeatDetail.status === 'Available' ? 'bg-slate-100 text-slate-700' : ''}
									{selectedSeatDetail.status === 'Sold' ? 'bg-emerald-50 text-emerald-700' : ''}
									{selectedSeatDetail.status === 'Reserved' ? 'bg-amber-50 text-amber-700' : ''}
									{selectedSeatDetail.status === 'Held' ? 'bg-red-50 text-red-700' : ''}"
									>
										{selectedSeatDetail.status}
									</span>
								</div>

								{#if selectedSeatDetail.status === 'Available'}
									<div class="space-y-2">
										<button
											type="button"
											onclick={() => {
												selectedSeatDetail.status = 'Held';
												selectedSeatId = null;
											}}
											class="w-full rounded-lg border border-red-200 bg-red-50 py-2.5 text-center text-xs font-bold text-red-700 transition hover:bg-red-100"
										>
											Lock / Hold Seat
										</button>
										<button
											type="button"
											onclick={() => {
												selectedSeatDetail.status = 'Reserved';
												selectedSeatId = null;
											}}
											class="w-full rounded-lg border border-amber-200 bg-amber-50 py-2.5 text-center text-xs font-bold text-amber-700 transition hover:bg-amber-100"
										>
											Reserve Seat
										</button>
									</div>
								{:else}
									<div>
										<button
											type="button"
											onclick={() => {
												selectedSeatDetail.status = 'Available';
												selectedSeatId = null;
											}}
											class="w-full rounded-lg border border-slate-200 bg-slate-50 py-2.5 text-center text-xs font-bold text-slate-700 transition hover:bg-slate-100"
										>
											Release / Unlock Seat
										</button>
									</div>
								{/if}
							</div>
						{:else}
							<div class="mt-12 text-center text-xs text-slate-400 italic">
								Click any seat in the map chart to inspect its properties and control reservations.
							</div>
						{/if}
					</div>
				</div>
			</div>
		{/if}

		<!-- TAB 4: HOLDS & KILLS PANEL -->
		{#if activeTab === 'holds'}
			<div class="mx-auto max-w-4xl space-y-6">
				<div class="flex items-center justify-between">
					<div>
						<h2 class="text-base font-bold text-slate-900">Held & Locked Blocks</h2>
						<p class="text-xs text-slate-500">
							Reserve seat blocks for sponsors, media partners, or stage equipment obstruction.
						</p>
					</div>
					<button
						type="button"
						onclick={() => (isAddHoldModalOpen = true)}
						class="flex items-center gap-1.5 rounded-full bg-slate-900 px-4 py-2 text-xs font-bold text-white transition hover:bg-slate-800"
					>
						<IconPlus size={14} />
						<span>Allocate Lock Block</span>
					</button>
				</div>

				<div class="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xs">
					<table class="w-full border-collapse text-left text-xs text-slate-600">
						<thead
							class="border-b border-slate-200 bg-slate-50 text-[10px] font-bold tracking-wider text-slate-400 uppercase"
						>
							<tr>
								<th class="px-5 py-3.5">Block / Group Name</th>
								<th class="px-5 py-3.5">Held Count</th>
								<th class="px-5 py-3.5">Reason / Allocation Note</th>
								<th class="px-5 py-3.5">Type Status</th>
								<th class="px-5 py-3.5 text-right">Actions</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-slate-100">
							{#each holds as h (h.id)}
								<tr class="hover:bg-slate-50/50">
									<td class="px-5 py-4 font-bold text-slate-900">{h.name}</td>
									<td class="px-5 py-4 font-semibold text-slate-700">{h.count} seats</td>
									<td class="px-5 py-4 text-slate-500">{h.reason}</td>
									<td class="px-5 py-4">
										{#if h.status === 'LOCKED'}
											<span
												class="inline-flex items-center rounded-full border border-amber-200/50 bg-amber-50 px-2 py-0.5 text-[9px] font-bold text-amber-700 uppercase"
											>
												Locked (Hold)
											</span>
										{:else}
											<span
												class="inline-flex items-center rounded-full border border-red-200/50 bg-red-50 px-2 py-0.5 text-[9px] font-bold text-red-700 uppercase"
											>
												Killed (Loss)
											</span>
										{/if}
									</td>
									<td class="px-5 py-4 text-right">
										<button
											type="button"
											onclick={() => releaseHold(h.id)}
											class="text-xs font-bold text-slate-500 transition hover:text-red-600"
										>
											Release Block
										</button>
									</td>
								</tr>
							{:else}
								<tr>
									<td colspan="5" class="py-12 text-center text-sm text-slate-400 italic">
										No held seating blocks recorded for this event. Tap "Allocate Lock Block" to
										secure sponsor VIP tiers.
									</td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			</div>
		{/if}

		<!-- TAB 5: COLLABORATORS PANEL -->
		{#if activeTab === 'collaborators'}
			<div class="mx-auto max-w-4xl space-y-6">
				<div class="flex items-center justify-between">
					<div>
						<h2 class="text-base font-bold text-slate-900">Authorized Team Members</h2>
						<p class="text-xs text-slate-500">
							Grant permissions to other event hosts, managers, and check-in scanners.
						</p>
					</div>
					<button
						type="button"
						onclick={() => (isAddCollabModalOpen = true)}
						class="flex items-center gap-1.5 rounded-full bg-slate-900 px-4 py-2 text-xs font-bold text-white transition hover:bg-slate-800"
					>
						<IconPlus size={14} />
						<span>Add Collaborator</span>
					</button>
				</div>

				<div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
					{#each collaborators as c (c.id)}
						<div
							class="relative flex items-center justify-between rounded-xl border border-slate-200 bg-white p-4 shadow-xs"
						>
							<div class="flex items-center gap-3">
								<div
									class="flex h-9 w-9 items-center justify-center rounded-full border border-slate-200 bg-slate-100 text-xs font-bold text-slate-600"
								>
									{c.initials}
								</div>
								<div>
									<h3 class="text-xs font-bold text-slate-900">{c.name}</h3>
									<p class="text-[10px] text-slate-400">{c.email}</p>
									<div class="mt-1">
										{#if c.role === 'Owner'}
											<span
												class="inline-flex items-center rounded-full bg-blue-50 px-2 py-0.5 text-[8px] font-bold text-blue-700 uppercase"
											>
												Owner
											</span>
										{:else if c.role === 'Editor'}
											<span
												class="inline-flex items-center rounded-full bg-green-50 px-2 py-0.5 text-[8px] font-bold text-green-700 uppercase"
											>
												Editor
											</span>
										{:else}
											<span
												class="inline-flex items-center rounded-full bg-purple-50 px-2 py-0.5 text-[8px] font-bold text-purple-700 uppercase"
											>
												Scanner
											</span>
										{/if}
									</div>
								</div>
							</div>

							{#if c.role !== 'Owner'}
								<button
									type="button"
									onclick={() => removeCollab(c.id)}
									class="rounded p-1.5 text-slate-400 transition hover:text-red-500"
									aria-label="Remove collaborator"
								>
									<IconTrash size={14} />
								</button>
							{/if}
						</div>
					{/each}
				</div>
			</div>
		{/if}
	</div>
</div>

<!-- ======================== ADD OFFER MODAL ======================== -->
{#if isAddOfferModalOpen}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs">
		<div class="w-full max-w-md rounded-xl bg-white p-6 shadow-2xl" role="dialog">
			<div class="mb-4 flex items-center justify-between border-b border-slate-100 pb-3">
				<h3 class="text-sm font-bold text-slate-900">Add New Pricing Tier Offer</h3>
				<button
					type="button"
					onclick={() => (isAddOfferModalOpen = false)}
					class="text-xs font-bold text-slate-400 hover:text-slate-600"
				>
					✕
				</button>
			</div>

			<form onsubmit={handleAddOffer} class="space-y-4">
				<div class="space-y-1">
					<label for="offer-name" class="block text-xs font-semibold text-slate-700"
						>Tier Name *</label
					>
					<input
						type="text"
						id="offer-name"
						bind:value={newOfferName}
						required
						placeholder="e.g. VIP Front Row"
						class="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
					/>
				</div>

				<div class="grid grid-cols-2 gap-3">
					<div class="space-y-1">
						<label for="offer-price" class="block text-xs font-semibold text-slate-700"
							>Price (VND) *</label
						>
						<input
							type="number"
							id="offer-price"
							bind:value={newOfferPrice}
							required
							min="0"
							class="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
						/>
					</div>
					<div class="space-y-1">
						<label for="offer-limit" class="block text-xs font-semibold text-slate-700"
							>Max Limit *</label
						>
						<input
							type="number"
							id="offer-limit"
							bind:value={newOfferLimit}
							required
							min="1"
							class="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
						/>
					</div>
				</div>

				<div class="flex items-center gap-2">
					<input
						type="checkbox"
						id="offer-act"
						bind:checked={newOfferActive}
						class="border-slate-350 rounded text-blue-600 focus:ring-blue-400"
					/>
					<label for="offer-act" class="text-xs font-semibold text-slate-700"
						>Mark tier as active for sales</label
					>
				</div>

				<div class="flex items-center justify-end gap-2 border-t border-slate-100 pt-4">
					<button
						type="button"
						onclick={() => (isAddOfferModalOpen = false)}
						class="rounded-full border border-slate-200 bg-white px-4 py-2 text-xs font-bold text-slate-600 hover:bg-slate-50"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="rounded-full bg-slate-900 px-5 py-2 text-xs font-bold text-white transition hover:bg-slate-800"
					>
						Add Tier
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- ======================== ADD HOLD MODAL ======================== -->
{#if isAddHoldModalOpen}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs">
		<div class="w-full max-w-md rounded-xl bg-white p-6 shadow-2xl" role="dialog">
			<div class="mb-4 flex items-center justify-between border-b border-slate-100 pb-3">
				<h3 class="text-sm font-bold text-slate-900">Allocate Locked Seats Block</h3>
				<button
					type="button"
					onclick={() => (isAddHoldModalOpen = false)}
					class="text-xs font-bold text-slate-400 hover:text-slate-600"
				>
					✕
				</button>
			</div>

			<form onsubmit={handleAddHold} class="space-y-4">
				<div class="space-y-1">
					<label for="hold-name" class="block text-xs font-semibold text-slate-700"
						>Block Name *</label
					>
					<input
						type="text"
						id="hold-name"
						bind:value={newHoldName}
						required
						placeholder="e.g. VIP Sponsor Allocations"
						class="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
					/>
				</div>

				<div class="grid grid-cols-2 gap-3">
					<div class="space-y-1">
						<label for="hold-cnt" class="block text-xs font-semibold text-slate-700"
							>Seat Quantity *</label
						>
						<input
							type="number"
							id="hold-cnt"
							bind:value={newHoldCount}
							required
							min="1"
							class="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
						/>
					</div>
					<div class="space-y-1">
						<label for="hold-type" class="block text-xs font-semibold text-slate-700"
							>Lock Type *</label
						>
						<select
							id="hold-type"
							bind:value={newHoldType}
							class="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
						>
							<option value="LOCKED">Held (Locked)</option>
							<option value="KILLED">Killed (Obstruction)</option>
						</select>
					</div>
				</div>

				<div class="space-y-1">
					<label for="hold-reason" class="block text-xs font-semibold text-slate-700"
						>Reason / Description</label
					>
					<input
						type="text"
						id="hold-reason"
						bind:value={newHoldReason}
						placeholder="e.g. Partner VIP list block allocation"
						class="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
					/>
				</div>

				<div class="flex items-center justify-end gap-2 border-t border-slate-100 pt-4">
					<button
						type="button"
						onclick={() => (isAddHoldModalOpen = false)}
						class="rounded-full border border-slate-200 bg-white px-4 py-2 text-xs font-bold text-slate-600 hover:bg-slate-50"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="rounded-full bg-slate-900 px-5 py-2 text-xs font-bold text-white transition hover:bg-slate-800"
					>
						Allocate Block
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- ======================== ADD COLLABORATOR MODAL ======================== -->
{#if isAddCollabModalOpen}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs">
		<div class="w-full max-w-md rounded-xl bg-white p-6 shadow-2xl" role="dialog">
			<div class="mb-4 flex items-center justify-between border-b border-slate-100 pb-3">
				<h3 class="text-sm font-bold text-slate-900">Add Authorized Partner</h3>
				<button
					type="button"
					onclick={() => (isAddCollabModalOpen = false)}
					class="text-xs font-bold text-slate-400 hover:text-slate-600"
				>
					✕
				</button>
			</div>

			<form onsubmit={handleAddCollab} class="space-y-4">
				<div class="space-y-1">
					<label for="col-name" class="block text-xs font-semibold text-slate-700"
						>Full Name *</label
					>
					<input
						type="text"
						id="col-name"
						bind:value={newCollabName}
						required
						placeholder="e.g. Nguyen Van A"
						class="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
					/>
				</div>

				<div class="grid grid-cols-2 gap-3">
					<div class="space-y-1">
						<label for="col-email" class="block text-xs font-semibold text-slate-700"
							>Email Address *</label
						>
						<input
							type="email"
							id="col-email"
							bind:value={newCollabEmail}
							required
							placeholder="e.g. partner@example.com"
							class="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
						/>
					</div>
					<div class="space-y-1">
						<label for="col-role" class="block text-xs font-semibold text-slate-700"
							>Access Role *</label
						>
						<select
							id="col-role"
							bind:value={newCollabRole}
							class="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
						>
							<option value="Editor">Editor</option>
							<option value="Scanner">Check-In Scanner</option>
						</select>
					</div>
				</div>

				<div class="flex items-center justify-end gap-2 border-t border-slate-100 pt-4">
					<button
						type="button"
						onclick={() => (isAddCollabModalOpen = false)}
						class="rounded-full border border-slate-200 bg-white px-4 py-2 text-xs font-bold text-slate-600 hover:bg-slate-50"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="rounded-full bg-slate-900 px-5 py-2 text-xs font-bold text-white transition hover:bg-slate-800"
					>
						Add Collaborator
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
