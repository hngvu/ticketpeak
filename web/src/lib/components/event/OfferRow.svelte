<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import OfferQtySelector from './OfferQtySelector.svelte';
	let {
		offer,
		inventory,
		selectedQty = 0,
		isExpanded = false,
		hasManifest = true,
		globalQty = 2,
		onToggle,
		onQtyChange,
		onChooseSeats
	} = $props<{
		offer: any;
		inventory: any;
		selectedQty: number;
		isExpanded?: boolean;
		hasManifest?: boolean;
		globalQty?: number;
		onToggle?: () => void;
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

	function handleClick() {
		if (stateLabel === 'soldout') return;
		if (typeof availState === 'object' && availState.status === 'not-on-sale') return;

		if (offer.seatingMode === 'RESERVED_SEATING') {
			onChooseSeats?.();
		} else {
			onQtyChange(isSelected ? 0 : globalQty);
		}
	}
</script>

<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
	onclick={handleClick}
	class="flex w-full cursor-pointer items-center gap-3 border-b border-hairline bg-white px-4 py-3.5 text-left transition-colors hover:bg-[#f5f7fa] {stateLabel ===
	'soldout'
		? 'cursor-not-allowed opacity-50'
		: ''}"
>


	<!-- Info -->
	<div class="min-w-0 flex-1">
		<span class="block truncate text-[13px] font-bold text-ink">{offer.name}</span>
		<span class="mt-0.5 block truncate text-[12px] text-mute"
			>{offer.description || 'Standard Ticket'}</span
		>
	</div>

	<!-- Price & Action -->
	<div class="shrink-0 text-right">
		{#if stateLabel === 'soldout'}
			<span class="text-[12px] font-bold text-error">Sold Out</span>
		{:else if stateLabel === 'not-on-sale'}
			<span class="text-[11px] font-bold text-amber-600"
				>On sale: {formatDateShort((availState as any).upcoming.startAt)}</span
			>
		{:else}
			<span class="block text-[14px] font-bold text-[#026cdf]"
				>{formatCurrency(offer.faceValue, offer.currency)}</span
			>
		{/if}
	</div>
</div>
