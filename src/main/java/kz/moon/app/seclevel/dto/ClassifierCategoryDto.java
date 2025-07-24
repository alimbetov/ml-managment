package kz.moon.app.seclevel.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassifierCategoryDto {
    private Long id;
    private Long projectId;
    private String name;
    private String instructions;
    private String superCategory;
    private String description;

}
