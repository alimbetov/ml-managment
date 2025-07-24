package kz.moon.app.seclevel.controllers;

import jakarta.annotation.security.RolesAllowed;
import kz.moon.app.seclevel.dto.ClassifierCategoryDto;
import kz.moon.app.seclevel.dto.ProjectUserData;
import kz.moon.app.seclevel.mapers.ProjectMapper;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.services.ClassifierCategoryService;
import kz.moon.app.seclevel.services.ProjectUserAssignmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/classifier/")
@RolesAllowed("ROLE_MARKER")
public class ClassifierCategoryController {
    private final ProjectUserAssignmentService projectUserAssignmentService;
    private final ClassifierCategoryService classifierCategoryService;
    public ClassifierCategoryController(ProjectUserAssignmentService projectUserAssignmentService,
                                        ClassifierCategoryService classifierCategoryService) {
        this.projectUserAssignmentService = projectUserAssignmentService;
        this.classifierCategoryService = classifierCategoryService;
    }
    @GetMapping("categories")
    public ProjectUserData getMyProjectClassifierCategories() {
        List<Project> userProjects = projectUserAssignmentService.getAvailsableProjectsList();
        var list =  classifierCategoryService.getCategoryDtosByProjects(userProjects);
        return  ProjectUserData.builder().projectDtoList(ProjectMapper.toDtoList(userProjects)).categoryDtoList(list).build();
    }
}
