<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { page } from '$app/state';
	import { enhance } from '$app/forms';

	let { data, form } = $props();

	// Active tab derived from query params, defaulting to 'realtime'
	const activeTab = $derived(page.url.searchParams.get('tab') || 'realtime');

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

	// Calculate stats for the selected event
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

<div class="flex flex-1 flex-col space-y-6 p-6">
	<!-- Header block with Event Selector -->
	<div
		class="flex flex-col gap-4 border-b border-hairline pb-5 md:flex-row md:items-center md:justify-between"
	>
		<div class="flex flex-col gap-1">
			<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">
				Commercial Sales & Pricing
			</h1>
			<p class="font-sans text-xs text-body">
				Manage ticket types, publish active offers, allocate inventory, and inspect live ledger
				statements.
			</p>
		</div>

		<!-- Event Selector Dropdown -->
		{#if data.events && data.events.length > 0}
			<div class="flex w-full max-w-xs flex-col gap-1.5">
				<label
					for="event-select"
					class="font-mono text-[9px] font-bold tracking-widest text-mute uppercase"
				>
					Select Scope Event
				</label>
				<select
					id="event-select"
					class="w-full cursor-pointer rounded-md border border-hairline bg-canvas px-3 py-2 text-xs text-ink focus:border-primary focus:outline-none"
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
			class="rounded-md border border-error bg-error/10 p-3.5 font-sans text-xs font-semibold text-error"
		>
			⚠️ Error: {form.error}
		</div>
	{/if}
	{#if form?.success}
		<div
			class="rounded-md border border-success bg-success/10 p-3.5 font-sans text-xs font-semibold text-success"
		>
			✨ Success: {form.message}
		</div>
	{/if}

	{#if !selectedEvent}
		<div
			class="flex h-60 flex-col items-center justify-center space-y-3 rounded-lg border border-dashed border-hairline bg-canvas p-6 text-center shadow-xs"
		>
			<span class="text-xs font-semibold text-mute">No events found for this organization.</span>
			<a
				href="/b2b/events/create?organizationId={data.selectedOrgId || ''}"
				class="cursor-pointer rounded-full bg-primary px-4 py-1.5 text-xs font-semibold text-on-primary shadow-sm transition hover:bg-primary/90"
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
				<div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
					<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
						<p class="font-mono text-[10px] font-semibold tracking-wider text-mute uppercase">
							Gross Revenue
						</p>
						<p class="mt-2 font-mono text-2xl font-bold tracking-tight text-ink">
							{formatCurrency(eventRevenue)}
						</p>
						<p class="mt-1 font-mono text-[9px] font-bold text-success">▲ 14.2% vs last week</p>
					</div>

					<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
						<p class="font-mono text-[10px] font-semibold tracking-wider text-mute uppercase">
							Tickets Sold
						</p>
						<div class="mt-2 flex items-baseline gap-1">
							<span class="font-mono text-2xl font-bold tracking-tight text-ink">{eventSold}</span>
							<span class="font-mono text-xs text-mute">/ {eventCapacity}</span>
						</div>
						<p class="mt-1 font-mono text-[9px] text-mute">
							Sell-through rate: {Math.round((eventSold / eventCapacity) * 100)}%
						</p>
					</div>

					<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
						<p class="font-mono text-[10px] font-semibold tracking-wider text-mute uppercase">
							Total Orders
						</p>
						<div class="mt-2 flex items-baseline gap-1">
							<span class="font-mono text-2xl font-bold tracking-tight text-ink"
								>{Math.round(eventSold / 1.8)}</span
							>
							<span class="text-xs text-mute">checkouts</span>
						</div>
						<p class="mt-1 font-mono text-[9px] text-mute">Average ticket per order: 1.8</p>
					</div>

					<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
						<p class="font-mono text-[10px] font-semibold tracking-wider text-mute uppercase">
							Average Order Value
						</p>
						<p class="mt-2 font-mono text-2xl font-bold tracking-tight text-ink">
							{formatCurrency(eventRevenue / Math.max(1, Math.round(eventSold / 1.8)))}
						</p>
						<p class="mt-1 font-mono text-[9px] text-primary">High-intent buyer segments</p>
					</div>
				</div>

				<!-- Live sales velocity chart -->
				<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
					<h2 class="font-sans text-sm font-semibold text-ink">Velocity Monitoring</h2>
					<p class="text-xs text-mute">Sales velocity and checkout spikes in the last 24 hours.</p>

					<div class="mt-8 flex h-40 items-end gap-3 px-2">
						{#each [40, 25, 45, 60, 80, 55, 90, 70, 110, 85, 130, 95] as height}
							<div class="group relative flex flex-1 flex-col items-center">
								<div
									class="absolute bottom-full mb-1 scale-0 rounded bg-ink px-2 py-0.5 font-mono text-[9px] font-semibold text-on-primary transition-all group-hover:scale-100"
								>
									{height} tix
								</div>
								<div
									class="w-full rounded-t bg-primary/70 transition-colors hover:bg-primary"
									style="height: {height}px"
								></div>
							</div>
						{/each}
					</div>
					<div
						class="mt-3 flex justify-between border-t border-hairline pt-3 font-mono text-[9px] font-bold tracking-wider text-mute uppercase"
					>
						<span>12 hours ago</span>
						<span>6 hours ago</span>
						<span>Now (Realtime)</span>
					</div>
				</div>
			</div>
		{:else if activeTab === 'pricing'}
			<!-- Price Management / Ticket Types -->
			<div class="grid grid-cols-1 gap-6 lg:grid-cols-[1.8fr_1.2fr]">
				<!-- List of Ticket Types -->
				<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
					<h2 class="font-sans text-sm font-semibold text-ink">Configured Ticket Types</h2>
					<p class="mb-5 text-xs text-mute">Base structures used for pricing layers.</p>

					{#if data.ticketTypes && data.ticketTypes.length > 0}
						<div class="space-y-3">
							{#each data.ticketTypes as tt (tt.id)}
								<div
									class="flex items-center justify-between rounded-md border border-hairline bg-canvas-soft/40 p-4 transition hover:bg-canvas-soft/60"
								>
									<div>
										<h3 class="text-xs font-semibold text-ink">{tt.name}</h3>
										<span
											class="mt-1 inline-block rounded bg-canvas-soft-2 px-2 py-0.5 font-mono text-[9px] font-bold text-body"
										>
											code: {tt.code}
										</span>
									</div>

									<form method="POST" action="?/deleteTicketType" use:enhance>
										<input type="hidden" name="eventId" value={data.selectedEventId} />
										<input type="hidden" name="code" value={tt.code} />
										<button
											type="submit"
											class="cursor-pointer rounded-full border border-error/20 bg-error/5 px-3 py-1 text-xs font-semibold text-error transition hover:bg-error/10"
										>
											Delete
										</button>
									</form>
								</div>
							{/each}
						</div>
					{:else}
						<div class="flex h-40 flex-col items-center justify-center text-center">
							<span class="text-xs font-semibold text-mute">No ticket types configured yet.</span>
						</div>
					{/if}
				</div>

				<!-- Quick Create Ticket Type Form -->
				<div class="h-fit rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
					<h2 class="font-sans text-sm font-semibold text-ink">Create Ticket Type</h2>
					<p class="mb-4 text-xs text-mute">Define a ticket classification code.</p>

					<form method="POST" action="?/createTicketType" use:enhance class="space-y-4">
						<input type="hidden" name="eventId" value={data.selectedEventId} />

						<div>
							<label
								for="tt-code"
								class="mb-1.5 block font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
								>Code *</label
							>
							<select
								id="tt-code"
								name="code"
								required
								class="w-full rounded-md border border-hairline bg-canvas p-2 text-xs text-ink focus:border-primary focus:outline-none"
							>
								<option value="">Select Code</option>
								<option value="ADULT">ADULT</option>
								<option value="CHILD">CHILD</option>
								<option value="STUDENT">STUDENT</option>
								<option value="COMP">COMP</option>
								<option value="VIP">VIP</option>
								<option value="GROUP">GROUP</option>
							</select>
						</div>

						<div>
							<label
								for="tt-name"
								class="mb-1.5 block font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
								>Name *</label
							>
							<input
								type="text"
								id="tt-name"
								name="name"
								required
								placeholder="e.g. Adult Ticket"
								class="w-full rounded-md border border-hairline bg-canvas p-2 text-xs text-ink focus:border-primary focus:outline-none"
							/>
						</div>

						<button
							type="submit"
							class="w-full cursor-pointer rounded-full bg-primary py-2 text-xs font-semibold text-on-primary transition hover:bg-primary/90"
						>
							Save Ticket Type
						</button>
					</form>
				</div>
			</div>
		{:else if activeTab === 'inventory'}
			<!-- Inventory control -->
			<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
				<h2 class="font-sans text-sm font-semibold text-ink">Capacity & Seating Inventory</h2>
				<p class="mb-5 text-xs text-mute">Current allocation bars across price categories.</p>

				{#if data.offers && data.offers.length > 0}
					<div class="space-y-5">
						{#each data.offers as offer (offer.id)}
							{@const soldCount =
								selectedEvent?.status === 'ONSALE' ? Math.round(offer.capacityCap * 0.35) : 0}
							{@const percent = Math.round((soldCount / offer.capacityCap) * 100)}

							<div class="space-y-1.5">
								<div class="flex items-center justify-between text-xs">
									<span class="font-semibold text-body">{offer.name}</span>
									<span class="font-mono font-medium text-ink">
										{soldCount} / {offer.capacityCap} sold ({percent}%)
									</span>
								</div>
								<div class="h-2 w-full overflow-hidden rounded-full bg-canvas-soft-2">
									<div
										class="h-full rounded-full bg-primary transition-all duration-500"
										style="width: {percent}%"
									></div>
								</div>
							</div>
						{/each}
					</div>
				{:else}
					<div class="flex h-40 flex-col items-center justify-center text-center">
						<span class="text-xs font-semibold text-mute">No inventory offers setup.</span>
					</div>
				{/if}
			</div>
		{:else}
			<!-- Transaction Ledger (Default/Orders) -->
			<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
				<h2 class="mb-4 font-sans text-sm font-semibold text-ink">Recent Transactions</h2>
				<div class="overflow-x-auto">
					<table class="w-full text-left text-xs text-body">
						<thead>
							<tr class="border-b border-hairline text-mute">
								<th class="py-2.5 font-mono font-semibold tracking-wider uppercase">Order ID</th>
								<th class="px-4 py-2.5 font-mono font-semibold tracking-wider uppercase"
									>Customer</th
								>
								<th class="px-4 py-2.5 font-mono font-semibold tracking-wider uppercase"
									>Event Detail</th
								>
								<th class="px-4 py-2.5 text-center font-mono font-semibold tracking-wider uppercase"
									>Qty</th
								>
								<th class="px-4 py-2.5 text-right font-mono font-semibold tracking-wider uppercase"
									>Revenue</th
								>
								<th class="px-4 py-2.5 text-center font-mono font-semibold tracking-wider uppercase"
									>Payment</th
								>
								<th class="py-2.5 text-right font-mono font-semibold tracking-wider uppercase"
									>Status</th
								>
							</tr>
						</thead>
						<tbody class="divide-y divide-hairline">
							<tr class="transition hover:bg-canvas-soft/40">
								<td class="py-3.5 font-mono font-bold text-ink">TXN-9021</td>
								<td class="px-4 py-3.5">
									<h4 class="text-xs font-semibold text-ink">Nguyễn Hoàng Long</h4>
									<p class="font-mono text-[9px] text-mute">long.nh@gmail.com</p>
								</td>
								<td class="max-w-[200px] truncate px-4 py-3.5 font-medium text-body">
									{selectedEvent.title}
								</td>
								<td class="px-4 py-3.5 text-center font-mono text-ink">2</td>
								<td class="px-4 py-3.5 text-right font-mono font-semibold text-ink">
									{formatCurrency(1000000)}
								</td>
								<td class="px-4 py-3.5 text-center font-mono font-semibold text-body">VNPAY</td>
								<td class="py-3.5 text-right">
									<span
										class="inline-flex items-center rounded bg-success/10 px-2 py-0.5 font-mono text-[9px] font-bold text-success uppercase"
									>
										Paid
									</span>
								</td>
							</tr>
							<tr class="transition hover:bg-canvas-soft/40">
								<td class="py-3.5 font-mono font-bold text-ink">TXN-9020</td>
								<td class="px-4 py-3.5">
									<h4 class="text-xs font-semibold text-ink">Phạm Minh Hưng</h4>
									<p class="font-mono text-[9px] text-mute">hung.pm@yahoo.com</p>
								</td>
								<td class="max-w-[200px] truncate px-4 py-3.5 font-medium text-body">
									{selectedEvent.title}
								</td>
								<td class="px-4 py-3.5 text-center font-mono text-ink">1</td>
								<td class="px-4 py-3.5 text-right font-mono font-semibold text-ink">
									{formatCurrency(500000)}
								</td>
								<td class="px-4 py-3.5 text-center font-mono font-semibold text-body">STRIPE</td>
								<td class="py-3.5 text-right">
									<span
										class="inline-flex items-center rounded bg-success/10 px-2 py-0.5 font-mono text-[9px] font-bold text-success uppercase"
									>
										Paid
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
