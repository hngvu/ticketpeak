import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, cookies }) => {
	const accessToken = cookies.get('b2b_access_token');
	if (!accessToken) {
		throw redirect(303, '/b2b/login');
	}

	try {
		// Fetch venues using the generic /api/venues endpoint
		const venuesRes = await apiFetch<PageResponse<any>>(
			fetch,
			'/api/venues?size=50&status=ACTIVE',
			{
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			}
		).catch(() => ({ content: [] }) as any);

		return {
			venues: venuesRes?.content || []
		};
	} catch (err: any) {
		console.error('[VENUES LOAD ERROR]:', err);
		return {
			venues: [],
			error: err.message || 'Failed to load venues.'
		};
	}
};
