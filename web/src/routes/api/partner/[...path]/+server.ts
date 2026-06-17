/* eslint-disable @typescript-eslint/no-explicit-any */
import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { apiFetch } from '$lib/server/api';

export const GET: RequestHandler = async ({ params, url, fetch, cookies }) => {
	const path = params.path;
	const accessToken = cookies.get('b2b_access_token') || cookies.get('access_token');

	try {
		const results = await apiFetch<any>(fetch, `/api/partner/${path}?${url.searchParams.toString()}`, {
			headers: {
				Authorization: accessToken ? `Bearer ${accessToken}` : ''
			}
		});
		return json({ success: true, data: results });
	} catch (err: any) {
		return json({ success: false, message: err.message || 'Request failed' }, { status: 500 });
	}
};

export const POST: RequestHandler = async ({ params, url, fetch, cookies, request }) => {
	const path = params.path;
	const accessToken = cookies.get('b2b_access_token') || cookies.get('access_token');

	try {
		let body: any = undefined;
		const contentType = request.headers.get('content-type') || '';
		if (contentType.includes('application/json')) {
			body = JSON.stringify(await request.json());
		} else if (contentType) {
			body = await request.text();
		}

		const results = await apiFetch<any>(fetch, `/api/partner/${path}?${url.searchParams.toString()}`, {
			method: 'POST',
			headers: {
				'Content-Type': contentType || 'application/json',
				Authorization: accessToken ? `Bearer ${accessToken}` : ''
			},
			body
		});
		return json({ success: true, data: results });
	} catch (err: any) {
		return json({ success: false, message: err.message || 'Request failed' }, { status: 500 });
	}
};

export const DELETE: RequestHandler = async ({ params, url, fetch, cookies, request }) => {
	const path = params.path;
	const accessToken = cookies.get('b2b_access_token') || cookies.get('access_token');

	try {
		let body: any = undefined;
		const contentType = request.headers.get('content-type') || '';
		if (contentType.includes('application/json')) {
			body = JSON.stringify(await request.json());
		} else if (contentType) {
			body = await request.text();
		}

		const results = await apiFetch<any>(fetch, `/api/partner/${path}?${url.searchParams.toString()}`, {
			method: 'DELETE',
			headers: {
				'Content-Type': contentType || 'application/json',
				Authorization: accessToken ? `Bearer ${accessToken}` : ''
			},
			body
		});
		return json({ success: true, data: results });
	} catch (err: any) {
		return json({ success: false, message: err.message || 'Request failed' }, { status: 500 });
	}
};
