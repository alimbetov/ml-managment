package kz.moon.app.seclevel.mapers;

import kz.moon.app.seclevel.dto.ExportFilesDTO;
import kz.moon.app.seclevel.model.ImageData;

public class ExportFilesMapper {
    public static ExportFilesDTO toDto(ImageData image) {
        return ExportFilesDTO.builder()
                .id(image.getId())
                .projectId(image.getProject().getId())
                .filename(image.getFilename())
                .username(image.getUploadedBy().getUsername())
                .uploadDate(image.getUploadDate())
                .status(image.getStatus())
                .build();
    }
}
