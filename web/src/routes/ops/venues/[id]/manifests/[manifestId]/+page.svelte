<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any, svelte/no-navigation-without-resolve, svelte/require-each-key */
	import { onMount, onDestroy } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { IconPointer2 } from '@tabler/icons-svelte';

	const pointer2Cursor = (() => {
		const svg = `<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'><path d='M0 0h24v24H0z' fill='none'/><path d='M14.185 13.14l5.644 -2.202c1.625 -.634 1.538 -2.962 -.13 -3.473l-14.319 -4.382c-1.41 -.431 -2.73 .888 -2.298 2.298l4.382 14.318c.51 1.668 2.84 1.755 3.473 .13l2.202 -5.644a1.84 1.84 0 0 1 1.045 -1.045' fill='none' stroke='#000' stroke-width='2'/></svg>`
		return `url("data:image/svg+xml,${encodeURIComponent(svg)}") 2 2, default`
	})()

	let { data } = $props<{ data: any }>();

	const venue = $derived(data.venue || {});

	let manifestIdVal = $state(data.manifest?.id || '');
	let manifestDescVal = $state(data.manifest?.description || '');
	let manifestCapVal = $state(data.manifest?.totalCapacity || 10000);

	let levels = $state<any[]>(data.levels || []);
	let sections = $state<any[]>(data.sections || []);
	let priceLevels = $state<any[]>(
		data.priceLevels?.length
			? data.priceLevels
			: [{ id: 'STANDARD', description: 'Standard Tier', color: '#3B82F6' }]
	);
	const PRESET_COLORS = [
		'#3b82f6',
		'#10b981',
		'#a855f7',
		'#ec4899',
		'#14b8a6',
		'#6366f1',
		'#84cc16',
		'#06b6d4'
	];
	function getNextPriceLevelColor() {
		const used = priceLevels.map((p) => (p.color || '').toLowerCase());
		for (const color of PRESET_COLORS) {
			if (!used.includes(color.toLowerCase())) return color;
		}
		return PRESET_COLORS[0];
	}
	let gaSections = $state<any[]>(data.gaSections || []);
	let rsSections = $state<any[]>(data.rsSections || []);
	let brushPriceLevelId = $state(data.priceLevels?.[0]?.id || 'STANDARD');
	let brushSectionId = $state('');

	let layoutObjects = $state<any[]>(data.manifest?.objects || []);
	let selectedObjectId = $state<number | null>(null);
	let selectedGaSectionId = $state<string>('');

	let activeMode = $state<'scaling' | 'inventory' | 'floor-edit'>('scaling');
	let selectionTool = $state<'seat' | 'row' | 'section'>('seat');
	let activeTool = $state<'select' | 'pan' | 'brush' | 'eraser'>('select');
	let previousToolBeforeSpace = $state<'select' | 'pan' | 'brush' | 'eraser'>('select');

	let showExportDropdown = $state(false);
	let showFilterDialog = $state(false);
	let showAssistantDialog = $state(false);
	let assistantPrompt = $state('');

	// onlyAvailable: filter by seat.status. onlyAccessible/onlyObstructed: UI-only,
	// these fields no longer exist in the API model so filters are intentional no-ops.
	let onlyAvailable = $state(false);
	let onlyAccessible = $state(false);
	let onlyObstructed = $state(false);

	let selectedSeatIds = $state<string[]>([]);
	let selectedRsSectionId = $state('');
	let isSaving = $state(false);
	let saveMessage = $state('');
	let errorMessage = $state('');

	// Price level base prices (editable)
	let priceLevelBasePrices = $state<Record<string, number>>({});
	$effect(() => {
		if (data.priceLevels?.length) {
			const initial: Record<string, number> = {};
			for (const pl of data.priceLevels) {
				const m = pl.description?.match(/[\d,.]+/);
				initial[pl.id] = m ? Number(m[0].replace(/,/g, '')) : 0;
			}
			priceLevelBasePrices = initial;
		}
	});

	let showRightPalette = $state(true);
	let snapGrid = $state(8);
	let blockRowPrefix = $state('A');
	let blockRowsCount = $state(5);
	let blockSeatsPerRow = $state(10);
	let blockSeatStartNum = $state(1);
	let blockShape = $state<'rectangle' | 'trapezoid' | 'diamond' | 'staggered'>('rectangle');

	let levelCode = $state('');
	let levelColor = $state('#3B82F6');
	let sectionCode = $state('');
	let sectionColor = $state('#10B981');

	let Konva: any;
	let stage = $state<any>();
	let layer = $state<any>();
	let stageContainer: HTMLDivElement | null = $state(null);

	let selectionRect: any;
	let x1 = 0,
		y1 = 0,
		x2 = 0,
		y2 = 0;
	let isShiftPressed = $state(false);

	let viewportX = $state(0);
	let viewportY = $state(0);
	let viewportW = $state(800);
	let viewportH = $state(600);
	let currentScale = $state(1);

	function getSeatRadius(_sc: number) {
		return 3; // 3 world-units
	}
	let fitScale = $state(1);

	function _showSeats(sc: number) {
		return sc >= fitScale * 1.5;
	}
	function _secProgress(sc: number) {
		const start = fitScale * 1.4,
			end = fitScale * 2.2;
		return Math.max(0, Math.min(1, (sc - start) / (end - start)));
	}

	const bounds = $derived.by(() => {
		let xs: number[] = [];
		let ys: number[] = [];
		rsSections.forEach((section) => {
			// include polygon points for proper bounding when seats are fan-shaped
			if (section.polygon?.length) {
				for (const [px, py] of section.polygon as [number, number][]) {
					xs.push(px);
					ys.push(py);
				}
			}
			(section.rows || []).forEach((row: any) => {
				(row.seats || []).forEach((seat: any) => {
					if (seat.positionX != null) xs.push(seat.positionX);
					if (seat.positionY != null) ys.push(seat.positionY);
				});
			});
		});
		gaSections.forEach((ga, idx) => {
			if (ga.polygon?.length) {
				for (const [px, py] of ga.polygon as [number, number][]) {
					xs.push(px);
					ys.push(py);
				}
			} else {
				const x = ga.x || 50 + idx * 220,
					y = ga.y || 50;
				xs.push(x, x + (ga.width || 200));
				ys.push(y, y + (ga.height || 100));
			}
		});
		layoutObjects.forEach((obj: any) => {
			if (obj.x != null) xs.push(obj.x, obj.x + (obj.width || 0));
			if (obj.y != null) ys.push(obj.y, obj.y + (obj.height || 0));
		});
		const minX = xs.length ? Math.min(...xs) - 80 : 0;
		const maxX = xs.length ? Math.max(...xs) + 80 : 800;
		const minY = ys.length ? Math.min(...ys) - 80 : 0;
		const maxY = ys.length ? Math.max(...ys) + 80 : 600;
		return { minX, maxX, minY, maxY, width: maxX - minX, height: maxY - minY };
	});

	const totalCapacity = $derived.by(() => {
		let total = 0;
		gaSections.forEach((ga) => (total += Number(ga.capacity || 0)));
		rsSections.forEach((section) => {
			(section.rows || []).forEach((row: any) => {
				total += (row.seats || []).length;
			});
		});
		return total;
	});

	const seatCountsByPriceLevel = $derived.by(() => {
		const counts: Record<string, number> = {};
		let unassigned = 0;
		rsSections.forEach((section) => {
			(section.rows || []).forEach((row: any) => {
				(row.seats || []).forEach((seat: any) => {
					if (seat.priceLevelId) counts[seat.priceLevelId] = (counts[seat.priceLevelId] || 0) + 1;
					else unassigned++;
				});
			});
		});
		return { counts, unassigned };
	});

	const financialInfo = $derived.by(() => {
		let totalSeats = 0;
		let totalRevenue = 0;
		for (const pl of priceLevels) {
			const count = seatCountsByPriceLevel.counts[pl.id] || 0;
			totalSeats += count;
			totalRevenue += count * (priceLevelBasePrices[pl.id] || 0);
		}
		const avg = totalSeats > 0 ? totalRevenue / totalSeats : 0;
		return { sellableCapacity: totalSeats, averagePerSeat: avg, potentialRevenue: totalRevenue };
	});

	// ── Convex Hull ───────────────────────────────────────────────────────────
	function _convexHull(pts: [number, number][]): [number, number][] {
		if (pts.length < 3) return pts;
		const sorted = [...pts].sort((a, b) => a[0] - b[0] || a[1] - b[1]);
		const cross = (o: [number, number], a: [number, number], b: [number, number]) =>
			(a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
		const lower: [number, number][] = [];
		for (const p of sorted) {
			while (lower.length >= 2 && cross(lower[lower.length - 2], lower[lower.length - 1], p) <= 0)
				lower.pop();
			lower.push(p);
		}
		const upper: [number, number][] = [];
		for (let i = sorted.length - 1; i >= 0; i--) {
			const p = sorted[i];
			while (upper.length >= 2 && cross(upper[upper.length - 2], upper[upper.length - 1], p) <= 0)
				upper.pop();
			upper.push(p);
		}
		upper.pop();
		lower.pop();
		return [...lower, ...upper];
	}

	function _hullPoints(rows: any[], ox: number, oy: number, pad = 14): number[] {
		const pts: [number, number][] = [];
		for (const row of rows)
			for (const seat of row.seats || [])
				pts.push([seat.positionX ?? 0, seat.positionY ?? row.positionY ?? 0]);
		if (pts.length === 0) return [];
		const hull = _convexHull(pts);
		const cx = hull.reduce((s, p) => s + p[0], 0) / hull.length;
		const cy = hull.reduce((s, p) => s + p[1], 0) / hull.length;
		const flat: number[] = [];
		for (const [hx, hy] of hull) {
			const dx = hx - cx,
				dy = hy - cy;
			const len = Math.sqrt(dx * dx + dy * dy) || 1;
			flat.push(hx + (dx / len) * pad - ox, hy + (dy / len) * pad - oy);
		}
		return flat;
	}

	onMount(() => {
		if (!browser) return;
		requestAnimationFrame(async () => {
			const konvaModule = await import('konva');
			Konva = konvaModule.default;
			const width = stageContainer?.offsetWidth || 900;
			const height = stageContainer?.offsetHeight || 600;
			stage = new Konva.Stage({ container: 'stage-container', width, height, draggable: false });
			layer = new Konva.Layer();
			stage.add(layer);
			selectionRect = new Konva.Rect({
				fill: 'rgba(59,130,246,0.1)',
				stroke: '#3b82f6',
				strokeWidth: 1.5,
				visible: false
			});
			layer.add(selectionRect);
			window.addEventListener('resize', handleResize);
			window.addEventListener('keydown', handleKeyDown);
			window.addEventListener('keyup', handleKeyUp);
			let _wheelTimer: ReturnType<typeof setTimeout> | null = null;
			stage.on('wheel', (e: any) => {
				e.evt.preventDefault();
				const oldScale = stage.scaleX();
				const pointer = stage.getPointerPosition();
				if (!pointer) return;
				const mp = { x: (pointer.x - stage.x()) / oldScale, y: (pointer.y - stage.y()) / oldScale };
				let newScale = e.evt.deltaY < 0 ? oldScale * 1.15 : oldScale / 1.15;
				newScale = Math.max(0.05, Math.min(newScale, 5));
				stage.scale({ x: newScale, y: newScale });
				stage.position({ x: pointer.x - mp.x * newScale, y: pointer.y - mp.y * newScale });
				stage.batchDraw();
				updateViewport();
				if (_wheelTimer) clearTimeout(_wheelTimer);
				_wheelTimer = setTimeout(() => drawSeatingMap(), 60);
			});
			setupCanvasEvents();
			fitToView();
			stage.on('dragmove dragend xChange yChange scaleXChange scaleYChange', updateViewport);
		});
	});

	function updateViewport() {
		if (!stage) return;
		const s = stage.scaleX() || 1;
		currentScale = s;
		viewportX = -stage.x() / s;
		viewportY = -stage.y() / s;
		viewportW = stage.width() / s;
		viewportH = stage.height() / s;
	}

	function fitToView() {
		if (!stage) return;
		const pad = 80;
		const sw = stage.width() - pad * 2,
			sh = stage.height() - pad * 2;
		const bw = bounds.width || 1,
			bh = bounds.height || 1;
		const scale = Math.min(sw / bw, sh / bh);
		fitScale = scale;
		stage.scale({ x: scale, y: scale });
		stage.x(pad + (sw - bw * scale) / 2 - bounds.minX * scale);
		stage.y(pad + (sh - bh * scale) / 2 - bounds.minY * scale);
		stage.batchDraw();
		updateViewport();
		drawSeatingMap();
	}

	function zoomIn() {
		if (!stage) return;
		const os = stage.scaleX(),
			ns = Math.min(os * 1.3, 5);
		const cx = stage.width() / 2,
			cy = stage.height() / 2;
		const mp = { x: (cx - stage.x()) / os, y: (cy - stage.y()) / os };
		stage.scale({ x: ns, y: ns });
		stage.x(cx - mp.x * ns);
		stage.y(cy - mp.y * ns);
		stage.batchDraw();
		updateViewport();
		drawSeatingMap();
	}

	function zoomOut() {
		if (!stage) return;
		const os = stage.scaleX(),
			ns = Math.max(os / 1.3, 0.1);
		const cx = stage.width() / 2,
			cy = stage.height() / 2;
		const mp = { x: (cx - stage.x()) / os, y: (cy - stage.y()) / os };
		stage.scale({ x: ns, y: ns });
		stage.x(cx - mp.x * ns);
		stage.y(cy - mp.y * ns);
		stage.batchDraw();
		updateViewport();
		drawSeatingMap();
	}

	function handleResize() {
		if (stage && stageContainer) {
			stage.width(stageContainer.offsetWidth);
			stage.height(stageContainer.offsetHeight);
			layer.batchDraw();
			updateViewport();
		}
	}

	function handleMinimapClick(e: MouseEvent) {
		const svg = e.currentTarget as SVGSVGElement;
		const rect = svg.getBoundingClientRect();
		const cx = bounds.minX + ((e.clientX - rect.left) / rect.width) * bounds.width;
		const cy = bounds.minY + ((e.clientY - rect.top) / rect.height) * bounds.height;
		if (!stage) return;
		const s = stage.scaleX() || 1;
		stage.x(-cx * s + stage.width() / 2);
		stage.y(-cy * s + stage.height() / 2);
		stage.batchDraw();
		updateViewport();
	}

	$effect(() => {
		if (!stage?.container()) return;
		stage.draggable(true);
		stage.container().style.cursor =
			activeTool === 'brush' ? 'copy' : activeTool === 'eraser' ? 'cell' : pointer2Cursor;
		drawSeatingMap();
	});

	$effect(() => {
		if (!brushPriceLevelId && priceLevels.length > 0) brushPriceLevelId = priceLevels[0].id;
	});

	$effect(() => {
		if (activeMode || onlyAvailable || onlyAccessible || onlyObstructed) drawSeatingMap();
	});

	const RS_PAD = 32;

	function computeSectionBounds(rows: any[]) {
		let minX = Infinity,
			maxX = -Infinity,
			minY = Infinity,
			maxY = -Infinity;
		rows.forEach((row: any, ri: number) => {
			const ry = row.positionY ?? 200 + ri * 50;
			(row.seats || []).forEach((seat: any, si: number) => {
				const sx = seat.positionX ?? 100 + si * 32,
					sy = seat.positionY ?? ry;
				if (sx < minX) minX = sx;
				if (sx > maxX) maxX = sx;
				if (sy < minY) minY = sy;
				if (sy > maxY) maxY = sy;
			});
		});
		if (!isFinite(minX)) return null;
		return {
			boxX: minX - RS_PAD,
			boxY: minY - RS_PAD,
			boxW: maxX - minX + RS_PAD * 2,
			boxH: maxY - minY + RS_PAD * 2
		};
	}

	function getSectionColor(section: any): string {
		// 1. section carries its own color from the section (set by +page.server.ts)
		if (section.color) return section.color;
		// 2. dominant sectionId on seats → section color
		const cnt: Record<string, number> = {};
		for (const row of section.rows || [])
			for (const seat of row.seats || [])
				if (seat.sectionId) cnt[seat.sectionId] = (cnt[seat.sectionId] || 0) + 1;
		let max = 0,
			dom = '';
		for (const [id, c] of Object.entries(cnt))
			if (c > max) {
				max = c;
				dom = id;
			}
		if (dom) {
			const s = sections.find((s) => s.id === dom);
			if (s?.color) return s.color;
		}
		// 3. dominant priceLevelId on seats → price level color
		const pc: Record<string, number> = {};
		for (const row of section.rows || [])
			for (const seat of row.seats || [])
				if (seat.priceLevelId) pc[seat.priceLevelId] = (pc[seat.priceLevelId] || 0) + 1;
		let pmax = 0,
			pdom = '';
		for (const [id, c] of Object.entries(pc))
			if (c > pmax) {
				pmax = c;
				pdom = id;
			}
		if (pdom) {
			const p = priceLevels.find((p) => p.id === pdom);
			if (p?.color) return p.color;
		}
		return '#3B82F6';
	}

	function drawSeatingMap() {
		if (!stage || !layer || !Konva) return;
		layer.destroyChildren();
		layer.add(selectionRect);

		const sc = stage.scaleX() || 1;
		const SEAT_R = getSeatRadius(sc);

		const secProgress = _secProgress(sc);
		const secFill = Math.max(0, 0.85 * (1 - secProgress));
		const secStroke = Math.max(0.3, 1.0 - secProgress * 0.7);
		const showLabel = sc < fitScale * 2.0;
		const showSeats = _showSeats(sc);

		// ── Layout Objects ─────────────────────────────────────────────────
		layoutObjects.forEach((obj, idx) => {
			const ox = obj.x ?? 100,
				oy = obj.y ?? 100,
				ow = obj.width ?? 200,
				oh = obj.height ?? 100;
			const isSel = selectedObjectId === idx;
			const g = new Konva.Group({ x: ox, y: oy, draggable: false, id: 'obj-' + idx });
			if (obj.type === 'stage') {
				g.add(
					new Konva.Rect({
						width: ow,
						height: oh,
						fill: '#334155',
						stroke: isSel ? '#F59E0B' : '#64748B',
						strokeWidth: isSel ? 3 : 2,
						cornerRadius: 8
					})
				);
				g.add(
					new Konva.Text({
						text: (obj.text || 'STAGE').toUpperCase(),
						fontSize: Math.max(12, Math.min(ow, oh) * 0.2),
						fontStyle: 'bold',
						fill: '#F8FAFC',
						align: 'center',
						verticalAlign: 'middle',
						width: ow,
						height: oh
					})
				);
			} else if (obj.type === 'label') {
				const t = new Konva.Text({
					text: obj.text || 'Label',
					fontSize: obj.fontSize || 14,
					fontStyle: 'bold',
					fill: isSel ? '#F59E0B' : obj.color || '#0F172A',
					align: 'center',
					verticalAlign: 'middle'
				});
				g.add(
					new Konva.Rect({
						width: t.width() + 16,
						height: t.height() + 16,
						x: -8,
						y: -8,
						fill: 'transparent',
						stroke: isSel ? '#F59E0B' : 'transparent',
						strokeWidth: 1.5,
						cornerRadius: 4
					})
				);
				g.add(t);
			} else {
				const fill = obj.color || '#CBD5E1',
					op = obj.opacity !== undefined ? Number(obj.opacity) : 0.5;
				if (obj.shape === 'circle') {
					const r = obj.radius || Math.min(ow, oh) / 2;
					g.add(
						new Konva.Circle({
							x: r,
							y: r,
							radius: r,
							fill,
							opacity: op,
							stroke: isSel ? '#F59E0B' : '#94A3B8',
							strokeWidth: isSel ? 3 : 1
						})
					);
				} else {
					g.add(
						new Konva.Rect({
							width: ow,
							height: oh,
							fill,
							opacity: op,
							stroke: isSel ? '#F59E0B' : '#94A3B8',
							strokeWidth: isSel ? 3 : 1,
							cornerRadius: obj.cornerRadius || 4
						})
					);
				}
			}
			g.on('click tap', (e: any) => {
				e.cancelBubble = true;
				selectedObjectId = idx;
				selectedGaSectionId = '';
				selectedRsSectionId = '';
				drawSeatingMap();
			});
			layer.add(g);
		});

		// ── GA Areas ───────────────────────────────────────────────────────
		gaSections.forEach((ga, idx) => {
			const x = ga.x || 50 + idx * 220,
				y = ga.y || 50,
				gw = ga.width || 200,
				gh = ga.height || 100;
			const isSel = selectedGaSectionId === ga.id;
			const g = new Konva.Group({ x, y, draggable: false, id: ga.id });
			g.add(
				new Konva.Rect({
					width: gw,
					height: gh,
					fill: isSel ? '#F1F5F9' : '#F8FAFC',
					stroke: isSel ? '#0F172A' : '#CBD5E1',
					strokeWidth: isSel ? 2.5 : 1.5,
					cornerRadius: 12
				})
			);
			// label: prefer ga.name, fall back to cleaned id
			const gaLabel = (ga.name || ga.id.replace(manifestIdVal + '-', '')).toUpperCase();
			g.add(
				new Konva.Text({
					text: gaLabel,
					fontSize: Math.max(14, Math.min(gw, gh) * 0.2),
					fontStyle: 'bold',
					fill: '#94A3B8',
					opacity: 0.4,
					width: gw,
					height: gh,
					align: 'center',
					verticalAlign: 'middle'
				})
			);
			g.on('click tap', (e: any) => {
				e.cancelBubble = true;
				selectedGaSectionId = ga.id;
				selectedRsSectionId = '';
				selectedObjectId = null;
				drawSeatingMap();
			});
			layer.add(g);
		});

		// ── RS Areas ───────────────────────────────────────────────────────
		rsSections.forEach((section) => {
			const rows = section.rows || [];
			const ud = section.uiData;
			if (ud?.x == null) return;
			const b = { boxX: ud.x, boxY: ud.y, boxW: ud.width, boxH: ud.height };
			const { boxX, boxY, boxW, boxH } = b;
			const isSel = selectedRsSectionId === section.id;
			const secColor = getSectionColor(section);
			const ag = new Konva.Group({ x: boxX, y: boxY, draggable: false, id: 'group-' + section.id });

			// Shape: prefer section.polygon (arc sections), else compute hull from seats
			const hull =
				section.polygon?.length >= 6
					? (section.polygon as [number, number][]).flatMap(([px, py]) => [px - boxX, py - boxY])
					: _hullPoints(rows, boxX, boxY, RS_PAD - 4);

			const shapeAttrs = {
				fill: isSel ? `rgba(241,245,249,${secFill + 0.05})` : `rgba(248,250,252,${secFill})`,
				stroke: isSel ? `rgba(15,23,42,${secStroke})` : `rgba(160,180,210,${secStroke})`,
				strokeWidth: (isSel ? 2 : 1) / sc,
				hitStrokeWidth: 0,
				closed: true
			};
			if (hull.length >= 6) ag.add(new Konva.Line({ ...shapeAttrs, points: hull }));
			else
				ag.add(
					new Konva.Rect({ ...shapeAttrs, x: 0, y: 0, width: boxW, height: boxH, cornerRadius: 8 })
				);

			if (showLabel) {
				// Label: section.name (from section.name) is the primary source now
				const cleanId = (id: string) =>
					id
						.replace(new RegExp('^' + manifestIdVal + '[-_]?', 'i'), '')
						.replace(/^(section|sec|section)[-_]/i, '')
						.replace(/[-_]/g, ' ')
						.replace(/\b\w/g, (c) => c.toUpperCase())
						.trim();

				// Find the section object matching this section
				const sec = sections.find((s) => s.id === section.id);
				const rawLabel = section.name || sec?.name || sec?.description || cleanId(section.id);
				const labelText = rawLabel.toUpperCase();
				const chars = labelText.length;
				const maxFontByWidth = (boxW * 0.78) / Math.max(chars * 0.58, 1);
				const maxFontByHeight = boxH * 0.28;
				const fontSize = Math.max(8, Math.min(22, maxFontByWidth, maxFontByHeight));

				ag.add(
					new Konva.Text({
						text: labelText,
						fontSize,
						fontStyle: 'bold',
						fontFamily: 'Inter, system-ui, sans-serif',
						fill: '#4A5568',
						opacity: Math.max(0.75, 1 - secProgress * 0.35),
						align: 'center',
						verticalAlign: 'middle',
						x: 0,
						y: 0,
						width: boxW,
						height: boxH,
						wrap: 'none',
						ellipsis: true,
						listening: false
					})
				);
			}

			if (showSeats) {
				rows.forEach((row: any, ri: number) => {
					const rowY = row.positionY ?? 200 + ri * 50;
					(row.seats || []).forEach((seat: any, si: number) => {
						const sx = seat.positionX ?? 100 + si * 32;
						const sy = seat.positionY ?? rowY;

						// onlyAvailable: real filter. onlyAccessible/onlyObstructed: fields removed
						// from API — these checkboxes exist in UI but have no effect (always false).
						let isFiltered = false;
						if (onlyAvailable && seat.status !== 'AVAILABLE') isFiltered = true;

						const isSeatSel = selectedSeatIds.includes(seat.id);
						let color = secColor;
						if (activeMode === 'inventory') {
							color = seat.status === 'AVAILABLE' ? '#10B981' : '#EF4444';
						} else if (seat.status === 'UNAVAILABLE') {
							color = '#E2E8F0';
						} else {
							const sec = seat.sectionId ? sections.find((s) => s.id === seat.sectionId) : null;
							if (sec?.color) color = sec.color;
							else {
								const pl = seat.priceLevelId
									? priceLevels.find((p) => p.id === seat.priceLevelId)
									: null;
								if (pl?.color) color = pl.color;
							}
						}

						const sg = new Konva.Group({
							x: sx - boxX,
							y: sy - boxY,
							id: seat.id,
							draggable: activeMode === 'floor-edit'
						});

						sg.add(
							new Konva.Circle({
								radius: SEAT_R,
								fill: color,
								opacity: isFiltered ? 0.15 : 1,
								stroke: 'transparent',
								strokeWidth: 0
							})
						);

						if (isSeatSel) {
							const sw = Math.max(1.5 / sc, 1);
							sg.add(
								new Konva.Circle({
									radius: SEAT_R - sw / 2,
									stroke: '#000000',
									strokeWidth: sw,
									listening: false
								})
							);
						}

						const circle = sg.getChildren()[0];
						circle.on('click tap', (e: any) => {
							if (!['select', 'brush', 'eraser'].includes(activeTool)) return;
							e.cancelBubble = true;
							if (activeTool === 'brush') {
								if (brushSectionId) paintSection(seat.id, brushSectionId);
								else if (brushPriceLevelId) paintSeat(seat.id, brushPriceLevelId);
								drawSeatingMap();
								return;
							}
							if (activeTool === 'eraser') {
								removeSeatById(seat.id);
								drawSeatingMap();
								return;
							}
							const isRight = e.evt?.button === 2 || e.evt?.which === 3;
							if (selectionTool === 'seat') {
								if (isRight) {
									selectedSeatIds = selectedSeatIds.filter((x) => x !== seat.id);
									drawSeatingMap();
								}
								return;
							}
							let ids: string[] = [];
							if (selectionTool === 'row') ids = row.seats.map((s: any) => s.id);
							else rows.forEach((r: any) => ids.push(...(r.seats || []).map((s: any) => s.id)));
							if (isRight) {
								selectedSeatIds = selectedSeatIds.filter((x) => !ids.includes(x));
							} else {
								selectedSeatIds = Array.from(new Set([...selectedSeatIds, ...ids]));
							}
							drawSeatingMap();
						});
						circle.on('mouseenter', () => {
							if (activeTool !== 'pan') stage.container().style.cursor = pointer2Cursor;
						});
						circle.on('mouseleave', () => {
							if (activeTool !== 'pan') stage.container().style.cursor = pointer2Cursor;
						});
						sg.on('dragend', () => {
							seat.positionX = boxX + Math.round(sg.x() / snapGrid) * snapGrid;
							seat.positionY = boxY + Math.round(sg.y() / snapGrid) * snapGrid;
							drawSeatingMap();
							updateViewport();
						});
						ag.add(sg);
					});
				});
			}

			layer.add(ag);
		});

		layer.batchDraw();
	}

	function setupCanvasEvents() {
		if (!stage) return;
		let isDown = false,
			brushActive = false;

		stage.on('mousedown touchstart', (e: any) => {
			if (e.target === stage) {
				selectedObjectId = null;
				selectedGaSectionId = '';
				selectedRsSectionId = '';
				drawSeatingMap();
			}
			isDown = true;
			const t = e.target;
			const isSeat =
				t?.className === 'Circle' &&
				t?.parent?.id() &&
				!t.parent.id().startsWith('group-') &&
				!t.parent.id().startsWith('obj-');
			if (isSeat) {
				brushActive = true;
				const id = t.parent?.id() || t.id();
				if (id && !id.startsWith('group-') && id !== stage.id()) {
					if (activeTool === 'eraser') {
						removeSeatById(id);
						drawSeatingMap();
					} else if (activeTool === 'brush') {
						if (brushSectionId) paintSection(id, brushSectionId);
						else if (brushPriceLevelId) paintSeat(id, brushPriceLevelId);
						drawSeatingMap();
					} else if (activeTool === 'select') {
						const isRight = e.evt?.button === 2 || e.evt?.which === 3;
						if (isRight) {
							selectedSeatIds = selectedSeatIds.filter((x) => x !== id);
						} else if (isShiftPressed) {
							selectedSeatIds = selectedSeatIds.includes(id)
								? selectedSeatIds.filter((x) => x !== id)
								: [...selectedSeatIds, id];
						} else if (!selectedSeatIds.includes(id)) {
							selectedSeatIds = [...selectedSeatIds, id];
						}
						drawSeatingMap();
					}
				}
				return;
			}
			if (activeTool !== 'select') return;
			brushActive = false;
			const pos = stage.getPointerPosition();
			if (!pos) return;
			const s = stage.scaleX() || 1;
			x1 = (pos.x - stage.x()) / s;
			y1 = (pos.y - stage.y()) / s;
			x2 = x1;
			y2 = y1;
			selectionRect.x(x1);
			selectionRect.y(y1);
			selectionRect.width(0);
			selectionRect.height(0);
			selectionRect.visible(true);
			layer.batchDraw();
		});

		stage.on('mousemove touchmove', () => {
			if (!isDown) return;
			if (brushActive) {
				const pos = stage.getPointerPosition();
				if (!pos) return;
				const shape = stage.getIntersection(pos);
				if (shape?.className === 'Circle' && !shape.parent?.id()?.startsWith('obj-')) {
					const id = shape.parent?.id() || shape.id();
					if (id && !id.startsWith('group-') && id !== stage.id()) {
						if (activeTool === 'eraser') {
							removeSeatById(id);
							drawSeatingMap();
						} else if (activeTool === 'brush') {
							if (brushSectionId) paintSection(id, brushSectionId);
							else if (brushPriceLevelId) paintSeat(id, brushPriceLevelId);
							drawSeatingMap();
						} else if (activeTool === 'select' && !selectedSeatIds.includes(id)) {
							selectedSeatIds = [...selectedSeatIds, id];
							drawSeatingMap();
						}
					}
				}
				return;
			}
			if (!selectionRect.visible()) return;
			const pos = stage.getPointerPosition();
			if (!pos) return;
			const s = stage.scaleX() || 1;
			x2 = (pos.x - stage.x()) / s;
			y2 = (pos.y - stage.y()) / s;
			selectionRect.setAttrs({
				x: Math.min(x1, x2),
				y: Math.min(y1, y2),
				width: Math.abs(x2 - x1),
				height: Math.abs(y2 - y1)
			});
			layer.batchDraw();
		});

		stage.on('mouseup touchend', () => {
			isDown = false;
			brushActive = false;
			if (!selectionRect.visible()) return;
			selectionRect.visible(false);
			const rx = selectionRect.x(),
				ry = selectionRect.y(),
				rw = selectionRect.width(),
				rh = selectionRect.height();
			const sel: string[] = [];
			rsSections.forEach((section) =>
				(section.rows || []).forEach((row: any) =>
					(row.seats || []).forEach((seat: any) => {
						const sx = seat.positionX ?? 0,
							sy = seat.positionY ?? row.positionY ?? 0;
						if (sx >= rx && sx <= rx + rw && sy >= ry && sy <= ry + rh) {
							if (selectionTool === 'seat') sel.push(seat.id);
							else if (selectionTool === 'row') row.seats.forEach((s: any) => sel.push(s.id));
							else
								(section.rows || []).forEach((r: any) =>
									r.seats.forEach((s: any) => sel.push(s.id))
								);
						}
					})
				)
			);
			selectedSeatIds =
				sel.length > 0 ? Array.from(new Set([...selectedSeatIds, ...sel])) : selectedSeatIds;
			drawSeatingMap();
		});
	}

	function findSeatById(id: string) {
		for (const section of rsSections)
			for (const row of section.rows || []) {
				const seat = (row.seats || []).find((s: any) => s.id === id);
				if (seat) return { section, row, seat };
			}
		return null;
	}
	function removeSeatById(id: string) {
		selectedSeatIds = selectedSeatIds.filter((x) => x !== id);
		rsSections.forEach((section) => {
			if (!section.rows) return;
			section.rows.forEach((row: any) => {
				if (row.seats) row.seats = row.seats.filter((s: any) => s.id !== id);
			});
			section.rows = section.rows.filter((row: any) => row.seats?.length > 0);
		});
	}
	function paintSeat(id: string, plId: string) {
		const f = findSeatById(id);
		if (f) f.seat.priceLevelId = plId;
	}
	function paintSection(id: string, secId: string) {
		const f = findSeatById(id);
		if (f) f.seat.sectionId = secId;
	}
	function deleteSelectedSeats() {
		if (!selectedSeatIds.length) return;
		rsSections.forEach((section) => {
			if (!section.rows) return;
			section.rows.forEach((row: any) => {
				if (row.seats) row.seats = row.seats.filter((s: any) => !selectedSeatIds.includes(s.id));
			});
			section.rows = section.rows.filter((r: any) => r.seats?.length > 0);
		});
		selectedSeatIds = [];
		drawSeatingMap();
	}

	function handleKeyDown(e: KeyboardEvent) {
		const el = document.activeElement;
		if (el && ['INPUT', 'SELECT', 'TEXTAREA'].includes(el.tagName)) return;
		if (e.key === 'Shift') isShiftPressed = true;
		if (e.code === 'Space') {
			previousToolBeforeSpace = activeTool;
			activeTool = 'pan';
			e.preventDefault();
		}
		if (e.key.toLowerCase() === 'v') activeTool = 'select';
		if (e.key.toLowerCase() === 'b') activeTool = 'brush';
		if (e.key.toLowerCase() === 'e') activeTool = 'eraser';
		if (e.key === 'Delete' || e.key === 'Backspace') deleteSelectedSeats();
		if ((e.ctrlKey || e.metaKey) && (e.key === '=' || e.key === '+')) {
			e.preventDefault();
			zoomIn();
		}
		if ((e.ctrlKey || e.metaKey) && e.key === '-') {
			e.preventDefault();
			zoomOut();
		}
		if ((e.ctrlKey || e.metaKey) && e.key === '0') {
			e.preventDefault();
			fitToView();
		}
	}
	function handleKeyUp(e: KeyboardEvent) {
		if (e.key === 'Shift') isShiftPressed = false;
		if (e.code === 'Space') activeTool = previousToolBeforeSpace;
	}

	onDestroy(() => {
		if (browser) {
			window.removeEventListener('resize', handleResize);
			window.removeEventListener('keydown', handleKeyDown);
			window.removeEventListener('keyup', handleKeyUp);
			if (stage) stage.destroy();
		}
	});

	function generateSeatingBlock() {
		if (!selectedRsSectionId) {
			errorMessage = 'Select a Reserved Seating Area first.';
			return;
		}
		const section = rsSections.find((a) => a.id === selectedRsSectionId);
		if (!section) return;
		const rows = section.rows || [];
		const startCode = blockRowPrefix.toUpperCase().charCodeAt(0);
		for (let ri = 0; ri < blockRowsCount; ri++) {
			const rowName = String.fromCharCode(startCode + ri);
			const rowId = `${selectedRsSectionId}-row-${rowName}-${Date.now()}`;
			const rowY = 220 + rows.length * 48;
			let cnt = blockSeatsPerRow,
				off = 0;
			if (blockShape === 'trapezoid') {
				cnt = blockSeatsPerRow + ri * 2;
				off = -ri * 16;
			} else if (blockShape === 'diamond') {
				const mid = Math.floor(blockRowsCount / 2),
					d = Math.abs(ri - mid);
				cnt = Math.max(1, blockSeatsPerRow - d * 2);
				off = d * 16;
			} else if (blockShape === 'staggered') off = ri % 2 === 1 ? 16 : 0;
			// Seats: only fields matching SeatResponse (no accessibility/obstructedView/aisle)
			const newRow: any = { id: rowId, sectionId: section.id, name: rowName, seats: [] };
			for (let si = 0; si < cnt; si++) {
				const num = blockSeatStartNum + si;
				newRow.seats.push({
					id: `${rowId}-seat-${num}`,
					rowId,
					name: String(num).padStart(3, '0'),
					positionX: 120 + si * 32 + off,
					positionY: rowY,
					status: 'AVAILABLE',
					priceLevelId: brushPriceLevelId || null,
					sectionId: section.id
				});
			}
			rows.push(newRow);
		}
		section.rows = rows;
		rsSections = [...rsSections];
		drawSeatingMap();
		errorMessage = '';
	}

	function setInventoryStatus(status: 'AVAILABLE' | 'UNAVAILABLE') {
		if (!selectedSeatIds.length) return;
		rsSections.forEach((section) =>
			(section.rows || []).forEach((row: any) =>
				(row.seats || []).forEach((seat: any) => {
					if (selectedSeatIds.includes(seat.id)) seat.status = status;
				})
			)
		);
		rsSections = [...rsSections];
		saveMessage = `Marked ${selectedSeatIds.length} seats as ${status}`;
		selectedSeatIds = [];
		drawSeatingMap();
	}

	function assignPriceLevelToSelected(plId: string) {
		if (!selectedSeatIds.length) return;
		rsSections.forEach((section) =>
			(section.rows || []).forEach((row: any) =>
				(row.seats || []).forEach((seat: any) => {
					if (selectedSeatIds.includes(seat.id)) seat.priceLevelId = plId;
				})
			)
		);
		rsSections = [...rsSections];
		saveMessage = `Assigned ${plId}!`;
		selectedSeatIds = [];
		drawSeatingMap();
	}

	function createLookup(type: 'level' | 'section') {
		const code = type === 'level' ? levelCode : sectionCode;
		const color = type === 'level' ? levelColor : sectionColor;
		if (!code) {
			errorMessage = 'Code required.';
			return;
		}
		const item = { id: code.trim(), description: code.trim(), color };
		if (type === 'level') {
			levels = [...levels, item];
			levelCode = '';
		} else {
			sections = [...sections, item];
			sectionCode = '';
		}
		saveMessage = `Added ${type}.`;
		errorMessage = '';
	}

	function runAssistantPrompt() {
		if (!assistantPrompt) return;
		const q = assistantPrompt.toLowerCase();
		let ids: string[] = [];
		if (q.includes('vip') || q.includes('gold'))
			rsSections.forEach((a) =>
				(a.rows || []).forEach((r: any) =>
					(r.seats || []).forEach((s: any) => {
						if (s.priceLevelId?.toLowerCase().match(/vip|gold/)) ids.push(s.id);
					})
				)
			);
		else if (q.includes('standard') || q.includes('p1'))
			rsSections.forEach((a) =>
				(a.rows || []).forEach((r: any) =>
					(r.seats || []).forEach((s: any) => {
						if (s.priceLevelId?.toLowerCase().match(/standard|p1/)) ids.push(s.id);
					})
				)
			);
		else if (q.includes('row')) {
			const m = q.match(/row\s+([a-z0-9]+)/i);
			if (m)
				rsSections.forEach((a) =>
					(a.rows || []).forEach((r: any) => {
						if (r.name.toUpperCase() === m[1].toUpperCase())
							ids.push(...(r.seats || []).map((s: any) => s.id));
					})
				);
		} else if (q.includes('all'))
			rsSections.forEach((a) =>
				(a.rows || []).forEach((r: any) => ids.push(...(r.seats || []).map((s: any) => s.id)))
			);
		if (ids.length > 0) {
			selectedSeatIds = ids;
			saveMessage = `Selected ${ids.length} seats.`;
			errorMessage = '';
			drawSeatingMap();
		} else errorMessage = `No seats matched '${assistantPrompt}'.`;
		assistantPrompt = '';
	}

	function exportLayout(fmt: 'json' | 'csv' | 'svg') {
		let fn = `manifest-${manifestIdVal || 'export'}-${Date.now()}`,
			content = '',
			mime = 'text/plain';
		if (fmt === 'json') {
			content = JSON.stringify({ levels, sections, priceLevels, gaSections, rsSections }, null, 2);
			mime = 'application/json';
			fn += '.json';
		} else if (fmt === 'csv') {
			const rows = ['SeatID,AreaID,Row,SeatName,PositionX,PositionY,Status,PriceLevelId,SectionId'];
			rsSections.forEach((a) =>
				(a.rows || []).forEach((r: any) =>
					(r.seats || []).forEach((s: any) =>
						rows.push(
							[
								s.id,
								a.id,
								r.name,
								s.name,
								s.positionX,
								s.positionY ?? r.positionY,
								s.status,
								s.priceLevelId || '',
								s.sectionId || ''
							].join(',')
						)
					)
				)
			);
			content = rows.join('\n');
			mime = 'text/csv';
			fn += '.csv';
		} else {
			let svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${bounds.minX} ${bounds.minY} ${bounds.width} ${bounds.height}">\n`;
			rsSections.forEach((a) =>
				(a.rows || []).forEach((r: any) =>
					(r.seats || []).forEach((s: any) => {
						const sec = s.sectionId ? sections.find((sc: any) => sc.id === s.sectionId) : null;
						svg += `  <circle cx="${s.positionX}" cy="${s.positionY ?? r.positionY}" r="8" fill="${s.status === 'UNAVAILABLE' ? '#E2E8F0' : sec?.color || a.color || '#3B82F6'}" />\n`;
					})
				)
			);
			svg += '</svg>';
			content = svg;
			mime = 'image/svg+xml';
			fn += '.svg';
		}
		const url = URL.createObjectURL(new Blob([content], { type: mime }));
		const a = document.createElement('a');
		a.href = url;
		a.download = fn;
		a.click();
		URL.revokeObjectURL(url);
	}

	async function saveLayout() {
		isSaving = true;
		saveMessage = 'Saving...';
		errorMessage = '';
		try {
			// 1. Update manifest metadata
			await fetch(`/api/ops/venues/manifests/${manifestIdVal}`, {
				method: 'PUT',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					description: manifestDescVal,
					totalCapacity: Number(manifestCapVal),
					objects: layoutObjects
				})
			});
			// 2. Upsert levels
			for (const lvl of levels)
				await fetch(`/api/ops/venues/manifests/${manifestIdVal}/levels`, {
					method: 'PUT',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify(lvl)
				});
			// 3. Upsert sections (includes GA sections via type field)
			for (const sec of sections)
				await fetch(`/api/ops/venues/manifests/${manifestIdVal}/sections`, {
					method: 'PUT',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify(sec)
				});
			// 4. Upsert price levels
			for (const pl of priceLevels)
				await fetch(`/api/ops/venues/manifests/${manifestIdVal}/price-levels`, {
					method: 'PUT',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify(pl)
				});
			// 5. Save RS section rows & seats using correct API endpoints:
			//    POST /api/ops/venues/sections/{sectionId}/rows
			//    POST /api/ops/venues/rows/{rowId}/seats
			for (const section of rsSections) {
				for (const row of section.rows || []) {
					await fetch(`/api/ops/venues/sections/${section.id}/rows`, {
						method: 'POST',
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify({ id: row.id, name: row.name })
					});
					for (const seat of row.seats || [])
						await fetch(`/api/ops/venues/rows/${row.id}/seats`, {
							method: 'POST',
							headers: { 'Content-Type': 'application/json' },
							body: JSON.stringify({
								id: seat.id,
								name: seat.name,
								positionX: seat.positionX ?? null,
								positionY: seat.positionY,
								status: seat.status,
								priceLevelId: seat.priceLevelId || null,
								sectionId: seat.sectionId || section.id
							})
						});
				}
			}
			saveMessage = 'Layout saved!';
			selectedSeatIds = [];
			drawSeatingMap();
			if (data.isNew)
				setTimeout(
					() => goto(`/ops/venues/${venue.id}/manifests/${manifestIdVal}`, { invalidateAll: true }),
					1000
				);
		} catch (err: any) {
			errorMessage = err.message || 'Error saving.';
		} finally {
			isSaving = false;
		}
	}
</script>

<div class="flex h-screen w-screen flex-col overflow-hidden bg-slate-50 font-sans select-none">
	<header
		class="z-50 flex h-12 shrink-0 items-center justify-between border-b border-slate-200 bg-white px-4"
	>
		<div class="flex items-center gap-2">
			<a
				href="/ops/venues/{venue?.id}/manifests"
				class="rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-800"
				aria-label="Back"
			>
				<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
					><path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2.5"
						d="M10 19l-7-7m0 0l7-7m-7 7h18"
					/></svg
				>
			</a>
			<span class="text-xs font-bold text-slate-500">{venue?.name || 'Venue'}</span>
			<span class="text-slate-300">|</span>
			<span class="text-xs text-slate-400">{manifestDescVal || 'Manifest'}</span>
		</div>
		<div class="flex rounded-lg border border-slate-200 bg-slate-100 p-0.5">
			{#each ['scaling', 'inventory', 'floor-edit'] as mode}
				<button
					onclick={() => {
						activeMode = mode as any;
						selectedSeatIds = [];
					}}
					class="rounded-md px-3 py-1 text-[10px] font-bold capitalize transition-all {activeMode ===
					mode
						? 'bg-white text-slate-900 shadow-xs'
						: 'text-slate-500 hover:text-slate-800'}"
					>{mode === 'floor-edit'
						? 'Floor Edit'
						: mode.charAt(0).toUpperCase() + mode.slice(1)}</button
				>
			{/each}
		</div>
		<div class="flex items-center gap-2">
			{#if saveMessage}<span class="text-[10px] font-bold text-emerald-600">{saveMessage}</span
				>{/if}
			{#if errorMessage}<span class="text-[10px] font-bold text-rose-600">{errorMessage}</span>{/if}
			<div class="relative">
				<button
					onclick={() => (showExportDropdown = !showExportDropdown)}
					class="flex items-center gap-1.5 rounded-lg bg-slate-900 px-3 py-1.5 text-[10px] font-bold text-white transition hover:bg-slate-800"
				>
					<svg class="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
						/></svg
					>
					<span>Export</span>
				</button>
				{#if showExportDropdown}
					<div
						class="absolute right-0 z-60 mt-1.5 w-44 rounded-lg border border-slate-200 bg-white py-1 shadow-xl"
					>
						<button
							onclick={() => {
								exportLayout('json');
								showExportDropdown = false;
							}}
							class="w-full px-3 py-2 text-left text-[10px] font-bold text-slate-700 hover:bg-slate-50"
							>Layout (JSON)</button
						>
						<button
							onclick={() => {
								exportLayout('csv');
								showExportDropdown = false;
							}}
							class="w-full px-3 py-2 text-left text-[10px] font-bold text-slate-700 hover:bg-slate-50"
							>Seats (CSV)</button
						>
						<button
							onclick={() => {
								exportLayout('svg');
								showExportDropdown = false;
							}}
							class="w-full px-3 py-2 text-left text-[10px] font-bold text-slate-700 hover:bg-slate-50"
							>Blueprint (SVG)</button
						>
					</div>
				{/if}
			</div>
			<button
				onclick={saveLayout}
				disabled={isSaving}
				class="rounded-lg bg-slate-900 px-3 py-1.5 text-[10px] font-extrabold text-white hover:bg-black disabled:opacity-50"
				>{isSaving ? 'Saving...' : 'Save'}</button
			>
		</div>
	</header>

	<div class="relative flex min-h-0 flex-1">
		<!-- ── Left Sidebar: Price Levels ──────────────────────────── -->
		<aside class="flex w-[280px] shrink-0 flex-col border-r border-slate-200 bg-white">
			<div class="flex items-center justify-between border-b border-slate-200 px-4 py-2.5">
				<h3 class="text-[10px] font-black tracking-widest text-slate-400 uppercase">
					Price Levels
				</h3>
				<button
					onclick={() =>
						priceLevels.push({
							id: 'NEW',
							description: 'New Level',
							color: getNextPriceLevelColor()
						})}
					class="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
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
			<div class="flex-1 space-y-px overflow-y-auto px-3 py-2">
				<div class="flex items-center gap-2 rounded-lg px-2.5 py-2 text-[11px]">
					<span
						class="flex h-3 w-3 shrink-0 items-center justify-center rounded-full border-2 border-slate-300 bg-white"
					></span>
					<span class="flex-1 font-semibold text-slate-500">Unassigned</span>
					<span class="font-bold text-slate-400">{seatCountsByPriceLevel.unassigned}</span>
				</div>
				{#each priceLevels as pl}
					{@const count = seatCountsByPriceLevel.counts[pl.id] || 0}
					{@const price = priceLevelBasePrices[pl.id] || 0}
					{@const revenue = count * price}
					<div
						class="flex items-center gap-2 rounded-lg px-2.5 py-2 text-[11px] transition-colors hover:bg-slate-50 {brushPriceLevelId ===
						pl.id
							? 'ring-2 ring-slate-300 ring-inset'
							: ''}"
						onclick={() => {
							brushPriceLevelId = pl.id;
							activeTool = 'brush';
						}}
						role="button"
					>
						<span
							class="h-3 w-3 shrink-0 rounded-full border border-white/50"
							style="background:{pl.color}"
						></span>
						<div class="flex flex-1 flex-col gap-0.5">
							<span class="font-bold text-slate-800">{pl.description || pl.id}</span>
							<div class="flex items-center gap-1 text-[10px] text-slate-400">
								<span class="font-semibold">{count.toLocaleString()}</span>
								<span class="text-slate-300">·</span>
								<span>$</span>
								<input
									type="number"
									step="0.01"
									min="0"
									value={price}
									oninput={(e) => {
										priceLevelBasePrices = {
											...priceLevelBasePrices,
											[pl.id]: Number((e.target as HTMLInputElement).value) || 0
										};
									}}
									class="w-14 border-b border-transparent bg-transparent px-0.5 text-[10px] font-semibold text-slate-500 outline-none hover:border-slate-300 focus:border-slate-400"
								/>
							</div>
						</div>
						<div class="text-right">
							<div class="font-bold text-slate-800">${revenue.toLocaleString()}</div>
							<div class="text-[9px] text-slate-400">revenue</div>
						</div>
					</div>
				{/each}
			</div>
			<div class="border-t border-slate-200 bg-slate-50/50 px-4 py-3">
				<h4 class="mb-2 text-[9px] font-black tracking-widest text-slate-400 uppercase">
					Financial Information
				</h4>
				<div class="space-y-1.5 text-[11px]">
					<div class="flex items-center justify-between">
						<span class="font-medium text-slate-500">Sellable Capacity</span>
						<span class="font-bold text-slate-800"
							>{financialInfo.sellableCapacity.toLocaleString()}</span
						>
					</div>
					<div class="flex items-center justify-between">
						<span class="font-medium text-slate-500">Average per seat</span>
						<span class="font-bold text-slate-800">${financialInfo.averagePerSeat.toFixed(2)}</span>
					</div>
					<div class="flex items-center justify-between">
						<span class="font-medium text-slate-500">Potential Revenue</span>
						<span class="font-bold text-emerald-700"
							>${financialInfo.potentialRevenue.toLocaleString(undefined, {
								minimumFractionDigits: 2,
								maximumFractionDigits: 2
							})}</span
						>
					</div>
				</div>
				<label
					class="mt-2 flex cursor-pointer items-center gap-1.5 text-[10px] text-slate-400 hover:text-slate-600"
				>
					<input type="checkbox" class="h-3 w-3 rounded border-slate-300" checked />
					Exclude kills from seat counts
				</label>
			</div>
		</aside>

		<!-- ── Canvas ──────────────────────────────────────────────── -->
		<main class="relative flex flex-1 flex-col overflow-hidden bg-[#FAFAFA]">
			<div
				id="stage-container"
				bind:this={stageContainer}
				role="application"
				aria-label="Seatmap"
				class="relative w-full flex-1 overflow-hidden"
				oncontextmenu={(e) => e.preventDefault()}
			></div>

			<!-- Zoom controls -->
			<div
				class="absolute right-4 bottom-4 z-30 flex flex-col items-center gap-0.5 rounded-xl border border-slate-200/80 bg-white/90 p-0.5 shadow-lg backdrop-blur-md"
			>
				<button
					type="button"
					onclick={zoomIn}
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
					onclick={zoomOut}
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
					onclick={fitToView}
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
						>{Math.round(currentScale * 100)}%</span
					>
				</div>
				<div
					class="relative w-full flex-1 overflow-hidden rounded-lg border border-slate-200/60 bg-slate-50"
				>
					<button
						type="button"
						aria-label="Minimap"
						class="relative flex h-full w-full cursor-crosshair items-center justify-center overflow-hidden border-0 bg-transparent p-0"
						onclick={handleMinimapClick}
					>
						<svg
							viewBox="{bounds.minX} {bounds.minY} {bounds.width} {bounds.height}"
							class="pointer-events-none h-full w-full"
						>
							{#each rsSections as section}
								{@const secColor = section.color || '#94A3B8'}
								{#if section.polygon?.length >= 6}
									<polygon
										points={section.polygon.map((p: [number, number]) => p.join(',')).join(' ')}
										fill={secColor}
										fill-opacity="0.08"
										stroke={secColor}
										stroke-opacity="0.55"
										stroke-width="1.5"
										vector-effect="non-scaling-stroke"
									/>
								{:else}
									{#each section.rows || [] as row}
										{#each row.seats || [] as seat}
											<circle
												cx={seat.positionX ?? 0}
												cy={seat.positionY ?? row.positionY ?? 0}
												r={Math.max(1.5, Math.min(bounds.width / 220, 4))}
												fill={secColor}
												fill-opacity="0.45"
											/>
										{/each}
									{/each}
								{/if}
							{/each}
							{#each gaSections as ga}
								<rect
									x={ga.x || 50}
									y={ga.y || 50}
									width={ga.width || 200}
									height={ga.height || 100}
									fill="none"
									stroke="#334155"
									stroke-opacity="0.35"
									stroke-width="1.5"
									rx="3"
								/>
							{/each}
							{#each layoutObjects as obj}
								{#if obj.type === 'stage'}
									<rect
										x={obj.x ?? 100}
										y={obj.y ?? 100}
										width={obj.width ?? 200}
										height={obj.height ?? 100}
										fill="#334155"
										stroke="#64748B"
										stroke-width="1.5"
										rx="4"
									/>
								{:else if obj.type === 'label'}
									<rect
										x={(obj.x ?? 0) - 4}
										y={(obj.y ?? 0) - 4}
										width={8}
										height={8}
										fill={obj.color || '#0F172A'}
										stroke={obj.color || '#0F172A'}
										stroke-opacity="0.5"
										stroke-width="1"
										rx="1"
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
								x={viewportX}
								y={viewportY}
								width={viewportW}
								height={viewportH}
								fill="rgba(239,68,68,0.06)"
								stroke="#EF4444"
								stroke-width="5"
								stroke-dasharray="8 6"
								rx="3"
							/>
						</svg>
					</button>
				</div>
			</div>

			{#if selectedSeatIds.length > 0}
				<div
					class="absolute top-3 left-3 z-30 flex items-center gap-3 rounded-xl border border-slate-200 bg-white/95 px-4 py-2.5 shadow-lg backdrop-blur-md"
				>
					<div class="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-100">
						<svg class="h-4 w-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"
							><path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z"
							/></svg
						>
					</div>
					<span class="text-xs font-bold text-slate-800"
						>{selectedSeatIds.length} seats selected</span
					>
					<button
						onclick={() => {
							selectedSeatIds = [];
							drawSeatingMap();
						}}
						class="ml-2 rounded-lg border border-slate-200 px-3 py-1.5 text-[10px] font-bold text-slate-600 transition hover:bg-slate-50 hover:text-slate-800"
						>Clear</button
					>
				</div>
			{/if}

			{#if isSaving}
				<div
					class="absolute inset-0 z-60 flex flex-col items-center justify-center gap-3 bg-white/70 backdrop-blur-xs"
				>
					<svg class="h-8 w-8 animate-spin text-slate-900" fill="none" viewBox="0 0 24 24"
						><circle
							class="opacity-25"
							cx="12"
							cy="12"
							r="10"
							stroke="currentColor"
							stroke-width="4"
						></circle><path
							class="opacity-75"
							fill="currentColor"
							d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
						></path></svg
					>
					<span class="text-sm font-semibold text-slate-800">Saving layout...</span>
				</div>
			{/if}
		</main>

		<!-- ── Right Tool Palette ─────────────── -->
		<aside
			class="flex w-[56px] shrink-0 flex-col items-center gap-0.5 border-l border-slate-200 bg-white py-3"
		>
			<button
				onclick={() => {
					activeTool = 'select';
					selectionTool = 'seat';
				}}
				class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold transition-colors {activeTool ===
					'select' && selectionTool === 'seat'
					? 'bg-slate-100 text-slate-900'
					: 'text-slate-400 hover:bg-slate-50 hover:text-slate-700'}"
			>
				<IconPointer2 class="h-4 w-4" />
				Seat
			</button>
			<button
				onclick={() => {
					activeTool = 'select';
					selectionTool = 'row';
				}}
				class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold transition-colors {activeTool ===
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
				onclick={() => {
					activeTool = 'select';
					selectionTool = 'section';
				}}
				class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold transition-colors {activeTool ===
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
			<div class="my-1.5 h-px w-8 bg-slate-100"></div>
			<button
				onclick={() => (showFilterDialog = !showFilterDialog)}
				class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold text-slate-400 transition-colors hover:bg-slate-50 hover:text-slate-700"
			>
				<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
					><path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="1.5"
						d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
					/></svg
				>
				Filters
			</button>
			<button
				onclick={() => (showAssistantDialog = !showAssistantDialog)}
				class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold text-slate-400 transition-colors hover:bg-slate-50 hover:text-slate-700"
			>
				<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
					><path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="1.5"
						d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
					/></svg
				>
				Assist
			</button>
		</aside>
	</div>
</div>

{#if showFilterDialog}
	<div
		class="fixed top-40 right-24 z-50 w-60 rounded-2xl border border-slate-200 bg-white/95 p-4 shadow-xl backdrop-blur-md"
	>
		<div class="mb-3 flex items-center justify-between border-b border-slate-100 pb-2">
			<h4 class="text-xs font-black tracking-wider text-slate-800 uppercase">Filters</h4>
			<button onclick={() => (showFilterDialog = false)} class="text-slate-400 hover:text-slate-700"
				><svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
					><path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M6 18L18 6M6 6l12 12"
					/></svg
				></button
			>
		</div>
		<div class="space-y-2.5 text-xs font-semibold text-slate-600">
			<label class="flex cursor-pointer items-center gap-2"
				><input type="checkbox" bind:checked={onlyAvailable} class="h-3.5 w-3.5 rounded" /><span
					>Available Only</span
				></label
			>
			<label
				class="flex cursor-pointer items-center gap-2 opacity-40"
				title="Field not in API model"
			>
				<input
					type="checkbox"
					bind:checked={onlyAccessible}
					class="h-3.5 w-3.5 rounded"
					disabled
				/><span>♿ Accessible Only <span class="text-[9px] text-slate-400">(n/a)</span></span>
			</label>
			<label
				class="flex cursor-pointer items-center gap-2 opacity-40"
				title="Field not in API model"
			>
				<input
					type="checkbox"
					bind:checked={onlyObstructed}
					class="h-3.5 w-3.5 rounded"
					disabled
				/><span>⚠️ Obstructed Only <span class="text-[9px] text-slate-400">(n/a)</span></span>
			</label>
		</div>
	</div>
{/if}

{#if showAssistantDialog}
	<div
		class="fixed top-56 right-24 z-50 w-72 rounded-2xl border border-slate-200 bg-white/95 p-4 shadow-xl backdrop-blur-md"
	>
		<div class="mb-3 flex items-center justify-between border-b border-slate-100 pb-2">
			<h4 class="text-xs font-black tracking-wider text-slate-800 uppercase">Seating Assistant</h4>
			<button
				onclick={() => (showAssistantDialog = false)}
				class="text-slate-400 hover:text-slate-700"
				><svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
					><path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M6 18L18 6M6 6l12 12"
					/></svg
				></button
			>
		</div>
		<form
			onsubmit={(e) => {
				e.preventDefault();
				runAssistantPrompt();
			}}
			class="space-y-3"
		>
			<p class="text-[10px] leading-relaxed font-semibold text-slate-500">
				Try: <code class="rounded bg-slate-100 px-1 text-slate-700">Select VIP</code>,
				<code class="rounded bg-slate-100 px-1 text-slate-700">Select Row A</code>
			</p>
			<div class="flex gap-2">
				<input
					type="text"
					bind:value={assistantPrompt}
					placeholder="Prompt..."
					class="flex-1 rounded-lg border border-slate-200 px-2.5 py-1.5 text-xs font-semibold outline-none focus:border-slate-400"
				/>
				<button
					type="submit"
					class="rounded-lg bg-slate-900 px-3 py-1.5 text-xs font-bold text-white hover:bg-black"
					>Run</button
				>
			</div>
		</form>
	</div>
{/if}
