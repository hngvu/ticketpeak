/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch } from '$lib/server/api';

export const load: PageServerLoad = async ({ locals }) => {
	// If already logged in as organizer, redirect to B2B dashboard
	if (locals.user && locals.user.role === 'ORGANIZER') {
		throw redirect(303, '/b2b/dashboard');
	}
	return {};
};

export const actions: Actions = {
	login: async ({ request, cookies }) => {
		const data = await request.formData();
		const email = data.get('email') as string;
		const password = data.get('password') as string;

		if (!email || !password) {
			return fail(400, {
				error: 'Email and password are required',
				email
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

			// Store cookies
			cookies.set('b2b_access_token', accessToken, {
				path: '/',
				httpOnly: true,
				secure: true,
				sameSite: 'strict',
				maxAge: accessTokenExpiresIn || 900
			});

			cookies.set('b2b_refresh_token', refreshToken, {
				path: '/',
				httpOnly: true,
				secure: true,
				sameSite: 'strict',
				maxAge: refreshTokenExpiresIn || 604800
			});
		} catch (err: any) {
			return fail(400, {
				error: err.message || 'Invalid organizer email or password',
				email
			});
		}

		throw redirect(303, '/b2b/dashboard');
	}
};
