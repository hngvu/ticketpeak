/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	try {
		const classifications = await apiFetch<any[]>(fetch, '/api/classifications').catch(() => []);

		return {
			classifications: classifications || []
		};
	} catch (err: any) {
		console.error('[OPS CLASSIFICATIONS LOAD ERROR]:', err);
		return {
			classifications: [],
			error: err.message || 'Failed to load classifications data.'
		};
	}
};

export const actions: Actions = {
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
