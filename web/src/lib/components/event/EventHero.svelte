<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	let { event } = $props<{ event: any }>();

	const fallbackImage =
		'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=1200&q=80';
</script>

<section class="relative h-[260px] overflow-hidden bg-ink select-none sm:h-[340px] md:h-[400px]">
	<!-- Background image -->
	<img
		src={event.imageUrl || fallbackImage}
		alt={event.title}
		class="absolute inset-0 h-full w-full object-cover object-center"
		loading="eager"
	/>

	<!-- Gradient overlay -->
	<div class="absolute inset-0 bg-gradient-to-t from-black/85 via-black/30 to-black/5"></div>

	<!-- Content overlaid bottom-left -->
	<div class="relative mx-auto flex h-full max-w-[1400px] flex-col justify-end px-4 pb-8 md:px-6">
		<div class="mb-3 flex flex-wrap gap-2">
			{#if event.classifications}
				{#each event.classifications as cls (cls.id)}
					<span
						class="rounded-full bg-blue-accent-soft px-3 py-1 font-sans text-xs font-bold tracking-wider text-blue-accent uppercase"
					>
						{cls.name}
					</span>
				{/each}
			{/if}
		</div>
		<h1
			class="max-w-3xl font-sans text-2xl leading-tight font-bold tracking-tight text-white md:text-3xl lg:text-4xl"
		>
			{event.title}
		</h1>
	</div>
</section>
