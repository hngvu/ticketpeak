<script lang="ts">
	import { Slider } from 'bits-ui';

	let {
		value = $bindable([39, 190]),
		min = 0,
		max = 500,
		step = 1
	} = $props<{
		value?: number[];
		min?: number;
		max?: number;
		step?: number;
	}>();
</script>

<Slider.Root
	bind:value
	{min}
	{max}
	{step}
	class="relative flex h-8 w-full touch-none items-center select-none"
	let:thumbs
>
	<span class="relative h-1 w-full rounded-full bg-ink/20">
		<Slider.Range class="absolute h-full rounded-full bg-ink" />
	</span>
	{#each thumbs as thumb}
		<Slider.Thumb
			{thumb}
			class="block h-4 w-4 cursor-pointer rounded-full border border-ink bg-white focus:outline-none"
		/>
	{/each}
</Slider.Root>
