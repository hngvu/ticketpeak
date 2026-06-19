import { redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';

export const load: PageServerLoad = async ({ cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	let feesStr = cookies.get('ops_mock_fees');
	let fees = {
		flatFee: 10000,
		percentageFee: 5.0,
		resaleFee: 7.5,
		payoutFee: 11000
	};

	if (feesStr) {
		try {
			fees = JSON.parse(feesStr);
		} catch {
			// ignore
		}
	} else {
		cookies.set('ops_mock_fees', JSON.stringify(fees), { path: '/', maxAge: 60 * 60 * 24 });
	}

	return {
		fees
	};
};

export const actions: Actions = {
	saveFees: async ({ request, cookies }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const flatFee = Number(data.get('flatFee'));
		const percentageFee = Number(data.get('percentageFee'));
		const resaleFee = Number(data.get('resaleFee'));
		const payoutFee = Number(data.get('payoutFee'));

		const fees = { flatFee, percentageFee, resaleFee, payoutFee };
		cookies.set('ops_mock_fees', JSON.stringify(fees), { path: '/', maxAge: 60 * 60 * 24 });

		return { success: true, message: 'Platform commission and transaction fees successfully updated.' };
	}
};
