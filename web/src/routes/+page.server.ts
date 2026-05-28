import type { PageServerLoad } from './$types';
import { MOCK_EVENTS, MOCK_VENUES } from '$lib/server/mockData';

export const load: PageServerLoad = async () => {
	return {
		featuredEvents: MOCK_EVENTS.slice(0, 6),
		featuredVenues: MOCK_VENUES.slice(0, 4)
	};
};
