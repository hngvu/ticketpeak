<script lang="ts">
	import { onMount, onDestroy } from 'svelte';
	import { browser } from '$app/environment';

	let {
		manifestDetail,
		seats = [],
		selectedSeatIds = $bindable([]),
		activeTab = 'seats',
		seatPriceLevels = {},
		basePriceLevels = [],
		activeCanvasTool = 'select',
		selectionTool = 'seat',
		brushPriceLevelId = null,
		viewport = $bindable({ x: 0, y: 0, w: 800, h: 600, scale: 1 }),
		bounds = $bindable({ minX: 0, minY: 0, width: 800, height: 600 })
	} = $props();

	let containerRef: HTMLDivElement | null = null;
	let Konva: any;
	let stage: any;
	let layer: any;
	let selectionRect: any;

	let x1 = 0,
		y1 = 0,
		x2 = 0,
		y2 = 0;
	let isShiftPressed = false;

	let viewportX = 0,
		viewportY = 0,
		viewportW = 800,
		viewportH = 600;
	let currentScale = 1;
	let fitScale = 1;
	let selectedObjectId: number | null = null;
	let selectedGaSectionId = '';
	let selectedRsSectionId = '';
	let isDown = false;
	let isRightButton = false;

	const pointer2Cursor = (() => {
		const svg = `<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'><path d='M0 0h24v24H0z' fill='none'/><path d='M14.185 13.14l5.644 -2.202c1.625 -.634 1.538 -2.962 -.13 -3.473l-14.319 -4.382c-1.41 -.431 -2.73 .888 -2.298 2.298l4.382 14.318c.51 1.668 2.84 1.755 3.473 .13l2.202 -5.644a1.84 1.84 0 0 1 1.045 -1.045' fill='white' stroke='#000' stroke-width='2'/></svg>`;
		return `url("data:image/svg+xml,${encodeURIComponent(svg)}") 2 2, default`;
	})();

	const rsSections = $derived(manifestDetail?.rsAreas || []);
	const gaSections = $derived(manifestDetail?.gaAreas || []);
	const layoutObjects = $derived(manifestDetail?.objects || []);
	const sections = $derived(manifestDetail?.sections || []);

	const _bounds = $derived.by(() => {
		let xs: number[] = [],
			ys: number[] = [];
		rsSections.forEach((section: any) => {
			if (section.polygon?.length) {
				for (const [px, py] of section.polygon) {
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
		gaSections.forEach((ga: any, idx: number) => {
			if (ga.polygon?.length) {
				for (const [px, py] of ga.polygon) {
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
		layoutObjects.forEach((o: any) => {
			if (o.x != null) {
				xs.push(o.x, o.x + (o.width ?? 200));
				ys.push(o.y, o.y + (o.height ?? 100));
			}
		});

		const minX = Math.min(...xs, 0);
		const minY = Math.min(...ys, 0);
		const maxX = Math.max(...xs, 800);
		const maxY = Math.max(...ys, 600);
		const result = { minX, minY, width: maxX - minX, height: maxY - minY };
		return result;
	});

	$effect(() => {
		bounds = _bounds;
	});

	function getSeatFill(seat: any) {
		if (activeTab === 'seats' || activeTab === 'offers') {
			if (seat.status === 'KILLED') return '#E2E8F0';
			const plId = seatPriceLevels[seat.id] || seat.priceLevelId;
			if (plId) {
				const pl = basePriceLevels.find((p: any) => p.id === plId);
				if (pl?.color) return pl.color;
			}
			return '#CBD5E1'; // Unassigned
		}
		if (activeTab === 'holds') {
			if (seat.status === 'KILLED') return '#1E293B';
			if (seat.status === 'ON_HOLD') return '#EAB308';
			return '#CBD5E1';
		}
		return '#CBD5E1';
		return '#CBD5E1';
	}

	function drawSeatingMap() {
		if (!stage || !layer || !Konva) return;
		layer.destroyChildren();
		layer.add(selectionRect);

		const sc = stage.scaleX() || 1;
		const SEAT_R = 3;
		const showSeats = sc >= fitScale * 1.5;
		const showLabel = !showSeats;
		const secProgress = Math.max(0, Math.min(1, (sc - fitScale * 1.4) / (fitScale * 0.8)));

		layoutObjects.forEach((obj: any, idx: number) => {
			const ox = obj.x ?? 100,
				oy = obj.y ?? 100,
				ow = obj.width ?? 200,
				oh = obj.height ?? 100;
			const isSel = selectedObjectId === idx;
			const g = new Konva.Group({ x: ox, y: oy, draggable: false, id: 'obj-' + idx });

			if (obj.type === 'stage') {
				const curveStart = Math.max(ow * 0.6, ow - 60);
				g.add(
					new Konva.Shape({
						sceneFunc: (context: any, shape: any) => {
							context.beginPath();
							context.moveTo(0, 0);
							context.lineTo(curveStart, 0);
							context.quadraticCurveTo(2 * ow - curveStart, oh / 2, curveStart, oh);
							context.lineTo(0, oh);
							context.closePath();
							context.fillStrokeShape(shape);
						},
						fill: '#334155',
						strokeWidth: 0
					})
				);
				g.add(
					new Konva.Text({
						text: (obj.text || 'STAGE').toUpperCase(),
						fontSize: Math.max(16, Math.min(ow, oh) * 0.15),
						fontStyle: 'bold',
						fill: '#F8FAFC',
						align: 'center',
						verticalAlign: 'middle',
						width: oh,
						height: ow,
						rotation: -90,
						x: 0,
						y: oh
					})
				);
			} else {
				// Fallback
				g.add(new Konva.Rect({ width: ow, height: oh, fill: obj.color || '#CBD5E1' }));
			}
			layer.add(g);
		});

		gaSections.forEach((ga: any, idx: number) => {
			const bx = ga.x ?? 50 + idx * 220,
				by = ga.y ?? 50;
			const gw = ga.width ?? 200,
				gh = ga.height ?? 100;
			const g = new Konva.Group({ x: bx, y: by, draggable: false, id: ga.id });

			const color = sections.find((s: any) => s.id === ga.sectionId)?.color || '#EF4444';
			const isSel = selectedGaSectionId === ga.id;

			const shapeAttrs = { fill: color, opacity: 0.15, stroke: color, strokeWidth: isSel ? 3 : 1 };
			if (ga.polygon?.length > 0) {
				const hull = ga.polygon.flatMap((p: any) =>
					p.x != null ? [p.x - bx, p.y - by] : [p[0] - bx, p[1] - by]
				);
				g.add(new Konva.Line({ ...shapeAttrs, points: hull, closed: true }));
			} else {
				g.add(new Konva.Rect({ ...shapeAttrs, width: gw, height: gh }));
			}
			g.add(
				new Konva.Text({
					text: ga.name || 'GA',
					fontSize: 14,
					fontStyle: 'bold',
					fill: color,
					width: gw,
					height: gh,
					align: 'center',
					verticalAlign: 'middle'
				})
			);
			layer.add(g);
		});

		rsSections.forEach((section: any) => {
			const rows = section.rows || [];
			if (section.x == null) return;
			const boxX = section.x,
				boxY = section.y,
				boxW = section.width,
				boxH = section.height;
			const isSel = selectedRsSectionId === section.id;
			const color = sections.find((s: any) => s.id === section.sectionId)?.color || '#7C3AED';

			const ag = new Konva.Group({ x: boxX, y: boxY, id: 'group-' + section.id });

			let hull: number[] = [];
			if (section.polygon?.length > 0) {
				hull = section.polygon.flatMap((p: any) =>
					p.x != null ? [p.x - boxX, p.y - boxY] : [p[0] - boxX, p[1] - boxY]
				);
			}

			const shapeAttrs = {
				fill: isSel ? color + '33' : 'transparent',
				stroke: color,
				strokeWidth: 1,
				closed: true
			};
			if (hull.length > 0) ag.add(new Konva.Line({ ...shapeAttrs, points: hull }));
			else ag.add(new Konva.Rect({ ...shapeAttrs, width: boxW, height: boxH }));

			if (showLabel) {
				ag.add(
					new Konva.Text({
						text: section.name || 'RS',
						fontSize: 14,
						fontStyle: 'bold',
						fill: color,
						width: boxW,
						height: boxH,
						align: 'center',
						verticalAlign: 'middle',
						opacity: Math.max(0, 1 - secProgress * 2)
					})
				);
			}

			if (showSeats) {
				rows.forEach((row: any) => {
					(row.seats || []).forEach((seat: any) => {
						const sx = seat.positionX ?? 0,
							sy = seat.positionY ?? 0;
						const isSeatSel = selectedSeatIds.includes(seat.id);
						const sg = new Konva.Group({ x: sx - boxX, y: sy - boxY, id: seat.id });

						sg.add(new Konva.Circle({ radius: SEAT_R, fill: getSeatFill(seat) }));
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
						circle.on('mouseenter', () => {
							stage.container().style.cursor = pointer2Cursor;
						});
						circle.on('mouseleave', () => {
							stage.container().style.cursor = pointer2Cursor;
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

		stage.on('contextmenu', (e: any) => {
			e.evt?.preventDefault();
		});

		stage.on('mousedown touchstart', (e: any) => {
			isRightButton = e.evt?.button === 2 || e.evt?.which === 3;
			if (!isRightButton) return; // Only select on right click

			e.evt?.preventDefault();
			isDown = true;
			if (e.target === stage) {
				if (!isShiftPressed) selectedSeatIds = [];
				drawSeatingMap();
			}

			const t = e.target;
			const isSeat =
				t?.className === 'Circle' && t?.parent?.id() && !t.parent.id().startsWith('group-');
			if (isSeat) {
				const seatId = t.parent.id();
				let idsToToggle = [seatId];

				if (selectionTool === 'row' || selectionTool === 'section') {
					let targetSection = null;
					let targetRow = null;
					for (const sec of rsSections) {
						for (const row of sec.rows || []) {
							if ((row.seats || []).some((s: any) => s.id === seatId)) {
								targetSection = sec;
								targetRow = row;
								break;
							}
						}
						if (targetSection) break;
					}

					if (targetSection && targetRow) {
						if (selectionTool === 'row') {
							idsToToggle = (targetRow.seats || []).map((s: any) => s.id);
						} else if (selectionTool === 'section') {
							idsToToggle = [];
							for (const r of targetSection.rows || []) {
								idsToToggle.push(...(r.seats || []).map((s: any) => s.id));
							}
						}
					}
				}

				if (isShiftPressed) {
					const allSelected = idsToToggle.every((id) => selectedSeatIds.includes(id));
					if (allSelected) {
						selectedSeatIds = selectedSeatIds.filter((x: any) => !idsToToggle.includes(x));
					} else {
						selectedSeatIds = [...new Set([...selectedSeatIds, ...idsToToggle])];
					}
				} else {
					selectedSeatIds = [...new Set([...selectedSeatIds, ...idsToToggle])];
				}
				drawSeatingMap();
				return;
			}

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
			if (!selectionRect.visible()) return;
			selectionRect.visible(false);
			const rx = selectionRect.x(),
				ry = selectionRect.y(),
				rw = selectionRect.width(),
				rh = selectionRect.height();
			const sel: string[] = [];
			rsSections.forEach((section: any) => {
				const boxX = section.x,
					boxY = section.y;
				if (
					boxX > rx + rw ||
					boxX + section.width < rx ||
					boxY > ry + rh ||
					boxY + section.height < ry
				)
					return;
				(section.rows || []).forEach((row: any) => {
					(row.seats || []).forEach((seat: any) => {
						const sx = seat.positionX ?? 0,
							sy = seat.positionY ?? 0;
						if (sx >= rx && sx <= rx + rw && sy >= ry && sy <= ry + rh) sel.push(seat.id);
					});
				});
			});
			if (isShiftPressed) {
				const newSel = new Set([...selectedSeatIds, ...sel]);
				selectedSeatIds = Array.from(newSel);
			} else {
				selectedSeatIds = Array.from(new Set([...selectedSeatIds, ...sel]));
			}
			drawSeatingMap();
		});
	}

	function updateViewport() {
		if (!stage) return;
		const s = stage.scaleX() || 1;
		currentScale = s;
		viewportX = -stage.x() / s;
		viewportY = -stage.y() / s;
		viewportW = stage.width() / s;
		viewportH = stage.height() / s;
		viewport = { x: viewportX, y: viewportY, w: viewportW, h: viewportH, scale: currentScale };
	}

	export function redraw() {
		drawSeatingMap();
	}

	export function fitToView() {
		if (!stage) return;
		const pad = 40;
		const sw = stage.width() - pad * 2,
			sh = stage.height() - pad * 2;
		const bw = _bounds.width || 1,
			bh = _bounds.height || 1;
		const scale = Math.min(sw / bw, sh / bh);
		fitScale = scale;
		stage.scale({ x: scale, y: scale });
		stage.x(pad + (sw - bw * scale) / 2 - _bounds.minX * scale);
		stage.y(pad + (sh - bh * scale) / 2 - _bounds.minY * scale);
		stage.batchDraw();
		updateViewport();
		drawSeatingMap();
	}

	export function zoomIn() {
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

	export function zoomOut() {
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

	onMount(() => {
		if (!browser) return;
		window.addEventListener('keydown', (e) => {
			if (e.key === 'Shift') isShiftPressed = true;
		});
		window.addEventListener('keyup', (e) => {
			if (e.key === 'Shift') isShiftPressed = false;
		});

		requestAnimationFrame(async () => {
			const konvaModule = await import('konva');
			Konva = konvaModule.default;

			const width = containerRef?.offsetWidth || 800;
			const height = containerRef?.offsetHeight || 600;

			stage = new Konva.Stage({ container: containerRef, width, height, draggable: true });
			layer = new Konva.Layer();
			stage.add(layer);

			selectionRect = new Konva.Rect({
				fill: 'rgba(59,130,246,0.1)',
				stroke: '#3b82f6',
				strokeWidth: 1.5,
				visible: false
			});
			layer.add(selectionRect);

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
				setTimeout(drawSeatingMap, 50);
			});

			stage.on('dragmove dragend', updateViewport);

			const resizeObserver = new ResizeObserver(() => {
				if (stage && containerRef) {
					stage.width(containerRef.offsetWidth);
					stage.height(containerRef.offsetHeight);
					drawSeatingMap();
				}
			});
			if (containerRef) resizeObserver.observe(containerRef);

			setupCanvasEvents();
			setTimeout(() => {
				fitToView();
			}, 100);

			// cleanup
			return () => {
				resizeObserver.disconnect();
				stage.destroy();
			};
		});
	});

	$effect(() => {
		if (stage) {
			stage.draggable(true);
			stage.container().style.cursor = pointer2Cursor;
		}
	});

	$effect(() => {
		if (activeTab || manifestDetail || selectedSeatIds || seatPriceLevels || basePriceLevels) {
			if (stage) drawSeatingMap();
		}
	});

	$effect(() => {
		if (manifestDetail && stage) {
			setTimeout(fitToView, 50);
		}
	});
</script>

<div class="absolute inset-0 z-0 h-full w-full" bind:this={containerRef}></div>
