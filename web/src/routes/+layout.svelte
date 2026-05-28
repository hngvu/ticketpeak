<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable svelte/require-each-key */
	import './layout.css';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';

	let { children, data } = $props();

	let searchQuery = $state('');
	let showCountryMenu = $state(false);
	let showDateMenu = $state(false);
	let showLocationMenu = $state(false);

	const countries = [
		{
			value: 'vn',
			label: 'Việt Nam',
			code: 'VN',
			flagCode: 'vn',
			locale: 'vi-VN',
			lang: 'vi',
			note: 'Ưu tiên thị trường trong nước'
		},
		{
			value: 'us',
			label: 'United States',
			code: 'US',
			flagCode: 'us',
			locale: 'en-US',
			lang: 'en',
			note: 'English-first discovery'
		},
		{
			value: 'au',
			label: 'Australia',
			code: 'AU',
			flagCode: 'au',
			locale: 'en-AU',
			lang: 'en',
			note: 'Southern hemisphere events'
		},
		{
			value: 'be',
			label: 'Belgium',
			code: 'BE',
			flagCode: 'be',
			locale: 'nl-BE',
			lang: 'en',
			note: 'European live experiences'
		},
		{
			value: 'br',
			label: 'Brasil',
			code: 'BR',
			flagCode: 'br',
			locale: 'pt-BR',
			lang: 'pt',
			note: 'Latin America coverage'
		},
		{
			value: 'ca',
			label: 'Canada',
			code: 'CA',
			flagCode: 'ca',
			locale: 'en-CA',
			lang: 'en',
			note: 'North American events'
		},
		{
			value: 'cz',
			label: 'Česká republika',
			code: 'CZ',
			flagCode: 'cz',
			locale: 'cs-CZ',
			lang: 'en',
			note: 'Central Europe'
		},
		{
			value: 'cl',
			label: 'Chile',
			code: 'CL',
			flagCode: 'cl',
			locale: 'es-CL',
			lang: 'es',
			note: 'Latin America coverage'
		},
		{
			value: 'cy',
			label: 'Cyprus',
			code: 'CY',
			flagCode: 'cy',
			locale: 'el-CY',
			lang: 'en',
			note: 'Mediterranean live events'
		},
		{
			value: 'dk',
			label: 'Danmark',
			code: 'DK',
			flagCode: 'dk',
			locale: 'da-DK',
			lang: 'en',
			note: 'Nordic live events'
		},
		{
			value: 'de',
			label: 'Deutschland',
			code: 'DE',
			flagCode: 'de',
			locale: 'de-DE',
			lang: 'de',
			note: 'German market'
		},
		{
			value: 'es',
			label: 'España',
			code: 'ES',
			flagCode: 'es',
			locale: 'es-ES',
			lang: 'es',
			note: 'Spanish market'
		},
		{
			value: 'fr',
			label: 'France',
			code: 'FR',
			flagCode: 'fr',
			locale: 'fr-FR',
			lang: 'fr',
			note: 'French market'
		},
		{
			value: 'ie',
			label: 'Ireland',
			code: 'IE',
			flagCode: 'ie',
			locale: 'en-IE',
			lang: 'en',
			note: 'Irish market'
		},
		{
			value: 'il',
			label: 'Israel',
			code: 'IL',
			flagCode: 'il',
			locale: 'he-IL',
			lang: 'en',
			note: 'Middle East events'
		},
		{
			value: 'it',
			label: 'Italia',
			code: 'IT',
			flagCode: 'it',
			locale: 'it-IT',
			lang: 'it',
			note: 'Italian market'
		},
		{
			value: 'mx',
			label: 'México',
			code: 'MX',
			flagCode: 'mx',
			locale: 'es-MX',
			lang: 'es',
			note: 'Latin America coverage'
		}
	] as const;

	const cities = [
		{ value: 'ho-chi-minh', label: 'Hồ Chí Minh' },
		{ value: 'ha-noi', label: 'Hà Nội' },
		{ value: 'da-nang', label: 'Đà Nẵng' },
		{ value: 'hai-phong', label: 'Hải Phòng' }
	] as const;

	const navItems = [
		{ href: '/discover/concerts', label: 'Concerts' },
		{ href: '/discover/sports', label: 'Sports' },
		{ href: '/discover/arts', label: 'Arts, Theater & Comedy' },
		{ href: '/discover/family', label: 'Family' },
		{ href: '/discover', label: 'Cities' }
	];

	const datePresets = [
		{ value: 'all', label: 'All Dates' },
		{ value: 'today', label: 'Today' },
		{ value: 'weekend', label: 'This Weekend' },
		{ value: 'month', label: 'This Month' }
	] as const;

	$effect(() => {
		searchQuery = page.url.searchParams.get('q') || '';
		document.documentElement.lang = data.preferredLang || 'vi';
	});

	const activeCountry = $derived(
		countries.find((country) => country.value === data.preferredCountry) || countries[0]
	);

	const activeCity = $derived(
		cities.find((city) => city.value === data.preferredCity) || cities[0]
	);

	const activeDatePreset = $derived(datePresets[0]);

	const countryFlagSrc = (flagCode: string) => `https://flagcdn.com/w40/${flagCode}.png`;

	function handleSearch(event: SubmitEvent) {
		event.preventDefault();
		const query = searchQuery.trim();
		goto(query ? `/search?q=${encodeURIComponent(query)}` : '/search');
	}

	function setCountry(country: (typeof countries)[number]) {
		document.cookie = `preferred_country=${country.value}; path=/; max-age=${60 * 60 * 24 * 365}`;
		document.cookie = `preferred_locale=${country.locale}; path=/; max-age=${60 * 60 * 24 * 365}`;
		document.cookie = `preferred_lang=${country.lang}; path=/; max-age=${60 * 60 * 24 * 365}`;
		showCountryMenu = false;
		window.location.reload();
	}

	function setCity(city: (typeof cities)[number]) {
		document.cookie = `preferred_city=${city.value}; path=/; max-age=${60 * 60 * 24 * 365}`;
		showLocationMenu = false;
		window.location.reload();
	}

	const isAuthOrPortal = $derived(
		page.url.pathname.startsWith('/auth') ||
			page.url.pathname.startsWith('/b2b') ||
			page.url.pathname.startsWith('/ops')
	);
</script>

<svelte:head>
	<link rel="icon" type="image/png" href="/favicon.png" />
	<title>Ticketpeak</title>
</svelte:head>

{#if isAuthOrPortal}
	{@render children()}
{:else}
	<div class="flex min-h-screen flex-col bg-canvas-soft text-body">
		<div class="border-b border-[#20262d] bg-ink text-white/95">
			<div
				class="mx-auto flex max-w-[1600px] items-center justify-between gap-3 px-4 py-2.5 md:px-6"
			>
				<div class="relative">
					<button
						type="button"
						onclick={() => (showCountryMenu = true)}
						class="inline-flex items-center gap-2 rounded-full px-2 py-1 text-sm font-semibold text-white/95 transition-colors hover:bg-white/10"
					>
						<img
							src={countryFlagSrc(activeCountry.flagCode)}
							alt={activeCountry.label}
							class="h-5 w-5 rounded-full border border-white/25 object-cover"
						/>
						<span>{activeCountry.code}</span>
					</button>
				</div>

				<div class="hidden items-center gap-4 text-xs font-semibold text-white/80 md:flex">
					<a href="/discover" class="transition-colors hover:text-white">Hotels</a>
					<a href="/discover" class="transition-colors hover:text-white">Sell</a>
					<a href="/discover" class="transition-colors hover:text-white">Gift Cards</a>
					<a href="/discover" class="transition-colors hover:text-white">Help</a>
					<a href="/discover" class="transition-colors hover:text-white">VIP</a>
					<img
						src="/vietqr.png"
						alt="VietQR"
						class="h-5 w-auto object-contain brightness-0 invert"
					/>
				</div>
			</div>
		</div>

		<header class="bg-primary text-white">
			<div class="mx-auto max-w-[1600px] px-4 pt-5 pb-8 md:px-6">
				<div class="flex items-center justify-between gap-4">
					<div class="flex items-center gap-8">
						<a href="/" class="flex items-center gap-3">
							<img
								src="/logo.png"
								alt="Ticketpeak"
								class="h-16 w-auto brightness-0 invert md:h-[4.75rem]"
							/>
						</a>

						<nav class="hidden items-center gap-7 lg:flex">
							{#each navItems as item}
								<a
									href={item.href}
									class="text-base font-bold text-white/95 transition-opacity hover:opacity-80"
								>
									{item.label}
								</a>
							{/each}
						</nav>
					</div>

					{#if data.user}
						<div class="flex items-center gap-3">
							{#if data.user.role === 'ADMIN'}
								<a href="/ops/dashboard" class="text-sm font-bold text-white hover:underline"
									>Admin</a
								>
							{:else if data.user.role === 'ORGANIZER'}
								<a href="/b2b/dashboard" class="text-sm font-bold text-white hover:underline"
									>Partner</a
								>
							{:else}
								<a href="/discover" class="text-sm font-bold text-white hover:underline">Discover</a
								>
							{/if}
							<form method="POST" action="/logout">
								<button
									type="submit"
									class="inline-flex cursor-pointer items-center gap-2 rounded-full border border-white/25 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-white/10"
								>
									Sign Out
								</button>
							</form>
						</div>
					{:else}
						<a
							href="/auth"
							class="inline-flex items-center gap-2 rounded-full border border-white/25 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-white/10"
						>
							<span class="hidden sm:inline">Sign In/Register</span>
							<span class="sm:hidden">Sign In</span>
						</a>
					{/if}
				</div>

				<form
					onsubmit={handleSearch}
					class="mx-auto mt-8 w-full max-w-[900px] overflow-hidden rounded-[4px] border border-white/35 bg-canvas shadow-[0_8px_22px_rgba(48,56,65,0.12)]"
				>
					<div class="grid grid-cols-1 md:grid-cols-[0.8fr_0.75fr_1.45fr_auto]">
						<div class="relative border-r border-b border-hairline md:border-b-0">
							<button
								type="button"
								onclick={() => (showLocationMenu = !showLocationMenu)}
								class="flex h-full w-full items-center gap-3 px-4 py-3 text-left text-[#111]"
							>
								<span class="text-primary">
									<svg
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2.2"
										class="h-5 w-5"
									>
										<path d="M12 21s7-5.5 7-12a7 7 0 1 0-14 0c0 6.5 7 12 7 12z" />
										<circle cx="12" cy="9" r="2.5" />
									</svg>
								</span>
								<span class="min-w-0">
									<span
										class="block text-[11px] font-bold tracking-[0.12em] text-black/45 uppercase"
									>
										Location
									</span>
									<span class="block truncate text-sm font-medium">{activeCity.label}</span>
								</span>
								<span class="ml-auto text-[#777]">
									<svg
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2"
										class="h-4 w-4"
									>
										<path d="m6 9 6 6 6-6" />
									</svg>
								</span>
							</button>

							{#if showLocationMenu}
								<div
									class="absolute top-full left-0 z-50 mt-2 w-full overflow-hidden rounded-xl border border-hairline bg-canvas shadow-xl"
								>
									{#each cities as city}
										<button
											type="button"
											onclick={() => setCity(city)}
											class="flex w-full items-center justify-between px-4 py-3 text-left text-sm hover:bg-blue-accent-soft"
										>
											<span>{city.label}</span>
											{#if data.preferredCity === city.value}
												<span class="text-xs font-semibold text-primary">Selected</span>
											{/if}
										</button>
									{/each}
								</div>
							{/if}
						</div>

						<div class="relative border-r border-b border-hairline md:border-b-0">
							<button
								type="button"
								onclick={() => (showDateMenu = !showDateMenu)}
								class="flex h-full w-full items-center gap-3 px-4 py-3 text-left text-[#111]"
							>
								<span class="text-primary">
									<svg
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2.2"
										class="h-5 w-5"
									>
										<rect x="3" y="4" width="18" height="18" rx="2" />
										<path d="M16 2v4" />
										<path d="M8 2v4" />
										<path d="M3 10h18" />
									</svg>
								</span>
								<span class="min-w-0">
									<span
										class="block text-[11px] font-bold tracking-[0.12em] text-black/45 uppercase"
									>
										Dates
									</span>
									<span class="block truncate text-sm font-medium">{activeDatePreset.label}</span>
								</span>
								<span class="ml-auto text-[#777]">
									<svg
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2"
										class="h-4 w-4"
									>
										<path d="m6 9 6 6 6-6" />
									</svg>
								</span>
							</button>

							{#if showDateMenu}
								<div
									class="absolute top-full left-0 z-50 mt-2 w-full overflow-hidden rounded-xl border border-hairline bg-canvas shadow-xl"
								>
									{#each datePresets as preset}
										<button
											type="button"
											class="flex w-full items-center justify-between px-4 py-3 text-left text-sm hover:bg-blue-accent-soft"
										>
											<span>{preset.label}</span>
											{#if preset.value === activeDatePreset.value}
												<span class="text-xs font-semibold text-primary">Selected</span>
											{/if}
										</button>
									{/each}
								</div>
							{/if}
						</div>

						<label class="flex items-center gap-3 px-4 py-3 text-[#111]">
							<span class="text-primary">
								<svg
									viewBox="0 0 24 24"
									fill="none"
									stroke="currentColor"
									stroke-width="2.2"
									class="h-5 w-5"
								>
									<circle cx="11" cy="11" r="7" />
									<path d="m20 20-3.5-3.5" />
								</svg>
							</span>
							<span class="min-w-0 flex-1">
								<span class="block text-[11px] font-bold tracking-[0.12em] text-black/45 uppercase">
									Search
								</span>
								<input
									id="home-search"
									type="text"
									placeholder="Artist, Event or Venue"
									bind:value={searchQuery}
									class="block w-full border-0 bg-transparent p-0 text-sm font-medium text-[#111] placeholder:text-black/35 focus:ring-0 focus:outline-none"
								/>
							</span>
						</label>

						<button
							type="submit"
							class="bg-primary px-6 py-4 text-sm font-bold text-white transition-colors hover:bg-link-deep"
						>
							Search
						</button>
					</div>
				</form>
			</div>
		</header>

		{#if showCountryMenu}
			<div class="fixed inset-0 z-50">
				<button
					type="button"
					class="absolute inset-0 bg-black/40"
					aria-label="Close country drawer"
					onclick={() => (showCountryMenu = false)}
				></button>

				<aside
					class="absolute top-0 left-0 h-full w-[min(92vw,470px)] overflow-hidden bg-canvas shadow-2xl"
				>
					<div class="flex h-20 items-center gap-4 border-b border-hairline px-5">
						<button
							type="button"
							class="inline-flex h-10 w-10 items-center justify-center rounded-full text-primary transition-colors hover:bg-blue-accent-soft"
							onclick={() => (showCountryMenu = false)}
							aria-label="Close"
						>
							<svg
								viewBox="0 0 24 24"
								fill="none"
								stroke="currentColor"
								stroke-width="2.5"
								class="h-5 w-5"
							>
								<path d="M15 18l-6-6 6-6" />
							</svg>
						</button>
						<div>
							<h2 class="text-2xl font-extrabold tracking-tight text-ink">Location</h2>
						</div>
					</div>

					<div class="h-[calc(100vh-5rem)] overflow-y-auto">
						<div class="border-b border-hairline px-5 py-5">
							<div class="flex items-center gap-3">
								<img
									src={countryFlagSrc(activeCountry.flagCode)}
									alt={activeCountry.label}
									class="h-8 w-8 rounded-full border border-hairline object-cover shadow-sm"
								/>
								<div>
									<p class="text-sm text-mute">Countries</p>
									<p class="text-lg font-semibold text-ink">{activeCountry.label}</p>
								</div>
								<button
									type="button"
									aria-label="Close location drawer"
									class="ml-auto text-primary transition-colors hover:text-link-deep"
									onclick={() => (showCountryMenu = false)}
								>
									<svg
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2.5"
										class="h-5 w-5"
									>
										<path d="m6 9 6 6 6-6" />
									</svg>
								</button>
							</div>
						</div>

						<div class="px-5 py-4">
							<div class="space-y-1">
								{#each countries as country}
									<button
										type="button"
										onclick={() => setCountry(country)}
										class="flex w-full items-center gap-4 rounded-2xl px-3 py-3 text-left transition-colors hover:bg-blue-accent-soft"
									>
										<img
											src={countryFlagSrc(country.flagCode)}
											alt={country.label}
											class="h-8 w-8 rounded-full border border-hairline object-cover shadow-sm"
										/>
										<div class="min-w-0 flex-1">
											<p class="text-base font-medium text-ink">{country.label}</p>
										</div>
										{#if activeCountry.value === country.value}
											<span
												class="rounded-full bg-blue-accent-soft px-3 py-1 text-xs font-semibold text-primary"
											>
												Selected
											</span>
										{/if}
									</button>
								{/each}
							</div>
						</div>
					</div>
				</aside>
			</div>
		{/if}

		<main class="flex-grow">
			{@render children()}
		</main>
	</div>
{/if}
