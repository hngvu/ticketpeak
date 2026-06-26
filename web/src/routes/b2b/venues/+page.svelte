<script lang="ts">
	import { IconMapPin, IconUsers, IconCalendarEvent } from '@tabler/icons-svelte';

	let { data } = $props();
	const venues = $derived(data.venues || []);
</script>

<svelte:head>
	<title>Venue Manifests — Ticketpeak</title>
</svelte:head>

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-8 p-6">
	<!-- Top Bar -->
	<div class="flex items-center justify-between">
		<div>
			<h1 class="text-2xl font-bold text-slate-900 md:text-3xl">Venue Manifests</h1>
			<p class="text-sm font-medium text-slate-500">
				Manage and monitor your venue layouts, capacity, and status.
			</p>
		</div>
	</div>

	{#if data.error}
		<div class="rounded-lg bg-red-50 p-4 text-sm font-medium text-red-600">
			{data.error}
		</div>
	{/if}

	<!-- Venues Grid -->
	{#if venues.length === 0 && !data.error}
		<div
			class="flex h-64 flex-col items-center justify-center rounded-2xl border border-dashed border-slate-300 bg-white/50"
		>
			<IconMapPin size={48} class="mb-4 text-slate-300" stroke={1.5} />
			<h3 class="text-lg font-semibold text-slate-800">No Venues Found</h3>
			<p class="mt-1 text-sm text-slate-500">You don't have any venues assigned to you yet.</p>
		</div>
	{:else}
		<div class="flex flex-col">
			{#each venues as venue}
				<a
					href={`/b2b/venues/${venue.id}`}
					class="group flex cursor-pointer items-center justify-between border-b border-slate-200 py-4 transition-colors hover:bg-slate-50/50"
				>
					<div class="flex items-center gap-6">
						<!-- Icon Avatar -->
						<div
							class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-slate-100 text-slate-500"
						>
							<IconMapPin size={20} stroke={2} />
						</div>

						<!-- Main Title Block -->
						<div class="flex w-96 shrink-0 flex-col gap-0.5">
							<span
								class="text-[14px] font-bold text-blue-700 transition-colors group-hover:text-blue-800 group-hover:underline"
							>
								{venue.name}
							</span>
							<p class="truncate text-[13px] font-medium text-slate-500">
								{venue.address} &bull; {venue.city}
							</p>
						</div>
					</div>
				</a>
			{/each}
		</div>
	{/if}
</div>
