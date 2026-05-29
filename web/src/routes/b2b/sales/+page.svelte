<script lang="ts">
	let { data } = $props();

	// Mock sales transactions
	const transactions = [
		{
			id: 'TXN-9021',
			buyer: 'Nguyễn Hoàng Long',
			email: 'long.nh@gmail.com',
			event: data.events?.[0]?.title || 'Sơn Tùng M-TP Concert 2026',
			tickets: 2,
			amount: '3,000,000',
			method: 'VNPAY',
			date: '2026-05-28 14:23',
			status: 'PAID'
		},
		{
			id: 'TXN-9020',
			buyer: 'Phạm Minh Hưng',
			email: 'hung.pm@yahoo.com',
			event: data.events?.[0]?.title || 'Sơn Tùng M-TP Concert 2026',
			tickets: 1,
			amount: '1,500,000',
			method: 'STRIPE',
			date: '2026-05-28 11:15',
			status: 'PAID'
		},
		{
			id: 'TXN-9019',
			buyer: 'Trần Thị Thu Thảo',
			email: 'thao.ttt@hotmail.com',
			event: data.events?.[1]?.title || 'Đen Vâu Live Show 2026',
			tickets: 4,
			amount: '4,800,000',
			method: 'VNPAY',
			date: '2026-05-27 20:05',
			status: 'PAID'
		},
		{
			id: 'TXN-9018',
			buyer: 'Vũ Anh Tuấn',
			email: 'tuan.va@gmail.com',
			event: data.events?.[0]?.title || 'Sơn Tùng M-TP Concert 2026',
			tickets: 2,
			amount: '3,000,000',
			method: 'VNPAY',
			date: '2026-05-27 18:40',
			status: 'REFUNDED'
		},
		{
			id: 'TXN-9017',
			buyer: 'Lê Minh Triết',
			email: 'triet.lm@gmail.com',
			event: data.events?.[1]?.title || 'Đen Vâu Live Show 2026',
			tickets: 1,
			amount: '1,200,000',
			method: 'STRIPE',
			date: '2026-05-27 09:30',
			status: 'PAID'
		}
	];
</script>

<svelte:head>
	<title>Ticket Sales — Ticketpeak for Business</title>
</svelte:head>

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-8 p-6">
	<!-- Title block -->
	<div>
		<h1 class="text-2xl font-extrabold text-slate-900 md:text-3xl dark:text-white">
			Ticket Sales Ledger
		</h1>
		<p class="text-sm font-medium text-slate-500">
			Track transaction records, manage buyer profiles, and inspect payment gateways.
		</p>
	</div>

	<!-- Sales KPIs -->
	<div class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
		<div class="animate-fade-in rounded-xl border border-hairline bg-canvas bg-white p-5 shadow-xs">
			<span class="text-xs font-bold tracking-wider text-slate-400 uppercase">Gross Sales</span>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-extrabold text-slate-900">450.0M</span>
				<span class="text-xs font-semibold text-emerald-600">VND</span>
			</div>
		</div>
		<div class="animate-fade-in rounded-xl border border-hairline bg-canvas bg-white p-5 shadow-xs">
			<span class="text-xs font-bold tracking-wider text-slate-400 uppercase">Tickets Sold</span>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-extrabold text-slate-900">328</span>
				<span class="text-xs font-medium text-slate-500">tickets</span>
			</div>
		</div>
		<div class="animate-fade-in rounded-xl border border-hairline bg-canvas bg-white p-5 shadow-xs">
			<span class="text-xs font-bold tracking-wider text-slate-400 uppercase">Total Orders</span>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-extrabold text-slate-900">184</span>
				<span class="text-xs font-medium text-slate-500">successful checkouts</span>
			</div>
		</div>
		<div class="animate-fade-in rounded-xl border border-hairline bg-canvas bg-white p-5 shadow-xs">
			<span class="text-xs font-bold tracking-wider text-slate-400 uppercase"
				>Chargebacks / Refunds</span
			>
			<div class="mt-2 flex items-baseline gap-2">
				<span class="text-3xl font-extrabold text-red-600">1.2%</span>
				<span class="text-xs font-medium text-slate-500">rate</span>
			</div>
		</div>
	</div>

	<!-- Transaction Table -->
	<div class="rounded-xl border border-hairline bg-canvas bg-white p-6 shadow-xs">
		<h2 class="mb-4 text-lg font-bold text-slate-900">Recent Transactions</h2>
		<div class="overflow-x-auto">
			<table class="w-full text-left text-sm text-slate-500">
				<thead>
					<tr
						class="border-b border-hairline text-xs font-bold tracking-wider text-slate-400 uppercase"
					>
						<th class="py-3 pr-4">Order ID</th>
						<th class="px-4 py-3">Customer</th>
						<th class="px-4 py-3">Event Detail</th>
						<th class="px-4 py-3 text-center">Qty</th>
						<th class="px-4 py-3 text-right">Revenue</th>
						<th class="px-4 py-3 text-center">Payment</th>
						<th class="py-3 pl-4 text-right">Status</th>
					</tr>
				</thead>
				<tbody class="divide-y divide-hairline">
					{#each transactions as txn (txn.id)}
						<tr class="transition-colors hover:bg-slate-50/50">
							<td class="py-4 pr-4 font-mono font-bold text-slate-800">{txn.id}</td>
							<td class="px-4 py-4">
								<div class="font-bold text-slate-900">{txn.buyer}</div>
								<div class="text-xs text-slate-400">{txn.email}</div>
							</td>
							<td class="max-w-[200px] truncate px-4 py-4 font-medium text-slate-700"
								>{txn.event}</td
							>
							<td class="px-4 py-4 text-center font-bold text-slate-900">{txn.tickets}</td>
							<td class="px-4 py-4 text-right font-extrabold text-slate-900"
								>{txn.amount} <span class="text-[10px] text-mute">VND</span></td
							>
							<td class="px-4 py-4 text-center text-xs font-bold text-slate-500">{txn.method}</td>
							<td class="py-4 pl-4 text-right">
								{#if txn.status === 'PAID'}
									<span
										class="inline-flex items-center rounded-full bg-green-50 px-2.5 py-0.5 text-xs font-bold text-green-700 uppercase"
										>PAID</span
									>
								{:else}
									<span
										class="inline-flex items-center rounded-full bg-red-50 px-2.5 py-0.5 text-xs font-bold text-red-700 uppercase"
										>REFUNDED</span
									>
								{/if}
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	</div>
</div>
