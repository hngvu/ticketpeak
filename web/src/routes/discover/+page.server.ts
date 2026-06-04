/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';

export const load: PageServerLoad = async ({ fetch, parent }) => {
	const { preferredCity } = await parent();

	// Calculate weekend boundaries (Friday 16:00 to Sunday 23:59)
	const now = new Date();
	const day = now.getDay();

	const fri = new Date(now);
	const daysUntilFriday = (5 - day + 7) % 7;
	fri.setDate(now.getDate() + (daysUntilFriday === 0 && day !== 5 ? 7 : daysUntilFriday));
	fri.setHours(16, 0, 0, 0);

	const sun = new Date(now);
	const daysUntilSunday = (0 - day + 7) % 7;
	sun.setDate(now.getDate() + (daysUntilSunday === 0 && day !== 0 ? 7 : daysUntilSunday));
	sun.setHours(23, 59, 59, 999);

	// If we are currently in the weekend (Fri evening, Sat, Sun), start from now
	const startAfter = day === 5 || day === 6 || day === 0 ? now.toISOString() : fri.toISOString();
	const startBefore = sun.toISOString();

	let classifications: any[] = [];
	let concertsSegmentId: string | undefined;

	try {
		// Fetch categories to dynamically map segment names
		const apiClassifications = await apiFetch<any[]>(fetch, '/api/classifications');
		if (apiClassifications && apiClassifications.length > 0) {
			classifications = apiClassifications;
			const concertsSegment = classifications.find(
				(c) => c.level === 1 && c.slug.toLowerCase().includes('concert')
			);
			concertsSegmentId = concertsSegment?.id;
		}
	} catch (err) {
		console.error('[Discover Classifications Load Error]:', err);
	}

	// Prepare parallel promises
	const trendingPromise = apiFetch<PageResponse<any>>(
		fetch,
		'/api/events?sort=startAt,asc&size=12'
	).catch(() => ({ content: [] }));

	const weekendPromise = apiFetch<PageResponse<any>>(
		fetch,
		`/api/events?startAfter=${encodeURIComponent(startAfter)}&startBefore=${encodeURIComponent(startBefore)}&size=12`
	).catch(() => ({ content: [] }));

	const concertsPromise = concertsSegmentId
		? apiFetch<PageResponse<any>>(
				fetch,
				`/api/events?classificationId=${concertsSegmentId}&city=${encodeURIComponent(preferredCity)}&size=12`
			).catch(() => ({ content: [] }))
		: Promise.resolve({ content: [] });

	const venuesPromise = apiFetch<PageResponse<any>>(fetch, `/api/venues?size=6`).catch(() => ({
		content: []
	}));

	// Await all results
	const [trendingData, weekendData, concertsData, venuesData] = await Promise.all([
		trendingPromise,
		weekendPromise,
		concertsPromise,
		venuesPromise
	]);

	return {
		trendingEvents: trendingData.content || [],
		weekendEvents: weekendData.content || [],
		concertEvents: concertsData.content || [],
		venues: venuesData.content || [],
		classifications
	};
};
