package io.qzz.hoangvu.ticketpeak.api.event.service;

import io.qzz.hoangvu.ticketpeak.api.event.dto.AttractionResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.CreateAttractionRequest;
import io.qzz.hoangvu.ticketpeak.api.event.exception.EventException;
import io.qzz.hoangvu.ticketpeak.api.event.model.Attraction;
import io.qzz.hoangvu.ticketpeak.api.event.repository.AttractionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AttractionService {

    private final AttractionRepository attractionRepository;

    public AttractionService(AttractionRepository attractionRepository) {
        this.attractionRepository = attractionRepository;
    }

    @Transactional
    public AttractionResponse createAttraction(CreateAttractionRequest req) {
        String slug = req.slug();
        if (slug == null || slug.isBlank()) {
            slug = slugify(req.name());
        }
        slug = slug.toLowerCase();

        if (attractionRepository.existsBySlug(slug)) {
            throw EventException.slugAlreadyExists("Attraction with slug '" + slug + "' already exists");
        }

        Attraction attraction = Attraction.builder()
                .name(req.name())
                .slug(slug)
                .type(req.type())
                .imageUrl(req.imageUrl())
                .description(req.description())
                .build();

        return AttractionResponse.from(attractionRepository.save(attraction));
    }

    @Transactional(readOnly = true)
    public AttractionResponse getAttraction(UUID id) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> EventException.attractionNotFound());
        return AttractionResponse.from(attraction);
    }

    @Transactional(readOnly = true)
    public List<AttractionResponse> listAttractions() {
        return attractionRepository.findAll().stream()
                .map(AttractionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttractionResponse> searchOrListAttractions(String query) {
        if (query == null || query.isBlank()) {
            return listAttractions();
        }
        return attractionRepository.searchByNameOrDescription(query.trim()).stream()
                .map(AttractionResponse::from)
                .toList();
    }

    private String slugify(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }
}
