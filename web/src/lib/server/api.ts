/* eslint-disable @typescript-eslint/no-explicit-any */
import { env } from '$env/dynamic/private';

const API_BASE = env.API_BASE || 'http://localhost:8080';

export interface PageResponse<T> {
	content: T[];
	pageable: {
		pageNumber: number;
		pageSize: number;
		sort: {
			empty: boolean;
			sorted: boolean;
			unsorted: boolean;
		};
		offset: number;
		unpaged: boolean;
		paged: boolean;
	};
	totalElements: number;
	totalPages: number;
	last: boolean;
	size: number;
	number: number;
	sort: {
		empty: boolean;
		sorted: boolean;
		unsorted: boolean;
	};
	numberOfElements: number;
	first: boolean;
	empty: boolean;
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
		console.error(`[API FETCH ERROR] path: ${path}, error:`, e.message || e);
		throw e;
	}
}
