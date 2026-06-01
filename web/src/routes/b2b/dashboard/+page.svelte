<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */

	let { data } = $props();

	// Svelte 5 states
	let selectedOrgId = $state(data.selectedOrgId);

	// Event stats summaries
	const totalEvents = $derived(data.events?.length || 0);
	const activeEvents = $derived(
		data.events?.filter(
			(e: { status: string }) => e.status === 'SALES_ACTIVE' || e.status === 'PUBLISHED'
		).length || 0
	);

	function handleOrgChange(e: Event) {
		const target = e.target as HTMLSelectElement;
		selectedOrgId = target.value;
		window.location.href = `?organizationId=${selectedOrgId}`;
	}
</script>

<svelte:head>
	<title>Organizer Dashboard — Ticketpeak for Business</title>
</svelte:head>

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-8 p-6">
	<!-- Top Bar / Organization Switcher -->
	<div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
		<div>
			<h1 class="text-2xl font-bold text-slate-900 md:text-3xl">Promoter Dashboard</h1>
			<p class="text-sm font-medium text-slate-500">
				High-level business overview, sales intelligence, and system gateway.
			</p>
		</div>

		<!-- Organization Switcher -->
		{#if data.organizations && data.organizations.length > 0}
			<div class="flex items-center gap-2">
				<label
					for="org-select"
					class="text-xs font-semibold tracking-wider text-slate-500 uppercase"
				>
					Organization:
				</label>
				<select
					id="org-select"
					value={selectedOrgId}
					onchange={handleOrgChange}
					class="rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm font-semibold text-slate-900 shadow-sm focus:border-primary focus:outline-none"
				>
					{#each data.organizations as org (org.id)}
						<option value={org.id}>{org.name}</option>
					{/each}
				</select>
			</div>
		{/if}
	</div>

	<!-- KPI Analytics Overview -->
	<div class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
		<!-- KPI 1 -->
		<div class="rounded-xl border border-hairline bg-canvas p-5 shadow-xs transition hover:shadow">
			<div class="flex items-center justify-between text-slate-400">
				<span class="text-xs font-semibold tracking-wider uppercase">Total Events</span>
				<svg
					class="h-5 w-5 text-slate-400"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
					stroke-width="2"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
					/>
				</svg>
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-bold text-slate-900">{totalEvents}</span>
				<span class="text-xs font-medium text-slate-500">hosted events</span>
			</div>
		</div>

		<!-- KPI 2 -->
		<div class="rounded-xl border border-hairline bg-canvas p-5 shadow-xs transition hover:shadow">
			<div class="flex items-center justify-between text-slate-400">
				<span class="text-xs font-semibold tracking-wider uppercase">Active Ticket Sales</span>
				<span class="flex h-2.5 w-2.5 animate-pulse rounded-full bg-success"></span>
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-bold text-slate-900">{activeEvents}</span>
				<span class="text-xs font-medium text-slate-500">active items</span>
			</div>
		</div>

		<!-- KPI 3 -->
		<div class="rounded-xl border border-hairline bg-canvas p-5 shadow-xs transition hover:shadow">
			<div class="flex items-center justify-between text-slate-400">
				<span class="text-xs font-semibold tracking-wider uppercase">Estimated Revenue</span>
				<svg
					class="h-5 w-5 text-slate-400"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
					stroke-width="2"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M12 16v1"
					/>
				</svg>
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-bold text-slate-900">450.0M</span>
				<span class="text-xs font-semibold text-emerald-600">VND</span>
			</div>
		</div>

		<!-- KPI 4 -->
		<div class="rounded-xl border border-hairline bg-canvas p-5 shadow-xs transition hover:shadow">
			<div class="flex items-center justify-between text-slate-400">
				<span class="text-xs font-semibold tracking-wider uppercase">Avg Check-In Rate</span>
				<svg
					class="h-5 w-5 text-slate-400"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
					stroke-width="2"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
					/>
				</svg>
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-bold text-slate-900">89.4%</span>
				<span class="text-xs font-medium text-slate-500">check-in rate</span>
			</div>
		</div>
	</div>

	<!-- Sales Charts & Transactions Overview -->
	<div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
		<!-- Left / Velocity Analytics -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs lg:col-span-2">
			<div class="flex items-center justify-between">
				<div>
					<h2 class="text-base font-semibold text-slate-900">Real-Time Ticket Checkout Velocity</h2>
					<p class="text-xs text-slate-400">
						Hourly checkout counts and spikes in the last 24 hours.
					</p>
				</div>
				<span class="rounded-full bg-blue-50 px-2.5 py-1 text-[10px] font-semibold text-blue-600"
					>LIVE MONITORING</span
				>
			</div>

			<div class="mt-8 flex h-48 items-end gap-3.5 border-b border-slate-100 pb-2">
				{#each [40, 25, 45, 60, 80, 55, 90, 70, 110, 85, 130, 95] as height, i (i)}
					<div class="group relative flex flex-1 flex-col items-center">
						<div
							class="absolute bottom-full mb-1 scale-0 rounded bg-slate-900 px-2 py-1 text-[9px] font-semibold text-white transition-all group-hover:scale-100"
						>
							{height} tix
						</div>
						<div
							class="w-full rounded-t-md bg-gradient-to-t from-blue-500 to-indigo-500 opacity-80 transition hover:opacity-100"
							style="height: {height}px"
						></div>
					</div>
				{/each}
			</div>

			<div class="mt-3 flex justify-between text-[10px] font-semibold text-slate-400 uppercase">
				<span>12 Hours Ago</span>
				<span>6 Hours Ago</span>
				<span>Just Now</span>
			</div>
		</div>

		<!-- Right / Recent Alert Log / Scanner Feed -->
		<div
			class="flex flex-col justify-between rounded-2xl border border-slate-100 bg-white p-6 shadow-xs"
		>
			<div>
				<h2 class="text-base font-semibold text-slate-900">Live Entrance Feed</h2>
				<p class="text-xs text-slate-400">Activity stream from smart QR check-in gates.</p>
			</div>

			<div class="mt-4 flex max-h-48 flex-1 flex-col gap-3 overflow-y-auto pr-1">
				<div class="flex items-start gap-2.5 border-l-2 border-emerald-500 pl-3">
					<div class="flex flex-col">
						<span class="text-xs font-semibold text-slate-800">Gate A - VIP check-in</span>
						<span class="text-[10px] text-slate-400">Nguyễn Hoàng Long check-in at VIP Seat A1</span
						>
					</div>
				</div>
				<div class="flex items-start gap-2.5 border-l-2 border-emerald-500 pl-3">
					<div class="flex flex-col">
						<span class="text-xs font-semibold text-slate-800">Gate B - Standard check-in</span>
						<span class="text-[10px] text-slate-400">Phạm Minh Hưng check-in at GA Area</span>
					</div>
				</div>
				<div class="flex items-start gap-2.5 border-l-2 border-purple-500 pl-3">
					<div class="flex flex-col">
						<span class="text-xs font-semibold text-slate-800">Device Activated</span>
						<span class="text-[10px] text-slate-400"
							>Scan Device TP-004 went online at Main Ingress</span
						>
					</div>
				</div>
			</div>

			<div class="mt-4 border-t border-slate-100 pt-3">
				<a
					href="/b2b/entry"
					class="text-xs font-semibold text-blue-600 transition hover:text-blue-700"
				>
					View Check-In Settings →
				</a>
			</div>
		</div>
	</div>

	<!-- Module Gateway / Hub Links -->
	<div>
		<div class="mb-4">
			<h2 class="text-base font-semibold text-slate-900">Business Control Center</h2>
			<p class="text-xs text-slate-400">Quick shortcuts to B2B Organizer Core modules.</p>
		</div>

		<div class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
			<!-- Link 1: Events -->
			<a
				href="/b2b/events"
				class="group flex flex-col justify-between rounded-2xl border border-slate-100 bg-white p-5 shadow-xs transition hover:-translate-y-0.5 hover:shadow-md"
			>
				<div class="flex items-center gap-3">
					<span
						class="flex h-9 w-9 items-center justify-center rounded-xl bg-blue-50 text-blue-600 transition group-hover:bg-blue-600 group-hover:text-white"
					>
						🎫
					</span>
					<div class="flex flex-col">
						<span class="text-xs font-semibold text-slate-800">Events Portal</span>
						<span class="text-[10px] text-slate-400">Catalog & Manifests</span>
					</div>
				</div>
				<span
					class="mt-4 inline-flex items-center gap-1 text-[10px] font-semibold text-blue-600 transition group-hover:translate-x-1"
				>
					Manage Catalog <span>→</span>
				</span>
			</a>

			<!-- Link 2: Sales -->
			<a
				href="/b2b/sales"
				class="group flex flex-col justify-between rounded-2xl border border-slate-100 bg-white p-5 shadow-xs transition hover:-translate-y-0.5 hover:shadow-md"
			>
				<div class="flex items-center gap-3">
					<span
						class="flex h-9 w-9 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600 transition group-hover:bg-emerald-600 group-hover:text-white"
					>
						💰
					</span>
					<div class="flex flex-col">
						<span class="text-xs font-semibold text-slate-800">Sales Office</span>
						<span class="text-[10px] text-slate-400">Offers & Pricing</span>
					</div>
				</div>
				<span
					class="mt-4 inline-flex items-center gap-1 text-[10px] font-semibold text-emerald-600 transition group-hover:translate-x-1"
				>
					Adjust Pricing <span>→</span>
				</span>
			</a>

			<!-- Link 3: Entry -->
			<a
				href="/b2b/entry"
				class="group flex flex-col justify-between rounded-2xl border border-slate-100 bg-white p-5 shadow-xs transition hover:-translate-y-0.5 hover:shadow-md"
			>
				<div class="flex items-center gap-3">
					<span
						class="flex h-9 w-9 items-center justify-center rounded-xl bg-purple-50 text-purple-600 transition group-hover:bg-purple-600 group-hover:text-white"
					>
						🚪
					</span>
					<div class="flex flex-col">
						<span class="text-xs font-semibold text-slate-800">Entry Gates</span>
						<span class="text-[10px] text-slate-400">Scan & Check-In</span>
					</div>
				</div>
				<span
					class="mt-4 inline-flex items-center gap-1 text-[10px] font-semibold text-purple-600 transition group-hover:translate-x-1"
				>
					Manage Entry <span>→</span>
				</span>
			</a>

			<!-- Link 4: Reports -->
			<a
				href="/b2b/reports"
				class="group flex flex-col justify-between rounded-2xl border border-slate-100 bg-white p-5 shadow-xs transition hover:-translate-y-0.5 hover:shadow-md"
			>
				<div class="flex items-center gap-3">
					<span
						class="flex h-9 w-9 items-center justify-center rounded-xl bg-amber-50 text-amber-600 transition group-hover:bg-amber-600 group-hover:text-white"
					>
						📊
					</span>
					<div class="flex flex-col">
						<span class="text-xs font-semibold text-slate-800">Financial Reports</span>
						<span class="text-[10px] text-slate-400">Settlements & Audits</span>
					</div>
				</div>
				<span
					class="mt-4 inline-flex items-center gap-1 text-[10px] font-semibold text-amber-600 transition group-hover:translate-x-1"
				>
					Audit Revenue <span>→</span>
				</span>
			</a>
		</div>
	</div>
</div>
