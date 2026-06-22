<script lang="ts">
	let { data } = $props<{ data: any }>();
</script>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-sidebar-brand font-bold text-ink">Dashboard</h1>
		<p class="text-sidebar-label text-body">Platform operations overview</p>
	</div>

	<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
		<div class="rounded-lg border border-hairline bg-canvas p-4">
			<p class="text-sidebar-tiny font-semibold tracking-wider text-mute uppercase">Events</p>
			<p class="mt-1 text-2xl font-bold text-ink">{(data.events || []).length}</p>
		</div>
		<div class="rounded-lg border border-hairline bg-canvas p-4">
			<p class="text-sidebar-tiny font-semibold tracking-wider text-mute uppercase">
				Organizations
			</p>
			<p class="mt-1 text-2xl font-bold text-ink">{(data.organizations || []).length}</p>
		</div>
		<div class="rounded-lg border border-hairline bg-canvas p-4">
			<p class="text-sidebar-tiny font-semibold tracking-wider text-mute uppercase">Users</p>
			<p class="mt-1 text-2xl font-bold text-ink">0</p>
		</div>
		<div class="rounded-lg border border-hairline bg-canvas p-4">
			<p class="text-sidebar-tiny font-semibold tracking-wider text-mute uppercase">Revenue</p>
			<p class="mt-1 text-2xl font-bold text-ink">$0</p>
		</div>
	</div>
</div>
