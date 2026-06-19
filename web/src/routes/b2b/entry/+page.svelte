<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { page } from '$app/state';

	let { data } = $props();

	// Derived active tab from query params, defaulting to 'entrances' (matches layout)
	const activeTab = $derived(page.url.searchParams.get('tab') || 'entrances');

	// Local states for search lookup simulation in Customer Service desk
	let csSearchQuery = $state('');

	interface TicketLookup {
		id: string;
		name: string;
		email: string;
		seat: string;
		status: string;
	}
	let lookupResult = $state<TicketLookup[]>([]);

	const sampleTickets = [
		{
			id: 'TIX-001',
			name: 'Nguyễn Hoàng Long',
			email: 'long.nh@gmail.com',
			seat: 'VIP-A1',
			status: 'ISSUED'
		},
		{
			id: 'TIX-002',
			name: 'Phạm Minh Hưng',
			email: 'hung.pm@yahoo.com',
			seat: 'VIP-A2',
			status: 'CHECKED_IN'
		},
		{
			id: 'TIX-003',
			name: 'Vũ Anh Tuấn',
			email: 'tuan.va@gmail.com',
			seat: 'GA-129',
			status: 'ISSUED'
		}
	];

	function handleLookup() {
		if (!csSearchQuery.trim()) {
			lookupResult = [];
			return;
		}
		lookupResult = sampleTickets.filter(
			(t) =>
				t.name.toLowerCase().includes(csSearchQuery.toLowerCase()) ||
				t.email.toLowerCase().includes(csSearchQuery.toLowerCase()) ||
				t.id.toLowerCase().includes(csSearchQuery.toLowerCase())
		);
	}

	function checkInTicket(ticketId: string) {
		lookupResult = lookupResult.map((t) =>
			t.id === ticketId ? { ...t, status: 'CHECKED_IN' } : t
		);
	}

	function formatDateTime(isoString: string) {
		if (!isoString) return 'N/A';
		const date = new Date(isoString);
		return date.toLocaleDateString('vi-VN', {
			day: '2-digit',
			month: '2-digit',
			year: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	}
</script>

<svelte:head>
	<title>Entry Gates — Ticketpeak for Business</title>
</svelte:head>

<div class="flex flex-1 flex-col space-y-6 p-6">
	<!-- Title block -->
	<div class="flex flex-col gap-1">
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">
			Entry Gates & Check-In Control
		</h1>
		<p class="font-sans text-xs text-body">
			Monitor physical guest check-in rates in real-time and manage gate devices and staff
			allocations.
		</p>
	</div>

	<!-- Page-level Tab Switcher -->
	<div class="flex gap-4 border-b border-hairline font-sans text-xs font-semibold">
		<a
			href="?tab=entrances&organizationId={data.selectedOrgId || ''}"
			class="border-b-2 pb-2 transition-all {activeTab === 'entrances'
				? 'border-primary text-ink'
				: 'border-transparent text-mute hover:text-body'}"
		>
			Ingress Gates
		</a>
		<a
			href="?tab=devices&organizationId={data.selectedOrgId || ''}"
			class="border-b-2 pb-2 transition-all {activeTab === 'devices'
				? 'border-primary text-ink'
				: 'border-transparent text-mute hover:text-body'}"
		>
			Authorized Devices
		</a>
		<a
			href="?tab=staff&organizationId={data.selectedOrgId || ''}"
			class="border-b-2 pb-2 transition-all {activeTab === 'staff'
				? 'border-primary text-ink'
				: 'border-transparent text-mute hover:text-body'}"
		>
			Staff Allocation
		</a>
		<a
			href="?tab=support&organizationId={data.selectedOrgId || ''}"
			class="border-b-2 pb-2 transition-all {activeTab === 'support'
				? 'border-primary text-ink'
				: 'border-transparent text-mute hover:text-body'}"
		>
			Support Desk
		</a>
	</div>

	<!-- Tab content logic -->
	{#if activeTab === 'entrances'}
		<!-- Ingress monitor -->
		<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
			<h2 class="font-sans text-sm font-semibold text-ink">Gate Check-In Simulator Control</h2>
			<p class="mb-5 font-sans text-xs text-mute">
				Select an active event below to launch its ticket validator and entry gate scanner.
			</p>

			{#if data.events && data.events.length > 0}
				<div class="space-y-4">
					{#each data.events as event (event.id)}
						{@const totalCapacity = 300}
						{@const scannedCount = event.status === 'ONSALE' ? 104 : 0}
						{@const scannedPercent = Math.round((scannedCount / totalCapacity) * 100)}

						<div
							class="flex flex-col gap-4 rounded-md border border-hairline bg-canvas p-5 transition hover:bg-canvas-soft/30 md:flex-row md:items-center md:justify-between"
						>
							<!-- Details -->
							<div class="space-y-1.5">
								<h3 class="text-xs font-semibold text-ink">{event.title}</h3>
								<div class="font-mono text-[10px] text-mute">
									📅 {formatDateTime(event.startAt)}
								</div>
								<div class="flex items-center gap-2 text-[10px]">
									<span
										class="inline-flex items-center rounded bg-canvas-soft-2 px-2 py-0.5 font-mono font-bold text-body uppercase"
									>
										{event.status}
									</span>
									<span class="font-semibold text-mute">Gate Status: ACTIVE</span>
								</div>
							</div>

							<!-- Progress bar -->
							<div class="w-full max-w-xs space-y-1.5">
								<div class="flex items-center justify-between text-[11px] font-bold text-mute">
									<span>Check-In Rate</span>
									<span class="font-mono text-ink"
										>{scannedCount} / {totalCapacity} ({scannedPercent}%)</span
									>
								</div>
								<div class="h-1.5 w-full overflow-hidden rounded-full bg-canvas-soft-2">
									<div
										class="h-full rounded-full bg-primary transition-all duration-500"
										style="width: {scannedPercent}%"
									></div>
								</div>
							</div>

							<!-- Actions -->
							<div class="flex items-center gap-3">
								<a
									href="/b2b/check-in/{event.id}"
									class="inline-flex items-center gap-1.5 rounded-full bg-primary px-4 py-1.5 text-xs font-semibold text-on-primary shadow-xs transition hover:bg-primary/90 active:scale-95"
								>
									<svg
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2.5"
										class="h-3.5 w-3.5"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											d="M3.75 4.875c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5A1.125 1.125 0 013.75 9.375v-4.5zM13.5 4.875c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5A1.125 1.125 0 0113.5 9.375v-4.5z"
										/>
									</svg>
									Open Scanner Gate
								</a>
							</div>
						</div>
					{/each}
				</div>
			{:else}
				<div
					class="flex h-32 items-center justify-center rounded-md border border-dashed border-hairline"
				>
					<p class="text-xs font-semibold text-mute">No events found to simulate check-in.</p>
				</div>
			{/if}
		</div>
	{:else if activeTab === 'devices'}
		<!-- Device management -->
		<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
			<div class="mb-5 flex items-center justify-between">
				<div>
					<h2 class="font-sans text-sm font-semibold text-ink">Authorized Gate Devices</h2>
					<p class="font-sans text-xs text-mute">
						Physical scanning hardware and tablet systems paired with this gate.
					</p>
				</div>
				<button
					class="cursor-pointer rounded-full bg-primary px-4 py-1.5 text-xs font-semibold text-on-primary transition hover:bg-primary/90"
				>
					Pair New Terminal
				</button>
			</div>

			<div class="divide-y divide-hairline">
				<div class="flex items-center justify-between py-4">
					<div class="flex items-center gap-3">
						<div
							class="flex h-8 w-8 items-center justify-center rounded bg-primary/10 font-mono text-xs font-bold text-primary"
						>
							IP
						</div>
						<div>
							<h3 class="text-xs font-semibold text-ink">iPad Pro - Main Gate Terminal</h3>
							<p class="font-mono text-[9px] text-mute">IP Address: 192.168.1.144 • Battery: 85%</p>
						</div>
					</div>
					<div class="flex items-center gap-2">
						<span class="h-2 w-2 rounded-full bg-success"></span>
						<span class="font-mono text-[9px] font-bold text-success uppercase">Online</span>
					</div>
				</div>

				<div class="flex items-center justify-between py-4">
					<div class="flex items-center gap-3">
						<div
							class="flex h-8 w-8 items-center justify-center rounded bg-primary/10 font-mono text-xs font-bold text-primary"
						>
							PH
						</div>
						<div>
							<h3 class="text-xs font-semibold text-ink">iPhone 15 - Gate 2 Mobile</h3>
							<p class="font-mono text-[9px] text-mute">IP Address: 192.168.1.168 • Battery: 94%</p>
						</div>
					</div>
					<div class="flex items-center gap-2">
						<span class="h-2 w-2 rounded-full bg-success"></span>
						<span class="font-mono text-[9px] font-bold text-success uppercase">Online</span>
					</div>
				</div>

				<div class="flex items-center justify-between py-4">
					<div class="flex items-center gap-3">
						<div
							class="flex h-8 w-8 items-center justify-center rounded bg-canvas-soft-2 font-mono text-xs font-bold text-mute"
						>
							SC
						</div>
						<div>
							<h3 class="text-xs font-semibold text-ink">Honeywell Laser Barcode Scanner</h3>
							<p class="font-mono text-[9px] text-mute">IP Address: Unknown • Battery: 0%</p>
						</div>
					</div>
					<div class="flex items-center gap-2">
						<span class="h-2 w-2 rounded-full bg-canvas-soft-2"></span>
						<span class="font-mono text-[9px] font-bold text-mute uppercase">Offline</span>
					</div>
				</div>
			</div>
		</div>
	{:else if activeTab === 'staff'}
		<!-- Staff Allocation -->
		<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
			<h2 class="font-sans text-sm font-semibold text-ink">Staff Allocation</h2>
			<p class="mb-5 font-sans text-xs text-mute">
				Assigned gate attendants and authorization policies.
			</p>

			<div class="overflow-x-auto">
				<table class="w-full text-left text-xs text-body">
					<thead>
						<tr class="border-b border-hairline text-mute">
							<th class="py-2.5 font-mono font-semibold tracking-wider uppercase">Staff Member</th>
							<th class="px-4 py-2.5 font-mono font-semibold tracking-wider uppercase"
								>Assigned Role</th
							>
							<th class="px-4 py-2.5 font-mono font-semibold tracking-wider uppercase"
								>Access Gate</th
							>
							<th class="py-2.5 text-right font-mono font-semibold tracking-wider uppercase"
								>Status</th
							>
						</tr>
					</thead>
					<tbody class="divide-y divide-hairline">
						<tr class="transition hover:bg-canvas-soft/40">
							<td class="flex items-center gap-2.5 py-3">
								<div
									class="flex h-7 w-7 items-center justify-center rounded-full bg-primary/10 text-[10px] font-bold text-primary"
								>
									JD
								</div>
								<div>
									<h4 class="text-xs font-semibold text-ink">Jamie Dao</h4>
									<p class="font-mono text-[9px] text-mute">jamie.dao@livenation.com</p>
								</div>
							</td>
							<td class="px-4 py-3 font-sans font-semibold text-ink">Box Office Lead</td>
							<td class="px-4 py-3 font-medium text-body">All Gates</td>
							<td class="py-3 text-right">
								<span
									class="inline-flex items-center rounded bg-success/10 px-2 py-0.5 font-mono text-[9px] font-bold text-success uppercase"
								>
									Active
								</span>
							</td>
						</tr>

						<tr class="transition hover:bg-canvas-soft/40">
							<td class="flex items-center gap-2.5 py-3">
								<div
									class="flex h-7 w-7 items-center justify-center rounded-full bg-primary/10 text-[10px] font-bold text-primary"
								>
									MT
								</div>
								<div>
									<h4 class="text-xs font-semibold text-ink">Minh Tran</h4>
									<p class="font-mono text-[9px] text-mute">minh.tran@livenation.com</p>
								</div>
							</td>
							<td class="px-4 py-3 font-sans font-semibold text-ink">Gate Attendant</td>
							<td class="px-4 py-3 font-medium text-body">Gate 1</td>
							<td class="py-3 text-right">
								<span
									class="inline-flex items-center rounded bg-success/10 px-2 py-0.5 font-mono text-[9px] font-bold text-success uppercase"
								>
									Active
								</span>
							</td>
						</tr>

						<tr class="transition hover:bg-canvas-soft/40">
							<td class="flex items-center gap-2.5 py-3">
								<div
									class="flex h-7 w-7 items-center justify-center rounded-full bg-canvas-soft-2 text-[10px] font-bold text-mute"
								>
									PN
								</div>
								<div>
									<h4 class="text-xs font-semibold text-ink">Phuong Nguyen</h4>
									<p class="font-mono text-[9px] text-mute">phuong.ng@livenation.com</p>
								</div>
							</td>
							<td class="px-4 py-3 font-sans font-semibold text-ink">Gate Attendant</td>
							<td class="px-4 py-3 font-medium text-body">Gate 2</td>
							<td class="py-3 text-right">
								<span
									class="inline-flex items-center rounded bg-canvas-soft-2 px-2 py-0.5 font-mono text-[9px] font-bold text-mute uppercase"
								>
									Offline
								</span>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	{:else if activeTab === 'support'}
		<!-- Customer Service Desk (support) -->
		<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
			<h2 class="font-sans text-sm font-semibold text-ink">Customer Support Ticket Lookup</h2>
			<p class="mb-5 font-sans text-xs text-mute">
				Search attendees by name, email, or Ticket ID to inspect check-in credentials or issue
				manual override check-ins.
			</p>

			<div class="mb-5 flex items-center gap-3">
				<input
					type="text"
					bind:value={csSearchQuery}
					oninput={handleLookup}
					placeholder="Search e.g. Long, Hưng, or TIX-001..."
					class="w-full max-w-md rounded-md border border-hairline bg-canvas p-2 text-xs text-ink focus:border-primary focus:outline-none"
				/>
				<button
					onclick={handleLookup}
					class="cursor-pointer rounded-full bg-primary px-4 py-2 text-xs font-semibold text-on-primary shadow-sm transition hover:bg-primary/90"
				>
					Lookup
				</button>
			</div>

			{#if lookupResult.length > 0}
				<div class="overflow-x-auto">
					<table class="w-full text-left text-xs text-body">
						<thead>
							<tr class="border-b border-hairline text-mute">
								<th class="py-2.5 font-mono font-semibold tracking-wider uppercase">Ticket ID</th>
								<th class="px-4 py-2.5 font-mono font-semibold tracking-wider uppercase"
									>Attendee Name</th
								>
								<th class="px-4 py-2.5 font-mono font-semibold tracking-wider uppercase"
									>Seat / Area</th
								>
								<th class="px-4 py-2.5 font-mono font-semibold tracking-wider uppercase">Status</th>
								<th class="py-2.5 text-right font-mono font-semibold tracking-wider uppercase"
									>Action</th
								>
							</tr>
						</thead>
						<tbody class="divide-y divide-hairline">
							{#each lookupResult as t (t.id)}
								<tr class="transition hover:bg-canvas-soft/40">
									<td class="py-3 font-mono font-bold text-ink">{t.id}</td>
									<td class="px-4 py-3">
										<h4 class="text-xs font-semibold text-ink">{t.name}</h4>
										<p class="font-mono text-[9px] text-mute">{t.email}</p>
									</td>
									<td class="px-4 py-3 font-medium text-body">{t.seat}</td>
									<td class="px-4 py-3">
										{#if t.status === 'CHECKED_IN'}
											<span
												class="inline-flex items-center rounded bg-success/10 px-2 py-0.5 font-mono text-[9px] font-bold text-success uppercase"
											>
												Checked In
											</span>
										{:else}
											<span
												class="inline-flex items-center rounded bg-canvas-soft-2 px-2 py-0.5 font-mono text-[9px] font-bold text-mute uppercase"
											>
												Issued
											</span>
										{/if}
									</td>
									<td class="py-3 text-right">
										{#if t.status !== 'CHECKED_IN'}
											<button
												onclick={() => checkInTicket(t.id)}
												class="cursor-pointer rounded-full bg-primary px-3 py-1 text-[11px] font-semibold text-on-primary transition hover:bg-primary/90"
											>
												Manual Check-In
											</button>
										{:else}
											<span class="text-xs font-bold text-mute">None required</span>
										{/if}
									</td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			{:else if csSearchQuery.trim() !== ''}
				<div class="py-8 text-center text-xs font-bold text-mute">
					No records matching "{csSearchQuery}" found.
				</div>
			{/if}
		</div>
	{/if}
</div>
