package kz.moon.app.seclevel.services;

import kz.moon.app.seclevel.model.Classifier;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.repository.ClassifierRepository;
import kz.moon.app.seclevel.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
public class ClassifierService {

    private final ClassifierRepository classifierRepository;
    private final ProjectRepository projectRepository;

    private final MyUserDetailsService myUserDetailsService;

    public ClassifierService(ClassifierRepository classifierRepository,
                             ProjectRepository projectRepository,
                             MyUserDetailsService myUserDetailsService) {
        this.classifierRepository = classifierRepository;
        this.projectRepository = projectRepository;
        this.myUserDetailsService = myUserDetailsService;

    }

    public List<Classifier> find(String nameFilter, int offset, int limit, String sortBy, boolean asc) {
        Pageable pageable = PageRequest.of(offset / limit, limit,
                asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        if (nameFilter == null || nameFilter.isEmpty()) {
            return classifierRepository.findAllBy(pageable).getContent();
        } else {
            return classifierRepository.findByNameContainingIgnoreCase(nameFilter, pageable).getContent();
        }
    }

    public long count(String nameFilter) {
        if (nameFilter == null || nameFilter.isEmpty()) {
            return classifierRepository.count();
        } else {
            return classifierRepository.countByNameContainingIgnoreCase(nameFilter);
        }
    }

    public Classifier createClassifier(String name, String description, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        var createdBy = myUserDetailsService.getCurrentUser()
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Classifier classifier = Classifier.builder()
                .name(name)
                .description(description)
                .project(project)
                .build();
        classifier.setCreatedBy(createdBy);
        return classifierRepository.save(classifier);
    }

    public Classifier updateClassifier(Classifier classifier) {
        return classifierRepository.save(classifier);
    }

    public void deleteClassifier(Long classifierId) {
        classifierRepository.deleteById(classifierId);
    }

    public List<Classifier> findAllClassifiers() {
        return classifierRepository.findAll();
    }
}
