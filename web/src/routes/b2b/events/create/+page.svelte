<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';
	import DateTimePicker from '$lib/components/common/DateTimePicker.svelte';
	import { IconInfoCircle, IconChevronDown } from '@tabler/icons-svelte';

	let { data, form } = $props();

	// Svelte 5 states
	let selectedOrgId = $state(data.selectedOrgId);
	let loading = $state(false);

	let title = $state('');
	let slug = $state('');
	let isSlugManuallyEdited = $state(false);

	let startAt = $state('');

	// Custom Dropdowns states
	let showAttractionDropdown = $state(false);
	let showVenueDropdown = $state(false);

	let selectedClassIds = $state<string[]>(data.classifications?.map((c: any) => c.id) || []);
	let selectedAttractionIds = $state<string[]>([]);
	let selectedVenueId = $state('');

	let attractionSearchQuery = $state('');
	let venueSearchQuery = $state('');

	function cleanVietnamese(text: string): string {
		return text
			.normalize('NFD')
			.replace(/[\u0300-\u036f]/g, '')
			.replace(/đ/g, 'd')
			.replace(/Đ/g, 'd');
	}

	function slugify(text: string) {
		return cleanVietnamese(text.toString())
			.toLowerCase()
			.replace(/\s+/g, '-') // Replace spaces with -
			.replace(/[^\w-]+/g, '') // Remove all non-word chars
			.replace(/--+/g, '-') // Replace multiple - with single -
			.replace(/^-+/, '') // Trim - from start
			.replace(/-+$/, ''); // Trim - from end
	}

	const citySlugMap: Record<string, string> = {
		'ho chi minh': 'ho-chi-minh',
		'tp. ho chi minh': 'ho-chi-minh',
		tphcm: 'ho-chi-minh',
		'ha noi': 'ha-noi',
		'da nang': 'da-nang',
		'hai phong': 'hai-phong'
	};

	function getCitySlug(city: string): string {
		const clean = cleanVietnamese(city.trim().toLowerCase());
		if (citySlugMap[clean]) {
			return citySlugMap[clean];
		}
		return slugify(city);
	}

	$effect(() => {
		if (!isSlugManuallyEdited) {
			const slugParts = [];

			const titleSlug = slugify(title);
			if (titleSlug) slugParts.push(titleSlug);

			if (selectedVenue && selectedVenue.city) {
				const citySlug = getCitySlug(selectedVenue.city);
				if (citySlug) slugParts.push(citySlug);
			}

			if (startAt) {
				const parts = startAt.split('T')[0].split('-');
				if (parts.length === 3) {
					slugParts.push(`${parts[1]}-${parts[2]}-${parts[0]}`);
				}
			}

			slug = slugParts.join('-');
		}
	});

	// Derived states
	const filteredAttractions = $derived(
		data.attractions?.filter((a: any) => {
			const cleanName = cleanVietnamese(a.name.toLowerCase());
			const cleanQuery = cleanVietnamese(attractionSearchQuery.toLowerCase());
			return cleanName.includes(cleanQuery);
		}) || []
	);

	const filteredVenues = $derived(
		data.venues?.filter((v: any) => {
			const cleanName = cleanVietnamese(v.name.toLowerCase());
			const cleanCity = cleanVietnamese(v.city.toLowerCase());
			const cleanQuery = cleanVietnamese(venueSearchQuery.toLowerCase());
			return cleanName.includes(cleanQuery) || cleanCity.includes(cleanQuery);
		}) || []
	);

	const selectedVenue = $derived(data.venues?.find((v: any) => v.id === selectedVenueId));
</script>

<svelte:head>
	<title>Create New Event — Ticketpeak for Business</title>
</svelte:head>

<div class="mx-auto w-full max-w-xl flex-1 flex-col space-y-10 px-6 py-12">
	<!-- System notification banners -->
	{#if form?.error}
		<div
			class="flex items-center gap-3 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-800 shadow-xs"
		>
			<IconInfoCircle size={18} class="text-red-650 shrink-0" />
			<div class="flex flex-col">
				<span class="font-semibold">Failed to create event</span>
				<span class="mt-0.5 text-xs text-red-600/90">{form.error}</span>
			</div>
		</div>
	{/if}

	<form
		id="event-creation-form"
		method="POST"
		use:enhance={() => {
			loading = true;
			return async ({ update }) => {
				await update();
				loading = false;
			};
		}}
		class="space-y-8"
	>
		<!-- Hidden organization input for form submission -->
		<input type="hidden" name="organizationId" value={selectedOrgId} />
		<input type="hidden" name="safeTixEnabled" value="off" />
		<input type="hidden" name="restrictSingleSeat" value="off" />
		<input type="hidden" name="transferEnabled" value="on" />
		<input type="hidden" name="maxTransferCount" value="5" />

		<!-- Hidden inputs to submit multiple classificationIds via Svelte Action -->
		{#each selectedClassIds as id (id)}
			<input type="hidden" name="classificationIds" value={id} />
		{/each}

		<div class="space-y-8 rounded-xl border border-slate-200 bg-white p-8 shadow-xs">
			<!-- Field 1: Event Title -->
			<div class="space-y-1.5">
				<label for="event-title" class="block text-xs font-semibold text-slate-700">
					Event Title *
				</label>
				<input
					type="text"
					id="event-title"
					name="title"
					bind:value={title}
					placeholder="e.g. Son Tung M-TP Live Session"
					required
					class="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-normal text-slate-900 placeholder-slate-400 transition-all duration-150 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
				/>
			</div>

			<!-- Field 3: Attraction / Artists -->
			<div class="relative space-y-1.5">
				<label for="attraction-select" class="block text-xs font-semibold text-slate-700">
					Attraction
				</label>
				<div
					role="button"
					tabindex="0"
					onclick={() => (showAttractionDropdown = !showAttractionDropdown)}
					onkeydown={(e) => {
						if (e.key === 'Enter' || e.key === ' ') {
							e.preventDefault();
							showAttractionDropdown = !showAttractionDropdown;
						}
					}}
					class="flex min-h-[42px] w-full cursor-pointer items-center justify-between gap-2 rounded-lg border border-slate-200 bg-white p-2 text-left text-sm font-normal text-slate-900 transition-all duration-150 focus-within:border-blue-500 hover:border-slate-300"
				>
					<div class="flex flex-wrap gap-1.5">
						{#each selectedAttractionIds as id (id)}
							{@const artist = data.attractions.find((a: any) => a.id === id)}
							{#if artist}
								<span
									class="inline-flex items-center gap-1.5 rounded-full border border-slate-200 bg-slate-50 py-0.5 pr-2 pl-1 text-xs font-medium text-slate-800"
								>
									<img
										src={artist.imageUrl || '/placeholder-artist.jpg'}
										alt={artist.name}
										class="h-4 w-4 shrink-0 rounded-full object-cover"
									/>
									<span>{artist.name}</span>
									<button
										type="button"
										onclick={(e) => {
											e.stopPropagation();
											selectedAttractionIds = selectedAttractionIds.filter((x) => x !== id);
										}}
										class="hover:text-slate-650 ml-0.5 cursor-pointer text-[10px] font-semibold text-slate-400"
									>
										✕
									</button>
								</span>
							{/if}
						{:else}
							<span class="text-slate-400 px-1.5 text-xs">Select artists...</span>
						{/each}
					</div>
					<IconChevronDown
						size={14}
						class="mr-1 shrink-0 text-slate-400 transition-transform {showAttractionDropdown
							? 'rotate-180'
							: ''}"
					/>
				</div>

				<!-- Hidden inputs to submit multiple attractionIds via Svelte Action -->
				{#each selectedAttractionIds as id (id)}
					<input type="hidden" name="attractionIds" value={id} />
				{/each}

				{#if showAttractionDropdown}
					<button
						type="button"
						class="fixed inset-0 z-45 bg-transparent"
						onclick={() => (showAttractionDropdown = false)}
						aria-label="Close attraction dropdown"
					></button>
					<div
						class="absolute left-0 z-50 mt-1 w-full space-y-1.5 rounded-lg border border-slate-200 bg-white p-2 shadow-lg"
					>
						<!-- Search query input inside dropdown -->
						<div class="relative">
							<input
								type="text"
								placeholder="Search artists..."
								bind:value={attractionSearchQuery}
								class="text-slate-850 w-full rounded border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-xs font-normal transition-all duration-150 focus:border-blue-500 focus:bg-white focus:outline-none"
							/>
						</div>

						<div class="no-scrollbar max-h-48 space-y-0.5 overflow-y-auto">
							{#each filteredAttractions as artist (artist.id)}
								{@const isSelected = selectedAttractionIds.includes(artist.id)}
								<button
									type="button"
									onclick={() => {
										if (isSelected) {
											selectedAttractionIds = selectedAttractionIds.filter((x) => x !== artist.id);
										} else {
											selectedAttractionIds = [...selectedAttractionIds, artist.id];
										}
									}}
									class="flex w-full items-center justify-between rounded px-2 py-1 text-left text-xs font-medium transition-colors hover:bg-slate-50 {isSelected
										? 'text-blue-655 bg-slate-50 font-semibold'
										: 'text-slate-700'}"
								>
									<div class="flex items-center gap-2">
										<img
											src={artist.imageUrl || '/placeholder-artist.jpg'}
											alt={artist.name}
											class="h-5 w-5 shrink-0 rounded-full object-cover"
										/>
										<span>{artist.name}</span>
									</div>
									{#if isSelected}
										<span class="text-blue-655 text-xs font-bold">✓</span>
									{/if}
								</button>
							{:else}
								<p class="text-xs text-slate-400 py-3 text-center">No artists found.</p>
							{/each}
						</div>
					</div>
				{/if}
			</div>

			<!-- Field 4: Classification (Automatic/Read-Only) -->
			<div class="space-y-2">
				<label class="block text-xs font-semibold text-slate-700"> Classification </label>
				<div class="flex flex-wrap gap-1.5 pt-1">
					{#each data.classifications as cat (cat.id)}
						<span
							class="inline-flex items-center gap-1.5 rounded-full border border-slate-200 bg-slate-50 px-3.5 py-1 text-xs font-medium text-slate-600"
						>
							<span>{cat.name}</span>
							<span class="text-[10px] font-bold text-slate-400">✓</span>
						</span>
					{/each}
				</div>
			</div>

			<!-- Field 5: Hosting Venue -->
			<div class="relative space-y-1.5">
				<label for="venue-select" class="block text-xs font-semibold text-slate-700">
					Hosting Venue *
				</label>
				<div
					role="button"
					tabindex="0"
					onclick={() => (showVenueDropdown = !showVenueDropdown)}
					onkeydown={(e) => {
						if (e.key === 'Enter' || e.key === ' ') {
							e.preventDefault();
							showVenueDropdown = !showVenueDropdown;
						}
					}}
					class="flex min-h-[42px] w-full cursor-pointer items-center justify-between rounded-lg border border-slate-200 bg-white p-2.5 text-left text-sm font-normal text-slate-900 transition-all duration-150 hover:border-slate-300 focus:border-blue-500"
				>
					{#if selectedVenue}
						<span class="truncate text-xs font-medium text-slate-700">
							{selectedVenue.name} ({selectedVenue.city})
						</span>
					{:else}
						<span class="text-xs text-slate-400">Select Venue...</span>
					{/if}
					<IconChevronDown
						size={14}
						class="shrink-0 text-slate-400 transition-transform {showVenueDropdown
							? 'rotate-180'
							: ''}"
					/>
				</div>

				<!-- Hidden input to submit selected venueId -->
				<input type="hidden" name="venueId" value={selectedVenueId} required />

				{#if showVenueDropdown}
					<button
						type="button"
						class="fixed inset-0 z-45 bg-transparent"
						onclick={() => (showVenueDropdown = false)}
						aria-label="Close venue dropdown"
					></button>
					<div
						class="absolute left-0 z-50 mt-1 w-full space-y-1.5 rounded-lg border border-slate-200 bg-white p-1.5 shadow-lg"
					>
						<!-- Search query input inside dropdown -->
						<div class="relative">
							<input
								type="text"
								placeholder="Search venues..."
								bind:value={venueSearchQuery}
								class="text-slate-855 w-full rounded border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-xs font-normal transition-all duration-150 focus:border-blue-500 focus:bg-white focus:outline-none"
							/>
						</div>

						<div class="no-scrollbar max-h-48 space-y-0.5 overflow-y-auto">
							{#each filteredVenues as venue (venue.id)}
								{@const isSelected = selectedVenueId === venue.id}
								<button
									type="button"
									onclick={() => {
										selectedVenueId = venue.id;
										showVenueDropdown = false;
									}}
									class="flex w-full items-center justify-between rounded px-2.5 py-1.5 text-left text-xs font-medium transition-colors hover:bg-slate-50 {isSelected
										? 'text-blue-655 bg-slate-50 font-semibold'
										: 'text-slate-750'}"
								>
									<div class="flex min-w-0 flex-col">
										<span class="truncate">{venue.name}</span>
										<span class="mt-0.5 text-[9px] text-slate-400"
											>{venue.city}, {venue.countryCode}</span
										>
									</div>
									{#if isSelected}
										<span class="text-blue-655 text-xs font-bold">✓</span>
									{/if}
								</button>
							{:else}
								<p class="text-xs text-slate-400 py-3 text-center">No venues found.</p>
							{/each}
						</div>
					</div>
				{/if}
			</div>

			<!-- Field 6: Start Date -->
			<div class="space-y-1.5">
				<span class="block text-xs font-semibold text-slate-700"> Start Date * </span>
				<DateTimePicker
					name="startAt"
					required={true}
					placeholder="Select start"
					bind:value={startAt}
				/>
			</div>

			<!-- Field 7: Timezone (Automatic/Read-Only) -->
			<div class="space-y-1.5">
				<label for="event-tz" class="block text-xs font-semibold text-slate-700"> Timezone </label>
				<input
					type="text"
					id="event-tz"
					name="timezone"
					value="Asia/Ho_Chi_Minh"
					readonly
					class="border-slate-150 w-full cursor-not-allowed rounded-lg border bg-slate-50 px-3.5 py-2.5 text-sm font-medium text-slate-400 outline-none"
				/>
			</div>

			<!-- Field 2: Custom Slug (URL) -->
			<div class="space-y-1.5">
				<label for="event-slug" class="block text-xs font-semibold text-slate-700"> URL </label>
				<div class="relative flex items-center">
					<span class="absolute left-3.5 text-xs text-slate-400 select-none">ticketpeak.com/</span>
					<input
						type="text"
						id="event-slug"
						name="slug"
						bind:value={slug}
						oninput={() => (isSlugManuallyEdited = true)}
						placeholder="son-tung-live"
						class="w-full rounded-lg border border-slate-200 bg-white py-2.5 pr-3.5 pl-[104px] text-sm font-normal text-slate-900 placeholder-slate-400 transition-all duration-150 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
					/>
				</div>
			</div>
		</div>

		<!-- Action bar -->
		<div class="mt-4 flex items-center justify-end gap-3 border-t border-slate-200 pt-6">
			<a
				href="/b2b/events?organizationId={selectedOrgId}"
				class="rounded-lg border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
			>
				Cancel
			</a>
			<button
				type="submit"
				disabled={loading}
				class="flex cursor-pointer items-center justify-center gap-2 rounded-lg bg-slate-900 px-4.5 py-2 text-xs font-semibold text-white transition hover:bg-slate-800 disabled:opacity-50"
			>
				{#if loading}
					<span
						class="h-3.5 w-3.5 animate-spin rounded-full border-2 border-white border-t-transparent"
					></span>
				{/if}
				<span>Create Event</span>
			</button>
		</div>
	</form>
</div>
