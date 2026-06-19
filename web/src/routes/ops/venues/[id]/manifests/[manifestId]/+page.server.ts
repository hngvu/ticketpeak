/* eslint-disable @typescript-eslint/no-explicit-any */
import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';

export const load: PageServerLoad = async ({ params, fetch, cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	const { id: venueId, manifestId } = params;

	// ── "new" mode: auto-create a blank manifest then redirect ────────────────
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



	// ── Real API path ──────────────────────────────────────────────────────────
	try {
		const [venue, manifest, levels, sections, priceLevels] = await Promise.all([
			apiFetch<any>(fetch, `/api/venues/${venueId}`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}),
			apiFetch<any>(fetch, `/api/ops/venues/manifests/${manifestId}`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}),
			apiFetch<any[]>(fetch, `/api/ops/venues/manifests/${manifestId}/levels`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => [] as any[]),
			apiFetch<any[]>(fetch, `/api/ops/venues/manifests/${manifestId}/sections`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => [] as any[]),
			apiFetch<any[]>(fetch, `/api/ops/venues/manifests/${manifestId}/price-levels`, {
				headers: { Authorization: `Bearer ${accessToken}` }
			}).catch(() => [] as any[])
		]);

		// Build rsSections: fetch rows → seats per RS section
		const rsSectionList: any[] = (sections ?? []).filter((s: any) => s.type === 'RS');
		const rsSections = await Promise.all(
			rsSectionList.map(async (sec: any) => {
				const rows = await apiFetch<any[]>(
					fetch,
					`/api/ops/venues/sections/${sec.id}/rows`,
					{ headers: { Authorization: `Bearer ${accessToken}` } }
				).catch(() => [] as any[]);

				const rowsWithSeats = await Promise.all(
					rows.map(async (row: any) => {
						const seats = await apiFetch<any[]>(fetch, `/api/ops/venues/rows/${row.id}/seats`, {
							headers: { Authorization: `Bearer ${accessToken}` }
						}).catch(() => [] as any[]);
						return { ...row, seats };
					})
				);

				return {
					id: sec.id,
					name: sec.name ?? null,
					color: sec.color ?? null,
					levelId: sec.levelId ?? null,
					polygon: (sec.uiData?.polygon as [number, number][] | undefined) ?? [],
					rows: rowsWithSeats
				};
			})
		);

		// Build gaSections from GA sections (no rows/seats)
		const gaSections = (sections ?? [])
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
				height: (sec.uiData?.height as number | undefined) ?? 100
			}));

		return {
			isNew: false,
			venue,
			manifest,
			levels: levels ?? [],
			sections: sections ?? [],
			priceLevels: priceLevels ?? [],
			gaSections,
			rsSections
		};
	} catch (err: any) {
		console.error('[OPS MANIFEST EDITOR LOAD ERROR]:', err);
		throw redirect(303, `/ops/venues/${venueId}/manifests`);
	}
};
