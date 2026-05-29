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
				message: 'Authentication is required to proceed with payments.'
			},
			{ status: 401 }
		);
	}

	try {
		const payload = await request.json();

		if (!payload.reservationId || !payload.provider) {
			return json(
				{
					success: false,
					message: 'Reservation ID and payment provider are required.'
				},
				{ status: 400 }
			);
		}

		// Forward checkout payment intent to Spring Boot backend
		const data = await apiFetch<any>(fetch, '/api/payments', {
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
		console.error('[PAYMENTS INITIATION PROXY ERROR]:', err);
		return json(
			{
				success: false,
				message: err.message || 'Could not initiate payment.'
			},
			{ status: 500 }
		);
	}
};
