<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import AuthCard from '$lib/components/auth/AuthCard.svelte';
	import LoginForm from '$lib/components/auth/LoginForm.svelte';
	import SocialAuthButton from '$lib/components/auth/SocialAuthButton.svelte';
	import AuthErrorBanner from '$lib/components/auth/AuthErrorBanner.svelte';

	let { form } = $props();
	let loading = $state(false);
</script>

<svelte:head>
	<title>Sign In — Ticketpeak for Business</title>
</svelte:head>

<div class="flex flex-1 items-center justify-center bg-slate-50 px-6 py-20 dark:bg-slate-950">
	<div class="flex w-full max-w-md flex-col space-y-6">
		<AuthCard
			title="Ticketpeak for Business"
			subtitle="Sign in to your organizer account"
			darkVariant={false}
		>
			<AuthErrorBanner message={form?.error || null} />

			<LoginForm actionUrl="?/login" {loading} />

			<!-- Divider -->
			<div class="relative my-6 flex items-center justify-center">
				<div class="absolute inset-0 flex items-center">
					<div class="w-full border-t border-hairline"></div>
				</div>
				<span
					class="relative bg-canvas px-3 text-xs font-semibold tracking-wider text-mute uppercase"
				>
					or
				</span>
			</div>

			<!-- Google OAuth CTA -->
			<SocialAuthButton text="Continue with Google" />
		</AuthCard>

		<!-- Bottom Navigation Links -->
		<div class="flex flex-col items-center gap-3 text-sm font-medium text-slate-500">
			<p>
				Don't have an account?
				<a
					href="/auth?tab=register&redirect=/b2b/dashboard"
					class="ml-1 font-bold text-primary hover:underline"
				>
					Create organizer account →
				</a>
			</p>

			<a
				href="/"
				class="flex items-center gap-1 text-xs font-bold text-slate-400 transition hover:text-slate-600"
			>
				<span>←</span>
				<span>Back to Ticketpeak</span>
			</a>
		</div>
	</div>
</div>
