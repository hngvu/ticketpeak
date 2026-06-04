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

	console.log(`[API ops/accounts] Received search request: q='${q}', role='${role}'`);
	const accessToken = cookies.get('ops_access_token') || cookies.get('access_token');
	console.log(
		`[API ops/accounts] Using accessToken: ${accessToken ? 'PRESENT (starts with ' + accessToken.substring(0, 10) + '...)' : 'MISSING'}`
	);

	try {
		const results = await apiFetch<any[]>(fetch, `/api/ops/accounts?${params.toString()}`, {
			headers: {
				Authorization: accessToken ? `Bearer ${accessToken}` : ''
			}
		});
		console.log(`[API ops/accounts] Success! Found ${results?.length || 0} results`);
		return json({ success: true, data: results });
	} catch (err: any) {
		console.error(`[API ops/accounts] Fetch error:`, err.message || err);
		return json({ success: false, message: err.message || 'Request failed' }, { status: 500 });
	}
};
