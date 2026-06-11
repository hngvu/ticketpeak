<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve, @typescript-eslint/no-explicit-any */
	import { page } from '$app/state';
	import {
		IconChevronDown,
		IconLayoutGrid,
		IconLogout,
		IconExternalLink,
		IconCalendarEvent,
		IconDots,
		IconArrowLeft,
		IconUsers,
		IconReceipt,
		IconSettings,
		IconFileText,
	} from '@tabler/icons-svelte';

	let showAccountMenu = $state(false);

	let openFolders = $state<Record<string, boolean>>({
		moderation: true,
		users: true,
		finance: true,
		metadata: true,
		system: true
	});

	const categories = $derived([
		{
			id: 'dashboard',
			label: 'Dashboard',
			icon: IconLayoutGrid,
			href: '/ops/dashboard',
			active:
				page.url.pathname === '/ops/dashboard' &&
				(!page.url.searchParams.get('tab') || page.url.searchParams.get('tab') === 'dashboard')
		},
		{
			id: 'moderation',
			label: 'Moderation',
			icon: IconCalendarEvent,
			items: [
				{
					label: 'Events',
					href: '/ops/events',
					active: page.url.pathname === '/ops/events'
				},
				{
					label: 'Organizations',
					href: '/ops/organizations',
					active: page.url.pathname === '/ops/organizations'
				},
				{
					label: 'Attractions',
					href: '/ops/attractions',
					active: page.url.pathname === '/ops/attractions'
				}
			]
		},
		{
			id: 'users',
			label: 'Users',
			icon: IconUsers,
			items: [
				{
					label: 'Users',
					href: '/ops/users',
					active: page.url.pathname === '/ops/users'
				},
				{
					label: 'Roles',
					href: '/ops/roles',
					active: page.url.pathname === '/ops/roles'
				}
			]
		},
		{
			id: 'finance',
			label: 'Finance',
			icon: IconReceipt,
			items: [
				{
					label: 'Orders',
					href: '/ops/orders',
					active: page.url.pathname === '/ops/orders'
				},
				{
					label: 'Payouts',
					href: '/ops/payouts',
					active: page.url.pathname === '/ops/payouts'
				},
				{
					label: 'Fees',
					href: '/ops/fees',
					active: page.url.pathname === '/ops/fees'
				},
				{
					label: 'Resale',
					href: '/ops/resale',
					active: page.url.pathname === '/ops/resale'
				}
			]
		},
		{
			id: 'metadata',
			label: 'Metadata',
			icon: IconFileText,
			items: [
				{
					label: 'Classifications',
					href: '/ops/classifications',
					active: page.url.pathname === '/ops/classifications'
				}
			]
		},
		{
			id: 'system',
			label: 'System',
			icon: IconSettings,
			items: [
				{
					label: 'Audit Log',
					href: '/ops/logs',
					active: page.url.pathname === '/ops/logs'
				},
				{
					label: 'Settings',
					href: '/ops/settings',
					active: page.url.pathname === '/ops/settings'
				},
				{
					label: 'Status',
					href: '/ops/system',
					active: page.url.pathname === '/ops/system'
				}
			]
		}
	]);

</script>

<aside class="flex h-full w-56 shrink-0 flex-col justify-between border-r border-hairline bg-canvas">
	<div class="flex shrink-0 items-center justify-between border-b border-hairline p-3">
		<a href="/ops/dashboard" class="flex min-w-0 items-center gap-2">
			<div
				class="flex h-8 w-8 shrink-0 items-center justify-center rounded-md bg-ink text-sidebar-label font-bold tracking-tighter text-canvas shadow-sm select-none"
			>
				OP
			</div>
			<div class="flex min-w-0 flex-col">
				<span class="text-sidebar-brand font-semibold tracking-tight text-ink">Ticketpeak</span>
				<span
					class="mt-0.5 font-mono text-sidebar-tiny font-medium tracking-wider text-body uppercase"
				>
					Operations
				</span>
			</div>
		</a>
	</div>

	<div class="no-scrollbar flex flex-1 flex-col gap-1 overflow-y-auto p-2">
		{#each categories as cat (cat.id)}
			{@const CategoryIcon = cat.icon}
			{@const hasActiveItem = cat.items ? cat.items.some((i: any) => i.active) : cat.active}
			<div class="flex flex-col">
				{#if cat.items && cat.items.length > 0}
					<button
						type="button"
						onclick={() => {
							openFolders[cat.id] = !openFolders[cat.id];
						}}
						class="group flex w-full cursor-pointer items-center justify-between rounded-md border-0 bg-transparent px-2 py-1.5 text-left transition-all hover:bg-canvas-soft"
					>
						<div class="flex items-center gap-2">
							<div
								class="flex h-6 w-6 shrink-0 items-center justify-center text-body transition-colors group-hover:text-ink {hasActiveItem
									? 'text-ink'
									: ''}"
							>
								<CategoryIcon size={16} stroke={1.5} />
							</div>
							<span
								class="text-sidebar-label font-bold tracking-tight transition-colors {hasActiveItem
									? 'text-ink'
									: 'text-body'}"
							>
								{cat.label}
							</span>
						</div>
						<IconChevronDown
							size={12}
							stroke={1.5}
							class="text-body transition-transform duration-200 {openFolders[cat.id]
								? 'rotate-180'
								: ''}"
						/>
					</button>

					{#if openFolders[cat.id]}
						<div class="mt-0.5 flex flex-col gap-0.5 pr-1 pb-1 pl-1.5">
							{#each cat.items as item (item.label)}
								<a
									href={item.href}
									class="relative flex items-center border-l py-1 pr-3 pl-6.5 text-sidebar-label font-medium transition-all duration-150 {item.active
										? 'border-ink bg-canvas-soft-2 text-ink'
										: 'border-hairline text-body hover:bg-canvas-soft hover:text-ink'}"
								>
									<span class="truncate">{item.label}</span>
								</a>
							{/each}
						</div>
					{/if}
				{:else}
					<a
						href={cat.href}
						class="group flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left transition-all hover:bg-canvas-soft {cat.active
							? 'bg-canvas-soft-2 text-ink'
							: 'text-body hover:text-ink'}"
					>
						<div
							class="flex h-6 w-6 shrink-0 items-center justify-center transition-colors {cat.active
								? 'text-ink'
								: 'text-body group-hover:text-ink'}"
						>
							<CategoryIcon size={16} stroke={1.5} />
						</div>
						<span
							class="text-sidebar-label font-bold tracking-tight transition-colors {cat.active
								? 'text-ink'
								: 'text-body group-hover:text-ink'}"
						>
							{cat.label}
						</span>
					</a>
				{/if}
			</div>
		{/each}
	</div>

	<div class="shrink-0 space-y-0.5 border-t border-hairline bg-canvas p-2">
		<div class="relative">
			<button
				type="button"
				onclick={() => (showAccountMenu = !showAccountMenu)}
				class="flex w-full cursor-pointer items-center justify-between rounded-md border-0 bg-transparent p-1 text-left transition-all hover:bg-canvas-soft"
			>
				<div class="flex min-w-0 items-center gap-2">
					<div
						class="flex h-8 w-8 shrink-0 items-center justify-center rounded-md bg-canvas-soft-2 font-mono text-sidebar-caption font-semibold text-body uppercase"
					>
						AD
					</div>
					<div class="flex min-w-0 flex-col">
						<span class="truncate text-sidebar-label font-bold text-ink"> Admin </span>
						<span class="mt-1 truncate font-mono text-sidebar-tiny leading-none text-body uppercase">
							Platform Ops
						</span>
					</div>
				</div>
				<IconDots size={16} class="shrink-0 text-body hover:text-ink" />
			</button>

			{#if showAccountMenu}
				<button
					type="button"
					class="fixed inset-0 z-45 cursor-default bg-transparent"
					onclick={() => (showAccountMenu = false)}
					aria-label="Close user menu"
				></button>
				<div
					class="absolute right-0 bottom-full left-0 z-50 mb-1 rounded-md border border-hairline bg-canvas p-1 text-ink shadow-xl"
				>
					<div class="mb-1 border-b border-hairline px-2 py-1.5 select-none">
						<p class="text-sidebar-caption font-medium text-body">Signed in as</p>
						<p class="truncate font-mono text-sidebar-caption font-semibold text-ink">
							admin@ticketpeak.com
						</p>
					</div>
					<a
						href="/discover"
						class="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left text-sidebar-label font-medium text-body transition hover:bg-canvas-soft"
					>
						<IconExternalLink size={14} stroke={1.5} class="text-body" />
						Buyer Portal
					</a>
					<form method="POST" action="/logout" class="w-full">
						<button
							type="submit"
							class="flex w-full cursor-pointer items-center gap-2 rounded-md border-0 bg-transparent px-2 py-1.5 text-left text-sidebar-label font-semibold text-error transition hover:bg-error/10 focus:outline-none"
						>
							<IconLogout size={14} stroke={1.5} />
							Sign Out
						</button>
					</form>
				</div>
			{/if}
		</div>

		<button
			type="button"
			class="flex w-full cursor-pointer items-center gap-2 rounded-md border-0 bg-transparent px-2 py-1.5 text-left text-sidebar-label font-semibold text-body transition-all hover:bg-canvas-soft hover:text-ink"
		>
			<IconArrowLeft size={16} stroke={1.5} class="text-body" />
			<span>Collapse</span>
		</button>
	</div>
</aside>
