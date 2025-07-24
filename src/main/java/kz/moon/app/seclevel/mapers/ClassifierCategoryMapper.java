package kz.moon.app.seclevel.mapers;
import kz.moon.app.seclevel.dto.ClassifierCategoryDto;
import kz.moon.app.seclevel.model.ClassifierCategory;

public class ClassifierCategoryMapper {
    public static ClassifierCategoryDto toDto(ClassifierCategory category) {
        return ClassifierCategoryDto.builder()
                .id(category.getId())
                .projectId(category.getClassifier().getProject().getId()) // важно!
                .name(category.getName())
                .instructions(category.getInstructions())
                .superCategory(category.getClassifier().getName())
                .description(category.getClassifier().getDescription())
                .build();
    }
}