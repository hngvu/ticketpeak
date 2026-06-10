<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */

	let { data } = $props<{ data: any }>();

	const venue = $derived(data.venue || {});
</script>

<svelte:head>
	<title>{venue.name || 'Venue'} | Ticketpeak Platform Admin</title>
</svelte:head>

<div class="space-y-6 p-6">
	<!-- Breadcrumb -->
	<div class="flex items-center gap-1.5 text-xs font-semibold text-[#71717A] select-none">
		<a href="/ops/venues" class="transition-colors hover:text-[#111111]">Venues</a>
		<span class="text-[#D4D4D8]">/</span>
		<span class="text-[#111111]">{venue.name}</span>
	</div>

	<!-- Header -->
	<div class="overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
		<div class="flex flex-col gap-6 p-6 sm:flex-row sm:items-start sm:gap-8">
			{#if venue.thumbnailUrl}
				<img
					src={venue.thumbnailUrl}
					alt={venue.name}
					class="h-24 w-24 shrink-0 rounded-xl border border-[#E4E4E7] object-cover sm:h-32 sm:w-32"
				/>
			{:else}
				<div
					class="flex h-24 w-24 shrink-0 items-center justify-center rounded-xl bg-slate-100 text-2xl font-bold text-slate-700 uppercase sm:h-32 sm:w-32"
				>
					{venue.name?.[0] || 'V'}
				</div>
			{/if}
			<div class="flex min-w-0 flex-1 flex-col gap-2">
				<div class="flex flex-wrap items-center gap-3">
					<h1 class="truncate text-xl font-bold tracking-tight text-[#111111]">{venue.name}</h1>
					{#if venue.status !== 'ACTIVE'}
						<span
							class="inline-block shrink-0 rounded-md bg-rose-50 px-2.5 py-0.5 text-[9px] font-bold text-rose-600 uppercase select-none"
						>
							{venue.status}
						</span>
					{/if}
				</div>
				{#if venue.description}
					<p class="text-sm leading-relaxed text-[#71717A]">{venue.description}</p>
				{/if}
				<div class="mt-auto flex items-center gap-3 pt-2">
					<a
						href="/ops/venues/{venue.id}/manifests"
						class="inline-flex cursor-pointer items-center gap-1.5 rounded-md border border-[#111111] bg-[#111111] px-4 py-2 text-xs font-bold text-white shadow-xs transition hover:bg-black active:scale-95"
					>
						Manifests
					</a>
				</div>
			</div>
		</div>
	</div>

	<!-- Two-column content -->
	<div class="grid gap-6 lg:grid-cols-3">
		<!-- Left: Info -->
		<div class="overflow-hidden rounded-lg border border-[#E4E4E7] bg-white lg:col-span-2">
			<div class="border-b border-[#F4F4F5] px-6 py-4">
				<h3 class="text-sm font-semibold text-[#111111] select-none">Venue Information</h3>
			</div>
			<div class="divide-y divide-[#F4F4F5]">
				{#if venue.address || venue.city || venue.country}
					<div class="grid grid-cols-3 gap-4 px-6 py-4">
						<span class="text-xs font-semibold text-[#71717A]">Address</span>
						<span class="col-span-2 text-sm font-medium text-[#111111]">
							{venue.address || ''}{venue.address && (venue.city || venue.country)
								? ', '
								: ''}{venue.city || ''}{venue.city && venue.country ? ', ' : ''}{venue.country ||
								''}
						</span>
					</div>
				{/if}
				{#if venue.phone}
					<div class="grid grid-cols-3 gap-4 px-6 py-4">
						<span class="text-xs font-semibold text-[#71717A]">Phone</span>
						<span class="col-span-2 text-sm font-medium text-[#111111]">{venue.phone}</span>
					</div>
				{/if}
				{#if venue.email}
					<div class="grid grid-cols-3 gap-4 px-6 py-4">
						<span class="text-xs font-semibold text-[#71717A]">Email</span>
						<span class="col-span-2 text-sm font-medium text-[#111111]">{venue.email}</span>
					</div>
				{/if}
				{#if venue.website}
					<div class="grid grid-cols-3 gap-4 px-6 py-4">
						<span class="text-xs font-semibold text-[#71717A]">Website</span>
						<a
							href={venue.website}
							target="_blank"
							rel="noopener noreferrer"
							class="col-span-2 text-sm font-medium text-blue-600 hover:underline"
						>
							{venue.website}
						</a>
					</div>
				{/if}
				{#if venue.latitude != null && venue.longitude != null}
					<div class="grid grid-cols-3 gap-4 px-6 py-4">
						<span class="text-xs font-semibold text-[#71717A]">Coordinates</span>
						<span class="col-span-2 font-mono text-sm font-medium text-[#111111]">
							{venue.latitude}, {venue.longitude}
						</span>
					</div>
				{/if}
			</div>
		</div>

		<!-- Right: Meta sidebar -->
		<div class="overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
			<div class="border-b border-[#F4F4F5] px-6 py-4">
				<h3 class="text-sm font-semibold text-[#111111] select-none">Details</h3>
			</div>
			<div class="divide-y divide-[#F4F4F5]">
				<div class="px-6 py-4">
					<div class="space-y-1">
						<span class="text-[10px] font-semibold tracking-wider text-[#71717A] uppercase"
							>Status</span
						>
						<div>
							<span
								class="inline-block rounded-md px-2.5 py-0.5 text-[9px] font-bold uppercase {venue.status ===
								'ACTIVE'
									? 'bg-emerald-50 text-emerald-600'
									: 'bg-rose-50 text-rose-600'}"
							>
								{venue.status || 'INACTIVE'}
							</span>
						</div>
					</div>
				</div>
				{#if venue.createdAt}
					<div class="px-6 py-4">
						<div class="space-y-1">
							<span class="text-[10px] font-semibold tracking-wider text-[#71717A] uppercase"
								>Created</span
							>
							<div class="text-xs font-medium text-[#111111]">
								{new Date(venue.createdAt).toLocaleDateString('en-GB', {
									day: 'numeric',
									month: 'short',
									year: 'numeric'
								})}
							</div>
						</div>
					</div>
				{/if}
				{#if venue.updatedAt}
					<div class="px-6 py-4">
						<div class="space-y-1">
							<span class="text-[10px] font-semibold tracking-wider text-[#71717A] uppercase"
								>Updated</span
							>
							<div class="text-xs font-medium text-[#111111]">
								{new Date(venue.updatedAt).toLocaleDateString('en-GB', {
									day: 'numeric',
									month: 'short',
									year: 'numeric'
								})}
							</div>
						</div>
					</div>
				{/if}
			</div>
		</div>
	</div>
</div>
