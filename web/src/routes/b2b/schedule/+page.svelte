<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */

	let { data } = $props();

	// Svelte 5 states
	let selectedOrgId = $state(data.selectedOrgId);
	let currentMonth = $state(4); // May (0-indexed)
	let currentYear = $state(2026);

	interface B2BEvent {
		id: string;
		title: string;
		status: string;
		venueId: string;
		startAt: string;
		endAt?: string;
	}

	function handleOrgChange(e: Event) {
		const target = e.target as HTMLSelectElement;
		selectedOrgId = target.value;
		window.location.href = `?organizationId=${selectedOrgId}`;
	}

	// Helper to format date/time
	function formatTime(isoString: string) {
		if (!isoString) return 'N/A';
		const date = new Date(isoString);
		return date.toLocaleTimeString('vi-VN', {
			hour: '2-digit',
			minute: '2-digit'
		});
	}

	function formatDate(isoString: string) {
		if (!isoString) return 'N/A';
		const date = new Date(isoString);
		return date.toLocaleDateString('vi-VN', {
			day: '2-digit',
			month: '2-digit',
			year: 'numeric'
		});
	}

	// Doors open is usually 1.5 hours before startAt
	function getDoorsOpenTime(isoString: string) {
		if (!isoString) return 'N/A';
		const date = new Date(new Date(isoString).getTime() - 90 * 60 * 1000);
		return date.toLocaleTimeString('vi-VN', {
			hour: '2-digit',
			minute: '2-digit'
		});
	}

	// Calendar calculation helpers
	const monthNames = [
		'January',
		'February',
		'March',
		'April',
		'May',
		'June',
		'July',
		'August',
		'September',
		'October',
		'November',
		'December'
	];

	const daysInMonth = $derived(new Date(currentYear, currentMonth + 1, 0).getDate());
	const startDayOfWeek = $derived(new Date(currentYear, currentMonth, 1).getDay()); // 0 = Sunday

	const calendarCells = $derived.by(() => {
		const cells = [];
		// Pad initial empty cells
		for (let i = 0; i < startDayOfWeek; i++) {
			cells.push({ day: 0, hasEvent: false, eventsList: [] as B2BEvent[] });
		}
		// Populate days
		for (let day = 1; day <= daysInMonth; day++) {
			const matchingEvents =
				data.events?.filter((e: B2BEvent) => {
					const eDate = new Date(e.startAt);
					return (
						eDate.getDate() === day &&
						eDate.getMonth() === currentMonth &&
						eDate.getFullYear() === currentYear
					);
				}) || [];

			cells.push({
				day,
				hasEvent: matchingEvents.length > 0,
				eventsList: matchingEvents
			});
		}
		return cells;
	});

	function nextMonth() {
		if (currentMonth === 11) {
			currentMonth = 0;
			currentYear += 1;
		} else {
			currentMonth += 1;
		}
	}

	function prevMonth() {
		if (currentMonth === 0) {
			currentMonth = 11;
			currentYear -= 1;
		} else {
			currentMonth -= 1;
		}
	}
</script>

<svelte:head>
	<title>Showtimes Schedule & Calendar — Ticketpeak for Business</title>
</svelte:head>

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-8 p-6">
	<!-- Header block with Organizer selector -->
	<div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
		<div>
			<h1 class="text-2xl font-extrabold text-slate-900 md:text-3xl">Showtimes Schedule</h1>
			<p class="text-sm font-medium text-slate-500">
				Monitor door times, coordinate gate check-in shifts, and track show calendars.
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

	<!-- Main layout splits: Left Calendar, Right chronologically showtimes timeline -->
	<div class="grid grid-cols-1 gap-8 lg:grid-cols-3">
		<!-- Left: Calendar Grid Block (Col span 2) -->
		<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs lg:col-span-2">
			<div class="flex items-center justify-between border-b border-slate-100 pb-4">
				<h2 class="text-base font-bold text-slate-900">
					{monthNames[currentMonth]}
					{currentYear}
				</h2>

				<div class="flex items-center gap-2">
					<button
						onclick={prevMonth}
						class="rounded-lg border border-slate-200 p-2 transition hover:bg-slate-50"
						aria-label="Previous month"
					>
						←
					</button>
					<button
						onclick={nextMonth}
						class="rounded-lg border border-slate-200 p-2 transition hover:bg-slate-50"
						aria-label="Next month"
					>
						→
					</button>
				</div>
			</div>

			<!-- Days names -->
			<div
				class="mt-6 grid grid-cols-7 text-center text-xs font-bold tracking-wider text-slate-400 uppercase"
			>
				<span>Sun</span>
				<span>Mon</span>
				<span>Tue</span>
				<span>Wed</span>
				<span>Thu</span>
				<span>Fri</span>
				<span>Sat</span>
			</div>

			<!-- Calendar Grid Cells -->
			<div class="mt-4 grid grid-cols-7 gap-2">
				{#each calendarCells as cell, i (i)}
					<div
						class="flex min-h-24 flex-col justify-between rounded-xl border border-slate-100/75 p-2 transition-all {cell.day ===
						0
							? 'border-dashed bg-slate-50/45 opacity-30'
							: 'bg-slate-50/20 hover:bg-slate-50/70'} {cell.hasEvent
							? 'bg-blue-50/10 ring-1 ring-blue-100'
							: ''}"
					>
						{#if cell.day > 0}
							<span class="text-xs font-bold {cell.hasEvent ? 'text-blue-600' : 'text-slate-500'}">
								{cell.day}
							</span>

							{#if cell.hasEvent}
								<div class="flex flex-col gap-1 pr-0.5">
									{#each cell.eventsList as ev (ev.id)}
										<span
											class="block truncate rounded-md bg-blue-100/70 px-1.5 py-0.5 text-[9px] font-bold text-blue-700"
											title={ev.title}
										>
											{ev.title}
										</span>
									{/each}
								</div>
							{/if}
						{/if}
					</div>
				{/each}
			</div>
		</div>

		<!-- Right: Showtime Timeline -->
		<div
			class="flex flex-col justify-between rounded-2xl border border-slate-100 bg-white p-6 shadow-xs"
		>
			<div>
				<h2 class="text-base font-bold text-slate-900">Upcoming Showtimes</h2>
				<p class="text-xs text-slate-400">Scheduled door times & attendance rosters.</p>
			</div>

			<div class="mt-6 flex max-h-120 flex-1 flex-col gap-4 overflow-y-auto pr-1 select-none">
				{#if data.events && data.events.length > 0}
					{#each data.events as event (event.id)}
						<div
							class="flex flex-col gap-1.5 rounded-xl border border-slate-100 bg-slate-50/30 p-3.5 transition hover:bg-slate-50"
						>
							<div class="flex items-center justify-between">
								<span
									class="rounded-md bg-purple-50 px-2 py-0.5 text-[9px] font-bold text-purple-600 uppercase"
								>
									{event.status}
								</span>
								<span class="text-[9px] font-bold text-slate-400">
									📅 {formatDate(event.startAt)}
								</span>
							</div>

							<span class="line-clamp-1 text-xs font-bold text-slate-800">{event.title}</span>

							<div class="flex flex-col gap-0.5 text-[10px] font-semibold text-slate-500">
								<span
									>📍 {data.venues.find((v: { id: string }) => v.id === event.venueId)?.name ||
										'Default Venue'}</span
								>
								<span class="mt-0.5 flex items-center gap-1.5">
									<span class="rounded-sm bg-slate-100 px-1 font-bold text-slate-600"
										>Doors: {getDoorsOpenTime(event.startAt)}</span
									>
									<span class="rounded-sm bg-slate-100 px-1 font-bold text-slate-600"
										>Start: {formatTime(event.startAt)}</span
									>
								</span>
							</div>

							<div class="mt-2 flex items-center justify-between border-t border-slate-100 pt-2">
								<span class="text-[9px] font-bold text-slate-400">Expected: 800 fans</span>
								<a
									href="/b2b/check-in/{event.id}"
									class="inline-flex items-center gap-1 rounded-full bg-purple-600 px-2.5 py-1 text-[9px] font-bold text-white transition hover:bg-purple-700"
								>
									Scanner Gate
								</a>
							</div>
						</div>
					{/each}
				{:else}
					<div class="flex h-40 flex-col items-center justify-center text-center">
						<span class="text-xs font-bold text-slate-400">No events scheduled.</span>
					</div>
				{/if}
			</div>

			<div class="mt-4 border-t border-slate-100 pt-3">
				<a
					href="/b2b/events/create?organizationId={selectedOrgId || ''}"
					class="text-xs font-bold text-blue-600 transition hover:text-blue-700"
				>
					+ Schedule New Event
				</a>
			</div>
		</div>
	</div>
</div>
