<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';
	import DateTimePicker from '$lib/components/common/DateTimePicker.svelte';
	import {
		IconSpeakerphone,
		IconShieldCheck,
		IconInfoCircle,
		IconChevronDown
	} from '@tabler/icons-svelte';

	let { data, form } = $props();

	// Svelte 5 states
	let selectedOrgId = $state(data.selectedOrgId);
	let loading = $state(false);

	let startAt = $state('');

	// Policy Toggles
	let restrictSingleSeat = $state(false);
	let safeTixEnabled = $state(false);
	let transferEnabled = $state(true);

	// Custom Dropdowns states
	let showClassDropdown = $state(false);
	let showAttractionDropdown = $state(false);
	let showVenueDropdown = $state(false);

	let selectedClassIds = $state<string[]>([]);
	let selectedAttractionIds = $state<string[]>([]);
	let selectedVenueId = $state('');

	let attractionSearchQuery = $state('');
	let venueSearchQuery = $state('');

	// Derived states
	const filteredAttractions = $derived(
		data.attractions?.filter((a: any) =>
			a.name.toLowerCase().includes(attractionSearchQuery.toLowerCase())
		) || []
	);

	const filteredVenues = $derived(
		data.venues?.filter(
			(v: any) =>
				v.name.toLowerCase().includes(venueSearchQuery.toLowerCase()) ||
				v.city.toLowerCase().includes(venueSearchQuery.toLowerCase())
		) || []
	);

	const selectedVenue = $derived(data.venues?.find((v: any) => v.id === selectedVenueId));
</script>

<svelte:head>
	<title>Create New Event — Ticketpeak for Business</title>
</svelte:head>

<div class="mx-auto w-full max-w-7xl flex-1 flex-col space-y-6 p-6">
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

	<!-- Event Creation Layout (Balanced 2-Column Dashboard Grid) -->
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
		class="grid grid-cols-1 gap-6 lg:grid-cols-3"
	>
		<!-- Left Column: Core Event Details & Policies (Takes 2/3 width on desktop) -->
		<div class="space-y-6 lg:col-span-2">
			<!-- Section 1: Basic Information -->
			<div class="space-y-5 rounded-xl border border-slate-200 bg-white p-6 shadow-xs">
				<!-- Hidden organization input for form submission -->
				<input type="hidden" name="organizationId" value={selectedOrgId} />

				<!-- Title & Slug Stack -->
				<div class="space-y-5">
					<!-- Event Title -->
					<div class="space-y-1.5">
						<label for="event-title" class="block text-xs font-semibold text-slate-700">
							Event Title *
						</label>
						<input
							type="text"
							id="event-title"
							name="title"
							placeholder="e.g. Son Tung M-TP Live Session"
							required
							class="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm font-normal text-slate-900 placeholder-slate-400 transition-all duration-150 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
						/>
					</div>

					<!-- Custom Slug -->
					<div class="space-y-1.5">
						<label for="event-slug" class="block text-xs font-semibold text-slate-700"> URL </label>
						<input
							type="text"
							id="event-slug"
							name="slug"
							placeholder="son-tung-live"
							class="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm font-normal text-slate-900 placeholder-slate-400 transition-all duration-150 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
						/>
					</div>
				</div>
			</div>

			<!-- Section 2: Performers & Categorization -->
			<div class="space-y-5 rounded-xl border border-slate-200 bg-white p-6 shadow-xs">
				<div class="space-y-5">
					<!-- Classifications -->
					<div class="relative max-w-md space-y-1.5">
						<span class="block text-xs font-semibold text-slate-700"> Classification </span>
						<div
							role="button"
							tabindex="0"
							onclick={() => (showClassDropdown = !showClassDropdown)}
							onkeydown={(e) => {
								if (e.key === 'Enter' || e.key === ' ') {
									e.preventDefault();
									showClassDropdown = !showClassDropdown;
								}
							}}
							class="flex min-h-[38px] w-full cursor-pointer items-center justify-between gap-2 rounded-lg border border-slate-200 bg-white p-1.5 text-left text-sm font-normal text-slate-900 transition-all duration-150 focus-within:border-blue-500 hover:border-slate-300"
						>
							<div class="flex flex-wrap gap-1">
								{#each selectedClassIds as id (id)}
									{@const cat = data.classifications.find((c: any) => c.id === id)}
									{#if cat}
										<span
											class="inline-flex items-center gap-1 rounded border border-slate-200/50 bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-800"
										>
											{cat.name}
											<button
												type="button"
												onclick={(e) => {
													e.stopPropagation();
													selectedClassIds = selectedClassIds.filter((x) => x !== id);
												}}
												class="hover:text-slate-650 ml-0.5 cursor-pointer text-[10px] font-semibold text-slate-400"
											>
												✕
											</button>
										</span>
									{/if}
								{:else}
									<span class="text-slate-400 px-1 text-xs">Select categories...</span>
								{/each}
							</div>
							<IconChevronDown
								size={14}
								class="mr-1 shrink-0 text-slate-400 transition-transform {showClassDropdown
									? 'rotate-180'
									: ''}"
							/>
						</div>

						<!-- Hidden inputs to submit multiple classificationIds via Svelte Action -->
						{#each selectedClassIds as id (id)}
							<input type="hidden" name="classificationIds" value={id} />
						{/each}

						{#if showClassDropdown}
							<button
								type="button"
								class="fixed inset-0 z-45 bg-transparent"
								onclick={() => (showClassDropdown = false)}
								aria-label="Close classification dropdown"
							></button>
							<div
								class="absolute left-0 z-50 mt-1 max-h-60 w-full space-y-0.5 overflow-y-auto rounded-lg border border-slate-200 bg-white p-1 shadow-lg"
							>
								{#each data.classifications as cat (cat.id)}
									{@const isSelected = selectedClassIds.includes(cat.id)}
									<button
										type="button"
										onclick={() => {
											if (isSelected) {
												selectedClassIds = selectedClassIds.filter((x) => x !== cat.id);
											} else {
												selectedClassIds = [...selectedClassIds, cat.id];
											}
										}}
										class="flex w-full items-center justify-between rounded px-2.5 py-2 text-left text-xs font-medium transition-colors hover:bg-slate-50 {isSelected
											? 'text-blue-650 bg-slate-50 font-semibold'
											: 'text-slate-700'}"
									>
										<span>{cat.name}</span>
										{#if isSelected}
											<span class="text-blue-655 text-xs font-bold">✓</span>
										{/if}
									</button>
								{/each}
							</div>
						{/if}
					</div>

					<!-- Attractions / Artists -->
					<div class="relative space-y-1.5">
						<span class="block text-xs font-semibold text-slate-700"> Attraction </span>
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
							class="flex min-h-[38px] w-full cursor-pointer items-center justify-between gap-2 rounded-lg border border-slate-200 bg-white p-1.5 text-left text-sm font-normal text-slate-900 transition-all duration-150 focus-within:border-blue-500 hover:border-slate-300"
						>
							<div class="flex flex-wrap gap-1">
								{#each selectedAttractionIds as id (id)}
									{@const artist = data.attractions.find((a: any) => a.id === id)}
									{#if artist}
										<span
											class="inline-flex items-center gap-1 rounded border border-slate-200/50 bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-800"
										>
											{artist.name}
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
									<span class="text-slate-400 px-1 text-xs">Select artists...</span>
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
													selectedAttractionIds = selectedAttractionIds.filter(
														(x) => x !== artist.id
													);
												} else {
													selectedAttractionIds = [...selectedAttractionIds, artist.id];
												}
											}}
											class="flex w-full items-center justify-between rounded px-2 py-1.5 text-left text-xs font-medium transition-colors hover:bg-slate-50 {isSelected
												? 'text-blue-655 bg-slate-50 font-semibold'
												: 'text-slate-700'}"
										>
											<span>{artist.name}</span>
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
				</div>
			</div>

			<!-- Section 3: Rules & Policies -->
			<div class="space-y-5 rounded-xl border border-slate-200 bg-white p-6 shadow-xs">
				<div>
					<h2 class="text-sm font-semibold text-slate-900">Anti-Scalping & Ticket Policies</h2>
					<p class="mt-0.5 text-xs text-slate-500">
						Configure transaction safety parameters and distribution security.
					</p>
				</div>

				<div class="divide-y divide-slate-100 border-t border-slate-100">
					<!-- SafeTix Dynamic QR -->
					<div class="flex items-center justify-between py-4">
						<div class="space-y-0.5 pr-4">
							<span class="text-slate-850 flex items-center gap-1.5 text-xs font-semibold">
								<IconShieldCheck size={16} class="text-slate-450" />
								SafeTix™ Dynamic QR
							</span>
							<p class="text-xs leading-relaxed font-normal text-slate-500">
								Enforces secure rotating TOTP codes. Prevents screenshot ticket fraud and ticket
								duplication.
							</p>
						</div>
						<div class="flex shrink-0 items-center">
							<button
								type="button"
								onclick={() => (safeTixEnabled = !safeTixEnabled)}
								class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none {safeTixEnabled
									? 'bg-blue-600'
									: 'bg-slate-200'}"
								aria-label="Toggle SafeTix Dynamic QR"
							>
								<span
									class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out {safeTixEnabled
										? 'translate-x-5'
										: 'translate-x-0'}"
								></span>
							</button>
						</div>
						<input type="hidden" name="safeTixEnabled" value={safeTixEnabled ? 'on' : 'off'} />
					</div>

					<!-- Restrict Single Seat -->
					<div class="flex items-center justify-between py-4">
						<div class="space-y-0.5 pr-4">
							<span class="text-slate-850 flex items-center gap-1.5 text-xs font-semibold">
								<IconSpeakerphone size={16} class="text-slate-450" />
								Restrict Single Seats
							</span>
							<p class="text-xs leading-relaxed font-normal text-slate-500">
								Prevents ticketing selections that leave a single empty seat stranded on row
								manifests.
							</p>
						</div>
						<div class="flex shrink-0 items-center">
							<button
								type="button"
								onclick={() => (restrictSingleSeat = !restrictSingleSeat)}
								class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none {restrictSingleSeat
									? 'bg-blue-600'
									: 'bg-slate-200'}"
								aria-label="Toggle Restrict Single Seats"
							>
								<span
									class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out {restrictSingleSeat
										? 'translate-x-5'
										: 'translate-x-0'}"
								></span>
							</button>
						</div>
						<input
							type="hidden"
							name="restrictSingleSeat"
							value={restrictSingleSeat ? 'on' : 'off'}
						/>
					</div>

					<!-- Transfer Enabled Toggle -->
					<div class="flex items-center justify-between py-4">
						<div class="space-y-0.5 pr-4">
							<span class="text-slate-850 flex items-center gap-1.5 text-xs font-semibold">
								<IconShieldCheck size={16} class="text-slate-450" />
								Ticket Transferring
							</span>
							<p class="text-xs leading-relaxed font-normal text-slate-500">
								Allows original buyers to securely transfer tickets to other user accounts.
							</p>
						</div>
						<div class="flex shrink-0 items-center">
							<button
								type="button"
								onclick={() => (transferEnabled = !transferEnabled)}
								class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none {transferEnabled
									? 'bg-blue-600'
									: 'bg-slate-200'}"
								aria-label="Toggle Ticket Transferring"
							>
								<span
									class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out {transferEnabled
										? 'translate-x-5'
										: 'translate-x-0'}"
								></span>
							</button>
						</div>
						<input type="hidden" name="transferEnabled" value={transferEnabled ? 'on' : 'off'} />
					</div>
				</div>
			</div>
		</div>

		<!-- Right Column: Scheduling & Financials (Takes 1/3 width on desktop) -->
		<div class="space-y-6">
			<!-- Section 5: Schedule & Venue -->
			<div class="space-y-5 rounded-xl border border-slate-200 bg-white p-6 shadow-xs">
				<div>
					<h2 class="text-sm font-semibold text-slate-900">Schedule & Venue</h2>
					<p class="mt-0.5 text-xs text-slate-500">
						Define showtime timestamps and select host location.
					</p>
				</div>

				<!-- Venue Selector (Custom Searchable Combobox) -->
				<div class="relative space-y-1.5">
					<span class="block text-xs font-semibold text-slate-700"> Hosting Venue * </span>
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
						class="flex w-full cursor-pointer items-center justify-between rounded-lg border border-slate-200 bg-white p-2.5 text-left text-sm font-normal text-slate-900 transition-all duration-150 hover:border-slate-300 focus:border-blue-500"
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

				<!-- Timezone block -->
				<div class="space-y-1.5">
					<label for="event-tz" class="block text-xs font-semibold text-slate-700">
						Timezone
					</label>
					<input
						type="text"
						id="event-tz"
						name="timezone"
						value="Asia/Ho_Chi_Minh"
						readonly
						class="border-slate-150 text-slate-450 w-full cursor-not-allowed rounded-lg border bg-slate-50 px-3 py-2 text-sm font-medium outline-none"
					/>
				</div>

				<!-- Divider -->
				<div class="border-t border-slate-100"></div>

				<!-- Showtimes -->
				<div class="space-y-1.5">
					<span class="block text-xs font-semibold text-slate-700"> Start Date * </span>
					<DateTimePicker
						name="startAt"
						required={true}
						placeholder="Select start"
						bind:value={startAt}
					/>
				</div>
			</div>

			<!-- Section 6: Fees & Limits -->
			<div class="space-y-5 rounded-xl border border-slate-200 bg-white p-6 shadow-xs">
				<div>
					<h2 class="text-sm font-semibold text-slate-900">Fees & Limits</h2>
					<p class="mt-0.5 text-xs text-slate-500">
						Configure ticket pricing commissions and transaction boundaries.
					</p>
				</div>

				<div>
					<!-- Max Transfer Limit (Conditional on Transfer Enabled) -->
					{#if transferEnabled}
						<div class="space-y-1.5">
							<label for="max-transfers" class="block text-xs font-semibold text-slate-700">
								Max Transfers
							</label>
							<input
								type="number"
								min="1"
								id="max-transfers"
								name="maxTransferCount"
								value="5"
								class="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm font-normal text-slate-900 transition-all duration-150 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
							/>
						</div>
					{/if}
				</div>
			</div>
		</div>

		<!-- Action bar at the bottom, spanning across the 3 columns of the grid -->
		<div
			class="mt-2 flex items-center justify-end gap-3 border-t border-slate-200 pt-6 lg:col-span-3"
		>
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
