<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import type { Snippet } from 'svelte';
	import '../../routes/layout.css';

	let { data, children }: { data: any; children: Snippet } = $props();
</script>

<div class="flex min-h-screen flex-col bg-canvas-soft">
	<!-- B2B Header -->
	<header
		class="sticky top-0 z-40 flex items-center justify-between border-b border-slate-800 bg-slate-900 px-6 py-4 text-white"
	>
		<div class="flex items-center gap-3">
			<a href="/b2b" class="flex items-center gap-2">
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

	<!-- Page Content Slot -->
	<main class="flex flex-1 flex-col">
		{@render children()}
	</main>
</div>
