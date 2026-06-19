import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

const INITIAL_LOGS = [
	{
		id: 'log-1',
		timestamp: '2026-06-19T02:15:30Z',
		adminEmail: 'admin@ticketpeak.com',
		category: 'FEES',
		action: 'UPDATE_FEES',
		details: 'Updated secondary resale commission fee to 7.5%.'
	},
	{
		id: 'log-2',
		timestamp: '2026-06-19T01:10:45Z',
		adminEmail: 'moderator.one@ticketpeak.com',
		category: 'EVENTS',
		action: 'POSTPONE_EVENT',
		details: 'Postponed event "Đen Vâu Live Concert 2026" (ID: 884c19) due to scheduling conflict.'
	},
	{
		id: 'log-3',
		timestamp: '2026-06-18T14:32:05Z',
		adminEmail: 'admin@ticketpeak.com',
		category: 'PAYOUTS',
		action: 'APPROVE_PAYOUT',
		details: 'Marked payout for Tran Thi B (ID: 3c41a9e5) as PAID.'
	},
	{
		id: 'log-4',
		timestamp: '2026-06-18T09:20:11Z',
		adminEmail: 'admin@ticketpeak.com',
		category: 'USER_MANAGEMENT',
		action: 'SUSPEND_USER',
		details: 'Suspended user scalper.alert@gmail.com for malicious activity on ticket transfers.'
	}
];

export const load: PageServerLoad = async ({ cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	return {
		logs: INITIAL_LOGS
	};
};
