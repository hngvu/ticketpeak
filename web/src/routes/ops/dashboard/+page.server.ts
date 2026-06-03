/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	try {
		// Fetch global events, organizations, classifications, and attractions concurrently
		const [eventsRes, orgsRes, classifications, attractions] = await Promise.all([
			apiFetch<PageResponse<any>>(fetch, '/api/ops/events?size=100&sort=startAt,asc', {
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			}).catch(() => ({ content: [] }) as any),
			apiFetch<PageResponse<any>>(fetch, '/api/ops/organizations?size=100', {
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			}).catch(() => ({ content: [] }) as any),
			apiFetch<any[]>(fetch, '/api/classifications').catch(() => []),
			apiFetch<any[]>(fetch, '/api/attractions').catch(() => [])
		]);

		return {
			events: eventsRes?.content || [],
			organizations: orgsRes?.content || [],
			classifications: classifications || [],
			attractions: attractions || []
		};
	} catch (err: any) {
		console.error('[OPS DASHBOARD LOAD ERROR]:', err);
		return {
			events: [],
			organizations: [],
			classifications: [],
			attractions: [],
			error: err.message || 'Failed to load platform operations data.'
		};
	}
};

export const actions: Actions = {
	publishEvent: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;

		try {
			await apiFetch<any>(fetch, `/api/ops/events/${id}/publish`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true, message: 'Event approved and published successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to publish event.' });
		}
	},

	startSales: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;

		try {
			await apiFetch<any>(fetch, `/api/ops/events/${id}/onsale`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true, message: 'Event ticket sales started successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to start ticket sales.' });
		}
	},

	cancelEvent: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;

		try {
			await apiFetch<any>(fetch, `/api/ops/events/${id}/cancel`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true, message: 'Event cancelled successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to cancel event.' });
		}
	},

	postponeEvent: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;
		const reason = (data.get('reason') as string) || 'Scheduling conflicts.';

		try {
			await apiFetch<any>(fetch, `/api/ops/events/${id}/postpone`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({ reason })
			});
			return { success: true, message: 'Event postponed successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to postpone event.' });
		}
	},

	updateOrganization: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;
		const name = data.get('name') as string;
		const bio = data.get('bio') as string;
		const logoUrl = data.get('logoUrl') as string;
		const websiteUrl = data.get('websiteUrl') as string;
		const email = data.get('email') as string;
		const phone = data.get('phone') as string;
		const cityId = data.get('cityId') as string;
		const countryCode = data.get('countryCode') as string;

		try {
			await apiFetch<any>(fetch, `/api/ops/organizations/${id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({
					name,
					bio,
					logoUrl,
					websiteUrl,
					email,
					phone,
					cityId,
					countryCode
				})
			});
			return { success: true, message: 'Organization updated successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to update organization.' });
		}
	},

	createClassification: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const name = data.get('name') as string;
		const slug = data.get('slug') as string;
		const parentId = data.get('parentId') as string;

		try {
			await apiFetch<any>(fetch, '/api/ops/classifications', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({
					name,
					slug,
					level: parentId ? 2 : 1,
					parentId: parentId || null,
					isActive: true
				})
			});
			return { success: true, message: 'Classification created successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to create classification.' });
		}
	},

	createAttraction: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const name = data.get('name') as string;
		const slug = data.get('slug') as string;
		const type = data.get('type') as string;
		const imageUrl = data.get('imageUrl') as string;
		const description = data.get('description') as string;

		try {
			await apiFetch<any>(fetch, '/api/ops/attractions', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({
					name,
					slug,
					type,
					imageUrl,
					description
				})
			});
			return { success: true, message: 'Attraction created successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to create attraction.' });
		}
	},

	createOrganization: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const name = data.get('name') as string;
		const ownerEmail = data.get('ownerEmail') as string;
		const bio = data.get('bio') as string;
		const logoUrl = data.get('logoUrl') as string;
		const websiteUrl = data.get('websiteUrl') as string;
		const email = data.get('email') as string;
		const phone = data.get('phone') as string;
		const cityId = data.get('cityId') as string;
		const countryCode = data.get('countryCode') as string;

		try {
			await apiFetch<any>(fetch, '/api/ops/organizations', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({
					name,
					ownerEmail,
					bio: bio || null,
					logoUrl: logoUrl || null,
					websiteUrl: websiteUrl || null,
					email: email || null,
					phone: phone || null,
					cityId: cityId ? parseInt(cityId, 10) : null,
					countryCode: countryCode || null
				})
			});
			return { success: true, message: 'Organization created successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to create organization.' });
		}
	},

	updateClassification: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;
		const name = data.get('name') as string;
		const slug = data.get('slug') as string;
		const parentId = data.get('parentId') as string;

		try {
			await apiFetch<any>(fetch, `/api/ops/classifications/${id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({
					name,
					slug,
					level: parentId ? 2 : 1,
					parentId: parentId || null,
					isActive: true
				})
			});
			return { success: true, message: 'Classification updated successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to update classification.' });
		}
	},

	toggleClassificationStatus: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;
		const name = data.get('name') as string;
		const slug = data.get('slug') as string;
		const parentId = data.get('parentId') as string;
		const isActive = data.get('isActive') === 'true';

		try {
			await apiFetch<any>(fetch, `/api/ops/classifications/${id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({
					name,
					slug,
					level: parentId ? 2 : 1,
					parentId: parentId || null,
					isActive: !isActive
				})
			});
			return {
				success: true,
				message: `Classification ${!isActive ? 'activated' : 'deactivated'} successfully.`
			};
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to toggle classification status.' });
		}
	}
};
