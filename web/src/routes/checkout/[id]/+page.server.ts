/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';
import { error } from '@sveltejs/kit';

export const load: PageServerLoad = async ({ fetch, params, locals }) => {
	const { id } = params;

	// Validate UUID format
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
	if (!uuidRegex.test(id)) {
		throw error(404, 'Invalid Reservation ID');
	}

	try {
		// 1. Fetch reservation
		const reservation = await apiFetch<any>(fetch, `/api/reservations/${id}`);
		if (!reservation) {
			throw error(404, 'Reservation not found');
		}

		// 2. Fetch event
		const event = await apiFetch<any>(fetch, `/api/events/${reservation.eventId}`);

		return {
			reservation,
			event,
			currentUser: locals.user ?? null
		};
	} catch (err) {
		console.error('[Checkout Load Error]:', err);
		throw error(404, 'Reservation details not found');
	}
};
