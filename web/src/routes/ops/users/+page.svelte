<script lang="ts">
	import { page } from '$app/state';
	import PaginationBar from '$lib/components/catalog/PaginationBar.svelte';

	interface User {
		id: string;
		name: string;
		email: string;
		role: string;
		status: string;
		registeredAt: string;
	}

	let { data } = $props<{ data: { users?: User[] } }>();

	const seedUsers: User[] = [
		{ id: 'sys-1', name: 'Admin Ticketpeak',  email: 'admin@ticketpeak.com',    role: 'ADMIN',          status: 'ACTIVE', registeredAt: '2026-01-01T00:00:00Z' },
		{ id: 'sys-2', name: 'Tran Buyer',         email: 'buyer@ticketpeak.com',    role: 'BUYER',          status: 'ACTIVE', registeredAt: '2026-06-09T08:00:00Z' },
		{ id: 'sys-3', name: 'Venue Manager',      email: 'manager@ticketpeak.com',  role: 'VENUE_MANAGER',  status: 'ACTIVE', registeredAt: '2026-06-09T08:00:00Z' },
		{ id: 'sys-4', name: 'Nguyen Organizer',   email: 'organizer@ticketpeak.com',role: 'ORGANIZER',      status: 'ACTIVE', registeredAt: '2026-06-09T08:00:00Z' },
		{ id: 'sys-5', name: 'Promoter User',      email: 'promoter@ticketpeak.com', role: 'PROMOTER',       status: 'ACTIVE', registeredAt: '2026-06-09T08:00:00Z' },
		{ id: 'usr-1', name: 'Nguyen Van A',        email: 'vana@gmail.com',          role: 'BUYER',          status: 'ACTIVE', registeredAt: '2026-05-10T10:00:00Z' },
		{ id: 'usr-2', name: 'Le Thi B',            email: 'thib@gmail.com',          role: 'BUYER',          status: 'ACTIVE', registeredAt: '2026-05-12T14:30:00Z' },
		{ id: 'usr-3', name: 'Tran Van C',          email: 'vanc@gmail.com',          role: 'ORGANIZER',      status: 'ACTIVE', registeredAt: '2026-04-01T08:15:00Z' },
		{ id: 'usr-4', name: 'Pham Minh D',         email: 'minhd@gmail.com',         role: 'ORGANIZER',      status: 'ACTIVE', registeredAt: '2026-04-15T09:20:00Z' },
		{ id: 'usr-5', name: 'Scalper Boy',         email: 'scalper@badactor.org',    role: 'BUYER',          status: 'BANNED', registeredAt: '2026-05-20T11:00:00Z' },
	];

	let usersList = $state<User[]>([]);
	$effect(() => {
		const api = data.users || [];
		if (api.length > 0) {
			const merged = [...api];
			seedUsers.forEach(s => { if (!merged.some(u => u.email.toLowerCase() === s.email.toLowerCase())) merged.push(s); });
			usersList = merged;
		} else {
			usersList = seedUsers;
		}
	});

	let search = $state('');
	let roleFilter = $state('all');

	function normalize(t: string) {
		return t.normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/[đĐ]/g, d => d === 'đ' ? 'd' : 'D').toLowerCase();
	}

	const filtered = $derived(usersList.filter(u => {
		const q = normalize(search);
		const match = normalize(u.name).includes(q) || normalize(u.email).includes(q);
		const role  = roleFilter === 'all' || u.role === roleFilter;
		return match && role;
	}));

	const PER_PAGE = 15;
	const currentPage = $derived(Number(page.url.searchParams.get('page')) || 0);
	const totalPages  = $derived(Math.ceil(filtered.length / PER_PAGE));
	const rows        = $derived(filtered.slice(currentPage * PER_PAGE, (currentPage + 1) * PER_PAGE));

	let selected = $state<string[]>([]);
	const allSel = $derived(rows.length > 0 && rows.every(u => selected.includes(u.id)));
	function toggleAll() { selected = allSel ? selected.filter(id => !rows.some(u => u.id === id)) : [...new Set([...selected, ...rows.map(u => u.id)])]; }
	function toggleOne(id: string) { selected = selected.includes(id) ? selected.filter(i => i !== id) : [...selected, id]; }

	$effect(() => { search; roleFilter; selected = []; });

	function toggleBan(id: string) { const u = usersList.find(u => u.id === id); if (u) u.status = u.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE'; }

	function fmtDate(iso: string) {
		return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
	}

	const ROLE_META: Record<string, { label: string; color: string }> = {
		ADMIN:         { label: 'Admin',         color: 'bg-violet-50 text-violet-700 ring-violet-200' },
		BUYER:         { label: 'Buyer',          color: 'bg-sky-50 text-sky-700 ring-sky-200' },
		ORGANIZER:     { label: 'Organizer',      color: 'bg-amber-50 text-amber-700 ring-amber-200' },
		VENUE_MANAGER: { label: 'Venue Manager',  color: 'bg-teal-50 text-teal-700 ring-teal-200' },
		PROMOTER:      { label: 'Promoter',       color: 'bg-orange-50 text-orange-700 ring-orange-200' },
	};

	// Summary stats
	const stats = $derived({
		total:   usersList.length,
		active:  usersList.filter(u => u.status === 'ACTIVE').length,
		banned:  usersList.filter(u => u.status === 'BANNED').length,
		orgs:    usersList.filter(u => u.role === 'ORGANIZER').length,
	});
</script>

<div class="flex min-h-screen flex-col bg-[#F5F6F8]">

	<!-- Page Header -->
	<div class="border-b border-slate-200 bg-white px-8 pb-0 pt-7">
		<div class="mb-5 flex items-start justify-between">
			<div>
				<div class="mb-1 flex items-center gap-2 text-[11px] font-semibold tracking-widest text-slate-400 uppercase">
					<span>Users &amp; Security</span>
					<svg class="h-3 w-3 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
					<span class="text-slate-600">Platform Users</span>
				</div>
				<h1 class="text-[22px] font-bold tracking-tight text-slate-900">Platform Users</h1>
				<p class="mt-0.5 text-[13px] text-slate-500">Manage all registered accounts, roles, and access control.</p>
			</div>
			<button class="flex items-center gap-2 rounded-md bg-slate-900 px-4 py-2.5 text-[13px] font-semibold text-white shadow-sm transition hover:bg-slate-800 active:scale-95">
				<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 4v16m8-8H4"/></svg>
				Invite User
			</button>
		</div>

		<!-- Stats strip -->
		<div class="flex gap-8 border-t border-slate-100 pt-4">
			{#each [
				{ label: 'Total Users',  value: stats.total,  color: 'text-slate-900' },
				{ label: 'Active',       value: stats.active, color: 'text-emerald-600' },
				{ label: 'Banned',       value: stats.banned, color: 'text-red-500' },
				{ label: 'Organizers',   value: stats.orgs,   color: 'text-amber-600' },
			] as s}
				<div class="pb-4">
					<div class="text-[22px] font-bold tabular-nums {s.color}">{s.value}</div>
					<div class="text-[11px] font-semibold tracking-wide text-slate-400 uppercase">{s.label}</div>
				</div>
			{/each}
		</div>
	</div>

	<!-- Toolbar -->
	<div class="flex items-center justify-between border-b border-slate-200 bg-white px-8 py-3">
		<div class="flex items-center gap-3">
			<!-- Search -->
			<div class="relative">
				<svg class="pointer-events-none absolute top-1/2 left-3 h-3.5 w-3.5 -translate-y-1/2 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
				<input
					type="text"
					placeholder="Search by name or email…"
					bind:value={search}
					class="h-8 w-72 rounded-md border border-slate-200 bg-white pl-9 pr-3 text-[13px] font-medium text-slate-800 placeholder-slate-400 outline-none transition focus:border-slate-400 focus:ring-2 focus:ring-slate-200"
				/>
			</div>

			<!-- Role filter -->
			<div class="flex items-center rounded-md border border-slate-200 bg-white">
				{#each [['all','All Roles'],['ADMIN','Admin'],['BUYER','Buyer'],['ORGANIZER','Organizer'],['VENUE_MANAGER','Venue Mgr'],['PROMOTER','Promoter']] as [val, lbl]}
					<button
						onclick={() => (roleFilter = val)}
						class="h-8 border-r border-slate-200 px-3 text-[12px] font-semibold transition last:border-r-0
							{roleFilter === val
								? 'bg-slate-900 text-white first:rounded-l-md last:rounded-r-md'
								: 'text-slate-500 hover:bg-slate-50 first:rounded-l-md last:rounded-r-md'}"
					>{lbl}</button>
				{/each}
			</div>
		</div>

		<div class="flex items-center gap-3 text-[12px] font-medium text-slate-400">
			<span>{filtered.length} result{filtered.length !== 1 ? 's' : ''}</span>
			{#if selected.length > 0}
				<span class="h-3.5 w-px bg-slate-200"></span>
				<span class="font-semibold text-slate-600">{selected.length} selected</span>
				<button class="rounded-md border border-red-200 bg-red-50 px-3 py-1 text-[12px] font-semibold text-red-600 transition hover:bg-red-100">Bulk Ban</button>
			{/if}
		</div>
	</div>

	<!-- Table -->
	<div class="flex-1 px-8 py-5">
		<div class="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
			<table class="w-full border-collapse text-left">
				<thead>
					<tr class="border-b border-slate-200 bg-slate-50">
						<th class="w-10 px-4 py-3">
							<input
								type="checkbox"
								checked={allSel}
								onchange={toggleAll}
								class="h-[15px] w-[15px] cursor-pointer rounded border-slate-300 accent-slate-800"
							/>
						</th>
						{#each ['Name', 'Email', 'Role', 'Joined', 'Status', 'Actions'] as col, i}
							<th class="px-4 py-3 text-[11px] font-bold tracking-widest text-slate-400 uppercase {i === 5 ? 'text-right' : ''}">
								{col}
							</th>
						{/each}
					</tr>
				</thead>
				<tbody>
					{#each rows as user (user.id)}
						{@const roleMeta = ROLE_META[user.role] ?? { label: user.role, color: 'bg-slate-100 text-slate-600 ring-slate-200' }}
						{@const isBanned = user.status === 'BANNED'}
						{@const isSel    = selected.includes(user.id)}
						<tr class="group border-b border-slate-100 transition-colors last:border-b-0
							{isSel ? 'bg-slate-50' : 'hover:bg-slate-50/60'}">

							<!-- Checkbox -->
							<td class="px-4 py-3.5">
								<input
									type="checkbox"
									checked={isSel}
									onchange={() => toggleOne(user.id)}
									class="h-[15px] w-[15px] cursor-pointer rounded border-slate-300 accent-slate-800"
								/>
							</td>

							<!-- Name -->
							<td class="px-4 py-3.5">
								<div class="flex items-center gap-3">
									<div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-slate-100 text-[12px] font-bold text-slate-600">
										{user.name.split(' ').map(w => w[0]).slice(-2).join('').toUpperCase()}
									</div>
									<span class="text-[13px] font-semibold text-slate-900 {isBanned ? 'line-through opacity-50' : ''}">{user.name}</span>
								</div>
							</td>

							<!-- Email -->
							<td class="px-4 py-3.5">
								<span class="font-mono text-[12px] text-slate-500">{user.email}</span>
							</td>

							<!-- Role -->
							<td class="px-4 py-3.5">
								<span class="inline-flex items-center rounded-md px-2 py-0.5 text-[11px] font-semibold ring-1 ring-inset {roleMeta.color}">
									{roleMeta.label}
								</span>
							</td>

							<!-- Joined -->
							<td class="px-4 py-3.5">
								<span class="text-[13px] tabular-nums text-slate-500">{fmtDate(user.registeredAt)}</span>
							</td>

							<!-- Status -->
							<td class="px-4 py-3.5">
								{#if isBanned}
									<span class="inline-flex items-center gap-1.5 rounded-md bg-red-50 px-2.5 py-1 text-[11px] font-bold tracking-wide text-red-600 ring-1 ring-inset ring-red-200 uppercase">
										<span class="h-1.5 w-1.5 rounded-full bg-red-500"></span>
										Banned
									</span>
								{:else}
									<span class="inline-flex items-center gap-1.5 rounded-md bg-emerald-50 px-2.5 py-1 text-[11px] font-bold tracking-wide text-emerald-700 ring-1 ring-inset ring-emerald-200 uppercase">
										<span class="h-1.5 w-1.5 rounded-full bg-emerald-500"></span>
										Active
									</span>
								{/if}
							</td>

							<!-- Actions -->
							<td class="px-4 py-3.5 text-right">
								<button
									onclick={() => toggleBan(user.id)}
									class="rounded-md border px-3 py-1.5 text-[12px] font-semibold transition-all active:scale-95
										{isBanned
											? 'border-slate-200 bg-white text-slate-700 hover:border-slate-300 hover:bg-slate-50'
											: 'border-red-200 bg-white text-red-600 hover:border-red-300 hover:bg-red-50'}"
								>
									{isBanned ? 'Unban' : 'Ban'}
								</button>
							</td>
						</tr>
					{:else}
						<tr>
							<td colspan="7" class="py-16 text-center">
								<div class="mx-auto flex max-w-xs flex-col items-center gap-2">
									<svg class="h-8 w-8 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
									<p class="text-[13px] font-semibold text-slate-400">No users found</p>
									<p class="text-[12px] text-slate-300">Try adjusting your search or filter criteria.</p>
								</div>
							</td>
						</tr>
					{/each}
				</tbody>
			</table>

			{#if totalPages > 1}
				<div class="flex items-center justify-between border-t border-slate-100 bg-slate-50/50 px-6 py-3">
					<span class="text-[12px] text-slate-400">
						Showing {currentPage * PER_PAGE + 1}–{Math.min((currentPage + 1) * PER_PAGE, filtered.length)} of {filtered.length} users
					</span>
					<PaginationBar {currentPage} {totalPages} />
				</div>
			{/if}
		</div>
	</div>
</div>
