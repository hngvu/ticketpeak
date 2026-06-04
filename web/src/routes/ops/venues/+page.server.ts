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
		const venuesRes = await apiFetch<PageResponse<any>>(fetch, '/api/venues?size=100', {
			headers: {
				Authorization: `Bearer ${accessToken}`
			}
		}).catch(() => ({ content: [] }) as any);

		return {
			venues: venuesRes?.content || []
		};
	} catch (err: any) {
		console.error('[OPS VENUES LOAD ERROR]:', err);
		return {
			venues: [],
			error: err.message || 'Failed to load venues.'
		};
	}
};

export const actions: Actions = {
	toggleStatus: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;
		const currentStatus = data.get('status') as string;
		const action = currentStatus === 'ACTIVE' ? 'deactivate' : 'activate';

		try {
			await apiFetch<any>(fetch, `/api/ops/venues/${id}/${action}`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true, message: `Venue successfully ${action}d.` };
		} catch (err: any) {
			return fail(400, { error: err.message || `Failed to change venue status.` });
		}
	},

	createVenue: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const name = data.get('name') as string;
		const address = data.get('address') as string;
		const city = data.get('city') as string;
		const country = data.get('country') as string;
		const latitude = data.get('latitude') as string;
		const longitude = data.get('longitude') as string;
		const phone = data.get('phone') as string;
		const email = data.get('email') as string;
		const website = data.get('website') as string;
		const description = data.get('description') as string;
		const thumbnailUrl = data.get('thumbnailUrl') as string;

		try {
			await apiFetch<any>(fetch, '/api/ops/venues', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({
					name,
					address,
					city,
					country,
					latitude: latitude ? parseFloat(latitude) : null,
					longitude: longitude ? parseFloat(longitude) : null,
					phone: phone || null,
					email: email || null,
					website: website || null,
					description: description || null,
					thumbnailUrl: thumbnailUrl || null,
					images: []
				})
			});
			return { success: true, message: 'Venue created successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to create venue.' });
		}
	}
};
