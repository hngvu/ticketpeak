/* eslint-disable @typescript-eslint/no-explicit-any */

import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';
import { error } from '@sveltejs/kit';
import { MOCK_VENUES, MOCK_EVENTS } from '$lib/server/mockData';

export const load: PageServerLoad = async ({ fetch, params, url }) => {
	const paramString = params.id || '';

	// Extract the UUID part (first 36 characters)
	let venueId = paramString;
	if (paramString.length > 36) {
		venueId = paramString.slice(0, 36);
	}

	// Validate UUID format or mock format
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
	const isMockId = venueId.startsWith('venue-');

	if (!uuidRegex.test(venueId) && !isMockId) {
		throw error(404, 'Invalid Venue ID');
	}

	const pageParam = url.searchParams.get('page') || '0';
	let pageNum = 0;
	try {
		pageNum = parseInt(pageParam, 10);
		if (isNaN(pageNum) || pageNum < 0) pageNum = 0;
	} catch {
		// ignore
	}

	let venue: any = null;
	let isBackendAvailable = false;

	try {
		// Fetch Venue Details
		venue = await apiFetch<any>(fetch, `/api/venues/${venueId}`);
		isBackendAvailable = true;
	} catch {
		// fallback to mock
		venue = MOCK_VENUES.find((v) => v.id === venueId);
		if (!venue) {
			// fallback by slug match
			const slugMatched = MOCK_VENUES.find((v) => paramString.includes(v.slug));
			venue = slugMatched || MOCK_VENUES[0];
		}
	}

	let showsResponse: PageResponse<any> = {
		content: [],
		pageable: {
			pageNumber: 0,
			pageSize: 12,
			sort: { empty: true, sorted: false, unsorted: true },
			offset: 0,
			unpaged: false,
			paged: true
		},
		totalElements: 0,
		totalPages: 0,
		last: true,
		size: 12,
		number: 0,
		sort: { empty: true, sorted: false, unsorted: true },
		numberOfElements: 0,
		first: true,
		empty: true
	};

	if (isBackendAvailable) {
		try {
			// Fetch hosted events at the venue
			showsResponse = await apiFetch<PageResponse<any>>(
				fetch,
				`/api/events?venueId=${venueId}&page=${pageNum}&size=12&sort=startAt,asc`
			);
		} catch {
			// ignore
		}
	}

	let finalShows = showsResponse.content || [];
	let finalTotalShows = showsResponse.totalElements || 0;
	let finalTotalPages = showsResponse.totalPages || 0;
	let finalCurrentPage = showsResponse.number || 0;

	if (finalShows.length === 0) {
		// Filter mock events matching this venue
		finalShows = MOCK_EVENTS.filter((e) => e.venueId === venue.id || e.venueName === venue.name);
		finalTotalShows = finalShows.length;
		finalTotalPages = Math.ceil(finalShows.length / 12);
		finalCurrentPage = 0;
	}

	return {
		venue,
		shows: finalShows,
		totalShows: finalTotalShows,
		currentPage: finalCurrentPage,
		totalPages: finalTotalPages
	};
};
