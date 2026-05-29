/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch } from '$lib/server/api';
import { parseJwt } from '$lib/server/auth';

export const load: PageServerLoad = async ({ locals }) => {
	// If already authenticated as ADMIN, redirect to ops dashboard
	if (locals.user && locals.user.role === 'ADMIN') {
		throw redirect(303, '/ops/dashboard');
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
			// 1. Perform login fetch
			const res = await apiFetch<any>(fetch, '/api/auth/login', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({ email, password })
			});

			const { accessToken, refreshToken, accessTokenExpiresIn, refreshTokenExpiresIn } = res;

			// 2. Decode access token claims immediately to inspect role
			const parsed = parseJwt(accessToken);
			if (!parsed || parsed.role !== 'ADMIN') {
				// Immediate Session Safeguard: Invalidate the token pair immediately on the backend
				try {
					await apiFetch(fetch, '/api/auth/logout', {
						method: 'POST',
						headers: {
							'Content-Type': 'application/json'
						},
						body: JSON.stringify({ refreshToken })
					});
				} catch (logoutErr) {
					console.error('[OPS ADMIN GUARD LOGOUT FAILED]:', logoutErr);
				}

				return fail(403, {
					error: 'Access denied: Authorized administrator access only',
					email
				});
			}

			// 3. Store cookies for genuine admin accounts
			cookies.set('ops_access_token', accessToken, {
				path: '/',
				httpOnly: true,
				secure: true,
				sameSite: 'strict',
				maxAge: accessTokenExpiresIn || 900
			});

			cookies.set('ops_refresh_token', refreshToken, {
				path: '/',
				httpOnly: true,
				secure: true,
				sameSite: 'strict',
				maxAge: refreshTokenExpiresIn || 604800
			});
		} catch (err: any) {
			return fail(400, {
				error: err.message || 'Invalid administrator credentials',
				email
			});
		}

		throw redirect(303, '/ops/dashboard');
	}
};
