/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, url }) => {
	const query = url.searchParams.get('q') || '';
	const city = url.searchParams.get('location') || '';
	const startDateParam = url.searchParams.get('startDate') || '';
	const endDateParam = url.searchParams.get('endDate') || '';
	const pageParam = url.searchParams.get('page') || '0';

	let pageNum = 0;
	try {
		pageNum = parseInt(pageParam, 10);
		if (isNaN(pageNum) || pageNum < 0) pageNum = 0;
	} catch {
		// ignore
	}

	// Prepare API request parameters
	const apiParams = new URLSearchParams();
	apiParams.set('page', String(pageNum));
	apiParams.set('size', '12');
	apiParams.set('sort', 'startAt,asc');

	if (query.trim()) {
		apiParams.set('query', query.trim());
	}
	if (city.trim()) {
		apiParams.set('city', city.trim());
	}

	// Dates conversions
	if (startDateParam) {
		try {
			const startIso = new Date(startDateParam).toISOString();
			apiParams.set('startAfter', startIso);
		} catch {
			// ignore invalid dates
		}
	}
	if (endDateParam) {
		try {
			const endDate = new Date(endDateParam);
			endDate.setHours(23, 59, 59, 999);
			apiParams.set('startBefore', endDate.toISOString());
		} catch {
			// ignore
		}
	}

	let searchResults: PageResponse<any> = {
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
		const apiPath = `/api/events?${apiParams.toString()}`;
		searchResults = await apiFetch<PageResponse<any>>(fetch, apiPath);
	} catch (err) {
		console.error('[Search Load Error]:', err);
	}

	let suggestions: any[] = [];
	if (query.trim()) {
		try {
			const sugParams = new URLSearchParams();
			sugParams.set('query', query.trim());
			const sugRes = await apiFetch<any[]>(fetch, `/api/attractions?${sugParams.toString()}`);
			suggestions = sugRes || [];
		} catch {
			suggestions = [];
		}
	}

	return {
		query,
		location: city,
		startDate: startDateParam,
		endDate: endDateParam,
		results: searchResults.content || [],
		totalResults: searchResults.totalElements || 0,
		currentPage: searchResults.number || 0,
		totalPages: searchResults.totalPages || 0,
		suggestions
	};
};
