/* eslint-disable @typescript-eslint/no-explicit-any */
import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { apiFetch } from '$lib/server/api';

export const GET: RequestHandler = async ({ url, fetch, cookies }) => {
	const params = new URLSearchParams();
	const q = url.searchParams.get('q');
	const role = url.searchParams.get('role');

	if (q) params.set('q', q);
	if (role) params.set('role', role);

	const accessToken = cookies.get('ops_access_token') || cookies.get('access_token');

	try {
		const results = await apiFetch<any[]>(fetch, `/api/ops/accounts?${params.toString()}`, {
			headers: {
				Authorization: accessToken ? `Bearer ${accessToken}` : ''
			}
		});
		return json({ success: true, data: results });
	} catch (err: any) {
		return json({ success: false, message: err.message || 'Request failed' }, { status: 500 });
	}
};
