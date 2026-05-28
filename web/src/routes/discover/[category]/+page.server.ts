/* eslint-disable @typescript-eslint/no-explicit-any */

import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';
import { error } from '@sveltejs/kit';
import { MOCK_CLASSIFICATIONS, MOCK_EVENTS } from '$lib/server/mockData';

export const load: PageServerLoad = async ({ fetch, params, url }) => {
	const category = params.category.toLowerCase(); // e.g. concerts, sports, arts, family
	const classificationId = url.searchParams.get('classificationId') || '';
	const pageParam = url.searchParams.get('page') || '0';

	let pageNum = 0;
	try {
		pageNum = parseInt(pageParam, 10);
		if (isNaN(pageNum) || pageNum < 0) pageNum = 0;
	} catch {
		// ignore
	}

	// Validate valid categories
	const validCategories = ['concerts', 'sports', 'arts', 'family'];
	if (!validCategories.includes(category)) {
		throw error(404, 'Category not found');
	}

	let activeSegment: any = null;
	let subGenres: any[] = [];
	let classifications = MOCK_CLASSIFICATIONS;

	try {
		// Fetch categories
		const apiClassifications = await apiFetch<any[]>(fetch, '/api/classifications');
		if (apiClassifications && apiClassifications.length > 0) {
			classifications = apiClassifications;
		}
	} catch {
		// fallback to mock
	}

	// Find Level 1 Category (e.g., Concerts)
	activeSegment = classifications.find(
		(c) => c.level === 1 && c.slug.toLowerCase().includes(category.slice(0, 4))
	);

	if (activeSegment) {
		// Find Level 2 Sub-genres under this parent segment
		subGenres = classifications.filter((c) => c.level === 2 && c.parentId === activeSegment.id);
	}

	// Determine what ID to query for events
	const targetId = classificationId || activeSegment?.id || '';

	let eventsData: PageResponse<any> = {
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

	if (targetId) {
		try {
			eventsData = await apiFetch<PageResponse<any>>(
				fetch,
				`/api/events?classificationId=${targetId}&page=${pageNum}&size=12&sort=startAt,asc`
			);
			isBackendAvailable = true;
		} catch {
			// ignore, fallback below
		}
	}

	// If classifications not seeded or no targetId, fallback to fetching all events matching text query
	if (!isBackendAvailable || eventsData.content.length === 0) {
		try {
			eventsData = await apiFetch<PageResponse<any>>(
				fetch,
				`/api/events?page=${pageNum}&size=12&sort=startAt,asc`
			);
			if (eventsData.content && eventsData.content.length > 0) {
				isBackendAvailable = true;
			}
		} catch {
			// ignore
		}
	}

	// Fallback to high-fidelity mock data if database is empty/offline
	let finalEvents = eventsData.content || [];
	let finalTotalPages = eventsData.totalPages || 0;
	let finalCurrentPage = eventsData.number || 0;

	if (!isBackendAvailable || finalEvents.length === 0) {
		// Filter mock events matching category
		const matchKeyword = category.slice(0, 4);
		finalEvents = MOCK_EVENTS.filter((e) => {
			const classSlug = e.classifications?.[0]?.slug || '';
			const matchesCategory =
				classSlug.includes(matchKeyword) ||
				(category === 'concerts' &&
					(classSlug.includes('pop') ||
						classSlug.includes('indie') ||
						classSlug.includes('edm'))) ||
				(category === 'sports' && (classSlug.includes('foot') || classSlug.includes('basket'))) ||
				(category === 'arts' &&
					(classSlug.includes('music') ||
						classSlug.includes('comedy') ||
						classSlug.includes('dance')));

			// If classificationId query param is active, narrow down to that specific sub-genre
			if (classificationId) {
				// e.g. genre-pop
				return (
					matchesCategory &&
					(classificationId.includes(classSlug) ||
						classSlug.includes(classificationId.replace('genre-', '')))
				);
			}
			return matchesCategory;
		});

		finalTotalPages = Math.ceil(finalEvents.length / 12);
		finalCurrentPage = 0;
	}

	return {
		category,
		activeSegment,
		subGenres,
		events: finalEvents,
		currentPage: finalCurrentPage,
		totalPages: finalTotalPages,
		classificationId
	};
};
