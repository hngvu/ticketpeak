import { redirect, type Handle } from '@sveltejs/kit';
import { getCurrentUser, parseJwt } from '$lib/server/auth';
import { env } from '$env/dynamic/private';

const API_BASE = env.API_BASE || 'http://localhost:8080';

export const handle: Handle = async ({ event, resolve }) => {
	const pathname = event.url.pathname;
	let accessKey = 'access_token';
	let refreshKey = 'refresh_token';

	if (pathname.startsWith('/b2b')) {
		accessKey = 'b2b_access_token';
		refreshKey = 'b2b_refresh_token';
	} else if (pathname.startsWith('/ops')) {
		accessKey = 'ops_access_token';
		refreshKey = 'ops_refresh_token';
	}

	let user = getCurrentUser(event.cookies, accessKey);
	const accessToken = event.cookies.get(accessKey);
	const refreshToken = event.cookies.get(refreshKey);

	// If access token is missing or expired, but we have a refresh token, try to refresh
	if ((!accessToken || !user) && refreshToken) {
		const parsedRefresh = parseJwt(refreshToken);
		const isRefreshExpired =
			parsedRefresh && parsedRefresh.exp && Date.now() >= parsedRefresh.exp * 1000;

		if (!isRefreshExpired) {
			try {
				const response = await fetch(`${API_BASE}/api/auth/refresh`, {
					method: 'POST',
					headers: {
						'Content-Type': 'application/json'
					},
					body: JSON.stringify({ refreshToken })
				});

				if (response.ok) {
					const json = await response.json();
					if (json.success && json.data) {
						const {
							accessToken: newAccess,
							refreshToken: newRefresh,
							accessTokenExpiresIn,
							refreshTokenExpiresIn
						} = json.data;

						// Set new cookies
						event.cookies.set(accessKey, newAccess, {
							path: '/',
							httpOnly: true,
							secure: true,
							sameSite: 'strict',
							maxAge: accessTokenExpiresIn || 900
						});

						event.cookies.set(refreshKey, newRefresh, {
							path: '/',
							httpOnly: true,
							secure: true,
							sameSite: 'strict',
							maxAge: refreshTokenExpiresIn || 604800
						});

						const parsedNew = parseJwt(newAccess);
						if (parsedNew && parsedNew.sub && parsedNew.role) {
							user = {
								id: parsedNew.sub,
								role: parsedNew.role
							};
						}
					} else {
						// refresh payload is invalid, clear cookies
						event.cookies.delete(accessKey, { path: '/' });
						event.cookies.delete(refreshKey, { path: '/' });
					}
				} else {
					// refresh failed (e.g. 401), clear cookies
					event.cookies.delete(accessKey, { path: '/' });
					event.cookies.delete(refreshKey, { path: '/' });
				}
			} catch (err) {
				console.error('[HOOKS REFRESH ERROR]:', err);
			}
		} else {
			// refresh token expired, clear both cookies
			event.cookies.delete(accessKey, { path: '/' });
			event.cookies.delete(refreshKey, { path: '/' });
		}
	}

	event.locals.user = user;

	// Route guards:
	// 1. /auth: if logged in, redirect to correct portal based on role
	if (pathname === '/auth') {
		if (user) {
			if (user.role === 'ADMIN') {
				throw redirect(303, '/ops/dashboard');
			} else if (user.role === 'ORGANIZER') {
				throw redirect(303, '/b2b/dashboard');
			} else {
				const redirectTo = event.url.searchParams.get('redirect') || '/discover';
				throw redirect(303, redirectTo);
			}
		}
	}

	// 2. /b2b/**: require role ORGANIZER except landing and login
	if (pathname.startsWith('/b2b') && pathname !== '/b2b' && pathname !== '/b2b/login') {
		if (!user || user.role !== 'ORGANIZER') {
			throw redirect(303, '/b2b/login');
		}
	}

	// 3. /ops/**: require role ADMIN except login
	if (pathname.startsWith('/ops') && pathname !== '/ops/login') {
		if (!user || user.role !== 'ADMIN') {
			throw redirect(303, '/ops/login');
		}
	}

	const response = await resolve(event);
	return response;
};
