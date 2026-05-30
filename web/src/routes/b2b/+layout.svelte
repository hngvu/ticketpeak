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
		IconReportMoney,
		IconScan,
		IconChartBar,
		IconLayoutDashboard,
		IconCalendar
	} from '@tabler/icons-svelte';

	let { data, children }: { data: any; children: Snippet } = $props();

	let showAccountMenu = $state(false);
	let showOrgMenu = $state(false);

	let openFolders = $state<Record<string, boolean>>({
		events: true,
		sales: false,
		entry: false,
		reports: false,
		marketing: false
	});

	const isLoginPageOrLanding = $derived(page.url.pathname === '/b2b/login');

	const categories = $derived([
		{
			id: 'events',
			label: 'Events',
			subtitle: 'Cấu hình & Hạ tầng',
			icon: IconTicket,
			items: [
				{
					label: 'All Events',
					href: `/b2b/events?tab=all&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/events') &&
						page.url.searchParams.get('tab') === 'all'
				},
				{
					label: 'Create Event',
					href: `/b2b/events/create?organizationId=${data.selectedOrgId || ''}`,
					active: page.url.pathname === '/b2b/events/create'
				},
				{
					label: 'Event Groups',
					href: `/b2b/events?tab=groups&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/events') &&
						page.url.searchParams.get('tab') === 'groups'
				},
				{
					label: 'Bulk Edit',
					href: `/b2b/events?tab=bulk&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/events') &&
						page.url.searchParams.get('tab') === 'bulk'
				},
				{
					label: 'Templates',
					href: `/b2b/events?tab=templates&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/events') &&
						page.url.searchParams.get('tab') === 'templates'
				},
				{
					label: 'Offers & Presales',
					href: `/b2b/events?tab=offers&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/events') &&
						page.url.searchParams.get('tab') === 'offers'
				},
				{
					label: 'Holds & Kills',
					href: `/b2b/events?tab=holds&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/events') &&
						page.url.searchParams.get('tab') === 'holds'
				},
				{
					label: 'Seat Map',
					href: `/b2b/events?tab=ism&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/events') &&
						page.url.searchParams.get('tab') === 'ism'
				},
				{
					label: 'Collaborators',
					href: `/b2b/events?tab=collaborators&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/events') &&
						page.url.searchParams.get('tab') === 'collaborators'
				}
			]
		},
		{
			id: 'sales',
			label: 'Sales',
			subtitle: 'Thương mại & Kho vé',
			icon: IconReportMoney,
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
				},
				{
					label: 'Orders',
					href: `/b2b/sales?tab=orders&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/sales') &&
						(page.url.searchParams.get('tab') === 'orders' || !page.url.searchParams.get('tab'))
				},
				{
					label: 'Price Requests',
					href: `/b2b/sales?tab=requests&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/sales') &&
						page.url.searchParams.get('tab') === 'requests'
				}
			]
		},
		{
			id: 'entry',
			label: 'Entry',
			subtitle: 'Vận hành & Kiểm soát',
			icon: IconScan,
			items: [
				{
					label: 'Entrances',
					href: `/b2b/entry?tab=entrances&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/entry') &&
						page.url.searchParams.get('tab') === 'entrances'
				},
				{
					label: 'Scan Devices',
					href: `/b2b/entry?tab=devices&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/entry') &&
						page.url.searchParams.get('tab') === 'devices'
				},
				{
					label: 'Attendance',
					href: `/b2b/entry?tab=attendance&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/entry') &&
						(page.url.searchParams.get('tab') === 'attendance' || !page.url.searchParams.get('tab'))
				},
				{
					label: 'Pre-Entry Check',
					href: `/b2b/entry?tab=precheck&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/entry') &&
						page.url.searchParams.get('tab') === 'precheck'
				},
				{
					label: 'Offline Barcodes',
					href: `/b2b/entry?tab=offline&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/entry') &&
						page.url.searchParams.get('tab') === 'offline'
				},
				{
					label: 'Ticket Rules',
					href: `/b2b/entry?tab=rules&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/entry') &&
						page.url.searchParams.get('tab') === 'rules'
				},
				{
					label: 'Scan Log Export',
					href: `/b2b/entry?tab=export&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/entry') &&
						page.url.searchParams.get('tab') === 'export'
				}
			]
		},
		{
			id: 'reports',
			label: 'Reports',
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
				},
				{
					label: 'Sales Trends',
					href: `/b2b/reports?tab=trends&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/reports') &&
						page.url.searchParams.get('tab') === 'trends'
				},
				{
					label: 'Demographics',
					href: `/b2b/reports?tab=demographics&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/reports') &&
						page.url.searchParams.get('tab') === 'demographics'
				},
				{
					label: 'Sales Comparison',
					href: `/b2b/reports?tab=comparison&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/reports') &&
						page.url.searchParams.get('tab') === 'comparison'
				},
				{
					label: 'Fans Expected',
					href: `/b2b/reports?tab=expected&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/reports') &&
						page.url.searchParams.get('tab') === 'expected'
				},
				{
					label: 'Custom Reports',
					href: `/b2b/reports?tab=custom&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/reports') &&
						page.url.searchParams.get('tab') === 'custom'
				}
			]
		},
		{
			id: 'marketing',
			label: 'Marketing',
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
				},
				{
					label: 'Email',
					href: `/b2b/marketing?tab=email&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/marketing') &&
						page.url.searchParams.get('tab') === 'email'
				},
				{
					label: 'Promoted Ads',
					href: `/b2b/marketing?tab=ads&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/marketing') &&
						page.url.searchParams.get('tab') === 'ads'
				},
				{
					label: 'Performance',
					href: `/b2b/marketing?tab=performance&organizationId=${data.selectedOrgId || ''}`,
					active:
						page.url.pathname.startsWith('/b2b/marketing') &&
						page.url.searchParams.get('tab') === 'performance'
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
						class="rounded-full bg-primary/20 px-2.5 py-0.5 text-xs font-bold tracking-wider text-primary uppercase"
					>
						for Business
					</span>
				</a>
			</div>

			<nav class="flex items-center gap-4">
				{#if data.user && data.user.role === 'ORGANIZER'}
					<a href="/b2b/dashboard" class="text-sm font-semibold transition hover:text-primary">
						Dashboard
					</a>
					<form method="POST" action="/logout">
						<button
							type="submit"
							class="cursor-pointer rounded-full bg-slate-800 px-4.5 py-2 text-xs font-bold text-white transition hover:bg-slate-700"
						>
							Sign Out
						</button>
					</form>
				{:else}
					<a href="/b2b/login" class="text-sm font-bold text-gray-300 transition hover:text-white">
						Sign In
					</a>
					<a
						href="/auth?tab=register&redirect=/b2b/dashboard"
						class="hover:bg-primary-hover rounded-full bg-primary px-4.5 py-2 text-xs font-bold text-white transition active:scale-95"
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
					<a href="/b2b/dashboard" class="flex min-w-0 items-center gap-2">
						<!-- Blue TP Square Logo -->
						<div
							class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-blue-600 text-base font-extrabold tracking-tighter text-white shadow-sm"
						>
							TP
						</div>
						<div class="flex min-w-0 flex-col">
							<span class="text-sm leading-none font-bold tracking-tight text-slate-800"
								>Ticketpeak</span
							>
							<span class="mt-0.5 text-[9px] font-bold tracking-wider text-slate-400 uppercase"
								>Enterprise Platform</span
							>
						</div>
					</a>
				</div>

				<!-- Promoter / Organization Selector Box -->
				<div class="relative shrink-0 border-b border-slate-100/60 px-3 py-2">
					<button
						type="button"
						onclick={() => (showOrgMenu = !showOrgMenu)}
						class="flex w-full items-center justify-between rounded-xl border border-slate-200/70 bg-slate-50/50 p-2 text-left transition-all hover:bg-slate-50"
					>
						<div class="flex min-w-0 items-center gap-2.5">
							<!-- LN Initials Rounded Square -->
							<div
								class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border border-blue-100 bg-blue-50 text-xs font-extrabold text-blue-600"
							>
								{activeOrg?.name?.[0]?.toUpperCase() || 'T'}{activeOrg?.name
									?.split(' ')?.[1]?.[0]
									?.toUpperCase() || 'P'}
							</div>
							<div class="flex min-w-0 flex-col">
								<span class="truncate text-xs leading-tight font-bold text-slate-700">
									{activeOrg?.name || 'Live Nation APAC'}
								</span>
								<span class="mt-0.5 text-[9px] font-semibold text-slate-400"
									>Promoter · 12 venues</span
								>
							</div>
						</div>
						<IconChevronDown
							size={14}
							stroke={2.5}
							class="shrink-0 text-slate-400 transition-transform {showOrgMenu ? 'rotate-180' : ''}"
						/>
					</button>

					<!-- Floating Organization Dropdown -->
					{#if showOrgMenu}
						<button
							type="button"
							class="fixed inset-0 z-40 bg-transparent"
							onclick={() => (showOrgMenu = false)}
							aria-label="Close organization menu"
						></button>
						<div
							class="absolute top-full right-3 left-3 z-50 mt-1 rounded-xl border border-slate-200 bg-white p-2 shadow-xl"
						>
							<div class="mb-1.5 border-b border-slate-100 px-2 pb-1.5">
								<p class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">
									Switch Organization
								</p>
							</div>
							<div class="flex max-h-40 flex-col gap-0.5 overflow-y-auto">
								{#each data.organizations || [] as org (org.id)}
									<button
										type="button"
										onclick={() => {
											showOrgMenu = false;
											window.location.href = `?organizationId=${org.id}`;
										}}
										class="flex w-full items-center gap-2.5 rounded-lg px-2 py-1.5 text-left text-xs font-semibold transition-all hover:bg-slate-50 {org.id ===
										data.selectedOrgId
											? 'bg-blue-50/55 font-bold text-blue-600'
											: 'text-slate-600'}"
									>
										<span
											class="flex h-6 w-6 shrink-0 items-center justify-center rounded-md bg-slate-100 text-[10px] font-extrabold text-slate-500"
										>
											{org.name?.[0]?.toUpperCase() || 'O'}
										</span>
										<span class="truncate">{org.name}</span>
									</button>
								{/each}
							</div>
						</div>
					{/if}
				</div>

				<!-- Sidebar Scrollable Body -->
				<div class="no-scrollbar flex flex-1 flex-col gap-1 overflow-y-auto p-3">
					<!-- MAIN SECTION -->
					<div class="px-3 pt-2 pb-1">
						<span class="text-[9px] font-extrabold tracking-wider text-slate-400 uppercase"
							>MAIN</span
						>
					</div>

					<!-- Dashboard link -->
					<a
						href={`/b2b/dashboard?organizationId=${data.selectedOrgId || ''}`}
						class="flex items-center gap-3.5 rounded-xl px-3 py-2 text-xs font-extrabold transition-all duration-200 {page
							.url.pathname === '/b2b/dashboard'
							? 'bg-blue-50/50 text-blue-600'
							: 'text-slate-500 hover:bg-slate-50 hover:text-slate-800'}"
					>
						<IconLayoutDashboard size={20} stroke={1.8} class="shrink-0" />
						<span class="truncate">Dashboard</span>
					</a>

					<!-- Schedule list link with 24 badge -->
					<a
						href={`/b2b/schedule?organizationId=${data.selectedOrgId || ''}`}
						class="flex items-center justify-between rounded-xl px-3 py-2 text-xs font-extrabold transition-all duration-200 {page
							.url.pathname === '/b2b/schedule'
							? 'bg-blue-50/50 text-blue-600'
							: 'text-slate-500 hover:bg-slate-50 hover:text-slate-800'}"
					>
						<div class="flex min-w-0 items-center gap-3.5">
							<IconCalendar size={20} stroke={1.8} class="shrink-0" />
							<span class="truncate">Schedule</span>
						</div>
						<span
							class="shrink-0 rounded-full bg-blue-50 px-1.5 py-0.5 text-[10px] font-bold text-blue-600"
							>24</span
						>
					</a>

					<!-- APPS SECTION -->
					<div class="px-3 pt-4 pb-1">
						<span class="text-[9px] font-extrabold tracking-wider text-slate-400 uppercase"
							>APPS</span
						>
					</div>

					{#each categories as cat (cat.id)}
						{@const CategoryIcon = cat.icon}
						<!-- Expanded Sidebar Category Folder -->
						<div class="flex flex-col rounded-xl border border-slate-100 bg-slate-50/20 p-1">
							<!-- Category Header Toggle Button -->
							<button
								type="button"
								onclick={() => (openFolders[cat.id] = !openFolders[cat.id])}
								class="flex w-full items-center justify-between rounded-lg px-2.5 py-2 text-left transition-all hover:bg-slate-50"
							>
								<div class="flex items-center gap-3">
									<div
										class="shrink-0 transition-colors {cat.items.some((i) => i.active)
											? 'text-blue-600'
											: 'text-slate-400'}"
									>
										<CategoryIcon size={20} stroke={1.8} />
									</div>
									<span
										class="text-xs font-extrabold tracking-tight transition-colors {cat.items.some(
											(i) => i.active
										)
											? 'text-blue-600'
											: 'text-slate-700'}">{cat.label}</span
									>
								</div>
								<IconChevronRight
									size={14}
									stroke={2.5}
									class="text-slate-400 transition-transform duration-200 {openFolders[cat.id]
										? 'rotate-90'
										: ''}"
								/>
							</button>

							<!-- Collapsible Submenus List -->
							{#if openFolders[cat.id]}
								<div class="mt-0.5 flex flex-col gap-0.5 pr-1 pb-1.5 pl-8">
									{#each cat.items as item (item.label)}
										<a
											href={item.href}
											class="flex items-center rounded-lg px-2.5 py-1.5 text-xs font-semibold transition-all duration-150 {item.active
												? 'bg-blue-50/50 font-bold text-blue-600'
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

				<!-- Bottom User Profile Card -->
				<div class="relative shrink-0 border-t border-slate-100 bg-white p-3">
					<button
						type="button"
						onclick={() => (showAccountMenu = !showAccountMenu)}
						class="flex w-full items-center justify-between rounded-xl p-1.5 text-left transition-all hover:bg-slate-50"
					>
						<div class="flex min-w-0 items-center gap-2.5">
							<!-- Emerald Initials Circle -->
							<div
								class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-emerald-100 bg-emerald-50 text-sm font-bold text-emerald-600"
							>
								{data.user?.fullName?.[0]?.toUpperCase() || 'J'}{data.user?.fullName
									?.split(' ')?.[1]?.[0]
									?.toUpperCase() || 'D'}
							</div>
							<div class="flex min-w-0 flex-col">
								<span class="truncate text-xs leading-tight font-bold text-slate-700">
									{data.user?.fullName || 'Jamie Dao'}
								</span>
								<span class="mt-0.5 truncate text-[9px] font-semibold text-slate-400"
									>Box Office Manager</span
								>
							</div>
						</div>
						<IconChevronRight size={14} stroke={2.5} class="shrink-0 text-slate-400" />
					</button>

					<!-- Floating Account / User Dropdown Menu -->
					{#if showAccountMenu}
						<button
							type="button"
							class="fixed inset-0 z-40 cursor-default bg-transparent"
							onclick={() => (showAccountMenu = false)}
							aria-label="Close user menu"
						></button>
						<div
							class="absolute right-3 bottom-full left-3 z-50 mb-1 rounded-xl border border-slate-200 bg-white p-2 text-slate-800 shadow-xl"
						>
							<div class="mb-1 border-b border-slate-100 px-3 py-2 select-none">
								<p class="text-xs font-semibold text-slate-400">Signed in as</p>
								<p class="truncate text-xs font-bold text-slate-900">{data.user?.email}</p>
							</div>
							<a
								href="/discover"
								class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
							>
								<IconExternalLink size={16} stroke={2} class="text-slate-400" />
								Buyer Portal
							</a>
							<form method="POST" action="/logout" class="w-full">
								<button
									type="submit"
									class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-xs font-bold text-red-600 transition hover:bg-red-50 focus:outline-none"
								>
									<IconLogout size={16} stroke={2} />
									Sign Out
								</button>
							</form>
						</div>
					{/if}
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
						<span class="text-[10px] font-extrabold tracking-widest text-slate-400 uppercase">
							{breadcrumbParent}
						</span>
						<IconChevronRight size={12} stroke={2.5} class="text-slate-300" />
						<span class="text-sm font-extrabold tracking-tight text-slate-900">
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
								class="w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pr-12 pl-9 text-xs font-semibold text-slate-800 placeholder-slate-400 transition-all focus:border-blue-500 focus:bg-white focus:outline-none"
							/>
							<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
								<kbd
									class="rounded border border-slate-200/80 bg-white px-1.5 text-[9px] font-bold text-slate-400 shadow-sm"
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
