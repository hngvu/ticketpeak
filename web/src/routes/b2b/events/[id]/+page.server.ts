/* eslint-disable @typescript-eslint/no-explicit-any */
import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, params, cookies }) => {
	const { id } = params;
	const accessToken = cookies.get('b2b_access_token');
	if (!accessToken) {
		throw redirect(303, '/b2b/login');
	}

	try {
		const [event, offers, inventory, venuesRes, classifications, attractions, ticketTypes] =
			await Promise.all([
				apiFetch<any>(fetch, `/api/partner/events/${id}`, {
					headers: {
						Authorization: `Bearer ${accessToken}`
					}
				}),
				apiFetch<any[]>(fetch, `/api/events/${id}/offers`).catch(() => []),
				apiFetch<any>(fetch, `/api/events/${id}/inventory`).catch(() => null),
				apiFetch<PageResponse<any>>(fetch, '/api/venues?size=100').catch(
					() => ({ content: [] }) as any
				),
				apiFetch<any[]>(fetch, '/api/classifications').catch(() => []),
				apiFetch<any[]>(fetch, '/api/attractions').catch(() => []),
				apiFetch<any[]>(fetch, `/api/partner/events/${id}/ticket-types`, {
					headers: { Authorization: `Bearer ${accessToken}` }
				}).catch(() => [])
			]);

		let manifests: any[] = [];
		if (event?.venueId) {
			manifests = await apiFetch<any[]>(fetch, `/api/partner/venues/${event.venueId}/manifests`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => []);
		}

		return {
			event,
			offers,
			inventory,
			venues: venuesRes?.content || [],
			classifications,
			attractions,
			ticketTypes,
			manifests
		};
	} catch (err: any) {
		console.error('[EVENT DETAILS LOAD ERROR]:', err);
		throw redirect(303, '/b2b/events');
	}
};
