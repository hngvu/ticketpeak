<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	let {
		items = [],
		currency = 'VND',
		event = null
	} = $props<{
		items: any[];
		currency: string;
		event?: any;
	}>();

	function formatEventDate(dateStr: string, timezone?: string) {
		const d = new Date(dateStr);
		return d.toLocaleDateString('en-US', {
			weekday: 'short',
			month: 'short',
			day: 'numeric',
			year: 'numeric',
			timeZone: timezone ?? 'Asia/Ho_Chi_Minh'
		});
	}

	function formatCurrency(amount: number, currencyCode: string = 'VND') {
		if (currencyCode === 'VND') {
			return amount.toLocaleString('vi-VN') + ' ₫';
		}
		return new Intl.NumberFormat('en-US', { style: 'currency', currency: currencyCode }).format(
			amount
		);
	}

	function getChargeAmount(unitFaceValue: number, charge: any) {
		if (charge.isPercentage) {
			return (unitFaceValue * charge.amount) / 100;
		}
		return charge.amount;
	}

	// Calculate aggregates
	const orderTotal = $derived(
		items.reduce((acc: number, item: any) => {
			const face = parseFloat(item.unitFaceValue);
			const chargesSum = (item.charges || []).reduce((sum: number, c: any) => {
				return sum + getChargeAmount(face, c);
			}, 0);
			return acc + (face + chargesSum) * item.qty;
		}, 0)
	);
</script>

<div
	class="card-marketing-large w-full rounded-2xl border border-hairline bg-canvas p-6 shadow-xs select-none"
>
	{#if event}
		<div class="mb-5 flex items-start gap-3 border-b border-hairline pb-5">
			{#if event.imageUrl}
				<img
					src={event.imageUrl}
					alt={event.title}
					class="h-14 w-14 shrink-0 rounded-lg object-cover shadow-xs"
				/>
			{/if}
			<div class="min-w-0">
				<p class="line-clamp-2 font-sans text-sm leading-snug font-bold text-ink">
					{event.title}
				</p>
				{#if event.startAt}
					<p class="mt-1 font-sans text-xs text-mute">
						{formatEventDate(event.startAt, event.timezone)}
					</p>
				{/if}
				{#if event.venueName}
					<p class="mt-0.5 truncate font-sans text-xs text-mute">
						{event.venueName}{event.cityName ? ` · ${event.cityName}` : ''}
					</p>
				{/if}
			</div>
		</div>
	{/if}

	<h3 class="mb-4.5 font-sans text-base font-extrabold text-ink sm:text-lg">Order Summary</h3>

	<!-- Items List -->
	<div class="space-y-4">
		{#each items as item (item.id)}
			{@const unitFace = parseFloat(item.unitFaceValue)}
			<div class="space-y-2 border-b border-hairline/60 pb-4 last:border-0 last:pb-0">
				<!-- Main Ticket Title & Subtotal -->
				<div class="flex items-center justify-between font-sans text-sm font-bold text-ink">
					<div>
						<p>{item.seatingMode === 'GA' ? 'General Admission' : 'Assigned Seating'}</p>
						<p class="mt-0.5 font-sans text-[11px] font-semibold text-mute">
							Qty: {item.qty} × {formatCurrency(unitFace, currency)}
						</p>
					</div>
					<span class="font-mono">{formatCurrency(unitFace * item.qty, currency)}</span>
				</div>

				<!-- itemized charges -->
				{#if item.charges && item.charges.length > 0}
					<div class="space-y-1 pl-3 font-sans text-[11px] text-mute">
						{#each item.charges as charge (charge.name)}
							{@const chargeAmt = getChargeAmount(unitFace, charge)}
							<div class="flex justify-between">
								<span>{charge.name} (× {item.qty})</span>
								<span class="font-mono">{formatCurrency(chargeAmt * item.qty, currency)}</span>
							</div>
						{/each}
					</div>
				{/if}
			</div>
		{/each}
	</div>

	<!-- Divider -->
	<div class="my-4.5 border-t border-hairline"></div>

	<!-- Totals -->
	<div class="flex items-baseline justify-between font-sans">
		<span class="text-sm font-extrabold tracking-wider text-ink uppercase">Total</span>
		<div class="text-right">
			<span class="font-mono text-xl font-black text-primary">
				{formatCurrency(orderTotal, currency)}
			</span>
			<span
				class="mt-0.5 block font-sans text-[9px] font-semibold tracking-wide text-mute uppercase"
			>
				All taxes and service fees included
			</span>
		</div>
	</div>
</div>
