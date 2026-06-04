/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';
import { error } from '@sveltejs/kit';
import { decodeBase62ToUuid } from '$lib/base62';

export const load: PageServerLoad = async ({ fetch, params }) => {
	const paramId = params.id || '';
	const paramSlug = params.slug || '';

	// Decode Base62 to standard UUID format
	let resolvedId = paramId;
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

	if (!uuidRegex.test(paramId)) {
		try {
			resolvedId = decodeBase62ToUuid(paramId);
		} catch {
			throw error(404, 'Invalid Attraction ID');
		}
	}

	if (!uuidRegex.test(resolvedId)) {
		throw error(404, 'Invalid Attraction ID');
	}

	try {
		// Parallel SSR fetches
		const [attraction, eventsRes] = await Promise.all([
			apiFetch<any>(fetch, `/api/attractions/${resolvedId}`),
			apiFetch<PageResponse<any>>(
				fetch,
				`/api/events?attractionId=${resolvedId}&page=0&size=10&sort=startAt,asc`
			)
		]);

		return {
			attraction,
			initialEvents: eventsRes.content || [],
			totalEvents: eventsRes.totalElements || 0,
			totalPages: eventsRes.totalPages || 0,
			slug: paramSlug
		};
	} catch (err) {
		console.error('[Artist Load Error]:', err);
		throw error(404, 'Artist not found');
	}
};
