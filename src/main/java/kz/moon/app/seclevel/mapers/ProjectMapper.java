package kz.moon.app.seclevel.mapers;
import kz.moon.app.seclevel.dto.ProjectDto;
import kz.moon.app.seclevel.model.Project;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectMapper {

    public static ProjectDto toDto(Project project) {
        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .industry(project.getIndustry())
                .build();
    }
    public static List<ProjectDto> toDtoList(List<Project> projects) {
        return projects.stream()
                .map(ProjectMapper::toDto)
                .collect(Collectors.toList());
    }
}
