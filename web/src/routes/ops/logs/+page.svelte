<script lang="ts">
	let { data } = $props<{ data: any }>();

	let filterCategory = $state('ALL');
	let searchAdmin = $state('');

	const filteredLogs = $derived(
		data.logs.filter((log: any) => {
			const matchesCategory = filterCategory === 'ALL' || log.category === filterCategory;
			const matchesSearch =
				log.adminEmail.toLowerCase().includes(searchAdmin.toLowerCase()) ||
				log.details.toLowerCase().includes(searchAdmin.toLowerCase());
			return matchesCategory && matchesSearch;
		})
	);

	function formatDateTime(isoString: string) {
		if (!isoString) return 'N/A';
		const date = new Date(isoString);
		return date.toLocaleDateString('vi-VN', {
			day: '2-digit',
			month: '2-digit',
			year: 'numeric',
			hour: '2-digit',
			minute: '2-digit',
			second: '2-digit'
		});
	}
</script>

<svelte:head>
	<title>Audit Logs — Platform Operations</title>
</svelte:head>

<div class="flex flex-1 flex-col space-y-6 p-6">
	<div class="flex flex-col gap-1 border-b border-hairline pb-5">
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">System Audit Trail</h1>
		<p class="font-sans text-xs text-body">
			A permanent chronological ledger tracking administrator activities, security events, and configuration overrides.
		</p>
	</div>

	<!-- Filters & Actions bar -->
	<div class="flex flex-wrap items-center justify-between gap-3">
		<div class="flex items-center gap-2">
			<input
				type="text"
				placeholder="Search logs or administrators..."
				bind:value={searchAdmin}
				class="rounded-md border border-hairline bg-canvas px-3 py-1.5 font-sans text-xs text-ink placeholder-body focus:border-body focus:outline-none"
			/>

			<select
				bind:value={filterCategory}
				class="rounded-md border border-hairline bg-canvas px-3 py-1.5 font-sans text-xs text-ink focus:border-body focus:outline-none"
			>
				<option value="ALL">All Categories</option>
				<option value="USER_MANAGEMENT">User Management</option>
				<option value="EVENTS">Events Moderation</option>
				<option value="PAYOUTS">Payouts Clearing</option>
				<option value="FEES">Fees Override</option>
			</select>
		</div>
		<div class="font-mono text-[10px] font-bold text-mute uppercase">
			{filteredLogs.length} entries found
		</div>
	</div>

	{#if filteredLogs.length > 0}
		<div class="rounded-lg border border-hairline bg-canvas shadow-xs">
			<div class="overflow-x-auto">
				<table class="w-full text-left text-xs text-body">
					<tbody class="divide-y divide-hairline">
						{#each filteredLogs as log (log.id)}
							<tr class="hover:bg-canvas-soft/40 transition">
								<!-- Column 1: Time and Category -->
								<td class="py-4 pl-6 pr-4 w-48 shrink-0">
									<div class="flex items-center gap-2">
										<span class="rounded bg-canvas-soft-2 px-2 py-0.5 font-mono text-[9px] font-bold text-body uppercase border border-hairline">
											{log.category}
										</span>
									</div>
									<p class="font-mono mt-1 text-[10px] text-mute">
										{formatDateTime(log.timestamp)}
									</p>
								</td>

								<!-- Column 2: Admin Email -->
								<td class="px-4 py-4 w-64">
									<p class="font-sans text-xs font-semibold text-ink">{log.adminEmail}</p>
									<p class="font-mono mt-0.5 text-[9px] text-mute">Action: {log.action}</p>
								</td>

								<!-- Column 3: Log Details -->
								<td class="py-4 pr-6 pl-4">
									<p class="font-sans text-xs text-ink leading-relaxed">
										{log.details}
									</p>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{:else}
		<div class="flex h-40 flex-col items-center justify-center rounded-lg border border-dashed border-hairline bg-canvas text-center">
			<span class="text-xs font-semibold text-mute">No system logs match the current criteria.</span>
		</div>
	{/if}
</div>
