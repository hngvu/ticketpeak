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

export const MOCK_MANIFEST_HANOI_OPERA_SECTIONS = [
	{ id: 'SEC-VIP', manifestId: 'M100001', description: 'VIP Front Row', color: '#F59E0B' },
	{
		id: 'SEC-A',
		manifestId: 'M100001',
		description: 'Standard A – Orchestra Center',
		color: '#3B82F6'
	},
	{
		id: 'SEC-B',
		manifestId: 'M100001',
		description: 'Standard B – Orchestra Wings',
		color: '#10B981'
	},
	{ id: 'SEC-C', manifestId: 'M100001', description: 'Balcony Seats', color: '#8B5CF6' }
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

export const MOCK_MANIFEST_MY_DINH_SECTIONS = [
	{ id: 'S-VIP', manifestId: 'M200001', description: 'VIP Floor Front', color: '#F59E0B' },
	{ id: 'S-GA', manifestId: 'M200001', description: 'General Admission Floor', color: '#EF4444' },
	{ id: 'S-A', manifestId: 'M200001', description: 'Stand A – North', color: '#3B82F6' },
	{ id: 'S-B', manifestId: 'M200001', description: 'Stand B – South', color: '#10B981' },
	{ id: 'S-C', manifestId: 'M200001', description: 'Stand C – East', color: '#6366F1' },
	{ id: 'S-D', manifestId: 'M200001', description: 'Stand D – West', color: '#EC4899' },
	{ id: 'S-U', manifestId: 'M200001', description: 'Upper Tier', color: '#8B5CF6' }
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

export const MOCK_MANIFEST_HB_SECTIONS = [
	{ id: 'SEC-BOX', manifestId: 'MHB0001', description: 'Box Seats', color: '#F59E0B' },
	{ id: 'SEC-ORCH', manifestId: 'MHB0001', description: 'Orchestra', color: '#3B82F6' },
	{ id: 'SEC-TER', manifestId: 'MHB0001', description: 'Terrace', color: '#8B5CF6' },
	{ id: 'SEC-GARD', manifestId: 'MHB0001', description: 'Garden Lawn', color: '#94A3B8' }
];

export const MOCK_MANIFEST_HB_PRICE_LEVELS = [
	{ id: 'P-BOX', manifestId: 'MHB0001', description: 'Box – $350', color: '#F59E0B' },
	{ id: 'P-PREM', manifestId: 'MHB0001', description: 'Premium – $220', color: '#1E40AF' },
	{ id: 'P-STD', manifestId: 'MHB0001', description: 'Standard – $120', color: '#3B82F6' },
	{ id: 'P-TER', manifestId: 'MHB0001', description: 'Terrace – $85', color: '#8B5CF6' },
	{ id: 'P-GARD', manifestId: 'MHB0001', description: 'Garden – $55', color: '#94A3B8' }
];

// ── Hollywood Bowl – Fan geometry ───────────────────────────────────────────────────
//
// Design principles:
//  - Fan center: CX=520, CY=500 (stage ở phía trái, fan mở sang phải)
//  - Orchestra: 6 rows × r 220–370, spread -75° → 75°, 7 sections (mhb gap 6°)
//  - Terrace:   5 rows × r 400–520, spread -60° → 40°, 3 sections
//  - Box:       3 rows × r 160–210, góc -110° → -80° (lõm vào bên trái)
//  - Mỗi row có đủ seats để ho arc có khoảng cách tương đương ~18px giữa các seat

const _HB_CX = 520,
	_HB_CY = 500;
const _toRad = (d: number) => (d * Math.PI) / 180;

/** Tạo polygon hình quạt (innerR, outerR, deg1, deg2) */
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

/**
 * Tạo các hàng ghế dạng cung tron (fan rows).
 * - rowCount rows, phân bố đều từ innerR → outerR
 * - Mỗi row: seatsPerRow ghế, phân bố đều từ deg1 → deg2 (sau khi đã trừ GAP)
 * - Khoảng cách arc giữa 2 ghế liên tiếp ≈ r * Δrad ≥ 16px (kiểm tra)
 */
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
	const SEAT_GAP_DEG = 6; // gap mỗi bên giữa section → 12° tổng cộng
	const ROW_GAP_R = 10; // px bỏ trống ở inner và outer edge
	const usableDeg1 = deg1 + SEAT_GAP_DEG;
	const usableDeg2 = deg2 - SEAT_GAP_DEG;
	return Array.from({ length: rowCount }, (_, ri) => {
		const rowChar = String.fromCharCode(startChar + ri);
		const rowId = `${areaId}-row-${rowChar}`;
		// R phân bố đều, bỏ gap nhỏ ở innerR và outerR
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

/**
 * Trả về flat points array cho một sector hình quạt
 * (inner arc → outer arc → đóng lại)
 * Được dùng bỌi renderer để vẽ đúng shape của từng section
 */
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
	// inner arc (deg1 → deg2)
	for (let i = 0; i <= steps; i++) {
		const a = _toRad(deg1 + ((deg2 - deg1) * i) / steps);
		pts.push(cx + innerR * Math.cos(a), cy + innerR * Math.sin(a));
	}
	// outer arc (deg2 → deg1)
	for (let i = steps; i >= 0; i--) {
		const a = _toRad(deg1 + ((deg2 - deg1) * i) / steps);
		pts.push(cx + outerR * Math.cos(a), cy + outerR * Math.sin(a));
	}
	return pts;
}

// Orchestra: 7 sections, spread -75° → 75° (150° tổng)
// Mỗi section: 150/7 ≈ 21.4°, gap 6° mỗi bên → usable ≈ 9.4°
// r_mid=295, arc usable = 295 * 9.4 * π/180 ≈ 48px → ~3 seats @ 18px
// center (D) rộng hơn 24° → usable 12° → ~6 seats
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

// Terrace: 3 sections, gap 6° mỗi bên
// spread 100°/3 = 33.3°, usable = 21.3° → r_mid=465 → arc=172px → ~10 seats @ 18px
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

// Box: 3 rows × 6 seats, góc -115° → -85° (gap 6° mỗi bên → usable 18°)
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

// Garden Lawn: đặt ngoài Terrace phía trên-phải, không có ghế lẻ
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

export const MOCK_MANIFESTS = [
	MOCK_MANIFEST_HANOI_OPERA,
	MOCK_MANIFEST_MY_DINH,
	MOCK_MANIFEST_HOLLYWOOD_BOWL
];

export const MOCK_MANIFEST_LEVELS = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_LEVELS,
	M200001: MOCK_MANIFEST_MY_DINH_LEVELS,
	MHB0001: MOCK_MANIFEST_HB_LEVELS
};

export const MOCK_MANIFEST_SECTIONS = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_SECTIONS,
	M200001: MOCK_MANIFEST_MY_DINH_SECTIONS,
	MHB0001: MOCK_MANIFEST_HB_SECTIONS
};

export const MOCK_MANIFEST_PRICE_LEVELS = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_PRICE_LEVELS,
	M200001: MOCK_MANIFEST_MY_DINH_PRICE_LEVELS,
	MHB0001: MOCK_MANIFEST_HB_PRICE_LEVELS
};

export const MOCK_MANIFEST_RS_AREAS: Record<string, any[]> = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_RS_AREAS,
	M200001: MOCK_MANIFEST_MY_DINH_RS_AREAS,
	MHB0001: MOCK_MANIFEST_HB_RS_AREAS
};

export const MOCK_MANIFEST_GA_AREAS = {
	M100001: MOCK_MANIFEST_HANOI_OPERA_GA_AREAS,
	M200001: MOCK_MANIFEST_MY_DINH_GA_AREAS,
	MHB0001: MOCK_MANIFEST_HB_GA_AREAS
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
export const MOCK_VENUE_MANIFESTS: Record<string, any[]> = {
	'018f4e1a-0006-4000-8000-000000000001': [MOCK_MANIFEST_HANOI_OPERA],
	'018f4e1a-0003-4000-8000-000000000001': [MOCK_MANIFEST_MY_DINH],
	'019e90ee-6afa-70fc-aa55-2159192f0729': [MOCK_MANIFEST_HOLLYWOOD_BOWL]
};

/** Full mock bundle for the manifest editor (venue + manifest + levels + sections + priceLevels + areas) */
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
	}
};
