/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';
import { error } from '@sveltejs/kit';
import { decodeBase62ToUuid } from '$lib/base62';

export const load: PageServerLoad = async ({ fetch, params, locals }) => {
	const paramId = params.id || '';
	const paramSlug = params.slug || '';

	// Decode Base62 to standard UUID format
	let resolvedId = paramId;
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

	if (!uuidRegex.test(paramId)) {
		try {
			resolvedId = decodeBase62ToUuid(paramId);
		} catch {
			throw error(404, 'Invalid Event ID');
		}
	}

	if (!uuidRegex.test(resolvedId)) {
		throw error(404, 'Invalid Event ID');
	}

	try {
		// Parallel SSR fetches
		const [event, offers, inventory] = await Promise.all([
			apiFetch<any>(fetch, `/api/events/${resolvedId}`),
			apiFetch<any[]>(fetch, `/api/events/${resolvedId}/offers`).catch(() => []),
			apiFetch<any>(fetch, `/api/events/${resolvedId}/inventory`).catch(() => ({
				gaInventory: [],
				reservedInventory: []
			}))
		]);

		return {
			event,
			offers: offers || [],
			inventory: inventory || { gaInventory: [], reservedInventory: [] },
			currentUser: locals.user ?? null,
			slug: paramSlug
		};
	} catch (err) {
		console.error('[Event Load Error]:', err);
		throw error(404, 'Event not found');
	}
};
