/* eslint-disable @typescript-eslint/no-explicit-any, no-useless-assignment */

import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';
import { error } from '@sveltejs/kit';

export const load: PageServerLoad = async ({ fetch, params, url }) => {
	const paramString = params.id || '';

	// SvelteKit route matches attractions/[id]-[slug]
	// If id contains a hyphen, extract the UUID part (first 36 characters)
	let attractionId = paramString;
	if (paramString.length > 36) {
		attractionId = paramString.slice(0, 36);
	}

	// Validate UUID format
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
	if (!uuidRegex.test(attractionId)) {
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
	try {
		// Fetch Attraction Info
		attraction = await apiFetch<any>(fetch, `/api/attractions/${attractionId}`);
	} catch (err) {
		console.error('[Attraction Load Error]:', err);
		throw error(404, 'Attraction not found');
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
		// Fetch attraction upcoming events
		showsResponse = await apiFetch<PageResponse<any>>(
			fetch,
			`/api/events?attractionId=${attractionId}&page=${pageNum}&size=12&sort=startAt,asc`
		);
	} catch (err) {
		console.error('[Attraction Events Load Error]:', err);
	}

	return {
		attraction,
		shows: showsResponse.content || [],
		totalShows: showsResponse.totalElements || 0,
		currentPage: showsResponse.number || 0,
		totalPages: showsResponse.totalPages || 0
	};
};
