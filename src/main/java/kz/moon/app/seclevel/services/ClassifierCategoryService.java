package kz.moon.app.seclevel.services;

import kz.moon.app.seclevel.model.ClassifierCategory;
import kz.moon.app.seclevel.model.Classifier;
import kz.moon.app.seclevel.repository.ClassifierCategoryRepository;
import kz.moon.app.seclevel.repository.ClassifierRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
public class ClassifierCategoryService {

    private final ClassifierCategoryRepository categoryRepository;
    private final ClassifierRepository classifierRepository;

    public ClassifierCategoryService(ClassifierCategoryRepository categoryRepository,
                                     ClassifierRepository classifierRepository) {
        this.categoryRepository = categoryRepository;
        this.classifierRepository = classifierRepository;
    }

    public List<ClassifierCategory> find(String nameFilter, int offset, int limit, String sortBy, boolean asc) {
        Pageable pageable = PageRequest.of(offset / limit, limit,
                asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        if (nameFilter == null || nameFilter.isEmpty()) {
            return categoryRepository.findAllBy(pageable).getContent();
        } else {
            return categoryRepository.findByNameContainingIgnoreCase(nameFilter, pageable).getContent();
        }
    }

    public long count(String nameFilter) {
        if (nameFilter == null || nameFilter.isEmpty()) {
            return categoryRepository.count();
        } else {
            return categoryRepository.countByNameContainingIgnoreCase(nameFilter);
        }
    }

    public ClassifierCategory createCategory(String name, String instructions, Long classifierId) {
        Classifier classifier = classifierRepository.findById(classifierId)
                .orElseThrow(() -> new IllegalArgumentException("Classifier not found"));
        ClassifierCategory category = ClassifierCategory.builder()
                .name(name)
                .instructions(instructions)
                .classifier(classifier)
                .build();
        return categoryRepository.save(category);
    }

    public ClassifierCategory updateCategory(ClassifierCategory category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    public List<ClassifierCategory> findAllCategories() {
        return categoryRepository.findAll();
    }
}
