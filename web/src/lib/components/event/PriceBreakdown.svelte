<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	let { offer, qty } = $props<{ offer: any; qty: number }>();

	function formatCurrency(amount: number, currency: string = 'VND') {
		if (currency === 'VND') {
			return amount.toLocaleString('vi-VN') + ' ₫';
		}
		return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount);
	}

	const charges = $derived(offer.charges || []);

	function getChargeAmount(charge: any) {
		if (charge.isPercentage) {
			return (offer.faceValue * charge.amount) / 100;
		}
		return charge.amount;
	}

	const totalChargesPerTicket = $derived(
		charges.reduce((sum: number, c: any) => sum + getChargeAmount(c), 0)
	);

	const subtotalPerTicket = $derived(offer.faceValue + totalChargesPerTicket);
</script>

<div
	class="space-y-2.5 rounded-lg border border-hairline bg-canvas-soft p-4.5 font-sans text-xs select-none"
>
	<h5 class="mb-3 text-[10px] font-bold tracking-wider text-ink uppercase">
		Price Breakdown ({qty} Ticket{qty > 1 ? 's' : ''})
	</h5>

	<!-- Face value -->
	<div class="flex items-center justify-between font-medium text-body">
		<span>Face Value</span>
		<span class="font-mono">
			{formatCurrency(offer.faceValue, offer.currency)} × {qty} =
			<span class="font-bold text-ink">{formatCurrency(offer.faceValue * qty, offer.currency)}</span
			>
		</span>
	</div>

	<!-- Charges loop -->
	{#if charges.length > 0}
		<div class="space-y-1.5 border-t border-hairline/60 pt-2.5">
			{#each charges as charge (charge.name)}
				{@const chargeAmt = getChargeAmount(charge)}
				<div class="flex items-center justify-between text-mute">
					<span class="flex items-center gap-1.5 pl-2">
						<span class="inline-block h-1 w-1 rounded-full bg-mute/40"></span>
						{charge.name}
					</span>
					<span class="font-mono">
						{formatCurrency(chargeAmt, offer.currency)} × {qty} = {formatCurrency(
							chargeAmt * qty,
							offer.currency
						)}
					</span>
				</div>
			{/each}
		</div>
	{/if}

	<!-- Subtotal row -->
	<div
		class="flex items-center justify-between border-t border-hairline-strong pt-3 text-sm font-bold text-ink"
	>
		<span>Subtotal</span>
		<span class="font-mono text-primary">
			{formatCurrency(subtotalPerTicket * qty, offer.currency)}
		</span>
	</div>
</div>
