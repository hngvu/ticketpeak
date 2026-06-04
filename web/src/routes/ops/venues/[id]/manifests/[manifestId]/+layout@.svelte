<script lang="ts">
	import type { Snippet } from 'svelte';

	let { children }: { children: Snippet } = $props();
</script>

<!-- Layout reset: bypasses the ops sidebar/header for the manifest editor canvas -->
<div class="h-screen w-screen overflow-hidden">
	{@render children()}
</div>
