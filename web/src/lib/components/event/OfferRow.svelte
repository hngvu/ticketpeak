<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	let {
		offer,
		inventory,
		selectedQty = 0,
		isExpanded = false,
		onToggle,
		onQtyChange,
		onChooseSeats
	} = $props<{
		offer: any;
		inventory: any;
		selectedQty: number;
		isExpanded: boolean;
		onToggle: () => void;
		onQtyChange: (qty: number) => void;
		onChooseSeats?: () => void;
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

	const isSelected = $derived(selectedQty > 0);

	const availState = $derived.by(() => {
		const now = new Date();
		const inv = inventory.gaInventory.find((g: any) => g.offerId === offer.id);
		if (inv && inv.available === 0) return 'soldout';

		const activeWindow = offer.saleWindows.find((w: any) => {
			const start = new Date(w.startAt);
			const end = new Date(w.endAt);
			return start <= now && now <= end;
		});

		if (!activeWindow) {
			const upcoming = offer.saleWindows.find((w: any) => new Date(w.startAt) > now);
			if (upcoming) return { status: 'not-on-sale', upcoming };
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

	function handleClick() {
		if (stateLabel === 'soldout') return;
		if (offer.seatingMode === 'RESERVED_SEATING') {
			onChooseSeats?.();
		} else {
			onQtyChange(isSelected ? 0 : offer.sellableQuantities?.[0] || 1);
		}
	}
</script>

<button
	type="button"
	onclick={handleClick}
	disabled={stateLabel === 'soldout'}
	class="flex w-full items-center justify-between gap-3 border border-hairline px-4 py-3.5 text-left transition-all
		{stateLabel === 'soldout'
			? 'cursor-not-allowed opacity-50'
			: isSelected
				? 'border-primary/50 bg-primary/5'
				: 'cursor-pointer hover:bg-canvas-soft hover:border-hairline-strong'}"
>
	<div class="min-w-0 flex-1">
		<span class="block text-sm font-semibold text-ink truncate">{offer.name}</span>
		{#if offer.description}
			<span class="mt-0.5 block text-[11px] text-mute truncate">{offer.description}</span>
		{/if}
	</div>

	<div class="shrink-0 text-right">
		{#if stateLabel === 'soldout'}
			<span class="text-[11px] font-bold text-error">Sold Out</span>
		{:else if stateLabel === 'not-on-sale'}
			<span class="text-[10px] font-bold text-amber-600">On sale: {formatDateShort((availState as any).upcoming.startAt)}</span>
		{:else}
			<span class="block text-sm font-bold text-ink">{formatCurrency(offer.faceValue, offer.currency)}</span>
			<span class="block text-[10px] text-mute">per ticket</span>
		{/if}
	</div>
</button>
