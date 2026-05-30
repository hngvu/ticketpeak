/* eslint-disable @typescript-eslint/no-explicit-any */
import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { apiFetch } from '$lib/server/api';

export const POST: RequestHandler = async ({ request, cookies, fetch }) => {
	const b2bToken = cookies.get('b2b_access_token');
	const opsToken = cookies.get('ops_access_token');
	const accessToken = cookies.get('access_token');
	const token = b2bToken || opsToken || accessToken;

	if (!token) {
		return json(
			{
				success: false,
				error: 'UNAUTHORIZED',
				message: 'Authentication is required to request upload credentials.'
			},
			{ status: 401 }
		);
	}

	try {
		const payload = await request.json();

		if (!payload.fileName || !payload.contentType) {
			return json(
				{
					success: false,
					message: 'fileName and contentType are required.'
				},
				{ status: 400 }
			);
		}

		// Forward request to Spring Boot backend
		const resData = await apiFetch<any>(fetch, '/api/storage/presigned-url', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${token}`
			},
			body: JSON.stringify(payload)
		});

		return json({
			success: true,
			data: resData
		});
	} catch (err: any) {
		console.error('[STORAGE PROXY ERROR]:', err);
		return json(
			{
				success: false,
				message: err.message || 'Failed to generate upload URL.'
			},
			{ status: 500 }
		);
	}
};
