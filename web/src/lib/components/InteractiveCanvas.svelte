<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { onMount, onDestroy } from 'svelte';

	let {
		seats = $bindable([]),
		selectedSeatId = $bindable(null),
		activeTool = 'select',
		brushPriceLevelId = null,
		canvasZoom = $bindable(1),
		panX = $bindable(0),
		panY = $bindable(0)
	} = $props();

	let svgRef: SVGSVGElement | null = $state(null);
	let isPanning = $state(false);
	let isBrushing = $state(false);
	let panStartX = 0;
	let panStartY = 0;

	function getEventPoint(e: MouseEvent | TouchEvent) {
		if ('touches' in e && e.touches.length > 0) {
			return { x: e.touches[0].clientX, y: e.touches[0].clientY };
		}
		return { x: (e as MouseEvent).clientX, y: (e as MouseEvent).clientY };
	}

	function startPanning(e: MouseEvent | TouchEvent) {
		isPanning = true;
		const pt = getEventPoint(e);
		panStartX = pt.x - panX;
		panStartY = pt.y - panY;
		if (e.cancelable) e.preventDefault();
	}

	function getHoveredSeatCount(e: MouseEvent | TouchEvent): number {
		if (!svgRef) return 0;
		const pt = getEventPoint(e);
		const rect = svgRef.getBoundingClientRect();
		const canvasX = (pt.x - rect.left - panX) / canvasZoom;
		const canvasY = (pt.y - rect.top - panY) / canvasZoom;
		const brushRadius = 15;
		let count = 0;
		for (let i = 0; i < seats.length; i++) {
			const dx = seats[i].x - canvasX;
			const dy = seats[i].y - canvasY;
			if (dx * dx + dy * dy < brushRadius * brushRadius) count++;
		}
		return count;
	}

	function handlePointerDown(e: MouseEvent | TouchEvent) {
		const isMiddleClick = e.type === 'mousedown' && (e as MouseEvent).button === 1;
		if (activeTool === 'pan' || isMiddleClick) {
			startPanning(e);
		} else if (activeTool === 'brush' || activeTool === 'eraser') {
			const hits = getHoveredSeatCount(e);
			if (hits > 0) {
				isBrushing = true;
				handleBrush(e);
			} else {
				// Clicked on empty background -> allow panning
				startPanning(e);
			}
		} else {
			// select tool or other
			const hits = getHoveredSeatCount(e);
			if (hits === 0) {
				startPanning(e);
			}
		}
	}

	function handlePointerMove(e: MouseEvent | TouchEvent) {
		if (isPanning) {
			const pt = getEventPoint(e);
			panX = pt.x - panStartX;
			panY = pt.y - panStartY;
		} else if (isBrushing) {
			handleBrush(e);
		}
	}

	function handlePointerUp() {
		isPanning = false;
		isBrushing = false;
	}

	function handleWheel(e: WheelEvent) {
		e.preventDefault();
		const zoomSensitivity = 0.001;
		const delta = -e.deltaY * zoomSensitivity;
		const newZoom = Math.min(Math.max(0.1, canvasZoom * (1 + delta)), 5);

		if (svgRef) {
			const rect = svgRef.getBoundingClientRect();
			const mouseX = e.clientX - rect.left;
			const mouseY = e.clientY - rect.top;

			const zoomRatio = newZoom / canvasZoom;
			panX = mouseX - (mouseX - panX) * zoomRatio;
			panY = mouseY - (mouseY - panY) * zoomRatio;
		}

		canvasZoom = newZoom;
	}

	function handleBrush(e: MouseEvent | TouchEvent) {
		if (!svgRef) return;
		const pt = getEventPoint(e);
		const rect = svgRef.getBoundingClientRect();
		// Convert screen coordinates to canvas coordinates
		const canvasX = (pt.x - rect.left - panX) / canvasZoom;
		const canvasY = (pt.y - rect.top - panY) / canvasZoom;

		// Find seats near the brush
		const brushRadius = 15; // Hit radius
		let changed = false;
		for (let i = 0; i < seats.length; i++) {
			const seat = seats[i];
			const dx = seat.x - canvasX;
			const dy = seat.y - canvasY;
			if (dx * dx + dy * dy < brushRadius * brushRadius) {
				if (activeTool === 'brush' && seat.priceLevelId !== brushPriceLevelId) {
					seats[i].priceLevelId = brushPriceLevelId;
					changed = true;
				} else if (activeTool === 'eraser' && seat.priceLevelId) {
					seats[i].priceLevelId = undefined;
					changed = true;
				}
			}
		}
		if (changed) seats = [...seats];
	}

	export function zoomIn() {
		canvasZoom = Math.min(5, canvasZoom * 1.25);
	}
	export function zoomOut() {
		canvasZoom = Math.max(0.1, canvasZoom / 1.25);
	}
	export function fitToView() {
		canvasZoom = 1;
		panX = 0;
		panY = 0;
	}

	onMount(() => {
		if (svgRef) {
			svgRef.addEventListener('wheel', handleWheel, { passive: false });
		}
		return () => {
			if (svgRef) {
				svgRef.removeEventListener('wheel', handleWheel);
			}
		};
	});
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
	class="relative h-full w-full overflow-hidden"
	style="cursor: {activeTool === 'pan'
		? isPanning
			? 'grabbing'
			: 'grab'
		: activeTool === 'brush'
			? 'copy'
			: activeTool === 'eraser'
				? 'cell'
				: 'default'}"
	onmousedown={handlePointerDown}
	onmousemove={handlePointerMove}
	onmouseup={handlePointerUp}
	onmouseleave={handlePointerUp}
	ontouchstart={handlePointerDown}
	ontouchmove={handlePointerMove}
	ontouchend={handlePointerUp}
	ontouchcancel={handlePointerUp}
>
	<svg bind:this={svgRef} class="absolute inset-0 h-full w-full">
		<g transform="translate({panX}, {panY}) scale({canvasZoom})">
			<slot />
		</g>
	</svg>
</div>
