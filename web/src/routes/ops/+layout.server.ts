import { redirect } from '@sveltejs/kit';
import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = async ({ locals, url }) => {
	const user = locals.user;
	const pathname = url.pathname;

	// Enforce administrator role guard on all sub-routes under /ops/ except /ops/login
	if (pathname !== '/ops/login') {
		if (!user || user.role !== 'ADMIN') {
			throw redirect(303, '/ops/login');
		}
	}

	return {
		user
	};
};
