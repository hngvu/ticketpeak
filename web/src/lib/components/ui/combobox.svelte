<script lang="ts">
	import type { Snippet } from 'svelte'
	import { Popover, Command } from 'bits-ui'
	import { IconChevronDown, IconSearch, IconCheck } from '@tabler/icons-svelte'

	let {
		items,
		value = $bindable(''),
		values = $bindable([]),
		multiple = false,
		name = '',
		label = '',
		placeholder = 'Select...',
		searchPlaceholder = 'Search...',
		required = false,
		displayFn = (item: any) => item?.name || '',
		searchFn = (items: any[], query: string) =>
			query
				? items.filter((item: any) =>
						(item.name || '').toLowerCase().includes(query.toLowerCase())
					)
				: items,
		renderItem,
		children,
		itemSnippet
	}: {
		items: any[]
		value?: string
		values?: string[]
		multiple?: boolean
		name?: string
		label?: string
		placeholder?: string
		searchPlaceholder?: string
		required?: boolean
		displayFn?: (item: any) => string
		searchFn?: (items: any[], query: string) => any[]
		renderItem?: (item: any) => string
		children?: Snippet
		itemSnippet?: Snippet<[item: any]>
	} = $props()

	let open = $state(false)
	let searchQuery = $state('')

	const filtered = $derived(
		searchFn(items, searchQuery)
	)

	function isSelected(id: string) {
		if (multiple) return values.includes(id)
		return value === id
	}

	function toggleItem(id: string) {
		if (multiple) {
			if (values.includes(id)) {
				values = values.filter((x) => x !== id)
			} else {
				values = [...values, id]
			}
		} else {
			value = id
			open = false
		}
	}

	const selectedItem = $derived(
		multiple ? null : items.find((i: any) => i.id === value)
	)
</script>

{#if name}
	{#if multiple}
		{#each values as id (id)}
			<input type="hidden" {name} value={id} />
		{/each}
	{:else}
		<input type="hidden" {name} value={value} {required} />
	{/if}
{/if}

<Popover.Root bind:open>
	<Popover.Trigger class="flex min-h-[42px] w-full items-center justify-between gap-2 rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-left text-sm transition-all hover:border-blue-500 focus:border-blue-500 focus:outline-none data-[state=open]:border-blue-500">
		{#if label}
			<span class="sr-only">{label}</span>
		{/if}
		<div class="flex flex-wrap gap-1.5">
			{#if multiple}
				{#each values as id (id)}
					{@const item = items.find((i: any) => i.id === id)}
					{#if item}
						<span class="inline-flex items-center gap-1 rounded-md border border-slate-200 bg-slate-50 px-2 py-0.5 text-xs font-medium text-slate-700">
							{#if itemSnippet}
								{@render itemSnippet(item)}
							{:else if children}
								{@render children()}
							{:else}
								{renderItem ? renderItem(item) : displayFn(item)}
							{/if}
							<button
								type="button"
								onclick={(e) => {
									e.stopPropagation()
									values = values.filter((x) => x !== id)
								}}
								class="ml-0.5 text-slate-400 hover:text-slate-600"
							>✕</button>
						</span>
					{/if}
				{/each}
				{#if values.length === 0}
					<span class="text-slate-400">{placeholder}</span>
				{/if}
			{:else}
				{#if selectedItem}
					<span class="text-slate-700">{displayFn(selectedItem)}</span>
				{:else}
					<span class="text-slate-400">{placeholder}</span>
				{/if}
			{/if}
		</div>
		<IconChevronDown
			size={14}
			class="shrink-0 text-slate-400 transition-transform {open ? 'rotate-180' : ''}"
		/>
	</Popover.Trigger>

	<Popover.Portal>
		<Popover.Content
			side="bottom"
			sideOffset={4}
			class="z-50 w-[var(--bits-popover-anchor-width)] rounded-xl border border-slate-200 bg-white p-0 shadow-lg data-[state=open]:animate-in data-[state=closed]:animate-out"
		>
			<Command.Root shouldFilter={false}>
				<div class="flex items-center border-b border-slate-200 px-3">
					<IconSearch size={15} class="shrink-0 text-slate-400" />
					<Command.Input
						bind:value={searchQuery}
						placeholder={searchPlaceholder}
						class="flex h-10 w-full bg-transparent px-2 text-sm text-slate-800 outline-none placeholder:text-slate-400 border-none focus:border-none focus:ring-0 focus-visible:ring-0 focus-visible:outline-none"
					/>
				</div>
				<div class="overflow-hidden">
					<Command.List class="max-h-48 overflow-y-scroll p-1 pr-3 [margin-right:-12px]">
						<Command.Empty class="py-6 text-center text-sm text-slate-400">
							No results found.
						</Command.Empty>
						{#each filtered as item (item.id)}
							{@const sel = isSelected(item.id)}
							<Command.Item
								value={item.id}
								onSelect={() => toggleItem(item.id)}
								class="flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2.5 text-sm text-slate-700 data-[selected]:bg-slate-100"
							>
								{#if itemSnippet}
									{@render itemSnippet(item)}
								{:else if children}
									{@render children()}
								{:else if renderItem}
									{renderItem(item)}
								{:else}
									{displayFn(item)}
								{/if}
								{#if sel}
									<IconCheck size={15} class="ml-auto shrink-0 text-blue-600" />
								{/if}
							</Command.Item>
						{/each}
					</Command.List>
				</div>
			</Command.Root>
		</Popover.Content>
	</Popover.Portal>
</Popover.Root>

<style>
	:global(.combobox-list::-webkit-scrollbar) {
		width: 4px;
		height: 4px;
	}
	:global(.combobox-list::-webkit-scrollbar-track) {
		background: transparent;
	}
	:global(.combobox-list::-webkit-scrollbar-thumb) {
		background-color: #cbd5e1;
		border-radius: 9999px;
	}
	:global(.combobox-list::-webkit-scrollbar-thumb:hover) {
		background-color: #94a3b8;
	}
	:global(.combobox-list::-webkit-scrollbar-button) {
		display: none !important;
		width: 0 !important;
		height: 0 !important;
	}
	:global(.combobox-list) {
		scrollbar-width: thin;
		scrollbar-color: #cbd5e1 transparent;
	}
</style>
