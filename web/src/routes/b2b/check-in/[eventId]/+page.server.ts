/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, params, cookies }) => {
	const { eventId } = params;
	const accessToken = cookies.get('b2b_access_token');
	if (!accessToken) {
		throw redirect(303, '/b2b/login');
	}

	try {
		// Fetch event details and check-in tickets concurrently
		const [event, tickets] = await Promise.all([
			apiFetch<any>(fetch, `/api/partner/events/${eventId}`, {
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			}),
			apiFetch<any[]>(fetch, `/api/partner/events/${eventId}/check-in/tickets`, {
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			}).catch(() => [])
		]);

		return {
			event,
			tickets
		};
	} catch (err: any) {
		console.error('[CHECK-IN LOAD ERROR]:', err);
		throw redirect(303, '/b2b/events');
	}
};

export const actions: Actions = {
	checkIn: async ({ params, request, cookies, fetch }) => {
		const { eventId } = params;
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const qrPayload = data.get('qrPayload') as string;

		if (!qrPayload) {
			return fail(400, { error: 'Scan payload is empty' });
		}

		try {
			const res = await apiFetch<any>(fetch, `/api/partner/events/${eventId}/check-in`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({ qrPayload })
			});

			return { success: true, scanResult: res };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Ticket verification failed' });
		}
	}
};
