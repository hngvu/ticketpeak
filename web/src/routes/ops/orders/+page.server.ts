import { redirect, fail } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';

// Seed mock orders
const INITIAL_ORDERS = [
	{
		id: 'ord-8f92b7c4',
		customerName: 'Nguyễn Văn A',
		customerEmail: 'nva@gmail.com',
		eventName: 'Đại Nhạc Hội Rhapsody 2026',
		eventDate: '2026-07-15T19:30:00Z',
		tickets: 'VIP Zone A x2',
		totalAmount: 4800000,
		status: 'PAID',
		paymentMethod: 'MOMO',
		createdAt: '2026-06-18T10:14:22Z'
	},
	{
		id: 'ord-3c41a9e5',
		customerName: 'Trần Thị B',
		customerEmail: 'ttb@outlook.com',
		eventName: 'Sơn Tùng M-TP Concert "Sky Decades"',
		eventDate: '2026-08-20T20:00:00Z',
		tickets: 'GA Standard x3',
		totalAmount: 2700000,
		status: 'PAID',
		paymentMethod: 'VNPAY',
		createdAt: '2026-06-18T14:32:05Z'
	},
	{
		id: 'ord-7d82e1b9',
		customerName: 'Phạm Minh C',
		customerEmail: 'pmc@yahoo.com',
		eventName: 'Đen Vâu Live Concert 2026',
		eventDate: '2026-09-05T19:00:00Z',
		tickets: 'Standing Zone B x1',
		totalAmount: 950000,
		status: 'PENDING',
		paymentMethod: 'BANK_TRANSFER',
		createdAt: '2026-06-19T01:10:45Z'
	},
	{
		id: 'ord-5a11d2c3',
		customerName: 'Lê Hoàng D',
		customerEmail: 'lhd@gmail.com',
		eventName: 'Đại Nhạc Hội Rhapsody 2026',
		eventDate: '2026-07-15T19:30:00Z',
		tickets: 'VVIP Premium Lounge x1',
		totalAmount: 5000000,
		status: 'REFUNDED',
		paymentMethod: 'VNPAY',
		createdAt: '2026-06-17T09:20:11Z'
	},
	{
		id: 'ord-9b22e3f4',
		customerName: 'Hoàng Thị E',
		customerEmail: 'hte@gmail.com',
		eventName: 'Sơn Tùng M-TP Concert "Sky Decades"',
		eventDate: '2026-08-20T20:00:00Z',
		tickets: 'VIP Zone B x2',
		totalAmount: 3600000,
		status: 'CANCELLED',
		paymentMethod: 'MOMO',
		createdAt: '2026-06-16T15:40:00Z'
	}
];

export const load: PageServerLoad = async ({ cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	let ordersStr = cookies.get('ops_mock_orders');
	let orders = INITIAL_ORDERS;
	if (ordersStr) {
		try {
			orders = JSON.parse(ordersStr);
		} catch {
			orders = INITIAL_ORDERS;
		}
	} else {
		cookies.set('ops_mock_orders', JSON.stringify(orders), { path: '/', maxAge: 60 * 60 * 24 });
	}

	return {
		orders
	};
};

export const actions: Actions = {
	updateStatus: async ({ request, cookies }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		const data = await request.formData();
		const id = data.get('id') as string;
		const status = data.get('status') as string;

		let ordersStr = cookies.get('ops_mock_orders');
		let orders = INITIAL_ORDERS;
		if (ordersStr) {
			try {
				orders = JSON.parse(ordersStr);
			} catch {
				orders = INITIAL_ORDERS;
			}
		}

		orders = orders.map((o) => {
			if (o.id === id) {
				return { ...o, status };
			}
			return o;
		});

		cookies.set('ops_mock_orders', JSON.stringify(orders), { path: '/', maxAge: 60 * 60 * 24 });

		return { success: true, message: `Order ${id} status updated to ${status}.` };
	}
};
