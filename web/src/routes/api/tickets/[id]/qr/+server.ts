/* eslint-disable @typescript-eslint/no-explicit-any */
import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { apiFetch } from '$lib/server/api';

export const GET: RequestHandler = async ({ params, cookies, fetch }) => {
	const { id } = params;
	const accessToken = cookies.get('access_token');

	if (!accessToken) {
		return json(
			{
				success: false,
				error: 'UNAUTHORIZED',
				message: 'Access token is required to load dynamic QR codes.'
			},
			{ status: 401 }
		);
	}

	try {
		// Fetch secure rotating OTP data for the specific ticket
		const data = await apiFetch<any>(fetch, `/api/tickets/${id}/qr`, {
			method: 'GET',
			headers: {
				Authorization: `Bearer ${accessToken}`
			}
		});

		return json({
			success: true,
			data
		});
	} catch (err: any) {
		console.error('[TICKET QR PROXY ERROR]:', err);
		return json(
			{
				success: false,
				message: err.message || 'Could not retrieve ticket QR code.'
			},
			{ status: 500 }
		);
	}
};
