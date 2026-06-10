<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any, svelte/no-navigation-without-resolve, svelte/require-each-key */
	import { enhance } from '$app/forms';

	let { data, form } = $props<{ data: any; form: any }>();

	const venues = $derived(data.venues || []);

	// Search and filter states
	let venueSearch = $state('');
	let venueStatusFilter = $state('all'); // 'all', 'ACTIVE', 'INACTIVE'

	// Create Venue modal and local binds
	let showAddVenueModal = $state(false);
	let name = $state('');
	let addressSearch = $state('');
	let selectedLocation = $state<any>(null);
	let showAddressSuggestions = $state(false);

	// Venue Manager search binds
	let managerQuery = $state('');
	let selectedManager = $state<any>(null);
	let managerSuggestions = $state<any[]>([]);
	let showManagerSuggestions = $state(false);
	let isSearchingManager = $state(false);

	// Sample high-fidelity Vietnam location suggestions (Geocode database)
	const locationSuggestions = [
		{
			display: 'Lê Đức Thọ, Mỹ Đình 1, Hà Nội, Vietnam',
			address: 'Lê Đức Thọ, Mỹ Đình 1',
			city: 'Hà Nội',
			country: 'Vietnam',
			lat: 21.020504,
			lng: 105.773824
		},
		{
			display: '30 Văn Cao, Ngọc Khánh, Ba Đình, Hà Nội, Vietnam',
			address: '30 Văn Cao, Ngọc Khánh',
			city: 'Hà Nội',
			country: 'Vietnam',
			lat: 21.042211,
			lng: 105.815911
		},
		{
			display: '799 Nguyễn Văn Linh, Tân Phú, Quận 7, Hồ Chí Minh, Vietnam',
			address: '799 Nguyễn Văn Linh, Tân Phú',
			city: 'Hồ Chí Minh',
			country: 'Vietnam',
			lat: 10.729222,
			lng: 106.721444
		},
		{
			display: '1 Tràng Tiền, Phan Chu Trinh, Hoàn Kiếm, Hà Nội, Vietnam',
			address: '1 Tràng Tiền, Phan Chu Trinh',
			city: 'Hà Nội',
			country: 'Vietnam',
			lat: 21.024222,
			lng: 105.855833
		},
		{
			display: '240 Đường 3 Tháng 2, Phường 12, Quận 10, Hồ Chí Minh, Vietnam',
			address: '240 Đường 3 Tháng 2, Phường 12',
			city: 'Hồ Chí Minh',
			country: 'Vietnam',
			lat: 10.776611,
			lng: 106.678311
		},
		{
			display: '202 Hoàng Văn Thụ, Phường 9, Phú Nhuận, Hồ Chí Minh, Vietnam',
			address: '202 Hoàng Văn Thụ, Phường 9',
			city: 'Hồ Chí Minh',
			country: 'Vietnam',
			lat: 10.799511,
			lng: 106.666691
		},
		{
			display: 'Phan Đăng Lưu, Hòa Cường Bắc, Hải Châu, Đà Nẵng, Vietnam',
			address: 'Phan Đăng Lưu, Hòa Cường Bắc',
			city: 'Đà Nẵng',
			country: 'Vietnam',
			lat: 16.031511,
			lng: 108.225911
		}
	];

	// Helper to strip Vietnamese diacritics for easier search
	function cleanVietnamese(text: string): string {
		return text
			.normalize('NFD')
			.replace(/[\u0300-\u036f]/g, '')
			.replace(/đ/g, 'd')
			.replace(/Đ/g, 'd');
	}

	let addressSuggestions = $state<any[]>([]);
	let isSearchingAddress = $state(false);

	// Filter locations based on input
	const localLocations = $derived(
		addressSearch.trim().length > 0
			? locationSuggestions.filter((loc) =>
					cleanVietnamese(loc.display)
						.toLowerCase()
						.includes(cleanVietnamese(addressSearch).toLowerCase())
				)
			: []
	);

	const filteredLocations = $derived(
		addressSuggestions.length > 0 ? addressSuggestions : localLocations
	);

	function selectLocation(loc: any) {
		addressSearch = loc.display;
		selectedLocation = loc;
		showAddressSuggestions = false;
	}

	function selectManager(account: any) {
		managerQuery = account.email;
		selectedManager = account;
		showManagerSuggestions = false;
	}

	// Address change watcher: reset selected location when search changes
	$effect(() => {
		if (selectedLocation && addressSearch !== selectedLocation.display) {
			selectedLocation = null;
		}
	});

	// Address search trigger (nominatim dynamic geocoding)
	$effect(() => {
		const query = addressSearch;
		if (!query || query.trim().length < 3) {
			addressSuggestions = [];
			return;
		}

		if (selectedLocation && selectedLocation.display === query.trim()) {
			return;
		}

		isSearchingAddress = true;
		const timer = setTimeout(async () => {
			try {
				const res = await fetch(`/api/ops/geocode?q=${encodeURIComponent(query)}`);
				const json = await res.json();
				if (json.success && Array.isArray(json.data)) {
					addressSuggestions = json.data;
				}
			} catch (err) {
				console.error(err);
			} finally {
				isSearchingAddress = false;
			}
		}, 300);

		return () => clearTimeout(timer);
	});

	// Manager search trigger
	$effect(() => {
		const query = managerQuery;
		if (!query || query.trim().length < 2) {
			managerSuggestions = [];
			showManagerSuggestions = false;
			return;
		}

		if (selectedManager && selectedManager.email.toLowerCase() === query.trim().toLowerCase()) {
			showManagerSuggestions = false;
			return;
		}

		isSearchingManager = true;
		const timer = setTimeout(async () => {
			try {
				const res = await fetch(`/api/ops/accounts?q=${encodeURIComponent(query)}`);
				const json = await res.json();
				if (json.success && Array.isArray(json.data)) {
					managerSuggestions = json.data;
					showManagerSuggestions = managerSuggestions.length > 0;
				}
			} catch (err) {
				console.error(err);
			} finally {
				isSearchingManager = false;
			}
		}, 300);

		return () => clearTimeout(timer);
	});

	const filteredVenues = $derived(
		venues.filter((v: any) => {
			const queryClean = cleanVietnamese(venueSearch).toLowerCase();
			const matchesSearch =
				cleanVietnamese(v.name).toLowerCase().includes(queryClean) ||
				cleanVietnamese(v.address || '')
					.toLowerCase()
					.includes(queryClean) ||
				cleanVietnamese(v.city || '')
					.toLowerCase()
					.includes(queryClean);
			const matchesStatus = venueStatusFilter === 'all' || v.status === venueStatusFilter;
			return matchesSearch && matchesStatus;
		})
	);

	// Document clicks to close autocomplete dropdowns
	let addrContainer = $state<HTMLDivElement | null>(null);
	let mgrContainer = $state<HTMLDivElement | null>(null);

	function handleDocumentClick(event: MouseEvent) {
		const target = event.target as Node;
		if (showAddressSuggestions && addrContainer && !addrContainer.contains(target)) {
			showAddressSuggestions = false;
		}
		if (showManagerSuggestions && mgrContainer && !mgrContainer.contains(target)) {
			showManagerSuggestions = false;
		}
	}
</script>

<svelte:window onclick={handleDocumentClick} />

<svelte:head>
	<title>Venues Moderation | Ticketpeak Platform Admin</title>
</svelte:head>

<div class="space-y-6 p-6">
	{#if form?.error}
		<div class="rounded-lg bg-rose-50 p-4 text-xs font-bold text-rose-600 select-none">
			⚠️ {form.error}
		</div>
	{/if}
	{#if form?.message}
		<div class="rounded-lg bg-emerald-50 p-4 text-xs font-bold text-emerald-600 select-none">
			✓ {form.message}
		</div>
	{/if}

	<div class="animate-fade-in overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
		<div
			class="flex flex-col gap-4 border-b border-[#F4F4F5] px-6 py-4 sm:flex-row sm:items-center sm:justify-between"
		>
			<div class="flex flex-1 items-center gap-3">
				<div class="relative w-full max-w-xs">
					<span
						class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-[#71717A]"
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
					</span>
					<input
						type="text"
						placeholder="Search venues by name or location..."
						bind:value={venueSearch}
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] py-2 pr-4 pl-9 text-xs font-semibold text-[#111111] placeholder-[#71717A] transition outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>
				<select
					bind:value={venueStatusFilter}
					class="rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3 py-2 text-xs font-semibold text-[#111111] transition outline-none focus:border-[#71717A] focus:bg-white"
				>
					<option value="all">All Status</option>
					<option value="ACTIVE">Active</option>
					<option value="INACTIVE">Inactive</option>
				</select>
			</div>
		</div>

		<div class="overflow-x-auto">
			<table class="w-full border-collapse text-left text-xs font-semibold text-[#71717A]">
				<tbody class="divide-y divide-[#F4F4F5] bg-white text-[#111111]">
					{#each filteredVenues as ven (ven.id)}
						<tr class="hover:bg-[#FAFAFA]">
							<td class="px-6 py-4">
								<div class="flex items-center gap-3">
									{#if ven.thumbnailUrl}
										<img
											src={ven.thumbnailUrl}
											alt={ven.name}
											class="h-10 w-10 rounded-lg border border-[#E4E4E7] object-cover"
										/>
									{:else}
										<div
											class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-xs font-bold text-slate-700 uppercase"
										>
											{ven.name?.[0] || 'V'}
										</div>
									{/if}
									<div class="flex min-w-0 flex-col">
										<div class="flex items-center gap-2">
											<a href="/ops/venues/{ven.id}" class="text-sm font-bold text-[#111111] hover:text-blue-600 hover:underline">{ven.name}</a>
											{#if ven.status !== 'ACTIVE'}
												<span class="rounded-md bg-rose-50 px-2 py-0.5 text-[9px] font-bold uppercase text-rose-600 select-none">{ven.status}</span>
											{/if}
										</div>
									</div>
								</div>
							</td>
							<td class="px-6 py-4 font-medium text-[#71717A]">
								{ven.address || ''}{ven.address && (ven.city || ven.country) ? ', ' : ''}{ven.city || ''}{ven.city && ven.country ? ', ' : ''}{ven.country || ''}
							</td>
						</tr>
					{:else}
						<tr>
							<td colspan="2" class="p-12 text-center text-[#71717A] font-medium"
								>No venues registered in this platform.</td
							>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	</div>
</div>

<!-- ======================== ADD VENUE DIALOG MODAL ======================== -->
{#if showAddVenueModal}
	<div class="fixed inset-0 z-50 flex items-start justify-center bg-black/40 px-4 pt-[12vh]">
		<div
			class="animate-scale-up h-auto max-h-[80vh] w-[90%] max-w-[550px] overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white p-6 text-left shadow-xl select-none"
		>
			<h3 class="text-lg font-bold tracking-tight text-[#09090B]">Create Venue</h3>
			<p class="mt-1 text-xs text-[#71717A]">
				Register a new global event hosting venue using automatic address geocoding.
			</p>

			<form
				method="POST"
				action="?/createVenue"
				use:enhance={() => {
					return async ({ result, update }) => {
						if (result.type === 'success') {
							showAddVenueModal = false;
						}
						await update();
					};
				}}
				class="mt-4 space-y-4"
			>
				<!-- Hidden Address Geocoding Fields -->
				<input
					type="hidden"
					name="address"
					value={selectedLocation ? selectedLocation.address : addressSearch}
				/>
				<input
					type="hidden"
					name="city"
					value={selectedLocation ? selectedLocation.city : 'Hà Nội'}
				/>
				<input
					type="hidden"
					name="country"
					value={selectedLocation ? selectedLocation.country : 'Vietnam'}
				/>
				<input
					type="hidden"
					name="latitude"
					value={selectedLocation ? selectedLocation.lat : 21.028511}
				/>
				<input
					type="hidden"
					name="longitude"
					value={selectedLocation ? selectedLocation.lng : 105.854211}
				/>

				<!-- Field 1: Venue Name -->
				<div class="space-y-1">
					<label for="venue-name" class="text-xs font-semibold text-[#71717A]">Venue Name *</label>
					<input
						type="text"
						id="venue-name"
						name="name"
						bind:value={name}
						required
						placeholder="e.g. Mỹ Đình National Stadium"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<!-- Field 2: Address Search Autocomplete (Geocoding) -->
				<div class="relative space-y-1" bind:this={addrContainer}>
					<label for="venue-address-search" class="text-xs font-semibold text-[#71717A]"
						>Address *</label
					>
					<input
						type="text"
						id="venue-address-search"
						bind:value={addressSearch}
						required
						onfocus={() => {
							showAddressSuggestions = true;
						}}
						placeholder="Search location (e.g. My Dinh Stadium, Saigon Exhibition...)"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>

					{#if showAddressSuggestions && filteredLocations.length > 0}
						<div
							class="absolute right-0 left-0 mt-1 max-h-40 overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white shadow-lg"
							style="z-index: 60;"
						>
							{#each filteredLocations as loc}
								<button
									type="button"
									onclick={() => selectLocation(loc)}
									class="w-full cursor-pointer border-0 bg-transparent px-3.5 py-2.5 text-left text-xs font-bold text-[#111111] transition hover:bg-slate-50"
								>
									{loc.display}
								</button>
							{/each}
						</div>
					{/if}

					{#if isSearchingAddress}
						<div class="mt-1.5 flex items-center gap-1.5 text-[10px] font-semibold text-[#71717A]">
							<svg class="h-3 w-3 animate-spin text-[#71717A]" fill="none" viewBox="0 0 24 24">
								<circle
									class="opacity-25"
									cx="12"
									cy="12"
									r="10"
									stroke="currentColor"
									stroke-width="4"
								></circle>
								<path
									class="opacity-75"
									fill="currentColor"
									d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
								></path>
							</svg>
							<span>Searching addresses...</span>
						</div>
					{/if}
				</div>

				<!-- Field 3: Venue Manager Autocomplete -->
				<div class="relative space-y-1" bind:this={mgrContainer}>
					<label for="venue-manager" class="text-xs font-semibold text-[#71717A]"
						>Venue Manager *</label
					>
					{#if selectedManager}
						<div
							class="flex w-full items-center justify-between rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111]"
						>
							<div class="flex min-w-0 items-center gap-2">
								{#if selectedManager.avatarUrl}
									<img
										src={selectedManager.avatarUrl}
										alt="Avatar"
										class="h-5 w-5 rounded-full border border-[#E4E4E7] object-cover"
									/>
								{:else}
									<div
										class="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-200 text-[9px] font-bold text-slate-700 uppercase"
									>
										{selectedManager.firstName?.[0] || selectedManager.email?.[0] || 'M'}
									</div>
								{/if}
								<span class="truncate text-xs font-normal text-[#111111]">
									{selectedManager.email}
								</span>
							</div>
							<button
								type="button"
								onclick={() => {
									selectedManager = null;
									managerQuery = '';
								}}
								class="ml-2 flex h-5 w-5 items-center justify-center rounded-full text-slate-400 transition-colors hover:bg-slate-200 hover:text-slate-600"
								aria-label="Remove manager"
							>
								<svg
									class="h-3 w-3"
									fill="none"
									viewBox="0 0 24 24"
									stroke="currentColor"
									stroke-width="2.5"
								>
									<path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
								</svg>
							</button>
						</div>
						<input type="hidden" name="managerEmail" value={selectedManager.email} />
					{:else}
						<input
							type="text"
							id="venue-manager"
							bind:value={managerQuery}
							onfocus={() => {
								showManagerSuggestions = true;
							}}
							placeholder="Search by manager name or email..."
							class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
						/>

						{#if showManagerSuggestions && managerSuggestions.length > 0}
							<div
								class="absolute right-0 left-0 mt-1 max-h-40 overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white shadow-lg"
								style="z-index: 60;"
							>
								{#each managerSuggestions as account}
									<button
										type="button"
										onclick={() => selectManager(account)}
										class="flex w-full cursor-pointer items-center gap-3 border-0 bg-transparent px-3.5 py-2 text-left transition hover:bg-slate-50"
									>
										{#if account.avatarUrl}
											<img
												src={account.avatarUrl}
												alt="Avatar"
												class="h-7 w-7 rounded-full border border-[#E4E4E7] object-cover"
											/>
										{:else}
											<div
												class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-slate-100 text-[10px] font-bold text-slate-700 uppercase"
											>
												{account.firstName?.[0] || account.email?.[0] || 'M'}
											</div>
										{/if}
										<div class="flex min-w-0 flex-col">
											<span class="truncate text-xs font-bold text-[#111111]">
												{account.firstName || ''}
												{account.lastName || ''}
											</span>
											<span class="truncate text-[9px] font-semibold text-[#71717A]">
												{account.email}
											</span>
										</div>
									</button>
								{/each}
							</div>
						{/if}
					{/if}

					{#if isSearchingManager}
						<div class="mt-1.5 flex items-center gap-1.5 text-[10px] font-semibold text-[#71717A]">
							<svg class="h-3 w-3 animate-spin text-[#71717A]" fill="none" viewBox="0 0 24 24">
								<circle
									class="opacity-25"
									cx="12"
									cy="12"
									r="10"
									stroke="currentColor"
									stroke-width="4"
								></circle>
								<path
									class="opacity-75"
									fill="currentColor"
									d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
								></path>
							</svg>
							<span>Searching managers...</span>
						</div>
					{/if}
				</div>

				<!-- Actions Footer -->
				<div class="flex items-center justify-between gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showAddVenueModal = false)}
						class="cursor-pointer rounded-md border border-[#E4E4E7] bg-transparent px-5 py-2 text-xs font-bold text-[#71717A] transition-all hover:bg-[#FAFAFA] hover:text-[#111111]"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-5 py-2 text-xs font-bold text-white transition-all hover:border-black hover:bg-black active:scale-95"
					>
						Create
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
