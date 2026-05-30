<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import { enhance } from '$app/forms';

	let { data, form } = $props();

	let qrInput = $state('');
	let loading = $state(false);

	// Simulated scanner animation state
	let scanningActive = $state(false);

	function autoScanTicket(payload: string) {
		qrInput = payload;
		scanningActive = true;

		// Micro-animation delay before submission to look like a real scan!
		setTimeout(() => {
			const formEl = document.getElementById('scan-form') as HTMLFormElement;
			if (formEl) {
				formEl.requestSubmit();
			}
			scanningActive = false;
		}, 600);
	}

	function refreshOtpCodes() {
		window.location.reload();
	}
</script>

<svelte:head>
	<title>Check-In Gate Simulator — {data.event.title}</title>
</svelte:head>

<div class="mx-auto flex w-full max-w-7xl flex-1 flex-col space-y-8 p-6">
	<!-- Top Bar header -->
	<div class="flex flex-col gap-4 border-b border-hairline pb-6">
		<a
			href="/b2b/events?organizationId={data.event.organizationId}"
			class="flex items-center gap-1.5 text-xs font-bold text-slate-400 transition hover:text-slate-600"
		>
			<span>←</span>
			<span>Back to Events</span>
		</a>

		<div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
			<div>
				<h1 class="text-2xl font-extrabold text-slate-900 md:text-3xl">Gate Check-In Simulator</h1>
				<p class="mt-1 text-sm font-semibold text-purple-600">
					Event: {data.event.title}
				</p>
			</div>

			<!-- Status indicator / Refresh -->
			<div class="flex items-center gap-2">
				<span class="inline-flex h-2.5 w-2.5 animate-pulse rounded-full bg-emerald-500"></span>
				<span class="text-xs font-bold text-emerald-600 uppercase">Simulator Gate Online</span>
			</div>
		</div>
	</div>

	<!-- Main grid -->
	<div class="grid grid-cols-1 gap-8 lg:grid-cols-12">
		<!-- Left: Smartphone Scan Mockup (5 cols) -->
		<div class="flex flex-col items-center lg:col-span-5">
			<h2 class="mb-4 w-full text-center text-base font-bold text-slate-900">
				Smartphone Scan Device
			</h2>

			<!-- Phone container -->
			<div
				class="relative flex aspect-[9/18.5] w-full max-w-[340px] flex-col justify-between overflow-hidden rounded-[48px] border-[12px] border-slate-900 bg-slate-950 p-4 shadow-2xl ring-4 ring-slate-800"
			>
				<!-- Notch -->
				<div
					class="absolute top-0 left-1/2 z-30 h-5 w-32 -translate-x-1/2 rounded-b-2xl bg-slate-900"
				></div>

				<!-- Screen Content -->
				<div class="z-10 flex flex-1 flex-col justify-between space-y-4 pt-6 pb-2 text-white">
					<!-- Header on phone screen -->
					<div class="text-center">
						<span class="text-[10px] font-bold tracking-widest text-slate-500 uppercase"
							>Ticketpeak Scanner</span
						>
						<div class="mt-0.5 truncate text-xs font-bold text-slate-300">{data.event.title}</div>
					</div>

					<!-- Visual Camera / Targeting Area -->
					<div
						class="relative flex flex-1 flex-col items-center justify-center overflow-hidden rounded-2xl border border-slate-800 bg-slate-900 p-4"
					>
						<!-- Pulsing Green Scanner Laser -->
						{#if scanningActive}
							<div
								class="absolute inset-x-0 top-1/2 z-20 h-1 animate-bounce bg-purple-500 shadow-[0_0_12px_#a855f7]"
							></div>
						{/if}

						<!-- Standard Targeting Reticle Corners -->
						<div class="absolute top-4 left-4 h-6 w-6 border-t-4 border-l-4 border-slate-700"></div>
						<div
							class="absolute top-4 right-4 h-6 w-6 border-t-4 border-r-4 border-slate-700"
						></div>
						<div
							class="absolute bottom-4 left-4 h-6 w-6 border-b-4 border-l-4 border-slate-700"
						></div>
						<div
							class="absolute right-4 bottom-4 h-6 w-6 border-r-4 border-b-4 border-slate-700"
						></div>

						<!-- Scan feedback banners -->
						{#if form?.success && form?.scanResult}
							<!-- SUCCESS STATE -->
							{#if form.scanResult.alreadyCheckedIn}
								<div
									class="animate-fade-in absolute inset-0 z-10 flex flex-col items-center justify-center space-y-3 bg-amber-950/95 p-6 text-center"
								>
									<div
										class="flex h-14 w-14 items-center justify-center rounded-full border border-amber-500 bg-amber-900/50 text-amber-500 shadow-lg"
									>
										<svg
											class="h-8 w-8"
											fill="none"
											viewBox="0 0 24 24"
											stroke="currentColor"
											stroke-width="2.5"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
											/>
										</svg>
									</div>
									<div class="text-sm font-extrabold tracking-wide text-amber-400 uppercase">
										Duplicate Scan
									</div>
									<p class="text-xs leading-relaxed font-semibold text-amber-200">
										This ticket was already checked in.
									</p>
									<div
										class="rounded-full bg-amber-900/40 px-2 py-0.5 text-[10px] font-bold text-amber-300"
									>
										Seat: {form.scanResult.seatId || 'GA'} • Area: {form.scanResult.areaId || 'GA'}
									</div>
								</div>
							{:else}
								<div
									class="animate-fade-in absolute inset-0 z-10 flex flex-col items-center justify-center space-y-3 bg-emerald-950/95 p-6 text-center"
								>
									<div
										class="flex h-14 w-14 items-center justify-center rounded-full border border-emerald-500 bg-emerald-900/50 text-emerald-500 shadow-lg"
									>
										<svg
											class="h-8 w-8"
											fill="none"
											viewBox="0 0 24 24"
											stroke="currentColor"
											stroke-width="2.5"
										>
											<path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
										</svg>
									</div>
									<div class="text-sm font-extrabold tracking-wide text-emerald-400 uppercase">
										Scan Approved
									</div>
									<p class="text-xs leading-relaxed font-semibold text-emerald-200">
										Ticket verified successfully!
									</p>
									<div
										class="rounded-full bg-emerald-900/40 px-2.5 py-0.5 text-[10px] font-bold text-emerald-300"
									>
										Seat: {form.scanResult.seatId || 'GA'} • Area: {form.scanResult.areaId || 'GA'}
									</div>
								</div>
							{/if}
						{:else if form?.error}
							<!-- FAIL/ERROR STATE -->
							<div
								class="animate-fade-in absolute inset-0 z-10 flex flex-col items-center justify-center space-y-3 bg-red-950/95 p-6 text-center"
							>
								<div
									class="flex h-14 w-14 items-center justify-center rounded-full border border-red-500 bg-red-900/50 text-red-500 shadow-lg"
								>
									<svg
										class="h-8 w-8"
										fill="none"
										viewBox="0 0 24 24"
										stroke="currentColor"
										stroke-width="2.5"
									>
										<path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
									</svg>
								</div>
								<div class="text-sm font-extrabold tracking-wide text-red-400 uppercase">
									Scan Rejected
								</div>
								<p class="max-w-xs text-[11px] leading-relaxed font-bold text-red-200">
									{form.error}
								</p>
							</div>
						{:else}
							<!-- DEFAULT ACTIVE CAMERA SIMULATOR STATE -->
							<div class="space-y-2 p-2 text-center">
								<svg
									class="mx-auto h-10 w-10 animate-pulse text-slate-500"
									fill="none"
									viewBox="0 0 24 24"
									stroke="currentColor"
									stroke-width="1.5"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										d="M6.827 6.175A2.31 2.31 0 015.186 7.23c-.38.054-.757.112-1.134.175C2.999 7.58 2.25 8.507 2.25 9.574V18a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9.574c0-1.067-.75-1.994-1.802-2.169a47.865 47.865 0 00-1.134-.175 2.31 2.31 0 01-1.64-1.055l-.822-1.316a2.192 2.192 0 00-1.736-1.039 48.774 48.774 0 00-5.232 0 2.192 2.192 0 00-1.736 1.039l-.821 1.316z"
									/>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										d="M16.5 12.75a4.5 4.5 0 11-9 0 4.5 4.5 0 019 0zM18.75 10.5h.008v.008h-.008V10.5z"
									/>
								</svg>
								<div class="text-xs font-semibold text-slate-400">
									Position ticket QR within target
								</div>
								<div class="text-[10px] font-medium text-slate-500">
									Or select an attendee ticket on the right panel to auto-scan
								</div>
							</div>
						{/if}
					</div>

					<!-- Form Scan Input -->
					<form
						id="scan-form"
						method="POST"
						action="?/checkIn"
						use:enhance={() => {
							loading = true;
							return async ({ update }) => {
								await update();
								loading = false;
							};
						}}
						class="space-y-3"
					>
						<div>
							<label
								for="scan-input"
								class="mb-1 block text-[10px] font-bold tracking-widest text-slate-400 uppercase"
							>
								QR Payload string
							</label>
							<input
								type="text"
								id="scan-input"
								name="qrPayload"
								required
								bind:value={qrInput}
								placeholder="ticketId:otp"
								class="w-full rounded-xl border border-slate-800 bg-slate-900 px-3 py-2 text-xs text-white placeholder-slate-600 outline-none focus:border-purple-500"
							/>
						</div>

						<button
							type="submit"
							disabled={loading}
							class="flex w-full cursor-pointer items-center justify-center gap-1.5 rounded-xl bg-purple-600 py-2.5 text-xs font-bold text-white transition hover:bg-purple-500 active:scale-[0.97] disabled:opacity-50"
						>
							{#if loading}
								<span
									class="h-3.5 w-3.5 animate-spin rounded-full border-2 border-white border-t-transparent"
								></span>
							{/if}
							<span>Simulate Scan</span>
						</button>
					</form>
				</div>
			</div>
		</div>

		<!-- Right: Simulated Attendee Ticket Database (7 cols) -->
		<div class="flex flex-col space-y-6 lg:col-span-7">
			<div class="flex flex-1 flex-col rounded-xl border border-hairline bg-canvas p-6 shadow-xs">
				<div class="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
					<div>
						<h2 class="text-base font-bold text-slate-900">Event Issued Tickets</h2>
						<p class="text-xs text-slate-500">Active OTP codes recalculate every 30 seconds.</p>
					</div>

					<!-- Refresh CTA -->
					<button
						onclick={refreshOtpCodes}
						class="flex cursor-pointer items-center justify-center gap-1.5 rounded-full border border-hairline bg-white px-4 py-2 text-xs font-bold text-slate-700 shadow-xs transition hover:bg-slate-50 hover:text-slate-900 active:scale-95"
					>
						<svg
							class="h-3.5 w-3.5 text-slate-400"
							fill="none"
							viewBox="0 0 24 24"
							stroke="currentColor"
							stroke-width="2.5"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0l3.181 3.183a8.25 8.25 0 0013.803-3.7M4.031 9.865a8.25 8.25 0 0113.803-3.7l3.181 3.182m0-4.991v4.99"
							/>
						</svg>
						<span>Refresh OTPs</span>
					</button>
				</div>

				<!-- Tickets list -->
				{#if data.tickets && data.tickets.length > 0}
					<div class="flex-1 overflow-x-auto">
						<table class="w-full text-left text-sm text-slate-500">
							<thead>
								<tr
									class="border-b border-hairline text-xs font-bold tracking-wider text-slate-400 uppercase"
								>
									<th class="py-3 pr-4">Ticket Type</th>
									<th class="px-4 py-3">Seat ID</th>
									<th class="px-4 py-3 text-center">Status</th>
									<th class="px-4 py-3">Active OTP</th>
									<th class="py-3 pl-4 text-right">Action</th>
								</tr>
							</thead>
							<tbody class="divide-y divide-hairline">
								{#each data.tickets as ticket (ticket.ticketId)}
									<tr class="transition hover:bg-slate-50/50">
										<!-- Ticket Type -->
										<td class="py-3.5 pr-4 text-xs font-bold text-slate-900">
											{ticket.offerName}
											<div
												class="mt-0.5 max-w-[120px] truncate text-[9px] font-semibold tracking-wider text-slate-400 uppercase"
												title={ticket.ticketId}
											>
												ID: {ticket.ticketId.substring(0, 8)}...
											</div>
										</td>

										<!-- Seat ID -->
										<td class="px-4 py-3.5 text-xs font-semibold text-slate-700">
											{ticket.seatId || 'General Adm'}
										</td>

										<!-- Status -->
										<td class="px-4 py-3.5 text-center">
											{#if ticket.status === 'ISSUED'}
												<span
													class="inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[9px] font-bold text-slate-600 uppercase"
												>
													Issued
												</span>
											{:else if ticket.status === 'CHECKED_IN'}
												<span
													class="inline-flex items-center rounded-full bg-emerald-50 px-2 py-0.5 text-[9px] font-bold text-emerald-700 uppercase"
												>
													Checked In
												</span>
											{:else}
												<span
													class="inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[9px] font-bold text-slate-600 uppercase"
												>
													{ticket.status}
												</span>
											{/if}
										</td>

										<!-- OTP -->
										<td class="px-4 py-3.5 font-mono text-xs font-bold text-purple-600">
											{ticket.currentOtp || '------'}
										</td>

										<!-- Trigger -->
										<td class="py-3.5 pl-4 text-right">
											<button
												onclick={() => autoScanTicket(ticket.qrPayload)}
												class="cursor-pointer rounded-full border border-purple-200 bg-purple-50 px-3 py-1 text-xs font-bold text-purple-700 transition hover:bg-purple-100"
											>
												Scan Ticket
											</button>
										</td>
									</tr>
								{/each}
							</tbody>
						</table>
					</div>
				{:else}
					<div
						class="flex flex-1 flex-col items-center justify-center space-y-3 rounded-lg border border-dashed border-hairline p-10 text-center"
					>
						<div
							class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-400"
						>
							<svg
								class="h-5 w-5"
								fill="none"
								viewBox="0 0 24 24"
								stroke="currentColor"
								stroke-width="2"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									d="M15 5v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
								/>
							</svg>
						</div>
						<div class="text-sm font-bold text-slate-800">No Tickets Issued Yet</div>
						<p class="max-w-sm text-xs leading-relaxed text-slate-500">
							Tickets must be bought for this event before check-in codes become active. Complete a
							checkout flow in the consumer app first.
						</p>
					</div>
				{/if}
			</div>
		</div>
	</div>
</div>
