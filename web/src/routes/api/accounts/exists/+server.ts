/* eslint-disable @typescript-eslint/no-explicit-any */
import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { apiFetch } from '$lib/server/api';

export const GET: RequestHandler = async ({ url, fetch }) => {
	const email = url.searchParams.get('email');

	if (!email) {
		return json({ success: false, message: 'Email is required' }, { status: 400 });
	}

	try {
		const exists = await apiFetch<boolean>(
			fetch,
			`/api/accounts/exists?email=${encodeURIComponent(email)}`
		);
		return json({ success: true, data: exists });
	} catch (err: any) {
		return json({ success: false, message: err.message || 'Check failed' }, { status: 500 });
	}
};
