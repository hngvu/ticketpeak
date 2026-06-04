/* eslint-disable @typescript-eslint/no-explicit-any */
import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	try {
		const users = await apiFetch<any[]>(fetch, '/api/ops/accounts', {
			headers: {
				Authorization: `Bearer ${accessToken}`
			}
		}).catch(() => [] as any[]);

		// Transform real user models to match view format if needed
		const mappedUsers = users.map((u: any) => ({
			id: u.id,
			name: `${u.firstName || ''} ${u.lastName || ''}`.trim() || u.email.split('@')[0],
			email: u.email,
			role: u.role,
			status: u.status || 'ACTIVE',
			registeredAt: u.registeredAt || new Date().toISOString()
		}));

		return {
			users: mappedUsers
		};
	} catch (err: any) {
		console.error('[OPS USERS LOAD ERROR]:', err);
		return {
			users: [],
			error: err.message || 'Failed to load users.'
		};
	}
};
