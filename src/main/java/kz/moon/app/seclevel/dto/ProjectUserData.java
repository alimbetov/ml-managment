package kz.moon.app.seclevel.dto;

import kz.moon.app.seclevel.repository.ImageStatus;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ProjectUserData {
    List<ProjectDto> projectDtoList = new ArrayList<>();
    List<ClassifierCategoryDto> categoryDtoList = new ArrayList<>();
    List<String> exportFilesList =ImageStatus.allNamesList();
}
