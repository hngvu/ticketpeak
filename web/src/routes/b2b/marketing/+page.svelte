<script lang="ts">
	import { page } from '$app/state';

	// Derived active tab from query params
	const activeTab = $derived(page.url.searchParams.get('tab') || 'affiliates');

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

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-8 p-6">
	<!-- Title block -->
	<div>
		<h1 class="text-2xl font-extrabold text-slate-900 md:text-3xl dark:text-white">
			Marketing & Promotions
		</h1>
		<p class="text-sm font-medium text-slate-500">
			Manage discount campaigns, fan CRM lists, early bird schedules, and tracking pixel tags.
		</p>
	</div>

	<!-- Tab content logic -->
	{#if activeTab === 'crm'}
		<!-- Fan database (CRM) -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
			<h2 class="text-base font-bold text-slate-900">Fan Audience Database</h2>
			<p class="mb-6 text-xs text-slate-400">
				Total list of buyers, lifetime ticket metrics, and booking loyalty tier.
			</p>

			<div class="overflow-x-auto">
				<table class="w-full text-left text-sm text-slate-500">
					<thead>
						<tr
							class="border-b border-slate-100 text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
						>
							<th class="py-3 pr-4">Fan Name</th>
							<th class="px-4 py-3">Purchased Tickets</th>
							<th class="px-4 py-3 text-right">Lifetime Spent</th>
							<th class="py-3 pl-4 text-right">Loyalty Tier</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-slate-100">
						<tr class="transition hover:bg-slate-50/50">
							<td class="flex items-center gap-2.5 py-3.5 pr-4">
								<div
									class="flex h-7.5 w-7.5 items-center justify-center rounded-full bg-blue-50 text-xs font-bold text-blue-600"
								>
									NL
								</div>
								<div>
									<h4 class="text-xs font-bold text-slate-900">Nguyễn Hoàng Long</h4>
									<span class="text-[9px] font-medium text-slate-400">long.nh@gmail.com</span>
								</div>
							</td>
							<td class="px-4 py-3.5 text-xs font-bold text-slate-700">12 tickets</td>
							<td class="px-4 py-3.5 text-right text-xs font-extrabold text-slate-900"
								>18,000,000 VND</td
							>
							<td class="py-3.5 pl-4 text-right">
								<span
									class="inline-flex items-center rounded-full bg-purple-50 px-2 py-0.5 text-[9px] font-bold text-purple-700 uppercase"
									>VIP Platinum</span
								>
							</td>
						</tr>

						<tr class="transition hover:bg-slate-50/50">
							<td class="flex items-center gap-2.5 py-3.5 pr-4">
								<div
									class="flex h-7.5 w-7.5 items-center justify-center rounded-full bg-blue-50 text-xs font-bold text-blue-600"
								>
									MH
								</div>
								<div>
									<h4 class="text-xs font-bold text-slate-900">Phạm Minh Hưng</h4>
									<span class="text-[9px] font-medium text-slate-400">hung.pm@yahoo.com</span>
								</div>
							</td>
							<td class="px-4 py-3.5 text-xs font-bold text-slate-700">6 tickets</td>
							<td class="px-4 py-3.5 text-right text-xs font-extrabold text-slate-900"
								>9,000,000 VND</td
							>
							<td class="py-3.5 pl-4 text-right">
								<span
									class="inline-flex items-center rounded-full bg-blue-50 px-2 py-0.5 text-[9px] font-bold text-blue-600 uppercase"
									>Regular Gold</span
								>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	{:else if activeTab === 'campaigns'}
		<!-- Campaign manager -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
			<div class="mb-6 flex items-center justify-between">
				<div>
					<h2 class="text-base font-bold text-slate-900">Active Fan Broadcast Campaigns</h2>
					<p class="text-xs text-slate-400">
						Manage early bird notifications, SMS/Email broadcasts, and newsletter schedules.
					</p>
				</div>
				<button
					class="rounded-full bg-blue-600 px-4 py-2 text-xs font-bold text-white transition hover:bg-blue-700"
				>
					Create Campaign
				</button>
			</div>

			<div class="space-y-4">
				<div
					class="flex items-center justify-between rounded-xl border border-slate-100 bg-slate-50/20 p-4"
				>
					<div>
						<h3 class="text-xs font-bold text-slate-800">Sơn Tùng M-TP Presale Alert</h3>
						<span class="text-[9px] font-semibold text-slate-400"
							>Sent to: 24,500 active subscribers • Email & Push Notification</span
						>
					</div>
					<div class="text-right">
						<span
							class="inline-flex items-center rounded-full bg-green-50 px-2.5 py-0.5 text-[9px] font-bold text-green-700 uppercase"
							>DELIVERED</span
						>
						<p class="mt-1 text-[10px] font-semibold text-slate-400">
							Open rate: 64.2% • CTR: 15.4%
						</p>
					</div>
				</div>

				<div
					class="flex items-center justify-between rounded-xl border border-slate-100 bg-slate-50/20 p-4"
				>
					<div>
						<h3 class="text-xs font-bold text-slate-800">Đen Vâu Last Chance Ticket Reminder</h3>
						<span class="text-[9px] font-semibold text-slate-400"
							>Targets: Cart abandoners & High-intent listing watchers</span
						>
					</div>
					<div class="text-right">
						<span
							class="inline-flex items-center rounded-full bg-amber-50 px-2.5 py-0.5 text-[9px] font-bold text-amber-700 uppercase"
							>SCHEDULED</span
						>
						<p class="mt-1 text-[10px] font-semibold text-slate-400">
							Target dispatch: 2026-06-01 10:00
						</p>
					</div>
				</div>
			</div>
		</div>
	{:else if activeTab === 'pixels'}
		<!-- Tracking pixels -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
			<h2 class="text-base font-bold text-slate-900">Audience Tracking Pixels</h2>
			<p class="mb-6 text-xs text-slate-400">
				Configure Meta Pixel, Google Analytics (GA4), or TikTok advertising tag events.
			</p>

			<div class="divide-y divide-slate-100">
				<div class="flex items-center justify-between py-4">
					<div>
						<h3 class="text-xs font-bold text-slate-800">Meta Pixel Integration</h3>
						<span class="text-[10px] font-semibold text-slate-400"
							>Tracking page views, seat selections, checkout starts, and ticket purchase events.</span
						>
					</div>
					<div class="flex items-center gap-3">
						<span class="rounded bg-slate-100 px-2 py-1 text-xs font-bold text-slate-500"
							>ID: 194850938201</span
						>
						<span class="inline-flex h-2.5 w-2.5 rounded-full bg-green-500"></span>
					</div>
				</div>

				<div class="flex items-center justify-between py-4">
					<div>
						<h3 class="text-xs font-bold text-slate-800">Google Analytics 4 (GA4) Tag</h3>
						<span class="text-[10px] font-semibold text-slate-400"
							>Standard page view metrics, channel attribution, and funnel dropoff rates.</span
						>
					</div>
					<div class="flex items-center gap-3">
						<span class="rounded bg-slate-100 px-2 py-1 text-xs font-bold text-slate-500"
							>ID: G-TP888888</span
						>
						<span class="inline-flex h-2.5 w-2.5 rounded-full bg-green-500"></span>
					</div>
				</div>
			</div>
		</div>
	{:else}
		<!-- Affiliate & Partners (Promo codes list & create) -->
		<div class="grid grid-cols-1 gap-6 lg:grid-cols-[1.8fr_1.2fr]">
			<!-- Codes listing -->
			<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
				<h2 class="mb-4 text-base font-bold text-slate-900">Active Promo Codes</h2>

				<div class="overflow-x-auto">
					<table class="w-full text-left text-sm text-slate-500">
						<thead>
							<tr
								class="border-b border-slate-100 text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
							>
								<th class="py-3 pr-4">Code String</th>
								<th class="px-4 py-3">Discount Details</th>
								<th class="px-4 py-3">Ticket Scope</th>
								<th class="px-4 py-3 text-center">Allocated Usage</th>
								<th class="py-3 pl-4 text-right">Status</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-slate-100">
							{#each promoCodes as promo (promo.code)}
								<tr class="transition-colors hover:bg-slate-50/50">
									<td class="py-4 pr-4 font-mono text-base font-bold text-slate-900"
										>{promo.code}</td
									>
									<td class="px-4 py-4 font-bold text-slate-800">{promo.discount}</td>
									<td class="px-4 py-4 font-semibold text-slate-500">{promo.scope}</td>
									<td class="px-4 py-4 text-center font-bold text-slate-800">{promo.usage}</td>
									<td class="py-4 pl-4 text-right">
										{#if promo.status === 'ACTIVE'}
											<span
												class="inline-flex items-center rounded-full bg-green-50 px-2.5 py-0.5 text-[10px] font-bold text-green-700 uppercase"
												>ACTIVE</span
											>
										{:else}
											<span
												class="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-0.5 text-[10px] font-bold text-slate-500 uppercase"
												>COMPLETED</span
											>
										{/if}
									</td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			</div>

			<!-- Quick Create Form -->
			<div class="h-fit space-y-5 rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
				<h2 class="text-base font-bold text-slate-900">Create Promo Code</h2>
				<div class="space-y-4">
					<div>
						<label for="promo-code" class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
							>Promo Code String *</label
						>
						<input
							type="text"
							id="promo-code"
							bind:value={newCode}
							placeholder="e.g. EARLYBIRD15"
							class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
						/>
					</div>
					<div>
						<label for="promo-type" class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
							>Discount Type *</label
						>
						<select
							id="promo-type"
							bind:value={newDiscountType}
							class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
						>
							<option>Percentage Off (%)</option>
							<option>Fixed Amount Off (VND)</option>
						</select>
					</div>
					<div>
						<label for="promo-value" class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
							>Discount Value *</label
						>
						<input
							type="number"
							id="promo-value"
							bind:value={newDiscountValue}
							placeholder="e.g. 15"
							class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none"
						/>
					</div>
					<button
						type="button"
						onclick={createPromoCode}
						class="w-full cursor-pointer rounded-xl bg-blue-600 py-2.5 text-xs font-bold text-white transition hover:bg-blue-700"
					>
						Save Promotion
					</button>
				</div>
			</div>
		</div>
	{/if}
</div>
