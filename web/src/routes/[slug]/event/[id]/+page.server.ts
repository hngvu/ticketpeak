/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';
import { error } from '@sveltejs/kit';
import { decodeBase62ToUuid } from '$lib/base62';

export const load: PageServerLoad = async ({ fetch, params, locals }) => {
	const paramId = params.id || '';
	const paramSlug = params.slug || '';

	// Decode Base62 to standard UUID format
	let resolvedId = paramId;
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

	if (!uuidRegex.test(paramId)) {
		try {
			resolvedId = decodeBase62ToUuid(paramId);
		} catch {
			throw error(404, 'Invalid Event ID');
		}
	}

	if (!uuidRegex.test(resolvedId)) {
		throw error(404, 'Invalid Event ID');
	}

	try {
		// Parallel SSR fetches
		const [event, offers, inventory] = await Promise.all([
			apiFetch<any>(fetch, `/api/events/${resolvedId}`),
			apiFetch<any[]>(fetch, `/api/events/${resolvedId}/offers`).catch(() => []),
			apiFetch<any>(fetch, `/api/events/${resolvedId}/inventory`).catch(() => ({
				gaInventory: [],
				reservedInventory: []
			}))
		]);

		let manifestDetail: any = null;
		if (event?.venueId && event?.manifestId) {
			const rawManifest = await apiFetch<any>(fetch, `/api/events/${resolvedId}/manifest`).catch(
				() => null
			);
			if (rawManifest) {
				const { manifest, levels, sections, priceLevels, rows, seats } = rawManifest;

				const gaAreas = (sections ?? [])
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
						width: (sec.uiData?.width as number | undefined) ?? 100,
						height: (sec.uiData?.height as number | undefined) ?? 100,
						sectionId: sec.id
					}));

				const rsAreas = (sections ?? [])
					.filter((s: any) => s.type === 'RS')
					.map((sec: any) => {
						const secRows = (rows ?? []).filter((r: any) => r.sectionId === sec.id);
						const rowsWithSeats = secRows.map((r: any) => ({
							...r,
							seats: (seats ?? []).filter((seat: any) => seat.seatRowId === r.id)
						}));
						return {
							id: sec.id,
							name: sec.name ?? null,
							color: sec.color ?? null,
							levelId: sec.levelId ?? null,
							polygon: (sec.uiData?.polygon as [number, number][] | undefined) ?? [],
							rows: rowsWithSeats,
							sectionId: sec.id
						};
					});

				manifestDetail = {
					manifest,
					levels,
					sections,
					priceLevels,
					gaAreas,
					rsAreas
				};
			}
		}

		return {
			event,
			offers: offers || [],
			inventory: inventory || { gaInventory: [], reservedInventory: [] },
			manifestDetail,
			currentUser: locals.user ?? null,
			slug: paramSlug
		};
	} catch (err) {
		console.error('[Event Load Error]:', err);
		throw error(404, 'Event not found');
	}
};
