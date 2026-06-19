<script lang="ts">
	import { enhance } from '$app/forms';

	let { data, form } = $props<{ data: any; form: any }>();

	function formatCurrency(amount: number) {
		return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
	}

	function isCapViolated(original: number, resale: number) {
		return resale > original * 1.5;
	}
</script>

<svelte:head>
	<title>Secondary Marketplace — Platform Operations</title>
</svelte:head>

<div class="flex flex-1 flex-col space-y-6 p-6">
	<div class="flex flex-col gap-1 border-b border-hairline pb-5">
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">Secondary Ticket Marketplace</h1>
		<p class="font-sans text-xs text-body">
			Audit resale listings, verify platform price caps, and moderate scalping activities.
		</p>
	</div>

	{#if form?.success}
		<div class="rounded-md border border-success bg-success/10 p-3.5 text-xs font-semibold text-success">
			✨ Success: {form.message}
		</div>
	{/if}

	{#if data.resales && data.resales.length > 0}
		<div class="rounded-lg border border-hairline bg-canvas shadow-xs">
			<div class="overflow-x-auto">
				<table class="w-full text-left text-xs text-body">
					<tbody class="divide-y divide-hairline">
						{#each data.resales as resale (resale.id)}
							{@const violated = isCapViolated(resale.originalPrice, resale.resalePrice)}
							<tr class="hover:bg-canvas-soft/40 transition">
								<!-- Column 1: Event Name & Ticket Code -->
								<td class="py-4 pl-6 pr-4">
									<div class="flex items-center gap-2">
										<h4 class="text-sm font-semibold text-ink">{resale.eventName}</h4>
										{#if resale.status !== 'COMPLETED'}
											<span class="rounded bg-canvas-soft-2 px-2 py-0.5 font-mono text-[9px] font-bold text-body uppercase border border-hairline">
												{resale.status}
											</span>
										{/if}
										{#if violated}
											<span class="rounded bg-error/10 px-2 py-0.5 font-mono text-[9px] font-bold text-error uppercase border border-error/20">
												CAP VIOLATION (>150%)
											</span>
										{/if}
									</div>
									<p class="font-mono mt-1 text-[10px] text-mute">
										🎫 Code: {resale.ticketCode} • ID: {resale.id}
									</p>
								</td>

								<!-- Column 2: Price Details & Scalping Audit -->
								<td class="px-4 py-4">
									<p class="font-sans text-xs font-medium text-ink">
										Resale: {formatCurrency(resale.resalePrice)}
									</p>
									<p class="font-mono mt-0.5 text-[10px] text-mute">
										Face Value: {formatCurrency(resale.originalPrice)}
									</p>
								</td>

								<!-- Column 3: Seller / Buyer details -->
								<td class="px-4 py-4">
									<p class="font-sans text-xs font-semibold text-ink">Seller: {resale.sellerEmail}</p>
									{#if resale.buyerEmail}
										<p class="font-mono mt-0.5 text-[10px] text-mute">Buyer: {resale.buyerEmail}</p>
									{:else}
										<p class="font-mono mt-0.5 text-[10px] text-mute">No buyer yet</p>
									{/if}
								</td>

								<!-- Column 4: Operational actions -->
								<td class="py-4 pr-6 text-right">
									<div class="inline-flex items-center gap-2">
										{#if resale.status === 'PENDING'}
											<form method="POST" action="?/updateResaleStatus" use:enhance>
												<input type="hidden" name="id" value={resale.id} />
												<input type="hidden" name="status" value="COMPLETED" />
												<button
													type="submit"
													class="cursor-pointer rounded-full bg-primary px-3 py-1 font-mono text-[10px] font-bold text-on-primary hover:bg-primary/95 transition"
												>
													APPROVE
												</button>
											</form>

											<form method="POST" action="?/updateResaleStatus" use:enhance>
												<input type="hidden" name="id" value={resale.id} />
												<input type="hidden" name="status" value="SUSPENDED" />
												<button
													type="submit"
													class="cursor-pointer rounded-full border border-error/25 bg-error/5 px-3 py-1 font-mono text-[10px] font-bold text-error hover:bg-error/10 transition"
												>
													SUSPEND
												</button>
											</form>
										{/if}

										{#if resale.status === 'SUSPENDED'}
											<form method="POST" action="?/updateResaleStatus" use:enhance>
												<input type="hidden" name="id" value={resale.id} />
												<input type="hidden" name="status" value="PENDING" />
												<button
													type="submit"
													class="cursor-pointer rounded-full border border-hairline bg-canvas px-3 py-1 font-mono text-[10px] font-bold text-ink hover:bg-canvas-soft-2 transition"
												>
													RE-ACTIVATE
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
			<span class="text-xs font-semibold text-mute">No secondary market activity currently recorded.</span>
		</div>
	{/if}
</div>
