import { redirect } from '@sveltejs/kit';
import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = async ({ locals, url }) => {
	const user = locals.user;
	const pathname = url.pathname;

	// If administrator is already logged in and goes to the /ops base path, redirect them to the dashboard
	if (pathname === '/ops' || pathname === '/ops/') {
		if (user && user.roles.includes('ADMIN')) {
			throw redirect(303, `/ops/dashboard${url.search}`);
		}
	}

	// Enforce administrator role guard on all sub-routes under /ops/ except /ops/login
	if (pathname !== '/ops/login') {
		if (!user || !user.roles.includes('ADMIN')) {
			throw redirect(303, '/ops/login');
		}
	}

	return {
		user
	};
};
