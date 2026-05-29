<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable svelte/no-at-html-tags */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import type { Snippet } from 'svelte';
	import '../../routes/layout.css';
	import { page } from '$app/state';

	let { data, children }: { data: any; children: Snippet } = $props();

	let showAccountMenu = $state(false);
	let isSidebarCollapsed = $state(false);

	const isLoginPageOrLanding = $derived(page.url.pathname === '/b2b/login');

	const sidebarItems = $derived([
		{
			label: 'Events',
			href: `/b2b/dashboard?organizationId=${data.selectedOrgId || ''}`,
			active: page.url.pathname.startsWith('/b2b/dashboard'),
			iconUrl: '/calendar.svg'
		},
		{
			label: 'Sales',
			href: `/b2b/sales?organizationId=${data.selectedOrgId || ''}`,
			active: page.url.pathname.startsWith('/b2b/sales'),
			iconUrl: '/trendingup.svg'
		},
		{
			label: 'Entry Gate',
			href: `/b2b/entry?organizationId=${data.selectedOrgId || ''}`,
			active:
				page.url.pathname.startsWith('/b2b/entry') || page.url.pathname.startsWith('/b2b/check-in'),
			iconUrl: '/scan.svg'
		},
		{
			label: 'Reports',
			href: `/b2b/reports?organizationId=${data.selectedOrgId || ''}`,
			active: page.url.pathname.startsWith('/b2b/reports'),
			iconUrl: '/document.svg'
		},
		{
			label: 'Marketing',
			href: `/b2b/marketing?organizationId=${data.selectedOrgId || ''}`,
			active: page.url.pathname.startsWith('/b2b/marketing'),
			iconUrl: '/megaphone.svg'
		}
	]);

	function handleOrgChange(e: Event) {
		const target = e.target as HTMLSelectElement;
		const selectedOrgId = target.value;
		window.location.href = `?organizationId=${selectedOrgId}`;
	}
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
		<div class="flex h-screen flex-col overflow-hidden bg-canvas-soft">
			<!-- B2B Top Header -->
			<header
				class="z-40 flex h-16 shrink-0 items-center justify-between border-b border-slate-800 bg-slate-900 px-6 text-white"
			>
				<!-- Left: Logo -->
				<div class="flex items-center gap-3">
					<a href="/b2b/dashboard" class="flex items-center gap-2">
						<img src="/logo.png" alt="Ticketpeak Logo" class="h-10 brightness-0 invert" />
						<span
							class="rounded-full bg-primary/20 px-2.5 py-0.5 text-[10px] font-bold tracking-wider text-primary uppercase"
						>
							for Business
						</span>
					</a>
				</div>

				<!-- Right: Noti, Organization Switcher, Account -->
				<div class="flex items-center gap-6">
					<!-- Notifications Button -->
					<button
						type="button"
						class="relative rounded-full p-1.5 text-slate-300 transition hover:bg-slate-800 hover:text-white focus:outline-none"
						aria-label="Notifications"
					>
						<span
							class="block h-5 w-5 bg-current"
							style="mask: url(/bell.svg) no-repeat center / contain; -webkit-mask: url(/bell.svg) no-repeat center / contain;"
							aria-hidden="true"
						></span>
						<span class="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-primary"></span>
					</button>

					<!-- Organization / Workspace Switcher -->
					{#if data.organizations && data.organizations.length > 0}
						<div class="flex items-center gap-2 border-l border-slate-800 pl-4">
							<select
								id="org-select"
								value={data.selectedOrgId}
								onchange={handleOrgChange}
								class="cursor-pointer rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-xs font-semibold text-white shadow-sm focus:border-primary focus:outline-none"
							>
								{#each data.organizations as org (org.id)}
									<option value={org.id} class="text-slate-900">{org.name}</option>
								{/each}
							</select>
						</div>
					{/if}

					<!-- Account / User Menu -->
					<div class="relative flex items-center gap-2 border-l border-slate-800 pl-4">
						<button
							type="button"
							onclick={() => (showAccountMenu = !showAccountMenu)}
							class="flex h-8 w-8 items-center justify-center rounded-full bg-primary/20 text-sm font-bold text-primary transition-all duration-200 hover:opacity-85 focus:outline-none"
							aria-label="User Account Menu"
						>
							<span
								class="h-4.5 w-4.5 bg-current"
								style="mask: url(/user.svg) no-repeat center / contain; -webkit-mask: url(/user.svg) no-repeat center / contain;"
								aria-hidden="true"
							></span>
						</button>

						<!-- Account Dropdown Menu -->
						{#if showAccountMenu}
							<button
								type="button"
								class="fixed inset-0 z-40 cursor-default bg-transparent"
								onclick={() => (showAccountMenu = false)}
								aria-label="Close user menu"
							></button>
							<div
								class="absolute top-full right-0 z-50 mt-2 w-48 rounded-xl border border-hairline bg-canvas p-2 text-slate-800 shadow-xl"
							>
								<div class="mb-1 border-b border-hairline px-3 py-2 select-none">
									<p class="text-xs font-semibold text-mute">Signed in as</p>
									<p class="truncate text-xs font-bold text-slate-900">{data.user?.email}</p>
								</div>
								<a
									href="/discover"
									class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-xs font-semibold text-slate-700 transition hover:bg-blue-accent-soft"
								>
									<svg
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2"
										class="h-4 w-4"
									>
										<rect width="20" height="14" x="2" y="5" rx="2" /><line
											x1="2"
											x2="22"
											y1="10"
											y2="10"
										/>
									</svg>
									Buyer Portal
								</a>
								<form method="POST" action="/logout" class="w-full">
									<button
										type="submit"
										class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-xs font-bold text-red-600 transition hover:bg-red-50 focus:outline-none"
									>
										<svg
											viewBox="0 0 24 24"
											fill="none"
											stroke="currentColor"
											stroke-width="2"
											class="h-4 w-4"
										>
											<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" />
										</svg>
										Sign Out
									</button>
								</form>
							</div>
						{/if}
					</div>
				</div>
			</header>

			<!-- Sub-header Main Work Area (Sidebar + Content) -->
			<div class="flex flex-1 overflow-hidden">
				<!-- Sidebar -->
				<aside
					class="flex shrink-0 flex-col justify-between border-r border-slate-200 bg-white transition-all duration-300 {isSidebarCollapsed
						? 'w-16'
						: 'w-64'}"
				>
					<!-- Sidebar Nav Items -->
					<div class="flex flex-col gap-1 p-3">
						{#each sidebarItems as item (item.label)}
							<a
								href={item.href}
								class="flex items-center gap-3.5 rounded-xl px-3 py-3 text-sm font-bold transition-all duration-200 {item.active
									? 'bg-primary/10 text-primary'
									: 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'}"
								title={isSidebarCollapsed ? item.label : ''}
							>
								<span
									class="h-5 w-5 shrink-0 bg-current"
									style="mask: url({item.iconUrl}) no-repeat center / contain; -webkit-mask: url({item.iconUrl}) no-repeat center / contain;"
									aria-hidden="true"
								></span>
								{#if !isSidebarCollapsed}
									<span class="truncate transition-opacity duration-200">{item.label}</span>
								{/if}
							</a>
						{/each}
					</div>

					<!-- Collapse Toggle at bottom -->
					<div class="border-t border-slate-100 p-3">
						<button
							type="button"
							onclick={() => (isSidebarCollapsed = !isSidebarCollapsed)}
							class="flex w-full items-center gap-3.5 rounded-xl px-3 py-2.5 text-xs font-bold text-slate-500 transition-all duration-200 hover:bg-slate-50 hover:text-slate-900 focus:outline-none"
							title={isSidebarCollapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
						>
							<span class="shrink-0">
								{#if isSidebarCollapsed}
									<!-- Right arrow -->
									<svg
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2.5"
										class="h-5 w-5"
									>
										<path d="m9 18 6-6-6-6" />
									</svg>
								{:else}
									<!-- Left arrow -->
									<svg
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2.5"
										class="h-5 w-5"
									>
										<path d="m15 18-6-6 6-6" />
									</svg>
								{/if}
							</span>
							{#if !isSidebarCollapsed}
								<span class="truncate">Collapse Sidebar</span>
							{/if}
						</button>
					</div>
				</aside>

				<!-- Content Area -->
				<main class="flex-1 overflow-y-auto bg-slate-50/50">
					{@render children()}
				</main>
			</div>
		</div>
	{/if}
</div>
