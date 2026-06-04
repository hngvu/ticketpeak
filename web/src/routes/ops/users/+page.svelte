<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */

	let { data } = $props<{ data: any }>();

	// Initial high-fidelity mock users
	const defaultMockUsers = [
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
	let usersList = $state<any[]>([]);

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
			.replace(/Đ/g, 'd');
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
			year: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	}
</script>

<div class="space-y-6 p-6">
	<div class="animate-fade-in overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
		<div
			class="flex flex-col gap-4 border-b border-[#F4F4F5] px-6 py-4 sm:flex-row sm:items-center sm:justify-between"
		>
			<div class="flex flex-1 items-center gap-3">
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
						placeholder="Search user email or name..."
						bind:value={userSearch}
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] py-2 pr-4 pl-9 text-xs font-semibold text-[#111111] placeholder-[#71717A] transition outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>
				<select
					bind:value={userRoleFilter}
					class="rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3 py-2 text-xs font-semibold text-[#111111] transition outline-none focus:border-[#71717A] focus:bg-white"
				>
					<option value="all">All Roles</option>
					<option value="BUYER">Buyer</option>
					<option value="ORGANIZER">Organizer</option>
					<option value="ADMIN">Admin</option>
				</select>
			</div>
		</div>
		<div class="overflow-x-auto">
			<table class="w-full border-collapse text-left text-xs font-semibold text-[#71717A]">
				<thead>
					<tr
						class="border-b border-[#E4E4E7] bg-[#FAFAFA] text-[10px] text-[#71717A] uppercase select-none"
					>
						<th class="px-6 py-3.5">User Details</th>
						<th class="px-6 py-3.5">Email Address</th>
						<th class="px-6 py-3.5">Assigned Role</th>
						<th class="px-6 py-3.5">Registered Date</th>
						<th class="px-6 py-3.5">Security Status</th>
						<th class="px-6 py-3.5 text-right font-bold">Moderation Actions</th>
					</tr>
				</thead>
				<tbody class="divide-y divide-[#F4F4F5] bg-white text-[#111111]">
					{#each filteredUsers as user (user.id)}
						<tr class="hover:bg-[#FAFAFA]">
							<td class="px-6 py-4 font-bold">{user.name}</td>
							<td class="px-6 py-4 font-medium text-[#71717A]">{user.email}</td>
							<td class="px-6 py-4">
								<span
									class="inline-block rounded-md border border-[#E4E4E7] bg-[#F4F4F5] px-2 py-0.5 font-mono text-[9px] text-[#71717A]"
								>
									{user.role}
								</span>
							</td>
							<td class="px-6 py-4 font-medium text-[#71717A]"
								>{formatDateTime(user.registeredAt)}</td
							>
							<td class="px-6 py-4">
								<span
									class="inline-block rounded-md px-2.5 py-0.5 text-[9px] font-bold uppercase select-none {user.status ===
									'ACTIVE'
										? 'bg-emerald-50 text-emerald-600'
										: 'bg-rose-50 text-rose-600'}"
								>
									{user.status}
								</span>
							</td>
							<td class="px-6 py-4 text-right">
								<button
									onclick={() => toggleUserBan(user.id)}
									class="cursor-pointer rounded-md border px-3 py-1 text-xs font-bold transition-all active:scale-95 {user.status ===
									'ACTIVE'
										? 'border-rose-100 bg-rose-50 text-rose-600 hover:bg-rose-600 hover:text-white'
										: 'border-emerald-100 bg-emerald-50 text-emerald-600 hover:bg-emerald-600 hover:text-white'}"
								>
									{user.status === 'ACTIVE' ? 'Ban User' : 'Unban User'}
								</button>
							</td>
						</tr>
					{:else}
						<tr>
							<td colspan="6" class="p-12 text-center text-[#71717A] font-medium"
								>No platform users found matching your search.</td
							>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	</div>
</div>
