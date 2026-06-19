<script lang="ts">
	import { enhance } from '$app/forms';

	let { data, form } = $props<{ data: any; form: any }>();

	let priceCapPercent = $state(data.settings.priceCapPercent);
	let maxTransfersPerTicket = $state(data.settings.maxTransfersPerTicket);
	let kycRequiredForSeller = $state(data.settings.kycRequiredForSeller);
	let mfaRequiredForAdmin = $state(data.settings.mfaRequiredForAdmin);
	let totpQrExpirySeconds = $state(data.settings.totpQrExpirySeconds);
</script>

<svelte:head>
	<title>System Settings — Platform Operations</title>
</svelte:head>

<div class="flex flex-1 flex-col space-y-6 p-6">
	<div class="flex flex-col gap-1 border-b border-hairline pb-5">
		<h1 class="font-sans text-2xl font-semibold tracking-tight text-ink">System Regulations & Policies</h1>
		<p class="font-sans text-xs text-body">
			Configure global security boundaries, anti-scalping price caps, and buyer ticket transfer limits.
		</p>
	</div>

	{#if form?.success}
		<div class="rounded-md border border-success bg-success/10 p-3.5 text-xs font-semibold text-success">
			✨ Success: {form.message}
		</div>
	{/if}

	<div class="max-w-2xl rounded-lg border border-hairline bg-canvas p-6 shadow-xs">
		<form method="POST" action="?/saveSettings" use:enhance class="space-y-6">
			<!-- Anti-Scalping Price Cap -->
			<div class="space-y-2 border-b border-hairline pb-4">
				<div class="flex justify-between">
					<label for="price-cap" class="font-sans text-sm font-semibold text-ink">Secondary Market Price Cap Limit</label>
					<span class="font-mono text-xs font-bold text-primary">{priceCapPercent}% of Face Value</span>
				</div>
				<input
					type="range"
					id="price-cap"
					name="priceCapPercent"
					min="100"
					max="200"
					step="10"
					bind:value={priceCapPercent}
					class="w-full accent-primary"
				/>
				<p class="text-[10px] text-mute">Prevents sellers listing secondary market tickets higher than this markup.</p>
			</div>

			<!-- Ticket Transfer Frequency Limit -->
			<div class="space-y-2 border-b border-hairline pb-4">
				<div class="flex justify-between">
					<label for="max-transfers" class="font-sans text-sm font-semibold text-ink">Max Transfers Per Individual Ticket</label>
					<span class="font-mono text-xs font-bold text-primary">{maxTransfersPerTicket} Times</span>
				</div>
				<input
					type="range"
					id="max-transfers"
					name="maxTransfersPerTicket"
					min="1"
					max="5"
					step="1"
					bind:value={maxTransfersPerTicket}
					class="w-full accent-primary"
				/>
				<p class="text-[10px] text-mute">Limits the number of times a single ticket can be transferred before lock-in.</p>
			</div>

			<!-- TOTP Expiry Limit -->
			<div class="space-y-2 border-b border-hairline pb-4">
				<div class="flex justify-between">
					<label for="totp-expiry" class="font-sans text-sm font-semibold text-ink">Smart QR Ticket TOTP Refresh Interval</label>
					<span class="font-mono text-xs font-bold text-primary">{totpQrExpirySeconds} Seconds</span>
				</div>
				<input
					type="range"
					id="totp-expiry"
					name="totpQrExpirySeconds"
					min="15"
					max="120"
					step="5"
					bind:value={totpQrExpirySeconds}
					class="w-full accent-primary"
				/>
				<p class="text-[10px] text-mute">Controls how frequently the TOTP checking code in QR tickets regenerates to prevent screenshot-scalping.</p>
			</div>

			<!-- Toggles -->
			<div class="space-y-4">
				<h3 class="font-sans text-xs font-bold uppercase tracking-wider text-mute">Security Compliance Policies</h3>

				<div class="flex items-center justify-between py-2">
					<div>
						<h4 class="font-sans text-xs font-semibold text-ink">Seller KYC Verification Requirement</h4>
						<p class="text-[10px] text-mute mt-0.5">Enforces full identity verification before secondary listing submission.</p>
					</div>
					<div class="flex items-center">
						<input type="hidden" name="kycRequiredForSeller" value={kycRequiredForSeller ? "true" : "false"} />
						<button
							type="button"
							onclick={() => (kycRequiredForSeller = !kycRequiredForSeller)}
							class="relative inline-flex h-5 w-10 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none {kycRequiredForSeller ? 'bg-primary' : 'bg-canvas-soft-3'}"
						>
							<span
								class="pointer-events-none inline-block h-4 w-4 transform rounded-full bg-canvas shadow-sm ring-0 transition duration-200 ease-in-out {kycRequiredForSeller ? 'translate-x-5' : 'translate-x-0'}"
							></span>
						</button>
					</div>
				</div>

				<div class="flex items-center justify-between py-2">
					<div>
						<h4 class="font-sans text-xs font-semibold text-ink">Mandatory Multi-Factor Authentication (MFA)</h4>
						<p class="text-[10px] text-mute mt-0.5">Force all platform admin and staff accounts to configure authenticator apps.</p>
					</div>
					<div class="flex items-center">
						<input type="hidden" name="mfaRequiredForAdmin" value={mfaRequiredForAdmin ? "true" : "false"} />
						<button
							type="button"
							onclick={() => (mfaRequiredForAdmin = !mfaRequiredForAdmin)}
							class="relative inline-flex h-5 w-10 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none {mfaRequiredForAdmin ? 'bg-primary' : 'bg-canvas-soft-3'}"
						>
							<span
								class="pointer-events-none inline-block h-4 w-4 transform rounded-full bg-canvas shadow-sm ring-0 transition duration-200 ease-in-out {mfaRequiredForAdmin ? 'translate-x-5' : 'translate-x-0'}"
							></span>
						</button>
					</div>
				</div>
			</div>

			<div class="pt-6 border-t border-hairline">
				<button
					type="submit"
					class="cursor-pointer rounded-full bg-primary px-5 py-2 font-mono text-[11px] font-bold text-on-primary hover:bg-primary/95 transition"
				>
					SAVE SYSTEM POLICIES
				</button>
			</div>
		</form>
	</div>
</div>
