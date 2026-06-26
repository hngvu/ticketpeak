<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { onMount, onDestroy } from 'svelte';
	import { browser } from '$app/environment';
	import {
		IconArrowLeft,
		IconFocusCentered,
		IconBuildingEstate,
		IconPlus,
		IconMinus
	} from '@tabler/icons-svelte';

	let { data } = $props();
	const venue = $derived(data.venue);
	const manifest = $derived(data.manifest);
	const rsSections = $derived(data.rsSections || []);
	const gaSections = $derived(data.gaSections || []);
	const layoutObjects = $derived(data.manifest?.objects || []);
	const allSections = $derived([...rsSections, ...gaSections]);
	const totalCapacity = $derived(
		allSections.reduce(
			(sum, sec) =>
				sum +
				(sec.capacity ||
					(sec.rows ? sec.rows.reduce((s: number, r: any) => s + r.seats.length, 0) : 0)),
			0
		)
	);

	const allSeats = $derived.by(() => {
		let list: any[] = [];
		for (const sec of rsSections) {
			for (const row of sec.rows || []) {
				for (const seat of row.seats || []) {
					list.push({ ...seat, sectionColor: sec.color || '#cbd5e1' });
				}
			}
		}
		return list;
	});

	let Konva: any;
	let stageContainer: HTMLDivElement | null = $state(null);
	let stage: any = null;
	let layer: any = null;
	let canvasZoom = $state(1);

	function getPolygonCenter(polygon: [number, number][]) {
		if (!polygon || polygon.length === 0) return { x: 0, y: 0 };
		let minX = Infinity,
			maxX = -Infinity,
			minY = Infinity,
			maxY = -Infinity;
		for (const p of polygon) {
			if (p[0] < minX) minX = p[0];
			if (p[0] > maxX) maxX = p[0];
			if (p[1] < minY) minY = p[1];
			if (p[1] > maxY) maxY = p[1];
		}
		return { x: (minX + maxX) / 2, y: (minY + maxY) / 2 };
	}

	function drawStagePath(ow: number, oh: number) {
		const curveStart = Math.max(ow * 0.6, ow - 60);
		return `M 0 0 L ${curveStart} 0 Q ${2 * ow - curveStart} ${oh / 2} ${curveStart} ${oh} L 0 ${oh} Z`;
	}

	// For minimap
	const bounds = $derived.by(() => {
		let minX = Infinity,
			maxX = -Infinity,
			minY = Infinity,
			maxY = -Infinity;

		for (const obj of layoutObjects) {
			if (obj.x < minX) minX = obj.x;
			if (obj.x + (obj.width || 0) > maxX) maxX = obj.x + (obj.width || 0);
			if (obj.y < minY) minY = obj.y;
			if (obj.y + (obj.height || 0) > maxY) maxY = obj.y + (obj.height || 0);
		}

		for (const sec of rsSections) {
			if (sec.polygon && sec.polygon.length > 0) {
				for (const [px, py] of sec.polygon) {
					const ax = px;
					const ay = py;
					if (ax < minX) minX = ax;
					if (ax > maxX) maxX = ax;
					if (ay < minY) minY = ay;
					if (ay > maxY) maxY = ay;
				}
			} else if (sec.uiData?.width && sec.uiData?.height) {
				const ax = sec.uiData?.x || 0;
				const ay = sec.uiData?.y || 0;
				if (ax < minX) minX = ax;
				if (ax + sec.uiData.width > maxX) maxX = ax + sec.uiData.width;
				if (ay < minY) minY = ay;
				if (ay + sec.uiData.height > maxY) maxY = ay + sec.uiData.height;
			}
		}

		for (const sec of gaSections) {
			if (sec.polygon && sec.polygon.length > 0) {
				for (const [px, py] of sec.polygon) {
					const ax = px;
					const ay = py;
					if (ax < minX) minX = ax;
					if (ax > maxX) maxX = ax;
					if (ay < minY) minY = ay;
					if (ay > maxY) maxY = ay;
				}
			} else if (sec.width && sec.height) {
				const ax = sec.x || 0;
				const ay = sec.y || 0;
				if (ax < minX) minX = ax;
				if (ax + sec.width > maxX) maxX = ax + sec.width;
				if (ay < minY) minY = ay;
				if (ay + sec.height > maxY) maxY = ay + sec.height;
			}
		}

		if (minX === Infinity)
			return { minX: 0, maxX: 800, minY: 0, maxY: 600, width: 800, height: 600 };
		const pad = 50;
		return {
			minX: minX - pad,
			maxX: maxX + pad,
			minY: minY - pad,
			maxY: maxY + pad,
			width: maxX - minX + pad * 2,
			height: maxY - minY + pad * 2
		};
	});

	function drawSeatingMap() {
		if (!stage || !layer || !Konva) return;
		layer.destroyChildren();

		// Background Objects
		for (let i = 0; i < layoutObjects.length; i++) {
			const obj = layoutObjects[i];
			const g = new Konva.Group({ x: obj.x, y: obj.y, draggable: false });
			if (obj.type === 'stage') {
				g.add(
					new Konva.Path({
						data: drawStagePath(obj.width || 200, obj.height || 100),
						fill: '#334155'
					})
				);
				g.add(
					new Konva.Text({
						x: 0,
						y: obj.height || 100,
						text: obj.text || 'STAGE',
						fontSize: Math.max(16, Math.min(obj.width || 200, obj.height || 100) * 0.15),
						fontFamily: 'sans-serif',
						fontStyle: 'bold',
						fill: '#F8FAFC',
						align: 'center',
						verticalAlign: 'middle',
						rotation: -90,
						offsetX: -(obj.height || 100) / 2,
						offsetY: -(obj.width || 200) / 2
					})
				);
			} else if (obj.type === 'rect') {
				g.add(
					new Konva.Rect({
						width: obj.width,
						height: obj.height,
						fill: obj.fill,
						stroke: obj.stroke,
						strokeWidth: obj.strokeWidth,
						cornerRadius: obj.cornerRadius
					})
				);
			} else if (obj.type === 'circle') {
				g.add(
					new Konva.Circle({
						radius: obj.radius,
						fill: obj.fill,
						stroke: obj.stroke,
						strokeWidth: obj.strokeWidth
					})
				);
			} else if (obj.type === 'text') {
				g.add(
					new Konva.Text({
						text: obj.text,
						fill: obj.fill,
						fontSize: obj.fontSize,
						fontFamily: obj.fontFamily,
						fontStyle: obj.fontStyle === 'bold' ? 'bold' : 'normal'
					})
				);
			}
			layer.add(g);
		}

		// GA Sections
		for (let i = 0; i < gaSections.length; i++) {
			const sec = gaSections[i];
			const g = new Konva.Group({ x: sec.x || 0, y: sec.y || 0, draggable: false });
			if (sec.polygon && sec.polygon.length > 0) {
				const points = sec.polygon.reduce(
					(acc: number[], p: any) => [...acc, p[0] - (sec.x || 0), p[1] - (sec.y || 0)],
					[]
				);
				g.add(
					new Konva.Line({
						points,
						fill: 'transparent',
						stroke: '#CBD5E1',
						strokeWidth: 1,
						closed: true
					})
				);
			} else {
				g.add(
					new Konva.Rect({
						width: sec.width,
						height: sec.height,
						fill: sec.color ? sec.color + '05' : 'transparent',
						stroke: sec.color || '#CBD5E1',
						strokeWidth: 1,
						cornerRadius: 4
					})
				);
			}
			layer.add(g);
		}

		// RS Sections
		for (let i = 0; i < rsSections.length; i++) {
			const sec = rsSections[i];
			const boxX = sec.uiData?.x || 0;
			const boxY = sec.uiData?.y || 0;
			const g = new Konva.Group({ x: boxX, y: boxY, draggable: false });
			if (sec.polygon && sec.polygon.length > 0) {
				const points = sec.polygon.reduce(
					(acc: number[], p: any) => [...acc, p[0] - boxX, p[1] - boxY],
					[]
				);
				g.add(
					new Konva.Line({
						points,
						fill: 'transparent',
						stroke: '#CBD5E1',
						strokeWidth: 1,
						closed: true
					})
				);
			} else if (sec.uiData?.width && sec.uiData?.height) {
				g.add(
					new Konva.Rect({
						width: sec.uiData.width,
						height: sec.uiData.height,
						fill: sec.color ? sec.color + '05' : 'transparent',
						stroke: sec.color || '#CBD5E1',
						strokeWidth: 1,
						cornerRadius: 4
					})
				);
			}
			layer.add(g);
		}

		// Seats
		for (let i = 0; i < allSeats.length; i++) {
			const seat = allSeats[i];
			if (seat.positionX != null && seat.positionY != null) {
				layer.add(
					new Konva.Circle({
						x: seat.positionX,
						y: seat.positionY,
						radius: 4,
						fill: seat.status === 'AVAILABLE' ? '#94A3B8' : '#e2e8f0',
						stroke: seat.status === 'AVAILABLE' ? '#ffffff' : '#cbd5e1',
						strokeWidth: 0.5,
						listening: false
					})
				);
			}
		}

		// Section Labels (On top of seats, with listening: false)
		for (const sec of allSections) {
			let labelX: number;
			let labelY: number;
			if (sec.polygon && sec.polygon.length > 0) {
				const center = getPolygonCenter(sec.polygon);
				labelX = center.x;
				labelY = center.y;
			} else {
				const x = sec.x || sec.uiData?.x || 0;
				const y = sec.y || sec.uiData?.y || 0;
				const w = sec.width || sec.uiData?.width || 0;
				const h = sec.height || sec.uiData?.height || 0;
				labelX = x + w / 2;
				labelY = y + h / 2;
			}

			const t = new Konva.Text({
				x: labelX,
				y: labelY,
				text: (sec.name || '').toUpperCase(),
				fill: '#4A5568',
				fontSize: 16,
				fontStyle: 'bold',
				align: 'center',
				verticalAlign: 'middle',
				listening: false,
				shadowColor: '#ffffff',
				shadowBlur: 6,
				shadowOpacity: 1,
				shadowOffset: { x: 0, y: 0 }
			});
			t.offsetX(t.width() / 2);
			t.offsetY(t.height() / 2);
			layer.add(t);
		}

		layer.batchDraw();
	}

	let resizeObserver: ResizeObserver | null = null;
	let hasFit = false;

	onMount(() => {
		if (!browser) return;
		requestAnimationFrame(async () => {
			const konvaModule = await import('konva');
			Konva = konvaModule.default;
			const width = stageContainer?.offsetWidth || 900;
			const height = stageContainer?.offsetHeight || 600;
			stage = new Konva.Stage({ container: 'stage-container', width, height, draggable: true });
			layer = new Konva.Layer();
			stage.add(layer);

			resizeObserver = new ResizeObserver(() => {
				if (stage && stageContainer) {
					stage.width(stageContainer.offsetWidth);
					stage.height(stageContainer.offsetHeight);
					if (!hasFit && stageContainer.offsetWidth > 0) {
						hasFit = true;
						fitToView();
					} else {
						layer.batchDraw();
					}
				}
			});
			if (stageContainer) resizeObserver.observe(stageContainer);

			window.addEventListener('resize', handleResize);

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
				canvasZoom = newScale;
			});

			stage.on('dragmove dragend xChange yChange scaleXChange scaleYChange', () => {
				if (stage) canvasZoom = stage.scaleX();
			});
		});
	});

	onDestroy(() => {
		if (browser) {
			window.removeEventListener('resize', handleResize);
			if (resizeObserver) resizeObserver.disconnect();
		}
	});

	function handleResize() {
		if (stage && stageContainer) {
			stage.width(stageContainer.offsetWidth);
			stage.height(stageContainer.offsetHeight);
			layer.batchDraw();
		}
	}

	function fitToView() {
		if (!stage) return;
		const pad = 80;
		const sw = Math.max(1, stage.width() - pad * 2),
			sh = Math.max(1, stage.height() - pad * 2);
		const bw = bounds.width || 1,
			bh = bounds.height || 1;
		let scale = Math.min(sw / bw, sh / bh);
		scale = Math.max(0.01, scale);
		stage.scale({ x: scale, y: scale });
		stage.x(pad + (sw - bw * scale) / 2 - bounds.minX * scale);
		stage.y(pad + (sh - bh * scale) / 2 - bounds.minY * scale);
		stage.batchDraw();
		canvasZoom = scale;
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
		canvasZoom = ns;
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
		canvasZoom = ns;
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
	}

	$effect(() => {
		if (manifest && allSections && allSeats) {
			drawSeatingMap();
		}
	});
</script>

<svelte:head>
	<title>{venue.name} — Ticketpeak</title>
</svelte:head>

<div class="flex h-full w-full flex-col overflow-hidden bg-slate-50 font-sans text-slate-900">
	<header
		class="relative z-50 flex h-14 shrink-0 items-center justify-between border-b border-slate-200 bg-white px-4 shadow-sm"
	>
		<div class="flex items-center gap-4">
			<a
				href="/b2b/venues"
				class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-700"
			>
				<IconArrowLeft size={18} stroke={2} />
			</a>
			<div class="flex items-center gap-2">
				<span class="text-sm font-bold text-slate-800">{venue.name}</span>
				<span class="text-slate-300">/</span>
				<select
					class="cursor-pointer border-none bg-transparent text-xs font-semibold text-slate-500 transition-colors outline-none hover:text-slate-700"
					onchange={(e) => {
						const url = new URL(window.location.href);
						url.searchParams.set('manifestId', e.currentTarget.value);
						window.location.href = url.toString();
					}}
				>
					{#each data.manifests as m (m.id)}
						<option value={m.id} selected={m.id === data.manifest?.id}>
							{m.description || m.name || m.id}
						</option>
					{/each}
				</select>
			</div>
		</div>
		<div class="flex items-center gap-3">
			<span
				class="inline-flex items-center rounded-md bg-emerald-50 px-2 py-1 text-[10px] font-bold tracking-wider text-emerald-600 uppercase ring-1 ring-emerald-500/20 ring-inset"
			>
				{venue.status || 'ACTIVE'}
			</span>
		</div>
	</header>

	<div class="flex flex-1 overflow-hidden">
		<aside class="flex w-[260px] shrink-0 flex-col border-r border-slate-200 bg-[#F8FAFC]">
			<div class="flex items-center justify-between border-b border-slate-200 bg-white px-5 py-3.5">
				<h3 class="text-[11px] font-black tracking-wider text-slate-800 uppercase">Sections</h3>
				<span class="text-[15px] font-bold text-[#1E293B]">{totalCapacity}</span>
			</div>
			<div class="flex-1 overflow-x-hidden overflow-y-auto">
				{#each allSections as sec (sec.id)}
					<div
						class="flex flex-col gap-2.5 border-b border-[#F1F5F9] bg-white px-4 py-3 transition-colors hover:bg-slate-50"
					>
						<div class="flex items-center justify-between">
							<span class="max-w-[170px] truncate text-[14px] font-bold text-[#334155]"
								>{sec.name || sec.uiData?.label || 'Unnamed Section'}</span
							>
							<span class="ml-2 shrink-0 text-[14px] font-medium text-[#475569]"
								>{sec.capacity ||
									(sec.rows
										? sec.rows.reduce((sum: number, r: any) => sum + r.seats.length, 0)
										: 0)}</span
							>
						</div>
					</div>
				{/each}
			</div>
		</aside>

		<div class="relative flex flex-1 flex-col overflow-hidden bg-white">
			{#if manifest}
				<div class="absolute inset-0 z-0 bg-[#F8FAFC]">
					<div
						id="stage-container"
						bind:this={stageContainer}
						class="h-full w-full outline-none"
						tabindex="-1"
					></div>
				</div>

				<div
					class="absolute z-30 flex h-32 w-48 flex-col overflow-hidden rounded-xl border border-slate-200/80 bg-white/90 p-2 shadow-lg backdrop-blur-md"
					style="bottom: 24px; right: 24px;"
				>
					<div
						class="mb-1 flex items-center justify-between border-b border-slate-100 pb-1 text-[8px] font-bold tracking-wider text-slate-400 uppercase"
					>
						<span>Map</span>
						<span class="rounded bg-slate-100 px-1 py-0.5 font-mono text-[7px] text-slate-500"
							>{Math.round(canvasZoom * 100)}%</span
						>
					</div>
					<div
						class="relative w-full flex-1 overflow-hidden rounded-lg border border-slate-200/60 bg-[#f8fafc]"
					>
						<!-- svelte-ignore a11y_click_events_have_key_events -->
						<!-- svelte-ignore a11y_no_static_element_interactions -->
						<div
							class="relative flex h-full w-full items-center justify-center overflow-hidden p-1"
							onclick={handleMinimapClick}
						>
							<svg
								viewBox="{bounds.minX} {bounds.minY} {bounds.width} {bounds.height}"
								class="pointer-events-none h-full w-full"
							>
								{#each layoutObjects as obj, i (obj.id || i)}
									{#if obj.type === 'stage'}
										<path
											transform="translate({obj.x},{obj.y})"
											d={drawStagePath(obj.width || 200, obj.height || 100)}
											fill="#334155"
										/>
									{/if}
								{/each}
								{#each rsSections as sec (sec.id)}
									{#if sec.polygon && sec.polygon.length > 0}
										<polygon
											points={sec.polygon.map((p: any) => p.join(',')).join(' ')}
											fill="none"
											stroke="#CBD5E1"
											stroke-width={Math.max(2, bounds.width / 150)}
										/>
									{:else if sec.uiData?.width && sec.uiData?.height}
										<rect
											transform="translate({sec.uiData?.x || 0},{sec.uiData?.y || 0})"
											width={sec.uiData.width}
											height={sec.uiData.height}
											fill="none"
											stroke="#CBD5E1"
											stroke-width={Math.max(2, bounds.width / 150)}
											rx="4"
										/>
									{/if}
								{/each}
								{#each gaSections as sec (sec.id)}
									{#if sec.polygon && sec.polygon.length > 0}
										<polygon
											points={sec.polygon.map((p: any) => p.join(',')).join(' ')}
											fill="none"
											stroke="#CBD5E1"
											stroke-width={Math.max(2, bounds.width / 150)}
										/>
									{:else if sec.width && sec.height}
										<rect
											transform="translate({sec.x || 0},{sec.y || 0})"
											width={sec.width}
											height={sec.height}
											fill="none"
											stroke="#CBD5E1"
											stroke-width={Math.max(2, bounds.width / 150)}
											rx="4"
										/>
									{/if}
								{/each}
							</svg>
						</div>
					</div>
				</div>
			{:else}
				<div
					class="absolute inset-0 z-10 flex flex-col items-center justify-center bg-white p-8 text-center"
				>
					<IconBuildingEstate size={48} class="mb-4 text-slate-300" stroke={1.5} />
					<h4 class="text-sm font-bold text-slate-700">No Manifest Found</h4>
					<p class="mt-1 max-w-sm text-xs text-slate-500">
						This venue does not have an active manifest setup yet.
					</p>
				</div>
			{/if}
		</div>

		<aside
			class="z-20 flex w-[56px] shrink-0 flex-col items-center gap-0.5 border-l border-slate-200 bg-white py-3 shadow-[-4px_0_12px_rgba(0,0,0,0.02)]"
		>
			<button
				onclick={zoomIn}
				class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold text-slate-400 transition-colors hover:bg-slate-50 hover:text-slate-700"
				title="Zoom In"
			>
				<IconPlus size={16} stroke={2} />
				Zoom In
			</button>
			<button
				onclick={zoomOut}
				class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold text-slate-400 transition-colors hover:bg-slate-50 hover:text-slate-700"
				title="Zoom Out"
			>
				<IconMinus size={16} stroke={2} />
				Zoom Out
			</button>
			<div class="my-1.5 h-px w-8 bg-slate-100"></div>
			<button
				onclick={fitToView}
				class="flex w-10 flex-col items-center gap-0.5 rounded-lg px-1 py-2 text-[9px] font-bold text-slate-400 transition-colors hover:bg-slate-50 hover:text-slate-700"
				title="Fit to View"
			>
				<IconFocusCentered size={16} stroke={2} />
				Fit View
			</button>
		</aside>
	</div>
</div>
