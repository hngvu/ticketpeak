const ALPHABET = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';

/**
 * Encodes a standard 36-character UUID string into a short Base62 string (~22 characters).
 * Returns the input unchanged if it is not a valid UUID or is a mock ID.
 */
export function encodeUuidToBase62(uuid: string): string {
	if (!uuid) return '';

	// Validate UUID format
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
	if (!uuidRegex.test(uuid)) {
		return uuid;
	}

	const hex = uuid.replace(/-/g, '').toLowerCase();
	let num = BigInt('0x' + hex);
	let encoded = '';
	while (num > 0n) {
		encoded = ALPHABET[Number(num % 62n)] + encoded;
		num /= 62n;
	}
	return encoded || '0';
}

/**
 * Decodes a Base62 string back into a standard 36-character UUID string.
 * Returns the input unchanged if it is already a UUID or is a mock ID.
 */
export function decodeBase62ToUuid(b62: string): string {
	if (!b62) return '';

	// If it is a mock ID, return as-is
	if (b62.startsWith('artist-') || b62.startsWith('team-') || b62.startsWith('event-')) {
		return b62;
	}

	// If it is already a standard UUID, return as-is
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
	if (uuidRegex.test(b62)) {
		return b62;
	}

	let num = 0n;
	for (const char of b62) {
		const index = ALPHABET.indexOf(char);
		if (index === -1) {
			throw new Error('Invalid Base62 character');
		}
		num = num * 62n + BigInt(index);
	}

	const hex = num.toString(16).padStart(32, '0');
	return [
		hex.substring(0, 8),
		hex.substring(8, 12),
		hex.substring(12, 16),
		hex.substring(16, 20),
		hex.substring(20, 32)
	].join('-');
}
