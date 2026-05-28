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
	class="flex min-h-screen flex-1 flex-col items-center justify-center bg-[#303841] p-6 text-white select-none"
>
	<div class="flex w-full max-w-md flex-col items-center space-y-8">
		<!-- Header Logo -->
		<div class="space-y-2 text-center">
			<img src="/logo.png" alt="Ticketpeak Logo" class="mx-auto h-10 brightness-0 invert" />
			<div class="text-xs font-extrabold tracking-widest text-gray-400 uppercase">
				Internal Platform Tools
			</div>
		</div>

		<!-- Obsidian dark card container -->
		<AuthCard
			title="Sign In"
			subtitle="Please enter your platform admin credentials to proceed."
			darkVariant={true}
		>
			<AuthErrorBanner message={form?.error || null} />

			<LoginForm actionUrl="?/login" {loading} darkVariant={true} />
		</AuthCard>

		<!-- Footer caption only -->
		<div class="text-center">
			<span class="text-xs font-bold tracking-wider text-gray-500 uppercase">
				Authorized Access Only
			</span>
		</div>
	</div>
</div>
