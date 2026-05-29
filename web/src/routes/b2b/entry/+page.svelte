<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	let { data } = $props();

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
			Monitor physical guest check-in rates in real-time and launch gate barcode scanners.
		</p>
	</div>

	<!-- Operations -->
	<div class="rounded-xl border border-hairline bg-canvas bg-white p-6 shadow-xs">
		<h2 class="mb-2 text-lg font-bold text-slate-900">Gate Check-In Simulator Control</h2>
		<p class="mb-6 text-xs text-slate-500">
			Select an active event below to launch its ticket validator and entry gate scanner.
		</p>

		{#if data.events && data.events.length > 0}
			<div class="space-y-6">
				{#each data.events as event (event.id)}
					{@const totalCapacity = 300}
					{@const scannedCount = event.status === 'SALES_ACTIVE' ? 245 : 0}
					{@const scannedPercent = Math.round((scannedCount / totalCapacity) * 100)}

					<div
						class="flex flex-col gap-4 rounded-xl border border-hairline bg-white p-5 transition hover:shadow-md md:flex-row md:items-center md:justify-between"
					>
						<!-- Details -->
						<div class="space-y-1">
							<h3 class="text-base font-bold text-slate-900">{event.title}</h3>
							<div class="text-xs font-semibold text-slate-400">
								📅 {formatDateTime(event.startAt)}
							</div>
							<div class="flex items-center gap-2 text-xs">
								<span
									class="inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-bold text-slate-700 uppercase"
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
								class="inline-flex items-center gap-1.5 rounded-full bg-purple-600 px-5 py-2.5 text-xs font-bold text-white shadow-sm transition hover:bg-purple-700 hover:shadow active:scale-95"
							>
								<svg
									viewBox="0 0 24 24"
									fill="none"
									stroke="currentColor"
									stroke-width="2.5"
									class="h-4 w-4"
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
</div>
