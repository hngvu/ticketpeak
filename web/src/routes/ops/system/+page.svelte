<script lang="ts">
	import { enhance } from '$app/forms';

	let { data, form } = $props<{ data: any; form: any }>();

	function formatBytes(bytes: number) {
		if (bytes === 0) return '0 Bytes';
		const k = 1024;
		const sizes = ['Bytes', 'KB', 'MB', 'GB'];
		const i = Math.floor(Math.log(bytes) / Math.log(k));
		return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
	}
</script>

<svelte:head>
	<title>System Status — Platform Operations</title>
</svelte:head>

<div class="flex flex-1 flex-col space-y-6 p-6">
	<div class="flex flex-col gap-1 border-b border-hairline pb-5">
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">
			System Status & Telemetry
		</h1>
		<p class="font-sans text-xs text-body">
			Real-time operational dashboard for GraalVM JVM Heap, PostgreSQL connections, Redis caching
			efficiency, and MinIO storage.
		</p>
	</div>

	{#if form?.success}
		<div
			class="rounded-md border border-success bg-success/10 p-3.5 text-xs font-semibold text-success"
		>
			✨ Success: {form.message}
		</div>
	{/if}

	<!-- System Telemetry Grid -->
	<div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
		<!-- JVM CPU Usage -->
		<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
			<div class="font-mono text-[10px] font-bold tracking-wider text-mute uppercase">
				GraalVM Virtual CPU
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="font-sans text-3xl font-semibold tracking-tight text-ink"
					>{data.systemState.cpuUsage}%</span
				>
				<span
					class="rounded border border-success/10 bg-success/15 px-1.5 py-0.5 font-mono text-[9px] font-bold text-success uppercase"
				>
					ONLINE
				</span>
			</div>
			<div class="bg-canvas-soft-3 mt-4 h-1.5 w-full overflow-hidden rounded-full">
				<div
					class="h-full bg-primary transition-all duration-500"
					style="width: {data.systemState.cpuUsage}%"
				></div>
			</div>
		</div>

		<!-- JVM Memory Heap -->
		<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
			<div class="font-mono text-[10px] font-bold tracking-wider text-mute uppercase">
				JVM Heap Memory
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="font-sans text-3xl font-semibold tracking-tight text-ink"
					>{data.systemState.heapUsedMb} MB</span
				>
				<span class="text-xs text-mute">/ {data.systemState.heapMaxMb} MB</span>
			</div>
			<div class="bg-canvas-soft-3 mt-4 h-1.5 w-full overflow-hidden rounded-full">
				<div
					class="h-full bg-primary transition-all duration-500"
					style="width: {(data.systemState.heapUsedMb / data.systemState.heapMaxMb) * 100}%"
				></div>
			</div>
		</div>

		<!-- PostgreSQL Connections -->
		<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
			<div class="font-mono text-[10px] font-bold tracking-wider text-mute uppercase">
				PostgreSQL Pool
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="font-sans text-3xl font-semibold tracking-tight text-ink"
					>{data.systemState.postgresConnections}</span
				>
				<span class="text-xs text-mute">Active Connections</span>
			</div>
			<div class="bg-canvas-soft-3 mt-4 h-1.5 w-full overflow-hidden rounded-full">
				<div
					class="h-full bg-primary transition-all duration-500"
					style="width: {(data.systemState.postgresConnections / 50) * 100}%"
				></div>
			</div>
		</div>

		<!-- Redis Hit Rate -->
		<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
			<div class="font-mono text-[10px] font-bold tracking-wider text-mute uppercase">
				Redis Cache Hit Rate
			</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="font-sans text-3xl font-semibold tracking-tight text-ink"
					>{data.systemState.redisCacheHits}%</span
				>
				<span class="text-xs text-mute">Efficiency</span>
			</div>
			<div class="bg-canvas-soft-3 mt-4 h-1.5 w-full overflow-hidden rounded-full">
				<div
					class="h-full bg-primary transition-all duration-500"
					style="width: {data.systemState.redisCacheHits}%"
				></div>
			</div>
		</div>
	</div>

	<!-- Storage Details & Operations Trigger -->
	<div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
		<!-- Object Storage Stats -->
		<div class="space-y-4 rounded-lg border border-hairline bg-canvas p-6 shadow-xs lg:col-span-2">
			<h3 class="border-b border-hairline pb-3 font-sans text-sm font-semibold text-ink">
				MinIO Object Storage
			</h3>

			<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
				<div class="rounded-md border border-hairline bg-canvas-soft-2 p-4">
					<div class="font-mono text-[10px] font-bold tracking-wider text-mute uppercase">
						Buckets Count
					</div>
					<div class="mt-1 font-sans text-xl font-bold text-ink">
						{data.systemState.minioBucketsCount} buckets
					</div>
					<p class="mt-1 text-[10px] text-mute">events-assets, ticket-qrs, invoices</p>
				</div>
				<div class="rounded-md border border-hairline bg-canvas-soft-2 p-4">
					<div class="font-mono text-[10px] font-bold tracking-wider text-mute uppercase">
						Storage Utilized
					</div>
					<div class="mt-1 font-sans text-xl font-bold text-ink">
						{formatBytes(data.systemState.minioStorageUsedBytes)}
					</div>
					<p class="mt-1 text-[10px] text-mute">Total assets size across objects</p>
				</div>
			</div>
		</div>

		<!-- Operations Control Panel -->
		<div class="space-y-6 rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
			<div>
				<h3 class="border-b border-hairline pb-3 font-sans text-sm font-semibold text-ink">
					Maintenance Tasks
				</h3>
				<p class="mt-2 text-xs leading-relaxed text-mute">
					Perform hot-swaps, release cached memory allocations, or void Redis caches to clear seat
					allocation locks.
				</p>
			</div>

			<div class="space-y-3">
				<form method="POST" action="?/triggerGc" use:enhance>
					<button
						type="submit"
						class="flex w-full cursor-pointer items-center justify-between rounded-full border border-hairline bg-canvas px-4 py-2 text-left font-sans text-xs font-semibold text-ink transition hover:bg-canvas-soft-2"
					>
						<span>Run Garbage Collector (GC)</span>
						<span class="font-mono text-[10px] text-mute"
							>GC Run: {data.systemState.garbageCollectedCount}</span
						>
					</button>
				</form>

				<form method="POST" action="?/clearRedis" use:enhance>
					<button
						type="submit"
						class="flex w-full cursor-pointer items-center justify-between rounded-full border border-error/25 bg-error/5 px-4 py-2 text-left font-sans text-xs font-semibold text-error transition hover:bg-error/10"
					>
						<span>Flush Redis Cache Keys</span>
						<span class="font-mono text-[10px] text-error/85">COLD START</span>
					</button>
				</form>
			</div>
		</div>
	</div>
</div>
