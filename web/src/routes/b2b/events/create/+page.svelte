<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { enhance } from '$app/forms';

	let { data, form } = $props();

	// Svelte 5 states
	let selectedOrgId = $state(data.selectedOrgId);
	let loading = $state(false);

	function handleOrgChange(e: Event) {
		const target = e.target as HTMLSelectElement;
		selectedOrgId = target.value;
		window.location.href = `?organizationId=${selectedOrgId}`;
	}
</script>

<svelte:head>
	<title>Create New Event — Ticketpeak for Business</title>
</svelte:head>

<div class="mx-auto flex w-full max-w-3xl flex-1 flex-col space-y-8 p-6">
	<!-- Navigation & Title Header -->
	<div class="flex flex-col gap-4 border-b border-hairline pb-6">
		<a
			href="/b2b/events?organizationId={selectedOrgId}"
			class="flex items-center gap-1.5 text-xs font-bold text-slate-400 transition hover:text-slate-600"
		>
			<span>←</span>
			<span>Back to Events Catalog</span>
		</a>

		<div>
			<h1 class="text-2xl font-extrabold text-slate-900 md:text-3xl">Create New Event</h1>
			<p class="text-sm font-medium text-slate-500">
				Define basic details, showtimes schedule, and allocate the venue structure.
			</p>
		</div>
	</div>

	<!-- System notification banners -->
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

	<!-- Event Creation Form Box -->
	<div class="rounded-2xl border border-slate-100 bg-white p-6 shadow-xs">
		<form
			method="POST"
			use:enhance={() => {
				loading = true;
				return async ({ update }) => {
					await update();
					loading = false;
				};
			}}
			class="space-y-6"
		>
			<!-- Organization Scope Selector -->
			{#if data.organizations && data.organizations.length > 0}
				<div class="border-b border-slate-50 pb-4">
					<label
						for="org-select"
						class="mb-1.5 block text-xs font-bold tracking-wider text-slate-400 uppercase"
					>
						Hosting Organization
					</label>
					<select
						id="org-select"
						name="organizationId"
						value={selectedOrgId}
						onchange={handleOrgChange}
						class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-800 focus:border-blue-500 focus:outline-none"
					>
						{#each data.organizations as org (org.id)}
							<option value={org.id}>{org.name}</option>
						{/each}
					</select>
				</div>
			{/if}

			<!-- Event Title -->
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
					class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-800 placeholder-slate-400 transition-all focus:border-blue-500 focus:outline-none"
				/>
			</div>

			<!-- Description -->
			<div>
				<label
					for="event-desc"
					class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
				>
					Event Description
				</label>
				<textarea
					id="event-desc"
					name="description"
					rows="5"
					placeholder="Enter event details, schedule, highlights, promoter guidelines..."
					class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-800 placeholder-slate-400 transition-all focus:border-blue-500 focus:outline-none"
				></textarea>
			</div>

			<!-- Venue & Classification grids -->
			<div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
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
						class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-800 focus:border-blue-500 focus:outline-none"
					>
						<option value="" disabled selected>-- Select Venue --</option>
						{#each data.venues as venue (venue.id)}
							<option value={venue.id}>{venue.name} ({venue.city}, {venue.countryCode})</option>
						{/each}
					</select>
				</div>

				<!-- Classification Selector -->
				<div>
					<label
						for="event-category"
						class="mb-1.5 block text-xs font-bold tracking-wider text-slate-500 uppercase"
					>
						Classification Category
					</label>
					<select
						id="event-category"
						name="classificationId"
						class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-800 focus:border-blue-500 focus:outline-none"
					>
						<option value="">-- Select Category (Optional) --</option>
						{#each data.classifications as cat (cat.id)}
							<option value={cat.id}>{cat.name}</option>
						{/each}
					</select>
				</div>
			</div>

			<!-- Date details -->
			<div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
				<!-- Start Showtime -->
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
						class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-800 focus:border-blue-500 focus:outline-none"
					/>
				</div>

				<!-- End Showtime -->
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
						class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-800 focus:border-blue-500 focus:outline-none"
					/>
				</div>
			</div>

			<!-- Timezone block -->
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
					class="w-full cursor-not-allowed rounded-xl border border-slate-100 bg-slate-50 px-3.5 py-2.5 text-sm font-semibold text-slate-400 outline-none"
				/>
			</div>

			<!-- Form Actions -->
			<div class="flex items-center justify-end gap-3.5 border-t border-slate-100 pt-6">
				<a
					href="/b2b/events?organizationId={selectedOrgId}"
					class="rounded-full border border-slate-200 bg-white px-6 py-2.5 text-sm font-bold text-slate-600 transition hover:bg-slate-50 active:scale-95"
				>
					Cancel
				</a>
				<button
					type="submit"
					disabled={loading}
					class="flex cursor-pointer items-center justify-center gap-2 rounded-full bg-blue-600 px-7.5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-blue-700 active:scale-95 disabled:opacity-50"
				>
					{#if loading}
						<span
							class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"
						></span>
					{/if}
					<span>Create Event</span>
				</button>
			</div>
		</form>
	</div>
</div>
