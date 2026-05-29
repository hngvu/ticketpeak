<script lang="ts">
	// Mock promo campaigns
	const promoCodes = [
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
	];
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
			Manage discount campaigns, early bird scheduling, and custom promo codes.
		</p>
	</div>

	<!-- Operations row -->
	<div class="grid grid-cols-1 gap-6 lg:grid-cols-[1.8fr_1.2fr]">
		<!-- Left: Codes listing -->
		<div class="rounded-xl border border-hairline bg-canvas bg-white p-6 shadow-xs">
			<h2 class="mb-4 text-lg font-bold text-slate-900">Active Promo Codes</h2>

			<div class="overflow-x-auto">
				<table class="w-full text-left text-sm text-slate-500">
					<thead>
						<tr
							class="border-b border-hairline text-xs font-bold tracking-wider text-slate-400 uppercase"
						>
							<th class="py-3 pr-4">Code String</th>
							<th class="px-4 py-3">Discount Details</th>
							<th class="px-4 py-3">Ticket Scope</th>
							<th class="px-4 py-3 text-center">Allocated Usage</th>
							<th class="py-3 pl-4 text-right">Status</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-hairline">
						{#each promoCodes as promo (promo.code)}
							<tr class="transition-colors hover:bg-slate-50/50">
								<td class="py-4 pr-4 font-mono text-base font-bold text-slate-900">{promo.code}</td>
								<td class="px-4 py-4 font-bold text-slate-800">{promo.discount}</td>
								<td class="px-4 py-4 font-semibold text-slate-500">{promo.scope}</td>
								<td class="px-4 py-4 text-center font-bold text-slate-800">{promo.usage}</td>
								<td class="py-4 pl-4 text-right">
									{#if promo.status === 'ACTIVE'}
										<span
											class="inline-flex items-center rounded-full bg-green-50 px-2.5 py-0.5 text-xs font-bold text-green-700 uppercase"
											>ACTIVE</span
										>
									{:else}
										<span
											class="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-bold text-slate-500 uppercase"
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

		<!-- Right: Quick Create Form -->
		<div class="h-fit space-y-5 rounded-xl border border-hairline bg-canvas bg-white p-6 shadow-xs">
			<h2 class="text-lg font-bold text-slate-900">Create Promo Code</h2>
			<div class="space-y-4">
				<div>
					<span class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
						>Promo Code String *</span
					>
					<input
						type="text"
						placeholder="e.g. EARLYBIRD15"
						class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-primary focus:outline-none"
					/>
				</div>
				<div>
					<span class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
						>Discount Type *</span
					>
					<select
						class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-primary focus:outline-none"
					>
						<option>Percentage Off (%)</option>
						<option>Fixed Amount Off (VND)</option>
						<option>Free Ticket (100% complimentary)</option>
					</select>
				</div>
				<div>
					<span class="mb-1.5 block text-xs font-bold text-slate-500 uppercase"
						>Discount Value *</span
					>
					<input
						type="number"
						placeholder="e.g. 15"
						class="w-full rounded-lg border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-primary focus:outline-none"
					/>
				</div>
				<button
					type="button"
					class="w-full rounded-lg bg-primary py-2.5 text-sm font-bold text-white transition-colors hover:bg-link-deep focus:outline-none active:scale-98"
				>
					Save Promotion
				</button>
			</div>
		</div>
	</div>
</div>
