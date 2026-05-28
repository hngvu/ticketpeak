import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = ({ cookies }) => {
	const preferredCity = cookies.get('preferred_city') || 'ho-chi-minh';
	const preferredCountry = cookies.get('preferred_country') || 'vn';
	const preferredLocale =
		cookies.get('preferred_locale') || (preferredCountry === 'us' ? 'en-US' : 'vi-VN');
	const preferredLang = cookies.get('preferred_lang') || (preferredCountry === 'us' ? 'en' : 'vi');
	return {
		preferredCity,
		preferredCountry,
		preferredCountryName: preferredCountry === 'us' ? 'United States' : 'Việt Nam',
		preferredCountryCode: preferredCountry === 'us' ? 'US' : 'VN',
		preferredLocale,
		preferredLang
	};
};
