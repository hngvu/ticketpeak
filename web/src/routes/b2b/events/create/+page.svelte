<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';
	import DateTimePicker from '$lib/components/common/DateTimePicker.svelte';
	import {
		IconSpeakerphone,
		IconShieldCheck,
		IconInfoCircle,
		IconCalendarEvent,
		IconChevronDown,
		IconArrowLeft
	} from '@tabler/icons-svelte';

	let { data, form } = $props();

	// Svelte 5 states
	let selectedOrgId = $state(data.selectedOrgId);
	let loading = $state(false);

	let startAt = $state('');
	let endAt = $state('');
	let saleStartAt = $state('');
	let saleEndAt = $state('');

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
				<span class="text-xs text-red-600/90 mt-0.5">{form.error}</span>
			</div>
		</div>
	{/if}

	<!-- Section Header (Clean Enterprise SaaS style) -->
	<div class="border-b border-slate-200 pb-5">
		<h1 class="text-xl font-bold tracking-tight text-slate-900">
			Create Event
		</h1>
		<p class="text-xs text-slate-500 font-normal mt-1 leading-relaxed">
			Configure the show details, time schedules, hosting venues, and ticket transfer parameters.
		</p>
	</div>

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
			<div class="rounded-xl border border-slate-200 bg-white p-6 shadow-xs space-y-5">
				<div>
					<h2 class="text-sm font-semibold text-slate-900">
						Basic Details
					</h2>
					<p class="text-xs text-slate-500 mt-0.5">Primary identification parameters for this event showcase.</p>
				</div>

				<!-- Hidden organization input for form submission -->
				<input type="hidden" name="organizationId" value={selectedOrgId} />

				<!-- Title & Slug Grid -->
				<div class="grid grid-cols-1 gap-5 sm:grid-cols-2">
					<!-- Event Title -->
					<div class="space-y-1.5">
						<label
							for="event-title"
							class="block text-xs font-semibold text-slate-700"
						>
							Event Title *
						</label>
						<input
							type="text"
							id="event-title"
							name="title"
							placeholder="e.g. Son Tung M-TP Live Session"
							required
							class="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm font-normal text-slate-900 placeholder-slate-400 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none transition-all duration-150"
						/>
					</div>

					<!-- Custom Slug -->
					<div class="space-y-1.5">
						<label
							for="event-slug"
							class="block text-xs font-semibold text-slate-700"
						>
							Custom URL Slug
						</label>
						<div class="relative flex rounded-lg">
							<span class="inline-flex items-center rounded-l-lg border border-r-0 border-slate-200 bg-slate-50 px-3 text-xs font-medium text-slate-450">
								/event/
							</span>
							<input
								type="text"
								id="event-slug"
								name="slug"
								placeholder="son-tung-live"
								class="w-full rounded-r-lg border border-slate-200 bg-white px-3 py-2 text-sm font-normal text-slate-900 placeholder-slate-400 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none transition-all duration-150"
							/>
						</div>
						<span class="block text-[10px] text-slate-400">
							Optional. Left blank to auto-generate slug.
						</span>
					</div>
				</div>
			</div>

			<!-- Section 2: Performers & Categorization -->
			<div class="rounded-xl border border-slate-200 bg-white p-6 shadow-xs space-y-5">
				<div>
					<h2 class="text-sm font-semibold text-slate-900">
						Performers & Genres
					</h2>
					<p class="text-xs text-slate-500 mt-0.5">Tag genres, performers, or artists featured in this showcase.</p>
				</div>

				<div class="grid grid-cols-1 gap-5 sm:grid-cols-2">
					<!-- Classifications -->
					<div class="space-y-1.5 relative">
						<span class="block text-xs font-semibold text-slate-700">
							Classification Categories
						</span>
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
							class="flex min-h-[38px] w-full items-center justify-between gap-2 rounded-lg border border-slate-200 bg-white p-1.5 text-left text-sm font-normal text-slate-900 hover:border-slate-300 focus-within:border-blue-500 cursor-pointer transition-all duration-150"
						>
							<div class="flex flex-wrap gap-1">
								{#each selectedClassIds as id}
									{@const cat = data.classifications.find((c: any) => c.id === id)}
									{#if cat}
										<span
											class="inline-flex items-center gap-1 rounded bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-800 border border-slate-200/50"
										>
											{cat.name}
											<button
												type="button"
												onclick={(e) => {
													e.stopPropagation();
													selectedClassIds = selectedClassIds.filter((x) => x !== id);
												}}
												class="text-slate-400 hover:text-slate-650 font-semibold text-[10px] ml-0.5 cursor-pointer"
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
								class="text-slate-400 mr-1 shrink-0 transition-transform {showClassDropdown
									? 'rotate-180'
									: ''}"
							/>
						</div>

						<!-- Hidden inputs to submit multiple classificationIds via Svelte Action -->
						{#each selectedClassIds as id}
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
								class="absolute left-0 z-50 mt-1 w-full max-h-60 overflow-y-auto rounded-lg border border-slate-200 bg-white p-1 shadow-lg space-y-0.5"
							>
								{#each data.classifications as cat}
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
											? 'bg-slate-50 text-blue-650 font-semibold'
											: 'text-slate-700'}"
									>
										<span>{cat.name}</span>
										{#if isSelected}
											<span class="text-blue-655 font-bold text-xs">✓</span>
										{/if}
									</button>
								{/each}
							</div>
						{/if}
					</div>

					<!-- Attractions / Artists -->
					<div class="space-y-1.5 relative">
						<span class="block text-xs font-semibold text-slate-700">
							Featured Performers / Artists
						</span>
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
							class="flex min-h-[38px] w-full items-center justify-between gap-2 rounded-lg border border-slate-200 bg-white p-1.5 text-left text-sm font-normal text-slate-900 hover:border-slate-300 focus-within:border-blue-500 cursor-pointer transition-all duration-150"
						>
							<div class="flex flex-wrap gap-1">
								{#each selectedAttractionIds as id}
									{@const artist = data.attractions.find((a: any) => a.id === id)}
									{#if artist}
										<span
											class="inline-flex items-center gap-1 rounded bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-800 border border-slate-200/50"
										>
											{artist.name}
											<button
												type="button"
												onclick={(e) => {
													e.stopPropagation();
													selectedAttractionIds = selectedAttractionIds.filter(
														(x) => x !== id
													);
												}}
												class="text-slate-400 hover:text-slate-650 font-semibold text-[10px] ml-0.5 cursor-pointer"
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
								class="text-slate-400 mr-1 shrink-0 transition-transform {showAttractionDropdown
									? 'rotate-180'
									: ''}"
							/>
						</div>

						<!-- Hidden inputs to submit multiple attractionIds via Svelte Action -->
						{#each selectedAttractionIds as id}
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
								class="absolute left-0 z-50 mt-1 w-full rounded-lg border border-slate-200 bg-white p-2 shadow-lg space-y-1.5"
							>
								<!-- Search query input inside dropdown -->
								<div class="relative">
									<input
										type="text"
										placeholder="Search artists..."
										bind:value={attractionSearchQuery}
										class="w-full rounded border border-slate-200 bg-slate-50 py-1.5 px-2.5 text-xs font-normal text-slate-850 focus:border-blue-500 focus:bg-white focus:outline-none transition-all duration-150"
									/>
								</div>

								<div class="no-scrollbar max-h-48 overflow-y-auto space-y-0.5">
									{#each filteredAttractions as artist}
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
												? 'bg-slate-50 text-blue-655 font-semibold'
												: 'text-slate-700'}"
										>
											<span>{artist.name}</span>
											{#if isSelected}
												<span class="text-blue-655 font-bold text-xs">✓</span>
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
			<div class="rounded-xl border border-slate-200 bg-white p-6 shadow-xs space-y-5">
				<div>
					<h2 class="text-sm font-semibold text-slate-900">
						Anti-Scalping & Ticket Policies
					</h2>
					<p class="text-xs text-slate-500 mt-0.5">Configure transaction safety parameters and distribution security.</p>
				</div>

				<div class="divide-y divide-slate-100 border-t border-slate-100">
					<!-- SafeTix Dynamic QR -->
					<div class="flex items-center justify-between py-4">
						<div class="space-y-0.5 pr-4">
							<span class="text-xs font-semibold text-slate-850 flex items-center gap-1.5">
								<IconShieldCheck size={16} class="text-slate-450" />
								SafeTix™ Dynamic QR
							</span>
							<p class="text-xs text-slate-500 font-normal leading-relaxed">
								Enforces secure rotating TOTP codes. Prevents screenshot ticket fraud and ticket duplication.
							</p>
						</div>
						<div class="flex items-center shrink-0">
							<button
								type="button"
								onclick={() => (safeTixEnabled = !safeTixEnabled)}
								class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none {safeTixEnabled ? 'bg-blue-600' : 'bg-slate-200'}"
								aria-label="Toggle SafeTix Dynamic QR"
							>
								<span class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out {safeTixEnabled ? 'translate-x-5' : 'translate-x-0'}"></span>
							</button>
						</div>
						<input type="hidden" name="safeTixEnabled" value={safeTixEnabled ? 'on' : 'off'} />
					</div>

					<!-- Restrict Single Seat -->
					<div class="flex items-center justify-between py-4">
						<div class="space-y-0.5 pr-4">
							<span class="text-xs font-semibold text-slate-850 flex items-center gap-1.5">
								<IconSpeakerphone size={16} class="text-slate-450" />
								Restrict Single Seats
							</span>
							<p class="text-xs text-slate-500 font-normal leading-relaxed">
								Prevents ticketing selections that leave a single empty seat stranded on row manifests.
							</p>
						</div>
						<div class="flex items-center shrink-0">
							<button
								type="button"
								onclick={() => (restrictSingleSeat = !restrictSingleSeat)}
								class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none {restrictSingleSeat ? 'bg-blue-600' : 'bg-slate-200'}"
								aria-label="Toggle Restrict Single Seats"
							>
								<span class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out {restrictSingleSeat ? 'translate-x-5' : 'translate-x-0'}"></span>
							</button>
						</div>
						<input type="hidden" name="restrictSingleSeat" value={restrictSingleSeat ? 'on' : 'off'} />
					</div>

					<!-- Transfer Enabled Toggle -->
					<div class="flex items-center justify-between py-4">
						<div class="space-y-0.5 pr-4">
							<span class="text-xs font-semibold text-slate-850 flex items-center gap-1.5">
								<IconShieldCheck size={16} class="text-slate-450" />
								Ticket Transferring
							</span>
							<p class="text-xs text-slate-500 font-normal leading-relaxed">
								Allows original buyers to securely transfer tickets to other user accounts.
							</p>
						</div>
						<div class="flex items-center shrink-0">
							<button
								type="button"
								onclick={() => (transferEnabled = !transferEnabled)}
								class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none {transferEnabled ? 'bg-blue-600' : 'bg-slate-200'}"
								aria-label="Toggle Ticket Transferring"
							>
								<span class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out {transferEnabled ? 'translate-x-5' : 'translate-x-0'}"></span>
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
			<div class="rounded-xl border border-slate-200 bg-white p-6 shadow-xs space-y-5">
				<div>
					<h2 class="text-sm font-semibold text-slate-900">
						Schedule & Venue
					</h2>
					<p class="text-xs text-slate-500 mt-0.5">Define showtime timestamps and select host location.</p>
				</div>

				<!-- Venue Selector (Custom Searchable Combobox) -->
				<div class="space-y-1.5 relative">
					<span class="block text-xs font-semibold text-slate-700">
						Hosting Venue *
					</span>
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
						class="flex w-full items-center justify-between rounded-lg border border-slate-200 bg-white p-2.5 text-left text-sm font-normal text-slate-900 hover:border-slate-300 focus:border-blue-500 cursor-pointer transition-all duration-150"
					>
						{#if selectedVenue}
							<span class="truncate text-xs font-medium text-slate-700">
								{selectedVenue.name} ({selectedVenue.city})
							</span>
						{:else}
							<span class="text-slate-400 text-xs">Select Venue...</span>
						{/if}
						<IconChevronDown
							size={14}
							class="text-slate-400 shrink-0 transition-transform {showVenueDropdown
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
							class="absolute left-0 z-50 mt-1 w-full rounded-lg border border-slate-200 bg-white p-1.5 shadow-lg space-y-1.5"
						>
							<!-- Search query input inside dropdown -->
							<div class="relative">
								<input
									type="text"
									placeholder="Search venues..."
									bind:value={venueSearchQuery}
									class="w-full rounded border border-slate-200 bg-slate-50 py-1.5 px-2.5 text-xs font-normal text-slate-855 focus:border-blue-500 focus:bg-white focus:outline-none transition-all duration-150"
								/>
							</div>

							<div class="no-scrollbar max-h-48 overflow-y-auto space-y-0.5">
								{#each filteredVenues as venue}
									{@const isSelected = selectedVenueId === venue.id}
									<button
										type="button"
										onclick={() => {
											selectedVenueId = venue.id;
											showVenueDropdown = false;
										}}
										class="flex w-full items-center justify-between rounded px-2.5 py-1.5 text-left text-xs font-medium transition-colors hover:bg-slate-50 {isSelected
											? 'bg-slate-50 text-blue-655 font-semibold'
											: 'text-slate-750'}"
									>
										<div class="flex flex-col min-w-0">
											<span class="truncate">{venue.name}</span>
											<span class="text-[9px] text-slate-400 mt-0.5"
												>{venue.city}, {venue.countryCode}</span
											>
										</div>
										{#if isSelected}
											<span class="text-blue-655 font-bold text-xs">✓</span>
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
					<label
						for="event-tz"
						class="block text-xs font-semibold text-slate-700"
					>
						Timezone
					</label>
					<input
						type="text"
						id="event-tz"
						name="timezone"
						value="Asia/Ho_Chi_Minh"
						readonly
						class="w-full cursor-not-allowed rounded-lg border border-slate-150 bg-slate-50 px-3 py-2 text-sm font-medium text-slate-450 outline-none"
					/>
				</div>

				<!-- Divider -->
				<div class="border-t border-slate-100"></div>

				<!-- Showtimes -->
				<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
					<div class="space-y-1.5">
						<span class="block text-xs font-semibold text-slate-700">
							Start Date *
						</span>
						<DateTimePicker
							name="startAt"
							required={true}
							placeholder="Select start"
							bind:value={startAt}
						/>
					</div>

					<div class="space-y-1.5">
						<span class="block text-xs font-semibold text-slate-700">
							End Date
						</span>
						<DateTimePicker
							name="endAt"
							required={false}
							placeholder="Select end"
							bind:value={endAt}
						/>
					</div>
				</div>

				<!-- Divider -->
				<div class="border-t border-slate-100"></div>

				<!-- Sales Launch Windows -->
				<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
					<div class="space-y-1.5">
						<span class="block text-xs font-semibold text-slate-700">
							Sales Launch
						</span>
						<DateTimePicker
							name="saleStartAt"
							required={false}
							placeholder="Select launch"
							bind:value={saleStartAt}
						/>
						<span class="block text-[10px] text-slate-400 mt-1 leading-normal">
							Defaults to immediate launch.
						</span>
					</div>

					<div class="space-y-1.5">
						<span class="block text-xs font-semibold text-slate-700">
							Sales Close
						</span>
						<DateTimePicker
							name="saleEndAt"
							required={false}
							placeholder="Select closure"
							bind:value={saleEndAt}
						/>
						<span class="block text-[10px] text-slate-400 mt-1 leading-normal">
							Defaults to show start time.
						</span>
					</div>
				</div>
			</div>

			<!-- Section 6: Fees & Limits -->
			<div class="rounded-xl border border-slate-200 bg-white p-6 shadow-xs space-y-5">
				<div>
					<h2 class="text-sm font-semibold text-slate-900">
						Fees & Limits
					</h2>
					<p class="text-xs text-slate-500 mt-0.5">Configure ticket pricing commissions and transaction boundaries.</p>
				</div>

				<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
					<!-- Service Fee Snapshot -->
					<div class="space-y-1.5">
						<label
							for="service-fee"
							class="block text-xs font-semibold text-slate-700"
						>
							Service Fee (%)
						</label>
						<div class="relative rounded-lg shadow-2xs">
							<input
								type="number"
								step="0.01"
								min="0"
								max="100"
								id="service-fee"
								name="serviceFeePercent"
								placeholder="0.00"
								class="w-full rounded-lg border border-slate-200 bg-white py-2 px-3 text-sm font-normal text-slate-900 placeholder-slate-400 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none transition-all duration-150"
							/>
							<div class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
								<span class="text-xs font-semibold text-slate-400">%</span>
							</div>
						</div>
					</div>

					<!-- Max Transfer Limit (Conditional on Transfer Enabled) -->
					{#if transferEnabled}
						<div class="space-y-1.5">
							<label
								for="max-transfers"
								class="block text-xs font-semibold text-slate-700"
							>
								Max Transfers
							</label>
							<input
								type="number"
								min="1"
								id="max-transfers"
								name="maxTransferCount"
								value="5"
								class="w-full rounded-lg border border-slate-200 bg-white py-2 px-3 text-sm font-normal text-slate-900 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none transition-all duration-150"
							/>
						</div>
					{/if}
				</div>
			</div>
		</div>

		<!-- Action bar at the bottom, spanning across the 3 columns of the grid -->
		<div class="lg:col-span-3 flex items-center justify-end gap-3 border-t border-slate-200 pt-6 mt-2">
			<a
				href="/b2b/events?organizationId={selectedOrgId}"
				class="rounded-lg border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition"
			>
				Cancel
			</a>
			<button
				type="submit"
				disabled={loading}
				class="flex cursor-pointer items-center justify-center gap-2 rounded-lg bg-slate-900 px-4.5 py-2 text-xs font-semibold text-white hover:bg-slate-800 transition disabled:opacity-50"
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
