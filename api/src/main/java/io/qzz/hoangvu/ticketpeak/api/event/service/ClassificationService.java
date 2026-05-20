package io.qzz.hoangvu.ticketpeak.api.event.service;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.dto.ClassificationResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.CreateClassificationRequest;
import io.qzz.hoangvu.ticketpeak.api.event.model.Classification;
import io.qzz.hoangvu.ticketpeak.api.event.repository.ClassificationRepository;
import org.springframework.http.HttpStatus;
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
            throw new ApiException(HttpStatus.CONFLICT, "SLUG_ALREADY_EXISTS", "Classification with slug '" + slug + "' already exists");
        }

        if (req.parentId() != null) {
            Classification parent = classificationRepository.findById(req.parentId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PARENT_NOT_FOUND", "Parent classification not found"));
            if (req.level() <= parent.getLevel()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_LEVEL", "Child level must be greater than parent level");
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

    @Transactional(readOnly = true)
    public ClassificationResponse getClassification(UUID id) {
        Classification classification = classificationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CLASSIFICATION_NOT_FOUND", "Classification not found"));
        return ClassificationResponse.from(classification);
    }

    @Transactional(readOnly = true)
    public List<ClassificationResponse> listClassifications() {
        return classificationRepository.findAll().stream()
                .map(ClassificationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteClassification(UUID id) {
        if (!classificationRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "CLASSIFICATION_NOT_FOUND", "Classification not found");
        }
        classificationRepository.deleteById(id);
    }

    private String slugify(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }
}
