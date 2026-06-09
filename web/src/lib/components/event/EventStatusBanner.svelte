<script lang="ts">
	let { status } = $props<{ status: string }>();
</script>

{#if status === 'CANCELLED'}
	<div
		class="bg-error-soft mb-6 flex items-start gap-3 rounded-lg border border-error/20 p-4 select-none"
	>
		<svg
			xmlns="http://www.w3.org/2000/svg"
			viewBox="0 0 24 24"
			fill="none"
			stroke="currentColor"
			stroke-width="2"
			stroke-linecap="round"
			stroke-linejoin="round"
			class="text-error-deep mt-0.5 h-5 w-5 shrink-0"
		>
			<circle cx="12" cy="12" r="10" />
			<line x1="15" x2="9" y1="9" y2="15" />
			<line x1="9" x2="15" y1="9" y2="15" />
		</svg>
		<div>
			<h4 class="text-error-deep text-sm font-bold">Event Cancelled</h4>
			<p class="text-error-deep/80 mt-0.5 text-xs leading-relaxed font-semibold">
				This event has been officially cancelled. Ticket refunds will be processed automatically to
				the original method of payment.
			</p>
		</div>
	</div>
{:else if status === 'POSTPONED'}
	<div
		class="bg-warning-soft mb-6 flex items-start gap-3 rounded-lg border border-warning/20 p-4 select-none"
	>
		<svg
			xmlns="http://www.w3.org/2000/svg"
			viewBox="0 0 24 24"
			fill="none"
			stroke="currentColor"
			stroke-width="2"
			stroke-linecap="round"
			stroke-linejoin="round"
			class="text-warning-deep mt-0.5 h-5 w-5 shrink-0"
		>
			<circle cx="12" cy="12" r="10" />
			<polyline points="12 6 12 12 16 14" />
		</svg>
		<div>
			<h4 class="text-warning-deep text-sm font-bold">Event Postponed</h4>
			<p class="text-warning-deep/80 mt-0.5 text-xs leading-relaxed font-semibold">
				This event has been postponed. Hang on to your tickets as they will be valid for the new
				date, which will be announced soon.
			</p>
		</div>
	</div>
{:else if status === 'DRAFT'}
	<div
		class="mb-6 flex items-start gap-3 rounded-lg border border-hairline-strong/30 bg-canvas-soft-2 p-4 select-none"
	>
		<svg
			xmlns="http://www.w3.org/2000/svg"
			viewBox="0 0 24 24"
			fill="none"
			stroke="currentColor"
			stroke-width="2"
			stroke-linecap="round"
			stroke-linejoin="round"
			class="mt-0.5 h-5 w-5 shrink-0 text-mute"
		>
			<rect width="18" height="11" x="3" y="11" rx="2" ry="2" />
			<path d="M7 11V7a5 5 0 0 1 10 0v4" />
		</svg>
		<div>
			<h4 class="text-sm font-bold text-ink">Preview Mode</h4>
			<p class="mt-0.5 text-xs leading-relaxed font-semibold text-mute">
				This event is currently in Draft. You are viewing this page as an organizer. Tickets cannot
				be reserved or purchased by the public at this stage.
			</p>
		</div>
	</div>
{/if}
