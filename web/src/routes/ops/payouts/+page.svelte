<script lang="ts">
	import { enhance } from '$app/forms';

	let { data, form } = $props<{ data: any; form: any }>();

	let showUpdateModal = $state(false);
	let selectedPayoutId = $state('');
	let updateStatus = $state('PROCESSING');
	let externalRef = $state('');
	let note = $state('');

	function formatCurrency(amount: number) {
		return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
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

	function triggerUpdateStatus(id: string, currentStatus: string) {
		selectedPayoutId = id;
		updateStatus = currentStatus;
		externalRef = '';
		note = '';
		showUpdateModal = true;
	}
</script>

<svelte:head>
	<title>Organizer Payouts — Platform Operations</title>
</svelte:head>

<div class="flex flex-1 flex-col space-y-6 p-6">
	<div class="flex flex-col gap-1 border-b border-hairline pb-5">
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">Organizer Payout Queue</h1>
		<p class="font-sans text-xs text-body">
			Review gross sales, authorize bank wire clearances, and update secondary market payouts.
		</p>
	</div>

	{#if form?.error}
		<div class="rounded-md border border-error bg-error/10 p-3.5 text-xs font-semibold text-error">
			⚠️ Error: {form.error}
		</div>
	{/if}
	{#if form?.success}
		<div
			class="rounded-md border border-success bg-success/10 p-3.5 text-xs font-semibold text-success"
		>
			✨ Success: {form.message}
		</div>
	{/if}

	{#if data.payouts && data.payouts.length > 0}
		<div class="rounded-lg border border-hairline bg-canvas shadow-xs">
			<div class="overflow-x-auto">
				<table class="w-full text-left text-xs text-body">
					<tbody class="divide-y divide-hairline">
						{#each data.payouts as payout (payout.id)}
							<tr class="transition hover:bg-canvas-soft/40">
								<!-- Column 1: Account Holder & ID -->
								<td class="py-4 pr-4 pl-6">
									<div class="flex items-center gap-2">
										<h4 class="text-sm font-semibold text-ink">
											{payout.payoutMethodSnapshot?.holderName || 'Seller Account'}
										</h4>
										{#if payout.status !== 'PAID'}
											<span
												class="rounded border border-hairline bg-canvas-soft-2 px-2 py-0.5 font-mono text-[9px] font-bold text-body uppercase"
											>
												{payout.status}
											</span>
										{/if}
									</div>
									<p class="mt-1 font-mono text-[10px] text-mute">
										🏦 {payout.payoutMethodSnapshot?.bankCode} • A/C: {payout.payoutMethodSnapshot
											?.maskedAccountNumber}
									</p>
								</td>

								<!-- Column 2: Amounts Details -->
								<td class="px-4 py-4">
									<div class="flex flex-col">
										<span class="font-sans text-xs font-semibold text-ink">
											Net: {formatCurrency(payout.netAmount)}
										</span>
										<span class="mt-0.5 font-mono text-[10px] text-mute">
											Gross: {formatCurrency(payout.grossAmount)} (Fee: {payout.platformFeePercent}%)
										</span>
									</div>
								</td>

								<!-- Column 3: Scheduled date & ID info -->
								<td class="px-4 py-4">
									<p class="font-sans text-xs font-medium text-ink">
										Scheduled: {formatDateTime(payout.scheduledAfter)}
									</p>
									{#if payout.processedAt}
										<p class="mt-0.5 font-mono text-[10px] text-mute">
											Processed: {formatDateTime(payout.processedAt)}
										</p>
									{/if}
								</td>

								<!-- Column 4: Action Buttons -->
								<td class="py-4 pr-6 text-right">
									<button
										type="button"
										onclick={() => triggerUpdateStatus(payout.id, payout.status)}
										class="cursor-pointer rounded-full bg-primary px-3 py-1 font-mono text-[10px] font-bold text-on-primary transition hover:bg-primary/95"
									>
										UPDATE STATUS
									</button>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{:else}
		<div
			class="flex h-40 flex-col items-center justify-center rounded-lg border border-dashed border-hairline bg-canvas text-center"
		>
			<span class="text-xs font-semibold text-mute">No payouts currently recorded.</span>
		</div>
	{/if}
</div>

<!-- Update Status Modal -->
{#if showUpdateModal}
	<div
		class="fixed inset-0 z-50 flex items-center justify-center bg-ink/40 backdrop-blur-xs"
		role="dialog"
	>
		<div class="w-full max-w-sm rounded-lg border border-hairline bg-canvas p-6 shadow-xl">
			<h3 class="font-sans text-sm font-semibold text-ink">Update Payout Status</h3>
			<p class="mt-1 text-xs text-mute">
				Process organizer settlement state and reference details.
			</p>

			<form
				method="POST"
				action="?/updatePayoutStatus"
				use:enhance={() => {
					showUpdateModal = false;
				}}
				class="mt-4 space-y-4"
			>
				<input type="hidden" name="id" value={selectedPayoutId} />

				<div>
					<label
						for="status"
						class="mb-1 block font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
						>Status *</label
					>
					<select
						id="status"
						name="status"
						bind:value={updateStatus}
						required
						class="w-full rounded-md border border-hairline bg-canvas p-2 text-xs text-ink focus:border-primary focus:outline-none"
					>
						<option value="PENDING">PENDING</option>
						<option value="PROCESSING">PROCESSING</option>
						<option value="PAID">PAID</option>
						<option value="FAILED">FAILED</option>
					</select>
				</div>

				<div>
					<label
						for="external-ref"
						class="mb-1 block font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
						>External Reference</label
					>
					<input
						type="text"
						id="external-ref"
						name="externalRef"
						bind:value={externalRef}
						placeholder="e.g. FT2617092102"
						class="w-full rounded-md border border-hairline bg-canvas p-2 text-xs text-ink focus:border-primary focus:outline-none"
					/>
				</div>

				<div>
					<label
						for="note"
						class="mb-1 block font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
						>Note</label
					>
					<input
						type="text"
						id="note"
						name="note"
						bind:value={note}
						placeholder="e.g. Transfer cleared successfully"
						class="w-full rounded-md border border-hairline bg-canvas p-2 text-xs text-ink focus:border-primary focus:outline-none"
					/>
				</div>

				<div class="flex justify-between gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showUpdateModal = false)}
						class="cursor-pointer rounded-full border border-hairline bg-canvas px-4 py-1.5 text-xs font-semibold text-ink transition hover:bg-canvas-soft-2"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="cursor-pointer rounded-full bg-primary px-4 py-1.5 text-xs font-semibold text-on-primary transition hover:bg-primary/95"
					>
						Update
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
