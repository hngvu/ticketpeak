/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';
import { error } from '@sveltejs/kit';
import { MOCK_ATTRACTIONS, MOCK_EVENTS } from '$lib/server/mockData';
import { decodeBase62ToUuid } from '$lib/base62';

export const load: PageServerLoad = async ({ fetch, params }) => {
	const paramId = params.id || '';
	const paramSlug = params.slug || '';

	// Decode Base62 to standard UUID format
	let resolvedId = paramId;
	const isMockId = paramId.startsWith('artist-') || paramId.startsWith('team-');
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

	if (!isMockId && !uuidRegex.test(paramId)) {
		try {
			resolvedId = decodeBase62ToUuid(paramId);
		} catch {
			throw error(404, 'Invalid Attraction ID');
		}
	}

	if (!uuidRegex.test(resolvedId) && !isMockId) {
		throw error(404, 'Invalid Attraction ID');
	}

	let attraction: any = null;
	let initialEventsData: PageResponse<any> = {
		content: [],
		pageable: {
			pageNumber: 0,
			pageSize: 10,
			sort: { empty: true, sorted: false, unsorted: true },
			offset: 0,
			unpaged: false,
			paged: true
		},
		totalElements: 0,
		totalPages: 0,
		last: true,
		size: 10,
		number: 0,
		sort: { empty: true, sorted: false, unsorted: true },
		numberOfElements: 0,
		first: true,
		empty: true
	};

	let isBackendAvailable = false;

	try {
		// Parallel SSR fetches
		const [attractionRes, eventsRes] = await Promise.all([
			apiFetch<any>(fetch, `/api/attractions/${resolvedId}`),
			apiFetch<PageResponse<any>>(
				fetch,
				`/api/events?attractionId=${resolvedId}&page=0&size=10&sort=startAt,asc`
			)
		]);

		attraction = attractionRes;
		initialEventsData = eventsRes;
		isBackendAvailable = true;
	} catch (err) {
		console.warn('[ADP SSR BACKEND OFFLINE OR FALLBACK]:', err);
	}

	// Dynamic fallback to high-fidelity mock data if database is empty/offline
	if (!isBackendAvailable || !attraction) {
		// Map database seeded UUIDs to mock IDs for seamless frontend testing
		let mockId = resolvedId;
		if (resolvedId === '018fbe92-bc24-7476-80db-31cccefa9b61') mockId = 'artist-denvau';
		else if (resolvedId === '018fbe92-bc24-7476-80db-31cccefa9b62') mockId = 'artist-sontung';
		else if (resolvedId === '018fbe92-bc24-7476-80db-31cccefa9b63') mockId = 'artist-taylorswift';

		// Find mock attraction matching id
		attraction = MOCK_ATTRACTIONS.find((a) => a.id === mockId);
		if (!attraction) {
			// Fallback by slug matching if id was randomly generated
			const cleanSlug = paramSlug.replace('-tickets', '');
			const slugMatched = MOCK_ATTRACTIONS.find((a) => a.slug === cleanSlug);
			attraction = slugMatched || MOCK_ATTRACTIONS[0];
		}

		// Filter mock events matching this attraction
		const mockShows = MOCK_EVENTS.filter((e) =>
			e.attractions.some((a) => a.id === attraction.id || a.slug === attraction.slug)
		);

		initialEventsData = {
			content: mockShows.slice(0, 10),
			pageable: {
				pageNumber: 0,
				pageSize: 10,
				sort: { empty: true, sorted: false, unsorted: true },
				offset: 0,
				unpaged: false,
				paged: true
			},
			totalElements: mockShows.length,
			totalPages: Math.ceil(mockShows.length / 10),
			last: mockShows.length <= 10,
			size: 10,
			number: 0,
			sort: { empty: true, sorted: false, unsorted: true },
			numberOfElements: Math.min(mockShows.length, 10),
			first: true,
			empty: mockShows.length === 0
		};
	}

	return {
		attraction,
		initialEvents: initialEventsData.content || [],
		totalEvents: initialEventsData.totalElements || 0,
		totalPages: initialEventsData.totalPages || 0,
		slug: paramSlug
	};
};
