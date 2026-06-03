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
		const orgsRes = await apiFetch<PageResponse<any>>(fetch, '/api/ops/organizations?size=100', {
			headers: {
				Authorization: `Bearer ${accessToken}`
			}
		}).catch(() => ({ content: [] }) as any);

		return {
			organizations: orgsRes?.content || []
		};
	} catch (err: any) {
		console.error('[OPS ORGANIZATIONS LOAD ERROR]:', err);
		return {
			organizations: [],
			error: err.message || 'Failed to load platform organizations data.'
		};
	}
};

export const actions: Actions = {
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
	}
};
