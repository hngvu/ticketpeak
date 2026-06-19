<script lang="ts">
	import { enhance } from '$app/forms';

	let { data, form } = $props<{ data: any; form: any }>();

	let showPostponeModal = $state(false);
	let selectedEventId = $state('');
	let postponeReason = $state('');

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

	function triggerPostpone(id: string) {
		selectedEventId = id;
		postponeReason = '';
		showPostponeModal = true;
	}
</script>

<svelte:head>
	<title>Global Events — Platform Operations</title>
</svelte:head>

<div class="flex flex-1 flex-col space-y-6 p-6">
	<div class="flex flex-col gap-1 border-b border-hairline pb-5">
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">Event Moderation</h1>
		<p class="font-sans text-xs text-body">
			Review, approve, postpone, or cancel event schedules across the ticketing network.
		</p>
	</div>

	{#if form?.error}
		<div class="rounded-md border border-error bg-error/10 p-3.5 text-xs font-semibold text-error">
			⚠️ Error: {form.error}
		</div>
	{/if}
	{#if form?.success}
		<div class="rounded-md border border-success bg-success/10 p-3.5 text-xs font-semibold text-success">
			✨ Success: {form.message}
		</div>
	{/if}

	{#if data.events && data.events.length > 0}
		<div class="rounded-lg border border-hairline bg-canvas shadow-xs">
			<div class="overflow-x-auto">
				<table class="w-full text-left text-xs text-body">
					<tbody class="divide-y divide-hairline">
						{#each data.events as event (event.id)}
							<tr class="hover:bg-canvas-soft/40 transition">
								<!-- Column 1: Event Details & Inline Badge -->
								<td class="py-4 pl-6 pr-4">
									<div class="flex items-center gap-2">
										<h4 class="text-sm font-semibold text-ink">{event.title}</h4>
										{#if event.status !== 'ON_SALE'}
											<span class="rounded bg-canvas-soft-2 px-2 py-0.5 font-mono text-[9px] font-bold text-body uppercase border border-hairline">
												{event.status}
											</span>
										{/if}
									</div>
									<p class="font-mono mt-1 text-[10px] text-mute">
										📅 {formatDateTime(event.startAt)} • ID: {event.id}
									</p>
								</td>

								<!-- Column 2: Venue & Organization Info -->
								<td class="px-4 py-4">
									<p class="font-sans text-xs font-medium text-ink">Venue Manager / Partner ID</p>
									<p class="font-mono mt-0.5 text-[10px] text-mute">Org ID: {event.organizationId}</p>
								</td>

								<!-- Column 3: Quick Moderation Action buttons -->
								<td class="py-4 pr-6 text-right">
									<div class="inline-flex items-center gap-2">
										{#if event.status === 'DRAFT' || event.status === 'PENDING_APPROVAL'}
											<form method="POST" action="?/publishEvent" use:enhance>
												<input type="hidden" name="id" value={event.id} />
												<button
													type="submit"
													class="cursor-pointer rounded-full bg-primary px-3 py-1 font-mono text-[10px] font-bold text-on-primary hover:bg-primary/95 transition"
												>
													PUBLISH
												</button>
											</form>
										{/if}

										{#if event.status === 'PUBLISHED'}
											<form method="POST" action="?/startSales" use:enhance>
												<input type="hidden" name="id" value={event.id} />
												<button
													type="submit"
													class="cursor-pointer rounded-full bg-primary px-3 py-1 font-mono text-[10px] font-bold text-on-primary hover:bg-primary/95 transition"
												>
													OPEN SALES
												</button>
											</form>
										{/if}

										{#if event.status === 'ON_SALE' || event.status === 'POSTPONED'}
											<button
												onclick={() => triggerPostpone(event.id)}
												class="cursor-pointer rounded-full border border-hairline bg-canvas px-3 py-1 font-mono text-[10px] font-bold text-ink hover:bg-canvas-soft-2 transition"
											>
												POSTPONE
											</button>

											<form method="POST" action="?/cancelEvent" use:enhance>
												<input type="hidden" name="id" value={event.id} />
												<button
													type="submit"
													class="cursor-pointer rounded-full border border-error/25 bg-error/5 px-3 py-1 font-mono text-[10px] font-bold text-error hover:bg-error/10 transition"
												>
													CANCEL
												</button>
											</form>
										{/if}
									</div>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{:else}
		<div class="flex h-40 flex-col items-center justify-center rounded-lg border border-dashed border-hairline bg-canvas text-center">
			<span class="text-xs font-semibold text-mute">No events currently submitted for moderation.</span>
		</div>
	{/if}
</div>

<!-- Postpone Modal Overlay -->
{#if showPostponeModal}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-ink/40 backdrop-blur-xs" role="dialog">
		<div class="w-full max-w-sm rounded-lg border border-hairline bg-canvas p-6 shadow-xl">
			<h3 class="font-sans text-sm font-semibold text-ink">Postpone Event</h3>
			<p class="mt-1 text-xs text-mute">State a reschedule notice reason for active ticket buyers.</p>

			<form method="POST" action="?/postponeEvent" use:enhance={() => {
				showPostponeModal = false;
			}} class="mt-4 space-y-4">
				<input type="hidden" name="id" value={selectedEventId} />
				<div>
					<label for="postpone-reason" class="mb-1 block font-mono text-[10px] font-bold uppercase tracking-wider text-mute">Reason *</label>
					<input
						type="text"
						id="postpone-reason"
						name="reason"
						required
						bind:value={postponeReason}
						placeholder="e.g. Rescheduled due to typhoons"
						class="w-full rounded-md border border-hairline bg-canvas p-2 text-xs text-ink focus:border-primary focus:outline-none"
					/>
				</div>

				<div class="flex justify-between gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showPostponeModal = false)}
						class="cursor-pointer rounded-full border border-hairline bg-canvas px-4 py-1.5 text-xs font-semibold text-ink hover:bg-canvas-soft-2 transition"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="cursor-pointer rounded-full bg-primary px-4 py-1.5 text-xs font-semibold text-on-primary hover:bg-primary/95 transition"
					>
						Postpone
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
