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
    private final GAAreaRepository gaAreaRepository;
    private final RSAreaRepository rsAreaRepository;
    private final SeatRowRepository seatRowRepository;
    private final SeatRepository seatRepository;

    public VenueService(VenueRepository venueRepository, ManifestRepository manifestRepository,
                        LevelRepository levelRepository, SectionRepository sectionRepository,
                        PriceLevelRepository priceLevelRepository, GAAreaRepository gaAreaRepository,
                        RSAreaRepository rsAreaRepository, SeatRowRepository seatRowRepository,
                        SeatRepository seatRepository) {
        this.venueRepository = venueRepository;
        this.manifestRepository = manifestRepository;
        this.levelRepository = levelRepository;
        this.sectionRepository = sectionRepository;
        this.priceLevelRepository = priceLevelRepository;
        this.gaAreaRepository = gaAreaRepository;
        this.rsAreaRepository = rsAreaRepository;
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
                .build();
        manifestRepository.save(clone);

        // Clone lookup tables in batch
        List<Level> levels = levelRepository.findByManifestId(sourceManifestId).stream()
                .map(l -> Level.builder().id(l.getId()).manifest(clone).description(l.getDescription()).color(l.getColor()).build())
                .toList();
        levelRepository.saveAll(levels);

        List<Section> sections = sectionRepository.findByManifestId(sourceManifestId).stream()
                .map(s -> Section.builder().id(s.getId()).manifest(clone).description(s.getDescription()).color(s.getColor()).build())
                .toList();
        sectionRepository.saveAll(sections);

        List<PriceLevel> priceLevels = priceLevelRepository.findByManifestId(sourceManifestId).stream()
                .map(p -> PriceLevel.builder().id(p.getId()).manifest(clone).description(p.getDescription()).color(p.getColor()).build())
                .toList();
        priceLevelRepository.saveAll(priceLevels);

        // Clone areas in batch
        List<GAArea> gaAreas = gaAreaRepository.findByManifestId(sourceManifestId).stream()
                .map(g -> GAArea.builder()
                        .id(newId + "-" + g.getId())
                        .manifestId(clone.getId())
                        .levelId(g.getLevelId())
                        .sectionId(g.getSectionId())
                        .priceLevelId(g.getPriceLevelId())
                        .capacity(g.getCapacity())
                        .build())
                .toList();
        gaAreaRepository.saveAll(gaAreas);

        List<RSArea> rsAreas = rsAreaRepository.findByManifestId(sourceManifestId).stream()
                .map(r -> RSArea.builder()
                        .id(newId + "-" + r.getId())
                        .manifestId(clone.getId())
                        .levelId(r.getLevelId())
                        .sectionId(r.getSectionId())
                        .priceLevelId(r.getPriceLevelId())
                        .build())
                .toList();
        rsAreas = rsAreaRepository.saveAll(rsAreas);

        // Clone rows and seats in batch
        java.util.Map<String, RSArea> rsAreaMap = rsAreas.stream()
                .collect(java.util.stream.Collectors.toMap(RSArea::getId, java.util.function.Function.identity()));

        List<SeatRow> seatRows = new java.util.ArrayList<>();
        List<Seat> seats = new java.util.ArrayList<>();

        List<RSArea> sourceRsAreas = rsAreaRepository.findByManifestId(sourceManifestId);
        for (RSArea r : sourceRsAreas) {
            String newAreaId = newId + "-" + r.getId();
            RSArea newArea = rsAreaMap.get(newAreaId);
            if (newArea == null) {
                continue;
            }

            List<SeatRow> rowsInArea = seatRowRepository.findByRsAreaId(r.getId());
            for (SeatRow row : rowsInArea) {
                String newRowId = newId + "-" + row.getId();
                SeatRow newRow = SeatRow.builder()
                        .id(newRowId)
                        .rsArea(newArea)
                        .name(row.getName())
                        .positionY(row.getPositionY())
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
                            .accessibility(seat.getAccessibility())
                            .obstructedView(seat.getObstructedView())
                            .aisle(seat.getAisle())
                            .build();
                    seats.add(newSeat);
                }
            }
        }

        seatRowRepository.saveAll(seatRows);
        seatRepository.saveAll(seats);

        return ManifestResponse.from(clone);
    }

    // ======================== LOOKUP TABLES ========================

    @Transactional
    public LevelResponse upsertLevel(String manifestId, UpsertLookupRequest req) {
        Manifest manifest = requireManifest(manifestId);
        Level level = Level.builder().id(req.id()).manifest(manifest).description(req.description()).color(req.color()).build();
        return LevelResponse.from(levelRepository.save(level));
    }

    @Transactional(readOnly = true)
    public List<LevelResponse> listLevels(String manifestId) {
        requireManifest(manifestId);
        return levelRepository.findByManifestId(manifestId).stream().map(LevelResponse::from).toList();
    }

    @Transactional
    public SectionResponse upsertSection(String manifestId, UpsertLookupRequest req) {
        Manifest manifest = requireManifest(manifestId);
        Section section = Section.builder().id(req.id()).manifest(manifest).description(req.description()).color(req.color()).build();
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
        PriceLevel pl = PriceLevel.builder().id(req.id()).manifest(manifest).description(req.description()).color(req.color()).build();
        return PriceLevelResponse.from(priceLevelRepository.save(pl));
    }

    @Transactional(readOnly = true)
    public List<PriceLevelResponse> listPriceLevels(String manifestId) {
        requireManifest(manifestId);
        return priceLevelRepository.findByManifestId(manifestId).stream().map(PriceLevelResponse::from).toList();
    }

    // ======================== AREAS ========================

    @Transactional
    public GAAreaResponse createGAArea(String manifestId, CreateGAAreaRequest req) {
        requireManifest(manifestId);
        if (gaAreaRepository.existsById(req.id())) {
            throw VenueException.gaAreaIdExists(req.id());
        }
        GAArea area = GAArea.builder().id(req.id()).manifestId(manifestId)
                .levelId(req.levelId()).sectionId(req.sectionId()).priceLevelId(req.priceLevelId())
                .capacity(req.capacity()).build();
        return GAAreaResponse.from(gaAreaRepository.save(area));
    }

    @Transactional(readOnly = true)
    public List<GAAreaResponse> listGAAreas(String manifestId) {
        requireManifest(manifestId);
        return gaAreaRepository.findByManifestId(manifestId).stream().map(GAAreaResponse::from).toList();
    }

    @Transactional
    public RSAreaResponse createRSArea(String manifestId, CreateRSAreaRequest req) {
        requireManifest(manifestId);
        if (rsAreaRepository.existsById(req.id())) {
            throw VenueException.rsAreaIdExists(req.id());
        }
        RSArea area = RSArea.builder().id(req.id()).manifestId(manifestId)
                .levelId(req.levelId()).sectionId(req.sectionId()).priceLevelId(req.priceLevelId()).build();
        return RSAreaResponse.from(rsAreaRepository.save(area));
    }

    @Transactional(readOnly = true)
    public List<RSAreaResponse> listRSAreas(String manifestId) {
        requireManifest(manifestId);
        return rsAreaRepository.findByManifestId(manifestId).stream().map(RSAreaResponse::from).toList();
    }

    // ======================== SEAT ROWS & SEATS ========================

    @Transactional
    public SeatRowResponse createSeatRow(String rsAreaId, CreateSeatRowRequest req) {
        RSArea rsArea = rsAreaRepository.findById(rsAreaId)
                .orElseThrow(() -> VenueException.rsAreaNotFound(rsAreaId));
        if (seatRowRepository.existsById(req.id())) {
            throw VenueException.seatRowIdExists(req.id());
        }
        if (seatRowRepository.existsByRsAreaIdAndName(rsAreaId, req.name())) {
            throw VenueException.seatRowNameDuplicate(req.name());
        }
        SeatRow row = SeatRow.builder().id(req.id()).rsArea(rsArea).name(req.name()).positionY(req.positionY()).build();
        return SeatRowResponse.from(seatRowRepository.save(row));
    }

    @Transactional(readOnly = true)
    public List<SeatRowResponse> listSeatRows(String rsAreaId) {
        return seatRowRepository.findByRsAreaId(rsAreaId).stream().map(SeatRowResponse::from).toList();
    }

    @Transactional
    public SeatResponse createSeat(String rowId, CreateSeatRequest req) {
        SeatRow row = seatRowRepository.findById(rowId)
                .orElseThrow(() -> VenueException.seatRowNotFound(rowId));
        if (seatRepository.existsById(req.id())) {
            throw VenueException.seatIdExists(req.id());
        }
        if (seatRepository.existsBySeatRowIdAndName(rowId, req.name())) {
            throw VenueException.seatNameDuplicate(req.name());
        }
        Seat seat = Seat.builder().id(req.id()).seatRow(row).name(req.name()).positionX(req.positionX())
                .status(SeatStatus.AVAILABLE).accessibility(req.accessibility())
                .obstructedView(req.obstructedView()).aisle(req.aisle()).build();
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
