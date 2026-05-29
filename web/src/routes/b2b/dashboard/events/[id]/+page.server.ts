/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, params, cookies }) => {
	const { id } = params;
	const accessToken = cookies.get('b2b_access_token');
	if (!accessToken) {
		throw redirect(303, '/b2b/login');
	}

	try {
		// Fetch event details, offers, inventory, venues, categories concurrently
		const [event, offers, inventory, venuesRes, classifications] = await Promise.all([
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
			apiFetch<any[]>(fetch, '/api/classifications').catch(() => [])
		]);

		return {
			event,
			offers,
			inventory,
			venues: venuesRes?.content || [],
			classifications
		};
	} catch (err: any) {
		console.error('[EVENT DETAILS LOAD ERROR]:', err);
		throw redirect(303, '/b2b/dashboard');
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
		const description = data.get('description') as string;
		const startAtStr = data.get('startAt') as string;
		const endAtStr = data.get('endAt') as string;
		const timezone = (data.get('timezone') as string) || 'Asia/Ho_Chi_Minh';
		const classificationId = data.get('classificationId') as string;

		if (!venueId || !title || !startAtStr) {
			return fail(400, { error: 'Required fields are missing' });
		}

		const startAt = new Date(startAtStr).toISOString();
		const endAt = endAtStr ? new Date(endAtStr).toISOString() : null;

		if (endAt && new Date(endAt) <= new Date(startAt)) {
			return fail(400, { error: 'End time must be strictly after start time' });
		}

		const payload = {
			venueId,
			title,
			description,
			startAt,
			endAt,
			timezone,
			restrictSingleSeat: false,
			safeTixEnabled: false,
			transferEnabled: true,
			maxTransferCount: 5,
			serviceFeePercent: 0,
			classificationIds: classificationId ? [classificationId] : [],
			attractionIds: []
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
