<script lang="ts">
	let { name, description = '' } = $props<{
		name: string;
		description: string;
	}>();

	let isExpanded = $state(false);

	// Fallback dynamic biography for seeded artists if empty
	const finalBio = $derived(
		description ||
			(name.includes('Sơn Tùng')
				? 'Sơn Tùng M-TP (Nguyễn Thanh Tùng) is a Vietnamese singer-songwriter, producer, and actor. He is widely considered the "King of V-Pop" and is famous for breaking international records with hits like "Hãy Trao Cho Anh" and "Chúng Ta Của Tương Lai". Known for his unique style, energetic performances, and innovative musical production, Sơn Tùng remains the most influential pop figure in the modern Vietnamese music scene.'
				: name.includes('Đen Vâu')
					? "Đen Vâu (Nguyễn Đức Cường) is a renowned Vietnamese rapper, singer, and songwriter. Famous for his poetic lyrics, down-to-earth persona, and raw acoustic rap instrumentals, he has captured the hearts of millions of Vietnamese fans. Đen frequently uses his platform to inspire youth, donate to charity, and showcase beautiful local landscapes, making him one of the country's most respected artists."
					: name.includes('Taylor Swift')
						? 'Taylor Swift is an American singer-songwriter. A global pop sensation and cultural icon, she is recognized for her songwriting, musical versatility, and artistic reinventions. With multiple Grammy Awards, record-breaking global stadium tours like "The Eras Tour", and millions of albums sold worldwide, she is one of the most successful and critically acclaimed musicians of all time.'
						: `Welcome to the official artist profile of ${name}. Discover upcoming tour dates, tickets, and secure bookings for their upcoming live experiences here on Ticketpeak.`)
	);

	const showToggle = $derived(finalBio.length > 300);
</script>

<section class="space-y-4 select-none">
	<h3 class="font-sans text-xl font-bold tracking-tight text-ink md:text-2xl">
		About {name}
	</h3>

	<div class="space-y-4 rounded-xl border border-hairline bg-canvas p-6 shadow-xs">
		<p
			class="font-sans text-sm leading-relaxed text-body transition-all duration-300
				{!isExpanded && showToggle ? 'line-clamp-4' : ''}"
		>
			{finalBio}
		</p>

		{#if showToggle}
			<button
				type="button"
				onclick={() => (isExpanded = !isExpanded)}
				class="hover:text-link-deep inline-flex cursor-pointer items-center gap-1.5 text-xs font-bold text-primary transition-colors select-none"
			>
				<span>{isExpanded ? 'Read Less' : 'Read More'}</span>
				<svg
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2.5"
					class="h-3.5 w-3.5 transition-transform duration-200 {isExpanded ? 'rotate-180' : ''}"
				>
					<path d="m6 9 6 6 6-6" />
				</svg>
			</button>
		{/if}
	</div>
</section>
