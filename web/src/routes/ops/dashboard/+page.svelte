<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';

	let { data, form } = $props<{ data: any; form: any }>();

	const events = $derived(data.events || []);
	const organizations = $derived(data.organizations || []);
	const classifications = $derived(data.classifications || []);
	const attractions = $derived(data.attractions || []);

	// Search and filter states
	let classSearch = $state('');
	let classLevelFilter = $state('all'); // 'all', '1', '2'

	let attrSearch = $state('');
	let attrTypeFilter = $state('all'); // 'all', 'ARTIST', 'VENUE', etc.

	// Derived filtered lists
	const filteredClassifications = $derived(
		classifications.filter((cat: any) => {
			const matchesSearch =
				cat.name.toLowerCase().includes(classSearch.toLowerCase()) ||
				(cat.slug || '').toLowerCase().includes(classSearch.toLowerCase()) ||
				(cat.id || '').toLowerCase().includes(classSearch.toLowerCase());
			const matchesLevel = classLevelFilter === 'all' || cat.level?.toString() === classLevelFilter;
			return matchesSearch && matchesLevel;
		})
	);

	const filteredAttractions = $derived(
		attractions.filter((attr: any) => {
			const matchesSearch =
				attr.name.toLowerCase().includes(attrSearch.toLowerCase()) ||
				(attr.slug || '').toLowerCase().includes(attrSearch.toLowerCase()) ||
				(attr.description || '').toLowerCase().includes(attrSearch.toLowerCase());
			const matchesType = attrTypeFilter === 'all' || attr.type === attrTypeFilter;
			return matchesSearch && matchesType;
		})
	);

	// Derive active tab from URL search parameters to keep in perfect sync with the layout sidebar
	const activeTab = $derived(
		(page.url.searchParams.get('tab') as
			| 'events'
			| 'classifications'
			| 'organizations'
			| 'attractions'
			| 'settings'
			| 'logs') || 'events'
	);

	// Local platform settings
	let platformCommission = $state(5);
	let antiScalpCap = $state(150);
	let maxTransferCount = $state(5);
	let settingsSaved = $state(false);

	// Modals toggling
	let selectedEventId = $state<string | null>(null);
	let postponeReason = $state('');
	let showPostponeModal = $state(false);

	let showAddClassificationModal = $state(false);
	let showAddAttractionModal = $state(false);

	// Classification form local binds
	let editingClassificationId = $state<string | null>(null);
	let className = $state('');
	let classSlug = $state('');
	let classParentId = $state('');

	function openEditClassification(cat: any) {
		editingClassificationId = cat.id;
		className = cat.name;
		classSlug = cat.slug;
		classParentId = cat.parentId || '';
		showAddClassificationModal = true;
	}

	// Attraction form local binds
	let attrName = $state('');
	let attrSlug = $state('');
	let attrType = $state('ARTIST');
	let attrImageUrl = $state('');
	let attrDescription = $state('');

	// Suspended organizations tracking
	let suspendedOrgIds = $state<string[]>([]);

	// Audit logs timeline
	let auditLogs = $state([
		{
			id: 1,
			time: '14:20:15',
			action: 'ADMIN_SIGN_IN',
			user: 'admin@ticketpeak.com',
			details: 'Authorized admin session started successfully.'
		},
		{
			id: 2,
			time: '13:05:40',
			action: 'UPDATE_SYSTEM_SETTINGS',
			user: 'admin@ticketpeak.com',
			details: 'Platform commission updated to 5.0%.'
		},
		{
			id: 3,
			time: '11:15:22',
			action: 'EVENT_PUBLISH',
			user: 'admin@ticketpeak.com',
			details: 'Event approved and published.'
		},
		{
			id: 4,
			time: '09:44:10',
			action: 'ORG_ACTIVATE',
			user: 'admin@ticketpeak.com',
			details: 'Organization active status verified.'
		}
	]);

	function addLog(action: string, details: string) {
		const now = new Date();
		const time = now.toTimeString().split(' ')[0];
		auditLogs = [
			{
				id: Date.now(),
				time,
				action,
				user: 'admin@ticketpeak.com',
				details
			},
			...auditLogs
		];
	}

	function saveSettings() {
		settingsSaved = true;
		addLog(
			'UPDATE_SETTINGS',
			`Commission: ${platformCommission}%, Resale Cap: ${antiScalpCap}%, Max Transfers: ${maxTransferCount}`
		);
		setTimeout(() => {
			settingsSaved = false;
		}, 3000);
	}

	function toggleOrgSuspend(orgId: string, orgName: string) {
		if (suspendedOrgIds.includes(orgId)) {
			suspendedOrgIds = suspendedOrgIds.filter((id) => id !== orgId);
			addLog('ORG_ACTIVATE', `Organization "${orgName}" reactivated successfully.`);
		} else {
			suspendedOrgIds = [...suspendedOrgIds, orgId];
			addLog('ORG_SUSPEND', `Organization "${orgName}" suspended due to policy check.`);
		}
	}

	function openPostpone(eventId: string) {
		selectedEventId = eventId;
		postponeReason = '';
		showPostponeModal = true;
	}

	function formatDateTime(isoString: string) {
		const d = new Date(isoString);
		return d.toLocaleString('en-US', {
			month: 'short',
			day: 'numeric',
			year: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	}

	// Dynamic slug generation helpers
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
			.replace(/\s+/g, '-')
			.replace(/[^\w-]+/g, '')
			.replace(/--+/g, '-')
			.replace(/^-+/, '')
			.replace(/-+$/, '');
	}
</script>

<svelte:head>
	<title>Ops Dashboard | Ticketpeak Platform Admin</title>
</svelte:head>

<div class="min-h-full w-full bg-slate-50/40 p-6 font-sans text-slate-700 antialiased">
	<!-- Error or Notifications -->
	{#if form?.error}
		<div
			class="mb-6 flex items-center gap-3 border border-red-200 bg-red-50 p-4 text-sm font-semibold text-red-700 select-none"
		>
			<span>⚠️ Operation Failed: {form.error}</span>
		</div>
	{/if}
	{#if form?.message}
		<div
			class="mb-6 flex items-center gap-3 border border-green-200 bg-green-50 p-4 text-sm font-semibold text-green-700 select-none"
		>
			<span>✅ Action Succeeded: {form.message}</span>
		</div>
	{/if}

	<!-- Dashboard Welcome & Overview -->
	<div class="mb-8 flex items-center justify-between">
		<div>
			<h1 class="text-2xl font-black tracking-tight text-slate-900 sm:text-3xl">
				Operations Control
			</h1>
			<p class="mt-1.5 text-xs font-semibold text-slate-500">
				Real-time moderation panel for global events, attractions, classifications, and platform
				settings.
			</p>
		</div>
	</div>

	<!-- KPI Stats Row -->
	<div class="mb-8 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
		<!-- KPI 1 -->
		<div
			class="group relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-6 shadow-xs transition-all duration-300 hover:scale-[1.02] hover:shadow-sm"
		>
			<div class="flex items-center justify-between">
				<span class="text-slate-450 text-xs font-bold tracking-wider uppercase">Total Events</span>
				<div class="rounded-lg bg-slate-100 p-2 text-slate-700">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="2.2"
						stroke="currentColor"
						class="h-5 w-5"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25v7.5"
						/>
					</svg>
				</div>
			</div>
			<p class="mt-4 font-mono text-3xl font-black text-slate-900">{events.length}</p>
			<p class="mt-1.5 text-[10px] font-bold text-slate-400 uppercase">
				Currently issued on platform
			</p>
		</div>

		<!-- KPI 2 -->
		<div
			class="group relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-6 shadow-xs transition-all duration-300 hover:scale-[1.02] hover:shadow-sm"
		>
			<div class="flex items-center justify-between">
				<span class="text-slate-455 text-xs font-bold tracking-wider uppercase">Active Orgs</span>
				<div class="rounded-lg bg-blue-50 p-2 text-blue-600">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="2.2"
						stroke="currentColor"
						class="h-5 w-5"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M2.25 21h19.5m-18-18v18m10.5-18v18m6-13.5V21M6.75 6.75h.75m-.75 3h.75m-.75 3h.75m3-6h.75m-.75 3h.75m-.75 3h.75M6.75 21h10.5"
						/>
					</svg>
				</div>
			</div>
			<p class="mt-4 font-mono text-3xl font-black text-slate-900">{organizations.length}</p>
			<p class="mt-1.5 text-[10px] font-bold text-slate-400 uppercase">
				Verified organizer accounts
			</p>
		</div>

		<!-- KPI 3 -->
		<div
			class="group relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-6 shadow-xs transition-all duration-300 hover:scale-[1.02] hover:shadow-sm"
		>
			<div class="flex items-center justify-between">
				<span class="text-slate-455 text-xs font-bold tracking-wider uppercase"
					>Classifications</span
				>
				<div class="rounded-lg bg-emerald-50 p-2 text-emerald-600">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="2.2"
						stroke="currentColor"
						class="h-5 w-5"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M9.568 3H5.25A2.25 2.25 0 0 0 3 5.25v4.318c0 .597.237 1.17.659 1.591l9.581 9.581c.699.699 1.78.872 2.674.509a8.948 8.948 0 0 0 3.54-2.54 8.948 8.948 0 0 0 2.54-3.54c.363-.894.19-1.975-.509-2.674l-9.581-9.581A2.25 2.25 0 0 0 9.568 3Z"
						/>
						<path
							d="M6 6h.008v.008H6V6Z"
							stroke-width="2.5"
							stroke-linecap="round"
							stroke-linejoin="round"
						/>
					</svg>
				</div>
			</div>
			<p class="mt-4 font-mono text-3xl font-black text-slate-900">
				{classifications.length}
			</p>
			<p class="mt-1.5 text-[10px] font-bold text-slate-400 uppercase">
				Active genres & event categories
			</p>
		</div>

		<!-- KPI 4 -->
		<div
			class="group relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-6 shadow-xs transition-all duration-300 hover:scale-[1.02] hover:shadow-sm"
		>
			<div class="flex items-center justify-between">
				<span class="text-slate-455 text-xs font-bold tracking-wider uppercase">Attractions</span>
				<div class="rounded-lg bg-purple-50 p-2 text-purple-600">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="2.2"
						stroke="currentColor"
						class="h-5 w-5"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z"
						/>
					</svg>
				</div>
			</div>
			<p class="mt-4 font-mono text-3xl font-black text-slate-900">{attractions.length}</p>
			<p class="mt-1.5 text-[10px] font-bold text-slate-400 uppercase">
				Registered Artists & Performers
			</p>
		</div>
	</div>

	<!-- Secondary on-page Navigation Tabs (perfectly styled to match B2B) -->
	<div class="no-scrollbar mb-6 flex overflow-x-auto border-b border-slate-200">
		<button
			onclick={() => goto('/ops/dashboard?tab=events')}
			class="shrink-0 cursor-pointer border-b-2 px-6 py-3 font-sans text-xs font-extrabold tracking-wider uppercase transition-all outline-none
				{activeTab === 'events'
				? 'border-slate-950 font-extrabold text-slate-950'
				: 'border-transparent text-slate-400 hover:text-slate-700'}"
		>
			📢 Event Moderation
		</button>
		<button
			onclick={() => goto('/ops/dashboard?tab=classifications')}
			class="shrink-0 cursor-pointer border-b-2 px-6 py-3 font-sans text-xs font-extrabold tracking-wider uppercase transition-all outline-none
				{activeTab === 'classifications'
				? 'border-slate-950 font-extrabold text-slate-950'
				: 'border-transparent text-slate-400 hover:text-slate-700'}"
		>
			🏷️ Classifications
		</button>
		<button
			onclick={() => goto('/ops/dashboard?tab=organizations')}
			class="shrink-0 cursor-pointer border-b-2 px-6 py-3 font-sans text-xs font-extrabold tracking-wider uppercase transition-all outline-none
				{activeTab === 'organizations'
				? 'border-slate-950 font-extrabold text-slate-950'
				: 'border-transparent text-slate-400 hover:text-slate-700'}"
		>
			🏢 Organizations
		</button>
		<button
			onclick={() => goto('/ops/dashboard?tab=attractions')}
			class="shrink-0 cursor-pointer border-b-2 px-6 py-3 font-sans text-xs font-extrabold tracking-wider uppercase transition-all outline-none
				{activeTab === 'attractions'
				? 'border-slate-950 font-extrabold text-slate-950'
				: 'border-transparent text-slate-400 hover:text-slate-700'}"
		>
			🎭 Attractions
		</button>
		<button
			onclick={() => goto('/ops/dashboard?tab=settings')}
			class="shrink-0 cursor-pointer border-b-2 px-6 py-3 font-sans text-xs font-extrabold tracking-wider uppercase transition-all outline-none
				{activeTab === 'settings'
				? 'border-slate-950 font-extrabold text-slate-950'
				: 'border-transparent text-slate-400 hover:text-slate-700'}"
		>
			⚙️ Platform Settings
		</button>
		<button
			onclick={() => goto('/ops/dashboard?tab=logs')}
			class="shrink-0 cursor-pointer border-b-2 px-6 py-3 font-sans text-xs font-extrabold tracking-wider uppercase transition-all outline-none
				{activeTab === 'logs'
				? 'border-slate-950 font-extrabold text-slate-950'
				: 'border-transparent text-slate-400 hover:text-slate-700'}"
		>
			📋 Audit Logs
		</button>
	</div>

	<!-- TAB CONTENT: EVENTS -->
	{#if activeTab === 'events'}
		<div
			class="animate-fade-in overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs"
		>
			<div class="border-b border-slate-100 px-6 py-4">
				<h3 class="font-sans text-sm font-extrabold text-slate-800 uppercase select-none">
					Platform Events Inventory
				</h3>
			</div>
			<div class="overflow-x-auto">
				<table class="w-full border-collapse text-left">
					<thead>
						<tr
							class="border-slate-150 border-b bg-slate-50/50 text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
						>
							<th class="px-6 py-3.5">Event Name</th>
							<th class="px-6 py-3.5">Venue</th>
							<th class="px-6 py-3.5">Start Date</th>
							<th class="px-6 py-3.5">Status</th>
							<th class="px-6 py-3.5 text-right">Moderation Actions</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-slate-100 font-sans text-xs font-semibold text-slate-600">
						{#each events as event (event.id)}
							<tr class="transition-colors hover:bg-slate-50/50">
								<td class="px-6 py-4">
									<p class="text-sm font-extrabold text-slate-900">{event.title}</p>
									<p class="mt-0.5 text-[10px] text-slate-400">ID: {event.id}</p>
								</td>
								<td class="px-6 py-4 font-medium text-slate-700">{event.venueName}</td>
								<td class="px-6 py-4 text-slate-500">{formatDateTime(event.startAt)}</td>
								<td class="px-6 py-4">
									{#if event.status === 'DRAFT'}
										<span
											class="inline-block rounded-md bg-slate-100 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-slate-600 uppercase select-none"
										>
											Draft
										</span>
									{:else if event.status === 'PUBLISHED'}
										<span
											class="inline-block rounded-md bg-blue-50/70 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-blue-600 uppercase select-none"
										>
											Published
										</span>
									{:else if event.status === 'ON_SALE'}
										<span
											class="inline-block rounded-md bg-emerald-50 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-emerald-600 uppercase select-none"
										>
											On Sale
										</span>
									{:else if event.status === 'POSTPONED'}
										<span
											class="inline-block rounded-md bg-amber-50 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-amber-600 uppercase select-none"
										>
											Postponed
										</span>
									{:else if event.status === 'CANCELLED'}
										<span
											class="inline-block rounded-md bg-rose-50 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-rose-600 uppercase select-none"
										>
											Cancelled
										</span>
									{:else}
										<span
											class="inline-block rounded-md bg-slate-100 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-slate-500 uppercase select-none"
										>
											{event.status}
										</span>
									{/if}
								</td>
								<td class="px-6 py-4 text-right">
									<div class="flex items-center justify-end gap-2">
										{#if event.status === 'DRAFT'}
											<form
												method="POST"
												action="?/publishEvent"
												use:enhance={() => {
													return ({ result }) => {
														if (result.type === 'success')
															addLog('EVENT_PUBLISH', `Approved and published "${event.title}".`);
													};
												}}
											>
												<input type="hidden" name="id" value={event.id} />
												<button
													type="submit"
													class="cursor-pointer rounded-lg bg-blue-600 px-3 py-1 text-xs font-bold text-white transition-all hover:bg-blue-700 active:scale-95"
												>
													Approve
												</button>
											</form>
										{/if}

										{#if event.status === 'PUBLISHED'}
											<form
												method="POST"
												action="?/startSales"
												use:enhance={() => {
													return ({ result }) => {
														if (result.type === 'success')
															addLog('START_SALES', `Ticket sales opened for "${event.title}".`);
													};
												}}
											>
												<input type="hidden" name="id" value={event.id} />
												<button
													type="submit"
													class="cursor-pointer rounded-lg bg-emerald-600 px-3 py-1 text-xs font-bold text-white transition-all hover:bg-emerald-700 active:scale-95"
												>
													Open Sales
												</button>
											</form>
										{/if}

										{#if event.status !== 'CANCELLED' && event.status !== 'POSTPONED'}
											<button
												onclick={() => openPostpone(event.id)}
												class="cursor-pointer rounded-lg border border-slate-200 bg-white px-3 py-1 text-xs font-bold text-slate-700 transition-all hover:bg-slate-50 active:scale-95"
											>
												Postpone
											</button>

											<form
												method="POST"
												action="?/cancelEvent"
												use:enhance={() => {
													return ({ result }) => {
														if (result.type === 'success')
															addLog(
																'EVENT_CANCEL',
																`Platform-enforced cancellation of event "${event.title}".`
															);
													};
												}}
											>
												<input type="hidden" name="id" value={event.id} />
												<button
													type="submit"
													class="cursor-pointer rounded-lg border border-rose-100 bg-rose-50/50 px-3 py-1 text-xs font-bold text-rose-600 transition-all hover:bg-rose-600 hover:text-white active:scale-95"
												>
													Cancel
												</button>
											</form>
										{/if}
									</div>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- TAB CONTENT: CLASSIFICATIONS -->
	{#if activeTab === 'classifications'}
		<div
			class="animate-fade-in overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs"
		>
			<div
				class="flex flex-col gap-4 border-b border-slate-100 px-6 py-4 sm:flex-row sm:items-center sm:justify-between"
			>
				<!-- Search and Filter controls -->
				<div class="flex flex-1 flex-col gap-3 sm:flex-row sm:items-center">
					<!-- Search Input -->
					<div class="relative w-full max-w-xs">
						<span
							class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400"
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
							placeholder="Search classifications..."
							bind:value={classSearch}
							class="w-full rounded-lg border border-slate-200 bg-slate-50 py-2 pr-4 pl-9 text-xs font-semibold text-slate-800 placeholder-slate-400 transition outline-none focus:border-slate-400 focus:bg-white"
						/>
					</div>

					<!-- Filter dropdown -->
					<div class="flex items-center gap-2">
						<select
							bind:value={classLevelFilter}
							class="text-slate-650 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs font-semibold transition outline-none focus:border-slate-400 focus:bg-white"
						>
							<option value="all">All Levels</option>
							<option value="1">Root Categories</option>
							<option value="2">Subcategories</option>
						</select>
					</div>
				</div>

				<!-- Create button -->
				<button
					type="button"
					onclick={() => {
						editingClassificationId = null;
						className = '';
						classSlug = '';
						classParentId = '';
						showAddClassificationModal = true;
					}}
					class="cursor-pointer rounded-lg border border-slate-900 bg-slate-900 px-4 py-2 text-center text-xs font-extrabold text-white shadow-xs transition-all hover:border-slate-800 hover:bg-slate-800 active:scale-95"
				>
					Create
				</button>
			</div>
			<div class="overflow-x-auto">
				<table class="w-full border-collapse text-left">
					<tbody class="divide-y divide-slate-100 font-sans text-xs font-semibold text-slate-600">
						{#each filteredClassifications as cat (cat.id)}
							<tr class="transition-colors hover:bg-slate-50/50">
								<td class="px-6 py-4">
									<button
										type="button"
										onclick={() => openEditClassification(cat)}
										class="cursor-pointer border-0 bg-transparent p-0 text-left text-sm font-extrabold text-slate-900 transition-colors hover:text-slate-700 hover:underline focus:outline-none"
									>
										{cat.name}
									</button>
								</td>
								<td class="px-6 py-4 font-mono text-slate-500">{cat.slug}</td>
								<td class="px-6 py-4">
									{#if cat.parentId}
										{@const parent = classifications.find((c: any) => c.id === cat.parentId)}
										<span class="text-xs font-semibold text-slate-700">
											{parent ? parent.name : '-'}
										</span>
									{:else}
										<span class="text-xs font-semibold text-slate-400">-</span>
									{/if}
								</td>
								<td class="px-6 py-4">
									<div class="flex items-center justify-between gap-4">
										{#if cat.isActive}
											<span
												class="inline-block rounded-md bg-emerald-50 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-emerald-600 uppercase select-none"
											>
												Active
											</span>
										{:else}
											<span
												class="inline-block rounded-md bg-rose-50 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-rose-600 uppercase select-none"
											>
												Inactive
											</span>
										{/if}

										<form method="POST" action="?/toggleClassificationStatus" use:enhance>
											<input type="hidden" name="id" value={cat.id} />
											<input type="hidden" name="name" value={cat.name} />
											<input type="hidden" name="slug" value={cat.slug} />
											<input type="hidden" name="parentId" value={cat.parentId || ''} />
											<input
												type="hidden"
												name="isActive"
												value={cat.isActive ? 'true' : 'false'}
											/>
											<button
												type="submit"
												class="cursor-pointer rounded-lg border px-3 py-1 text-xs font-bold transition-all active:scale-95
													{cat.isActive
													? 'border-rose-100 bg-rose-50/50 text-rose-600 hover:bg-rose-600 hover:text-white'
													: 'border-emerald-100 bg-emerald-50/50 text-emerald-600 hover:bg-emerald-600 hover:text-white'}"
											>
												{cat.isActive ? 'Deactivate' : 'Activate'}
											</button>
										</form>
									</div>
								</td>
							</tr>
						{:else}
							<tr>
								<td colspan="4" class="p-12 text-center text-slate-400 font-medium">
									No classifications found matching your search criteria.
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- TAB CONTENT: ORGANIZATIONS -->
	{#if activeTab === 'organizations'}
		<div
			class="animate-fade-in overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs"
		>
			<div class="border-b border-slate-100 px-6 py-4">
				<h3 class="font-sans text-sm font-extrabold text-slate-800 uppercase select-none">
					Platform Organizations
				</h3>
			</div>
			<div class="overflow-x-auto">
				<table class="w-full border-collapse text-left">
					<thead>
						<tr
							class="border-slate-150 border-b bg-slate-50/50 text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
						>
							<th class="px-6 py-3.5">Organization Name</th>
							<th class="px-6 py-3.5">Contact Details</th>
							<th class="px-6 py-3.5">Website</th>
							<th class="px-6 py-3.5">Status</th>
							<th class="px-6 py-3.5 text-right font-bold">Actions</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-slate-100 font-sans text-xs font-semibold text-slate-600">
						{#each organizations as org (org.id)}
							{@const isSuspended = suspendedOrgIds.includes(org.id)}
							<tr class="transition-colors hover:bg-slate-50/50">
								<td class="px-6 py-4">
									<p class="text-sm font-extrabold text-slate-900">{org.name}</p>
									<p class="mt-0.5 text-[10px] text-slate-400">Slug: {org.slug}</p>
								</td>
								<td class="px-6 py-4">
									<p class="font-medium text-slate-800">{org.email || 'No email'}</p>
									<p class="mt-0.5 font-normal text-slate-400">{org.phone || 'No phone'}</p>
								</td>
								<td class="px-6 py-4 text-blue-600 hover:underline">
									{#if org.websiteUrl}
										<a href={org.websiteUrl} target="_blank" rel="noopener noreferrer"
											>{org.websiteUrl}</a
										>
									{:else}
										<span class="font-normal text-slate-400">N/A</span>
									{/if}
								</td>
								<td class="px-6 py-4">
									{#if isSuspended}
										<span
											class="inline-block rounded-md bg-rose-50 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-rose-600 uppercase select-none"
										>
											Suspended
										</span>
									{:else}
										<span
											class="inline-block rounded-md bg-emerald-50 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-emerald-600 uppercase select-none"
										>
											Active
										</span>
									{/if}
								</td>
								<td class="px-6 py-4 text-right">
									<button
										onclick={() => toggleOrgSuspend(org.id, org.name)}
										class="cursor-pointer rounded-lg border px-3 py-1 text-xs font-bold transition-all active:scale-95
											{isSuspended
											? 'border-emerald-100 bg-emerald-50 text-emerald-600 hover:bg-emerald-600 hover:text-white'
											: 'border-rose-100 bg-rose-50 text-rose-600 hover:bg-rose-600 hover:text-white'}"
									>
										{isSuspended ? 'Reactivate' : 'Suspend'}
									</button>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- TAB CONTENT: ATTRACTIONS -->
	{#if activeTab === 'attractions'}
		<div
			class="animate-fade-in overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs"
		>
			<div
				class="flex flex-col gap-4 border-b border-slate-100 px-6 py-4 sm:flex-row sm:items-center sm:justify-between"
			>
				<!-- Search and Filter controls -->
				<div class="flex flex-1 flex-col gap-3 sm:flex-row sm:items-center">
					<!-- Search Input -->
					<div class="relative w-full max-w-xs">
						<span
							class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400"
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
							placeholder="Search attractions..."
							bind:value={attrSearch}
							class="w-full rounded-lg border border-slate-200 bg-slate-50 py-2 pr-4 pl-9 text-xs font-semibold text-slate-800 placeholder-slate-400 transition outline-none focus:border-slate-400 focus:bg-white"
						/>
					</div>

					<!-- Filter dropdown -->
					<div class="flex items-center gap-2">
						<select
							bind:value={attrTypeFilter}
							class="text-slate-650 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs font-semibold transition outline-none focus:border-slate-400 focus:bg-white"
						>
							<option value="all">All Types</option>
							<option value="ARTIST">Artist / Performer</option>
							<option value="VENUE">Venue / Host</option>
							<option value="OTHER">Other Type</option>
						</select>
					</div>
				</div>

				<!-- Create button -->
				<button
					type="button"
					onclick={() => {
						attrName = '';
						attrSlug = '';
						attrType = 'ARTIST';
						attrImageUrl = '';
						attrDescription = '';
						showAddAttractionModal = true;
					}}
					class="cursor-pointer rounded-lg border border-slate-900 bg-slate-900 px-4 py-2 text-center text-xs font-extrabold text-white shadow-xs transition-all hover:border-slate-800 hover:bg-slate-800 active:scale-95"
				>
					Create
				</button>
			</div>
			<div class="overflow-x-auto">
				<table class="w-full border-collapse text-left">
					<tbody class="divide-y divide-slate-100 font-sans text-xs font-semibold text-slate-600">
						{#each filteredAttractions as attr (attr.id)}
							<tr class="transition-colors hover:bg-slate-50/50">
								<td class="px-6 py-4">
									<div class="flex items-center gap-3">
										{#if attr.imageUrl}
											<img
												src={attr.imageUrl}
												alt={attr.name}
												class="h-8 w-8 rounded-full border object-cover"
											/>
										{:else}
											<div
												class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-[10px] font-bold text-slate-500"
											>
												{attr.name[0]?.toUpperCase()}
											</div>
										{/if}
										<div>
											<p class="text-sm font-extrabold text-slate-900">{attr.name}</p>
										</div>
									</div>
								</td>
								<td class="px-6 py-4 font-mono text-slate-500">{attr.slug}</td>
								<td class="px-6 py-4">
									<span
										class="rounded bg-slate-100 px-2.5 py-0.5 text-[9px] font-extrabold tracking-wider text-slate-500 uppercase"
									>
										{attr.type}
									</span>
								</td>
								<td class="text-slate-450 max-w-sm truncate px-6 py-4 font-normal">
									{attr.description || 'No description provided.'}
								</td>
							</tr>
						{:else}
							<tr>
								<td colspan="4" class="p-12 text-center text-slate-400 font-medium">
									No attractions found matching your search criteria.
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- TAB CONTENT: PLATFORM SETTINGS -->
	{#if activeTab === 'settings'}
		<div
			class="animate-fade-in max-w-2xl rounded-2xl border border-slate-200 bg-white p-6 shadow-xs select-none"
		>
			<h3 class="mb-6 font-sans text-sm font-extrabold tracking-wider text-slate-800 uppercase">
				Global Configuration Settings
			</h3>

			<div class="space-y-6">
				<!-- Setting 1 -->
				<div class="space-y-2">
					<div class="flex items-center justify-between text-xs font-bold text-slate-500 uppercase">
						<span>Platform Commission Fee</span>
						<span class="font-mono text-sm font-extrabold text-slate-900"
							>{platformCommission}%</span
						>
					</div>
					<input
						type="range"
						min="0"
						max="15"
						step="0.5"
						bind:value={platformCommission}
						class="h-1.5 w-full cursor-pointer rounded-lg bg-slate-100 accent-slate-900"
					/>
					<p class="text-[10px] font-bold text-slate-400">
						Service charge taken from organizer payouts automatically upon transaction clearance.
					</p>
				</div>

				<!-- Setting 2 -->
				<div class="space-y-2">
					<div class="flex items-center justify-between text-xs font-bold text-slate-500 uppercase">
						<span>Anti-Scalping Resale Price Cap</span>
						<span class="font-mono text-sm font-extrabold text-slate-900">{antiScalpCap}%</span>
					</div>
					<input
						type="range"
						min="100"
						max="200"
						step="5"
						bind:value={antiScalpCap}
						class="h-1.5 w-full cursor-pointer rounded-lg bg-slate-100 accent-slate-900"
					/>
					<p class="text-[10px] font-bold text-slate-400">
						Maximum percentage above face value secondary ticket listings are capped at.
					</p>
				</div>

				<!-- Setting 3 -->
				<div class="space-y-2">
					<div class="flex items-center justify-between text-xs font-bold text-slate-500 uppercase">
						<span>Max Ticket Transfers Limit</span>
						<span class="font-mono text-sm font-extrabold text-slate-900"
							>{maxTransferCount} times</span
						>
					</div>
					<input
						type="number"
						min="1"
						max="10"
						bind:value={maxTransferCount}
						class="w-full rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2.5 font-sans text-sm font-semibold text-slate-800 outline-none focus:border-slate-950 focus:bg-white"
					/>
					<p class="text-[10px] font-bold text-slate-400">
						Maximum threshold for one-tap ticket transfers before lock-up.
					</p>
				</div>

				<!-- Save Notification -->
				{#if settingsSaved}
					<div
						class="rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-xs font-bold text-emerald-700"
					>
						✨ Configurations applied and propagated to active events.
					</div>
				{/if}

				<!-- Submit Button -->
				<button
					onclick={saveSettings}
					class="cursor-pointer rounded-lg border border-slate-900 bg-slate-900 px-6 py-2.5 text-center font-sans text-xs font-extrabold text-white shadow-xs transition-all hover:border-slate-800 hover:bg-slate-800 active:scale-95"
				>
					Save Settings
				</button>
			</div>
		</div>
	{/if}

	<!-- TAB CONTENT: AUDIT LOGS -->
	{#if activeTab === 'logs'}
		<div
			class="animate-fade-in overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs"
		>
			<div
				class="flex items-center justify-between border-b border-slate-100 px-6 py-4 select-none"
			>
				<h3 class="font-sans text-sm font-extrabold text-slate-800 uppercase">System Audit Logs</h3>
				<span
					class="rounded-md bg-slate-100 px-2.5 py-0.5 font-mono text-[10px] font-bold text-slate-500"
				>
					Real-Time Timeline
				</span>
			</div>

			<div class="max-h-[500px] space-y-4 overflow-y-auto p-6 font-mono text-xs text-slate-600">
				{#each auditLogs as log (log.id)}
					<div
						class="flex items-start gap-4 border-b border-slate-100 pb-3 last:border-0 last:pb-0"
					>
						<span class="shrink-0 font-bold text-slate-950">{log.time}</span>
						<div class="space-y-1">
							<p class="text-[11px] font-bold text-slate-900">
								[{log.action}] by <span class="font-semibold text-blue-600">{log.user}</span>
							</p>
							<p class="text-xs text-slate-500">{log.details}</p>
						</div>
					</div>
				{/each}
			</div>
		</div>
	{/if}
</div>

<!-- ======================== ADD CLASSIFICATION DIALOG MODAL ======================== -->
{#if showAddClassificationModal}
	<div
		class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4 backdrop-blur-xs"
	>
		<div
			class="animate-scale-up w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 text-left shadow-2xl select-none"
		>
			<h3 class="text-lg font-black tracking-tight text-slate-900 uppercase">
				{editingClassificationId ? 'Edit Classification' : 'Add Classification'}
			</h3>
			<p class="mt-1 text-xs text-slate-500">
				{editingClassificationId
					? 'Modify details for this event classification.'
					: 'Create a root category or a sub-genre for events ticketing.'}
			</p>

			<form
				method="POST"
				action={editingClassificationId ? '?/updateClassification' : '?/createClassification'}
				use:enhance={() => {
					return ({ result }) => {
						if (result.type === 'success') {
							addLog(
								editingClassificationId ? 'CLASSIFICATION_UPDATE' : 'CLASSIFICATION_CREATE',
								`Classification "${className}" (${classSlug}) ${editingClassificationId ? 'updated' : 'created'} successfully.`
							);
							showAddClassificationModal = false;
						}
					};
				}}
				class="mt-4 space-y-4"
			>
				{#if editingClassificationId}
					<input type="hidden" name="id" value={editingClassificationId} />
				{/if}
				<div class="space-y-1">
					<label
						for="class-name"
						class="text-[10px] font-bold tracking-wider text-slate-500 uppercase">Name *</label
					>
					<input
						type="text"
						id="class-name"
						name="name"
						bind:value={className}
						oninput={() => {
							classSlug = slugify(className);
						}}
						required
						placeholder="e.g. Pop Music"
						class="w-full rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2.5 font-sans text-xs text-slate-800 outline-none focus:border-slate-900 focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label
						for="class-slug"
						class="text-[10px] font-bold tracking-wider text-slate-500 uppercase">URL Slug *</label
					>
					<input
						type="text"
						id="class-slug"
						name="slug"
						bind:value={classSlug}
						required
						placeholder="pop-music"
						class="w-full rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2.5 font-sans text-xs text-slate-800 outline-none focus:border-slate-900 focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label
						for="class-parent"
						class="text-[10px] font-bold tracking-wider text-slate-500 uppercase"
						>Parent Category (Optional)</label
					>
					<select
						id="class-parent"
						name="parentId"
						bind:value={classParentId}
						class="w-full rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2.5 font-sans text-xs text-slate-800 outline-none focus:border-slate-900 focus:bg-white"
					>
						<option value="">None (Level 1 Root Category)</option>
						{#each classifications.filter((c: any) => c.level === 1 && c.id !== editingClassificationId) as rootCat (rootCat.id)}
							<option value={rootCat.id}>{rootCat.name}</option>
						{/each}
					</select>
					<p class="pt-1 text-[9px] leading-normal text-slate-400">
						Leave blank to create a main root category, or select a parent to create a sub-genre.
					</p>
				</div>

				<div class="flex items-center justify-end gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showAddClassificationModal = false)}
						class="cursor-pointer rounded-lg border border-slate-200 bg-transparent px-5 py-2 text-xs font-bold text-slate-500 transition-all hover:bg-slate-50 hover:text-slate-800"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="hover:bg-slate-850 hover:border-slate-850 cursor-pointer rounded-lg border border-slate-900 bg-slate-900 px-5 py-2 text-xs font-extrabold text-white transition-all active:scale-95"
					>
						Create
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- ======================== ADD ATTRACTION DIALOG MODAL ======================== -->
{#if showAddAttractionModal}
	<div
		class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4 backdrop-blur-xs"
	>
		<div
			class="animate-scale-up w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 text-left shadow-2xl select-none"
		>
			<h3 class="text-lg font-black tracking-tight text-slate-900 uppercase">Add Attraction</h3>
			<p class="mt-1 text-xs text-slate-500">
				Register a new featured artist, band, team, or performing group.
			</p>

			<form
				method="POST"
				action="?/createAttraction"
				use:enhance={() => {
					return ({ result }) => {
						if (result.type === 'success') {
							addLog(
								'ATTRACTION_CREATE',
								`Attraction "${attrName}" (${attrSlug}) created successfully.`
							);
							showAddAttractionModal = false;
						}
					};
				}}
				class="mt-4 space-y-4"
			>
				<div class="space-y-1">
					<label
						for="attr-name"
						class="text-[10px] font-bold tracking-wider text-slate-500 uppercase">Name *</label
					>
					<input
						type="text"
						id="attr-name"
						name="name"
						bind:value={attrName}
						oninput={() => {
							attrSlug = slugify(attrName);
						}}
						required
						placeholder="e.g. Son Tung M-TP"
						class="w-full rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2.5 font-sans text-xs text-slate-800 outline-none focus:border-slate-900 focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label
						for="attr-slug"
						class="text-[10px] font-bold tracking-wider text-slate-500 uppercase">URL Slug *</label
					>
					<input
						type="text"
						id="attr-slug"
						name="slug"
						bind:value={attrSlug}
						required
						placeholder="son-tung-mtp"
						class="w-full rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2.5 font-sans text-xs text-slate-800 outline-none focus:border-slate-900 focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label
						for="attr-type"
						class="text-[10px] font-bold tracking-wider text-slate-500 uppercase"
						>Performer Type</label
					>
					<select
						id="attr-type"
						name="type"
						bind:value={attrType}
						class="w-full rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2.5 font-sans text-xs text-slate-800 outline-none focus:border-slate-900 focus:bg-white"
					>
						<option value="ARTIST">ARTIST (Singer, Musician, etc.)</option>
						<option value="TEAM">TEAM (Sports Club/Group)</option>
						<option value="SHOW_CREW">SHOW CREW (Theater / Performing Group)</option>
					</select>
				</div>

				<div class="space-y-1">
					<label
						for="attr-image"
						class="text-[10px] font-bold tracking-wider text-slate-500 uppercase"
						>Image URL (Optional)</label
					>
					<input
						type="url"
						id="attr-image"
						name="imageUrl"
						bind:value={attrImageUrl}
						placeholder="https://example.com/avatar.jpg"
						class="w-full rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2.5 font-sans text-xs text-slate-800 outline-none focus:border-slate-900 focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label
						for="attr-desc"
						class="text-[10px] font-bold tracking-wider text-slate-500 uppercase"
						>Description (Optional)</label
					>
					<textarea
						id="attr-desc"
						name="description"
						bind:value={attrDescription}
						rows="2"
						placeholder="Brief biography or details..."
						class="w-full rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2.5 font-sans text-xs text-slate-800 outline-none focus:border-slate-900 focus:bg-white"
					></textarea>
				</div>

				<div class="flex items-center justify-end gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showAddAttractionModal = false)}
						class="cursor-pointer rounded-lg border border-slate-200 bg-transparent px-5 py-2 text-xs font-bold text-slate-500 transition-all hover:bg-slate-50 hover:text-slate-800"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="hover:bg-slate-850 hover:border-slate-850 cursor-pointer rounded-lg border border-slate-900 bg-slate-900 px-5 py-2 text-xs font-extrabold text-white transition-all active:scale-95"
					>
						Create
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- POSTPONE DIALOG MODAL -->
{#if showPostponeModal}
	<div
		class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4 backdrop-blur-xs"
	>
		<div
			class="animate-scale-up w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 text-left shadow-2xl select-none"
		>
			<h3 class="text-lg font-black tracking-tight text-slate-900 uppercase">Postpone Event</h3>
			<p class="mt-1 text-xs text-slate-500">
				Provide an official platform announcement reason for postponing.
			</p>

			<form
				method="POST"
				action="?/postponeEvent"
				use:enhance={() => {
					return ({ result }) => {
						if (result.type === 'success') {
							addLog(
								'EVENT_POSTPONE',
								`Event ID: ${selectedEventId} officially postponed. Reason: ${postponeReason}`
							);
							showPostponeModal = false;
						}
					};
				}}
				class="mt-4 space-y-4"
			>
				<input type="hidden" name="id" value={selectedEventId} />

				<div class="space-y-1">
					<label
						for="postpone-reason"
						class="text-[10px] font-bold tracking-wider text-slate-500 uppercase">Reason</label
					>
					<textarea
						id="postpone-reason"
						bind:value={postponeReason}
						required
						rows="3"
						placeholder="e.g. Unavoidable structural delays at military venue."
						class="w-full rounded-lg border border-slate-200 bg-slate-50 px-3.5 py-2.5 font-sans text-xs text-slate-800 outline-none focus:border-slate-900 focus:bg-white"
					></textarea>
				</div>

				<div class="flex items-center justify-end gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showPostponeModal = false)}
						class="cursor-pointer rounded-lg border border-slate-200 bg-transparent px-5 py-2 text-xs font-bold text-slate-500 transition-all hover:bg-slate-50 hover:text-slate-800"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="hover:bg-slate-850 hover:border-slate-850 cursor-pointer rounded-lg border border-slate-900 bg-slate-900 px-5 py-2 text-xs font-extrabold text-white transition-all active:scale-95"
					>
						Confirm Postpone
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
