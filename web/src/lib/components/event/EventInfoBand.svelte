<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	let { event, currentUser } = $props<{ event: any; currentUser: any }>();

	// Format date nicely
	const eventDate = $derived(new Date(event.startAt));
	const formattedDate = $derived(
		eventDate.toLocaleDateString('en-US', {
			weekday: 'long',
			year: 'numeric',
			month: 'long',
			day: 'numeric'
		})
	);
	const formattedTime = $derived(
		eventDate.toLocaleTimeString('en-US', {
			hour: 'numeric',
			minute: '2-digit',
			timeZoneName: 'short'
		})
	);

	let isSaved = $state(false);
	let showCopied = $state(false);

	function toggleSave() {
		isSaved = !isSaved;
	}

	async function handleShare() {
		const shareData = {
			title: event.title,
			text: `Get tickets for ${event.title} on Ticketpeak!`,
			url: window.location.href
		};

		if (navigator.share) {
			try {
				await navigator.share(shareData);
			} catch (err) {
				console.log('Share canceled or failed', err);
			}
		} else {
			try {
				await navigator.clipboard.writeText(window.location.href);
				showCopied = true;
				setTimeout(() => {
					showCopied = false;
				}, 2000);
			} catch (err) {
				console.error('Clipboard copy failed', err);
			}
		}
	}
</script>

<div class="border-b border-hairline bg-canvas select-none">
	<div
		class="mx-auto flex max-w-[1400px] flex-col justify-between gap-4 px-4 py-5 md:flex-row md:items-center md:px-6"
	>
		<!-- Left: Date/Time + Location -->
		<div class="flex flex-col gap-3 sm:flex-row sm:gap-6">
			<!-- Date & Time -->
			<div class="flex items-start gap-2.5">
				<svg
					xmlns="http://www.w3.org/2000/svg"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2"
					stroke-linecap="round"
					stroke-linejoin="round"
					class="mt-0.5 h-4.5 w-4.5 text-mute"
				>
					<rect width="18" height="18" x="3" y="4" rx="2" />
					<path d="M16 2v4M8 2v4M3 10h18" />
				</svg>
				<div class="space-y-0.5">
					<p class="text-sm font-semibold text-ink">{formattedDate}</p>
					<p class="text-xs font-medium text-mute">{formattedTime}</p>
				</div>
			</div>

			<!-- Venue details -->
			<div class="flex items-start gap-2.5">
				<svg
					xmlns="http://www.w3.org/2000/svg"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2"
					stroke-linecap="round"
					stroke-linejoin="round"
					class="mt-0.5 h-4.5 w-4.5 text-mute"
				>
					<path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z" />
					<circle cx="12" cy="10" r="3" />
				</svg>
				<div class="space-y-0.5">
					<p class="text-sm font-semibold text-ink">{event.venueName}</p>
					<p class="text-xs font-medium text-mute">{event.cityName}, VN</p>
				</div>
			</div>
		</div>

		<!-- Right: Action Buttons -->
		<div class="flex items-center gap-3">
			{#if currentUser}
				<button
					type="button"
					onclick={toggleSave}
					class="inline-flex h-9 cursor-pointer items-center gap-2 rounded-full border border-hairline bg-canvas px-4.5 text-xs font-bold text-ink shadow-2xs transition-all hover:border-hairline-strong hover:bg-canvas-soft active:scale-95"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						viewBox="0 0 24 24"
						fill={isSaved ? 'var(--color-error)' : 'none'}
						stroke={isSaved ? 'var(--color-error)' : 'currentColor'}
						stroke-width="2"
						stroke-linecap="round"
						stroke-linejoin="round"
						class="h-4 w-4 transition-colors"
					>
						<path
							d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"
						/>
					</svg>
					<span>{isSaved ? 'Saved' : 'Save'}</span>
				</button>
			{/if}

			<button
				type="button"
				onclick={handleShare}
				class="relative inline-flex h-9 cursor-pointer items-center gap-2 rounded-full border border-hairline bg-canvas px-4.5 text-xs font-bold text-ink shadow-2xs transition-all hover:border-hairline-strong hover:bg-canvas-soft active:scale-95"
			>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2"
					stroke-linecap="round"
					stroke-linejoin="round"
					class="h-4 w-4"
				>
					<path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8M16 6l-4-4-4 4M12 2v13" />
				</svg>
				<span>{showCopied ? 'Link Copied!' : 'Share'}</span>
			</button>
		</div>
	</div>
</div>
