import { redirect } from '@sveltejs/kit';
import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = async ({ locals, url }) => {
	const user = locals.user;

	// Enforce organizer role guard on nested pages, excluding landing page and login page
	const pathname = url.pathname;
	if (pathname !== '/b2b' && pathname !== '/b2b/login') {
		if (!user || user.role !== 'ORGANIZER') {
			throw redirect(303, '/b2b/login');
		}
	}

	return {
		user
	};
};
