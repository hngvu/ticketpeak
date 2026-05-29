<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { enhance } from '$app/forms';
	import EmptyState from '$lib/components/common/EmptyState.svelte';

	let { data, form } = $props();

	// Svelte 5 States
	let selectedOrgId = $state(data.selectedOrgId);
	let isCreateModalOpen = $state(false);
	let isCloneModalOpen = $state(false);
	let cloneEventId = $state('');
	let cloneEventTitle = $state('');
	let loading = $state(false);

	// Event stats summaries
	const totalEvents = $derived(data.events?.length || 0);
	const activeEvents = $derived(
		data.events?.filter(
			(e: { status: string }) => e.status === 'SALES_ACTIVE' || e.status === 'PUBLISHED'
		).length || 0
	);

	// Helper to format date/time
	function formatDateTime(isoString: string) {
		if (!isoString) return 'N/A';
		const date = new Date(isoString);
		return date.toLocaleDateString('vi-VN', {
			day: '2-digit',
			month: '2-digit',
			year: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	}

	function handleOrgChange(e: Event) {
		const target = e.target as HTMLSelectElement;
		selectedOrgId = target.value;
		window.location.href = `?organizationId=${selectedOrgId}`;
	}

	function openCloneModal(eventId: string, eventTitle: string) {
		cloneEventId = eventId;
		cloneEventTitle = `Copy of ${eventTitle}`;
		isCloneModalOpen = true;
	}
</script>

<svelte:head>
	<title>Organizer Dashboard — Ticketpeak for Business</title>
</svelte:head>

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-8 p-6">
	<!-- Top Bar / Switcher -->
	<div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
		<div>
			<h1 class="text-2xl font-extrabold text-slate-900 md:text-3xl dark:text-white">
				Event Dashboard
			</h1>
			<p class="text-sm font-medium text-slate-500">
				Manage events, view ticket sales metrics, and schedule gate scanners.
			</p>
		</div>

		<!-- Organization Switcher -->
		{#if data.organizations && data.organizations.length > 0}
			<div class="flex items-center gap-2">
				<label for="org-select" class="text-xs font-bold tracking-wider text-slate-500 uppercase">
					Organization:
				</label>
				<select
					id="org-select"
					value={selectedOrgId}
					onchange={handleOrgChange}
					class="rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm font-semibold text-slate-900 shadow-sm focus:border-primary focus:outline-none"
				>
					{#each data.organizations as org (org.id)}
						<option value={org.id}>{org.name}</option>
					{/each}
				</select>
			</div>
		{/if}
	</div>

	<!-- Status messages -->
	{#if form?.error}
		<div
			class="flex items-center gap-3 rounded-lg border border-red-200 bg-red-50 p-4 text-sm font-semibold text-red-700"
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
			class="flex items-center gap-3 rounded-lg border border-green-200 bg-green-50 p-4 text-sm font-semibold text-green-700"
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

	<!-- KPI Analytics Overview -->
	<div class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
		<!-- KPI 1 -->
		<div class="rounded-xl border border-hairline bg-canvas p-5 shadow-xs transition hover:shadow">
			<div class="flex items-center justify-between text-slate-400">
				<span class="text-xs font-bold tracking-wider uppercase">Total Events</span>
				<svg
					class="h-5 w-5 text-slate-400"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
					stroke-width="2"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
					/>
				</svg>
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-extrabold text-slate-900">{totalEvents}</span>
				<span class="text-xs font-medium text-slate-500">hosted events</span>
			</div>
		</div>

		<!-- KPI 2 -->
		<div class="rounded-xl border border-hairline bg-canvas p-5 shadow-xs transition hover:shadow">
			<div class="flex items-center justify-between text-slate-400">
				<span class="text-xs font-bold tracking-wider uppercase">Active Ticket Sales</span>
				<span class="flex h-2.5 w-2.5 animate-pulse rounded-full bg-success"></span>
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-extrabold text-slate-900">{activeEvents}</span>
				<span class="text-xs font-medium text-slate-500">active items</span>
			</div>
		</div>

		<!-- KPI 3 -->
		<div class="rounded-xl border border-hairline bg-canvas p-5 shadow-xs transition hover:shadow">
			<div class="flex items-center justify-between text-slate-400">
				<span class="text-xs font-bold tracking-wider uppercase">Estimated Revenue</span>
				<svg
					class="h-5 w-5 text-slate-400"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
					stroke-width="2"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M12 16v1"
					/>
				</svg>
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-extrabold text-slate-900">450.0M</span>
				<span class="text-xs font-semibold text-emerald-600">VND</span>
			</div>
		</div>

		<!-- KPI 4 -->
		<div class="rounded-xl border border-hairline bg-canvas p-5 shadow-xs transition hover:shadow">
			<div class="flex items-center justify-between text-slate-400">
				<span class="text-xs font-bold tracking-wider uppercase">Avg Check-In Rate</span>
				<svg
					class="h-5 w-5 text-slate-400"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
					stroke-width="2"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
					/>
				</svg>
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-extrabold text-slate-900">89.4%</span>
				<span class="text-xs font-medium text-slate-500">check-in rate</span>
			</div>
		</div>
	</div>

	<!-- Main Operations Grid -->
	<div class="flex flex-col gap-6 lg:flex-row">
		<!-- Left: Events Management -->
		<div class="flex-1 rounded-xl border border-hairline bg-canvas p-6 shadow-xs">
			<div class="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
				<div>
					<h2 class="text-lg font-bold text-slate-900">Events Catalog</h2>
					<p class="text-xs text-slate-500">Create, edit, publish, clone, and configure events.</p>
				</div>

				{#if data.selectedOrgId}
					<button
						onclick={() => (isCreateModalOpen = true)}
						class="flex cursor-pointer items-center justify-center gap-2 rounded-full bg-primary px-5 py-2.5 text-xs font-bold text-white shadow-sm transition hover:bg-primary/95 hover:shadow active:scale-95"
					>
						<svg
							class="h-4.5 w-4.5"
							fill="none"
							viewBox="0 0 24 24"
							stroke="currentColor"
							stroke-width="2.5"
						>
							<path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
						</svg>
						<span>Create Event</span>
					</button>
				{/if}
			</div>

			<!-- Events Listing -->
			{#if data.events && data.events.length > 0}
				<div class="overflow-x-auto">
					<table class="w-full text-left text-sm text-slate-500">
						<thead>
							<tr
								class="border-b border-hairline text-xs font-bold tracking-wider text-slate-400 uppercase"
							>
								<th class="py-3.5 pr-4">Event Details</th>
								<th class="px-4 py-3.5">Venue & City</th>
								<th class="px-4 py-3.5">Event Status</th>
								<th class="py-3.5 pl-4 text-right">Actions</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-hairline">
							{#each data.events as event (event.id)}
								<tr class="transition hover:bg-slate-50/50">
									<!-- Title & Dates -->
									<td class="py-4 pr-4">
										<a
											href="/b2b/dashboard/events/{event.id}"
											class="font-bold text-slate-900 transition hover:text-primary"
										>
											{event.title}
										</a>
										<div class="mt-1 text-xs font-medium text-slate-400">
											📅 {formatDateTime(event.startAt)}
										</div>
									</td>

									<!-- Venue -->
									<td class="px-4 py-4">
										<span class="font-semibold text-slate-700">
											{data.venues.find((v: { id: string; name: string }) => v.id === event.venueId)
												?.name || 'Default Venue'}
										</span>
										<div class="text-xs text-slate-400">
											📍 {data.venues.find(
												(v: { id: string; city: string }) => v.id === event.venueId
											)?.city || 'Ho Chi Minh, VN'}
										</div>
									</td>

									<!-- Status -->
									<td class="px-4 py-4">
										{#if event.status === 'DRAFT'}
											<span
												class="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-bold text-slate-700 uppercase"
											>
												Draft
											</span>
										{:else if event.status === 'PUBLISHED'}
											<span
												class="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-bold text-blue-700 uppercase"
											>
												Published
											</span>
										{:else if event.status === 'SALES_ACTIVE'}
											<span
												class="inline-flex items-center rounded-full bg-green-50 px-2.5 py-0.5 text-xs font-bold text-green-700 uppercase"
											>
												Sales Active
											</span>
										{:else if event.status === 'CANCELLED'}
											<span
												class="inline-flex items-center rounded-full bg-red-50 px-2.5 py-0.5 text-xs font-bold text-red-700 uppercase"
											>
												Cancelled
											</span>
										{:else if event.status === 'POSTPONED'}
											<span
												class="inline-flex items-center rounded-full bg-amber-50 px-2.5 py-0.5 text-xs font-bold text-amber-700 uppercase"
											>
												Postponed
											</span>
										{:else}
											<span
												class="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-bold text-slate-700 uppercase"
											>
												{event.status}
											</span>
										{/if}
									</td>

									<!-- Actions -->
									<td class="py-4 pl-4 text-right">
										<div class="flex items-center justify-end gap-2.5">
											<!-- Scan Gate simulator -->
											<a
												href="/b2b/check-in/{event.id}"
												class="inline-flex items-center gap-1 rounded-full border border-purple-200 bg-purple-50 px-3 py-1.5 text-xs font-bold text-purple-700 transition hover:bg-purple-100"
												title="Access Ticket Check-In Simulator"
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
													<path
														stroke-linecap="round"
														stroke-linejoin="round"
														d="M15 15h.008v.008H15V15zm0 3h.008v.008H15V18zm3-3h.008v.008H18V15zm0 3h.008v.008H18V18zm-3-3h.008v.008H15V15zm0 3h.008v.008H15V18z"
													/>
												</svg>
												<span>Scanner</span>
											</a>

											<!-- Details edit link -->
											<a
												href="/b2b/dashboard/events/{event.id}"
												class="inline-flex items-center gap-1 rounded-full border border-slate-200 bg-white px-3 py-1.5 text-xs font-bold text-slate-700 transition hover:bg-slate-100"
											>
												Edit
											</a>

											<!-- Contextual Lifecycle Button Forms -->
											{#if event.status === 'DRAFT'}
												<form
													method="POST"
													action="?/publishEvent"
													use:enhance={() => {
														loading = true;
														return async ({ update }) => {
															await update();
															loading = false;
														};
													}}
												>
													<input type="hidden" name="id" value={event.id} />
													<button
														type="submit"
														class="cursor-pointer rounded-full bg-blue-600 px-3.5 py-1.5 text-xs font-bold text-white transition hover:bg-blue-700"
													>
														Publish
													</button>
												</form>
											{:else if event.status === 'PUBLISHED'}
												<form
													method="POST"
													action="?/startSales"
													use:enhance={() => {
														loading = true;
														return async ({ update }) => {
															await update();
															loading = false;
														};
													}}
												>
													<input type="hidden" name="id" value={event.id} />
													<button
														type="submit"
														class="cursor-pointer rounded-full bg-emerald-600 px-3.5 py-1.5 text-xs font-bold text-white transition hover:bg-emerald-700"
													>
														Open Sales
													</button>
												</form>
											{/if}

											<!-- Clone Trigger -->
											<button
												onclick={() => openCloneModal(event.id, event.title)}
												class="cursor-pointer rounded-full border border-hairline bg-white p-1.5 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
												title="Clone event"
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
														d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2"
													/>
												</svg>
											</button>
										</div>
									</td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			{:else}
				<EmptyState
					title="No Events Found"
					message="This organization hasn't created any events yet. Get started by clicking Create Event above."
					actionHref="#"
					actionText=""
				/>
			{/if}
		</div>
	</div>
</div>

<!-- ======================== CREATE EVENT SLIDEOUT MODAL ======================== -->
{#if isCreateModalOpen}
	<div class="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-xs transition">
		<div
			class="flex h-full w-full max-w-lg flex-col overflow-y-auto bg-canvas p-6 shadow-2xl"
			role="dialog"
		>
			<div class="mb-6 flex items-center justify-between border-b border-hairline pb-4">
				<h3 class="text-lg font-bold text-slate-900">Create New Event</h3>
				<button
					onclick={() => (isCreateModalOpen = false)}
					class="cursor-pointer rounded-full p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
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
				action="?/createEvent"
				use:enhance={() => {
					loading = true;
					return async ({ update }) => {
						await update();
						loading = false;
						isCreateModalOpen = false;
					};
				}}
				class="flex-1 space-y-5"
			>
				<input type="hidden" name="organizationId" value={selectedOrgId} />

				<!-- Title -->
				<div>
					<label
						for="event-title"
						class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
					>
						Event Title *
					</label>
					<input
						type="text"
						id="event-title"
						name="title"
						required
						placeholder="e.g. Pop Music Festival 2026"
						class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
					/>
				</div>

				<!-- Description -->
				<div>
					<label
						for="event-desc"
						class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
					>
						Description
					</label>
					<textarea
						id="event-desc"
						name="description"
						rows="4"
						placeholder="Enter event details, schedule, highlights..."
						class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
					></textarea>
				</div>

				<!-- Venue Selector -->
				<div>
					<label
						for="event-venue"
						class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
					>
						Hosting Venue *
					</label>
					<select
						id="event-venue"
						name="venueId"
						required
						class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
					>
						<option value="" disabled selected>-- Select a Venue --</option>
						{#each data.venues as venue (venue.id)}
							<option value={venue.id}>{venue.name} ({venue.city}, {venue.countryCode})</option>
						{/each}
					</select>
				</div>

				<!-- Category -->
				<div>
					<label
						for="event-category"
						class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
					>
						Category / Classification
					</label>
					<select
						id="event-category"
						name="classificationId"
						class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
					>
						<option value="">-- Select a Category (Optional) --</option>
						{#each data.classifications as cat (cat.id)}
							<option value={cat.id}>{cat.name}</option>
						{/each}
					</select>
				</div>

				<!-- Date Range -->
				<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
					<div>
						<label
							for="event-start"
							class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
						>
							Start Date & Time *
						</label>
						<input
							type="datetime-local"
							id="event-start"
							name="startAt"
							required
							class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
						/>
					</div>
					<div>
						<label
							for="event-end"
							class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
						>
							End Date & Time
						</label>
						<input
							type="datetime-local"
							id="event-end"
							name="endAt"
							class="w-full rounded-lg border border-hairline bg-canvas px-3 py-2 text-sm text-slate-900 shadow-inner focus:border-primary focus:outline-none"
						/>
					</div>
				</div>

				<!-- Timezone -->
				<div>
					<label
						for="event-tz"
						class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
					>
						Timezone
					</label>
					<input
						type="text"
						id="event-tz"
						name="timezone"
						value="Asia/Ho_Chi_Minh"
						readonly
						class="w-full rounded-lg border border-hairline bg-slate-50 px-3 py-2 text-sm text-slate-400 outline-none"
					/>
				</div>

				<!-- Form Actions -->
				<div class="flex items-center justify-end gap-3 border-t border-hairline pt-6">
					<button
						type="button"
						onclick={() => (isCreateModalOpen = false)}
						class="cursor-pointer rounded-full border border-hairline bg-white px-5 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-50 active:scale-95"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={loading}
						class="flex cursor-pointer items-center justify-center gap-1.5 rounded-full bg-primary px-6 py-2.5 text-sm font-bold text-white transition hover:bg-primary/95 active:scale-95 disabled:opacity-50"
					>
						{#if loading}
							<span
								class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"
							></span>
						{/if}
						<span>Save Draft</span>
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- ======================== CLONE EVENT MODAL ======================== -->
{#if isCloneModalOpen}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs">
		<div class="w-full max-w-md rounded-xl bg-canvas p-6 shadow-2xl" role="dialog">
			<div class="mb-4 flex items-center justify-between border-b border-hairline pb-3">
				<h3 class="text-base font-bold text-slate-900">Clone Event</h3>
				<button
					onclick={() => (isCloneModalOpen = false)}
					class="cursor-pointer rounded-full p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
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
						class="cursor-pointer rounded-full border border-hairline bg-white px-4 py-2 text-xs font-bold text-slate-600 hover:bg-slate-50"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={loading}
						class="flex cursor-pointer items-center justify-center gap-1 rounded-full bg-primary px-5 py-2 text-xs font-bold text-white transition hover:bg-primary/95 disabled:opacity-50"
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
