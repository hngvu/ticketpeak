<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	/* eslint-disable @typescript-eslint/no-explicit-any */
	import { encodeUuidToBase62 } from '$lib/base62';
	let { attractions = [] } = $props<{ attractions: any[] }>();

	const defaultAvatar =
		'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=150&q=80';
</script>

{#if attractions.length > 0}
	<div class="mt-8 border-t border-hairline pt-6 select-none">
		<h3 class="mb-4 font-sans text-base font-bold text-ink sm:text-lg">Lineup</h3>

		<div class="flex flex-wrap gap-4">
			{#each attractions as attraction (attraction.id)}
				<a
					href="/{attraction.slug}/artist/{encodeUuidToBase62(attraction.id)}"
					class="group flex items-center gap-3 rounded-lg border border-hairline bg-canvas p-2.5 pr-4 shadow-2xs transition-all duration-200 hover:border-hairline-strong hover:bg-canvas-soft/30 hover:shadow-xs"
				>
					<!-- Avatar -->
					<img
						src={attraction.imageUrl || defaultAvatar}
						alt={attraction.name}
						class="h-10 w-10 shrink-0 rounded-full border border-hairline object-cover transition group-hover:scale-105"
					/>

					<!-- Name and Info -->
					<div class="min-w-0">
						<p
							class="truncate font-sans text-xs font-bold text-ink transition-colors group-hover:text-primary sm:text-sm"
						>
							{attraction.name}
						</p>
						<p class="font-sans text-[10px] tracking-wide text-mute uppercase">
							{attraction.type || 'Artist'}
						</p>
					</div>
				</a>
			{/each}
		</div>
	</div>
{/if}
