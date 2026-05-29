<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';
	import { goto } from '$app/navigation';

	let { data, form } = $props<{ data: any; form: any }>();

	const events = $derived(data.events || []);
	const organizations = $derived(data.organizations || []);

	// Active tab state
	let activeTab = $state<'events' | 'organizations' | 'settings' | 'logs'>('events');

	// Local settings state
	let platformCommission = $state(5);
	let antiScalpCap = $state(150);
	let maxTransferCount = $state(5);
	let settingsSaved = $state(false);

	// Postpone modal state
	let selectedEventId = $state<string | null>(null);
	let postponeReason = $state('');
	let showPostponeModal = $state(false);

	// Local organization suspend simulation state
	let suspendedOrgIds = $state<string[]>([]);

	// Simulated logs
	let auditLogs = $state([
		{
			id: 1,
			time: '14:20:15',
			action: 'ADMIN_SIGN_IN',
			user: 'admin@ticketpeak.com',
			details: 'Authorized admin session started successfully.'
		},
		{
			id: 2,
			time: '13:05:40',
			action: 'UPDATE_SYSTEM_SETTINGS',
			user: 'admin@ticketpeak.com',
			details: 'Platform commission updated to 5.0%.'
		},
		{
			id: 3,
			time: '11:15:22',
			action: 'EVENT_PUBLISH',
			user: 'admin@ticketpeak.com',
			details: 'Event approved and published.'
		},
		{
			id: 4,
			time: '09:44:10',
			action: 'ORG_ACTIVATE',
			user: 'admin@ticketpeak.com',
			details: 'Organization active status verified.'
		}
	]);

	function addLog(action: string, details: string) {
		const now = new Date();
		const time = now.toTimeString().split(' ')[0];
		auditLogs = [
			{
				id: Date.now(),
				time,
				action,
				user: 'admin@ticketpeak.com',
				details
			},
			...auditLogs
		];
	}

	function saveSettings() {
		settingsSaved = true;
		addLog(
			'UPDATE_SETTINGS',
			`Commission: ${platformCommission}%, Resale Cap: ${antiScalpCap}%, Max Transfers: ${maxTransferCount}`
		);
		setTimeout(() => {
			settingsSaved = false;
		}, 3000);
	}

	function toggleOrgSuspend(orgId: string, orgName: string) {
		if (suspendedOrgIds.includes(orgId)) {
			suspendedOrgIds = suspendedOrgIds.filter((id) => id !== orgId);
			addLog('ORG_ACTIVATE', `Organization "${orgName}" reactivated successfully.`);
		} else {
			suspendedOrgIds = [...suspendedOrgIds, orgId];
			addLog('ORG_SUSPEND', `Organization "${orgName}" suspended due to policy check.`);
		}
	}

	function openPostpone(eventId: string) {
		selectedEventId = eventId;
		postponeReason = '';
		showPostponeModal = true;
	}

	function formatDateTime(isoString: string) {
		const d = new Date(isoString);
		return d.toLocaleString('en-US', {
			month: 'short',
			day: 'numeric',
			year: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	}
</script>

<svelte:head>
	<title>Ops Dashboard | Ticketpeak Platform Admin</title>
</svelte:head>

<div class="min-h-screen bg-[#13181F] font-sans text-slate-100 antialiased">
	<!-- Admin Global Header -->
	<header class="border-b border-[#222E3A] bg-[#1A222B] px-6 py-4.5">
		<div class="mx-auto flex max-w-7xl items-center justify-between">
			<div class="flex items-center gap-3">
				<img src="/logo.png" alt="Ticketpeak Logo" class="h-8 brightness-0 invert" />
				<div class="h-5 w-px bg-[#2C3B4A]"></div>
				<span
					class="bg-gradient-to-r from-amber-400 to-amber-200 bg-clip-text text-xs font-black tracking-widest text-transparent uppercase"
				>
					Ops Portal
				</span>
			</div>

			<div class="flex items-center gap-4">
				<div class="text-right">
					<p class="text-xs font-bold text-slate-400">Signed in as</p>
					<p class="text-xs font-semibold text-amber-400">admin@ticketpeak.com</p>
				</div>
				<button
					onclick={() => goto('/logout')}
					class="cursor-pointer rounded-full border border-rose-500/30 bg-rose-500/10 px-4.5 py-1.5 text-xs font-extrabold text-rose-400 transition-all hover:bg-rose-500 hover:text-white"
				>
					Sign Out
				</button>
			</div>
		</div>
	</header>

	<main class="mx-auto max-w-7xl px-6 py-8">
		<!-- Error or Notifications -->
		{#if form?.error}
			<div
				class="mb-6 rounded-xl border border-rose-500/20 bg-rose-950/40 p-4 text-xs font-bold text-rose-400 select-none"
			>
				⚠️ Operation Failed: {form.error}
			</div>
		{/if}
		{#if form?.message}
			<div
				class="mb-6 rounded-xl border border-emerald-500/20 bg-emerald-950/40 p-4 text-xs font-bold text-emerald-400 select-none"
			>
				✅ Action Succeeded: {form.message}
			</div>
		{/if}

		<!-- Dashboard Welcome & Overview -->
		<div class="mb-8 flex items-center justify-between">
			<div>
				<h1 class="text-2xl font-black tracking-tight text-white sm:text-3xl">
					Operations Control
				</h1>
				<p class="mt-1.5 text-xs font-semibold text-slate-400">
					Real-time moderation panel for global events, organizations, and platform settings.
				</p>
			</div>
		</div>

		<!-- KPI Stats Row -->
		<div class="mb-8 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
			<!-- KPI 1 -->
			<div
				class="group relative overflow-hidden rounded-2xl border border-[#222E3A] bg-[#1A222B] p-6 transition-all duration-300 hover:scale-[1.02] hover:border-amber-500/25 hover:shadow-2xs"
			>
				<div class="flex items-center justify-between">
					<span class="text-xs font-bold tracking-wider text-slate-400 uppercase">Total Events</span
					>
					<div class="rounded-lg bg-amber-500/10 p-2 text-amber-400">
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
							stroke-width="2"
							stroke="currentColor"
							class="h-5 w-5"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25v7.5"
							/>
						</svg>
					</div>
				</div>
				<p class="mt-4 font-mono text-3xl font-black text-white">{events.length}</p>
				<p class="mt-1 text-[10px] font-bold text-slate-500">Currently issued on platform</p>
			</div>

			<!-- KPI 2 -->
			<div
				class="group relative overflow-hidden rounded-2xl border border-[#222E3A] bg-[#1A222B] p-6 transition-all duration-300 hover:scale-[1.02] hover:border-blue-500/25 hover:shadow-2xs"
			>
				<div class="flex items-center justify-between">
					<span class="text-xs font-bold tracking-wider text-slate-400 uppercase">Active Orgs</span>
					<div class="rounded-lg bg-blue-500/10 p-2 text-blue-400">
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
							stroke-width="2"
							stroke="currentColor"
							class="h-5 w-5"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M2.25 21h19.5m-18-18v18m10.5-18v18m6-13.5V21M6.75 6.75h.75m-.75 3h.75m-.75 3h.75m3-6h.75m-.75 3h.75m-.75 3h.75M6.75 21h10.5"
							/>
						</svg>
					</div>
				</div>
				<p class="mt-4 font-mono text-3xl font-black text-white">{organizations.length}</p>
				<p class="mt-1 text-[10px] font-bold text-slate-500">Verified organizer accounts</p>
			</div>

			<!-- KPI 3 -->
			<div
				class="group relative overflow-hidden rounded-2xl border border-[#222E3A] bg-[#1A222B] p-6 transition-all duration-300 hover:scale-[1.02] hover:border-emerald-500/25 hover:shadow-2xs"
			>
				<div class="flex items-center justify-between">
					<span class="text-xs font-bold tracking-wider text-slate-400 uppercase">Revenue</span>
					<div class="rounded-lg bg-emerald-500/10 p-2 text-emerald-400">
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
							stroke-width="2"
							stroke="currentColor"
							class="h-5 w-5"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M12 6v12m-3-2.818.879.659c1.171.879 3.07.879 4.242 0 1.172-.879 1.172-2.303 0-3.182C13.536 12.219 12.768 12 12 12c-.725 0-1.45-.22-2.003-.659-1.106-.879-1.106-2.303 0-3.182s2.9-.879 4.006 0l.415.33M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"
							/>
						</svg>
					</div>
				</div>
				<p class="mt-4 font-mono text-3xl font-black text-emerald-400">12,450,000 ₫</p>
				<p class="mt-1 text-[10px] font-bold text-slate-500">5.0% commission share</p>
			</div>

			<!-- KPI 4 -->
			<div
				class="group relative overflow-hidden rounded-2xl border border-[#222E3A] bg-[#1A222B] p-6 transition-all duration-300 hover:scale-[1.02] hover:border-purple-500/25 hover:shadow-2xs"
			>
				<div class="flex items-center justify-between">
					<span class="text-xs font-bold tracking-wider text-slate-400 uppercase">System Users</span
					>
					<div class="rounded-lg bg-purple-500/10 p-2 text-purple-400">
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
							stroke-width="2"
							stroke="currentColor"
							class="h-5 w-5"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z"
							/>
						</svg>
					</div>
				</div>
				<p class="mt-4 font-mono text-3xl font-black text-white">412</p>
				<p class="mt-1 text-[10px] font-bold text-slate-500">Registered buyers & sellers</p>
			</div>
		</div>

		<!-- Nav Tabs Row -->
		<div class="mb-6 flex border-b border-[#222E3A]">
			<button
				onclick={() => (activeTab = 'events')}
				class="cursor-pointer border-b-2 px-6 py-3 font-sans text-xs font-extrabold tracking-wider uppercase transition-all outline-none
					{activeTab === 'events'
					? 'border-amber-400 text-white'
					: 'border-transparent text-slate-400 hover:text-slate-200'}"
			>
				📢 Event Moderation
			</button>
			<button
				onclick={() => (activeTab = 'organizations')}
				class="cursor-pointer border-b-2 px-6 py-3 font-sans text-xs font-extrabold tracking-wider uppercase transition-all outline-none
					{activeTab === 'organizations'
					? 'border-amber-400 text-white'
					: 'border-transparent text-slate-400 hover:text-slate-200'}"
			>
				🏢 Organizations
			</button>
			<button
				onclick={() => (activeTab = 'settings')}
				class="cursor-pointer border-b-2 px-6 py-3 font-sans text-xs font-extrabold tracking-wider uppercase transition-all outline-none
					{activeTab === 'settings'
					? 'border-amber-400 text-white'
					: 'border-transparent text-slate-400 hover:text-slate-200'}"
			>
				⚙️ Platform Settings
			</button>
			<button
				onclick={() => (activeTab = 'logs')}
				class="cursor-pointer border-b-2 px-6 py-3 font-sans text-xs font-extrabold tracking-wider uppercase transition-all outline-none
					{activeTab === 'logs'
					? 'border-amber-400 text-white'
					: 'border-transparent text-slate-400 hover:text-slate-200'}"
			>
				📋 Audit Logs
			</button>
		</div>

		<!-- TAB CONTENT: EVENTS -->
		{#if activeTab === 'events'}
			<div class="overflow-hidden rounded-2xl border border-[#222E3A] bg-[#1A222B]">
				<div class="border-b border-[#222E3A] px-6 py-4">
					<h3 class="font-sans text-sm font-extrabold text-white uppercase">
						Platform Events Inventory
					</h3>
				</div>
				<div class="overflow-x-auto">
					<table class="w-full border-collapse text-left">
						<thead>
							<tr
								class="border-b border-[#222E3A] bg-[#151D24] text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
							>
								<th class="px-6 py-3.5">Event Name</th>
								<th class="px-6 py-3.5">Venue</th>
								<th class="px-6 py-3.5">Start Date</th>
								<th class="px-6 py-3.5">Status</th>
								<th class="px-6 py-3.5 text-right">Moderation Actions</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-[#222E3A] font-sans text-xs font-semibold text-slate-200">
							{#each events as event (event.id)}
								<tr class="transition-colors hover:bg-[#1C252F]">
									<td class="px-6 py-4">
										<p class="text-sm font-bold text-white">{event.title}</p>
										<p class="mt-0.5 text-[10px] text-slate-500">ID: {event.id}</p>
									</td>
									<td class="px-6 py-4">{event.venueName}</td>
									<td class="px-6 py-4 text-slate-300">{formatDateTime(event.startAt)}</td>
									<td class="px-6 py-4">
										{#if event.status === 'DRAFT'}
											<span
												class="inline-block rounded-md bg-slate-500/10 px-2 py-0.5 text-[10px] font-bold text-slate-400 uppercase"
											>
												Draft
											</span>
										{:else if event.status === 'PUBLISHED'}
											<span
												class="inline-block rounded-md bg-blue-500/10 px-2 py-0.5 text-[10px] font-bold text-blue-400 uppercase"
											>
												Published
											</span>
										{:else if event.status === 'ON_SALE'}
											<span
												class="inline-block rounded-md bg-emerald-500/10 px-2 py-0.5 text-[10px] font-bold text-emerald-400 uppercase"
											>
												On Sale
											</span>
										{:else if event.status === 'POSTPONED'}
											<span
												class="inline-block rounded-md bg-amber-500/10 px-2 py-0.5 text-[10px] font-bold text-amber-400 uppercase"
											>
												Postponed
											</span>
										{:else if event.status === 'CANCELLED'}
											<span
												class="inline-block rounded-md bg-rose-500/10 px-2 py-0.5 text-[10px] font-bold text-rose-400 uppercase"
											>
												Cancelled
											</span>
										{:else}
											<span
												class="inline-block rounded-md bg-zinc-500/10 px-2 py-0.5 text-[10px] font-bold text-zinc-400 uppercase"
											>
												{event.status}
											</span>
										{/if}
									</td>
									<td class="px-6 py-4 text-right">
										<div class="flex items-center justify-end gap-2">
											{#if event.status === 'DRAFT'}
												<form
													method="POST"
													action="?/publishEvent"
													use:enhance={() => {
														return ({ result }) => {
															if (result.type === 'success')
																addLog('EVENT_PUBLISH', `Approved and published "${event.title}".`);
														};
													}}
												>
													<input type="hidden" name="id" value={event.id} />
													<button
														type="submit"
														class="cursor-pointer rounded-lg bg-blue-600 px-3 py-1 font-bold text-white transition-all hover:bg-blue-700"
													>
														Approve
													</button>
												</form>
											{/if}

											{#if event.status === 'PUBLISHED'}
												<form
													method="POST"
													action="?/startSales"
													use:enhance={() => {
														return ({ result }) => {
															if (result.type === 'success')
																addLog('START_SALES', `Ticket sales opened for "${event.title}".`);
														};
													}}
												>
													<input type="hidden" name="id" value={event.id} />
													<button
														type="submit"
														class="cursor-pointer rounded-lg bg-emerald-600 px-3 py-1 font-bold text-white transition-all hover:bg-emerald-700"
													>
														Open Sales
													</button>
												</form>
											{/if}

											{#if event.status !== 'CANCELLED' && event.status !== 'POSTPONED'}
												<button
													onclick={() => openPostpone(event.id)}
													class="cursor-pointer rounded-lg border border-[#3E4A56] bg-[#2D3843] px-3 py-1 font-bold text-slate-300 transition-all hover:bg-[#394654]"
												>
													Postpone
												</button>

												<form
													method="POST"
													action="?/cancelEvent"
													use:enhance={() => {
														return ({ result }) => {
															if (result.type === 'success')
																addLog(
																	'EVENT_CANCEL',
																	`Platform-enforced cancellation of event "${event.title}".`
																);
														};
													}}
												>
													<input type="hidden" name="id" value={event.id} />
													<button
														type="submit"
														class="cursor-pointer rounded-lg border border-rose-500/25 bg-rose-600/10 px-3 py-1 font-bold text-rose-400 transition-all hover:bg-rose-600 hover:text-white"
													>
														Cancel
													</button>
												</form>
											{/if}
										</div>
									</td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			</div>
		{/if}

		<!-- TAB CONTENT: ORGANIZATIONS -->
		{#if activeTab === 'organizations'}
			<div class="overflow-hidden rounded-2xl border border-[#222E3A] bg-[#1A222B]">
				<div class="border-b border-[#222E3A] px-6 py-4">
					<h3 class="font-sans text-sm font-extrabold text-white uppercase">
						Platform Organizations
					</h3>
				</div>
				<div class="overflow-x-auto">
					<table class="w-full border-collapse text-left">
						<thead>
							<tr
								class="border-b border-[#222E3A] bg-[#151D24] text-[10px] font-extrabold tracking-wider text-slate-400 uppercase"
							>
								<th class="px-6 py-3.5">Organization Name</th>
								<th class="px-6 py-3.5">Contact Details</th>
								<th class="px-6 py-3.5">Website</th>
								<th class="px-6 py-3.5">Status</th>
								<th class="px-6 py-3.5 text-right font-bold">Actions</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-[#222E3A] font-sans text-xs font-semibold text-slate-200">
							{#each organizations as org (org.id)}
								{@const isSuspended = suspendedOrgIds.includes(org.id)}
								<tr class="transition-colors hover:bg-[#1C252F]">
									<td class="px-6 py-4">
										<p class="text-sm font-bold text-white">{org.name}</p>
										<p class="mt-0.5 text-[10px] text-slate-500">Slug: {org.slug}</p>
									</td>
									<td class="px-6 py-4">
										<p>{org.email || 'No email'}</p>
										<p class="mt-0.5 text-slate-400">{org.phone || 'No phone'}</p>
									</td>
									<td class="px-6 py-4 text-blue-400 hover:underline">
										{#if org.websiteUrl}
											<a href={org.websiteUrl} target="_blank" rel="noopener noreferrer"
												>{org.websiteUrl}</a
											>
										{:else}
											<span class="font-normal text-slate-500">N/A</span>
										{/if}
									</td>
									<td class="px-6 py-4">
										{#if isSuspended}
											<span
												class="inline-block rounded-md bg-rose-500/10 px-2 py-0.5 text-[10px] font-bold text-rose-400 uppercase"
											>
												Suspended
											</span>
										{:else}
											<span
												class="inline-block rounded-md bg-emerald-500/10 px-2 py-0.5 text-[10px] font-bold text-emerald-400 uppercase"
											>
												Active
											</span>
										{/if}
									</td>
									<td class="px-6 py-4 text-right">
										<button
											onclick={() => toggleOrgSuspend(org.id, org.name)}
											class="cursor-pointer rounded-lg border px-3 py-1 font-bold transition-all
												{isSuspended
												? 'border-emerald-500/25 bg-emerald-600/10 text-emerald-400 hover:bg-emerald-600 hover:text-white'
												: 'border-rose-500/25 bg-rose-600/10 text-rose-400 hover:bg-rose-600 hover:text-white'}"
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
		{/if}

		<!-- TAB CONTENT: PLATFORM SETTINGS -->
		{#if activeTab === 'settings'}
			<div class="max-w-2xl rounded-2xl border border-[#222E3A] bg-[#1A222B] p-6 select-none">
				<h3 class="mb-6 font-sans text-sm font-extrabold tracking-wider text-white uppercase">
					Global Configuration Settings
				</h3>

				<div class="space-y-6">
					<!-- Setting 1 -->
					<div class="space-y-2">
						<div
							class="flex items-center justify-between text-xs font-bold text-slate-400 uppercase"
						>
							<span>Platform Commission Fee</span>
							<span class="font-mono text-sm text-amber-400">{platformCommission}%</span>
						</div>
						<input
							type="range"
							min="0"
							max="15"
							step="0.5"
							bind:value={platformCommission}
							class="h-1.5 w-full cursor-pointer rounded-lg bg-[#2C3B4A] accent-amber-500"
						/>
						<p class="text-[10px] font-bold text-slate-500">
							Service charge taken from organizer payouts automatically upon transaction clearance.
						</p>
					</div>

					<!-- Setting 2 -->
					<div class="space-y-2">
						<div
							class="flex items-center justify-between text-xs font-bold text-slate-400 uppercase"
						>
							<span>Anti-Scalping Resale Price Cap</span>
							<span class="font-mono text-sm text-amber-400">{antiScalpCap}%</span>
						</div>
						<input
							type="range"
							min="100"
							max="200"
							step="5"
							bind:value={antiScalpCap}
							class="h-1.5 w-full cursor-pointer rounded-lg bg-[#2C3B4A] accent-amber-500"
						/>
						<p class="text-[10px] font-bold text-slate-500">
							Maximum percentage above face value secondary ticket listings are capped at.
						</p>
					</div>

					<!-- Setting 3 -->
					<div class="space-y-2">
						<div
							class="flex items-center justify-between text-xs font-bold text-slate-400 uppercase"
						>
							<span>Max Ticket Transfers Limit</span>
							<span class="font-mono text-sm text-amber-400">{maxTransferCount} times</span>
						</div>
						<input
							type="number"
							min="1"
							max="10"
							bind:value={maxTransferCount}
							class="w-full rounded-lg border border-[#222E3A] bg-[#151D24] px-3.5 py-2.5 font-sans text-sm font-semibold text-white outline-none focus:border-amber-500"
						/>
						<p class="text-[10px] font-bold text-slate-500">
							Maximum threshold for one-tap ticket transfers before lock-up.
						</p>
					</div>

					<!-- Save Notification -->
					{#if settingsSaved}
						<div
							class="rounded-lg border border-emerald-500/20 bg-emerald-950/40 p-3 text-xs font-bold text-emerald-400"
						>
							✨ Configurations applied and propagated to active events.
						</div>
					{/if}

					<!-- Submit Button -->
					<button
						onclick={saveSettings}
						class="cursor-pointer rounded-full bg-amber-500 px-6 py-2.5 font-sans text-xs font-bold text-black transition-all hover:bg-amber-400 active:scale-[0.98]"
					>
						Save Settings
					</button>
				</div>
			</div>
		{/if}

		<!-- TAB CONTENT: AUDIT LOGS -->
		{#if activeTab === 'logs'}
			<div class="overflow-hidden rounded-2xl border border-[#222E3A] bg-[#1A222B]">
				<div class="flex items-center justify-between border-b border-[#222E3A] px-6 py-4">
					<h3 class="font-sans text-sm font-extrabold text-white uppercase">System Audit Logs</h3>
					<span
						class="rounded-md bg-[#2C3B4A] px-2.5 py-0.5 font-mono text-[10px] font-bold text-slate-300"
					>
						Real-Time Timeline
					</span>
				</div>

				<div class="max-h-[500px] space-y-4 overflow-y-auto p-6 font-mono text-xs text-slate-300">
					{#each auditLogs as log (log.id)}
						<div
							class="flex items-start gap-4 border-b border-[#222E3A]/60 pb-3 last:border-0 last:pb-0"
						>
							<span class="shrink-0 font-bold text-amber-500">{log.time}</span>
							<div class="space-y-1">
								<p class="text-[11px] font-bold text-white">
									[{log.action}] by <span class="text-blue-400">{log.user}</span>
								</p>
								<p class="text-xs text-slate-400">{log.details}</p>
							</div>
						</div>
					{/each}
				</div>
			</div>
		{/if}
	</main>
</div>

<!-- POSTPONE DIALOG MODAL -->
{#if showPostponeModal}
	<div
		class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 px-4 backdrop-blur-xs"
	>
		<div
			class="w-full max-w-md rounded-2xl border border-[#222E3A] bg-[#1A222B] p-6 text-left shadow-xl select-none"
		>
			<h3 class="text-lg font-black tracking-tight text-white uppercase">Postpone Event</h3>
			<p class="mt-1 text-xs text-slate-400">
				Provide an official platform announcement reason for postponing.
			</p>

			<form
				method="POST"
				action="?/postponeEvent"
				use:enhance={() => {
					return ({ result }) => {
						if (result.type === 'success') {
							addLog(
								'EVENT_POSTPONE',
								`Event ID: ${selectedEventId} officially postponed. Reason: ${postponeReason}`
							);
							showPostponeModal = false;
						}
					};
				}}
				class="mt-4 space-y-4"
			>
				<input type="hidden" name="id" value={selectedEventId} />

				<div class="space-y-1">
					<label
						for="postpone-reason"
						class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">Reason</label
					>
					<textarea
						id="postpone-reason"
						bind:value={postponeReason}
						required
						rows="3"
						placeholder="e.g. Unavoidable structural delays at military venue."
						class="w-full rounded-lg border border-[#222E3A] bg-[#151D24] px-3.5 py-2.5 font-sans text-xs text-white outline-none focus:border-amber-500"
					></textarea>
				</div>

				<div class="flex items-center justify-end gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showPostponeModal = false)}
						class="cursor-pointer rounded-full border border-[#2C3B4A] bg-transparent px-5 py-2 text-xs font-bold text-slate-400 hover:text-white"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="cursor-pointer rounded-full bg-amber-500 px-5 py-2 text-xs font-bold text-black hover:bg-amber-400"
					>
						Confirm Postpone
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
