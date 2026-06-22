<script lang="ts">
	import { enhance } from '$app/forms';

	let { data, form } = $props<{ data: any; form: any }>();

	let flatFee = $state(data.fees.flatFee);
	let percentageFee = $state(data.fees.percentageFee);
	let resaleFee = $state(data.fees.resaleFee);
	let payoutFee = $state(data.fees.payoutFee);

	// Preview ticket price for simulator
	let sampleTicketPrice = $state(1000000);

	const previewTotalFee = $derived(flatFee + (sampleTicketPrice * percentageFee) / 100);

	function formatCurrency(amount: number) {
		return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
	}
</script>

<svelte:head>
	<title>Global Fees — Platform Operations</title>
</svelte:head>

<div class="flex flex-1 flex-col space-y-6 p-6">
	<div class="flex flex-col gap-1 border-b border-hairline pb-5">
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">
			Commission & Fees Configuration
		</h1>
		<p class="font-sans text-xs text-body">
			Configure global ticketer booking fees, resale secondary commission rates, and payout clearing
			costs.
		</p>
	</div>

	{#if form?.success}
		<div
			class="rounded-md border border-success bg-success/10 p-3.5 text-xs font-semibold text-success"
		>
			✨ Success: {form.message}
		</div>
	{/if}

	<div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
		<!-- Fee parameters form -->
		<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs lg:col-span-2">
			<form method="POST" action="?/saveFees" use:enhance class="space-y-6">
				<!-- Flat Fee -->
				<div class="space-y-2">
					<div class="flex justify-between">
						<label for="flat-fee" class="font-sans text-sm font-semibold text-ink"
							>Flat Processing Fee</label
						>
						<span class="font-mono text-xs font-bold text-primary">{formatCurrency(flatFee)}</span>
					</div>
					<input
						type="range"
						id="flat-fee"
						name="flatFee"
						min="0"
						max="100000"
						step="5000"
						bind:value={flatFee}
						class="w-full accent-primary"
					/>
					<p class="text-[10px] text-mute">
						Applied to every individual ticket checkout transaction.
					</p>
				</div>

				<!-- Percentage Fee -->
				<div class="space-y-2">
					<div class="flex justify-between">
						<label for="percentage-fee" class="font-sans text-sm font-semibold text-ink"
							>Percentage Ticket Fee</label
						>
						<span class="font-mono text-xs font-bold text-primary">{percentageFee}%</span>
					</div>
					<input
						type="range"
						id="percentage-fee"
						name="percentageFee"
						min="0"
						max="20"
						step="0.5"
						bind:value={percentageFee}
						class="w-full accent-primary"
					/>
					<p class="text-[10px] text-mute">
						Platform revenue share relative to face-value tickets sold.
					</p>
				</div>

				<!-- Resale Fee -->
				<div class="space-y-2">
					<div class="flex justify-between">
						<label for="resale-fee" class="font-sans text-sm font-semibold text-ink"
							>Secondary Resale Commission</label
						>
						<span class="font-mono text-xs font-bold text-primary">{resaleFee}%</span>
					</div>
					<input
						type="range"
						id="resale-fee"
						name="resaleFee"
						min="0"
						max="25"
						step="0.5"
						bind:value={resaleFee}
						class="w-full accent-primary"
					/>
					<p class="text-[10px] text-mute">
						Collected from the seller when secondary listings are purchased.
					</p>
				</div>

				<!-- Payout Fee -->
				<div class="space-y-2">
					<div class="flex justify-between">
						<label for="payout-fee" class="font-sans text-sm font-semibold text-ink"
							>Bank Payout Clearing Cost</label
						>
						<span class="font-mono text-xs font-bold text-primary">{formatCurrency(payoutFee)}</span
						>
					</div>
					<input
						type="range"
						id="payout-fee"
						name="payoutFee"
						min="0"
						max="50000"
						step="1000"
						bind:value={payoutFee}
						class="w-full accent-primary"
					/>
					<p class="text-[10px] text-mute">Deducted from organizers when releasing bank payouts.</p>
				</div>

				<div class="border-t border-hairline pt-4">
					<button
						type="submit"
						class="cursor-pointer rounded-full bg-primary px-5 py-2 font-mono text-[11px] font-bold text-on-primary transition hover:bg-primary/95"
					>
						SAVE FEE SYSTEM RULES
					</button>
				</div>
			</form>
		</div>

		<!-- Calculator Preview Box -->
		<div
			class="flex flex-col justify-between space-y-6 rounded-lg border border-hairline bg-canvas p-6 shadow-xs"
		>
			<div>
				<h3 class="border-b border-hairline pb-3 font-sans text-sm font-semibold text-ink">
					Booking Fee Simulator
				</h3>

				<div class="mt-4 space-y-4">
					<div>
						<label
							for="sample-ticket"
							class="mb-1 block font-mono text-[9px] font-bold tracking-wider text-mute uppercase"
							>Sample Ticket Face Value</label
						>
						<input
							type="number"
							id="sample-ticket"
							bind:value={sampleTicketPrice}
							step="50000"
							min="10000"
							class="w-full rounded-md border border-hairline bg-canvas p-2 text-xs text-ink focus:border-primary focus:outline-none"
						/>
					</div>

					<div
						class="space-y-2 rounded-md border border-hairline bg-canvas-soft-2/50 p-4 font-sans text-xs"
					>
						<div class="flex justify-between">
							<span class="text-mute">Face Value</span>
							<span class="font-semibold text-ink">{formatCurrency(sampleTicketPrice)}</span>
						</div>
						<div class="flex justify-between">
							<span class="text-mute">Flat Fee</span>
							<span class="font-mono text-ink">{formatCurrency(flatFee)}</span>
						</div>
						<div class="flex justify-between">
							<span class="text-mute">Percentage Fee ({percentageFee}%)</span>
							<span class="font-mono text-ink"
								>{formatCurrency((sampleTicketPrice * percentageFee) / 100)}</span
							>
						</div>
						<div class="mt-2 flex justify-between border-t border-hairline pt-2 font-semibold">
							<span class="text-ink">Total Platform Fee</span>
							<span class="font-mono text-primary">{formatCurrency(previewTotalFee)}</span>
						</div>
					</div>
				</div>
			</div>

			<div
				class="rounded-md border border-dashed border-hairline bg-canvas-soft-2 p-3 text-[10px] text-mute italic"
			>
				💡 Adjust sliders on the left to see immediate feedback on how changes affect checkout
				totals.
			</div>
		</div>
	</div>
</div>
