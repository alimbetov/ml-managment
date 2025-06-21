package kz.moon.app.seclevel.services;

import kz.moon.app.seclevel.model.ImageAnnotation;
import kz.moon.app.seclevel.model.Image;
import kz.moon.app.seclevel.model.ClassifierCategory;
import kz.moon.app.seclevel.repository.ImageAnnotationRepository;
import kz.moon.app.seclevel.repository.ImageRepository;
import kz.moon.app.seclevel.repository.ClassifierCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

@Service
public class ImageAnnotationService {

    private final ImageAnnotationRepository annotationRepository;
    private final ImageRepository imageRepository;
    private final ClassifierCategoryRepository categoryRepository;

    public ImageAnnotationService(ImageAnnotationRepository annotationRepository,
                                  ImageRepository imageRepository,
                                  ClassifierCategoryRepository categoryRepository) {
        this.annotationRepository = annotationRepository;
        this.imageRepository = imageRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ImageAnnotation> find(String categoryNameFilter, int offset, int limit, String sortBy, boolean asc) {
        Pageable pageable = PageRequest.of(offset / limit, limit,
                asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        if (categoryNameFilter == null || categoryNameFilter.isEmpty()) {
            return annotationRepository.findAllBy(pageable).getContent();
        } else {
            return annotationRepository.findByCategory_NameContainingIgnoreCase(categoryNameFilter, pageable).getContent();
        }
    }

    public long count(String categoryNameFilter) {
        if (categoryNameFilter == null || categoryNameFilter.isEmpty()) {
            return annotationRepository.count();
        } else {
            return annotationRepository.countByCategory_NameContainingIgnoreCase(categoryNameFilter);
        }
    }

    public ImageAnnotation createAnnotation(Long imageId, Long categoryId, String annotationJson, boolean validated) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
        ClassifierCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        ImageAnnotation annotation = ImageAnnotation.builder()
                .image(image)
                .category(category)
                .annotationJson(annotationJson)
                .validated(validated)
                .createdAt(Instant.now())
                .build();
        if (validated) {
            annotation.setValidatedAt(Instant.now());
        }
        return annotationRepository.save(annotation);
    }

    public ImageAnnotation updateAnnotation(ImageAnnotation annotation) {
        return annotationRepository.save(annotation);
    }

    public void deleteAnnotation(Long annotationId) {
        annotationRepository.deleteById(annotationId);
    }

    public List<ImageAnnotation> findAllAnnotations() {
        return annotationRepository.findAll();
    }
}
