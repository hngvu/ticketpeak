<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';

	let { data, form } = $props<{ data: any; form: any }>();

	const organizations = $derived(data.organizations || []);

	let showAddOrganizationModal = $state(false);

	// Organization form local binds
	let orgName = $state('');
	let orgOwnerEmail = $state('');
	let searchedOwner = $state<any>(null);
	let isSearchingOwner = $state(false);
	let ownerSearchError = $state('');

	let searchResults = $state<any[]>([]);
	let showSuggestions = $state(false);

	let suggestionsContainer = $state<HTMLDivElement | null>(null);
	let ownerInput = $state<HTMLInputElement | null>(null);

	function handleDocumentClick(event: MouseEvent) {
		const target = event.target as Node;
		if (
			showSuggestions &&
			suggestionsContainer &&
			!suggestionsContainer.contains(target) &&
			ownerInput &&
			!ownerInput.contains(target)
		) {
			showSuggestions = false;
		}
	}

	function handleKeyDown(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			showSuggestions = false;
		}
	}

	$effect(() => {
		const query = orgOwnerEmail;
		if (!query || query.trim().length < 2) {
			searchResults = [];
			showSuggestions = false;
			searchedOwner = null;
			ownerSearchError = '';
			return;
		}

		if (searchedOwner && searchedOwner.email.toLowerCase() === query.trim().toLowerCase()) {
			showSuggestions = false;
			return;
		}

		isSearchingOwner = true;
		const timer = setTimeout(async () => {
			try {
				const res = await fetch(`/api/ops/accounts?q=${encodeURIComponent(query)}&role=ORGANIZER`);
				const json = await res.json();
				if (json.success && Array.isArray(json.data)) {
					searchResults = json.data;
					showSuggestions = searchResults.length > 0;

					const exactMatch = searchResults.find(
						(a) => a.email.toLowerCase() === query.trim().toLowerCase()
					);
					if (exactMatch) {
						searchedOwner = exactMatch;
						showSuggestions = false;
					} else {
						searchedOwner = null;
					}

					if (searchResults.length === 0) {
						ownerSearchError = 'No matching accounts found';
					} else {
						ownerSearchError = '';
					}
				}
			} catch {
				ownerSearchError = 'Failed to search accounts';
			} finally {
				isSearchingOwner = false;
			}
		}, 300);

		return () => clearTimeout(timer);
	});

	function selectOwner(account: any) {
		orgOwnerEmail = account.email;
		searchedOwner = account;
		showSuggestions = false;
		searchResults = [];
	}

	// Suspended organizations tracking
	let suspendedOrgIds = $state<string[]>([]);

	function toggleOrgSuspend(orgId: string, orgName: string) {
		if (suspendedOrgIds.includes(orgId)) {
			suspendedOrgIds = suspendedOrgIds.filter((id) => id !== orgId);
		} else {
			suspendedOrgIds = [...suspendedOrgIds, orgId];
		}
	}
</script>

<svelte:window onclick={handleDocumentClick} onkeydown={handleKeyDown} />

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
		<div class="flex items-center justify-between border-b border-[#F4F4F5] px-6 py-4">
			<h3 class="font-sans text-sm font-semibold text-[#111111] select-none">
				Platform Organizations
			</h3>
			<button
				type="button"
				onclick={() => {
					orgName = '';
					orgOwnerEmail = '';
					searchedOwner = null;
					searchResults = [];
					showSuggestions = false;
					ownerSearchError = '';
					showAddOrganizationModal = true;
				}}
				class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-4 py-2 text-xs font-bold text-white shadow-xs transition hover:bg-black active:scale-95"
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
						<th class="px-6 py-3.5">Organization Name</th>
						<th class="px-6 py-3.5">Contact Details</th>
						<th class="px-6 py-3.5">Website</th>
						<th class="px-6 py-3.5">Status</th>
						<th class="px-6 py-3.5 text-right font-bold">Actions</th>
					</tr>
				</thead>
				<tbody class="divide-y divide-slate-100 font-sans text-xs font-semibold text-[#71717A]">
					{#each organizations as org (org.id)}
						{@const isSuspended = suspendedOrgIds.includes(org.id)}
						<tr class="transition-colors hover:bg-[#FAFAFA]">
							<td class="px-6 py-4">
								<p class="text-sm font-bold text-[#111111]">{org.name}</p>
								<p class="mt-0.5 text-[10px] text-[#71717A]">Slug: {org.slug}</p>
							</td>
							<td class="px-6 py-4">
								<p class="font-medium text-[#111111]">{org.email || 'No email'}</p>
								<p class="mt-0.5 font-normal text-[#71717A]">{org.phone || 'No phone'}</p>
							</td>
							<td class="px-6 py-4 text-blue-600 hover:underline">
								{#if org.websiteUrl}
									<a href={org.websiteUrl} target="_blank" rel="noopener noreferrer"
										>{org.websiteUrl}</a
									>
								{:else}
									<span class="font-normal text-[#71717A]">N/A</span>
								{/if}
							</td>
							<td class="px-6 py-4">
								{#if isSuspended}
									<span
										class="inline-block rounded-md bg-rose-50 px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-rose-600 uppercase select-none"
									>
										Suspended
									</span>
								{:else}
									<span
										class="inline-block rounded-md bg-emerald-50 px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-emerald-600 uppercase select-none"
									>
										Active
									</span>
								{/if}
							</td>
							<td class="px-6 py-4 text-right">
								<button
									onclick={() => toggleOrgSuspend(org.id, org.name)}
									class="cursor-pointer rounded-md border px-3 py-1 text-xs font-bold transition-all active:scale-95
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
</div>

<!-- ======================== ADD ORGANIZATION DIALOG MODAL ======================== -->
{#if showAddOrganizationModal}
	<div class="fixed inset-0 z-50 flex items-start justify-center bg-black/40 px-4 pt-[12vh]">
		<div
			class="animate-scale-up h-auto max-h-[80vh] w-[90%] max-w-[600px] overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white p-6 text-left shadow-xl select-none"
		>
			<h3 class="text-lg font-bold tracking-tight text-[#09090B]">Create Organization</h3>
			<p class="mt-1 text-xs text-[#71717A]">
				Register a new global business organizer partner profile.
			</p>

			<form
				method="POST"
				action="?/createOrganization"
				use:enhance={() => {
					return async ({ result, update }) => {
						if (result.type === 'success') {
							showAddOrganizationModal = false;
						}
						await update();
					};
				}}
				class="mt-4 space-y-4"
			>
				<div class="space-y-1">
					<label for="org-name" class="text-xs font-semibold text-[#71717A]"
						>Organization Name *</label
					>
					<input
						type="text"
						id="org-name"
						name="name"
						bind:value={orgName}
						required
						placeholder="e.g. Live Nation Vietnam"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label for="org-owner" class="text-xs font-semibold text-[#71717A]">Owner *</label>
					<div class="relative">
						{#if searchedOwner}
							<!-- Selected state styled like the input -->
							<div class="flex w-full items-center justify-between rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] select-none">
								<div class="flex items-center gap-2 min-w-0">
									{#if searchedOwner.avatarUrl}
										<img
											src={searchedOwner.avatarUrl}
											alt="Avatar"
											class="h-5 w-5 rounded-full border border-[#E4E4E7] object-cover"
										/>
									{:else}
										<div class="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-200 text-[9px] font-bold text-slate-700 uppercase">
											{searchedOwner.firstName?.[0] || searchedOwner.email?.[0] || 'O'}
										</div>
									{/if}
									<span class="truncate text-xs font-normal text-[#111111]">
										{searchedOwner.email}
									</span>
								</div>
								<button
									type="button"
									onclick={() => {
										searchedOwner = null;
										orgOwnerEmail = '';
										searchResults = [];
										showSuggestions = false;
									}}
									class="ml-2 flex h-5 w-5 items-center justify-center rounded-full text-slate-400 hover:bg-slate-200 hover:text-slate-600 transition-colors"
									aria-label="Remove owner"
								>
									<svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
										<path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
									</svg>
								</button>
							</div>
							<input type="hidden" name="ownerEmail" value={searchedOwner.email} />
						{:else}
							<input
								type="email"
								id="org-owner"
								name="ownerEmail"
								bind:value={orgOwnerEmail}
								required
								placeholder="e.g. organizer@ticketpeak.com"
								class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
								bind:this={ownerInput}
								onfocus={() => {
									if (searchResults.length > 0) showSuggestions = true;
								}}
							/>

							<!-- Autocomplete Suggestions List -->
							{#if showSuggestions && searchResults.length > 0}
								<div
									bind:this={suggestionsContainer}
									class="absolute right-0 left-0 z-55 mt-1 max-h-48 overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white shadow-lg"
								>
									{#each searchResults as account (account.id)}
										<button
											type="button"
											onclick={() => selectOwner(account)}
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
													{account.firstName?.[0] || account.email?.[0] || 'O'}
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
					</div>

					{#if isSearchingOwner}
						<div
							class="mt-2 flex items-center gap-2 text-[10px] font-semibold text-[#71717A] select-none"
						>
							<svg class="h-3.5 w-3.5 animate-spin text-[#71717A]" fill="none" viewBox="0 0 24 24">
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
							<span>Searching for account...</span>
						</div>
					{:else if ownerSearchError}
						<p class="mt-1 text-[10px] font-bold text-rose-600 select-none">
							⚠️ {ownerSearchError}
						</p>
					{/if}
				</div>

				<div class="flex items-center justify-between gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showAddOrganizationModal = false)}
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
