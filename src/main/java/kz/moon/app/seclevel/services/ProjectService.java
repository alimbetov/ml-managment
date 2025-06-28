package kz.moon.app.seclevel.services;

import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final MyUserDetailsService userDetailsService;
    public ProjectService(ProjectRepository projectRepository,
                          MyUserDetailsService userDetailsService) {
        this.projectRepository = projectRepository;
        this.userDetailsService = userDetailsService;
    }

    // Поиск с пагинацией и опциональным фильтром по названию (для Grid)
    public List<Project> find(String nameFilter, int offset, int limit, String sortBy, boolean asc) {
        // Создание объекта Pageable с сортировкой
        Pageable pageable = PageRequest.of(offset / limit, limit,
                asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        // Выбор: либо все, либо по фильтру
        if (nameFilter == null || nameFilter.isEmpty()) {
            return projectRepository.findAllBy(pageable).getContent();
        } else {
            return projectRepository.findByNameContainingIgnoreCase(nameFilter, pageable).getContent();
        }
    }

    // Подсчет количества проектов (с фильтром или без)
    public long count(String nameFilter) {
        if (nameFilter == null || nameFilter.isEmpty()) {
            return projectRepository.count();
        } else {
            return projectRepository.countByNameContainingIgnoreCase(nameFilter);
        }
    }

    // Создание нового проекта
    public Project createProject(String name, String description, String industry) {
        var createdBy = userDetailsService.getCurrentUser()
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Project project = Project.builder()
                .name(name)
                .description(description)
                .industry(industry)
                .createdDate(LocalDate.now())
                .build();

                 project.setCreatedBy(createdBy);

        return projectRepository.save(project);
    }

    // Обновление существующего проекта
    public Project updateProject(Project project) {
        return projectRepository.save(project);
    }

    // Удаление проекта по идентификатору
    public void deleteProject(Long projectId) {
        projectRepository.deleteById(projectId);
    }

    // Получение всех проектов (для заполнения списков, например, в ComboBox)
    public List<Project> findAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> getProject(Long projectId) {
        return projectRepository.findById(projectId);
    }



}

