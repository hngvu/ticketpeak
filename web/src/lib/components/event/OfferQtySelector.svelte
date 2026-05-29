<script lang="ts">
	let {
		quantities = [1, 2, 3, 4],
		value = 0,
		onchange
	} = $props<{
		quantities?: number[];
		value: number;
		onchange: (qty: number) => void;
	}>();

	function handleChange(e: Event) {
		const select = e.target as HTMLSelectElement;
		onchange(parseInt(select.value, 10));
	}
</script>

<div class="relative shrink-0 select-none">
	<select
		{value}
		onchange={handleChange}
		class="h-9 min-w-[76px] cursor-pointer rounded-lg border border-hairline bg-canvas px-3 py-1 font-mono text-sm font-semibold text-ink shadow-2xs transition-colors hover:border-hairline-strong focus:border-primary focus:outline-none"
	>
		<option value={0}>0</option>
		{#each quantities as q (q)}
			<option value={q}>{q}</option>
		{/each}
	</select>
</div>
