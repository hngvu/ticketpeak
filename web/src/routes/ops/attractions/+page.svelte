<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';
	import ImageInput from '$lib/components/common/ImageInput.svelte';

	let { data, form } = $props<{ data: any; form: any }>();

	const attractions = $derived(data.attractions || []);

	let showAddAttractionModal = $state(false);

	// Form binds
	let attrName = $state('');
	let attrSlug = $state('');
	let attrType = $state('ARTIST');
	let attrImageUrl = $state('');
	let attrDescription = $state('');

	// Search & Filter binds
	let attrSearch = $state('');
	let attrTypeFilter = $state('all'); // 'all', 'ARTIST', 'VENUE', etc.

	function slugify(text: string) {
		return text
			.toString()
			.toLowerCase()
			.replace(/\s+/g, '-') // Replace spaces with -
			.replace(/[^\w\-]+/g, '') // Remove all non-word chars
			.replace(/\-\-+/g, '-') // Replace multiple - with single -
			.replace(/^-+/, '') // Trim - from start
			.replace(/-+$/, ''); // Trim - from end
	}

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
</script>

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
			<!-- Search and Filter controls -->
			<div class="flex flex-1 flex-col gap-3 sm:flex-row sm:items-center">
				<!-- Search Input -->
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
						placeholder="Search attractions..."
						bind:value={attrSearch}
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] py-2 pr-4 pl-9 text-xs font-semibold text-[#111111] placeholder-[#71717A] transition outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<!-- Filter dropdown -->
				<div class="flex items-center gap-2">
					<select
						bind:value={attrTypeFilter}
						class="rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3 py-2 text-xs font-semibold text-[#111111] transition outline-none focus:border-[#71717A] focus:bg-white"
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
				class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-4 py-2 text-center text-xs font-bold text-white shadow-xs transition-all hover:border-black hover:bg-black active:scale-95"
			>
				Create
			</button>
		</div>
		<div class="overflow-x-auto">
			<table class="w-full border-collapse text-left">
				<thead>
					<tr
						class="border-b border-[#E4E4E7] bg-white text-xs font-semibold tracking-wider text-[#71717A] uppercase"
					>
						<th class="px-6 py-3.5">Name</th>
						<th class="px-6 py-3.5">Slug</th>
						<th class="px-6 py-3.5">Type</th>
						<th class="px-6 py-3.5">Description</th>
					</tr>
				</thead>
				<tbody
					class="divide-y divide-slate-100 bg-white font-sans text-xs font-semibold text-[#71717A]"
				>
					{#each filteredAttractions as attr (attr.id)}
						<tr class="transition-colors hover:bg-[#FAFAFA]">
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
											class="flex h-8 w-8 items-center justify-center rounded-full bg-[#F4F4F5] text-[10px] font-bold text-[#71717A]"
										>
											{attr.name[0]?.toUpperCase()}
										</div>
									{/if}
									<div>
										<p class="text-sm font-bold text-[#111111]">{attr.name}</p>
									</div>
								</div>
							</td>
							<td class="px-6 py-4 font-mono text-[#71717A]">{attr.slug}</td>
							<td class="px-6 py-4">
								<span
									class="rounded bg-[#F4F4F5] px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-[#71717A] uppercase"
								>
									{attr.type}
								</span>
							</td>
							<td class="max-w-sm truncate px-6 py-4 font-normal text-[#71717A]">
								{attr.description || 'No description provided.'}
							</td>
						</tr>
					{:else}
						<tr>
							<td colspan="4" class="p-12 text-center text-[#71717A] font-medium">
								No attractions found matching your search criteria.
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	</div>
</div>

<!-- ======================== ADD ATTRACTION DIALOG MODAL ======================== -->
{#if showAddAttractionModal}
	<div class="fixed inset-0 z-50 flex items-start justify-center bg-black/40 px-4 pt-[12vh]">
		<div
			class="animate-scale-up h-auto max-h-[80vh] w-[90%] max-w-[600px] overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white p-6 text-left shadow-xl select-none"
		>
			<h3 class="text-lg font-bold tracking-tight text-[#09090B]">Add Attraction</h3>
			<p class="mt-1 text-xs text-[#71717A]">
				Register a new featured artist, band, team, or performing group.
			</p>

			<form
				method="POST"
				action="?/createAttraction"
				use:enhance={() => {
					return async ({ result, update }) => {
						if (result.type === 'success') {
							showAddAttractionModal = false;
						}
						await update();
					};
				}}
				class="mt-4 space-y-4"
			>
				<div class="space-y-1">
					<label for="attr-name" class="text-xs font-semibold text-[#71717A]">Name *</label>
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
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label for="attr-slug" class="text-xs font-semibold text-[#71717A]">URL Slug *</label>
					<input
						type="text"
						id="attr-slug"
						name="slug"
						bind:value={attrSlug}
						required
						placeholder="son-tung-mtp"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label for="attr-type" class="text-xs font-semibold text-[#71717A]">Performer Type</label>
					<select
						id="attr-type"
						name="type"
						bind:value={attrType}
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] outline-none focus:border-[#71717A] focus:bg-white"
					>
						<option value="ARTIST">ARTIST (Singer, Musician, etc.)</option>
						<option value="TEAM">TEAM (Sports Club/Group)</option>
						<option value="SHOW_CREW">SHOW CREW (Theater / Performing Group)</option>
					</select>
				</div>

				<div class="space-y-1">
					<label class="text-xs font-semibold text-[#71717A]">Image</label>
					<ImageInput bind:value={attrImageUrl} name="imageUrl" />
				</div>

				<div class="space-y-1">
					<label for="attr-desc" class="text-xs font-semibold text-[#71717A]"
						>Description (Optional)</label
					>
					<textarea
						id="attr-desc"
						name="description"
						bind:value={attrDescription}
						rows="2"
						placeholder="Brief biography or details..."
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					></textarea>
				</div>

				<div class="flex items-center justify-between gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showAddAttractionModal = false)}
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
