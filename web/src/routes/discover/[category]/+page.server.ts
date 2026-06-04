/* eslint-disable @typescript-eslint/no-explicit-any */

import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';
import { error } from '@sveltejs/kit';

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
	let classifications: any[] = [];

	try {
		// Fetch categories
		const apiClassifications = await apiFetch<any[]>(fetch, '/api/classifications');
		if (apiClassifications && apiClassifications.length > 0) {
			classifications = apiClassifications;
		}
	} catch (err) {
		console.error('[Discover Category Classifications Load Error]:', err);
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

	if (targetId) {
		try {
			eventsData = await apiFetch<PageResponse<any>>(
				fetch,
				`/api/events?classificationId=${targetId}&page=${pageNum}&size=12&sort=startAt,asc`
			);
		} catch (err) {
			console.error('[Discover Category Events Load Error]:', err);
		}
	} else {
		try {
			eventsData = await apiFetch<PageResponse<any>>(
				fetch,
				`/api/events?page=${pageNum}&size=12&sort=startAt,asc`
			);
		} catch (err) {
			console.error('[Discover Category All Events Load Error]:', err);
		}
	}

	return {
		category,
		activeSegment,
		subGenres,
		events: eventsData.content || [],
		currentPage: eventsData.number || 0,
		totalPages: eventsData.totalPages || 0,
		classificationId
	};
};
