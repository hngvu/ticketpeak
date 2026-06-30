<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { 
		IconPlus, IconChevronDown, IconTicket, IconFolderPlus,
		IconPointer2, IconArmchair, IconBorderOuter, IconHandGrab, IconFilter, IconWand
	} from '@tabler/icons-svelte';
	import Combobox from '$lib/components/ui/combobox.svelte';
	import DateTimePicker from '$lib/components/common/DateTimePicker.svelte';
	import TimePicker from '$lib/components/common/TimePicker.svelte';
	import KonvaSeatingCanvas from '$lib/components/event/KonvaSeatingCanvas.svelte';

	function cleanVietnamese(text: string): string {
		return text
			.normalize('NFD')
			.replace(/[\u0300-\u036f]/g, '')
			.replace(/đ/g, 'd')
			.replace(/Đ/g, 'D');
	}

	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { tick } from 'svelte';

	let { data } = $props();

	let saving = $state(false);
	let saveSuccess = $state(false);
	let saveError = $state('');
	let editTitle = $state(data.event?.title || '');
	let isAddOfferModalOpen = $state(false);

	// Canvas Tools
	let activeCanvasTool = $state<'select' | 'pan'>('select');
	let selectionTool = $state<'seat' | 'row' | 'section'>('seat');
	let showFilterDialog = $state(false);
	let showAssistantDialog = $state(false);
	let panX = $state(0);
	let panY = $state(0);
	let isPanning = $state(false);
	let panStartX = $state(0);
	let panStartY = $state(0);

	let konvaCanvasRef: any;
	let canvasViewport = $state({ x: 0, y: 0, w: 800, h: 600, scale: 1 });
	let canvasBounds = $state({ minX: 0, minY: 0, width: 800, height: 600 });

	function handleWheel(e: WheelEvent) {
		// Handled internally by KonvaSeatingCanvas
	}

	function handlePointerDown(e: PointerEvent) {}
	function handlePointerMove(e: PointerEvent) {}
	function handlePointerUp(e: PointerEvent) {}
	let canvasZoom = $state(1);

	let activeTab = $state('general');

	const event = $derived(data.event);
	const offers = $derived(data.offers || []);
	const venueName = $derived(data.venues?.find((v: any) => v.id === event.venueId)?.name || '');
	const attractions = $derived(data.attractions || []);

	let localOffers = $state<any[]>([]);
	let localOffersInitialized = $state(false);
	$effect(() => {
		if (offers && !localOffersInitialized) {
			localOffers = [...offers];
			localOffersInitialized = true;
		}
	});

	let classificationId = $state(
		data.classifications?.find((c: any) => event.classifications?.some((ec: any) => ec.id === c.id))
			?.id || ''
	);
	let venueId = $state(event.venueId || '');
	let selectedAttractionIds = $state<string[]>(event.attractions?.map((a: any) => a.id) || []);
	let startDate = $state(event.startAt ? event.startAt.split('T')[0] : '');
	let startTime = $state(event.startAt ? event.startAt.split('T')[1].substring(0, 5) : '');
	let startAt = $derived(startDate && startTime ? `${startDate}T${startTime}:00` : '');

	const sortedClassifications = $derived.by(() => {
		const raw = data.classifications || [];
		const parents = raw.filter((c: any) => !c.parentId);
		const result: any[] = [];
		for (const p of parents) {
			result.push(p);
			const children = raw.filter((c: any) => c.parentId === p.id);
			result.push(...children);
		}
		const inResult = new Set(result.map((c) => c.id));
		const orphans = raw.filter((c: any) => !inResult.has(c.id));
		result.push(...orphans);
		return result;
	});

	let editingOffer = $state<any>(null);

	let ticketTypes = $state<any[]>(data.ticketTypes || []);
	let isTicketTypeModalOpen = $state(false);
	let isCreateMenuOpen = $state(false);
	let newTicketType = $state<any>({ name: '', code: '' });
	let savingTicketType = $state(false);

	function getTicketTypeName(id: string) {
		const tt = ticketTypes.find((t) => t.id === id);
		return tt ? tt.name : id;
	}

	async function createTicketType() {
		savingTicketType = true;
		try {
			const payload = {
				name: newTicketType.name,
				code: newTicketType.code
			};

			const res = await fetch(`/api/partner/events/${event.id}/ticket-types`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(payload)
			});
			if (!res.ok) {
				const err = await res.json();
				throw new Error(err.message || 'Failed to save ticket type');
			}
			const json = await res.json();
			ticketTypes = [...ticketTypes, json.data];
			newTicketType = { name: '', code: '' };
		} catch (err: any) {
			alert(err.message);
		} finally {
			savingTicketType = false;
		}
	}

	async function deleteTicketType(code: string) {
		if (!confirm('Are you sure you want to delete this ticket type?')) return;
		try {
			const res = await fetch(`/api/partner/events/${event.id}/ticket-types/${code}`, {
				method: 'DELETE'
			});
			if (!res.ok) {
				const err = await res.json();
				throw new Error(err.message || 'Failed to delete ticket type');
			}
			ticketTypes = ticketTypes.filter((t) => t.code !== code);
		} catch (err: any) {
			alert(err.message);
		}
	}

	let holds = $state([
		{
			id: 'hold-1',
			name: 'Sponsor Block A (VIP)',
			count: 20,
			reason: 'Gold Sponsor Allocation',
			status: 'LOCKED'
		},
		{
			id: 'hold-2',
			name: 'Press & Media Tier',
			count: 10,
			reason: 'Journalist Review',
			status: 'LOCKED'
		},
		{
			id: 'hold-3',
			name: 'Technical Camera Hold',
			count: 6,
			reason: 'Camera Obstruction',
			status: 'KILLED'
		}
	]);

	let isAddHoldModalOpen = $state(false);
	let newHoldName = $state('');
	let newHoldCount = $state<number>(15);
	let newHoldReason = $state('');
	let newHoldType = $state<'LOCKED' | 'KILLED'>('LOCKED');

	let selectedSeatIds = $state<string[]>([]);
	let selectedManifestId = $state(
		data.event?.manifestId || (data.manifests && data.manifests[0]?.id)
	);
	let manifestDetail = $state({
		manifest: data.activeManifest,
		levels: data.levels,
		sections: data.sections,
		priceLevels: data.priceLevels,
		rsAreas: data.rsSections,
		gaAreas: data.gaSections,
		objects: data.activeManifest?.objects || data.activeManifest?.uiData?.objects || []
	});

	async function handleManifestChange(e: Event) {
		const newId = (e.target as HTMLSelectElement).value;
		selectedManifestId = newId;
		
		try {
			const res = await fetch(`/api/custom/manifests/${newId}`).then((r) => r.json());
			if (res.success && res.data) {
				const { levels, sections, priceLevels, rsAreas, gaAreas } = res.data;
				const manifest = data.manifests.find((m: any) => m.id === newId);
				manifestDetail = {
					manifest,
					levels,
					sections,
					priceLevels,
					rsAreas,
					gaAreas,
					objects: manifest?.objects || manifest?.uiData?.objects || []
				};
			}
		} catch (err) {
			console.error('Error fetching manifest details:', err);
		}
	}

	const selectedManifest = $derived(data.manifests.find((m: any) => m.id === selectedManifestId));

	const hasLayoutData = $derived(
		manifestDetail && (manifestDetail.gaAreas?.length > 0 || manifestDetail.rsAreas?.length > 0)
	);

	const seatR = 6;
	function seatColor(status: string) {
		if (status === 'Sold') return '#059669';
		if (status === 'Reserved') return '#D97706';
		if (status === 'Held') return '#F59E0B';
		if (status === 'Killed') return '#EF4444';
		return '#94A3B8';
	}

	function getSeatFill(seat: any, currentTab: string) {
		if (currentTab === 'holds') {
			// In Holds mode, emphasize ONLY holds and kills. Fade out Sold and Available.
			if (seat.status === 'Held') return '#F59E0B';
			if (seat.status === 'Killed') return '#EF4444';
			if (seat.status === 'Sold' || seat.status === 'Reserved') return '#CBD5E1'; // darker grey for unavailable seats
			return '#E2E8F0'; // light grey for available seats
		} else {
			// In Seats mode (Scaling), emphasize Price Level assignments
			const plId = seatPriceLevels[seat.id] || (seat.rowLetter < 'C' ? 'VIP' : 'GA');
			return basePriceLevels.find((p) => p.id === plId)?.color || '#e2e8f0';
		}
	}
	const seats = $derived.by(() => {
		if (hasLayoutData) return [];
		const result: {
			id: string;
			rowLetter: string;
			seatNum: number;
			status: string;
			x: number;
			y: number;
			isArea?: boolean;
			capacity?: number;
		}[] = [];
		const cap = selectedManifest?.totalCapacity || 500;
		const isLarge = cap > 5000;
		const rowCount = isLarge ? 6 : 2;
		const colsPerRow = isLarge ? 14 : 10;
		for (let r = 0; r < rowCount; r++) {
			const rowLetter = String.fromCharCode(65 + r);
			for (let c = 0; c < colsPerRow; c++) {
				const seatNum = c + 1;
				const id = `${rowLetter}-${seatNum}`;
				const isLeftBlock = c < colsPerRow / 2;
				const localIndex = isLeftBlock ? c : c - colsPerRow / 2;
				const aisleOffset = isLeftBlock ? 0 : isLarge ? 60 : 44;
				let x = 75 + localIndex * 32 + aisleOffset;
				let y = 80 + r * (isLarge ? 50 : 42);
				if (!isLarge) {
					x = 100 + localIndex * 28 + aisleOffset;
					y = 85 + r * 40;
				}
				let status: string = 'Available';
				if (!isLarge) {
					if (r === 1 && c > 7) status = 'Held';
				} else {
					if (r === 4 && c < 2) status = 'Held';
				}
				status = seatStatuses[id] || status;
				if (status === 'Available') {
					status = (r * colsPerRow + c) % 7 === 0 ? 'Held' : 'Available';
				}
				result.push({ id, rowLetter, seatNum, status, x, y });
			}
		}

		if (!isLarge) {
			result.push({
				id: 'GA',
				rowLetter: 'C', // to map to 'GA' base price level
				seatNum: 0,
				status: seatStatuses['GA'] || 'Available',
				x: 0,
				y: 0,
				isArea: true,
				capacity: 1000
			});
		}
		return result;
	});
	const selectedSeatDetails = $derived.by(() => {
		if (selectedSeatIds.length === 0) return [];
		if (!hasLayoutData) {
			return seats.filter((s) => selectedSeatIds.includes(s.id));
		}
		
		const result: any[] = [];
		for (const sec of manifestDetail?.rsAreas || []) {
			for (const row of sec.rows || []) {
				for (const seat of row.seats || []) {
					if (selectedSeatIds.includes(seat.id)) {
						result.push(seat);
					}
				}
			}
		}
		
		return result;
	});
	let seatPriceLevels = $state<Record<string, string>>({});
	let seatStatuses = $state<Record<string, string>>({});
	let seatHoldIds = $state<Record<string, string>>({});

	const selectedSeatsByPriceLevel = $derived.by(() => {
		const counts: Record<string, number> = {};
		selectedSeatDetails.forEach((s) => {
			const plId = seatPriceLevels[s.id] || s.priceLevelId || 'unassigned';
			counts[plId] = (counts[plId] || 0) + (s.capacity || 1);
		});
		return Object.entries(counts).map(([plId, count]) => ({
			pl: plId === 'unassigned' ? null : basePriceLevels.find((p) => p.id === plId),
			count
		}));
	});

	const totalSelectedCount = $derived.by(() => {
		let total = 0;
		selectedSeatDetails.forEach((s) => {
			total += s.capacity || 1;
		});
		return total;
	});
	const totalSeats = $derived(seats.length);
	const totalSold = $derived(seats.filter((s) => s.status === 'Sold').length);
	const totalReserved = $derived(
		seats.filter((s) => s.status === 'Reserved' || s.status === 'Held').length
	);
	const totalKilled = $derived(seats.filter((s) => s.status === 'Killed').length);
	let basePriceLevels = $state([
		{ id: 'VIP', color: '#7c3aed', label: 'VIP' },
		{ id: 'GA', color: '#059669', label: 'General Admission' }
	]);
	const PRESET_COLORS = [
		'#2563eb', // blue-600
		'#db2777', // pink-600
		'#0891b2', // cyan-600
		'#4f46e5', // indigo-600
		'#65a30d', // lime-600
		'#0d9488', // teal-600
		'#c026d3', // fuchsia-600
		'#0284c7', // sky-600
		'#047857', // emerald-700
		'#6d28d9' // violet-700
	];
	function getNextPriceLevelColor() {
		const used = basePriceLevels.map((p) => (p.color || '').toLowerCase());
		for (const color of PRESET_COLORS) {
			if (!used.includes(color.toLowerCase())) return color;
		}
		return PRESET_COLORS[basePriceLevels.length % PRESET_COLORS.length];
	}

	function drawStagePath(ow: number, oh: number) {
		const curveStart = Math.max(ow * 0.6, ow - 60);
		return `M 0 0 L ${curveStart} 0 Q ${2 * ow - curveStart} ${oh / 2} ${curveStart} ${oh} L 0 ${oh} Z`;
	}

	let unassignedCount = $derived.by(() => {
		let total = 0;
		let assigned = 0;
		if (manifestDetail?.rsAreas) {
			manifestDetail.rsAreas.forEach((sec: any) => {
				sec.rows?.forEach((r: any) => {
					total += r.seats?.length || 0;
					r.seats?.forEach((s: any) => {
						const plId = seatPriceLevels[s.id] || s.priceLevelId || 'unassigned';
						if (plId !== 'unassigned') assigned++;
					});
				});
			});
		}
		if (manifestDetail?.gaAreas) {
			manifestDetail.gaAreas.forEach((ga: any) => {
				total += ga.capacity || 0;
				const plId = seatPriceLevels[ga.id] || ga.priceLevelId || 'unassigned';
				if (plId !== 'unassigned') assigned += (ga.capacity || 0);
			});
		}
		return Math.max(0, total - assigned);
	});

	let priceLevels = $derived(
		basePriceLevels.map((bpl) => {
			let count = 0;
			if (manifestDetail?.rsAreas) {
				manifestDetail.rsAreas.forEach((sec: any) => {
					sec.rows?.forEach((r: any) => {
						r.seats?.forEach((s: any) => {
							const plId = seatPriceLevels[s.id] || s.priceLevelId || 'unassigned';
							if (plId === bpl.id) count++;
						});
					});
				});
			}
			if (manifestDetail?.gaAreas) {
				manifestDetail.gaAreas.forEach((ga: any) => {
					const plId = seatPriceLevels[ga.id] || ga.priceLevelId || 'unassigned';
					if (plId === bpl.id) count += (ga.capacity || 0);
				});
			}
			return { ...bpl, count };
		})
	);

	function zoomIn() {
		canvasZoom = Math.min(3, canvasZoom + 0.25);
	}
	function zoomOut() {
		canvasZoom = Math.max(0.25, canvasZoom - 0.25);
	}
	function fitToView() {
		canvasZoom = 1;
	}

	function formatCurrency(amount: number) {
		if (amount === null || amount === undefined) return '';
		return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
	}

	function formatDateTime(isoString: string) {
		if (!isoString) return 'TBA';
		try {
			return new Intl.DateTimeFormat('en-US', {
				month: 'short',
				day: 'numeric',
				hour: '2-digit',
				minute: '2-digit'
			}).format(new Date(isoString));
		} catch (e) {
			return 'Invalid Date';
		}
	}

	function openAddOfferModal() {
		editingOffer = {
			name: '',
			description: '',
			ticketTypeId: ticketTypes.length > 0 ? ticketTypes[0].id : '',
			saleWindows: [{ type: 'GENERAL_SALE', startAt: '', endAt: '' }],
			currency: 'VND',
			faceValue: 200000,
			capacityCap: 300,
			eventTicketMinimum: 1,
			seatingMode: 'GA',
			priceLevelId: 'P1',
			restrictedPayment: false,
			active: true
		};
		isAddOfferModalOpen = true;
	}

	function openUpdateOfferModal(offer: any) {
		editingOffer = { ...offer };
		isAddOfferModalOpen = true;
	}

	function handleSaveOffer(e: Event) {
		e.preventDefault();
		if (!editingOffer?.name?.trim()) return;

		if (editingOffer.id) {
			localOffers = localOffers.map((o) => (o.id === editingOffer.id ? { ...editingOffer } : o));
		} else {
			const mockId = `offer-${Date.now()}`;
			localOffers = [
				...localOffers,
				{
					...editingOffer,
					id: mockId,
					quantitySold: 0
				}
			];
		}
		isAddOfferModalOpen = false;
	}

	function handleAddHold(e: Event) {
		e.preventDefault();
		if (!newHoldName.trim()) return;
		holds = [
			...holds,
			{
				id: `hold-${Date.now()}`,
				name: newHoldName,
				count: newHoldCount,
				reason: newHoldReason,
				status: newHoldType
			}
		];
		newHoldName = '';
		newHoldCount = 15;
		newHoldReason = '';
		newHoldType = 'LOCKED';
		isAddHoldModalOpen = false;
	}

	async function handleSaveAll() {
		saving = true;
		saveSuccess = false;
		saveError = '';

		try {
			// 1. Save General tab - event info
			const eventPayload = {
				venueId,
				title: editTitle,
				slug: event.slug || '',
				startAt: new Date(startAt).toISOString(),
				timezone: event.timezone || 'Asia/Ho_Chi_Minh',
				restrictSingleSeat: false,
				safeTixEnabled: false,
				transferEnabled: true,
				maxTransferCount: 5,
				serviceFeePercent: 0,
				classificationIds: classificationId ? [classificationId] : [],
				attractionIds: selectedAttractionIds
			};
			const eventRes = await fetch(`/api/partner/events/${event.id}`, {
				method: 'PUT',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(eventPayload)
			});
			if (!eventRes.ok) {
				const err = await eventRes.json().catch(() => ({}));
				throw new Error(err.message || 'Failed to update event');
			}

			// 2. Sync offers (pricing tiers)
			const originalOffers: any[] = data.offers || [];
			const currentOfferIds = new Set(localOffers.map((o) => o.id));
			const originalOfferIds = new Set(originalOffers.map((o) => o.id));

			// Delete removed offers
			for (const offer of originalOffers) {
				if (!currentOfferIds.has(offer.id)) {
					const delRes = await fetch(`/api/partner/events/${event.id}/offers/${offer.id}`, {
						method: 'DELETE'
					});
					if (!delRes.ok && delRes.status !== 404) {
						const err = await delRes.json().catch(() => ({}));
						throw new Error(err.message || `Failed to delete offer "${offer.name}"`);
					}
				}
			}

			// Create new or update existing offers
			for (const offer of localOffers) {
				const isNew = !originalOfferIds.has(offer.id);
				const sellableQuantities = offer.sellableQuantitiesStr
					? String(offer.sellableQuantitiesStr)
							.split(',')
							.map((s: string) => parseInt(s.trim()))
							.filter((n: number) => !isNaN(n))
					: offer.sellableQuantities || [1];
				const seatingMode =
					offer.seatingMode === 'RESERVED' ? 'RESERVED_SEATING' : 'GENERAL_ADMISSION';

				const payload: any = {
					code:
						offer.code ||
						offer.name
							?.toUpperCase()
							.replace(/[^A-Z0-9_]/g, '_')
							.slice(0, 20) ||
						'OFFER',
					name: offer.name,
					description: offer.description || '',
					currency: offer.currency || 'VND',
					faceValue: offer.faceValue ?? offer.price ?? 0,
					restrictedPayment: offer.restrictedPayment || false,
					eventTicketMinimum: offer.eventTicketMinimum || 1,
					capacityCap: offer.capacityCap ?? 9999,
					sellableQuantities: sellableQuantities.length > 0 ? sellableQuantities : [1],
					seatingMode
				};
				if (offer.saleWindows && offer.saleWindows.length > 0) {
					payload.saleWindows = offer.saleWindows.map((sw: any) => ({
						type: sw.type || 'GENERAL_SALE',
						startAt: sw.startAt,
						endAt: sw.endAt,
						accessCode: sw.accessCode || null
					}));
				}
				if (seatingMode === 'RESERVED_SEATING') {
					payload.sectionId = offer.sectionId || null;
					payload.priceLevelId = offer.priceLevelId || null;
				}
				if (offer.charges) payload.charges = offer.charges;

				let res: Response;
				if (isNew) {
					const ticketTypeId =
						offer.ticketTypeId || (ticketTypes.length > 0 ? ticketTypes[0].id : null);
					if (!ticketTypeId)
						throw new Error(
							`Offer "${offer.name}" requires a ticket type. Create one first, then save again.`
						);
					payload.ticketTypeId = ticketTypeId;
					res = await fetch(`/api/partner/events/${event.id}/offers`, {
						method: 'POST',
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify(payload)
					});
				} else {
					res = await fetch(`/api/partner/events/${event.id}/offers/${offer.id}`, {
						method: 'PUT',
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify(payload)
					});
				}
				if (!res.ok) {
					const err = await res.json().catch(() => ({}));
					throw new Error(err.message || `Failed to save offer "${offer.name}"`);
				}
				// Update localOffers with real ID if new
				if (isNew) {
					const saved = await res.json();
					const newOffer = saved.data || saved;
					localOffers = localOffers.map((o) => (o.id === offer.id ? { ...o, id: newOffer.id } : o));
				}
			}

			saveSuccess = true;
			// Reset local offers so next $effect reloads from server
			localOffers = [];
			localOffersInitialized = false;
		} catch (err: any) {
			saveError = err.message || 'Failed to save changes';
		} finally {
			saving = false;
			setTimeout(() => {
				saveSuccess = false;
				saveError = '';
			}, 5000);
		}
	}
</script>

<svelte:head>
	<title>{event.title} — Event Workspace</title>
</svelte:head>

<div class="page">
	<div class="mb-6 flex items-center justify-between border-b border-slate-200">
		<nav class="-mb-px flex space-x-6" aria-label="Main sub-navigation">
			<button
				type="button"
				onclick={() => (activeTab = 'general')}
				class="border-b-2 px-1 py-3 text-sm font-semibold transition-all duration-150 focus:outline-none {activeTab ===
				'general'
					? 'border-[#026CDF] font-extrabold text-[#026CDF]'
					: 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'}"
			>
				General
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'seats')}
				class="border-b-2 px-1 py-3 text-sm font-semibold transition-all duration-150 focus:outline-none {activeTab ===
				'seats'
					? 'border-[#026CDF] font-extrabold text-[#026CDF]'
					: 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'}"
			>
				Seat Map
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'offers')}
				class="border-b-2 px-1 py-3 text-sm font-semibold transition-all duration-150 focus:outline-none {activeTab ===
				'offers'
					? 'border-[#026CDF] font-extrabold text-[#026CDF]'
					: 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'}"
			>
				Offers
			</button>
			<button
				type="button"
				onclick={() => (activeTab = 'holds')}
				class="border-b-2 px-1 py-3 text-sm font-semibold transition-all duration-150 focus:outline-none {activeTab ===
				'holds'
					? 'border-[#026CDF] font-extrabold text-[#026CDF]'
					: 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'}"
			>
				Holds & Kills
			</button>
		</nav>
		<div class="flex items-center gap-3">
			{#if saveSuccess}
				<span class="text-sm font-semibold text-emerald-600">All changes saved!</span>
			{/if}
			{#if saveError}
				<span class="text-sm font-semibold text-red-600">{saveError}</span>
			{/if}
			<button
				type="button"
				onclick={handleSaveAll}
				disabled={saving}
				class="flex cursor-pointer items-center justify-center gap-2 rounded border-0 bg-[#026CDF] px-6 py-2.5 text-sm font-bold text-white shadow-sm transition outline-none hover:bg-blue-700 disabled:opacity-50"
			>
				{#if saving}
					<span
						class="h-3.5 w-3.5 animate-spin rounded-none border-2 border-white border-t-transparent"
					></span>
				{/if}
				<span>{saving ? 'Saving...' : 'Save'}</span>
			</button>
		</div>
	</div>

	<div class="content">
		{#if activeTab === 'general'}
			<div class="card">
				<div class="form">
					<div class="field">
						<label for="edit-title" class="label">Event Title <span class="required">*</span></label
						>
						<input type="text" id="edit-title" required bind:value={editTitle} class="input" />
					</div>
					<div class="field">
						<label for="edit-category" class="label">Classification</label>
						<Combobox
							items={sortedClassifications}
							bind:value={classificationId}
							placeholder="Select a Category"
							searchPlaceholder="Search classification..."
							displayFn={(c) => {
								if (!c) return '';
								if (c.parentId) {
									const parent = sortedClassifications.find((p: any) => p.id === c.parentId);
									return parent ? `${parent.name} > ${c.name}` : c.name;
								}
								return c.name;
							}}
						>
							{#snippet itemSnippet(item)}
								<div class="flex w-full items-center">
									<span class={item?.parentId ? 'text-slate-600' : 'font-semibold text-slate-900'}>
										{#if item?.parentId}
											{@const parent = sortedClassifications.find(
												(p: any) => p.id === item.parentId
											)}
											{parent ? `${parent.name} > ${item.name}` : item.name}
										{:else}
											{item?.name}
										{/if}
									</span>
								</div>
							{/snippet}
						</Combobox>
					</div>
					<div class="field">
						<label for="edit-attraction" class="label">Attraction</label>
						<Combobox
							items={attractions}
							bind:values={selectedAttractionIds}
							multiple={true}
							placeholder="Select an Attraction"
							searchPlaceholder="Search attraction..."
						>
							{#snippet itemSnippet(item)}
								<div class="flex items-center gap-2">
									<img
										src={item?.thumbnailUrl ||
											item?.imageUrl ||
											`https://ui-avatars.com/api/?name=${encodeURIComponent(item?.name || 'Unknown')}&background=random`}
										alt={item?.name || ''}
										class="h-6 w-6 rounded-full object-cover"
									/>
									<span>{item?.name || ''}</span>
								</div>
							{/snippet}
						</Combobox>
					</div>
					<div class="field">
						<label for="edit-venue" class="label">Venue</label>
						<Combobox
							items={data.venues || []}
							bind:value={venueId}
							placeholder="Select a Venue"
							searchPlaceholder="Search venue..."
							displayFn={(v) => v?.name || ''}
							searchFn={(items, query) => {
								const q = cleanVietnamese(query).toLowerCase();
								return items.filter(
									(v: any) =>
										!q ||
										cleanVietnamese(v.name || '')
											.toLowerCase()
											.includes(q)
								);
							}}
						>
							{#snippet itemSnippet(item)}
								<div class="flex items-center gap-2">
									<img
										src={item?.thumbnailUrl ||
											`https://ui-avatars.com/api/?name=${encodeURIComponent(item?.name || 'Unknown')}&background=random`}
										alt={item?.name || ''}
										class="h-6 w-6 rounded-full object-cover"
									/>
									<span>{item?.name || ''}</span>
								</div>
							{/snippet}
						</Combobox>
					</div>
					<div class="grid grid-cols-3 gap-4">
						<div class="field">
							<label for="edit-start" class="label">Date<span class="required">*</span></label>
							<DateTimePicker
								name="startDate"
								bind:value={startDate}
								required={true}
								placeholder="Select date"
							/>
						</div>
						<div class="field">
							<label for="edit-time" class="label">Time<span class="required">*</span></label>
							<TimePicker
								name="startTime"
								bind:value={startTime}
								required={true}
								placeholder="Select time"
							/>
						</div>
						<div class="field">
							<label for="edit-tz" class="label">Timezone</label>
							<input
								type="text"
								id="edit-tz"
								value={event.timezone || 'Asia/Ho_Chi_Minh'}
								readonly
								class="input input-readonly"
							/>
						</div>
					</div>
				</div>
			</div>
		{:else if activeTab === 'seats' || activeTab === 'holds'}
			<div class="venue-bar">
				<label for="seat-manifest" class="venue-label">Manifest</label>
				<select
					id="seat-manifest"
					name="manifestId"
					bind:value={selectedManifestId}
					onchange={handleManifestChange}
					class="flex h-10 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm placeholder:text-slate-400 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 focus:outline-none disabled:cursor-not-allowed disabled:opacity-50"
				>
					{#each data.manifests as manifest (manifest.id)}
						<option value={manifest.id}>
							{manifest.description || manifest.id}
						</option>
					{/each}
				</select>
				<div class="venue-actions">
					<span class="manifest-badge">{venueName || 'No manifest'}</span>
				</div>
			</div>

			<div
				class="relative flex h-[calc(100vh-160px)] min-h-[500px] flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"
			>
				<div class="relative flex min-h-0 flex-1">
					<aside class="flex w-[280px] shrink-0 flex-col border-r border-slate-200 bg-white">
						{#if activeTab === 'seats'}
							<div class="flex items-center justify-between border-b border-slate-200 px-4 py-2.5">
								<h3 class="text-[10px] font-black tracking-widest text-slate-400 uppercase">
									Price Levels
								</h3>
								<button
									type="button"
									onclick={() =>
										basePriceLevels.push({
											id: `PL-${Date.now()}`,
											color: getNextPriceLevelColor(),
											label: 'New Level'
										})}
									class="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
									aria-label="Add price level"
								>
									<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
										><path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2.5"
											d="M12 4v16m8-8H4"
										/></svg
									>
								</button>
							</div>
							<div class="flex-1 overflow-y-auto bg-slate-50">
								<div class="flex items-center justify-between border-b border-[#F1F5F9] bg-[#F8FAFC] px-4 py-3">
									<div class="flex items-center gap-3">
										<span class="h-[14px] w-[14px] shrink-0 rounded-full bg-[#E2E8F0]"></span>
										<span class="text-[14px] font-medium text-[#64748B]">Unassigned</span>
									</div>
									<span class="text-[14px] font-semibold text-[#475569]">{unassignedCount}</span>
								</div>
								{#each basePriceLevels as bpl, i (bpl.id)}
									{@const pl = priceLevels[i]}
									<div
										class="flex cursor-pointer flex-col gap-2.5 border-b bg-white px-4 py-3 transition-colors hover:bg-slate-50 border-[#F1F5F9]"
										role="button"
										tabindex="0"
										ondragover={(e) => {
											e.preventDefault();
											if (e.dataTransfer) e.dataTransfer.dropEffect = 'move';
										}}
										ondrop={async (e) => {
											e.preventDefault();
											const data = e.dataTransfer?.getData('text/plain');
											if (data === 'selected_seats' && selectedSeatIds.length > 0) {
												const updated = { ...seatPriceLevels };
												selectedSeatIds.forEach((id) => {
													updated[id] = bpl.id;
												});
												seatPriceLevels = updated;
												selectedSeatIds = []; // clear selection after assign
												await tick();
												konvaCanvasRef?.redraw?.();
											}
										}}
									>
										<div class="flex items-center justify-between">
											<div class="flex items-center gap-3">
												<label
													class="relative flex h-[14px] w-[14px] shrink-0 cursor-pointer items-center justify-center overflow-hidden rounded-full"
													style="background:{bpl.color}"
												>
													<input
														type="color"
														bind:value={bpl.color}
														onchange={async () => {
															await tick();
															konvaCanvasRef?.redraw?.();
														}}
														class="absolute -inset-2 h-8 w-8 cursor-pointer opacity-0"
													/>
												</label>
												<input
													type="text"
													bind:value={bpl.label}
													placeholder="Price Level"
													class="-ml-1.5 w-[150px] rounded border border-transparent bg-transparent px-1.5 py-0.5 text-[14px] font-medium text-[#475569] transition-colors outline-none hover:border-slate-200 focus:border-[#3B82F6]"
												/>
											</div>
											<span class="ml-2 shrink-0 text-[14px] font-medium text-[#475569]">{pl.count.toLocaleString()}</span>
										</div>

										<div class="flex items-center justify-between">
											<div class="flex items-center gap-3">
												<span class="text-[11px] font-bold text-[#94A3B8] uppercase">BASE</span>
												<input
													type="number"
													step="1000"
													min="0"
													bind:value={bpl.price}
													class="-ml-1.5 w-[100px] rounded border border-transparent bg-transparent px-1.5 py-0.5 text-[14px] font-medium text-[#475569] transition-colors outline-none hover:border-slate-200 focus:border-[#3B82F6]"
													placeholder="0"
												/>
											</div>
											<button
												type="button"
												onclick={() => {
													basePriceLevels = basePriceLevels.filter((p) => p.id !== bpl.id);
												}}
												class="flex h-6 w-6 items-center justify-center rounded text-slate-300 transition-colors hover:bg-slate-100 hover:text-red-500"
												aria-label="Remove {bpl.label}"
											>
												<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
													<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
												</svg>
											</button>
										</div>
									</div>
								{/each}
							</div>
						{/if}

						{#if activeTab === 'holds'}
							{#snippet holdItem(hold: any)}
								<div
									class="group flex items-center gap-2 rounded-lg px-2.5 py-2 transition-colors hover:bg-slate-50"
									ondragover={(e) => {
										e.preventDefault();
										if (e.dataTransfer) e.dataTransfer.dropEffect = 'move';
									}}
									ondrop={(e) => {
										e.preventDefault();
										const data = e.dataTransfer?.getData('text/plain');
										if (data === 'selected_seats' && selectedSeatIds.length > 0) {
											const updatedStatuses = { ...seatStatuses };
											const updatedHoldIds = { ...seatHoldIds };
											selectedSeatIds.forEach((id) => {
												updatedStatuses[id] = hold.status === 'LOCKED' ? 'Held' : 'Killed';
												updatedHoldIds[id] = hold.id;
											});
											seatStatuses = updatedStatuses;
											seatHoldIds = updatedHoldIds;
											selectedSeatIds = [];
										}
									}}
								>
									<button
										type="button"
										class="flex h-5 w-5 shrink-0 items-center justify-center rounded-full hover:bg-slate-200"
										onclick={(e) => {
											e.stopPropagation();
											selectedSeatIds = seats
												.filter((s) => seatHoldIds[s.id] === hold.id)
												.map((s) => s.id);
										}}
										aria-label="Select seats in {hold.name}"
									>
										<span
											class="block h-3 w-3 rounded-full {hold.status === 'LOCKED'
												? 'bg-amber-500'
												: 'bg-red-500'}"
										></span>
									</button>
									<div class="flex flex-1 items-center justify-between gap-2">
										<input
											type="text"
											bind:value={hold.name}
											class="w-full min-w-0 border-none bg-transparent p-0 text-sm font-medium text-slate-500 outline-none hover:text-slate-700 focus:text-slate-900 focus:ring-0"
										/>
										<div class="text-sm text-slate-500">
											<span
												>{hold.count === 0
													? 0
													: hold.count +
														seats.filter((s) => seatHoldIds[s.id] === hold.id).length}</span
											>
										</div>
									</div>
									<button
										type="button"
										onclick={() => {
											holds = holds.filter((h) => h.id !== hold.id);
										}}
										class="hidden h-5 w-5 items-center justify-center rounded text-slate-400 group-hover:flex hover:bg-slate-200 hover:text-red-500"
										aria-label="Remove {hold.name}"
									>
										<svg class="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"
											><path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M6 18L18 6M6 6l12 12"
											/></svg
										>
									</button>
								</div>
							{/snippet}

							<div class="flex items-center justify-between border-b border-slate-200 px-4 py-2.5">
								<h3 class="text-[10px] font-black tracking-widest text-slate-400 uppercase">
									Holds & Kills
								</h3>
							</div>
							<div class="flex-1 overflow-y-auto px-3 py-2">
								<div class="flex items-center justify-between px-2.5 py-1.5">
									<div class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">
										Holds
									</div>
									<button
										type="button"
										onclick={() =>
											holds.push({
												id: `hold_${Date.now()}`,
												name: 'New Hold',
												status: 'LOCKED',
												count: 0,
												reason: ''
											})}
										class="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
										aria-label="Add hold"
									>
										<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
											><path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2.5"
												d="M12 4v16m8-8H4"
											/></svg
										>
									</button>
								</div>
								<div class="mb-2 space-y-px">
									{#each holds.filter((h) => h.status === 'LOCKED') as hold (hold.id)}
										{@render holdItem(hold)}
									{/each}
								</div>

								<div class="mt-2 flex items-center justify-between px-2.5 py-1.5">
									<div class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">
										Kills
									</div>
									<button
										type="button"
										onclick={() =>
											holds.push({
												id: `kill_${Date.now()}`,
												name: 'New Kill',
												status: 'KILLED',
												count: 0,
												reason: ''
											})}
										class="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
										aria-label="Add kill"
									>
										<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
											><path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2.5"
												d="M12 4v16m8-8H4"
											/></svg
										>
									</button>
								</div>
								<div class="space-y-px">
									{#each holds.filter((h) => h.status === 'KILLED') as hold (hold.id)}
										{@render holdItem(hold)}
									{/each}
								</div>
							</div>
						{/if}
						<div class="border-t border-slate-200 bg-slate-50/50 px-4 py-3">
							<h4 class="mb-2 text-[9px] font-black tracking-widest text-slate-400 uppercase">
								Financial Information
							</h4>
							<div class="space-y-1.5 text-[11px]">
								<div class="flex items-center justify-between">
									<span class="font-medium text-slate-500">Total Capacity</span>
									<span class="font-bold text-slate-800">{totalSeats}</span>
								</div>
								<div class="flex items-center justify-between">
									<span class="font-medium text-slate-500">Sold</span>
									<span class="font-bold text-emerald-700">{totalSold}</span>
								</div>
								<div class="flex items-center justify-between">
									<span class="font-medium text-slate-500">Reserved / Held</span>
									<span class="font-bold text-amber-600">{totalReserved}</span>
								</div>
								<div class="flex items-center justify-between">
									<span class="font-medium text-slate-500">Killed</span>
									<span class="font-bold text-slate-800">{totalKilled}</span>
								</div>
							</div>
						</div>
					</aside>

					<main class="relative flex flex-1 flex-col overflow-hidden bg-[#FAFAFA] select-none">
						<div
							class="relative flex w-full flex-1 items-center justify-center overflow-hidden"
							role="application"
							aria-label="Seat map canvas"
						>
							<KonvaSeatingCanvas
								bind:this={konvaCanvasRef}
								{manifestDetail}
								{seats}
								bind:selectedSeatIds
								{activeTab}
								seatPriceLevels={activeTab === 'seats' ? seatPriceLevels : undefined}
								basePriceLevels={priceLevels}
								{activeCanvasTool}
								{selectionTool}
								bind:viewport={canvasViewport}
								bind:bounds={canvasBounds}
							/>
						</div>

						<div
							class="absolute right-4 bottom-4 z-30 flex flex-col items-center gap-0.5 rounded-xl border border-slate-200/80 bg-white/90 p-0.5 shadow-lg backdrop-blur-md"
						>
							<button
								type="button"
								onclick={() => konvaCanvasRef?.zoomIn?.()}
								class="flex h-7 w-7 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100"
								title="Zoom In"
							>
								<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
									><path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2.5"
										d="M12 4v16m8-8H4"
									/></svg
								>
							</button>
							<button
								type="button"
								onclick={() => konvaCanvasRef?.zoomOut?.()}
								class="flex h-7 w-7 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100"
								title="Zoom Out"
							>
								<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
									><path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2.5"
										d="M20 12H4"
									/></svg
								>
							</button>
							<div class="h-px w-4 bg-slate-200"></div>
							<button
								type="button"
								onclick={() => konvaCanvasRef?.fitToView?.()}
								class="flex h-7 w-7 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100"
								title="Fit to View"
							>
								<svg class="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"
									><path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4"
									/></svg
								>
							</button>
						</div>

						<!-- Mini-map -->
						<div
							class="absolute right-16 bottom-4 z-30 flex h-32 w-44 flex-col overflow-hidden rounded-xl border border-slate-200/80 bg-white/90 p-2 shadow-lg backdrop-blur-md"
						>
							<div
								class="flex items-center justify-between pb-1 text-[8px] font-bold tracking-wider text-slate-400 uppercase"
							>
								<span>Map</span>
								<span class="rounded bg-slate-100 px-1 py-0.5 font-mono text-[7px] text-slate-500"
									>{Math.round(canvasZoom * 100)}%</span
								>
							</div>
							<div
								class="relative w-full flex-1 overflow-hidden rounded-lg border border-slate-200/60 bg-slate-50"
							>
								<svg viewBox="{canvasBounds.minX} {canvasBounds.minY} {canvasBounds.width} {canvasBounds.height}" class="pointer-events-none h-full w-full">
									{#each manifestDetail?.rsAreas || [] as section}
										{#if section.polygon?.length >= 6}
											<polygon
												points={section.polygon.map((p: any) => p.x != null ? `${p.x},${p.y}` : `${p[0]},${p[1]}`).join(' ')}
												fill="#FFFFFF"
												stroke="#CBD5E1"
												stroke-width="1"
												vector-effect="non-scaling-stroke"
											/>
										{:else if section.x != null}
											<rect
												x={section.x}
												y={section.y}
												width={section.width}
												height={section.height}
												fill="#FFFFFF"
												stroke="#CBD5E1"
												stroke-width="1"
												vector-effect="non-scaling-stroke"
											/>
										{/if}
									{/each}
									{#each manifestDetail?.gaAreas || [] as ga}
										{#if ga.polygon?.length >= 6}
											<polygon
												points={ga.polygon.map((p: any) => p.x != null ? `${p.x},${p.y}` : `${p[0]},${p[1]}`).join(' ')}
												fill="none"
												stroke="#334155"
												stroke-opacity="0.35"
												stroke-width="0.5"
												vector-effect="non-scaling-stroke"
											/>
										{:else}
											<rect
												x={ga.x ?? 50}
												y={ga.y ?? 50}
												width={ga.width ?? 200}
												height={ga.height ?? 100}
												fill="none"
												stroke="#334155"
												stroke-opacity="0.35"
												stroke-width="0.5"
												rx="3"
												vector-effect="non-scaling-stroke"
											/>
										{/if}
									{/each}
									{#each manifestDetail?.objects || [] as obj}
										{#if obj.type === 'stage'}
											{@const sx = obj.x ?? 100}{@const sy = obj.y ?? 100}{@const sw = obj.width ?? 200}{@const sh = obj.height ?? 100}{@const scs = Math.max(sw * 0.6, sw - 60)}
											<path
												d={`M ${sx} ${sy} L ${sx + scs} ${sy} Q ${sx + 2 * sw - scs} ${sy + sh / 2} ${sx + scs} ${sy + sh} L ${sx} ${sy + sh} Z`}
												fill="#334155"
												stroke="#64748B"
												stroke-width="1.5"
											/>
										{:else}
											<rect
												x={obj.x ?? 100}
												y={obj.y ?? 100}
												width={obj.width ?? 200}
												height={obj.height ?? 100}
												fill={obj.color || '#94A3B8'}
												fill-opacity="0.25"
												stroke={obj.color || '#64748B'}
												stroke-opacity="0.7"
												stroke-width="1.5"
												rx="3"
											/>
										{/if}
									{/each}
									<rect
										x={canvasViewport.x}
										y={canvasViewport.y}
										width={canvasViewport.w}
										height={canvasViewport.h}
										fill="rgba(239,68,68,0.06)"
										stroke="#EF4444"
										stroke-width="5"
										stroke-dasharray="8 6"
										rx="3"
									/>
								</svg>
							</div>
						</div>

						{#if selectedSeatDetails.length > 0}
							<div
								class="absolute top-3 left-3 z-30 min-w-48 cursor-grab rounded-xl bg-white shadow-xl active:cursor-grabbing"
								draggable="true"
								ondragstart={(e) => {
									e.dataTransfer?.setData('text/plain', 'selected_seats');
									if (e.dataTransfer) e.dataTransfer.effectAllowed = 'copyMove';
								}}
								role="region"
								aria-label="Selected seats"
							>
								<!-- Header -->
								<div class="flex items-center gap-4 border-b border-slate-100 p-3 px-4">
									<div
										class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-blue-600 text-white shadow-[inset_0_-2px_4px_rgba(0,0,0,0.2)]"
									>
										<IconArmchair class="h-6 w-6" />
									</div>
									<div class="flex flex-1 items-center justify-between gap-4">
										<span class="text-xl font-semibold text-slate-600">{totalSelectedCount}</span>
									<button
										class="text-sm font-medium text-blue-600 hover:text-blue-800"
										type="button"
										onclick={async () => {
											selectedSeatIds = [];
											await tick();
											konvaCanvasRef?.redraw?.();
										}}
									>
										Clear
									</button>
									</div>
								</div>

								<!-- Details -->
								<div class="flex flex-col gap-3 p-3 px-4 pt-3">
									{#each selectedSeatsByPriceLevel as item (item.pl?.id)}
										<div class="flex items-center justify-between">
											<div class="flex items-center gap-2">
												<div
													class="flex h-4 w-4 items-center justify-center rounded-full text-white"
													style="background-color: {item.pl?.color || '#CBD5E1'};"
												>
												</div>
												<span class="text-sm font-medium text-slate-600">{item.pl?.label || 'Unassigned'}</span>
											</div>
											<span class="font-semibold text-slate-600">{item.count}</span>
										</div>
									{/each}
								</div>
							</div>
						{/if}
					</main>

					<aside
						class="z-10 flex w-[56px] shrink-0 flex-col items-center gap-0.5 border-l border-slate-200 bg-white py-3 shadow-sm"
					>
						<button
							onclick={() => {
								activeCanvasTool = 'select';
								selectionTool = 'seat';
							}}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold transition-colors {activeCanvasTool ===
								'select' && selectionTool === 'seat'
								? 'bg-slate-100 text-slate-900'
								: 'text-slate-400 hover:bg-slate-50 hover:text-slate-700'}"
						>
							<IconPointer2 size={16} stroke={2} />
							Seat
						</button>
						<button
							type="button"
							onclick={() => (selectionTool = 'row')}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold transition-colors {activeCanvasTool ===
								'select' && selectionTool === 'row'
								? 'bg-slate-100 text-slate-900'
								: 'text-slate-400 hover:bg-slate-50 hover:text-slate-700'}"
						>
							<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="1.5"
									d="M4 6h16M4 12h16M4 18h16"
								/></svg
							>
							Row
						</button>
						<button
							type="button"
							onclick={() => (selectionTool = 'section')}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold transition-colors {activeCanvasTool ===
								'select' && selectionTool === 'section'
								? 'bg-slate-100 text-slate-900'
								: 'text-slate-400 hover:bg-slate-50 hover:text-slate-700'}"
						>
							<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="1.5"
									d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"
								/></svg
							>
							Section
						</button>
						<button
							onclick={() => {
								activeCanvasTool = 'pan';
							}}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold transition-colors {activeCanvasTool ===
							'pan'
								? 'bg-slate-100 text-slate-900'
								: 'text-slate-400 hover:bg-slate-50 hover:text-slate-700'}"
						>
							<IconHandGrab size={16} stroke={2} />
							Pan
						</button>
						<div class="my-1.5 h-px w-8 bg-slate-100"></div>
						<button
							onclick={() => (showFilterDialog = !showFilterDialog)}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold text-slate-400 transition-colors hover:bg-slate-50 hover:text-slate-700"
						>
							<IconFilter size={16} stroke={2} />
							Filters
						</button>
						<button
							onclick={() => (showAssistantDialog = !showAssistantDialog)}
							class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold text-slate-400 transition-colors hover:bg-slate-50 hover:text-slate-700"
						>
							<IconWand size={16} stroke={2} />
							Assist
						</button>
					</aside>
				</div>
			</div>
		{:else if activeTab === 'offers'}
			<div
				class="flex flex-col gap-6"
				onclick={() => (isCreateMenuOpen = false)}
				role="presentation"
			>
				<div class="flex items-center justify-between">
					<div>
						<h2 class="text-2xl font-bold tracking-tight text-slate-900">Ticketing & Offers</h2>
						<p class="mt-1 text-sm text-slate-500">
							Manage your ticket types and pricing strategies
						</p>
					</div>

					<!-- prominent [+ Create] dropdown button (Top Right) -->
					<div class="relative" onclick={(e) => e.stopPropagation()} role="presentation">
						<button
							type="button"
							onclick={() => (isCreateMenuOpen = !isCreateMenuOpen)}
							class="flex cursor-pointer items-center justify-center gap-1.5 rounded-none bg-[#026CDF] px-5 py-2.5 text-xs font-bold text-white shadow-none transition hover:bg-blue-700 focus:outline-none"
						>
							<span>Create</span>
							<IconChevronDown
								size={12}
								class="transition-transform duration-150 {isCreateMenuOpen ? 'rotate-180' : ''}"
							/>
						</button>

						<!-- Floating Create Dropdown -->
						{#if isCreateMenuOpen}
							<button
								type="button"
								class="fixed inset-0 z-40 cursor-default bg-transparent"
								onclick={() => (isCreateMenuOpen = false)}
								aria-label="Close creation dropdown"
							></button>
							<div
								class="absolute right-0 z-50 mt-1.5 w-44 rounded-none border border-slate-200 bg-white p-1.5 shadow-xl"
							>
								<button
									type="button"
									onclick={() => {
										isCreateMenuOpen = false;
										isTicketTypeModalOpen = true;
									}}
									class="flex w-full cursor-pointer items-center gap-2 rounded-none px-3 py-2 text-left text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
								>
									<IconTicket size={14} class="text-slate-400" />
									<span>Ticket Type</span>
								</button>
								<button
									type="button"
									onclick={() => {
										isCreateMenuOpen = false;
										openAddOfferModal();
									}}
									class="flex w-full cursor-pointer items-center gap-2 rounded-none px-3 py-2 text-left text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
								>
									<IconFolderPlus size={14} class="text-slate-400" />
									<span>Pricing Tier</span>
								</button>
							</div>
						{/if}
					</div>
				</div>

				<div class="mt-2 flex flex-col gap-6">
					<!-- Top Area: Ticket Types (Compact Badges) -->
					<div class="flex flex-wrap items-center gap-2">
						{#each ticketTypes as tt}
							<div
								class="group flex items-center gap-1.5 rounded bg-slate-100 py-1 pr-2 pl-2.5 transition-colors hover:bg-slate-200"
							>
								<span class="text-[13px] text-slate-700">{tt.name}</span>
								<button
									type="button"
									onclick={() => deleteTicketType(tt.code)}
									class="text-slate-400 transition-colors hover:text-slate-600 focus:outline-none"
									title="Delete"
								>
									<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
										><path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="1.5"
											d="M6 18L18 6M6 6l12 12"
										/></svg
									>
								</button>
							</div>
						{:else}
							<span class="text-sm text-slate-500 italic"
								>No ticket types defined. Click "Create" to add one.</span
							>
						{/each}
					</div>

					<!-- Main Area: Pricing Tiers -->
					<div class="w-full space-y-4">
						<div class="flex items-center justify-between">
							<h3 class="text-sm font-bold tracking-wider text-slate-500 uppercase">
								Pricing Tiers
							</h3>
						</div>

						<div class="overflow-hidden rounded-none border border-slate-200 bg-white shadow-sm">
							<table class="min-w-full divide-y divide-slate-100 text-left text-sm">
								<thead class="bg-slate-50/50">
									<tr>
										<th scope="col" class="px-6 py-4 font-semibold text-slate-900">Offer Name</th>
										<th scope="col" class="px-6 py-4 font-semibold text-slate-900">Ticket Type</th>
										<th scope="col" class="px-6 py-4 font-semibold text-slate-900">Price (VND)</th>
										<th scope="col" class="px-6 py-4 font-semibold text-slate-900">Sale Window</th>
										<th scope="col" class="px-6 py-4 font-semibold text-slate-900">Status</th>
									</tr>
								</thead>
								<tbody class="divide-y divide-slate-100 bg-white">
									{#each localOffers as offer (offer.id)}
										<tr class="group transition-colors duration-200 hover:bg-slate-50/80">
											<td class="px-6 py-4 font-bold whitespace-nowrap text-slate-900">
												<button
													type="button"
													class="hover:text-blue-600 hover:underline"
													onclick={() => openUpdateOfferModal(offer)}
												>
													{offer.name}
												</button>
											</td>
											<td class="px-6 py-4 text-sm font-medium whitespace-nowrap text-slate-700">
												<span
													class="inline-flex rounded-md bg-slate-100/80 px-2 py-1 text-xs font-semibold text-slate-700 ring-1 ring-slate-200/50 ring-inset"
													>{getTicketTypeName(offer.ticketTypeId)}</span
												>
											</td>
											<td
												class="px-6 py-4 font-black tracking-tight whitespace-nowrap text-slate-900"
												>{formatCurrency(offer.faceValue ?? offer.price)}</td
											>
											<td class="px-6 py-4 text-sm whitespace-nowrap text-slate-600">
												{#if offer.saleWindows && offer.saleWindows.length > 0}
													<div class="flex flex-col gap-1.5">
														{#each offer.saleWindows as sw}
															<div class="flex items-center gap-2">
																<span
																	class="inline-flex items-center rounded bg-slate-100 px-1.5 py-0.5 text-[10px] font-bold tracking-wider text-slate-500 uppercase"
																	>{sw.type === 'PRESALE' ? 'Pre' : 'Gen'}</span
																>
																<span class="text-xs font-medium text-slate-700"
																	>{formatDateTime(sw.startAt)} - {formatDateTime(sw.endAt)}</span
																>
															</div>
														{/each}
													</div>
												{:else}
													<span
														class="inline-flex items-center rounded-md bg-amber-50 px-2 py-1 text-[10px] font-semibold text-amber-700 ring-1 ring-amber-600/20 ring-inset"
														>Needs schedule</span
													>
												{/if}
											</td>
											<td class="px-6 py-4 whitespace-nowrap">
												{#if offer.active ?? true}
													<span
														class="inline-flex items-center rounded-full bg-emerald-50 px-2.5 py-1 text-[11px] font-semibold text-emerald-600 ring-1 ring-emerald-500/20 ring-inset"
														>Active</span
													>
												{:else}
													<span
														class="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-1 text-[11px] font-medium text-slate-500 ring-1 ring-slate-500/20 ring-inset"
														>Inactive</span
													>
												{/if}
											</td>
										</tr>
									{:else}
										<tr>
											<td colspan="5" class="px-6 py-16 text-center">
												<div
													class="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-slate-50 text-slate-400 ring-1 ring-slate-100"
												>
													<IconPlus size={24} />
												</div>
												<h3 class="text-sm font-bold text-slate-900">No offers yet</h3>
												<p class="mt-1 text-sm text-slate-500">
													Create your first pricing tier to start selling tickets.
												</p>
											</td>
										</tr>
									{/each}
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</div>
		{/if}
	</div>
</div>

{#if isTicketTypeModalOpen}
	<div class="fixed inset-0 z-50 flex items-center justify-center p-4">
		<div
			class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity"
			onclick={() => (isTicketTypeModalOpen = false)}
			aria-hidden="true"
			role="presentation"
		></div>
		<div
			class="animate-in fade-in zoom-in-95 relative w-full max-w-md overflow-hidden rounded-none bg-white shadow-2xl ring-1 ring-slate-200 duration-200"
		>
			<div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
				<h3 class="text-lg font-bold tracking-tight text-slate-900">New Ticket Type</h3>
				<button
					type="button"
					onclick={() => (isTicketTypeModalOpen = false)}
					class="rounded-full p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600"
				>
					<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M6 18L18 6M6 6l12 12"
						/></svg
					>
				</button>
			</div>

			<div class="px-6 py-6">
				<div class="space-y-5">
					<div>
						<label class="mb-1.5 block text-sm font-semibold text-slate-700"
							>Code <span class="text-red-500">*</span></label
						>
						<select
							bind:value={newTicketType.code}
							onchange={() => {
								if (!newTicketType.name && newTicketType.code) {
									newTicketType.name =
										newTicketType.code.charAt(0) + newTicketType.code.slice(1).toLowerCase();
								}
							}}
							class="block w-full rounded-lg border-0 px-3.5 py-2.5 text-slate-900 shadow-sm ring-1 ring-slate-300 ring-inset focus:ring-2 focus:ring-indigo-600 focus:ring-inset sm:text-sm"
						>
							<option value="">Select Code</option>
							<option value="ADULT">ADULT</option>
							<option value="CHILD">CHILD</option>
							<option value="STUDENT">STUDENT</option>
							<option value="COMP">COMP</option>
							<option value="VIP">VIP</option>
							<option value="GROUP">GROUP</option>
						</select>
						<p class="mt-1.5 text-[11px] text-slate-500">
							System identifier for integration and reporting.
						</p>
					</div>
					<div>
						<label class="mb-1.5 block text-sm font-semibold text-slate-700"
							>Name <span class="text-red-500">*</span></label
						>
						<input
							type="text"
							bind:value={newTicketType.name}
							placeholder="e.g. Adult Ticket"
							class="block w-full rounded-lg border-0 px-3.5 py-2.5 text-slate-900 shadow-sm ring-1 ring-slate-300 ring-inset focus:ring-2 focus:ring-indigo-600 focus:ring-inset sm:text-sm"
						/>
						<p class="mt-1.5 text-[11px] text-slate-500">Display name shown to buyers.</p>
					</div>
				</div>
				<div class="mt-8 flex justify-end gap-3">
					<button
						type="button"
						onclick={() => (isTicketTypeModalOpen = false)}
						class="rounded-lg px-4 py-2.5 text-sm font-semibold text-slate-600 transition-colors hover:bg-slate-100"
					>
						Cancel
					</button>
					<button
						type="button"
						disabled={!newTicketType.name || !newTicketType.code || savingTicketType}
						onclick={async () => {
							await createTicketType();
							isTicketTypeModalOpen = false;
						}}
						class="rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-all hover:bg-indigo-500 hover:shadow-md disabled:opacity-50"
					>
						{savingTicketType ? 'Saving...' : 'Create Ticket Type'}
					</button>
				</div>
			</div>
		</div>
	</div>
{/if}

{#if isAddOfferModalOpen}
	<div
		class="fixed inset-0 z-50 flex justify-end bg-slate-900/40 backdrop-blur-sm transition-all"
		role="dialog"
	>
		<div
			class="absolute inset-0"
			onclick={() => (isAddOfferModalOpen = false)}
			role="button"
			aria-label="Close"
		></div>

		<div
			class="relative flex h-full w-full max-w-xl flex-col bg-white shadow-2xl ring-1 ring-slate-200"
		>
			<!-- Header -->
			<div
				class="flex shrink-0 items-center justify-between border-b border-slate-100 bg-white px-6 py-4"
			>
				<h3 class="text-lg font-bold tracking-tight text-slate-900">
					{editingOffer?.id ? 'Update Pricing Tier' : 'Add New Pricing Tier'}
				</h3>
				<button
					type="button"
					onclick={() => (isAddOfferModalOpen = false)}
					class="rounded-none p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600"
				>
					<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M6 18L18 6M6 6l12 12"
						/></svg
					>
				</button>
			</div>

			<!-- Form Content -->
			<form onsubmit={handleSaveOffer} class="flex flex-1 flex-col overflow-hidden">
				<div class="flex-1 space-y-6 overflow-y-auto p-6">
					<div class="grid grid-cols-1 gap-4">
						<div>
							<label for="offer-name" class="mb-1.5 block text-sm font-semibold text-slate-700"
								>Offer Name <span class="text-red-500">*</span></label
							>
							<input
								type="text"
								id="offer-name"
								bind:value={editingOffer.name}
								required
								placeholder="e.g. VIP Early Bird"
								class="block w-full rounded-md border-0 px-3.5 py-2 text-slate-900 shadow-sm ring-1 ring-slate-300 ring-inset placeholder:text-slate-400 focus:ring-2 focus:ring-[#026CDF] focus:ring-inset sm:text-sm"
							/>
						</div>
					</div>

					<hr class="border-slate-100" />

					<div class="grid grid-cols-2 gap-4">
						<div>
							<label for="ticket-type" class="mb-1.5 block text-sm font-semibold text-slate-700"
								>Ticket Type <span class="text-red-500">*</span></label
							>
							<select
								id="ticket-type"
								bind:value={editingOffer.ticketTypeId}
								required
								class="block w-full rounded-md border-0 px-3.5 py-2 text-slate-900 shadow-sm ring-1 ring-slate-300 ring-inset focus:ring-2 focus:ring-[#026CDF] focus:ring-inset sm:text-sm"
							>
								{#each ticketTypes as tt}
									<option value={tt.id}>{tt.name}</option>
								{/each}
								{#if ticketTypes.length === 0}
									<option value="" disabled>Please create a Ticket Type first</option>
								{/if}
							</select>
						</div>
						<div>
							<label for="price-level" class="mb-1.5 block text-sm font-semibold text-slate-700"
								>Price Level</label
							>
							<select
								id="price-level"
								bind:value={editingOffer.priceLevelId}
								class="block w-full rounded-md border-0 px-3.5 py-2 text-slate-900 shadow-sm ring-1 ring-slate-300 ring-inset focus:ring-2 focus:ring-[#026CDF] focus:ring-inset sm:text-sm"
							>
								<option value="">None (Applies to all)</option>
								{#each basePriceLevels as pl}
									<option value={pl.id}>{pl.label}</option>
								{/each}
							</select>
						</div>
					</div>

					<div class="grid grid-cols-2 gap-4">
						<div>
							<label for="offer-price" class="mb-1.5 block text-sm font-semibold text-slate-700"
								>Price (VND) <span class="text-red-500">*</span></label
							>
							<input
								type="number"
								id="offer-price"
								bind:value={editingOffer.faceValue}
								required
								min="0"
								class="block w-full rounded-md border-0 px-3.5 py-2 text-slate-900 shadow-sm ring-1 ring-slate-300 ring-inset focus:ring-2 focus:ring-[#026CDF] focus:ring-inset sm:text-sm"
							/>
						</div>
						<div>
							<label for="offer-limit" class="mb-1.5 block text-sm font-semibold text-slate-700"
								>Capacity Cap <span class="text-red-500">*</span></label
							>
							<input
								type="number"
								id="offer-limit"
								bind:value={editingOffer.capacityCap}
								required
								min="1"
								class="block w-full rounded-md border-0 px-3.5 py-2 text-slate-900 shadow-sm ring-1 ring-slate-300 ring-inset focus:ring-2 focus:ring-[#026CDF] focus:ring-inset sm:text-sm"
							/>
						</div>
					</div>

					<div class="grid grid-cols-2 gap-4">
						<div>
							<label
								for="offer-quantities"
								class="mb-1.5 block text-sm font-semibold text-slate-700">Allowed Quantities</label
							>
							<input
								type="text"
								id="offer-quantities"
								bind:value={editingOffer.sellableQuantitiesStr}
								placeholder="e.g. 1, 2, 3, 4"
								class="block w-full rounded-md border-0 px-3.5 py-2 text-slate-900 shadow-sm ring-1 ring-slate-300 ring-inset focus:ring-2 focus:ring-[#026CDF] focus:ring-inset sm:text-sm"
							/>
						</div>
						<div>
							<label for="offer-mode" class="mb-1.5 block text-sm font-semibold text-slate-700"
								>Seating Mode</label
							>
							<select
								id="offer-mode"
								bind:value={editingOffer.seatingMode}
								class="block w-full rounded-md border-0 px-3.5 py-2 text-slate-900 shadow-sm ring-1 ring-slate-300 ring-inset focus:ring-2 focus:ring-[#026CDF] focus:ring-inset sm:text-sm"
							>
								<option value="GA">General Admission</option>
								<option value="RESERVED">Reserved Seating</option>
							</select>
						</div>
					</div>

					<hr class="border-slate-100" />

					<div class="rounded-xl border border-slate-200 bg-slate-50 p-4">
						<h4 class="mb-3 text-sm font-bold text-slate-900">Sale Window</h4>
						<div class="space-y-4">
							{#if editingOffer.saleWindows && editingOffer.saleWindows.length > 0}
								{#each editingOffer.saleWindows as window, i}
									<div class="relative rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
										{#if editingOffer.saleWindows.length > 1}
											<button
												type="button"
												onclick={() =>
													(editingOffer.saleWindows = editingOffer.saleWindows.filter(
														(_w: any, idx: number) => idx !== i
													))}
												class="absolute top-2 right-2 text-slate-400 hover:text-red-600"
												title="Remove Window"
											>
												<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
													><path
														stroke-linecap="round"
														stroke-linejoin="round"
														stroke-width="2"
														d="M6 18L18 6M6 6l12 12"
													/></svg
												>
											</button>
										{/if}
										<div class="mb-3">
											<label class="mb-1.5 block text-xs font-semibold text-slate-700"
												>Window Type</label
											>
											<select
												bind:value={window.type}
												class="block w-full rounded-md border-0 px-3 py-2 text-slate-900 shadow-sm ring-1 ring-slate-300 ring-inset focus:ring-2 focus:ring-[#026CDF] focus:ring-inset sm:text-sm"
											>
												<option value="PRESALE">Pre-Sale</option>
												<option value="GENERAL_SALE">General Sale</option>
											</select>
										</div>
										<div class="grid grid-cols-2 gap-3">
											<div>
												<label
													class="mb-1 block text-[10px] font-semibold tracking-wider text-slate-500 uppercase"
													>Start Time</label
												>
												<DateTimePicker
													name={'saleWindowStartAt_' + i}
													bind:value={window.startAt}
													placeholder="Select date & time"
												/>
											</div>
											<div>
												<label
													class="mb-1 block text-[10px] font-semibold tracking-wider text-slate-500 uppercase"
													>End Time</label
												>
												<DateTimePicker
													name={'saleWindowEndAt_' + i}
													bind:value={window.endAt}
													placeholder="Select date & time"
												/>
											</div>
										</div>
										{#if window.type === 'PRESALE'}
											<div class="mt-3">
												<label class="mb-1.5 block text-xs font-semibold text-slate-700"
													>Access Code (Optional)</label
												>
												<input
													type="text"
													bind:value={window.accessCode}
													placeholder="e.g. EARLYBIRD2026"
													class="block w-full rounded-md border-0 px-3 py-2 text-slate-900 shadow-sm ring-1 ring-slate-300 ring-inset focus:ring-2 focus:ring-[#026CDF] focus:ring-inset sm:text-sm"
												/>
											</div>
										{/if}
									</div>
								{/each}
								<button
									type="button"
									class="mt-2 text-sm font-semibold text-[#026CDF] hover:text-blue-700"
									onclick={() =>
										(editingOffer.saleWindows = [
											...editingOffer.saleWindows,
											{ type: 'PRESALE', startAt: '', endAt: '' }
										])}
								>
									+ Add Another Window
								</button>
							{:else}
								<button
									type="button"
									class="text-sm font-semibold text-[#026CDF] hover:text-blue-700"
									onclick={() =>
										(editingOffer.saleWindows = [{ type: 'GENERAL_SALE', startAt: '', endAt: '' }])}
								>
									+ Add Sale Window
								</button>
							{/if}
						</div>
					</div>
				</div>

				<!-- Footer / Actions -->
				<div
					class="flex shrink-0 items-center justify-between border-t border-slate-100 bg-white px-6 py-4"
				>
					<button
						type="button"
						onclick={() => (isAddOfferModalOpen = false)}
						class="rounded-none px-4 py-2.5 text-sm font-semibold text-slate-600 transition-colors hover:bg-slate-100"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="rounded-none bg-[#026CDF] px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-all hover:bg-blue-700 hover:shadow-md"
					>
						{editingOffer?.id ? 'Save Changes' : 'Add Tier'}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

{#if isAddHoldModalOpen}
	<div class="modal-overlay" role="dialog">
		<div class="modal">
			<div class="modal-header">
				<h3 class="modal-title">Allocate Locked Seats Block</h3>
				<button type="button" onclick={() => (isAddHoldModalOpen = false)} class="modal-close"
					>&times;</button
				>
			</div>
			<form onsubmit={handleAddHold} class="modal-form">
				<div class="field">
					<label for="hold-name" class="label">Block Name <span class="required">*</span></label>
					<input
						type="text"
						id="hold-name"
						bind:value={newHoldName}
						required
						placeholder="e.g. VIP Sponsor Allocation"
						class="input"
					/>
				</div>
				<div class="field-row">
					<div class="field">
						<label for="hold-cnt" class="label">Seat Qty <span class="required">*</span></label>
						<input
							type="number"
							id="hold-cnt"
							bind:value={newHoldCount}
							required
							min="1"
							class="input"
						/>
					</div>
					<div class="field">
						<label for="hold-type" class="label">Lock Type <span class="required">*</span></label>
						<select id="hold-type" bind:value={newHoldType} class="input">
							<option value="LOCKED">Held (Locked)</option>
							<option value="KILLED">Killed (Obstruction)</option>
						</select>
					</div>
				</div>
				<div class="field">
					<label for="hold-reason" class="label">Reason</label>
					<input
						type="text"
						id="hold-reason"
						bind:value={newHoldReason}
						placeholder="e.g. Partner VIP list block allocation"
						class="input"
					/>
				</div>
				<div class="modal-actions">
					<button type="button" onclick={() => (isAddHoldModalOpen = false)} class="btn-ghost"
						>Cancel</button
					>
					<button type="submit" class="btn-primary">Allocate Block</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<style>
	:root {
		--bg: #ffffff;
		--surface: #ffffff;
		--fg: #171717;
		--fg-2: #4d4d4d;
		--muted: #666666;
		--meta: #808080;
		--border: rgba(0, 0, 0, 0.08);
		--border-soft: rgba(0, 0, 0, 0.04);
		--accent: #0070f3;
		--accent-on: #ffffff;
		--accent-hover: #005bb5;
		--font-display: 'Geist', 'Geist Sans', -apple-system, 'Segoe UI', Arial, sans-serif;
		--font-body: 'Geist', 'Geist Sans', -apple-system, 'Segoe UI', Arial, sans-serif;
		--font-mono:
			'Geist Mono', ui-monospace, 'SF Mono', 'Roboto Mono', Menlo, Monaco, 'Liberation Mono',
			'DejaVu Sans Mono', 'Courier New', monospace;
		--text-xs: 12px;
		--text-sm: 14px;
		--text-base: 16px;
		--text-lg: 20px;
		--text-xl: 24px;
		--text-2xl: 32px;
		--text-3xl: 40px;
		--text-4xl: 48px;
		--leading-body: 1.5;
		--leading-tight: 1.1;
		--tracking-display: -0.05em;
		--radius-sm: 6px;
		--radius-md: 8px;
		--radius-lg: 12px;
		--radius-pill: 9999px;
		--elev-flat: none;
		--elev-ring: 0 0 0 1px rgba(0, 0, 0, 0.08);
		--elev-raised:
			0 0 0 1px rgba(0, 0, 0, 0.08), 0 2px 2px rgba(0, 0, 0, 0.04),
			0 8px 8px -8px rgba(0, 0, 0, 0.04), 0 0 0 1px #fafafa;
		--focus-ring: 0 0 0 2px var(--accent);
		--motion-fast: 150ms;
		--motion-base: 200ms;
		--ease-standard: cubic-bezier(0.2, 0, 0, 1);
		--container-max: 1200px;
		--container-gutter: 24px;
	}

	.page {
		max-width: var(--container-max);
		margin: 0 auto;
		padding: 32px var(--container-gutter);
		display: flex;
		flex-direction: column;
		gap: var(--space-6, 24px);
	}

	.badge {
		display: inline-flex;
		align-items: center;
		padding: 2px 10px;
		border-radius: var(--radius-pill);
		font-size: 11px;
		font-weight: 500;
		background: #ebebeb;
		color: var(--fg);
	}
	.badge-green {
		background: #ecfdf5;
		color: #059669;
	}
	.badge-red {
		background: #fef2f2;
		color: #dc2626;
	}
	.badge-amber {
		background: #fffbeb;
		color: #d97706;
	}
	.btn-primary {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		padding: 8px 18px;
		border-radius: var(--radius-sm);
		background: var(--fg);
		color: #ffffff;
		font-size: var(--text-sm);
		font-weight: 500;
		font-family: var(--font-body);
		text-decoration: none;
		border: none;
		cursor: pointer;
		transition: opacity var(--motion-fast) var(--ease-standard);
		white-space: nowrap;
	}
	.btn-primary:hover {
		opacity: 0.85;
	}
	.btn-primary:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	.btn-secondary {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		padding: 7px 16px;
		border-radius: var(--radius-sm);
		background: var(--bg);
		box-shadow: var(--elev-ring);
		color: var(--fg);
		font-size: var(--text-sm);
		font-weight: 500;
		font-family: var(--font-body);
		border: none;
		cursor: pointer;
		transition: background var(--motion-fast) var(--ease-standard);
	}
	.btn-secondary:hover {
		background: #f5f5f5;
	}

	.btn-ghost {
		display: inline-flex;
		align-items: center;
		padding: 8px 20px;
		border-radius: var(--radius-sm);
		background: transparent;
		box-shadow: var(--elev-ring);
		color: var(--fg-2);
		font-size: var(--text-sm);
		font-weight: 500;
		font-family: var(--font-body);
		border: none;
		cursor: pointer;
		transition: background var(--motion-fast) var(--ease-standard);
	}
	.btn-ghost:hover {
		background: #f5f5f5;
	}

	.spinner {
		display: inline-block;
		width: 14px;
		height: 14px;
		border: 2px solid rgba(255, 255, 255, 0.3);
		border-top-color: #fff;
		border-radius: 50%;
		animation: spin 0.5s linear infinite;
	}
	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}

	.alert {
		padding: 10px 14px;
		border-radius: var(--radius-sm);
		font-size: var(--text-sm);
		font-weight: 500;
		box-shadow: var(--elev-ring);
	}
	.alert-error {
		background: #fef2f2;
		color: #dc2626;
	}
	.alert-success {
		background: #ecfdf5;
		color: #059669;
	}

	.tabs {
		box-shadow: 0 1px 0 0 var(--border);
	}
	.tab-nav {
		display: flex;
		overflow: hidden;
		flex-wrap: wrap;
	}
	.tab-btn {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		padding: 10px 16px;
		margin-right: 2px;
		font-size: var(--text-sm);
		font-weight: 500;
		color: var(--muted);
		background: transparent;
		border: none;
		box-shadow: 0 2px 0 0 transparent;
		cursor: pointer;
		transition:
			color var(--motion-fast) var(--ease-standard),
			box-shadow var(--motion-fast) var(--ease-standard);
		white-space: nowrap;
		font-family: var(--font-body);
	}
	.tab-btn:hover {
		color: var(--fg);
	}
	.tab-btn.active {
		color: #2563eb;
		box-shadow: 0 2px 0 0 #2563eb;
	}

	.content {
		display: flex;
		flex-direction: column;
		gap: 24px;
	}

	.card {
		background: var(--surface);
		border-radius: var(--radius-md);
		padding: 24px;
		box-shadow: var(--elev-ring);
	}
	.card-title {
		font-size: var(--text-base);
		font-weight: 600;
		color: var(--fg);
		margin: 0 0 20px;
		letter-spacing: -0.02em;
		font-family: var(--font-display);
	}

	.form {
		display: flex;
		flex-direction: column;
		gap: 16px;
	}
	.field {
		display: flex;
		flex-direction: column;
		gap: 6px;
	}
	.field-row {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 16px;
	}

	@media (max-width: 640px) {
		.field-row {
			grid-template-columns: 1fr;
		}
	}
	.label {
		font-size: var(--text-xs);
		font-weight: 600;
		color: var(--fg);
	}
	.required {
		color: #dc2626;
	}
	.input {
		width: 100%;
		padding: 8px 12px;
		border-radius: var(--radius-sm);
		background: var(--bg);
		box-shadow: 0 0 0 1px var(--border);
		font-size: var(--text-sm);
		color: var(--fg);
		outline: none;
		transition: box-shadow var(--motion-fast) var(--ease-standard);
		box-sizing: border-box;
		font-family: var(--font-body);
		border: none;
	}
	.input:focus {
		box-shadow: 0 0 0 2px var(--accent);
	}
	.input-readonly {
		background: #f5f5f5;
		color: var(--muted);
		cursor: not-allowed;
	}
	.dropdown-trigger {
		display: flex !important;
		align-items: center;
		justify-content: space-between;
		gap: 8px;
		cursor: pointer;
		text-align: left;
		font-family: var(--font-body);
	}
	.dropdown-trigger .placeholder {
		color: var(--muted);
		font-weight: 400;
	}
	.chevron {
		flex-shrink: 0;
		color: var(--meta);
		transition: transform var(--motion-fast) var(--ease-standard);
	}
	.chevron.rotated {
		transform: rotate(180deg);
	}
	.dropdown-backdrop {
		position: fixed;
		inset: 0;
		z-index: 45;
		background: transparent;
		border: none;
		padding: 0;
		cursor: default;
	}
	.dropdown-panel {
		position: absolute;
		left: 0;
		right: 0;
		z-index: 50;
		margin-top: 4px;
		border-radius: var(--radius-md);
		background: #fff;
		box-shadow: var(--elev-raised);
		overflow: hidden;
	}
	.dropdown-search {
		display: flex;
		align-items: center;
		gap: 8px;
		padding: 8px 10px;
		border-bottom: 1px solid var(--border);
	}
	.search-icon {
		flex-shrink: 0;
		color: var(--meta);
	}
	.search-input {
		flex: 1;
		border: none;
		outline: none;
		font-size: var(--text-sm);
		color: var(--fg);
		background: transparent;
		font-family: var(--font-body);
	}
	.search-input::placeholder {
		color: var(--meta);
	}
	.dropdown-list {
		max-height: 192px;
		overflow-y: auto;
		padding: 4px;
	}
	.dropdown-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		width: 100%;
		padding: 8px 10px;
		border-radius: var(--radius-sm);
		border: none;
		background: transparent;
		font-size: var(--text-sm);
		color: var(--fg);
		cursor: pointer;
		text-align: left;
		font-family: var(--font-body);
		transition: background var(--motion-fast) var(--ease-standard);
	}
	.dropdown-item:hover {
		background: #f5f5f5;
	}
	.dropdown-item.selected {
		background: #f0f0f0;
	}
	.check-icon {
		flex-shrink: 0;
		color: #2563eb;
	}
	.dropdown-empty {
		padding: 24px;
		text-align: center;
		font-size: var(--text-sm);
		color: var(--meta);
	}
	.chip-wrap {
		display: flex;
		flex-wrap: wrap;
		gap: 4px;
		flex: 1;
		min-width: 0;
	}
	.chip {
		display: inline-flex;
		align-items: center;
		padding: 2px 8px;
		font-size: var(--text-xs);
		font-weight: 500;
		color: var(--fg);
		background: var(--surface);
		border-radius: var(--radius-sm);
		box-shadow: 0 0 0 1px var(--border);
		white-space: nowrap;
	}
	.venue-item {
		display: flex;
		flex-direction: column;
		gap: 1px;
	}
	.venue-name {
		font-weight: 500;
	}
	.venue-loc {
		font-size: 11px;
		color: var(--meta);
	}
	.form-actions {
		display: flex;
		justify-content: flex-end;
		padding-top: 12px;
		box-shadow: 0 -1px 0 0 var(--border);
	}

	.section-header {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16px;
	}
	.section-desc {
		font-size: var(--text-sm);
		color: var(--muted);
		margin: 4px 0 0;
	}

	.offer-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
		gap: 16px;
	}
	.offer-grid > .card {
		transition: box-shadow var(--motion-base) var(--ease-standard);
	}
	.offer-grid > .card:hover {
		box-shadow: var(--elev-raised);
	}
	.offer-top {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		gap: 12px;
	}
	.offer-name {
		font-size: var(--text-base);
		font-weight: 600;
		color: var(--fg);
		margin: 0;
	}
	.offer-right {
		text-align: right;
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: 4px;
	}
	.offer-price {
		font-size: var(--text-xl);
		font-weight: 600;
		color: var(--accent);
		font-variant-numeric: tabular-nums;
	}

	.progress-section {
		margin-top: 16px;
	}
	.progress-label {
		display: flex;
		justify-content: space-between;
		font-size: var(--text-xs);
		color: var(--muted);
		margin-bottom: 6px;
	}
	.progress-bar {
		height: 6px;
		border-radius: var(--radius-pill);
		background: #ebebeb;
		overflow: hidden;
	}
	.progress-fill {
		height: 100%;
		border-radius: var(--radius-pill);
		background: var(--accent);
		transition: width var(--motion-base) var(--ease-standard);
	}

	.empty-state {
		grid-column: 1 / -1;
		padding: 40px;
		text-align: center;
		font-size: var(--text-sm);
		color: var(--muted);
		box-shadow: 0 0 0 1px var(--border-soft);
		border-radius: var(--radius-md);
		background: var(--surface);
	}

	.venue-bar {
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 12px 16px;
		border-radius: var(--radius-md);
		background: var(--surface);
		box-shadow: var(--elev-ring);
	}
	.venue-label {
		font-size: var(--text-xs);
		font-weight: 600;
		color: var(--fg);
		white-space: nowrap;
	}
	.venue-select {
		max-width: 320px;
	}
	.manifest-badge {
		padding: 2px 10px;
		border-radius: var(--radius-pill);
		background: #ebf5ff;
		color: #0068d6;
		font-size: 10px;
		font-weight: 600;
		margin-left: auto;
	}

	.table-wrap {
		border-radius: var(--radius-md);
		overflow: hidden;
		background: var(--surface);
		box-shadow: var(--elev-ring);
	}
	.table {
		width: 100%;
		border-collapse: collapse;
		font-size: var(--text-sm);
	}
	.table th {
		padding: 12px 16px;
		text-align: left;
		font-size: 10px;
		font-weight: 600;
		color: var(--muted);
		text-transform: uppercase;
		letter-spacing: 0.04em;
		background: #f5f5f5;
		box-shadow: 0 1px 0 0 var(--border);
	}
	.th-right {
		text-align: right;
	}
	.table td {
		padding: 12px 16px;
		box-shadow: 0 1px 0 0 var(--border-soft);
		color: var(--fg-2);
	}
	.table tr:last-child td {
		box-shadow: none;
	}
	.td-name {
		font-weight: 600;
		color: var(--fg);
	}
	.td-muted {
		color: var(--muted);
	}
	.td-actions {
		text-align: right;
	}
	.link-btn {
		background: none;
		border: none;
		font-size: var(--text-xs);
		font-weight: 600;
		color: var(--accent);
		cursor: pointer;
		padding: 0;
		font-family: var(--font-body);
		transition: opacity var(--motion-fast) var(--ease-standard);
	}
	.link-btn:hover {
		opacity: 0.8;
	}
	.empty-cell {
		padding: 40px !important;
		text-align: center;
		font-style: italic;
		color: var(--muted);
	}

	.modal-overlay {
		position: fixed;
		inset: 0;
		z-index: 50;
		display: flex;
		align-items: center;
		justify-content: center;
		background: rgba(0, 0, 0, 0.4);
	}
	.modal {
		width: 100%;
		max-width: 460px;
		padding: 24px;
		border-radius: var(--radius-md);
		background: var(--surface);
		box-shadow: var(--elev-raised);
	}
	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20px;
		padding-bottom: 12px;
		box-shadow: 0 1px 0 0 var(--border);
	}
	.modal-title {
		font-size: var(--text-base);
		font-weight: 600;
		color: var(--fg);
		margin: 0;
	}
	.modal-close {
		background: none;
		border: none;
		font-size: 20px;
		color: var(--muted);
		cursor: pointer;
		padding: 0;
		line-height: 1;
	}
	.modal-close:hover {
		color: var(--fg);
	}
	.modal-form {
		display: flex;
		flex-direction: column;
		gap: 16px;
	}
	.modal-actions {
		display: flex;
		justify-content: flex-end;
		gap: 8px;
		padding-top: 12px;
		box-shadow: 0 -1px 0 0 var(--border);
	}
	.checkbox-label {
		display: flex;
		align-items: center;
		gap: 8px;
		font-size: var(--text-sm);
		color: var(--fg-2);
		cursor: pointer;
	}
	.checkbox {
		width: 16px;
		height: 16px;
		accent-color: var(--accent);
	}
</style>
