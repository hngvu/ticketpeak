<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve, @typescript-eslint/no-explicit-any */
	import type { Snippet } from 'svelte';
	import '../../routes/layout.css';
	import { page } from '$app/state';
	import {
		IconChevronRight,
		IconChevronDown,
		IconSearch,
		IconBell,
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
		IconPresentation
	} from '@tabler/icons-svelte';

	let { children }: { children: Snippet } = $props();

	let showAccountMenu = $state(false);

	let openFolders = $state<Record<string, boolean>>({
		overview: true,
		moderation: true,
		users: true,
		finance: true,
		content: true,
		settings: true
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
			id: 'analytics',
			label: 'Reports & Analytics',
			icon: IconPresentation,
			href: '/ops/dashboard?tab=analytics',
			active:
				page.url.pathname === '/ops/dashboard' && page.url.searchParams.get('tab') === 'analytics'
		},
		{
			id: 'moderation',
			label: 'Moderation',
			icon: IconCalendarEvent,
			items: [
				{
					label: 'Events Inventory',
					href: '/ops/dashboard?tab=events',
					active: page.url.searchParams.get('tab') === 'events'
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
				},
				{
					label: 'Venues',
					href: '/ops/venues',
					active: page.url.pathname === '/ops/venues'
				}
			]
		},
		{
			id: 'users',
			label: 'Users & Security',
			icon: IconUsers,
			items: [
				{
					label: 'Platform Users',
					href: '/ops/users',
					active: page.url.pathname === '/ops/users'
				},
				{
					label: 'Roles & Permissions',
					href: '/ops/roles',
					active: page.url.pathname === '/ops/roles'
				}
			]
		},
		{
			id: 'finance',
			label: 'Sales & Finance',
			icon: IconReceipt,
			items: [
				{
					label: 'Orders & Transactions',
					href: '/ops/dashboard?tab=orders',
					active: page.url.searchParams.get('tab') === 'orders'
				},
				{
					label: 'Payouts Queue',
					href: '/ops/dashboard?tab=payouts',
					active: page.url.searchParams.get('tab') === 'payouts'
				},
				{
					label: 'Platform Fees',
					href: '/ops/dashboard?tab=fees',
					active: page.url.searchParams.get('tab') === 'fees'
				},
				{
					label: 'Resale Marketplace',
					href: '/ops/dashboard?tab=resale',
					active: page.url.searchParams.get('tab') === 'resale'
				}
			]
		},
		{
			id: 'content',
			label: 'Metadata & CMS',
			icon: IconFileText,
			items: [
				{
					label: 'Classifications',
					href: '/ops/classifications',
					active: page.url.pathname === '/ops/classifications'
				},
				{
					label: 'Content / CMS',
					href: '/ops/dashboard?tab=cms',
					active: page.url.searchParams.get('tab') === 'cms'
				},
				{
					label: 'Promo Campaigns',
					href: '/ops/dashboard?tab=promotions',
					active: page.url.searchParams.get('tab') === 'promotions'
				}
			]
		},
		{
			id: 'settings',
			label: 'System Config',
			icon: IconSettings,
			items: [
				{
					label: 'Audit Logs',
					href: '/ops/dashboard?tab=logs',
					active: page.url.searchParams.get('tab') === 'logs'
				},
				{
					label: 'Platform Settings',
					href: '/ops/dashboard?tab=settings',
					active: page.url.searchParams.get('tab') === 'settings'
				},
				{
					label: 'System Status',
					href: '/ops/dashboard?tab=system',
					active: page.url.searchParams.get('tab') === 'system'
				}
			]
		}
	]);

	const activeCategory = $derived(
		categories.find((cat) => (cat.items ? cat.items.some((item) => item.active) : cat.active))
	);
	const activeItem = $derived(activeCategory?.items?.find((item) => item.active));

	const breadcrumbParent = $derived(activeCategory?.label || 'Overview');
	const breadcrumbChild = $derived(activeItem?.label || '');

	const isLoginPage = $derived(page.url.pathname === '/ops/login');
</script>

<div class="flex min-h-screen flex-col bg-canvas-soft">
	{#if isLoginPage}
		<main class="flex flex-1 flex-col">
			{@render children()}
		</main>
	{:else}
		<div class="flex h-screen w-screen flex-row overflow-hidden bg-canvas-soft text-ink">
			<!-- Sidebar (Full Height) -->
			<aside
				class="flex h-full w-56 shrink-0 flex-col justify-between border-r border-hairline bg-canvas"
			>
				<!-- Top Branding Block -->
				<div class="flex shrink-0 items-center justify-between border-b border-hairline p-3">
					<a href="/ops/dashboard" class="flex min-w-0 items-center gap-2">
						<!-- Amber OP Square Logo -->
						<div
							class="flex h-8 w-8 shrink-0 items-center justify-center rounded-md bg-ink text-sm font-bold tracking-tighter text-canvas shadow-sm select-none"
						>
							OP
						</div>
						<div class="flex min-w-0 flex-col">
							<span class="text-xs leading-none font-semibold tracking-tight text-ink"
								>Ticketpeak</span
							>
							<span
								class="mt-0.5 font-mono text-[9px] font-medium tracking-wider text-body uppercase"
								>Operations</span
							>
						</div>
					</a>
				</div>

				<!-- Sidebar Scrollable Body -->
				<div class="no-scrollbar flex flex-1 flex-col gap-1 overflow-y-auto p-2">
					{#each categories as cat (cat.id)}
						{@const CategoryIcon = cat.icon}
						{@const hasActiveItem = cat.items ? cat.items.some((i: any) => i.active) : cat.active}
						<div class="flex flex-col">
							{#if cat.items && cat.items.length > 0}
								<!-- Category Header Button -->
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
											class="text-[11px] font-bold tracking-tight transition-colors {hasActiveItem
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

								<!-- Collapsible Submenus List -->
								{#if openFolders[cat.id]}
									<div class="mt-0.5 flex flex-col gap-0.5 pr-1 pb-1 pl-1.5">
										{#each cat.items as item (item.label)}
											<a
												href={item.href}
												class="relative flex items-center border-l py-1 pr-3 pl-6.5 text-[11px] font-medium transition-all duration-150 {item.active
													? 'border-ink bg-canvas-soft-2 text-ink'
													: 'border-hairline text-body hover:bg-canvas-soft hover:text-ink'}"
											>
												<span class="truncate">{item.label}</span>
											</a>
										{/each}
									</div>
								{/if}
							{:else}
								<!-- Direct Link Item -->
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
										class="text-[11px] font-bold tracking-tight transition-colors {cat.active
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

				<!-- Bottom Actions and Profile Block -->
				<div class="shrink-0 space-y-0.5 border-t border-hairline bg-canvas p-2">
					<!-- Profile Card -->
					<div class="relative">
						<button
							type="button"
							onclick={() => (showAccountMenu = !showAccountMenu)}
							class="flex w-full cursor-pointer items-center justify-between rounded-md border-0 bg-transparent p-1 text-left transition-all hover:bg-canvas-soft"
						>
							<div class="flex min-w-0 items-center gap-2">
								<!-- Amber Initials Rounded Square -->
								<div
									class="flex h-8 w-8 shrink-0 items-center justify-center rounded-md bg-canvas-soft-2 font-mono text-[10px] font-semibold text-body uppercase"
								>
									AD
								</div>
								<div class="flex min-w-0 flex-col">
									<span class="truncate text-[11px] leading-none font-bold text-ink"> Admin </span>
									<span class="mt-1 truncate font-mono text-[9px] leading-none text-body uppercase">
										Platform Ops
									</span>
								</div>
							</div>
							<IconDots size={16} class="shrink-0 text-body hover:text-ink" />
						</button>

						<!-- Floating User Dropdown -->
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
									<p class="text-[10px] font-medium text-body">Signed in as</p>
									<p class="truncate font-mono text-[10px] font-semibold text-ink">
										admin@ticketpeak.com
									</p>
								</div>
								<a
									href="/discover"
									class="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left text-[11px] font-medium text-body transition hover:bg-canvas-soft"
								>
									<IconExternalLink size={14} stroke={1.5} class="text-body" />
									Buyer Portal
								</a>
								<form method="POST" action="/logout" class="w-full">
									<button
										type="submit"
										class="flex w-full cursor-pointer items-center gap-2 rounded-md border-0 bg-transparent px-2 py-1.5 text-left text-[11px] font-semibold text-error transition hover:bg-error/10 focus:outline-none"
									>
										<IconLogout size={14} stroke={1.5} />
										Sign Out
									</button>
								</form>
							</div>
						{/if}
					</div>

					<!-- Collapse Button -->
					<button
						type="button"
						class="flex w-full cursor-pointer items-center gap-2 rounded-md border-0 bg-transparent px-2 py-1.5 text-left text-[11px] font-semibold text-body transition-all hover:bg-canvas-soft hover:text-ink"
					>
						<IconArrowLeft size={16} stroke={1.5} class="text-body" />
						<span>Collapse</span>
					</button>
				</div>
			</aside>

			<!-- Right Pane Workspace (Header + Content) -->
			<div class="flex h-full min-w-0 flex-1 flex-col overflow-hidden bg-canvas-soft">
				<!-- Ops Top Header (White Theme) -->
				<header
					class="z-40 flex h-12 shrink-0 items-center justify-between border-b border-hairline bg-canvas px-4 text-ink"
				>
					<!-- Left: Breadcrumb Path -->
					<div class="flex items-center gap-2">
						{#if activeItem}
							<span class="text-[11px] font-medium text-body">
								{breadcrumbParent}
							</span>
							<IconChevronRight size={10} stroke={1.5} class="text-body" />
							<span class="text-xs font-semibold tracking-tight text-ink">
								{breadcrumbChild}
							</span>
						{:else}
							<span class="text-xs font-semibold tracking-tight text-ink">
								{breadcrumbParent}
							</span>
						{/if}
					</div>

					<!-- Right: Notifications, Apps -->
					<div class="flex items-center gap-3">
						<div class="relative w-56 shrink-0">
							<span
								class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-2.5 text-body"
							>
								<IconSearch size={14} stroke={1.5} />
							</span>
							<input
								type="text"
								placeholder="Search ops inventory..."
								class="w-full rounded-md border border-hairline bg-canvas-soft py-1 pr-10 pl-8 font-mono text-[11px] font-medium text-ink placeholder-body transition-all focus:border-body focus:bg-canvas focus:outline-none"
							/>
							<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2.5">
								<kbd
									class="rounded border border-hairline bg-canvas px-1.5 font-mono text-[9px] font-semibold text-body shadow-xs"
									>⌘K</kbd
								>
							</span>
						</div>

						<button
							type="button"
							class="relative rounded-md p-1.5 text-body transition-all hover:bg-canvas-soft hover:text-ink focus:outline-none"
							aria-label="Notifications"
						>
							<IconBell size={18} stroke={1.5} />
							<span
								class="absolute top-1.5 right-1.5 h-1 w-1 rounded-full bg-ink ring-1 ring-canvas"
							></span>
						</button>

						<button
							type="button"
							class="rounded-md p-1.5 text-body transition-all hover:bg-canvas-soft hover:text-ink focus:outline-none"
							aria-label="App switcher"
						>
							<IconLayoutGrid size={18} stroke={1.5} />
						</button>
					</div>
				</header>

				<!-- Main Content Viewport -->
				<main class="flex-1 overflow-y-auto">
					{@render children()}
				</main>
			</div>
		</div>
	{/if}
</div>
