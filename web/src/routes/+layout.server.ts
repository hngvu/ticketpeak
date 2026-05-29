/* eslint-disable @typescript-eslint/no-explicit-any */
import type { LayoutServerLoad } from './$types';
import { apiFetch } from '$lib/server/api';

export const load: LayoutServerLoad = async ({ cookies, locals, fetch, url }) => {
	const preferredCity = cookies.get('preferred_city') || 'ho-chi-minh';
	const preferredCountry = cookies.get('preferred_country') || 'vn';
	const preferredLocale =
		cookies.get('preferred_locale') || (preferredCountry === 'us' ? 'en-US' : 'vi-VN');
	const preferredLang = cookies.get('preferred_lang') || (preferredCountry === 'us' ? 'en' : 'vi');
	const user = locals.user;

	const pathname = url.pathname;
	let accessKey = 'access_token';
	if (pathname.startsWith('/b2b')) {
		accessKey = 'b2b_access_token';
	} else if (pathname.startsWith('/ops')) {
		accessKey = 'ops_access_token';
	}
	const accessToken = cookies.get(accessKey);

	let profile: any = null;
	if (user && accessToken) {
		try {
			profile = await apiFetch<any>(fetch, '/api/accounts/me', {
				headers: {
					Authorization: `Bearer ${accessToken}`
				}
			});
		} catch (err) {
			console.error('[LAYOUT SERVER PROFILE LOAD ERROR]:', err);
		}
	}

	return {
		preferredCity,
		preferredCountry,
		preferredCountryName: preferredCountry === 'us' ? 'United States' : 'Việt Nam',
		preferredCountryCode: preferredCountry === 'us' ? 'US' : 'VN',
		preferredLocale,
		preferredLang,
		user,
		profile
	};
};
