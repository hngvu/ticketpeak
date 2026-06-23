<script lang="ts">
	import type { Snippet } from 'svelte';

	let { children }: { children: Snippet } = $props();
</script>

<div class="h-screen w-screen overflow-hidden">
	{@render children()}
</div>
