<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { resolve } from '$app/paths';

	export interface EmptyStateProps {
		title?: string;
		message?: string;
		actionHref?: string;
		actionText?: string;
	}

	let {
		title = 'No events found',
		message = 'Try adjusting your search keywords, locations, or date range filters.',
		actionHref = '/discover',
		actionText = 'Browse All Events'
	}: EmptyStateProps = $props();
</script>

<div
	class="mx-auto my-12 flex max-w-xl flex-col items-center justify-center rounded-lg border border-hairline bg-canvas p-8 text-center shadow-sm md:p-12"
>
	<!-- Glass icon background -->
	<div
		class="bg-blue-accent-soft mb-5 flex h-14 w-14 items-center justify-center rounded-full text-primary shadow-inner"
	>
		<svg
			class="h-6 w-6"
			fill="none"
			stroke="currentColor"
			stroke-width="2.5"
			viewBox="0 0 24 24"
			xmlns="http://www.w3.org/2000/svg"
		>
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"
			/>
		</svg>
	</div>

	<!-- Title -->
	<h3 class="mb-2 text-base font-bold text-ink md:text-lg">
		{title}
	</h3>

	<!-- Message -->
	<p class="mb-6 max-w-md text-xs leading-relaxed text-mute md:text-sm">
		{message}
	</p>

	<!-- Action CTA -->
	{#if actionText}
		<a
			href={resolve(actionHref as any)}
			class="inline-flex h-10 items-center justify-center rounded-full bg-primary px-6 text-sm font-semibold text-on-primary shadow-sm transition-colors hover:bg-primary/95 hover:shadow"
		>
			{actionText}
		</a>
	{/if}
</div>
