<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	/* eslint-disable svelte/prefer-svelte-reactivity */
	import { page } from '$app/state';
	import { enhance } from '$app/forms';
	import EmptyState from '$lib/components/common/EmptyState.svelte';
	import DateTimePicker from '$lib/components/common/DateTimePicker.svelte';
	import { IconChevronDown, IconCalendarEvent, IconX } from '@tabler/icons-svelte';

	let { data, form } = $props();

	// Svelte 5 States
	let activeTab = $state<'individual' | 'groups'>('individual');
	let isCloneModalOpen = $state(false);
	let cloneEventId = $state('');
	let cloneEventTitle = $state('');
	let loading = $state(false);

	// Create Event Modal States
	let isCreateModalOpen = $state(false);
	let isCreateDropdownOpen = $state(false);
	let createDropdownEl = $state<HTMLDivElement | null>(null);
	let title = $state('');
	let slug = $state('');
	let isSlugManuallyEdited = $state(false);
	let selectedAttractionIds = $state<string[]>([]);
	let selectedVenueId = $state('');
	let startAt = $state('');

	// Dropdowns
	let showAttractionDropdown = $state(false);
	let showVenueDropdown = $state(false);
	let attractionSearchQuery = $state('');
	let venueSearchQuery = $state('');

	// Auto-loaded classifications
	let selectedClassIds = $state<string[]>(data.classifications?.map((c: any) => c.id) || []);

	// Catalog Search & Filter States
	let searchQuery = $state('');
	let filterVenueId = $state('all');
	let filterClassId = $state('all');
	let filterDateRange = $state('all'); // all, today, this-week, this-month, custom
	let customFilterDate = $state('');

	const allEvents = $derived(data.events || []);

	const filteredEvents = $derived(
		allEvents.filter((event: any) => {
			// 1. Search Query
			if (searchQuery.trim()) {
				const query = cleanVietnamese(searchQuery.toLowerCase());
				const titleMatch = cleanVietnamese(event.title || '')
					.toLowerCase()
					.includes(query);
				const venueObj = data.venues?.find((v: any) => v.id === event.venueId);
				const venueMatch = venueObj
					? cleanVietnamese(venueObj.name || '')
							.toLowerCase()
							.includes(query)
					: false;
				const idMatch = event.id.toLowerCase().includes(query);
				const attractionMatch = event.attractions
					? event.attractions.some((attraction: any) => {
							return cleanVietnamese(attraction.name || '')
								.toLowerCase()
								.includes(query);
						})
					: false;

				if (!titleMatch && !venueMatch && !idMatch && !attractionMatch) {
					return false;
				}
			}

			// 2. Date Filter
			if (filterDateRange !== 'all') {
				if (!event.startAt) return false;
				const eventDate = new Date(event.startAt);
				const today = new Date();
				today.setHours(0, 0, 0, 0);

				if (filterDateRange === 'today') {
					const tomorrow = new Date(today);
					tomorrow.setDate(tomorrow.getDate() + 1);
					if (eventDate < today || eventDate >= tomorrow) {
						return false;
					}
				} else if (filterDateRange === 'this-week') {
					const endOfWeek = new Date(today);
					endOfWeek.setDate(endOfWeek.getDate() + 7);
					if (eventDate < today || eventDate > endOfWeek) {
						return false;
					}
				} else if (filterDateRange === 'this-month') {
					const endOfMonth = new Date(today);
					endOfMonth.setDate(endOfMonth.getDate() + 30);
					if (eventDate < today || eventDate > endOfMonth) {
						return false;
					}
				} else if (filterDateRange === 'custom') {
					if (customFilterDate) {
						const pickDate = new Date(customFilterDate);
						pickDate.setHours(0, 0, 0, 0);
						const nextDay = new Date(pickDate);
						nextDay.setDate(nextDay.getDate() + 1);
						if (eventDate < pickDate || eventDate >= nextDay) {
							return false;
						}
					}
				}
			}

			// 3. Venue Filter
			if (filterVenueId !== 'all') {
				if (event.venueId !== filterVenueId) {
					return false;
				}
			}

			// 4. Event Type (Classification) Filter
			if (filterClassId !== 'all') {
				const hasClass =
					event.classifications && event.classifications.some((c: any) => c.id === filterClassId);
				if (!hasClass) {
					return false;
				}
			}

			return true;
		}) || []
	);

	// Vietnamese helper & Slugify
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

	const selectedVenue = $derived(data.venues?.find((v: any) => v.id === selectedVenueId));

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

	function formatDateHeader(startIso: string) {
		if (!startIso) return '1 Apr';
		const startDate = new Date(startIso);
		const month = startDate.toLocaleDateString('en-US', { month: 'short' });
		const day = startDate.getDate();
		return `${day} ${month}`;
	}

	function formatSubtitle(startIso: string, venueName: string) {
		const v = venueName || 'Capital One Arena';
		if (!startIso) return `Tue 19:00 • ${v}`;
		const date = new Date(startIso);
		const day = date.toLocaleDateString('en-US', { weekday: 'short' });
		const time = date.toLocaleTimeString('en-US', {
			hour: '2-digit',
			minute: '2-digit',
			hour12: false
		});
		return `${day} ${time} • ${v}`;
	}

	function getYear(isoString: string) {
		if (!isoString) return '2025';
		return new Date(isoString).getFullYear().toString();
	}

	function openCloneModal(eventId: string, eventTitle: string) {
		cloneEventId = eventId;
		cloneEventTitle = `Copy of ${eventTitle}`;
		isCloneModalOpen = true;
	}
</script>

<svelte:window
	onclick={(e) => {
		if (isCreateDropdownOpen && createDropdownEl && !createDropdownEl.contains(e.target as Node)) {
			isCreateDropdownOpen = false;
		}
	}}
/>

<svelte:head>
	<title>Event Management — Ticketpeak for Business</title>
</svelte:head>

<div class="flex min-h-full w-full flex-1 flex-col space-y-6 bg-white p-6">
	<!-- Status messages -->
	{#if form?.error}
		<div
			class="flex items-center gap-3 rounded-none border border-red-200 bg-red-50 p-4 text-sm font-semibold text-red-700"
		>
			<svg
				class="h-5 w-5 shrink-0 text-red-500"
				fill="none"
				viewBox="0 0 24 24"
				stroke="currentColor"
				stroke-width="2"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
				/>
			</svg>
			<span>{form.error}</span>
		</div>
	{/if}

	{#if form?.success}
		<div
			class="flex items-center gap-3 rounded-none border border-green-200 bg-green-50 p-4 text-sm font-semibold text-green-700"
		>
			<svg
				class="h-5 w-5 shrink-0 text-green-500"
				fill="none"
				viewBox="0 0 24 24"
				stroke="currentColor"
				stroke-width="2"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
				/>
			</svg>
			<span>{form.message || 'Operation successful!'}</span>
		</div>
	{/if}

	<!-- Top Header with Tabs and Create Button -->
	<div class="flex items-center justify-between">
		<!-- Tabs (underline style) -->
		<div class="flex items-center">
			<button
				type="button"
				onclick={() => (activeTab = 'individual')}
				class="cursor-pointer border-b-2 px-4 pb-3 text-xs font-semibold transition-all {activeTab ===
				'individual'
					? 'border-[#026CDF] text-[#026CDF]'
					: 'border-transparent text-slate-400 hover:text-slate-600'}"
			>
				Individual
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'groups')}
				class="cursor-pointer border-b-2 px-4 pb-3 text-xs font-semibold transition-all {activeTab ===
				'groups'
					? 'border-[#026CDF] text-[#026CDF]'
					: 'border-transparent text-slate-400 hover:text-slate-600'}"
			>
				Groups
			</button>
		</div>
		<!-- Create Button Dropdown -->
		<div class="relative" bind:this={createDropdownEl}>
			<button
				type="button"
				onclick={() => (isCreateDropdownOpen = !isCreateDropdownOpen)}
				class="flex cursor-pointer items-center justify-center gap-1.5 rounded-none bg-[#026CDF] px-5 py-2.5 text-xs font-bold text-white shadow-none transition hover:bg-blue-700 focus:outline-none"
			>
				<span>Create</span>
				<IconChevronDown
					size={14}
					stroke={2.5}
					class="transition-transform duration-200 {isCreateDropdownOpen ? 'rotate-180' : ''}"
				/>
			</button>

			{#if isCreateDropdownOpen}
				<div
					class="absolute top-full right-0 z-50 mt-1 w-48 rounded bg-white shadow-lg ring-1 ring-black/5"
				>
					<div class="p-1.5">
						<button
							type="button"
							onclick={() => {
								isCreateDropdownOpen = false;
								isCreateModalOpen = true;
							}}
							class="flex w-full cursor-pointer items-center gap-3 rounded px-3 py-2 text-left text-sm text-slate-700 hover:bg-slate-50 hover:text-slate-900"
						>
							<div
								class="flex h-8 w-8 items-center justify-center rounded bg-slate-100 text-slate-500"
							>
								<IconCalendarEvent size={18} stroke={1.5} />
							</div>
							<span class="text-[13px] font-medium">Event</span>
						</button>
						<button
							type="button"
							onclick={() => {
								isCreateDropdownOpen = false;
							}}
							class="flex w-full cursor-pointer items-center gap-3 rounded px-3 py-2 text-left text-sm text-slate-700 hover:bg-slate-50 hover:text-slate-900"
						>
							<div
								class="flex h-8 w-8 items-center justify-center rounded bg-slate-100 text-slate-500"
							>
								<svg
									class="h-[18px] w-[18px]"
									fill="none"
									viewBox="0 0 24 24"
									stroke="currentColor"
									stroke-width="1.5"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 002-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
									/>
								</svg>
							</div>
							<span class="text-[13px] font-medium">Event Group</span>
						</button>
					</div>
				</div>
			{/if}
		</div>
	</div>

	<!-- EVENTS LIST -->
	<div>
		<div class="space-y-6">
			{#if activeTab === 'individual'}
				<!-- TAB PANEL: INDIVIDUAL EVENTS -->
				<div class="space-y-6">
					<!-- Search & Filters Row (TM style) -->
					<div class="flex items-center gap-3 py-3">
						<!-- Search Input (takes most of the width) -->
						<div class="relative w-[60%] flex-none">
							<div
								class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-slate-400"
							>
								<svg
									class="h-4 w-4"
									fill="none"
									viewBox="0 0 24 24"
									stroke="currentColor"
									stroke-width="2"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
									/>
								</svg>
							</div>
							<input
								id="search-input"
								type="text"
								bind:value={searchQuery}
								placeholder="Search by event, artist, venue or event ID..."
								class="w-full rounded-md border border-slate-400 bg-white py-2 pr-4 pl-9 text-sm text-slate-800 placeholder-slate-400 focus:border-blue-500 focus:outline-none"
							/>
						</div>
						<!-- Date Filter (pill button style) -->
						<div class="relative flex items-center">
							{#if filterDateRange === 'custom'}
								<div class="flex items-center gap-2">
									<div class="w-[200px]">
										<DateTimePicker
											name="filter-date"
											placeholder="Select custom date"
											bind:value={customFilterDate}
										/>
									</div>
									<button
										type="button"
										onclick={() => {
											filterDateRange = 'all';
											customFilterDate = '';
										}}
										class="cursor-pointer text-slate-400 hover:text-slate-700"
									>
										<IconX size={16} stroke={2} />
									</button>
								</div>
							{:else}
								<div
									class="pointer-events-none absolute inset-y-0 left-2.5 flex items-center text-slate-400"
								>
									<IconCalendarEvent size={16} stroke={1.8} />
								</div>
								<select
									id="date-filter"
									bind:value={filterDateRange}
									class="cursor-pointer appearance-none rounded-md border border-slate-400 bg-white py-2 pr-8 pl-8 text-sm font-medium text-slate-700 focus:border-blue-500 focus:outline-none"
								>
									<option value="all">Upcoming Events</option>
									<option value="today">Today</option>
									<option value="this-week">Next 7 Days</option>
									<option value="this-month">Next 30 Days</option>
									<option value="custom">Custom Date...</option>
								</select>
								<div
									class="pointer-events-none absolute inset-y-0 right-2.5 flex items-center text-slate-400"
								>
									<svg
										class="h-3.5 w-3.5"
										fill="none"
										viewBox="0 0 24 24"
										stroke="currentColor"
										stroke-width="2.5"
									>
										<path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
									</svg>
								</div>
							{/if}
						</div>
					</div>

					<!-- Secondary bar only for clearing active filters -->
					{#if searchQuery || filterVenueId !== 'all' || filterClassId !== 'all' || filterDateRange !== 'all'}
						<div class="flex justify-end pt-1">
							<button
								type="button"
								onclick={() => {
									searchQuery = '';
									filterVenueId = 'all';
									filterClassId = 'all';
									filterDateRange = 'all';
									customFilterDate = '';
								}}
								class="hover:text-blue-750 cursor-pointer border-0 bg-transparent text-xs font-bold text-blue-600 transition outline-none"
							>
								Clear Filters
							</button>
						</div>
					{/if}

					{#if allEvents && allEvents.length > 0}
						<!-- Select All Row -->
						<div class="mb-2 flex items-center gap-2 py-1 text-xs font-semibold text-slate-500">
							<input
								type="checkbox"
								class="border-slate-350 cursor-pointer rounded-sm text-blue-600 focus:ring-blue-400"
							/>
							<span>Select all ({filteredEvents.length})</span>
							<span class="cursor-pointer text-slate-400 hover:text-slate-600" title="Information"
								>ⓘ</span
							>
						</div>

						{#if filteredEvents.length > 0}
							<!-- Events List Rows divided by thin lines -->
							<div class="divide-y divide-slate-300 border-t border-slate-300">
								{#each filteredEvents as event (event.id)}
									{@const venue = data.venues.find((v: any) => v.id === event.venueId)}
									<!-- svelte-ignore a11y_click_events_have_key_events -->
									<!-- svelte-ignore a11y_no_static_element_interactions -->
									<div
										onclick={(e) => {
											if (!(e.target as HTMLElement).closest('button, a, input')) {
												window.location.href = `/b2b/events/${event.id}`;
											}
										}}
										class="flex cursor-pointer items-center justify-between px-2 py-4 transition-colors hover:bg-slate-50/40"
									>
										<div class="flex min-w-0 flex-1 items-center gap-4">
											<!-- Select Checkbox -->
											<input
												type="checkbox"
												class="shrink-0 cursor-pointer rounded-sm border-slate-300 text-blue-600 focus:ring-blue-400"
											/>

											<!-- Thumbnail -->
											<div
												class="hidden h-[54px] w-[96px] shrink-0 items-center justify-center overflow-hidden rounded border border-slate-200 bg-slate-100 sm:flex"
											>
												{#if event.imageUrl}
													<img
														src={event.imageUrl}
														alt={event.title}
														class="h-full w-full object-cover"
													/>
												{:else}
													<IconCalendarEvent size={24} stroke={1.5} class="text-slate-400" />
												{/if}
											</div>

											<!-- Date display -->
											<div class="w-20 shrink-0 select-none">
												<div class="text-sm font-bold text-[#1e8d9b]">
													{formatDateHeader(event.startAt)}
												</div>
												<div class="text-[13px] text-slate-500">
													{getYear(event.startAt)}
												</div>
											</div>

											<!-- Event Details -->
											<div class="min-w-0 flex-1">
												<a
													href="/b2b/events/{event.id}"
													class="block truncate text-[15px] font-bold text-slate-900 transition-colors hover:text-blue-600"
												>
													{event.title}
												</a>
												<div class="mt-0.5 truncate text-[13px] text-slate-500">
													{formatSubtitle(event.startAt, venue?.name)}
												</div>
											</div>
										</div>

										<!-- Badges and context actions trigger -->
										<div class="ml-4 flex shrink-0 items-center gap-3">
											<!-- Contextual Badges exactly matching image -->
											<span
												class="bg-purple-650 rounded px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-white uppercase select-none"
											>
												RUN
											</span>

											{#if event.status === 'DRAFT'}
												<span
													class="rounded bg-slate-200 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-slate-600 uppercase select-none"
												>
													DRAFT
												</span>
											{:else if event.status === 'PUBLISHED'}
												<span
													class="rounded bg-slate-200 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-slate-600 uppercase select-none"
												>
													PUBLISHED
												</span>
											{:else if event.status === 'SALES_ACTIVE'}
												<span
													class="rounded bg-blue-600 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-white uppercase select-none"
												>
													VISIBLE
												</span>
											{/if}

											<!-- More Vertical Action Dots Menu trigger / Bookmark outline -->
											<div class="relative flex items-center justify-center">
												{#if event.status === 'SALES_ACTIVE'}
													<!-- Blue Bookmark Icon -->
													<button
														class="flex cursor-pointer items-center justify-center border-0 bg-transparent p-1.5 text-blue-600 transition outline-none hover:text-blue-800"
													>
														<svg
															class="h-4 w-4"
															fill="none"
															viewBox="0 0 24 24"
															stroke="currentColor"
															stroke-width="2.5"
														>
															<path
																stroke-linecap="round"
																stroke-linejoin="round"
																d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"
															/>
														</svg>
													</button>
												{:else}
													<!-- Scanner simulator shortcut -->
													<a
														href="/b2b/check-in/{event.id}"
														class="mr-1.5 inline-flex items-center gap-1 rounded-full border border-purple-200 bg-purple-50 p-1.5 text-xs font-bold text-purple-700 transition hover:bg-purple-100"
														title="Check-In Scanner"
													>
														<svg
															class="h-3.5 w-3.5"
															fill="none"
															viewBox="0 0 24 24"
															stroke="currentColor"
															stroke-width="2.5"
														>
															<path
																stroke-linecap="round"
																stroke-linejoin="round"
																d="M3.75 4.875c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5A1.125 1.125 0 013.75 9.375v-4.5zM3.75 14.625c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5a1.125 1.125 0 01-1.125-1.125v-4.5zM13.5 4.875c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5A1.125 1.125 0 0113.5 9.375v-4.5z"
															/>
														</svg>
													</a>

													<!-- Clone button shortcut -->
													<button
														onclick={() => openCloneModal(event.id, event.title)}
														class="mr-1.5 cursor-pointer rounded-full border border-hairline bg-white p-1.5 text-slate-400 transition outline-none hover:bg-slate-100 hover:text-slate-600"
														title="Clone event"
													>
														<svg
															class="h-3.5 w-3.5"
															fill="none"
															viewBox="0 0 24 24"
															stroke="currentColor"
															stroke-width="2"
														>
															<path
																stroke-linecap="round"
																stroke-linejoin="round"
																d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2"
															/>
														</svg>
													</button>

													<a
														href="/b2b/events/{event.id}"
														class="cursor-pointer border-0 bg-transparent p-1.5 text-sm font-semibold text-slate-400 transition outline-none select-none hover:text-slate-700"
														title="View details"
													>
														⋮
													</a>
												{/if}
											</div>
										</div>
									</div>
								{/each}
							</div>
						{:else}
							<div class="border-t border-slate-200 py-12 text-center text-xs text-slate-400">
								No events match your current search or filter options.
							</div>
						{/if}
					{:else}
						<EmptyState
							title="No Events Found"
							message="This organization hasn't created any events yet. Open the Create button above to configure."
							actionHref="#"
							actionText=""
						/>
					{/if}
				</div>
			{:else}
				<!-- TAB PANEL: GROUPS -->
				<div class="space-y-6 pt-4">
					<EmptyState
						title="No Group Events"
						message="Group events and season passes feature is coming soon."
						actionHref="#"
						actionText=""
					/>
				</div>
			{/if}
		</div>
	</div>
</div>

<!-- ======================== CLONE EVENT MODAL ======================== -->
{#if isCloneModalOpen}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs">
		<div class="w-full max-w-md rounded-xl bg-canvas p-6 shadow-2xl" role="dialog">
			<div class="mb-4 flex items-center justify-between border-b border-hairline pb-3">
				<h3 class="text-base font-bold text-slate-900">Clone Event</h3>
				<button
					onclick={() => (isCloneModalOpen = false)}
					class="cursor-pointer rounded-full border-0 bg-transparent p-1 text-slate-400 outline-none hover:bg-slate-100 hover:text-slate-600"
				>
					<svg
						class="h-4.5 w-4.5"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
						stroke-width="2.5"
					>
						<path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
					</svg>
				</button>
			</div>

			<form
				method="POST"
				action="?/cloneEvent"
				use:enhance={() => {
					loading = true;
					return async ({ update }) => {
						await update();
						loading = false;
						isCloneModalOpen = false;
					};
				}}
				class="space-y-4"
			>
				<input type="hidden" name="id" value={cloneEventId} />

				<!-- Cloned Title -->
				<div>
					<label
						for="clone-title"
						class="mb-1 block text-xs font-bold tracking-wider text-slate-500 uppercase"
					>
						New Event Title *
					</label>
					<input
						type="text"
						id="clone-title"
						name="title"
						required
						bind:value={cloneEventTitle}
						class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
					/>
				</div>

				<!-- New Start Date -->
				<div>
					<label
						for="clone-start"
						class="mb-1 block text-xs font-bold tracking-wider text-slate-500 uppercase"
					>
						New Start Date & Time *
					</label>
					<input
						type="datetime-local"
						id="clone-start"
						name="startAt"
						required
						class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
					/>
				</div>

				<!-- New End Date -->
				<div>
					<label
						for="clone-end"
						class="mb-1 block text-xs font-bold tracking-wider text-slate-500 uppercase"
					>
						New End Date & Time
					</label>
					<input
						type="datetime-local"
						id="clone-end"
						name="endAt"
						class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
					/>
				</div>

				<div class="flex items-center justify-end gap-2.5 pt-4">
					<button
						type="button"
						onclick={() => (isCloneModalOpen = false)}
						class="cursor-pointer rounded-full border border-0 border-hairline bg-white px-4 py-2 text-xs font-bold text-slate-600 outline-none hover:bg-slate-50"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={loading}
						class="flex cursor-pointer items-center justify-center gap-1 rounded-full border-0 bg-primary px-5 py-2 text-xs font-bold text-white transition outline-none hover:bg-primary/95 disabled:opacity-50"
					>
						{#if loading}
							<span
								class="h-3 w-3 animate-spin rounded-full border border-white border-t-transparent"
							></span>
						{/if}
						<span>Clone Now</span>
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- ======================== CREATE EVENT MODAL (Ticketmaster Inspired Minimalist) ======================== -->
{#if isCreateModalOpen}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs">
		<div
			class="no-scrollbar max-h-[90vh] w-full max-w-md overflow-y-auto rounded-xl bg-white p-8 shadow-2xl"
			role="dialog"
		>
			<div class="mb-6 flex items-center justify-between border-b border-slate-200 pb-4">
				<div>
					<h3 class="text-lg font-bold text-slate-900">Create New Event</h3>
					<p class="mt-0.5 text-xs font-normal text-slate-400">
						Fill in the fields to configure your ticketed event.
					</p>
				</div>
				<button
					onclick={() => (isCreateModalOpen = false)}
					class="cursor-pointer rounded-full border-0 bg-transparent p-1 text-slate-400 outline-none hover:bg-slate-100 hover:text-slate-600"
				>
					<svg
						class="h-5 w-5"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
						stroke-width="2.5"
					>
						<path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
					</svg>
				</button>
			</div>

			<form
				method="POST"
				action="/b2b/events/create"
				use:enhance={() => {
					loading = true;
					return async ({ update }) => {
						await update();
						loading = false;
						isCreateModalOpen = false;
					};
				}}
				class="space-y-6"
			>
				<!-- Hidden organization input for B2B API compliance -->
				<input type="hidden" name="organizationId" value={page.data.selectedOrgId} />
				<input type="hidden" name="safeTixEnabled" value="off" />
				<input type="hidden" name="restrictSingleSeat" value="off" />
				<input type="hidden" name="transferEnabled" value="on" />
				<input type="hidden" name="maxTransferCount" value="5" />

				<!-- Hidden inputs to submit multiple classificationIds via Svelte Action -->
				{#each selectedClassIds as id (id)}
					<input type="hidden" name="classificationIds" value={id} />
				{/each}

				<!-- Field 2: Event Title -->
				<div class="space-y-1.5">
					<label for="modal-title" class="block text-xs font-semibold text-slate-700">
						Event Title *
					</label>
					<input
						type="text"
						id="modal-title"
						name="title"
						bind:value={title}
						required
						placeholder="e.g. Son Tung M-TP Live Session"
						class="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-normal text-slate-900 placeholder-slate-400 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
					/>
				</div>

				<!-- Field 3: Attraction / Artists -->
				<div class="relative space-y-1.5">
					<label for="modal-attraction" class="block text-xs font-semibold text-slate-700">
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
						class="flex min-h-[42px] w-full cursor-pointer items-center justify-between gap-2 rounded-lg border border-slate-200 bg-white p-2 text-left text-sm font-normal text-slate-900 focus-within:border-blue-500 hover:border-slate-300 focus:outline-none"
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
											class="hover:text-slate-655 ml-0.5 cursor-pointer border-0 bg-transparent text-[10px] font-semibold text-slate-400 outline-none"
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
							<div class="relative">
								<input
									type="text"
									placeholder="Search artists..."
									bind:value={attractionSearchQuery}
									class="text-slate-855 w-full rounded border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-xs font-normal focus:border-blue-500 focus:bg-white focus:outline-none"
								/>
							</div>

							<div class="no-scrollbar max-h-40 space-y-0.5 overflow-y-auto">
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
										class="flex w-full cursor-pointer items-center justify-between rounded border-0 bg-transparent px-2 py-1 text-left text-xs font-medium transition-colors outline-none hover:bg-slate-50 {isSelected
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
									<p class="text-xs text-slate-400 py-2 text-center">No artists found.</p>
								{/each}
							</div>
						</div>
					{/if}

					<!-- Dynamic Auto-Populated Classification Sub-label -->
					{#if selectedAttractionIds.length > 0}
						<div class="mt-1.5 text-xs font-semibold text-slate-500 select-none">
							Classification: <span class="font-normal tracking-wide text-slate-600 uppercase"
								>Music</span
							>
						</div>
					{/if}
				</div>

				<!-- Field 4: Hosting Venue -->
				<div class="relative space-y-1.5">
					<label for="modal-venue" class="block text-xs font-semibold text-slate-700">
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
						class="flex min-h-[42px] w-full cursor-pointer items-center justify-between rounded-lg border border-slate-200 bg-white p-2.5 text-left text-sm font-normal text-slate-900 hover:border-slate-300 focus:border-blue-500 focus:outline-none"
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
							<div class="relative">
								<input
									type="text"
									placeholder="Search venues..."
									bind:value={venueSearchQuery}
									class="text-slate-855 w-full rounded border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-xs font-normal focus:border-blue-500 focus:bg-white focus:outline-none"
								/>
							</div>

							<div class="no-scrollbar max-h-40 space-y-0.5 overflow-y-auto">
								{#each filteredVenues as venue (venue.id)}
									{@const isSelected = selectedVenueId === venue.id}
									<button
										type="button"
										onclick={() => {
											selectedVenueId = venue.id;
											showVenueDropdown = false;
										}}
										class="flex w-full cursor-pointer items-center justify-between rounded border-0 bg-transparent px-2.5 py-1.5 text-left text-xs font-medium transition-colors outline-none hover:bg-slate-50 {isSelected
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
									<p class="text-xs text-slate-400 py-2 text-center">No venues found.</p>
								{/each}
							</div>
						</div>
					{/if}
				</div>

				<!-- Field 5: Start Date & Time -->
				<div class="space-y-1.5">
					<span class="block text-xs font-semibold text-slate-700"> Start Date & Time * </span>
					<DateTimePicker
						name="startAt"
						required={true}
						placeholder="Select start date & time"
						bind:value={startAt}
					/>
				</div>

				<!-- Field 6: URL Slug Preview (Minimalist clean text label at the bottom) -->
				<div class="mt-4 border-t border-slate-200 pt-4">
					<span class="mb-1 block text-[10px] font-bold tracking-wider text-slate-400 uppercase">
						Event URL Slug
					</span>
					<div class="flex items-center gap-1.5 text-xs font-semibold text-slate-600 select-all">
						<span class="text-slate-400">ticketpeak.com/event/</span>
						{#if slug}
							<span class="font-semibold text-blue-600 underline decoration-2 underline-offset-2"
								>{slug}</span
							>
						{:else}
							<span class="font-normal text-slate-400 italic">[auto-generated-slug]</span>
						{/if}
					</div>
					<input type="hidden" name="slug" value={slug} />
					<input type="hidden" name="timezone" value="Asia/Ho_Chi_Minh" />
				</div>

				<!-- Action Buttons (Bottom Right Align) -->
				<div class="flex items-center justify-end gap-3 border-t border-slate-200 pt-5">
					<button
						type="button"
						onclick={() => (isCreateModalOpen = false)}
						class="animate-none cursor-pointer rounded-full border border-0 border-slate-200 bg-white px-5 py-2 text-xs font-bold text-slate-700 transition outline-none hover:bg-slate-50"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={loading}
						class="flex animate-none cursor-pointer items-center justify-center gap-2 rounded-full border-0 bg-slate-900 px-6 py-2.5 text-xs font-bold text-white transition outline-none hover:bg-slate-800 disabled:opacity-50"
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
	</div>
{/if}
