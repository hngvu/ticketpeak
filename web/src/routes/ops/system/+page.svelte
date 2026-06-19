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
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">System Status & Telemetry</h1>
		<p class="font-sans text-xs text-body">
			Real-time operational dashboard for GraalVM JVM Heap, PostgreSQL connections, Redis caching efficiency, and MinIO storage.
		</p>
	</div>

	{#if form?.success}
		<div class="rounded-md border border-success bg-success/10 p-3.5 text-xs font-semibold text-success">
			✨ Success: {form.message}
		</div>
	{/if}

	<!-- System Telemetry Grid -->
	<div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
		<!-- JVM CPU Usage -->
		<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
			<div class="font-mono text-[10px] font-bold text-mute uppercase tracking-wider">GraalVM Virtual CPU</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="font-sans text-3xl font-semibold tracking-tight text-ink">{data.systemState.cpuUsage}%</span>
				<span class="rounded bg-success/15 px-1.5 py-0.5 font-mono text-[9px] font-bold text-success border border-success/10 uppercase">
					ONLINE
				</span>
			</div>
			<div class="mt-4 h-1.5 w-full bg-canvas-soft-3 rounded-full overflow-hidden">
				<div class="h-full bg-primary transition-all duration-500" style="width: {data.systemState.cpuUsage}%"></div>
			</div>
		</div>

		<!-- JVM Memory Heap -->
		<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
			<div class="font-mono text-[10px] font-bold text-mute uppercase tracking-wider">JVM Heap Memory</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="font-sans text-3xl font-semibold tracking-tight text-ink">{data.systemState.heapUsedMb} MB</span>
				<span class="text-xs text-mute">/ {data.systemState.heapMaxMb} MB</span>
			</div>
			<div class="mt-4 h-1.5 w-full bg-canvas-soft-3 rounded-full overflow-hidden">
				<div class="h-full bg-primary transition-all duration-500" style="width: {(data.systemState.heapUsedMb / data.systemState.heapMaxMb) * 100}%"></div>
			</div>
		</div>

		<!-- PostgreSQL Connections -->
		<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
			<div class="font-mono text-[10px] font-bold text-mute uppercase tracking-wider">PostgreSQL Pool</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="font-sans text-3xl font-semibold tracking-tight text-ink">{data.systemState.postgresConnections}</span>
				<span class="text-xs text-mute">Active Connections</span>
			</div>
			<div class="mt-4 h-1.5 w-full bg-canvas-soft-3 rounded-full overflow-hidden">
				<div class="h-full bg-primary transition-all duration-500" style="width: {(data.systemState.postgresConnections / 50) * 100}%"></div>
			</div>
		</div>

		<!-- Redis Hit Rate -->
		<div class="rounded-lg border border-hairline bg-canvas p-5 shadow-xs">
			<div class="font-mono text-[10px] font-bold text-mute uppercase tracking-wider">Redis Cache Hit Rate</div>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="font-sans text-3xl font-semibold tracking-tight text-ink">{data.systemState.redisCacheHits}%</span>
				<span class="text-xs text-mute">Efficiency</span>
			</div>
			<div class="mt-4 h-1.5 w-full bg-canvas-soft-3 rounded-full overflow-hidden">
				<div class="h-full bg-primary transition-all duration-500" style="width: {data.systemState.redisCacheHits}%"></div>
			</div>
		</div>
	</div>

	<!-- Storage Details & Operations Trigger -->
	<div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
		<!-- Object Storage Stats -->
		<div class="lg:col-span-2 rounded-lg border border-hairline bg-canvas p-6 shadow-xs space-y-4">
			<h3 class="font-sans text-sm font-semibold text-ink border-b border-hairline pb-3">MinIO Object Storage</h3>
			
			<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
				<div class="p-4 rounded-md bg-canvas-soft-2 border border-hairline">
					<div class="text-[10px] font-mono font-bold text-mute uppercase tracking-wider">Buckets Count</div>
					<div class="mt-1 font-sans text-xl font-bold text-ink">{data.systemState.minioBucketsCount} buckets</div>
					<p class="mt-1 text-[10px] text-mute">events-assets, ticket-qrs, invoices</p>
				</div>
				<div class="p-4 rounded-md bg-canvas-soft-2 border border-hairline">
					<div class="text-[10px] font-mono font-bold text-mute uppercase tracking-wider">Storage Utilized</div>
					<div class="mt-1 font-sans text-xl font-bold text-ink">{formatBytes(data.systemState.minioStorageUsedBytes)}</div>
					<p class="mt-1 text-[10px] text-mute">Total assets size across objects</p>
				</div>
			</div>
		</div>

		<!-- Operations Control Panel -->
		<div class="rounded-lg border border-hairline bg-canvas p-6 shadow-xs space-y-6">
			<div>
				<h3 class="font-sans text-sm font-semibold text-ink border-b border-hairline pb-3">Maintenance Tasks</h3>
				<p class="mt-2 text-xs text-mute leading-relaxed">
					Perform hot-swaps, release cached memory allocations, or void Redis caches to clear seat allocation locks.
				</p>
			</div>

			<div class="space-y-3">
				<form method="POST" action="?/triggerGc" use:enhance>
					<button
						type="submit"
						class="w-full cursor-pointer rounded-full border border-hairline bg-canvas px-4 py-2 text-left font-sans text-xs font-semibold text-ink hover:bg-canvas-soft-2 transition flex items-center justify-between"
					>
						<span>Run Garbage Collector (GC)</span>
						<span class="font-mono text-[10px] text-mute">GC Run: {data.systemState.garbageCollectedCount}</span>
					</button>
				</form>

				<form method="POST" action="?/clearRedis" use:enhance>
					<button
						type="submit"
						class="w-full cursor-pointer rounded-full border border-error/25 bg-error/5 px-4 py-2 text-left font-sans text-xs font-semibold text-error hover:bg-error/10 transition flex items-center justify-between"
					>
						<span>Flush Redis Cache Keys</span>
						<span class="font-mono text-[10px] text-error/85">COLD START</span>
					</button>
				</form>
			</div>
		</div>
	</div>
</div>
