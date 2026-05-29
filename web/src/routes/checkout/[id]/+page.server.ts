/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';
import { error } from '@sveltejs/kit';
import { MOCK_EVENTS, MOCK_VENUES } from '$lib/server/mockData';

export const load: PageServerLoad = async ({ fetch, params, locals }) => {
	const { id } = params;

	// Validate UUID format or mock format
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
	const isMockId = id.startsWith('reservation-');

	if (!uuidRegex.test(id) && !isMockId) {
		throw error(404, 'Invalid Reservation ID');
	}

	let reservation: any = null;
	let event: any = null;
	let isBackendAvailable = false;

	try {
		// 1. Fetch reservation
		reservation = await apiFetch<any>(fetch, `/api/reservations/${id}`);
		if (reservation) {
			// 2. Fetch event
			event = await apiFetch<any>(fetch, `/api/events/${reservation.eventId}`);
			isBackendAvailable = true;
		}
	} catch (err) {
		console.warn('[CHECKOUT SSR BACKEND OFFLINE OR FALLBACK]:', err);
	}

	// Graceful fallback to mock data
	if (!isBackendAvailable || !reservation) {
		// Mock event details
		const mockEvent = MOCK_EVENTS[0]; // Sơn Tùng Pop show
		const mockVenue = MOCK_VENUES.find((v) => v.id === mockEvent.venueId) || MOCK_VENUES[0];

		event = {
			id: mockEvent.id,
			title: mockEvent.title,
			slug: mockEvent.slug,
			imageUrl: mockVenue.imageUrl,
			startAt: mockEvent.startAt,
			timezone: 'Asia/Ho_Chi_Minh',
			venueName: mockVenue.name,
			cityName: mockVenue.city
		};

		const now = new Date();
		const expiresAt = new Date(now.getTime() + 14 * 60 * 60 * 1000 + 45 * 1000).toISOString(); // 14m 45s hold

		// Standard mock reservation
		reservation = {
			id: id,
			eventId: event.id,
			status: 'PENDING',
			currency: 'VND',
			expiresAt,
			createdAt: now.toISOString(),
			items: [
				{
					id: `item-${id}-ga`,
					offerId: `offer-${event.id}-ga`,
					seatingMode: 'GA',
					areaId: 'area-ga-floor',
					seatId: null,
					qty: 2,
					unitFaceValue: 1200000,
					currency: 'VND',
					charges: [
						{ name: 'Service fee', amount: 80000, isPercentage: false },
						{ name: 'Facility fee', amount: 20000, isPercentage: false }
					]
				}
			]
		};
	}

	return {
		reservation,
		event,
		currentUser: locals.user ?? null
	};
};
