<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { onDestroy } from 'svelte';

	let { expiresAt, onExpire } = $props<{
		expiresAt: string;
		onExpire?: () => void;
	}>();

	let timeLeftSeconds = $state(0);
	let timerInterval: any = null;

	const formattedTime = $derived.by(() => {
		if (timeLeftSeconds <= 0) return '00:00';
		const m = Math.floor(timeLeftSeconds / 60);
		const s = timeLeftSeconds % 60;
		return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
	});

	const isCritical = $derived(timeLeftSeconds > 0 && timeLeftSeconds < 120); // Less than 2 minutes
	const isExpired = $derived(timeLeftSeconds <= 0);

	// Start the countdown timer
	$effect(() => {
		// Clean up existing interval
		if (timerInterval) clearInterval(timerInterval);

		const targetTime = new Date(expiresAt).getTime();

		function updateTimer() {
			const now = Date.now();
			const diffMs = targetTime - now;
			const diffSeconds = Math.max(0, Math.floor(diffMs / 1000));

			timeLeftSeconds = diffSeconds;

			if (diffSeconds <= 0) {
				clearInterval(timerInterval);
				if (onExpire) onExpire();
			}
		}

		updateTimer();
		timerInterval = setInterval(updateTimer, 1000);

		return () => {
			if (timerInterval) clearInterval(timerInterval);
		};
	});

	onDestroy(() => {
		if (timerInterval) clearInterval(timerInterval);
	});
</script>

<div class="select-none">
	{#if isExpired}
		<div
			class="bg-error-soft text-error-deep flex animate-pulse items-center gap-3 rounded-xl border border-error/20 p-4"
		>
			<svg
				xmlns="http://www.w3.org/2000/svg"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2"
				stroke-linecap="round"
				stroke-linejoin="round"
				class="h-5 w-5 shrink-0"
			>
				<circle cx="12" cy="12" r="10" />
				<line x1="12" x2="12" y1="8" y2="12" />
				<line x1="12" x2="12.01" y1="16" y2="16" />
			</svg>
			<div>
				<h4 class="text-sm font-bold">Hold Time Expired</h4>
				<p class="text-error-deep/80 mt-0.5 text-xs leading-relaxed font-semibold">
					Your tickets have been released back to the general inventory. Please return to the event
					page to select tickets again.
				</p>
			</div>
		</div>
	{:else}
		<div
			class="flex items-center justify-between rounded-xl border p-4 transition-colors duration-300
				{isCritical
				? 'bg-error-soft/30 text-error-deep border-error/25'
				: 'bg-warning-soft/30 text-warning-deep border-warning/25'}"
		>
			<div class="flex items-center gap-3">
				<svg
					xmlns="http://www.w3.org/2000/svg"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2.2"
					stroke-linecap="round"
					stroke-linejoin="round"
					class="animate-spin-slow h-5 w-5 shrink-0"
				>
					<circle cx="12" cy="12" r="10" />
					<polyline points="12 6 12 12 16 14" />
				</svg>
				<div>
					<h4 class="text-sm font-bold text-ink">We're holding your tickets</h4>
					<p class="mt-0.5 text-xs font-medium text-mute">
						Complete checkout before the countdown timer expires to secure your seats.
					</p>
				</div>
			</div>

			<!-- Large dynamic timer -->
			<div class="shrink-0 text-right">
				<span
					class="font-mono text-xl font-black tracking-tight sm:text-2xl
						{isCritical ? 'text-error-deep animate-pulse' : 'text-primary'}"
				>
					{formattedTime}
				</span>
			</div>
		</div>
	{/if}
</div>
