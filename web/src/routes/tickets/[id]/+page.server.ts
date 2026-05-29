/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';
import { error } from '@sveltejs/kit';
import { MOCK_EVENTS, MOCK_VENUES } from '$lib/server/mockData';

export const load: PageServerLoad = async ({ fetch, params, locals }) => {
	const { id } = params;

	// Validate UUID format or mock format
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
	const isMockId = id.startsWith('mock-order-');

	if (!uuidRegex.test(id) && !isMockId) {
		throw error(404, 'Invalid Order ID');
	}

	let order: any = null;
	let tickets: any[] = [];
	let event: any = null;
	let isBackendAvailable = false;

	try {
		// 1. Fetch confirmed order details
		order = await apiFetch<any>(fetch, `/api/orders/${id}`);
		if (order) {
			// 2. Fetch event details
			event = await apiFetch<any>(fetch, `/api/events/${order.eventId}`);

			// 3. Fetch owner's tickets
			const allTicketsRes = await apiFetch<any>(fetch, '/api/tickets?size=100');
			const allTickets = allTicketsRes?.content || [];

			// 4. Filter tickets belonging to this order
			tickets = allTickets.filter((t: any) => t.orderId === id);
			isBackendAvailable = true;
		}
	} catch (err) {
		console.warn('[TICKETS CONFIRMATION SSR BACKEND OFFLINE OR FALLBACK]:', err);
	}

	// Graceful mock data fallback
	if (!isBackendAvailable || !order) {
		// Mock event
		const mockEvent = MOCK_EVENTS[0];
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

		// Confirmed mock order
		order = {
			id,
			reservationId: id.replace('mock-order-', ''),
			paymentId: 'payment-mock-999',
			accountId: 'user-mock-123',
			eventId: event.id,
			status: 'CONFIRMED',
			currency: 'VND',
			totalAmount: 2500000, // 2 GA tickets + mock fees
			createdAt: new Date().toISOString()
		};

		// Generate mock digital tickets matching order quantity
		tickets = [
			{
				id: `ticket-mock-1`,
				orderId: id,
				eventId: event.id,
				offerId: `offer-${event.id}-ga`,
				ticketTypeName: 'Pop Standard',
				offerName: 'Standard GA',
				faceValue: 1200000,
				currency: 'VND',
				seatingMode: 'GENERAL_ADMISSION',
				areaId: 'AREA-GA-FLOOR',
				seatId: 'Row GA / Seat 234',
				status: 'ISSUED',
				checkedInAt: null,
				transferCount: 0,
				createdAt: new Date().toISOString()
			},
			{
				id: `ticket-mock-2`,
				orderId: id,
				eventId: event.id,
				offerId: `offer-${event.id}-ga`,
				ticketTypeName: 'Pop Standard',
				offerName: 'Standard GA',
				faceValue: 1200000,
				currency: 'VND',
				seatingMode: 'GENERAL_ADMISSION',
				areaId: 'AREA-GA-FLOOR',
				seatId: 'Row GA / Seat 235',
				status: 'ISSUED',
				checkedInAt: null,
				transferCount: 0,
				createdAt: new Date().toISOString()
			}
		];
	}

	return {
		order,
		tickets,
		event,
		currentUser: locals.user ?? null
	};
};
