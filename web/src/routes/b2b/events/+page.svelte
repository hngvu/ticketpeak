<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	/* eslint-disable svelte/prefer-svelte-reactivity */
	import { enhance } from '$app/forms';
	import EmptyState from '$lib/components/common/EmptyState.svelte';
	import DateTimePicker from '$lib/components/common/DateTimePicker.svelte';
	import { IconChevronDown, IconTicket, IconFolderPlus } from '@tabler/icons-svelte';

	let { data, form } = $props();

	// Svelte 5 States
	let selectedOrgId = $state(data.selectedOrgId);
	let isCloneModalOpen = $state(false);
	let cloneEventId = $state('');
	let cloneEventTitle = $state('');
	let loading = $state(false);

	// Page-level sub-navigation tabs ("individual" | "groups")
	let eventsTab = $state('individual');

	// Prominent [+ Create] dropdown state
	let showCreateDropdown = $state(false);

	// Event Group Modal States
	let isCreateGroupModalOpen = $state(false);
	let newGroupName = $state('');
	let newGroupDescription = $state('');
	let newGroupCategory = $state('Music');

	// Active list of mock event groups
	let eventGroups = $state([
		{
			id: 'group-1',
			name: 'Summer Stadium Tour 2026',
			count: 3,
			status: 'ACTIVE',
			category: 'Music'
		},
		{
			id: 'group-2',
			name: 'Unplugged Acoustic Live Sessions',
			count: 5,
			status: 'ACTIVE',
			category: 'Music'
		},
		{
			id: 'group-3',
			name: 'Asia Grand Arena Championship',
			count: 2,
			status: 'DRAFT',
			category: 'Sports'
		}
	]);

	// Create Event Modal States
	let isCreateModalOpen = $state(false);
	let selectedTemplateId = $state('');
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
				const attractionMatch = event.attractionIds ? event.attractionIds.some((attId: string) => {
					const attraction = data.attractions?.find((a: any) => a.id === attId);
					return attraction ? cleanVietnamese(attraction.name || '').toLowerCase().includes(query) : false;
				}) : false;

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

	$effect(() => {
		if (selectedTemplateId === 'concert') {
			title = 'Rock / Pop Concert Experience';
		} else if (selectedTemplateId === 'theater') {
			title = 'Classic Theater Production';
		} else if (selectedTemplateId === 'sports') {
			title = 'Championship Match Event';
		} else if (selectedTemplateId === 'festival') {
			title = 'Summer Music Festival';
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

	const templates = [
		{ id: 'temp-1', name: '1 - Chart FLO EDCEPL40 With MPE' },
		{ id: 'temp-2', name: '2 - Chart FLO EIRVPLAT With MPE' },
		{ id: 'temp-3', name: 'abcdefgh' },
		{ id: 'temp-4', name: 'ACL Template 2' },
		{ id: 'temp-5', name: 'All-in pricing in chart' },
		{ id: 'temp-6', name: 'Amanda new template' },
		{ id: 'temp-7', name: 'Anastaciya' },
		{ id: 'temp-8', name: 'Baseline NJO' },
		{ id: 'temp-9', name: 'BONJOUR' },
		{ id: 'temp-10', name: 'Brazil Bruno Mars' }
	];

	function formatDateHeader(startIso: string, endIso?: string) {
		if (!startIso) return 'APR 1';
		const startDate = new Date(startIso);
		const startMonth = startDate.toLocaleDateString('en-US', { month: 'short' }).toUpperCase();
		const startDay = startDate.getDate();

		if (endIso) {
			const endDate = new Date(endIso);
			const endMonth = endDate.toLocaleDateString('en-US', { month: 'short' }).toUpperCase();
			const endDay = endDate.getDate();
			return `${startMonth} ${startDay} - ${endMonth} ${endDay}`;
		}

		return `${startMonth} ${startDay}`;
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

	function handleCreateGroup(e: Event) {
		e.preventDefault();
		if (!newGroupName.trim()) return;
		eventGroups = [
			...eventGroups,
			{
				id: `group-${Date.now()}`,
				name: newGroupName,
				count: 0,
				status: 'DRAFT',
				category: newGroupCategory
			}
		];
		// reset
		newGroupName = '';
		newGroupDescription = '';
		newGroupCategory = 'Music';
		isCreateGroupModalOpen = false;
	}
</script>

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

	<!-- Top Header Menu with Horizontal Navigation Tabs (Left) & Dropdown Button (Right) -->
	<div class="flex items-center justify-between border-b border-slate-200">
		<!-- Horizontal Tabs (Top Left) -->
		<nav class="-mb-px flex space-x-6" aria-label="Events sub-navigation">
			<button
				type="button"
				onclick={() => (eventsTab = 'individual')}
				class="border-b-2 px-1 py-3 text-sm font-semibold transition-all duration-150 focus:outline-none {eventsTab ===
				'individual'
					? 'border-[#026CDF] font-extrabold text-[#026CDF]'
					: 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'}"
			>
				Individual
			</button>
			<button
				type="button"
				onclick={() => (eventsTab = 'groups')}
				class="border-b-2 px-1 py-3 text-sm font-semibold transition-all duration-150 focus:outline-none {eventsTab ===
				'groups'
					? 'border-[#026CDF] font-extrabold text-[#026CDF]'
					: 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'}"
			>
				Groups
			</button>
		</nav>

		<!-- prominent [+ Create] dropdown button (Top Right) -->
		<div class="relative">
			<button
				type="button"
				onclick={() => (showCreateDropdown = !showCreateDropdown)}
				class="flex cursor-pointer items-center justify-center gap-1.5 rounded-none bg-[#026CDF] px-5 py-2.5 text-xs font-bold text-white shadow-none transition hover:bg-blue-700 focus:outline-none"
			>
				<span>Create</span>
				<IconChevronDown
					size={12}
					class="transition-transform duration-150 {showCreateDropdown ? 'rotate-180' : ''}"
				/>
			</button>

			<!-- Floating Create Dropdown -->
			{#if showCreateDropdown}
				<button
					type="button"
					class="fixed inset-0 z-40 cursor-default bg-transparent"
					onclick={() => (showCreateDropdown = false)}
					aria-label="Close creation dropdown"
				></button>
				<div
					class="absolute right-0 z-50 mt-1.5 w-44 rounded-none border border-slate-200 bg-white p-1.5 shadow-xl"
				>
					<button
						type="button"
						onclick={() => {
							isCreateModalOpen = true;
							showCreateDropdown = false;
						}}
						class="flex w-full cursor-pointer items-center gap-2 rounded-none px-3 py-2 text-left text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
					>
						<IconTicket size={14} class="text-slate-400" />
						<span>Event</span>
					</button>
					<button
						type="button"
						onclick={() => {
							isCreateGroupModalOpen = true;
							showCreateDropdown = false;
						}}
						class="flex w-full cursor-pointer items-center gap-2 rounded-none px-3 py-2 text-left text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
					>
						<IconFolderPlus size={14} class="text-slate-400" />
						<span>Event Group</span>
					</button>
				</div>
			{/if}
		</div>
	</div>

	<!-- TWO-COLUMN GRID VIEW -->
	<div
		class="grid grid-cols-1 items-start gap-8 border-t border-slate-100 pt-6 lg:grid-cols-4 xl:grid-cols-5"
	>
		<!-- Left area: takes 3 columns (75% width) or 4 columns (80% width) on xl+ screens -->
		<div class="space-y-6 lg:col-span-3 lg:border-r lg:border-slate-200 lg:pr-8 xl:col-span-4">
			<!-- TAB PANEL: INDIVIDUAL EVENTS -->
			{#if eventsTab === 'individual'}
				<div class="animate-fade-in space-y-6">
					<!-- Search & Filters Row -->
					<div
						class="grid grid-cols-1 gap-4 border-b border-slate-100 pb-4 sm:grid-cols-2 lg:flex lg:flex-wrap lg:items-end xl:flex-nowrap"
					>
						<!-- Search Input -->
						<div class="space-y-1.5 sm:col-span-2 lg:min-w-[280px] lg:flex-1">
							<label for="search-input" class="block text-xs font-semibold text-slate-500"
								>Search</label
							>
							<div class="relative">
								<input
									id="search-input"
									type="text"
									placeholder="Search by event, attraction, venue and/or event ID"
									bind:value={searchQuery}
									class="w-full rounded-sm border border-slate-200 bg-white py-2 pr-4 pl-9 text-xs text-slate-800 focus:border-blue-500 focus:outline-none"
								/>
								<div class="absolute top-2.5 left-3 text-slate-400">
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
							</div>
						</div>

						<!-- Date Filter Select -->
						<div class="space-y-1.5 lg:w-44">
							<label for="date-filter" class="block text-xs font-semibold text-slate-500"
								>Date</label
							>
							<select
								id="date-filter"
								bind:value={filterDateRange}
								class="text-slate-705 w-full rounded-sm border border-slate-200 bg-white px-3 py-2 text-xs font-semibold focus:border-blue-500 focus:outline-none"
							>
								<option value="all">Upcoming Events</option>
								<option value="today">Today</option>
								<option value="this-week">Next 7 Days</option>
								<option value="this-month">Next 30 Days</option>
								<option value="custom">Custom Date...</option>
							</select>
							{#if filterDateRange === 'custom'}
								<input
									type="date"
									bind:value={customFilterDate}
									class="mt-1.5 w-full rounded-sm border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-700 focus:border-blue-500 focus:outline-none"
								/>
							{/if}
						</div>

						<!-- Venue Filter Select -->
						<div class="space-y-1.5 lg:w-44">
							<label for="venue-filter" class="block text-xs font-semibold text-slate-500"
								>Venue</label
							>
							<select
								id="venue-filter"
								bind:value={filterVenueId}
								class="text-slate-705 w-full rounded-sm border border-slate-200 bg-white px-3 py-2 text-xs font-semibold focus:border-blue-500 focus:outline-none"
							>
								<option value="all">All Venues</option>
								{#each data.venues as venue (venue.id)}
									<option value={venue.id}>{venue.name} ({venue.city})</option>
								{/each}
							</select>
						</div>

						<!-- Event Type / Classification Filter Select -->
						<div class="space-y-1.5 lg:w-44">
							<label for="type-filter" class="block text-xs font-semibold text-slate-500"
								>Event Type</label
							>
							<select
								id="type-filter"
								bind:value={filterClassId}
								class="text-slate-705 w-full rounded-sm border border-slate-200 bg-white px-3 py-2 text-xs font-semibold focus:border-blue-500 focus:outline-none"
							>
								<option value="all">All Types</option>
								<option value="concert">Concerts</option>
								{#each data.classifications as cat (cat.id)}
									{#if cat.id !== 'concert'}
										<option value={cat.id}>{cat.name}</option>
									{/if}
								{/each}
							</select>
						</div>

						<!-- View Bookmarks Checkbox (Horizontally inline) -->
						<div class="flex h-9 items-center gap-2 pb-1 lg:self-end">
							<input
								type="checkbox"
								id="bookmarks-toggle"
								class="border-slate-350 cursor-pointer rounded-sm text-blue-600 focus:ring-blue-400"
							/>
							<label
								for="bookmarks-toggle"
								class="cursor-pointer text-xs font-semibold text-slate-500 select-none"
							>
								View Bookmarks
							</label>
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
							<div class="divide-y divide-slate-100 border-t border-slate-100">
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

											<!-- Performer Circle/Avatar or placeholder -->
											<div
												class="hidden h-11 w-11 shrink-0 items-center justify-center overflow-hidden rounded-full border border-slate-200 shadow-xs sm:flex"
											>
													<!-- Camera icon placeholder inside solid grey circle -->
													<div
														class="text-slate-455 flex h-full w-full items-center justify-center bg-slate-100"
													>
														<svg
															class="h-5 w-5"
															fill="none"
															viewBox="0 0 24 24"
															stroke="currentColor"
															stroke-width="1.5"
														>
															<path
																stroke-linecap="round"
																stroke-linejoin="round"
																d="M6.827 6.175A2.31 2.31 0 015.186 7.23c-.38.054-.757.112-1.134.175C2.999 7.58 2.25 8.507 2.25 9.574V18a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9.574c0-1.067-.75-1.994-1.802-2.169a47.865 47.865 0 00-1.134-.175 2.31 2.31 0 01-1.64-1.055l-.822-1.316a2.192 2.192 0 00-1.736-1.039 48.774 48.774 0 00-5.232 0 2.192 2.192 0 00-1.736 1.039l-.821 1.316z"
															/>
															<path
																stroke-linecap="round"
																stroke-linejoin="round"
																d="M16.5 12.75a4.5 4.5 0 11-9 0 4.5 4.5 0 019 0zM18.75 10.5h.008v.008h-.008V10.5z"
															/>
														</svg>
													</div>
											</div>

											<!-- Date display (month abbreviation, day range, and year underneath) -->
											<div class="w-24 shrink-0 text-xs select-none sm:w-32">
												<div class="text-xs font-extrabold tracking-wide text-slate-900 uppercase">
													{formatDateHeader(event.startAt, event.endAt)}
												</div>
												<div class="mt-0.5 text-[10px] font-semibold text-slate-400">
													{getYear(event.startAt)}
												</div>
											</div>

											<!-- Event Details (Title & Venue/City details) -->
											<div class="min-w-0 flex-1">
												<a
													href="/b2b/events/{event.id}"
													class="block truncate text-sm leading-tight font-extrabold text-slate-900 transition-colors hover:text-blue-600"
												>
													{event.title}
												</a>
												<div class="text-slate-450 mt-0.5 truncate text-xs font-medium">
													{venue?.name} • {venue?.city}, {venue?.stateCode}
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
							<div class="border-t border-slate-100 py-12 text-center text-xs text-slate-400">
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
			{/if}

			<!-- TAB PANEL: EVENT GROUPS -->
			{#if eventsTab === 'groups'}
				<div class="animate-fade-in space-y-6">
					<div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
						{#each eventGroups as group (group.id)}
							<div
								class="relative flex flex-col justify-between rounded-sm border border-slate-200 bg-white p-5 shadow-xs transition hover:border-slate-300"
							>
								<div class="space-y-2">
									<span
										class="inline-flex items-center rounded bg-slate-100 px-2 py-0.5 text-[9px] font-bold tracking-wider text-slate-600 uppercase"
									>
										{group.category}
									</span>
									<h3 class="text-sm font-bold text-slate-900">{group.name}</h3>
									<p class="text-xs text-slate-400">
										{group.count} events connected inside this group
									</p>
								</div>

								<div class="mt-5 flex items-center justify-between border-t border-slate-100 pt-4">
									<span
										class="inline-flex items-center rounded-full px-2 py-0.5 text-[9px] font-bold uppercase {group.status ===
										'ACTIVE'
											? 'bg-emerald-50 text-emerald-700'
											: 'bg-slate-50 text-slate-500'}"
									>
										{group.status}
									</span>
									<button
										type="button"
										class="cursor-pointer border-0 bg-transparent text-xs font-bold text-slate-500 transition outline-none hover:text-slate-700"
									>
										Manage Group
									</button>
								</div>
							</div>
						{:else}
							<div
								class="col-span-3 rounded-sm border border-dashed border-slate-200 p-12 text-center text-sm text-slate-400"
							>
								No event groups created. Choose "Event Group" from the Create dropdown above to
								start a tour package.
							</div>
						{/each}
					</div>
				</div>
			{/if}
		</div>

		<!-- Right: Templates Sidebar (1/4 width on lg, 1/5 width on xl+) -->
		<div class="pl-2 lg:col-span-1 xl:col-span-1">
			<div class="space-y-4 bg-white">
				<div class="mb-4 flex items-center justify-between border-b border-slate-100 pb-2">
					<h2 class="text-sm font-bold tracking-wide text-slate-800 uppercase select-none">
						Templates
					</h2>
					<button
						type="button"
						onclick={() => (isCreateModalOpen = true)}
						class="cursor-pointer rounded-none border border-blue-600 bg-white px-3 py-1 text-[11px] font-bold text-blue-600 transition outline-none hover:bg-blue-50"
					>
						Add New
					</button>
				</div>

				<div class="mb-1 text-[10px] font-bold tracking-wider text-slate-400 uppercase">
					AMERICANAIRLINES ARENA
				</div>

				<div class="divide-y divide-slate-100 border-t border-slate-100">
					{#each templates as t (t.id)}
						<div
							class="flex w-full items-center justify-between py-3 text-left text-xs font-semibold text-slate-700 transition-colors"
						>
							<span class="truncate pr-2 font-medium">{t.name}</span>
							<button
								type="button"
								onclick={() => {
									selectedTemplateId = t.id;
									isCreateModalOpen = true;
								}}
								class="cursor-pointer border-0 bg-transparent px-1 font-bold text-slate-400 transition outline-none hover:text-slate-700"
							>
								⋮
							</button>
						</div>
					{/each}
				</div>
			</div>
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

<!-- ======================== CREATE EVENT GROUP MODAL ======================== -->
{#if isCreateGroupModalOpen}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs">
		<div class="w-full max-w-md rounded-xl bg-canvas p-6 shadow-2xl" role="dialog">
			<div class="mb-4 flex items-center justify-between border-b border-hairline pb-3">
				<h3 class="text-base font-bold text-slate-900">Create Event Group (Tour / Festival)</h3>
				<button
					onclick={() => (isCreateGroupModalOpen = false)}
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

			<form onsubmit={handleCreateGroup} class="space-y-4">
				<div class="space-y-1">
					<label for="group-name" class="block text-xs font-semibold text-slate-700"
						>Group Name *</label
					>
					<input
						type="text"
						id="group-name"
						bind:value={newGroupName}
						required
						placeholder="e.g. Summer Music Festival Tour"
						class="w-full rounded-lg border border-slate-200 px-3.5 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
					/>
				</div>

				<div class="space-y-1">
					<label for="group-category" class="block text-xs font-semibold text-slate-700"
						>Category *</label
					>
					<select
						id="group-category"
						bind:value={newGroupCategory}
						class="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
					>
						<option value="Music">Music & Concerts</option>
						<option value="Theater">Theater & Stage</option>
						<option value="Sports">Sports & Matches</option>
					</select>
				</div>

				<div class="space-y-1">
					<label for="group-desc" class="block text-xs font-semibold text-slate-700"
						>Description</label
					>
					<textarea
						id="group-desc"
						bind:value={newGroupDescription}
						rows="3"
						placeholder="Brief tour packages or festival context description..."
						class="w-full rounded-lg border border-slate-200 px-3.5 py-2 text-xs text-slate-900 focus:border-blue-500 focus:outline-none"
					></textarea>
				</div>

				<div class="flex items-center justify-end gap-2 border-t border-slate-100 pt-4">
					<button
						type="button"
						onclick={() => (isCreateGroupModalOpen = false)}
						class="rounded-full border border-0 border-slate-200 bg-white px-4 py-2 text-xs font-bold text-slate-600 outline-none hover:bg-slate-50"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="rounded-full border-0 bg-slate-900 px-5 py-2 text-xs font-bold text-white transition outline-none hover:bg-slate-800"
					>
						Create Group
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
			<div class="mb-6 flex items-center justify-between border-b border-slate-100 pb-4">
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
				<input type="hidden" name="organizationId" value={selectedOrgId} />
				<input type="hidden" name="safeTixEnabled" value="off" />
				<input type="hidden" name="restrictSingleSeat" value="off" />
				<input type="hidden" name="transferEnabled" value="on" />
				<input type="hidden" name="maxTransferCount" value="5" />

				<!-- Hidden inputs to submit multiple classificationIds via Svelte Action -->
				{#each selectedClassIds as id (id)}
					<input type="hidden" name="classificationIds" value={id} />
				{/each}

				<!-- Field 1: Select Template -->
				<div class="space-y-1.5">
					<label for="modal-template" class="block text-xs font-semibold text-slate-700">
						Select Template
					</label>
					<select
						id="modal-template"
						bind:value={selectedTemplateId}
						class="text-slate-750 w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-medium shadow-xs focus:border-blue-500 focus:outline-none"
					>
						<option value="">No Template</option>
						{#each templates as t (t.id)}
							<option value={t.id}>{t.name}</option>
						{/each}
					</select>
				</div>

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
				<div class="mt-4 border-t border-slate-100 pt-4">
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
				<div class="flex items-center justify-end gap-3 border-t border-slate-100 pt-5">
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
