<script lang="ts">
	let { cities, selectedLocation = $bindable() } = $props<{
		cities: string[];
		selectedLocation: string;
	}>();

	let isOpen = $state(false);

	function selectLocation(city: string) {
		selectedLocation = city;
		isOpen = false;
	}

	const displayLabel = $derived(
		selectedLocation === 'all'
			? 'All Locations'
			: cities.find((c: string) => c === selectedLocation) || 'Locations'
	);
</script>

<div class="relative inline-block text-left select-none">
	<button
		type="button"
		onclick={() => (isOpen = !isOpen)}
		class="inline-flex h-10 cursor-pointer items-center justify-between gap-2.5 rounded-lg border border-hairline bg-canvas px-4 text-xs font-bold text-slate-800 shadow-xs transition hover:bg-canvas-soft active:scale-[0.98]"
		aria-expanded={isOpen}
	>
		<span>📍 {displayLabel}</span>
		<svg
			viewBox="0 0 24 24"
			fill="none"
			stroke="currentColor"
			stroke-width="2.5"
			class="h-3.5 w-3.5 text-gray-400 transition-transform duration-200 {isOpen
				? 'rotate-180'
				: ''}"
		>
			<path d="m6 9 6 6 6-6" />
		</svg>
	</button>

	{#if isOpen}
		<!-- Backdrop for closing -->
		<button
			type="button"
			class="fixed inset-0 z-30 cursor-default bg-transparent"
			onclick={() => (isOpen = false)}
			aria-label="Close location dropdown"
		></button>

		<!-- Dropdown items -->
		<div
			class="absolute right-0 z-40 mt-2 w-52 overflow-hidden rounded-lg border border-hairline bg-canvas shadow-xl"
		>
			<div class="flex max-h-60 flex-col overflow-y-auto py-1">
				<!-- All Locations -->
				<button
					type="button"
					onclick={() => selectLocation('all')}
					class="hover:bg-blue-accent-soft flex w-full items-center justify-between px-4 py-2.5 text-left text-xs font-semibold transition-colors
						{selectedLocation === 'all' ? 'bg-blue-accent-soft/30 text-primary' : 'text-slate-700'}"
				>
					<span>All Locations</span>
					{#if selectedLocation === 'all'}
						<svg
							xmlns="http://www.w3.org/2000/svg"
							viewBox="0 0 24 24"
							fill="currentColor"
							class="h-4 w-4 text-primary"
						>
							<path
								fill-rule="evenodd"
								d="M20.25 7.5l-9 9-4.5-4.5 1.5-1.5 3 3 7.5-7.5 1.5 1.5z"
								clip-rule="evenodd"
							/>
						</svg>
					{/if}
				</button>

				<!-- City suggestions -->
				{#each cities as city (city)}
					<button
						type="button"
						onclick={() => selectLocation(city)}
						class="hover:bg-blue-accent-soft flex w-full items-center justify-between px-4 py-2.5 text-left text-xs font-semibold transition-colors
							{selectedLocation === city ? 'bg-blue-accent-soft/30 text-primary' : 'text-slate-700'}"
					>
						<span>{city}</span>
						{#if selectedLocation === city}
							<svg
								xmlns="http://www.w3.org/2000/svg"
								viewBox="0 0 24 24"
								fill="currentColor"
								class="h-4 w-4 text-primary"
							>
								<path
									fill-rule="evenodd"
									d="M20.25 7.5l-9 9-4.5-4.5 1.5-1.5 3 3 7.5-7.5 1.5 1.5z"
									clip-rule="evenodd"
								/>
							</svg>
						{/if}
					</button>
				{/each}
			</div>
		</div>
	{/if}
</div>
