<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { enhance } from '$app/forms';

	let { data, form } = $props();

	let loading = $state(false);

	// Derived values for stats
	const event = $derived(form?.event || data.event);
	const offers = $derived(data.offers || []);
	const inventory = $derived(data.inventory || null);

	// Compute inventory variables
	const totalCapacity = $derived(inventory?.totalCapacity || 0);
	const totalSold = $derived(inventory?.soldCount || 0);
	const totalReserved = $derived(inventory?.reservedCount || 0);
	const soldPercent = $derived(
		totalCapacity > 0 ? Math.round((totalSold / totalCapacity) * 100) : 0
	);
	const reservedPercent = $derived(
		totalCapacity > 0 ? Math.round((totalReserved / totalCapacity) * 100) : 0
	);

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
</script>

<svelte:head>
	<title>{event.title} — Event Details</title>
</svelte:head>

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-8 p-6">
	<!-- Navigation & Title Header -->
	<div class="flex flex-col gap-4 border-b border-hairline pb-6">
		<a
			href="/b2b/dashboard?organizationId={event.organizationId}"
			class="flex items-center gap-1.5 text-xs font-bold text-slate-400 transition hover:text-slate-600"
		>
			<span>←</span>
			<span>Back to Dashboard</span>
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

	<!-- Layout Grid -->
	<div class="grid grid-cols-1 gap-8 lg:grid-cols-3">
		<!-- Left: Event Metadata Form (2 Cols on desktop) -->
		<div class="space-y-6 lg:col-span-2">
			<div class="rounded-xl border border-hairline bg-canvas p-6 shadow-xs">
				<h2 class="mb-6 text-base font-bold text-slate-900">Modify General Details</h2>

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
					<div>
						<label
							for="edit-title"
							class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
						>
							Event Title *
						</label>
						<input
							type="text"
							id="edit-title"
							name="title"
							required
							value={event.title}
							class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
						/>
					</div>

					<!-- Description -->
					<div>
						<label
							for="edit-desc"
							class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
						>
							Description
						</label>
						<textarea
							id="edit-desc"
							name="description"
							rows="5"
							class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
							>{event.description || ''}</textarea
						>
					</div>

					<!-- Venue Selector -->
					<div>
						<label
							for="edit-venue"
							class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
						>
							Venue *
						</label>
						<select
							id="edit-venue"
							name="venueId"
							required
							class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
						>
							{#each data.venues as venue (venue.id)}
								<option value={venue.id} selected={venue.id === event.venueId}>
									{venue.name} ({venue.city}, {venue.countryCode})
								</option>
							{/each}
						</select>
					</div>

					<!-- Category Classification -->
					<div>
						<label
							for="edit-category"
							class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
						>
							Classification Category
						</label>
						<select
							id="edit-category"
							name="classificationId"
							class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
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
						<div>
							<label
								for="edit-start"
								class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
							>
								Start Date & Time *
							</label>
							<input
								type="datetime-local"
								id="edit-start"
								name="startAt"
								required
								value={toDateTimeLocalString(event.startAt)}
								class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
							/>
						</div>
						<div>
							<label
								for="edit-end"
								class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
							>
								End Date & Time
							</label>
							<input
								type="datetime-local"
								id="edit-end"
								name="endAt"
								value={toDateTimeLocalString(event.endAt)}
								class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
							/>
						</div>
					</div>

					<!-- Timezone display -->
					<div>
						<label
							for="edit-tz"
							class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
						>
							Timezone
						</label>
						<input
							type="text"
							id="edit-tz"
							name="timezone"
							value={event.timezone || 'Asia/Ho_Chi_Minh'}
							readonly
							class="w-full rounded-lg border border-hairline bg-slate-50 px-3 py-2 text-sm text-slate-400 outline-none"
						/>
					</div>

					<!-- Form Submit Button -->
					<div class="flex items-center justify-end border-t border-hairline pt-5">
						<button
							type="submit"
							disabled={loading}
							class="flex cursor-pointer items-center justify-center gap-1.5 rounded-full bg-primary px-6 py-2.5 text-xs font-bold text-white transition hover:bg-primary/95 active:scale-95 disabled:opacity-50"
						>
							{#if loading}
								<span
									class="h-3 w-3 animate-spin rounded-full border border-white border-t-transparent"
								></span>
							{/if}
							<span>Save Changes</span>
						</button>
					</div>
				</form>
			</div>
		</div>

		<!-- Right: Analytics, Seating & Pricing (1 Col on desktop) -->
		<div class="space-y-6">
			<!-- Visual Ticket Inventory Progress -->
			<div class="rounded-xl border border-hairline bg-canvas p-6 shadow-xs">
				<h2 class="mb-4 text-base font-bold text-slate-900">Real-Time Inventory</h2>

				{#if inventory}
					<div class="space-y-5">
						<div>
							<div
								class="mb-1.5 flex items-center justify-between text-xs font-bold tracking-wider text-slate-400 uppercase"
							>
								<span>Tickets Sold</span>
								<span class="text-slate-800">{totalSold} / {totalCapacity}</span>
							</div>
							<div class="flex h-3 w-full overflow-hidden rounded-full bg-slate-100">
								<div
									style="width: {soldPercent}%"
									class="h-full bg-success transition-all duration-500"
									title="Sold: {soldPercent}%"
								></div>
								<div
									style="width: {reservedPercent}%"
									class="h-full bg-warning transition-all duration-500"
									title="Reserved: {reservedPercent}%"
								></div>
							</div>
							<div
								class="mt-2 flex items-center justify-between text-xs font-medium text-slate-500"
							>
								<span class="flex items-center gap-1">
									<span class="h-2 w-2 rounded-full bg-success"></span>
									<span>{soldPercent}% Sold</span>
								</span>
								<span class="flex items-center gap-1">
									<span class="h-2 w-2 rounded-full bg-warning"></span>
									<span>{reservedPercent}% Reserved</span>
								</span>
							</div>
						</div>

						<!-- Capacity details -->
						<div class="grid grid-cols-3 gap-3 border-t border-hairline pt-4 text-center">
							<div class="p-1">
								<div class="text-xs font-bold tracking-wide text-slate-400">Capacity</div>
								<div class="text-base font-bold text-slate-900">{totalCapacity}</div>
							</div>
							<div class="p-1">
								<div class="text-xs font-bold tracking-wide text-slate-400">Sold</div>
								<div class="text-base font-bold text-emerald-600 text-slate-900">{totalSold}</div>
							</div>
							<div class="p-1">
								<div class="text-xs font-bold tracking-wide text-slate-400">Reserved</div>
								<div class="text-base font-bold text-amber-600 text-slate-900">{totalReserved}</div>
							</div>
						</div>
					</div>
				{:else}
					<div
						class="rounded-lg border border-dashed border-hairline p-8 text-center text-xs text-mute"
					>
						No inventory manifest loaded for this event status. Set event pricing tier and manifest
						parameters first.
					</div>
				{/if}
			</div>

			<!-- Ticket Offers / Pricing Tiers -->
			<div class="rounded-xl border border-hairline bg-canvas p-6 shadow-xs">
				<h2 class="mb-4 text-base font-bold text-slate-900">Ticket Pricing Tiers</h2>

				{#if offers && offers.length > 0}
					<div class="divide-y divide-hairline">
						{#each offers as offer (offer.id || offer.name)}
							<div class="flex items-center justify-between py-3 first:pt-0 last:pb-0">
								<div>
									<div class="text-sm font-bold text-slate-900">
										{offer.name}
									</div>
									<div class="mt-0.5 text-xs text-slate-400">
										Limit: {offer.quantitySold || 0} / {offer.limitQuantity || 'Unlimited'}
									</div>
								</div>
								<div class="text-right">
									<div class="text-sm font-bold text-primary">
										{formatCurrency(offer.price)}
									</div>
									<div class="mt-0.5">
										{#if offer.active}
											<span
												class="inline-flex items-center rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-bold text-emerald-700 uppercase"
											>
												Active
											</span>
										{:else}
											<span
												class="inline-flex items-center rounded-full bg-slate-50 px-2 py-0.5 text-[10px] font-bold text-slate-400 uppercase"
											>
												Inactive
											</span>
										{/if}
									</div>
								</div>
							</div>
						{/each}
					</div>
				{:else}
					<div
						class="rounded-lg border border-dashed border-hairline p-8 text-center text-xs text-mute"
					>
						No active ticket pricing tiers config found. Set ticket pricing offers to open ticket
						sales.
					</div>
				{/if}
			</div>
		</div>
	</div>
</div>
