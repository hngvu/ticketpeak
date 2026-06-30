<script lang="ts">
	import { onMount } from 'svelte';
	import { IconClock } from '@tabler/icons-svelte';

	let {
		name,
		required = false,
		placeholder = 'Select time',
		value = $bindable('')
	}: {
		name: string;
		required?: boolean;
		placeholder?: string;
		value?: string;
	} = $props();

	let isOpen = $state(false);
	let popoverEl = $state<HTMLDivElement | null>(null);

	// Default time
	let selectedHour = $state('12');
	let selectedMinute = $state('00');

	const hours = Array.from({ length: 24 }, (_, i) => i.toString().padStart(2, '0'));
	const minutes = ['00', '15', '30', '45'];

	$effect(() => {
		if (value && value !== '') {
			const [h, m] = value.split(':');
			if (h && m) {
				selectedHour = h;
				selectedMinute = m;
			}
		}
	});

	function updateValue() {
		value = `${selectedHour}:${selectedMinute}`;
	}

	function handleSelect(h: string, m: string) {
		selectedHour = h;
		selectedMinute = m;
		updateValue();
		isOpen = false;
	}

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
	<button
		type="button"
		onclick={() => (isOpen = !isOpen)}
		class="flex w-full items-center gap-2.5 rounded-none border border-slate-300 bg-white px-3.5 py-2.5 text-left text-sm font-medium text-slate-800 transition-all hover:border-blue-500 focus:border-blue-500 focus:outline-none"
	>
		<IconClock size={18} stroke={1.8} class="shrink-0 text-slate-400" />
		{#if value}
			<span class="truncate text-slate-800">{value}</span>
		{:else}
			<span class="truncate text-slate-400">{placeholder}</span>
		{/if}
	</button>

	<input type="hidden" {name} {value} {required} />

	{#if isOpen}
		<div
			class="absolute left-0 z-50 mt-1.5 w-64 rounded-2xl border border-slate-100 bg-white p-4 shadow-xl select-none"
		>
			<div class="mb-3 text-sm font-semibold text-slate-800">Select Time</div>

			<div class="flex h-48 gap-4">
				<div class="no-scrollbar flex-1 overflow-y-auto rounded-lg border border-slate-100">
					{#each hours as h}
						<button
							type="button"
							onclick={() => {
								selectedHour = h;
								updateValue();
							}}
							class="w-full py-2 text-center text-sm font-medium transition-colors hover:bg-slate-50 {selectedHour ===
							h
								? 'bg-blue-50 font-bold text-blue-600'
								: 'text-slate-700'}"
						>
							{h}
						</button>
					{/each}
				</div>
				<div class="flex flex-col justify-center text-xl font-bold text-slate-400">:</div>
				<div class="no-scrollbar flex-1 overflow-y-auto rounded-lg border border-slate-100">
					{#each minutes as m}
						<button
							type="button"
							onclick={() => {
								selectedMinute = m;
								updateValue();
							}}
							class="w-full py-2 text-center text-sm font-medium transition-colors hover:bg-slate-50 {selectedMinute ===
							m
								? 'bg-blue-50 font-bold text-blue-600'
								: 'text-slate-700'}"
						>
							{m}
						</button>
					{/each}
				</div>
			</div>

			<div class="mt-4 flex justify-end">
				<button
					type="button"
					onclick={() => (isOpen = false)}
					class="rounded-lg bg-blue-600 px-4 py-1.5 text-xs font-bold text-white shadow-sm transition hover:bg-blue-700 active:scale-95"
				>
					Done
				</button>
			</div>
		</div>
	{/if}
</div>
