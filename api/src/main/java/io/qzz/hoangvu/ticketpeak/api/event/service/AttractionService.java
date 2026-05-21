package io.qzz.hoangvu.ticketpeak.api.event.service;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.dto.AttractionResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.CreateAttractionRequest;
import io.qzz.hoangvu.ticketpeak.api.event.model.Attraction;
import io.qzz.hoangvu.ticketpeak.api.event.repository.AttractionRepository;
import org.springframework.http.HttpStatus;
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
            throw new ApiException(HttpStatus.CONFLICT, "SLUG_ALREADY_EXISTS", "Attraction with slug '" + slug + "' already exists");
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
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ATTRACTION_NOT_FOUND", "Attraction not found"));
        return AttractionResponse.from(attraction);
    }

    @Transactional(readOnly = true)
    public List<AttractionResponse> listAttractions() {
        return attractionRepository.findAll().stream()
                .map(AttractionResponse::from)
                .toList();
    }

    @Transactional
    public void deleteAttraction(UUID id) {
        if (!attractionRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "ATTRACTION_NOT_FOUND", "Attraction not found");
        }
        attractionRepository.deleteById(id);
    }

    private String slugify(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }
}
