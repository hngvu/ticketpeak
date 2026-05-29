<script lang="ts">
	import AuthCard from '$lib/components/auth/AuthCard.svelte';
	import LoginForm from '$lib/components/auth/LoginForm.svelte';
	import AuthErrorBanner from '$lib/components/auth/AuthErrorBanner.svelte';

	let { form } = $props();
	let loading = $state(false);
</script>

<svelte:head>
	<title>Sign In — Ticketpeak for Business</title>
</svelte:head>

<div class="flex flex-1 items-center justify-center bg-slate-50 px-6 py-20 dark:bg-slate-950">
	<div class="flex w-full max-w-md flex-col space-y-6">
		<AuthCard title="Ticketpeak for Business" darkVariant={false}>
			<AuthErrorBanner message={form?.error || null} />

			<LoginForm actionUrl="?/login" {loading} />
		</AuthCard>
	</div>
</div>
