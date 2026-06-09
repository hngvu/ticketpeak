<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	let {
		event,
		selectedItems = [],
		orderTotal = 0,
		totalQty = 0,
		reserving = false,
		reservationError = null,
		onGetTickets
	} = $props<{
		event: any;
		selectedItems: any[];
		orderTotal: number;
		totalQty: number;
		reserving: boolean;
		reservationError: string | null;
		onGetTickets: () => void;
	}>();

	function formatCurrency(amount: number, currency: string = 'VND') {
		if (currency === 'VND') {
			return amount.toLocaleString('vi-VN') + ' ₫';
		}
		return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount);
	}

	const eventDate = $derived(new Date(event.startAt));
	const dateShort = $derived(
		eventDate
			.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })
			.toUpperCase()
	);
	const timeLabel = $derived(
		eventDate.toLocaleTimeString('en-US', {
			hour: 'numeric',
			minute: '2-digit',
			timeZoneName: 'short'
		})
	);

	function getChargeAmount(offer: any, charge: any) {
		if (charge.isPercentage) {
			return (offer.faceValue * charge.amount) / 100;
		}
		return charge.amount;
	}

	const currencyCode = $derived(selectedItems[0]?.offer?.currency || 'VND');
</script>

<div
	class="card-marketing-large w-full rounded-2xl border border-hairline bg-canvas p-6 shadow-xs select-none"
>
	<!-- Event Header Band -->
	<div class="space-y-1.5 pb-5">
		<p class="font-mono text-[10px] font-bold tracking-widest text-mute uppercase">
			{dateShort} · {timeLabel}
		</p>
		<h3 class="font-sans text-lg leading-snug font-extrabold text-ink">
			{event.venueName}
		</h3>
		<p class="font-sans text-xs font-semibold text-mute">
			{event.cityName}, VN
		</p>
	</div>

	<!-- Divider -->
	<div class="border-t border-hairline"></div>

	<!-- Ticket Selection list -->
	<div class="flex min-h-[160px] flex-col justify-center py-5">
		{#if selectedItems.length === 0}
			<!-- Empty State -->
			<div class="flex flex-col items-center justify-center space-y-3 text-center">
				<div class="rounded-full bg-canvas-soft p-3 text-mute">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2"
						stroke-linecap="round"
						stroke-linejoin="round"
						class="h-6 w-6"
					>
						<path d="M2 9a3 3 0 0 1 3-3h14a3 3 0 0 1 3 3v6a3 3 0 0 1-3 3H5a3 3 0 0 1-3-3z" />
						<path d="M9 6v12M15 6v12" />
					</svg>
				</div>
				<p class="font-sans text-xs font-semibold text-mute">Select tickets to get started</p>
			</div>
		{:else}
			<!-- Items list -->
			<div class="space-y-4">
				<h4 class="font-sans text-xs font-extrabold tracking-wider text-ink uppercase">
					Your Selection
				</h4>
				<div class="max-h-[220px] space-y-3 overflow-y-auto pr-1">
					{#each selectedItems as item (item.offer.id)}
						{@const offer = item.offer}
						{@const qty = item.qty}
						<div class="space-y-1.5">
							<!-- Main row -->
							<div class="flex items-center justify-between font-sans text-xs font-bold text-ink">
								<span>{offer.name} × {qty}</span>
								<span class="font-mono"
									>{formatCurrency(offer.faceValue * qty, offer.currency)}</span
								>
							</div>

							<!-- Itemized charges -->
							{#if offer.charges && offer.charges.length > 0}
								<div class="space-y-1 pl-3 font-sans text-[10px] text-mute">
									{#each offer.charges as charge (charge.name)}
										{@const amt = getChargeAmount(offer, charge)}
										<div class="flex justify-between">
											<span>{charge.name} × {qty}</span>
											<span class="font-mono">{formatCurrency(amt * qty, offer.currency)}</span>
										</div>
									{/each}
								</div>
							{/if}
						</div>
					{/each}
				</div>
			</div>
		{/if}
	</div>

	<!-- Divider -->
	<div class="border-t border-hairline pb-4.5"></div>

	<!-- Aggregated Total -->
	<div class="flex items-baseline justify-between pb-5">
		<span class="font-sans text-sm font-extrabold tracking-wide text-ink uppercase">Total</span>
		<div class="text-right">
			<span class="font-mono text-xl font-black text-primary">
				{formatCurrency(orderTotal, currencyCode)}
			</span>
			<span
				class="mt-0.5 block font-sans text-[9px] font-semibold tracking-wide text-mute uppercase"
			>
				All fees included
			</span>
		</div>
	</div>

	<!-- Error Banner (if reservation fails) -->
	{#if reservationError}
		<div
			class="bg-error-soft text-error-deep mb-4 rounded-lg border border-error/20 p-3.5 font-sans text-[11px] leading-relaxed font-semibold"
		>
			⚠️ {reservationError}
		</div>
	{/if}

	<!-- Get Tickets Action -->
	<button
		type="button"
		onclick={onGetTickets}
		disabled={totalQty === 0 || reserving}
		class="hover:bg-link-deep flex w-full cursor-pointer items-center justify-center rounded-full bg-primary py-3.5 font-sans text-xs font-bold text-white shadow-md transition-all active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50"
	>
		{#if reserving}
			<svg
				class="mr-2 h-4 w-4 animate-spin text-white"
				xmlns="http://www.w3.org/2000/svg"
				fill="none"
				viewBox="0 0 24 24"
			>
				<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
				></circle>
				<path
					class="opacity-75"
					fill="currentColor"
					d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
				></path>
			</svg>
			<span>Reserving Tickets...</span>
		{:else}
			<span>Get Tickets →</span>
		{/if}
	</button>
</div>
