/* eslint-disable @typescript-eslint/no-explicit-any */
import { env } from '$env/dynamic/private';
import {
	MOCK_EVENT_BY_ID,
	MOCK_OFFERS_BY_EVENT_ID,
	MOCK_VENUES,
	MOCK_VENUE_BY_ID,
	MOCK_CLASSIFICATIONS,
	MOCK_EVENTS,
	MOCK_VENUE_MANIFESTS,
	MOCK_MANIFEST_DETAIL,
	mockInventoryForEvent
} from './mockData';

const API_BASE = env.API_BASE || 'http://localhost:8080';

export interface PageResponse<T> {
	content: T[];
	pageable: {
		pageNumber: number;
		pageSize: number;
		sort: { empty: boolean; sorted: boolean; unsorted: boolean };
		offset: number;
		unpaged: boolean;
		paged: boolean;
	};
	totalElements: number;
	totalPages: number;
	last: boolean;
	size: number;
	number: number;
	sort: { empty: boolean; sorted: boolean; unsorted: boolean };
	numberOfElements: number;
	first: boolean;
	empty: boolean;
}

const MOCK_PAGE = <T>(items: T[]): PageResponse<T> => ({
	content: items,
	pageable: {
		pageNumber: 0,
		pageSize: 100,
		sort: { empty: true, sorted: false, unsorted: true },
		offset: 0,
		unpaged: false,
		paged: true
	},
	totalElements: items.length,
	totalPages: 1,
	last: true,
	size: 100,
	number: 0,
	sort: { empty: true, sorted: false, unsorted: true },
	numberOfElements: items.length,
	first: true,
	empty: items.length === 0
});

async function tryMockFallback<T>(path: string, _options?: RequestInit): Promise<T | undefined> {
	const [basePath] = path.split('?');

	// GET /api/events/{id}
	const eventDetailMatch = basePath.match(/^\/api\/events\/([^/]+)$/);
	if (eventDetailMatch) {
		const evt = MOCK_EVENT_BY_ID[eventDetailMatch[1]];
		if (evt) return evt as T;
	}

	// GET /api/events/{id}/offers
	const offersMatch = basePath.match(/^\/api\/events\/([^/]+)\/offers$/);
	if (offersMatch) {
		const offers = MOCK_OFFERS_BY_EVENT_ID[offersMatch[1]];
		if (offers) return offers as T;
	}

	// GET /api/events/{id}/inventory
	const invMatch = basePath.match(/^\/api\/events\/([^/]+)\/inventory$/);
	if (invMatch) {
		return mockInventoryForEvent(invMatch[1]) as T;
	}

	// GET /api/venues (list)
	if (basePath === '/api/venues' || basePath.startsWith('/api/venues?')) {
		return MOCK_PAGE(MOCK_VENUES) as T;
	}

	// GET /api/venues/{id}
	const venueDetailMatch = basePath.match(/^\/api\/venues\/([^/]+)$/);
	if (venueDetailMatch) {
		const venue = MOCK_VENUE_BY_ID[venueDetailMatch[1]];
		if (venue) return venue as T;
	}

	// GET /api/classifications
	if (basePath === '/api/classifications') {
		return MOCK_CLASSIFICATIONS as T;
	}

	// GET /api/events (public search: exclude DRAFT)
	if (basePath === '/api/events' || basePath.startsWith('/api/events?')) {
		const publishedEvents = MOCK_EVENTS.filter((e) => e.status !== 'DRAFT');
		return MOCK_PAGE(publishedEvents) as T;
	}

	// GET /api/partner/events/{id}
	const partnerEventMatch = basePath.match(/^\/api\/partner\/events\/([^/]+)$/);
	if (partnerEventMatch) {
		const evt = MOCK_EVENT_BY_ID[partnerEventMatch[1]];
		if (evt) return evt as T;
	}

	// GET /api/partner/events (list — includes all statuses)
	if (basePath === '/api/partner/events' || basePath.startsWith('/api/partner/events?')) {
		return MOCK_PAGE(MOCK_EVENTS) as T;
	}

	// GET /api/partner/venues/{id}/manifests
	const partnerManifestMatch = basePath.match(/^\/api\/partner\/venues\/([^/]+)\/manifests$/);
	if (partnerManifestMatch) {
		const venueId = partnerManifestMatch[1];
		const manifests = MOCK_VENUE_MANIFESTS[venueId];
		if (!manifests) return [] as T;
		return manifests.map((m) => ({
			id: m.id,
			name: m.description,
			description: m.description,
			totalCapacity: m.totalCapacity,
			status: m.status,
			isDraft: m.status === 'DRAFT',
			objects: m.objects
		})) as T;
	}

	// GET /api/partner/venues/{id}/manifests/{manifestId}
	const partnerManifestDetailMatch = basePath.match(
		/^\/api\/partner\/venues\/([^/]+)\/manifests\/([^/]+)$/
	);
	if (partnerManifestDetailMatch) {
		const manifestId = partnerManifestDetailMatch[2];
		const detail = MOCK_MANIFEST_DETAIL[manifestId];
		if (!detail) return undefined;
		return {
			manifest: detail.manifest,
			levels: detail.levels,
			sections: detail.sections,
			priceLevels: detail.priceLevels,
			rsAreas: detail.rsAreas,
			gaAreas: detail.gaAreas
		} as T;
	}

	return undefined;
}

export async function apiFetch<T>(
	fetchFn: typeof fetch,
	path: string,
	options?: RequestInit
): Promise<T> {
	const url = path.startsWith('http') ? path : `${API_BASE}${path}`;

	try {
		const res = await fetchFn(url, options);
		const text = await res.text();

		if (!res.ok) {
			let errorMsg = `API Error ${res.status}: ${res.statusText}`;
			try {
				const json = JSON.parse(text);
				if (json.message) {
					errorMsg = json.message;
				}
			} catch {
				// use fallback errorMsg
			}
			throw new Error(errorMsg);
		}

		const json = JSON.parse(text);
		if (json.success === false) {
			throw new Error(json.message || 'API operation failed');
		}
		return json.data;
	} catch (e: any) {
		// Try mock fallback on any error (backend down, missing endpoint, etc.)
		const mockData = await tryMockFallback<T>(path, options);
		if (mockData !== undefined) return mockData;
		console.error(`[API FETCH ERROR] path: ${path}, error:`, e.message || e);
		throw e;
	}
}
