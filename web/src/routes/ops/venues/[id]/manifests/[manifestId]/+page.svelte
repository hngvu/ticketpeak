<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any, svelte/no-navigation-without-resolve, svelte/require-each-key */
	import { onMount, onDestroy } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';

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
	let gaAreas = $state<any[]>(data.gaAreas || []);
	let rsAreas = $state<any[]>(data.rsAreas || []);
	let brushPriceLevelId = $state(data.priceLevels?.[0]?.id || 'STANDARD');
	let brushSectionId = $state('');

	let activeTab = $state<'sections' | 'seats'>('sections');
	let layoutObjects = $state<any[]>(data.manifest?.objects || []);
	let selectedObjectId = $state<number | null>(null);
	let selectedGaAreaId = $state<string>('');

	let activeMode = $state<'scaling' | 'inventory' | 'floor-edit'>('scaling');
	let selectionTool = $state<'seat' | 'row' | 'section'>('seat');
	let activeTool = $state<'select' | 'pan' | 'brush' | 'eraser'>('select');
	let previousToolBeforeSpace = $state<'select' | 'pan' | 'brush' | 'eraser'>('select');

	let showExportDropdown = $state(false);
	let showFilterDialog = $state(false);
	let showAssistantDialog = $state(false);
	let assistantPrompt = $state('');

	let onlyAvailable = $state(false);
	let onlyAccessible = $state(false);
	let onlyObstructed = $state(false);

	let selectedSeatIds = $state<string[]>([]);
	let selectedRsAreaId = $state('');
	let isSaving = $state(false);
	let saveMessage = $state('');
	let errorMessage = $state('');

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
	let x1 = 0, y1 = 0, x2 = 0, y2 = 0;
	let isShiftPressed = $state(false);

	// FIX 4: viewport state dùng cho minimap
	let viewportX = $state(0);
	let viewportY = $state(0);
	let viewportW = $state(800);
	let viewportH = $state(600);

	// ── Semantic Zoom constants ───────────────────────────────────────────────
	// fitToView() thường cho scale ~0.3–0.8 với sân vận động lớn.
	// Ghế cần hiển thị ngay từ đầu → ZOOM_START thấp hơn mức fit.
	// Cross-fade kết thúc khi zoom vừa đủ thấy từng ghế rõ ràng.
	const ZOOM_START = 0.4;  // scale bắt đầu fade in ghế (dưới mức fit thông thường)
	const ZOOM_END   = 0.9;  // scale kết thúc: ghế hiện 100%, section block mờ hết
	const BASE_RADIUS = 5;   // bán kính cơ sở (px trong không gian canvas)
	const LABEL_HIDE_SCALE = 0.8; // ẩn section label khi scale > 0.8

	const bounds = $derived.by(() => {
		let xs: number[] = [];
		let ys: number[] = [];
		rsAreas.forEach((area) => {
			(area.rows || []).forEach((row: any) => {
				(row.seats || []).forEach((seat: any) => {
					if (seat.positionX !== undefined) xs.push(seat.positionX);
					if (seat.positionY !== undefined) ys.push(seat.positionY);
				});
			});
		});
		gaAreas.forEach((ga, idx) => {
			const x = ga.x || 50 + idx * 220;
			const y = ga.y || 50;
			xs.push(x, x + (ga.width || 200));
			ys.push(y, y + (ga.height || 100));
		});
		layoutObjects.forEach((obj: any) => {
			if (obj.x !== undefined) xs.push(obj.x);
			if (obj.y !== undefined) ys.push(obj.y);
			if (obj.x !== undefined && obj.width) xs.push(obj.x + obj.width);
			if (obj.y !== undefined && obj.height) ys.push(obj.y + obj.height);
		});
		const minX = xs.length ? Math.min(...xs) - 60 : 0;
		const maxX = xs.length ? Math.max(...xs) + 60 : 800;
		const minY = ys.length ? Math.min(...ys) - 60 : 0;
		const maxY = ys.length ? Math.max(...ys) + 60 : 600;
		return { minX, maxX, minY, maxY, width: maxX - minX, height: maxY - minY };
	});

	const totalCapacity = $derived.by(() => {
		let total = 0;
		gaAreas.forEach((ga) => (total += Number(ga.capacity || 0)));
		rsAreas.forEach((area) => {
			(area.rows || []).forEach((row: any) => {
				total += (row.seats || []).length;
			});
		});
		return total;
	});

	// ── Convex Hull (Graham scan) ─────────────────────────────────────────────
	function _convexHull(pts: [number, number][]): [number, number][] {
		if (pts.length < 3) return pts;
		const sorted = [...pts].sort((a, b) => a[0] - b[0] || a[1] - b[1]);
		const cross = (o: [number,number], a: [number,number], b: [number,number]) =>
			(a[0]-o[0])*(b[1]-o[1]) - (a[1]-o[1])*(b[0]-o[0]);
		const lower: [number,number][] = [];
		for (const p of sorted) {
			while (lower.length >= 2 && cross(lower[lower.length-2], lower[lower.length-1], p) <= 0)
				lower.pop();
			lower.push(p);
		}
		const upper: [number,number][] = [];
		for (let i = sorted.length - 1; i >= 0; i--) {
			const p = sorted[i];
			while (upper.length >= 2 && cross(upper[upper.length-2], upper[upper.length-1], p) <= 0)
				upper.pop();
			upper.push(p);
		}
		upper.pop(); lower.pop();
		return [...lower, ...upper];
	}

	function _hullPoints(rows: any[], ox: number, oy: number, pad = 14): number[] {
		const pts: [number, number][] = [];
		for (const row of rows) {
			for (const seat of row.seats || []) {
				const sx = seat.positionX ?? 0;
				const sy = seat.positionY ?? row.positionY ?? 0;
				pts.push([sx, sy]);
			}
		}
		if (pts.length === 0) return [];
		const hull = _convexHull(pts);
		const cx = hull.reduce((s, p) => s + p[0], 0) / hull.length;
		const cy = hull.reduce((s, p) => s + p[1], 0) / hull.length;
		const flat: number[] = [];
		for (const [hx, hy] of hull) {
			const dx = hx - cx, dy = hy - cy;
			const len = Math.sqrt(dx*dx + dy*dy) || 1;
			flat.push(hx + (dx/len)*pad - ox, hy + (dy/len)*pad - oy);
		}
		return flat;
	}

	onMount(() => {
		if (!browser) return;
		async function init() {
			const konvaModule = await import('konva');
			Konva = konvaModule.default;

			const width = stageContainer?.offsetWidth || 800;
			const height = stageContainer?.offsetHeight || 600;

			stage = new Konva.Stage({ container: 'stage-container', width, height, draggable: activeTool === 'pan' });
			layer = new Konva.Layer();
			stage.add(layer);

			selectionRect = new Konva.Rect({
				fill: 'rgba(59, 130, 246, 0.12)',
				stroke: '#3b82f6',
				strokeWidth: 1.5,
				visible: false
			});
			layer.add(selectionRect);

			window.addEventListener('resize', handleResize);
			window.addEventListener('keydown', handleKeyDown);
			window.addEventListener('keyup', handleKeyUp);

			let _wheelRedrawTimer: ReturnType<typeof setTimeout> | null = null;

			// FIX 1: Zoom to mouse position
			stage.on('wheel', (e: any) => {
				e.evt.preventDefault();
				const scaleBy = 1.15;
				const oldScale = stage.scaleX();
				const pointer = stage.getPointerPosition();
				if (!pointer) return;

				const mousePointTo = {
					x: (pointer.x - stage.x()) / oldScale,
					y: (pointer.y - stage.y()) / oldScale
				};

				let newScale = e.evt.deltaY < 0 ? oldScale * scaleBy : oldScale / scaleBy;
				newScale = Math.max(0.15, Math.min(newScale, 8));

				stage.scale({ x: newScale, y: newScale });
				stage.position({
					x: pointer.x - mousePointTo.x * newScale,
					y: pointer.y - mousePointTo.y * newScale
				});
				stage.batchDraw();
				updateViewport();

				if (_wheelRedrawTimer) clearTimeout(_wheelRedrawTimer);
				_wheelRedrawTimer = setTimeout(() => drawSeatingMap(), 60);
			});

			drawSeatingMap();
			setupCanvasEvents();
			fitToView();
			stage.on('dragmove dragend xChange yChange scaleXChange scaleYChange', updateViewport);
		}
		init();
	});

	function deleteSelectedSeats() {
		if (selectedSeatIds.length === 0) return;
		rsAreas.forEach((area) => {
			if (area.rows) {
				area.rows.forEach((row: any) => {
					if (row.seats) row.seats = row.seats.filter((s: any) => !selectedSeatIds.includes(s.id));
				});
				area.rows = area.rows.filter((row: any) => row.seats && row.seats.length > 0);
			}
		});
		selectedSeatIds = [];
		drawSeatingMap();
	}

	function getAreaColor(area: any): string {
		if (area.color) return area.color;
		const counts: Record<string, number> = {};
		for (const row of area.rows || []) {
			for (const seat of row.seats || []) {
				if (seat.sectionId) counts[seat.sectionId] = (counts[seat.sectionId] || 0) + 1;
			}
		}
		let max = 0, domId = '';
		for (const [sid, cnt] of Object.entries(counts)) { if (cnt > max) { max = cnt; domId = sid; } }
		if (domId) { const sec = sections.find((s) => s.id === domId); if (sec) return sec.color; }
		const plCounts: Record<string, number> = {};
		for (const row of area.rows || []) {
			for (const seat of row.seats || []) {
				if (seat.priceLevelId) plCounts[seat.priceLevelId] = (plCounts[seat.priceLevelId] || 0) + 1;
			}
		}
		let plMax = 0, plDomId = '';
		for (const [pid, cnt] of Object.entries(plCounts)) { if (cnt > plMax) { plMax = cnt; plDomId = pid; } }
		if (plDomId) { const pl = priceLevels.find((p) => p.id === plDomId); if (pl) return pl.color; }
		return '#3B82F6';
	}

	function getAreaLabel(area: any): string {
		return area.id.replace(manifestIdVal + '-', '');
	}

	function applyCurvature(area: any, curvature: number) {
		if (!area || !area.rows) return;
		area.curvature = curvature;
		area.rows.forEach((row: any) => {
			const seats = row.seats || [];
			if (seats.length <= 1) return;
			const xs = seats.map((s: any) => s.positionX);
			const minX = Math.min(...xs), maxX = Math.max(...xs);
			const centerX = (minX + maxX) / 2;
			seats.forEach((seat: any) => {
				const dx = seat.positionX - centerX;
				seat.positionY = Math.round(row.positionY + (curvature * dx * dx) / 2000);
			});
		});
		drawSeatingMap();
	}

	function findSeatById(seatId: string) {
		for (const area of rsAreas) {
			for (const row of area.rows || []) {
				const seat = (row.seats || []).find((c: any) => c.id === seatId);
				if (seat) return { area, row, seat };
			}
		}
		return null;
	}

	function removeSeatById(seatId: string) {
		selectedSeatIds = selectedSeatIds.filter((id) => id !== seatId);
		rsAreas.forEach((area) => {
			if (!area.rows) return;
			area.rows.forEach((row: any) => {
				if (row.seats) row.seats = row.seats.filter((s: any) => s.id !== seatId);
			});
			area.rows = area.rows.filter((row: any) => row.seats && row.seats.length > 0);
		});
	}

	function paintSeat(seatId: string, priceLevelId: string) {
		const found = findSeatById(seatId);
		if (found) found.seat.priceLevelId = priceLevelId;
	}

	function paintSection(seatId: string, sectionId: string) {
		const found = findSeatById(seatId);
		if (found) found.seat.sectionId = sectionId;
	}

	function handleKeyDown(e: KeyboardEvent) {
		const activeEl = document.activeElement;
		if (activeEl && (activeEl.tagName === 'INPUT' || activeEl.tagName === 'SELECT' || activeEl.tagName === 'TEXTAREA')) return;
		if (e.key === 'Shift') isShiftPressed = true;
		if (e.code === 'Space') { previousToolBeforeSpace = activeTool; activeTool = 'pan'; e.preventDefault(); }
		if (e.key.toLowerCase() === 'v') activeTool = 'select';
		if (e.key.toLowerCase() === 'b') activeTool = 'brush';
		if (e.key.toLowerCase() === 'e') activeTool = 'eraser';
		if (e.key === 'Delete' || e.key === 'Backspace') deleteSelectedSeats();
		if ((e.ctrlKey || e.metaKey) && (e.key === '=' || e.key === '+')) { e.preventDefault(); zoomIn(); }
		if ((e.ctrlKey || e.metaKey) && e.key === '-') { e.preventDefault(); zoomOut(); }
		if ((e.ctrlKey || e.metaKey) && e.key === '0') { e.preventDefault(); fitToView(); }
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

	function handleResize() {
		if (stage && stageContainer) {
			stage.width(stageContainer.offsetWidth);
			stage.height(stageContainer.offsetHeight);
			layer.batchDraw();
			updateViewport();
		}
	}

	function updateViewport() {
		if (!stage) return;
		const scale = stage.scaleX() || 1;
		viewportX = -stage.x() / scale;
		viewportY = -stage.y() / scale;
		viewportW = stage.width() / scale;
		viewportH = stage.height() / scale;
	}

	function fitToView() {
		if (!stage) return;
		const padding = 40;
		const sw = stage.width() - padding * 2;
		const sh = stage.height() - padding * 2;
		const bw = bounds.width || 1, bh = bounds.height || 1;
		const scale = Math.min(sw / bw, sh / bh, 4);
		stage.scale({ x: scale, y: scale });
		stage.x(padding + (sw - bw * scale) / 2 - bounds.minX * scale);
		stage.y(padding + (sh - bh * scale) / 2 - bounds.minY * scale);
		stage.batchDraw();
		updateViewport();
		drawSeatingMap();
	}

	function zoomIn() {
		if (!stage) return;
		const oldScale = stage.scaleX();
		const newScale = Math.min(oldScale * 1.3, 8);
		const cx = stage.width() / 2, cy = stage.height() / 2;
		const mp = { x: (cx - stage.x()) / oldScale, y: (cy - stage.y()) / oldScale };
		stage.scale({ x: newScale, y: newScale });
		stage.x(cx - mp.x * newScale); stage.y(cy - mp.y * newScale);
		stage.batchDraw(); updateViewport(); drawSeatingMap();
	}

	function zoomOut() {
		if (!stage) return;
		const oldScale = stage.scaleX();
		const newScale = Math.max(oldScale / 1.3, 0.15);
		const cx = stage.width() / 2, cy = stage.height() / 2;
		const mp = { x: (cx - stage.x()) / oldScale, y: (cy - stage.y()) / oldScale };
		stage.scale({ x: newScale, y: newScale });
		stage.x(cx - mp.x * newScale); stage.y(cy - mp.y * newScale);
		stage.batchDraw(); updateViewport(); drawSeatingMap();
	}

	function handleMinimapClick(e: MouseEvent) {
		const svg = e.currentTarget as SVGSVGElement;
		const rect = svg.getBoundingClientRect();
		const canvasX = bounds.minX + ((e.clientX - rect.left) / rect.width) * bounds.width;
		const canvasY = bounds.minY + ((e.clientY - rect.top) / rect.height) * bounds.height;
		if (stage) {
			const scale = stage.scaleX() || 1;
			stage.x(-canvasX * scale + stage.width() / 2);
			stage.y(-canvasY * scale + stage.height() / 2);
			stage.batchDraw(); updateViewport();
		}
	}

	$effect(() => {
		if (stage && stage.container()) {
			stage.draggable(activeTool === 'pan');
			stage.container().style.cursor = activeTool === 'pan' ? 'grab' : activeTool === 'brush' ? 'copy' : activeTool === 'eraser' ? 'cell' : 'default';
		}
	});

	$effect(() => {
		if (!brushPriceLevelId && priceLevels.length > 0) brushPriceLevelId = priceLevels[0].id;
	});

	$effect(() => {
		if (activeMode || onlyAvailable || onlyAccessible || onlyObstructed) drawSeatingMap();
	});

	const RS_AREA_PADDING = 18;

	function computeRsAreaBounds(rows: any[]) {
		let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
		rows.forEach((row: any, rIdx: number) => {
			const rowY = row.positionY ?? 200 + rIdx * 50;
			(row.seats || []).forEach((seat: any, sIdx: number) => {
				const sx = seat.positionX ?? 100 + sIdx * 32;
				const sy = seat.positionY ?? rowY;
				if (sx < minX) minX = sx; if (sx > maxX) maxX = sx;
				if (sy < minY) minY = sy; if (sy > maxY) maxY = sy;
			});
		});
		if (!isFinite(minX)) return null;
		return {
			boxX: minX - RS_AREA_PADDING,
			boxY: minY - RS_AREA_PADDING,
			boxW: maxX - minX + RS_AREA_PADDING * 2,
			boxH: maxY - minY + RS_AREA_PADDING * 2
		};
	}

	function applyRsAreaBounds(area: any, oldBounds: any, newBounds: any) {
		(area.rows || []).forEach((row: any, rIdx: number) => {
			const rowY = row.positionY ?? 200 + rIdx * 50;
			row.positionY = Math.round(newBounds.boxY + ((rowY - oldBounds.boxY) / oldBounds.boxH) * newBounds.boxH);
			(row.seats || []).forEach((seat: any, sIdx: number) => {
				const sx = seat.positionX ?? 100 + sIdx * 32;
				const sy = seat.positionY ?? rowY;
				seat.positionX = Math.round(newBounds.boxX + ((sx - oldBounds.boxX) / oldBounds.boxW) * newBounds.boxW);
				seat.positionY = Math.round(newBounds.boxY + ((sy - oldBounds.boxY) / oldBounds.boxH) * newBounds.boxH);
			});
		});
		area.x = Math.round(newBounds.boxX);
		area.y = Math.round(newBounds.boxY);
		area.width = Math.round(newBounds.boxW);
		area.height = Math.round(newBounds.boxH);
	}

	function drawSeatingMap() {
		if (!stage || !layer) return;

		layer.destroyChildren();
		layer.add(selectionRect);

		const _scale = stage.scaleX() || 1;

		// ── Semantic Zoom với Opacity Fading ──────────────────────────────────
		// ZOOM_START=0.4, ZOOM_END=0.9 khớp với dải scale thực tế của fitToView()
		// S < 0.4  → chỉ section block (opacity=1), ghế ẩn hoàn toàn
		// 0.4–0.9  → cross-fade: section mờ dần, ghế hiện dần
		// S > 0.9  → chỉ ghế (opacity=1), section block ẩn hoàn toàn
		const seatProgress    = Math.max(0, Math.min(1, (_scale - ZOOM_START) / (ZOOM_END - ZOOM_START)));
		const seatOpacity     = seatProgress;        // 0 → 1
		const sectionOpacity  = 1 - seatProgress;    // 1 → 0
		const showSeats       = _scale > ZOOM_START; // render seats chỉ khi đã bắt đầu fade in
		const showSectionLabel = _scale <= LABEL_HIDE_SCALE;

		// Bán kính nghịch biến với √scale
		const seatRadius = BASE_RADIUS / Math.sqrt(_scale);

		drawBackgroundGrid();

		// ── Layout Objects ────────────────────────────────────────────────────
		layoutObjects.forEach((obj, idx) => {
			const objX = obj.x ?? 100, objY = obj.y ?? 100;
			const objW = obj.width ?? 200, objH = obj.height ?? 100;
			const isSelected = selectedObjectId === idx;

			const group = new Konva.Group({ x: objX, y: objY, draggable: activeTool === 'select', id: 'obj-' + idx });

			if (obj.type === 'stage') {
				group.add(new Konva.Rect({ width: objW, height: objH, fill: '#334155', stroke: isSelected ? '#F59E0B' : '#64748B', strokeWidth: isSelected ? 3 : 2, cornerRadius: 8, shadowColor: '#000', shadowBlur: 5, shadowOpacity: 0.15 }));
				group.add(new Konva.Text({ text: (obj.text || 'STAGE').toUpperCase(), fontSize: Math.max(12, Math.min(objW, objH) * 0.2), fontStyle: 'bold', fill: '#F8FAFC', align: 'center', verticalAlign: 'middle', width: objW, height: objH }));
			} else if (obj.type === 'label') {
				const t = new Konva.Text({ text: obj.text || 'Label', fontSize: obj.fontSize || 14, fontStyle: 'bold', fill: isSelected ? '#F59E0B' : obj.color || '#0F172A', align: 'center', verticalAlign: 'middle' });
				group.add(new Konva.Rect({ width: t.width() + 16, height: t.height() + 16, x: -8, y: -8, fill: 'rgba(0,0,0,0)', stroke: isSelected ? '#F59E0B' : 'transparent', strokeWidth: 1.5, cornerRadius: 4 }));
				group.add(t);
			} else {
				const fill = obj.color || '#CBD5E1';
				const opacity = obj.opacity !== undefined ? Number(obj.opacity) : 0.5;
				if (obj.shape === 'circle') {
					const radius = obj.radius || Math.min(objW, objH) / 2;
					group.add(new Konva.Circle({ x: radius, y: radius, radius, fill, opacity, stroke: isSelected ? '#F59E0B' : '#94A3B8', strokeWidth: isSelected ? 3 : 1 }));
				} else {
					group.add(new Konva.Rect({ width: objW, height: objH, fill, opacity, stroke: isSelected ? '#F59E0B' : '#94A3B8', strokeWidth: isSelected ? 3 : 1, cornerRadius: obj.cornerRadius || 4 }));
				}
			}

			group.on('click tap', (e: any) => { e.cancelBubble = true; selectedObjectId = idx; selectedGaAreaId = ''; selectedRsAreaId = ''; activeTab = 'objects'; drawSeatingMap(); });
			group.on('dragend', () => { obj.x = Math.round(group.x()); obj.y = Math.round(group.y()); selectedObjectId = idx; saveMessage = 'Objects updated. Save to persist.'; drawSeatingMap(); });
			group.on('mouseenter', () => { if (activeTool === 'select') stage.container().style.cursor = 'move'; });
			group.on('mouseleave', () => { if (activeTool === 'select') stage.container().style.cursor = 'default'; });
			layer.add(group);
		});

		// ── GA Areas ─────────────────────────────────────────────────────────
		gaAreas.forEach((ga, idx) => {
			const x = ga.x || 50 + idx * 220, y = ga.y || 50;
			const gaW = ga.width || 200, gaH = ga.height || 100;
			const isSelectedGa = selectedGaAreaId === ga.id;

			const group = new Konva.Group({ x, y, draggable: activeTool === 'select', id: ga.id });
			group.add(new Konva.Rect({ width: gaW, height: gaH, fill: isSelectedGa ? '#F1F5F9' : '#F8FAFC', stroke: isSelectedGa ? '#0F172A' : '#CBD5E1', strokeWidth: isSelectedGa ? 2.5 : 1.5, cornerRadius: 12 }));
			group.add(new Konva.Text({ text: (ga.name || ga.id.replace(manifestIdVal + '-', '')).toUpperCase(), fontSize: Math.max(14, Math.min(gaW, gaH) * 0.2), fontStyle: 'bold', fill: '#94A3B8', opacity: 0.4, x: 0, y: 0, width: gaW, height: gaH, align: 'center', verticalAlign: 'middle' }));

			group.on('click tap', (e: any) => { e.cancelBubble = true; selectedGaAreaId = ga.id; selectedRsAreaId = ''; selectedObjectId = null; activeTab = 'areas'; drawSeatingMap(); });
			group.on('dragend', () => { ga.x = Math.round(group.x() / snapGrid) * snapGrid; ga.y = Math.round(group.y() / snapGrid) * snapGrid; selectedGaAreaId = ga.id; drawSeatingMap(); updateViewport(); });
			group.on('mouseenter', () => { if (activeTool === 'select') stage.container().style.cursor = 'move'; });
			group.on('mouseleave', () => { if (activeTool === 'select') stage.container().style.cursor = 'default'; });
			layer.add(group);
		});

		// ── RS Areas ─────────────────────────────────────────────────────────
		rsAreas.forEach((area) => {
			const rows = area.rows || [];
			const b = computeRsAreaBounds(rows);
			if (!b) return;
			const { boxX, boxY, boxW, boxH } = b;

			const areaGroup = new Konva.Group({ x: boxX, y: boxY, draggable: activeTool === 'select', id: 'group-' + area.id });
			const areaColor = getAreaColor(area);
			const areaLabel = getAreaLabel(area);
			const isSelectedArea = selectedRsAreaId === area.id;

			// Section hull: fade out theo sectionOpacity
			const hullPts = _hullPoints(rows, boxX, boxY, RS_AREA_PADDING - 4);
			if (hullPts.length >= 6) {
				areaGroup.add(new Konva.Line({
					points: hullPts,
					closed: true,
					fill: isSelectedArea ? `rgba(241,245,249,${sectionOpacity})` : `rgba(248,250,252,${sectionOpacity})`,
					stroke: isSelectedArea ? `rgba(15,23,42,${sectionOpacity})` : `rgba(203,213,225,${Math.max(sectionOpacity, 0.12)})`,
					strokeWidth: isSelectedArea ? 2.5 : 1.5
				}));
			} else {
				areaGroup.add(new Konva.Rect({
					x: 0, y: 0, width: boxW, height: boxH,
					fill: isSelectedArea ? `rgba(241,245,249,${sectionOpacity})` : `rgba(248,250,252,${sectionOpacity})`,
					stroke: isSelectedArea ? `rgba(15,23,42,${sectionOpacity})` : `rgba(203,213,225,${Math.max(sectionOpacity, 0.12)})`,
					strokeWidth: isSelectedArea ? 2.5 : 1.5,
					cornerRadius: 8
				}));
			}

			// Label phân khu: ẩn khi scale > LABEL_HIDE_SCALE
			if (showSectionLabel && sectionOpacity > 0.05) {
				const labelW = boxW * 0.8, labelH = 24;
				const fontSize = Math.max(8, Math.min(16, labelW / (areaLabel.length * 0.6 + 1)));
				areaGroup.add(new Konva.Text({
					text: areaLabel.toUpperCase(),
					fontSize,
					fontStyle: 'bold',
					fill: '#94A3B8',
					opacity: sectionOpacity * 0.6,
					align: 'center',
					verticalAlign: 'middle',
					x: boxW / 2 - labelW / 2,
					y: boxH / 2 - labelH / 2,
					width: labelW,
					height: labelH
				}));
			}

			areaGroup.on('click tap', () => { selectedRsAreaId = area.id; selectedGaAreaId = ''; selectedObjectId = null; drawSeatingMap(); });
			areaGroup.on('mouseenter', () => { if (activeTool === 'select') stage.container().style.cursor = 'move'; });
			areaGroup.on('mouseleave', () => { if (activeTool === 'select') stage.container().style.cursor = 'default'; });
			areaGroup.on('dragend', () => {
				selectedRsAreaId = area.id;
				const dx = Math.round((areaGroup.x() - boxX) / snapGrid) * snapGrid;
				const dy = Math.round((areaGroup.y() - boxY) / snapGrid) * snapGrid;
				rows.forEach((row: any, rIdx: number) => {
					const rowY = row.positionY ?? 200 + rIdx * 50;
					row.positionY = rowY + dy;
					(row.seats || []).forEach((seat: any, sIdx: number) => {
						seat.positionX = (seat.positionX ?? 100 + sIdx * 32) + dx;
						seat.positionY = (seat.positionY ?? rowY) + dy;
					});
				});
				drawSeatingMap(); updateViewport();
			});

			// ── Seats: chỉ render khi showSeats=true, dùng seatOpacity để fade in ──
			if (showSeats && seatOpacity > 0.01) {
				rows.forEach((row: any, rIdx: number) => {
					const rowY = row.positionY ?? 200 + rIdx * 50;
					(row.seats || []).forEach((seat: any, sIdx: number) => {
						const seatX = seat.positionX ?? 100 + sIdx * 32;
						const seatY = seat.positionY ?? rowY;
						const relX = seatX - boxX, relY = seatY - boxY;

						// Viewport Culling
						const vpBuffer = seatRadius * 3;
						if (
							seatX < viewportX - vpBuffer || seatX > viewportX + viewportW + vpBuffer ||
							seatY < viewportY - vpBuffer || seatY > viewportY + viewportH + vpBuffer
						) return;

						let isFiltered = false;
						if (onlyAvailable && seat.status !== 'AVAILABLE') isFiltered = true;
						if (onlyAccessible && !seat.accessibility) isFiltered = true;
						if (onlyObstructed && !seat.obstructedView) isFiltered = true;

						const isSelected = selectedSeatIds.includes(seat.id);

						let seatColor = areaColor;
						if (!isSelected && activeMode !== 'inventory' && seat.status !== 'UNAVAILABLE') {
							const sec = seat.sectionId ? sections.find((s) => s.id === seat.sectionId) : null;
							if (sec) {
								seatColor = sec.color;
							} else {
								const pl = seat.priceLevelId ? priceLevels.find((p) => p.id === seat.priceLevelId) : null;
								if (pl) seatColor = pl.color;
							}
						}
						if (isSelected) seatColor = '#1A1A1A';
						else if (activeMode === 'inventory') seatColor = seat.status === 'AVAILABLE' ? '#10B981' : '#EF4444';
						else if (seat.status === 'UNAVAILABLE') seatColor = '#E2E8F0';

						const seatGroup = new Konva.Group({ x: relX, y: relY, id: seat.id, draggable: activeMode === 'floor-edit' });

						seatGroup.add(new Konva.Circle({
							radius: seatRadius,
							fill: seatColor,
							opacity: isFiltered ? seatOpacity * 0.15 : seatOpacity,
							stroke: isSelected ? '#FFFFFF' : seat.obstructedView ? '#EF4444' : 'transparent',
							strokeWidth: isSelected ? 1.5 / _scale : 0,
							shadowColor: '#000',
							shadowBlur: isSelected ? 3 : 0,
							shadowOpacity: 0.15
						}));

						if (seat.accessibility) seatGroup.add(new Konva.Circle({ radius: seatRadius * 0.4, fill: '#FFFFFF', opacity: seatOpacity }));
						else if (seat.aisle) seatGroup.add(new Konva.Line({ points: [-seatRadius * 0.4, 0, seatRadius * 0.4, 0], stroke: '#FFFFFF', strokeWidth: 1 / _scale, opacity: seatOpacity }));

						seatGroup.getChildren()[0].on('click tap', (e: any) => {
							if (activeTool !== 'select' && activeTool !== 'brush' && activeTool !== 'eraser') return;
							e.cancelBubble = true;
							if (activeTool === 'brush') {
								if (brushSectionId) { paintSection(seat.id, brushSectionId); drawSeatingMap(); }
								else { const tpl = brushPriceLevelId || area.priceLevelId; if (tpl) { paintSeat(seat.id, tpl); drawSeatingMap(); } }
								return;
							}
							if (activeTool === 'eraser') { removeSeatById(seat.id); drawSeatingMap(); return; }
							let targetIds: string[] = [];
							if (selectionTool === 'seat') targetIds = [seat.id];
							else if (selectionTool === 'row') targetIds = row.seats.map((s: any) => s.id);
							else if (selectionTool === 'section') rows.forEach((r: any) => targetIds.push(...(r.seats || []).map((s: any) => s.id)));
							const allSel = targetIds.every((id) => selectedSeatIds.includes(id));
							selectedSeatIds = allSel ? selectedSeatIds.filter((id) => !targetIds.includes(id)) : Array.from(new Set([...selectedSeatIds, ...targetIds]));
							drawSeatingMap();
						});

						seatGroup.getChildren()[0].on('mouseenter', () => { if (activeTool !== 'pan') stage.container().style.cursor = 'pointer'; });
						seatGroup.getChildren()[0].on('mouseleave', () => { if (activeTool !== 'pan') stage.container().style.cursor = 'default'; });

						seatGroup.on('dragend', () => {
							seat.positionX = boxX + Math.round(seatGroup.x() / snapGrid) * snapGrid;
							seat.positionY = boxY + Math.round(seatGroup.y() / snapGrid) * snapGrid;
							drawSeatingMap(); updateViewport();
						});

						areaGroup.add(seatGroup);
					});
				});
			}

			layer.add(areaGroup);
		});

		// ── Transformers ─────────────────────────────────────────────────────
		const addTransformer = (nodeId: string, onEnd: (t: any) => void) => {
			const transformer = new Konva.Transformer({ rotateEnabled: false, keepRatio: false, enabledAnchors: ['top-left','top-right','bottom-left','bottom-right','top-center','bottom-center','middle-left','middle-right'], boundBoxFunc: (o: any, n: any) => (n.width < 20 || n.height < 20 ? o : n) });
			layer.add(transformer);
			const node = layer.findOne('#' + nodeId);
			if (node) { transformer.nodes([node]); node.on('transformend', () => onEnd(node)); }
		};

		if (selectedObjectId !== null && activeTool === 'select') {
			addTransformer('obj-' + selectedObjectId, (node) => {
				const obj = layoutObjects[selectedObjectId!];
				if (!obj) return;
				const sx = node.scaleX(), sy = node.scaleY();
				node.scaleX(1); node.scaleY(1);
				if (obj.type === 'stage' || (obj.type === 'shape' && obj.shape !== 'circle')) { obj.width = Math.round((obj.width ?? 200) * sx); obj.height = Math.round((obj.height ?? 100) * sy); }
				else if (obj.type === 'shape' && obj.shape === 'circle') { obj.radius = Math.round((obj.radius || 50) * Math.max(sx, sy)); }
				else if (obj.type === 'label') { obj.fontSize = Math.round((obj.fontSize || 14) * Math.max(sx, sy)); }
				obj.x = Math.round(node.x()); obj.y = Math.round(node.y());
				saveMessage = 'Object resized. Save to persist.'; drawSeatingMap();
			});
		}

		if (selectedGaAreaId !== '' && activeTool === 'select') {
			addTransformer(selectedGaAreaId, (node) => {
				const ga = gaAreas.find((g) => g.id === selectedGaAreaId);
				if (!ga) return;
				const sx = node.scaleX(), sy = node.scaleY();
				node.scaleX(1); node.scaleY(1);
				ga.width = Math.max(50, Math.round((ga.width || 200) * sx));
				ga.height = Math.max(50, Math.round((ga.height || 100) * sy));
				ga.x = Math.round(node.x()); ga.y = Math.round(node.y());
				saveMessage = 'GA Area resized. Save to persist.'; drawSeatingMap();
			});
		}

		if (selectedRsAreaId !== '' && activeTool === 'select') {
			addTransformer('group-' + selectedRsAreaId, (node) => {
				const area = rsAreas.find((a) => a.id === selectedRsAreaId);
				if (!area) return;
				const oldB = computeRsAreaBounds(area.rows || []);
				if (!oldB) return;
				const sx = node.scaleX(), sy = node.scaleY();
				node.scaleX(1); node.scaleY(1);
				const newB = { boxX: Math.round(node.x()), boxY: Math.round(node.y()), boxW: Math.max(50, Math.round(oldB.boxW * sx)), boxH: Math.max(50, Math.round(oldB.boxH * sy)) };
				applyRsAreaBounds(area, oldB, newB);
				rsAreas = [...rsAreas];
				saveMessage = 'RS Area resized. Save to persist.'; drawSeatingMap();
			});
		}

		layer.batchDraw();
	}

	function drawBackgroundGrid() {
		if (!stage || !layer) return;
		for (let i = -10; i < 60; i++) {
			layer.add(new Konva.Line({ points: [i * 40, -1000, i * 40, 2000], stroke: '#F1F5F9', strokeWidth: 1 }));
			layer.add(new Konva.Line({ points: [-1000, i * 40, 2000, i * 40], stroke: '#F1F5F9', strokeWidth: 1 }));
		}
	}

	function setupCanvasEvents() {
		if (!stage) return;
		let isMouseDown = false;
		let brushModeActive = false;

		stage.on('mousedown touchstart', (e: any) => {
			if (e.target === stage) { selectedObjectId = null; selectedGaAreaId = ''; selectedRsAreaId = ''; drawSeatingMap(); }
			isMouseDown = true;

			const target = e.target;
			const isSeat = target?.className === 'Circle' && target?.parent?.id() && !target.parent.id().startsWith('group-') && !target.parent.id().startsWith('obj-');
			if (isSeat) {
				brushModeActive = true;
				const seatId = target.parent?.id() || target.id();
				if (seatId && !seatId.startsWith('group-') && seatId !== stage.id()) {
					if (activeTool === 'eraser') { removeSeatById(seatId); drawSeatingMap(); }
					else if (activeTool === 'brush') {
						if (brushSectionId) { paintSection(seatId, brushSectionId); drawSeatingMap(); }
						else { const tpl = brushPriceLevelId || findSeatById(seatId)?.area?.priceLevelId; if (tpl) { paintSeat(seatId, tpl); drawSeatingMap(); } }
					} else if (activeTool === 'select') {
						if (isShiftPressed) { selectedSeatIds = selectedSeatIds.includes(seatId) ? selectedSeatIds.filter((id) => id !== seatId) : [...selectedSeatIds, seatId]; }
						else if (!selectedSeatIds.includes(seatId)) selectedSeatIds = [...selectedSeatIds, seatId];
						drawSeatingMap();
					}
				}
				return;
			}

			if (activeTool !== 'select' || e.target !== stage) return;
			brushModeActive = false;
			const pos = stage.getPointerPosition();
			if (!pos) return;
			const scale = stage.scaleX() || 1;
			x1 = (pos.x - stage.x()) / scale; y1 = (pos.y - stage.y()) / scale;
			x2 = x1; y2 = y1;
			selectionRect.x(x1); selectionRect.y(y1); selectionRect.width(0); selectionRect.height(0); selectionRect.visible(true);
			layer.batchDraw();
		});

		stage.on('mousemove touchmove', () => {
			if (!isMouseDown) return;
			if (brushModeActive) {
				const pos = stage.getPointerPosition();
				if (!pos) return;
				const shape = stage.getIntersection(pos);
				if (shape?.className === 'Circle' && !shape.parent?.id()?.startsWith('obj-')) {
					const seatId = shape.parent?.id() || shape.id();
					if (seatId && !seatId.startsWith('group-') && seatId !== stage.id()) {
						if (activeTool === 'eraser') { removeSeatById(seatId); drawSeatingMap(); }
						else if (activeTool === 'brush') {
							if (brushSectionId) { paintSection(seatId, brushSectionId); drawSeatingMap(); }
							else { const tpl = brushPriceLevelId || findSeatById(seatId)?.area?.priceLevelId; if (tpl) { paintSeat(seatId, tpl); drawSeatingMap(); } }
						} else if (activeTool === 'select' && !selectedSeatIds.includes(seatId)) { selectedSeatIds = [...selectedSeatIds, seatId]; drawSeatingMap(); }
					}
				}
				return;
			}
			if (!selectionRect.visible()) return;
			const pos = stage.getPointerPosition();
			if (!pos) return;
			const scale = stage.scaleX() || 1;
			x2 = (pos.x - stage.x()) / scale; y2 = (pos.y - stage.y()) / scale;
			selectionRect.setAttrs({ x: Math.min(x1,x2), y: Math.min(y1,y2), width: Math.abs(x2-x1), height: Math.abs(y2-y1) });
			layer.batchDraw();
		});

		stage.on('mouseup touchend', () => {
			isMouseDown = false; brushModeActive = false;
			if (!selectionRect.visible()) return;
			selectionRect.visible(false);
			const rx = selectionRect.x(), ry = selectionRect.y(), rw = selectionRect.width(), rh = selectionRect.height();
			const selected: string[] = [];
			rsAreas.forEach((area) => {
				(area.rows || []).forEach((row: any) => {
					(row.seats || []).forEach((seat: any) => {
						const sx = seat.positionX ?? 0, sy = seat.positionY ?? row.positionY ?? 0;
						if (sx >= rx && sx <= rx+rw && sy >= ry && sy <= ry+rh) {
							if (selectionTool === 'seat') selected.push(seat.id);
							else if (selectionTool === 'row') row.seats.forEach((s: any) => selected.push(s.id));
							else if (selectionTool === 'section') (area.rows || []).forEach((r: any) => r.seats.forEach((s: any) => selected.push(s.id)));
						}
					});
				});
			});
			if (selected.length > 0) selectedSeatIds = isShiftPressed ? Array.from(new Set([...selectedSeatIds, ...selected])) : selected;
			else if (!isShiftPressed) selectedSeatIds = [];
			drawSeatingMap();
		});
	}

	function assignPriceLevelToSelected(priceLevelId: string) {
		if (selectedSeatIds.length === 0) return;
		rsAreas.forEach((area) => (area.rows || []).forEach((row: any) => (row.seats || []).forEach((seat: any) => { if (selectedSeatIds.includes(seat.id)) seat.priceLevelId = priceLevelId; })));
		rsAreas = [...rsAreas];
		saveMessage = `Assigned ${priceLevelId} to selected seats!`;
		selectedSeatIds = []; drawSeatingMap();
	}

	function assignSectionToSelected(sectionId: string) {
		if (selectedSeatIds.length === 0) return;
		rsAreas.forEach((area) => (area.rows || []).forEach((row: any) => (row.seats || []).forEach((seat: any) => { if (selectedSeatIds.includes(seat.id)) seat.sectionId = sectionId || null; })));
		rsAreas = [...rsAreas];
		saveMessage = `Assigned section ${sectionId || 'None'} to selected seats!`;
		selectedSeatIds = []; drawSeatingMap();
	}

	function clearSectionFromSelected() { assignSectionToSelected(''); }

	function setInventoryStatus(status: 'AVAILABLE' | 'UNAVAILABLE') {
		if (selectedSeatIds.length === 0) return;
		rsAreas.forEach((area) => (area.rows || []).forEach((row: any) => (row.seats || []).forEach((seat: any) => { if (selectedSeatIds.includes(seat.id)) seat.status = status; })));
		rsAreas = [...rsAreas];
		saveMessage = `Marked ${selectedSeatIds.length} seats as ${status}`;
		selectedSeatIds = []; drawSeatingMap();
	}

	function runAssistantPrompt() {
		if (!assistantPrompt) return;
		const query = assistantPrompt.toLowerCase();
		let targetIds: string[] = [];
		if (query.includes('vip') || query.includes('gold')) {
			rsAreas.forEach((area) => (area.rows || []).forEach((row: any) => (row.seats || []).forEach((s: any) => { if (s.priceLevelId?.toLowerCase().includes('vip') || s.priceLevelId?.toLowerCase().includes('gold')) targetIds.push(s.id); })));
		} else if (query.includes('standard') || query.includes('p1')) {
			rsAreas.forEach((area) => (area.rows || []).forEach((row: any) => (row.seats || []).forEach((s: any) => { if (s.priceLevelId?.toLowerCase().includes('standard') || s.priceLevelId?.toLowerCase().includes('p1')) targetIds.push(s.id); })));
		} else if (query.includes('row')) {
			const match = query.match(/row\s+([a-z0-9]+)/i);
			if (match) { const rowName = match[1].toUpperCase(); rsAreas.forEach((area) => (area.rows || []).forEach((row: any) => { if (row.name.toUpperCase() === rowName) targetIds.push(...(row.seats || []).map((s: any) => s.id)); })); }
		} else if (query.includes('all')) {
			rsAreas.forEach((area) => (area.rows || []).forEach((row: any) => targetIds.push(...(row.seats || []).map((s: any) => s.id))));
		}
		if (targetIds.length > 0) { selectedSeatIds = targetIds; saveMessage = `Assistant selected ${targetIds.length} seats.`; errorMessage = ''; drawSeatingMap(); }
		else errorMessage = `No matching seats for '${assistantPrompt}'.`;
		assistantPrompt = '';
	}

	function exportLayout(format: 'json' | 'csv' | 'svg') {
		let filename = `manifest-${manifestIdVal || 'export'}-${Date.now()}`;
		let content = '', mimeType = 'text/plain';
		if (format === 'json') { content = JSON.stringify({ levels, sections, priceLevels, gaAreas, rsAreas }, null, 2); mimeType = 'application/json'; filename += '.json'; }
		else if (format === 'csv') {
			const rows = ['SeatID,AreaID,Row,SeatName,PositionX,PositionY,Status,Accessibility,ObstructedView,Aisle'];
			rsAreas.forEach((area) => (area.rows || []).forEach((row: any) => (row.seats || []).forEach((seat: any) => rows.push([seat.id, area.id, row.name, seat.name, seat.positionX, seat.positionY ?? row.positionY, seat.status, seat.accessibility||false, seat.obstructedView||false, seat.aisle||false].join(',')))));
			content = rows.join('\n'); mimeType = 'text/csv'; filename += '.csv';
		} else if (format === 'svg') {
			let s = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${bounds.minX} ${bounds.minY} ${bounds.width} ${bounds.height}">\n`;
			rsAreas.forEach((area) => (area.rows || []).forEach((row: any) => (row.seats || []).forEach((seat: any) => {
				const sec = seat.sectionId ? sections.find((sc: any) => sc.id === seat.sectionId) : null;
				s += `  <circle cx="${seat.positionX}" cy="${seat.positionY ?? row.positionY}" r="10" fill="${seat.status === 'UNAVAILABLE' ? '#E2E8F0' : (sec?.color || '#3B82F6')}" />\n`;
			})));
			s += '</svg>'; content = s; mimeType = 'image/svg+xml'; filename += '.svg';
		}
		const blob = new Blob([content], { type: mimeType });
		const url = URL.createObjectURL(blob);
		const link = document.createElement('a');
		link.href = url; link.download = filename; link.click();
		URL.revokeObjectURL(url);
	}

	function generateSeatingBlock() {
		if (!selectedRsAreaId) { errorMessage = 'Please select a Reserved Seating Area first.'; return; }
		const targetArea = rsAreas.find((a) => a.id === selectedRsAreaId);
		if (!targetArea) return;
		const startRowCode = blockRowPrefix.toUpperCase().charCodeAt(0);
		const rowsList = targetArea.rows || [];
		for (let rIdx = 0; rIdx < blockRowsCount; rIdx++) {
			const rowName = String.fromCharCode(startRowCode + rIdx);
			const rowId = `${selectedRsAreaId}-row-${rowName}-${Date.now()}`;
			const rowY = 220 + rowsList.length * 48;
			const newRow: any = { id: rowId, name: rowName, positionY: rowY, seats: [] };
			let rowSeatCount = blockSeatsPerRow, startXOffset = 0;
			if (blockShape === 'trapezoid') { rowSeatCount = blockSeatsPerRow + rIdx * 2; startXOffset = -rIdx * 16; }
			else if (blockShape === 'diamond') { const mid = Math.floor(blockRowsCount / 2), diff = Math.abs(rIdx - mid); rowSeatCount = Math.max(1, blockSeatsPerRow - diff * 2); startXOffset = diff * 16; }
			else if (blockShape === 'staggered') { startXOffset = rIdx % 2 === 1 ? 16 : 0; }
			for (let sIdx = 0; sIdx < rowSeatCount; sIdx++) {
				const seatNum = blockSeatStartNum + sIdx;
				newRow.seats.push({ id: `${rowId}-seat-${seatNum}`, name: String(seatNum).padStart(3, '0'), positionX: 120 + sIdx * 32 + startXOffset, status: 'AVAILABLE', accessibility: false, obstructedView: false, aisle: false });
			}
			rowsList.push(newRow);
		}
		targetArea.rows = rowsList; rsAreas = [...rsAreas]; drawSeatingMap(); errorMessage = '';
	}

	function toggleSelectedAttribute(attr: 'accessibility' | 'obstructedView' | 'aisle' | 'status') {
		if (selectedSeatIds.length === 0) return;
		rsAreas.forEach((area) => (area.rows || []).forEach((row: any) => (row.seats || []).forEach((seat: any) => {
			if (selectedSeatIds.includes(seat.id)) {
				if (attr === 'status') seat.status = seat.status === 'AVAILABLE' ? 'UNAVAILABLE' : 'AVAILABLE';
				else seat[attr] = !seat[attr];
			}
		})));
		rsAreas = [...rsAreas]; drawSeatingMap();
	}

	function createLookup(type: 'level' | 'section') {
		const code = type === 'level' ? levelCode : sectionCode;
		const color = type === 'level' ? levelColor : sectionColor;
		if (!code) { errorMessage = 'Code is required.'; return; }
		const newLookup = { id: code.trim(), description: code.trim(), color };
		if (type === 'level') { levels = [...levels, newLookup]; levelCode = ''; }
		else { sections = [...sections, newLookup]; sectionCode = ''; }
		saveMessage = `Added ${type.toUpperCase()} locally. Save to persist.`; errorMessage = '';
	}

	function deleteSelectedObject() {
		if (selectedObjectId === null) return;
		layoutObjects = layoutObjects.filter((_, idx) => idx !== selectedObjectId);
		selectedObjectId = null; saveMessage = 'Object removed. Save to persist.'; drawSeatingMap();
	}

	async function saveLayout() {
		isSaving = true; saveMessage = 'Saving...'; errorMessage = '';
		try {
			await fetch(`/api/ops/venues/manifests/${manifestIdVal}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ description: manifestDescVal, totalCapacity: Number(manifestCapVal), objects: layoutObjects }) });
			for (const lvl of levels) await fetch(`/api/ops/venues/manifests/${manifestIdVal}/levels`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(lvl) });
			for (const sec of sections) await fetch(`/api/ops/venues/manifests/${manifestIdVal}/sections`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(sec) });
			for (const pl of priceLevels) await fetch(`/api/ops/venues/manifests/${manifestIdVal}/price-levels`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(pl) });
			for (const ga of gaAreas) await fetch(`/api/ops/venues/manifests/${manifestIdVal}/ga-areas`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id: ga.id, levelId: ga.levelId, sectionId: ga.sectionId, priceLevelId: ga.priceLevelId, capacity: Number(ga.capacity), x: ga.x ?? null, y: ga.y ?? null, width: ga.width || 200, height: ga.height || 100 }) });
			for (const area of rsAreas) {
				let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
				(area.rows || []).forEach((row: any) => (row.seats || []).forEach((seat: any) => { const sx = seat.positionX || 0, sy = seat.positionY ?? (row.positionY || 0); if (sx<minX) minX=sx; if (sx>maxX) maxX=sx; if (sy<minY) minY=sy; if (sy>maxY) maxY=sy; }));
				const pad = 25;
				await fetch(`/api/ops/venues/manifests/${manifestIdVal}/rs-areas`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id: area.id, levelId: area.levelId, x: isFinite(minX) ? minX-pad : 0, y: isFinite(minY) ? minY-pad : 0, width: isFinite(maxX) ? maxX-minX+pad*2 : 0, height: isFinite(maxY) ? maxY-minY+pad*2 : 0 }) });
				for (const row of area.rows || []) {
					await fetch(`/api/ops/venues/rs-areas/${area.id}/rows`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id: row.id, name: row.name, positionY: row.positionY }) });
					for (const seat of row.seats || []) {
						await fetch(`/api/ops/venues/rows/${row.id}/seats`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id: seat.id, name: seat.name, positionX: seat.positionX, positionY: seat.positionY ?? row.positionY, accessibility: seat.accessibility, obstructedView: seat.obstructedView, aisle: seat.aisle, status: seat.status, priceLevelId: seat.priceLevelId || null, sectionId: seat.sectionId || null }) });
					}
				}
			}
			saveMessage = 'Layout saved successfully!'; selectedSeatIds = []; drawSeatingMap();
			if (data.isNew) setTimeout(() => goto(`/ops/venues/${venue.id}/manifests/${manifestIdVal}`, { invalidateAll: true }), 1000);
		} catch (err: any) { errorMessage = err.message || 'Error saving layout.'; } finally { isSaving = false; }
	}
</script>

<div class="flex h-screen w-screen flex-col overflow-hidden bg-slate-50 font-sans select-none">
	<!-- Top Nav -->
	<header class="z-50 flex h-16 shrink-0 items-center justify-between border-b border-slate-200 bg-white px-6">
		<div class="flex items-center gap-4">
			<a href="/ops/venues/{venue?.id}/manifests" class="text-slate-400 transition-colors hover:text-slate-800" aria-label="Back">
				<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
			</a>
			<div>
				<div class="text-[9px] leading-none font-black tracking-widest text-slate-400 uppercase">Interactive Builder</div>
				<div class="mt-1 flex items-center gap-2">
					<span class="rounded bg-slate-100 px-1.5 py-0.5 text-xs font-black text-slate-700">{venue?.name || 'Venue'}</span>
					<span class="text-slate-300">|</span>
					<input type="text" bind:value={manifestDescVal} placeholder="Manifest description" class="w-44 border-b border-transparent bg-transparent px-1 text-xs font-bold text-slate-800 transition outline-none hover:border-slate-300 focus:border-slate-500 focus:bg-white" />
					<span class="text-slate-300">|</span>
					<span class="text-[9px] font-bold tracking-wider text-slate-400 uppercase">Code:</span>
					<input type="text" bind:value={manifestIdVal} placeholder="Code" class="w-20 border-b border-transparent bg-transparent px-1 font-mono text-xs font-bold text-slate-600 transition outline-none hover:border-slate-300 focus:border-slate-500 focus:bg-white" />
					<span class="text-slate-300">|</span>
					<span class="text-[9px] font-bold tracking-wider text-slate-400 uppercase">Capacity:</span>
					<input type="number" bind:value={manifestCapVal} placeholder="Capacity" class="w-16 border-b border-transparent bg-transparent px-1 text-xs font-bold text-slate-600 transition outline-none hover:border-slate-300 focus:border-slate-500 focus:bg-white" />
				</div>
			</div>
		</div>

		<div class="flex rounded-xl border border-slate-200/50 bg-slate-100 p-0.5 shadow-inner">
			{#each ['scaling', 'inventory', 'floor-edit'] as mode}
				<button onclick={() => { activeMode = mode as any; selectedSeatIds = []; }} class="rounded-lg px-4 py-1.5 text-xs font-bold capitalize transition-all select-none {activeMode === mode ? 'bg-white text-slate-900 shadow-xs' : 'text-slate-500 hover:text-slate-800'}">
					{mode === 'floor-edit' ? 'Floor Edit' : mode.charAt(0).toUpperCase() + mode.slice(1)}
				</button>
			{/each}
		</div>

		<div class="flex items-center gap-3">
			<div class="relative">
				<button onclick={() => (showExportDropdown = !showExportDropdown)} class="flex items-center gap-2 rounded-lg bg-slate-900 px-4 py-2 text-xs font-bold text-white shadow-xs transition hover:bg-slate-800 active:scale-95">
					<span>Export</span>
					<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7" /></svg>
				</button>
				{#if showExportDropdown}
					<div class="absolute right-0 z-60 mt-2 w-48 rounded-lg border border-slate-200 bg-white py-1 shadow-xl">
						<button onclick={() => { exportLayout('json'); showExportDropdown = false; }} class="w-full px-4 py-2.5 text-left text-xs font-bold text-slate-700 hover:bg-slate-50">Export Layout (JSON)</button>
						<button onclick={() => { exportLayout('csv'); showExportDropdown = false; }} class="w-full px-4 py-2.5 text-left text-xs font-bold text-slate-700 hover:bg-slate-50">Export Seats (CSV)</button>
						<button onclick={() => { exportLayout('svg'); showExportDropdown = false; }} class="w-full px-4 py-2.5 text-left text-xs font-bold text-slate-700 hover:bg-slate-50">Export Blueprint (SVG)</button>
					</div>
				{/if}
			</div>
		</div>
	</header>

	<div class="relative flex min-h-0 flex-1">
		<!-- Left Sidebar -->
		<aside class="z-40 flex w-[20%] min-w-[280px] flex-col border-r border-slate-200 bg-white select-none">
			{#if activeMode === 'scaling'}
				<div class="flex shrink-0 border-b border-slate-200 text-[10px] font-black tracking-wider text-slate-400 uppercase">
					<button type="button" onclick={() => (activeTab = 'sections')} class="flex-1 border-b-2 py-3 text-center transition-all {activeTab === 'sections' ? 'border-slate-900 font-extrabold text-slate-900' : 'border-transparent text-slate-400 hover:bg-slate-50'}">Sections</button>
					<button type="button" onclick={() => (activeTab = 'seats')} class="flex-1 border-b-2 py-3 text-center transition-all {activeTab === 'seats' ? 'border-slate-900 font-extrabold text-slate-900' : 'border-transparent text-slate-400 hover:bg-slate-50'}">Seats</button>
				</div>

				<div class="flex min-h-0 flex-1 flex-col overflow-y-auto">
					<div class="space-y-4 p-4">
						{#if activeTab === 'sections'}
							<div class="space-y-4">
								<h3 class="text-xs font-bold text-slate-800">Manage Sections</h3>
								<div class="space-y-2">
									{#each sections as sec}
										<div class="flex items-center justify-between rounded-lg border border-slate-100 bg-slate-50/50 p-2 text-xs font-bold">
											<span class="flex items-center gap-2">
												<span class="h-3 w-3 rounded-full border border-white shadow-2xs" style="background-color: {sec.color}"></span>
												{sec.description || sec.id}
											</span>
											<span class="font-mono text-[9px] text-slate-400">{sec.id}</span>
										</div>
									{/each}
								</div>
								<form onsubmit={(e) => { e.preventDefault(); createLookup('section'); }} class="space-y-3 border-t border-slate-100 pt-4 font-semibold text-slate-700">
									<h4 class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">Add New Section</h4>
									<div class="space-y-1">
										<label for="section-code-input" class="text-[10px] text-slate-500">Section Code</label>
										<input id="section-code-input" type="text" bind:value={sectionCode} placeholder="e.g. SEC-A, VIP-1" class="w-full rounded-md border border-slate-200 px-3 py-1.5 text-xs outline-none" required />
									</div>
									<div class="space-y-1">
										<label for="section-color-input" class="text-[10px] text-slate-500">Color Tag</label>
										<div class="flex items-center gap-2">
											<input id="section-color-input" type="color" bind:value={sectionColor} class="h-8 w-8 cursor-pointer rounded border border-slate-200" />
											<span class="font-mono text-xs font-bold text-slate-500">{sectionColor}</span>
										</div>
									</div>
									<button type="submit" class="w-full rounded-md bg-slate-900 py-1.5 text-xs font-bold text-white transition hover:bg-black">Add Section</button>
								</form>
							</div>
						{:else if activeTab === 'seats'}
							<div class="space-y-4">
								<h3 class="text-xs font-bold text-slate-800">Price Levels</h3>
								<div class="space-y-2">
									{#each priceLevels as pl}
										<div class="flex items-center justify-between rounded-lg border border-slate-100 bg-slate-50/50 p-2 text-xs font-bold">
											<span class="flex items-center gap-2">
												<span class="h-3 w-3 rounded-full border border-white shadow-2xs" style="background-color: {pl.color}"></span>
												{pl.description}
											</span>
										</div>
									{/each}
								</div>
							</div>
						{/if}

						{#if selectedRsAreaId}
							{@const targetArea = rsAreas.find((a) => a.id === selectedRsAreaId)}
							{#if targetArea}
								<div class="space-y-3 rounded-lg border border-slate-200 bg-slate-50/50 p-3 mt-4">
									<div class="flex items-center justify-between">
										<span class="text-xs font-extrabold text-slate-900">Area: {targetArea.id.replace(manifestIdVal + '-', '')}</span>
										<button type="button" onclick={() => { selectedRsAreaId = ''; drawSeatingMap(); }} class="text-[10px] font-bold text-slate-400 hover:text-slate-600">✕</button>
									</div>
									<p class="text-[10px] text-slate-500">Rows: {targetArea.rows?.length || 0} · Seats: {(targetArea.rows || []).reduce((s: number, r: any) => s + (r.seats?.length || 0), 0)}</p>
								</div>

								<form onsubmit={(e) => { e.preventDefault(); generateSeatingBlock(); }} class="space-y-3 border-t border-slate-100 pt-3 font-semibold text-slate-700">
									<h4 class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">Generate Block</h4>
									<div class="grid grid-cols-2 gap-2">
										<div class="space-y-1"><label for="block-row-prefix" class="text-[10px] text-slate-500">Row Prefix</label><input id="block-row-prefix" type="text" bind:value={blockRowPrefix} placeholder="A" class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none" required /></div>
										<div class="space-y-1"><label for="block-rows-count" class="text-[10px] text-slate-500">Rows</label><input id="block-rows-count" type="number" bind:value={blockRowsCount} min="1" max="26" class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none" required /></div>
									</div>
									<div class="grid grid-cols-2 gap-2">
										<div class="space-y-1"><label for="block-seats-per-row" class="text-[10px] text-slate-500">Seats/Row</label><input id="block-seats-per-row" type="number" bind:value={blockSeatsPerRow} min="1" class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none" required /></div>
										<div class="space-y-1"><label for="block-seat-start-num" class="text-[10px] text-slate-500">Start #</label><input id="block-seat-start-num" type="number" bind:value={blockSeatStartNum} min="1" class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none" required /></div>
									</div>
									<div class="space-y-1">
										<label for="block-shape" class="text-[10px] text-slate-500">Shape</label>
										<select id="block-shape" bind:value={blockShape} class="w-full rounded-md border border-slate-200 bg-white px-2 py-1.5 text-xs font-semibold text-slate-700 outline-none">
											<option value="rectangle">Rectangle</option>
											<option value="trapezoid">Trapezoid</option>
											<option value="diamond">Diamond</option>
											<option value="staggered">Staggered</option>
										</select>
									</div>
									<button type="submit" class="w-full rounded-md bg-slate-900 py-1.5 text-xs font-bold text-white transition hover:bg-black">Generate Block</button>
								</form>

								<div class="space-y-2 border-t border-slate-100 pt-3">
									<h4 class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">Curvature</h4>
									<div class="flex items-center gap-3">
										<input type="range" min="-200" max="200" value={targetArea.curvature || 0} oninput={(e) => applyCurvature(targetArea, Number((e.target as HTMLInputElement).value))} class="flex-1 cursor-pointer accent-slate-900" />
										<span class="w-10 text-right text-[11px] font-bold text-slate-600 tabular-nums">{targetArea.curvature || 0}</span>
									</div>
								</div>
							{/if}
						{/if}
					</div>
				</div>
			{:else}
				<div class="flex-1 space-y-4 overflow-y-auto p-4">
					{#if activeMode === 'inventory'}
						<div class="space-y-3">
							<h3 class="text-[10px] font-black tracking-widest text-slate-400 uppercase">Inventory Management</h3>
							<div class="space-y-2 rounded-xl border border-slate-100 bg-slate-50 p-3 text-xs font-semibold text-slate-600">
								<p class="text-[10px] leading-relaxed text-slate-500">Toggle seat status to open or hold seats for booking.</p>
								<div class="flex gap-2 pt-2">
									<button onclick={() => setInventoryStatus('AVAILABLE')} disabled={selectedSeatIds.length === 0} class="flex-1 rounded-lg bg-emerald-600 py-2 text-center text-[10px] font-bold text-white transition hover:bg-emerald-700 disabled:opacity-50">Unlock / Open</button>
									<button onclick={() => setInventoryStatus('UNAVAILABLE')} disabled={selectedSeatIds.length === 0} class="flex-1 rounded-lg bg-rose-600 py-2 text-center text-[10px] font-bold text-white transition hover:bg-rose-700 disabled:opacity-50">Lock / Hold</button>
								</div>
							</div>
						</div>
					{:else}
						<div class="space-y-4">
							<h3 class="text-[10px] font-black tracking-widest text-slate-400 uppercase">Physical Positioning</h3>
							<div class="space-y-3 rounded-xl border border-slate-100 bg-slate-50 p-3 text-xs font-semibold text-slate-600">
								<div class="space-y-1">
									<label for="grid-snap" class="text-[9px] font-bold tracking-wider text-slate-400 uppercase">Grid Snap</label>
									<select id="grid-snap" bind:value={snapGrid} class="w-full rounded-md border border-slate-200 bg-white px-2 py-1.5 text-xs outline-none">
										<option value={1}>Free Movement</option>
										<option value={8}>Snap to 8px</option>
										<option value={16}>Snap to 16px</option>
										<option value={32}>Snap to 32px</option>
									</select>
								</div>
							</div>
						</div>
					{/if}
				</div>
			{/if}

			<div class="border-t border-slate-200 bg-slate-50 p-4">
				<h3 class="mb-3.5 text-[10px] font-black tracking-widest text-slate-400 uppercase">Stadium Metrics</h3>
				<div class="flex items-center justify-between rounded-lg border border-slate-200/60 bg-white p-2.5 shadow-2xs">
					<span class="text-xs font-semibold text-slate-400">Total Capacity</span>
					<span class="font-mono text-sm font-extrabold text-slate-800">{totalCapacity.toLocaleString()}</span>
				</div>
			</div>
		</aside>

		<!-- Canvas -->
		<main class="relative flex flex-1 flex-col overflow-hidden bg-[#FAFAFA]">
			<!-- Toolbar -->
			<div class="z-30 flex h-12 items-center justify-between border-b border-slate-200 bg-white px-6">
				<div class="flex items-center gap-3">
					<div class="flex items-center rounded-lg border border-slate-200 bg-slate-50 p-0.5">
						{#each [['select','V','M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5'],['pan','Space','']] as [tool, key, path]}
							<button onclick={() => (activeTool = tool as any)} class="flex items-center gap-1 rounded-md px-3 py-1 text-xs font-bold transition-all {activeTool === tool ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-800'}">
								{tool === 'select' ? 'Select' : 'Pan'}
								<span class="ml-0.5 rounded bg-slate-200 px-1 text-[9px] text-slate-500">{key}</span>
							</button>
						{/each}
						<div class="mx-0.5 h-4 w-px bg-slate-200"></div>
						<button onclick={() => (activeTool = 'brush')} class="flex items-center gap-1 rounded-md px-3 py-1 text-xs font-bold transition-all {activeTool === 'brush' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-800'}">Brush <span class="ml-0.5 rounded bg-slate-200 px-1 text-[9px] text-slate-500">B</span></button>
						<button onclick={() => (activeTool = 'eraser')} class="flex items-center gap-1 rounded-md px-3 py-1 text-xs font-bold transition-all {activeTool === 'eraser' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-800'}">Eraser <span class="ml-0.5 rounded bg-slate-200 px-1 text-[9px] text-slate-500">E</span></button>
					</div>

					{#if activeTool === 'brush'}
						<div class="mx-2 flex items-center gap-2">
							<span class="text-[10px] font-bold text-slate-400">Paint:</span>
							<select bind:value={brushPriceLevelId} class="rounded-md border border-slate-200 bg-white px-2 py-1 text-[10px] font-semibold outline-none">
								{#each priceLevels as pl}<option value={pl.id}>{pl.description}</option>{/each}
							</select>
						</div>
					{/if}

					{#if selectedSeatIds.length > 0}
						<div class="mx-2 h-5 w-px bg-slate-200"></div>
						<div class="flex items-center gap-1.5">
							<button onclick={() => toggleSelectedAttribute('accessibility')} class="rounded-md border border-slate-200 bg-white px-2 py-1 text-[10px] font-bold text-slate-700 hover:bg-slate-50">♿</button>
							<button onclick={() => toggleSelectedAttribute('obstructedView')} class="rounded-md border border-slate-200 bg-white px-2 py-1 text-[10px] font-bold text-slate-700 hover:bg-slate-50">⚠️</button>
							<button onclick={() => toggleSelectedAttribute('aisle')} class="rounded-md border border-slate-200 bg-white px-2 py-1 text-[10px] font-bold text-slate-700 hover:bg-slate-50">↔</button>
							<span class="ml-1 text-[10px] font-bold text-slate-400">({selectedSeatIds.length} selected)</span>
							<span class="ml-1 rounded bg-slate-100 px-1 text-[9px] text-slate-400">Del to remove</span>
						</div>
					{/if}
				</div>

				<div class="flex items-center gap-3">
					{#if saveMessage}<span class="animate-pulse text-xs font-semibold text-emerald-600">{saveMessage}</span>{/if}
					{#if errorMessage}<span class="animate-pulse text-xs font-semibold text-rose-600">{errorMessage}</span>{/if}
					<button onclick={saveLayout} disabled={isSaving} class="rounded-lg border border-slate-900 bg-slate-900 px-4 py-1.5 text-xs font-extrabold text-white shadow-xs hover:bg-black active:scale-95 disabled:opacity-50">{isSaving ? 'Saving...' : 'Save Layout'}</button>
				</div>
			</div>

			<!-- Stage container -->
			<div id="stage-container" bind:this={stageContainer} role="application" aria-label="Seatmap working area" class="relative w-full flex-1 overflow-hidden bg-[#FAFAFA]" ondragover={(e) => e.preventDefault()} ondrop={(e) => { e.preventDefault(); const plId = e.dataTransfer?.getData('text/plain'); if (plId) assignPriceLevelToSelected(plId); }}></div>

			<!-- Floating tool panel -->
			<div class="absolute top-6 right-6 z-40 flex w-14 flex-col items-center gap-2.5 rounded-2xl border border-slate-200/80 bg-white/90 p-1.5 shadow-lg backdrop-blur-md">
				{#each [['seat', 'Seat Select'], ['row', 'Row Select'], ['section', 'Section Select']] as [tool, label]}
					<button onclick={() => (selectionTool = tool as any)} class="group relative rounded-xl p-2.5 transition-all {selectionTool === tool ? 'bg-slate-900 text-white shadow-md' : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900'}">
						<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							{#if tool === 'seat'}<circle cx="12" cy="12" r="8" stroke-width="2.5" />
							{:else if tool === 'row'}<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M4 6h16M4 12h16M4 18h16" />
							{:else}<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z" />{/if}
						</svg>
						<div class="pointer-events-none absolute top-1/2 right-16 z-50 -translate-y-1/2 rounded bg-slate-900 px-2 py-1 text-[10px] font-bold whitespace-nowrap text-white opacity-0 shadow-md transition group-hover:opacity-100">{label}</div>
					</button>
				{/each}

				<div class="h-px w-8 bg-slate-200"></div>

				<button onclick={() => (showFilterDialog = !showFilterDialog)} class="group relative rounded-xl p-2.5 text-slate-500 transition-all hover:bg-slate-100 hover:text-slate-950">
					<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" /></svg>
					<div class="pointer-events-none absolute top-1/2 right-16 z-50 -translate-y-1/2 rounded bg-slate-900 px-2 py-1 text-[10px] font-bold whitespace-nowrap text-white opacity-0 shadow-md transition group-hover:opacity-100">Filters</div>
				</button>

				<button onclick={() => (showAssistantDialog = !showAssistantDialog)} class="group relative rounded-xl p-2.5 text-slate-500 transition-all hover:bg-slate-100 hover:text-slate-950">
					<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" /></svg>
					<div class="pointer-events-none absolute top-1/2 right-16 z-50 -translate-y-1/2 rounded bg-slate-900 px-2 py-1 text-[10px] font-bold whitespace-nowrap text-white opacity-0 shadow-md transition group-hover:opacity-100">AI Assistant</div>
				</button>
			</div>

			<!-- Zoom controls -->
			<div class="absolute right-6 bottom-52 z-40 flex flex-col items-center gap-1 rounded-2xl border border-slate-200/80 bg-white/90 p-1 shadow-lg backdrop-blur-md select-none">
				<button type="button" onclick={fitToView} class="rounded-xl p-2 text-slate-500 transition-all hover:bg-slate-100 hover:text-slate-900" title="Fit to View">
					<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" /></svg>
				</button>
				<button type="button" onclick={zoomIn} class="rounded-xl p-2 text-slate-500 transition-all hover:bg-slate-100 hover:text-slate-900">
					<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v12m6-6H6" /></svg>
				</button>
				<button type="button" onclick={zoomOut} class="rounded-xl p-2 text-slate-500 transition-all hover:bg-slate-100 hover:text-slate-900">
					<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 12H6" /></svg>
				</button>
			</div>

			<!-- Mini-map -->
			<div class="absolute right-6 bottom-6 z-40 flex h-40 w-52 flex-col overflow-hidden rounded-2xl border border-slate-200/80 bg-white/90 p-2.5 shadow-lg backdrop-blur-md select-none">
				<div class="flex items-center justify-between pb-1 text-[9px] font-bold tracking-wider text-slate-400 uppercase">
					<span>Mini-map</span>
					<span class="rounded bg-slate-100 px-1 py-0.5 font-mono text-[8px] text-slate-500">Zoom: {stage ? Math.round(stage.scaleX() * 100) : 100}%</span>
				</div>
				<div class="relative w-full flex-1 overflow-hidden rounded-xl border border-slate-200/60 bg-slate-50">
					<button type="button" aria-label="Minimap" class="relative flex h-full w-full cursor-crosshair items-center justify-center overflow-hidden border-0 bg-transparent p-0" onclick={handleMinimapClick}>
						<svg viewBox="{bounds.minX} {bounds.minY} {bounds.width} {bounds.height}" class="pointer-events-none h-full w-full">
							{#each rsAreas as area}
								{#each area.rows || [] as row}
									{#each row.seats || [] as seat}
										{@const sec = seat.sectionId ? sections.find((s) => s.id === seat.sectionId) : null}
										{@const pl = (!sec && seat.priceLevelId) ? priceLevels.find((p) => p.id === seat.priceLevelId) : null}
										{@const dotColor = sec?.color || pl?.color || '#3B82F6'}
										<circle cx={seat.positionX || 0} cy={seat.positionY || row.positionY || 0} r="2.5" fill={selectedSeatIds.includes(seat.id) ? '#1A1A1A' : seat.status === 'UNAVAILABLE' ? '#CBD5E1' : dotColor} />
									{/each}
								{/each}
							{/each}
							{#each gaAreas as ga}
								<rect x={ga.x || 50} y={ga.y || 50} width={ga.width || 200} height={ga.height || 100} fill="rgba(15,23,42,0.05)" stroke="#0F172A" stroke-width="2" />
							{/each}
							<rect x={viewportX} y={viewportY} width={viewportW} height={viewportH} fill="rgba(239,68,68,0.06)" stroke="#EF4444" stroke-width="5" stroke-dasharray="10 8" rx="4" />
						</svg>
					</button>
				</div>
			</div>

			{#if isSaving}
				<div class="absolute inset-0 z-60 flex flex-col items-center justify-center gap-3 bg-white/70 font-semibold text-slate-800 backdrop-blur-xs">
					<svg class="h-8 w-8 animate-spin text-slate-900" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
					<span>Saving layout...</span>
				</div>
			{/if}
		</main>
	</div>
</div>

{#if showFilterDialog}
	<div class="fixed top-40 right-24 z-50 w-60 rounded-2xl border border-slate-200 bg-white/95 p-4 shadow-xl backdrop-blur-md select-none">
		<div class="mb-3 flex items-center justify-between border-b border-slate-100 pb-2">
			<h4 class="text-xs font-black tracking-wider text-slate-800 uppercase">Filters</h4>
			<button onclick={() => (showFilterDialog = false)} class="text-slate-400 hover:text-slate-700"><svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg></button>
		</div>
		<div class="space-y-2.5 text-xs font-semibold text-slate-600">
			<label class="flex cursor-pointer items-center gap-2"><input type="checkbox" bind:checked={onlyAvailable} class="h-3.5 w-3.5 rounded" /><span>Available Only</span></label>
			<label class="flex cursor-pointer items-center gap-2"><input type="checkbox" bind:checked={onlyAccessible} class="h-3.5 w-3.5 rounded" /><span>♿ Accessible Only</span></label>
			<label class="flex cursor-pointer items-center gap-2"><input type="checkbox" bind:checked={onlyObstructed} class="h-3.5 w-3.5 rounded" /><span>⚠️ Obstructed Only</span></label>
		</div>
	</div>
{/if}

{#if showAssistantDialog}
	<div class="fixed top-56 right-24 z-50 w-72 rounded-2xl border border-slate-200 bg-white/95 p-4 shadow-xl backdrop-blur-md select-none">
		<div class="mb-3 flex items-center justify-between border-b border-slate-100 pb-2">
			<h4 class="text-xs font-black tracking-wider text-slate-800 uppercase">Seating Assistant</h4>
			<button onclick={() => (showAssistantDialog = false)} class="text-slate-400 hover:text-slate-700"><svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg></button>
		</div>
		<form onsubmit={(e) => { e.preventDefault(); runAssistantPrompt(); }} class="space-y-3">
			<p class="text-[10px] leading-relaxed font-semibold text-slate-500">Try: <code class="rounded bg-slate-100 px-1 text-slate-700">Select VIP</code>, <code class="rounded bg-slate-100 px-1 text-slate-700">Select Row A</code></p>
			<div class="flex gap-2">
				<input type="text" bind:value={assistantPrompt} placeholder="Prompt assistant..." class="flex-1 rounded-lg border border-slate-200 px-2.5 py-1.5 text-xs font-semibold outline-none focus:border-slate-400" />
				<button type="submit" class="rounded-lg bg-slate-900 px-3 py-1.5 text-xs font-bold text-white transition hover:bg-black">Run</button>
			</div>
		</form>
	</div>
{/if}
