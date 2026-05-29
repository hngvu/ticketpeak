<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
</script>

<svelte:head>
	<title>Ticketpeak for Business — Event Ticketing Platform</title>
	<meta
		name="description"
		content="Sell tickets, manage seat charts, track real-time analytics, and receive fast event payouts."
	/>
</svelte:head>

<!-- Hero Section -->
<section
	class="relative flex flex-col items-center justify-center overflow-hidden bg-gradient-to-r from-slate-950 via-slate-900 to-indigo-950 px-6 py-24 text-center text-white md:py-32"
>
	<!-- Visual ambient styling -->
	<div
		class="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(99,102,241,0.1),transparent_50%)]"
	></div>
	<div class="absolute -top-40 -right-40 h-80 w-80 rounded-full bg-primary/10 blur-3xl"></div>

	<div class="relative z-10 max-w-3xl space-y-6">
		<h1 class="text-4xl leading-tight font-extrabold tracking-tight md:text-6xl">
			Sell tickets. <br />
			<span
				class="bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-primary text-transparent"
			>
				Grow your audience.
			</span>
		</h1>
		<p class="mx-auto max-w-xl text-lg leading-relaxed font-medium text-gray-300 md:text-xl">
			Everything organizers need to publish events, manage physical seating configurations, issue
			secure QR tickets, and analyze revenue dashboards.
		</p>

		<div class="flex flex-wrap justify-center gap-4 pt-6">
			<a
				href="/auth?tab=register&redirect=/b2b/dashboard"
				class="hover:bg-primary-hover rounded-full bg-primary px-8 py-3.5 text-sm font-bold text-white shadow-lg transition active:scale-[0.98]"
			>
				Get started free
			</a>
			<a
				href="/b2b/login"
				class="flex items-center gap-2 rounded-full border border-slate-700 bg-slate-800 px-8 py-3.5 text-sm font-bold text-white transition hover:bg-slate-700 active:scale-[0.98]"
			>
				<span>Sign in</span>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					fill="none"
					viewBox="0 0 24 24"
					stroke-width="2.5"
					stroke="currentColor"
					class="h-4 w-4 text-primary"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"
					/>
				</svg>
			</a>
		</div>
	</div>
</section>

<!-- Features Grid Section -->
<section class="mx-auto max-w-6xl px-6 py-20">
	<div class="mb-16 space-y-4 text-center">
		<h2 class="text-3xl font-bold tracking-tight text-slate-900">
			Enterprise Features for Modern Organizers
		</h2>
		<p class="mx-auto max-w-lg font-medium text-slate-600">
			Built for high performance, reliability, and security in the Vietnamese ticketing landscape.
		</p>
	</div>

	<div class="grid grid-cols-1 gap-8 md:grid-cols-3">
		<!-- Feature 1: Venue Builder -->
		<div
			class="flex flex-col justify-between space-y-4 rounded-2xl border border-hairline bg-white p-8 shadow-xs transition duration-200 hover:shadow-md"
		>
			<div class="space-y-4">
				<div
					class="flex h-12 w-12 items-center justify-center rounded-xl bg-indigo-50 text-primary"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="2"
						stroke="currentColor"
						class="h-6 w-6"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M2.25 7.125C2.25 6.504 2.754 6 3.375 6h6c.621 0 1.125.504 1.125 1.125v.75c0 .621-.504 1.125-1.125 1.125h-6a1.125 1.125 0 01-1.125-1.125v-.75zM14.25 8.625c0-.621.504-1.125 1.125-1.125h5.25c.621 0 1.125.504 1.125 1.125v.75c0 .621-.504 1.125-1.125 1.125h-5.25a1.125 1.125 0 01-1.125-1.125v-.75zM3.75 16.125c0-.621.504-1.125 1.125-1.125h5.25c.621 0 1.125.504 1.125 1.125v.75c0 .621-.504 1.125-1.125 1.125h-5.25a1.125 1.125 0 01-1.125-1.125v-.75zM15.75 14.625c0-.621.504-1.125 1.125-1.125h3c.621 0 1.125.504 1.125 1.125v.75c0 .621-.504 1.125-1.125 1.125h-3a1.125 1.125 0 01-1.125-1.125v-.75z"
						/>
					</svg>
				</div>
				<h3 class="text-xl font-bold text-slate-900">Venue & Seat Builder</h3>
				<p class="text-sm leading-relaxed text-slate-600">
					Design interactive manifests and assign custom seat layouts, pricing sections, capacity
					rows, and specific price levels dynamically.
				</p>
			</div>
			<div class="flex cursor-pointer items-center gap-1.5 pt-2 text-xs font-bold text-primary">
				<span>Learn more</span>
				<span>→</span>
			</div>
		</div>

		<!-- Feature 2: Analytics -->
		<div
			class="flex flex-col justify-between space-y-4 rounded-2xl border border-hairline bg-white p-8 shadow-xs transition duration-200 hover:shadow-md"
		>
			<div class="space-y-4">
				<div
					class="flex h-12 w-12 items-center justify-center rounded-xl bg-purple-50 text-purple-600"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="2"
						stroke="currentColor"
						class="h-6 w-6"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M10.5 6a7.5 7.5 0 107.5 7.5h-7.5V6z"
						/>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M13.5 10.5H21A7.5 7.5 0 0013.5 3v7.5z"
						/>
					</svg>
				</div>
				<h3 class="text-xl font-bold text-slate-900">Real-Time Analytics</h3>
				<p class="text-sm leading-relaxed text-slate-600">
					Monitor active ticketing sales windows, inventory charts, checkout conversion ratios,
					marketing promotions, and user demographic groups.
				</p>
			</div>
			<div class="flex cursor-pointer items-center gap-1.5 pt-2 text-xs font-bold text-purple-600">
				<span>Learn more</span>
				<span>→</span>
			</div>
		</div>

		<!-- Feature 3: Payouts -->
		<div
			class="flex flex-col justify-between space-y-4 rounded-2xl border border-hairline bg-white p-8 shadow-xs transition duration-200 hover:shadow-md"
		>
			<div class="space-y-4">
				<div
					class="flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="2"
						stroke="currentColor"
						class="h-6 w-6"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M2.25 18.75a60.07 60.07 0 0115.797 2.101c.727.198 1.453-.342 1.453-1.096V18.75M3.75 4.5h16.5a1.5 1.5 0 011.5 1.5v11.25a1.5 1.5 0 01-1.5 1.5H3.75a1.5 1.5 0 01-1.5-1.5V6a1.5 1.5 0 01.5-1.5zm1.5 3h16.5M18 10.5h.008v.008H18V10.5zm-3 0h.008v.008H15V10.5z"
						/>
					</svg>
				</div>
				<h3 class="text-xl font-bold text-slate-900">Fast Automated Payouts</h3>
				<p class="text-sm leading-relaxed text-slate-600">
					Integrated secure payout workflow with multiple banks and gateways. Fast clearance and
					post-event payouts settlement matching local compliance.
				</p>
			</div>
			<div class="flex cursor-pointer items-center gap-1.5 pt-2 text-xs font-bold text-emerald-600">
				<span>Learn more</span>
				<span>→</span>
			</div>
		</div>
	</div>
</section>

<!-- Footer -->
<footer
	class="border-t border-slate-800 bg-slate-900 px-6 py-12 text-center text-xs text-slate-400"
>
	<p>© {new Date().getFullYear()} Ticketpeak for Business. All rights reserved.</p>
</footer>
