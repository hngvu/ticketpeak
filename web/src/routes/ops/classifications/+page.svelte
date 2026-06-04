<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';

	let { data, form } = $props<{ data: any; form: any }>();

	const classifications = $derived(data.classifications || []);

	// Search and filter states
	let classSearch = $state('');
	let classLevelFilter = $state('all'); // 'all', '1', '2'

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

	let showAddClassificationModal = $state(false);

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
						placeholder="Search classifications..."
						bind:value={classSearch}
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] py-2 pr-4 pl-9 text-xs font-semibold text-[#111111] placeholder-[#71717A] transition outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<!-- Filter dropdown -->
				<div class="flex items-center gap-2">
					<select
						bind:value={classLevelFilter}
						class="rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3 py-2 text-xs font-semibold text-[#111111] transition outline-none focus:border-[#71717A] focus:bg-white"
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
						<th class="px-6 py-3.5">Classification Name</th>
						<th class="px-6 py-3.5">Slug</th>
						<th class="px-6 py-3.5">Parent Category</th>
						<th class="px-6 py-3.5 text-right font-bold">Status & Actions</th>
					</tr>
				</thead>
				<tbody class="divide-y divide-slate-100 font-sans text-xs font-semibold text-[#71717A]">
					{#each filteredClassifications as cat (cat.id)}
						<tr class="transition-colors hover:bg-[#FAFAFA]">
							<td class="px-6 py-4">
								<button
									type="button"
									onclick={() => openEditClassification(cat)}
									class="cursor-pointer border-0 bg-transparent p-0 text-left text-sm font-bold text-[#111111] transition-colors hover:text-slate-700 hover:underline focus:outline-none"
								>
									{cat.name}
								</button>
							</td>
							<td class="px-6 py-4 font-mono text-[#71717A]">{cat.slug}</td>
							<td class="px-6 py-4">
								{#if cat.parentId}
									{@const parent = classifications.find((c: any) => c.id === cat.parentId)}
									<span class="text-xs font-semibold text-[#111111]">
										{parent ? parent.name : '-'}
									</span>
								{:else}
									<span class="text-xs font-semibold text-[#71717A]">-</span>
								{/if}
							</td>
							<td class="px-6 py-4 text-right">
								<div class="flex items-center justify-end gap-4">
									{#if cat.isActive}
										<span
											class="inline-block rounded-md bg-emerald-50 px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-emerald-600 uppercase select-none"
										>
											Active
										</span>
									{:else}
										<span
											class="inline-block rounded-md bg-rose-50 px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-rose-600 uppercase select-none"
										>
											Inactive
										</span>
									{/if}

									<form method="POST" action="?/toggleClassificationStatus" use:enhance>
										<input type="hidden" name="id" value={cat.id} />
										<input type="hidden" name="name" value={cat.name} />
										<input type="hidden" name="slug" value={cat.slug} />
										<input type="hidden" name="parentId" value={cat.parentId || ''} />
										<input type="hidden" name="isActive" value={cat.isActive ? 'true' : 'false'} />
										<button
											type="submit"
											class="cursor-pointer rounded-md border px-3 py-1 text-xs font-bold transition-all active:scale-95
												{cat.isActive
												? 'border-rose-100 bg-rose-50 text-rose-600 hover:bg-rose-600 hover:text-white'
												: 'border-emerald-100 bg-emerald-50 text-emerald-600 hover:bg-emerald-600 hover:text-white'}"
										>
											{cat.isActive ? 'Deactivate' : 'Activate'}
										</button>
									</form>
								</div>
							</td>
						</tr>
					{:else}
						<tr>
							<td colspan="4" class="p-12 text-center text-[#71717A] font-medium">
								No classifications found matching your search criteria.
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	</div>
</div>

<!-- ======================== ADD CLASSIFICATION DIALOG MODAL ======================== -->
{#if showAddClassificationModal}
	<div class="fixed inset-0 z-50 flex items-start justify-center bg-black/40 px-4 pt-[12vh]">
		<div
			class="animate-scale-up h-auto max-h-[80vh] w-[90%] max-w-[600px] overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white p-6 text-left shadow-xl select-none"
		>
			<h3 class="text-lg font-bold tracking-tight text-[#09090B]">
				{editingClassificationId ? 'Edit Classification' : 'Add Classification'}
			</h3>
			<p class="mt-1 text-xs text-[#71717A]">
				{editingClassificationId
					? 'Modify details for this event classification.'
					: 'Create a root category or a sub-genre for events ticketing.'}
			</p>

			<form
				method="POST"
				action={editingClassificationId ? '?/updateClassification' : '?/createClassification'}
				use:enhance={() => {
					return async ({ result, update }) => {
						if (result.type === 'success') {
							showAddClassificationModal = false;
						}
						await update();
					};
				}}
				class="mt-4 space-y-4"
			>
				{#if editingClassificationId}
					<input type="hidden" name="id" value={editingClassificationId} />
				{/if}
				<div class="space-y-1">
					<label for="class-name" class="text-xs font-semibold text-[#71717A]">Name *</label>
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
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label for="class-slug" class="text-xs font-semibold text-[#71717A]">URL Slug *</label>
					<input
						type="text"
						id="class-slug"
						name="slug"
						bind:value={classSlug}
						required
						placeholder="pop-music"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label for="class-parent" class="text-xs font-semibold text-[#71717A]"
						>Parent Category (Optional)</label
					>
					<select
						id="class-parent"
						name="parentId"
						bind:value={classParentId}
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] outline-none focus:border-[#71717A] focus:bg-white"
					>
						<option value="">None (Level 1 Root Category)</option>
						{#each classifications.filter((c: any) => c.level === 1 && c.id !== editingClassificationId) as rootCat (rootCat.id)}
							<option value={rootCat.id}>{rootCat.name}</option>
						{/each}
					</select>
					<p class="pt-1 text-[9px] leading-normal text-[#71717A]">
						Leave blank to create a main root category, or select a parent to create a sub-genre.
					</p>
				</div>

				<div class="flex items-center justify-end gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showAddClassificationModal = false)}
						class="cursor-pointer rounded-md border border-[#E4E4E7] bg-transparent px-5 py-2 text-xs font-bold text-[#71717A] transition-all hover:bg-[#FAFAFA] hover:text-[#111111]"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-5 py-2 text-xs font-bold text-white transition-all hover:border-black hover:bg-black active:scale-95"
					>
						{editingClassificationId ? 'Save' : 'Create'}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
