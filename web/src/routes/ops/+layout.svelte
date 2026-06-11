<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve, @typescript-eslint/no-explicit-any */
	import type { Snippet } from 'svelte';
	import '../../routes/layout.css';
	import { page } from '$app/state';
	import { IconSearch, IconBell, IconLayoutGrid } from '@tabler/icons-svelte';
	import OpsSidebar from '$lib/components/ops/OpsSidebar.svelte';

	let { children }: { children: Snippet } = $props();

	const isLoginPage = $derived(page.url.pathname === '/ops/login');

	const pageTitle = $derived(
		page.url.pathname === '/ops/dashboard'
			? 'Dashboard'
			: page.url.pathname === '/ops/events'
				? 'Events'
				: page.url.pathname === '/ops/orders'
					? 'Orders'
					: page.url.pathname === '/ops/payouts'
						? 'Payouts'
						: page.url.pathname === '/ops/fees'
							? 'Fees'
							: page.url.pathname === '/ops/resale'
								? 'Resale'
								: page.url.pathname === '/ops/logs'
									? 'Audit Log'
									: page.url.pathname === '/ops/settings'
										? 'Settings'
										: page.url.pathname === '/ops/system'
											? 'Status'
											: page.url.pathname === '/ops/organizations'
												? 'Organizations'
												: page.url.pathname === '/ops/attractions'
													? 'Attractions'
													: page.url.pathname === '/ops/venues'
														? 'Venues'
														: page.url.pathname.startsWith('/ops/venues/')
															? 'Venue'
															: page.url.pathname === '/ops/users'
																? 'Users'
																: page.url.pathname === '/ops/roles'
																	? 'Roles'
																	: page.url.pathname === '/ops/classifications'
																		? 'Classifications'
																		: ''
	);
</script>

<div class="flex min-h-screen flex-col bg-canvas-soft">
	{#if isLoginPage}
		<main class="flex flex-1 flex-col">
			{@render children()}
		</main>
	{:else}
		<div class="flex h-screen w-screen flex-row overflow-hidden bg-canvas-soft text-ink">
			<OpsSidebar />

			<!-- Right Pane Workspace (Header + Content) -->
			<div class="flex h-full min-w-0 flex-1 flex-col overflow-hidden bg-canvas-soft">
				<!-- Ops Top Header -->
				<header
					class="z-40 flex h-12 shrink-0 items-center justify-between border-b border-hairline bg-canvas px-4 text-ink"
				>
					<!-- Left: Page Title -->
					<div class="flex items-center gap-2">
						<span class="text-sidebar-brand font-semibold tracking-tight text-ink">
							{pageTitle}
						</span>
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
								class="w-full rounded-md border border-hairline bg-canvas-soft py-1 pr-10 pl-8 font-mono text-sidebar-label font-medium text-ink placeholder-body transition-all focus:border-body focus:bg-canvas focus:outline-none"
							/>
							<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2.5">
								<kbd
									class="rounded border border-hairline bg-canvas px-1.5 font-mono text-sidebar-tiny font-semibold text-body shadow-xs"
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
