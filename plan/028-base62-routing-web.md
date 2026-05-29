# 028 — Base62 Routing — Web

## Description

Re-implement event and attraction (artist) detail routing on the browser URL bar to utilize short Base62 compressed IDs (~22 characters) instead of full 36-character raw UUIDs, while transparently decoding them back to UUIDs before calling the Spring Boot backend REST APIs.

Specifically:
- Artist details path on browser: `/[slug]/artist/[base62_id]`
- Event details path on browser: `/[slug]/event/[base62_id]`
- Bidirectional utility `web/src/lib/base62.ts` will translate UUIDs to/from Base62.
- SvelteKit load functions will decode the incoming `id` parameter before making backend API requests.
- All links to artist and event details in components (`EventCard`, `EventCardHorizontal`, `EventLineup`, canonical links) will encode the standard UUIDs to Base62.

---

## Files Liên Quan

| File | Thay đổi |
|---|---|
| [NEW] `web/src/lib/base62.ts` | Tạo helper utility mã hóa / giải mã UUID <-> Base62 |
| `web/src/routes/[slug]/artist/[id]/+page.server.ts` | Giải mã `params.id` từ Base62 sang UUID trước khi gọi API |
| `web/src/routes/[slug]/event/[id]/+page.server.ts` | Giải mã `params.id` từ Base62 sang UUID trước khi gọi API |
| `web/src/lib/components/catalog/EventCard.svelte` | Cập nhật link dẫn tới event page dùng Base62 ID |
| `web/src/lib/components/catalog/EventCardHorizontal.svelte` | Cập nhật link dẫn tới event page dùng Base62 ID |
| `web/src/lib/components/event/EventLineup.svelte` | Cập nhật link dẫn tới artist page dùng Base62 ID |
| `web/src/routes/[slug]/artist/[id]/+page.svelte` | Cập nhật link canonical dùng Base62 ID |
| `web/src/routes/[slug]/event/[id]/+page.svelte` | Cập nhật link canonical dùng Base62 ID |

---

## Base62 Math Utility Specification

We map the 128-bit number represented by a standard hexadecimal UUID string to and from a Base62 string using JS `BigInt`.

```typescript
const ALPHABET = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';

export function encodeUuidToBase62(uuid: string): string {
	if (!uuid) return '';
	// If it is already a mock ID or not a valid UUID, return as-is
	const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
	if (!uuidRegex.test(uuid)) {
		return uuid;
	}

	const hex = uuid.replace(/-/g, '');
	let num = BigInt('0x' + hex);
	let encoded = '';
	while (num > 0n) {
		encoded = ALPHABET[Number(num % 62n)] + encoded;
		num /= 62n;
	}
	return encoded || '0';
}

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
```

---

## Acceptance Criteria

- [ ] SvelteKit builds successfully and passes `npm run check` and `npm run lint` cleanly.
- [ ] Bidirectional encoding/decoding is completely lossless and deterministic for standard UUIDs (including UUID v7).
- [ ] Detail page server loaders successfully decode incoming Base62 ID parameters before making parallel SSR fetches.
- [ ] Fallbacks are preserved: if a mock ID (e.g. `artist-taylor-swift`) is provided, or a direct standard UUID is typed into the URL, the load function handles it perfectly without throwing an error.
- [ ] Dynamic URLs on all listings (`EventCard`, `EventCardHorizontal`, `EventLineup`, search results, canonical meta tags) correctly format links using `/[slug]/event/[base62_id]` or `/[slug]/artist/[base62_id]`.
- [ ] Navigating between pages (e.g., clicking on an event in search or lineup) functions perfectly on the browser, displaying the sleek, short Base62 ID.

---

## Status

`planned`

## Outcome

_To be filled after implementation._
