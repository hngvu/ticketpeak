<script lang="ts">
	let {
		orderTotal = 0,
		totalQty = 0,
		reserving = false,
		currency = 'VND',
		onGetTickets
	} = $props<{
		orderTotal: number;
		totalQty: number;
		reserving: boolean;
		currency: string;
		onGetTickets: () => void;
	}>();

	function formatCurrency(amount: number, currencyCode: string = 'VND') {
		if (currencyCode === 'VND') {
			return amount.toLocaleString('vi-VN') + ' ₫';
		}
		return new Intl.NumberFormat('en-US', { style: 'currency', currency: currencyCode }).format(
			amount
		);
	}
</script>

<div
	class="fixed right-0 bottom-0 left-0 z-50 border-t border-hairline bg-canvas px-4 py-3.5 shadow-lg select-none lg:hidden"
>
	<div class="mx-auto max-w-md">
		{#if totalQty === 0}
			<!-- Empty / Disabled State (Full width button) -->
			<button
				type="button"
				disabled
				class="flex w-full cursor-not-allowed items-center justify-center rounded-full border border-hairline-strong/10 bg-canvas-soft-2 py-3.5 font-sans text-xs font-bold text-mute"
			>
				Select tickets to continue
			</button>
		{:else}
			<!-- Populated State (Split View) -->
			<div class="flex items-center justify-between gap-4">
				<!-- Total price & count -->
				<div class="flex min-w-0 flex-col">
					<span
						class="font-sans text-[10px] leading-none font-bold tracking-wider text-mute uppercase"
					>
						Total
					</span>
					<span
						class="mt-1 truncate font-mono text-base leading-tight font-extrabold text-ink sm:text-lg"
					>
						{formatCurrency(orderTotal, currency)}
					</span>
					<span class="mt-0.5 font-sans text-[10px] leading-none font-medium text-mute">
						{totalQty} ticket{totalQty > 1 ? 's' : ''}
					</span>
				</div>

				<!-- Get Tickets Button -->
				<button
					type="button"
					onclick={onGetTickets}
					disabled={reserving}
					class="flex h-11 shrink-0 items-center justify-center rounded-full bg-primary px-6 font-sans text-xs font-bold text-white shadow-md transition-all hover:bg-link-deep active:scale-[0.98] disabled:opacity-50"
				>
					{#if reserving}
						<svg
							class="mr-2 h-4 w-4 animate-spin text-white"
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
						>
							<circle
								class="opacity-25"
								cx="12"
								cy="12"
								r="10"
								stroke="currentColor"
								stroke-width="4"
							></circle>
							<path
								class="opacity-75"
								fill="currentColor"
								d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
							></path>
						</svg>
						<span>Reserving...</span>
					{:else}
						<span>Get Tickets →</span>
					{/if}
				</button>
			</div>
		{/if}
	</div>
</div>
