<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { page } from '$app/state';

	let { data } = $props();

	// Derived active tab from query params
	const activeTab = $derived(page.url.searchParams.get('tab') || 'ingress');

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

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-8 p-6">
	<!-- Title block -->
	<div>
		<h1 class="text-2xl font-extrabold text-slate-900 md:text-3xl dark:text-white">
			Entry Gates & Check-In Control
		</h1>
		<p class="text-sm font-medium text-slate-500">
			Monitor physical guest check-in rates in real-time and manage gate devices and staff
			allocations.
		</p>
	</div>

	<!-- Tab content logic -->
	{#if activeTab === 'ingress'}
		<!-- Ingress monitor -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
			<h2 class="mb-2 text-base font-bold text-slate-900">Gate Check-In Simulator Control</h2>
			<p class="mb-6 text-xs text-slate-400">
				Select an active event below to launch its ticket validator and entry gate scanner.
			</p>

			{#if data.events && data.events.length > 0}
				<div class="space-y-6">
					{#each data.events as event (event.id)}
						{@const totalCapacity = 300}
						{@const scannedCount = event.status === 'ONSALE' ? 104 : 0}
						{@const scannedPercent = Math.round((scannedCount / totalCapacity) * 100)}

						<div
							class="flex flex-col gap-4 rounded-xl border border-slate-100 bg-white p-5 transition hover:shadow-md md:flex-row md:items-center md:justify-between"
						>
							<!-- Details -->
							<div class="space-y-1">
								<h3 class="text-sm font-bold text-slate-900">{event.title}</h3>
								<div class="text-xs font-semibold text-slate-400">
									📅 {formatDateTime(event.startAt)}
								</div>
								<div class="flex items-center gap-2 text-xs">
									<span
										class="inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[9px] font-extrabold text-slate-700 uppercase"
									>
										{event.status}
									</span>
									<span class="font-semibold text-slate-400">Gate Status: ACTIVE</span>
								</div>
							</div>

							<!-- Progress bar -->
							<div class="w-full max-w-xs space-y-1">
								<div class="flex items-center justify-between text-xs font-bold text-slate-500">
									<span>Check-In Rate</span>
									<span class="text-slate-800"
										>{scannedCount} / {totalCapacity} ({scannedPercent}%)</span
									>
								</div>
								<div class="h-2 w-full overflow-hidden rounded-full bg-slate-100">
									<div
										class="h-full rounded-full bg-purple-500 transition-all duration-500"
										style="width: {scannedPercent}%"
									></div>
								</div>
							</div>

							<!-- Actions -->
							<div class="flex items-center gap-3">
								<a
									href="/b2b/check-in/{event.id}"
									class="inline-flex items-center gap-1.5 rounded-full bg-purple-600 px-4.5 py-2 text-xs font-bold text-white shadow-xs transition hover:bg-purple-700 active:scale-95"
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
					class="flex h-36 items-center justify-center rounded-xl border border-dashed border-slate-300"
				>
					<p class="text-sm font-semibold text-slate-400">No events found to simulate check-in.</p>
				</div>
			{/if}
		</div>
	{:else if activeTab === 'devices'}
		<!-- Device management -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
			<div class="mb-6 flex items-center justify-between">
				<div>
					<h2 class="text-base font-bold text-slate-900">Authorized Gate Devices</h2>
					<p class="text-xs text-slate-400">
						Physical scanning hardware and tablet systems paired with this gate.
					</p>
				</div>
				<button
					class="rounded-full bg-blue-600 px-4 py-2 text-xs font-bold text-white transition hover:bg-blue-700"
				>
					Pair New Terminal
				</button>
			</div>

			<div class="divide-y divide-slate-100">
				<div class="flex items-center justify-between py-4">
					<div class="flex items-center gap-3">
						<div
							class="flex h-8.5 w-8.5 items-center justify-center rounded-lg bg-blue-50 text-xs font-bold text-blue-600"
						>
							iP
						</div>
						<div>
							<h3 class="text-xs font-bold text-slate-800">iPad Pro - Main Gate Terminal</h3>
							<span class="text-[10px] font-semibold text-slate-400"
								>IP Address: 192.168.1.144 • Battery: 85%</span
							>
						</div>
					</div>
					<div class="flex items-center gap-2">
						<span class="h-2 w-2 rounded-full bg-green-500"></span>
						<span class="text-[10px] font-bold text-green-600 uppercase">ONLINE</span>
					</div>
				</div>

				<div class="flex items-center justify-between py-4">
					<div class="flex items-center gap-3">
						<div
							class="flex h-8.5 w-8.5 items-center justify-center rounded-lg bg-blue-50 text-xs font-bold text-blue-600"
						>
							Ph
						</div>
						<div>
							<h3 class="text-xs font-bold text-slate-800">iPhone 15 - Gate 2 Mobile</h3>
							<span class="text-[10px] font-semibold text-slate-400"
								>IP Address: 192.168.1.168 • Battery: 94%</span
							>
						</div>
					</div>
					<div class="flex items-center gap-2">
						<span class="h-2 w-2 rounded-full bg-green-500"></span>
						<span class="text-[10px] font-bold text-green-600 uppercase">ONLINE</span>
					</div>
				</div>

				<div class="flex items-center justify-between py-4">
					<div class="flex items-center gap-3">
						<div
							class="flex h-8.5 w-8.5 items-center justify-center rounded-lg bg-slate-100 text-xs font-bold text-slate-500"
						>
							Sc
						</div>
						<div>
							<h3 class="text-xs font-bold text-slate-800">Honeywell Laser Barcode Scanner</h3>
							<span class="text-[10px] font-semibold text-slate-400"
								>IP Address: Unknown • Battery: 0%</span
							>
						</div>
					</div>
					<div class="flex items-center gap-2">
						<span class="h-2 w-2 rounded-full bg-slate-300"></span>
						<span class="text-[10px] font-bold text-slate-400 uppercase">OFFLINE</span>
					</div>
				</div>
			</div>
		</div>
	{:else if activeTab === 'staff'}
		<!-- Staff Allocation -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
			<h2 class="text-base font-bold text-slate-900">Staff Allocation</h2>
			<p class="mb-6 text-xs text-slate-400">
				Assigned gate attendants and authorization policies.
			</p>

			<div class="overflow-x-auto">
				<table class="w-full text-left text-sm text-slate-500">
					<thead>
						<tr
							class="border-b border-slate-100 text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
						>
							<th class="py-3 pr-4">Staff Member</th>
							<th class="px-4 py-3">Assigned Role</th>
							<th class="px-4 py-3">Access Gate</th>
							<th class="py-3 pl-4 text-right">Status</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-slate-100">
						<tr class="transition hover:bg-slate-50/50">
							<td class="flex items-center gap-2.5 py-3.5 pr-4">
								<div
									class="flex h-7 w-7 items-center justify-center rounded-full bg-emerald-50 text-xs font-bold text-emerald-600"
								>
									JD
								</div>
								<div>
									<h4 class="text-xs font-bold text-slate-900">Jamie Dao</h4>
									<span class="text-[9px] font-medium text-slate-400">jamie.dao@livenation.com</span
									>
								</div>
							</td>
							<td class="px-4 py-3.5 text-xs font-semibold text-slate-700">Box Office Lead</td>
							<td class="px-4 py-3.5 text-xs font-semibold text-slate-700">All Gates</td>
							<td class="py-3.5 pl-4 text-right">
								<span
									class="inline-flex items-center rounded-full bg-green-50 px-2 py-0.5 text-[9px] font-bold text-green-700 uppercase"
									>Active</span
								>
							</td>
						</tr>

						<tr class="transition hover:bg-slate-50/50">
							<td class="flex items-center gap-2.5 py-3.5 pr-4">
								<div
									class="flex h-7 w-7 items-center justify-center rounded-full bg-indigo-50 text-xs font-bold text-indigo-600"
								>
									MT
								</div>
								<div>
									<h4 class="text-xs font-bold text-slate-900">Minh Tran</h4>
									<span class="text-[9px] font-medium text-slate-400">minh.tran@livenation.com</span
									>
								</div>
							</td>
							<td class="px-4 py-3.5 text-xs font-semibold text-slate-700">Gate Attendant</td>
							<td class="px-4 py-3.5 text-xs font-semibold text-slate-700">Gate 1</td>
							<td class="py-3.5 pl-4 text-right">
								<span
									class="inline-flex items-center rounded-full bg-green-50 px-2 py-0.5 text-[9px] font-bold text-green-700 uppercase"
									>Active</span
								>
							</td>
						</tr>

						<tr class="transition hover:bg-slate-50/50">
							<td class="flex items-center gap-2.5 py-3.5 pr-4">
								<div
									class="flex h-7 w-7 items-center justify-center rounded-full bg-indigo-50 text-xs font-bold text-indigo-600"
								>
									PN
								</div>
								<div>
									<h4 class="text-xs font-bold text-slate-900">Phuong Nguyen</h4>
									<span class="text-[9px] font-medium text-slate-400">phuong.ng@livenation.com</span
									>
								</div>
							</td>
							<td class="px-4 py-3.5 text-xs font-semibold text-slate-700">Gate Attendant</td>
							<td class="px-4 py-3.5 text-xs font-semibold text-slate-700">Gate 2</td>
							<td class="py-3.5 pl-4 text-right">
								<span
									class="inline-flex items-center rounded-full bg-slate-50 px-2 py-0.5 text-[9px] font-bold text-slate-500 uppercase"
									>Offline</span
								>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	{:else}
		<!-- Customer Service Desk -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
			<h2 class="text-base font-bold text-slate-900">Customer Support Ticket Lookup</h2>
			<p class="mb-6 text-xs text-slate-400">
				Search attendees by name, email, or Ticket ID to inspect check-in credentials or issue
				manual override check-ins.
			</p>

			<div class="mb-6 flex items-center gap-3">
				<input
					type="text"
					bind:value={csSearchQuery}
					oninput={handleLookup}
					placeholder="Search e.g. Long, Hưng, or TIX-001..."
					class="w-full max-w-md rounded-xl border border-slate-200 bg-white p-2.5 text-xs font-bold text-slate-700 shadow-sm focus:border-blue-500 focus:outline-none"
				/>
				<button
					onclick={handleLookup}
					class="rounded-xl bg-blue-600 px-5 py-2.5 text-xs font-bold text-white shadow-xs transition hover:bg-blue-700"
				>
					Lookup
				</button>
			</div>

			{#if lookupResult.length > 0}
				<div class="overflow-x-auto">
					<table class="w-full text-left text-sm text-slate-500">
						<thead>
							<tr
								class="border-b border-slate-100 text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
							>
								<th class="py-3 pr-4">Ticket ID</th>
								<th class="px-4 py-3">Attendee Name</th>
								<th class="px-4 py-3">Seat / Area</th>
								<th class="px-4 py-3">Status</th>
								<th class="py-3 pl-4 text-right">Action</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-slate-100">
							{#each lookupResult as t (t.id)}
								<tr class="transition hover:bg-slate-50/50">
									<td class="py-3.5 pr-4 font-mono font-bold text-slate-900">{t.id}</td>
									<td class="px-4 py-3.5">
										<div class="font-bold text-slate-950">{t.name}</div>
										<div class="text-[10px] font-semibold text-slate-400">{t.email}</div>
									</td>
									<td class="px-4 py-3.5 text-xs font-semibold text-slate-700">{t.seat}</td>
									<td class="px-4 py-3.5">
										{#if t.status === 'CHECKED_IN'}
											<span
												class="inline-flex items-center rounded-full bg-emerald-50 px-2 py-0.5 text-[9px] font-bold text-emerald-700 uppercase"
												>Checked In</span
											>
										{:else}
											<span
												class="inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[9px] font-bold text-slate-600 uppercase"
												>Issued</span
											>
										{/if}
									</td>
									<td class="py-3.5 pl-4 text-right">
										{#if t.status !== 'CHECKED_IN'}
											<button
												onclick={() => checkInTicket(t.id)}
												class="cursor-pointer rounded-full bg-purple-600 px-3 py-1 text-xs font-bold text-white transition hover:bg-purple-700"
											>
												Manual Check-In
											</button>
										{:else}
											<span class="text-xs font-bold text-slate-400">None required</span>
										{/if}
									</td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			{:else if csSearchQuery.trim() !== ''}
				<div class="py-8 text-center text-xs font-bold text-slate-400">
					No records matching "{csSearchQuery}" found.
				</div>
			{/if}
		</div>
	{/if}
</div>
