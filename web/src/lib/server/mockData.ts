// ──────────────────────────────────────────────────────────────────────────────
// Mock Entities & Responses
// Each section defines mock entities (DB-row-like) and derived response objects
// that map cleanly to backend DTOs for frontend rendering.
//
// Modules: Event, Venue, Offer, plus supporting Classifications & Attractions.
// ──────────────────────────────────────────────────────────────────────────────

// ── Helpers ──────────────────────────────────────────────────────────────────

const getFutureDate = (daysAhead: number, hours: number) => {
	const d = new Date();
	d.setDate(d.getDate() + daysAhead);
	d.setHours(hours, 0, 0, 0);
	return d.toISOString();
};

const getPastDate = (daysAgo: number, hours: number) => {
	const d = new Date();
	d.setDate(d.getDate() - daysAgo);
	d.setHours(hours, 0, 0, 0);
	return d.toISOString();
};

/** UUID v7–style helpers: produce deterministic UUIDs from a base + counter. */
const _UUID_NS = '00000000-0000-7000-0000-000000000000';

function _uuid(base: string, seq: number): string {
	const hex = seq.toString(16).padStart(12, '0');
	return `${base.slice(0, 8)}-${base.slice(8, 12)}-7${base.slice(13, 15)}-${base.slice(16, 20)}-${hex}`;
}

// ── Organization IDs (shared across events) ──────────────────────────────────

export const MOCK_ORG_IDS = {
	TP: '018f4e1a-0001-4000-8000-000000000001',
	ELITE: '018f4e1a-0002-4000-8000-000000000002'
} as const;

// ── Classifications (matches ClassificationResponse DTO) ─────────────────────

const CLS_BASE = '018f4e10-0000';

export const MOCK_CLASSIFICATIONS = [
	// Segments (level 1)
	{
		id: _uuid(CLS_BASE, 1),
		name: 'Concerts',
		slug: 'concerts',
		level: 1,
		isActive: true,
		parentId: null
	},
	{
		id: _uuid(CLS_BASE, 2),
		name: 'Sports',
		slug: 'sports',
		level: 1,
		isActive: true,
		parentId: null
	},
	{
		id: _uuid(CLS_BASE, 3),
		name: 'Arts & Theatre',
		slug: 'arts',
		level: 1,
		isActive: true,
		parentId: null
	},
	{
		id: _uuid(CLS_BASE, 4),
		name: 'Family',
		slug: 'family',
		level: 1,
		isActive: true,
		parentId: null
	},

	// Concert genres (level 2)
	{
		id: _uuid(CLS_BASE, 5),
		name: 'Pop',
		slug: 'pop',
		level: 2,
		isActive: true,
		parentId: _uuid(CLS_BASE, 1)
	},
	{
		id: _uuid(CLS_BASE, 6),
		name: 'Rock',
		slug: 'rock',
		level: 2,
		isActive: true,
		parentId: _uuid(CLS_BASE, 1)
	},
	{
		id: _uuid(CLS_BASE, 7),
		name: 'Indie & Folk',
		slug: 'indie',
		level: 2,
		isActive: true,
		parentId: _uuid(CLS_BASE, 1)
	},
	{
		id: _uuid(CLS_BASE, 8),
		name: 'EDM & Electronic',
		slug: 'edm',
		level: 2,
		isActive: true,
		parentId: _uuid(CLS_BASE, 1)
	},

	// Sports genres (level 2)
	{
		id: _uuid(CLS_BASE, 9),
		name: 'Football (Soccer)',
		slug: 'football',
		level: 2,
		isActive: true,
		parentId: _uuid(CLS_BASE, 2)
	},
	{
		id: _uuid(CLS_BASE, 10),
		name: 'Basketball',
		slug: 'basketball',
		level: 2,
		isActive: true,
		parentId: _uuid(CLS_BASE, 2)
	},
	{
		id: _uuid(CLS_BASE, 11),
		name: 'Marathons & Running',
		slug: 'running',
		level: 2,
		isActive: true,
		parentId: _uuid(CLS_BASE, 2)
	},

	// Arts genres (level 2)
	{
		id: _uuid(CLS_BASE, 12),
		name: 'Musicals & Plays',
		slug: 'musical',
		level: 2,
		isActive: true,
		parentId: _uuid(CLS_BASE, 3)
	},
	{
		id: _uuid(CLS_BASE, 13),
		name: 'Stand-Up Comedy',
		slug: 'comedy',
		level: 2,
		isActive: true,
		parentId: _uuid(CLS_BASE, 3)
	},
	{
		id: _uuid(CLS_BASE, 14),
		name: 'Classical Dance',
		slug: 'dance',
		level: 2,
		isActive: true,
		parentId: _uuid(CLS_BASE, 3)
	}
] as const;

// ── Venues (matches VenueResponse DTO) ───────────────────────────────────────
// IDs kept stable from original mock data.

export const MOCK_VENUES = [
	{
		id: '018f4e1a-0003-4000-8000-000000000001',
		name: 'Sân vận động Quốc gia Mỹ Đình',
		address: 'Đường Lê Đức Thọ, Mỹ Đình 1, Nam Từ Liêm',
		city: 'Hà Nội',
		country: 'Vietnam',
		latitude: 21.020504,
		longitude: 105.773824,
		phone: '+84 24 3785 0789',
		email: 'info@mydinhstadium.vn',
		website: 'https://mydinhstadium.vn',
		description: 'Sân vận động quốc gia lớn nhất Việt Nam, sức chứa 40.000 chỗ ngồi.',
		thumbnailUrl:
			'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=800&q=80',
		images: [],
		status: 'ACTIVE' as const,
		createdAt: '2025-06-01T08:00:00Z',
		updatedAt: '2026-01-15T10:30:00Z'
	},
	{
		id: '018f4e1a-0004-4000-8000-000000000001',
		name: 'Sân vận động Quân khu 7',
		address: '202 Hoàng Văn Thụ, Phường 9, Phú Nhuận',
		city: 'Hồ Chí Minh',
		country: 'Vietnam',
		latitude: 10.799511,
		longitude: 106.666691,
		phone: '+84 28 3844 1234',
		email: 'contact@qk7stadium.vn',
		website: null,
		description: 'Sân vận động đa năng tại trung tâm TP.HCM, sức chứa 25.000.',
		thumbnailUrl:
			'https://images.unsplash.com/photo-1487466365836-440d8294a87a?auto=format&fit=crop&w=800&q=80',
		images: [],
		status: 'ACTIVE' as const,
		createdAt: '2025-06-15T08:00:00Z',
		updatedAt: '2026-02-10T14:20:00Z'
	},
	{
		id: '018f4e1a-0005-4000-8000-000000000001',
		name: 'Nhà thi đấu Phú Thọ',
		address: '1 Lữ Gia, Phường 15, Quận 11',
		city: 'Hồ Chí Minh',
		country: 'Vietnam',
		latitude: 10.763511,
		longitude: 106.656811,
		phone: '+84 28 3865 5678',
		email: 'booking@phuthoarena.vn',
		website: 'https://phuthoarena.vn',
		description: 'Nhà thi đấu trong nhà hiện đại, phù hợp concert và sự kiện thể thao.',
		thumbnailUrl:
			'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=800&q=80',
		images: [],
		status: 'ACTIVE' as const,
		createdAt: '2025-07-20T08:00:00Z',
		updatedAt: '2026-03-01T09:00:00Z'
	},
	{
		id: '018f4e1a-0006-4000-8000-000000000001',
		name: 'Nhà hát Lớn Hà Nội',
		address: '1 Tràng Tiền, Phan Chu Trinh, Hoàn Kiếm',
		city: 'Hà Nội',
		country: 'Vietnam',
		latitude: 21.024222,
		longitude: 105.855833,
		phone: '+84 24 3933 1488',
		email: 'info@hanoiopera.org.vn',
		website: 'https://hanoiopera.org.vn',
		description: 'Công trình kiến trúc Pháp cổ điển, trung tâm văn hóa nghệ thuật Hà Nội.',
		thumbnailUrl:
			'https://images.unsplash.com/photo-1503095396549-807759245b35?auto=format&fit=crop&w=800&q=80',
		images: [],
		status: 'ACTIVE' as const,
		createdAt: '2025-05-01T08:00:00Z',
		updatedAt: '2026-03-20T10:30:00Z'
	},
	{
		id: '018f4e1a-0001-4000-8000-000000000001',
		name: 'Nhà hát Hòa Bình',
		address: '240 Đường 3 Tháng 2, Phường 12, Quận 10',
		city: 'Hồ Chí Minh',
		country: 'Vietnam',
		latitude: 10.776611,
		longitude: 106.678311,
		phone: '+84 28 3865 1234',
		email: 'events@hoabinhtheatre.vn',
		website: null,
		description: 'Nhà hát lớn tại Quận 10, chuyên tổ chức concert và sân khấu nghệ thuật.',
		thumbnailUrl:
			'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=800&q=80',
		images: [],
		status: 'ACTIVE' as const,
		createdAt: '2025-08-10T08:00:00Z',
		updatedAt: '2026-01-05T11:00:00Z'
	},
	{
		id: '018f4e1a-0007-4000-8000-000000000001',
		name: 'Cung thể thao Tuyên Sơn',
		address: 'Phường Hòa Cường Bắc, Hải Châu',
		city: 'Đà Nẵng',
		country: 'Vietnam',
		latitude: 16.031511,
		longitude: 108.225911,
		phone: '+84 236 3621 789',
		email: 'info@tuyensonarena.vn',
		website: 'https://tuyensonarena.vn',
		description: 'Trung tâm thể thao và sự kiện hàng đầu tại Đà Nẵng.',
		thumbnailUrl:
			'https://images.unsplash.com/photo-1472712739516-7ad2b786e1f7?auto=format&fit=crop&w=800&q=80',
		images: [],
		status: 'ACTIVE' as const,
		createdAt: '2025-09-01T08:00:00Z',
		updatedAt: '2026-02-28T15:00:00Z'
	},
	{
		id: '019e90ee-6afa-70fc-aa55-2159192f0729',
		name: 'Hollywood Bowl',
		address: '2301 N Highland Ave, Los Angeles, CA 90068',
		city: 'Los Angeles',
		country: 'United States',
		latitude: 34.112222,
		longitude: -118.339811,
		phone: '+1 323 850 2000',
		email: 'info@hollywoodbowl.com',
		website: 'https://hollywoodbowl.com',
		description: 'Iconic outdoor amphitheater in the Hollywood Hills, seating 17,500.',
		thumbnailUrl:
			'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=800&q=80',
		images: [],
		status: 'ACTIVE' as const,
		createdAt: '2025-12-01T08:00:00Z',
		updatedAt: '2026-05-01T12:00:00Z'
	}
];

// ── Attractions (matches AttractionResponse DTO) ─────────────────────────────
// type uses backend AttractionType enum values: ARTIST, TEAM, PERFORMER, BAND, SPEAKER, ORGANIZER

const ATR_BASE = '019d0001-4000';

export const MOCK_ATTRACTIONS = [
	{
		id: _uuid(ATR_BASE, 1),
		name: 'Sơn Tùng M-TP',
		slug: 'son-tung-mtp',
		type: 'ARTIST' as const,
		description: "Vietnam's pop king, pioneer of modern V-Pop beats.",
		imageUrl:
			'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=400&q=80'
	},
	{
		id: _uuid(ATR_BASE, 2),
		name: 'Đen Vâu',
		slug: 'den-vau',
		type: 'ARTIST' as const,
		description: 'Award-winning Vietnamese rapper, famous for raw acoustic rap beats.',
		imageUrl:
			'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?auto=format&fit=crop&w=400&q=80'
	},
	{
		id: _uuid(ATR_BASE, 3),
		name: 'Taylor Swift',
		slug: 'taylor-swift',
		type: 'ARTIST' as const,
		description: 'Multi-Grammy winning global pop icon.',
		imageUrl:
			'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=400&q=80'
	},
	{
		id: _uuid(ATR_BASE, 4),
		name: 'Hanoi FC',
		slug: 'hanoi-fc',
		type: 'TEAM' as const,
		description: 'Multi-time champions of the domestic V-League tournament.',
		imageUrl:
			'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=400&q=80'
	}
];

// ── Ticket Types (matches TicketTypeResponse DTO) ────────────────────────────
// Used by Offers. Each event will reference one of these.

const TT_BASE = '019b0001-4000';

export const MOCK_TICKET_TYPES = [
	{
		id: _uuid(TT_BASE, 1),
		eventId: '' as string,
		name: 'Standard',
		slug: 'standard',
		description: 'General admission & standard reserved seating.',
		createdAt: getPastDate(60, 0),
		updatedAt: getPastDate(30, 0)
	},
	{
		id: _uuid(TT_BASE, 2),
		eventId: '' as string,
		name: 'VIP',
		slug: 'vip',
		description: 'Premium VIP experience with exclusive perks.',
		createdAt: getPastDate(60, 0),
		updatedAt: getPastDate(30, 0)
	},
	{
		id: _uuid(TT_BASE, 3),
		eventId: '' as string,
		name: 'Premium',
		slug: 'premium',
		description: 'Premium seating with great views.',
		createdAt: getPastDate(60, 0),
		updatedAt: getPastDate(30, 0)
	},
	{
		id: _uuid(TT_BASE, 4),
		eventId: '' as string,
		name: 'Economy',
		slug: 'economy',
		description: 'Budget-friendly ticket option.',
		createdAt: getPastDate(60, 0),
		updatedAt: getPastDate(30, 0)
	}
];

// ── Venue → Manifest ID mapping ──────────────────────────────────────────────
// Links events to their venue's default manifest for seat map rendering.
// Extended after manifests are defined below.

export const MOCK_VENUE_TO_MANIFEST: Record<string, string> = {
	'018f4e1a-0006-4000-8000-000000000001': 'M100001', // Hanoi Opera House
	'018f4e1a-0003-4000-8000-000000000001': 'M200001', // Mỹ Đình
	'019e90ee-6afa-70fc-aa55-2159192f0729': 'MHB0001', // Hollywood Bowl
	'018f4e1a-0004-4000-8000-000000000001': 'M300001', // Quân khu 7
	'018f4e1a-0005-4000-8000-000000000001': 'M400001', // Phú Thọ
	'018f4e1a-0001-4000-8000-000000000001': 'M500001', // Hòa Bình
	'018f4e1a-0007-4000-8000-000000000001': 'M600001' // Tuyên Sơn
};

// ── Events (matches EventResponse DTO + extra frontend convenience fields) ───

const EVT_BASE = '019a0001-4000';

// Helper: produce a full EventResponse-shaped object
function _makeEvent(
	seq: number,
	overrides: {
		title: string;
		slug: string;
		venueIdx: number;
		cityName: string;
		description: string;
		status: string;
		daysAhead: number;
		startHour: number;
		durationHours: number;
		classifications: Array<{ id: string; name: string; slug: string }>;
		attractions: (typeof MOCK_ATTRACTIONS)[number][];
		imageUrl: string;
		orgId: string;
	}
) {
	const id = _uuid(EVT_BASE, seq);
	const venue = MOCK_VENUES[overrides.venueIdx];
	const startAt = getFutureDate(overrides.daysAhead, overrides.startHour);
	const endAt = new Date(
		new Date(startAt).getTime() + overrides.durationHours * 3600000
	).toISOString();
	const saleStartAt = getPastDate(14, 0);
	const saleEndAt = startAt;

	// Clone ticket types and assign eventId
	const tts = MOCK_TICKET_TYPES.map((tt) => ({ ...tt, eventId: id }));

	return {
		// EventResponse fields (one-to-one with backend DTO)
		id,
		organizationId: overrides.orgId,
		venueId: venue.id,
		title: overrides.title,
		slug: overrides.slug,
		description: overrides.description,
		status: overrides.status,
		startAt,
		endAt,
		timezone: 'Asia/Ho_Chi_Minh',
		saleStartAt,
		saleEndAt,
		restrictSingleSeat: false,
		safeTixEnabled: true,
		transferEnabled: true,
		maxTransferCount: 3,
		serviceFeePercent: 0,
		resaleEnabled: false,
		resalePriceCapPercent: null,
		maxResaleListingsPerUser: 0,
		attractions: overrides.attractions.map((a) => ({ ...a })),
		classifications: overrides.classifications.map((c) => ({ ...c })),
		manifestId: MOCK_VENUE_TO_MANIFEST[venue.id] ?? null,
		createdAt: getPastDate(30, 8),
		updatedAt: getPastDate(7, 10),

		// Extra fields for frontend (not in EventResponse DTO, but needed by UI)
		venueName: venue.name,
		cityName: overrides.cityName,
		imageUrl: overrides.imageUrl
	};
}

export const MOCK_EVENTS = [
	_makeEvent(1, {
		title: 'Sơn Tùng M-TP | Sky Decibel Live Tour HCMC',
		slug: 'son-tung-decibel-hcm',
		venueIdx: 1, // Quân khu 7
		cityName: 'Hồ Chí Minh',
		description:
			'Sơn Tùng M-TP trở lại với Sky Decibel Live Tour 2026 tại TP.HCM. Đêm nhạc hứa hẹn bùng nổ với những bản hit đình đám và màn trình diễn hoành tráng.',
		status: 'ONSALE',
		daysAhead: 3,
		startHour: 19,
		durationHours: 4,
		classifications: [{ id: _uuid(CLS_BASE, 5), name: 'Pop', slug: 'pop' }],
		attractions: [MOCK_ATTRACTIONS[0]],
		imageUrl:
			'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=1200&q=80',
		orgId: MOCK_ORG_IDS.TP
	}),
	_makeEvent(2, {
		title: 'Đen Vâu Live Concert - Show của Đen 2026',
		slug: 'show-cua-den-hn',
		venueIdx: 0, // Mỹ Đình
		cityName: 'Hà Nội',
		description:
			'Đen Vâu tổ chức đêm nhạc đặc biệt tại sân vận động Mỹ Đình. Những ca khúc rap acoustic đầy cảm xúc sẽ được trình diễn trong không gian hoành tráng.',
		status: 'PUBLISHED',
		daysAhead: 10,
		startHour: 19,
		durationHours: 3,
		classifications: [{ id: _uuid(CLS_BASE, 7), name: 'Indie & Folk', slug: 'indie' }],
		attractions: [MOCK_ATTRACTIONS[1]],
		imageUrl:
			'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?auto=format&fit=crop&w=1200&q=80',
		orgId: MOCK_ORG_IDS.TP
	}),
	_makeEvent(3, {
		title: 'Taylor Swift | The Eras Tour Vietnam Special',
		slug: 'the-eras-tour-vietnam',
		venueIdx: 0, // Mỹ Đình
		cityName: 'Hà Nội',
		description:
			'Taylor Swift mang The Eras Tour đến Việt Nam với đêm nhạc đặc biệt tại Mỹ Đình Stadium. Một hành trình qua tất cả các kỷ nguyên âm nhạc.',
		status: 'PUBLISHED',
		daysAhead: 18,
		startHour: 18,
		durationHours: 3.5,
		classifications: [{ id: _uuid(CLS_BASE, 5), name: 'Pop', slug: 'pop' }],
		attractions: [MOCK_ATTRACTIONS[2]],
		imageUrl:
			'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=1200&q=80',
		orgId: MOCK_ORG_IDS.ELITE
	}),
	_makeEvent(4, {
		title: 'V-League Classic Match: Hanoi FC vs HA Gia Lai',
		slug: 'hanoi-fc-vs-hagl',
		venueIdx: 0, // Mỹ Đình
		cityName: 'Hà Nội',
		description:
			'Trận cầu đỉnh cao V-League giữa Hanoi FC và HA Gia Lai tại sân vận động Mỹ Đình. Hứa hẹn những pha bóng nghẹt thở.',
		status: 'ONSALE',
		daysAhead: 1,
		startHour: 17,
		durationHours: 2.5,
		classifications: [{ id: _uuid(CLS_BASE, 9), name: 'Football (Soccer)', slug: 'football' }],
		attractions: [MOCK_ATTRACTIONS[3]],
		imageUrl:
			'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=1200&q=80',
		orgId: MOCK_ORG_IDS.TP
	}),
	_makeEvent(5, {
		title: 'Broadway Musical: Les Misérables (Những Người Khốn Khổ)',
		slug: 'les-miserables-vietnam',
		venueIdx: 3, // Hanoi Opera House
		cityName: 'Hà Nội',
		description:
			'Vở nhạc kịch Broadway kinh điển Les Misérables được trình diễn tại Nhà hát Lớn Hà Nội. Một tác phẩm nghệ thuật đỉnh cao.',
		status: 'PUBLISHED',
		daysAhead: 2,
		startHour: 19,
		durationHours: 3,
		classifications: [{ id: _uuid(CLS_BASE, 12), name: 'Musicals & Plays', slug: 'musical' }],
		attractions: [],
		imageUrl:
			'https://images.unsplash.com/photo-1503095396549-807759245b35?auto=format&fit=crop&w=1200&q=80',
		orgId: MOCK_ORG_IDS.TP
	}),
	_makeEvent(6, {
		title: 'Saigon Stand-Up Comedy Special Night 2026',
		slug: 'saigon-comedy-special',
		venueIdx: 4, // Hòa Bình
		cityName: 'Hồ Chí Minh',
		description:
			'Đêm hài kịch đặc biệt tại Nhà hát Hòa Bình với sự góp mặt của các danh hài nổi tiếng Việt Nam.',
		status: 'PUBLISHED',
		daysAhead: 4,
		startHour: 20,
		durationHours: 2.5,
		classifications: [{ id: _uuid(CLS_BASE, 13), name: 'Stand-Up Comedy', slug: 'comedy' }],
		attractions: [],
		imageUrl:
			'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=1200&q=80',
		orgId: MOCK_ORG_IDS.TP
	}),
	_makeEvent(7, {
		title: 'Vibe Nation Electronic Dance Music Festival',
		slug: 'vibe-nation-edm-fest',
		venueIdx: 5, // Tuyên Sơn
		cityName: 'Đà Nẵng',
		description:
			'Lễ hội âm nhạc điện tử lớn nhất Đà Nẵng với sự tham gia của các DJ hàng đầu thế giới.',
		status: 'PUBLISHED',
		daysAhead: 8,
		startHour: 15,
		durationHours: 8,
		classifications: [{ id: _uuid(CLS_BASE, 8), name: 'EDM & Electronic', slug: 'edm' }],
		attractions: [],
		imageUrl:
			'https://images.unsplash.com/photo-1472712739516-7ad2b786e1f7?auto=format&fit=crop&w=1200&q=80',
		orgId: MOCK_ORG_IDS.ELITE
	}),
	_makeEvent(8, {
		title: 'Rap Việt All-Stars Concert 2026 Live in HCMC',
		slug: 'rap-viet-all-stars-2026',
		venueIdx: 1, // Quân khu 7
		cityName: 'Hồ Chí Minh',
		description:
			'Rap Việt All-Stars Concert quy tụ dàn rapper đình đám nhất trong một đêm nhạc bùng nổ tại TP.HCM.',
		status: 'PUBLISHED',
		daysAhead: 12,
		startHour: 19,
		durationHours: 4,
		classifications: [{ id: _uuid(CLS_BASE, 7), name: 'Indie & Folk', slug: 'indie' }],
		attractions: [MOCK_ATTRACTIONS[1]],
		imageUrl:
			'https://images.unsplash.com/photo-1388035482091-7e5b4a54e6c9?auto=format&fit=crop&w=1200&q=80',
		orgId: MOCK_ORG_IDS.ELITE
	}),
	_makeEvent(9, {
		title: 'Vietnam National Symphony Orchestra Concert Series',
		slug: 'vnso-classical-concert',
		venueIdx: 3, // Hanoi Opera House
		cityName: 'Hà Nội',
		description:
			'Đêm hòa nhạc của Dàn nhạc Giao hưởng Quốc gia Việt Nam tại Nhà hát Lớn Hà Nội. Thưởng thức những tác phẩm kinh điển.',
		status: 'PUBLISHED',
		daysAhead: 6,
		startHour: 19,
		durationHours: 2.5,
		classifications: [{ id: _uuid(CLS_BASE, 14), name: 'Classical Dance', slug: 'dance' }],
		attractions: [],
		imageUrl:
			'https://images.unsplash.com/photo-1388035482091-7e5b4a54e6c9?auto=format&fit=crop&w=1200&q=80',
		orgId: MOCK_ORG_IDS.TP
	}),
	_makeEvent(10, {
		title: 'VBA Playoff Finals: Saigon Heat vs Danang Dragons',
		slug: 'saigon-heat-vs-danang-dragons',
		venueIdx: 2, // Phú Thọ
		cityName: 'Hồ Chí Minh',
		description:
			'Trận chung kết VBA Playoff giữa Saigon Heat và Danang Dragons tại Nhà thi đấu Phú Thọ.',
		status: 'PUBLISHED',
		daysAhead: 14,
		startHour: 18,
		durationHours: 3,
		classifications: [{ id: _uuid(CLS_BASE, 10), name: 'Basketball', slug: 'basketball' }],
		attractions: [],
		imageUrl:
			'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=1200&q=80',
		orgId: MOCK_ORG_IDS.TP
	})
];

// ── Offers (matches OfferResponse DTO) ───────────────────────────────────────
//
// OfferResponse fields:
//   id, eventId, ticketTypeId, name, description, currency, faceValue,
//   restrictedPayment, eventTicketMinimum, capacityCap, sellableQuantities,
//   saleWindows (OfferSaleWindowResponse[]), seatingMode,
//   sectionId, priceLevelId, charges (OfferChargeResponse[]), createdAt, updatedAt

const OFF_BASE = '019c0001-4000';
const SW_BASE = '019c0010-4000';

// Reusable charge templates
const SERVICE_CHARGE = {
	name: 'Service Fee',
	type: 'SERVICE' as const,
	amount: 5,
	isPercentage: true
};
const TAX_CHARGE = { name: 'VAT (8%)', type: 'TAX' as const, amount: 8, isPercentage: true };
const DELIVERY_CHARGE = {
	name: 'E-Delivery',
	type: 'DELIVERY' as const,
	amount: 15000,
	isPercentage: false
};

type OfferDef = {
	eventIdx: number;
	ttIdx: number;
	name: string;
	faceValue: number;
	currency: string;
	seatingMode: string;
	capacityCap: number;
	eventTicketMinimum: number;
	sellableQuantities: number[];
	presaleOnly?: boolean;
	charges: Array<{ name: string; type: string; amount: number; isPercentage: boolean }>;
	sectionId?: string;
	priceLevelId?: string;
};

const OFFER_DEFS: OfferDef[] = [
	// Event 1: Sơn Tùng M-TP @ Quân khu 7
	{
		eventIdx: 0,
		ttIdx: 2,
		name: 'VIP – Sky Box',
		faceValue: 5000000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 200,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 0,
		ttIdx: 1,
		name: 'Standard Seated',
		faceValue: 1800000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 3000,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3, 4],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 0,
		ttIdx: 0,
		name: 'General Admission',
		faceValue: 800000,
		currency: 'VND',
		seatingMode: 'GENERAL_ADMISSION',
		capacityCap: 5000,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3, 4, 5],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},

	// Event 2: Đen Vâu @ Mỹ Đình
	{
		eventIdx: 1,
		ttIdx: 2,
		name: 'VIP – Front Row',
		faceValue: 3500000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 500,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 1,
		ttIdx: 1,
		name: 'Premium',
		faceValue: 2000000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 2000,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3, 4],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 1,
		ttIdx: 0,
		name: 'Standard',
		faceValue: 1000000,
		currency: 'VND',
		seatingMode: 'GENERAL_ADMISSION',
		capacityCap: 10000,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3, 4, 5],
		charges: [SERVICE_CHARGE, TAX_CHARGE, DELIVERY_CHARGE]
	},

	// Event 3: Taylor Swift @ Mỹ Đình
	{
		eventIdx: 2,
		ttIdx: 2,
		name: 'VIP – The Eras Package',
		faceValue: 8500000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 300,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 2,
		ttIdx: 1,
		name: 'Premium Bowl',
		faceValue: 4500000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 2000,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 2,
		ttIdx: 0,
		name: 'General Admission',
		faceValue: 1500000,
		currency: 'VND',
		seatingMode: 'GENERAL_ADMISSION',
		capacityCap: 15000,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3, 4],
		charges: [SERVICE_CHARGE, TAX_CHARGE, DELIVERY_CHARGE]
	},

	// Event 4: Hanoi FC vs HA Gia Lai @ Mỹ Đình
	{
		eventIdx: 3,
		ttIdx: 2,
		name: 'VIP – Khán đài A',
		faceValue: 2000000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 400,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 3,
		ttIdx: 1,
		name: 'Premium – Khán đài B',
		faceValue: 1200000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 2000,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3, 4],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 3,
		ttIdx: 0,
		name: 'Standard – Khán đài C',
		faceValue: 500000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 5000,
		eventTicketMinimum: 2,
		sellableQuantities: [2, 3, 4, 5],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},

	// Event 5: Les Misérables @ Hanoi Opera House
	{
		eventIdx: 4,
		ttIdx: 2,
		name: 'VIP – Orchestra Front',
		faceValue: 2500000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 80,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 4,
		ttIdx: 1,
		name: 'Premium – Orchestra Center',
		faceValue: 1500000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 200,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 4,
		ttIdx: 0,
		name: 'Standard – Balcony',
		faceValue: 800000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 300,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3, 4],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},

	// Event 6: Comedy Night @ Hòa Bình
	{
		eventIdx: 5,
		ttIdx: 1,
		name: 'VIP – Front Rows',
		faceValue: 1200000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 100,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 5,
		ttIdx: 0,
		name: 'Standard',
		faceValue: 600000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 500,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3, 4],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},

	// Event 7: EDM Festival @ Tuyên Sơn
	{
		eventIdx: 6,
		ttIdx: 2,
		name: 'VIP – Early Access',
		faceValue: 3000000,
		currency: 'VND',
		seatingMode: 'GENERAL_ADMISSION',
		capacityCap: 500,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 6,
		ttIdx: 1,
		name: 'Premium Pass',
		faceValue: 1800000,
		currency: 'VND',
		seatingMode: 'GENERAL_ADMISSION',
		capacityCap: 2000,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 6,
		ttIdx: 0,
		name: 'GA Pass',
		faceValue: 800000,
		currency: 'VND',
		seatingMode: 'GENERAL_ADMISSION',
		capacityCap: 5000,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3, 4, 5],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},

	// Event 8: Rap Việt All-Stars @ Quân khu 7
	{
		eventIdx: 7,
		ttIdx: 2,
		name: 'VIP – Stage Front',
		faceValue: 4000000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 200,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 7,
		ttIdx: 0,
		name: 'General Admission',
		faceValue: 1200000,
		currency: 'VND',
		seatingMode: 'GENERAL_ADMISSION',
		capacityCap: 6000,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3, 4],
		charges: [SERVICE_CHARGE, TAX_CHARGE, DELIVERY_CHARGE]
	},

	// Event 9: Classical Concert @ Hanoi Opera House
	{
		eventIdx: 8,
		ttIdx: 1,
		name: 'Premium',
		faceValue: 1800000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 200,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 8,
		ttIdx: 0,
		name: 'Standard',
		faceValue: 700000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 380,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3, 4],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},

	// Event 10: VBA Finals @ Phú Thọ
	{
		eventIdx: 9,
		ttIdx: 2,
		name: 'VIP – Courtside',
		faceValue: 2500000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 100,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 9,
		ttIdx: 1,
		name: 'Premium',
		faceValue: 1500000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 800,
		eventTicketMinimum: 1,
		sellableQuantities: [1, 2, 3],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	},
	{
		eventIdx: 9,
		ttIdx: 0,
		name: 'Standard',
		faceValue: 500000,
		currency: 'VND',
		seatingMode: 'RESERVED_SEATING',
		capacityCap: 2000,
		eventTicketMinimum: 2,
		sellableQuantities: [2, 3, 4],
		charges: [SERVICE_CHARGE, TAX_CHARGE]
	}
];

let _offerSeq = 0;
let _swSeq = 0;

function _makeOffersForEvent(eventIdx: number): Array<ReturnType<typeof _makeOfferResponse>> {
	return OFFER_DEFS.filter((d) => d.eventIdx === eventIdx).map((d) => _makeOfferResponse(d));
}

function _makeOfferResponse(def: OfferDef) {
	_offerSeq++;
	const evt = MOCK_EVENTS[def.eventIdx];
	const tt = MOCK_TICKET_TYPES[def.ttIdx];
	const id = _uuid(OFF_BASE, _offerSeq);
	const saleWindows = _makeSaleWindows(evt.id, def.presaleOnly);
	return {
		id,
		eventId: evt.id,
		ticketTypeId: tt.id,
		name: def.name,
		description: `${def.name} ticket for ${evt.title}`,
		currency: def.currency,
		faceValue: def.faceValue,
		restrictedPayment: false,
		eventTicketMinimum: def.eventTicketMinimum,
		capacityCap: def.capacityCap,
		sellableQuantities: def.sellableQuantities,
		saleWindows,
		seatingMode: def.seatingMode,
		sectionId: def.sectionId || null,
		priceLevelId: def.priceLevelId || null,
		charges: def.charges,
		createdAt: getPastDate(20, 10),
		updatedAt: getPastDate(5, 14)
	};
}

function _makeSaleWindows(eventId: string, presaleOnly?: boolean) {
	const windows = [];
	if (!presaleOnly) {
		windows.push({
			id: _uuid(SW_BASE, ++_swSeq),
			type: 'GENERAL_SALE' as const,
			startAt: getPastDate(14, 0),
			endAt: getFutureDate(30, 23)
		});
	}
	return windows;
}

export const MOCK_OFFERS: ReturnType<typeof _makeOfferResponse>[] = MOCK_EVENTS.flatMap((_, i) =>
	_makeOffersForEvent(i)
);

// ── Convenience: events grouped by IDs for quick lookup ──────────────────────

export const MOCK_EVENT_BY_ID: Record<string, (typeof MOCK_EVENTS)[number]> = Object.fromEntries(
	MOCK_EVENTS.map((e) => [e.id, e])
);

export const MOCK_OFFERS_BY_EVENT_ID: Record<string, (typeof MOCK_OFFERS)[number][]> = {};
for (const off of MOCK_OFFERS) {
	if (!MOCK_OFFERS_BY_EVENT_ID[off.eventId]) MOCK_OFFERS_BY_EVENT_ID[off.eventId] = [];
	MOCK_OFFERS_BY_EVENT_ID[off.eventId].push(off);
}

// ── Inventory helpers (matches EventInventoryStatusResponse shape) ───────────

export function mockInventoryForEvent(eventId: string) {
	const offers = MOCK_OFFERS_BY_EVENT_ID[eventId] || [];
	return {
		gaInventory: offers
			.filter((o) => o.seatingMode === 'GENERAL_ADMISSION')
			.map((o) => ({
				offerId: o.id,
				areaId: `ga-area-${o.id}`,
				total: o.capacityCap,
				sold: Math.floor(o.capacityCap * 0.3),
				held: Math.floor(o.capacityCap * 0.05),
				available: Math.floor(o.capacityCap * 0.65)
			})),
		reservedInventory: offers
			.filter((o) => o.seatingMode === 'RESERVED_SEATING')
			.map((o) => ({
				offerId: o.id,
				total: o.capacityCap,
				available: Math.floor(o.capacityCap * 0.7)
			}))
	};
}

// ── Venue ↔ Manifest convenience mappings ────────────────────────────────────
// (used by ops/venues pages — kept from original mock data)

export const MOCK_VENUE_BY_ID: Record<string, (typeof MOCK_VENUES)[number]> = Object.fromEntries(
	MOCK_VENUES.map((v) => [v.id, v])
);

// ──────────────────────────────────────────────────────────────────────────────
// Venue Manifest Mock Data
// Mirrors the API response shapes for rendering seat maps on the web.
// Hierarchy: Manifest → Level, Section, PriceLevel, RSArea → SeatRow → Seat, GAArea
// ──────────────────────────────────────────────────────────────────────────────

/** Helper: generate a rectangular block of seats */
function _genSeats(
	rowId: string,
	startNum: number,
	count: number,
	startX: number,
	rowY: number,
	sectionId?: string,
	priceLevelId?: string
) {
	return Array.from({ length: count }, (_, i) => ({
		id: `${rowId}-s${String(startNum + i).padStart(3, '0')}`,
		rowId,
		name: String(startNum + i).padStart(3, '0'),
		positionX: startX + i * 18,
		positionY: rowY,
		status: 'AVAILABLE' as const,
		accessibility: false,
		obstructedView: false,
		aisle: false,
		priceLevelId: priceLevelId || null,
		sectionId: sectionId || null
	}));
}

/** Helper: generate rows within an RS area */
function _genRows(
	areaId: string,
	rowPrefix: string,
	startRowCode: number,
	rowCount: number,
	seatsPerRow: number,
	startSeatNum: number,
	startX: number,
	startY: number,
	sectionId?: string,
	priceLevelId?: string
) {
	return Array.from({ length: rowCount }, (_, rIdx) => {
		const rowName = String.fromCharCode(startRowCode + rIdx);
		const rowId = `${areaId}-row-${rowName}`;
		const rowY = startY + rIdx * 22;
		return {
			id: rowId,
			rsAreaId: areaId,
			name: rowName,
			positionY: rowY,
			seats: _genSeats(rowId, startSeatNum, seatsPerRow, startX, rowY, sectionId, priceLevelId)
		};
	});
}

// ── Nhà hát Lớn Hà Nội – Concert Layout ─────────────────────────────────────

export const MOCK_MANIFEST_HANOI_OPERA = {
	id: 'M100001',
	venueId: '018f4e1a-0006-4000-8000-000000000001',
	description: 'Concert Layout – Nhà hát Lớn Hà Nội',
	totalCapacity: 580,
	status: 'PUBLISHED',
	createdAt: '2026-01-15T08:00:00Z',
	updatedAt: '2026-03-20T10:30:00Z',
	objects: [
		{ type: 'stage', text: 'SÂN KHẤU', x: 280, y: 20, width: 340, height: 80 },
		{ type: 'label', text: 'Mixer / Sound Booth', x: 430, y: 520, fontSize: 12, color: '#64748B' }
	]
};

export const MOCK_MANIFEST_HANOI_OPERA_LEVELS = [
	{ id: 'L1', manifestId: 'M100001', description: 'Orchestra (Tầng trệt)', color: '#3B82F6' },
	{ id: 'L2', manifestId: 'M100001', description: 'Balcony (Tầng lầu)', color: '#8B5CF6' }
];

export const MOCK_MANIFEST_HANOI_OPERA_SECTIONS: Record<string, unknown>[] = [
	{
		id: 'SEC-VIP',
		type: 'RS',
		name: 'VIP Front Row',
		color: '#F59E0B',
		levelId: 'L1',
		capacity: 32,
		uiData: { polygon: [], x: 220, y: 140, width: 460, height: 120 }
	},
	{
		id: 'SEC-A',
		type: 'RS',
		name: 'Standard A – Orchestra Center',
		color: '#3B82F6',
		levelId: 'L1',
		capacity: 64,
		uiData: { polygon: [], x: 220, y: 280, width: 460, height: 180 }
	},
	{
		id: 'SEC-B',
		type: 'RS',
		name: 'Standard B – Orchestra Wings',
		color: '#10B981',
		levelId: 'L1',
		capacity: 18,
		uiData: { polygon: [], x: 60, y: 280, width: 140, height: 140 }
	},
	{
		id: 'SEC-C',
		type: 'RS',
		name: 'Balcony Seats',
		color: '#8B5CF6',
		levelId: 'L2',
		capacity: 40,
		uiData: { polygon: [], x: 120, y: 500, width: 660, height: 100 }
	}
];

export const MOCK_MANIFEST_HANOI_OPERA_PRICE_LEVELS = [
	{ id: 'PL-VIP', manifestId: 'M100001', description: 'VIP – 2.500.000₫', color: '#F59E0B' },
	{ id: 'PL-PREM', manifestId: 'M100001', description: 'Premium – 1.500.000₫', color: '#3B82F6' },
	{ id: 'PL-STD', manifestId: 'M100001', description: 'Standard – 800.000₫', color: '#10B981' }
];

const _haOperaVipAreaId = 'M100001-VIP';
const _haOperaCenterAreaId = 'M100001-CENTER';
const _haOperaWingAreaId = 'M100001-WING';
const _haOperaBalconyAreaId = 'M100001-BALCONY';

export const MOCK_MANIFEST_HANOI_OPERA_RS_AREAS = [
	{
		id: _haOperaVipAreaId,
		manifestId: 'M100001',
		levelId: 'L1',
		x: 220,
		y: 140,
		width: 460,
		height: 120,
		sectionId: 'SEC-VIP',
		priceLevelId: 'PL-VIP',
		curvature: 20,
		rows: _genRows(_haOperaVipAreaId, 'A', 65, 2, 16, 1, 240, 150, 'SEC-VIP', 'PL-VIP')
	},
	{
		id: _haOperaCenterAreaId,
		manifestId: 'M100001',
		levelId: 'L1',
		x: 220,
		y: 280,
		width: 460,
		height: 180,
		sectionId: 'SEC-A',
		priceLevelId: 'PL-PREM',
		curvature: 10,
		rows: _genRows(_haOperaCenterAreaId, 'C', 67, 4, 16, 1, 240, 290, 'SEC-A', 'PL-PREM')
	},
	{
		id: _haOperaWingAreaId,
		manifestId: 'M100001',
		levelId: 'L1',
		x: 60,
		y: 280,
		width: 140,
		height: 140,
		sectionId: 'SEC-B',
		priceLevelId: 'PL-STD',
		curvature: 0,
		rows: _genRows(_haOperaWingAreaId, 'L', 76, 3, 6, 1, 70, 290, 'SEC-B', 'PL-STD')
	},
	{
		id: _haOperaBalconyAreaId,
		manifestId: 'M100001',
		levelId: 'L2',
		x: 120,
		y: 500,
		width: 660,
		height: 100,
		sectionId: 'SEC-C',
		priceLevelId: 'PL-STD',
		curvature: 5,
		rows: _genRows(_haOperaBalconyAreaId, 'P', 80, 2, 20, 1, 140, 510, 'SEC-C', 'PL-STD')
	}
];

export const MOCK_MANIFEST_HANOI_OPERA_GA_AREAS = [
	{
		id: 'M100001-GA-LOBBY',
		manifestId: 'M100001',
		levelId: 'L1',
		priceLevelId: 'PL-STD',
		capacity: 50,
		x: 60,
		y: 140,
		width: 140,
		height: 100
	}
];

// ── Sân vận động Mỹ Đình – Concert Stadium Layout ───────────────────────────

export const MOCK_MANIFEST_MY_DINH = {
	id: 'M200001',
	venueId: '018f4e1a-0003-4000-8000-000000000001',
	description: 'Concert Stadium Layout – Mỹ Đình',
	totalCapacity: 38000,
	status: 'DRAFT',
	createdAt: '2026-02-10T09:00:00Z',
	updatedAt: '2026-04-05T14:20:00Z',
	objects: [
		{ type: 'stage', text: 'MAIN STAGE', x: 350, y: 10, width: 500, height: 100 },
		{
			type: 'shape',
			shape: 'rect',
			x: 350,
			y: 120,
			width: 500,
			height: 30,
			color: '#E2E8F0',
			opacity: 0.4
		},
		{ type: 'label', text: 'BARRIER', x: 550, y: 125, fontSize: 10, color: '#94A3B8' },
		{ type: 'label', text: 'FOH / Sound Tower', x: 600, y: 750, fontSize: 12, color: '#64748B' }
	]
};

export const MOCK_MANIFEST_MY_DINH_LEVELS = [
	{ id: 'FL', manifestId: 'M200001', description: 'Floor (Sân)', color: '#EF4444' },
	{ id: 'T1', manifestId: 'M200001', description: 'Tier 1 – Lower Stand', color: '#3B82F6' },
	{ id: 'T2', manifestId: 'M200001', description: 'Tier 2 – Upper Stand', color: '#8B5CF6' }
];

export const MOCK_MANIFEST_MY_DINH_SECTIONS: Record<string, unknown>[] = [
	{
		id: 'S-VIP',
		type: 'RS',
		name: 'VIP Floor Front',
		color: '#F59E0B',
		levelId: 'FL',
		capacity: 80,
		uiData: { polygon: [], x: 300, y: 180, width: 600, height: 200 }
	},
	{
		id: 'S-GA',
		type: 'GA',
		name: 'General Admission Floor',
		color: '#EF4444',
		levelId: 'FL',
		capacity: 15000,
		uiData: { polygon: [], x: 300, y: 400, width: 600, height: 20 }
	},
	{
		id: 'S-A',
		type: 'RS',
		name: 'Stand A – North',
		color: '#3B82F6',
		levelId: 'T1',
		capacity: 42,
		uiData: { polygon: [], x: 100, y: 440, width: 400, height: 160 }
	},
	{
		id: 'S-B',
		type: 'RS',
		name: 'Stand B – South',
		color: '#10B981',
		levelId: 'T1',
		capacity: 42,
		uiData: { polygon: [], x: 700, y: 440, width: 400, height: 160 }
	},
	{
		id: 'S-C',
		type: 'RS',
		name: 'Stand C – East',
		color: '#6366F1',
		levelId: 'T1',
		capacity: 0,
		uiData: {}
	},
	{
		id: 'S-D',
		type: 'RS',
		name: 'Stand D – West',
		color: '#EC4899',
		levelId: 'T1',
		capacity: 0,
		uiData: {}
	},
	{
		id: 'S-U',
		type: 'RS',
		name: 'Upper Tier',
		color: '#8B5CF6',
		levelId: 'T2',
		capacity: 60,
		uiData: { polygon: [], x: 100, y: 650, width: 1000, height: 120 }
	}
];

export const MOCK_MANIFEST_MY_DINH_PRICE_LEVELS = [
	{ id: 'P-VIP', manifestId: 'M200001', description: 'VIP – 5.000.000₫', color: '#F59E0B' },
	{ id: 'P-PRM', manifestId: 'M200001', description: 'Premium – 3.000.000₫', color: '#EF4444' },
	{ id: 'P-STD', manifestId: 'M200001', description: 'Standard – 1.200.000₫', color: '#3B82F6' },
	{ id: 'P-ECO', manifestId: 'M200001', description: 'Economy – 600.000₫', color: '#8B5CF6' }
];

const _mdVipId = 'M200001-VIP';
const _mdStandAId = 'M200001-STAND-A';
const _mdStandBId = 'M200001-STAND-B';
const _mdUpperId = 'M200001-UPPER';

export const MOCK_MANIFEST_MY_DINH_RS_AREAS = [
	{
		id: _mdVipId,
		manifestId: 'M200001',
		levelId: 'FL',
		x: 300,
		y: 180,
		width: 600,
		height: 200,
		sectionId: 'S-VIP',
		priceLevelId: 'P-VIP',
		curvature: 15,
		rows: _genRows(_mdVipId, 'A', 65, 4, 20, 1, 320, 190, 'S-VIP', 'P-VIP')
	},
	{
		id: _mdStandAId,
		manifestId: 'M200001',
		levelId: 'T1',
		x: 100,
		y: 440,
		width: 400,
		height: 160,
		sectionId: 'S-A',
		priceLevelId: 'P-STD',
		curvature: 0,
		rows: _genRows(_mdStandAId, 'N', 78, 3, 14, 1, 120, 450, 'S-A', 'P-STD')
	},
	{
		id: _mdStandBId,
		manifestId: 'M200001',
		levelId: 'T1',
		x: 700,
		y: 440,
		width: 400,
		height: 160,
		sectionId: 'S-B',
		priceLevelId: 'P-STD',
		curvature: 0,
		rows: _genRows(_mdStandBId, 'S', 83, 3, 14, 1, 720, 450, 'S-B', 'P-STD')
	},
	{
		id: _mdUpperId,
		manifestId: 'M200001',
		levelId: 'T2',
		x: 100,
		y: 650,
		width: 1000,
		height: 120,
		sectionId: 'S-U',
		priceLevelId: 'P-ECO',
		curvature: 8,
		rows: _genRows(_mdUpperId, 'U', 85, 2, 30, 1, 120, 660, 'S-U', 'P-ECO')
	}
];

export const MOCK_MANIFEST_MY_DINH_GA_AREAS = [
	{
		id: 'M200001-GA-FLOOR',
		manifestId: 'M200001',
		levelId: 'FL',
		priceLevelId: 'P-PRM',
		capacity: 15000,
		x: 300,
		y: 400,
		width: 600,
		height: 20
	},
	{
		id: 'M200001-GA-STAND',
		manifestId: 'M200001',
		levelId: 'T1',
		priceLevelId: 'P-ECO',
		capacity: 8000,
		x: 520,
		y: 440,
		width: 160,
		height: 160
	}
];

// ── Hollywood Bowl – Amphitheater Layout ──────────────────────────────────────

export const MOCK_MANIFEST_HOLLYWOOD_BOWL = {
	id: 'MHB0001',
	venueId: '019e90ee-6afa-70fc-aa55-2159192f0729',
	description: 'Summer Concert Series – Hollywood Bowl',
	totalCapacity: 17500,
	status: 'PUBLISHED',
	createdAt: '2026-01-10T08:00:00Z',
	updatedAt: '2026-05-01T12:00:00Z',
	objects: [{ type: 'stage', text: 'SHELL', x: 390, y: 418, width: 120, height: 164 }]
};

export const MOCK_MANIFEST_HB_LEVELS = [
	{ id: 'L-ORCH', manifestId: 'MHB0001', description: 'Orchestra', color: '#3B82F6' },
	{ id: 'L-TER', manifestId: 'MHB0001', description: 'Terrace', color: '#8B5CF6' },
	{ id: 'L-BOX', manifestId: 'MHB0001', description: 'Box', color: '#F59E0B' },
	{ id: 'L-GARD', manifestId: 'MHB0001', description: 'Garden', color: '#94A3B8' }
];

export const MOCK_MANIFEST_HB_SECTIONS: Record<string, unknown>[] = [
	{
		id: 'SEC-BOX',
		type: 'RS',
		name: 'Box Seats',
		color: '#F59E0B',
		levelId: 'L-BOX',
		capacity: 18,
		uiData: {}
	},
	{
		id: 'SEC-ORCH',
		type: 'RS',
		name: 'Orchestra',
		color: '#3B82F6',
		levelId: 'L-ORCH',
		capacity: 280,
		uiData: {}
	},
	{
		id: 'SEC-TER',
		type: 'RS',
		name: 'Terrace',
		color: '#8B5CF6',
		levelId: 'L-TER',
		capacity: 204,
		uiData: {}
	},
	{
		id: 'SEC-GARD',
		type: 'GA',
		name: 'Garden Lawn',
		color: '#94A3B8',
		levelId: 'L-GARD',
		capacity: 2000,
		uiData: {}
	}
];

export const MOCK_MANIFEST_HB_PRICE_LEVELS = [
	{ id: 'P-BOX', manifestId: 'MHB0001', description: 'Box – $350', color: '#F59E0B' },
	{ id: 'P-PREM', manifestId: 'MHB0001', description: 'Premium – $220', color: '#1E40AF' },
	{ id: 'P-STD', manifestId: 'MHB0001', description: 'Standard – $120', color: '#3B82F6' },
	{ id: 'P-TER', manifestId: 'MHB0001', description: 'Terrace – $85', color: '#8B5CF6' },
	{ id: 'P-GARD', manifestId: 'MHB0001', description: 'Garden – $55', color: '#94A3B8' }
];

// ── Hollywood Bowl – Fan geometry ────────────────────────────────────────────

const _HB_CX = 520,
	_HB_CY = 500;
const _toRad = (d: number) => (d * Math.PI) / 180;

function _fanPolygon(
	innerR: number,
	outerR: number,
	deg1: number,
	deg2: number,
	steps = 32
): [number, number][] {
	const pts: [number, number][] = [];
	for (let i = 0; i <= steps; i++) {
		const a = _toRad(deg1 + ((deg2 - deg1) * i) / steps);
		pts.push([
			Math.round(_HB_CX + innerR * Math.cos(a)),
			Math.round(_HB_CY + innerR * Math.sin(a))
		]);
	}
	for (let i = steps; i >= 0; i--) {
		const a = _toRad(deg1 + ((deg2 - deg1) * i) / steps);
		pts.push([
			Math.round(_HB_CX + outerR * Math.cos(a)),
			Math.round(_HB_CY + outerR * Math.sin(a))
		]);
	}
	return pts;
}

function _fanRows(
	areaId: string,
	startChar: number,
	rowCount: number,
	seatsPerRow: number,
	innerR: number,
	outerR: number,
	deg1: number,
	deg2: number,
	sectionId: string | null,
	priceLevelId: string
) {
	const SEAT_GAP_DEG = 6;
	const ROW_GAP_R = 10;
	const usableDeg1 = deg1 + SEAT_GAP_DEG;
	const usableDeg2 = deg2 - SEAT_GAP_DEG;
	return Array.from({ length: rowCount }, (_, ri) => {
		const rowChar = String.fromCharCode(startChar + ri);
		const rowId = `${areaId}-row-${rowChar}`;
		const r =
			innerR + ROW_GAP_R + ((outerR - innerR - 2 * ROW_GAP_R) * ri) / Math.max(rowCount - 1, 1);
		const midRad = _toRad((usableDeg1 + usableDeg2) / 2);
		const rowY = Math.round(_HB_CY + r * Math.sin(midRad));
		const seats = Array.from({ length: seatsPerRow }, (_, si) => {
			const d =
				seatsPerRow === 1
					? (usableDeg1 + usableDeg2) / 2
					: usableDeg1 + ((usableDeg2 - usableDeg1) * si) / (seatsPerRow - 1);
			const a = _toRad(d);
			return {
				id: `${rowId}-s${String(si + 1).padStart(3, '0')}`,
				rowId,
				name: String(si + 1).padStart(3, '0'),
				positionX: Math.round(_HB_CX + r * Math.cos(a)),
				positionY: Math.round(_HB_CY + r * Math.sin(a)),
				status: 'AVAILABLE' as const,
				accessibility: false,
				obstructedView: false,
				aisle: false,
				priceLevelId,
				sectionId
			};
		});
		return { id: rowId, rsAreaId: areaId, name: rowChar, positionY: rowY, seats };
	});
}

const _hb = 'MHB0001';

function _sectorPoints(
	cx: number,
	cy: number,
	innerR: number,
	outerR: number,
	deg1: number,
	deg2: number,
	steps = 24
): number[] {
	const pts: number[] = [];
	for (let i = 0; i <= steps; i++) {
		const a = _toRad(deg1 + ((deg2 - deg1) * i) / steps);
		pts.push(cx + innerR * Math.cos(a), cy + innerR * Math.sin(a));
	}
	for (let i = steps; i >= 0; i--) {
		const a = _toRad(deg1 + ((deg2 - deg1) * i) / steps);
		pts.push(cx + outerR * Math.cos(a), cy + outerR * Math.sin(a));
	}
	return pts;
}

const _ORCH_IN = 220,
	_ORCH_OUT = 370,
	_ORCH_ROWS = 5;

const _areaA = {
	id: `${_hb}-AREA-A`,
	manifestId: _hb,
	levelId: 'L-ORCH',
	priceLevelId: 'P-STD',
	color: '#93C5FD',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	fanShape: { cx: _HB_CX, cy: _HB_CY, innerR: _ORCH_IN, outerR: _ORCH_OUT, deg1: -75, deg2: -55 },
	rows: _fanRows(
		`${_hb}-AREA-A`,
		65,
		_ORCH_ROWS,
		4,
		_ORCH_IN,
		_ORCH_OUT,
		-75,
		-55,
		'SEC-ORCH',
		'P-STD'
	)
};
const _areaB = {
	id: `${_hb}-AREA-B`,
	manifestId: _hb,
	levelId: 'L-ORCH',
	priceLevelId: 'P-STD',
	color: '#60A5FA',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	fanShape: { cx: _HB_CX, cy: _HB_CY, innerR: _ORCH_IN, outerR: _ORCH_OUT, deg1: -55, deg2: -34 },
	rows: _fanRows(
		`${_hb}-AREA-B`,
		65,
		_ORCH_ROWS,
		6,
		_ORCH_IN,
		_ORCH_OUT,
		-55,
		-34,
		'SEC-ORCH',
		'P-STD'
	)
};
const _areaC = {
	id: `${_hb}-AREA-C`,
	manifestId: _hb,
	levelId: 'L-ORCH',
	priceLevelId: 'P-STD',
	color: '#3B82F6',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	fanShape: { cx: _HB_CX, cy: _HB_CY, innerR: _ORCH_IN, outerR: _ORCH_OUT, deg1: -34, deg2: -12 },
	rows: _fanRows(
		`${_hb}-AREA-C`,
		65,
		_ORCH_ROWS,
		7,
		_ORCH_IN,
		_ORCH_OUT,
		-34,
		-12,
		'SEC-ORCH',
		'P-STD'
	)
};
const _areaD = {
	id: `${_hb}-AREA-D`,
	manifestId: _hb,
	levelId: 'L-ORCH',
	priceLevelId: 'P-PREM',
	color: '#1D4ED8',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	fanShape: { cx: _HB_CX, cy: _HB_CY, innerR: _ORCH_IN, outerR: _ORCH_OUT, deg1: -12, deg2: 12 },
	rows: _fanRows(
		`${_hb}-AREA-D`,
		65,
		_ORCH_ROWS,
		8,
		_ORCH_IN,
		_ORCH_OUT,
		-12,
		12,
		'SEC-ORCH',
		'P-PREM'
	)
};
const _areaE = {
	id: `${_hb}-AREA-E`,
	manifestId: _hb,
	levelId: 'L-ORCH',
	priceLevelId: 'P-STD',
	color: '#3B82F6',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	fanShape: { cx: _HB_CX, cy: _HB_CY, innerR: _ORCH_IN, outerR: _ORCH_OUT, deg1: 12, deg2: 34 },
	rows: _fanRows(
		`${_hb}-AREA-E`,
		65,
		_ORCH_ROWS,
		7,
		_ORCH_IN,
		_ORCH_OUT,
		12,
		34,
		'SEC-ORCH',
		'P-STD'
	)
};
const _areaF = {
	id: `${_hb}-AREA-F`,
	manifestId: _hb,
	levelId: 'L-ORCH',
	priceLevelId: 'P-STD',
	color: '#60A5FA',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	fanShape: { cx: _HB_CX, cy: _HB_CY, innerR: _ORCH_IN, outerR: _ORCH_OUT, deg1: 34, deg2: 55 },
	rows: _fanRows(
		`${_hb}-AREA-F`,
		65,
		_ORCH_ROWS,
		6,
		_ORCH_IN,
		_ORCH_OUT,
		34,
		55,
		'SEC-ORCH',
		'P-STD'
	)
};
const _areaG = {
	id: `${_hb}-AREA-G`,
	manifestId: _hb,
	levelId: 'L-ORCH',
	priceLevelId: 'P-STD',
	color: '#93C5FD',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	fanShape: { cx: _HB_CX, cy: _HB_CY, innerR: _ORCH_IN, outerR: _ORCH_OUT, deg1: 55, deg2: 75 },
	rows: _fanRows(
		`${_hb}-AREA-G`,
		65,
		_ORCH_ROWS,
		4,
		_ORCH_IN,
		_ORCH_OUT,
		55,
		75,
		'SEC-ORCH',
		'P-STD'
	)
};

const _TER_IN = 400,
	_TER_OUT = 540,
	_TER_ROWS = 4;

const _areaT1 = {
	id: `${_hb}-AREA-T1`,
	manifestId: _hb,
	levelId: 'L-TER',
	priceLevelId: 'P-TER',
	color: '#A78BFA',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	rows: _fanRows(
		`${_hb}-AREA-T1`,
		80,
		_TER_ROWS,
		11,
		_TER_IN,
		_TER_OUT,
		-60,
		-27,
		'SEC-TER',
		'P-TER'
	)
};
const _areaT2 = {
	id: `${_hb}-AREA-T2`,
	manifestId: _hb,
	levelId: 'L-TER',
	priceLevelId: 'P-TER',
	color: '#7C3AED',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	rows: _fanRows(`${_hb}-AREA-T2`, 80, _TER_ROWS, 12, _TER_IN, _TER_OUT, -27, 7, 'SEC-TER', 'P-TER')
};
const _areaT3 = {
	id: `${_hb}-AREA-T3`,
	manifestId: _hb,
	levelId: 'L-TER',
	priceLevelId: 'P-TER',
	color: '#A78BFA',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	rows: _fanRows(`${_hb}-AREA-T3`, 80, _TER_ROWS, 11, _TER_IN, _TER_OUT, 7, 40, 'SEC-TER', 'P-TER')
};

const _areaBox = {
	id: `${_hb}-AREA-BOX`,
	manifestId: _hb,
	levelId: 'L-BOX',
	priceLevelId: 'P-BOX',
	color: '#FCD34D',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	rows: _fanRows(`${_hb}-AREA-BOX`, 65, 3, 6, 155, 210, -115, -85, 'SEC-BOX', 'P-BOX')
};

const _areaGard = {
	id: `${_hb}-AREA-GARD`,
	manifestId: _hb,
	levelId: 'L-GARD',
	priceLevelId: 'P-GARD',
	color: '#94A3B8',
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	curvature: 0,
	rows: [] as any[]
};

export const MOCK_MANIFEST_HB_RS_AREAS = [
	_areaA,
	_areaB,
	_areaC,
	_areaD,
	_areaE,
	_areaF,
	_areaG,
	_areaT1,
	_areaT2,
	_areaT3,
	_areaBox,
	_areaGard
];

export const MOCK_MANIFEST_HB_GA_AREAS: any[] = [];

// ── Convenience aggregates ────────────────────────────────────────────────────

// ── Quân khu 7 Stadium – Basic Layout ───────────────────────────────────────

export const MOCK_MANIFEST_QK7 = {
	id: 'M300001',
	venueId: '018f4e1a-0004-4000-8000-000000000001',
	description: 'Concert Layout – Sân vận động Quân khu 7',
	totalCapacity: 25000,
	status: 'PUBLISHED',
	createdAt: '2026-03-01T08:00:00Z',
	updatedAt: '2026-05-10T10:00:00Z',
	objects: [
		{ type: 'stage', text: 'MAIN STAGE', x: 300, y: 20, width: 400, height: 80 },
		{ type: 'label', text: 'FOH', x: 500, y: 580, fontSize: 12, color: '#64748B' }
	]
};

export const MOCK_MANIFEST_QK7_LEVELS = [
	{ id: 'FL', manifestId: 'M300001', description: 'Floor (Sân)', color: '#EF4444' },
	{ id: 'ST', manifestId: 'M300001', description: 'Stand (Khán đài)', color: '#3B82F6' }
];

export const MOCK_MANIFEST_QK7_SECTIONS: Record<string, unknown>[] = [
	{
		id: 'QK7-VIP',
		type: 'RS',
		name: 'VIP Floor',
		color: '#F59E0B',
		levelId: 'FL',
		capacity: 200,
		uiData: {}
	},
	{
		id: 'QK7-GA',
		type: 'GA',
		name: 'GA Floor',
		color: '#EF4444',
		levelId: 'FL',
		capacity: 8000,
		uiData: {}
	},
	{
		id: 'QK7-A',
		type: 'RS',
		name: 'Stand A',
		color: '#3B82F6',
		levelId: 'ST',
		capacity: 5000,
		uiData: {}
	},
	{
		id: 'QK7-B',
		type: 'RS',
		name: 'Stand B',
		color: '#10B981',
		levelId: 'ST',
		capacity: 5000,
		uiData: {}
	}
];

export const MOCK_MANIFEST_QK7_PRICE_LEVELS = [
	{ id: 'PL-VIP', manifestId: 'M300001', description: 'VIP – 5.000.000₫', color: '#F59E0B' },
	{ id: 'PL-PRM', manifestId: 'M300001', description: 'Premium – 3.000.000₫', color: '#EF4444' },
	{ id: 'PL-STD', manifestId: 'M300001', description: 'Standard – 1.200.000₫', color: '#3B82F6' },
	{ id: 'PL-ECO', manifestId: 'M300001', description: 'Economy – 600.000₫', color: '#8B5CF6' }
];

export const MOCK_MANIFEST_QK7_RS_AREAS: any[] = [];
export const MOCK_MANIFEST_QK7_GA_AREAS: any[] = [
	{
		id: 'M300001-GA-FLOOR',
		manifestId: 'M300001',
		levelId: 'FL',
		priceLevelId: 'PL-PRM',
		capacity: 8000,
		x: 200,
		y: 140,
		width: 600,
		height: 400
	}
];

// ── Phú Thọ Arena – Basic Layout ────────────────────────────────────────────

export const MOCK_MANIFEST_PHUTHO = {
	id: 'M400001',
	venueId: '018f4e1a-0005-4000-8000-000000000001',
	description: 'Indoor Layout – Nhà thi đấu Phú Thọ',
	totalCapacity: 3000,
	status: 'PUBLISHED',
	createdAt: '2026-02-15T08:00:00Z',
	updatedAt: '2026-04-20T09:00:00Z',
	objects: [{ type: 'stage', text: 'SÂN THI ĐẤU', x: 250, y: 20, width: 300, height: 80 }]
};

export const MOCK_MANIFEST_PHUTHO_LEVELS = [
	{ id: 'L1', manifestId: 'M400001', description: 'Khán đài chính', color: '#3B82F6' }
];

export const MOCK_MANIFEST_PHUTHO_SECTIONS: Record<string, unknown>[] = [
	{
		id: 'PT-VIP',
		type: 'RS',
		name: 'VIP Courtside',
		color: '#F59E0B',
		levelId: 'L1',
		capacity: 80,
		uiData: {}
	},
	{
		id: 'PT-PREM',
		type: 'RS',
		name: 'Premium',
		color: '#3B82F6',
		levelId: 'L1',
		capacity: 500,
		uiData: {}
	},
	{
		id: 'PT-STD',
		type: 'RS',
		name: 'Standard',
		color: '#10B981',
		levelId: 'L1',
		capacity: 1200,
		uiData: {}
	}
];

export const MOCK_MANIFEST_PHUTHO_PRICE_LEVELS = [
	{ id: 'P-VIP', manifestId: 'M400001', description: 'VIP – 2.500.000₫', color: '#F59E0B' },
	{ id: 'P-PREM', manifestId: 'M400001', description: 'Premium – 1.500.000₫', color: '#3B82F6' },
	{ id: 'P-STD', manifestId: 'M400001', description: 'Standard – 500.000₫', color: '#10B981' }
];

export const MOCK_MANIFEST_PHUTHO_RS_AREAS: any[] = [];
export const MOCK_MANIFEST_PHUTHO_GA_AREAS: any[] = [];

// ── Hòa Bình Theatre – Basic Layout ─────────────────────────────────────────

export const MOCK_MANIFEST_HOA_BINH = {
	id: 'M500001',
	venueId: '018f4e1a-0001-4000-8000-000000000001',
	description: 'Theatre Layout – Nhà hát Hòa Bình',
	totalCapacity: 1200,
	status: 'PUBLISHED',
	createdAt: '2026-01-20T08:00:00Z',
	updatedAt: '2026-03-15T11:00:00Z',
	objects: [{ type: 'stage', text: 'SÂN KHẤU', x: 300, y: 20, width: 300, height: 70 }]
};

export const MOCK_MANIFEST_HOA_BINH_LEVELS = [
	{ id: 'L1', manifestId: 'M500001', description: 'Tầng trệt', color: '#3B82F6' }
];

export const MOCK_MANIFEST_HOA_BINH_SECTIONS: Record<string, unknown>[] = [
	{
		id: 'HB-VIP',
		type: 'RS',
		name: 'VIP Front',
		color: '#F59E0B',
		levelId: 'L1',
		capacity: 60,
		uiData: {}
	},
	{
		id: 'HB-STD',
		type: 'RS',
		name: 'Standard',
		color: '#10B981',
		levelId: 'L1',
		capacity: 600,
		uiData: {}
	}
];

export const MOCK_MANIFEST_HOA_BINH_PRICE_LEVELS = [
	{ id: 'P-VIP', manifestId: 'M500001', description: 'VIP – 1.200.000₫', color: '#F59E0B' },
	{ id: 'P-STD', manifestId: 'M500001', description: 'Standard – 600.000₫', color: '#10B981' }
];

export const MOCK_MANIFEST_HOA_BINH_RS_AREAS: any[] = [];
export const MOCK_MANIFEST_HOA_BINH_GA_AREAS: any[] = [];

// ── Hòa Bình Theatre – GA Layout (Standing + RS) ───────────────────────────

export const MOCK_MANIFEST_HOA_BINH_GA = {
	id: 'M500002',
	venueId: '018f4e1a-0001-4000-8000-000000000001',
	description: 'GA Layout – Nhà hát Hòa Bình (Sàn đứng + Ghế ngồi)',
	totalCapacity: 3000,
	status: 'PUBLISHED',
	createdAt: '2026-04-01T08:00:00Z',
	updatedAt: '2026-05-10T10:00:00Z',
	objects: [
		{ type: 'stage', text: 'SÂN KHẤU', x: 300, y: 20, width: 300, height: 70 },
		{
			type: 'label',
			text: 'GA Standing – 2000 chỗ',
			x: 450,
			y: 280,
			fontSize: 11,
			color: '#EF4444'
		}
	]
};

export const MOCK_MANIFEST_HOA_BINH_GA_LEVELS = [
	{ id: 'L1', manifestId: 'M500002', description: 'Tầng trệt', color: '#3B82F6' },
	{ id: 'L2', manifestId: 'M500002', description: 'Ban công', color: '#8B5CF6' }
];

export const MOCK_MANIFEST_HOA_BINH_GA_SECTIONS: Record<string, unknown>[] = [
	{
		id: 'HBG-VIP',
		type: 'RS',
		name: 'VIP Seated',
		color: '#F59E0B',
		levelId: 'L1',
		capacity: 200,
		uiData: {}
	},
	{
		id: 'HBG-GA',
		type: 'GA',
		name: 'GA Standing Floor',
		color: '#EF4444',
		levelId: 'L1',
		capacity: 2000,
		uiData: {}
	},
	{
		id: 'HBG-BAL',
		type: 'RS',
		name: 'Balcony Seated',
		color: '#8B5CF6',
		levelId: 'L2',
		capacity: 800,
		uiData: {}
	}
];

export const MOCK_MANIFEST_HOA_BINH_GA_PRICE_LEVELS = [
	{ id: 'HBG-P-VIP', manifestId: 'M500002', description: 'VIP – 1.800.000₫', color: '#F59E0B' },
	{ id: 'HBG-P-GA', manifestId: 'M500002', description: 'GA – 500.000₫', color: '#EF4444' },
	{ id: 'HBG-P-BAL', manifestId: 'M500002', description: 'Balcony – 1.000.000₫', color: '#8B5CF6' }
];

export const MOCK_MANIFEST_HOA_BINH_GA_RS_AREAS: any[] = [];
export const MOCK_MANIFEST_HOA_BINH_GA_GA_AREAS: any[] = [
	{
		id: 'M500002-GA-FLOOR',
		manifestId: 'M500002',
		levelId: 'L1',
		priceLevelId: 'HBG-P-GA',
		sectionId: 'HBG-GA',
		capacity: 2000,
		x: 55,
		y: 60,
		width: 310,
		height: 180
	}
];

// ── Tuyên Sơn Arena – Basic Layout ──────────────────────────────────────────

export const MOCK_MANIFEST_TUYEN_SON = {
	id: 'M600001',
	venueId: '018f4e1a-0007-4000-8000-000000000001',
	description: 'Arena Layout – Cung thể thao Tuyên Sơn',
	totalCapacity: 5000,
	status: 'PUBLISHED',
	createdAt: '2026-04-01T08:00:00Z',
	updatedAt: '2026-05-15T10:00:00Z',
	objects: [{ type: 'stage', text: 'MAIN STAGE', x: 350, y: 20, width: 300, height: 70 }]
};

export const MOCK_MANIFEST_TUYEN_SON_LEVELS = [
	{ id: 'FL', manifestId: 'M600001', description: 'Floor', color: '#EF4444' },
	{ id: 'ST', manifestId: 'M600001', description: 'Stand', color: '#3B82F6' }
];

export const MOCK_MANIFEST_TUYEN_SON_SECTIONS: Record<string, unknown>[] = [
	{
		id: 'TS-VIP',
		type: 'RS',
		name: 'VIP Floor',
		color: '#F59E0B',
		levelId: 'FL',
		capacity: 100,
		uiData: {}
	},
	{
		id: 'TS-GA',
		type: 'GA',
		name: 'GA Floor',
		color: '#EF4444',
		levelId: 'FL',
		capacity: 2500,
		uiData: {}
	},
	{
		id: 'TS-STD',
		type: 'RS',
		name: 'Stand Seats',
		color: '#3B82F6',
		levelId: 'ST',
		capacity: 2000,
		uiData: {}
	}
];

export const MOCK_MANIFEST_TUYEN_SON_PRICE_LEVELS = [
	{ id: 'P-VIP', manifestId: 'M600001', description: 'VIP – 3.000.000₫', color: '#F59E0B' },
	{ id: 'P-PRM', manifestId: 'M600001', description: 'Premium – 1.800.000₫', color: '#EF4444' },
	{ id: 'P-STD', manifestId: 'M600001', description: 'Standard – 800.000₫', color: '#3B82F6' }
];

export const MOCK_MANIFEST_TUYEN_SON_RS_AREAS: any[] = [];
export const MOCK_MANIFEST_TUYEN_SON_GA_AREAS: any[] = [
	{
		id: 'M600001-GA-FLOOR',
		manifestId: 'M600001',
		levelId: 'FL',
		priceLevelId: 'P-PRM',
		capacity: 2500,
		x: 200,
		y: 140,
		width: 600,
		height: 400
	}
];

// ── Convenience aggregates ────────────────────────────────────────────────────

export const MOCK_MANIFESTS = [
	MOCK_MANIFEST_HANOI_OPERA,
	MOCK_MANIFEST_MY_DINH,
	MOCK_MANIFEST_HOLLYWOOD_BOWL,
	MOCK_MANIFEST_QK7,
	MOCK_MANIFEST_PHUTHO,
	MOCK_MANIFEST_HOA_BINH,
	MOCK_MANIFEST_HOA_BINH_GA,
	MOCK_MANIFEST_TUYEN_SON
];

export const MOCK_MANIFEST_LEVELS: Record<string, any[]> = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_LEVELS,
	M200001: MOCK_MANIFEST_MY_DINH_LEVELS,
	MHB0001: MOCK_MANIFEST_HB_LEVELS,
	M300001: MOCK_MANIFEST_QK7_LEVELS,
	M400001: MOCK_MANIFEST_PHUTHO_LEVELS,
	M500001: MOCK_MANIFEST_HOA_BINH_LEVELS,
	M500002: MOCK_MANIFEST_HOA_BINH_GA_LEVELS,
	M600001: MOCK_MANIFEST_TUYEN_SON_LEVELS
};

export const MOCK_MANIFEST_SECTIONS: Record<string, Record<string, unknown>[]> = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_SECTIONS,
	M200001: MOCK_MANIFEST_MY_DINH_SECTIONS,
	MHB0001: MOCK_MANIFEST_HB_SECTIONS,
	M300001: MOCK_MANIFEST_QK7_SECTIONS,
	M400001: MOCK_MANIFEST_PHUTHO_SECTIONS,
	M500001: MOCK_MANIFEST_HOA_BINH_SECTIONS,
	M500002: MOCK_MANIFEST_HOA_BINH_GA_SECTIONS,
	M600001: MOCK_MANIFEST_TUYEN_SON_SECTIONS
};

export const MOCK_MANIFEST_PRICE_LEVELS: Record<string, any[]> = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_PRICE_LEVELS,
	M200001: MOCK_MANIFEST_MY_DINH_PRICE_LEVELS,
	MHB0001: MOCK_MANIFEST_HB_PRICE_LEVELS,
	M300001: MOCK_MANIFEST_QK7_PRICE_LEVELS,
	M400001: MOCK_MANIFEST_PHUTHO_PRICE_LEVELS,
	M500001: MOCK_MANIFEST_HOA_BINH_PRICE_LEVELS,
	M500002: MOCK_MANIFEST_HOA_BINH_GA_PRICE_LEVELS,
	M600001: MOCK_MANIFEST_TUYEN_SON_PRICE_LEVELS
};

export const MOCK_MANIFEST_RS_AREAS: Record<string, any[]> = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_RS_AREAS,
	M200001: MOCK_MANIFEST_MY_DINH_RS_AREAS,
	MHB0001: MOCK_MANIFEST_HB_RS_AREAS,
	M300001: MOCK_MANIFEST_QK7_RS_AREAS,
	M400001: MOCK_MANIFEST_PHUTHO_RS_AREAS,
	M500001: MOCK_MANIFEST_HOA_BINH_RS_AREAS,
	M500002: MOCK_MANIFEST_HOA_BINH_GA_RS_AREAS,
	M600001: MOCK_MANIFEST_TUYEN_SON_RS_AREAS
};

export const MOCK_MANIFEST_GA_AREAS: Record<string, any[]> = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_GA_AREAS,
	M200001: MOCK_MANIFEST_MY_DINH_GA_AREAS,
	MHB0001: MOCK_MANIFEST_HB_GA_AREAS,
	M300001: MOCK_MANIFEST_QK7_GA_AREAS,
	M400001: MOCK_MANIFEST_PHUTHO_GA_AREAS,
	M500001: MOCK_MANIFEST_HOA_BINH_GA_AREAS,
	M500002: MOCK_MANIFEST_HOA_BINH_GA_GA_AREAS,
	M600001: MOCK_MANIFEST_TUYEN_SON_GA_AREAS
};

/** For each mock venue, list which manifests belong to it */
export const MOCK_VENUE_MANIFESTS: Record<string, any[]> = {
	'018f4e1a-0006-4000-8000-000000000001': [MOCK_MANIFEST_HANOI_OPERA],
	'018f4e1a-0003-4000-8000-000000000001': [MOCK_MANIFEST_MY_DINH],
	'019e90ee-6afa-70fc-aa55-2159192f0729': [MOCK_MANIFEST_HOLLYWOOD_BOWL],
	'018f4e1a-0004-4000-8000-000000000001': [MOCK_MANIFEST_QK7],
	'018f4e1a-0005-4000-8000-000000000001': [MOCK_MANIFEST_PHUTHO],
	'018f4e1a-0001-4000-8000-000000000001': [MOCK_MANIFEST_HOA_BINH, MOCK_MANIFEST_HOA_BINH_GA],
	'018f4e1a-0007-4000-8000-000000000001': [MOCK_MANIFEST_TUYEN_SON]
};

/** Full mock bundle for the manifest editor */
export const MOCK_VENUE_MANIFEST_EDITOR_DATA: Record<
	string,
	{
		venue: (typeof MOCK_VENUES)[number];
		manifest: any;
		levels: any[];
		sections: any[];
		priceLevels: any[];
		rsAreas: any[];
		gaAreas: any[];
	}
> = {
	'018f4e1a-0006-4000-8000-000000000001': {
		venue: MOCK_VENUE_BY_ID['018f4e1a-0006-4000-8000-000000000001'],
		manifest: MOCK_MANIFEST_HANOI_OPERA,
		levels: MOCK_MANIFEST_HANOI_OPERA_LEVELS,
		sections: MOCK_MANIFEST_HANOI_OPERA_SECTIONS,
		priceLevels: MOCK_MANIFEST_HANOI_OPERA_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_HANOI_OPERA_RS_AREAS,
		gaAreas: MOCK_MANIFEST_HANOI_OPERA_GA_AREAS
	},
	'018f4e1a-0003-4000-8000-000000000001': {
		venue: MOCK_VENUE_BY_ID['018f4e1a-0003-4000-8000-000000000001'],
		manifest: MOCK_MANIFEST_MY_DINH,
		levels: MOCK_MANIFEST_MY_DINH_LEVELS,
		sections: MOCK_MANIFEST_MY_DINH_SECTIONS,
		priceLevels: MOCK_MANIFEST_MY_DINH_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_MY_DINH_RS_AREAS,
		gaAreas: MOCK_MANIFEST_MY_DINH_GA_AREAS
	},
	'019e90ee-6afa-70fc-aa55-2159192f0729': {
		venue: MOCK_VENUE_BY_ID['019e90ee-6afa-70fc-aa55-2159192f0729'],
		manifest: MOCK_MANIFEST_HOLLYWOOD_BOWL,
		levels: MOCK_MANIFEST_HB_LEVELS,
		sections: MOCK_MANIFEST_HB_SECTIONS,
		priceLevels: MOCK_MANIFEST_HB_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_HB_RS_AREAS,
		gaAreas: MOCK_MANIFEST_HB_GA_AREAS
	},
	'018f4e1a-0004-4000-8000-000000000001': {
		venue: MOCK_VENUE_BY_ID['018f4e1a-0004-4000-8000-000000000001'],
		manifest: MOCK_MANIFEST_QK7,
		levels: MOCK_MANIFEST_QK7_LEVELS,
		sections: MOCK_MANIFEST_QK7_SECTIONS,
		priceLevels: MOCK_MANIFEST_QK7_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_QK7_RS_AREAS,
		gaAreas: MOCK_MANIFEST_QK7_GA_AREAS
	},
	'018f4e1a-0005-4000-8000-000000000001': {
		venue: MOCK_VENUE_BY_ID['018f4e1a-0005-4000-8000-000000000001'],
		manifest: MOCK_MANIFEST_PHUTHO,
		levels: MOCK_MANIFEST_PHUTHO_LEVELS,
		sections: MOCK_MANIFEST_PHUTHO_SECTIONS,
		priceLevels: MOCK_MANIFEST_PHUTHO_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_PHUTHO_RS_AREAS,
		gaAreas: MOCK_MANIFEST_PHUTHO_GA_AREAS
	},
	'018f4e1a-0001-4000-8000-000000000001': {
		venue: MOCK_VENUE_BY_ID['018f4e1a-0001-4000-8000-000000000001'],
		manifest: MOCK_MANIFEST_HOA_BINH,
		levels: MOCK_MANIFEST_HOA_BINH_LEVELS,
		sections: MOCK_MANIFEST_HOA_BINH_SECTIONS,
		priceLevels: MOCK_MANIFEST_HOA_BINH_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_HOA_BINH_RS_AREAS,
		gaAreas: MOCK_MANIFEST_HOA_BINH_GA_AREAS
	},
	'018f4e1a-0007-4000-8000-000000000001': {
		venue: MOCK_VENUE_BY_ID['018f4e1a-0007-4000-8000-000000000001'],
		manifest: MOCK_MANIFEST_TUYEN_SON,
		levels: MOCK_MANIFEST_TUYEN_SON_LEVELS,
		sections: MOCK_MANIFEST_TUYEN_SON_SECTIONS,
		priceLevels: MOCK_MANIFEST_TUYEN_SON_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_TUYEN_SON_RS_AREAS,
		gaAreas: MOCK_MANIFEST_TUYEN_SON_GA_AREAS
	}
};

/** Full manifest detail keyed by manifestId — used by B2B manifest detail API */
export const MOCK_MANIFEST_DETAIL: Record<
	string,
	{
		manifest: any;
		levels: any[];
		sections: any[];
		priceLevels: any[];
		rsAreas: any[];
		gaAreas: any[];
	}
> = {
	M500001: {
		manifest: MOCK_MANIFEST_HOA_BINH,
		levels: MOCK_MANIFEST_HOA_BINH_LEVELS,
		sections: MOCK_MANIFEST_HOA_BINH_SECTIONS,
		priceLevels: MOCK_MANIFEST_HOA_BINH_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_HOA_BINH_RS_AREAS,
		gaAreas: MOCK_MANIFEST_HOA_BINH_GA_AREAS
	},
	M500002: {
		manifest: MOCK_MANIFEST_HOA_BINH_GA,
		levels: MOCK_MANIFEST_HOA_BINH_GA_LEVELS,
		sections: MOCK_MANIFEST_HOA_BINH_GA_SECTIONS,
		priceLevels: MOCK_MANIFEST_HOA_BINH_GA_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_HOA_BINH_GA_RS_AREAS,
		gaAreas: MOCK_MANIFEST_HOA_BINH_GA_GA_AREAS
	},
	M100001: {
		manifest: MOCK_MANIFEST_HANOI_OPERA,
		levels: MOCK_MANIFEST_HANOI_OPERA_LEVELS,
		sections: MOCK_MANIFEST_HANOI_OPERA_SECTIONS,
		priceLevels: MOCK_MANIFEST_HANOI_OPERA_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_HANOI_OPERA_RS_AREAS,
		gaAreas: MOCK_MANIFEST_HANOI_OPERA_GA_AREAS
	},
	M200001: {
		manifest: MOCK_MANIFEST_MY_DINH,
		levels: MOCK_MANIFEST_MY_DINH_LEVELS,
		sections: MOCK_MANIFEST_MY_DINH_SECTIONS,
		priceLevels: MOCK_MANIFEST_MY_DINH_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_MY_DINH_RS_AREAS,
		gaAreas: MOCK_MANIFEST_MY_DINH_GA_AREAS
	},
	MHB0001: {
		manifest: MOCK_MANIFEST_HOLLYWOOD_BOWL,
		levels: MOCK_MANIFEST_HB_LEVELS,
		sections: MOCK_MANIFEST_HB_SECTIONS,
		priceLevels: MOCK_MANIFEST_HB_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_HB_RS_AREAS,
		gaAreas: MOCK_MANIFEST_HB_GA_AREAS
	},
	M300001: {
		manifest: MOCK_MANIFEST_QK7,
		levels: MOCK_MANIFEST_QK7_LEVELS,
		sections: MOCK_MANIFEST_QK7_SECTIONS,
		priceLevels: MOCK_MANIFEST_QK7_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_QK7_RS_AREAS,
		gaAreas: MOCK_MANIFEST_QK7_GA_AREAS
	},
	M400001: {
		manifest: MOCK_MANIFEST_PHUTHO,
		levels: MOCK_MANIFEST_PHUTHO_LEVELS,
		sections: MOCK_MANIFEST_PHUTHO_SECTIONS,
		priceLevels: MOCK_MANIFEST_PHUTHO_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_PHUTHO_RS_AREAS,
		gaAreas: MOCK_MANIFEST_PHUTHO_GA_AREAS
	},
	M600001: {
		manifest: MOCK_MANIFEST_TUYEN_SON,
		levels: MOCK_MANIFEST_TUYEN_SON_LEVELS,
		sections: MOCK_MANIFEST_TUYEN_SON_SECTIONS,
		priceLevels: MOCK_MANIFEST_TUYEN_SON_PRICE_LEVELS,
		rsAreas: MOCK_MANIFEST_TUYEN_SON_RS_AREAS,
		gaAreas: MOCK_MANIFEST_TUYEN_SON_GA_AREAS
	}
};
