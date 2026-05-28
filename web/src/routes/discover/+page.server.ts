/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PageServerLoad } from './$types';
import { apiFetch, type PageResponse } from '$lib/server/api';
import { MOCK_EVENTS, MOCK_VENUES, MOCK_CLASSIFICATIONS } from '$lib/server/mockData';

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

	let classifications = MOCK_CLASSIFICATIONS;
	let concertsSegmentId: string | undefined = 'segment-concerts';

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
	} catch {
		// fallback to mock
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

	// Extract active lists
	let trendingEvents = trendingData.content || [];
	let weekendEvents = weekendData.content || [];
	let concertEvents = concertsData.content || [];
	let venues = venuesData.content || [];

	// Fallback to high-fidelity mock data if database is empty/unseeded
	if (trendingEvents.length === 0) {
		trendingEvents = MOCK_EVENTS.slice(0, 12);
	}
	if (weekendEvents.length === 0) {
		// Filter events that fall on the weekend (Saturday or Sunday)
		weekendEvents = MOCK_EVENTS.filter((e) => {
			const d = new Date(e.startAt);
			return d.getDay() === 0 || d.getDay() === 6 || d.getDay() === 5;
		});
	}
	if (concertEvents.length === 0) {
		const activeCityLabel = preferredCity === 'ho-chi-minh' ? 'Hồ Chí Minh' : 'Hà Nội';
		concertEvents = MOCK_EVENTS.filter(
			(e) =>
				e.classifications?.[0]?.slug.includes('pop') ||
				e.classifications?.[0]?.slug.includes('indie') ||
				e.cityName === activeCityLabel
		);
	}
	if (venues.length === 0) {
		venues = MOCK_VENUES;
	}

	return {
		trendingEvents,
		weekendEvents,
		concertEvents,
		venues,
		classifications
	};
};
