<script lang="ts">
	import AuthCard from '$lib/components/auth/AuthCard.svelte';
	import LoginForm from '$lib/components/auth/LoginForm.svelte';
	import AuthErrorBanner from '$lib/components/auth/AuthErrorBanner.svelte';

	let { form } = $props();
	let loading = $state(false);
</script>

<svelte:head>
	<title>Internal Access — Ticketpeak Ops</title>
</svelte:head>

<div
	class="relative flex min-h-screen flex-1 flex-col items-center justify-center overflow-hidden bg-slate-50 p-6 text-slate-900 select-none"
>
	<!-- Ambient Glow Backgrounds -->
	<div
		class="pointer-events-none absolute -top-48 -left-48 h-96 w-96 rounded-full bg-primary/5 blur-[120px]"
	></div>
	<div
		class="pointer-events-none absolute -right-48 -bottom-48 h-96 w-96 rounded-full bg-primary/5 blur-[120px]"
	></div>

	<div class="relative z-10 flex w-full max-w-md flex-col items-center space-y-8">
		<!-- Header Logo -->
		<div class="text-center">
			<img src="/logo.png" alt="Ticketpeak Logo" class="mx-auto h-12" />
		</div>

		<!-- White card container -->
		<AuthCard title="Sign In" darkVariant={false}>
			<AuthErrorBanner message={form?.error || null} />

			<LoginForm actionUrl="?/login" {loading} darkVariant={false} showArrow={false} />
		</AuthCard>
	</div>
</div>
