/* eslint-disable @typescript-eslint/no-explicit-any */
import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, params, cookies, url }) => {
	const { id } = params;
	const accessToken = cookies.get('b2b_access_token');
	if (!accessToken) {
		throw redirect(303, '/b2b/login');
	}

	try {
		const [event, offers, inventory, venuesRes, classifications, attractions, ticketTypes] =
			await Promise.all([
				apiFetch<any>(fetch, `/api/partner/events/${id}`, {
					headers: { Authorization: `Bearer ${accessToken}` }
				}),
				apiFetch<any[]>(fetch, `/api/partner/events/${id}/offers`, {
					headers: { Authorization: `Bearer ${accessToken}` }
				}).catch(() => []),
				apiFetch<any>(fetch, `/api/partner/events/${id}/inventory`, {
					headers: { Authorization: `Bearer ${accessToken}` }
				}).catch(() => null),
				apiFetch<PageResponse<any>>(fetch, '/api/venues?size=100').catch(
					() => ({ content: [] }) as any
				),
				apiFetch<any[]>(fetch, '/api/classifications').catch(() => []),
				apiFetch<any[]>(fetch, '/api/attractions').catch(() => []),
				apiFetch<any[]>(fetch, `/api/partner/events/${id}/ticket-types`, {
					headers: { Authorization: `Bearer ${accessToken}` }
				}).catch(() => [])
			]);

		let manifests: any[] = [];
		let activeManifest = null;
		let levels: any[] = [];
		let sections: any[] = [];
		let priceLevels: any[] = [];
		let gaSections: any[] = [];
		let rsSections: any[] = [];

		if (event?.venueId) {
			manifests = await apiFetch<any[]>(fetch, `/api/partner/venues/${event.venueId}/manifests`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => []);

			if (manifests.length > 0) {
				manifests.forEach(m => {
					if (typeof m.uiData === 'string') {
						try {
							m.uiData = JSON.parse(m.uiData);
						} catch (e) {
							console.error('Failed to parse manifest uiData for', m.id);
							m.uiData = {};
						}
					}
				});

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
						const rows = await apiFetch<any[]>(
							fetch,
							`/api/partner/venues/sections/${sec.id}/rows`,
							{
								headers: { Authorization: `Bearer ${accessToken}` }
							}
						).catch(() => []);

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
							x: (sec.uiData?.x as number | undefined) ?? null,
							y: (sec.uiData?.y as number | undefined) ?? null,
							width: (sec.uiData?.width as number | undefined) ?? null,
							height: (sec.uiData?.height as number | undefined) ?? null,
							uiData: sec.uiData ?? null,
							rows: rowsWithSeats,
							sectionId: sec.id
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
						uiData: sec.uiData ?? null,
						sectionId: sec.id
					}));
			}
		}

		return {
			event,
			offers,
			inventory,
			venues: venuesRes?.content || [],
			classifications,
			attractions,
			ticketTypes,
			manifests,
			activeManifest,
			levels,
			sections,
			priceLevels,
			gaSections,
			rsSections
		};
	} catch (err: any) {
		console.error('[EVENT DETAILS LOAD ERROR]:', err);
		throw redirect(303, '/b2b/events');
	}
};
