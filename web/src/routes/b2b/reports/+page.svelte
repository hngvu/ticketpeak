<script lang="ts">
	import { page } from '$app/state';

	// Derived active tab from query params, defaulting to 'overview'
	const activeTab = $derived(page.url.searchParams.get('tab') || 'overview');

	// Mock seating levels
	const seatingLevels = [
		{ name: 'VVIP Zone', sold: 76, capacity: 80, percentage: 95, color: 'bg-primary' },
		{ name: 'VIP Standing', sold: 112, capacity: 120, percentage: 93, color: 'bg-primary/80' },
		{ name: 'GA Standing', sold: 140, capacity: 200, percentage: 70, color: 'bg-primary/50' }
	];

	function formatCurrency(val: number) {
		return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
	}
</script>

<svelte:head>
	<title>Business Reports — Ticketpeak for Business</title>
</svelte:head>

<div class="flex flex-1 flex-col space-y-6 p-6">
	<!-- Title block -->
	<div class="flex flex-col gap-1">
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">
			Business Analytics & Reports
		</h1>
		<p class="font-sans text-xs text-body">
			Review gross profit margins, seating charts occupancy metrics, and dynamic payout balances.
		</p>
	</div>

	{#if activeTab === 'overview'}
		<!-- Overview tab -->
		<div class="space-y-6">
			<!-- KPIs row -->
			<div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
				<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
					<p class="font-mono text-[10px] font-semibold tracking-wider text-mute uppercase">
						Gross Ticket Sales
					</p>
					<p class="mt-2 font-mono text-2xl font-bold tracking-tight text-ink">
						{formatCurrency(450000000)}
					</p>
				</div>
				<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
					<p class="font-mono text-[10px] font-semibold tracking-wider text-mute uppercase">
						Net Organizer Payout
					</p>
					<p class="mt-2 font-mono text-2xl font-bold tracking-tight text-ink">
						{formatCurrency(405000000)}
					</p>
					<p class="mt-1 text-[10px] text-mute">10% standard platform commission fee deducted</p>
				</div>
				<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
					<p class="font-mono text-[10px] font-semibold tracking-wider text-mute uppercase">
						Average Order Value
					</p>
					<p class="mt-2 font-mono text-2xl font-bold tracking-tight text-ink">
						{formatCurrency(2440000)}
					</p>
				</div>
			</div>

			<div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
				<!-- Seating Occupancy -->
				<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
					<h2 class="font-sans text-sm font-semibold text-ink">Occupancy by Seating Level</h2>
					<p class="mb-5 text-xs text-mute">
						Percentage of tickets sold across the price categories.
					</p>

					<div class="space-y-5">
						{#each seatingLevels as level}
							<div class="space-y-1.5">
								<div class="flex items-center justify-between text-xs">
									<span class="font-semibold text-body">{level.name}</span>
									<span class="font-mono font-medium text-ink"
										>{level.sold} / {level.capacity} sold ({level.percentage}%)</span
									>
								</div>
								<div class="h-1.5 w-full rounded-full bg-canvas-soft-2">
									<div
										class="h-full rounded-full {level.color} transition-all duration-500"
										style="width: {level.percentage}%"
									></div>
								</div>
							</div>
						{/each}
					</div>
				</div>

				<!-- Sales Channel -->
				<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
					<h2 class="font-sans text-sm font-semibold text-ink">Sales Channel Distribution</h2>
					<p class="mb-5 text-xs text-mute">Where your ticket buyers discovered and checked out.</p>

					<div class="space-y-3.5">
						<div
							class="flex items-center justify-between rounded-md border border-hairline bg-canvas-soft p-3"
						>
							<span class="text-xs font-medium text-body">Direct Web Portal</span>
							<span class="font-mono text-xs font-bold text-ink"
								>76% <span class="text-[10px] font-normal text-mute">sales</span></span
							>
						</div>
						<div
							class="flex items-center justify-between rounded-md border border-hairline bg-canvas-soft p-3"
						>
							<span class="text-xs font-medium text-body">Organizer Referral Links</span>
							<span class="font-mono text-xs font-bold text-ink"
								>14% <span class="text-[10px] font-normal text-mute">sales</span></span
							>
						</div>
						<div
							class="flex items-center justify-between rounded-md border border-hairline bg-canvas-soft p-3"
						>
							<span class="text-xs font-medium text-body">Secondary Resale Market</span>
							<span class="font-mono text-xs font-bold text-ink"
								>10% <span class="text-[10px] font-normal text-mute">sales</span></span
							>
						</div>
					</div>
				</div>
			</div>
		</div>
	{:else if activeTab === 'sales-report'}
		<!-- Sales report / Settlements -->
		<div class="space-y-6">
			<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
				<h2 class="font-sans text-sm font-semibold text-ink">Event Settlements Summary</h2>
				<p class="mb-5 text-xs text-mute">
					Approved settlement schedules and net earnings breakdown.
				</p>

				<div class="mb-6 grid grid-cols-1 gap-4 md:grid-cols-3">
					<div class="rounded-lg border border-hairline bg-canvas-soft p-4">
						<p class="font-mono text-[9px] font-bold tracking-wider text-mute uppercase">
							Settled Gross
						</p>
						<p class="mt-1 font-mono text-lg font-bold text-ink">{formatCurrency(450000000)}</p>
					</div>
					<div class="rounded-lg border border-hairline bg-canvas-soft p-4">
						<p class="font-mono text-[9px] font-bold tracking-wider text-mute uppercase">
							Platform Fees (10%)
						</p>
						<p class="mt-1 font-mono text-lg font-bold text-error">{formatCurrency(45000000)}</p>
					</div>
					<div class="rounded-lg border border-hairline bg-canvas-soft p-4">
						<p class="font-mono text-[9px] font-bold tracking-wider text-mute uppercase">
							Net Organizer Payout
						</p>
						<p class="mt-1 font-mono text-lg font-bold text-success">{formatCurrency(405000000)}</p>
					</div>
				</div>

				<div class="overflow-x-auto">
					<table class="w-full text-left text-xs text-body">
						<thead>
							<tr class="border-b border-hairline text-mute">
								<th class="py-2.5 font-mono font-semibold tracking-wider uppercase">Event Date</th>
								<th class="px-4 py-2.5 font-mono font-semibold tracking-wider uppercase"
									>Gross Ticket Sales</th
								>
								<th class="px-4 py-2.5 text-right font-mono font-semibold tracking-wider uppercase"
									>Commissions</th
								>
								<th class="py-2.5 text-right font-mono font-semibold tracking-wider uppercase"
									>Net Settlement</th
								>
							</tr>
						</thead>
						<tbody class="divide-y divide-hairline">
							<tr class="transition hover:bg-canvas-soft/40">
								<td class="py-3 font-mono font-medium text-ink">2026-05-28</td>
								<td class="px-4 py-3 font-mono">{formatCurrency(250000000)}</td>
								<td class="px-4 py-3 text-right font-mono text-error">{formatCurrency(2500000)}</td>
								<td class="py-3 text-right font-mono font-bold text-success"
									>{formatCurrency(225000000)}</td
								>
							</tr>
							<tr class="transition hover:bg-canvas-soft/40">
								<td class="py-3 font-mono font-medium text-ink">2026-05-27</td>
								<td class="px-4 py-3 font-mono">{formatCurrency(200000000)}</td>
								<td class="px-4 py-3 text-right font-mono text-error">{formatCurrency(2000000)}</td>
								<td class="py-3 text-right font-mono font-bold text-success"
									>{formatCurrency(180000000)}</td
								>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- Payout details -->
			<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
				<h2 class="font-sans text-sm font-semibold text-ink">Organizer Payout Status</h2>
				<p class="mb-5 text-xs text-mute">
					Track standard bank payouts and wire settlement progressions.
				</p>

				<div class="space-y-5 rounded-lg border border-hairline bg-canvas-soft p-5">
					<div class="flex items-center justify-between border-b border-hairline pb-4">
						<div>
							<h3 class="text-xs font-semibold text-ink">BIDV Joint Stock Bank Transfer</h3>
							<p class="font-mono text-[10px] text-mute">
								Account: 3101000******495 • Live Nation Vietnam
							</p>
						</div>
						<div class="text-right">
							<p class="font-mono text-sm font-bold text-ink">{formatCurrency(405000000)}</p>
							<span
								class="mt-1 inline-flex items-center rounded bg-warning/10 px-2 py-0.5 font-mono text-[9px] font-bold text-warning uppercase"
							>
								Processing
							</span>
						</div>
					</div>

					<div class="space-y-4">
						<div class="flex gap-3">
							<span
								class="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-success text-[10px] font-bold text-on-primary"
								>✓</span
							>
							<div>
								<h4 class="text-xs font-semibold text-ink">Payout Initiated</h4>
								<p class="font-mono text-[9px] text-mute">
									2026-05-28 14:00 • Standard scheduler reconciliation sweep
								</p>
							</div>
						</div>
						<div class="flex gap-3">
							<span
								class="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-success text-[10px] font-bold text-on-primary"
								>✓</span
							>
							<div>
								<h4 class="text-xs font-semibold text-ink">Approved by Platform Admin</h4>
								<p class="font-mono text-[9px] text-mute">
									2026-05-28 16:30 • Compliance check passed
								</p>
							</div>
						</div>
						<div class="flex gap-3">
							<span
								class="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-primary/10 text-[10px] font-bold text-primary"
								>!</span
							>
							<div>
								<h4 class="text-xs font-semibold text-ink">Bank Transfer Pending</h4>
								<p class="font-mono text-[9px] text-mute">
									Processing via Interbank gateway • Expected completion within 24 hours
								</p>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	{:else if activeTab === 'inventory-status'}
		<!-- Inventory status / Audit logs -->
		<div class="space-y-6">
			<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
				<h2 class="font-sans text-sm font-semibold text-ink">System Financial Audit Logs</h2>
				<p class="mb-5 text-xs text-mute">
					Chronological history of commercial edits, tier modifications, and payout requests.
				</p>

				<div class="space-y-3">
					<div
						class="flex items-start gap-3 rounded-lg border border-hairline bg-canvas-soft/40 p-4"
					>
						<span
							class="mt-0.5 rounded bg-primary/10 px-2 py-0.5 font-mono text-[9px] font-bold text-primary uppercase"
							>INFO</span
						>
						<div>
							<h4 class="text-xs font-semibold text-ink">Event status changed to ONSALE</h4>
							<p class="mt-0.5 font-mono text-[9px] text-mute">
								By system scheduler • 2026-05-27 10:00:02
							</p>
						</div>
					</div>

					<div
						class="flex items-start gap-3 rounded-lg border border-hairline bg-canvas-soft/40 p-4"
					>
						<span
							class="mt-0.5 rounded bg-success/10 px-2 py-0.5 font-mono text-[9px] font-bold text-success uppercase"
							>UPDATE</span
						>
						<div>
							<h4 class="text-xs font-semibold text-ink">
								Offer "Vé GA Standard" pricing modified
							</h4>
							<p class="mt-0.5 font-mono text-[9px] text-mute">
								By jamie.dao@livenation.com • New Value: 500,000 VND • 2026-05-26 15:42:19
							</p>
						</div>
					</div>

					<div
						class="flex items-start gap-3 rounded-lg border border-hairline bg-canvas-soft/40 p-4"
					>
						<span
							class="mt-0.5 rounded bg-warning/10 px-2 py-0.5 font-mono text-[9px] font-bold text-warning uppercase"
							>ACTION</span
						>
						<div>
							<h4 class="text-xs font-semibold text-ink">
								Payout method linked to BIDV Bank Account
							</h4>
							<p class="mt-0.5 font-mono text-[9px] text-mute">
								By organizer@ticketpeak.com • Live Nation Vietnam Joint Stock • 2026-05-25 09:12:00
							</p>
						</div>
					</div>
				</div>
			</div>
		</div>
	{/if}
</div>
