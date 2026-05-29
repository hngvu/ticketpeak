<script lang="ts">
	// Seating Level distribution reports
	const seatingLevels = [
		{ name: 'VVIP Zone', sold: '76', capacity: '80', percentage: 95, color: 'bg-rose-500' },
		{ name: 'VIP Standing', sold: '112', capacity: '120', percentage: 93, color: 'bg-amber-500' },
		{ name: 'GA Standing', sold: '140', capacity: '200', percentage: 70, color: 'bg-emerald-500' }
	];
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

	<!-- KPIs row -->
	<div class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
		<div class="rounded-xl border border-hairline bg-canvas bg-white p-5 shadow-xs">
			<span class="text-xs font-bold tracking-wider text-slate-400 uppercase"
				>Gross Ticket Sales</span
			>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-extrabold text-slate-900">450.0M</span>
				<span class="text-xs font-semibold text-emerald-600">VND</span>
			</div>
		</div>
		<div class="rounded-xl border border-hairline bg-canvas bg-white p-5 shadow-xs">
			<span class="text-xs font-bold tracking-wider text-slate-400 uppercase"
				>Net Organizer Payout</span
			>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-extrabold text-slate-900">405.0M</span>
				<span class="text-xs font-semibold text-emerald-600">VND</span>
			</div>
			<div class="mt-1 text-[10px] font-semibold text-slate-400">
				10% standard platform commission fee deducted
			</div>
		</div>
		<div class="rounded-xl border border-hairline bg-canvas bg-white p-5 shadow-xs">
			<span class="text-xs font-bold tracking-wider text-slate-400 uppercase"
				>Average Order Value (AOV)</span
			>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-extrabold text-slate-900">2.44M</span>
				<span class="text-xs font-semibold text-emerald-600">VND</span>
			</div>
		</div>
	</div>

	<!-- Seating Levels Occupancy Chart -->
	<div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
		<div class="rounded-xl border border-hairline bg-canvas bg-white p-6 shadow-xs">
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
		<div class="rounded-xl border border-hairline bg-canvas bg-white p-6 shadow-xs">
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
