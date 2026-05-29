<script lang="ts">
	/* eslint-disable @typescript-eslint/no-explicit-any */
	let { attraction } = $props<{ attraction: any }>();

	let isFollowing = $state(false);
	// Simulated follower count increment
	let followersCount = $state(
		attraction.id === 'artist-sontung'
			? 248900
			: attraction.id === 'artist-denvau'
				? 189500
				: attraction.id === 'artist-taylorswift'
					? 1545000
					: 87400
	);

	function toggleFollow() {
		if (isFollowing) {
			isFollowing = false;
			followersCount--;
		} else {
			isFollowing = true;
			followersCount++;
		}
	}

	const displayFollowers = $derived(followersCount.toLocaleString('en-US'));

	// Map mock types to nice display values
	const displayType = $derived(
		attraction.type === 'SportsTeam' ? 'Sports Team' : attraction.type || 'Artist'
	);
</script>

<section class="relative w-full overflow-hidden bg-slate-950 text-white select-none">
	<!-- Blurred background image matching Ticketmaster ADP -->
	<div class="absolute inset-0 h-64 overflow-hidden md:h-80">
		<img
			src={attraction.imageUrl || '/placeholder-artist.jpg'}
			alt=""
			class="h-full w-full scale-105 object-cover opacity-40 blur-md brightness-50"
		/>
		<!-- Premium linear-gradient overlay -->
		<div
			class="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/20 to-black/30"
		></div>
	</div>

	<!-- Content Bounds -->
	<div
		class="relative z-10 mx-auto flex max-w-[1400px] flex-col gap-5 px-4 pt-16 pb-6 sm:flex-row sm:items-end md:px-6 md:pt-24"
	>
		<!-- circular Avatar -->
		<div class="shrink-0">
			<img
				src={attraction.imageUrl || '/placeholder-artist.jpg'}
				alt={attraction.name}
				class="h-20 w-20 rounded-full border-2 border-white/20 object-cover shadow-lg md:h-24 md:w-24"
			/>
		</div>

		<!-- Details -->
		<div class="flex-1 space-y-2.5">
			<div>
				<span
					class="inline-block rounded-full bg-white/10 px-2.5 py-0.5 text-[10px] font-bold tracking-wider text-white/90 uppercase backdrop-blur-sm"
				>
					{displayType}
				</span>
			</div>
			<div class="space-y-1">
				<h1 class="font-sans text-3xl font-extrabold tracking-tight text-white md:text-5xl">
					{attraction.name}
				</h1>
				<p class="font-mono text-xs font-semibold tracking-wide text-white/70">
					Vietnam · V-Pop · Alternative
				</p>
			</div>

			<!-- Follow actions -->
			<div class="flex items-center gap-3.5 pt-1">
				<button
					type="button"
					onclick={toggleFollow}
					class="inline-flex cursor-pointer items-center justify-center gap-1.5 rounded-full border px-6 py-2.5 text-xs font-bold transition-all select-none active:scale-[0.97]
						{isFollowing
						? 'border-white bg-white text-ink shadow-md'
						: 'border-white/20 bg-white/10 text-white hover:bg-white/20'}"
				>
					{#if isFollowing}
						<svg
							xmlns="http://www.w3.org/2000/svg"
							viewBox="0 0 24 24"
							fill="currentColor"
							class="h-3.5 w-3.5 text-error"
						>
							<path
								d="M11.645 20.91l-.007-.003-.022-.012a15.247 15.247 0 01-.383-.218 25.18 25.18 0 01-4.244-3.17C4.688 15.36 2.25 12.174 2.25 8.25 2.25 5.322 4.714 3 7.688 3A5.5 5.5 0 0112 5.052 5.5 5.5 0 0116.313 3c2.973 0 5.437 2.322 5.437 5.25 0 3.925-2.438 7.111-4.739 9.256a25.175 25.175 0 01-4.244 3.17 15.247 15.247 0 01-.383.219l-.022.012-.007.004-.003.001a.752.752 0 01-.704 0l-.003-.001z"
							/>
						</svg>
						<span>Following</span>
					{:else}
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
							stroke-width="2.5"
							stroke="currentColor"
							class="h-3.5 w-3.5"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12z"
							/>
						</svg>
						<span>Follow</span>
					{/if}
				</button>
				<span class="font-mono text-xs font-semibold text-white/60">
					{displayFollowers} Followers
				</span>
			</div>
		</div>
	</div>
</section>
