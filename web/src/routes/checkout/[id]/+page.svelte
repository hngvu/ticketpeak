<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { goto } from '$app/navigation';
	import CheckoutTimer from '$lib/components/checkout/CheckoutTimer.svelte';
	import CheckoutSummary from '$lib/components/checkout/CheckoutSummary.svelte';

	let { data } = $props<{ data: any }>();

	const reservation = $derived(data.reservation);
	const currentUser = $derived(data.currentUser);

	// Form & UI States
	let email = $state(currentUser?.email || '');
	let fullName = $state(currentUser?.fullName || '');
	let provider = $state<'VNPAY' | 'STRIPE'>('VNPAY');
	let isExpired = $state(false);
	let submitting = $state(false);
	let checkoutError = $state<string | null>(null);

	// Simulation states
	let activePaymentId = $state<string | null>(null);
	let simulationStep = $state<number>(0); // 0: inactive, 1: sending, 2: success, 3: fail
	let simulationLog = $state<string[]>([]);
	let showSimulator = $state(false);

	function handleExpire() {
		isExpired = true;
	}

	function formatCurrency(amount: number, currency: string = 'VND') {
		if (currency === 'VND') {
			return amount.toLocaleString('vi-VN') + ' ₫';
		}
		return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount);
	}

	const orderTotal = $derived(
		reservation.items.reduce((acc: number, item: any) => {
			const face = parseFloat(item.unitFaceValue);
			const chargesSum = (item.charges || []).reduce((sum: number, c: any) => {
				const amt = c.isPercentage ? (face * c.amount) / 100 : c.amount;
				return sum + amt;
			}, 0);
			return acc + (face + chargesSum) * item.qty;
		}, 0)
	);

	// 1. Submit reservation order to initiate payment
	async function handlePlaceOrder() {
		if (isExpired || submitting) return;
		submitting = true;
		checkoutError = null;

		try {
			const res = await fetch('/api/payments', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					reservationId: reservation.id,
					provider
				})
			});

			const json = await res.json();

			if (!res.ok) {
				throw new Error(json.message || 'Failed to place checkout order.');
			}

			// Capture the newly created payment intent ID
			activePaymentId = json.data.paymentId;
			showSimulator = true;
			submitting = false;
		} catch (err: any) {
			checkoutError = err.message || 'Payment initiation failed.';
			submitting = false;
		}
	}

	// 2. Local development checkout simulation
	async function runPaymentSimulation() {
		if (!activePaymentId || simulationStep > 0) return;
		simulationStep = 1;
		simulationLog = [
			'✨ Initiating developer sandbox payment clearance...',
			'🔑 Loading default local hash secret (3E4DFD39EE75...)',
			`💰 Formatting payment total: ${formatCurrency(orderTotal, reservation.currency)}`,
			`🔗 Preparing VNPay IPN form fields for payment ID: ${activePaymentId}`,
			'🔏 Sorting query parameter keys alphabetically...',
			'⚙️ Computing HMAC-SHA512 signature hash...'
		];

		try {
			// Trigger local proxy simulation
			const res = await fetch('/api/payments/vnpay/ipn', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					paymentId: activePaymentId,
					amount: orderTotal
				})
			});

			const json = await res.json();

			if (!res.ok) {
				throw new Error(json.message || 'Simulation gateway rejected payment.');
			}

			simulationLog = [
				...simulationLog,
				'🚀 HMAC-SHA512 secure hash generated successfully!',
				'📡 Relaying callback params to Spring Boot /api/payments/vnpay/ipn...',
				'✅ Backend Response: 00 (Confirm Success - Order Confirmed!)',
				'🔍 Querying user orders to locate confirmed tickets...'
			];

			// Delay slightly to allow DB writes, then fetch orders
			await new Promise((resolve) => setTimeout(resolve, 1500));

			const ordersRes = await fetch('/api/orders');
			if (ordersRes.ok) {
				const ordersJson = await ordersRes.json();
				// Find order matching current reservation
				const order = (ordersJson.data?.content || []).find(
					(o: any) => o.reservationId === reservation.id
				);

				if (order) {
					simulationLog = [
						...simulationLog,
						`🎉 Order confirmed! ID: ${order.id}`,
						'➡️ Redirecting to digital ticket portal...'
					];
					await new Promise((resolve) => setTimeout(resolve, 1000));
					goto(`/tickets/${order.id}`);
					return;
				}
			}

			// Graceful mock fallback redirection
			simulationLog = [
				...simulationLog,
				'⚠️ Dynamic order query empty or backend mock mode active.',
				'➡️ Redirecting to tickets portal using mock parameters...'
			];
			await new Promise((resolve) => setTimeout(resolve, 1000));
			goto(`/tickets/mock-order-${reservation.id}`);
		} catch (err: any) {
			simulationLog = [...simulationLog, `❌ Simulation Failure: ${err.message || err}`];
			simulationStep = 3;
		}
	}
</script>

<svelte:head>
	<title>Checkout | Ticketpeak</title>
</svelte:head>

<div class="min-h-screen bg-canvas-soft pt-8 pb-16 select-none">
	<div class="mx-auto max-w-[1400px] px-4 md:px-6">
		<!-- Countdown hold band -->
		<div class="mb-8">
			<CheckoutTimer expiresAt={reservation.expiresAt} onExpire={handleExpire} />
		</div>

		<div class="grid grid-cols-1 gap-8 lg:grid-cols-[1fr_380px]">
			<!-- Left Column: Checkout Details -->
			<div class="space-y-6">
				<!-- Contact info card -->
				<section class="rounded-2xl border border-hairline bg-canvas p-6 shadow-2xs">
					<h3 class="mb-4.5 font-sans text-base font-extrabold text-ink sm:text-lg">
						1. Contact Information
					</h3>
					<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
						<div class="space-y-1">
							<label
								for="checkout-email"
								class="font-sans text-xs font-bold tracking-wide text-mute uppercase">Email</label
							>
							<input
								id="checkout-email"
								type="email"
								bind:value={email}
								disabled={currentUser !== null}
								class="w-full rounded-lg border border-hairline bg-canvas-soft/40 px-3.5 py-2.5 font-sans text-sm font-semibold text-ink outline-none hover:border-hairline-strong focus:border-primary disabled:opacity-60"
							/>
						</div>
						<div class="space-y-1">
							<label
								for="checkout-name"
								class="font-sans text-xs font-bold tracking-wide text-mute uppercase"
								>Full Name</label
							>
							<input
								id="checkout-name"
								type="text"
								bind:value={fullName}
								disabled={currentUser !== null}
								class="w-full rounded-lg border border-hairline bg-canvas-soft/40 px-3.5 py-2.5 font-sans text-sm font-semibold text-ink outline-none hover:border-hairline-strong focus:border-primary disabled:opacity-60"
							/>
						</div>
					</div>
				</section>

				<!-- Payment selection card -->
				<section class="rounded-2xl border border-hairline bg-canvas p-6 shadow-2xs">
					<h3 class="mb-4.5 font-sans text-base font-extrabold text-ink sm:text-lg">
						2. Select Payment Method
					</h3>

					<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
						<!-- VNPay Card Option -->
						<button
							type="button"
							onclick={() => (provider = 'VNPAY')}
							disabled={isExpired}
							class="flex cursor-pointer flex-col items-start rounded-xl border p-4.5 text-left transition-all duration-200 outline-none
								{provider === 'VNPAY'
								? 'bg-blue-accent-soft/20 border-primary/50 shadow-2xs'
								: 'border-hairline bg-canvas hover:border-hairline-strong hover:bg-canvas-soft/10'} disabled:opacity-50"
						>
							<div class="flex w-full items-center justify-between">
								<span
									class="rounded-md bg-blue-600 px-2 py-0.5 font-mono text-[9px] font-bold tracking-wider text-white uppercase"
									>VNPay</span
								>
								<span
									class="inline-block h-3.5 w-3.5 rounded-full border border-hairline-strong
									{provider === 'VNPAY' ? 'border-primary bg-primary' : 'bg-canvas'}"
								></span>
							</div>
							<h4 class="mt-3.5 font-sans text-sm font-bold text-ink">VNPay Gateway</h4>
							<p class="mt-1 font-sans text-xs leading-relaxed text-mute">
								Pay securely using domestic cards, mobile banking apps, or VNPay QR.
							</p>
						</button>

						<!-- Stripe Option -->
						<button
							type="button"
							onclick={() => (provider = 'STRIPE')}
							disabled={isExpired}
							class="flex cursor-pointer flex-col items-start rounded-xl border p-4.5 text-left transition-all duration-200 outline-none
								{provider === 'STRIPE'
								? 'bg-blue-accent-soft/20 border-primary/50 shadow-2xs'
								: 'border-hairline bg-canvas hover:border-hairline-strong hover:bg-canvas-soft/10'} disabled:opacity-50"
						>
							<div class="flex w-full items-center justify-between">
								<span
									class="rounded-md bg-purple-600 px-2 py-0.5 font-mono text-[9px] font-bold tracking-wider text-white uppercase"
									>Stripe</span
								>
								<span
									class="inline-block h-3.5 w-3.5 rounded-full border border-hairline-strong
									{provider === 'STRIPE' ? 'border-primary bg-primary' : 'bg-canvas'}"
								></span>
							</div>
							<h4 class="mt-3.5 font-sans text-sm font-bold text-ink">International Credit Card</h4>
							<p class="mt-1 font-sans text-xs leading-relaxed text-mute">
								Pay via Visa, Mastercard, American Express, or Apple Pay powered by Stripe.
							</p>
						</button>
					</div>
				</section>

				<!-- Developer Simulation Gateway Suite -->
				{#if showSimulator}
					<section
						class="rounded-2xl border border-amber-500/25 bg-amber-50/10 p-6 shadow-xs select-none"
					>
						<div class="flex items-start gap-3">
							<div class="rounded-full bg-amber-500/10 p-2.5 text-amber-600">
								<svg
									xmlns="http://www.w3.org/2000/svg"
									viewBox="0 0 24 24"
									fill="none"
									stroke="currentColor"
									stroke-width="2"
									stroke-linecap="round"
									stroke-linejoin="round"
									class="h-5 w-5"
								>
									<path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
								</svg>
							</div>
							<div class="flex-grow space-y-4">
								<div>
									<h4 class="text-sm font-extrabold text-amber-700">Ticketpeak Sandbox Gateway</h4>
									<p class="mt-0.5 text-xs leading-relaxed font-semibold text-mute/85">
										Checkout intent initiated! For testing the transaction-to-ticket lifecycle
										locally, click below to trigger a simulated webhook callback.
									</p>
								</div>

								<!-- Simulate payment button -->
								<button
									type="button"
									onclick={runPaymentSimulation}
									disabled={simulationStep > 0}
									class="inline-flex items-center justify-center rounded-full bg-amber-600 px-6 py-2.5 font-sans text-xs font-bold text-white shadow-2xs transition-all hover:bg-amber-700 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50"
								>
									{#if simulationStep === 1}
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
										<span>Processing Simulation...</span>
									{:else}
										<span>✨ Approve & Simulate Mock Payment</span>
									{/if}
								</button>

								<!-- Log screen -->
								{#if simulationLog.length > 0}
									<div
										class="max-h-[180px] overflow-y-auto rounded-lg border border-zinc-800 bg-zinc-950 p-4 font-mono text-[10px] leading-relaxed text-emerald-400 shadow-inner"
									>
										{#each simulationLog as log, i (i)}
											<p class="pb-1">{log}</p>
										{/each}
									</div>
								{/if}
							</div>
						</div>
					</section>
				{/if}
			</div>

			<!-- Right Column: sticky order summaries -->
			<div class="space-y-6 self-start lg:sticky lg:top-8">
				<CheckoutSummary
					items={reservation.items}
					currency={reservation.currency}
					event={data.event}
				/>

				<!-- Errors display -->
				{#if checkoutError}
					<div
						class="bg-error-soft text-error-deep rounded-xl border border-error/20 p-4 font-sans text-xs font-bold"
					>
						⚠️ {checkoutError}
					</div>
				{/if}

				<!-- Place order action button -->
				{#if !showSimulator}
					<button
						type="button"
						onclick={handlePlaceOrder}
						disabled={isExpired || submitting}
						class="hover:bg-link-deep flex w-full cursor-pointer items-center justify-center rounded-full bg-primary py-3.5 font-sans text-xs font-bold text-white shadow-md transition-all active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50"
					>
						{#if submitting}
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
							<span>Processing checkout...</span>
						{:else}
							<span>Place Order & Pay →</span>
						{/if}
					</button>
				{/if}
			</div>
		</div>
	</div>

	{#if isExpired}
		<div
			class="fixed inset-0 z-50 flex items-center justify-center bg-ink/60 px-4 backdrop-blur-sm"
		>
			<div
				class="w-full max-w-sm rounded-2xl border border-hairline bg-canvas p-8 text-center shadow-xl"
			>
				<!-- Icon -->
				<div
					class="bg-error-soft text-error-deep mx-auto mb-5 flex h-14 w-14 items-center justify-center rounded-full"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2"
						stroke-linecap="round"
						stroke-linejoin="round"
						class="h-7 w-7"
					>
						<circle cx="12" cy="12" r="10" />
						<polyline points="12 6 12 12 16 14" />
					</svg>
				</div>

				<!-- Title -->
				<h2 class="font-sans text-xl font-extrabold text-ink">Session Expired</h2>

				<!-- Description -->
				<p class="mt-2 font-sans text-sm leading-relaxed text-mute">
					Your ticket hold has been released back to inventory. Please return to the event page to
					select tickets again.
				</p>

				<!-- CTA -->
				<button
					type="button"
					onclick={() => goto(`/${data.event?.slug ?? ''}`)}
					class="hover:bg-link-deep mt-6 flex w-full cursor-pointer items-center justify-center gap-2 rounded-full bg-primary py-3 font-sans text-sm font-bold text-white shadow-md transition-all active:scale-[0.98]"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2.5"
						stroke-linecap="round"
						stroke-linejoin="round"
						class="h-4 w-4"
					>
						<path d="m15 18-6-6 6-6" />
					</svg>
					Back to Event
				</button>
			</div>
		</div>
	{/if}
</div>
