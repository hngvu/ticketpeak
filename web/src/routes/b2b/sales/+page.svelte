<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { page } from '$app/state';
	import { enhance } from '$app/forms';

	let { data, form } = $props();

	// Active tab derived from query params
	const activeTab = $derived(page.url.searchParams.get('tab') || 'orders');

	interface B2BEvent {
		id: string;
		title: string;
		status: string;
		venueId: string;
		startAt: string;
		endAt?: string;
	}

	interface B2BOffer {
		id: string;
		name: string;
		capacityCap?: number;
		faceValue?: number;
	}

	// Active event details
	const selectedEvent = $derived(
		data.events?.find((e: B2BEvent) => e.id === data.selectedEventId) || data.events?.[0]
	);

	function switchEvent(eventId: string) {
		const url = new URL(window.location.href);
		url.searchParams.set('eventId', eventId);
		window.location.href = url.toString();
	}

	function formatCurrency(val: number) {
		return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
	}

	// Calculate mock/computed stats for the selected event
	const eventCapacity = $derived(
		data.offers?.reduce((sum: number, o: B2BOffer) => sum + (o.capacityCap || 0), 0) || 1200
	);
	const eventSold = $derived(selectedEvent?.status === 'ONSALE' ? 328 : 0);
	const eventRevenue = $derived(
		data.offers?.reduce(
			(sum: number, o: B2BOffer) =>
				sum + (o.faceValue || 0) * (eventSold / (data.offers.length || 1)),
			0
		) || 0
	);
</script>

<svelte:head>
	<title>Sales Module — Ticketpeak for Business</title>
</svelte:head>

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-8 p-6">
	<!-- Header block with Event Selector -->
	<div
		class="flex flex-col gap-6 border-b border-slate-100 pb-6 md:flex-row md:items-center md:justify-between"
	>
		<div>
			<h1 class="text-2xl font-extrabold text-slate-900 md:text-3xl">Commercial Sales & Pricing</h1>
			<p class="text-sm font-medium text-slate-500">
				Manage ticket types, publish active offers, allocate inventory, and inspect live ledger
				statements.
			</p>
		</div>

		<!-- Event Selector Dropdown -->
		{#if data.events && data.events.length > 0}
			<div class="flex w-full max-w-xs flex-col gap-1.5">
				<label
					for="event-select"
					class="text-[9px] font-extrabold tracking-widest text-slate-400 uppercase"
				>
					Select Scope Event
				</label>
				<select
					id="event-select"
					class="w-full cursor-pointer rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-xs font-bold text-slate-700 shadow-xs focus:border-blue-500 focus:outline-none"
					value={data.selectedEventId}
					onchange={(e: Event) => switchEvent((e.target as HTMLSelectElement).value)}
				>
					{#each data.events as event (event.id)}
						<option value={event.id}>{event.title}</option>
					{/each}
				</select>
			</div>
		{/if}
	</div>

	<!-- System notification banners -->
	{#if form?.error}
		<div
			class="rounded-xl border border-red-100 bg-red-50 p-4 text-xs font-bold text-red-700 shadow-xs"
		>
			⚠️ Error: {form.error}
		</div>
	{/if}
	{#if form?.success}
		<div
			class="rounded-xl border border-emerald-100 bg-emerald-50 p-4 text-xs font-bold text-emerald-700 shadow-xs"
		>
			✨ Success: {form.message}
		</div>
	{/if}

	{#if !selectedEvent}
		<div
			class="flex h-64 flex-col items-center justify-center space-y-3 rounded-2xl border border-dashed border-slate-200 bg-white p-6 text-center"
		>
			<span class="text-sm font-bold text-slate-400">No events found for this organization.</span>
			<a
				href="/b2b/events/create?organizationId={data.selectedOrgId || ''}"
				class="rounded-full bg-blue-600 px-4.5 py-2 text-xs font-bold text-white shadow-sm hover:bg-blue-700"
			>
				Create First Event
			</a>
		</div>
	{:else}
		<!-- Tab contents -->
		{#if activeTab === 'realtime'}
			<!-- Real-time Sales Viewport -->
			<div class="space-y-6">
				<!-- KPIs -->
				<div class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
					<div
						class="rounded-2xl border border-slate-100 bg-white p-5 shadow-xs transition hover:shadow-md"
					>
						<span class="text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
							>Gross Revenue</span
						>
						<div class="mt-2 flex items-baseline gap-1">
							<span class="text-2xl font-extrabold text-slate-900"
								>{formatCurrency(eventRevenue)}</span
							>
						</div>
						<div class="mt-1 text-[9px] font-bold text-emerald-600">▲ 14.2% vs last week</div>
					</div>

					<div
						class="rounded-2xl border border-slate-100 bg-white p-5 shadow-xs transition hover:shadow-md"
					>
						<span class="text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
							>Tickets Sold</span
						>
						<div class="mt-2 flex items-baseline gap-1">
							<span class="text-3xl font-extrabold text-slate-900">{eventSold}</span>
							<span class="text-xs font-bold text-slate-400">/ {eventCapacity}</span>
						</div>
						<div class="mt-1 text-[9px] font-bold text-slate-500">
							Sell-through rate: {Math.round((eventSold / eventCapacity) * 100)}%
						</div>
					</div>

					<div
						class="rounded-2xl border border-slate-100 bg-white p-5 shadow-xs transition hover:shadow-md"
					>
						<span class="text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
							>Total Orders</span
						>
						<div class="mt-2 flex items-baseline gap-1">
							<span class="text-3xl font-extrabold text-slate-900"
								>{Math.round(eventSold / 1.8)}</span
							>
							<span class="text-xs font-bold text-slate-400">checkouts</span>
						</div>
						<div class="mt-1 text-[9px] font-bold text-slate-500">
							Average ticket per order: 1.8
						</div>
					</div>

					<div
						class="rounded-2xl border border-slate-100 bg-white p-5 shadow-xs transition hover:shadow-md"
					>
						<span class="text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
							>Average Order Value</span
						>
						<div class="mt-2 flex items-baseline gap-1">
							<span class="text-2xl font-extrabold text-slate-900"
								>{formatCurrency(eventRevenue / Math.max(1, Math.round(eventSold / 1.8)))}</span
							>
						</div>
						<div class="mt-1 text-[9px] font-bold text-purple-600">High-intent buyer segments</div>
					</div>
				</div>

				<!-- Live sales rate -->
				<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
					<h2 class="text-base font-bold text-slate-900">Velocity Monitoring</h2>
					<p class="text-xs text-slate-400">
						Sales velocity and checkout spikes in the last 24 hours.
					</p>

					<div class="mt-8 flex h-48 items-end gap-3.5">
						{#each [40, 25, 45, 60, 80, 55, 90, 70, 110, 85, 130, 95] as height, i (i)}
							<div class="group relative flex flex-1 flex-col items-center">
								<div
									class="absolute bottom-full mb-1 scale-0 rounded bg-slate-900 px-2 py-1 text-[9px] font-bold text-white transition-all group-hover:scale-100"
								>
									{height} tix
								</div>
								<div
									class="w-full rounded-t-md bg-gradient-to-t from-blue-500 to-indigo-500 opacity-80 hover:opacity-100"
									style="height: {height}px"
								></div>
							</div>
						{/each}
					</div>
					<div
						class="mt-3 flex justify-between border-t border-slate-100 pt-3 text-[10px] font-extrabold text-slate-400 uppercase"
					>
						<span>12 hours ago</span>
						<span>6 hours ago</span>
						<span>Now (Realtime)</span>
					</div>
				</div>
			</div>
		{:else if activeTab === 'pricing'}
			<!-- Price Management / Ticket Types -->
			<div class="grid grid-cols-1 gap-8 lg:grid-cols-[1.8fr_1.2fr]">
				<!-- List of Ticket Types -->
				<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
					<div class="mb-6 flex items-center justify-between">
						<div>
							<h2 class="text-base font-bold text-slate-900">Configured Ticket Types</h2>
							<p class="text-xs text-slate-400">Base structures used for pricing layers.</p>
						</div>
					</div>

					{#if data.ticketTypes && data.ticketTypes.length > 0}
						<div class="space-y-4">
							{#each data.ticketTypes as tt (tt.id)}
								<div
									class="flex items-center justify-between rounded-xl border border-slate-100 bg-slate-50/20 p-4 transition hover:bg-slate-50/60"
								>
									<div>
										<h3 class="text-sm font-bold text-slate-900">{tt.name}</h3>
										<span
											class="mt-1 inline-block rounded bg-slate-100 px-2 py-0.5 font-mono text-[10px] text-slate-600"
										>
											slug: {tt.slug}
										</span>
										<p class="mt-1.5 text-xs text-slate-500">
											{tt.description || 'No description provided.'}
										</p>
									</div>

									<form method="POST" action="?/deleteTicketType" use:enhance>
										<input type="hidden" name="eventId" value={data.selectedEventId} />
										<input type="hidden" name="slug" value={tt.slug} />
										<button
											type="submit"
											class="cursor-pointer rounded-full border border-red-100 bg-red-50 px-3 py-1.5 text-xs font-bold text-red-600 transition hover:bg-red-100"
										>
											Delete
										</button>
									</form>
								</div>
							{/each}
						</div>
					{:else}
						<div class="flex h-48 flex-col items-center justify-center text-center">
							<span class="text-xs font-bold text-slate-400">No ticket types configured yet.</span>
						</div>
					{/if}
				</div>

				<!-- Quick Create Ticket Type Form -->
				<div class="h-fit rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
					<h2 class="text-base font-bold text-slate-900">Create Ticket Type</h2>
					<p class="mb-4 text-xs text-slate-400">Define a ticket classification code.</p>

					<form method="POST" action="?/createTicketType" use:enhance class="space-y-4">
						<input type="hidden" name="eventId" value={data.selectedEventId} />

						<div>
							<label for="tt-name" class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
								>Name *</label
							>
							<input
								type="text"
								id="tt-name"
								name="name"
								required
								placeholder="e.g. Vé VIP Sân"
								class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
							/>
						</div>

						<div>
							<label for="tt-slug" class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
								>Slug (kebab-case) *</label
							>
							<input
								type="text"
								id="tt-slug"
								name="slug"
								required
								pattern="^[a-z0-9-]+$"
								placeholder="e.g. ve-vip-san"
								class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
							/>
						</div>

						<div>
							<label
								for="tt-description"
								class="mb-1.5 block text-xs font-bold text-slate-500 uppercase">Description</label
							>
							<textarea
								id="tt-description"
								name="description"
								rows="3"
								placeholder="Describe the privileges..."
								class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
							></textarea>
						</div>

						<button
							type="submit"
							class="w-full cursor-pointer rounded-xl bg-blue-600 py-2.5 text-xs font-bold text-white transition hover:bg-blue-700"
						>
							Save Ticket Type
						</button>
					</form>
				</div>
			</div>
		{:else if activeTab === 'offers'}
			<!-- Offers & Presales -->
			<div class="grid grid-cols-1 gap-8 lg:grid-cols-[1.8fr_1.2fr]">
				<!-- List of Offers -->
				<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
					<div class="mb-6">
						<h2 class="text-base font-bold text-slate-900">Active Listing Offers</h2>
						<p class="text-xs text-slate-400">
							Public pricing and sale schedules visible to buyers.
						</p>
					</div>

					{#if data.offers && data.offers.length > 0}
						<div class="space-y-4">
							{#each data.offers as offer (offer.id)}
								<div
									class="flex items-center justify-between rounded-xl border border-slate-100 bg-slate-50/20 p-4 transition hover:bg-slate-50/60"
								>
									<div>
										<div class="flex items-center gap-2">
											<h3 class="text-sm font-bold text-slate-900">{offer.name}</h3>
											<span
												class="rounded-full bg-blue-50 px-2 py-0.5 text-[9px] font-bold text-blue-600 uppercase"
											>
												{offer.seatingMode}
											</span>
										</div>
										<p class="mt-1 text-xs font-bold text-purple-600">
											Face Value: {formatCurrency(offer.faceValue)}
										</p>
										<p class="mt-1 text-xs text-slate-500">
											Capacity: {offer.capacityCap} tickets
										</p>
										{#if offer.description}
											<p class="mt-2 text-xs text-slate-400">{offer.description}</p>
										{/if}
									</div>

									<form method="POST" action="?/deleteOffer" use:enhance>
										<input type="hidden" name="eventId" value={data.selectedEventId} />
										<input type="hidden" name="offerId" value={offer.id} />
										<button
											type="submit"
											class="cursor-pointer rounded-full border border-red-100 bg-red-50 px-3 py-1.5 text-xs font-bold text-red-600 transition hover:bg-red-100"
										>
											Delete
										</button>
									</form>
								</div>
							{/each}
						</div>
					{:else}
						<div class="flex h-48 flex-col items-center justify-center text-center">
							<span class="text-xs font-bold text-slate-400">No active offers published.</span>
						</div>
					{/if}
				</div>

				<!-- Quick Create Offer Form -->
				<div class="h-fit rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
					<h2 class="text-base font-bold text-slate-900">Publish New Offer</h2>
					<p class="mb-4 text-xs text-slate-400">Publish a pricing offer tier.</p>

					{#if !data.ticketTypes || data.ticketTypes.length === 0}
						<div class="rounded-lg bg-amber-50 p-4 text-xs text-amber-700">
							⚠️ Please create at least one Ticket Type before publishing an offer.
						</div>
					{:else}
						<form method="POST" action="?/createOffer" use:enhance class="space-y-4">
							<input type="hidden" name="eventId" value={data.selectedEventId} />

							<div>
								<label for="off-tt" class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
									>Base Ticket Type *</label
								>
								<select
									id="off-tt"
									name="ticketTypeId"
									required
									class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
								>
									{#each data.ticketTypes as tt (tt.id)}
										<option value={tt.id}>{tt.name}</option>
									{/each}
								</select>
							</div>

							<div>
								<label
									for="off-name"
									class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
									>Offer Name *</label
								>
								<input
									type="text"
									id="off-name"
									name="name"
									required
									placeholder="e.g. Vé GA Mở Bán Sớm"
									class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
								/>
							</div>

							<div>
								<label
									for="off-desc"
									class="mb-1.5 block text-xs font-bold text-slate-500 uppercase">Description</label
								>
								<textarea
									id="off-desc"
									name="description"
									rows="2"
									placeholder="Describe pricing restrictions..."
									class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
								></textarea>
							</div>

							<div class="grid grid-cols-2 gap-4">
								<div>
									<label
										for="off-value"
										class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
										>Price (VND) *</label
									>
									<input
										type="number"
										id="off-value"
										name="faceValue"
										required
										min="0"
										placeholder="e.g. 500000"
										class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
									/>
								</div>
								<div>
									<label
										for="off-cap"
										class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
										>Capacity Cap *</label
									>
									<input
										type="number"
										id="off-cap"
										name="capacityCap"
										required
										min="1"
										placeholder="e.g. 100"
										class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
									/>
								</div>
							</div>

							<div class="grid grid-cols-2 gap-4">
								<div>
									<label
										for="off-seating"
										class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
										>Seating Mode *</label
									>
									<select
										id="off-seating"
										name="seatingMode"
										required
										class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
									>
										<option value="GENERAL_ADMISSION">GA (General Adm)</option>
										<option value="RESERVED_SEATING">RS (Reserved Seat)</option>
									</select>
								</div>
								<div>
									<label
										for="off-section"
										class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
										>Section ID</label
									>
									<input
										type="text"
										id="off-section"
										name="sectionId"
										placeholder="e.g. sec-ga"
										class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
									/>
								</div>
							</div>

							<button
								type="submit"
								class="w-full cursor-pointer rounded-xl bg-blue-600 py-2.5 text-xs font-bold text-white transition hover:bg-blue-700"
							>
								Publish Offer
							</button>
						</form>
					{/if}
				</div>
			</div>
		{:else if activeTab === 'inventory'}
			<!-- Inventory control -->
			<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
				<h2 class="text-base font-bold text-slate-900">Capacity & Seating Inventory</h2>
				<p class="mb-6 text-xs text-slate-400">Current allocation bars across price categories.</p>

				{#if data.offers && data.offers.length > 0}
					<div class="space-y-6">
						{#each data.offers as offer (offer.id)}
							{@const soldCount =
								selectedEvent?.status === 'ONSALE' ? Math.round(offer.capacityCap * 0.35) : 0}
							{@const percent = Math.round((soldCount / offer.capacityCap) * 100)}

							<div class="space-y-2">
								<div class="flex items-center justify-between text-xs font-bold">
									<span class="text-slate-700">{offer.name}</span>
									<span class="text-slate-800"
										>{soldCount} / {offer.capacityCap} sold ({percent}%)</span
									>
								</div>
								<div class="h-3 w-full overflow-hidden rounded-full bg-slate-100">
									<div
										class="h-full rounded-full bg-gradient-to-r from-blue-500 to-indigo-500 transition-all duration-500"
										style="width: {percent}%"
									></div>
								</div>
							</div>
						{/each}
					</div>
				{:else}
					<div class="flex h-48 flex-col items-center justify-center text-center">
						<span class="text-xs font-bold text-slate-400">No inventory offers setup.</span>
					</div>
				{/if}
			</div>
		{:else}
			<!-- Transaction Ledger (Orders) -->
			<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
				<h2 class="mb-4 text-base font-bold text-slate-900">Recent Transactions</h2>
				<div class="overflow-x-auto">
					<table class="w-full text-left text-sm text-slate-500">
						<thead>
							<tr
								class="border-b border-slate-100 text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
							>
								<th class="py-3 pr-4">Order ID</th>
								<th class="px-4 py-3">Customer</th>
								<th class="px-4 py-3">Event Detail</th>
								<th class="px-4 py-3 text-center">Qty</th>
								<th class="px-4 py-3 text-right">Revenue</th>
								<th class="px-4 py-3 text-center">Payment</th>
								<th class="py-3 pl-4 text-right">Status</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-slate-100/60">
							<tr class="transition-colors hover:bg-slate-50/50">
								<td class="py-4 pr-4 font-mono font-bold text-slate-800">TXN-9021</td>
								<td class="px-4 py-4">
									<div class="font-bold text-slate-900">Nguyễn Hoàng Long</div>
									<div class="text-xs text-slate-400">long.nh@gmail.com</div>
								</td>
								<td class="max-w-[200px] truncate px-4 py-4 font-medium text-slate-700">
									{selectedEvent.title}
								</td>
								<td class="px-4 py-4 text-center font-bold text-slate-900">2</td>
								<td class="px-4 py-4 text-right font-extrabold text-slate-900">
									{formatCurrency(1000000)}
								</td>
								<td class="px-4 py-4 text-center text-xs font-bold text-slate-500">VNPAY</td>
								<td class="py-4 pl-4 text-right">
									<span
										class="inline-flex items-center rounded-full bg-green-50 px-2.5 py-0.5 text-[10px] font-bold text-green-700 uppercase"
									>
										PAID
									</span>
								</td>
							</tr>
							<tr class="transition-colors hover:bg-slate-50/50">
								<td class="py-4 pr-4 font-mono font-bold text-slate-800">TXN-9020</td>
								<td class="px-4 py-4">
									<div class="font-bold text-slate-900">Phạm Minh Hưng</div>
									<div class="text-xs text-slate-400">hung.pm@yahoo.com</div>
								</td>
								<td class="max-w-[200px] truncate px-4 py-4 font-medium text-slate-700">
									{selectedEvent.title}
								</td>
								<td class="px-4 py-4 text-center font-bold text-slate-900">1</td>
								<td class="px-4 py-4 text-right font-extrabold text-slate-900">
									{formatCurrency(500000)}
								</td>
								<td class="px-4 py-4 text-center text-xs font-bold text-slate-500">STRIPE</td>
								<td class="py-4 pl-4 text-right">
									<span
										class="inline-flex items-center rounded-full bg-green-50 px-2.5 py-0.5 text-[10px] font-bold text-green-700 uppercase"
									>
										PAID
									</span>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		{/if}
	{/if}
</div>
