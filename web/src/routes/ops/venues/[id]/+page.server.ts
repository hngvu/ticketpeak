/* eslint-disable @typescript-eslint/no-explicit-any */
import { redirect, error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';
import { MOCK_VENUE_BY_ID } from '$lib/server/mockData';

export const load: PageServerLoad = async ({ params, fetch, cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	const venueId = params.id;

	const mockVenue = MOCK_VENUE_BY_ID[venueId];
	if (mockVenue) {
		return { venue: mockVenue };
	}

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
