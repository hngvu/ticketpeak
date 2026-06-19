/* eslint-disable @typescript-eslint/no-explicit-any */
import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
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

		// Fetch events concurrently to populate schedule
		const [eventsRes, venuesRes] = await Promise.all([
			apiFetch<PageResponse<any>>(
				fetch,
				`/api/partner/events?organizationId=${selectedOrgId}&size=100&sort=startAt,asc`,
				{
					headers: {
						Authorization: `Bearer ${accessToken}`
					}
				}
			).catch(() => ({ content: [] }) as any),
			apiFetch<PageResponse<any>>(fetch, '/api/venues?size=100').catch(
				() => ({ content: [] }) as any
			)
		]);

		return {
			events: eventsRes?.content || [],
			venues: venuesRes?.content || []
		};
	} catch (err: any) {
		console.error('[DEDICATED SCHEDULE LOAD ERROR]:', err);
		return {
			events: [],
			venues: [],
			error: err.message || 'Failed to load schedule data.'
		};
	}
};
