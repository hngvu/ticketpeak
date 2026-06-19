<script lang="ts">
	import { page } from '$app/state';

	// Derived active tab from query params, defaulting to 'campaigns'
	const activeTab = $derived(page.url.searchParams.get('tab') || 'campaigns');

	// Local states for promo codes simulation
	let promoCodes = $state([
		{
			code: 'EARLY20',
			discount: '20% OFF',
			scope: 'VIP Standing, GA Standing',
			usage: '24 / 50',
			status: 'ACTIVE'
		},
		{
			code: 'DENVFAN',
			discount: '100,000 VND',
			scope: 'All Tickets',
			usage: '45 / 100',
			status: 'ACTIVE'
		},
		{
			code: 'COMPVVIP',
			discount: '100% OFF (Complimentary)',
			scope: 'VVIP Zone only',
			usage: '5 / 5',
			status: 'COMPLETED'
		}
	]);

	let newCode = $state('');
	let newDiscountType = $state('Percentage Off (%)');
	let newDiscountValue = $state(0);

	function createPromoCode() {
		if (!newCode.trim() || newDiscountValue <= 0) return;
		const desc = newDiscountType.includes('%')
			? `${newDiscountValue}% OFF`
			: `${newDiscountValue.toLocaleString('vi-VN')} VND`;
		promoCodes = [
			{
				code: newCode.toUpperCase(),
				discount: desc,
				scope: 'All Tickets',
				usage: '0 / 100',
				status: 'ACTIVE'
			},
			...promoCodes
		];
		newCode = '';
		newDiscountValue = 0;
	}
</script>

<svelte:head>
	<title>Marketing & Promotions — Ticketpeak for Business</title>
</svelte:head>

<div class="flex flex-1 flex-col space-y-6 p-6">
	<!-- Title block -->
	<div class="flex flex-col gap-1">
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">Marketing & Promotions</h1>
		<p class="font-sans text-xs text-body">
			Manage discount campaigns, fan CRM lists, early bird schedules, and tracking pixel tags.
		</p>
	</div>

	{#if activeTab === 'campaigns'}
		<!-- Campaigns Tab -->
		<div class="space-y-6">
			<!-- Broadcast campaigns -->
			<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
				<div class="mb-5 flex items-center justify-between">
					<div>
						<h2 class="font-sans text-sm font-semibold text-ink">Active Fan Broadcast Campaigns</h2>
						<p class="text-xs text-mute">
							Manage early bird notifications, SMS/Email broadcasts, and newsletter schedules.
						</p>
					</div>
					<button
						class="cursor-pointer rounded-full bg-primary px-4 py-1.5 text-xs font-semibold text-on-primary transition hover:bg-primary/90"
					>
						Create Campaign
					</button>
				</div>

				<div class="space-y-3">
					<div
						class="flex items-center justify-between rounded-md border border-hairline bg-canvas-soft/40 p-4"
					>
						<div>
							<h3 class="text-xs font-semibold text-ink">Sơn Tùng M-TP Presale Alert</h3>
							<p class="font-mono text-[9px] text-mute">
								Sent to: 24,500 active subscribers • Email & Push Notification
							</p>
						</div>
						<div class="text-right">
							<span
								class="inline-flex items-center rounded bg-success/10 px-2 py-0.5 font-mono text-[9px] font-bold text-success uppercase"
							>
								Delivered
							</span>
							<p class="mt-1 font-mono text-[9px] text-mute">Open rate: 64.2% • CTR: 15.4%</p>
						</div>
					</div>

					<div
						class="flex items-center justify-between rounded-md border border-hairline bg-canvas-soft/40 p-4"
					>
						<div>
							<h3 class="text-xs font-semibold text-ink">Đen Vâu Last Chance Ticket Reminder</h3>
							<p class="font-mono text-[9px] text-mute">
								Targets: Cart abandoners & High-intent listing watchers
							</p>
						</div>
						<div class="text-right">
							<span
								class="inline-flex items-center rounded bg-warning/10 px-2 py-0.5 font-mono text-[9px] font-bold text-warning uppercase"
							>
								Scheduled
							</span>
							<p class="mt-1 font-mono text-[9px] text-mute">Target dispatch: 2026-06-01 10:00</p>
						</div>
					</div>
				</div>
			</div>

			<!-- Promo Codes Section -->
			<div class="grid grid-cols-1 gap-6 lg:grid-cols-[1.8fr_1.2fr]">
				<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
					<h2 class="mb-4 font-sans text-sm font-semibold text-ink">Active Promo Codes</h2>
					<div class="overflow-x-auto">
						<table class="w-full text-left text-xs text-body">
							<thead>
								<tr class="border-b border-hairline text-mute">
									<th class="py-2.5 font-mono font-semibold tracking-wider uppercase"
										>Code String</th
									>
									<th class="px-4 py-2.5 font-mono font-semibold tracking-wider uppercase"
										>Discount Details</th
									>
									<th class="px-4 py-2.5 font-mono font-semibold tracking-wider uppercase"
										>Ticket Scope</th
									>
									<th
										class="px-4 py-2.5 text-center font-mono font-semibold tracking-wider uppercase"
										>Allocated Usage</th
									>
									<th class="py-2.5 text-right font-mono font-semibold tracking-wider uppercase"
										>Status</th
									>
								</tr>
							</thead>
							<tbody class="divide-y divide-hairline">
								{#each promoCodes as promo (promo.code)}
									<tr class="transition hover:bg-canvas-soft/40">
										<td class="py-3.5 font-mono font-bold text-ink">{promo.code}</td>
										<td class="px-4 py-3.5 font-semibold text-ink">{promo.discount}</td>
										<td class="px-4 py-3.5 text-mute">{promo.scope}</td>
										<td class="px-4 py-3.5 text-center font-mono text-ink">{promo.usage}</td>
										<td class="py-3.5 text-right">
											{#if promo.status === 'ACTIVE'}
												<span
													class="inline-flex items-center rounded bg-success/10 px-2 py-0.5 font-mono text-[9px] font-bold text-success uppercase"
												>
													Active
												</span>
											{:else}
												<span
													class="inline-flex items-center rounded bg-canvas-soft-2 px-2 py-0.5 font-mono text-[9px] font-bold text-mute uppercase"
												>
													Completed
												</span>
											{/if}
										</td>
									</tr>
								{/each}
							</tbody>
						</table>
					</div>
				</div>

				<div class="h-fit rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
					<h2 class="mb-4 font-sans text-sm font-semibold text-ink">Create Promo Code</h2>
					<div class="space-y-4">
						<div>
							<label
								for="promo-code"
								class="mb-1.5 block font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
								>Promo Code String *</label
							>
							<input
								type="text"
								id="promo-code"
								bind:value={newCode}
								placeholder="e.g. EARLYBIRD15"
								class="w-full rounded-md border border-hairline bg-canvas p-2 text-xs text-ink focus:border-primary focus:outline-none"
							/>
						</div>
						<div>
							<label
								for="promo-type"
								class="mb-1.5 block font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
								>Discount Type *</label
							>
							<select
								id="promo-type"
								bind:value={newDiscountType}
								class="w-full rounded-md border border-hairline bg-canvas p-2 text-xs text-ink focus:border-primary focus:outline-none"
							>
								<option>Percentage Off (%)</option>
								<option>Fixed Amount Off (VND)</option>
							</select>
						</div>
						<div>
							<label
								for="promo-value"
								class="mb-1.5 block font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
								>Discount Value *</label
							>
							<input
								type="number"
								id="promo-value"
								bind:value={newDiscountValue}
								placeholder="e.g. 15"
								class="w-full rounded-md border border-hairline bg-canvas p-2 text-xs text-ink focus:border-primary focus:outline-none"
							/>
						</div>
						<button
							type="button"
							onclick={createPromoCode}
							class="w-full cursor-pointer rounded-full bg-primary py-2 text-xs font-semibold text-on-primary transition hover:bg-primary/90"
						>
							Save Promotion
						</button>
					</div>
				</div>
			</div>
		</div>
	{:else if activeTab === 'segments'}
		<!-- Audience Segments Tab -->
		<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
			<h2 class="font-sans text-sm font-semibold text-ink">Fan Audience Database</h2>
			<p class="mb-5 text-xs text-mute">
				Total list of buyers, lifetime ticket metrics, and booking loyalty tier.
			</p>

			<div class="overflow-x-auto">
				<table class="w-full text-left text-xs text-body">
					<thead>
						<tr class="border-b border-hairline text-mute">
							<th class="py-2.5 font-mono font-semibold tracking-wider uppercase">Fan Name</th>
							<th class="px-4 py-2.5 font-mono font-semibold tracking-wider uppercase"
								>Purchased Tickets</th
							>
							<th class="px-4 py-2.5 text-right font-mono font-semibold tracking-wider uppercase"
								>Lifetime Spent</th
							>
							<th class="py-2.5 text-right font-mono font-semibold tracking-wider uppercase"
								>Loyalty Tier</th
							>
						</tr>
					</thead>
					<tbody class="divide-y divide-hairline">
						<tr class="transition hover:bg-canvas-soft/40">
							<td class="flex items-center gap-2.5 py-3">
								<div
									class="flex h-7 w-7 items-center justify-center rounded-full bg-primary/10 text-[10px] font-bold text-primary"
								>
									NL
								</div>
								<div>
									<h4 class="text-xs font-semibold text-ink">Nguyễn Hoàng Long</h4>
									<p class="font-mono text-[9px] text-mute">long.nh@gmail.com</p>
								</div>
							</td>
							<td class="px-4 py-3 font-medium text-ink">12 tickets</td>
							<td class="px-4 py-3 text-right font-mono font-semibold text-ink">18.000.000 VND</td>
							<td class="py-3 text-right">
								<span
									class="inline-flex items-center rounded bg-purple-500/10 px-2 py-0.5 font-mono text-[9px] font-bold text-purple-600 uppercase"
								>
									VIP Platinum
								</span>
							</td>
						</tr>

						<tr class="transition hover:bg-canvas-soft/40">
							<td class="flex items-center gap-2.5 py-3">
								<div
									class="flex h-7 w-7 items-center justify-center rounded-full bg-primary/10 text-[10px] font-bold text-primary"
								>
									MH
								</div>
								<div>
									<h4 class="text-xs font-semibold text-ink">Phạm Minh Hưng</h4>
									<p class="font-mono text-[9px] text-mute">hung.pm@yahoo.com</p>
								</div>
							</td>
							<td class="px-4 py-3 font-medium text-ink">6 tickets</td>
							<td class="px-4 py-3 text-right font-mono font-semibold text-ink">9.000.000 VND</td>
							<td class="py-3 text-right">
								<span
									class="inline-flex items-center rounded bg-primary/10 px-2 py-0.5 font-mono text-[9px] font-bold text-primary uppercase"
								>
									Regular Gold
								</span>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	{/if}
</div>
