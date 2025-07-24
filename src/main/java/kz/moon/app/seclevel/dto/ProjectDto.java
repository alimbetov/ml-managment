package kz.moon.app.seclevel.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private String industry;
}
