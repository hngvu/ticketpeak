/* eslint-disable @typescript-eslint/no-explicit-any */

import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';
import { error } from '@sveltejs/kit';
import { MOCK_ATTRACTIONS, MOCK_EVENTS } from '$lib/server/mockData';

export const load: PageServerLoad = async ({ fetch, params, url }) => {
	const paramString = params.id || '';

	// SvelteKit route matches attractions/[id]-[slug]
	// If id contains a hyphen, extract the UUID part (first 36 characters)
	let attractionId = paramString;
	if (paramString.length > 36) {
		attractionId = paramString.slice(0, 36);
	}

	// Validate UUID format or mock format
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
	const isMockId = attractionId.startsWith('artist-') || attractionId.startsWith('team-');

	if (!uuidRegex.test(attractionId) && !isMockId) {
		throw error(404, 'Invalid Attraction ID');
	}

	const pageParam = url.searchParams.get('page') || '0';
	let pageNum = 0;
	try {
		pageNum = parseInt(pageParam, 10);
		if (isNaN(pageNum) || pageNum < 0) pageNum = 0;
	} catch {
		// ignore
	}

	let attraction: any = null;
	let isBackendAvailable = false;

	try {
		// Fetch Attraction Info
		attraction = await apiFetch<any>(fetch, `/api/attractions/${attractionId}`);
		isBackendAvailable = true;
	} catch {
		// fallback to mock
		attraction = MOCK_ATTRACTIONS.find((a) => a.id === attractionId);
		if (!attraction) {
			// fallback to first mock if slug matched but id was random
			const slugMatched = MOCK_ATTRACTIONS.find((a) => paramString.includes(a.slug));
			attraction = slugMatched || MOCK_ATTRACTIONS[0];
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
			// Fetch attraction upcoming events
			showsResponse = await apiFetch<PageResponse<any>>(
				fetch,
				`/api/events?attractionId=${attractionId}&page=${pageNum}&size=12&sort=startAt,asc`
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
		// Filter mock events matching this attraction
		finalShows = MOCK_EVENTS.filter((e) =>
			e.attractions.some((a) => a.id === attraction.id || a.slug === attraction.slug)
		);
		finalTotalShows = finalShows.length;
		finalTotalPages = Math.ceil(finalShows.length / 12);
		finalCurrentPage = 0;
	}

	return {
		attraction,
		shows: finalShows,
		totalShows: finalTotalShows,
		currentPage: finalCurrentPage,
		totalPages: finalTotalPages
	};
};
