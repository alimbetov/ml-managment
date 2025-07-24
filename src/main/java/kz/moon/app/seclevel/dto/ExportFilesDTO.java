package kz.moon.app.seclevel.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import kz.moon.app.seclevel.repository.ImageStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ExportFilesDTO {
    private Long id;
    private Long projectId;
    private String filename;
    private Instant uploadDate;
    private  String username;
    private ImageStatus status; // Статус: загружен, размечен, проверен
}
