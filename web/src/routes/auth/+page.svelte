<script lang="ts">
	/* eslint-disable svelte/no-navigation-without-resolve */
	import AuthErrorBanner from '$lib/components/auth/AuthErrorBanner.svelte';

	let { data, form } = $props();

	// Step control: 'email' | 'signin' | 'signup'
	let step = $state<'email' | 'signin' | 'signup'>('email');
	let email = $state(form?.email || '');
	let password = $state('');
	let showPassword = $state(false);

	// High fidelity registration fields
	let firstName = $state('');
	let lastName = $state('');
	let countryCode = $state('VN');
	let zipCode = $state('');
	let agreeTerms = $state(false);

	let emailError = $state('');
	let passwordError = $state('');
	let firstNameError = $state('');
	let lastNameError = $state('');
	let zipError = $state('');
	let checkingEmail = $state(false);

	// Sync form states from action result
	$effect(() => {
		if (form?.error) {
			if (form.tab === 'register') {
				step = 'signup';
			} else {
				step = 'signin';
			}
		}
	});

	const isEmailValid = $derived(/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim()));

	// High-fidelity validation check for active "Next" button in Sign Up step
	const isSignUpFormComplete = $derived(
		password.length >= 8 &&
			firstName.trim().length > 0 &&
			lastName.trim().length > 0 &&
			zipCode.trim().length > 0 &&
			agreeTerms
	);

	async function handleContinue() {
		if (!email.trim()) {
			emailError = 'Email is required';
			return;
		}
		if (!isEmailValid) {
			emailError = 'Please enter a valid email address';
			return;
		}
		emailError = '';
		checkingEmail = true;

		try {
			// Call the backend endpoint to check if account exists
			const response = await fetch(
				`/api/accounts/exists?email=${encodeURIComponent(email.trim())}`
			);
			if (response.ok) {
				const json = await response.json();
				if (json.success && json.data === true) {
					// Account exists -> Sign In form
					step = 'signin';
				} else {
					// Account does NOT exist -> High fidelity Sign Up form
					step = 'signup';
				}
			} else {
				// Fallback to signin if backend check fails
				step = 'signin';
			}
		} catch (err) {
			console.error('[EMAIL CHECK ERROR]:', err);
			step = 'signin';
		} finally {
			checkingEmail = false;
		}
	}

	function validateSignIn() {
		let valid = true;
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

	function validateSignUp() {
		let valid = true;
		if (!password || password.length < 8) {
			passwordError = 'Password must be at least 8 characters';
			valid = false;
		} else {
			passwordError = '';
		}

		if (!firstName.trim()) {
			firstNameError = 'First name is required';
			valid = false;
		} else {
			firstNameError = '';
		}

		if (!lastName.trim()) {
			lastNameError = 'Last name is required';
			valid = false;
		} else {
			lastNameError = '';
		}

		if (!zipCode.trim()) {
			zipError = 'Zip/Postal code is required';
			valid = false;
		} else {
			zipError = '';
		}

		if (!agreeTerms) {
			valid = false;
		}

		return valid;
	}
</script>

<svelte:head>
	<title>Sign In or Create Account | Ticketpeak</title>
</svelte:head>

<main class="relative flex min-h-screen bg-canvas select-none">
	<!-- Left Side: Hero Section (Desktop only - 100% Ticketmaster style match) -->
	<section
		class="relative hidden w-[35%] flex-col justify-between bg-[#111111] p-16 text-white lg:flex"
	>
		<!-- Welcome Content -->
		<div class="relative z-10 space-y-8 pt-8">
			<div class="space-y-4">
				<h1 class="text-5xl font-bold tracking-tight text-white uppercase">Welcome</h1>
				<!-- TM iconic 3px thick blue line accent -->
				<div class="h-[3px] w-14 bg-[#026CDF]"></div>
			</div>

			<p class="text-lg leading-relaxed font-normal text-gray-300">
				Discover millions of events, get alerts about your favorite artists, teams, plays and more —
				plus always- secure, effortless ticketing.
			</p>
		</div>

		<!-- Stylized Ticketpeak Logo at bottom-left -->
		<div class="relative z-10 flex items-center">
			<a href="/" class="transition duration-150 hover:opacity-80">
				<img
					src="/logo.png"
					alt="Ticketpeak Logo"
					class="h-14 object-contain brightness-0 invert"
				/>
			</a>
		</div>
	</section>

	<!-- Right Side: Unified Auth Panel -->
	<section class="flex flex-1 flex-col items-center justify-center px-6 py-12 lg:p-16">
		<div class="w-full max-w-[460px] space-y-6">
			<!-- Banner Alerts -->
			<AuthErrorBanner message={form?.error || null} />

			{#if step === 'email'}
				<!-- Step 1: Email Entry (Standard TM style) -->
				<div class="space-y-2">
					<h2 class="text-[26px] font-bold tracking-tight text-[#111] uppercase">
						Sign In or Create Account
					</h2>
					<p class="text-sm leading-snug font-normal text-gray-500">
						If you don't have an account you will be prompted to create one.
					</p>
				</div>

				<div class="space-y-5 pt-4">
					<div>
						<label
							for="email"
							class="mb-2 block text-xs font-semibold tracking-wider text-gray-700 uppercase"
						>
							Email Address
						</label>
						<input
							type="email"
							id="email"
							bind:value={email}
							placeholder="name@example.com"
							class="h-11 w-full rounded-md border border-gray-300 px-4 text-sm font-medium text-body placeholder-gray-400 outline-hidden transition focus:border-[#026CDF] focus:ring-2 focus:ring-[#026CDF]/10"
						/>
						{#if emailError}
							<span class="mt-1.5 block text-xs font-semibold text-red-500">{emailError}</span>
						{/if}
					</div>

					<button
						type="button"
						onclick={handleContinue}
						disabled={checkingEmail}
						class="flex h-11 w-full cursor-pointer items-center justify-center rounded-md text-sm font-bold tracking-wide transition-all duration-150
							{isEmailValid && !checkingEmail
							? 'bg-[#026CDF] text-white hover:bg-[#0156b3] active:scale-[0.98]'
							: 'cursor-not-allowed bg-[#EDEDED] text-gray-400'}"
					>
						{#if checkingEmail}
							<svg
								class="h-5 w-5 animate-spin text-gray-400"
								xmlns="http://www.w3.org/2000/svg"
								fill="none"
								viewBox="0 0 24 24"
							>
								<circle
									class="opacity-25"
									cx="12"
									cy="12"
									r="10"
									stroke="currentColor"
									stroke-width="4"
								></circle>
								<path
									class="opacity-75"
									fill="currentColor"
									d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
								></path>
							</svg>
						{:else}
							Continue
						{/if}
					</button>

					<!-- Standard TM Policy footer info -->
					<div
						class="border-t border-gray-100 pt-6 text-[11px] leading-relaxed font-medium text-gray-500"
					>
						<p>
							By continuing past this page, I acknowledge that I have read and agree to the current
							<a href="/terms" class="font-bold text-[#026CDF] hover:underline">Terms of Use</a>,
							including the arbitration agreement and class action waiver, and understand that
							information will be used as described in our
							<a href="/privacy" class="font-bold text-[#026CDF] hover:underline">Privacy Policy</a
							>.
						</p>
						<p class="mt-3">
							We may use your information for email marketing, including promotions and updates on
							our own or third-party products. You can opt out of our marketing emails anytime.
						</p>
					</div>
				</div>
			{:else if step === 'signin'}
				<!-- Step 2A: Enter Password for Sign In -->
				<div class="space-y-2">
					<button
						type="button"
						onclick={() => (step = 'email')}
						class="flex cursor-pointer items-center gap-1.5 text-xs font-bold text-gray-400 transition hover:text-[#026CDF]"
					>
						<span>←</span>
						<span>Change Email ({email})</span>
					</button>
					<h2 class="pt-2 text-2xl font-bold tracking-tight text-[#111] uppercase">
						Welcome Back!
					</h2>
					<p class="text-sm font-normal text-gray-500">Please enter your password to sign in.</p>
				</div>

				<form
					method="POST"
					action="?/login&redirect={encodeURIComponent(data.redirectTo)}"
					onsubmit={(e) => {
						if (!validateSignIn()) e.preventDefault();
					}}
					class="space-y-5 pt-4"
				>
					<input type="hidden" name="email" value={email} />

					<div>
						<div class="mb-2 flex items-center justify-between">
							<label
								for="password"
								class="block text-xs font-semibold tracking-wider text-gray-700 uppercase"
							>
								Password
							</label>
							<a href="/forgot" class="text-xs font-bold text-[#026CDF] hover:underline">
								Forgot password?
							</a>
						</div>
						<div class="relative">
							<input
								type={showPassword ? 'text' : 'password'}
								name="password"
								id="password"
								bind:value={password}
								placeholder="••••••••"
								class="h-11 w-full rounded-md border border-gray-300 pr-10 pl-4 text-sm font-medium text-body placeholder-gray-400 outline-hidden transition focus:border-[#026CDF] focus:ring-2 focus:ring-[#026CDF]/10"
							/>
							<button
								type="button"
								onclick={() => (showPassword = !showPassword)}
								class="absolute top-1/2 right-3.5 -translate-y-1/2 cursor-pointer text-gray-400 transition hover:text-gray-600"
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
							<span class="mt-1.5 block text-xs font-semibold text-red-500">{passwordError}</span>
						{/if}
					</div>

					<button
						type="submit"
						class="flex h-11 w-full cursor-pointer items-center justify-center rounded-md bg-[#026CDF] text-sm font-bold tracking-wide text-white transition hover:bg-[#0156b3] active:scale-[0.98]"
					>
						Sign In
					</button>

					<div class="border-t border-gray-100 pt-4 text-center">
						<p class="text-xs font-semibold text-gray-500">
							Don't have an account?
							<button
								type="button"
								onclick={() => {
									step = 'signup';
									passwordError = '';
								}}
								class="ml-1 cursor-pointer font-bold text-[#026CDF] hover:underline"
							>
								Create an account instead →
							</button>
						</p>
					</div>
				</form>
			{:else if step === 'signup'}
				<!-- Step 2B: High Fidelity Sign Up Form (100% TM visual match) -->
				<div class="space-y-4">
					<h2 class="text-[26px] font-bold tracking-tight text-[#111] uppercase">Sign Up</h2>

					<!-- Envelope Icon and Email display badge -->
					<div
						class="flex items-center gap-3 rounded-lg border border-gray-100 bg-gray-50 px-4 py-3"
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
							stroke-width="2"
							stroke="currentColor"
							class="h-5 w-5 text-gray-500"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0l-7.5-4.615m19.5 0v3c0 .621-.504 1.125-1.125 1.125h-17.25A1.125 1.125 0 013 16.125v-3"
							/>
						</svg>
						<span class="text-sm font-medium text-slate-700">{email}</span>
					</div>
				</div>

				<form
					method="POST"
					action="?/register&redirect={encodeURIComponent(data.redirectTo)}"
					onsubmit={(e) => {
						if (!validateSignUp()) e.preventDefault();
					}}
					class="space-y-4 pt-2"
				>
					<input type="hidden" name="email" value={email} />

					<!-- Password field with eye toggle -->
					<div>
						<label
							for="reg-password"
							class="mb-2 block text-xs font-semibold tracking-wider text-gray-700 uppercase"
						>
							Password
						</label>
						<div class="relative">
							<input
								type={showPassword ? 'text' : 'password'}
								name="password"
								id="reg-password"
								bind:value={password}
								placeholder="••••••••"
								class="h-11 w-full rounded-md border border-gray-300 pr-10 pl-4 text-sm font-medium text-body placeholder-gray-400 outline-hidden transition focus:border-[#026CDF] focus:ring-2 focus:ring-[#026CDF]/10"
							/>
							<button
								type="button"
								onclick={() => (showPassword = !showPassword)}
								class="absolute top-1/2 right-3.5 -translate-y-1/2 cursor-pointer text-gray-400 transition hover:text-gray-600"
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
							<span class="mt-1.5 block text-xs font-semibold text-red-500">{passwordError}</span>
						{/if}
					</div>

					<!-- Horizontal grid for First Name and Last Name -->
					<div class="grid grid-cols-2 gap-4">
						<div>
							<label
								for="firstName"
								class="mb-2 block text-xs font-semibold tracking-wider text-gray-700 uppercase"
							>
								First Name
							</label>
							<input
								type="text"
								name="fullName"
								id="firstName"
								bind:value={firstName}
								placeholder="First name"
								class="h-11 w-full rounded-md border border-gray-300 px-4 text-sm font-medium text-body placeholder-gray-400 outline-hidden transition focus:border-[#026CDF] focus:ring-2 focus:ring-[#026CDF]/10"
							/>
							{#if firstNameError}
								<span class="mt-1.5 block text-xs font-semibold text-red-500">{firstNameError}</span
								>
							{/if}
						</div>
						<div>
							<label
								for="lastName"
								class="mb-2 block text-xs font-semibold tracking-wider text-gray-700 uppercase"
							>
								Last Name
							</label>
							<input
								type="text"
								id="lastName"
								bind:value={lastName}
								placeholder="Last name"
								class="h-11 w-full rounded-md border border-gray-300 px-4 text-sm font-medium text-body placeholder-gray-400 outline-hidden transition focus:border-[#026CDF] focus:ring-2 focus:ring-[#026CDF]/10"
							/>
							{#if lastNameError}
								<span class="mt-1.5 block text-xs font-semibold text-red-500">{lastNameError}</span>
							{/if}
						</div>
					</div>

					<!-- Custom First + Last Name merger for API submission -->
					<input type="hidden" name="fullName" value="{firstName} {lastName}" />

					<!-- Horizontal grid for Country and Zip Code -->
					<div class="grid grid-cols-2 gap-4">
						<div>
							<label
								for="countryCode"
								class="mb-2 block text-xs font-semibold tracking-wider text-gray-700 uppercase"
							>
								Country of Residence
							</label>
							<div class="relative">
								<select
									id="countryCode"
									name="countryCode"
									bind:value={countryCode}
									class="h-11 w-full appearance-none rounded-md border border-gray-300 bg-white px-4 pr-8 text-sm font-medium text-body outline-hidden transition focus:border-[#026CDF] focus:ring-2 focus:ring-[#026CDF]/10"
								>
									<option value="VN">Việt Nam</option>
									<option value="US">United States</option>
									<option value="AU">Australia</option>
									<option value="GB">United Kingdom</option>
								</select>
								<!-- custom dropdown arrow -->
								<div
									class="pointer-events-none absolute inset-y-0 right-3 flex items-center text-gray-500"
								>
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
											d="M19.5 8.25l-7.5 7.5-7.5-7.5"
										/>
									</svg>
								</div>
							</div>
						</div>
						<div>
							<label
								for="zipCode"
								class="mb-2 block text-xs font-semibold tracking-wider text-gray-700 uppercase"
							>
								Zip/Postal Code
							</label>
							<input
								type="text"
								id="zipCode"
								bind:value={zipCode}
								placeholder="Zip code"
								class="h-11 w-full rounded-md border border-gray-300 px-4 text-sm font-medium text-body placeholder-gray-400 outline-hidden transition focus:border-[#026CDF] focus:ring-2 focus:ring-[#026CDF]/10"
							/>
							{#if zipError}
								<span class="mt-1.5 block text-xs font-semibold text-red-500">{zipError}</span>
							{/if}
						</div>
					</div>

					<!-- High fidelity agreement checkbox -->
					<div class="flex items-start gap-3.5 pt-2">
						<input
							type="checkbox"
							id="agreeTerms"
							bind:checked={agreeTerms}
							class="mt-1 h-5 w-5 shrink-0 cursor-pointer rounded-sm border-gray-300 text-[#026CDF] transition focus:ring-[#026CDF]/20"
						/>
						<label
							for="agreeTerms"
							class="cursor-pointer text-[12px] leading-snug font-normal text-gray-600 select-none"
						>
							I acknowledge that I have read and agree to the current
							<a href="/terms" class="font-bold text-[#026CDF] hover:underline">Terms of Use</a>,
							including the arbitration agreement and class action waiver, and understand that
							information will be used as described in our
							<a href="/privacy" class="font-bold text-[#026CDF] hover:underline">Privacy Policy</a
							>.
						</label>
					</div>

					<button
						type="submit"
						disabled={!isSignUpFormComplete}
						class="flex h-11 w-full cursor-pointer items-center justify-center rounded-md text-sm font-bold tracking-wide transition duration-150
							{isSignUpFormComplete
							? 'bg-[#026CDF] text-white hover:bg-[#0156b3] active:scale-[0.98]'
							: 'cursor-not-allowed bg-[#EDEDED] text-[#A0A0A0]'}"
					>
						Next
					</button>

					<button
						type="button"
						onclick={() => (step = 'email')}
						class="mt-2 flex h-11 w-full cursor-pointer items-center justify-center text-sm font-bold text-[#026CDF] transition hover:text-[#0156b3]"
					>
						Back
					</button>
				</form>
			{/if}
		</div>
	</section>
</main>
