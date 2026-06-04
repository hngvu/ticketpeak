/* eslint-disable @typescript-eslint/no-explicit-any, no-useless-assignment */

import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';
import { error } from '@sveltejs/kit';

export const load: PageServerLoad = async ({ fetch, params, url }) => {
	const paramString = params.id || '';

	// SvelteKit route matches attractions/[id]-[slug]
	// If id contains a hyphen, extract the UUID part (first 36 characters)
	let venueId = paramString;
	if (paramString.length > 36) {
		venueId = paramString.slice(0, 36);
	}

	// Validate UUID format
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
	if (!uuidRegex.test(venueId)) {
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
	try {
		// Fetch Venue Details
		venue = await apiFetch<any>(fetch, `/api/venues/${venueId}`);
	} catch (err) {
		console.error('[Venue Load Error]:', err);
		throw error(404, 'Venue not found');
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

	try {
		// Fetch hosted events at the venue
		showsResponse = await apiFetch<PageResponse<any>>(
			fetch,
			`/api/events?venueId=${venueId}&page=${pageNum}&size=12&sort=startAt,asc`
		);
	} catch (err) {
		console.error('[Venue Events Load Error]:', err);
	}

	return {
		venue,
		shows: showsResponse.content || [],
		totalShows: showsResponse.totalElements || 0,
		currentPage: showsResponse.number || 0,
		totalPages: showsResponse.totalPages || 0
	};
};
