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

export const MOCK_VENUES = [
	{
		id: 'venue-my-dinh',
		name: 'Sân vận động Quốc gia Mỹ Đình',
		slug: 'san-van-dong-my-dinh',
		city: 'Hà Nội',
		address: 'Đường Lê Đức Thọ, Mỹ Đình 1, Nam Từ Liêm',
		capacity: 40000,
		imageUrl:
			'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=800&q=80',
		eventCount: 4
	},
	{
		id: 'venue-quan-khu-7',
		name: 'Sân vận động Quân khu 7',
		slug: 'san-van-dong-quan-khu-7',
		city: 'Hồ Chí Minh',
		address: '202 Hoàng Văn Thụ, Phường 9, Phú Nhuận',
		capacity: 25000,
		imageUrl:
			'https://images.unsplash.com/photo-1487466365836-440d8294a87a?auto=format&fit=crop&w=800&q=80',
		eventCount: 3
	},
	{
		id: 'venue-phu-tho',
		name: 'Nhà thi đấu Phú Thọ',
		slug: 'nha-thi-dau-phu-tho',
		city: 'Hồ Chí Minh',
		address: '1 Lữ Gia, Phường 15, Quận 11',
		capacity: 8000,
		imageUrl:
			'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=800&q=80',
		eventCount: 2
	},
	{
		id: 'venue-hanoi-opera',
		name: 'Nhà hát Lớn Hà Nội',
		slug: 'nha-hat-lon-ha-noi',
		city: 'Hà Nội',
		address: '1 Tràng Tiền, Phan Chu Trinh, Hoàn Kiếm',
		capacity: 600,
		imageUrl:
			'https://images.unsplash.com/photo-1503095396549-807759245b35?auto=format&fit=crop&w=800&q=80',
		eventCount: 3
	},
	{
		id: 'venue-hoa-binh',
		name: 'Nhà hát Hòa Bình',
		slug: 'nha-hat-hoa-binh',
		city: 'Hồ Chí Minh',
		address: '240 Đường 3 Tháng 2, Phường 12, Quận 10',
		capacity: 2300,
		imageUrl:
			'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=800&q=80',
		eventCount: 2
	},
	{
		id: 'venue-da-nang-tuyen-son',
		name: 'Cung thể thao Tuyên Sơn',
		slug: 'cung-the-thao-tuyen-son',
		city: 'Đà Nẵng',
		address: 'Phường Hòa Cường Bắc, Hải Châu',
		capacity: 7000,
		imageUrl:
			'https://images.unsplash.com/photo-1472712739516-7ad2b786e1f7?auto=format&fit=crop&w=800&q=80',
		eventCount: 2
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
		venueId: 'venue-quan-khu-7',
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
		venueId: 'venue-my-dinh',
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
		venueId: 'venue-my-dinh',
		venueName: 'Sân vận động Quốc gia Mỹ Đình',
		cityName: 'Hà Nội',
		classifications: [{ id: 'genre-pop', name: 'Pop', slug: 'pop' }],
		attractions: [MOCK_ATTRACTIONS[2]]
	},
	{
		id: 'event-vleague-classic',
		title: 'V-League Classic Match: Hanoi FC vs HA Gia Lai',
		slug: 'hanoi-fc-vs-hagl',
		startAt: getFutureDate(1, 17), // this weekend
		venueId: 'venue-my-dinh',
		venueName: 'Sân vận động Quốc gia Mỹ Đình',
		cityName: 'Hà Nội',
		classifications: [{ id: 'genre-football', name: 'Football (Soccer)', slug: 'football' }],
		attractions: [MOCK_ATTRACTIONS[3]]
	},
	{
		id: 'event-miserables-theatre',
		title: 'Broadway Musical: Les Misérables (Những Người Khốn Khổ)',
		slug: 'les-miserables-vietnam',
		startAt: getFutureDate(2, 19), // this weekend
		venueId: 'venue-hanoi-opera',
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
		venueId: 'venue-hoa-binh',
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
		venueId: 'venue-da-nang-tuyen-son',
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
		venueId: 'venue-quan-khu-7',
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
		venueId: 'venue-hanoi-opera',
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
		venueId: 'venue-phu-tho',
		venueName: 'Nhà thi đấu Phú Thọ',
		cityName: 'Hồ Chí Minh',
		classifications: [{ id: 'genre-basketball', name: 'Basketball', slug: 'basketball' }],
		attractions: []
	}
];
