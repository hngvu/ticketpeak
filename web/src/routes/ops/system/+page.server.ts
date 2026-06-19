import { redirect } from '@sveltejs/kit';
import type { PageServerLoad, Actions } from './$types';

export const load: PageServerLoad = async ({ cookies }) => {
	const accessToken = cookies.get('ops_access_token');
	if (!accessToken) {
		throw redirect(303, '/ops/login');
	}

	let systemStateStr = cookies.get('ops_mock_system');
	let systemState = {
		cpuUsage: 14,
		heapUsedMb: 342,
		heapMaxMb: 1024,
		postgresConnections: 8,
		redisCacheHits: 98.4,
		minioBucketsCount: 3,
		minioStorageUsedBytes: 15482910,
		garbageCollectedCount: 0
	};

	if (systemStateStr) {
		try {
			systemState = JSON.parse(systemStateStr);
		} catch {
			// ignore
		}
	} else {
		cookies.set('ops_mock_system', JSON.stringify(systemState), { path: '/', maxAge: 60 * 60 * 24 });
	}

	return {
		systemState
	};
};

export const actions: Actions = {
	triggerGc: async ({ cookies }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		let systemStateStr = cookies.get('ops_mock_system');
		let systemState = {
			cpuUsage: 14,
			heapUsedMb: 342,
			heapMaxMb: 1024,
			postgresConnections: 8,
			redisCacheHits: 98.4,
			minioBucketsCount: 3,
			minioStorageUsedBytes: 15482910,
			garbageCollectedCount: 0
		};

		if (systemStateStr) {
			try {
				systemState = JSON.parse(systemStateStr);
			} catch {
				// ignore
			}
		}

		systemState.heapUsedMb = Math.max(90, Math.floor(systemState.heapUsedMb * 0.45));
		systemState.garbageCollectedCount += 1;
		systemState.cpuUsage = 45; // momentarily spike during GC

		cookies.set('ops_mock_system', JSON.stringify(systemState), { path: '/', maxAge: 60 * 60 * 24 });

		return { success: true, message: 'JVM Garbage Collection requested. Cleared heap memory.' };
	},

	clearRedis: async ({ cookies }) => {
		const accessToken = cookies.get('ops_access_token');
		if (!accessToken) throw redirect(303, '/ops/login');

		let systemStateStr = cookies.get('ops_mock_system');
		let systemState = {
			cpuUsage: 14,
			heapUsedMb: 342,
			heapMaxMb: 1024,
			postgresConnections: 8,
			redisCacheHits: 98.4,
			minioBucketsCount: 3,
			minioStorageUsedBytes: 15482910,
			garbageCollectedCount: 0
		};

		if (systemStateStr) {
			try {
				systemState = JSON.parse(systemStateStr);
			} catch {
				// ignore
			}
		}

		systemState.redisCacheHits = 0.0; // reset cache hits as cache is cold now

		cookies.set('ops_mock_system', JSON.stringify(systemState), { path: '/', maxAge: 60 * 60 * 24 });

		return { success: true, message: 'Redis Cache flushed system-wide.' };
	}
};
