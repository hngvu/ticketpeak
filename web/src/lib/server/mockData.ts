export const MOCK_CLASSIFICATIONS = [
	{
		id: 'segment-concerts',
		name: 'Concerts',
		slug: 'concerts',
		level: 1,
		isActive: true,
		parentId: null
	},
	{
		id: 'segment-sports',
		name: 'Sports',
		slug: 'sports',
		level: 1,
		isActive: true,
		parentId: null
	},
	{
		id: 'segment-arts',
		name: 'Arts & Theatre',
		slug: 'arts',
		level: 1,
		isActive: true,
		parentId: null
	},
	{
		id: 'segment-family',
		name: 'Family',
		slug: 'family',
		level: 1,
		isActive: true,
		parentId: null
	},

	// Concert genres
	{
		id: 'genre-pop',
		name: 'Pop',
		slug: 'pop',
		level: 2,
		isActive: true,
		parentId: 'segment-concerts'
	},
	{
		id: 'genre-rock',
		name: 'Rock',
		slug: 'rock',
		level: 2,
		isActive: true,
		parentId: 'segment-concerts'
	},
	{
		id: 'genre-indie',
		name: 'Indie & Folk',
		slug: 'indie',
		level: 2,
		isActive: true,
		parentId: 'segment-concerts'
	},
	{
		id: 'genre-edm',
		name: 'EDM & Electronic',
		slug: 'edm',
		level: 2,
		isActive: true,
		parentId: 'segment-concerts'
	},

	// Sports genres
	{
		id: 'genre-football',
		name: 'Football (Soccer)',
		slug: 'football',
		level: 2,
		isActive: true,
		parentId: 'segment-sports'
	},
	{
		id: 'genre-basketball',
		name: 'Basketball',
		slug: 'basketball',
		level: 2,
		isActive: true,
		parentId: 'segment-sports'
	},
	{
		id: 'genre-running',
		name: 'Marathons & Running',
		slug: 'running',
		level: 2,
		isActive: true,
		parentId: 'segment-sports'
	},

	// Arts genres
	{
		id: 'genre-musical',
		name: 'Musicals & Plays',
		slug: 'musical',
		level: 2,
		isActive: true,
		parentId: 'segment-arts'
	},
	{
		id: 'genre-comedy',
		name: 'Stand-Up Comedy',
		slug: 'comedy',
		level: 2,
		isActive: true,
		parentId: 'segment-arts'
	},
	{
		id: 'genre-dance',
		name: 'Classical Dance',
		slug: 'dance',
		level: 2,
		isActive: true,
		parentId: 'segment-arts'
	}
];

// ──────────────────────────────────────────────────────────────────────────────
// Mock Venues – matches VenueResponse API shape
// ──────────────────────────────────────────────────────────────────────────────

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

export const MOCK_ATTRACTIONS = [
	{
		id: 'artist-sontung',
		name: 'Sơn Tùng M-TP',
		slug: 'son-tung-mtp',
		type: 'Artist',
		description: "Vietnam's pop king, pioneer of modern V-Pop beats.",
		imageUrl:
			'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=400&q=80'
	},
	{
		id: 'artist-denvau',
		name: 'Đen Vâu',
		slug: 'den-vau',
		type: 'Artist',
		description: 'Award-winning Vietnamese rapper, famous for raw acoustic rap beats.',
		imageUrl:
			'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?auto=format&fit=crop&w=400&q=80'
	},
	{
		id: 'artist-taylorswift',
		name: 'Taylor Swift',
		slug: 'taylor-swift',
		type: 'Artist',
		description: 'Multi-Grammy winning global pop icon.',
		imageUrl:
			'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=400&q=80'
	},
	{
		id: 'team-hanoifc',
		name: 'Hanoi FC',
		slug: 'hanoi-fc',
		type: 'SportsTeam',
		description: 'Multi-time champions of the domestic V-League tournament.',
		imageUrl:
			'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=400&q=80'
	}
];

// Generate upcoming dates dynamically
const getFutureDate = (daysAhead: number, hours: number) => {
	const d = new Date();
	d.setDate(d.getDate() + daysAhead);
	d.setHours(hours, 0, 0, 0);
	return d.toISOString();
};

export const MOCK_EVENTS = [
	{
		id: 'event-sontung-hcm',
		title: 'Sơn Tùng M-TP | Sky Decibel Live Tour HCMC',
		slug: 'son-tung-decibel-hcm',
		startAt: getFutureDate(3, 19),
		venueId: '018f4e1a-0004-4000-8000-000000000001',
		venueName: 'Sân vận động Quân khu 7',
		cityName: 'Hồ Chí Minh',
		classifications: [{ id: 'genre-pop', name: 'Pop', slug: 'pop' }],
		attractions: [MOCK_ATTRACTIONS[0]]
	},
	{
		id: 'event-denvau-hn',
		title: 'Đen Vâu Live Concert - Show của Đen 2026',
		slug: 'show-cua-den-hn',
		startAt: getFutureDate(10, 19),
		venueId: '018f4e1a-0003-4000-8000-000000000001',
		venueName: 'Sân vận động Quốc gia Mỹ Đình',
		cityName: 'Hà Nội',
		classifications: [{ id: 'genre-indie', name: 'Indie & Folk', slug: 'indie' }],
		attractions: [MOCK_ATTRACTIONS[1]]
	},
	{
		id: 'event-taylor-swift-hn',
		title: 'Taylor Swift | The Eras Tour Vietnam Special',
		slug: 'the-eras-tour-vietnam',
		startAt: getFutureDate(18, 18),
		venueId: '018f4e1a-0003-4000-8000-000000000001',
		venueName: 'Sân vận động Quốc gia Mỹ Đình',
		cityName: 'Hà Nội',
		classifications: [{ id: 'genre-pop', name: 'Pop', slug: 'pop' }],
		attractions: [MOCK_ATTRACTIONS[2]]
	},
	{
		id: 'event-vleague-classic',
		title: 'V-League Classic Match: Hanoi FC vs HA Gia Lai',
		slug: 'hanoi-fc-vs-hagl',
		startAt: getFutureDate(1, 17),
		venueId: '018f4e1a-0003-4000-8000-000000000001',
		venueName: 'Sân vận động Quốc gia Mỹ Đình',
		cityName: 'Hà Nội',
		classifications: [{ id: 'genre-football', name: 'Football (Soccer)', slug: 'football' }],
		attractions: [MOCK_ATTRACTIONS[3]]
	},
	{
		id: 'event-miserables-theatre',
		title: 'Broadway Musical: Les Misérables (Những Người Khốn Khổ)',
		slug: 'les-miserables-vietnam',
		startAt: getFutureDate(2, 19),
		venueId: '018f4e1a-0006-4000-8000-000000000001',
		venueName: 'Nhà hát Lớn Hà Nội',
		cityName: 'Hà Nội',
		classifications: [{ id: 'genre-musical', name: 'Musicals & Plays', slug: 'musical' }],
		attractions: []
	},
	{
		id: 'event-comedy-night',
		title: 'Saigon Stand-Up Comedy Special Night 2026',
		slug: 'saigon-comedy-special',
		startAt: getFutureDate(4, 20),
		venueId: '018f4e1a-0001-4000-8000-000000000001',
		venueName: 'Nhà hát Hòa Bình',
		cityName: 'Hồ Chí Minh',
		classifications: [{ id: 'genre-comedy', name: 'Stand-Up Comedy', slug: 'comedy' }],
		attractions: []
	},
	{
		id: 'event-edm-ultrafest',
		title: 'Vibe Nation Electronic Dance Music Festival',
		slug: 'vibe-nation-edm-fest',
		startAt: getFutureDate(8, 15),
		venueId: '018f4e1a-0007-4000-8000-000000000001',
		venueName: 'Cung thể thao Tuyên Sơn',
		cityName: 'Đà Nẵng',
		classifications: [{ id: 'genre-edm', name: 'EDM & Electronic', slug: 'edm' }],
		attractions: []
	},
	{
		id: 'event-rapviet-stadium',
		title: 'Rap Việt All-Stars Concert 2026 Live in HCMC',
		slug: 'rap-viet-all-stars-2026',
		startAt: getFutureDate(12, 19),
		venueId: '018f4e1a-0004-4000-8000-000000000001',
		venueName: 'Sân vận động Quân khu 7',
		cityName: 'Hồ Chí Minh',
		classifications: [{ id: 'genre-indie', name: 'Indie & Folk', slug: 'indie' }],
		attractions: [MOCK_ATTRACTIONS[1]]
	},
	{
		id: 'event-hanoiphil-classical',
		title: 'Vietnam National Symphony Orchestra Concert Series',
		slug: 'vnso-classical-concert',
		startAt: getFutureDate(6, 19),
		venueId: '018f4e1a-0006-4000-8000-000000000001',
		venueName: 'Nhà hát Lớn Hà Nội',
		cityName: 'Hà Nội',
		classifications: [{ id: 'genre-dance', name: 'Classical Dance', slug: 'dance' }],
		attractions: []
	},
	{
		id: 'event-vba-finals',
		title: 'VBA Playoff Finals: Saigon Heat vs Danang Dragons',
		slug: 'saigon-heat-vs-danang-dragons',
		startAt: getFutureDate(14, 18),
		venueId: '018f4e1a-0005-4000-8000-000000000001',
		venueName: 'Nhà thi đấu Phú Thọ',
		cityName: 'Hồ Chí Minh',
		classifications: [{ id: 'genre-basketball', name: 'Basketball', slug: 'basketball' }],
		attractions: []
	}
];

// ──────────────────────────────────────────────────────────────────────────────
// Venue Manifest Mock Data
// Mirrors the exact API response shapes (ManifestResponse, LevelResponse,
// SectionResponse, SeatRowResponse, SeatResponse, PriceLevelResponse).
//
// Hierarchy: Manifest → Level, Section, PriceLevel
//            RS Section → SeatRow → Seat
//            GA Section (no rows/seats; capacity stored on section)
// ──────────────────────────────────────────────────────────────────────────────

// TypeScript types matching the API DTOs exactly
type SeatStatus = 'AVAILABLE' | 'UNAVAILABLE' | 'RESERVED' | 'SOLD';
type SectionType = 'GA' | 'RS';
type ManifestStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

interface MockSeat {
	id: string;
	rowId: string;
	name: string;
	positionX: number | null;
	positionY: number;
	status: SeatStatus;
	priceLevelId: string | null;
	sectionId: string | null;
}

interface MockSeatRow {
	id: string;
	sectionId: string;
	name: string;
}

interface MockSection {
	id: string;
	type: SectionType;
	name: string | null;
	color: string | null;
	levelId: string | null;
	capacity: number | null;
	uiData: Record<string, unknown> | null;
}

interface MockLevel {
	id: string;
	manifestId: string;
	description: string;
	color: string | null;
}

interface MockPriceLevel {
	id: string;
	manifestId: string;
	description: string | null;
	color: string | null;
}

interface MockManifest {
	id: string;
	venueId: string;
	description: string;
	totalCapacity: number;
	status: ManifestStatus;
	createdAt: string;
	updatedAt: string;
	objects: Record<string, unknown>[] | null;
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

/**
 * Generate a rectangular block of seats for one row.
 * Matches SeatResponse: id, rowId, name, positionX, positionY, status, priceLevelId, sectionId
 */
function _genSeats(
	rowId: string,
	startNum: number,
	count: number,
	startX: number,
	rowY: number,
	sectionId: string | null = null,
	priceLevelId: string | null = null
): MockSeat[] {
	return Array.from({ length: count }, (_, i) => ({
		id: `${rowId}-s${String(startNum + i).padStart(3, '0')}`,
		rowId,
		name: String(startNum + i).padStart(3, '0'),
		positionX: startX + i * 18,
		positionY: rowY,
		status: 'AVAILABLE' as SeatStatus,
		priceLevelId,
		sectionId
	}));
}

/**
 * Generate rows + seats for a rectangular seating section.
 * Row names are uppercase letters (A, B, C…) starting from startRowCharCode.
 */
function _genRows(
	sectionId: string,
	startRowCharCode: number,
	rowCount: number,
	seatsPerRow: number,
	startSeatNum: number,
	startX: number,
	startY: number,
	sectionId2: string | null = null,
	priceLevelId: string | null = null
): { rows: MockSeatRow[]; seats: MockSeat[] } {
	const rows: MockSeatRow[] = [];
	const seats: MockSeat[] = [];
	for (let rIdx = 0; rIdx < rowCount; rIdx++) {
		const rowName = String.fromCharCode(startRowCharCode + rIdx);
		const rowId = `${sectionId}-row-${rowName}`;
		const rowY = startY + rIdx * 22;
		rows.push({ id: rowId, sectionId, name: rowName });
		seats.push(
			..._genSeats(rowId, startSeatNum, seatsPerRow, startX, rowY, sectionId2, priceLevelId)
		);
	}
	return { rows, seats };
}

// ─── Nhà hát Lớn Hà Nội – Concert Layout (M100001) ───────────────────────────

export const MOCK_MANIFEST_HANOI_OPERA: MockManifest = {
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

export const MOCK_MANIFEST_HANOI_OPERA_LEVELS: MockLevel[] = [
	{ id: 'L1', manifestId: 'M100001', description: 'Orchestra (Tầng trệt)', color: '#3B82F6' },
	{ id: 'L2', manifestId: 'M100001', description: 'Balcony (Tầng lầu)', color: '#8B5CF6' }
];

export const MOCK_MANIFEST_HANOI_OPERA_PRICE_LEVELS: MockPriceLevel[] = [
	{ id: 'PL-VIP', manifestId: 'M100001', description: 'VIP – 2.500.000₫', color: '#F59E0B' },
	{ id: 'PL-PREM', manifestId: 'M100001', description: 'Premium – 1.500.000₫', color: '#3B82F6' },
	{ id: 'PL-STD', manifestId: 'M100001', description: 'Standard – 800.000₫', color: '#10B981' }
];

// Sections (SectionResponse): id, type, name, color, levelId, capacity, uiData
export const MOCK_MANIFEST_HANOI_OPERA_SECTIONS: MockSection[] = [
	{
		id: 'SEC-VIP',
		type: 'RS',
		name: 'VIP',
		color: '#F59E0B',
		levelId: 'L1',
		capacity: 32,
		uiData: { x: 220, y: 140, width: 460, height: 120, curvature: 20 }
	},
	{
		id: 'SEC-A',
		type: 'RS',
		name: 'Orchestra A',
		color: '#3B82F6',
		levelId: 'L1',
		capacity: 64,
		uiData: { x: 220, y: 280, width: 460, height: 180, curvature: 10 }
	},
	{
		id: 'SEC-B',
		type: 'RS',
		name: 'Side Wings',
		color: '#10B981',
		levelId: 'L1',
		capacity: 18,
		uiData: { x: 60, y: 280, width: 140, height: 140 }
	},
	{
		id: 'SEC-C',
		type: 'RS',
		name: 'Balcony',
		color: '#8B5CF6',
		levelId: 'L2',
		capacity: 40,
		uiData: { x: 120, y: 500, width: 660, height: 100, curvature: 5 }
	},
	{
		id: 'SEC-GA',
		type: 'GA',
		name: 'Standing Area',
		color: '#94A3B8',
		levelId: 'L1',
		capacity: 50,
		uiData: { x: 60, y: 140, width: 140, height: 100 }
	}
];

// Rows & Seats
const _haVip = _genRows('SEC-VIP', 65, 2, 16, 1, 240, 150, 'SEC-VIP', 'PL-VIP');
const _haCtr = _genRows('SEC-A', 67, 4, 16, 1, 240, 290, 'SEC-A', 'PL-PREM');
const _haWng = _genRows('SEC-B', 76, 3, 6, 1, 70, 290, 'SEC-B', 'PL-STD');
const _haBlc = _genRows('SEC-C', 80, 2, 20, 1, 140, 510, 'SEC-C', 'PL-STD');

export const MOCK_MANIFEST_HANOI_OPERA_ROWS: MockSeatRow[] = [
	..._haVip.rows,
	..._haCtr.rows,
	..._haWng.rows,
	..._haBlc.rows
];

export const MOCK_MANIFEST_HANOI_OPERA_SEATS: MockSeat[] = [
	..._haVip.seats,
	..._haCtr.seats,
	..._haWng.seats,
	..._haBlc.seats
];

// ─── Hollywood Bowl – Summer Concert Series (MHB0001) ─────────────────────────

export const MOCK_MANIFEST_HOLLYWOOD_BOWL: MockManifest = {
	id: 'MHB0001',
	venueId: '019e90ee-6afa-70fc-aa55-2159192f0729',
	description: 'Summer Concert Series – Hollywood Bowl',
	totalCapacity: 17500,
	status: 'PUBLISHED',
	createdAt: '2026-01-10T08:00:00Z',
	updatedAt: '2026-05-01T12:00:00Z',
	objects: [{ type: 'stage', text: 'SHELL', x: 460, y: 340, width: 120, height: 160 }]
};

export const MOCK_MANIFEST_HB_LEVELS: MockLevel[] = [
	{ id: 'L-ORCH', manifestId: 'MHB0001', description: 'Orchestra', color: '#3B82F6' },
	{ id: 'L-TER', manifestId: 'MHB0001', description: 'Terrace', color: '#8B5CF6' },
	{ id: 'L-BOX', manifestId: 'MHB0001', description: 'Box', color: '#F59E0B' },
	{ id: 'L-GARD', manifestId: 'MHB0001', description: 'Garden', color: '#94A3B8' }
];

export const MOCK_MANIFEST_HB_PRICE_LEVELS: MockPriceLevel[] = [
	{ id: 'P-BOX', manifestId: 'MHB0001', description: 'Box – $350', color: '#F59E0B' },
	{ id: 'P-PREM', manifestId: 'MHB0001', description: 'Premium – $220', color: '#1E40AF' },
	{ id: 'P-STD', manifestId: 'MHB0001', description: 'Standard – $120', color: '#3B82F6' },
	{ id: 'P-TER', manifestId: 'MHB0001', description: 'Terrace – $85', color: '#8B5CF6' },
	{ id: 'P-GARD', manifestId: 'MHB0001', description: 'Garden – $55', color: '#94A3B8' }
];

// ── Fan-shape geometry helpers ────────────────────────────────────────────────
// The amphitheater is fan-shaped. Sections are arc slices described by polygons
// stored in uiData.polygon ([x,y][] in canvas coordinates).
// Center of the fan (stage focus point):
const _HB_CX = 520;
const _HB_CY = 420;
const _toRad = (deg: number) => (deg * Math.PI) / 180;

/** Build an arc polygon (innerR → outerR, deg1 → deg2) as [x,y][] pairs. */
function _fanPolygon(
	innerR: number,
	outerR: number,
	deg1: number,
	deg2: number,
	steps = 30
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

/**
 * Generate fan-shaped rows + seats for an arc section.
 * Seat positions are computed in polar coordinates around (_HB_CX, _HB_CY).
 * Matches SeatResponse exactly (no extra fields).
 */
function _fanSeats(
	sectionId: string,
	startCharCode: number,
	rowCount: number,
	seatsPerRow: number,
	innerR: number,
	outerR: number,
	deg1: number,
	deg2: number,
	priceLevelId: string
): { rows: MockSeatRow[]; seats: MockSeat[] } {
	const rows: MockSeatRow[] = [];
	const seats: MockSeat[] = [];
	const rPadding = 12; // pixels
	const aPadding = 2; // degrees
	for (let rIdx = 0; rIdx < rowCount; rIdx++) {
		const rowName = String.fromCharCode(startCharCode + rIdx);
		const rowId = `${sectionId}-row-${rowName}`;
		const r =
			innerR +
			rPadding +
			((outerR - rPadding - (innerR + rPadding)) * rIdx) / Math.max(rowCount - 1, 1);
		rows.push({ id: rowId, sectionId, name: rowName });
		for (let sIdx = 0; sIdx < seatsPerRow; sIdx++) {
			const deg =
				deg1 +
				aPadding +
				((deg2 - aPadding - (deg1 + aPadding)) * sIdx) / Math.max(seatsPerRow - 1, 1);
			const a = _toRad(deg);
			seats.push({
				id: `${rowId}-s${String(sIdx + 1).padStart(3, '0')}`,
				rowId,
				name: String(sIdx + 1).padStart(3, '0'),
				positionX: Math.round(_HB_CX + r * Math.cos(a)),
				positionY: Math.round(_HB_CY + r * Math.sin(a)),
				status: 'AVAILABLE',
				priceLevelId,
				sectionId
			});
		}
	}
	return { rows, seats };
}

const _HB = 'MHB0001';

// Generate rows & seats per arc section
const _hbA = _fanSeats(`${_HB}-AREA-A`, 65, 9, 6, 180, 360, -80, -61, 'P-STD');
const _hbB = _fanSeats(`${_HB}-AREA-B`, 65, 9, 6, 180, 360, -59, -41, 'P-STD');
const _hbC = _fanSeats(`${_HB}-AREA-C`, 65, 9, 8, 180, 360, -39, -16, 'P-STD');
const _hbD = _fanSeats(`${_HB}-AREA-D`, 65, 9, 10, 180, 360, -14, 14, 'P-PREM');
const _hbE = _fanSeats(`${_HB}-AREA-E`, 65, 9, 8, 180, 360, 16, 39, 'P-STD');
const _hbF = _fanSeats(`${_HB}-AREA-F`, 65, 9, 6, 180, 360, 41, 59, 'P-STD');
const _hbG = _fanSeats(`${_HB}-AREA-G`, 65, 9, 6, 180, 360, 61, 80, 'P-STD');
const _hbT1 = _fanSeats(`${_HB}-AREA-T1`, 80, 6, 16, 370, 520, -65, -36, 'P-TER');
const _hbT2 = _fanSeats(`${_HB}-AREA-T2`, 80, 6, 22, 370, 520, -34, 4, 'P-TER');
const _hbT3 = _fanSeats(`${_HB}-AREA-T3`, 80, 6, 16, 370, 520, 6, 35, 'P-TER');
const _hbBox = _fanSeats(`${_HB}-AREA-BOX`, 65, 4, 7, 185, 290, -107, -83, 'P-BOX');

// Sections: each arc + flat garden GA area
export const MOCK_MANIFEST_HB_SECTIONS: MockSection[] = [
	{
		id: `${_HB}-AREA-A`,
		type: 'RS',
		name: 'ORCH A',
		color: '#60A5FA',
		levelId: 'L-ORCH',
		capacity: _hbA.seats.length,
		uiData: { polygon: _fanPolygon(180, 360, -80, -61) }
	},
	{
		id: `${_HB}-AREA-B`,
		type: 'RS',
		name: 'ORCH B',
		color: '#3B82F6',
		levelId: 'L-ORCH',
		capacity: _hbB.seats.length,
		uiData: { polygon: _fanPolygon(180, 360, -59, -41) }
	},
	{
		id: `${_HB}-AREA-C`,
		type: 'RS',
		name: 'ORCH C',
		color: '#2563EB',
		levelId: 'L-ORCH',
		capacity: _hbC.seats.length,
		uiData: { polygon: _fanPolygon(180, 360, -39, -16) }
	},
	{
		id: `${_HB}-AREA-D`,
		type: 'RS',
		name: 'ORCH D',
		color: '#1E40AF',
		levelId: 'L-ORCH',
		capacity: _hbD.seats.length,
		uiData: { polygon: _fanPolygon(180, 360, -14, 14) }
	},
	{
		id: `${_HB}-AREA-E`,
		type: 'RS',
		name: 'ORCH E',
		color: '#2563EB',
		levelId: 'L-ORCH',
		capacity: _hbE.seats.length,
		uiData: { polygon: _fanPolygon(180, 360, 16, 39) }
	},
	{
		id: `${_HB}-AREA-F`,
		type: 'RS',
		name: 'ORCH F',
		color: '#3B82F6',
		levelId: 'L-ORCH',
		capacity: _hbF.seats.length,
		uiData: { polygon: _fanPolygon(180, 360, 41, 59) }
	},
	{
		id: `${_HB}-AREA-G`,
		type: 'RS',
		name: 'ORCH G',
		color: '#60A5FA',
		levelId: 'L-ORCH',
		capacity: _hbG.seats.length,
		uiData: { polygon: _fanPolygon(180, 360, 61, 80) }
	},
	{
		id: `${_HB}-AREA-T1`,
		type: 'RS',
		name: 'TER 1',
		color: '#8B5CF6',
		levelId: 'L-TER',
		capacity: _hbT1.seats.length,
		uiData: { polygon: _fanPolygon(370, 520, -65, -36) }
	},
	{
		id: `${_HB}-AREA-T2`,
		type: 'RS',
		name: 'TER 2',
		color: '#7C3AED',
		levelId: 'L-TER',
		capacity: _hbT2.seats.length,
		uiData: { polygon: _fanPolygon(370, 520, -34, 4) }
	},
	{
		id: `${_HB}-AREA-T3`,
		type: 'RS',
		name: 'TER 3',
		color: '#8B5CF6',
		levelId: 'L-TER',
		capacity: _hbT3.seats.length,
		uiData: { polygon: _fanPolygon(370, 520, 6, 35) }
	},
	{
		id: `${_HB}-AREA-BOX`,
		type: 'RS',
		name: 'BOX',
		color: '#F59E0B',
		levelId: 'L-BOX',
		capacity: _hbBox.seats.length,
		uiData: { polygon: _fanPolygon(185, 290, -107, -83) }
	},
	// Garden is a flat GA area (no rows/seats – capacity managed externally)
	{
		id: `${_HB}-AREA-GARD`,
		type: 'GA',
		name: 'Garden',
		color: '#94A3B8',
		levelId: 'L-GARD',
		capacity: 8000,
		uiData: {
			polygon: [
				[820, 50],
				[1020, 50],
				[1020, 250],
				[820, 250]
			] as [number, number][]
		}
	}
];

export const MOCK_MANIFEST_HB_ROWS: MockSeatRow[] = [
	..._hbA.rows,
	..._hbB.rows,
	..._hbC.rows,
	..._hbD.rows,
	..._hbE.rows,
	..._hbF.rows,
	..._hbG.rows,
	..._hbT1.rows,
	..._hbT2.rows,
	..._hbT3.rows,
	..._hbBox.rows
];

export const MOCK_MANIFEST_HB_SEATS: MockSeat[] = [
	..._hbA.seats,
	..._hbB.seats,
	..._hbC.seats,
	..._hbD.seats,
	..._hbE.seats,
	..._hbF.seats,
	..._hbG.seats,
	..._hbT1.seats,
	..._hbT2.seats,
	..._hbT3.seats,
	..._hbBox.seats
];

// ── Convenience aggregates ────────────────────────────────────────────────────

export const MOCK_MANIFESTS: MockManifest[] = [
	MOCK_MANIFEST_HANOI_OPERA,
	MOCK_MANIFEST_HOLLYWOOD_BOWL
];

export const MOCK_MANIFEST_LEVELS: Record<string, MockLevel[]> = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_LEVELS,
	MHB0001: MOCK_MANIFEST_HB_LEVELS
};

export const MOCK_MANIFEST_SECTIONS: Record<string, MockSection[]> = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_SECTIONS,
	MHB0001: MOCK_MANIFEST_HB_SECTIONS
};

export const MOCK_MANIFEST_ROWS: Record<string, MockSeatRow[]> = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_ROWS,
	MHB0001: MOCK_MANIFEST_HB_ROWS
};

export const MOCK_MANIFEST_SEATS: Record<string, MockSeat[]> = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_SEATS,
	MHB0001: MOCK_MANIFEST_HB_SEATS
};

export const MOCK_MANIFEST_PRICE_LEVELS: Record<string, MockPriceLevel[]> = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_PRICE_LEVELS,
	MHB0001: MOCK_MANIFEST_HB_PRICE_LEVELS
};

// ──────────────────────────────────────────────────────────────────────────────
// Venue ↔ Manifest convenience mappings
// Used by ops/venues pages to serve mock data without hitting the backend API.
// ──────────────────────────────────────────────────────────────────────────────

/** Lookup a mock venue by its ID */
export const MOCK_VENUE_BY_ID: Record<string, (typeof MOCK_VENUES)[number]> = Object.fromEntries(
	MOCK_VENUES.map((v) => [v.id, v])
);

/** For each mock venue, list which manifests belong to it */
export const MOCK_VENUE_MANIFESTS: Record<string, MockManifest[]> = {
	'018f4e1a-0006-4000-8000-000000000001': [MOCK_MANIFEST_HANOI_OPERA],
	'019e90ee-6afa-70fc-aa55-2159192f0729': [MOCK_MANIFEST_HOLLYWOOD_BOWL]
};

/**
 * Full mock bundle for the manifest editor page.
 * Keyed by venueId (the route param [id]).
 * Matches the shape returned by the real +page.server.ts load function.
 */
export const MOCK_VENUE_MANIFEST_EDITOR_DATA: Record<
	string,
	{
		venue: (typeof MOCK_VENUES)[number];
		manifest: MockManifest;
		levels: MockLevel[];
		sections: MockSection[];
		priceLevels: MockPriceLevel[];
		rows: MockSeatRow[];
		seats: MockSeat[];
	}
> = {
	'018f4e1a-0006-4000-8000-000000000001': {
		venue: MOCK_VENUE_BY_ID['018f4e1a-0006-4000-8000-000000000001'],
		manifest: MOCK_MANIFEST_HANOI_OPERA,
		levels: MOCK_MANIFEST_HANOI_OPERA_LEVELS,
		sections: MOCK_MANIFEST_HANOI_OPERA_SECTIONS,
		priceLevels: MOCK_MANIFEST_HANOI_OPERA_PRICE_LEVELS,
		rows: MOCK_MANIFEST_HANOI_OPERA_ROWS,
		seats: MOCK_MANIFEST_HANOI_OPERA_SEATS
	},
	'019e90ee-6afa-70fc-aa55-2159192f0729': {
		venue: MOCK_VENUE_BY_ID['019e90ee-6afa-70fc-aa55-2159192f0729'],
		manifest: MOCK_MANIFEST_HOLLYWOOD_BOWL,
		levels: MOCK_MANIFEST_HB_LEVELS,
		sections: MOCK_MANIFEST_HB_SECTIONS,
		priceLevels: MOCK_MANIFEST_HB_PRICE_LEVELS,
		rows: MOCK_MANIFEST_HB_ROWS,
		seats: MOCK_MANIFEST_HB_SEATS
	}
};
