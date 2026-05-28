/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';
import { MOCK_EVENTS } from '$lib/server/mockData';

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

	let isBackendAvailable = false;

	try {
		const apiPath = `/api/events?${apiParams.toString()}`;
		searchResults = await apiFetch<PageResponse<any>>(fetch, apiPath);
		if (searchResults.content && searchResults.content.length > 0) {
			isBackendAvailable = true;
		}
	} catch {
		// ignore, fallback to mock search below
	}

	let finalResults = searchResults.content || [];
	let finalTotalResults = searchResults.totalElements || 0;
	let finalTotalPages = searchResults.totalPages || 0;
	let finalCurrentPage = searchResults.number || 0;

	// Fallback to high-fidelity mock search if database is empty/offline
	if (!isBackendAvailable || finalResults.length === 0) {
		const qLower = query.toLowerCase().trim();
		const cityLower = city.toLowerCase().trim();

		finalResults = MOCK_EVENTS.filter((e) => {
			// Query keyword match
			const matchesQuery =
				!qLower ||
				e.title.toLowerCase().includes(qLower) ||
				(e.venueName && e.venueName.toLowerCase().includes(qLower)) ||
				e.classifications?.[0]?.name.toLowerCase().includes(qLower);

			// Location match
			const matchesLocation =
				!cityLower ||
				e.cityName.toLowerCase().includes(cityLower) ||
				(e.venueName && e.venueName.toLowerCase().includes(cityLower));

			// Date match
			let matchesDate = true;
			if (startDateParam) {
				const startLimit = new Date(startDateParam);
				const eventDate = new Date(e.startAt);
				matchesDate = matchesDate && eventDate >= startLimit;
			}
			if (endDateParam) {
				const endLimit = new Date(endDateParam);
				endLimit.setHours(23, 59, 59, 999);
				const eventDate = new Date(e.startAt);
				matchesDate = matchesDate && eventDate <= endLimit;
			}

			return matchesQuery && matchesLocation && matchesDate;
		});

		finalTotalResults = finalResults.length;
		finalTotalPages = Math.ceil(finalResults.length / 12);
		finalCurrentPage = 0;
	}

	return {
		query,
		location: city,
		startDate: startDateParam,
		endDate: endDateParam,
		results: finalResults,
		totalResults: finalTotalResults,
		currentPage: finalCurrentPage,
		totalPages: finalTotalPages
	};
};
