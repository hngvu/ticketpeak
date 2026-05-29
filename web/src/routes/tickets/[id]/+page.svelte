<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import DigitalTicketCard from '$lib/components/tickets/DigitalTicketCard.svelte';

	let { data } = $props<{ data: any }>();

	const order = $derived(data.order);
	const event = $derived(data.event);
	const tickets = $derived(data.tickets);

	let activeTicketIndex = $state(0);
	const activeTicket = $derived(tickets[activeTicketIndex] || tickets[0]);

	function formatCurrency(amount: number, currencyCode: string = 'VND') {
		if (currencyCode === 'VND') {
			return amount.toLocaleString('vi-VN') + ' ₫';
		}
		return new Intl.NumberFormat('en-US', { style: 'currency', currency: currencyCode }).format(
			amount
		);
	}

	const orderDate = $derived(new Date(order.createdAt));
	const formattedOrderDate = $derived(
		orderDate.toLocaleDateString('en-US', {
			year: 'numeric',
			month: 'long',
			day: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		})
	);
</script>

<svelte:head>
	<title>Order Confirmation | Ticketpeak</title>
</svelte:head>

<div class="min-h-screen bg-canvas-soft pt-8 pb-16 select-none">
	<div class="mx-auto max-w-4xl px-4 md:px-6">
		<!-- 1. Celebratory Success Banner -->
		<div class="mb-8 flex flex-col items-center justify-center space-y-4 text-center">
			<div
				class="rounded-full border border-emerald-200 bg-emerald-100 p-4 text-emerald-600 shadow-2xs"
			>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="3"
					stroke-linecap="round"
					stroke-linejoin="round"
					class="h-8 w-8"
				>
					<polyline points="20 6 9 17 4 12" />
				</svg>
			</div>
			<div class="space-y-1">
				<h2 class="font-sans text-2xl font-black tracking-tight text-ink md:text-3xl">
					You're Going!
				</h2>
				<p class="font-sans text-xs font-semibold text-mute md:text-sm">
					Your order is confirmed and digital tickets are ready below.
				</p>
			</div>
		</div>

		<div class="grid grid-cols-1 items-start gap-8 md:grid-cols-[1fr_320px]">
			<!-- Left Column: Receipt and instructions -->
			<div class="space-y-6">
				<!-- Order Receipt card -->
				<section class="rounded-2xl border border-hairline bg-canvas p-6 shadow-2xs">
					<h3
						class="mb-4 border-b border-hairline pb-2.5 font-sans text-base font-extrabold text-ink sm:text-lg"
					>
						Order Receipt
					</h3>

					<div class="space-y-3 font-sans text-xs font-semibold text-mute">
						<div class="flex justify-between">
							<span>Order Number</span>
							<span class="font-bold text-ink">#{order.id.slice(0, 8).toUpperCase()}</span>
						</div>
						<div class="flex justify-between">
							<span>Purchased On</span>
							<span class="font-bold text-ink">{formattedOrderDate}</span>
						</div>
						<div class="flex justify-between">
							<span>Payment Method</span>
							<span class="font-bold text-ink uppercase"
								>{order.paymentId ? 'Verified Gateway' : 'Sandbox Pay'}</span
							>
						</div>
						<div class="flex justify-between">
							<span>Total Amount Paid</span>
							<span class="font-mono text-sm font-bold text-primary">
								{formatCurrency(
									parseFloat(order.totalAmount || order.totalPrice || 0),
									order.currency
								)}
							</span>
						</div>
					</div>
				</section>

				<!-- Entry instructions -->
				<section class="rounded-2xl border border-hairline bg-canvas p-6 shadow-2xs">
					<h3
						class="mb-4 border-b border-hairline pb-2.5 font-sans text-base font-extrabold text-ink sm:text-lg"
					>
						Important Admission Info
					</h3>

					<ul class="space-y-3 text-xs leading-relaxed font-semibold text-mute">
						<li class="flex items-start gap-2.5">
							<span class="font-bold text-primary">📱</span>
							<span
								>Ticketpeak digital e-tickets are strictly electronic. Printed copies or screenshot
								images are invalid due to rotating TOTP barcodes.</span
							>
						</li>
						<li class="flex items-start gap-2.5">
							<span class="font-bold text-primary">⌛</span>
							<span
								>The barcode refreshes dynamically every 30 seconds. Ensure your mobile browser is
								loaded and active at the venue entrance.</span
							>
						</li>
						<li class="flex items-start gap-2.5">
							<span class="font-bold text-primary">💳</span>
							<span
								>Carry matching identification cards (KYC ID or passport) matching the booking email
								details in case verification is requested.</span
							>
						</li>
					</ul>
				</section>
			</div>

			<!-- Right Column: Interactive Digital rotating e-tickets -->
			<div class="space-y-4">
				<h4 class="text-center font-sans text-xs font-extrabold tracking-wider text-mute uppercase">
					Digital Ticket{tickets.length > 1 ? 's' : ''} ({tickets.length})
				</h4>

				<!-- Tab selectors if multi-ticket order -->
				{#if tickets.length > 1}
					<div class="mb-2.5 flex flex-wrap items-center justify-center gap-1.5 select-none">
						{#each tickets as ticket, index (ticket.id)}
							<button
								type="button"
								onclick={() => (activeTicketIndex = index)}
								class="cursor-pointer rounded-full border px-4 py-1.5 font-sans text-xs font-bold transition-all active:scale-95
									{activeTicketIndex === index
									? 'shadow-3xs border-primary bg-primary text-white'
									: 'border-hairline bg-canvas text-ink hover:border-hairline-strong'}"
							>
								Ticket {index + 1}
							</button>
						{/each}
					</div>
				{/if}

				<!-- Active digital ticket card card -->
				{#if activeTicket}
					<div class="flex justify-center transition-all duration-300">
						<DigitalTicketCard ticket={activeTicket} {event} />
					</div>
				{/if}
			</div>
		</div>
	</div>
</div>
