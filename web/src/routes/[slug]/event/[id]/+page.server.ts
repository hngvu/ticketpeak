/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';
import { error } from '@sveltejs/kit';
import { MOCK_EVENTS, MOCK_VENUES, MOCK_ATTRACTIONS } from '$lib/server/mockData';
import { decodeBase62ToUuid } from '$lib/base62';

export const load: PageServerLoad = async ({ fetch, params, locals }) => {
	const paramId = params.id || '';
	const paramSlug = params.slug || '';

	// Decode Base62 to standard UUID format
	let resolvedId = paramId;
	const isMockId = paramId.startsWith('event-');
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

	if (!isMockId && !uuidRegex.test(paramId)) {
		try {
			resolvedId = decodeBase62ToUuid(paramId);
		} catch {
			throw error(404, 'Invalid Event ID');
		}
	}

	if (!uuidRegex.test(resolvedId) && !isMockId) {
		throw error(404, 'Invalid Event ID');
	}

	let event: any = null;
	let offers: any[] = [];
	let inventory: any = { gaInventory: [], reservedInventory: [] };
	let isBackendAvailable = false;

	try {
		// Parallel SSR fetches
		const [eventRes, offersRes, inventoryRes] = await Promise.all([
			apiFetch<any>(fetch, `/api/events/${resolvedId}`),
			apiFetch<any[]>(fetch, `/api/events/${resolvedId}/offers`),
			apiFetch<any>(fetch, `/api/events/${resolvedId}/inventory`)
		]);

		event = eventRes;
		offers = offersRes || [];
		inventory = inventoryRes || { gaInventory: [], reservedInventory: [] };
		isBackendAvailable = true;
	} catch (err) {
		console.warn('[EDP SSR BACKEND OFFLINE OR FALLBACK]:', err);
	}

	// Dynamic fallback to high-fidelity mock data if database is empty/offline
	if (!isBackendAvailable || !event) {
		// Map database seeded UUIDs to mock IDs for seamless frontend testing
		let mockId = resolvedId;
		if (resolvedId === '018f4e1a-0002-4000-8000-000000000001') mockId = 'event-denvau-hn';

		// Find mock event matching id
		let mockEvent = MOCK_EVENTS.find((e) => e.id === mockId);
		if (!mockEvent) {
			// Fallback by slug matching
			mockEvent = MOCK_EVENTS.find((e) => e.slug === paramSlug) || MOCK_EVENTS[0];
		}

		// Ensure we resolve a full mock venue
		const mockVenue = MOCK_VENUES.find((v) => v.id === mockEvent.venueId) || MOCK_VENUES[0];

		// Populate high-fidelity event fields
		event = {
			id: mockEvent.id,
			title: mockEvent.title,
			slug: mockEvent.slug,
			description: `Join us for an unforgettable live experience! The event "${mockEvent.title}" features spectacular performances, state-of-the-art stage production, and an energetic crowd. Ticket categories are available now for standard and VIP admission.

This event is hosted at the legendary ${mockVenue.name} in ${mockVenue.city}. We expect a full house, so select your tickets and reserve your seats early to ensure entry. All tickets purchased through Ticketpeak are 100% verified and secure.`,
			imageUrl:
				mockVenue.imageUrl ||
				'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=1200&q=80',
			startAt: mockEvent.startAt,
			timezone: 'Asia/Ho_Chi_Minh',
			status: 'PUBLISHED',
			venueId: mockVenue.id,
			venueName: mockVenue.name,
			cityName: mockVenue.city,
			classifications: mockEvent.classifications || [{ id: 'genre-pop', name: 'Pop', slug: 'pop' }],
			attractions: mockEvent.attractions || [MOCK_ATTRACTIONS[0]]
		};

		// Customize mock offers based on the event title
		const currency =
			mockEvent.cityName === 'Hồ Chí Minh' ||
			mockEvent.cityName === 'Hà Nội' ||
			mockEvent.cityName === 'Đà Nẵng'
				? 'VND'
				: 'USD';
		const isVnd = currency === 'VND';

		const standardPrice = isVnd ? 1200000 : 80;
		const vipPrice = isVnd ? 3500000 : 250;
		const balconyPrice = isVnd ? 600000 : 40;

		const now = new Date();
		const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000).toISOString();
		const yearLater = new Date(now.getTime() + 365 * 24 * 60 * 60 * 1000).toISOString();

		offers = [
			{
				id: `offer-${mockEvent.id}-ga`,
				name: 'Standard GA',
				description: 'General Admission access to the main floor. Standard entry ticket.',
				faceValue: standardPrice,
				currency,
				ticketTypeId: 'type-standard',
				seatingMode: 'GA',
				sellableQuantities: [1, 2, 3, 4],
				eventTicketMinimum: 1,
				restrictSingleSeat: false,
				saleWindows: [{ startAt: weekAgo, endAt: yearLater, type: 'PUBLIC' }],
				charges: [
					{ name: 'Service fee', amount: isVnd ? 80000 : 6.5, isPercentage: false },
					{ name: 'Facility fee', amount: isVnd ? 20000 : 2.5, isPercentage: false }
				]
			},
			{
				id: `offer-${mockEvent.id}-vip`,
				name: 'VIP Floor',
				description:
					'Exclusive VIP access. Includes front-row area access, souvenir lanyard, and fast-track entrance.',
				faceValue: vipPrice,
				currency,
				ticketTypeId: 'type-vip',
				seatingMode: 'GA',
				sellableQuantities: [1, 2, 4],
				eventTicketMinimum: 1,
				restrictSingleSeat: false,
				saleWindows: [{ startAt: weekAgo, endAt: yearLater, type: 'PUBLIC' }],
				charges: [
					{ name: 'Service fee', amount: isVnd ? 250000 : 20.0, isPercentage: false },
					{ name: 'Facility fee', amount: isVnd ? 50000 : 5.0, isPercentage: false }
				]
			},
			{
				id: `offer-${mockEvent.id}-balcony`,
				name: 'Standard Balcony',
				description: 'Assigned seating in the upper tier balcony rows. Great panoramic view.',
				faceValue: balconyPrice,
				currency,
				ticketTypeId: 'type-standard',
				seatingMode: 'GA',
				sellableQuantities: [1, 2, 3, 4],
				eventTicketMinimum: 1,
				restrictSingleSeat: false,
				saleWindows: [{ startAt: weekAgo, endAt: yearLater, type: 'PUBLIC' }],
				charges: [
					{ name: 'Service fee', amount: isVnd ? 40000 : 3.5, isPercentage: false },
					{ name: 'Facility fee', amount: isVnd ? 10000 : 1.5, isPercentage: false }
				]
			}
		];

		// Populated dummy inventory for GA
		inventory = {
			gaInventory: [
				{ offerId: `offer-${mockEvent.id}-ga`, areaId: 'area-ga-floor', available: 150 },
				{ offerId: `offer-${mockEvent.id}-vip`, areaId: 'area-vip-floor', available: 45 },
				{ offerId: `offer-${mockEvent.id}-balcony`, areaId: 'area-balcony', available: 0 } // Sold Out test
			],
			reservedInventory: []
		};
	}

	return {
		event,
		offers,
		inventory,
		currentUser: locals.user ?? null
	};
};
