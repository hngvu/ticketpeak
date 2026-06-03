<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
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
		IconFileText
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
			id: 'overview',
			label: 'Overview',
			icon: IconLayoutGrid,
			items: [
				{
					label: 'Dashboard',
					href: '/ops/dashboard?tab=dashboard',
					active:
						!page.url.searchParams.get('tab') || page.url.searchParams.get('tab') === 'dashboard'
				},
				{
					label: 'Reports & Analytics',
					href: '/ops/dashboard?tab=analytics',
					active: page.url.searchParams.get('tab') === 'analytics'
				}
			]
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
					label: 'Organizers Review',
					href: '/ops/organizations',
					active: page.url.pathname === '/ops/organizations'
				},
				{
					label: 'Attractions',
					href: '/ops/attractions',
					active: page.url.pathname === '/ops/attractions'
				},
				{
					label: 'Venues & Manifests',
					href: '/ops/dashboard?tab=venues',
					active: page.url.searchParams.get('tab') === 'venues'
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
					href: '/ops/dashboard?tab=users',
					active: page.url.searchParams.get('tab') === 'users'
				},
				{
					label: 'Roles & Permissions',
					href: '/ops/dashboard?tab=roles',
					active: page.url.searchParams.get('tab') === 'roles'
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
					label: 'Categories & Tags',
					href: '/ops/dashboard?tab=classifications',
					active: page.url.searchParams.get('tab') === 'classifications'
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

	const activeCategory = $derived(categories.find((cat) => cat.items.some((item) => item.active)));
	const activeItem = $derived(activeCategory?.items.find((item) => item.active));

	const breadcrumbParent = $derived(activeCategory?.label || 'Overview');
	const breadcrumbChild = $derived(activeItem?.label || 'Dashboard');

	const isLoginPage = $derived(page.url.pathname === '/ops/login');
</script>

<div class="flex min-h-screen flex-col bg-[#FAFAFA]">
	{#if isLoginPage}
		<main class="flex flex-1 flex-col">
			{@render children()}
		</main>
	{:else}
		<div class="flex h-screen w-screen flex-row overflow-hidden bg-[#FAFAFA] text-[#111111]">
			<!-- Sidebar (Full Height) -->
			<aside
				class="flex h-full w-64 shrink-0 flex-col justify-between border-r border-[#E4E4E7] bg-white"
			>
				<!-- Top Branding Block -->
				<div class="flex shrink-0 items-center justify-between border-b border-[#F4F4F5] p-4">
					<a href="/ops/dashboard" class="flex min-w-0 items-center gap-2">
						<!-- Amber OP Square Logo -->
						<div
							class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#111111] text-base font-bold tracking-tighter text-white shadow-sm select-none"
						>
							OP
						</div>
						<div class="flex min-w-0 flex-col">
							<span class="text-sm leading-none font-semibold tracking-tight text-[#111111]"
								>Ticketpeak</span
							>
							<span class="mt-0.5 text-[9px] font-semibold tracking-wider text-[#71717A]"
								>Operations Portal</span
							>
						</div>
					</a>
				</div>

				<!-- Sidebar Scrollable Body -->
				<div class="no-scrollbar flex flex-1 flex-col gap-1.5 overflow-y-auto p-3.5">
					{#each categories as cat (cat.id)}
						{@const CategoryIcon = cat.icon}
						{@const hasActiveItem = cat.items.some((i) => i.active)}
						<div class="flex flex-col">
							<!-- Category Header Button -->
							<button
								type="button"
								onclick={() => {
									openFolders[cat.id] = !openFolders[cat.id];
								}}
								class="group flex w-full cursor-pointer items-center justify-between rounded-lg border-0 bg-transparent px-2.5 py-2 text-left transition-all hover:bg-[#FAFAFA]"
							>
								<div class="flex items-center gap-2.5">
									<div
										class="flex h-7 w-7 shrink-0 items-center justify-center text-[#71717A] transition-colors group-hover:text-[#111111] {hasActiveItem
											? 'text-[#111111]'
											: ''}"
									>
										<CategoryIcon size={18} stroke={1.8} />
									</div>
									<span
										class="text-[11px] font-bold tracking-wider transition-colors {hasActiveItem
											? 'text-[#111111]'
											: 'text-[#71717A]'}"
									>
										{cat.label}
									</span>
								</div>
								<IconChevronDown
									size={13}
									stroke={2.5}
									class="text-[#71717A] transition-transform duration-200 {openFolders[cat.id]
										? 'rotate-180'
										: ''}"
								/>
							</button>

							<!-- Collapsible Submenus List -->
							{#if openFolders[cat.id]}
								<div class="mt-0.5 flex flex-col gap-1 pr-1 pb-1.5 pl-2">
									{#each cat.items as item (item.label)}
										<a
											href={item.href}
											class="relative flex items-center border-l-[3px] py-1.5 pr-3.5 pl-9 text-xs font-semibold transition-all duration-150 {item.active
												? 'rounded-r-lg border-[#111111] bg-[#F4F4F5] text-[#111111]'
												: 'border-transparent text-[#71717A] hover:bg-[#FAFAFA] hover:text-[#111111]'}"
										>
											<span class="truncate">{item.label}</span>
										</a>
									{/each}
								</div>
							{/if}
						</div>
					{/each}
				</div>

				<!-- Bottom Actions and Profile Block -->
				<div class="shrink-0 space-y-1 border-t border-[#F4F4F5] bg-white p-3.5">
					<!-- Profile Card -->
					<div class="relative">
						<button
							type="button"
							onclick={() => (showAccountMenu = !showAccountMenu)}
							class="flex w-full cursor-pointer items-center justify-between rounded-lg border-0 bg-transparent p-1.5 text-left transition-all hover:bg-[#FAFAFA]"
						>
							<div class="flex min-w-0 items-center gap-2.5">
								<!-- Amber Initials Rounded Square -->
								<div
									class="flex h-9 w-9 shrink-0 items-center justify-center rounded-md bg-[#F4F4F5] text-xs font-semibold text-[#71717A] uppercase"
								>
									AD
								</div>
								<div class="flex min-w-0 flex-col">
									<span class="truncate text-xs leading-none font-bold text-[#111111]">
										Admin
									</span>
									<span class="mt-1 truncate text-[10px] leading-none text-[#71717A]">
										Platform Ops
									</span>
								</div>
							</div>
							<IconDots size={20} class="shrink-0 text-[#71717A] hover:text-[#111111]" />
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
								class="absolute right-0 bottom-full left-0 z-50 mb-1 rounded-lg border border-[#E4E4E7] bg-white p-2 text-[#111111] shadow-xl"
							>
								<div class="mb-1 border-b border-[#F4F4F5] px-3 py-2 select-none">
									<p class="text-xs font-medium text-[#71717A]">Signed in as</p>
									<p class="truncate text-xs font-semibold text-[#111111]">admin@ticketpeak.com</p>
								</div>
								<a
									href="/discover"
									class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-xs font-medium text-[#71717A] transition hover:bg-[#FAFAFA]"
								>
									<IconExternalLink size={16} stroke={2} class="text-[#71717A]" />
									Buyer Portal
								</a>
								<form method="POST" action="/logout" class="w-full">
									<button
										type="submit"
										class="flex w-full cursor-pointer items-center gap-2 rounded-lg border-0 bg-transparent px-3 py-2 text-left text-xs font-semibold text-red-600 transition hover:bg-red-50 focus:outline-none"
									>
										<IconLogout size={16} stroke={2} />
										Sign Out
									</button>
								</form>
							</div>
						{/if}
					</div>

					<!-- Collapse Button -->
					<button
						type="button"
						class="flex w-full cursor-pointer items-center gap-3 rounded-lg border-0 bg-transparent px-3 py-2 text-left text-xs font-semibold text-[#71717A] transition-all hover:bg-[#FAFAFA] hover:text-[#111111]"
					>
						<IconArrowLeft size={18} stroke={1.8} class="text-[#71717A]" />
						<span>Collapse</span>
					</button>
				</div>
			</aside>

			<!-- Right Pane Workspace (Header + Content) -->
			<div class="flex h-full min-w-0 flex-1 flex-col overflow-hidden bg-[#FAFAFA]">
				<!-- Ops Top Header (White Theme) -->
				<header
					class="z-40 flex h-16 shrink-0 items-center justify-between border-b border-[#E4E4E7] bg-white px-6 text-[#111111]"
				>
					<!-- Left: Breadcrumb Path -->
					<div class="flex items-center gap-2">
						<span class="text-xs font-medium text-[#71717A]">
							{breadcrumbParent}
						</span>
						<IconChevronRight size={12} stroke={2.5} class="text-[#71717A]" />
						<span class="text-sm font-semibold tracking-tight text-[#111111]">
							{breadcrumbChild}
						</span>
					</div>

					<!-- Right: Notifications, Apps -->
					<div class="flex items-center gap-4">
						<div class="relative w-60 shrink-0">
							<span
								class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-[#71717A]"
							>
								<IconSearch size={16} stroke={2.2} />
							</span>
							<input
								type="text"
								placeholder="Search ops inventory..."
								class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] py-1.5 pr-12 pl-9 text-xs font-medium text-[#111111] placeholder-[#71717A] transition-all focus:border-[#71717A] focus:bg-white focus:outline-none"
							/>
							<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
								<kbd
									class="rounded border border-[#E4E4E7] bg-white px-1.5 text-[9px] font-semibold text-[#71717A] shadow-xs"
									>⌘K</kbd
								>
							</span>
						</div>

						<button
							type="button"
							class="relative rounded-lg p-2 text-[#71717A] transition-all hover:bg-[#FAFAFA] hover:text-[#111111] focus:outline-none"
							aria-label="Notifications"
						>
							<IconBell size={20} stroke={2} />
							<span
								class="absolute top-2 right-2 h-1.5 w-1.5 rounded-full bg-[#111111] ring-2 ring-white"
							></span>
						</button>

						<button
							type="button"
							class="rounded-lg p-2 text-[#71717A] transition-all hover:bg-[#FAFAFA] hover:text-[#111111] focus:outline-none"
							aria-label="App switcher"
						>
							<IconLayoutGrid size={20} stroke={2.2} />
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
