<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
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
		IconSpeakerphone,
		IconTicket,
		IconScan,
		IconChartBar,
		IconCalendarEvent,
		IconDots,
		IconSparkles,
		IconArrowLeft,
		IconHelpCircle
	} from '@tabler/icons-svelte';

	let { data, children }: { data: any; children: Snippet } = $props();

	let showAccountMenu = $state(false);

	let openFolders = $state<Record<string, boolean>>({
		events: true,
		sales: false,
		entry: false,
		reports: false,
		marketing: false,
		attractions: false,
		tools: false
	});

	const isLoginPageOrLanding = $derived(page.url.pathname === '/b2b/login');

	const categories = $derived([
		{
			id: 'events',
			label: 'EVENTS',
			subtitle: 'Quản lý Sự kiện',
			icon: IconCalendarEvent,
			items: [
				{
					label: 'Event Management',
					href: `/b2b/events?organizationId=${data.selectedOrgId || ''}`,
					active: page.url.pathname.startsWith('/b2b/events')
				},
				{
					label: 'Platinum Tool',
					href: '#',
					active: false
				},
				{
					label: 'Publish',
					href: '#',
					active: false
				},
				{
					label: 'Scaling',
					href: '#',
					active: false
				}
			]
		},
		{
			id: 'attractions',
			label: 'ATTRACTIONS',
			subtitle: 'Nghệ sĩ & Diễn viên',
			icon: IconSparkles,
			items: []
		},
		{
			id: 'reports',
			label: 'REPORTS',
			subtitle: 'Kiểm toán & Đối soát',
			icon: IconChartBar,
			items: [
				{
					label: 'Overview',
					href: `/b2b/reports?tab=overview&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/reports') &&
						(page.url.searchParams.get('tab') === 'overview' || !page.url.searchParams.get('tab'))
				},
				{
					label: 'Sales Report',
					href: `/b2b/reports?tab=sales-report&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/reports') &&
						page.url.searchParams.get('tab') === 'sales-report'
				},
				{
					label: 'Inventory Status',
					href: `/b2b/reports?tab=inventory-status&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/reports') &&
						page.url.searchParams.get('tab') === 'inventory-status'
				}
			]
		},
		{
			id: 'marketing',
			label: 'MARKETING',
			subtitle: 'Tiếp cận & Tối ưu hóa',
			icon: IconSpeakerphone,
			items: [
				{
					label: 'Campaigns',
					href: `/b2b/marketing?tab=campaigns&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/marketing') &&
						(page.url.searchParams.get('tab') === 'campaigns' || !page.url.searchParams.get('tab'))
				},
				{
					label: 'Audience Segments',
					href: `/b2b/marketing?tab=segments&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/marketing') &&
						page.url.searchParams.get('tab') === 'segments'
				}
			]
		},
		{
			id: 'sales',
			label: 'SALES',
			subtitle: 'Thương mại & Kho vé',
			icon: IconTicket,
			isExternal: true,
			items: [
				{
					label: 'Real-time Sales',
					href: `/b2b/sales?tab=realtime&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/sales') &&
						page.url.searchParams.get('tab') === 'realtime'
				},
				{
					label: 'Inventory',
					href: `/b2b/sales?tab=inventory&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/sales') &&
						page.url.searchParams.get('tab') === 'inventory'
				},
				{
					label: 'Price Management',
					href: `/b2b/sales?tab=pricing&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/sales') &&
						page.url.searchParams.get('tab') === 'pricing'
				}
			]
		},
		{
			id: 'tools',
			label: 'TOOLS',
			subtitle: 'Vận hành & Kiểm soát',
			icon: IconScan,
			items: [
				{
					label: 'Gate Simulator',
					href: `/b2b/entry?tab=entrances&organizationId=${data.selectedOrgId || ''}`,
					active: page.url.pathname.startsWith('/b2b/entry')
				}
			]
		}
	]);

	const activeOrg = $derived(data.organizations?.find((o: any) => o.id === data.selectedOrgId));

	const activeCategory = $derived(categories.find((cat) => cat.items.some((item) => item.active)));
	const activeItem = $derived(activeCategory?.items.find((item) => item.active));

	const breadcrumbParent = $derived(
		activeCategory?.label ||
			(page.url.pathname.startsWith('/b2b/check-in') ? 'Entry' : '') ||
			(page.url.pathname === '/b2b/schedule' ? 'Main' : 'Dashboard')
	);
	const breadcrumbChild = $derived(
		activeItem?.label ||
			(page.url.pathname.startsWith('/b2b/check-in') ? 'Check-In Gate Simulator' : '') ||
			(page.url.pathname === '/b2b/schedule' ? 'Schedule' : 'Overview')
	);

	$effect(() => {
		categories.forEach((cat) => {
			if (cat.items.some((item) => item.active)) {
				openFolders[cat.id] = true;
			}
		});
	});
</script>

<div class="flex min-h-screen flex-col bg-canvas-soft">
	{#if isLoginPageOrLanding}
		<header
			class="sticky top-0 z-40 flex items-center justify-between border-b border-slate-800 bg-slate-900 px-6 py-4 text-white"
		>
			<div class="flex items-center gap-3">
				<a href="/business" class="flex items-center gap-2">
					<img src="/logo.png" alt="Ticketpeak Logo" class="h-8 brightness-0 invert" />
					<span
						class="rounded-full bg-primary/20 px-2.5 py-0.5 text-xs font-semibold tracking-wider text-primary uppercase"
					>
						for Business
					</span>
				</a>
			</div>

			<nav class="flex items-center gap-4">
				{#if data.user && data.user.role === 'ORGANIZER'}
					<a href="/b2b/dashboard" class="text-sm font-medium transition hover:text-primary">
						Dashboard
					</a>
					<form method="POST" action="/logout">
						<button
							type="submit"
							class="cursor-pointer rounded-full bg-slate-800 px-4.5 py-2 text-xs font-semibold text-white transition hover:bg-slate-700"
						>
							Sign Out
						</button>
					</form>
				{:else}
					<a
						href="/b2b/login"
						class="text-sm font-semibold text-gray-300 transition hover:text-white"
					>
						Sign In
					</a>
					<a
						href="/auth?tab=register&redirect=/b2b/dashboard"
						class="hover:bg-primary-hover rounded-full bg-primary px-4.5 py-2 text-xs font-semibold text-white transition active:scale-95"
					>
						Get Started
					</a>
				{/if}
			</nav>
		</header>

		<main class="flex flex-1 flex-col">
			{@render children()}
		</main>
	{:else}
		<div class="flex h-screen w-screen flex-row overflow-hidden bg-slate-50/40">
			<!-- Sidebar (Full Height) -->
			<aside
				class="flex h-full w-64 shrink-0 flex-col justify-between border-r border-slate-200 bg-white"
			>
				<!-- Top Branding Block -->
				<div class="flex shrink-0 items-center justify-between border-b border-slate-100 p-4">
					<a href="/b2b/events" class="flex min-w-0 items-center gap-2">
						<!-- Blue TP Square Logo -->
						<div
							class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-blue-600 text-base font-bold tracking-tighter text-white shadow-sm"
						>
							TP
						</div>
						<div class="flex min-w-0 flex-col">
							<span class="text-sm leading-none font-semibold tracking-tight text-slate-800"
								>Ticketpeak</span
							>
							<span class="mt-0.5 text-[9px] font-semibold tracking-wider text-slate-400 uppercase"
								>Enterprise Platform</span
							>
						</div>
					</a>
				</div>

				<!-- Sidebar Scrollable Body -->
				<div class="no-scrollbar flex flex-1 flex-col gap-1.5 overflow-y-auto p-3.5">
					{#each categories as cat (cat.id)}
						{@const CategoryIcon = cat.icon}
						{@const hasActiveItem = cat.items.some((i) => i.active)}
						<!-- Sidebar Category block (flat style, no borders or background) -->
						<div class="flex flex-col">
							<!-- Category Header Toggle Button -->
							<button
								type="button"
								onclick={() => {
									if (cat.items.length > 0) {
										openFolders[cat.id] = !openFolders[cat.id];
									}
								}}
								class="flex w-full cursor-pointer items-center justify-between rounded-lg border-0 bg-transparent px-2.5 py-2 text-left transition-all hover:bg-slate-50"
							>
								<div class="flex items-center gap-3.5">
									<!-- Highlight icon in blue if there is an active submenu item, matching screenshot -->
									<div
										class="shrink-0 transition-colors {hasActiveItem
											? 'text-blue-600'
											: 'text-slate-400'}"
									>
										<CategoryIcon size={18} stroke={hasActiveItem ? 2.2 : 1.8} />
									</div>
									<span
										class="text-[11px] font-bold tracking-wider uppercase transition-colors {hasActiveItem
											? 'text-slate-900'
											: 'text-slate-500'}"
									>
										{cat.label}
									</span>
								</div>
								{#if cat.items.length > 0}
									<IconChevronDown
										size={13}
										stroke={2.5}
										class="text-slate-400 transition-transform duration-200 {openFolders[cat.id]
											? 'rotate-180'
											: ''}"
									/>
								{:else if cat.isExternal}
									<IconExternalLink size={13} stroke={2} class="text-slate-400" />
								{/if}
							</button>

							<!-- Collapsible Submenus List -->
							{#if openFolders[cat.id] && cat.items.length > 0}
								<div class="mt-0.5 flex flex-col gap-1 pr-1 pb-1.5 pl-9">
									{#each cat.items as item (item.label)}
										<a
											href={item.href}
											class="flex items-center rounded-lg px-3.5 py-1.5 text-xs font-semibold transition-all duration-150 {item.active
												? 'bg-blue-50/70 font-semibold text-blue-600'
												: 'text-slate-500 hover:bg-slate-50 hover:text-slate-800'}"
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
				<div class="shrink-0 space-y-1 border-t border-slate-100 bg-white p-3.5">
					<!-- Help Link -->
					<a
						href="#help"
						class="flex items-center justify-between rounded-lg px-3 py-2 text-xs font-semibold text-slate-500 transition-all hover:bg-slate-50 hover:text-slate-800"
					>
						<div class="flex items-center gap-3">
							<IconHelpCircle size={18} stroke={1.8} class="text-slate-400" />
							<span>Help</span>
						</div>
						<IconChevronRight size={12} stroke={2.5} class="text-slate-350" />
					</a>

					<!-- Profile Card -->
					<div class="relative">
						<button
							type="button"
							onclick={() => (showAccountMenu = !showAccountMenu)}
							class="flex w-full cursor-pointer items-center justify-between rounded-xl border-0 bg-transparent p-1.5 text-left transition-all hover:bg-slate-50"
						>
							<div class="flex min-w-0 items-center gap-2.5">
								<!-- Slate Initials Rounded Square -->
								<div
									class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-xs font-bold text-slate-700 uppercase"
								>
									{data.user?.fullName?.[0]?.toUpperCase() || 'D'}{data.user?.fullName
										?.split(' ')?.[1]?.[0]
										?.toUpperCase() || 'Z'}
								</div>
								<div class="flex min-w-0 flex-col">
									<span class="truncate text-xs leading-none font-bold text-slate-800 uppercase">
										{data.user?.fullName || 'DANIELLE'}
									</span>
									<span
										class="mt-1 truncate text-[9px] leading-none font-semibold text-slate-400 uppercase"
									>
										{activeOrg?.name || 'Default Organi...'}
									</span>
								</div>
							</div>
							<IconDots size={20} class="shrink-0 text-slate-400 hover:text-slate-700" />
						</button>

						<!-- Floating Account / User Dropdown Menu -->
						{#if showAccountMenu}
							<button
								type="button"
								class="fixed inset-0 z-45 cursor-default bg-transparent"
								onclick={() => (showAccountMenu = false)}
								aria-label="Close user menu"
							></button>
							<div
								class="absolute right-0 bottom-full left-0 z-50 mb-1 rounded-xl border border-slate-200 bg-white p-2 text-slate-800 shadow-xl"
							>
								<div class="mb-1 border-b border-slate-100 px-3 py-2 select-none">
									<p class="text-xs font-medium text-slate-400">Signed in as</p>
									<p class="truncate text-xs font-semibold text-slate-900">{data.user?.email}</p>
								</div>
								<a
									href="/discover"
									class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-xs font-medium text-slate-700 transition hover:bg-slate-50"
								>
									<IconExternalLink size={16} stroke={2} class="text-slate-400" />
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
						class="flex w-full cursor-pointer items-center gap-3 rounded-lg border-0 bg-transparent px-3 py-2 text-left text-xs font-semibold text-slate-500 transition-all hover:bg-slate-50 hover:text-slate-800"
					>
						<IconArrowLeft size={18} stroke={1.8} class="text-slate-400" />
						<span>Collapse</span>
					</button>
				</div>
			</aside>

			<!-- Right Pane Workspace (Header + Content) -->
			<div class="flex h-full min-w-0 flex-1 flex-col overflow-hidden">
				<!-- B2B Top Header (White Theme) -->
				<header
					class="z-40 flex h-16 shrink-0 items-center justify-between border-b border-slate-200 bg-white px-6 text-slate-800"
				>
					<!-- Left: Breadcrumb / Active Context Path -->
					<div class="flex items-center gap-2">
						<span class="text-[10px] font-semibold tracking-widest text-slate-400 uppercase">
							{breadcrumbParent}
						</span>
						<IconChevronRight size={12} stroke={2.5} class="text-slate-300" />
						<span class="text-sm font-bold tracking-tight text-slate-900">
							{breadcrumbChild}
						</span>
					</div>

					<!-- Right: Search Bar, Notifications, Grid Apps -->
					<div class="flex items-center gap-4">
						<!-- Search Input (TP1 Style) -->
						<div class="relative w-60 shrink-0">
							<span
								class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400"
							>
								<IconSearch size={16} stroke={2.2} />
							</span>
							<input
								type="text"
								placeholder="Search events..."
								class="w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pr-12 pl-9 text-xs font-medium text-slate-800 placeholder-slate-400 transition-all focus:border-blue-500 focus:bg-white focus:outline-none"
							/>
							<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
								<kbd
									class="rounded border border-slate-200/80 bg-white px-1.5 text-[9px] font-semibold text-slate-400 shadow-sm"
									>⌘K</kbd
								>
							</span>
						</div>

						<!-- Notifications (Bell) -->
						<button
							type="button"
							class="relative rounded-lg p-2 text-slate-500 transition-all hover:bg-slate-50 hover:text-slate-800 focus:outline-none"
							aria-label="Notifications"
						>
							<IconBell size={20} stroke={2} />
							<span
								class="absolute top-2 right-2 h-1.5 w-1.5 rounded-full bg-red-500 ring-2 ring-white"
							></span>
						</button>

						<!-- Layout / Apps switcher icon -->
						<button
							type="button"
							class="rounded-lg p-2 text-slate-500 transition-all hover:bg-slate-50 hover:text-slate-800 focus:outline-none"
							aria-label="App switcher"
						>
							<IconLayoutGrid size={20} stroke={2.2} />
						</button>
					</div>
				</header>

				<!-- Main Content Viewport -->
				<main class="flex-1 overflow-y-auto bg-slate-50/30">
					{@render children()}
				</main>
			</div>
		</div>
	{/if}
</div>
