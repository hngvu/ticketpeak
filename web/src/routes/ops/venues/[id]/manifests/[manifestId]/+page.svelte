<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any, svelte/no-navigation-without-resolve, svelte/require-each-key */
	import { onMount, onDestroy } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';

	let { data } = $props<{ data: any }>();

	const venue = $derived(data.venue || {});

	// Mutable manifest details in Svelte local state
	let manifestIdVal = $state(data.manifest?.id || '');
	let manifestDescVal = $state(data.manifest?.description || '');
	let manifestCapVal = $state(data.manifest?.totalCapacity || 10000);

	// Seating Map Data (locally updated during edits)
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

	// Active tab in sidebar for configs
	let activeTab = $state<'levels' | 'sections' | 'priceLevels' | 'areas'>('levels');

	// Active layout mode
	let activeMode = $state<'scaling' | 'inventory' | 'floor-edit'>('scaling');

	// Active selection tool (Seat, Row, Section)
	let selectionTool = $state<'seat' | 'row' | 'section'>('seat');

	// Active canvas interaction tool (Select, Pan, Brush, Eraser)
	let activeTool = $state<'select' | 'pan' | 'brush' | 'eraser'>('select');
	let previousToolBeforeSpace = $state<'select' | 'pan' | 'brush' | 'eraser'>('select');

	// No pricing configuration on this screen

	// UI Dialog controls
	let showExportDropdown = $state(false);
	let showFilterDialog = $state(false);
	let showAssistantDialog = $state(false);
	let assistantPrompt = $state('');

	// Seating Filters
	let onlyAvailable = $state(false);
	let onlyAccessible = $state(false);
	let onlyObstructed = $state(false);

	// Canvas Selection State
	let selectedSeatIds = $state<string[]>([]);
	let selectedRsAreaId = $state('');
	let isSaving = $state(false);
	let saveMessage = $state('');
	let errorMessage = $state('');

	// Floor edit snap grid
	let snapGrid = $state(8);

	// Seating Block Generator Input binds
	let blockRowPrefix = $state('A');
	let blockRowsCount = $state(5);
	let blockSeatsPerRow = $state(10);
	let blockSeatStartNum = $state(1);
	let blockShape = $state<'rectangle' | 'trapezoid' | 'diamond' | 'staggered'>('rectangle');
	// Form inputs for creating lookup tables and areas
	let levelCode = $state('');
	let levelColor = $state('#3B82F6');

	let sectionCode = $state('');
	let sectionColor = $state('#10B981');

	let areaType = $state<'GA' | 'RS'>('RS');
	let areaLevelId = $state('');
	let areaSectionId = $state('');
	let areaPriceLevelId = $state('STANDARD');
	let areaCapacity = $state(500);

	// Konva references
	let Konva: any;
	let stage = $state<any>();
	let layer = $state<any>();
	let stageContainer: HTMLDivElement | null = $state(null);

	// Multi-select box variables
	let selectionRect: any;
	let x1 = 0,
		y1 = 0,
		x2 = 0,
		y2 = 0;
	let isShiftPressed = $state(false);

	// Viewport state for Mini-map
	let viewportX = $state(0);
	let viewportY = $state(0);
	let viewportW = $state(800);
	let viewportH = $state(600);

	// Pricing metrics removed from layout creation

	// Layout bounds calculation for Mini-map ViewBox
	const bounds = $derived.by(() => {
		let xs = [0, 800];
		let ys = [0, 600];
		rsAreas.forEach((area) => {
			(area.rows || []).forEach((row: any) => {
				if (row.positionY !== undefined) ys.push(row.positionY);
				(row.seats || []).forEach((seat: any) => {
					if (seat.positionX !== undefined) xs.push(seat.positionX);
					if (seat.positionY !== undefined) ys.push(seat.positionY);
				});
			});
		});
		gaAreas.forEach((ga, idx) => {
			const x = ga.x || 50 + idx * 220;
			const y = ga.y || 50;
			xs.push(x, x + 200);
			ys.push(y, y + 100);
		});
		const minX = Math.min(...xs) - 80;
		const maxX = Math.max(...xs) + 120;
		const minY = Math.min(...ys) - 80;
		const maxY = Math.max(...ys) + 120;
		return { minX, maxX, minY, maxY, width: maxX - minX, height: maxY - minY };
	});

	// Reactive financial numbers
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

	// Revenue calculations removed from layout creation

	// Load Konva.js dynamically on mount
	onMount(() => {
		if (!browser) return;

		async function init() {
			const konvaModule = await import('konva');
			Konva = konvaModule.default;

			const width = stageContainer?.offsetWidth || 800;
			const height = stageContainer?.offsetHeight || 600;

			stage = new Konva.Stage({
				container: 'stage-container',
				width: width,
				height: height,
				draggable: activeTool === 'pan'
			});

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

			// Zoom using wheel listener
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
				newScale = Math.max(0.15, Math.min(newScale, 4));

				stage.scale({ x: newScale, y: newScale });

				const newPos = {
					x: pointer.x - mousePointTo.x * newScale,
					y: pointer.y - mousePointTo.y * newScale
				};
				stage.position(newPos);
				stage.batchDraw();
				updateViewport();
			});

			drawSeatingMap();
			setupCanvasEvents();
			updateViewport();

			// Listen for stage changes to keep viewport state in sync
			stage.on('dragmove dragend xChange yChange scaleXChange scaleYChange', updateViewport);
		}

		init();
	});

	function deleteSelectedSeats() {
		if (selectedSeatIds.length === 0) return;
		rsAreas.forEach((area) => {
			if (area.rows) {
				area.rows.forEach((row: any) => {
					if (row.seats) {
						row.seats = row.seats.filter((seat: any) => !selectedSeatIds.includes(seat.id));
					}
				});
				area.rows = area.rows.filter((row: any) => row.seats && row.seats.length > 0);
			}
		});
		selectedSeatIds = [];
		drawSeatingMap();
	}

	function applyCurvature(area: any, curvature: number) {
		if (!area || !area.rows) return;
		area.curvature = curvature;
		area.rows.forEach((row: any) => {
			const seats = row.seats || [];
			if (seats.length <= 1) return;
			const xs = seats.map((s: any) => s.positionX);
			const minX = Math.min(...xs);
			const maxX = Math.max(...xs);
			const centerX = (minX + maxX) / 2;

			seats.forEach((seat: any) => {
				const dx = seat.positionX - centerX;
				const offset = (curvature * dx * dx) / 2000;
				seat.positionY = Math.round(row.positionY + offset);
			});
		});
		drawSeatingMap();
	}

	function buildRectangularRows(
		areaId: string,
		rowPrefix: string,
		rowsCount: number,
		seatsPerRow: number,
		seatStartNum: number,
		startY = 220,
		startX = 120
	) {
		const rows: any[] = [];
		const startRowCode = rowPrefix.toUpperCase().charCodeAt(0);

		for (let rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
			const rowName = String.fromCharCode(startRowCode + rowIndex);
			const rowId = `${areaId}-row-${rowName}-${Date.now()}-${rowIndex}`;
			const rowY = startY + rowIndex * 48;
			const seats: any[] = [];

			for (let seatIndex = 0; seatIndex < seatsPerRow; seatIndex++) {
				const seatNum = seatStartNum + seatIndex;
				seats.push({
					id: `${rowId}-seat-${seatNum}`,
					name: String(seatNum).padStart(3, '0'),
					positionX: startX + seatIndex * 32,
					status: 'AVAILABLE',
					accessibility: false,
					obstructedView: false,
					aisle: false
				});
			}

			rows.push({
				id: rowId,
				name: rowName,
				positionY: rowY,
				seats
			});
		}

		return rows;
	}

	function findSeatById(seatId: string) {
		for (const area of rsAreas) {
			for (const row of area.rows || []) {
				const seat = (row.seats || []).find((candidate: any) => candidate.id === seatId);
				if (seat) {
					return { area, row, seat };
				}
			}
		}
		return null;
	}

	function removeSeatById(seatId: string) {
		selectedSeatIds = selectedSeatIds.filter((id) => id !== seatId);
		rsAreas.forEach((area) => {
			if (!area.rows) return;
			area.rows.forEach((row: any) => {
				if (row.seats) {
					row.seats = row.seats.filter((seat: any) => seat.id !== seatId);
				}
			});
			area.rows = area.rows.filter((row: any) => row.seats && row.seats.length > 0);
		});
	}

	function paintSeat(seatId: string, priceLevelId: string) {
		const found = findSeatById(seatId);
		if (!found) return;
		found.seat.priceLevelId = priceLevelId;
	}

	function handleKeyDown(e: KeyboardEvent) {
		const activeEl = document.activeElement;
		if (
			activeEl &&
			(activeEl.tagName === 'INPUT' ||
				activeEl.tagName === 'SELECT' ||
				activeEl.tagName === 'TEXTAREA')
		) {
			return;
		}

		if (e.key === 'Shift') {
			isShiftPressed = true;
		}
		if (e.code === 'Space') {
			previousToolBeforeSpace = activeTool;
			activeTool = 'pan';
			e.preventDefault();
		}
		if (e.key.toLowerCase() === 'v') {
			activeTool = 'select';
		}
		if (e.key.toLowerCase() === 'b') {
			activeTool = 'brush';
		}
		if (e.key.toLowerCase() === 'e') {
			activeTool = 'eraser';
		}
		if (e.key === 'Delete' || e.key === 'Backspace') {
			deleteSelectedSeats();
		}
	}

	function handleKeyUp(e: KeyboardEvent) {
		if (e.key === 'Shift') {
			isShiftPressed = false;
		}
		if (e.code === 'Space') {
			activeTool = previousToolBeforeSpace;
		}
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

	function handleMinimapClick(e: MouseEvent) {
		const svg = e.currentTarget as SVGSVGElement;
		const rect = svg.getBoundingClientRect();
		const rx = (e.clientX - rect.left) / rect.width;
		const ry = (e.clientY - rect.top) / rect.height;
		const canvasX = bounds.minX + rx * bounds.width;
		const canvasY = bounds.minY + ry * bounds.height;

		if (stage) {
			const scale = stage.scaleX() || 1;
			stage.x(-canvasX * scale + stage.width() / 2);
			stage.y(-canvasY * scale + stage.height() / 2);
			stage.batchDraw();
			updateViewport();
		}
	}

	$effect(() => {
		if (stage) {
			stage.draggable(activeTool === 'pan');
			stage.cursor(
				activeTool === 'pan'
					? 'grab'
					: activeTool === 'brush'
						? 'copy'
						: activeTool === 'eraser'
							? 'cell'
							: 'default'
			);
		}
	});

	$effect(() => {
		if (!brushPriceLevelId && priceLevels.length > 0) {
			brushPriceLevelId = priceLevels[0].id;
		}
	});

	// Redraw canvas whenever mode, filters or state changes
	$effect(() => {
		if (activeMode || onlyAvailable || onlyAccessible || onlyObstructed) {
			drawSeatingMap();
		}
	});

	function drawSeatingMap() {
		if (!stage || !layer) return;

		// Clear previous drawings except selection box
		layer.destroyChildren();
		layer.add(selectionRect);

		drawBackgroundGrid();

		// Draw GA Areas
		gaAreas.forEach((ga, idx) => {
			const x = ga.x || 50 + idx * 220;
			const y = ga.y || 50;

			const group = new Konva.Group({
				x: ga.x || x,
				y: ga.y || y,
				draggable: activeMode === 'floor-edit',
				id: ga.id
			});

			const sec = sections.find((s) => s.id === ga.sectionId);
			const fillColor = sec ? sec.color : '#E2E8F0';

			const rect = new Konva.Rect({
				width: 200,
				height: 100,
				fill: fillColor + '20', // translucent overlay
				stroke: fillColor,
				strokeWidth: 2,
				cornerRadius: 12,
				shadowColor: '#000',
				shadowBlur: 3,
				shadowOpacity: 0.05
			});

			const title = new Konva.Text({
				text: `GA Area: ${ga.id}`,
				fontSize: 11,
				fontStyle: 'bold',
				fill: '#0F172A',
				x: 14,
				y: 14
			});

			const details = new Konva.Text({
				text: `Level: ${ga.levelId}\nSection: ${ga.sectionId}\nCapacity: ${ga.capacity}`,
				fontSize: 10,
				fill: '#475569',
				x: 14,
				y: 36,
				lineHeight: 1.4
			});

			group.add(rect, title, details);

			group.on('dragend', () => {
				ga.x = Math.round(group.x() / snapGrid) * snapGrid;
				ga.y = Math.round(group.y() / snapGrid) * snapGrid;
				drawSeatingMap();
				updateViewport();
			});

			layer.add(group);
		});

		// Draw RS Areas
		rsAreas.forEach((area) => {
			const rows = area.rows || [];
			if (rows.length === 0) return;

			// Calculate bounds of this RS Area based purely on seat positions
			let minX = Infinity;
			let maxX = -Infinity;
			let minY = Infinity;
			let maxY = -Infinity;

			rows.forEach((row: any, rIdx: number) => {
				const rowY =
					row.positionY !== undefined && row.positionY !== null ? row.positionY : 200 + rIdx * 50;

				const seats = row.seats || [];
				seats.forEach((seat: any, sIdx: number) => {
					const seatX =
						seat.positionX !== undefined && seat.positionX !== null
							? seat.positionX
							: 100 + sIdx * 32;
					if (seatX < minX) minX = seatX;
					if (seatX > maxX) maxX = seatX;

					const seatY =
						seat.positionY !== undefined && seat.positionY !== null ? seat.positionY : rowY;
					if (seatY < minY) minY = seatY;
					if (seatY > maxY) maxY = seatY;
				});
			});

			if (minX === Infinity || maxX === -Infinity || minY === Infinity || maxY === -Infinity)
				return;

			// Add margins for padding around the seats (tight crop)
			const paddingX = 25;
			const paddingY = 25;

			const boxX = minX - paddingX;
			const boxY = minY - paddingY;
			const boxW = maxX - minX + paddingX * 2;
			const boxH = maxY - minY + paddingY * 2;

			const areaGroup = new Konva.Group({
				x: boxX,
				y: boxY,
				draggable: activeMode === 'floor-edit',
				id: 'group-' + area.id
			});

			// Bounding rectangle
			const sec = sections.find((s) => s.id === area.sectionId);
			const defaultColor = sec ? sec.color : '#3B82F6';
			const isSelectedArea = selectedRsAreaId === area.id;

			// Solid light-gray border wrapping the seating block, matching professional diagrams
			const borderRect = new Konva.Rect({
				x: 0,
				y: 0,
				width: boxW,
				height: boxH,
				fill: 'rgba(148, 163, 184, 0.01)',
				stroke: isSelectedArea ? '#0F172A' : '#E2E8F0', // solid gray border
				strokeWidth: isSelectedArea ? 2.5 : 1.5,
				cornerRadius: 12
			});

			// Large centered watermark label inside the area box (e.g. 219, 220, VIP, etc.)
			const centerLabel = new Konva.Text({
				text: area.sectionId.toUpperCase(),
				fontSize: Math.max(20, Math.min(boxW, boxH) * 0.25),
				fontStyle: 'bold',
				fill: 'rgba(148, 163, 184, 0.12)', // light gray semi-transparent watermark
				align: 'center',
				verticalAlign: 'middle',
				x: 0,
				y: 0,
				width: boxW,
				height: boxH
			});

			areaGroup.add(borderRect, centerLabel);

			areaGroup.on('click tap', () => {
				selectedRsAreaId = area.id;
				drawSeatingMap();
			});

			// Drag interactions for the entire Area
			borderRect.on('mouseenter', () => {
				if (activeMode === 'floor-edit') {
					stage.container().style.cursor = 'move';
				}
			});
			borderRect.on('mouseleave', () => {
				if (activeMode === 'floor-edit') {
					stage.container().style.cursor = 'default';
				}
			});

			areaGroup.on('dragend', () => {
				const dx = Math.round((areaGroup.x() - boxX) / snapGrid) * snapGrid;
				const dy = Math.round((areaGroup.y() - boxY) / snapGrid) * snapGrid;

				// Shift all rows and seats in this area
				rows.forEach((row: any, rIdx: number) => {
					const rowY =
						row.positionY !== undefined && row.positionY !== null ? row.positionY : 200 + rIdx * 50;
					row.positionY = rowY + dy;

					const seats = row.seats || [];
					seats.forEach((seat: any, sIdx: number) => {
						const seatX =
							seat.positionX !== undefined && seat.positionX !== null
								? seat.positionX
								: 100 + sIdx * 32;
						seat.positionX = seatX + dx;

						const seatY =
							seat.positionY !== undefined && seat.positionY !== null ? seat.positionY : rowY;
						seat.positionY = seatY + dy;
					});
				});

				drawSeatingMap();
				updateViewport();
			});

			// Draw row content relative to the group
			rows.forEach((row: any, rIdx: number) => {
				const rowY =
					row.positionY !== undefined && row.positionY !== null ? row.positionY : 200 + rIdx * 50;

				// Relative Y position of this row
				const relY = rowY - boxY;

				const seats = row.seats || [];
				seats.forEach((seat: any, sIdx: number) => {
					const seatX =
						seat.positionX !== undefined && seat.positionX !== null
							? seat.positionX
							: 100 + sIdx * 32;

					// Relative X position of this seat
					const relX = seatX - boxX;

					// Filters check
					let isFiltered = false;
					if (onlyAvailable && seat.status !== 'AVAILABLE') isFiltered = true;
					if (onlyAccessible && !seat.accessibility) isFiltered = true;
					if (onlyObstructed && !seat.obstructedView) isFiltered = true;

					const isSelected = selectedSeatIds.includes(seat.id);

					// Dynamic color mappings based on mode & selection
					const seatPriceLevelId = seat.priceLevelId || area.priceLevelId;
					const seatPriceLevel = priceLevels.find((p) => p.id === seatPriceLevelId);
					let seatColor = seatPriceLevel?.color || defaultColor;
					if (isSelected) {
						seatColor = '#1A1A1A'; // turn black temporarily when selected
					} else if (activeMode === 'inventory') {
						seatColor = seat.status === 'AVAILABLE' ? '#10B981' : '#EF4444'; // Green if open, Red if blocked
					} else if (seat.status === 'UNAVAILABLE') {
						seatColor = '#E2E8F0';
					}

					const seatGroup = new Konva.Group({
						x: relX,
						y: relY,
						id: seat.id,
						draggable: activeMode === 'floor-edit'
					});

					const seatCircle = new Konva.Circle({
						radius: 10,
						fill: seatColor,
						opacity: isFiltered ? 0.15 : 1.0,
						stroke: isSelected ? '#FFFFFF' : seat.obstructedView ? '#EF4444' : 'transparent',
						strokeWidth: isSelected ? 2.5 : 1.5,
						shadowColor: '#000',
						shadowBlur: isSelected ? 4 : 0,
						shadowOpacity: 0.15
					});

					let innerIcon: any;
					if (seat.accessibility) {
						innerIcon = new Konva.Circle({
							radius: 3,
							fill: '#FFFFFF'
						});
					} else if (seat.aisle) {
						innerIcon = new Konva.Line({
							points: [-3, 0, 3, 0],
							stroke: '#FFFFFF',
							strokeWidth: 2
						});
					}

					seatGroup.add(seatCircle);
					if (innerIcon) seatGroup.add(innerIcon);

					// Tap & click triggers
					seatCircle.on('click tap', (e: any) => {
						if (activeTool !== 'select' && activeTool !== 'brush' && activeTool !== 'eraser')
							return;
						e.cancelBubble = true;

						if (activeTool === 'brush') {
							const targetPriceLevelId = brushPriceLevelId || area.priceLevelId;
							if (targetPriceLevelId) {
								paintSeat(seat.id, targetPriceLevelId);
								drawSeatingMap();
							}
							return;
						}

						if (activeTool === 'eraser') {
							removeSeatById(seat.id);
							drawSeatingMap();
							return;
						}

						let targetIds: string[] = [];
						if (selectionTool === 'seat') {
							targetIds = [seat.id];
						} else if (selectionTool === 'row') {
							targetIds = row.seats.map((s: any) => s.id);
						} else if (selectionTool === 'section') {
							targetIds = [];
							rows.forEach((r: any) => {
								targetIds.push(...(r.seats || []).map((s: any) => s.id));
							});
						}

						const allSelected = targetIds.every((id) => selectedSeatIds.includes(id));
						if (allSelected) {
							selectedSeatIds = selectedSeatIds.filter((id) => !targetIds.includes(id));
						} else {
							selectedSeatIds = Array.from(new Set([...selectedSeatIds, ...targetIds]));
						}
						drawSeatingMap();
					});

					// Cursor interactions
					seatCircle.on('mouseenter', () => {
						if (activeTool === 'select' || activeTool === 'brush' || activeTool === 'eraser') {
							stage.container().style.cursor = 'pointer';
						}
					});
					seatCircle.on('mouseleave', () => {
						if (activeTool === 'select' || activeTool === 'brush' || activeTool === 'eraser') {
							stage.container().style.cursor = 'default';
						}
					});

					seatGroup.on('dragend', () => {
						const snappedX = Math.round(seatGroup.x() / snapGrid) * snapGrid;
						const snappedY = Math.round(seatGroup.y() / snapGrid) * snapGrid;

						// Save back absolute coordinates (using current parent bounding box coords)
						seat.positionX = boxX + snappedX;
						row.positionY = boxY + snappedY;

						drawSeatingMap();
						updateViewport();
					});

					areaGroup.add(seatGroup);
				});
			});

			layer.add(areaGroup);
		});

		layer.batchDraw();
	}

	function drawBackgroundGrid() {
		if (!stage || !layer) return;
		for (let i = -10; i < 60; i++) {
			const xLine = new Konva.Line({
				points: [i * 40, -1000, i * 40, 2000],
				stroke: '#F1F5F9',
				strokeWidth: 1
			});
			layer.add(xLine);

			const yLine = new Konva.Line({
				points: [-1000, i * 40, 2000, i * 40],
				stroke: '#F1F5F9',
				strokeWidth: 1
			});
			layer.add(yLine);
		}
	}

	function setupCanvasEvents() {
		if (!stage) return;

		let isMouseDown = false;
		let brushModeActive = false;

		stage.on('mousedown touchstart', (e: any) => {
			isMouseDown = true;
			const target = e.target;
			const isSeat =
				target &&
				(target.className === 'Circle' ||
					(target.parent && target.parent.id() && !target.parent.id().startsWith('group-')));
			if (isSeat) {
				brushModeActive = true;
				const seatId = target.parent?.id() || target.id();
				if (seatId && !seatId.startsWith('group-') && seatId !== stage.id()) {
					if (activeTool === 'eraser') {
						removeSeatById(seatId);
						drawSeatingMap();
					} else if (activeTool === 'brush') {
						const found = findSeatById(seatId);
						const targetPriceLevelId = brushPriceLevelId || found?.area?.priceLevelId;
						if (targetPriceLevelId) {
							paintSeat(seatId, targetPriceLevelId);
							drawSeatingMap();
						}
					} else if (activeTool === 'select') {
						if (isShiftPressed) {
							if (selectedSeatIds.includes(seatId)) {
								selectedSeatIds = selectedSeatIds.filter((id) => id !== seatId);
							} else {
								selectedSeatIds = [...selectedSeatIds, seatId];
							}
						} else if (!selectedSeatIds.includes(seatId)) {
							selectedSeatIds = [...selectedSeatIds, seatId];
						}
						drawSeatingMap();
					}
				}
				return;
			}

			if (activeTool !== 'select') return;
			if (e.target !== stage) return;

			brushModeActive = false;
			const pos = stage.getPointerPosition();
			if (!pos) return;

			const scale = stage.scaleX() || 1;
			x1 = (pos.x - stage.x()) / scale;
			y1 = (pos.y - stage.y()) / scale;
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
			if (!isMouseDown) return;

			if (brushModeActive) {
				const pos = stage.getPointerPosition();
				if (!pos) return;
				const shape = stage.getIntersection(pos);
				if (shape && shape.className === 'Circle') {
					const seatId = shape.parent?.id() || shape.id();
					if (seatId && !seatId.startsWith('group-') && seatId !== stage.id()) {
						if (activeTool === 'eraser') {
							removeSeatById(seatId);
							drawSeatingMap();
						} else if (activeTool === 'brush') {
							const found = findSeatById(seatId);
							const targetPriceLevelId = brushPriceLevelId || found?.area?.priceLevelId;
							if (targetPriceLevelId) {
								paintSeat(seatId, targetPriceLevelId);
								drawSeatingMap();
							}
						} else if (activeTool === 'select') {
							if (!selectedSeatIds.includes(seatId)) {
								selectedSeatIds = [...selectedSeatIds, seatId];
								drawSeatingMap();
							}
						}
					}
				}
				return;
			}

			if (!selectionRect.visible()) return;

			const pos = stage.getPointerPosition();
			if (!pos) return;

			const scale = stage.scaleX() || 1;
			x2 = (pos.x - stage.x()) / scale;
			y2 = (pos.y - stage.y()) / scale;

			selectionRect.setAttrs({
				x: Math.min(x1, x2),
				y: Math.min(y1, y2),
				width: Math.abs(x2 - x1),
				height: Math.abs(y2 - y1)
			});
			layer.batchDraw();
		});

		stage.on('mouseup touchend', () => {
			isMouseDown = false;
			brushModeActive = false;
			if (!selectionRect.visible()) return;

			selectionRect.visible(false);

			const rectX = selectionRect.x();
			const rectY = selectionRect.y();
			const rectW = selectionRect.width();
			const rectH = selectionRect.height();

			const selected: string[] = [];

			rsAreas.forEach((area) => {
				const rows = area.rows || [];
				rows.forEach((row: any) => {
					const seats = row.seats || [];
					seats.forEach((seat: any) => {
						const seatX =
							seat.positionX !== undefined && seat.positionX !== null ? seat.positionX : 0;
						const rowY = row.positionY !== undefined && row.positionY !== null ? row.positionY : 0;
						const seatY =
							seat.positionY !== undefined && seat.positionY !== null ? seat.positionY : rowY;

						if (
							seatX >= rectX &&
							seatX <= rectX + rectW &&
							seatY >= rectY &&
							seatY <= rectY + rectH
						) {
							if (selectionTool === 'seat') {
								selected.push(seat.id);
							} else if (selectionTool === 'row') {
								row.seats.forEach((s: any) => selected.push(s.id));
							} else if (selectionTool === 'section') {
								rows.forEach((r: any) => {
									r.seats.forEach((s: any) => selected.push(s.id));
								});
							}
						}
					});
				});
			});

			if (selected.length > 0) {
				if (isShiftPressed) {
					selectedSeatIds = Array.from(new Set([...selectedSeatIds, ...selected]));
				} else {
					selectedSeatIds = selected;
				}
			} else {
				if (!isShiftPressed) {
					selectedSeatIds = [];
				}
			}

			drawSeatingMap();
		});
	}

	function assignPriceLevelToSelected(priceLevelId: string) {
		if (selectedSeatIds.length === 0) return;

		rsAreas.forEach((area) => {
			const rows = area.rows || [];
			rows.forEach((row: any) => {
				const seats = row.seats || [];
				seats.forEach((seat: any) => {
					if (selectedSeatIds.includes(seat.id)) {
						seat.priceLevelId = priceLevelId;
					}
				});
			});
		});
		rsAreas = [...rsAreas];

		saveMessage = `Assigned tier ${priceLevelId} to selected seats!`;
		selectedSeatIds = [];
		drawSeatingMap();
	}

	// Trigger quick inventory overrides in Inventory Mode
	function setInventoryStatus(status: 'AVAILABLE' | 'UNAVAILABLE') {
		if (selectedSeatIds.length === 0) return;

		rsAreas.forEach((area) => {
			(area.rows || []).forEach((row: any) => {
				(row.seats || []).forEach((seat: any) => {
					if (selectedSeatIds.includes(seat.id)) {
						seat.status = status;
					}
				});
			});
		});

		rsAreas = [...rsAreas];
		saveMessage = `Marked ${selectedSeatIds.length} seats as ${status}`;
		selectedSeatIds = [];
		drawSeatingMap();
	}

	function runAssistantPrompt() {
		if (!assistantPrompt) return;
		const query = assistantPrompt.toLowerCase();
		let targetIds: string[] = [];

		if (query.includes('vip') || query.includes('gold')) {
			rsAreas.forEach((area) => {
				if (
					area.priceLevelId.toLowerCase().includes('vip') ||
					area.priceLevelId.toLowerCase().includes('gold')
				) {
					(area.rows || []).forEach((row: any) => {
						targetIds.push(...(row.seats || []).map((s: any) => s.id));
					});
				}
			});
		} else if (query.includes('p1') || query.includes('standard')) {
			rsAreas.forEach((area) => {
				if (
					area.priceLevelId.toLowerCase().includes('standard') ||
					area.priceLevelId.toLowerCase().includes('p1')
				) {
					(area.rows || []).forEach((row: any) => {
						targetIds.push(...(row.seats || []).map((s: any) => s.id));
					});
				}
			});
		} else if (query.includes('row')) {
			const match = query.match(/row\s+([a-z0-9]+)/i);
			if (match) {
				const rowName = match[1].toUpperCase();
				rsAreas.forEach((area) => {
					(area.rows || []).forEach((row: any) => {
						if (row.name.toUpperCase() === rowName) {
							targetIds.push(...(row.seats || []).map((s: any) => s.id));
						}
					});
				});
			}
		} else if (query.includes('all')) {
			rsAreas.forEach((area) => {
				(area.rows || []).forEach((row: any) => {
					targetIds.push(...(row.seats || []).map((s: any) => s.id));
				});
			});
		}

		if (targetIds.length > 0) {
			selectedSeatIds = targetIds;
			saveMessage = `Assistant selected ${targetIds.length} seats.`;
			errorMessage = '';
			drawSeatingMap();
		} else {
			errorMessage = `No matching seats found for '${assistantPrompt}'. Try 'Select VIP' or 'Select Row A'.`;
		}
		assistantPrompt = '';
	}

	function exportLayout(format: 'json' | 'csv' | 'svg') {
		let filename = `manifest-${manifestIdVal || 'export'}-${Date.now()}`;
		let content = '';
		let mimeType = 'text/plain';

		if (format === 'json') {
			content = JSON.stringify({ levels, sections, priceLevels, gaAreas, rsAreas }, null, 2);
			mimeType = 'application/json';
			filename += '.json';
		} else if (format === 'csv') {
			let rowsList = [
				'SeatID,AreaID,Row,SeatName,PositionX,PositionY,Status,Accessibility,ObstructedView,Aisle'
			];
			rsAreas.forEach((area) => {
				(area.rows || []).forEach((row: any) => {
					(row.seats || []).forEach((seat: any) => {
						rowsList.push(
							[
								seat.id,
								area.id,
								row.name,
								seat.name,
								seat.positionX,
								row.positionY,
								seat.status,
								seat.accessibility || false,
								seat.obstructedView || false,
								seat.aisle || false
							].join(',')
						);
					});
				});
			});
			content = rowsList.join('\n');
			mimeType = 'text/csv';
			filename += '.csv';
		} else if (format === 'svg') {
			let svgContent = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${bounds.minX} ${bounds.minY} ${bounds.width} ${bounds.height}" width="100%" height="100%" style="background:#FAFAFA;">\n`;
			gaAreas.forEach((ga) => {
				svgContent += `  <rect x="${ga.x || 50}" y="${ga.y || 50}" width="200" height="100" rx="12" fill="rgba(15, 23, 42, 0.05)" stroke="#000000" stroke-width="2" />\n`;
				svgContent += `  <text x="${(ga.x || 50) + 14}" y="${(ga.y || 50) + 26}" font-family="sans-serif" font-size="11" font-weight="bold" fill="#0F172A">${ga.id}</text>\n`;
			});
			rsAreas.forEach((area) => {
				const pl = priceLevels.find((p) => p.id === area.priceLevelId);
				const color = pl ? pl.color : '#3B82F6';
				(area.rows || []).forEach((row: any) => {
					svgContent += `  <text x="35" y="${row.positionY + 4}" font-family="sans-serif" font-size="11" font-weight="bold" fill="#64748B">${row.name}</text>\n`;
					(row.seats || []).forEach((seat: any) => {
						svgContent += `  <circle cx="${seat.positionX}" cy="${row.positionY}" r="10" fill="${seat.status === 'UNAVAILABLE' ? '#E2E8F0' : color}" />\n`;
					});
				});
			});
			svgContent += '</svg>';
			content = svgContent;
			mimeType = 'image/svg+xml';
			filename += '.svg';
		}

		const blob = new Blob([content], { type: mimeType });
		const url = URL.createObjectURL(blob);
		const link = document.createElement('a');
		link.href = url;
		link.download = filename;
		link.click();
		URL.revokeObjectURL(url);
	}

	// Visual Seating Block Creator
	function generateSeatingBlock() {
		if (!selectedRsAreaId) {
			errorMessage = 'Please select a Reserved Seating Area in the sidebar first.';
			return;
		}

		const targetArea = rsAreas.find((a) => a.id === selectedRsAreaId);
		if (!targetArea) return;

		const startRowCode = blockRowPrefix.toUpperCase().charCodeAt(0);
		const rowsList = targetArea.rows || [];

		for (let rIdx = 0; rIdx < blockRowsCount; rIdx++) {
			const rowName = String.fromCharCode(startRowCode + rIdx);
			const rowId = `${selectedRsAreaId}-row-${rowName}-${Date.now()}`;
			const rowY = 220 + rowsList.length * 48;

			const newRow: any = {
				id: rowId,
				name: rowName,
				positionY: rowY,
				seats: []
			};

			let rowSeatCount = blockSeatsPerRow;
			let startXOffset = 0; // offset in pixels

			if (blockShape === 'trapezoid') {
				// Each subsequent row has more seats (wedge shape), e.g. add 2 seats per row
				rowSeatCount = blockSeatsPerRow + rIdx * 2;
				// Center the row by shifting it left
				startXOffset = -rIdx * 16;
			} else if (blockShape === 'diamond') {
				// Peak row is in the middle
				const midRow = Math.floor(blockRowsCount / 2);
				const diff = Math.abs(rIdx - midRow);
				rowSeatCount = Math.max(1, blockSeatsPerRow - diff * 2);
				// Center the row
				startXOffset = diff * 16;
			} else if (blockShape === 'staggered') {
				rowSeatCount = blockSeatsPerRow;
				// Alternate rows shifted by half a seat width (16px)
				startXOffset = rIdx % 2 === 1 ? 16 : 0;
			}

			for (let sIdx = 0; sIdx < rowSeatCount; sIdx++) {
				const seatNum = blockSeatStartNum + sIdx;
				const seatId = `${rowId}-seat-${seatNum}`;
				const seatX = 120 + sIdx * 32 + startXOffset;

				newRow.seats.push({
					id: seatId,
					name: String(seatNum).padStart(3, '0'),
					positionX: seatX,
					status: 'AVAILABLE',
					accessibility: false,
					obstructedView: false,
					aisle: false
				});
			}

			rowsList.push(newRow);
		}

		targetArea.rows = rowsList;
		rsAreas = [...rsAreas];
		drawSeatingMap();
		errorMessage = '';
	}

	function toggleSelectedAttribute(attr: 'accessibility' | 'obstructedView' | 'aisle' | 'status') {
		if (selectedSeatIds.length === 0) return;

		rsAreas.forEach((area) => {
			const rows = area.rows || [];
			rows.forEach((row: any) => {
				const seats = row.seats || [];
				seats.forEach((seat: any) => {
					if (selectedSeatIds.includes(seat.id)) {
						if (attr === 'status') {
							seat.status = seat.status === 'AVAILABLE' ? 'UNAVAILABLE' : 'AVAILABLE';
						} else {
							seat[attr] = !seat[attr];
						}
					}
				});
			});
		});

		rsAreas = [...rsAreas];
		drawSeatingMap();
	}

	function createLookup(type: 'level' | 'section') {
		const code = type === 'level' ? levelCode : sectionCode;
		const desc = type === 'level' ? levelCode : sectionCode;
		const color = type === 'level' ? levelColor : sectionColor;

		if (!code || !desc) {
			errorMessage = 'Code is required.';
			return;
		}

		const newLookup = { id: code.trim(), description: desc.trim(), color };
		if (type === 'level') {
			levels = [...levels, newLookup];
			levelCode = '';
		} else {
			sections = [...sections, newLookup];
			sectionCode = '';
		}
		saveMessage = `Added ${type.toUpperCase()} locally. Save Layout to persist.`;
		errorMessage = '';
	}

	function createArea() {
		if (!areaLevelId || !areaSectionId || !areaPriceLevelId) {
			errorMessage = 'Level, Section, and Price Tier links are required to create an Area.';
			return;
		}

		if (areaType === 'RS' && (blockRowsCount < 1 || blockSeatsPerRow < 1)) {
			errorMessage = 'Row count and seats per row must be at least 1.';
			return;
		}

		const areaId =
			`${manifestIdVal}-${areaLevelId}-${areaSectionId}-${areaPriceLevelId}-${areaType}`.toUpperCase();

		const payload: any = {
			id: areaId,
			levelId: areaLevelId,
			sectionId: areaSectionId,
			priceLevelId: areaPriceLevelId
		};

		if (areaType === 'GA') {
			payload.capacity = areaCapacity;
			gaAreas = [...gaAreas, payload];
		} else {
			payload.rows = buildRectangularRows(
				areaId,
				blockRowPrefix,
				blockRowsCount,
				blockSeatsPerRow,
				blockSeatStartNum
			);
			payload.curvature = 0;
			rsAreas = [...rsAreas, payload];
			selectedRsAreaId = areaId;
		}

		saveMessage = `Added ${areaType} Area locally. Save Layout to persist.`;
		errorMessage = '';
		drawSeatingMap();
	}

	async function saveLayout() {
		isSaving = true;
		saveMessage = 'Saving seating layout configurations...';
		errorMessage = '';

		try {
			// Save Levels
			for (const lvl of levels) {
				await fetch(`/api/ops/venues/manifests/${manifestIdVal}/levels`, {
					method: 'PUT',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify(lvl)
				});
			}

			// 3. Save Sections
			for (const sec of sections) {
				await fetch(`/api/ops/venues/manifests/${manifestIdVal}/sections`, {
					method: 'PUT',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify(sec)
				});
			}

			// 4. Save Price Levels
			for (const pl of priceLevels) {
				await fetch(`/api/ops/venues/manifests/${manifestIdVal}/price-levels`, {
					method: 'PUT',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify(pl)
				});
			}

			// 5. Save GA Areas
			for (const ga of gaAreas) {
				await fetch(`/api/ops/venues/manifests/${manifestIdVal}/ga-areas`, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({
						id: ga.id,
						levelId: ga.levelId,
						sectionId: ga.sectionId,
						priceLevelId: ga.priceLevelId,
						capacity: Number(ga.capacity)
					})
				});
			}

			// 6. Save RS Areas, Rows, and Seats
			for (const area of rsAreas) {
				await fetch(`/api/ops/venues/manifests/${manifestIdVal}/rs-areas`, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({
						id: area.id,
						levelId: area.levelId,
						sectionId: area.sectionId,
						priceLevelId: area.priceLevelId
					})
				});

				const rows = area.rows || [];
				for (const row of rows) {
					await fetch(`/api/ops/venues/rs-areas/${area.id}/rows`, {
						method: 'POST',
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify({
							id: row.id,
							name: row.name,
							positionY: row.positionY
						})
					});

					const seats = row.seats || [];
					for (const seat of seats) {
						await fetch(`/api/ops/venues/rows/${row.id}/seats`, {
							method: 'POST',
							headers: { 'Content-Type': 'application/json' },
							body: JSON.stringify({
								id: seat.id,
								name: seat.name,
								positionX: seat.positionX,
								accessibility: seat.accessibility,
								obstructedView: seat.obstructedView,
								aisle: seat.aisle
							})
						});
					}
				}
			}

			saveMessage = 'Seating chart manifest layout saved successfully!';
			selectedSeatIds = [];
			drawSeatingMap();

			if (isNew) {
				setTimeout(() => {
					goto(`/ops/venues/${venue.id}/manifests/${manifestIdVal}`, { invalidateAll: true });
				}, 1000);
			}
		} catch (err: any) {
			errorMessage = err.message || 'Error occurred during bulk layout saving.';
		} finally {
			isSaving = false;
		}
	}
</script>

<div class="flex h-screen w-screen flex-col overflow-hidden bg-slate-50 font-sans select-none">
	<!-- Zone 1: Top Navigation Bar -->
	<header
		class="z-50 flex h-16 shrink-0 items-center justify-between border-b border-slate-200 bg-white px-6"
	>
		<!-- Left: Venue details & link back -->
		<div class="flex items-center gap-4">
			<a
				href="/ops/venues/{venue?.id}/manifests"
				class="text-slate-400 transition-colors hover:text-slate-800"
				aria-label="Back to manifests"
			>
				<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2.5"
						d="M10 19l-7-7m0 0l7-7m-7 7h18"
					/>
				</svg>
			</a>
			<div>
				<div class="text-[9px] leading-none font-black tracking-widest text-slate-400 uppercase">
					Interactive Builder
				</div>
				<div class="mt-1 flex items-center gap-2">
					<span class="rounded bg-slate-100 px-1.5 py-0.5 text-xs font-black text-slate-700"
						>{venue?.name || 'Venue'}</span
					>
					<span class="text-slate-300">|</span>
					<input
						type="text"
						bind:value={manifestDescVal}
						placeholder="Manifest description"
						class="w-44 border-b border-transparent bg-transparent px-1 text-xs font-bold text-slate-800 transition outline-none hover:border-slate-300 hover:bg-slate-50 focus:border-slate-500 focus:bg-white"
					/>
					<span class="text-slate-300">|</span>
					<span class="text-[9px] font-bold tracking-wider text-slate-400 uppercase">Code:</span>
					<input
						type="text"
						bind:value={manifestIdVal}
						placeholder="Manifest Code"
						class="w-20 border-b border-transparent bg-transparent px-1 font-mono text-xs font-bold text-slate-600 transition outline-none hover:border-slate-300 hover:bg-slate-50 focus:border-slate-500 focus:bg-white"
					/>
					<span class="text-slate-300">|</span>
					<span class="text-[9px] font-bold tracking-wider text-slate-400 uppercase"
						>Max Capacity:</span
					>
					<input
						type="number"
						bind:value={manifestCapVal}
						placeholder="Capacity"
						class="w-16 border-b border-transparent bg-transparent px-1 text-xs font-bold text-slate-600 transition outline-none hover:border-slate-300 hover:bg-slate-50 focus:border-slate-500 focus:bg-white"
					/>
				</div>
			</div>
		</div>

		<!-- Middle: Mode Switcher -->
		<div class="flex rounded-xl border border-slate-200/50 bg-slate-100 p-0.5 shadow-inner">
			{#each ['scaling', 'inventory', 'floor-edit'] as mode}
				<button
					onclick={() => {
						activeMode = mode as any;
						selectedSeatIds = [];
					}}
					class="rounded-lg px-4 py-1.5 text-xs font-bold capitalize transition-all select-none {activeMode ===
					mode
						? 'bg-white text-slate-900 shadow-xs'
						: 'text-slate-500 hover:text-slate-800'}"
				>
					{#if mode === 'scaling'}
						Scaling
					{:else}
						{mode === 'floor-edit' ? 'Floor Edit' : mode}
					{/if}
				</button>
			{/each}
		</div>

		<!-- Right: Actions & Export Dropdown -->
		<div class="flex items-center gap-3">
			<div class="relative">
				<button
					onclick={() => (showExportDropdown = !showExportDropdown)}
					class="flex items-center gap-2 rounded-lg bg-slate-900 px-4 py-2 text-xs font-bold text-white shadow-xs transition hover:bg-slate-800 active:scale-95"
				>
					<span>Export</span>
					<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2.5"
							d="M19 9l-7 7-7-7"
						/>
					</svg>
				</button>
				{#if showExportDropdown}
					<!-- Dropdown Menu -->
					<div
						class="animate-in fade-in slide-in-from-top-2 absolute right-0 z-60 mt-2 w-48 rounded-lg border border-slate-200 bg-white py-1 shadow-xl duration-150"
					>
						<button
							onclick={() => {
								exportLayout('json');
								showExportDropdown = false;
							}}
							class="w-full px-4 py-2.5 text-left text-xs font-bold text-slate-700 transition-colors hover:bg-slate-50"
							>Export Layout (JSON)</button
						>
						<button
							onclick={() => {
								exportLayout('csv');
								showExportDropdown = false;
							}}
							class="w-full px-4 py-2.5 text-left text-xs font-bold text-slate-700 transition-colors hover:bg-slate-50"
							>Export Seats (CSV)</button
						>
						<button
							onclick={() => {
								exportLayout('svg');
								showExportDropdown = false;
							}}
							class="w-full px-4 py-2.5 text-left text-xs font-bold text-slate-700 transition-colors hover:bg-slate-50"
							>Export Blueprint (SVG)</button
						>
					</div>
				{/if}
			</div>
		</div>
	</header>

	<div class="relative flex min-h-0 flex-1">
		<!-- Zone 2: Left Control Panel (20% width) -->
		<aside
			class="z-40 flex w-[20%] min-w-[280px] flex-col border-r border-slate-200 bg-white select-none"
		>
			<!-- Price Levels/Inventory/Nudges depending on active Mode -->
				{#if activeMode === 'scaling'}
					<div
						class="flex shrink-0 border-b border-slate-200 text-[10px] font-black tracking-wider text-slate-400 uppercase select-none"
					>
						<button
							type="button"
							onclick={() => (activeTab = 'levels')}
							class="flex-1 border-b-2 py-3 text-center transition-all {activeTab === 'levels'
								? 'border-slate-900 font-extrabold text-slate-900'
								: 'border-transparent text-slate-400 hover:bg-slate-50 hover:text-slate-600'}"
						>
							Levels
						</button>
						<button
							type="button"
							onclick={() => (activeTab = 'sections')}
							class="flex-1 border-b-2 py-3 text-center transition-all {activeTab === 'sections'
								? 'border-slate-900 font-extrabold text-slate-900'
								: 'border-transparent text-slate-400 hover:bg-slate-50 hover:text-slate-600'}"
						>
							Sections
						</button>
						<button
							type="button"
							onclick={() => (activeTab = 'areas')}
							class="flex-1 border-b-2 py-3 text-center transition-all {activeTab === 'areas'
								? 'border-slate-900 font-extrabold text-slate-900'
								: 'border-transparent text-slate-400 hover:bg-slate-50 hover:text-slate-600'}"
						>
							Areas
						</button>
					</div>

					<div class="flex min-h-0 flex-1 flex-col overflow-y-auto">
						<!-- Config Views (levels, sections, areas) -->
						<div class="space-y-4 p-4">
							{#if activeTab === 'levels'}
								<div class="space-y-4">
									<h3 class="text-xs font-bold text-slate-800">Manage Levels</h3>
									<div class="space-y-2">
										{#each levels as lvl}
											<div
												class="flex items-center justify-between rounded-lg border border-slate-100 bg-slate-50/50 p-2 text-xs font-bold"
											>
												<span class="flex items-center gap-2">
													<span
														class="h-3 w-3 rounded-full border border-white shadow-2xs"
														style="background-color: {lvl.color}"
													></span>
													{lvl.description}
												</span>
												<span class="font-mono text-[9px] text-[#71717A] uppercase">{lvl.id}</span>
											</div>
										{/each}
									</div>

									<form
										onsubmit={(e) => {
											e.preventDefault();
											createLookup('level');
										}}
										class="space-y-3 border-t border-slate-100 pt-4 font-semibold text-slate-700"
									>
										<h4 class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">
											Add New Level
										</h4>
										<div class="space-y-1">
											<label for="level-code-input" class="text-[10px] text-slate-500"
												>Level Code / ID</label
											>
											<input
												id="level-code-input"
												type="text"
												bind:value={levelCode}
												placeholder="e.g. FLOOR, BALCONY"
												class="w-full rounded-md border border-slate-200 px-3 py-1.5 text-xs outline-none"
												required
											/>
										</div>
										<div class="space-y-1">
											<label for="level-color-input" class="text-[10px] text-slate-500"
												>Color Tag</label
											>
											<div class="flex items-center gap-2">
												<input
													id="level-color-input"
													type="color"
													bind:value={levelColor}
													class="h-8 w-8 cursor-pointer rounded border border-slate-200"
												/>
												<span class="font-mono text-xs font-bold text-slate-500">{levelColor}</span>
											</div>
										</div>
										<button
											type="submit"
											class="w-full rounded-md bg-slate-900 py-1.5 text-xs font-bold text-white transition hover:bg-black"
										>
											Add Level
										</button>
									</form>
								</div>
							{:else if activeTab === 'sections'}
								<div class="space-y-4">
									<h3 class="text-xs font-bold text-slate-800">Manage Sections</h3>
									<div class="space-y-2">
										{#each sections as sec}
											<div
												class="flex items-center justify-between rounded-lg border border-slate-100 bg-slate-50/50 p-2 text-xs font-bold"
											>
												<span class="flex items-center gap-2">
													<span
														class="h-3 w-3 rounded-full border border-white shadow-2xs"
														style="background-color: {sec.color}"
													></span>
													Section {sec.id}
												</span>
											</div>
										{/each}
									</div>

									<form
										onsubmit={(e) => {
											e.preventDefault();
											createLookup('section');
										}}
										class="space-y-3 border-t border-slate-100 pt-4 font-semibold text-slate-700"
									>
										<h4 class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">
											Add New Section
										</h4>
										<div class="space-y-1">
											<label for="section-code-input" class="text-[10px] text-slate-500"
												>Section Code</label
											>
											<input
												id="section-code-input"
												type="text"
												bind:value={sectionCode}
												placeholder="e.g. SEC-A, VIP-1"
												class="w-full rounded-md border border-slate-200 px-3 py-1.5 text-xs outline-none"
												required
											/>
										</div>
										<div class="space-y-1">
											<label for="section-color-input" class="text-[10px] text-slate-500"
												>Color Tag</label
											>
											<div class="flex items-center gap-2">
												<input
													id="section-color-input"
													type="color"
													bind:value={sectionColor}
													class="h-8 w-8 cursor-pointer rounded border border-slate-200"
												/>
												<span class="font-mono text-xs font-bold text-slate-500"
													>{sectionColor}</span
												>
											</div>
										</div>
										<button
											type="submit"
											class="w-full rounded-md bg-slate-900 py-1.5 text-xs font-bold text-white transition hover:bg-black"
										>
											Add Section
										</button>
									</form>
								</div>
							{:else if activeTab === 'areas'}
								<div class="space-y-4">
									<h3 class="text-xs font-bold text-slate-800">Seating & GA Areas</h3>

									{#if selectedRsAreaId}
										{@const targetArea = rsAreas.find((a) => a.id === selectedRsAreaId)}
										{#if targetArea}
											<div class="space-y-3 rounded-lg border border-slate-200 bg-slate-50/50 p-3">
												<div class="flex items-center justify-between">
													<span class="text-xs font-extrabold text-slate-900">
														Editing Area: {targetArea.id}
													</span>
													<button
														type="button"
														onclick={() => (selectedRsAreaId = '')}
														class="text-[10px] font-bold text-slate-400 hover:text-slate-600"
													>
														Back
													</button>
												</div>
												<div class="space-y-0.5 text-[10px] font-semibold text-slate-500">
													<p>Level: {targetArea.levelId}</p>
													<p>Section: {targetArea.sectionId}</p>
													<p>Current Rows: {targetArea.rows?.length || 0}</p>
												</div>
											</div>

											<form
												onsubmit={(e) => {
													e.preventDefault();
													generateSeatingBlock();
												}}
												class="space-y-3 border-t border-slate-100 pt-3 font-semibold text-slate-700"
											>
												<h4 class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">
													Generate Seating Block
												</h4>
												<div class="grid grid-cols-2 gap-2">
													<div class="space-y-1">
														<label for="block-row-prefix" class="text-[10px] text-slate-500"
															>Row Prefix</label
														>
														<input
															id="block-row-prefix"
															type="text"
															bind:value={blockRowPrefix}
															placeholder="A"
															class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none"
															required
														/>
													</div>
													<div class="space-y-1">
														<label for="block-rows-count" class="text-[10px] text-slate-500"
															>Number of Rows</label
														>
														<input
															id="block-rows-count"
															type="number"
															bind:value={blockRowsCount}
															min="1"
															max="26"
															class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none"
															required
														/>
													</div>
												</div>
												<div class="grid grid-cols-2 gap-2">
													<div class="space-y-1">
														<label for="block-seats-per-row" class="text-[10px] text-slate-500"
															>Seats Per Row</label
														>
														<input
															id="block-seats-per-row"
															type="number"
															bind:value={blockSeatsPerRow}
															min="1"
															class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none"
															required
														/>
													</div>
													<div class="space-y-1">
														<label for="block-seat-start-num" class="text-[10px] text-slate-500"
															>Seat Start Num</label
														>
														<input
															id="block-seat-start-num"
															type="number"
															bind:value={blockSeatStartNum}
															min="1"
															class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none"
															required
														/>
													</div>
												</div>
												<div class="space-y-1">
													<label for="block-shape" class="text-[10px] text-slate-500"
														>Layout Shape</label
													>
													<select
														id="block-shape"
														bind:value={blockShape}
														class="w-full rounded-md border border-slate-200 bg-white px-2 py-1.5 text-xs font-semibold text-slate-700 outline-none"
														required
													>
														<option value="rectangle">Rectangle (Chữ nhật)</option>
														<option value="trapezoid">Trapezoid (Hình thang / Vát chéo)</option>
														<option value="diamond">Diamond (Hình thoi)</option>
														<option value="staggered">Staggered (Xếp so le)</option>
													</select>
												</div>
												<button
													type="submit"
													class="w-full rounded-md bg-slate-900 py-1.5 text-xs font-bold text-white transition hover:bg-black"
												>
													Generate Block
												</button>
											</form>

											<!-- Curvature Slider -->
											<div class="space-y-2 border-t border-slate-100 pt-3">
												<h4 class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">
													Curvature / Bend (Độ cong)
												</h4>
												<div class="flex items-center gap-3">
													<input
														type="range"
														min="-200"
														max="200"
														value={targetArea.curvature || 0}
														oninput={(e) => {
															const val = Number((e.target as HTMLInputElement).value);
															applyCurvature(targetArea, val);
														}}
														class="flex-1 cursor-pointer accent-slate-900"
													/>
													<span
														class="w-10 text-right text-[11px] font-bold text-slate-600 tabular-nums"
														>{targetArea.curvature || 0}</span
													>
												</div>
												<div class="flex justify-between text-[9px] font-semibold text-slate-400">
													<span>Curve Down (Cong xuống)</span>
													<span>Curve Up (Cong lên)</span>
												</div>
											</div>
										{/if}
									{:else}
										{#if gaAreas.length > 0 || rsAreas.length > 0}
											<div class="space-y-2">
												{#each gaAreas as ga}
													<div
														class="flex items-center justify-between rounded-lg border border-emerald-100 bg-emerald-50/20 p-2.5 text-[10px] font-bold text-emerald-800"
													>
														<span>GA Area: {ga.id} (Cap: {ga.capacity})</span>
													</div>
												{/each}
												{#each rsAreas as rs}
													<button
														onclick={() => (selectedRsAreaId = rs.id)}
														class="block w-full rounded-lg border p-2.5 text-left text-[10px] font-bold transition select-none {selectedRsAreaId ===
														rs.id
															? 'border-slate-950 bg-slate-50 text-slate-950 shadow-2xs'
															: 'border-slate-100 bg-slate-50/50 text-slate-500'}"
													>
														<span>Reserved Area: {rs.id}</span>
														<span
															class="mt-1 block font-mono text-[9px] leading-none font-semibold text-slate-400"
														>
															Level: {rs.levelId} · Section: {rs.sectionId} · Rows: {rs.rows
																?.length || 0}
														</span>
													</button>
												{/each}
											</div>
										{/if}

										<form
											onsubmit={(e) => {
												e.preventDefault();
												createArea();
											}}
											class="space-y-3 border-t border-slate-100 pt-4 font-semibold text-slate-700"
										>
											<h4 class="text-[10px] font-bold tracking-wider text-slate-400 uppercase">
												Create Seating Area
											</h4>
											<div class="flex gap-2">
												<button
													type="button"
													onclick={() => (areaType = 'RS')}
													class="flex-1 rounded border py-1 text-center text-xs font-bold {areaType ===
													'RS'
														? 'border-slate-900 bg-slate-50 text-slate-900'
														: 'border-slate-200 text-slate-400'}"
												>
													Reserved
												</button>
												<button
													type="button"
													onclick={() => (areaType = 'GA')}
													class="flex-1 rounded border py-1 text-center text-xs font-bold {areaType ===
													'GA'
														? 'border-slate-900 bg-slate-50 text-slate-900'
														: 'border-slate-200 text-slate-400'}"
												>
													Standing
												</button>
											</div>

											<div class="space-y-1">
												<label for="area-level-select" class="text-[10px] text-slate-500"
													>Level Link</label
												>
												<select
													id="area-level-select"
													bind:value={areaLevelId}
													class="w-full rounded-md border border-slate-200 bg-white px-2 py-1.5 text-xs font-semibold outline-none"
													required
												>
													<option value="">-- Choose Level --</option>
													{#each levels as lvl}
														<option value={lvl.id}>{lvl.description} ({lvl.id})</option>
													{/each}
												</select>
											</div>

											<div class="space-y-1">
												<label for="area-section-select" class="text-[10px] text-slate-500"
													>Section Link</label
												>
												<select
													id="area-section-select"
													bind:value={areaSectionId}
													class="w-full rounded-md border border-slate-200 bg-white px-2 py-1.5 text-xs font-semibold outline-none"
													required
												>
													<option value="">-- Choose Section --</option>
													{#each sections as sec}
														<option value={sec.id}>Section {sec.id}</option>
													{/each}
												</select>
											</div>

											{#if areaType === 'GA'}
												<div class="space-y-1">
													<label for="area-capacity-input" class="text-[10px] text-slate-500"
														>Standing Capacity</label
													>
													<input
														id="area-capacity-input"
														type="number"
														bind:value={areaCapacity}
														min="1"
														class="w-full rounded-md border border-slate-200 bg-white px-3 py-1.5 text-xs outline-none"
														required
													/>
												</div>
											{:else}
												<div class="grid grid-cols-2 gap-2">
													<div class="space-y-1">
														<label for="rs-block-rows" class="text-[10px] text-slate-500"
															>Number of Rows</label
														>
														<input
															id="rs-block-rows"
															type="number"
															bind:value={blockRowsCount}
															min="1"
															max="26"
															class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none"
															required
														/>
													</div>
													<div class="space-y-1">
														<label for="rs-block-seats" class="text-[10px] text-slate-500"
															>Seats Per Row</label
														>
														<input
															id="rs-block-seats"
															type="number"
															bind:value={blockSeatsPerRow}
															min="1"
															class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none"
															required
														/>
													</div>
												</div>
												<div class="grid grid-cols-2 gap-2">
													<div class="space-y-1">
														<label for="rs-block-prefix" class="text-[10px] text-slate-500"
															>Row Prefix</label
														>
														<input
															id="rs-block-prefix"
															type="text"
															bind:value={blockRowPrefix}
															placeholder="A"
															class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none"
															required
														/>
													</div>
													<div class="space-y-1">
														<label for="rs-block-start-num" class="text-[10px] text-slate-500"
															>Seat Start Num</label
														>
														<input
															id="rs-block-start-num"
															type="number"
															bind:value={blockSeatStartNum}
															min="1"
															class="w-full rounded-md border border-slate-200 bg-white px-2.5 py-1.5 text-xs outline-none"
															required
														/>
													</div>
												</div>
												<div class="space-y-1">
													<label for="rs-block-shape" class="text-[10px] text-slate-500"
														>Layout Shape</label
													>
													<select
														id="rs-block-shape"
														bind:value={blockShape}
														class="w-full rounded-md border border-slate-200 bg-white px-2 py-1.5 text-xs font-semibold text-slate-700 outline-none"
														required
													>
														<option value="rectangle">Rectangle (Chữ nhật)</option>
														<option value="trapezoid">Trapezoid (Hình thang)</option>
														<option value="diamond">Diamond (Hình thoi)</option>
														<option value="staggered">Staggered (Xếp so le)</option>
													</select>
												</div>
											{/if}

											<button
												type="submit"
												class="w-full rounded-md bg-slate-900 py-1.5 text-xs font-bold text-white transition hover:bg-black"
											>
												Create Area
											</button>
										</form>
									{/if}
								</div>
							{/if}
						</div>
					</div>
				{:else}
					<!-- Mode is either 'inventory' or 'floor-edit' -->
					<div class="flex-1 space-y-4 overflow-y-auto p-4">
						{#if activeMode === 'inventory'}
							<div class="space-y-3">
								<h3 class="text-[10px] font-black tracking-widest text-slate-400 uppercase">
									Inventory Management
								</h3>
								<div
									class="space-y-2 rounded-xl border border-slate-100 bg-slate-50 p-3 text-xs font-semibold text-slate-600"
								>
									<p class="text-[10px] leading-relaxed text-slate-500">
										Toggle the status of selected seats to release them for public booking or hold
										them.
									</p>
									<div class="flex gap-2 pt-2">
										<button
											onclick={() => setInventoryStatus('AVAILABLE')}
											disabled={selectedSeatIds.length === 0}
											class="flex-1 rounded-lg bg-emerald-600 py-2 text-center text-[10px] font-bold text-white transition hover:bg-emerald-700 disabled:opacity-50"
										>
											Unlock / Open
										</button>
										<button
											onclick={() => setInventoryStatus('UNAVAILABLE')}
											disabled={selectedSeatIds.length === 0}
											class="flex-1 rounded-lg bg-rose-600 py-2 text-center text-[10px] font-bold text-white transition hover:bg-rose-700 disabled:opacity-50"
										>
											Lock / Hold
										</button>
									</div>
								</div>
							</div>
						{:else}
							<div class="space-y-4">
								<h3 class="text-[10px] font-black tracking-widest text-slate-400 uppercase">
									Physical Positioning
								</h3>
								<div
									class="space-y-3 rounded-xl border border-slate-100 bg-slate-50 p-3 text-xs font-semibold text-slate-600"
								>
									<p class="text-[10px] leading-relaxed text-slate-500">
										Seats can be dragged and repositioned directly on the map. Snapping aligns
										locations.
									</p>
									<div class="space-y-1">
										<label
											for="grid-snap"
											class="text-[9px] font-bold tracking-wider text-slate-400 uppercase"
											>Grid Alignment</label
										>
										<select
											id="grid-snap"
											bind:value={snapGrid}
											class="w-full rounded-md border border-slate-200 bg-white px-2 py-1.5 text-xs outline-none"
										>
											<option value={1}>Free Movement (No Snap)</option>
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

				<!-- Lower block: Thống kê tài chính (Financial Info) -->
				<div class="border-t border-slate-200 bg-slate-50 p-4">
					<h3 class="mb-3.5 text-[10px] font-black tracking-widest text-slate-400 uppercase">
						Stadium Metrics
					</h3>
					<div class="space-y-2.5 text-xs font-semibold text-slate-600">
						<div
							class="flex items-center justify-between rounded-lg border border-slate-200/60 bg-white p-2.5 shadow-2xs"
						>
							<span class="text-slate-400">Total Capacity</span>
							<span class="font-mono text-sm font-extrabold text-slate-800"
								>{totalCapacity.toLocaleString()}</span
							>
						</div>
					</div>
				</div>
		</aside>

		<!-- Zone 3: Canvas Working Area -->
		<main class="relative flex flex-1 flex-col overflow-hidden bg-[#FAFAFA]">
			<!-- Canvas Toolbar -->
			<div
				class="z-30 flex h-12 items-center justify-between border-b border-slate-200 bg-white px-6"
			>
				<div class="flex items-center gap-3">
					<div class="flex items-center rounded-lg border border-slate-200 bg-slate-50 p-0.5">
						<button
							onclick={() => (activeTool = 'select')}
							class="flex items-center gap-1 rounded-md px-3 py-1 text-xs font-bold transition-all {activeTool ===
							'select'
								? 'bg-white text-slate-900 shadow-sm'
								: 'text-slate-500 hover:text-slate-800'}"
						>
							<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5"
								/></svg
							>
							<span>Select</span>
							<span class="ml-0.5 rounded bg-slate-200 px-1 text-[9px] text-slate-500">V</span>
						</button>
						<button
							onclick={() => (activeTool = 'pan')}
							class="flex items-center gap-1 rounded-md px-3 py-1 text-xs font-bold transition-all {activeTool ===
							'pan'
								? 'bg-white text-slate-900 shadow-sm'
								: 'text-slate-500 hover:text-slate-800'}"
						>
							<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M7 11.5V14m0-2.5v-6a1.5 1.5 0 113 0V12m-3 .5a1.5 1.5 0 003 0M10 12V3.5a1.5 1.5 0 113 0V12m-3 0a1.5 1.5 0 003 0m0 0V5.5a1.5 1.5 0 113 0V12m-3 0a1.5 1.5 0 003 0m0 0V7.5a1.5 1.5 0 113 0V12m-3 0a1.5 1.5 0 003 0M4 11.5v3.935a8 8 0 001.657 4.884l.885 1.18a3 3 0 002.261 1.05h6.223a3 3 0 002.014-.775l.432-.387a6 6 0 001.396-4.577V11.5a1.5 1.5 0 10-3 0V12a.5.5 0 01-1 0v-7.5a1.5 1.5 0 10-3 0V12a.5.5 0 01-1 0v-8a1.5 1.5 0 10-3 0V12a.5.5 0 01-1 0V5.5a1.5 1.5 0 10-3 0V12a.5.5 0 01-1 0V11.5z"
								/></svg
							>
							<span>Pan</span>
							<span class="ml-0.5 rounded bg-slate-200 px-1 text-[9px] text-slate-500">Space</span>
						</button>
						<div class="mx-0.5 h-4 w-px bg-slate-200"></div>
						<button
							onclick={() => (activeTool = 'brush')}
							class="flex items-center gap-1 rounded-md px-3 py-1 text-xs font-bold transition-all {activeTool ===
							'brush'
								? 'bg-white text-slate-900 shadow-sm'
								: 'text-slate-500 hover:text-slate-800'}"
						>
							<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M7 21a4 4 0 01-4-4c0-3 3-5 7-9 4 4 7 6 7 9a4 4 0 01-4 4H7z"
								/></svg
							>
							<span>Brush</span>
							<span class="ml-0.5 rounded bg-slate-200 px-1 text-[9px] text-slate-500">B</span>
						</button>
						<button
							onclick={() => (activeTool = 'eraser')}
							class="flex items-center gap-1 rounded-md px-3 py-1 text-xs font-bold transition-all {activeTool ===
							'eraser'
								? 'bg-white text-slate-900 shadow-sm'
								: 'text-slate-500 hover:text-slate-800'}"
						>
							<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M21 21l-6-6m2-5a9 9 0 11-18 0 9 9 0 0118 0z"
								/></svg
							>
							<span>Eraser</span>
							<span class="ml-0.5 rounded bg-slate-200 px-1 text-[9px] text-slate-500">E</span>
						</button>
					</div>

					{#if activeTool === 'select'}
						<button
							onclick={() => {
								selectedRsAreaId = '';
								activeTab = 'areas';
								// trigger layout modal
							}}
							class="ml-2 flex items-center gap-1 text-xs font-bold text-blue-600 transition hover:text-blue-800"
						>
							<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M12 4v16m8-8H4"
								/></svg
							>
							Add Area
						</button>
					{/if}

					{#if activeTool === 'brush'}
						<div class="mx-2 flex items-center gap-2">
							<span class="text-[10px] font-bold text-slate-400">Paint:</span>
							<select
								bind:value={brushPriceLevelId}
								class="rounded-md border border-slate-200 bg-white px-2 py-1 text-[10px] font-semibold outline-none"
							>
								{#each priceLevels as pl}
									<option value={pl.id}>{pl.description}</option>
								{/each}
							</select>
						</div>
					{/if}

					<!-- Attribute shortcuts for selections -->
					{#if selectedSeatIds.length > 0}
						<div class="mx-2 h-5 w-px bg-slate-200"></div>
						<div class="flex items-center gap-1.5">
							<button
								onclick={() => toggleSelectedAttribute('accessibility')}
								class="rounded-md border border-slate-200 bg-white px-2 py-1 text-[10px] font-bold text-slate-700 hover:bg-slate-50"
								>♿ Accessible</button
							>
							<button
								onclick={() => toggleSelectedAttribute('obstructedView')}
								class="rounded-md border border-slate-200 bg-white px-2 py-1 text-[10px] font-bold text-slate-700 hover:bg-slate-50"
								>⚠️ Obstructed</button
							>
							<button
								onclick={() => toggleSelectedAttribute('aisle')}
								class="rounded-md border border-slate-200 bg-white px-2 py-1 text-[10px] font-bold text-slate-700 hover:bg-slate-50"
								>↔ Aisle</button
							>
							<span class="ml-1 text-[10px] font-bold text-slate-400"
								>({selectedSeatIds.length} selected)</span
							>
							<span class="ml-1 rounded bg-slate-100 px-1 text-[9px] text-slate-400"
								>Del to remove</span
							>
						</div>
					{/if}
				</div>

				<div class="flex items-center gap-3">
					{#if saveMessage}
						<span class="animate-pulse text-xs font-semibold text-emerald-600">{saveMessage}</span>
					{/if}
					{#if errorMessage}
						<span class="animate-pulse text-xs font-semibold text-rose-600">{errorMessage}</span>
					{/if}
					<button
						onclick={saveLayout}
						disabled={isSaving}
						class="rounded-lg border border-slate-900 bg-slate-900 px-4 py-1.5 text-xs font-extrabold text-white shadow-xs hover:bg-black active:scale-95 disabled:opacity-50"
					>
						{isSaving ? 'Saving...' : 'Save Layout'}
					</button>
				</div>
			</div>

			<!-- SVG Working Canvas -->
			<div
				id="stage-container"
				bind:this={stageContainer}
				role="application"
				aria-label="Seatmap working area"
				class="relative w-full flex-1 overflow-hidden bg-[#FAFAFA]"
				ondragover={(e) => e.preventDefault()}
				ondrop={(e) => {
					e.preventDefault();
					const plId = e.dataTransfer?.getData('text/plain');
					if (plId) {
						assignPriceLevelToSelected(plId);
					}
				}}
			></div>

			<!-- Floating Tool Panel (Top Right overlay) -->
			<div
				class="absolute top-6 right-6 z-40 flex w-14 flex-col items-center gap-2.5 rounded-2xl border border-slate-200/80 bg-white/90 p-1.5 shadow-lg backdrop-blur-md"
			>
				<!-- Seat Selection -->
				<button
					onclick={() => (selectionTool = 'seat')}
					class="group relative rounded-xl p-2.5 transition-all {selectionTool === 'seat'
						? 'bg-slate-900 text-white shadow-md'
						: 'text-slate-500 hover:bg-slate-100 hover:text-slate-900'}"
				>
					<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<circle cx="12" cy="12" r="8" stroke-width="2.5" />
					</svg>
					<div
						class="pointer-events-none absolute top-1/2 right-16 z-50 -translate-y-1/2 rounded bg-slate-900 px-2 py-1 text-[10px] font-bold whitespace-nowrap text-white opacity-0 shadow-md transition group-hover:opacity-100"
					>
						Seat Select
					</div>
				</button>

				<!-- Row Selection -->
				<button
					onclick={() => (selectionTool = 'row')}
					class="group relative rounded-xl p-2.5 transition-all {selectionTool === 'row'
						? 'bg-slate-900 text-white shadow-md'
						: 'text-slate-500 hover:bg-slate-100 hover:text-slate-900'}"
				>
					<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2.5"
							d="M4 6h16M4 12h16M4 18h16"
						/>
					</svg>
					<div
						class="pointer-events-none absolute top-1/2 right-16 z-50 -translate-y-1/2 rounded bg-slate-900 px-2 py-1 text-[10px] font-bold whitespace-nowrap text-white opacity-0 shadow-md transition group-hover:opacity-100"
					>
						Row Select (Shift+Drag)
					</div>
				</button>

				<!-- Section Selection -->
				<button
					onclick={() => (selectionTool = 'section')}
					class="group relative rounded-xl p-2.5 transition-all {selectionTool === 'section'
						? 'bg-slate-900 text-white shadow-md'
						: 'text-slate-500 hover:bg-slate-100 hover:text-slate-900'}"
				>
					<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"
						/>
					</svg>
					<div
						class="pointer-events-none absolute top-1/2 right-16 z-50 -translate-y-1/2 rounded bg-slate-900 px-2 py-1 text-[10px] font-bold whitespace-nowrap text-white opacity-0 shadow-md transition group-hover:opacity-100"
					>
						Section Select
					</div>
				</button>

				<div class="h-px w-8 bg-slate-200"></div>

				<!-- Filter Trigger -->
				<button
					onclick={() => (showFilterDialog = !showFilterDialog)}
					class="group relative rounded-xl p-2.5 text-slate-500 transition-all hover:bg-slate-100 hover:text-slate-950"
				>
					<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2.5"
							d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
						/>
					</svg>
					<div
						class="pointer-events-none absolute top-1/2 right-16 z-50 -translate-y-1/2 rounded bg-slate-900 px-2 py-1 text-[10px] font-bold whitespace-nowrap text-white opacity-0 shadow-md transition group-hover:opacity-100"
					>
						Filters
					</div>
				</button>

				<!-- Assistant Trigger -->
				<button
					onclick={() => (showAssistantDialog = !showAssistantDialog)}
					class="group relative rounded-xl p-2.5 text-slate-500 transition-all hover:bg-slate-100 hover:text-slate-950"
				>
					<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
						/>
					</svg>
					<div
						class="pointer-events-none absolute top-1/2 right-16 z-50 -translate-y-1/2 rounded bg-slate-900 px-2 py-1 text-[10px] font-bold whitespace-nowrap text-white opacity-0 shadow-md transition group-hover:opacity-100"
					>
						AI Assistant
					</div>
				</button>
			</div>

			<!-- Zone 3: Mini-map Navigator overlay -->
			<div
				class="absolute right-6 bottom-6 z-40 flex h-40 w-52 flex-col overflow-hidden rounded-2xl border border-slate-200/80 bg-white/90 p-2.5 shadow-lg backdrop-blur-md select-none"
			>
				<div
					class="flex items-center justify-between pb-1 text-[9px] font-bold tracking-wider text-slate-400 uppercase"
				>
					<span>Mini-map</span>
					<span class="rounded bg-slate-100 px-1 py-0.5 font-mono text-[8px] text-slate-500"
						>Zoom: {stage ? Math.round(stage.scaleX() * 100) : 100}%</span
					>
				</div>
				<div
					class="relative w-full flex-1 overflow-hidden rounded-xl border border-slate-200/60 bg-slate-50"
				>
					<button
						type="button"
						aria-label="Minimap viewport navigator"
						class="relative flex h-full w-full cursor-crosshair items-center justify-center overflow-hidden border-0 bg-transparent p-0"
						onclick={handleMinimapClick}
					>
						<svg
							viewBox="{bounds.minX} {bounds.minY} {bounds.width} {bounds.height}"
							class="pointer-events-none h-full w-full"
						>
							<!-- Tiny renders of seats -->
							{#each rsAreas as area}
								{@const pl = priceLevels.find((p) => p.id === area.priceLevelId)}
								{@const dotColor = pl ? pl.color : '#3B82F6'}
								{#each area.rows || [] as row}
									{#each row.seats || [] as seat}
										{@const isSel = selectedSeatIds.includes(seat.id)}
										<circle
											cx={seat.positionX || 0}
											cy={row.positionY || 0}
											r="2.5"
											fill={isSel
												? '#1A1A1A'
												: seat.status === 'UNAVAILABLE'
													? '#CBD5E1'
													: dotColor}
										/>
									{/each}
								{/each}
							{/each}

							<!-- Tiny renders of GA Areas -->
							{#each gaAreas as ga}
								<rect
									x={ga.x || 50}
									y={ga.y || 50}
									width="200"
									height="100"
									fill="rgba(15, 23, 42, 0.05)"
									stroke="#0F172A"
									stroke-width="2"
								/>
							{/each}

							<!-- Viewport tracking frame -->
							<rect
								x={viewportX}
								y={viewportY}
								width={viewportW}
								height={viewportH}
								fill="none"
								stroke="#EF4444"
								stroke-width="5"
								stroke-dasharray="10 8"
							/>
						</svg>
					</button>
				</div>
			</div>

			<!-- Saving indicator overlay -->
			{#if isSaving}
				<div
					class="absolute inset-0 z-60 flex flex-col items-center justify-center gap-3 bg-white/70 font-semibold text-slate-800 backdrop-blur-xs"
				>
					<svg class="h-8 w-8 animate-spin text-slate-900" fill="none" viewBox="0 0 24 24">
						<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
						></circle>
						<path
							class="opacity-75"
							fill="currentColor"
							d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
						></path>
					</svg>
					<span>Processing seating layout data operations...</span>
				</div>
			{/if}
		</main>
	</div>
</div>

<!-- ======================== FLOATING FILTERS DIALOG ======================== -->
{#if showFilterDialog}
	<div
		class="animate-in fade-in slide-in-from-right-3 fixed top-40 right-24 z-50 w-60 rounded-2xl border border-slate-200 bg-white/95 p-4 shadow-xl backdrop-blur-md duration-150 select-none"
	>
		<div class="mb-3 flex items-center justify-between border-b border-slate-100 pb-2">
			<h4 class="text-xs font-black tracking-wider text-slate-800 uppercase">Canvas Filters</h4>
			<button
				onclick={() => (showFilterDialog = false)}
				class="text-slate-400 transition hover:text-slate-700"
				aria-label="Close filters"
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
		</div>
		<div class="space-y-2.5 text-xs font-semibold text-slate-600">
			<label class="flex cursor-pointer items-center gap-2">
				<input
					type="checkbox"
					bind:checked={onlyAvailable}
					class="h-3.5 w-3.5 rounded border-slate-300 text-slate-900 focus:ring-slate-900"
				/>
				<span>Available Seats Only</span>
			</label>
			<label class="flex cursor-pointer items-center gap-2">
				<input
					type="checkbox"
					bind:checked={onlyAccessible}
					class="h-3.5 w-3.5 rounded border-slate-300 text-slate-900 focus:ring-slate-900"
				/>
				<span>♿ Accessible Only</span>
			</label>
			<label class="flex cursor-pointer items-center gap-2">
				<input
					type="checkbox"
					bind:checked={onlyObstructed}
					class="h-3.5 w-3.5 rounded border-slate-300 text-slate-900 focus:ring-slate-900"
				/>
				<span>⚠️ Obstructed Only</span>
			</label>
		</div>
	</div>
{/if}

<!-- ======================== FLOATING AI ASSISTANT DIALOG ======================== -->
{#if showAssistantDialog}
	<div
		class="animate-in fade-in slide-in-from-right-3 fixed top-56 right-24 z-50 w-72 rounded-2xl border border-slate-200 bg-white/95 p-4 shadow-xl backdrop-blur-md duration-150 select-none"
	>
		<div class="mb-3 flex items-center justify-between border-b border-slate-100 pb-2">
			<h4 class="text-xs font-black tracking-wider text-slate-800 uppercase">Seating Assistant</h4>
			<button
				onclick={() => (showAssistantDialog = false)}
				class="text-slate-400 transition hover:text-slate-700"
				aria-label="Close assistant"
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
		</div>
		<form
			onsubmit={(e) => {
				e.preventDefault();
				runAssistantPrompt();
			}}
			class="space-y-3"
		>
			<p class="text-[10px] leading-relaxed font-semibold text-slate-500">
				Enter a layout selection instruction. Examples: <code
					class="rounded bg-slate-100 px-1 text-slate-700">Select VIP</code
				>, <code class="rounded bg-slate-100 px-1 text-slate-700">Select Row A</code>.
			</p>
			<div class="flex gap-2">
				<input
					type="text"
					bind:value={assistantPrompt}
					placeholder="Prompt assistant..."
					class="flex-1 rounded-lg border border-slate-200 px-2.5 py-1.5 text-xs font-semibold outline-none focus:border-slate-400"
				/>
				<button
					type="submit"
					class="rounded-lg bg-slate-900 px-3 py-1.5 text-xs font-bold text-white transition hover:bg-black"
					>Run</button
				>
			</div>
		</form>
	</div>
{/if}

<!-- ======================== ADD SEATING BLOCK POPUP DIALOG ======================== -->
{#if activeTab === 'areas' && selectedRsAreaId === ''}
	<!-- Handled implicitly or keep popup wrapper -->
{/if}
