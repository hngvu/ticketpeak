/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, url, cookies, parent }) => {
	const accessToken = cookies.get('b2b_access_token');
	if (!accessToken) {
		throw redirect(303, '/b2b/login');
	}

	try {
		// Get selectedOrgId from parent layout
		const parentData = await parent();
		const selectedOrgId = parentData.selectedOrgId;

		// Fetch events, venues, classifications, attractions concurrently
		const [eventsRes, venuesRes, classifications, attractions] = await Promise.all([
			apiFetch<PageResponse<any>>(
				fetch,
				`/api/partner/events?organizationId=${selectedOrgId}&size=50&sort=startAt,asc`,
				{
					headers: {
						Authorization: `Bearer ${accessToken}`
					}
				}
			).catch(() => ({ content: [] }) as any),
			apiFetch<PageResponse<any>>(fetch, '/api/venues?size=100').catch(
				() => ({ content: [] }) as any
			),
			apiFetch<any[]>(fetch, '/api/classifications').catch(() => []),
			apiFetch<any[]>(fetch, '/api/attractions').catch(() => [])
		]);

		return {
			events: eventsRes?.content || [],
			venues: venuesRes?.content || [],
			classifications,
			attractions
		};
	} catch (err: any) {
		console.error('[B2B DASHBOARD LOAD ERROR]:', err);
		return {
			events: [],
			venues: [],
			classifications: [],
			attractions: [],
			error: err.message || 'Failed to load dashboard data.'
		};
	}
};

export const actions: Actions = {
	publishEvent: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const id = data.get('id') as string;

		try {
			await apiFetch<any>(fetch, `/api/partner/events/${id}/publish`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to publish event' });
		}
	},

	startSales: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const id = data.get('id') as string;

		try {
			await apiFetch<any>(fetch, `/api/partner/events/${id}/onsale`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to start event sales' });
		}
	},

	cancelEvent: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const id = data.get('id') as string;

		try {
			await apiFetch<any>(fetch, `/api/partner/events/${id}/cancel`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to cancel event' });
		}
	},

	postponeEvent: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const id = data.get('id') as string;
		const reason = (data.get('reason') as string) || 'Unforeseen circumstances';

		try {
			await apiFetch<any>(fetch, `/api/partner/events/${id}/postpone`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({ reason })
			});
			return { success: true };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to postpone event' });
		}
	},

	resumeEvent: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const id = data.get('id') as string;

		try {
			await apiFetch<any>(fetch, `/api/partner/events/${id}/resume`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to resume event' });
		}
	},

	cloneEvent: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const id = data.get('id') as string;
		const title = data.get('title') as string;
		const startAtStr = data.get('startAt') as string;
		const endAtStr = data.get('endAt') as string;

		if (!title || !startAtStr) {
			return fail(400, { error: 'Cloned title and start date are required' });
		}

		const startAt = new Date(startAtStr).toISOString();
		const endAt = endAtStr ? new Date(endAtStr).toISOString() : null;

		try {
			await apiFetch<any>(fetch, `/api/partner/events/${id}/clone`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({ title, startAt, endAt })
			});
			return { success: true, message: 'Event cloned successfully' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to clone event' });
		}
	}
};
