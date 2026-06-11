/* eslint-disable @typescript-eslint/no-explicit-any */
export interface User {
	id: string;
	roles: string[];
}

export function parseJwt(token: string): any {
	try {
		const parts = token.split('.');
		if (parts.length !== 3) return null;
		const payload = parts[1];
		const decoded = Buffer.from(payload, 'base64').toString('utf-8');
		return JSON.parse(decoded);
	} catch {
		return null;
	}
}

export function parseUserFromToken(token: string): User | null {
	const parsed = parseJwt(token);
	if (!parsed) return null;
	if (parsed.exp && Date.now() >= parsed.exp * 1000) return null;
	if (!parsed.sub || !parsed.role) return null;
	return {
		id: parsed.sub,
		roles: parsed.role.split(',').map((r: string) => r.trim())
	};
}

export function getCurrentUser(
	cookies: import('@sveltejs/kit').Cookies,
	tokenKey: string = 'access_token'
): User | null {
	const token = cookies.get(tokenKey);
	if (!token) return null;
	return parseUserFromToken(token);
}
