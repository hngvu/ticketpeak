<script lang="ts">
	import { page } from '$app/state';

	// Derived active tab from query params
	const activeTab = $derived(page.url.searchParams.get('tab') || 'tax');

	// Mock seating levels
	const seatingLevels = [
		{ name: 'VVIP Zone', sold: '76', capacity: '80', percentage: 95, color: 'bg-rose-500' },
		{ name: 'VIP Standing', sold: '112', capacity: '120', percentage: 93, color: 'bg-amber-500' },
		{ name: 'GA Standing', sold: '140', capacity: '200', percentage: 70, color: 'bg-emerald-500' }
	];

	function formatCurrency(val: number) {
		return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
	}
</script>

<svelte:head>
	<title>Business Reports — Ticketpeak for Business</title>
</svelte:head>

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-8 p-6">
	<!-- Title block -->
	<div>
		<h1 class="text-2xl font-extrabold text-slate-900 md:text-3xl dark:text-white">
			Business Analytics & Reports
		</h1>
		<p class="text-sm font-medium text-slate-500">
			Review gross profit margins, seating charts occupancy metrics, and dynamic payout balances.
		</p>
	</div>

	<!-- Tab content logic -->
	{#if activeTab === 'audits'}
		<!-- Financial Audits -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
			<h2 class="text-base font-bold text-slate-900">System Financial Audit Logs</h2>
			<p class="mb-6 text-xs text-slate-400">
				Chronological history of commercial edits, tier modifications, and payout requests.
			</p>

			<div class="space-y-4">
				<div class="flex items-start gap-3 rounded-xl border border-slate-100 bg-slate-50/50 p-4">
					<span
						class="mt-0.5 rounded bg-blue-50 px-2 py-0.5 text-[9px] font-bold text-blue-600 uppercase"
						>INFO</span
					>
					<div>
						<h4 class="text-xs font-bold text-slate-800">Event status changed to ONSALE</h4>
						<p class="mt-0.5 text-[10px] font-semibold text-slate-400">
							By system scheduler • 2026-05-27 10:00:02
						</p>
					</div>
				</div>

				<div class="flex items-start gap-3 rounded-xl border border-slate-100 bg-slate-50/50 p-4">
					<span
						class="mt-0.5 rounded bg-emerald-50 px-2 py-0.5 text-[9px] font-bold text-emerald-600 uppercase"
						>UPDATE</span
					>
					<div>
						<h4 class="text-xs font-bold text-slate-800">
							Offer "Vé GA Standard" pricing modified
						</h4>
						<p class="mt-0.5 text-[10px] font-semibold text-slate-400">
							By jamie.dao@livenation.com • New Value: 500,000 VND • 2026-05-26 15:42:19
						</p>
					</div>
				</div>

				<div class="flex items-start gap-3 rounded-xl border border-slate-100 bg-slate-50/50 p-4">
					<span
						class="mt-0.5 rounded bg-purple-50 px-2 py-0.5 text-[9px] font-bold text-purple-600 uppercase"
						>ACTION</span
					>
					<div>
						<h4 class="text-xs font-bold text-slate-800">
							Payout method linked to BIDV Bank Account
						</h4>
						<p class="mt-0.5 text-[10px] font-semibold text-slate-400">
							By organizer@ticketpeak.com • Live Nation Vietnam Joint Stock • 2026-05-25 09:12:00
						</p>
					</div>
				</div>
			</div>
		</div>
	{:else if activeTab === 'settlements'}
		<!-- Settlements reports -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
			<h2 class="text-base font-bold text-slate-900">Event Settlements Summary</h2>
			<p class="mb-6 text-xs text-slate-400">
				Approved settlement schedules and net earnings breakdown.
			</p>

			<div class="mb-6 grid grid-cols-1 gap-5 md:grid-cols-3">
				<div class="rounded-xl border border-slate-100 bg-slate-50/40 p-4">
					<span class="text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
						>Settled Gross</span
					>
					<h3 class="mt-1 text-xl font-extrabold text-slate-900">{formatCurrency(450000000)}</h3>
				</div>
				<div class="rounded-xl border border-slate-100 bg-slate-50/40 p-4">
					<span class="text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
						>Platform Fees (10%)</span
					>
					<h3 class="mt-1 text-xl font-extrabold text-red-600">{formatCurrency(45000000)}</h3>
				</div>
				<div class="rounded-xl border border-slate-100 bg-slate-50/40 p-4">
					<span class="text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
						>Net Organizer Payout</span
					>
					<h3 class="mt-1 text-xl font-extrabold text-green-600">{formatCurrency(405000000)}</h3>
				</div>
			</div>

			<div class="overflow-x-auto">
				<table class="w-full text-left text-sm text-slate-500">
					<thead>
						<tr
							class="border-b border-slate-100 text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
						>
							<th class="py-3 pr-4">Event Date</th>
							<th class="px-4 py-3">Gross Ticket Sales</th>
							<th class="px-4 py-3 text-right">Commissions</th>
							<th class="py-3 pl-4 text-right">Net Settlement</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-slate-100">
						<tr class="transition hover:bg-slate-50/50">
							<td class="py-3.5 pr-4 font-semibold text-slate-900">2026-05-28</td>
							<td class="px-4 py-3.5 text-xs text-slate-700">{formatCurrency(250000000)}</td>
							<td class="px-4 py-3.5 text-right text-xs text-red-600">{formatCurrency(25000000)}</td
							>
							<td class="py-3.5 pl-4 text-right text-xs font-bold text-green-600"
								>{formatCurrency(225000000)}</td
							>
						</tr>
						<tr class="transition hover:bg-slate-50/50">
							<td class="py-3.5 pr-4 font-semibold text-slate-900">2026-05-27</td>
							<td class="px-4 py-3.5 text-xs text-slate-700">{formatCurrency(200000000)}</td>
							<td class="px-4 py-3.5 text-right text-xs text-red-600">{formatCurrency(20000000)}</td
							>
							<td class="py-3.5 pl-4 text-right text-xs font-bold text-green-600"
								>{formatCurrency(180000000)}</td
							>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	{:else if activeTab === 'payouts'}
		<!-- Payout tracking -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
			<h2 class="text-base font-bold text-slate-900">Organizer Payout Status</h2>
			<p class="mb-6 text-xs text-slate-400">
				Track standard bank payouts and wire settlement progressions.
			</p>

			<div class="space-y-6 rounded-xl border border-slate-100 bg-slate-50/20 p-5">
				<div class="flex items-center justify-between border-b border-slate-100 pb-4">
					<div>
						<h3 class="text-xs font-bold text-slate-800">BIDV Joint Stock bank transfer</h3>
						<span class="text-[9px] font-semibold text-slate-400"
							>Account: 3101000******495 • Live Nation Vietnam</span
						>
					</div>
					<div class="text-right">
						<h4 class="text-sm font-extrabold text-slate-900">{formatCurrency(405000000)}</h4>
						<span
							class="mt-1 inline-flex items-center rounded-full bg-amber-50 px-2 py-0.5 text-[9px] font-bold text-amber-700 uppercase"
							>PROCESSING</span
						>
					</div>
				</div>

				<!-- Visual Step progress timeline -->
				<div class="space-y-4">
					<div class="flex gap-3">
						<span
							class="flex h-5 w-5 items-center justify-center rounded-full bg-green-500 text-[10px] font-bold text-white"
							>✓</span
						>
						<div>
							<h4 class="text-xs font-bold text-slate-800">Payout Initiated</h4>
							<p class="text-[9px] font-semibold text-slate-400">
								2026-05-28 14:00 • Standard scheduler reconciliation sweep
							</p>
						</div>
					</div>

					<div class="flex gap-3">
						<span
							class="flex h-5 w-5 items-center justify-center rounded-full bg-green-500 text-[10px] font-bold text-white"
							>✓</span
						>
						<div>
							<h4 class="text-xs font-bold text-slate-800">Approved by Platform Admin</h4>
							<p class="text-[9px] font-semibold text-slate-400">
								2026-05-28 16:30 • Compliance check passed
							</p>
						</div>
					</div>

					<div class="flex gap-3">
						<span
							class="flex h-5 w-5 items-center justify-center rounded-full bg-blue-100 text-[10px] font-extrabold text-blue-600"
							>!</span
						>
						<div>
							<h4 class="text-xs font-bold text-slate-800">Bank Transfer Pending</h4>
							<p class="text-[9px] font-semibold text-slate-400">
								Processing via Interbank gateway • Expected completion within 24 hours
							</p>
						</div>
					</div>
				</div>
			</div>
		</div>
	{:else}
		<!-- Tax & Fees analysis -->
		<div class="space-y-8">
			<!-- KPIs row -->
			<div class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
				<div class="rounded-xl border border-slate-100 bg-white p-5 shadow-xs">
					<span class="text-xs font-bold tracking-wider text-slate-400 uppercase"
						>Gross Ticket Sales</span
					>
					<div class="mt-2 flex items-baseline gap-2">
						<span class="text-3xl font-extrabold text-slate-900">{formatCurrency(450000000)}</span>
					</div>
				</div>
				<div class="rounded-xl border border-slate-100 bg-white p-5 shadow-xs">
					<span class="text-xs font-bold tracking-wider text-slate-400 uppercase"
						>Net Organizer Payout</span
					>
					<div class="mt-2 flex items-baseline gap-2">
						<span class="text-3xl font-extrabold text-slate-900">{formatCurrency(405000000)}</span>
					</div>
					<div class="mt-1 text-[10px] font-semibold text-slate-400">
						10% standard platform commission fee deducted
					</div>
				</div>
				<div class="rounded-xl border border-slate-100 bg-white p-5 shadow-xs">
					<span class="text-xs font-bold tracking-wider text-slate-400 uppercase"
						>Average Order Value</span
					>
					<div class="mt-2 flex items-baseline gap-2">
						<span class="text-3xl font-extrabold text-slate-900">{formatCurrency(2440000)}</span>
					</div>
				</div>
			</div>

			<!-- Seating Levels Occupancy Chart -->
			<div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
				<div class="rounded-xl border border-slate-100 bg-white p-6 shadow-xs">
					<h2 class="mb-2 text-lg font-bold text-slate-900">Occupancy by Seating Level</h2>
					<p class="mb-6 text-xs text-slate-400">
						Percentage of tickets sold across the price categories.
					</p>

					<div class="space-y-6">
						{#each seatingLevels as level (level.name)}
							<div class="space-y-2">
								<div class="flex items-center justify-between text-sm font-semibold">
									<span class="text-slate-800">{level.name}</span>
									<span class="text-slate-900"
										>{level.sold} / {level.capacity} sold ({level.percentage}%)</span
									>
								</div>
								<div class="h-3 w-full overflow-hidden rounded-full bg-slate-100">
									<div
										class="h-full rounded-full transition-all duration-500 {level.color}"
										style="width: {level.percentage}%"
									></div>
								</div>
							</div>
						{/each}
					</div>
				</div>

				<!-- Channel distribution -->
				<div class="rounded-xl border border-slate-100 bg-white p-6 shadow-xs">
					<h2 class="mb-2 text-lg font-bold text-slate-900">Sales Channel Distribution</h2>
					<p class="mb-6 text-xs text-slate-400">
						Where your ticket buyers discovered and checked out.
					</p>

					<div class="space-y-4">
						<div class="flex items-center justify-between rounded-lg bg-slate-50 p-3">
							<span class="text-sm font-bold text-slate-700">Direct Web Portal</span>
							<span class="text-sm font-extrabold text-slate-900"
								>76% <span class="text-xs text-slate-400">sales</span></span
							>
						</div>
						<div class="flex items-center justify-between rounded-lg bg-slate-50 p-3">
							<span class="text-sm font-bold text-slate-700">Organizer Referral Links</span>
							<span class="text-sm font-extrabold text-slate-900"
								>14% <span class="text-xs text-slate-400">sales</span></span
							>
						</div>
						<div class="flex items-center justify-between rounded-lg bg-slate-50 p-3">
							<span class="text-sm font-bold text-slate-700">Secondary Resale Market</span>
							<span class="text-sm font-extrabold text-slate-900"
								>10% <span class="text-xs text-slate-400">sales</span></span
							>
						</div>
					</div>
				</div>
			</div>
		</div>
	{/if}
</div>
