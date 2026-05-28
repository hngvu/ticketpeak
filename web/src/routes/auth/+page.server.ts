/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch } from '$lib/server/api';

export const load: PageServerLoad = async ({ url }) => {
	// If already authenticated, hook.server.ts redirects.
	// We just pass tab and redirect params to the page.
	const tab = url.searchParams.get('tab') || 'login';
	const redirectTo = url.searchParams.get('redirect') || '/discover';

	return {
		tab,
		redirectTo
	};
};

export const actions: Actions = {
	login: async ({ request, cookies, url }) => {
		const data = await request.formData();
		const email = data.get('email') as string;
		const password = data.get('password') as string;
		const redirectTo = url.searchParams.get('redirect') || '/discover';

		if (!email || !password) {
			return fail(400, {
				error: 'Email and password are required',
				email,
				tab: 'login'
			});
		}

		try {
			const res = await apiFetch<any>(fetch, '/api/auth/login', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({ email, password })
			});

			const { accessToken, refreshToken, accessTokenExpiresIn, refreshTokenExpiresIn } = res;

			// Store in httpOnly, Secure, SameSite=Strict cookies
			cookies.set('access_token', accessToken, {
				path: '/',
				httpOnly: true,
				secure: true,
				sameSite: 'strict',
				maxAge: accessTokenExpiresIn || 900
			});

			cookies.set('refresh_token', refreshToken, {
				path: '/',
				httpOnly: true,
				secure: true,
				sameSite: 'strict',
				maxAge: refreshTokenExpiresIn || 604800
			});
		} catch (err: any) {
			return fail(400, {
				error: err.message || 'Invalid email or password',
				email,
				tab: 'login'
			});
		}

		throw redirect(303, redirectTo);
	},

	register: async ({ request, cookies, url }) => {
		const data = await request.formData();
		const fullName = data.get('fullName') as string;
		const email = data.get('email') as string;
		const password = data.get('password') as string;
		const redirectTo = url.searchParams.get('redirect') || '/discover';

		if (!fullName || !email || !password) {
			return fail(400, {
				error: 'All fields are required',
				fullName,
				email,
				tab: 'register'
			});
		}

		// Split fullName into firstName and lastName
		const nameParts = fullName.trim().split(/\s+/);
		const firstName = nameParts[0] || '';
		const lastName = nameParts.slice(1).join(' ') || '';

		try {
			// 1. Register Account
			await apiFetch<any>(fetch, '/api/accounts/register', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					email,
					password,
					firstName,
					lastName
				})
			});

			// 2. Auto-login
			const loginRes = await apiFetch<any>(fetch, '/api/auth/login', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({ email, password })
			});

			const { accessToken, refreshToken, accessTokenExpiresIn, refreshTokenExpiresIn } = loginRes;

			// Store cookies
			cookies.set('access_token', accessToken, {
				path: '/',
				httpOnly: true,
				secure: true,
				sameSite: 'strict',
				maxAge: accessTokenExpiresIn || 900
			});

			cookies.set('refresh_token', refreshToken, {
				path: '/',
				httpOnly: true,
				secure: true,
				sameSite: 'strict',
				maxAge: refreshTokenExpiresIn || 604800
			});
		} catch (err: any) {
			return fail(400, {
				error: err.message || 'Registration failed. Please try again.',
				fullName,
				email,
				tab: 'register'
			});
		}

		throw redirect(303, redirectTo);
	}
};
