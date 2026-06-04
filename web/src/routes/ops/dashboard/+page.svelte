<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { enhance } from '$app/forms';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import ImageInput from '$lib/components/common/ImageInput.svelte';

	let { data, form } = $props<{ data: any; form: any }>();

	const events = $derived(data.events || []);
	const organizations = $derived(data.organizations || []);
	const classifications = $derived(data.classifications || []);

	// Search and filter states for original tables
	let classSearch = $state('');
	let classLevelFilter = $state('all'); // 'all', '1', '2'

	// Derived filtered lists for original tables
	const filteredClassifications = $derived(
		classifications.filter((cat: any) => {
			const matchesSearch =
				cat.name.toLowerCase().includes(classSearch.toLowerCase()) ||
				(cat.slug || '').toLowerCase().includes(classSearch.toLowerCase()) ||
				(cat.id || '').toLowerCase().includes(classSearch.toLowerCase());
			const matchesLevel = classLevelFilter === 'all' || cat.level?.toString() === classLevelFilter;
			return matchesSearch && matchesLevel;
		})
	);

	// Derive active tab from URL search parameters to keep in perfect sync with the layout sidebar
	const activeTab = $derived(
		(page.url.searchParams.get('tab') as
			| 'dashboard'
			| 'analytics'
			| 'events'
			| 'venues'
			| 'users'
			| 'roles'
			| 'orders'
			| 'payouts'
			| 'fees'
			| 'resale'
			| 'classifications'
			| 'cms'
			| 'promotions'
			| 'logs'
			| 'settings'
			| 'system') || 'dashboard'
	);

	// Mock databases for new tabs
	let usersList = $state([
		{
			id: 'usr-1',
			name: 'Nguyen Van A',
			email: 'vana@gmail.com',
			role: 'BUYER',
			status: 'ACTIVE',
			registeredAt: '2026-05-10T10:00:00Z'
		},
		{
			id: 'usr-2',
			name: 'Le Thi B',
			email: 'thib@gmail.com',
			role: 'BUYER',
			status: 'ACTIVE',
			registeredAt: '2026-05-12T14:30:00Z'
		},
		{
			id: 'usr-3',
			name: 'Tran Van C',
			email: 'vanc@gmail.com',
			role: 'ORGANIZER',
			status: 'ACTIVE',
			registeredAt: '2026-04-01T08:15:00Z'
		},
		{
			id: 'usr-4',
			name: 'Pham Minh D',
			email: 'minhd@gmail.com',
			role: 'ORGANIZER',
			status: 'ACTIVE',
			registeredAt: '2026-04-15T09:20:00Z'
		},
		{
			id: 'usr-5',
			name: 'Admin One',
			email: 'admin@ticketpeak.com',
			role: 'ADMIN',
			status: 'ACTIVE',
			registeredAt: '2026-01-01T00:00:00Z'
		},
		{
			id: 'usr-6',
			name: 'Scalper Boy',
			email: 'scalper@badactor.org',
			role: 'BUYER',
			status: 'BANNED',
			registeredAt: '2026-05-20T11:00:00Z'
		}
	]);

	let ordersList = $state([
		{
			id: 'ord-1001',
			customerName: 'Nguyen Van A',
			customerEmail: 'vana@gmail.com',
			eventTitle: 'Live Space Concert 2026',
			ticketCount: 2,
			totalAmount: 1200000,
			status: 'SUCCESS',
			date: '2026-06-02T15:20:00Z'
		},
		{
			id: 'ord-1002',
			customerName: 'Le Thi B',
			customerEmail: 'thib@gmail.com',
			eventTitle: 'Symphony of Lights',
			ticketCount: 1,
			totalAmount: 450000,
			status: 'SUCCESS',
			date: '2026-06-02T16:45:00Z'
		},
		{
			id: 'ord-1003',
			customerName: 'Hoang Long',
			customerEmail: 'longh@outlook.com',
			eventTitle: 'Rock Fest Hanoi',
			ticketCount: 4,
			totalAmount: 3200000,
			status: 'SUCCESS',
			date: '2026-06-01T09:10:00Z'
		},
		{
			id: 'ord-1004',
			customerName: 'Scalper Boy',
			customerEmail: 'scalper@badactor.org',
			eventTitle: 'Live Space Concert 2026',
			ticketCount: 5,
			totalAmount: 3000000,
			status: 'REFUNDED',
			date: '2026-05-28T22:30:00Z'
		}
	]);

	let payoutsList = $state([
		{
			id: 'pay-201',
			orgName: 'Hanoi Showbiz Ent.',
			eventTitle: 'Live Space Concert 2026',
			totalSales: 450000000,
			commissionRate: 5.0,
			netAmount: 427500000,
			status: 'PENDING',
			date: '2026-06-02T18:00:00Z'
		},
		{
			id: 'pay-202',
			orgName: 'V-Concert Group',
			eventTitle: 'Symphony of Lights',
			totalSales: 120000000,
			commissionRate: 5.0,
			netAmount: 114000000,
			status: 'APPROVED',
			date: '2026-06-01T12:00:00Z'
		},
		{
			id: 'pay-203',
			orgName: 'Hanoi Rock Club',
			eventTitle: 'Rock Fest Hanoi',
			totalSales: 80000000,
			commissionRate: 5.0,
			netAmount: 76000000,
			status: 'PAID',
			date: '2026-05-30T10:00:00Z'
		}
	]);

	let resaleListings = $state([
		{
			id: 'res-301',
			sellerName: 'Nguyen Van A',
			eventTitle: 'Live Space Concert 2026',
			originalPrice: 600000,
			resalePrice: 720000,
			markup: 20,
			status: 'ACTIVE'
		},
		{
			id: 'res-302',
			sellerName: 'Le Thi B',
			eventTitle: 'Symphony of Lights',
			originalPrice: 450000,
			resalePrice: 495000,
			markup: 10,
			status: 'ACTIVE'
		},
		{
			id: 'res-303',
			sellerName: 'Scalper Boy',
			eventTitle: 'Live Space Concert 2026',
			originalPrice: 600000,
			resalePrice: 1100000,
			markup: 83,
			status: 'SUSPICIOUS'
		},
		{
			id: 'res-304',
			sellerName: 'Tran Van C',
			eventTitle: 'Rock Fest Hanoi',
			originalPrice: 800000,
			resalePrice: 880000,
			markup: 10,
			status: 'ACTIVE'
		}
	]);

	let promotionsList = $state([
		{
			id: 'promo-401',
			code: 'EARLYBIRD10',
			type: 'PERCENTAGE',
			value: 10,
			usages: 154,
			status: 'ACTIVE'
		},
		{
			id: 'promo-402',
			code: 'WELCOME100K',
			type: 'FLAT',
			value: 100000,
			usages: 85,
			status: 'ACTIVE'
		},
		{
			id: 'promo-403',
			code: 'SUMMERFEST',
			type: 'PERCENTAGE',
			value: 15,
			usages: 0,
			status: 'INACTIVE'
		}
	]);

	let cmsBanners = $state([
		{
			id: 'banner-501',
			title: 'Summer Music Wave 2026',
			description: 'Experience the biggest music fest of the year live in Hanoi & HCM.',
			imageUrl:
				'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=600&q=80',
			status: 'ACTIVE',
			position: 1
		},
		{
			id: 'banner-502',
			title: 'Classical Symphony Nights',
			description: 'Book tickets to premium orchestral live performances.',
			imageUrl:
				'https://images.unsplash.com/photo-1465847899084-d164df4dedc6?auto=format&fit=crop&w=600&q=80',
			status: 'ACTIVE',
			position: 2
		},
		{
			id: 'banner-503',
			title: 'Early Access Presale Campaigns',
			description: 'Exclusive presale benefits for Ticketpeak cardholders.',
			imageUrl:
				'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=600&q=80',
			status: 'DRAFT',
			position: 3
		}
	]);

	// Search and filter states for new lists
	let userSearch = $state('');
	let userRoleFilter = $state('all');

	let orderSearch = $state('');
	let orderStatusFilter = $state('all');

	let payoutSearch = $state('');
	let payoutStatusFilter = $state('all');

	let resaleSearch = $state('');
	let resaleStatusFilter = $state('all');

	// Derived filtered lists for new databases
	const filteredUsers = $derived(
		usersList.filter((u) => {
			const matchesSearch =
				u.name.toLowerCase().includes(userSearch.toLowerCase()) ||
				u.email.toLowerCase().includes(userSearch.toLowerCase());
			const matchesRole = userRoleFilter === 'all' || u.role === userRoleFilter;
			return matchesSearch && matchesRole;
		})
	);

	const filteredOrders = $derived(
		ordersList.filter((o) => {
			const matchesSearch =
				o.customerName.toLowerCase().includes(orderSearch.toLowerCase()) ||
				o.customerEmail.toLowerCase().includes(orderSearch.toLowerCase()) ||
				o.eventTitle.toLowerCase().includes(orderSearch.toLowerCase()) ||
				o.id.toLowerCase().includes(orderSearch.toLowerCase());
			const matchesStatus = orderStatusFilter === 'all' || o.status === orderStatusFilter;
			return matchesSearch && matchesStatus;
		})
	);

	const filteredPayouts = $derived(
		payoutsList.filter((p) => {
			const matchesSearch =
				p.orgName.toLowerCase().includes(payoutSearch.toLowerCase()) ||
				p.eventTitle.toLowerCase().includes(payoutSearch.toLowerCase());
			const matchesStatus = payoutStatusFilter === 'all' || p.status === payoutStatusFilter;
			return matchesSearch && matchesStatus;
		})
	);

	const filteredResales = $derived(
		resaleListings.filter((r) => {
			const matchesSearch =
				r.sellerName.toLowerCase().includes(resaleSearch.toLowerCase()) ||
				r.eventTitle.toLowerCase().includes(resaleSearch.toLowerCase());
			const matchesStatus = resaleStatusFilter === 'all' || r.status === resaleStatusFilter;
			return matchesSearch && matchesStatus;
		})
	);

	// Local platform settings
	let platformCommission = $state(5);
	let antiScalpCap = $state(150);
	let maxTransferCount = $state(5);
	let settingsSaved = $state(false);

	// Platform fee tier configuration state
	let fixedProcessingFee = $state(2000);
	let buyerConvenienceFee = $state(1.5);
	let feeSettingsSaved = $state(false);

	// Modals toggling
	let selectedEventId = $state<string | null>(null);
	let postponeReason = $state('');
	let showPostponeModal = $state(false);

	let showAddClassificationModal = $state(false);
	let showAddPromoModal = $state(false);
	let showAddBannerModal = $state(false);

	// Classification form local binds
	let editingClassificationId = $state<string | null>(null);
	let className = $state('');
	let classSlug = $state('');
	let classParentId = $state('');

	function openEditClassification(cat: any) {
		editingClassificationId = cat.id;
		className = cat.name;
		classSlug = cat.slug;
		classParentId = cat.parentId || '';
		showAddClassificationModal = true;
	}

	// Promo form local binds
	let promoCode = $state('');
	let promoType = $state('PERCENTAGE');
	let promoVal = $state(10);

	// CMS banner form local binds
	let bannerTitle = $state('');
	let bannerDesc = $state('');
	let bannerImg = $state('');

	// Audit logs timeline
	let auditLogs = $state([
		{
			id: 1,
			time: '14:20:15',
			action: 'ADMIN_SIGN_IN',
			user: 'admin@ticketpeak.com',
			details: 'Authorized admin session started successfully.'
		},
		{
			id: 2,
			time: '13:05:40',
			action: 'UPDATE_SYSTEM_SETTINGS',
			user: 'admin@ticketpeak.com',
			details: 'Platform commission updated to 5.0%.'
		},
		{
			id: 3,
			time: '11:15:22',
			action: 'EVENT_PUBLISH',
			user: 'admin@ticketpeak.com',
			details: 'Event approved and published.'
		},
		{
			id: 4,
			time: '09:44:10',
			action: 'ORG_ACTIVATE',
			user: 'admin@ticketpeak.com',
			details: 'Organization active status verified.'
		}
	]);

	function addLog(action: string, details: string) {
		const now = new Date();
		const time = now.toTimeString().split(' ')[0];
		auditLogs = [
			{
				id: Date.now(),
				time,
				action,
				user: 'admin@ticketpeak.com',
				details
			},
			...auditLogs
		];
	}

	function saveSettings() {
		settingsSaved = true;
		addLog(
			'UPDATE_SETTINGS',
			`Commission: ${platformCommission}%, Resale Cap: ${antiScalpCap}%, Max Transfers: ${maxTransferCount}`
		);
		setTimeout(() => {
			settingsSaved = false;
		}, 3000);
	}

	function saveFeeSettings() {
		feeSettingsSaved = true;
		addLog(
			'UPDATE_FEE_SETTINGS',
			`Processing Fee: ${fixedProcessingFee} VND, Convenience Fee: ${buyerConvenienceFee}%`
		);
		setTimeout(() => {
			feeSettingsSaved = false;
		}, 3000);
	}

	function openPostpone(eventId: string) {
		selectedEventId = eventId;
		postponeReason = '';
		showPostponeModal = true;
	}

	// Local mutation actions
	function toggleUserBan(id: string) {
		const usr = usersList.find((u) => u.id === id);
		if (usr) {
			usr.status = usr.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE';
			addLog(
				'USER_STATUS_CHANGE',
				`User ${usr.email} ban status set to ${usr.status === 'BANNED' ? 'Banned' : 'Active'}.`
			);
		}
	}

	function refundOrder(id: string) {
		const ord = ordersList.find((o) => o.id === id);
		if (ord && ord.status !== 'REFUNDED') {
			ord.status = 'REFUNDED';
			addLog('ORDER_REFUND', `Order ${ord.id} has been fully refunded.`);
		}
	}

	function approvePayout(id: string) {
		const pay = payoutsList.find((p) => p.id === id);
		if (pay && pay.status === 'PENDING') {
			pay.status = 'APPROVED';
			addLog(
				'PAYOUT_APPROVE',
				`Payout ${pay.id} approved for ${pay.orgName}. Net: ${pay.netAmount.toLocaleString()} VND`
			);
		}
	}

	function flagResaleListing(id: string) {
		const res = resaleListings.find((r) => r.id === id);
		if (res) {
			res.status = res.status === 'SUSPICIOUS' ? 'ACTIVE' : 'SUSPICIOUS';
			addLog('RESALE_FLAG', `Resale listing ${res.id} flag toggled. Status: ${res.status}`);
		}
	}

	function togglePromoStatus(id: string) {
		const p = promotionsList.find((pr) => pr.id === id);
		if (p) {
			p.status = p.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
			addLog('PROMO_STATUS', `Promotion code ${p.code} status set to ${p.status}`);
		}
	}

	function toggleCmsBanner(id: string) {
		const b = cmsBanners.find((bn) => bn.id === id);
		if (b) {
			b.status = b.status === 'ACTIVE' ? 'DRAFT' : 'ACTIVE';
			addLog('CMS_BANNER_STATUS', `CMS banner "${b.title}" visibility toggled to ${b.status}`);
		}
	}

	function createPromo(e: Event) {
		e.preventDefault();
		if (!promoCode) return;
		promotionsList = [
			...promotionsList,
			{
				id: 'promo-' + Date.now(),
				code: promoCode.toUpperCase(),
				type: promoType,
				value: promoVal,
				usages: 0,
				status: 'ACTIVE'
			}
		];
		addLog('PROMO_CREATE', `Created promotion code ${promoCode.toUpperCase()}`);
		showAddPromoModal = false;
		promoCode = '';
	}

	function createCmsBanner(e: Event) {
		e.preventDefault();
		if (!bannerTitle) return;
		cmsBanners = [
			...cmsBanners,
			{
				id: 'banner-' + Date.now(),
				title: bannerTitle,
				description: bannerDesc,
				imageUrl:
					bannerImg ||
					'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=600&q=80',
				status: 'DRAFT',
				position: cmsBanners.length + 1
			}
		];
		addLog('CMS_BANNER_CREATE', `Created CMS banner "${bannerTitle}"`);
		showAddBannerModal = false;
		bannerTitle = '';
		bannerDesc = '';
		bannerImg = '';
	}

	function formatDateTime(isoString: string) {
		const d = new Date(isoString);
		return d.toLocaleString('en-US', {
			month: 'short',
			day: 'numeric',
			year: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	}

	// Dynamic slug generation helpers
	function cleanVietnamese(text: string): string {
		return text
			.normalize('NFD')
			.replace(/[\u0300-\u036f]/g, '')
			.replace(/đ/g, 'd')
			.replace(/Đ/g, 'd');
	}

	function slugify(text: string) {
		return cleanVietnamese(text.toString())
			.toLowerCase()
			.replace(/\s+/g, '-')
			.replace(/[^\w-]+/g, '')
			.replace(/--+/g, '-')
			.replace(/^-+/, '')
			.replace(/-+$/, '');
	}
</script>

<svelte:head>
	<title>Ops Dashboard | Ticketpeak Platform Admin</title>
</svelte:head>

<div class="min-h-full w-full bg-[#FAFAFA] p-6 font-sans text-[#111111] antialiased">
	<!-- Error or Notifications -->
	{#if form?.error}
		<div
			class="mb-6 flex items-center gap-3 border border-red-100 bg-red-50/50 p-4 text-sm font-semibold text-red-700 select-none"
		>
			<span>⚠️ Operation Failed: {form.error}</span>
		</div>
	{/if}
	{#if form?.message}
		<div
			class="mb-6 flex items-center gap-3 border border-[#E4E4E7] bg-white p-4 text-sm font-semibold text-[#111111] select-none"
		>
			<span>✅ Action Succeeded: {form.message}</span>
		</div>
	{/if}

	<!-- Dashboard Welcome & Overview -->
	<div class="mb-8 flex items-center justify-between select-none">
		<div>
			<h1 class="text-2xl font-bold tracking-tight text-[#09090B] sm:text-3xl">
				Operations Control
			</h1>
			<p class="mt-1.5 text-xs text-[#71717A]">
				Real-time command center for global ticket events, payments, platform users, and metadata.
			</p>
		</div>
	</div>

	<!-- KPI Stats Row (Visible on Dashboard Tab Only) -->
	{#if activeTab === 'dashboard'}
		<div class="mb-8 grid grid-cols-1 gap-5 select-none sm:grid-cols-2 lg:grid-cols-4">
			<!-- KPI 1 -->
			<div class="rounded-lg border border-[#E4E4E7] bg-white p-6 transition-all hover:shadow-xs">
				<div class="flex items-center justify-between">
					<span class="text-xs font-medium text-[#71717A]">Platform GMV</span>
					<span
						class="rounded border border-emerald-100 bg-emerald-50 px-2 py-0.5 text-[10px] font-semibold text-emerald-600"
						>+12.4%</span
					>
				</div>
				<p class="mt-4 text-2xl font-bold tracking-tight text-[#09090B]">₫4,520,380,000</p>
				<p class="mt-1 text-[11px] text-[#71717A]">Total gross volume transacted</p>
			</div>

			<!-- KPI 2 -->
			<div class="rounded-lg border border-[#E4E4E7] bg-white p-6 transition-all hover:shadow-xs">
				<div class="flex items-center justify-between">
					<span class="text-xs font-medium text-[#71717A]">Live Events</span>
					<span
						class="rounded border border-[#E4E4E7] bg-[#F4F4F5] px-2 py-0.5 text-[10px] font-semibold text-[#71717A]"
						>{events.length} Live</span
					>
				</div>
				<p class="mt-4 text-2xl font-bold tracking-tight text-[#09090B]">
					{events.filter((e: any) => e.status === 'ON_SALE' || e.status === 'PUBLISHED').length} Active
				</p>
				<p class="mt-1 text-[11px] text-[#71717A]">Events currently accepting orders</p>
			</div>

			<!-- KPI 3 -->
			<div class="rounded-lg border border-[#E4E4E7] bg-white p-6 transition-all hover:shadow-xs">
				<div class="flex items-center justify-between">
					<span class="text-xs font-medium text-[#71717A]">Verified Organizers</span>
					<span
						class="rounded border border-[#E4E4E7] bg-[#F4F4F5] px-2 py-0.5 text-[10px] font-semibold text-[#71717A]"
						>{organizations.length} Total</span
					>
				</div>
				<p class="mt-4 text-2xl font-bold tracking-tight text-[#09090B]">{organizations.length}</p>
				<p class="mt-1 text-[11px] text-[#71717A]">Active business organizers accounts</p>
			</div>

			<!-- KPI 4 -->
			<div class="rounded-lg border border-[#E4E4E7] bg-white p-6 transition-all hover:shadow-xs">
				<div class="flex items-center justify-between">
					<span class="text-xs font-medium text-[#71717A]">Check-in Count</span>
					<span
						class="rounded border border-emerald-100 bg-emerald-50 px-2 py-0.5 text-[10px] font-semibold text-emerald-600"
						>98% Success</span
					>
				</div>
				<p class="mt-4 text-2xl font-bold tracking-tight text-[#09090B]">1,485 scans</p>
				<p class="mt-1 text-[11px] text-[#71717A]">Scanned check-in entries today</p>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 1: DASHBOARD ======================== -->
	{#if activeTab === 'dashboard'}
		<div class="grid grid-cols-1 gap-6 select-none lg:grid-cols-3">
			<!-- GMV & Revenue Chart -->
			<div class="rounded-lg border border-[#E4E4E7] bg-white p-6 lg:col-span-2">
				<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-4">
					<h3 class="text-sm font-semibold text-[#111111]">Platform Sales Weekly Trend</h3>
					<span class="text-xs font-semibold text-[#71717A]">Amount in Millions (VND)</span>
				</div>
				<!-- Pure CSS Column Chart -->
				<div class="flex h-48 items-end justify-between gap-4 pt-6">
					<div class="flex flex-1 flex-col items-center gap-2">
						<div class="w-full rounded-xs bg-[#111111]" style="height: 35%;"></div>
						<span class="font-mono text-[10px] font-semibold text-[#71717A]">Mon</span>
					</div>
					<div class="flex flex-1 flex-col items-center gap-2">
						<div class="w-full rounded-xs bg-[#111111]" style="height: 55%;"></div>
						<span class="font-mono text-[10px] font-semibold text-[#71717A]">Tue</span>
					</div>
					<div class="flex flex-1 flex-col items-center gap-2">
						<div class="w-full rounded-xs bg-[#111111]" style="height: 48%;"></div>
						<span class="font-mono text-[10px] font-semibold text-[#71717A]">Wed</span>
					</div>
					<div class="flex flex-1 flex-col items-center gap-2">
						<div class="w-full rounded-xs bg-[#111111]" style="height: 72%;"></div>
						<span class="font-mono text-[10px] font-semibold text-[#71717A]">Thu</span>
					</div>
					<div class="flex flex-1 flex-col items-center gap-2">
						<div class="w-full rounded-xs bg-[#111111]" style="height: 60%;"></div>
						<span class="font-mono text-[10px] font-semibold text-[#71717A]">Fri</span>
					</div>
					<div class="flex flex-1 flex-col items-center gap-2">
						<div class="w-full rounded-xs bg-[#111111]" style="height: 90%;"></div>
						<span class="font-mono text-[10px] font-semibold text-[#71717A]">Sat</span>
					</div>
					<div class="flex flex-1 flex-col items-center gap-2">
						<div class="w-full rounded-xs bg-[#111111]" style="height: 82%;"></div>
						<span class="font-mono text-[10px] font-semibold text-[#71717A]">Sun</span>
					</div>
				</div>
			</div>

			<!-- Quick Actions / Tasks Alert Block -->
			<div class="space-y-4 rounded-lg border border-[#E4E4E7] bg-white p-6">
				<h3 class="border-b border-[#F4F4F5] pb-3 text-sm font-semibold text-[#111111]">
					Pending Moderation Tasks
				</h3>
				<div class="space-y-3">
					<div
						class="space-y-2 rounded-md border border-[#E4E4E7] bg-[#FAFAFA] p-3 text-xs font-semibold"
					>
						<div class="flex items-center justify-between">
							<span class="text-[#111111]">Draft Events Waiting Approval</span>
							<span class="rounded bg-[#111111] px-1.5 py-0.5 font-mono text-[10px] text-white">
								{events.filter((e: any) => e.status === 'DRAFT').length}
							</span>
						</div>
						<button
							onclick={() => goto('/ops/dashboard?tab=events')}
							class="w-full cursor-pointer rounded border border-[#E4E4E7] bg-white py-1 text-center font-bold text-[#111111] transition hover:bg-[#F4F4F5]"
							>Review events</button
						>
					</div>

					<div
						class="space-y-2 rounded-md border border-[#E4E4E7] bg-[#FAFAFA] p-3 text-xs font-semibold"
					>
						<div class="flex items-center justify-between">
							<span class="text-[#111111]">Pending Organizer Payouts</span>
							<span class="rounded bg-[#111111] px-1.5 py-0.5 font-mono text-[10px] text-white">
								{payoutsList.filter((p) => p.status === 'PENDING').length}
							</span>
						</div>
						<button
							onclick={() => goto('/ops/dashboard?tab=payouts')}
							class="w-full cursor-pointer rounded border border-[#E4E4E7] bg-white py-1 text-center font-bold text-[#111111] transition hover:bg-[#F4F4F5]"
							>Review payouts</button
						>
					</div>
				</div>
			</div>
		</div>

		<!-- Recent Activity Log Streams -->
		<div class="mt-6 rounded-lg border border-[#E4E4E7] bg-white p-6 select-none">
			<h3 class="mb-4 border-b border-[#F4F4F5] pb-3 text-sm font-semibold text-[#111111]">
				Latest System Activities
			</h3>
			<div class="space-y-3 font-mono text-xs text-[#71717A]">
				{#each auditLogs.slice(0, 4) as log (log.id)}
					<div
						class="flex items-center justify-between border-b border-[#F4F4F5] pb-2 last:border-0"
					>
						<div class="flex items-center gap-3">
							<span class="font-bold text-[#111111]">{log.time}</span>
							<span>[{log.action}] - {log.details}</span>
						</div>
						<span class="text-[10px] font-semibold text-[#71717A]">{log.user}</span>
					</div>
				{/each}
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 2: ANALYTICS ======================== -->
	{#if activeTab === 'analytics'}
		<div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
			<div class="rounded-lg border border-[#E4E4E7] bg-white p-6 lg:col-span-2">
				<h3 class="mb-4 border-b border-[#F4F4F5] pb-3 text-sm font-semibold text-[#111111]">
					Top Organizer Gross Sales
				</h3>
				<div class="overflow-x-auto">
					<table class="w-full text-left text-xs font-semibold text-[#71717A]">
						<thead>
							<tr class="border-b border-[#E4E4E7] text-[10px] text-[#71717A] uppercase">
								<th class="py-2">Organizer</th>
								<th class="py-2">Total Sales (VND)</th>
								<th class="py-2">Platform Cut (5%)</th>
								<th class="py-2">Commission Earned</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-[#F4F4F5] text-[#111111]">
							<tr class="hover:bg-[#FAFAFA]">
								<td class="py-3 font-bold">Hanoi Showbiz Ent.</td>
								<td class="py-3">₫450,000,000</td>
								<td class="py-3 font-mono">5.0%</td>
								<td class="py-3 font-bold text-emerald-600">₫22,500,000</td>
							</tr>
							<tr class="hover:bg-[#FAFAFA]">
								<td class="py-3 font-bold">V-Concert Group</td>
								<td class="py-3">₫120,000,000</td>
								<td class="py-3 font-mono">5.0%</td>
								<td class="py-3 font-bold text-emerald-600">₫6,000,000</td>
							</tr>
							<tr class="hover:bg-[#FAFAFA]">
								<td class="py-3 font-bold">Hanoi Rock Club</td>
								<td class="py-3">₫80,000,000</td>
								<td class="py-3 font-mono">5.0%</td>
								<td class="py-3 font-bold text-emerald-600">₫4,000,000</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<div class="space-y-6 rounded-lg border border-[#E4E4E7] bg-white p-6">
				<h3 class="border-b border-[#F4F4F5] pb-3 text-sm font-semibold text-[#111111]">
					Sales Category Breakdown
				</h3>
				<div class="space-y-4">
					<div>
						<div class="mb-1 flex justify-between text-xs font-semibold text-[#71717A]">
							<span>Music Concerts</span>
							<span class="font-bold text-[#111111]">65%</span>
						</div>
						<div class="h-2 w-full rounded-full bg-[#F4F4F5]">
							<div class="h-2 rounded-full bg-[#111111]" style="width: 65%;"></div>
						</div>
					</div>
					<div>
						<div class="mb-1 flex justify-between text-xs font-semibold text-[#71717A]">
							<span>Sports matches</span>
							<span class="font-bold text-[#111111]">18%</span>
						</div>
						<div class="h-2 w-full rounded-full bg-[#F4F4F5]">
							<div class="h-2 rounded-full bg-[#111111]" style="width: 18%;"></div>
						</div>
					</div>
					<div>
						<div class="mb-1 flex justify-between text-xs font-semibold text-[#71717A]">
							<span>Theater / Performing arts</span>
							<span class="font-bold text-[#111111]">12%</span>
						</div>
						<div class="h-2 w-full rounded-full bg-[#F4F4F5]">
							<div class="h-2 rounded-full bg-[#111111]" style="width: 12%;"></div>
						</div>
					</div>
					<div>
						<div class="mb-1 flex justify-between text-xs font-semibold text-[#71717A]">
							<span>Workshops & Seminars</span>
							<span class="font-bold text-[#111111]">5%</span>
						</div>
						<div class="h-2 w-full rounded-full bg-[#F4F4F5]">
							<div class="h-2 rounded-full bg-[#111111]" style="width: 5%;"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 3: EVENTS ======================== -->
	{#if activeTab === 'events'}
		<div class="animate-fade-in overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
			<div class="border-b border-[#F4F4F5] px-6 py-4">
				<h3 class="font-sans text-sm font-semibold text-[#111111] select-none">
					Platform Events Inventory
				</h3>
			</div>
			<div class="overflow-x-auto">
				<table class="w-full border-collapse text-left">
					<thead>
						<tr
							class="border-b border-[#E4E4E7] bg-white text-xs font-semibold tracking-wider text-[#71717A] uppercase"
						>
							<th class="px-6 py-3.5">Event Name</th>
							<th class="px-6 py-3.5">Venue</th>
							<th class="px-6 py-3.5">Start Date</th>
							<th class="px-6 py-3.5">Status</th>
							<th class="px-6 py-3.5 text-right">Moderation Actions</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-slate-100 font-sans text-xs font-semibold text-[#71717A]">
						{#each events as event (event.id)}
							<tr class="transition-colors hover:bg-[#FAFAFA]">
								<td class="px-6 py-4">
									<p class="text-sm font-bold text-[#111111]">{event.title}</p>
									<p class="mt-0.5 text-[10px] text-[#71717A]">ID: {event.id}</p>
								</td>
								<td class="px-6 py-4 font-medium text-[#111111]">{event.venueName}</td>
								<td class="px-6 py-4 text-[#71717A]">{formatDateTime(event.startAt)}</td>
								<td class="px-6 py-4">
									{#if event.status === 'DRAFT'}
										<span
											class="inline-block rounded-md bg-[#F4F4F5] px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-[#71717A] uppercase select-none"
										>
											Draft
										</span>
									{:else if event.status === 'PUBLISHED'}
										<span
											class="inline-block rounded-md bg-[#E4E4E7] px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-[#111111] uppercase select-none"
										>
											Published
										</span>
									{:else if event.status === 'ON_SALE'}
										<span
											class="inline-block rounded-md bg-emerald-50 px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-emerald-600 uppercase select-none"
										>
											On Sale
										</span>
									{:else if event.status === 'POSTPONED'}
										<span
											class="inline-block rounded-md bg-amber-50 px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-amber-600 uppercase select-none"
										>
											Postponed
										</span>
									{:else if event.status === 'CANCELLED'}
										<span
											class="inline-block rounded-md bg-rose-50 px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-rose-600 uppercase select-none"
										>
											Cancelled
										</span>
									{:else}
										<span
											class="inline-block rounded-md bg-[#F4F4F5] px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-[#71717A] uppercase select-none"
										>
											{event.status}
										</span>
									{/if}
								</td>
								<td class="px-6 py-4 text-right">
									<div class="flex items-center justify-end gap-2">
										{#if event.status === 'DRAFT'}
											<form
												method="POST"
												action="?/publishEvent"
												use:enhance={() => {
													return async ({ result, update }) => {
														if (result.type === 'success') {
															addLog('EVENT_PUBLISH', `Approved and published "${event.title}".`);
														}
														await update();
													};
												}}
											>
												<input type="hidden" name="id" value={event.id} />
												<button
													type="submit"
													class="cursor-pointer rounded-md bg-[#111111] px-3 py-1 text-xs font-bold text-white transition-all hover:bg-black active:scale-95"
												>
													Approve
												</button>
											</form>
										{/if}

										{#if event.status === 'PUBLISHED'}
											<form
												method="POST"
												action="?/startSales"
												use:enhance={() => {
													return async ({ result, update }) => {
														if (result.type === 'success') {
															addLog('START_SALES', `Ticket sales opened for "${event.title}".`);
														}
														await update();
													};
												}}
											>
												<input type="hidden" name="id" value={event.id} />
												<button
													type="submit"
													class="cursor-pointer rounded-md bg-emerald-600 px-3 py-1 text-xs font-bold text-white transition-all hover:bg-emerald-700 active:scale-95"
												>
													Open Sales
												</button>
											</form>
										{/if}

										{#if event.status !== 'CANCELLED' && event.status !== 'POSTPONED'}
											<button
												onclick={() => openPostpone(event.id)}
												class="cursor-pointer rounded-md border border-[#E4E4E7] bg-white px-3 py-1 text-xs font-bold text-[#111111] transition-all hover:bg-[#FAFAFA] active:scale-95"
											>
												Postpone
											</button>

											<form
												method="POST"
												action="?/cancelEvent"
												use:enhance={() => {
													return async ({ result, update }) => {
														if (result.type === 'success') {
															addLog(
																'EVENT_CANCEL',
																`Platform-enforced cancellation of event "${event.title}".`
															);
														}
														await update();
													};
												}}
											>
												<input type="hidden" name="id" value={event.id} />
												<button
													type="submit"
													class="cursor-pointer rounded-md border border-rose-100 bg-rose-50/50 px-3 py-1 text-xs font-bold text-rose-600 transition-all hover:bg-rose-600 hover:text-white active:scale-95"
												>
													Cancel
												</button>
											</form>
										{/if}
									</div>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 5: ATTRACTIONS ======================== -->

	<!-- ======================== TAB 7: PLATFORM USERS ======================== -->
	{#if activeTab === 'users'}
		<div class="animate-fade-in overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
			<div
				class="flex flex-col gap-4 border-b border-[#F4F4F5] px-6 py-4 sm:flex-row sm:items-center sm:justify-between"
			>
				<div class="flex flex-1 items-center gap-3">
					<div class="relative w-full max-w-xs">
						<span
							class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-[#71717A]"
						>
							<svg
								class="h-4 w-4"
								fill="none"
								viewBox="0 0 24 24"
								stroke="currentColor"
								stroke-width="2"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
								/>
							</svg>
						</span>
						<input
							type="text"
							placeholder="Search user email or name..."
							bind:value={userSearch}
							class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] py-2 pr-4 pl-9 text-xs font-semibold text-[#111111] placeholder-[#71717A] transition outline-none focus:border-[#71717A] focus:bg-white"
						/>
					</div>
					<select
						bind:value={userRoleFilter}
						class="rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3 py-2 text-xs font-semibold text-[#111111] transition outline-none focus:border-[#71717A] focus:bg-white"
					>
						<option value="all">All Roles</option>
						<option value="BUYER">Buyer</option>
						<option value="ORGANIZER">Organizer</option>
						<option value="ADMIN">Admin</option>
					</select>
				</div>
			</div>
			<div class="overflow-x-auto">
				<table class="w-full border-collapse text-left text-xs font-semibold text-[#71717A]">
					<thead>
						<tr
							class="border-b border-[#E4E4E7] bg-[#FAFAFA] text-[10px] text-[#71717A] uppercase select-none"
						>
							<th class="px-6 py-3.5">User Details</th>
							<th class="px-6 py-3.5">Email Address</th>
							<th class="px-6 py-3.5">Assigned Role</th>
							<th class="px-6 py-3.5">Registered Date</th>
							<th class="px-6 py-3.5">Security Status</th>
							<th class="px-6 py-3.5 text-right">Moderation Actions</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-[#F4F4F5] bg-white text-[#111111]">
						{#each filteredUsers as user (user.id)}
							<tr class="hover:bg-[#FAFAFA]">
								<td class="px-6 py-4 font-bold">{user.name}</td>
								<td class="px-6 py-4 font-medium text-[#71717A]">{user.email}</td>
								<td class="px-6 py-4">
									<span
										class="inline-block rounded-md border border-[#E4E4E7] bg-[#F4F4F5] px-2 py-0.5 font-mono text-[9px] text-[#71717A]"
									>
										{user.role}
									</span>
								</td>
								<td class="px-6 py-4 font-medium text-[#71717A]"
									>{formatDateTime(user.registeredAt)}</td
								>
								<td class="px-6 py-4">
									<span
										class="inline-block rounded-md px-2.5 py-0.5 text-[9px] font-bold uppercase select-none {user.status ===
										'ACTIVE'
											? 'bg-emerald-50 text-emerald-600'
											: 'bg-rose-50 text-rose-600'}"
									>
										{user.status}
									</span>
								</td>
								<td class="px-6 py-4 text-right">
									<button
										onclick={() => toggleUserBan(user.id)}
										class="cursor-pointer rounded-md border px-3 py-1 text-xs font-bold transition-all active:scale-95 {user.status ===
										'ACTIVE'
											? 'border-rose-100 bg-rose-50/50 text-rose-600 hover:bg-rose-600 hover:text-white'
											: 'border-emerald-100 bg-emerald-50/50 text-emerald-600 hover:bg-emerald-600 hover:text-white'}"
									>
										{user.status === 'ACTIVE' ? 'Ban User' : 'Unban User'}
									</button>
								</td>
							</tr>
						{:else}
							<tr>
								<td colspan="6" class="p-12 text-center text-[#71717A] font-medium"
									>No platform users found matching your search.</td
								>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 8: ROLES & PERMISSIONS ======================== -->
	{#if activeTab === 'roles'}
		<div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
			<div class="rounded-lg border border-[#E4E4E7] bg-white p-6 lg:col-span-2">
				<h3 class="mb-4 border-b border-[#F4F4F5] pb-3 text-sm font-semibold text-[#111111]">
					Default Authorization Policies
				</h3>
				<div class="space-y-4">
					<div class="rounded-md border border-[#E4E4E7] bg-[#FAFAFA] p-4">
						<div class="mb-2 flex items-center justify-between">
							<span class="text-xs font-bold text-[#111111] uppercase">ROLE_ADMIN</span>
							<span class="rounded bg-emerald-50 px-2 py-0.5 text-[9px] font-bold text-emerald-600"
								>All Scopes Allowed</span
							>
						</div>
						<p class="text-xs text-[#71717A]">
							Complete super administrator access. Can override tickets transfer locks, issue
							platform-level refunds, configure commission rates, ban users, and approve payout
							channels.
						</p>
					</div>

					<div class="rounded-md border border-[#E4E4E7] bg-[#FAFAFA] p-4">
						<div class="mb-2 flex items-center justify-between">
							<span class="text-xs font-bold text-[#111111] uppercase">ROLE_ORGANIZER</span>
							<span class="rounded bg-[#F4F4F5] px-2 py-0.5 text-[9px] font-bold text-[#71717A]"
								>8 scopes</span
							>
						</div>
						<p class="text-xs text-[#71717A]">
							Assigned to verified business organizers. Authorizes creation, publishing, inventory
							setup, holds allocation, seating manifests configurations, and payment settlement
							checks.
						</p>
					</div>

					<div class="rounded-md border border-[#E4E4E7] bg-[#FAFAFA] p-4">
						<div class="mb-2 flex items-center justify-between">
							<span class="text-xs font-bold text-[#111111] uppercase">ROLE_BUYER</span>
							<span class="rounded bg-[#F4F4F5] px-2 py-0.5 text-[9px] font-bold text-[#71717A]"
								>3 scopes</span
							>
						</div>
						<p class="text-xs text-[#71717A]">
							Assigned to registered end ticket purchasers. Authorizes buying tickets, generating
							TOTP-secured check-in QR codes, submitting secondary resale listings, and transferring
							tickets.
						</p>
					</div>
				</div>
			</div>

			<div class="space-y-4 rounded-lg border border-[#E4E4E7] bg-white p-6">
				<h3 class="border-b border-[#F4F4F5] pb-3 text-sm font-semibold text-[#111111]">
					Platform System Scopes
				</h3>
				<div class="space-y-3 font-mono text-[11px] text-[#71717A]">
					<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-2">
						<span>event:publish</span>
						<input type="checkbox" checked disabled class="accent-slate-900" />
					</div>
					<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-2">
						<span>order:refund</span>
						<input type="checkbox" checked disabled class="accent-slate-900" />
					</div>
					<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-2">
						<span>user:ban</span>
						<input type="checkbox" checked disabled class="accent-slate-900" />
					</div>
					<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-2">
						<span>payout:approve</span>
						<input type="checkbox" checked disabled class="accent-slate-900" />
					</div>
					<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-2">
						<span>resale:censor</span>
						<input type="checkbox" checked disabled class="accent-slate-900" />
					</div>
					<div class="flex items-center justify-between">
						<span>cms:manage</span>
						<input type="checkbox" checked disabled class="accent-slate-900" />
					</div>
				</div>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 9: ORDERS & TRANSACTIONS ======================== -->
	{#if activeTab === 'orders'}
		<div class="animate-fade-in overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
			<div
				class="flex flex-col gap-4 border-b border-[#F4F4F5] px-6 py-4 sm:flex-row sm:items-center sm:justify-between"
			>
				<div class="flex flex-1 items-center gap-3">
					<div class="relative w-full max-w-xs">
						<span
							class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-[#71717A]"
						>
							<svg
								class="h-4 w-4"
								fill="none"
								viewBox="0 0 24 24"
								stroke="currentColor"
								stroke-width="2"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
								/>
							</svg>
						</span>
						<input
							type="text"
							placeholder="Search order ID, event, customer..."
							bind:value={orderSearch}
							class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] py-2 pr-4 pl-9 text-xs font-semibold text-[#111111] placeholder-[#71717A] transition outline-none focus:border-[#71717A] focus:bg-white"
						/>
					</div>
					<select
						bind:value={orderStatusFilter}
						class="rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3 py-2 text-xs font-semibold text-[#111111] transition outline-none focus:border-[#71717A] focus:bg-white"
					>
						<option value="all">All Status</option>
						<option value="SUCCESS">Success</option>
						<option value="REFUNDED">Refunded</option>
					</select>
				</div>
			</div>
			<div class="overflow-x-auto">
				<table class="w-full border-collapse text-left text-xs font-semibold text-[#71717A]">
					<thead>
						<tr
							class="border-b border-[#E4E4E7] bg-[#FAFAFA] text-[10px] text-[#71717A] uppercase select-none"
						>
							<th class="px-6 py-3.5">Order ID</th>
							<th class="px-6 py-3.5">Customer details</th>
							<th class="px-6 py-3.5">Purchased Event</th>
							<th class="px-6 py-3.5">Tickets qty</th>
							<th class="px-6 py-3.5">Total Amount</th>
							<th class="px-6 py-3.5">Status</th>
							<th class="px-6 py-3.5 text-right">Actions</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-[#F4F4F5] bg-white text-[#111111]">
						{#each filteredOrders as order (order.id)}
							<tr class="hover:bg-[#FAFAFA]">
								<td class="px-6 py-4 font-mono font-bold text-[#111111]">{order.id}</td>
								<td class="px-6 py-4">
									<p class="font-bold">{order.customerName}</p>
									<p class="mt-0.5 font-normal text-[#71717A]">{order.customerEmail}</p>
								</td>
								<td class="px-6 py-4 font-medium">{order.eventTitle}</td>
								<td class="px-6 py-4 font-mono">{order.ticketCount}</td>
								<td class="px-6 py-4 font-mono font-bold text-[#111111]"
									>₫{order.totalAmount.toLocaleString()}</td
								>
								<td class="px-6 py-4">
									<span
										class="inline-block rounded-md px-2.5 py-0.5 text-[9px] font-bold uppercase select-none {order.status ===
										'SUCCESS'
											? 'bg-emerald-50 text-emerald-600'
											: 'bg-rose-50 text-rose-600'}"
									>
										{order.status}
									</span>
								</td>
								<td class="px-6 py-4 text-right">
									{#if order.status === 'SUCCESS'}
										<button
											onclick={() => refundOrder(order.id)}
											class="cursor-pointer rounded-md border border-rose-100 bg-rose-50/50 px-3 py-1 text-xs font-bold text-rose-600 transition-all hover:bg-rose-600 hover:text-white active:scale-95"
										>
											Issue Refund
										</button>
									{:else}
										<span class="text-xs font-semibold text-[#71717A] italic">Closed</span>
									{/if}
								</td>
							</tr>
						{:else}
							<tr>
								<td colspan="7" class="p-12 text-center text-[#71717A] font-medium"
									>No order transactions found matching your search.</td
								>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 10: PAYOUTS QUEUE ======================== -->
	{#if activeTab === 'payouts'}
		<div class="animate-fade-in overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
			<div
				class="flex flex-col gap-4 border-b border-[#F4F4F5] px-6 py-4 sm:flex-row sm:items-center sm:justify-between"
			>
				<div class="flex flex-1 items-center gap-3">
					<div class="relative w-full max-w-xs">
						<span
							class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-[#71717A]"
						>
							<svg
								class="h-4 w-4"
								fill="none"
								viewBox="0 0 24 24"
								stroke="currentColor"
								stroke-width="2"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
								/>
							</svg>
						</span>
						<input
							type="text"
							placeholder="Search organizer or event..."
							bind:value={payoutSearch}
							class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] py-2 pr-4 pl-9 text-xs font-semibold text-[#111111] placeholder-[#71717A] transition outline-none focus:border-[#71717A] focus:bg-white"
						/>
					</div>
					<select
						bind:value={payoutStatusFilter}
						class="rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3 py-2 text-xs font-semibold text-[#111111] transition outline-none focus:border-[#71717A] focus:bg-white"
					>
						<option value="all">All Payouts</option>
						<option value="PENDING">Pending Approval</option>
						<option value="APPROVED">Approved</option>
						<option value="PAID">Paid</option>
					</select>
				</div>
			</div>
			<div class="overflow-x-auto">
				<table class="w-full border-collapse text-left text-xs font-semibold text-[#71717A]">
					<thead>
						<tr
							class="border-b border-[#E4E4E7] bg-[#FAFAFA] text-[10px] text-[#71717A] uppercase select-none"
						>
							<th class="px-6 py-3.5">Payout ID</th>
							<th class="px-6 py-3.5">Organizer Partner</th>
							<th class="px-6 py-3.5">Settling Event</th>
							<th class="px-6 py-3.5">Total Gross sales</th>
							<th class="px-6 py-3.5">Commission Fee</th>
							<th class="px-6 py-3.5">Net Payout Amount</th>
							<th class="px-6 py-3.5">Status</th>
							<th class="px-6 py-3.5 text-right font-bold">Actions</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-[#F4F4F5] bg-white text-[#111111]">
						{#each filteredPayouts as pay (pay.id)}
							<tr class="hover:bg-[#FAFAFA]">
								<td class="px-6 py-4 font-mono font-bold text-[#111111]">{pay.id}</td>
								<td class="px-6 py-4 font-bold">{pay.orgName}</td>
								<td class="px-6 py-4 font-medium text-[#71717A]">{pay.eventTitle}</td>
								<td class="px-6 py-4 font-mono">₫{pay.totalSales.toLocaleString()}</td>
								<td class="px-6 py-4 font-mono text-[#71717A]">{pay.commissionRate}%</td>
								<td class="px-6 py-4 font-mono font-bold text-emerald-600"
									>₫{pay.netAmount.toLocaleString()}</td
								>
								<td class="px-6 py-4">
									<span
										class="inline-block rounded-md px-2.5 py-0.5 text-[9px] font-bold uppercase select-none {pay.status ===
										'PAID'
											? 'bg-emerald-50 text-emerald-600'
											: pay.status === 'APPROVED'
												? 'bg-blue-50 text-blue-600'
												: 'bg-amber-50 text-amber-600'}"
									>
										{pay.status}
									</span>
								</td>
								<td class="px-6 py-4 text-right">
									{#if pay.status === 'PENDING'}
										<button
											onclick={() => approvePayout(pay.id)}
											class="cursor-pointer rounded-md bg-[#111111] px-3 py-1 text-xs font-bold text-white transition-all hover:bg-black active:scale-95"
										>
											Approve Payout
										</button>
									{:else}
										<span class="text-xs font-semibold text-[#71717A] italic">Completed</span>
									{/if}
								</td>
							</tr>
						{:else}
							<tr>
								<td colspan="8" class="p-12 text-center text-[#71717A] font-medium"
									>No payouts queue transactions matching search.</td
								>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 11: PLATFORM FEES ======================== -->
	{#if activeTab === 'fees'}
		<div
			class="animate-fade-in max-w-2xl rounded-lg border border-[#E4E4E7] bg-white p-6 select-none"
		>
			<h3 class="mb-6 font-sans text-sm font-semibold text-[#111111]">
				Platform Revenue Fees Settings
			</h3>
			<div class="space-y-6">
				<!-- Fee 1 -->
				<div class="space-y-2">
					<div class="flex items-center justify-between text-xs font-semibold text-[#71717A]">
						<span>Merchant Commission rate (%)</span>
						<span class="text-sm font-bold text-[#111111]">{platformCommission}%</span>
					</div>
					<input
						type="range"
						min="0"
						max="15"
						step="0.5"
						bind:value={platformCommission}
						class="h-1.5 w-full cursor-pointer rounded-lg bg-slate-100 accent-slate-900"
					/>
					<p class="text-xs text-[#71717A]">
						Percentage taken from organizer payouts automatically upon transaction clearance.
					</p>
				</div>

				<!-- Fee 2 -->
				<div class="space-y-2">
					<div class="flex items-center justify-between text-xs font-semibold text-[#71717A]">
						<span>Fixed Buyer Processing Fee (VND)</span>
						<span class="text-sm font-bold text-[#111111]"
							>₫{fixedProcessingFee.toLocaleString()}</span
						>
					</div>
					<input
						type="number"
						min="0"
						step="500"
						bind:value={fixedProcessingFee}
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-sm font-semibold text-[#111111] outline-none focus:border-[#71717A] focus:bg-white"
					/>
					<p class="text-xs text-[#71717A]">
						Flat transaction charge added to every ticket purchase checkout.
					</p>
				</div>

				<!-- Fee 3 -->
				<div class="space-y-2">
					<div class="flex items-center justify-between text-xs font-semibold text-[#71717A]">
						<span>Buyer Convenience Fee (%)</span>
						<span class="text-sm font-bold text-[#111111]">{buyerConvenienceFee}%</span>
					</div>
					<input
						type="range"
						min="0"
						max="5"
						step="0.1"
						bind:value={buyerConvenienceFee}
						class="h-1.5 w-full cursor-pointer rounded-lg bg-slate-100 accent-slate-900"
					/>
					<p class="text-xs text-[#71717A]">
						Additional variable percentage surcharge applied at customer checkouts.
					</p>
				</div>

				{#if feeSettingsSaved}
					<div
						class="animate-fade-in rounded-md border border-emerald-200 bg-emerald-50/50 p-3 text-xs font-semibold text-emerald-800"
					>
						✨ Global transaction fee configuration applied successfully.
					</div>
				{/if}

				<button
					onclick={saveFeeSettings}
					class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-6 py-2.5 text-center font-sans text-xs font-bold text-white shadow-xs transition hover:bg-black active:scale-95"
				>
					Save Fee Tiers
				</button>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 12: RESALE MARKETPLACE ======================== -->
	{#if activeTab === 'resale'}
		<div class="animate-fade-in overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
			<div
				class="flex flex-col gap-4 border-b border-[#F4F4F5] px-6 py-4 sm:flex-row sm:items-center sm:justify-between"
			>
				<div class="flex flex-1 items-center gap-3">
					<div class="relative w-full max-w-xs">
						<span
							class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-[#71717A]"
						>
							<svg
								class="h-4 w-4"
								fill="none"
								viewBox="0 0 24 24"
								stroke="currentColor"
								stroke-width="2"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
								/>
							</svg>
						</span>
						<input
							type="text"
							placeholder="Search seller or event..."
							bind:value={resaleSearch}
							class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] py-2 pr-4 pl-9 text-xs font-semibold text-[#111111] placeholder-[#71717A] transition outline-none focus:border-[#71717A] focus:bg-white"
						/>
					</div>
					<select
						bind:value={resaleStatusFilter}
						class="rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3 py-2 text-xs font-semibold text-[#111111] transition outline-none focus:border-[#71717A] focus:bg-white"
					>
						<option value="all">All Resales</option>
						<option value="ACTIVE">Active Listing</option>
						<option value="SUSPICIOUS">Suspicious Markup</option>
					</select>
				</div>
			</div>
			<div class="overflow-x-auto">
				<table class="w-full border-collapse text-left text-xs font-semibold text-[#71717A]">
					<thead>
						<tr
							class="border-b border-[#E4E4E7] bg-[#FAFAFA] text-[10px] text-[#71717A] uppercase select-none"
						>
							<th class="px-6 py-3.5">Listing ID</th>
							<th class="px-6 py-3.5">Secondary Seller</th>
							<th class="px-6 py-3.5">Target Ticket Event</th>
							<th class="px-6 py-3.5">Face Value</th>
							<th class="px-6 py-3.5">Listed Resale Price</th>
							<th class="px-6 py-3.5">Markup Percentage</th>
							<th class="px-6 py-3.5">Market Status</th>
							<th class="px-6 py-3.5 text-right font-bold">Actions</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-[#F4F4F5] bg-white text-[#111111]">
						{#each filteredResales as resale (resale.id)}
							<tr class="hover:bg-[#FAFAFA]">
								<td class="px-6 py-4 font-mono font-bold text-[#111111]">{resale.id}</td>
								<td class="px-6 py-4 font-bold">{resale.sellerName}</td>
								<td class="px-6 py-4 font-medium text-[#71717A]">{resale.eventTitle}</td>
								<td class="px-6 py-4 font-mono text-[#71717A]"
									>₫{resale.originalPrice.toLocaleString()}</td
								>
								<td class="px-6 py-4 font-mono font-bold text-[#111111]"
									>₫{resale.resalePrice.toLocaleString()}</td
								>
								<td class="px-6 py-4">
									<span
										class="inline-block rounded px-1.5 py-0.5 font-mono text-[10px] font-bold {resale.markup >
										antiScalpCap
											? 'border border-red-100 bg-red-50 text-red-600'
											: 'bg-slate-50 text-[#71717A]'}"
									>
										+{resale.markup}%
									</span>
								</td>
								<td class="px-6 py-4">
									<span
										class="inline-block rounded-md px-2.5 py-0.5 text-[9px] font-bold uppercase select-none {resale.status ===
										'ACTIVE'
											? 'bg-emerald-50 text-emerald-600'
											: 'bg-rose-50 text-rose-600'}"
									>
										{resale.status}
									</span>
								</td>
								<td class="px-6 py-4 text-right">
									<button
										onclick={() => flagResaleListing(resale.id)}
										class="cursor-pointer rounded-md border px-3 py-1 text-xs font-bold transition-all active:scale-95 {resale.status ===
										'SUSPICIOUS'
											? 'border-emerald-100 bg-emerald-50/50 text-emerald-600 hover:bg-emerald-600'
											: 'border-rose-100 bg-rose-50/50 text-rose-600 hover:bg-rose-600 hover:text-white'}"
									>
										{resale.status === 'SUSPICIOUS' ? 'Clear Listing' : 'Flag Listing'}
									</button>
								</td>
							</tr>
						{:else}
							<tr>
								<td colspan="8" class="p-12 text-center text-[#71717A] font-medium"
									>No secondary resale listings match search criteria.</td
								>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 13: CLASSIFICATIONS ======================== -->
	{#if activeTab === 'classifications'}
		<div class="animate-fade-in overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
			<div
				class="flex flex-col gap-4 border-b border-[#F4F4F5] px-6 py-4 sm:flex-row sm:items-center sm:justify-between"
			>
				<!-- Search and Filter controls -->
				<div class="flex flex-1 flex-col gap-3 sm:flex-row sm:items-center">
					<!-- Search Input -->
					<div class="relative w-full max-w-xs">
						<span
							class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-[#71717A]"
						>
							<svg
								class="h-4 w-4"
								fill="none"
								viewBox="0 0 24 24"
								stroke="currentColor"
								stroke-width="2"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
								/>
							</svg>
						</span>
						<input
							type="text"
							placeholder="Search classifications..."
							bind:value={classSearch}
							class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] py-2 pr-4 pl-9 text-xs font-semibold text-[#111111] placeholder-[#71717A] transition outline-none focus:border-[#71717A] focus:bg-white"
						/>
					</div>

					<!-- Filter dropdown -->
					<div class="flex items-center gap-2">
						<select
							bind:value={classLevelFilter}
							class="rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3 py-2 text-xs font-semibold text-[#111111] transition outline-none focus:border-[#71717A] focus:bg-white"
						>
							<option value="all">All Levels</option>
							<option value="1">Root Categories</option>
							<option value="2">Subcategories</option>
						</select>
					</div>
				</div>

				<!-- Create button -->
				<button
					type="button"
					onclick={() => {
						editingClassificationId = null;
						className = '';
						classSlug = '';
						classParentId = '';
						showAddClassificationModal = true;
					}}
					class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-4 py-2 text-center text-xs font-bold text-white shadow-xs transition-all hover:border-black hover:bg-black active:scale-95"
				>
					Create
				</button>
			</div>
			<div class="overflow-x-auto">
				<table class="w-full border-collapse text-left">
					<tbody class="divide-y divide-slate-100 font-sans text-xs font-semibold text-[#71717A]">
						{#each filteredClassifications as cat (cat.id)}
							<tr class="transition-colors hover:bg-[#FAFAFA]">
								<td class="px-6 py-4">
									<button
										type="button"
										onclick={() => openEditClassification(cat)}
										class="cursor-pointer border-0 bg-transparent p-0 text-left text-sm font-bold text-[#111111] transition-colors hover:text-slate-700 hover:underline focus:outline-none"
									>
										{cat.name}
									</button>
								</td>
								<td class="px-6 py-4 font-mono text-[#71717A]">{cat.slug}</td>
								<td class="px-6 py-4">
									{#if cat.parentId}
										{@const parent = classifications.find((c: any) => c.id === cat.parentId)}
										<span class="text-xs font-semibold text-[#111111]">
											{parent ? parent.name : '-'}
										</span>
									{:else}
										<span class="text-xs font-semibold text-[#71717A]">-</span>
									{/if}
								</td>
								<td class="px-6 py-4">
									<div class="flex items-center justify-between gap-4">
										{#if cat.isActive}
											<span
												class="inline-block rounded-md bg-emerald-50 px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-emerald-600 uppercase select-none"
											>
												Active
											</span>
										{:else}
											<span
												class="inline-block rounded-md bg-rose-50 px-2.5 py-0.5 text-[9px] font-bold tracking-wider text-rose-600 uppercase select-none"
											>
												Inactive
											</span>
										{/if}

										<form method="POST" action="?/toggleClassificationStatus" use:enhance>
											<input type="hidden" name="id" value={cat.id} />
											<input type="hidden" name="name" value={cat.name} />
											<input type="hidden" name="slug" value={cat.slug} />
											<input type="hidden" name="parentId" value={cat.parentId || ''} />
											<input
												type="hidden"
												name="isActive"
												value={cat.isActive ? 'true' : 'false'}
											/>
											<button
												type="submit"
												class="cursor-pointer rounded-md border px-3 py-1 text-xs font-bold transition-all active:scale-95
													{cat.isActive
													? 'border-rose-100 bg-rose-50/50 text-rose-600 hover:bg-rose-600 hover:text-white'
													: 'border-emerald-100 bg-emerald-50/50 text-emerald-600 hover:bg-emerald-600 hover:text-white'}"
											>
												{cat.isActive ? 'Deactivate' : 'Activate'}
											</button>
										</form>
									</div>
								</td>
							</tr>
						{:else}
							<tr>
								<td colspan="4" class="p-12 text-center text-[#71717A] font-medium">
									No classifications found matching your search criteria.
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 14: CONTENT / CMS ======================== -->
	{#if activeTab === 'cms'}
		<div class="animate-fade-in space-y-6">
			<div
				class="flex items-center justify-between rounded-lg border-b border-[#E4E4E7] bg-white p-6"
			>
				<div>
					<h3 class="text-sm font-semibold text-[#111111]">
						Homepage Promotional Carousels Banners
					</h3>
					<p class="mt-1 text-xs text-[#71717A]">
						Configure top sliding announcements layouts seen by visitors.
					</p>
				</div>
				<button
					onclick={() => (showAddBannerModal = true)}
					class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-4 py-2 text-xs font-bold text-white shadow-xs transition hover:bg-black active:scale-95"
				>
					Add Slider Slot
				</button>
			</div>

			<div class="grid grid-cols-1 gap-6 select-none md:grid-cols-2 lg:grid-cols-3">
				{#each cmsBanners as banner (banner.id)}
					<div
						class="flex flex-col justify-between overflow-hidden rounded-lg border border-[#E4E4E7] bg-white"
					>
						<div>
							<img
								src={banner.imageUrl}
								alt={banner.title}
								class="h-40 w-full border-b object-cover"
							/>
							<div class="space-y-2 p-4">
								<div class="flex items-center justify-between">
									<span class="font-mono text-[10px] font-bold text-[#71717A]"
										>Slot {banner.position}</span
									>
									<span
										class="inline-block rounded px-2 py-0.5 text-[9px] font-bold uppercase {banner.status ===
										'ACTIVE'
											? 'bg-emerald-50 text-emerald-600'
											: 'bg-slate-50 text-[#71717A]'}"
									>
										{banner.status}
									</span>
								</div>
								<h4 class="text-sm font-bold text-[#111111]">{banner.title}</h4>
								<p class="text-xs leading-normal font-normal text-[#71717A]">
									{banner.description}
								</p>
							</div>
						</div>
						<div class="flex justify-end border-t border-[#F4F4F5] p-3">
							<button
								onclick={() => toggleCmsBanner(banner.id)}
								class="cursor-pointer rounded-md border border-[#E4E4E7] px-3 py-1 text-xs font-bold text-[#111111] transition hover:bg-[#FAFAFA] active:scale-95"
							>
								{banner.status === 'ACTIVE' ? 'Set to Draft' : 'Publish Banner'}
							</button>
						</div>
					</div>
				{/each}
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 15: PROMO CAMPAIGNS ======================== -->
	{#if activeTab === 'promotions'}
		<div class="animate-fade-in overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
			<div class="flex items-center justify-between border-b border-[#F4F4F5] px-6 py-4">
				<h3 class="font-sans text-sm font-semibold text-[#111111]">Platform Discount Vouchers</h3>
				<button
					onclick={() => (showAddPromoModal = true)}
					class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-4 py-2 text-xs font-bold text-white shadow-xs transition hover:bg-black active:scale-95"
				>
					Create Voucher
				</button>
			</div>
			<div class="overflow-x-auto select-none">
				<table class="w-full border-collapse text-left text-xs font-semibold text-[#71717A]">
					<thead>
						<tr class="border-b border-[#E4E4E7] bg-[#FAFAFA] text-[10px] text-[#71717A] uppercase">
							<th class="px-6 py-3.5">Promo Code</th>
							<th class="px-6 py-3.5">Discount Value</th>
							<th class="px-6 py-3.5">Calculation Type</th>
							<th class="px-6 py-3.5">Active Usages</th>
							<th class="px-6 py-3.5">Status</th>
							<th class="px-6 py-3.5 text-right font-bold">Actions</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-[#F4F4F5] bg-white text-[#111111]">
						{#each promotionsList as promo (promo.id)}
							<tr class="hover:bg-[#FAFAFA]">
								<td class="px-6 py-4 font-mono font-bold text-[#111111]">{promo.code}</td>
								<td class="px-6 py-4">
									{#if promo.type === 'PERCENTAGE'}
										{promo.value}% Off
									{:else}
										₫{promo.value.toLocaleString()} Deduction
									{/if}
								</td>
								<td class="px-6 py-4 font-normal text-[#71717A]">{promo.type}</td>
								<td class="px-6 py-4 font-mono">{promo.usages} times used</td>
								<td class="px-6 py-4">
									<span
										class="inline-block rounded-md px-2.5 py-0.5 text-[9px] font-bold uppercase select-none {promo.status ===
										'ACTIVE'
											? 'bg-emerald-50 text-emerald-600'
											: 'bg-slate-50 text-[#71717A]'}"
									>
										{promo.status}
									</span>
								</td>
								<td class="px-6 py-4 text-right">
									<button
										onclick={() => togglePromoStatus(promo.id)}
										class="cursor-pointer rounded-md border px-3 py-1 text-xs font-bold transition-all active:scale-95 {promo.status ===
										'ACTIVE'
											? 'border-rose-100 bg-rose-50/50 text-rose-600'
											: 'border-emerald-100 bg-emerald-50/50 text-emerald-600 hover:bg-emerald-600'}"
									>
										{promo.status === 'ACTIVE' ? 'Disable' : 'Enable'}
									</button>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 16: SYSTEM AUDIT LOGS ======================== -->
	{#if activeTab === 'logs'}
		<div class="animate-fade-in overflow-hidden rounded-lg border border-[#E4E4E7] bg-white">
			<div
				class="flex items-center justify-between border-b border-[#F4F4F5] px-6 py-4 select-none"
			>
				<h3 class="font-sans text-sm font-semibold text-[#111111]">System Audit Logs</h3>
				<span
					class="rounded-md bg-[#F4F4F5] px-2.5 py-0.5 font-mono text-[10px] font-medium text-[#71717A]"
				>
					Real-Time Timeline
				</span>
			</div>

			<div class="max-h-[500px] space-y-4 overflow-y-auto p-6 font-mono text-xs text-[#71717A]">
				{#each auditLogs as log (log.id)}
					<div
						class="flex items-start gap-4 border-b border-[#F4F4F5] pb-3 last:border-0 last:pb-0"
					>
						<span class="shrink-0 font-bold text-[#111111]">{log.time}</span>
						<div class="space-y-1">
							<p class="text-[11px] font-bold text-[#111111]">
								[{log.action}] by <span class="font-semibold text-[#71717A]">{log.user}</span>
							</p>
							<p class="text-xs text-[#71717A]">{log.details}</p>
						</div>
					</div>
				{/each}
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 17: PLATFORM SETTINGS ======================== -->
	{#if activeTab === 'settings'}
		<div
			class="animate-fade-in max-w-2xl rounded-lg border border-[#E4E4E7] bg-white p-6 select-none"
		>
			<h3 class="mb-6 font-sans text-sm font-semibold text-[#111111]">
				Global Configuration Settings
			</h3>

			<div class="space-y-6">
				<!-- Setting 1 -->
				<div class="space-y-2">
					<div class="flex items-center justify-between text-xs font-semibold text-[#71717A]">
						<span>Platform Commission Fee</span>
						<span class="text-sm font-bold text-[#111111]">{platformCommission}%</span>
					</div>
					<input
						type="range"
						min="0"
						max="15"
						step="0.5"
						bind:value={platformCommission}
						class="h-1.5 w-full cursor-pointer rounded-lg bg-slate-100 accent-slate-900"
					/>
					<p class="text-xs text-[#71717A]">
						Service charge taken from organizer payouts automatically upon transaction clearance.
					</p>
				</div>

				<!-- Setting 2 -->
				<div class="space-y-2">
					<div class="flex items-center justify-between text-xs font-semibold text-[#71717A]">
						<span>Anti-Scalping Resale Price Cap</span>
						<span class="text-sm font-bold text-[#111111]">{antiScalpCap}%</span>
					</div>
					<input
						type="range"
						min="100"
						max="200"
						step="5"
						bind:value={antiScalpCap}
						class="h-1.5 w-full cursor-pointer rounded-lg bg-slate-100 accent-slate-900"
					/>
					<p class="text-xs text-[#71717A]">
						Maximum percentage above face value secondary ticket listings are capped at.
					</p>
				</div>

				<!-- Setting 3 -->
				<div class="space-y-2">
					<div class="flex items-center justify-between text-xs font-semibold text-[#71717A]">
						<span>Max Ticket Transfers Limit</span>
						<span class="text-sm font-bold text-[#111111]">{maxTransferCount} times</span>
					</div>
					<input
						type="number"
						min="1"
						max="10"
						bind:value={maxTransferCount}
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-sm font-semibold text-[#111111] outline-none focus:border-[#71717A] focus:bg-white"
					/>
					<p class="text-xs text-[#71717A]">
						Maximum threshold for one-tap ticket transfers before lock-up.
					</p>
				</div>

				<!-- Save Notification -->
				{#if settingsSaved}
					<div
						class="rounded-md border border-emerald-200 bg-emerald-50/50 p-3 text-xs font-semibold text-emerald-800"
					>
						✨ Configurations applied and propagated to active events.
					</div>
				{/if}

				<!-- Submit Button -->
				<button
					onclick={saveSettings}
					class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-6 py-2.5 text-center font-sans text-xs font-bold text-white shadow-xs transition-all hover:border-black hover:bg-black active:scale-95"
				>
					Save Settings
				</button>
			</div>
		</div>
	{/if}

	<!-- ======================== TAB 18: SYSTEM STATUS ======================== -->
	{#if activeTab === 'system'}
		<div class="grid grid-cols-1 gap-6 select-none lg:grid-cols-2">
			<!-- System Status Checks -->
			<div class="space-y-6 rounded-lg border border-[#E4E4E7] bg-white p-6">
				<h3 class="mb-4 border-b border-[#F4F4F5] pb-3 text-sm font-semibold text-[#111111]">
					Infrastructure Connections status
				</h3>
				<div class="space-y-4">
					<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-3">
						<div class="space-y-1">
							<p class="text-xs font-bold text-[#111111]">Primary Database (PostgreSQL)</p>
							<p class="font-mono text-[10px] text-[#71717A]">localhost:5432 / ticketpeak</p>
						</div>
						<div class="flex items-center gap-2">
							<span class="font-mono text-[10px] font-semibold text-[#71717A]">2ms</span>
							<span class="inline-block h-2 w-2 rounded-full bg-emerald-500"></span>
						</div>
					</div>

					<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-3">
						<div class="space-y-1">
							<p class="text-xs font-bold text-[#111111]">Memory Cache Store (Redis)</p>
							<p class="font-mono text-[10px] text-[#71717A]">localhost:6379 / DB 0</p>
						</div>
						<div class="flex items-center gap-2">
							<span class="font-mono text-[10px] font-semibold text-[#71717A]">1ms</span>
							<span class="inline-block h-2 w-2 rounded-full bg-emerald-500"></span>
						</div>
					</div>

					<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-3">
						<div class="space-y-1">
							<p class="text-xs font-bold text-[#111111]">Object Storage Server (MinIO)</p>
							<p class="font-mono text-[10px] text-[#71717A]">localhost:9000 / ticketpeak-media</p>
						</div>
						<div class="flex items-center gap-2">
							<span class="font-mono text-[10px] font-semibold text-[#71717A]">4ms</span>
							<span class="inline-block h-2 w-2 rounded-full bg-emerald-500"></span>
						</div>
					</div>

					<div class="flex items-center justify-between">
						<div class="space-y-1">
							<p class="text-xs font-bold text-[#111111]">Notification Mail Gateway (SMTP)</p>
							<p class="font-mono text-[10px] text-[#71717A]">smtp.mailgun.org:587</p>
						</div>
						<div class="flex items-center gap-2">
							<span class="font-mono text-[10px] font-semibold text-[#71717A]">OK</span>
							<span class="inline-block h-2 w-2 rounded-full bg-emerald-500"></span>
						</div>
					</div>
				</div>
			</div>

			<!-- Background Queues & Schedulers -->
			<div class="space-y-4 rounded-lg border border-[#E4E4E7] bg-white p-6">
				<h3 class="mb-4 border-b border-[#F4F4F5] pb-3 text-sm font-semibold text-[#111111]">
					Background Queues & Crons
				</h3>
				<div class="space-y-4 font-mono text-xs text-[#71717A]">
					<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-3">
						<span>payout-settlement-job</span>
						<span class="text-[10px] font-semibold text-[#111111]">Next run in 3h 42m</span>
					</div>
					<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-3">
						<span>ticket-totp-regeneration</span>
						<span class="text-[10px] font-semibold text-[#111111]">Next run in 12s</span>
					</div>
					<div class="flex items-center justify-between border-b border-[#F4F4F5] pb-3">
						<span>expired-holds-release</span>
						<span class="text-[10px] font-semibold text-[#111111]">Next run in 45s</span>
					</div>
					<div class="flex items-center justify-between">
						<span>resale-markup-audit</span>
						<span class="text-[10px] font-semibold text-[#111111]">Next run in 14m</span>
					</div>
				</div>
			</div>
		</div>
	{/if}
</div>

<!-- ======================== ADD CLASSIFICATION DIALOG MODAL ======================== -->
{#if showAddClassificationModal}
	<div class="fixed inset-0 z-50 flex items-start justify-center bg-black/40 px-4 pt-[12vh]">
		<div
			class="animate-scale-up h-auto max-h-[80vh] w-[90%] max-w-[600px] overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white p-6 text-left shadow-xl select-none"
		>
			<h3 class="text-lg font-bold tracking-tight text-[#09090B]">
				{editingClassificationId ? 'Edit Classification' : 'Add Classification'}
			</h3>
			<p class="mt-1 text-xs text-[#71717A]">
				{editingClassificationId
					? 'Modify details for this event classification.'
					: 'Create a root category or a sub-genre for events ticketing.'}
			</p>

			<form
				method="POST"
				action={editingClassificationId ? '?/updateClassification' : '?/createClassification'}
				use:enhance={() => {
					return async ({ result, update }) => {
						if (result.type === 'success') {
							addLog(
								editingClassificationId ? 'CLASSIFICATION_UPDATE' : 'CLASSIFICATION_CREATE',
								`Classification "${className}" (${classSlug}) ${editingClassificationId ? 'updated' : 'created'} successfully.`
							);
							showAddClassificationModal = false;
						}
						await update();
					};
				}}
				class="mt-4 space-y-4"
			>
				{#if editingClassificationId}
					<input type="hidden" name="id" value={editingClassificationId} />
				{/if}
				<div class="space-y-1">
					<label for="class-name" class="text-xs font-semibold text-[#71717A]">Name *</label>
					<input
						type="text"
						id="class-name"
						name="name"
						bind:value={className}
						oninput={() => {
							classSlug = slugify(className);
						}}
						required
						placeholder="e.g. Pop Music"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label for="class-slug" class="text-xs font-semibold text-[#71717A]">URL Slug *</label>
					<input
						type="text"
						id="class-slug"
						name="slug"
						bind:value={classSlug}
						required
						placeholder="pop-music"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label for="class-parent" class="text-xs font-semibold text-[#71717A]"
						>Parent Category (Optional)</label
					>
					<select
						id="class-parent"
						name="parentId"
						bind:value={classParentId}
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] outline-none focus:border-[#71717A] focus:bg-white"
					>
						<option value="">None (Level 1 Root Category)</option>
						{#each classifications.filter((c: any) => c.level === 1 && c.id !== editingClassificationId) as rootCat (rootCat.id)}
							<option value={rootCat.id}>{rootCat.name}</option>
						{/each}
					</select>
					<p class="pt-1 text-[9px] leading-normal text-[#71717A]">
						Leave blank to create a main root category, or select a parent to create a sub-genre.
					</p>
				</div>

				<div class="flex items-center justify-end gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showAddClassificationModal = false)}
						class="cursor-pointer rounded-md border border-[#E4E4E7] bg-transparent px-5 py-2 text-xs font-bold text-[#71717A] transition-all hover:bg-[#FAFAFA] hover:text-[#111111]"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-5 py-2 text-xs font-bold text-white transition-all hover:border-black hover:bg-black active:scale-95"
					>
						Create
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- POSTPONE DIALOG MODAL -->
{#if showPostponeModal}
	<div class="fixed inset-0 z-50 flex items-start justify-center bg-black/40 px-4 pt-[12vh]">
		<div
			class="animate-scale-up h-auto max-h-[80vh] w-[90%] max-w-[600px] overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white p-6 text-left shadow-xl select-none"
		>
			<h3 class="text-lg font-bold tracking-tight text-[#09090B]">Postpone Event</h3>
			<p class="mt-1 text-xs text-[#71717A]">
				Provide an official platform announcement reason for postponing.
			</p>

			<form
				method="POST"
				action="?/postponeEvent"
				use:enhance={() => {
					return async ({ result, update }) => {
						if (result.type === 'success') {
							addLog(
								'EVENT_POSTPONE',
								`Event ID: ${selectedEventId} officially postponed. Reason: ${postponeReason}`
							);
							showPostponeModal = false;
						}
						await update();
					};
				}}
				class="mt-4 space-y-4"
			>
				<input type="hidden" name="id" value={selectedEventId} />

				<div class="space-y-1">
					<label for="postpone-reason" class="text-xs font-semibold text-[#71717A]">Reason</label>
					<textarea
						id="postpone-reason"
						bind:value={postponeReason}
						required
						rows="3"
						placeholder="e.g. Unavoidable structural delays at military venue."
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					></textarea>
				</div>

				<div class="flex items-center justify-end gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showPostponeModal = false)}
						class="cursor-pointer rounded-md border border-[#E4E4E7] bg-transparent px-5 py-2 text-xs font-bold text-[#71717A] transition-all hover:bg-[#FAFAFA] hover:text-[#111111]"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-5 py-2 text-xs font-bold text-white transition-all hover:border-black hover:bg-black active:scale-95"
					>
						Confirm Postpone
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- ======================== ADD PROMOTION DIALOG MODAL ======================== -->
{#if showAddPromoModal}
	<div class="fixed inset-0 z-50 flex items-start justify-center bg-black/40 px-4 pt-[12vh]">
		<div
			class="animate-scale-up h-auto max-h-[80vh] w-[90%] max-w-[600px] overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white p-6 text-left shadow-xl select-none"
		>
			<h3 class="text-lg font-bold tracking-tight text-[#09090B]">Create Promo Code</h3>
			<p class="mt-1 text-xs text-[#71717A]">
				Add a new global discount coupon for ticket checkouts.
			</p>

			<form onsubmit={createPromo} class="mt-4 space-y-4">
				<div class="space-y-1">
					<label for="promo-code" class="text-xs font-semibold text-[#71717A]">Code Name *</label>
					<input
						type="text"
						id="promo-code"
						bind:value={promoCode}
						required
						placeholder="e.g. WELCOME2026"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label for="promo-type" class="text-xs font-semibold text-[#71717A]">Discount Type</label>
					<select
						id="promo-type"
						bind:value={promoType}
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] outline-none focus:border-[#71717A] focus:bg-white"
					>
						<option value="PERCENTAGE">PERCENTAGE (% off)</option>
						<option value="FLAT">FLAT AMOUNT (VND deduction)</option>
					</select>
				</div>

				<div class="space-y-1">
					<label for="promo-val" class="text-xs font-semibold text-[#71717A]"
						>Discount Value *</label
					>
					<input
						type="number"
						id="promo-val"
						bind:value={promoVal}
						required
						min="1"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="flex items-center justify-end gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showAddPromoModal = false)}
						class="cursor-pointer rounded-md border border-[#E4E4E7] bg-transparent px-5 py-2 text-xs font-bold text-[#71717A] transition-all hover:bg-[#FAFAFA] hover:text-[#111111]"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-5 py-2 text-xs font-bold text-white transition-all hover:border-black hover:bg-black active:scale-95"
					>
						Create
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- ======================== ADD CMS BANNER DIALOG MODAL ======================== -->
{#if showAddBannerModal}
	<div class="fixed inset-0 z-50 flex items-start justify-center bg-black/40 px-4 pt-[12vh]">
		<div
			class="animate-scale-up h-auto max-h-[80vh] w-[90%] max-w-[600px] overflow-y-auto rounded-lg border border-[#E4E4E7] bg-white p-6 text-left shadow-xl select-none"
		>
			<h3 class="text-lg font-bold tracking-tight text-[#09090B]">Create CMS Banner</h3>
			<p class="mt-1 text-xs text-[#71717A]">
				Define a new promotion block or event banner on buyer homepage.
			</p>

			<form onsubmit={createCmsBanner} class="mt-4 space-y-4">
				<div class="space-y-1">
					<label for="banner-title" class="text-xs font-semibold text-[#71717A]">Title *</label>
					<input
						type="text"
						id="banner-title"
						bind:value={bannerTitle}
						required
						placeholder="e.g. Rock Week Presales"
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>

				<div class="space-y-1">
					<label for="banner-desc" class="text-xs font-semibold text-[#71717A]">Description</label>
					<textarea
						id="banner-desc"
						bind:value={bannerDesc}
						rows="2"
						placeholder="Brief summary of banner event..."
						class="w-full rounded-lg border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] placeholder-[#71717A] outline-none focus:border-[#71717A] focus:bg-white"
					></textarea>
				</div>

				<div class="space-y-1">
					<label class="text-xs font-semibold text-[#71717A]">Image</label>
					<ImageInput bind:value={bannerImg} name="imageUrl" />
				</div>

				<div class="flex items-center justify-end gap-3 pt-2">
					<button
						type="button"
						onclick={() => (showAddBannerModal = false)}
						class="cursor-pointer rounded-md border border-[#E4E4E7] bg-transparent px-5 py-2 text-xs font-bold text-[#71717A] transition-all hover:bg-[#FAFAFA] hover:text-[#111111]"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="cursor-pointer rounded-md border border-[#111111] bg-[#111111] px-5 py-2 text-xs font-bold text-white transition-all hover:border-black hover:bg-black active:scale-95"
					>
						Create
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
