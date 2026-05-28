<script lang="ts">
	import type { Snippet } from 'svelte';

	let {
		title,
		subtitle,
		darkVariant = false,
		children
	}: {
		title: string;
		subtitle?: string;
		darkVariant?: boolean;
		children: Snippet;
	} = $props();
</script>

<div
	class="w-full max-w-md rounded-2xl border p-8 shadow-xl backdrop-blur-md transition-all duration-300 md:p-10
		{darkVariant
		? 'border-zinc-800 bg-zinc-900/80 text-white'
		: 'border-hairline bg-canvas/90 text-body'}"
>
	<div class="mb-6 text-center">
		<h2 class="text-2xl font-bold tracking-tight {darkVariant ? 'text-white' : 'text-body-strong'}">
			{title}
		</h2>
		{#if subtitle}
			<p class="mt-2 text-sm {darkVariant ? 'text-gray-400' : 'text-mute'}">
				{subtitle}
			</p>
		{/if}
	</div>

	{@render children()}
</div>
