<script lang="ts">
	import { onMount } from 'svelte';
	import { IconChevronLeft, IconChevronRight, IconCalendarEvent } from '@tabler/icons-svelte';

	let {
		name,
		required = false,
		placeholder = 'Select date',
		value = $bindable('')
	}: {
		name: string;
		required?: boolean;
		placeholder?: string;
		value?: string;
	} = $props();

	// Open/close popover
	let isOpen = $state(false);
	let popoverEl = $state<HTMLDivElement | null>(null);

	// Date calculations state
	const today = new Date();
	let viewYear = $state(today.getFullYear());
	let viewMonth = $state(today.getMonth()); // 0-11

	// Selected Date representation
	let selectedDate = $state<Date | null>(null);

	// Month list
	const months = [
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

	const daysOfWeek = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];

	// Initialize selected values from input string if provided
	$effect(() => {
		if (value && value !== '') {
			const parsed = new Date(value);
			if (!isNaN(parsed.getTime())) {
				selectedDate = parsed;
				viewYear = parsed.getFullYear();
				viewMonth = parsed.getMonth();
			}
		}
	});

	// Get days in the viewMonth
	const daysInMonth = $derived(new Date(viewYear, viewMonth + 1, 0).getDate());
	// Get weekday of the 1st day of viewMonth
	const firstDayOffset = $derived(new Date(viewYear, viewMonth, 1).getDay());
	// Get days in previous month
	const daysInPrevMonth = $derived(new Date(viewYear, viewMonth, 0).getDate());

	// Array of all day objects to render in calendar grid
	const calendarCells = $derived.by(() => {
		const cells = [];
		// Previous month days to fill empty spots
		for (let i = firstDayOffset - 1; i >= 0; i--) {
			const dayNum = daysInPrevMonth - i;
			const month = viewMonth === 0 ? 11 : viewMonth - 1;
			const year = viewMonth === 0 ? viewYear - 1 : viewYear;
			cells.push({
				day: dayNum,
				month,
				year,
				isCurrentMonth: false,
				date: new Date(year, month, dayNum)
			});
		}
		// Current month days
		for (let i = 1; i <= daysInMonth; i++) {
			cells.push({
				day: i,
				month: viewMonth,
				year: viewYear,
				isCurrentMonth: true,
				date: new Date(viewYear, viewMonth, i)
			});
		}
		// Next month days to pad to a full grid (usually 42 cells)
		const totalCells = cells.length > 35 ? 42 : 35;
		const remaining = totalCells - cells.length;
		for (let i = 1; i <= remaining; i++) {
			const month = viewMonth === 11 ? 0 : viewMonth + 1;
			const year = viewMonth === 11 ? viewYear + 1 : viewYear;
			cells.push({
				day: i,
				month,
				year,
				isCurrentMonth: false,
				date: new Date(year, month, i)
			});
		}
		return cells;
	});

	// Display value
	const displayValue = $derived.by(() => {
		if (!selectedDate) return '';
		const d = selectedDate.getDate();
		const m = months[selectedDate.getMonth()].slice(0, 3);
		const y = selectedDate.getFullYear();
		return `${d} ${m} ${y}`;
	});

	// Update raw bound string value (format: YYYY-MM-DD)
	function updateValue() {
		if (!selectedDate) return;
		const y = selectedDate.getFullYear();
		const m = String(selectedDate.getMonth() + 1).padStart(2, '0');
		const d = String(selectedDate.getDate()).padStart(2, '0');
		value = `${y}-${m}-${d}`;
	}

	function handlePrevMonth() {
		if (viewMonth === 0) {
			viewMonth = 11;
			viewYear -= 1;
		} else {
			viewMonth -= 1;
		}
	}

	function handleNextMonth() {
		if (viewMonth === 11) {
			viewMonth = 0;
			viewYear += 1;
		} else {
			viewMonth += 1;
		}
	}

	function handleSelectCell(cell: { date: Date }) {
		selectedDate = cell.date;
		updateValue();
	}

	// Close on click outside
	onMount(() => {
		function handleClickOutside(e: MouseEvent) {
			if (popoverEl && !popoverEl.contains(e.target as Node)) {
				isOpen = false;
			}
		}
		document.addEventListener('mousedown', handleClickOutside);
		return () => {
			document.removeEventListener('mousedown', handleClickOutside);
		};
	});
</script>

<div class="relative w-full" bind:this={popoverEl}>
	<!-- Trigger Input mimicking standard custom inputs -->
	<button
		type="button"
		onclick={() => (isOpen = !isOpen)}
		class="flex w-full items-center gap-2.5 rounded-none border border-slate-300 bg-white px-3.5 py-2.5 text-left text-sm font-medium text-slate-800 transition-all hover:border-blue-500 focus:border-blue-500 focus:outline-none"
	>
		<IconCalendarEvent size={18} stroke={1.8} class="shrink-0 text-slate-400" />
		{#if displayValue}
			<span class="truncate text-slate-800">{displayValue}</span>
		{:else}
			<span class="truncate text-slate-400">{placeholder}</span>
		{/if}
	</button>

	<!-- Hidden input for standard HTML Form submission integration -->
	<input type="hidden" {name} {value} {required} />

	<!-- Date Time Picker Popover -->
	{#if isOpen}
		<div
			class="absolute left-0 z-50 mt-1.5 w-76 rounded-2xl border border-slate-100 bg-white p-4 shadow-xl select-none"
		>
			<!-- Popover Header -->
			<div class="mb-4 flex items-center justify-between">
				<span class="text-sm font-semibold text-slate-800">
					{months[viewMonth]}
					{viewYear}
				</span>
				<div class="flex items-center gap-1">
					<button
						type="button"
						onclick={handlePrevMonth}
						class="flex h-7 w-7 items-center justify-center rounded-lg text-slate-500 transition hover:bg-slate-50 hover:text-slate-800"
					>
						<IconChevronLeft size={16} stroke={2} />
					</button>
					<button
						type="button"
						onclick={handleNextMonth}
						class="flex h-7 w-7 items-center justify-center rounded-lg text-slate-500 transition hover:bg-slate-50 hover:text-slate-800"
					>
						<IconChevronRight size={16} stroke={2} />
					</button>
				</div>
			</div>

			<!-- Weekdays Heading -->
			<div class="mb-1.5 grid grid-cols-7 gap-y-1 text-center">
				{#each daysOfWeek as day (day)}
					<span class="text-[10px] font-bold text-slate-400 uppercase">{day}</span>
				{/each}
			</div>

			<!-- Days Grid -->
			<div class="grid grid-cols-7 gap-1">
				{#each calendarCells as cell, index (cell.date.getTime() + '-' + index)}
					{@const isSelected =
						selectedDate &&
						selectedDate.getDate() === cell.day &&
						selectedDate.getMonth() === cell.month &&
						selectedDate.getFullYear() === cell.year}
					{@const isToday =
						today.getDate() === cell.day &&
						today.getMonth() === cell.month &&
						today.getFullYear() === cell.year}

					<button
						type="button"
						onclick={() => handleSelectCell(cell)}
						class="flex h-8 w-8 items-center justify-center rounded-lg text-xs font-semibold transition-all
							{cell.isCurrentMonth ? 'text-slate-700' : 'text-slate-300'}
							{isSelected ? 'bg-blue-600 font-bold text-white' : 'hover:bg-slate-50'}
							{isToday && !isSelected ? 'border border-blue-200 bg-blue-50/20 text-blue-600' : ''}
						"
					>
						{cell.day}
					</button>
				{/each}
			</div>

			<!-- Popover Footer Action -->
			<div class="mt-4 flex justify-end">
				<button
					type="button"
					onclick={() => {
						if (!selectedDate) {
							selectedDate = new Date();
							updateValue();
						}
						isOpen = false;
					}}
					class="rounded-lg bg-blue-600 px-3 py-1.5 text-[11px] font-bold text-white shadow-sm transition hover:bg-blue-700 active:scale-95"
				>
					Done
				</button>
			</div>
		</div>
	{/if}
</div>

<style>
	select {
		-webkit-appearance: none !important;
		-moz-appearance: none !important;
		appearance: none !important;
		background-image: none !important;
		background-position: unset !important;
		background-size: unset !important;
		background-repeat: unset !important;
	}
	select::-ms-expand {
		display: none !important;
	}
</style>
