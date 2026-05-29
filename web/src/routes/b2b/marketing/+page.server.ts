/* eslint-disable @typescript-eslint/no-explicit-any */
import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, url, cookies }) => {
	const accessToken = cookies.get('b2b_access_token');
	if (!accessToken) throw redirect(303, '/b2b/login');

	let selectedOrgId = url.searchParams.get('organizationId');
	if (!selectedOrgId) {
		const orgs = await apiFetch<any[]>(fetch, '/api/partner/organizations', {
			headers: { Authorization: `Bearer ${accessToken}` }
		}).catch(() => []);
		if (orgs && orgs.length > 0) selectedOrgId = orgs[0].id;
	}

	const eventsRes = await apiFetch<PageResponse<any>>(
		fetch,
		`/api/partner/events?organizationId=${selectedOrgId}&size=50&sort=startAt,asc`,
		{
			headers: { Authorization: `Bearer ${accessToken}` }
		}
	).catch(() => ({ content: [] }) as any);

	return {
		events: eventsRes?.content || []
	};
};
