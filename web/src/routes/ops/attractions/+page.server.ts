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
		const attractions = await apiFetch<any[]>(fetch, '/api/attractions').catch(() => []);
		return {
			attractions: attractions || []
		};
	} catch (err: any) {
		console.error('[OPS ATTRACTIONS LOAD ERROR]:', err);
		return {
			attractions: [],
			error: err.message || 'Failed to load platform attractions data.'
		};
	}
};

export const actions: Actions = {
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
	}
};
