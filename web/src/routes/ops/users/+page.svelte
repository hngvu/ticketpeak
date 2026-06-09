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

	// Initial high-fidelity mock users
	const defaultMockUsers: User[] = [
		{
			id: 'usr-1',
			name: 'Nguyen Van A',
			email: 'vana@gmail.com',
			role: 'BUYER',
			status: 'ACTIVE',
			registeredAt: '2026-05-10T10:00:00Z'
		},
		{
			id: 'usr-2',
			name: 'Le Thi B',
			email: 'thib@gmail.com',
			role: 'BUYER',
			status: 'ACTIVE',
			registeredAt: '2026-05-12T14:30:00Z'
		},
		{
			id: 'usr-3',
			name: 'Tran Van C',
			email: 'vanc@gmail.com',
			role: 'ORGANIZER',
			status: 'ACTIVE',
			registeredAt: '2026-04-01T08:15:00Z'
		},
		{
			id: 'usr-4',
			name: 'Pham Minh D',
			email: 'minhd@gmail.com',
			role: 'ORGANIZER',
			status: 'ACTIVE',
			registeredAt: '2026-04-15T09:20:00Z'
		},
		{
			id: 'usr-5',
			name: 'Admin One',
			email: 'admin@ticketpeak.com',
			role: 'ADMIN',
			status: 'ACTIVE',
			registeredAt: '2026-01-01T00:00:00Z'
		},
		{
			id: 'usr-6',
			name: 'Scalper Boy',
			email: 'scalper@badactor.org',
			role: 'BUYER',
			status: 'BANNED',
			registeredAt: '2026-05-20T11:00:00Z'
		}
	];

	// Initialize state: merge DB users and default mock users
	let usersList = $state<User[]>([]);

	$effect(() => {
		const apiUsers = data.users || [];
		if (apiUsers.length > 0) {
			const merged = [...apiUsers];
			defaultMockUsers.forEach((mockUsr) => {
				if (!merged.some((u) => u.email.toLowerCase() === mockUsr.email.toLowerCase())) {
					merged.push(mockUsr);
				}
			});
			usersList = merged;
		} else {
			usersList = defaultMockUsers;
		}
	});

	let userSearch = $state('');
	let userRoleFilter = $state('all'); // 'all', 'BUYER', 'ORGANIZER', 'ADMIN'

	// Helper to strip Vietnamese diacritics for easier search
	function cleanVietnamese(text: string): string {
		return text
			.normalize('NFD')
			.replace(/[\u0300-\u036f]/g, '')
			.replace(/đ/g, 'd')
			.replace(/Đ/g, 'D');
	}

	const filteredUsers = $derived(
		usersList.filter((u) => {
			const searchClean = cleanVietnamese(userSearch).toLowerCase();
			const matchesSearch =
				cleanVietnamese(u.name).toLowerCase().includes(searchClean) ||
				cleanVietnamese(u.email).toLowerCase().includes(searchClean);
			const matchesRole = userRoleFilter === 'all' || u.role === userRoleFilter;
			return matchesSearch && matchesRole;
		})
	);

	// Pagination
	const itemsPerPage = 10;
	const currentPage = $derived(Number(page.url.searchParams.get('page')) || 0);
	const totalPages = $derived(Math.ceil(filteredUsers.length / itemsPerPage));

	const paginatedUsers = $derived(
		filteredUsers.slice(currentPage * itemsPerPage, (currentPage + 1) * itemsPerPage)
	);

	// Selection
	let selectedUserIds = $state<string[]>([]);
	const allSelected = $derived(
		paginatedUsers.length > 0 && paginatedUsers.every((u) => selectedUserIds.includes(u.id))
	);

	function toggleAll() {
		if (allSelected) {
			selectedUserIds = selectedUserIds.filter((id) => !paginatedUsers.some((u) => u.id === id));
		} else {
			const newSelections = paginatedUsers
				.map((u) => u.id)
				.filter((id) => !selectedUserIds.includes(id));
			selectedUserIds = [...selectedUserIds, ...newSelections];
		}
	}

	function toggleUserSelection(id: string) {
		if (selectedUserIds.includes(id)) {
			selectedUserIds = selectedUserIds.filter((i) => i !== id);
		} else {
			selectedUserIds = [...selectedUserIds, id];
		}
	}

	// Reset selection and pagination when filters change
	$effect(() => {
		// This effect runs when userSearch or userRoleFilter changes
		// eslint-disable-next-line @typescript-eslint/no-unused-expressions
		userSearch;
		// eslint-disable-next-line @typescript-eslint/no-unused-expressions
		userRoleFilter;

		selectedUserIds = [];
		// Note: page reset usually happens via URL navigation, but if we're on page 5 and filter results to 1 page,
		// the PaginationBar will handle showing page 1 (0).
	});

	function toggleUserBan(id: string) {
		const usr = usersList.find((u) => u.id === id);
		if (usr) {
			usr.status = usr.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE';
		}
	}

	function formatDateTime(isoString: string) {
		const d = new Date(isoString);
		return d.toLocaleString('en-US', {
			month: 'short',
			day: 'numeric',
			year: 'numeric'
		});
	}
</script>

<div class="relative min-h-screen space-y-4 p-6 font-sans">
	<div class="animate-fade-in overflow-hidden rounded-md border border-hairline bg-canvas">
		<div
			class="flex flex-col gap-4 border-b border-hairline bg-canvas-soft-2 px-6 py-3 sm:flex-row sm:items-center sm:justify-between"
		>
			<div class="flex flex-1 items-center gap-3">
				<div class="relative w-full max-w-xs">
					<span
						class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-mute"
					>
						<svg
							class="h-3.5 w-3.5"
							fill="none"
							viewBox="0 0 24 24"
							stroke="currentColor"
							stroke-width="2.5"
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
						placeholder="Search users..."
						bind:value={userSearch}
						class="w-full rounded-sm border border-hairline bg-canvas py-1.5 pr-4 pl-9 text-[13px] font-medium text-ink placeholder-mute transition outline-none focus:border-hairline-strong focus:bg-canvas"
					/>
				</div>
				<select
					bind:value={userRoleFilter}
					class="rounded-sm border border-hairline bg-canvas px-3 py-1.5 text-[13px] font-medium text-ink transition outline-none focus:border-hairline-strong"
				>
					<option value="all">All Roles</option>
					<option value="BUYER">Buyer</option>
					<option value="ORGANIZER">Organizer</option>
					<option value="ADMIN">Admin</option>
				</select>
			</div>
		</div>

		<div class="overflow-x-auto">
			<table class="w-full border-collapse text-left text-[13px] text-body">
				<thead>
					<tr
						class="border-b border-hairline bg-canvas-soft-2 font-mono text-[10px] tracking-wider text-mute uppercase select-none"
					>
						<th class="w-10 px-6 py-2.5">
							<input
								type="checkbox"
								checked={allSelected}
								onchange={toggleAll}
								aria-label="Select all users"
								class="h-3.5 w-3.5 cursor-pointer rounded-xs border-hairline accent-primary transition-all"
							/>
						</th>
						<th class="px-6 py-2.5 font-medium">Name</th>
						<th class="px-6 py-2.5 font-medium">Email</th>
						<th class="px-6 py-2.5 font-medium">Role</th>
						<th class="px-6 py-2.5 font-medium">Joined</th>
						<th class="px-6 py-2.5 font-medium">Status</th>
						<th class="px-6 py-2.5 text-right font-medium">Actions</th>
					</tr>
				</thead>
				<tbody class="divide-y divide-hairline bg-canvas">
					{#each paginatedUsers as user (user.id)}
						<tr class="group transition-colors hover:bg-canvas-soft">
							<td class="px-6 py-2.5">
								<input
									type="checkbox"
									checked={selectedUserIds.includes(user.id)}
									onchange={() => toggleUserSelection(user.id)}
									aria-label="Select {user.name}"
									class="h-3.5 w-3.5 cursor-pointer rounded-xs border-hairline accent-primary transition-all"
								/>
							</td>
							<td class="px-6 py-2.5 font-medium text-ink">{user.name}</td>
							<td class="px-6 py-2.5 font-mono text-mute">{user.email}</td>
							<td class="px-6 py-2.5">
								<span
									class="inline-flex items-center rounded-sm px-1.5 py-0.5 text-[11px] font-medium tracking-tight text-body ring-1 ring-hairline ring-inset"
								>
									{user.role}
								</span>
							</td>
							<td class="px-6 py-2.5 font-mono text-mute">{formatDateTime(user.registeredAt)}</td>
							<td class="px-6 py-2.5">
								<span
									class="inline-flex items-center rounded-sm px-2 py-0.5 text-[10px] font-bold tracking-wider uppercase {user.status ===
									'ACTIVE'
										? 'bg-success/10 text-success'
										: 'bg-error/10 text-error'}"
								>
									{user.status}
								</span>
							</td>
							<td class="px-6 py-2.5 text-right">
								<button
									onclick={() => toggleUserBan(user.id)}
									class="cursor-pointer rounded-sm border border-hairline bg-canvas px-2.5 py-1 text-[11px] font-bold text-ink transition-all hover:border-hairline-strong active:scale-95"
								>
									{user.status === 'ACTIVE' ? 'Ban' : 'Unban'}
								</button>
							</td>
						</tr>
					{:else}
						<tr>
							<td colspan="7" class="p-12 text-center font-medium text-mute"
								>No platform users found matching your search.</td
							>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>

		{#if totalPages > 1}
			<div
				class="flex items-center justify-center border-t border-hairline bg-canvas-soft-2 py-1.5"
			>
				<PaginationBar {currentPage} {totalPages} />
			</div>
		{/if}
	</div>

	<!-- Floating Selection Footer -->
	{#if selectedUserIds.length > 0}
		<div
			class="fixed bottom-8 left-1/2 flex -translate-x-1/2 items-center gap-6 rounded-full border border-hairline bg-canvas px-6 py-3 shadow-xl ring-1 ring-ink/5"
		>
			<span class="text-sm font-medium text-ink">
				{selectedUserIds.length} users selected
			</span>
			<div class="h-4 w-px bg-hairline"></div>
			<button
				class="rounded-full bg-ink px-4 py-1.5 text-xs font-bold text-on-primary transition-all hover:bg-ink/90 active:scale-95"
			>
				Bulk Actions
			</button>
		</div>
	{/if}
</div>
