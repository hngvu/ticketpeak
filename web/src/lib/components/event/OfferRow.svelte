<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import OfferQtySelector from './OfferQtySelector.svelte';
	import PriceBreakdown from './PriceBreakdown.svelte';

	let {
		offer,
		inventory,
		selectedQty = 0,
		isExpanded = false,
		onToggle,
		onQtyChange
	} = $props<{
		offer: any;
		inventory: any;
		selectedQty: number;
		isExpanded: boolean;
		onToggle: () => void;
		onQtyChange: (qty: number) => void;
	}>();

	function formatCurrency(amount: number, currency: string = 'VND') {
		if (currency === 'VND') {
			return amount.toLocaleString('vi-VN') + ' ₫';
		}
		return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount);
	}

	function formatDateShort(dateStr: string) {
		const d = new Date(dateStr);
		return d.toLocaleDateString('en-US', {
			month: 'short',
			day: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	}

	// Dynamic state derivations
	const isSelected = $derived(selectedQty > 0);

	const availState = $derived.by(() => {
		const now = new Date();

		// 1. Check inventory availability
		const inv = inventory.gaInventory.find((g: any) => g.offerId === offer.id);
		if (inv && inv.available === 0) return 'soldout';

		// 2. Check active sale windows
		const activeWindow = offer.saleWindows.find((w: any) => {
			const start = new Date(w.startAt);
			const end = new Date(w.endAt);
			return start <= now && now <= end;
		});

		if (!activeWindow) {
			const upcoming = offer.saleWindows.find((w: any) => new Date(w.startAt) > now);
			if (upcoming) {
				return { status: 'not-on-sale', upcoming };
			}
			return 'closed';
		}

		if (activeWindow.type === 'PRESALE') return 'presale';
		return 'available';
	});

	const stateLabel = $derived.by(() => {
		if (availState === 'soldout') return 'soldout';
		if (typeof availState === 'object' && availState.status === 'not-on-sale') return 'not-on-sale';
		if (availState === 'presale') return 'presale';
		return 'available';
	});
</script>

<div class="mb-3 overflow-hidden rounded-lg select-none">
	<!-- Collapsed Header Panel (div) -->
	<div
		class="flex w-full items-center gap-4 border px-4 py-5 transition-all duration-200
			{stateLabel === 'soldout'
			? 'cursor-not-allowed border-hairline bg-canvas-soft/60 opacity-60 select-none'
			: isSelected
				? 'bg-blue-accent-soft/30 border-primary/40 shadow-2xs'
				: 'border-hairline bg-canvas hover:border-hairline-strong hover:shadow-2xs'}"
	>
		<!-- Left: Clickable details button to expand/collapse -->
		<button
			type="button"
			onclick={onToggle}
			disabled={stateLabel === 'soldout'}
			class="min-w-0 flex-1 text-left focus:outline-none
				{stateLabel === 'soldout' ? 'cursor-not-allowed' : 'cursor-pointer'}"
		>
			<h4
				class="font-sans text-sm font-semibold text-ink transition-colors sm:text-base
					{stateLabel === 'soldout' ? '' : 'hover:text-primary'}"
			>
				{offer.name}
			</h4>
			<p class="mt-1 truncate font-sans text-xs text-mute sm:text-sm">{offer.description}</p>
		</button>

		<!-- Right: Pricing and select actions (sibling) -->
		<div class="flex shrink-0 items-center gap-3.5">
			<!-- Price info -->
			<div class="text-right">
				{#if stateLabel === 'soldout'}
					<span
						class="bg-error-soft text-error-deep rounded-md px-2.5 py-1 font-sans text-[10px] font-bold tracking-wider uppercase"
					>
						Sold Out
					</span>
				{:else if stateLabel === 'not-on-sale'}
					<span class="block font-sans text-[10px] font-bold text-amber-600">
						On sale: {formatDateShort((availState as any).upcoming.startAt)}
					</span>
				{:else}
					<div class="flex flex-col">
						<span class="font-mono text-sm font-bold text-ink sm:text-base">
							{formatCurrency(offer.faceValue, offer.currency)}
						</span>
						<span
							class="mt-0.5 block font-sans text-[9px] font-semibold tracking-wide text-mute uppercase"
						>
							per ticket
						</span>
					</div>
				{/if}
			</div>

			<!-- Qty selector (only if available) -->
			{#if stateLabel === 'available' || stateLabel === 'presale'}
				<OfferQtySelector
					quantities={offer.sellableQuantities}
					value={selectedQty}
					onchange={(qty) => onQtyChange(qty)}
				/>

				<!-- Toggle Add Button -->
				<button
					type="button"
					onclick={() => onQtyChange(isSelected ? 0 : offer.sellableQuantities[0] || 1)}
					class="flex h-9 w-9 cursor-pointer items-center justify-center rounded-full text-base font-bold shadow-2xs transition-all active:scale-90
						{isSelected
						? 'border border-hairline bg-canvas text-ink hover:border-hairline-strong hover:bg-canvas-soft'
						: 'hover:bg-link-deep bg-primary text-on-primary'}"
				>
					{isSelected ? '−' : '+'}
				</button>
			{/if}
		</div>
	</div>

	<!-- Expanded Details Panel -->
	{#if isExpanded}
		<div class="border-x border-b border-hairline/80 bg-canvas-soft/40 px-4.5 pt-4.5 pb-5">
			<!-- Full description -->
			<p class="mb-4.5 font-sans text-xs leading-relaxed text-body sm:text-sm">
				{offer.description}
			</p>

			<!-- Detailed price breakdown -->
			<PriceBreakdown {offer} qty={selectedQty || 1} />

			<!-- Informational restrictions -->
			<div class="mt-3.5 space-y-1 rounded-md border border-hairline/40 bg-canvas p-3">
				{#if offer.eventTicketMinimum > 1}
					<p class="flex items-center gap-1.5 font-sans text-[10px] font-semibold text-mute">
						<span class="font-bold text-primary">ℹ️</span> Minimum purchase of {offer.eventTicketMinimum}
						tickets per order.
					</p>
				{/if}
				{#if offer.restrictSingleSeat}
					<p class="flex items-center gap-1.5 font-sans text-[10px] font-semibold text-mute">
						<span class="font-bold text-primary">ℹ️</span> Single seat purchase restriction: Adjacent
						seating required.
					</p>
				{/if}
				<p class="flex items-center gap-1.5 font-sans text-[10px] font-semibold text-mute">
					<span class="font-bold text-primary">ℹ️</span> Verified tickets powered by Ticketpeak's secure
					checkout.
				</p>
			</div>
		</div>
	{/if}
</div>
