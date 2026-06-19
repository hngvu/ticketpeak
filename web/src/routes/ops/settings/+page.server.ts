import { redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';

export const load: PageServerLoad = async ({ cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	let settingsStr = cookies.get('ops_mock_settings');
	let settings = {
		priceCapPercent: 150,
		maxTransfersPerTicket: 2,
		kycRequiredForSeller: true,
		mfaRequiredForAdmin: false,
		totpQrExpirySeconds: 30
	};

	if (settingsStr) {
		try {
			settings = JSON.parse(settingsStr);
		} catch {
			// ignore
		}
	} else {
		cookies.set('ops_mock_settings', JSON.stringify(settings), { path: '/', maxAge: 60 * 60 * 24 });
	}

	return {
		settings
	};
};

export const actions: Actions = {
	saveSettings: async ({ request, cookies }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const priceCapPercent = Number(data.get('priceCapPercent'));
		const maxTransfersPerTicket = Number(data.get('maxTransfersPerTicket'));
		const kycRequiredForSeller = data.get('kycRequiredForSeller') === 'true';
		const mfaRequiredForAdmin = data.get('mfaRequiredForAdmin') === 'true';
		const totpQrExpirySeconds = Number(data.get('totpQrExpirySeconds'));

		const settings = {
			priceCapPercent,
			maxTransfersPerTicket,
			kycRequiredForSeller,
			mfaRequiredForAdmin,
			totpQrExpirySeconds
		};

		cookies.set('ops_mock_settings', JSON.stringify(settings), { path: '/', maxAge: 60 * 60 * 24 });

		return { success: true, message: 'Platform security and scaling regulations successfully updated.' };
	}
};
