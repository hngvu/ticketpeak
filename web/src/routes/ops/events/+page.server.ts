/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	try {
		const eventsRes = await apiFetch<PageResponse<any>>(fetch, '/api/ops/events?size=100&sort=startAt,asc', {
			headers: {
				Authorization: `Bearer ${accessToken}`
			}
		}).catch(() => ({ content: [] }) as any);

		return {
			events: eventsRes?.content || []
		};
	} catch (err: any) {
		console.error('[OPS EVENTS LOAD ERROR]:', err);
		return {
			events: [],
			error: err.message || 'Failed to load events.'
		};
	}
};

export const actions: Actions = {
	publishEvent: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;

		try {
			await apiFetch<any>(fetch, `/api/ops/events/${id}/publish`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true, message: 'Event approved and published.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to publish event.' });
		}
	},

	startSales: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;

		try {
			await apiFetch<any>(fetch, `/api/ops/events/${id}/onsale`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true, message: 'Event ticket sales started.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to start ticket sales.' });
		}
	},

	cancelEvent: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;

		try {
			await apiFetch<any>(fetch, `/api/ops/events/${id}/cancel`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true, message: 'Event cancelled.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to cancel event.' });
		}
	},

	postponeEvent: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;
		const reason = (data.get('reason') as string) || 'Scheduling conflicts.';

		try {
			await apiFetch<any>(fetch, `/api/ops/events/${id}/postpone`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({ reason })
			});
			return { success: true, message: 'Event postponed.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to postpone event.' });
		}
	}
};
