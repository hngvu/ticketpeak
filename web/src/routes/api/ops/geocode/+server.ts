/* eslint-disable @typescript-eslint/no-explicit-any */
import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';

export const GET: RequestHandler = async ({ url }) => {
	const q = url.searchParams.get('q');
	if (!q || q.trim().length < 3) {
		return json({ success: true, data: [] });
	}

	try {
		const res = await fetch(
			`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(q)}&limit=5&addressdetails=1`,
			{
				headers: {
					'User-Agent': 'Ticketpeak-Admin-App/1.0 (contact@ticketpeak.com)'
				}
			}
		);

		if (!res.ok) {
			throw new Error(`Nominatim error: ${res.statusText}`);
		}

		const data = await res.json();
		if (!Array.isArray(data)) {
			return json({ success: true, data: [] });
		}

		const mapped = data.map((item: any) => {
			const addr = item.address || {};
			const street = `${addr.house_number || ''} ${addr.road || ''}`.trim();
			const city = addr.city || addr.town || addr.village || addr.county || addr.state || 'Unknown';
			const country = addr.country || 'Unknown';

			return {
				display: item.display_name,
				address: street || item.display_name.split(',')[0],
				city,
				country,
				lat: parseFloat(item.lat),
				lng: parseFloat(item.lon)
			};
		});

		return json({ success: true, data: mapped });
	} catch (err: any) {
		console.error('[GEOCODE ERROR]:', err);
		return json({ success: false, message: err.message || 'Geocoding failed' }, { status: 500 });
	}
};
