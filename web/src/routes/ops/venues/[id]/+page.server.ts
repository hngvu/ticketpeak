/* eslint-disable @typescript-eslint/no-explicit-any */
import { redirect, error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';

export const load: PageServerLoad = async ({ params, fetch, cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	const venueId = params.id;

	try {
		const venue = await apiFetch<any>(fetch, `/api/venues/${venueId}`, {
			headers: {
				Authorization: `Bearer ${accessToken}`
			}
		});

		return { venue };
	} catch (err: any) {
		console.error('[OPS VENUE DETAIL LOAD ERROR]:', err);
		throw error(404, 'Venue not found.');
	}
};
