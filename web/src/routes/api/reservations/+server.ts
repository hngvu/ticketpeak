/* eslint-disable @typescript-eslint/no-explicit-any */
import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { apiFetch } from '$lib/server/api';

export const POST: RequestHandler = async ({ request, cookies, fetch }) => {
	const accessToken = cookies.get('access_token');

	if (!accessToken) {
		return json(
			{
				success: false,
				error: 'UNAUTHORIZED',
				message: 'Please log in to your account to reserve tickets.'
			},
			{ status: 401 }
		);
	}

	try {
		const payload = await request.json();

		if (!payload.eventId || !payload.items || !payload.items.length) {
			return json(
				{
					success: false,
					message: 'Event ID and ticket items are required.'
				},
				{ status: 400 }
			);
		}

		// Forward the reservation payload to the Spring Boot backend
		const data = await apiFetch<any>(fetch, '/api/reservations', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				Authorization: `Bearer ${accessToken}`
			},
			body: JSON.stringify(payload)
		});

		return json({
			success: true,
			data
		});
	} catch (err: any) {
		console.error('[RESERVATIONS PROXY ERROR]:', err);
		return json(
			{
				success: false,
				message: err.message || 'Failed to complete reservation.'
			},
			{ status: 500 }
		);
	}
};
