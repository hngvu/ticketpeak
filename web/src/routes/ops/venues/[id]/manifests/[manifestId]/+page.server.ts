/* eslint-disable @typescript-eslint/no-explicit-any */
import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';
import {
	MOCK_VENUE_HOLLYWOOD_BOWL,
	MOCK_MANIFEST_HOLLYWOOD_BOWL,
	MOCK_MANIFEST_HB_LEVELS,
	MOCK_MANIFEST_HB_SECTIONS,
	MOCK_MANIFEST_HB_PRICE_LEVELS,
	MOCK_MANIFEST_HB_RS_AREAS,
	MOCK_MANIFEST_HB_GA_AREAS
} from '$lib/server/mockData';

	const MOCK_VENUES: Record<string, any> = {
		'019e90ee-6afa-70fc-aa55-2159192f0729': {
			venue: MOCK_VENUE_HOLLYWOOD_BOWL,
			manifest: MOCK_MANIFEST_HOLLYWOOD_BOWL,
			levels: MOCK_MANIFEST_HB_LEVELS,
			sections: MOCK_MANIFEST_HB_SECTIONS,
			priceLevels: MOCK_MANIFEST_HB_PRICE_LEVELS,
			rsAreas: MOCK_MANIFEST_HB_RS_AREAS,
			gaAreas: MOCK_MANIFEST_HB_GA_AREAS
		}
	};

export const load: PageServerLoad = async ({ params, fetch, cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	const { id: venueId, manifestId } = params;

	// "new" mode — auto-create manifest with defaults and go straight to canvas
	if (manifestId === 'new') {
		try {
			const venue = await apiFetch<any>(fetch, `/api/venues/${venueId}`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			});
			const defaultManifestId = `M${Math.floor(100000 + Math.random() * 900000)}`;
			await apiFetch(fetch, `/api/ops/venues/${venueId}/manifests`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${accessToken}`
				},
				body: JSON.stringify({
					id: defaultManifestId,
					description: `Layout ${new Date().toLocaleDateString('en-GB')}`,
					totalCapacity: 10000
				})
			});
			throw redirect(303, `/ops/venues/${venueId}/manifests/${defaultManifestId}`);
		} catch (err: any) {
			if (err?.status === 303) throw err;
			console.error('[OPS NEW MANIFEST CREATE ERROR]:', err);
			throw redirect(303, `/ops/venues/${venueId}/manifests`);
		}
	}

	// Check if this is a mock venue – serve directly without API calls
	const mockData = MOCK_VENUES[venueId];
	if (mockData && mockData.manifest.id === manifestId) {
		return {
			isNew: false,
			venue: mockData.venue,
			manifest: mockData.manifest,
			levels: mockData.levels,
			sections: mockData.sections,
			priceLevels: mockData.priceLevels,
			gaAreas: mockData.gaAreas,
			rsAreas: mockData.rsAreas
		};
	}

	try {
		// 1. Fetch basic info concurrently
		const [venue, manifest, levels, sections, priceLevels, gaAreas, rsAreas] = await Promise.all([
			apiFetch<any>(fetch, `/api/venues/${venueId}`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}),
			apiFetch<any>(fetch, `/api/ops/venues/manifests/${manifestId}`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}),
			apiFetch<any[]>(fetch, `/api/ops/venues/manifests/${manifestId}/levels`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => []),
			apiFetch<any[]>(fetch, `/api/ops/venues/manifests/${manifestId}/sections`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => []),
			apiFetch<any[]>(fetch, `/api/ops/venues/manifests/${manifestId}/price-levels`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => []),
			apiFetch<any[]>(fetch, `/api/ops/venues/manifests/${manifestId}/ga-areas`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => []),
			apiFetch<any[]>(fetch, `/api/ops/venues/manifests/${manifestId}/rs-areas`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => [])
		]);

		// 2. Fetch rows and seats for each RS area recursively
		const rsAreasWithDetails = await Promise.all(
			rsAreas.map(async (area: any) => {
				try {
					const rows = await apiFetch<any[]>(fetch, `/api/ops/venues/rs-areas/${area.id}/rows`, {
						headers: { Authorization: `Bearer ${accessToken}` }
					});
					const rowsWithSeats = await Promise.all(
						rows.map(async (row: any) => {
							try {
								const seats = await apiFetch<any[]>(fetch, `/api/ops/venues/rows/${row.id}/seats`, {
									headers: { Authorization: `Bearer ${accessToken}` }
								});
								return { ...row, seats };
							} catch {
								return { ...row, seats: [] };
							}
						})
					);
					return { ...area, rows: rowsWithSeats };
				} catch {
					return { ...area, rows: [] };
				}
			})
		);

		return {
			isNew: false,
			venue,
			manifest,
			levels,
			sections,
			priceLevels,
			gaAreas,
			rsAreas: rsAreasWithDetails
		};
	} catch (err: any) {
		console.error('[OPS MANIFEST EDITOR LOAD ERROR]:', err);
		throw redirect(303, `/ops/venues/${venueId}/manifests`);
	}
};
