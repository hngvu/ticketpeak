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
		IconScan,
		IconCalendarEvent,
		IconDots,
		IconArrowLeft,
		IconHelpCircle
	} from '@tabler/icons-svelte';

	let { children }: { children: Snippet } = $props();

	let showAccountMenu = $state(false);

	let openFolders = $state<Record<string, boolean>>({
		moderation: true,
		partners: true,
		system: true
	});

	const categories = $derived([
		{
			id: 'moderation',
			label: 'MODERATION',
			icon: IconCalendarEvent,
			items: [
				{
					label: 'Event Moderation',
					href: '/ops/dashboard?tab=events',
					active: !page.url.searchParams.get('tab') || page.url.searchParams.get('tab') === 'events'
				}
			]
		},
		{
			id: 'partners',
			label: 'PARTNERS',
			icon: IconLayoutGrid,
			items: [
				{
					label: 'Organizations',
					href: '/ops/dashboard?tab=organizations',
					active: page.url.searchParams.get('tab') === 'organizations'
				}
			]
		},
		{
			id: 'system',
			label: 'SYSTEM',
			icon: IconScan,
			items: [
				{
					label: 'Platform Settings',
					href: '/ops/dashboard?tab=settings',
					active: page.url.searchParams.get('tab') === 'settings'
				},
				{
					label: 'Audit Logs',
					href: '/ops/dashboard?tab=logs',
					active: page.url.searchParams.get('tab') === 'logs'
				}
			]
		}
	]);

	const activeCategory = $derived(categories.find((cat) => cat.items.some((item) => item.active)));
	const activeItem = $derived(activeCategory?.items.find((item) => item.active));

	const breadcrumbParent = $derived(activeCategory?.label || 'MODERATION');
	const breadcrumbChild = $derived(activeItem?.label || 'Event Moderation');
</script>

<div class="flex min-h-screen flex-col bg-canvas-soft">
	<div class="flex h-screen w-screen flex-row overflow-hidden bg-slate-50/40 text-slate-800">
		<!-- Sidebar (Full Height) -->
		<aside
			class="flex h-full w-64 shrink-0 flex-col justify-between border-r border-slate-200 bg-white"
		>
			<!-- Top Branding Block -->
			<div class="flex shrink-0 items-center justify-between border-b border-slate-100 p-4">
				<a href="/ops/dashboard" class="flex min-w-0 items-center gap-2">
					<!-- Amber OP Square Logo -->
					<div
						class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-amber-500 text-base font-bold tracking-tighter text-black shadow-sm select-none"
					>
						OP
					</div>
					<div class="flex min-w-0 flex-col">
						<span class="text-sm leading-none font-semibold tracking-tight text-slate-800"
							>Ticketpeak</span
						>
						<span class="mt-0.5 text-[9px] font-semibold tracking-wider text-amber-600 uppercase"
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
							class="flex w-full cursor-pointer items-center justify-between rounded-lg border-0 bg-transparent px-2.5 py-2 text-left transition-all hover:bg-slate-50"
						>
							<div class="flex items-center gap-2.5">
								<div
									class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full transition-all duration-200 {hasActiveItem
										? 'bg-amber-500 text-black shadow-xs'
										: 'text-slate-400'}"
								>
									<CategoryIcon size={hasActiveItem ? 14 : 18} stroke={hasActiveItem ? 2.2 : 1.8} />
								</div>
								<span
									class="text-[11px] font-bold tracking-wider uppercase transition-colors {hasActiveItem
										? 'text-slate-900'
										: 'text-slate-500'}"
								>
									{cat.label}
								</span>
							</div>
							<IconChevronDown
								size={13}
								stroke={2.5}
								class="text-slate-400 transition-transform duration-200 {openFolders[cat.id]
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
											? 'rounded-r-lg border-amber-500 bg-amber-50/50 text-amber-700'
											: 'border-transparent text-slate-500 hover:bg-slate-50 hover:text-slate-800'}"
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
							<!-- Amber Initials Rounded Square -->
							<div
								class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-amber-100 text-xs font-bold text-amber-700 uppercase"
							>
								AD
							</div>
							<div class="flex min-w-0 flex-col">
								<span class="truncate text-xs leading-none font-bold text-slate-800 uppercase">
									ADMIN
								</span>
								<span
									class="mt-1 truncate text-[9px] leading-none font-semibold text-slate-400 uppercase"
								>
									Platform Ops
								</span>
							</div>
						</div>
						<IconDots size={20} class="shrink-0 text-slate-400 hover:text-slate-700" />
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
							class="absolute right-0 bottom-full left-0 z-50 mb-1 rounded-xl border border-slate-200 bg-white p-2 text-slate-800 shadow-xl"
						>
							<div class="mb-1 border-b border-slate-100 px-3 py-2 select-none">
								<p class="text-xs font-medium text-slate-400">Signed in as</p>
								<p class="truncate text-xs font-semibold text-slate-900">admin@ticketpeak.com</p>
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
		<div class="flex h-full min-w-0 flex-1 flex-col overflow-hidden bg-slate-50/50">
			<!-- Ops Top Header (White Theme) -->
			<header
				class="z-40 flex h-16 shrink-0 items-center justify-between border-b border-slate-200 bg-white px-6 text-slate-800"
			>
				<!-- Left: Breadcrumb Path -->
				<div class="flex items-center gap-2">
					<span class="text-[10px] font-semibold tracking-widest text-slate-400 uppercase">
						{breadcrumbParent}
					</span>
					<IconChevronRight size={12} stroke={2.5} class="text-slate-300" />
					<span class="text-sm font-bold tracking-tight text-slate-900">
						{breadcrumbChild}
					</span>
				</div>

				<!-- Right: Notifications, Apps -->
				<div class="flex items-center gap-4">
					<div class="relative w-60 shrink-0">
						<span
							class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400"
						>
							<IconSearch size={16} stroke={2.2} />
						</span>
						<input
							type="text"
							placeholder="Search ops inventory..."
							class="w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pr-12 pl-9 text-xs font-medium text-slate-800 placeholder-slate-400 transition-all focus:border-blue-500 focus:bg-white focus:outline-none"
						/>
						<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
							<kbd
								class="rounded border border-slate-200/80 bg-white px-1.5 text-[9px] font-semibold text-slate-400 shadow-sm"
								>⌘K</kbd
							>
						</span>
					</div>

					<button
						type="button"
						class="relative rounded-lg p-2 text-slate-500 transition-all hover:bg-slate-50 hover:text-slate-800 focus:outline-none"
						aria-label="Notifications"
					>
						<IconBell size={20} stroke={2} />
						<span
							class="absolute top-2 right-2 h-1.5 w-1.5 rounded-full bg-amber-500 ring-2 ring-white"
						></span>
					</button>

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
			<main class="flex-1 overflow-y-auto">
				{@render children()}
			</main>
		</div>
	</div>
</div>
