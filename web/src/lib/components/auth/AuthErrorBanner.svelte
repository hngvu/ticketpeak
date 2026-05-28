<script lang="ts">
	let { message }: { message: string | null } = $props();
</script>

{#if message}
	<div
		class="mb-4 flex items-start gap-3 rounded-lg border border-red-200/50 bg-red-50/50 p-4 text-sm text-red-600 backdrop-blur-xs dark:border-red-950/30 dark:bg-red-950/20 dark:text-red-400"
	>
		<svg
			xmlns="http://www.w3.org/2000/svg"
			fill="none"
			viewBox="0 0 24 24"
			stroke-width="2"
			stroke="currentColor"
			class="h-5 w-5 shrink-0 text-red-500"
		>
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"
			/>
		</svg>
		<div class="flex-1 leading-5 font-medium">{message}</div>
	</div>
{/if}
