package io.qzz.hoangvu.ticketpeak.api.event.service;

import io.qzz.hoangvu.ticketpeak.api.event.dto.ClassificationResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.CreateClassificationRequest;
import io.qzz.hoangvu.ticketpeak.api.event.exception.EventException;
import io.qzz.hoangvu.ticketpeak.api.event.model.Classification;
import io.qzz.hoangvu.ticketpeak.api.event.repository.ClassificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClassificationService {

    private final ClassificationRepository classificationRepository;

    public ClassificationService(ClassificationRepository classificationRepository) {
        this.classificationRepository = classificationRepository;
    }

    @Transactional
    public ClassificationResponse createClassification(CreateClassificationRequest req) {
        String slug = req.slug();
        if (slug == null || slug.isBlank()) {
            slug = slugify(req.name());
        }
        slug = slug.toLowerCase();

        if (classificationRepository.existsBySlug(slug)) {
            throw EventException.slugAlreadyExists("Classification with slug '" + slug + "' already exists");
        }

        if (req.parentId() != null) {
            Classification parent = classificationRepository.findById(req.parentId())
                    .orElseThrow(() -> EventException.parentNotFound());
            if (req.level() <= parent.getLevel()) {
                throw EventException.invalidLevel();
            }
        }

        Classification classification = Classification.builder()
                .name(req.name())
                .slug(slug)
                .level(req.level())
                .isActive(req.isActive())
                .parentId(req.parentId())
                .build();

        return ClassificationResponse.from(classificationRepository.save(classification));
    }

    @Transactional
    public ClassificationResponse updateClassification(UUID id, CreateClassificationRequest req) {
        Classification classification = classificationRepository.findById(id)
                .orElseThrow(() -> EventException.classificationNotFound());

        if (req.parentId() != null && req.parentId().equals(id)) {
            throw EventException.invalidParent();
        }

        String slug = req.slug();
        if (slug == null || slug.isBlank()) {
            slug = slugify(req.name());
        }
        slug = slug.toLowerCase();

        if (!classification.getSlug().equals(slug) && classificationRepository.existsBySlug(slug)) {
            throw EventException.slugAlreadyExists("Classification with slug '" + slug + "' already exists");
        }

        if (req.parentId() != null) {
            Classification parent = classificationRepository.findById(req.parentId())
                    .orElseThrow(() -> EventException.parentNotFound());
            if (req.level() <= parent.getLevel()) {
                throw EventException.invalidLevel();
            }
        }

        classification.setName(req.name());
        classification.setSlug(slug);
        classification.setLevel(req.level());
        classification.setIsActive(req.isActive());
        classification.setParentId(req.parentId());

        return ClassificationResponse.from(classificationRepository.save(classification));
    }


    @Transactional(readOnly = true)
    public ClassificationResponse getClassification(UUID id) {
        Classification classification = classificationRepository.findById(id)
                .orElseThrow(() -> EventException.classificationNotFound());
        return ClassificationResponse.from(classification);
    }

    @Transactional(readOnly = true)
    public List<ClassificationResponse> listClassifications() {
        return classificationRepository.findAll().stream()
                .map(ClassificationResponse::from)
                .toList();
    }

    private String slugify(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }
}
