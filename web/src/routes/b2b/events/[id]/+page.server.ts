/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';
import { MOCK_CLASSIFICATIONS } from '$lib/server/mockData';

export const load: PageServerLoad = async ({ fetch, params, cookies }) => {
	const { id } = params;
	const accessToken = cookies.get('b2b_access_token');
	if (!accessToken) {
		throw redirect(303, '/b2b/login');
	}

	try {
		// Fetch event details, offers, inventory, venues, categories, attractions concurrently
		const [event, offers, inventory, venuesRes, classifications, attractions] = await Promise.all([
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
			apiFetch<any[]>(fetch, '/api/classifications')
				.then((res) => (res && res.length > 0 ? res : [...MOCK_CLASSIFICATIONS]))
				.catch(() => [...MOCK_CLASSIFICATIONS]),
			apiFetch<any[]>(fetch, '/api/attractions').catch(() => [])
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
			manifests
		};
	} catch (err: any) {
		console.error('[EVENT DETAILS LOAD ERROR]:', err);
		throw redirect(303, '/b2b/events');
	}
};

export const actions: Actions = {
	updateEvent: async ({ params, request, cookies, fetch }) => {
		const { id } = params;
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const venueId = data.get('venueId') as string;
		const title = data.get('title') as string;
		const slug = data.get('slug') as string;
		const startAtStr = data.get('startAt') as string;
		const timezone = (data.get('timezone') as string) || 'Asia/Ho_Chi_Minh';
		const classificationId = data.get('classificationId') as string;
		const attractionIds = data.getAll('attractionIds') as string[];

		if (!venueId || !title || !startAtStr) {
			return fail(400, { error: 'Required fields are missing' });
		}

		const startAt = new Date(startAtStr).toISOString();

		const payload = {
			venueId,
			title,
			slug: slug || '',
			startAt,
			timezone,
			restrictSingleSeat: false,
			safeTixEnabled: false,
			transferEnabled: true,
			maxTransferCount: 5,
			serviceFeePercent: 0,
			classificationIds: classificationId ? [classificationId] : [],
			attractionIds
		};

		try {
			const updatedEvent = await apiFetch<any>(fetch, `/api/partner/events/${id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify(payload)
			});
			return { success: true, event: updatedEvent };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to update event' });
		}
	}
};
