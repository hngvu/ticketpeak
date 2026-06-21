<script lang="ts">
	/* eslint-disable svelte/prefer-svelte-reactivity */
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable svelte/require-each-key */
	import './layout.css';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import { fly } from 'svelte/transition';

	let { children, data } = $props();

	let showAccountSidebar = $state(false);
	let myTicketsExpanded = $state(true);
	let myProfileExpanded = $state(false);
	let mySettingsExpanded = $state(false);

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

	const citiesByCountry: Record<string, { value: string; label: string }[]> = {
		vn: [
			{ value: 'ho-chi-minh', label: 'Hồ Chí Minh' },
			{ value: 'ha-noi', label: 'Hà Nội' },
			{ value: 'da-nang', label: 'Đà Nẵng' },
			{ value: 'hai-phong', label: 'Hải Phòng' }
		],
		us: [
			{ value: 'new-york', label: 'New York' },
			{ value: 'los-angeles', label: 'Los Angeles' },
			{ value: 'chicago', label: 'Chicago' },
			{ value: 'san-francisco', label: 'San Francisco' },
			{ value: 'las-vegas', label: 'Las Vegas' }
		],
		au: [
			{ value: 'sydney', label: 'Sydney' },
			{ value: 'melbourne', label: 'Melbourne' },
			{ value: 'brisbane', label: 'Brisbane' },
			{ value: 'perth', label: 'Perth' }
		],
		de: [
			{ value: 'berlin', label: 'Berlin' },
			{ value: 'munich', label: 'München' },
			{ value: 'frankfurt', label: 'Frankfurt' },
			{ value: 'hamburg', label: 'Hamburg' }
		],
		fr: [
			{ value: 'paris', label: 'Paris' },
			{ value: 'lyon', label: 'Lyon' },
			{ value: 'marseille', label: 'Marseille' },
			{ value: 'nice', label: 'Nice' }
		],
		ca: [
			{ value: 'toronto', label: 'Toronto' },
			{ value: 'vancouver', label: 'Vancouver' },
			{ value: 'montreal', label: 'Montreal' }
		]
	};

	const currentCities = $derived(
		citiesByCountry[data.preferredCountry || 'vn'] || citiesByCountry['vn']
	);

	const navItems = [
		{ href: '/discover/concerts', label: 'Concerts' },
		{ href: '/discover/sports', label: 'Sports' },
		{ href: '/discover/arts', label: 'Arts, Theater & Comedy' },
		{ href: '/discover/family', label: 'Family' },
		{ href: '/discover', label: 'Cities' }
	];

	let selectedLocationText = $state('');
	let layoutStartDate = $state('');
	let layoutEndDate = $state('');

	let calendarStart = $state<string | null>(null);
	let calendarEnd = $state<string | null>(null);
	const todayDate = new Date(2026, 4, 29); // Today is fixed as May 29, 2026
	let currentViewMonth = $state(todayDate.getMonth());
	let currentViewYear = $state(todayDate.getFullYear());

	const monthNames = [
		'January',
		'February',
		'March',
		'April',
		'May',
		'June',
		'July',
		'August',
		'September',
		'October',
		'November',
		'December'
	];
	const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

	const leftMonth = $derived(currentViewMonth);
	const leftYear = $derived(currentViewYear);
	const leftTitle = $derived(`${monthNames[leftMonth]} ${leftYear}`);

	const rightMonth = $derived((currentViewMonth + 1) % 12);
	const rightYear = $derived(currentViewMonth === 11 ? currentViewYear + 1 : currentViewYear);
	const rightTitle = $derived(`${monthNames[rightMonth]} ${rightYear}`);

	function getMonthDays(year: number, month: number) {
		const firstDay = new Date(year, month, 1);
		const startDayOfWeek = firstDay.getDay(); // 0 is Sunday
		const totalDays = new Date(year, month + 1, 0).getDate();

		const days = [];
		for (let i = 0; i < startDayOfWeek; i++) {
			days.push(null);
		}
		for (let d = 1; d <= totalDays; d++) {
			days.push(d);
		}
		return days;
	}

	function prevMonths() {
		if (currentViewMonth === 0) {
			currentViewMonth = 11;
			currentViewYear -= 1;
		} else {
			currentViewMonth -= 1;
		}
	}

	function nextMonths() {
		if (currentViewMonth === 11) {
			currentViewMonth = 0;
			currentViewYear += 1;
		} else {
			currentViewMonth += 1;
		}
	}

	function handleDayClick(year: number, month: number, day: number) {
		const pad = (num: number) => String(num).padStart(2, '0');
		const clickedDateStr = `${year}-${pad(month + 1)}-${pad(day)}`;

		if (!calendarStart || (calendarStart && calendarEnd)) {
			calendarStart = clickedDateStr;
			calendarEnd = null;
		} else {
			if (clickedDateStr < calendarStart) {
				calendarStart = clickedDateStr;
				calendarEnd = null;
			} else {
				calendarEnd = clickedDateStr;
			}
		}
	}

	function formatToUSDate(dateStr: string | null) {
		if (!dateStr) return '';
		const parts = dateStr.split('-');
		if (parts.length !== 3) return '';
		const [year, month, day] = parts;
		return `${month}/${day}/${year}`;
	}

	function getDayClass(year: number, month: number, day: number) {
		if (!day) return '';
		const pad = (num: number) => String(num).padStart(2, '0');
		const dateStr = `${year}-${pad(month + 1)}-${pad(day)}`;

		let cls =
			'relative h-9 w-9 flex items-center justify-center text-sm font-semibold transition-all ';

		const isStart = dateStr === calendarStart;
		const isEnd = dateStr === calendarEnd;
		const inRange =
			calendarStart && calendarEnd && dateStr > calendarStart && dateStr < calendarEnd;

		if (year === 2026 && month === 4 && day === 29) {
			cls += 'underline decoration-2 underline-offset-4 font-bold ';
		}

		if (isStart) {
			cls += 'bg-primary text-white rounded-full font-bold z-10 ';
		} else if (isEnd) {
			cls += 'bg-primary text-white rounded-full font-bold z-10 ';
		} else if (inRange) {
			cls += 'bg-blue-50 text-primary rounded-none ';
		} else {
			cls += 'text-slate-800 hover:bg-slate-100 rounded-full ';
		}

		return cls;
	}

	$effect(() => {
		searchQuery = page.url.searchParams.get('q') || '';
		selectedLocationText = page.url.searchParams.get('location') || '';
		layoutStartDate = page.url.searchParams.get('startDate') || '';
		layoutEndDate = page.url.searchParams.get('endDate') || '';
		document.documentElement.lang = data.preferredLang || 'vi';
	});

	const activeCountry = $derived(
		countries.find((country) => country.value === data.preferredCountry) || countries[0]
	);

	function formatFriendlyDate(iso: string) {
		const d = new Date(iso + 'T00:00:00');
		return { month: monthNames[d.getMonth()], day: d.getDate(), year: d.getFullYear() };
	}

	const activeDateRangeLabel = $derived(
		layoutStartDate && layoutEndDate
			? (() => {
					const s = formatFriendlyDate(layoutStartDate);
					const e = formatFriendlyDate(layoutEndDate);
					if (s.month === e.month && s.year === e.year) return `${s.month} ${s.day} - ${e.day}, ${s.year}`;
					if (s.year === e.year) return `${s.month} ${s.day} - ${e.month} ${e.day}, ${s.year}`;
					return `${s.month} ${s.day}, ${s.year} - ${e.month} ${e.day}, ${e.year}`;
			  })()
			: layoutStartDate
				? (() => {
						const d = formatFriendlyDate(layoutStartDate);
						return `From ${d.month} ${d.day}, ${d.year}`;
				  })()
				: layoutEndDate
					? (() => {
							const d = formatFriendlyDate(layoutEndDate);
							return `Until ${d.month} ${d.day}, ${d.year}`;
					  })()
					: 'All Dates'
	);

	const countryFlagSrc = (flagCode: string) => `https://flagcdn.com/w40/${flagCode}.png`;

	function handleSearch(event: SubmitEvent) {
		event.preventDefault();
		const query = searchQuery.trim();
		const location = selectedLocationText.trim();
		const params = new URLSearchParams();

		if (query) {
			params.set('q', query);
		}

		if (location) {
			params.set('location', location);
		}

		if (layoutStartDate) {
			params.set('startDate', layoutStartDate);
		}
		if (layoutEndDate) {
			params.set('endDate', layoutEndDate);
		}

		const queryString = params.toString();
		goto(queryString ? `/search?${queryString}` : '/search');
	}

	function setCountry(country: (typeof countries)[number]) {
		const defaultCities: Record<string, string> = {
			vn: 'ho-chi-minh',
			us: 'new-york',
			au: 'sydney',
			de: 'berlin',
			fr: 'paris',
			ca: 'toronto'
		};
		const defaultCity = defaultCities[country.value] || 'ho-chi-minh';
		document.cookie = `preferred_city=${defaultCity}; path=/; max-age=${60 * 60 * 24 * 365}`;
		document.cookie = `preferred_country=${country.value}; path=/; max-age=${60 * 60 * 24 * 365}`;
		document.cookie = `preferred_locale=${country.locale}; path=/; max-age=${60 * 60 * 24 * 365}`;
		document.cookie = `preferred_lang=${country.lang}; path=/; max-age=${60 * 60 * 24 * 365}`;
		showCountryMenu = false;
		window.location.reload();
	}

	const isAuthOrPortal = $derived(
		page.url.pathname.startsWith('/auth') ||
			page.url.pathname.startsWith('/b2b') ||
			page.url.pathname.startsWith('/ops')
	);

	const isEventDetail = $derived(
		/\/[^/]+\/event\/[^/]+/.test(page.url.pathname)
	);
</script>

<svelte:head>
	<link rel="icon" type="image/png" href="/favicon.png" />
	<title>Ticketpeak</title>
</svelte:head>

{#if isAuthOrPortal || isEventDetail}
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
					<a href="/business" class="transition-colors hover:text-white">For Organizers</a>
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
							{#if data.user.roles.includes('ADMIN')}
								<a
									href="/ops/dashboard"
									class="hidden text-xs font-bold tracking-wider text-white/80 uppercase transition-colors hover:text-white sm:inline"
									>Admin Panel</a
								>
							{:else if data.user.roles.includes('ORGANIZER')}
								<a
									href="/b2b/dashboard"
									class="hidden text-xs font-bold tracking-wider text-white/80 uppercase transition-colors hover:text-white sm:inline"
									>Partner Portal</a
								>
							{/if}
							<button
								type="button"
								onclick={() => (showAccountSidebar = true)}
								class="inline-flex cursor-pointer items-center gap-2 rounded-full border border-white/25 px-4.5 py-2 text-sm font-semibold text-white transition-colors hover:bg-white/10 active:scale-[0.98]"
							>
								<!-- User SVG Icon -->
								<svg
									xmlns="http://www.w3.org/2000/svg"
									fill="none"
									viewBox="0 0 24 24"
									stroke-width="2.2"
									stroke="currentColor"
									class="h-4.5 w-4.5"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z"
									/>
								</svg>
								<span>My Account</span>
							</button>
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
					class="mx-auto mt-8 w-full max-w-[900px] rounded-[4px] border border-white/35 bg-canvas shadow-[0_8px_22px_rgba(48,56,65,0.12)]"
				>
					<div class="grid grid-cols-1 md:grid-cols-[0.8fr_0.75fr_1.45fr_auto]">
						<div
							class="relative flex items-center gap-3 border-r border-b border-hairline px-4 py-3 text-[#111] md:border-b-0"
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
							<span class="min-w-0 flex-1">
								<span class="block text-[11px] font-bold tracking-[0.12em] text-black/45 uppercase">
									Location
								</span>
								<input
									id="location-input"
									type="text"
									placeholder="City or State"
									bind:value={selectedLocationText}
									onfocus={() => (showLocationMenu = true)}
									onblur={() => setTimeout(() => (showLocationMenu = false), 200)}
									class="block w-full border-0 bg-transparent p-0 text-sm font-medium text-[#111] placeholder:text-black/35 focus:ring-0 focus:outline-none"
								/>
							</span>
							<button
								type="button"
								onclick={() => (showLocationMenu = !showLocationMenu)}
								class="ml-auto text-[#777] hover:text-primary focus:outline-none"
								aria-label="Toggle location suggestions"
							>
								<svg
									viewBox="0 0 24 24"
									fill="none"
									stroke="currentColor"
									stroke-width="2"
									class="h-4 w-4"
								>
									<path d="m6 9 6 6 6-6" />
								</svg>
							</button>

							{#if showLocationMenu}
								<div
									class="absolute top-full left-0 z-50 mt-2 w-full overflow-hidden rounded-xl border border-hairline bg-canvas text-slate-800 shadow-xl"
								>
									{#each currentCities as city}
										<button
											type="button"
											onclick={() => {
												selectedLocationText = city.label;
												showLocationMenu = false;
											}}
											class="hover:bg-blue-accent-soft flex w-full items-center justify-between px-4 py-3 text-left text-sm text-slate-800 hover:text-slate-900"
										>
											<span>{city.label}</span>
											{#if selectedLocationText.toLowerCase() === city.label.toLowerCase()}
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
								onclick={() => {
									showDateMenu = !showDateMenu;
									if (showDateMenu) {
										calendarStart = layoutStartDate || null;
										calendarEnd = layoutEndDate || null;
									}
								}}
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
									<span class="block truncate text-sm font-medium">{activeDateRangeLabel}</span>
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
									class="absolute top-full left-0 z-50 mt-2 w-[320px] rounded-xl border border-hairline bg-canvas p-5 text-slate-800 shadow-2xl md:-left-[100px] md:w-[640px]"
								>
									<!-- Inputs row -->
									<div class="mb-4 flex gap-4 select-none">
										<div class="flex-1">
											<span class="mb-1 block text-xs font-bold text-slate-500">Start date</span>
											<input
												type="text"
												placeholder="MM/DD/YYYY"
												value={formatToUSDate(calendarStart)}
												readonly
												class="w-full rounded border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-primary focus:outline-none"
											/>
										</div>
										<div class="flex-1">
											<span class="mb-1 block text-xs font-bold text-slate-500">End date</span>
											<input
												type="text"
												placeholder="MM/DD/YYYY"
												value={formatToUSDate(calendarEnd)}
												readonly
												class="w-full rounded border border-slate-200 bg-white p-2 text-sm text-slate-800 focus:border-primary focus:outline-none"
											/>
										</div>
									</div>

									<!-- Calendars Section -->
									<div class="flex flex-col gap-6 select-none md:flex-row">
										<!-- Left Month Calendar -->
										<div class="w-[280px] flex-shrink-0">
											<div class="mb-3 flex items-center justify-between px-1">
												<button
													type="button"
													onclick={prevMonths}
													class="rounded-full p-1.5 text-slate-600 transition-colors hover:bg-slate-100 focus:outline-none"
													aria-label="Previous month"
												>
													<svg
														viewBox="0 0 24 24"
														fill="none"
														stroke="currentColor"
														stroke-width="2.5"
														class="h-4 w-4"
													>
														<path d="M15 19l-7-7 7-7" />
													</svg>
												</button>
												<span class="text-sm font-bold text-slate-800">{leftTitle}</span>
												<div class="w-7 md:hidden">
													<!-- On mobile, show next button on the left calendar instead of hiding -->
													<button
														type="button"
														onclick={nextMonths}
														class="rounded-full p-1.5 text-slate-600 transition-colors hover:bg-slate-100 focus:outline-none"
														aria-label="Next month"
													>
														<svg
															viewBox="0 0 24 24"
															fill="none"
															stroke="currentColor"
															stroke-width="2.5"
															class="h-4 w-4"
														>
															<path d="M9 5l7 7-7 7" />
														</svg>
													</button>
												</div>
												<div class="hidden w-7 md:block"></div>
											</div>

											<div class="mb-1 grid grid-cols-7 justify-items-center gap-y-1 text-center">
												{#each weekDays as day}
													<span class="w-9 text-xs font-bold text-slate-400">{day}</span>
												{/each}
											</div>

											<div class="grid grid-cols-7 justify-items-center gap-y-1">
												{#each getMonthDays(leftYear, leftMonth) as day}
													{#if day === null}
														<div class="h-9 w-9"></div>
													{:else}
														<button
															type="button"
															onclick={() => handleDayClick(leftYear, leftMonth, day)}
															class={getDayClass(leftYear, leftMonth, day)}
														>
															{day}
														</button>
													{/if}
												{/each}
											</div>
										</div>

										<!-- Right Month Calendar (Desktop only) -->
										<div class="hidden w-[280px] flex-shrink-0 md:block">
											<div class="mb-3 flex items-center justify-between px-1">
												<div class="w-7"></div>
												<span class="text-sm font-bold text-slate-800">{rightTitle}</span>
												<button
													type="button"
													onclick={nextMonths}
													class="rounded-full p-1.5 text-slate-600 transition-colors hover:bg-slate-100 focus:outline-none"
													aria-label="Next month"
												>
													<svg
														viewBox="0 0 24 24"
														fill="none"
														stroke="currentColor"
														stroke-width="2.5"
														class="h-4 w-4"
													>
														<path d="M9 5l7 7-7 7" />
													</svg>
												</button>
											</div>

											<div class="mb-1 grid grid-cols-7 justify-items-center gap-y-1 text-center">
												{#each weekDays as day}
													<span class="w-9 text-xs font-bold text-slate-400">{day}</span>
												{/each}
											</div>

											<div class="grid grid-cols-7 justify-items-center gap-y-1">
												{#each getMonthDays(rightYear, rightMonth) as day}
													{#if day === null}
														<div class="h-9 w-9"></div>
													{:else}
														<button
															type="button"
															onclick={() => handleDayClick(rightYear, rightMonth, day)}
															class={getDayClass(rightYear, rightMonth, day)}
														>
															{day}
														</button>
													{/if}
												{/each}
											</div>
										</div>
									</div>

									<!-- Action Buttons Row -->
									<div class="mt-6 flex items-center justify-between border-t border-hairline pt-4">
										<button
											type="button"
											onclick={() => {
												calendarStart = null;
												calendarEnd = null;
											}}
											class="text-sm font-bold text-primary transition-all hover:underline focus:outline-none"
										>
											Reset
										</button>
										<div class="flex gap-3">
											<button
												type="button"
												onclick={() => (showDateMenu = false)}
												class="rounded-lg border border-primary px-5 py-2 text-sm font-bold text-primary transition-colors hover:bg-blue-50 focus:outline-none"
											>
												Cancel
											</button>
											<button
												type="button"
												onclick={() => {
													layoutStartDate = calendarStart || '';
													layoutEndDate = calendarEnd || '';
													showDateMenu = false;
												}}
												class="hover:bg-link-deep rounded-lg bg-primary px-5 py-2 text-sm font-bold text-white transition-colors focus:outline-none"
											>
												Apply
											</button>
										</div>
									</div>
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
							class="md:rounded-b-0 hover:bg-link-deep rounded-b-[4px] bg-primary px-6 py-4 text-sm font-bold text-white transition-colors md:rounded-r-[4px]"
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
							class="hover:bg-blue-accent-soft inline-flex h-10 w-10 items-center justify-center rounded-full text-primary transition-colors"
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
									class="hover:text-link-deep ml-auto text-primary transition-colors"
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
										class="hover:bg-blue-accent-soft flex w-full items-center gap-4 rounded-2xl px-3 py-3 text-left transition-colors"
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
												class="bg-blue-accent-soft rounded-full px-3 py-1 text-xs font-semibold text-primary"
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

		{#if showAccountSidebar}
			<div class="fixed inset-0 z-50 flex justify-end">
				<!-- Backdrop -->
				<button
					type="button"
					class="absolute inset-0 cursor-default bg-black/45 backdrop-blur-[1px] transition-opacity"
					onclick={() => (showAccountSidebar = false)}
					aria-label="Close account sidebar"
				></button>

				<!-- Sidebar Drawer -->
				<aside
					class="relative z-10 flex h-full w-[min(92vw,400px)] flex-col overflow-hidden border-l border-hairline bg-white shadow-2xl"
					transition:fly={{ x: 400, duration: 250 }}
				>
					<!-- Header -->
					<div class="flex items-center justify-between border-b border-gray-100 px-6 py-5.5">
						<h2 class="font-sans text-xl font-bold tracking-tight text-ink">My Account</h2>
						<button
							type="button"
							class="inline-flex h-9 w-9 cursor-pointer items-center justify-center rounded-full text-mute transition-colors hover:bg-gray-100 hover:text-ink"
							onclick={() => (showAccountSidebar = false)}
							aria-label="Close"
						>
							<svg
								viewBox="0 0 24 24"
								fill="none"
								stroke="currentColor"
								stroke-width="2.2"
								class="h-5.5 w-5.5"
							>
								<path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
							</svg>
						</button>
					</div>

					<!-- Scrollable Menu -->
					<div class="flex-grow divide-y divide-gray-100 overflow-y-auto">
						<!-- Welcome back banner -->
						<div class="bg-white px-6 py-6.5 select-none">
							<span
								class="block font-mono text-xs font-semibold tracking-wider text-gray-400 uppercase"
								>Welcome back!</span
							>
							<h3 class="mt-1 font-sans text-3xl font-extrabold tracking-tight text-ink">
								{data.profile?.firstName || data.profile?.fullName?.split(' ')[0] || 'User'}
							</h3>
						</div>

						<!-- Links Menu -->
						<div class="flex flex-col py-2">
							<!-- 1. My Tickets -->
							<div>
								<button
									type="button"
									onclick={() => (myTicketsExpanded = !myTicketsExpanded)}
									class="group flex w-full cursor-pointer items-center justify-between px-6 py-4 text-left transition hover:bg-gray-50/50"
								>
									<div class="flex items-center gap-3.5">
										<!-- Ticket SVG -->
										<svg
											xmlns="http://www.w3.org/2000/svg"
											fill="none"
											viewBox="0 0 24 24"
											stroke-width="1.8"
											stroke="currentColor"
											class="h-5.5 w-5.5 text-gray-500 transition-colors group-hover:text-primary"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												d="M16.5 6v.75m0 3v.75m0 3v.75m0 3V18m-9-5.25h5.25M7.5 15h3M3.375 5.25c-.621 0-1.125.504-1.125 1.125v3.026a2.999 2.999 0 0 1 0 5.198v3.026c0 .621.504 1.125 1.125 1.125h17.25c.621 0 1.125-.504 1.125-1.125v-3.026a2.999 2.999 0 0 1 0-5.198V6.375c0-.621-.504-1.125-1.125-1.125H3.375Z"
											/>
										</svg>
										<span class="font-sans text-[15px] font-semibold text-slate-800"
											>My Tickets</span
										>
									</div>
									<div>
										{#if myTicketsExpanded}
											<svg
												xmlns="http://www.w3.org/2000/svg"
												fill="none"
												viewBox="0 0 24 24"
												stroke-width="2.5"
												stroke="currentColor"
												class="h-4 w-4 text-gray-400"
											>
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													d="M4.5 15.75l7.5-7.5 7.5 7.5"
												/>
											</svg>
										{:else}
											<svg
												xmlns="http://www.w3.org/2000/svg"
												fill="none"
												viewBox="0 0 24 24"
												stroke-width="2.5"
												stroke="currentColor"
												class="h-4 w-4 text-gray-400"
											>
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													d="M19.5 8.25l-7.5 7.5-7.5-7.5"
												/>
											</svg>
										{/if}
									</div>
								</button>

								{#if myTicketsExpanded}
									<div
										class="flex flex-col space-y-3 border-b border-gray-50/30 bg-slate-50/50 py-2 pr-6 pl-15 font-sans select-none"
									>
										<a
											href="/discover"
											onclick={() => (showAccountSidebar = false)}
											class="block text-[14px] font-medium text-slate-500 transition-colors hover:text-primary"
											>Upcoming Events</a
										>
										<a
											href="/discover"
											onclick={() => (showAccountSidebar = false)}
											class="block text-[14px] font-medium text-slate-500 transition-colors hover:text-primary"
											>Past Events</a
										>
										<a
											href="/discover"
											onclick={() => (showAccountSidebar = false)}
											class="block text-[14px] font-medium text-slate-500 transition-colors hover:text-primary"
											>My Listings</a
										>
									</div>
								{/if}
							</div>

							<!-- 2. My Profile -->
							<div>
								<button
									type="button"
									onclick={() => (myProfileExpanded = !myProfileExpanded)}
									class="group flex w-full cursor-pointer items-center justify-between px-6 py-4 text-left transition hover:bg-gray-50/50"
								>
									<div class="flex items-center gap-3.5">
										<!-- Profile ID SVG -->
										<svg
											xmlns="http://www.w3.org/2000/svg"
											fill="none"
											viewBox="0 0 24 24"
											stroke-width="1.8"
											stroke="currentColor"
											class="h-5.5 w-5.5 text-gray-500 transition-colors group-hover:text-primary"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												d="M15 9h3.75M15 12h3.75M15 15h3.75M4.5 19.5h15a2.25 2.25 0 0 0 2.25-2.25V6.75A2.25 2.25 0 0 0 19.5 4.5h-15a2.25 2.25 0 0 0-2.25 2.25v10.5A2.25 2.25 0 0 0 4.5 19.5Zm6-10.125a1.875 1.875 0 1 1-3.75 0 1.875 1.875 0 0 1 3.75 0Zm-1.2 6.4h-3.2A1.6 1.6 0 0 0 4.5 17.375v.125h7.5v-.125a1.6 1.6 0 0 0-1.6-1.6Z"
											/>
										</svg>
										<span class="font-sans text-[15px] font-semibold text-slate-800"
											>My Profile</span
										>
									</div>
									<div>
										{#if myProfileExpanded}
											<svg
												xmlns="http://www.w3.org/2000/svg"
												fill="none"
												viewBox="0 0 24 24"
												stroke-width="2.5"
												stroke="currentColor"
												class="h-4 w-4 text-gray-400"
											>
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													d="M4.5 15.75l7.5-7.5 7.5 7.5"
												/>
											</svg>
										{:else}
											<svg
												xmlns="http://www.w3.org/2000/svg"
												fill="none"
												viewBox="0 0 24 24"
												stroke-width="2.5"
												stroke="currentColor"
												class="h-4 w-4 text-gray-400"
											>
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													d="M19.5 8.25l-7.5 7.5-7.5-7.5"
												/>
											</svg>
										{/if}
									</div>
								</button>

								{#if myProfileExpanded}
									<div
										class="flex flex-col space-y-3 border-b border-gray-50/30 bg-slate-50/50 py-2 pr-6 pl-15 font-sans select-none"
									>
										<a
											href="/discover"
											onclick={() => (showAccountSidebar = false)}
											class="block text-[14px] font-medium text-slate-500 transition-colors hover:text-primary"
											>Personal Info</a
										>
										<a
											href="/discover"
											onclick={() => (showAccountSidebar = false)}
											class="block text-[14px] font-medium text-slate-500 transition-colors hover:text-primary"
											>Addresses</a
										>
									</div>
								{/if}
							</div>

							<!-- 3. My Settings -->
							<div>
								<button
									type="button"
									onclick={() => (mySettingsExpanded = !mySettingsExpanded)}
									class="group flex w-full cursor-pointer items-center justify-between px-6 py-4 text-left transition hover:bg-gray-50/50"
								>
									<div class="flex items-center gap-3.5">
										<!-- Settings Cog SVG -->
										<svg
											xmlns="http://www.w3.org/2000/svg"
											fill="none"
											viewBox="0 0 24 24"
											stroke-width="1.8"
											stroke="currentColor"
											class="h-5.5 w-5.5 text-gray-500 transition-colors group-hover:text-primary"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												d="M9.594 3.94c.09-.542.56-.94 1.11-.94h2.593c.55 0 1.02.398 1.11.94l.213 1.281c.063.374.313.686.645.87.074.04.147.083.22.127.324.196.72.257 1.075.124l1.217-.456a1.125 1.125 0 0 1 1.37.49l1.296 2.247a1.125 1.125 0 0 1-.26 1.43l-1.003.828c-.293.241-.438.613-.43.992a7.723 7.723 0 0 1 0 .255c-.008.378.137.75.43.991l1.004.827c.424.35.534.954.26 1.43l-1.298 2.247a1.125 1.125 0 0 1-1.369.491l-1.217-.456c-.355-.133-.75-.072-1.076.124a6.47 6.47 0 0 1-.22.128c-.331.183-.581.495-.644.869l-.213 1.281c-.09.543-.56.94-1.11.94h-2.594c-.55 0-1.019-.398-1.11-.94l-.213-1.281c-.062-.374-.312-.686-.644-.87a6.52 6.52 0 0 1-.22-.127c-.325-.196-.72-.257-1.076-.124l-1.217.456a1.125 1.125 0 0 1-1.369-.49l-1.297-2.247a1.125 1.125 0 0 1 .26-1.43l1.004-.827c.292-.24.437-.613.43-.991a6.936 6.936 0 0 1 0-.255c.007-.38-.138-.751-.43-.992l-1.004-.827a1.125 1.125 0 0 1-.26-1.43l1.297-2.247a1.125 1.125 0 0 1 1.37-.491l1.216.456c.356.133.751.072 1.076-.124.072-.044.146-.086.22-.128.332-.183.582-.495.644-.869l.214-1.28Z"
											/>
											<circle
												cx="12"
												cy="12"
												r="3"
												stroke-linecap="round"
												stroke-linejoin="round"
											/>
										</svg>
										<span class="font-sans text-[15px] font-semibold text-slate-800"
											>My Settings</span
										>
									</div>
									<div>
										{#if mySettingsExpanded}
											<svg
												xmlns="http://www.w3.org/2000/svg"
												fill="none"
												viewBox="0 0 24 24"
												stroke-width="2.5"
												stroke="currentColor"
												class="h-4 w-4 text-gray-400"
											>
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													d="M4.5 15.75l7.5-7.5 7.5 7.5"
												/>
											</svg>
										{:else}
											<svg
												xmlns="http://www.w3.org/2000/svg"
												fill="none"
												viewBox="0 0 24 24"
												stroke-width="2.5"
												stroke="currentColor"
												class="h-4 w-4 text-gray-400"
											>
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													d="M19.5 8.25l-7.5 7.5-7.5-7.5"
												/>
											</svg>
										{/if}
									</div>
								</button>

								{#if mySettingsExpanded}
									<div
										class="flex flex-col space-y-3 border-b border-gray-50/30 bg-slate-50/50 py-2 pr-6 pl-15 font-sans select-none"
									>
										<a
											href="/discover"
											onclick={() => (showAccountSidebar = false)}
											class="block text-[14px] font-medium text-slate-500 transition-colors hover:text-primary"
											>Preferences</a
										>
										<a
											href="/discover"
											onclick={() => (showAccountSidebar = false)}
											class="block text-[14px] font-medium text-slate-500 transition-colors hover:text-primary"
											>Notification Settings</a
										>
									</div>
								{/if}
							</div>

							<!-- 4. Sign Out -->
							<form
								method="POST"
								action="/logout"
								class="w-full"
								onsubmit={() => (showAccountSidebar = false)}
							>
								<button
									type="submit"
									class="group flex w-full cursor-pointer items-center gap-3.5 px-6 py-4 text-left transition hover:bg-gray-50/50"
								>
									<!-- Exit SVG -->
									<svg
										xmlns="http://www.w3.org/2000/svg"
										fill="none"
										viewBox="0 0 24 24"
										stroke-width="1.8"
										stroke="currentColor"
										class="h-5.5 w-5.5 text-gray-500 transition-colors group-hover:text-error"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											d="M15.75 9V5.25A2.25 2.25 0 0 0 13.5 3h-6a2.25 2.25 0 0 0-2.25 2.25v13.5A2.25 2.25 0 0 0 7.5 21h6a2.25 2.25 0 0 0 2.25-2.25V15M12 9l-3 3m0 0 3 3m-3-3h12.75"
										/>
									</svg>
									<span class="font-sans text-[15px] font-semibold text-slate-800">Sign Out</span>
								</button>
							</form>
						</div>

						<!-- 5. Need Help? -->
						<div class="flex flex-col py-2">
							<a
								href="/discover"
								onclick={() => (showAccountSidebar = false)}
								class="group flex items-center gap-3.5 px-6 py-4 text-left transition hover:bg-gray-50/50"
							>
								<!-- Help question box SVG -->
								<svg
									xmlns="http://www.w3.org/2000/svg"
									fill="none"
									viewBox="0 0 24 24"
									stroke-width="1.8"
									stroke="currentColor"
									class="h-5.5 w-5.5 text-gray-500 transition-colors group-hover:text-primary"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										d="M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 5.25h.008v.008H12v-.008Z"
									/>
								</svg>
								<span class="font-sans text-[15px] font-semibold text-slate-800">Need Help?</span>
							</a>
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
