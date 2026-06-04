<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any, svelte/no-navigation-without-resolve, svelte/require-each-key */
	import { enhance } from '$app/forms';

	let { data, form } = $props<{ data: any; form: any }>();

	const venueId = $derived(data.venue?.id || '');

	const venue = $derived(data.venue || {});
	const manifests = $derived(data.manifests || []);

	// Clone Manifest modal binds
	let showCloneModal = $state(false);
	let targetManifestId = $state('');
	let cloneManifestCode = $state('');
	let cloneManifestDesc = $state('');

	// Expandable details (levels, sections, price levels)
	let loadingDetails = $state<Record<string, boolean>>({});
	let manifestDetails = $state<
		Record<string, { levels: any[]; sections: any[]; priceLevels: any[] }>
	>({});
	let expandedManifestId = $state<string | null>(null);

	async function toggleDetails(manifestId: string) {
		if (expandedManifestId === manifestId) {
			expandedManifestId = null;
			return;
		}

		expandedManifestId = manifestId;
		if (manifestDetails[manifestId]) return; // already loaded

		loadingDetails = { ...loadingDetails, [manifestId]: true };
		try {
			const [levelsRes, sectionsRes, priceLevelsRes] = await Promise.all([
				fetch(`/api/ops/venues/manifests/${manifestId}/levels`).then((r) => r.json()),
				fetch(`/api/ops/venues/manifests/${manifestId}/sections`).then((r) => r.json()),
				fetch(`/api/ops/venues/manifests/${manifestId}/price-levels`).then((r) => r.json())
			]);

			manifestDetails = {
				...manifestDetails,
				[manifestId]: {
					levels: levelsRes.success ? levelsRes.data : [],
					sections: sectionsRes.success ? sectionsRes.data : [],
					priceLevels: priceLevelsRes.success ? priceLevelsRes.data : []
				}
			};
		} catch (err) {
			console.error(err);
		} finally {
			loadingDetails = { ...loadingDetails, [manifestId]: false };
		}
	}
</script>

<svelte:head>
	<title>Seating Manifests - {venue?.name || 'Venue'} | Ticketpeak Admin</title>
</svelte:head>

<div class="space-y-6 p-6">
	<!-- Alerts -->
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

	<!-- Breadcrumbs and Header -->
	<div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
		<div class="space-y-1">
			<div class="flex items-center gap-1.5 text-xs font-semibold text-[#71717A] select-none">
				<a href="/ops/venues" class="transition-colors hover:text-[#111111]">Venues</a>
				<span>/</span>
				<span class="font-bold text-[#111111]">{venue?.name || 'Venue Details'}</span>
				<span>/</span>
				<span class="text-[#71717A]">Manifests</span>
			</div>
			<h2 class="text-xl font-bold tracking-tight text-[#111111] select-none">Seating Manifests</h2>
			<p class="text-xs font-medium text-[#71717A]">
				Configure levels, sections, price layouts, and seating charts for {venue?.name}.
			</p>
		</div>

		<div class="flex items-center gap-2">
			<a
				href="/ops/venues"
				class="rounded-md border border-[#E4E4E7] bg-white px-4 py-2 text-xs font-bold text-[#111111] shadow-xs transition hover:bg-[#FAFAFA]"
			>
				Back to Venues
			</a>
			<a
				href="/ops/venues/{venueId}/manifests/new"
				class="rounded-md border border-[#111111] bg-[#111111] px-4 py-2 text-xs font-bold text-white shadow-xs transition hover:bg-black active:scale-95"
			>
				Create Manifest
			</a>
		</div>
	</div>

	<!-- Venue Summary Card -->
	<div
		class="overflow-hidden rounded-lg border border-[#E4E4E7] bg-white p-6 shadow-xs select-none"
	>
		<div class="flex items-start gap-4">
			{#if venue?.thumbnailUrl}
				<img
					src={venue.thumbnailUrl}
					alt={venue.name}
					class="h-12 w-12 rounded-lg border border-[#E4E4E7] object-cover"
				/>
			{:else}
				<div
					class="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-sm font-bold text-slate-700 uppercase"
				>
					{venue?.name?.[0] || 'V'}
				</div>
			{/if}
			<div class="flex-1 space-y-1">
				<h3 class="text-sm font-bold text-[#111111]">{venue?.name}</h3>
				<div class="grid grid-cols-1 gap-2 text-xs text-[#71717A] sm:grid-cols-3">
					<div>
						<span class="font-semibold text-slate-400">Address:</span>
						{venue?.address || 'N/A'}, {venue?.city || ''}
					</div>
					<div>
						<span class="font-semibold text-slate-400">Contact:</span>
						{venue?.email || 'No email'} · {venue?.phone || 'No phone'}
					</div>
					<div>
						<span class="font-semibold text-slate-400">Coordinates:</span>
						{venue?.latitude?.toFixed(5) || 'N/A'}, {venue?.longitude?.toFixed(5) || 'N/A'}
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- Manifests Table -->
	<div class="animate-fade-in overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
		<div class="border-b border-[#F4F4F5] px-6 py-4">
			<h4 class="font-sans text-sm font-semibold text-[#111111] select-none">Seating Layouts</h4>
		</div>

		<div class="overflow-x-auto">
			<table class="w-full border-collapse text-left text-xs font-semibold text-[#71717A]">
				<thead>
					<tr
						class="border-b border-[#E4E4E7] bg-[#FAFAFA] text-[10px] text-[#71717A] uppercase select-none"
					>
						<th class="px-6 py-3.5">Code</th>
						<th class="px-6 py-3.5">Description</th>
						<th class="px-6 py-3.5">Total Capacity</th>
						<th class="px-6 py-3.5">Status</th>
						<th class="px-6 py-3.5">Created At</th>
						<th class="px-6 py-3.5 text-right">Actions</th>
					</tr>
				</thead>
				<tbody class="divide-y divide-[#F4F4F5] bg-white text-[#111111]">
					{#each manifests as manifest (manifest.id)}
						{@const isExpanded = expandedManifestId === manifest.id}
						<tr class="border-b border-slate-100 transition-colors hover:bg-[#FAFAFA]">
							<td class="px-6 py-4 font-mono font-bold text-slate-900">
								<button
									type="button"
									onclick={() => toggleDetails(manifest.id)}
									class="flex cursor-pointer items-center gap-1.5 text-left outline-none hover:text-blue-600"
								>
									<span
										class="inline-block transition-transform duration-200 {isExpanded
											? 'rotate-90'
											: ''}"
									>
										▸
									</span>
									{manifest.id}
								</button>
							</td>
							<td class="px-6 py-4 font-medium text-[#71717A]">{manifest.description}</td>
							<td class="px-6 py-4 font-mono font-bold text-[#111111]"
								>{manifest.totalCapacity?.toLocaleString() || 0}</td
							>
							<td class="px-6 py-4">
								<span
									class="inline-block rounded-md px-2.5 py-0.5 text-[9px] font-bold uppercase select-none {manifest.status ===
									'PUBLISHED'
										? 'bg-emerald-50 text-emerald-600'
										: manifest.status === 'DRAFT'
											? 'bg-amber-50 text-amber-600'
											: 'bg-slate-100 text-slate-500'}"
								>
									{manifest.status}
								</span>
							</td>
							<td class="px-6 py-4 font-medium text-[#71717A]"
								>{new Date(manifest.createdAt).toLocaleDateString()}</td
							>
							<td class="space-x-1 px-6 py-4 text-right">
								{#if manifest.status === 'DRAFT'}
									<form method="POST" action="?/publishManifest" use:enhance class="inline-block">
										<input type="hidden" name="manifestId" value={manifest.id} />
										<button
											type="submit"
											class="cursor-pointer rounded-md border border-emerald-100 bg-emerald-50/50 px-3 py-1.5 text-xs font-bold text-emerald-600 transition hover:bg-emerald-600 hover:text-white active:scale-95"
										>
											Publish
										</button>
									</form>
								{/if}
								{#if manifest.status === 'PUBLISHED'}
									<form method="POST" action="?/archiveManifest" use:enhance class="inline-block">
										<input type="hidden" name="manifestId" value={manifest.id} />
										<button
											type="submit"
											class="cursor-pointer rounded-md border border-rose-100 bg-rose-50/50 px-3 py-1.5 text-xs font-bold text-rose-600 transition hover:bg-rose-600 hover:text-white active:scale-95"
										>
											Archive
										</button>
									</form>
								{/if}
								<button
									type="button"
									onclick={() => {
										targetManifestId = manifest.id;
										cloneManifestCode = `${manifest.id}-CLONE`;
										cloneManifestDesc = `${manifest.description} (Cloned)`;
										showCloneModal = true;
									}}
									class="cursor-pointer rounded-md border border-[#E4E4E7] bg-white px-3 py-1.5 text-xs font-bold text-[#111111] transition hover:bg-[#FAFAFA] active:scale-95"
								>
									Clone
								</button>
							</td>
						</tr>

						<!-- Expanded seating info (levels, sections, price levels) -->
						{#if isExpanded}
							<tr class="bg-slate-50/40">
								<td colspan="6" class="px-8 py-4">
									{#if loadingDetails[manifest.id]}
										<div class="flex items-center gap-2 py-2 text-xs font-semibold text-[#71717A]">
											<svg
												class="h-4 w-4 animate-spin text-[#71717A]"
												fill="none"
												viewBox="0 0 24 24"
											>
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
											<span>Loading seating details...</span>
										</div>
									{:else if manifestDetails[manifest.id]}
										{@const details = manifestDetails[manifest.id]}
										<div class="grid grid-cols-1 gap-6 sm:grid-cols-3">
											<!-- Levels Info -->
											<div class="rounded-lg border border-slate-100 bg-white p-4 shadow-2xs">
												<h5
													class="mb-2 text-[10px] font-bold tracking-wider text-slate-400 uppercase select-none"
												>
													Levels
												</h5>
												{#if details.levels.length > 0}
													<ul class="space-y-1.5 text-xs font-semibold text-slate-700">
														{#each details.levels as level}
															<li
																class="flex items-center justify-between border-b border-slate-100 pb-1"
															>
																<span>{level.name}</span>
																<span
																	class="rounded bg-slate-100 px-1.5 py-0.5 font-mono text-[10px] text-slate-500"
																	>{level.code}</span
																>
															</li>
														{/each}
													</ul>
												{:else}
													<p class="text-xs font-medium text-slate-400 italic">
														No levels configured.
													</p>
												{/if}
											</div>

											<!-- Sections Info -->
											<div class="rounded-lg border border-slate-100 bg-white p-4 shadow-2xs">
												<h5
													class="mb-2 text-[10px] font-bold tracking-wider text-slate-400 uppercase select-none"
												>
													Sections
												</h5>
												{#if details.sections.length > 0}
													<ul class="space-y-1.5 text-xs font-semibold text-slate-700">
														{#each details.sections as section}
															<li
																class="flex items-center justify-between border-b border-slate-100 pb-1"
															>
																<span>{section.name}</span>
																<span
																	class="rounded bg-slate-100 px-1.5 py-0.5 font-mono text-[10px] text-slate-500"
																	>{section.code}</span
																>
															</li>
														{/each}
													</ul>
												{:else}
													<p class="text-xs font-medium text-slate-400 italic">
														No sections configured.
													</p>
												{/if}
											</div>

											<!-- Price Levels Info -->
											<div class="rounded-lg border border-slate-100 bg-white p-4 shadow-2xs">
												<h5
													class="mb-2 text-[10px] font-bold tracking-wider text-slate-400 uppercase select-none"
												>
													Price Tiers
												</h5>
												{#if details.priceLevels.length > 0}
													<ul class="space-y-1.5 text-xs font-semibold text-slate-700">
														{#each details.priceLevels as price}
															<li
																class="flex items-center justify-between border-b border-slate-100 pb-1"
															>
																<span style="color: {price.color || '#3b82f6'}">● {price.name}</span
																>
																<span
																	class="rounded bg-slate-100 px-1.5 py-0.5 font-mono text-[10px] text-slate-500"
																	>{price.code}</span
																>
															</li>
														{/each}
													</ul>
												{:else}
													<p class="text-xs font-medium text-slate-400 italic">
														No price levels configured.
													</p>
												{/if}
											</div>
										</div>
									{/if}
								</td>
							</tr>
						{/if}
					{:else}
						<tr>
							<td colspan="6" class="p-12 text-center text-[#71717A] font-medium"
								>No seating manifests configured for this venue.</td
							>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	</div>
</div>

<!-- ======================== CLONE MANIFEST DIALOG MODAL ======================== -->
{#if showCloneModal}
	<div class="fixed inset-0 z-50 flex items-start justify-center bg-black/40 px-4 pt-[15vh]">
		<div
			class="animate-scale-up h-auto max-h-[80vh] w-[90%] max-w-[480px] overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white p-6 text-left shadow-xl select-none"
		>
			<h3 class="text-lg font-bold tracking-tight text-[#09090B]">Clone Seating Manifest</h3>
			<p class="mt-1 text-xs text-[#71717A]">
				Duplicate this layout (including all price tiers, sections, levels, and seats) under a new
				ID.
			</p>

			<form
				method="POST"
				action="?/cloneManifest"
				use:enhance={() => {
					return async ({ result, update }) => {
						if (result.type === 'success') {
							showCloneModal = false;
						}
						await update();
					};
				}}
				class="mt-4 space-y-4"
			>
				<input type="hidden" name="manifestId" value={targetManifestId} />

				<div class="space-y-1">
					<label for="clone-code" class="text-xs font-semibold text-[#71717A]"
						>New Manifest Code *</label
					>
					<input
						type="text"
						id="clone-code"
						name="newId"
						bind:value={cloneManifestCode}
						required
						placeholder="e.g. M001-CLONE"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label for="clone-desc" class="text-xs font-semibold text-[#71717A]">Description *</label>
					<input
						type="text"
						id="clone-desc"
						name="description"
						bind:value={cloneManifestDesc}
						required
						placeholder="e.g. Concert Layout (Copy)"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="flex items-center justify-between gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showCloneModal = false)}
						class="cursor-pointer rounded-md border border-[#E4E4E7] bg-transparent px-5 py-2 text-xs font-bold text-[#71717A] transition-all hover:bg-[#FAFAFA] hover:text-[#111111]"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-5 py-2 text-xs font-bold text-white transition-all hover:border-black hover:bg-black active:scale-95"
					>
						Clone Layout
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
