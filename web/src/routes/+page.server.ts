/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch }) => {
	try {
		const [eventsPage, venuesPage] = await Promise.all([
			apiFetch<any>(fetch, '/api/events?size=6').catch(() => ({ content: [] })),
			apiFetch<any>(fetch, '/api/venues?size=4').catch(() => ({ content: [] }))
		]);

		return {
			featuredEvents: eventsPage?.content || [],
			featuredVenues: venuesPage?.content || []
		};
	} catch (err) {
		console.error('[Home Load Error]:', err);
		return {
			featuredEvents: [],
			featuredVenues: []
		};
	}
};
