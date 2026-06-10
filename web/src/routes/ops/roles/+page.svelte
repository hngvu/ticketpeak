<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */

	const SCOPES = [
		'event:publish',
		'order:refund',
		'user:ban',
		'payout:approve',
		'resale:censor',
		'cms:manage'
	];

	let { data } = $props<{ data: any }>();

	let scopeSearch = $state('');

	const filteredScopes = $derived(
		SCOPES.filter(
			(s) => !scopeSearch.trim() || s.toLowerCase().includes(scopeSearch.trim().toLowerCase())
		)
	);
</script>

<svelte:head>
	<title>Roles & Permissions | Ticketpeak Platform Admin</title>
</svelte:head>

<div class="space-y-6 p-6">
	<!-- Header -->
	<div>
		<h1 class="text-xl font-bold tracking-tight text-[#111111]">Roles & Permissions</h1>
		<p class="mt-1 text-xs font-medium text-[#71717A]">
			Manage platform authorization policies and system scopes.
		</p>
	</div>

	<!-- Search -->
	<div class="relative max-w-xs">
		<span
			class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-[#71717A]"
		>
			<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
				/>
			</svg>
		</span>
		<input
			type="text"
			placeholder="Search permissions..."
			bind:value={scopeSearch}
			class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] py-2 pr-4 pl-9 text-xs font-semibold text-[#111111] placeholder-[#71717A] transition outline-none focus:border-[#71717A] focus:bg-white"
		/>
	</div>

	<!-- Content grid -->
	<div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
		<!-- Default Authorization Policies -->
		<div class="overflow-hidden rounded-lg border border-[#E4E4E7] bg-white lg:col-span-2">
			<div class="border-b border-[#F4F4F5] px-6 py-4">
				<h3 class="text-sm font-semibold text-[#111111] select-none">
					Default Authorization Policies
				</h3>
			</div>
			<div class="divide-y divide-[#F4F4F5]">
				<div class="p-5">
					<div class="mb-2 flex items-center justify-between">
						<span class="text-xs font-bold text-[#111111] uppercase">ROLE_ADMIN</span>
						<span
							class="rounded-md bg-emerald-50 px-2.5 py-0.5 text-[9px] font-bold text-emerald-600 uppercase select-none"
							>All Scopes Allowed</span
						>
					</div>
					<p class="text-xs leading-relaxed text-[#71717A]">
						Complete super administrator access. Can override tickets transfer locks, issue
						platform-level refunds, configure commission rates, ban users, and approve payout
						channels.
					</p>
				</div>

				<div class="p-5">
					<div class="mb-2 flex items-center justify-between">
						<span class="text-xs font-bold text-[#111111] uppercase">ROLE_ORGANIZER</span>
						<span
							class="rounded-md bg-[#F4F4F5] px-2.5 py-0.5 text-[9px] font-bold text-[#71717A] select-none"
							>8 scopes</span
						>
					</div>
					<p class="text-xs leading-relaxed text-[#71717A]">
						Assigned to verified business organizers. Authorizes creation, publishing, inventory
						setup, holds allocation, seating manifests configurations, and payment settlement
						checks.
					</p>
				</div>

				<div class="p-5">
					<div class="mb-2 flex items-center justify-between">
						<span class="text-xs font-bold text-[#111111] uppercase">ROLE_BUYER</span>
						<span
							class="rounded-md bg-[#F4F4F5] px-2.5 py-0.5 text-[9px] font-bold text-[#71717A] select-none"
							>3 scopes</span
						>
					</div>
					<p class="text-xs leading-relaxed text-[#71717A]">
						Assigned to registered end ticket purchasers. Authorizes buying tickets, generating
						TOTP-secured check-in QR codes, submitting secondary resale listings, and transferring
						tickets.
					</p>
				</div>
			</div>
		</div>

		<!-- Platform System Scopes -->
		<div class="overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
			<div class="border-b border-[#F4F4F5] px-6 py-4">
				<h3 class="text-sm font-semibold text-[#111111] select-none">Platform System Scopes</h3>
			</div>
			<div class="divide-y divide-[#F4F4F5]">
				{#each filteredScopes as scope}
					<div class="flex items-center justify-between px-6 py-4">
						<span class="font-mono text-[11px] font-medium text-[#71717A]">{scope}</span>
						<input type="checkbox" checked disabled class="h-3.5 w-3.5 accent-slate-900" />
					</div>
				{/each}
				{#if filteredScopes.length === 0}
					<div class="px-6 py-8 text-center text-xs font-medium text-[#71717A]">
						No permissions match your search.
					</div>
				{/if}
			</div>
		</div>
	</div>
</div>
