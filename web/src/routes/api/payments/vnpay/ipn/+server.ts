/* eslint-disable @typescript-eslint/no-explicit-any */
import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import crypto from 'crypto';
import { env } from '$env/dynamic/private';

const API_BASE = env.API_BASE || 'http://localhost:8080';
const VNPAY_DEV_SECRET = '3E4DFD39EE75E5903C187F4A128B4F21';

export const POST: RequestHandler = async ({ request, fetch }) => {
	try {
		const { paymentId, amount } = await request.json();

		if (!paymentId || !amount) {
			return json(
				{
					success: false,
					message: 'paymentId and amount are required for simulation.'
				},
				{ status: 400 }
			);
		}

		// 1. Compile VNPay params
		const params: Record<string, string> = {
			vnp_TxnRef: paymentId,
			vnp_Amount: Math.round(amount * 100).toString(), // VNPay expects amount * 100
			vnp_ResponseCode: '00',
			vnp_TransactionStatus: '00',
			vnp_TransactionNo: 'mock_tx_' + Math.random().toString(36).substring(7)
		};

		// 2. Sort keys alphabetically
		const sortedKeys = Object.keys(params).sort();

		// 3. Join into hash data query string
		const hashData = sortedKeys.map((key) => `${key}=${params[key]}`).join('&');

		// 4. Compute HMAC-SHA512 signature using developer secret
		const secureHash = crypto.createHmac('sha512', VNPAY_DEV_SECRET).update(hashData).digest('hex');

		// 5. Append hash to params
		params.vnp_SecureHash = secureHash;

		// 6. Forward form parameters as request parameters to Spring Boot backend
		const backendUrl = new URL(`${API_BASE}/api/payments/vnpay/ipn`);
		Object.entries(params).forEach(([k, v]) => {
			backendUrl.searchParams.append(k, v);
		});

		const res = await fetch(backendUrl.toString(), {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			}
		});

		const text = await res.text();
		let result: any = {};
		try {
			result = JSON.parse(text);
		} catch {
			result = { message: text };
		}

		if (!res.ok) {
			throw new Error(result.message || 'Simulation gateway rejected payment.');
		}

		return json({
			success: true,
			data: result
		});
	} catch (err: any) {
		console.error('[VNPAY SIMULATION FAILURE]:', err);
		return json(
			{
				success: false,
				message: err.message || 'Payment simulation failed.'
			},
			{ status: 500 }
		);
	}
};
