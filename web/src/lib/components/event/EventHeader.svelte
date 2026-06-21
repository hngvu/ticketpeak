<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	let { event } = $props<{ event: any }>();

	const eventDate = $derived(new Date(event.startAt));
	const formattedDate = $derived(
		eventDate.toLocaleDateString('en-US', {
			weekday: 'short',
			month: 'short',
			day: 'numeric',
			year: 'numeric'
		})
	);
	const formattedTime = $derived(
		eventDate.toLocaleTimeString('en-US', {
			hour: 'numeric',
			minute: '2-digit'
		})
	);
</script>

<header class="bg-ink text-white">
	<div class="mx-auto flex max-w-[1600px] items-center gap-4 px-4 py-3 md:px-6">
		<a href="/" class="shrink-0 text-sm font-bold text-white/80 transition-colors hover:text-white">
			Home
		</a>
		<span class="text-white/40">/</span>
		<span class="truncate text-sm text-white/60">{event.title}</span>
	</div>

	<div class="border-t border-white/10">
		<div class="mx-auto flex max-w-[1600px] items-center gap-4 px-4 py-3 md:px-6">
			{#if event.imageUrl}
				<img
					src={event.imageUrl}
					alt={event.title}
					class="h-14 w-14 shrink-0 rounded-md object-cover md:h-16 md:w-16"
				/>
			{/if}

			<div class="min-w-0 flex-1">
				<h1 class="truncate text-base font-bold text-white md:text-lg">{event.title}</h1>
				<div class="mt-0.5 flex flex-wrap items-center gap-x-3 gap-y-0.5 text-xs text-white/70">
					<span>{formattedDate}, {formattedTime}</span>
					<span class="text-white/40">•</span>
					<span class="text-white/90">{event.venueName}</span>
					{#if event.cityName}
						<span class="text-white/50">, {event.cityName}</span>
					{/if}
				</div>
			</div>

			{#if event.classifications && event.classifications.length > 0}
				<div class="hidden items-center gap-2 md:flex">
					{#each event.classifications.slice(0, 3) as cls}
						<span
							class="rounded-full border border-white/15 bg-white/10 px-2.5 py-0.5 text-[10px] font-bold tracking-wider text-white/80 uppercase backdrop-blur-sm"
						>
							{cls.name}
						</span>
					{/each}
				</div>
			{/if}
		</div>
	</div>
</header>
