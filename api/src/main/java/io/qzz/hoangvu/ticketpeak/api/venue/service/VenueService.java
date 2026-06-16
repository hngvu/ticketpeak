package io.qzz.hoangvu.ticketpeak.api.venue.service;

import io.qzz.hoangvu.ticketpeak.api.venue.dto.*;
import io.qzz.hoangvu.ticketpeak.api.venue.exception.VenueException;
import io.qzz.hoangvu.ticketpeak.api.venue.model.*;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VenueService {

    private final VenueRepository venueRepository;
    private final ManifestRepository manifestRepository;
    private final LevelRepository levelRepository;
    private final SectionRepository sectionRepository;
    private final PriceLevelRepository priceLevelRepository;
    private final SeatRowRepository seatRowRepository;
    private final SeatRepository seatRepository;

    public VenueService(VenueRepository venueRepository, ManifestRepository manifestRepository,
                        LevelRepository levelRepository, SectionRepository sectionRepository,
                        PriceLevelRepository priceLevelRepository, SeatRowRepository seatRowRepository,
                        SeatRepository seatRepository) {
        this.venueRepository = venueRepository;
        this.manifestRepository = manifestRepository;
        this.levelRepository = levelRepository;
        this.sectionRepository = sectionRepository;
        this.priceLevelRepository = priceLevelRepository;
        this.seatRowRepository = seatRowRepository;
        this.seatRepository = seatRepository;
    }

    // ======================== VENUE ========================

    @Transactional
    public VenueResponse createVenue(CreateVenueRequest req) {
        Venue venue = Venue.builder()
                .name(req.name()).address(req.address()).city(req.city()).country(req.country())
                .latitude(req.latitude()).longitude(req.longitude()).phone(req.phone()).email(req.email())
                .website(req.website()).description(req.description()).thumbnailUrl(req.thumbnailUrl())
                .images(req.images()).status(VenueStatus.ACTIVE)
                .build();
        return VenueResponse.from(venueRepository.save(venue));
    }

    @Transactional(readOnly = true)
    public Page<VenueResponse> listVenues(String name, VenueStatus status, Pageable pageable) {
        String nameParam = (name != null && !name.isBlank()) ? "%" + name + "%" : null;
        return venueRepository.search(nameParam, status, pageable).map(VenueResponse::from);
    }

    @Transactional(readOnly = true)
    public VenueResponse getVenue(UUID id) {
        return VenueResponse.from(requireVenue(id));
    }

    @Transactional
    public VenueResponse updateVenue(UUID id, UpdateVenueRequest req) {
        Venue venue = requireVenue(id);
        if (req.name() != null) venue.setName(req.name());
        if (req.address() != null) venue.setAddress(req.address());
        if (req.city() != null) venue.setCity(req.city());
        if (req.country() != null) venue.setCountry(req.country());
        if (req.latitude() != null) venue.setLatitude(req.latitude());
        if (req.longitude() != null) venue.setLongitude(req.longitude());
        if (req.phone() != null) venue.setPhone(req.phone());
        if (req.email() != null) venue.setEmail(req.email());
        if (req.website() != null) venue.setWebsite(req.website());
        if (req.description() != null) venue.setDescription(req.description());
        if (req.thumbnailUrl() != null) venue.setThumbnailUrl(req.thumbnailUrl());
        if (req.images() != null) venue.setImages(req.images());
        return VenueResponse.from(venueRepository.save(venue));
    }

    @Transactional
    public VenueResponse activateVenue(UUID id) {
        Venue venue = requireVenue(id);
        venue.setStatus(VenueStatus.ACTIVE);
        return VenueResponse.from(venueRepository.save(venue));
    }

    @Transactional
    public VenueResponse deactivateVenue(UUID id) {
        Venue venue = requireVenue(id);
        venue.setStatus(VenueStatus.INACTIVE);
        return VenueResponse.from(venueRepository.save(venue));
    }

    // ======================== MANIFEST ========================

    @Transactional
    public ManifestResponse createManifest(UUID venueId, CreateManifestRequest req) {
        Venue venue = requireVenue(venueId);
        if (manifestRepository.existsById(req.id())) {
            throw VenueException.manifestIdExists(req.id());
        }
        Manifest manifest = Manifest.builder()
                .id(req.id()).venue(venue).description(req.description())
                .totalCapacity(req.totalCapacity()).status(ManifestStatus.DRAFT)
                .objects(req.objects())
                .build();
        return ManifestResponse.from(manifestRepository.save(manifest));
    }

    @Transactional(readOnly = true)
    public List<ManifestResponse> listManifests(UUID venueId) {
        requireVenue(venueId);
        return manifestRepository.findByVenueId(venueId).stream().map(ManifestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ManifestResponse getManifest(String manifestId) {
        return ManifestResponse.from(requireManifest(manifestId));
    }

    @Transactional
    public ManifestResponse publishManifest(String manifestId) {
        Manifest manifest = requireManifest(manifestId);
        if (manifest.getStatus() != ManifestStatus.DRAFT) {
            throw VenueException.invalidManifestStatus();
        }
        // Enforce at-most-one PUBLISHED per venue
        manifestRepository.findByVenueIdAndStatus(manifest.getVenue().getId(), ManifestStatus.PUBLISHED)
                .ifPresent(existing -> {
                    existing.setStatus(ManifestStatus.ARCHIVED);
                    manifestRepository.save(existing);
                });
        manifest.setStatus(ManifestStatus.PUBLISHED);
        return ManifestResponse.from(manifestRepository.save(manifest));
    }

    @Transactional
    public ManifestResponse archiveManifest(String manifestId) {
        Manifest manifest = requireManifest(manifestId);
        if (manifest.getStatus() == ManifestStatus.ARCHIVED) {
            throw VenueException.alreadyArchived();
        }
        manifest.setStatus(ManifestStatus.ARCHIVED);
        return ManifestResponse.from(manifestRepository.save(manifest));
    }

    @Transactional
    public ManifestResponse updateManifest(String manifestId, UpdateManifestRequest req) {
        Manifest manifest = requireManifest(manifestId);
        manifest.setDescription(req.description());
        manifest.setTotalCapacity(req.totalCapacity());
        manifest.setObjects(req.objects());
        return ManifestResponse.from(manifestRepository.save(manifest));
    }

    @Transactional
    public ManifestResponse cloneManifest(String sourceManifestId, CloneManifestRequest req) {
        Manifest source = requireManifest(sourceManifestId);
        String newId = req.newId() != null ? req.newId() : sourceManifestId + "-clone";
        String newDescription = req.description() != null ? req.description() : source.getDescription() + " (clone)";

        if (manifestRepository.existsById(newId)) {
            throw VenueException.manifestIdExists(newId);
        }
        Manifest clone = Manifest.builder()
                .id(newId).venue(source.getVenue()).description(newDescription)
                .totalCapacity(source.getTotalCapacity()).status(ManifestStatus.DRAFT)
                .objects(source.getObjects() != null ? new java.util.ArrayList<>(source.getObjects()) : null)
                .build();
        manifestRepository.save(clone);

        // Clone lookup tables in batch
        List<Level> levels = levelRepository.findByManifestId(sourceManifestId).stream()
                .map(l -> Level.builder().id(l.getId()).manifest(clone).description(l.getDescription()).color(l.getColor()).build())
                .toList();
        levelRepository.saveAll(levels);

        List<Section> sections = sectionRepository.findByManifestId(sourceManifestId).stream()
                .map(s -> Section.builder()
                        .id(newId + "-" + s.getId())
                        .manifest(clone)
                        .type(s.getType())
                        .color(s.getColor())
                        .levelId(s.getLevelId())
                        .capacity(s.getCapacity())
                        .uiData(s.getUiData())
                        .build())
                .toList();
        sections = sectionRepository.saveAll(sections);

        List<PriceLevel> priceLevels = priceLevelRepository.findByManifestId(sourceManifestId).stream()
                .map(p -> PriceLevel.builder().id(p.getId()).manifest(clone).description(p.getDescription()).color(p.getColor()).build())
                .toList();
        priceLevelRepository.saveAll(priceLevels);

        java.util.Map<String, Section> sectionMap = sections.stream()
                .collect(java.util.stream.Collectors.toMap(Section::getId, java.util.function.Function.identity()));

        List<SeatRow> seatRows = new java.util.ArrayList<>();
        List<Seat> seats = new java.util.ArrayList<>();

        List<Section> sourceSections = sectionRepository.findByManifestId(sourceManifestId);
        for (Section s : sourceSections) {
            if (s.getType() != SectionType.RS) continue;

            String newSectionId = newId + "-" + s.getId();
            Section newSection = sectionMap.get(newSectionId);
            if (newSection == null) {
                continue;
            }

            List<SeatRow> rowsInSection = seatRowRepository.findBySectionId(s.getId());
            for (SeatRow row : rowsInSection) {
                String newRowId = newId + "-" + row.getId();
                SeatRow newRow = SeatRow.builder()
                        .id(newRowId)
                        .section(newSection)
                        .name(row.getName())
                        
                        .build();
                seatRows.add(newRow);

                List<Seat> seatsInRow = seatRepository.findBySeatRowId(row.getId());
                for (Seat seat : seatsInRow) {
                    Seat newSeat = Seat.builder()
                            .id(newId + "-" + seat.getId())
                            .seatRow(newRow)
                            .name(seat.getName())
                            .positionX(seat.getPositionX())
                            .positionY(seat.getPositionY())
                            .status(SeatStatus.AVAILABLE)
                            .priceLevelId(seat.getPriceLevelId())
                            .sectionId(seat.getSectionId() != null ? newId + "-" + seat.getSectionId() : null)
                            .build();
                    seats.add(newSeat);
                }
            }
        }

        seatRowRepository.saveAll(seatRows);
        seatRepository.saveAll(seats);

        return ManifestResponse.from(clone);
    }

    private static final List<String> PRESET_COLORS = List.of(
            "#3b82f6", // Blue
            "#10b981", // Emerald
            "#a855f7", // Purple
            "#ec4899", // Pink
            "#14b8a6", // Teal
            "#6366f1", // Indigo
            "#84cc16", // Lime
            "#06b6d4"  // Cyan
    );

    private String getNextAvailableColor(List<String> usedColors) {
        List<String> lowercaseUsed = usedColors.stream()
                .filter(java.util.Objects::nonNull)
                .map(String::toLowerCase)
                .toList();
        for (String preset : PRESET_COLORS) {
            if (!lowercaseUsed.contains(preset.toLowerCase())) {
                return preset;
            }
        }
        return PRESET_COLORS.get(0); // Fallback
    }

    // ======================== LOOKUP TABLES ========================

    @Transactional
    public LevelResponse upsertLevel(String manifestId, UpsertLookupRequest req) {
        Manifest manifest = requireManifest(manifestId);
        LevelId id = new LevelId(req.id(), manifestId);
        Level level = levelRepository.findById(id)
                .map(existing -> {
                    existing.setDescription(req.description());
                    if (req.color() != null && !req.color().isBlank()) {
                        existing.setColor(req.color());
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    String color = req.color();
                    if (color == null || color.isBlank()) {
                        List<String> used = levelRepository.findByManifestId(manifestId)
                                .stream().map(Level::getColor).toList();
                        color = getNextAvailableColor(used);
                    }
                    return Level.builder().id(req.id()).manifest(manifest).description(req.description()).color(color).build();
                });
        return LevelResponse.from(levelRepository.save(level));
    }

    @Transactional(readOnly = true)
    public List<LevelResponse> listLevels(String manifestId) {
        requireManifest(manifestId);
        return levelRepository.findByManifestId(manifestId).stream().map(LevelResponse::from).toList();
    }

    @Transactional
    public SectionResponse upsertSection(String manifestId, UpsertSectionRequest req) {
        Manifest manifest = requireManifest(manifestId);
        Section section = sectionRepository.findById(req.id())
                .map(existing -> {
                    existing.setType(req.type());
                    existing.setName(req.name());
                    existing.setColor(req.color());
                    existing.setLevelId(req.levelId());
                    existing.setCapacity(req.capacity());
                    existing.setUiData(req.uiData());
                    return existing;
                })
                .orElseGet(() -> Section.builder()
                        .id(req.id())
                        .manifest(manifest)
                        .type(req.type())
                        .name(req.name())
                        .color(req.color())
                        .levelId(req.levelId())
                        .capacity(req.capacity())
                        .uiData(req.uiData())
                        .build());
        return SectionResponse.from(sectionRepository.save(section));
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> listSections(String manifestId) {
        requireManifest(manifestId);
        return sectionRepository.findByManifestId(manifestId).stream().map(SectionResponse::from).toList();
    }

    @Transactional
    public PriceLevelResponse upsertPriceLevel(String manifestId, UpsertLookupRequest req) {
        Manifest manifest = requireManifest(manifestId);
        PriceLevelId id = new PriceLevelId(req.id(), manifestId);
        PriceLevel pl = priceLevelRepository.findById(id)
                .map(existing -> {
                    existing.setDescription(req.description());
                    if (req.color() != null && !req.color().isBlank()) {
                        existing.setColor(req.color());
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    String color = req.color();
                    if (color == null || color.isBlank()) {
                        List<String> used = priceLevelRepository.findByManifestId(manifestId)
                                .stream().map(PriceLevel::getColor).toList();
                        color = getNextAvailableColor(used);
                    }
                    return PriceLevel.builder().id(req.id()).manifest(manifest).description(req.description()).color(color).build();
                });
        return PriceLevelResponse.from(priceLevelRepository.save(pl));
    }

    @Transactional(readOnly = true)
    public List<PriceLevelResponse> listPriceLevels(String manifestId) {
        requireManifest(manifestId);
        return priceLevelRepository.findByManifestId(manifestId).stream().map(PriceLevelResponse::from).toList();
    }



    // ======================== SEAT ROWS & SEATS ========================

    @Transactional
    public SeatRowResponse createSeatRow(String sectionId, CreateSeatRowRequest req) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> VenueException.sectionNotFound(sectionId));
        
        SeatRow row = seatRowRepository.findById(req.id())
                .map(existing -> {
                    if (!existing.getName().equals(req.name()) && seatRowRepository.existsBySectionIdAndName(sectionId, req.name())) {
                        throw VenueException.seatRowNameDuplicate(req.name());
                    }
                    existing.setSection(section);
                    existing.setName(req.name());
                    
                    return existing;
                })
                .orElseGet(() -> {
                    if (seatRowRepository.existsBySectionIdAndName(sectionId, req.name())) {
                        throw VenueException.seatRowNameDuplicate(req.name());
                    }
                    return SeatRow.builder()
                            .id(req.id())
                            .section(section)
                            .name(req.name())
                            
                            .build();
                });
        return SeatRowResponse.from(seatRowRepository.save(row));
    }

    @Transactional(readOnly = true)
    public List<SeatRowResponse> listSeatRows(String sectionId) {
        return seatRowRepository.findBySectionId(sectionId).stream().map(SeatRowResponse::from).toList();
    }

    @Transactional
    public SeatResponse createSeat(String rowId, CreateSeatRequest req) {
        SeatRow row = seatRowRepository.findById(rowId)
                .orElseThrow(() -> VenueException.seatRowNotFound(rowId));
        
        Integer posY = req.positionY() != null ? req.positionY() : 0;
        
        Seat seat = seatRepository.findById(req.id())
                .map(existing -> {
                    if (!existing.getName().equals(req.name()) && seatRepository.existsBySeatRowIdAndName(rowId, req.name())) {
                        throw VenueException.seatNameDuplicate(req.name());
                    }
                    existing.setSeatRow(row);
                    existing.setName(req.name());
                    existing.setPositionX(req.positionX());
                    existing.setPositionY(posY);
                    existing.setPriceLevelId(req.priceLevelId());
                    existing.setSectionId(req.sectionId());
                    if (req.status() != null) {
                        existing.setStatus(req.status());
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    if (seatRepository.existsBySeatRowIdAndName(rowId, req.name())) {
                        throw VenueException.seatNameDuplicate(req.name());
                    }
                    return Seat.builder()
                            .id(req.id())
                            .seatRow(row)
                            .name(req.name())
                            .positionX(req.positionX())
                            .status(req.status() != null ? req.status() : SeatStatus.AVAILABLE)
                            .priceLevelId(req.priceLevelId())
                            .sectionId(req.sectionId())
                            .build();
                });
        return SeatResponse.from(seatRepository.save(seat));
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> listSeats(String rowId) {
        return seatRepository.findBySeatRowId(rowId).stream().map(SeatResponse::from).toList();
    }

    // ======================== HELPERS ========================

    private Venue requireVenue(UUID id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> VenueException.notFound(id));
    }

    private Manifest requireManifest(String id) {
        return manifestRepository.findById(id)
                .orElseThrow(() -> VenueException.manifestNotFound(id));
    }
}
