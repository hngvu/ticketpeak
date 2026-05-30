/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, url, cookies }) => {
	const accessToken = cookies.get('b2b_access_token');
	if (!accessToken) throw redirect(303, '/b2b/login');

	// 1. Fetch organization ID
	let selectedOrgId = url.searchParams.get('organizationId');
	if (!selectedOrgId) {
		const orgs = await apiFetch<any[]>(fetch, '/api/partner/organizations', {
			headers: { Authorization: `Bearer ${accessToken}` }
		}).catch(() => []);
		if (orgs && orgs.length > 0) selectedOrgId = orgs[0].id;
	}

	if (!selectedOrgId) {
		return {
			events: [],
			selectedEventId: null,
			ticketTypes: [],
			offers: []
		};
	}

	// 2. Fetch all events for the organization
	const eventsRes = await apiFetch<PageResponse<any>>(
		fetch,
		`/api/partner/events?organizationId=${selectedOrgId}&size=50&sort=startAt,asc`,
		{
			headers: { Authorization: `Bearer ${accessToken}` }
		}
	).catch(() => ({ content: [] }) as any);

	const events = eventsRes?.content || [];

	// 3. Resolve active/selected event
	let selectedEventId = url.searchParams.get('eventId');
	if (!selectedEventId && events.length > 0) {
		selectedEventId = events[0].id;
	}

	let ticketTypes: any[] = [];
	let offers: any[] = [];

	if (selectedEventId) {
		// Fetch ticket types and offers for the selected event concurrently
		const [ttRes, offersRes] = await Promise.all([
			apiFetch<any[]>(fetch, `/api/partner/events/${selectedEventId}/ticket-types`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => []),
			apiFetch<any[]>(fetch, `/api/partner/events/${selectedEventId}/offers`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => [])
		]);

		ticketTypes = ttRes || [];
		offers = offersRes || [];
	}

	return {
		events,
		selectedEventId,
		ticketTypes,
		offers
	};
};

export const actions: Actions = {
	createTicketType: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const eventId = data.get('eventId') as string;
		const name = data.get('name') as string;
		const slug = data.get('slug') as string;
		const description = data.get('description') as string;

		if (!eventId || !name || !slug) {
			return fail(400, { error: 'Event ID, name, and slug are required fields' });
		}

		try {
			await apiFetch<any>(fetch, `/api/partner/events/${eventId}/ticket-types`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({ name, slug, description })
			});
			return { success: true, message: 'Ticket type created successfully' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to create ticket type' });
		}
	},

	deleteTicketType: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const eventId = data.get('eventId') as string;
		const slug = data.get('slug') as string;

		if (!eventId || !slug) {
			return fail(400, { error: 'Event ID and ticket type slug are required' });
		}

		try {
			await apiFetch<any>(fetch, `/api/partner/events/${eventId}/ticket-types/${slug}`, {
				method: 'DELETE',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true, message: 'Ticket type deleted successfully' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to delete ticket type' });
		}
	},

	createOffer: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const eventId = data.get('eventId') as string;
		const ticketTypeId = data.get('ticketTypeId') as string;
		const name = data.get('name') as string;
		const description = data.get('description') as string;
		const faceValueStr = data.get('faceValue') as string;
		const capacityCapStr = data.get('capacityCap') as string;
		const seatingMode = data.get('seatingMode') as string; // GENERAL_ADMISSION, RESERVED_SEATING
		const sectionId = data.get('sectionId') as string;
		const priceLevelId = data.get('priceLevelId') as string;

		if (!eventId || !ticketTypeId || !name || !faceValueStr || !capacityCapStr) {
			return fail(400, { error: 'Required fields are missing' });
		}

		// Configure general sale window matching event times
		const now = new Date();
		const saleStartAt = now.toISOString();
		const saleEndAt = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000).toISOString(); // 30 days out

		const payload = {
			ticketTypeId,
			name,
			description,
			currency: 'VND',
			faceValue: parseFloat(faceValueStr),
			restrictedPayment: false,
			eventTicketMinimum: 1,
			capacityCap: parseInt(capacityCapStr, 10),
			sellableQuantities: [1, 2, 4, 6],
			seatingMode,
			sectionId: sectionId || null,
			priceLevelId: priceLevelId || null,
			saleWindows: [
				{
					type: 'GENERAL_SALE',
					startAt: saleStartAt,
					endAt: saleEndAt
				}
			],
			charges: [
				{
					name: 'Phí dịch vụ',
					type: 'SERVICE',
					amount: parseFloat(faceValueStr) * 0.1, // 10% standard service fee
					isPercentage: false
				}
			]
		};

		try {
			await apiFetch<any>(fetch, `/api/partner/events/${eventId}/offers`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify(payload)
			});
			return { success: true, message: 'Offer created successfully' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to create offer' });
		}
	},

	deleteOffer: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const eventId = data.get('eventId') as string;
		const offerId = data.get('offerId') as string;

		if (!eventId || !offerId) {
			return fail(400, { error: 'Event ID and Offer ID are required' });
		}

		try {
			await apiFetch<any>(fetch, `/api/partner/events/${eventId}/offers/${offerId}`, {
				method: 'DELETE',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true, message: 'Offer deleted successfully' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to delete offer' });
		}
	}
};
