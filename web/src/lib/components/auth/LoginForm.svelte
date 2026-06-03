<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	let {
		actionUrl = '?/login',
		loading = false,
		darkVariant = false,
		showArrow = true
	}: {
		actionUrl?: string;
		loading?: boolean;
		darkVariant?: boolean;
		showArrow?: boolean;
	} = $props();

	let email = $state('');
	let password = $state('');
	let showPassword = $state(false);

	let emailError = $state('');
	let passwordError = $state('');

	function validate() {
		let valid = true;
		if (!email.trim()) {
			emailError = 'Email is required';
			valid = false;
		} else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
			emailError = 'Please enter a valid email address';
			valid = false;
		} else {
			emailError = '';
		}

		if (!password) {
			passwordError = 'Password is required';
			valid = false;
		} else if (password.length < 8) {
			passwordError = 'Password must be at least 8 characters';
			valid = false;
		} else {
			passwordError = '';
		}

		return valid;
	}

	function handleSubmit(e: SubmitEvent) {
		if (!validate()) {
			e.preventDefault();
		}
	}
</script>

<form method="POST" action={actionUrl} onsubmit={handleSubmit} class="space-y-7 text-left">
	<!-- Email field -->
	<div>
		<label
			for="email"
			class="mb-2.5 block text-xs font-semibold tracking-wide {darkVariant
				? 'text-gray-300'
				: 'text-slate-700'}"
		>
			Email
		</label>
		<input
			type="email"
			name="email"
			id="email"
			bind:value={email}
			placeholder="name@example.com"
			class="h-11 w-full rounded-md border px-3.5 text-sm outline-hidden transition duration-200 focus:ring-2 focus:ring-primary/20
				{darkVariant
				? 'border-zinc-700 bg-zinc-800 text-white placeholder-zinc-500 focus:border-primary'
				: 'border-hairline bg-canvas text-body placeholder-mute focus:border-primary'}"
		/>
		{#if emailError}
			<span class="mt-1 block text-xs font-medium text-red-500">{emailError}</span>
		{/if}
	</div>

	<!-- Password field -->
	<div>
		<div class="mb-2.5 flex items-center justify-between">
			<label
				for="password"
				class="block text-xs font-semibold tracking-wide {darkVariant
					? 'text-gray-300'
					: 'text-slate-700'}"
			>
				Password
			</label>
			{#if !darkVariant}
				<a
					href="/forgot-password"
					tabindex="-1"
					class="text-xs font-bold text-primary hover:underline"
				>
					Forgot password?
				</a>
			{/if}
		</div>

		<div class="relative">
			<input
				type={showPassword ? 'text' : 'password'}
				name="password"
				id="password"
				bind:value={password}
				placeholder="••••••••"
				class="h-11 w-full rounded-md border pr-10 pl-3.5 text-sm outline-hidden transition duration-200 focus:ring-2 focus:ring-primary/20
					{darkVariant
					? 'border-zinc-700 bg-zinc-800 text-white placeholder-zinc-500 focus:border-primary'
					: 'border-hairline bg-canvas text-body placeholder-mute focus:border-primary'}"
			/>
			<button
				type="button"
				onclick={() => (showPassword = !showPassword)}
				class="absolute top-1/2 right-3.5 -translate-y-1/2 cursor-pointer text-mute transition hover:text-body"
			>
				{#if showPassword}
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="2"
						stroke="currentColor"
						class="h-4.5 w-4.5"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88"
						/>
					</svg>
				{:else}
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="2"
						stroke="currentColor"
						class="h-4.5 w-4.5"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"
						/>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
						/>
					</svg>
				{/if}
			</button>
		</div>
		{#if passwordError}
			<span class="mt-1 block text-xs font-medium text-red-500">{passwordError}</span>
		{/if}
	</div>

	<!-- Submit CTA -->
	<button
		type="submit"
		disabled={loading}
		class="hover:bg-primary-hover mt-10 flex h-11 w-full cursor-pointer items-center justify-center gap-2 rounded-md bg-primary text-sm font-bold tracking-wide text-white transition duration-150 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60"
	>
		{#if loading}
			<svg
				class="h-5 w-5 animate-spin text-white"
				xmlns="http://www.w3.org/2000/svg"
				fill="none"
				viewBox="0 0 24 24"
			>
				<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
				></circle>
				<path
					class="opacity-75"
					fill="currentColor"
					d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
				></path>
			</svg>
			<span>Signing in...</span>
		{:else}
			<span>Sign in</span>
			{#if showArrow}
				<svg
					xmlns="http://www.w3.org/2000/svg"
					fill="none"
					viewBox="0 0 24 24"
					stroke-width="2.5"
					stroke="currentColor"
					class="h-4 w-4"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"
					/>
				</svg>
			{/if}
		{/if}
	</button>
</form>
