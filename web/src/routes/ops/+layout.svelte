<script lang="ts">
	import type { Snippet } from 'svelte';
	import '../../routes/layout.css';

	let { children }: { children: Snippet } = $props();
</script>

<div class="flex min-h-screen flex-col bg-[#1A1F26] text-white">
	<!-- Stripped dark shell container -->
	<main class="flex flex-1 flex-col">
		{@render children()}
	</main>
</div>
