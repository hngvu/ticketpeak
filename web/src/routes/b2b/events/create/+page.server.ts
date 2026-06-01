/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, url, cookies }) => {
	const accessToken = cookies.get('b2b_access_token');
	if (!accessToken) {
		throw redirect(303, '/b2b/login');
	}

	try {
		// 1. Fetch organizer's organizations
		let orgs = await apiFetch<any[]>(fetch, '/api/partner/organizations', {
			headers: {
				Authorization: `Bearer ${accessToken}`
			}
		}).catch(() => [] as any[]);

		// Fallback mock organizations
		if (!orgs || orgs.length === 0) {
			orgs = [
				{ id: 'org-default-tp', name: 'Ticketpeak Organizer Org' },
				{ id: 'org-secondary-tp', name: 'Elite Live Entertainment' }
			];
		}

		// Resolve selected organization ID
		let selectedOrgId = url.searchParams.get('organizationId');
		if (!selectedOrgId || !orgs.some((o: any) => o.id === selectedOrgId)) {
			selectedOrgId = orgs[0].id;
		}

		// 2. Fetch venues, classifications, and attractions concurrently
		const [venuesRes, classifications, attractions] = await Promise.all([
			apiFetch<PageResponse<any>>(fetch, '/api/venues?size=100').catch(
				() => ({ content: [] }) as any
			),
			apiFetch<any[]>(fetch, '/api/classifications').catch(() => []),
			apiFetch<any[]>(fetch, '/api/attractions').catch(() => [])
		]);

		return {
			organizations: orgs,
			selectedOrgId,
			venues: venuesRes?.content || [],
			classifications,
			attractions
		};
	} catch (err: any) {
		console.error('[EVENT CREATE LOAD ERROR]:', err);
		throw redirect(303, '/b2b/events');
	}
};

export const actions: Actions = {
	default: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('b2b_access_token');
		if (!accessToken) throw redirect(303, '/b2b/login');

		const data = await request.formData();
		const organizationId = data.get('organizationId') as string;
		const venueId = data.get('venueId') as string;
		const title = data.get('title') as string;
		const slug = data.get('slug') as string;
		const description = data.get('description') as string;
		const startAtStr = data.get('startAt') as string;
		const endAtStr = data.get('endAt') as string;
		const timezone = (data.get('timezone') as string) || 'Asia/Ho_Chi_Minh';
		
		// Optional sales windows
		const saleStartAtStr = data.get('saleStartAt') as string;
		const saleEndAtStr = data.get('saleEndAt') as string;

		// Anti-scalping & ticketing policies
		const restrictSingleSeat = data.get('restrictSingleSeat') === 'on';
		const safeTixEnabled = data.get('safeTixEnabled') === 'on';
		const transferEnabled = data.get('transferEnabled') === 'on';
		const maxTransferCount = parseInt(data.get('maxTransferCount') as string) || 0;
		const serviceFeePercent = parseFloat(data.get('serviceFeePercent') as string) || 0;

		// Attractions & Classifications multi-select
		const attractionIds = data.getAll('attractionIds') as string[];
		const classificationIds = data.getAll('classificationIds') as string[];

		if (!organizationId || !venueId || !title || !startAtStr) {
			return fail(400, { error: 'Required fields are missing' });
		}

		const startAt = new Date(startAtStr).toISOString();
		const endAt = endAtStr ? new Date(endAtStr).toISOString() : null;
		const saleStartAt = saleStartAtStr ? new Date(saleStartAtStr).toISOString() : null;
		const saleEndAt = saleEndAtStr ? new Date(saleEndAtStr).toISOString() : null;

		if (endAt && new Date(endAt) <= new Date(startAt)) {
			return fail(400, { error: 'Event end time must be strictly after start time' });
		}

		if (saleStartAt && saleEndAt && new Date(saleEndAt) <= new Date(saleStartAt)) {
			return fail(400, { error: 'Sale end time must be strictly after sale start time' });
		}

		const payload = {
			organizationId,
			venueId,
			title,
			slug: slug && slug.trim() !== '' ? slug.trim() : null,
			description,
			startAt,
			endAt,
			timezone,
			saleStartAt,
			saleEndAt,
			restrictSingleSeat,
			safeTixEnabled,
			transferEnabled,
			maxTransferCount,
			serviceFeePercent,
			classificationIds,
			attractionIds
		};

		let createdEventId: string | undefined;

		try {
			const res = await apiFetch<any>(fetch, '/api/partner/events', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify(payload)
			});
			createdEventId = res?.id;
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to create event' });
		}

		if (createdEventId) {
			throw redirect(303, `/b2b/events/${createdEventId}`);
		}

		throw redirect(303, '/b2b/events');
	}
};
