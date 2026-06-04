/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';
import { error } from '@sveltejs/kit';

export const load: PageServerLoad = async ({ fetch, params, locals }) => {
	const { id } = params;

	// Validate UUID format
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
	if (!uuidRegex.test(id)) {
		throw error(404, 'Invalid Order ID');
	}

	try {
		// 1. Fetch confirmed order details
		const order = await apiFetch<any>(fetch, `/api/orders/${id}`);
		if (!order) {
			throw error(404, 'Order not found');
		}

		// 2. Fetch event details
		const event = await apiFetch<any>(fetch, `/api/events/${order.eventId}`);

		// 3. Fetch owner's tickets
		const allTicketsRes = await apiFetch<any>(fetch, '/api/tickets?size=100');
		const allTickets = allTicketsRes?.content || [];

		// 4. Filter tickets belonging to this order
		const tickets = allTickets.filter((t: any) => t.orderId === id);

		return {
			order,
			tickets,
			event,
			currentUser: locals.user ?? null
		};
	} catch (err) {
		console.error('[Tickets Load Error]:', err);
		throw error(404, 'Order details not found');
	}
};
