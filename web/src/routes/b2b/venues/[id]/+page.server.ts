import { redirect, error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';

export const load: PageServerLoad = async ({ params, fetch, cookies, url }) => {
	const accessToken = cookies.get('b2b_access_token');
	if (!accessToken) {
		throw redirect(303, '/b2b/login');
	}

	const venueId = params.id;

	try {
		const venue = await apiFetch<any>(fetch, `/api/venues/${venueId}`, {
			headers: { Authorization: `Bearer ${accessToken}` }
		});

		if (!venue) {
			throw error(404, 'Venue not found');
		}

		// Fetch manifests
		const manifests = await apiFetch<any[]>(fetch, `/api/partner/venues/${venueId}/manifests`, {
			headers: { Authorization: `Bearer ${accessToken}` }
		}).catch(() => []);

		let activeManifest = null;
		let levels: any[] = [];
		let sections: any[] = [];
		let priceLevels: any[] = [];
		let gaSections: any[] = [];
		let rsSections: any[] = [];

		if (manifests.length > 0) {
			const requestedManifestId = url.searchParams.get('manifestId');
			activeManifest = manifests.find((m) => m.id === requestedManifestId) || manifests[0];
			const manifestId = activeManifest.id;

			[levels, sections, priceLevels] = await Promise.all([
				apiFetch<any[]>(fetch, `/api/partner/venues/manifests/${manifestId}/levels`, {
					headers: { Authorization: `Bearer ${accessToken}` }
				}).catch(() => []),
				apiFetch<any[]>(fetch, `/api/partner/venues/manifests/${manifestId}/sections`, {
					headers: { Authorization: `Bearer ${accessToken}` }
				}).catch(() => []),
				apiFetch<any[]>(fetch, `/api/partner/venues/manifests/${manifestId}/price-levels`, {
					headers: { Authorization: `Bearer ${accessToken}` }
				}).catch(() => [])
			]);

			// Build rsSections
			const rsSectionList = sections.filter((s: any) => s.type === 'RS');
			rsSections = await Promise.all(
				rsSectionList.map(async (sec: any) => {
					const rows = await apiFetch<any[]>(fetch, `/api/partner/venues/sections/${sec.id}/rows`, {
						headers: { Authorization: `Bearer ${accessToken}` }
					}).catch(() => []);

					const rowsWithSeats = await Promise.all(
						rows.map(async (row: any) => {
							const seats = await apiFetch<any[]>(
								fetch,
								`/api/partner/venues/rows/${row.id}/seats`,
								{
									headers: { Authorization: `Bearer ${accessToken}` }
								}
							).catch(() => []);
							return { ...row, seats };
						})
					);

					return {
						id: sec.id,
						name: sec.name ?? null,
						color: sec.color ?? null,
						levelId: sec.levelId ?? null,
						polygon: (sec.uiData?.polygon as [number, number][] | undefined) ?? [],
						uiData: sec.uiData ?? null,
						rows: rowsWithSeats
					};
				})
			);

			// Build gaSections
			gaSections = sections
				.filter((s: any) => s.type === 'GA')
				.map((sec: any) => ({
					id: sec.id,
					name: sec.name ?? null,
					levelId: sec.levelId ?? null,
					priceLevelId: null,
					capacity: sec.capacity ?? 0,
					color: sec.color ?? null,
					polygon: (sec.uiData?.polygon as [number, number][] | undefined) ?? [],
					x: (sec.uiData?.x as number | undefined) ?? 0,
					y: (sec.uiData?.y as number | undefined) ?? 0,
					width: (sec.uiData?.width as number | undefined) ?? 200,
					height: (sec.uiData?.height as number | undefined) ?? 100,
					uiData: sec.uiData ?? null
				}));
		}

		return {
			venue,
			manifests,
			manifest: activeManifest,
			levels,
			sections,
			priceLevels,
			gaSections,
			rsSections
		};
	} catch (err: any) {
		console.error('[VENUE DETAIL LOAD ERROR]:', err);
		throw error(404, 'Venue not found');
	}
};
