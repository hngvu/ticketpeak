/* eslint-disable @typescript-eslint/no-explicit-any */
import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
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

		// 2. Fetch events concurrently to calculate stats
		const [eventsRes, venuesRes] = await Promise.all([
			apiFetch<PageResponse<any>>(
				fetch,
				`/api/partner/events?organizationId=${selectedOrgId}&size=50&sort=startAt,asc`,
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
			organizations: orgs,
			selectedOrgId,
			events: eventsRes?.content || [],
			venues: venuesRes?.content || []
		};
	} catch (err: any) {
		console.error('[DEDICATED DASHBOARD LOAD ERROR]:', err);
		return {
			organizations: [],
			selectedOrgId: null,
			events: [],
			venues: [],
			error: err.message || 'Failed to load dashboard data.'
		};
	}
};
