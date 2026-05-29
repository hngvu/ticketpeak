<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { onDestroy } from 'svelte';

	let { ticket, event } = $props<{
		ticket: any;
		event: any;
	}>();

	let otp = $state<string>('');
	let windowExpiresAt = $state<number>(0);
	let secondsLeft = $state<number>(30);
	let loading = $state<boolean>(false);
	let countdownInterval: any = null;

	const eventDate = $derived(new Date(event.startAt));
	const dateString = $derived(
		eventDate.toLocaleDateString('en-US', {
			weekday: 'short',
			month: 'short',
			day: 'numeric',
			year: 'numeric'
		})
	);
	const timeString = $derived(
		eventDate.toLocaleTimeString('en-US', {
			hour: 'numeric',
			minute: '2-digit'
		})
	);

	// Derived values for secure QR payload format
	const qrPayload = $derived(otp ? `${ticket.id}:${otp}` : ticket.id);
	const qrImageUrl = $derived(
		`https://chart.googleapis.com/chart?cht=qr&chs=260x260&chl=${encodeURIComponent(qrPayload)}`
	);

	const progressPercentage = $derived(Math.min(100, Math.max(0, (secondsLeft / 30) * 100)));
	const strokeDashoffset = $derived(56.5 - (56.5 * progressPercentage) / 100);

	// Fetch dynamic OTP and expiresAt bounds
	async function loadSecureOtp() {
		if (loading) return;
		loading = true;

		try {
			if (ticket.id.startsWith('ticket-mock-')) {
				// Simulated developer mock mode
				otp = Math.floor(100000 + Math.random() * 900000).toString();
				windowExpiresAt = Math.floor(Date.now() / 1000) + 30;
			} else {
				// Secure HTTP-Only cookie proxy lookup
				const res = await fetch(`/api/tickets/${ticket.id}/qr`);
				const json = await res.json();
				if (res.ok && json.success) {
					otp = json.data.otp;
					windowExpiresAt = json.data.windowExpiresAt;
				} else {
					throw new Error(json.message);
				}
			}
		} catch (err) {
			console.warn(
				'[SECURE OTP RETRIEVAL OFFLINE]: Falling back to local rotation simulation.',
				err
			);
			// Local fallback
			otp = Math.floor(100000 + Math.random() * 900000).toString();
			windowExpiresAt = Math.floor(Date.now() / 1000) + 30;
		} finally {
			loading = false;
			updateTimer();
		}
	}

	function updateTimer() {
		const nowSeconds = Math.floor(Date.now() / 1000);
		const diff = windowExpiresAt - nowSeconds;
		secondsLeft = Math.max(0, diff);

		if (secondsLeft <= 0) {
			loadSecureOtp();
		}
	}

	// Active Effect loop
	$effect(() => {
		loadSecureOtp();

		if (countdownInterval) clearInterval(countdownInterval);
		countdownInterval = setInterval(updateTimer, 1000);

		return () => {
			if (countdownInterval) clearInterval(countdownInterval);
		};
	});

	onDestroy(() => {
		if (countdownInterval) clearInterval(countdownInterval);
	});
</script>

<div
	class="mx-auto max-w-[320px] overflow-hidden rounded-2xl border border-hairline bg-canvas font-sans shadow-xs select-none"
>
	<!-- 1. Header (Event Image & Banner info) -->
	<div class="relative h-24 overflow-hidden bg-ink">
		<img
			src={event.imageUrl ||
				'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=400&q=80'}
			alt={event.title}
			class="h-full w-full object-cover opacity-60"
		/>
		<div class="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent"></div>
		<div class="absolute right-4 bottom-2.5 left-4">
			<h4 class="truncate text-xs font-bold tracking-wider text-white uppercase">{event.title}</h4>
			<p class="mt-0.5 text-[10px] font-semibold text-zinc-300">{dateString} · {timeString}</p>
		</div>
	</div>

	<!-- 2. Secure QR Code Body Section -->
	<div class="flex flex-col items-center justify-center bg-canvas p-6">
		<div
			class="relative flex h-48 w-48 items-center justify-center rounded-xl border border-hairline bg-white p-2.5 shadow-2xs"
		>
			<img
				src={qrImageUrl}
				alt="Dynamic Secure QR Ticket"
				class="h-full w-full object-contain"
				loading="eager"
			/>

			{#if loading}
				<div class="absolute inset-0 flex items-center justify-center bg-white/70">
					<svg
						class="h-6 w-6 animate-spin text-primary"
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
				</div>
			{/if}
		</div>

		<!-- Dynamic OTP Indicator -->
		<div class="mt-4 flex items-center gap-2">
			<!-- Circular Countdown Ring -->
			<svg class="h-4.5 w-4.5 shrink-0 -rotate-90 transform select-none">
				<circle cx="9" cy="9" r="7.5" class="fill-none stroke-hairline" stroke-width="2.2" />
				<circle
					cx="9"
					cy="9"
					r="7.5"
					class="fill-none stroke-primary transition-all duration-1000"
					stroke-width="2.2"
					stroke-dasharray="47.1"
					stroke-dashoffset={(strokeDashoffset * 47.1) / 56.5}
				/>
			</svg>
			<span class="font-mono text-xs font-black tracking-wide text-ink uppercase">
				Code refreshes in {secondsLeft}s
			</span>
		</div>
	</div>

	<!-- 3. Dashed Perforation Line representation -->
	<div class="relative flex items-center justify-between bg-canvas px-3.5 py-1 select-none">
		<div
			class="absolute -left-2 h-4 w-4 rounded-full border-r border-hairline bg-canvas-soft"
		></div>
		<div class="w-full border-t-2 border-dashed border-hairline-strong/60"></div>
		<div
			class="absolute -right-2 h-4 w-4 rounded-full border-l border-hairline bg-canvas-soft"
		></div>
	</div>

	<!-- 4. Footer (Seating Specs, Entry details) -->
	<div
		class="flex flex-col items-center border-t border-hairline/20 bg-zinc-50/50 p-6 text-center font-sans text-xs select-none"
	>
		<div class="grid w-full grid-cols-2 gap-x-8 gap-y-4">
			<div class="space-y-0.5 border-r border-hairline/60 pr-4 text-left">
				<p class="text-[9px] leading-none font-extrabold tracking-widest text-mute uppercase">
					Sec/Area
				</p>
				<p class="mt-1 truncate text-xs leading-tight font-bold text-ink">{ticket.areaId}</p>
			</div>
			<div class="space-y-0.5 pl-2 text-left">
				<p class="text-[9px] leading-none font-extrabold tracking-widest text-mute uppercase">
					Seat Spec
				</p>
				<p class="mt-1 truncate text-xs leading-tight font-bold text-ink">
					{ticket.seatId || 'General Entry'}
				</p>
			</div>
			<div class="space-y-0.5 border-r border-hairline/60 pr-4 text-left">
				<p class="text-[9px] leading-none font-extrabold tracking-widest text-mute uppercase">
					Ticket Tier
				</p>
				<p class="mt-1 truncate text-xs leading-tight font-bold text-primary">{ticket.offerName}</p>
			</div>
			<div class="space-y-0.5 pl-2 text-left">
				<p class="text-[9px] leading-none font-extrabold tracking-widest text-mute uppercase">
					Status
				</p>
				<p
					class="mt-1 text-xs leading-tight font-bold tracking-wider text-emerald-600 text-ink uppercase"
				>
					{ticket.status}
				</p>
			</div>
		</div>

		<div class="mt-6 w-full border-t border-hairline/50 pt-4 text-center">
			<p class="text-[10px] leading-relaxed font-semibold text-mute">
				Scan this secure barcode at the venue entrance gate. Lanyard and credentials pick-up inside.
			</p>
		</div>
	</div>
</div>
