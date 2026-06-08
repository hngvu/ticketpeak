/* eslint-disable @typescript-eslint/no-explicit-any */
import { fail, redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';
import { apiFetch } from '$lib/server/api';
import { MOCK_VENUE_HOLLYWOOD_BOWL, MOCK_MANIFEST_HOLLYWOOD_BOWL } from '$lib/server/mockData';

const MOCK_VENUE_IDS: Record<string, { venue: any; manifests: any[] }> = {
	'019e90ee-6afa-70fc-aa55-2159192f0729': {
		venue: MOCK_VENUE_HOLLYWOOD_BOWL,
		manifests: [MOCK_MANIFEST_HOLLYWOOD_BOWL]
	}
};

export const load: PageServerLoad = async ({ params, fetch, cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	const venueId = params.id;

	// Check mock venues first
	const mock = MOCK_VENUE_IDS[venueId];
	if (mock) {
		return { venue: mock.venue, manifests: mock.manifests };
	}

	try {
		// 1. Fetch Venue details
		const venue = await apiFetch<any>(fetch, `/api/venues/${venueId}`, {
			headers: {
				Authorization: `Bearer ${accessToken}`
			}
		});

		// 2. Fetch Manifests list for this venue
		const manifests = await apiFetch<any[]>(fetch, `/api/ops/venues/${venueId}/manifests`, {
			headers: {
				Authorization: `Bearer ${accessToken}`
			}
		}).catch(() => [] as any[]);

		return {
			venue,
			manifests
		};
	} catch (err: any) {
		console.error('[OPS VENUE MANIFESTS LOAD ERROR]:', err);
		return {
			venue: null,
			manifests: [],
			error: err.message || 'Failed to load venue manifests.'
		};
	}
};

export const actions: Actions = {
	publishManifest: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const manifestId = data.get('manifestId') as string;

		try {
			await apiFetch<any>(fetch, `/api/ops/venues/manifests/${manifestId}/publish`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true, message: 'Manifest published successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to publish manifest.' });
		}
	},

	archiveManifest: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const manifestId = data.get('manifestId') as string;

		try {
			await apiFetch<any>(fetch, `/api/ops/venues/manifests/${manifestId}/archive`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
			return { success: true, message: 'Manifest archived successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to archive manifest.' });
		}
	},

	cloneManifest: async ({ request, cookies, fetch }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const manifestId = data.get('manifestId') as string;
		const newId = data.get('newId') as string;
		const description = data.get('description') as string;

		if (!newId || !description) {
			return fail(400, { error: 'New Manifest Code and Description are required.' });
		}

		try {
			await apiFetch<any>(fetch, `/api/ops/venues/manifests/${manifestId}/clone`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({
					newId: newId.trim(),
					description: description.trim()
				})
			});
			return { success: true, message: 'Manifest cloned successfully.' };
		} catch (err: any) {
			return fail(400, { error: err.message || 'Failed to clone manifest.' });
		}
	}
};
