import { redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';

const INITIAL_RESALES = [
	{
		id: 'res-491a82',
		eventName: 'Sơn Tùng M-TP Concert "Sky Decades"',
		ticketCode: 'TKT-992-881',
		originalPrice: 1200000,
		resalePrice: 1500000,
		sellerEmail: 'seller.one@gmail.com',
		buyerEmail: 'buyer.one@yahoo.com',
		status: 'COMPLETED',
		commissionEarned: 75000,
		createdAt: '2026-06-18T10:14:22Z'
	},
	{
		id: 'res-293b71',
		eventName: 'Đại Nhạc Hội Rhapsody 2026',
		ticketCode: 'TKT-104-582',
		originalPrice: 2400000,
		resalePrice: 3200000, // exceeds standard price cap warning
		sellerEmail: 'scalper.alert@gmail.com',
		buyerEmail: null,
		status: 'PENDING',
		commissionEarned: 160000,
		createdAt: '2026-06-19T03:30:00Z'
	},
	{
		id: 'res-884c19',
		eventName: 'Đen Vâu Live Concert 2026',
		ticketCode: 'TKT-492-302',
		originalPrice: 950000,
		resalePrice: 950000,
		sellerEmail: 'fan.fair@outlook.com',
		buyerEmail: null,
		status: 'SUSPENDED',
		commissionEarned: 0,
		createdAt: '2026-06-17T08:12:00Z'
	}
];

export const load: PageServerLoad = async ({ cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	let resalesStr = cookies.get('ops_mock_resales');
	let resales = INITIAL_RESALES;
	if (resalesStr) {
		try {
			resales = JSON.parse(resalesStr);
		} catch {
			resales = INITIAL_RESALES;
		}
	} else {
		cookies.set('ops_mock_resales', JSON.stringify(resales), { path: '/', maxAge: 60 * 60 * 24 });
	}

	return {
		resales
	};
};

export const actions: Actions = {
	updateResaleStatus: async ({ request, cookies }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;
		const status = data.get('status') as string;

		let resalesStr = cookies.get('ops_mock_resales');
		let resales = INITIAL_RESALES;
		if (resalesStr) {
			try {
				resales = JSON.parse(resalesStr);
			} catch {
				resales = INITIAL_RESALES;
			}
		}

		resales = resales.map((r) => {
			if (r.id === id) {
				return { ...r, status };
			}
			return r;
		});

		cookies.set('ops_mock_resales', JSON.stringify(resales), { path: '/', maxAge: 60 * 60 * 24 });

		return { success: true, message: `Resale listing ${id} status updated to ${status}.` };
	}
};
