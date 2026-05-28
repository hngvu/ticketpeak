import { redirect } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { apiFetch } from '$lib/server/api';

export const POST: RequestHandler = async ({ cookies }) => {
	const refreshToken = cookies.get('refresh_token');

	if (refreshToken) {
		try {
			await apiFetch(fetch, '/api/auth/logout', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({ refreshToken })
			});
		} catch (err) {
			console.error('[LOGOUT ENDPOINT ERROR]:', err);
		}
	}

	// Always clear cookies locally even if backend call fails
	cookies.delete('access_token', { path: '/' });
	cookies.delete('refresh_token', { path: '/' });

	throw redirect(303, '/');
};
