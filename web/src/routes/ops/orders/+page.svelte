<script lang="ts">
	import { enhance } from '$app/forms';

	let { data, form } = $props<{ data: any; form: any }>();

	let filterStatus = $state('ALL');
	let searchQuery = $state('');

	const filteredOrders = $derived(
		data.orders.filter((order: any) => {
			const matchesStatus = filterStatus === 'ALL' || order.status === filterStatus;
			const matchesSearch =
				order.customerName.toLowerCase().includes(searchQuery.toLowerCase()) ||
				order.customerEmail.toLowerCase().includes(searchQuery.toLowerCase()) ||
				order.eventName.toLowerCase().includes(searchQuery.toLowerCase()) ||
				order.id.toLowerCase().includes(searchQuery.toLowerCase());
			return matchesStatus && matchesSearch;
		})
	);

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
</script>

<svelte:head>
	<title>Global Orders — Platform Operations</title>
</svelte:head>

<div class="flex flex-1 flex-col space-y-6 p-6">
	<div class="flex flex-col gap-1 border-b border-hairline pb-5">
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">Order Ledger</h1>
		<p class="font-sans text-xs text-body">
			Track transaction processing states, execute operational refunds, or void orders system-wide.
		</p>
	</div>

	{#if form?.success}
		<div
			class="rounded-md border border-success bg-success/10 p-3.5 text-xs font-semibold text-success"
		>
			✨ Success: {form.message}
		</div>
	{/if}

	<!-- Filters & Actions bar -->
	<div class="flex flex-wrap items-center justify-between gap-3">
		<div class="flex items-center gap-2">
			<input
				type="text"
				placeholder="Filter by customer, event, or ID..."
				bind:value={searchQuery}
				class="rounded-md border border-hairline bg-canvas px-3 py-1.5 font-sans text-xs text-ink placeholder-body focus:border-body focus:outline-none"
			/>

			<select
				bind:value={filterStatus}
				class="rounded-md border border-hairline bg-canvas px-3 py-1.5 font-sans text-xs text-ink focus:border-body focus:outline-none"
			>
				<option value="ALL">All Statuses</option>
				<option value="PAID">Paid</option>
				<option value="PENDING">Pending</option>
				<option value="REFUNDED">Refunded</option>
				<option value="CANCELLED">Cancelled</option>
			</select>
		</div>
		<div class="font-mono text-[10px] font-bold text-mute uppercase">
			Showing {filteredOrders.length} of {data.orders.length} orders
		</div>
	</div>

	{#if filteredOrders.length > 0}
		<div class="rounded-lg border border-hairline bg-canvas shadow-xs">
			<div class="overflow-x-auto">
				<table class="w-full text-left text-xs text-body">
					<tbody class="divide-y divide-hairline">
						{#each filteredOrders as order (order.id)}
							<tr class="transition hover:bg-canvas-soft/40">
								<!-- Column 1: Order ID & Customer Info -->
								<td class="py-4 pr-4 pl-6">
									<div class="flex items-center gap-2">
										<h4 class="text-sm font-semibold text-ink">{order.customerName}</h4>
										{#if order.status !== 'PAID'}
											<span
												class="rounded border border-hairline bg-canvas-soft-2 px-2 py-0.5 font-mono text-[9px] font-bold text-body uppercase"
											>
												{order.status}
											</span>
										{/if}
									</div>
									<p class="mt-1 font-mono text-[10px] text-mute">
										✉️ {order.customerEmail} • ID: {order.id}
									</p>
								</td>

								<!-- Column 2: Event Name & Date -->
								<td class="px-4 py-4">
									<p class="font-sans text-xs font-semibold text-ink">{order.eventName}</p>
									<p class="mt-0.5 font-mono text-[10px] text-mute">
										📅 {formatDateTime(order.eventDate)}
									</p>
								</td>

								<!-- Column 3: Tickets Purchased & Price -->
								<td class="px-4 py-4">
									<p class="font-sans text-xs font-medium text-ink">{order.tickets}</p>
									<p class="mt-0.5 font-mono text-[10px] text-mute">
										💰 {formatCurrency(order.totalAmount)} ({order.paymentMethod})
									</p>
								</td>

								<!-- Column 4: Quick Action Buttons -->
								<td class="py-4 pr-6 text-right">
									<div class="inline-flex items-center gap-2">
										{#if order.status === 'PAID'}
											<form method="POST" action="?/updateStatus" use:enhance>
												<input type="hidden" name="id" value={order.id} />
												<input type="hidden" name="status" value="REFUNDED" />
												<button
													type="submit"
													class="cursor-pointer rounded-full border border-hairline bg-canvas px-3 py-1 font-mono text-[10px] font-bold text-ink transition hover:bg-canvas-soft-2"
												>
													REFUND
												</button>
											</form>
										{/if}
										{#if order.status === 'PENDING'}
											<form method="POST" action="?/updateStatus" use:enhance class="flex gap-2">
												<input type="hidden" name="id" value={order.id} />
												<input type="hidden" name="status" value="PAID" />
												<button
													type="submit"
													class="cursor-pointer rounded-full bg-primary px-3 py-1 font-mono text-[10px] font-bold text-on-primary transition hover:bg-primary/95"
												>
													CONFIRM PAID
												</button>
											</form>
											<form method="POST" action="?/updateStatus" use:enhance>
												<input type="hidden" name="id" value={order.id} />
												<input type="hidden" name="status" value="CANCELLED" />
												<button
													type="submit"
													class="cursor-pointer rounded-full border border-error/25 bg-error/5 px-3 py-1 font-mono text-[10px] font-bold text-error transition hover:bg-error/10"
												>
													VOID
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
		<div
			class="flex h-40 flex-col items-center justify-center rounded-lg border border-dashed border-hairline bg-canvas text-center"
		>
			<span class="text-xs font-semibold text-mute">No orders match the search criteria.</span>
		</div>
	{/if}
</div>
