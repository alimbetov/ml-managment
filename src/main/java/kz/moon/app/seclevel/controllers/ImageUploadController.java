package kz.moon.app.seclevel.controllers;


import kz.moon.app.seclevel.dto.ClassifierCategoryDto;
import kz.moon.app.seclevel.dto.ExportFilesDTO;
import kz.moon.app.seclevel.model.ImageAnnotation;
import kz.moon.app.seclevel.model.ImageData;

import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.repository.ImageAnnotationRepository;
import kz.moon.app.seclevel.repository.ImageRepository;
import kz.moon.app.seclevel.repository.ImageStatus;

import kz.moon.app.seclevel.services.*;
import kz.moon.app.seclevel.utils.FileHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final MinioService minioService;

    private final ProjectService projectService;
    private final ImageRepository imageDataRepository;

    private final ImageAnnotationRepository imageAnnotationRepository;

    private ImageService imageService;

    @GetMapping("/{filename}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String filename) {
        try {
            InputStream inputStream = minioService.getFile(filename);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(null);
        }
    }
    @PostMapping("/upload")
    public ImageData uploadImage(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "annotation", required = false) String annotationJson,
                                 @RequestParam("fileStatus") String status,
                                 @RequestParam("projectId") Long projectId) throws Exception {

        // 1. Вычисляем хеш
        String fileHash;
        try (InputStream stream = file.getInputStream()) {
            fileHash = FileHashUtil.calculateSHA256(stream);
        }

        // 2. Получение проекта
        Project project = projectService.getProject(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project with id " + projectId + " not found"));

        // 3. Проверка на дубликаты
        Optional<ImageData> existingImage = imageDataRepository
                .findTop1ByProject_IdAndFileHashOrderByUploadDateDesc(projectId, fileHash);
        if (existingImage.isPresent()) {
            return existingImage.get();
        }
        // 4. Генерация имени файла
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String generatedFilename = "img_" + UUID.randomUUID() + extension;
        // 5. Статус
        ImageStatus fileStatus = ImageStatus.fromStringSafe(status).orElse(ImageStatus.UPLOADED);

        // 6. Создание ImageData
        ImageData image = ImageData.builder()
                .filename(generatedFilename)
                .fileHash(fileHash)
                .uploadDate(Instant.now())
                .status(fileStatus)
                .project(project)
                .build();

        image = imageDataRepository.save(image);

        // 7. Загрузка в MinIO
        try (InputStream uploadStream = file.getInputStream()) {
            minioService.uploadFile(generatedFilename, uploadStream, file.getContentType(), file.getSize());
        }

        // 8. Обработка аннотации (если передана)
        if (annotationJson != null && !annotationJson.isBlank()) {
            ImageAnnotation annotation = ImageAnnotation.builder()
                    .image(image)
                    .annotationJson(annotationJson)
                    .validated(false)
                    .createdAt(Instant.now())
                    .build();
            imageAnnotationRepository.save(annotation);
        }

        return image;
    }


    @GetMapping("/projects/{projectId}/images")
    public Page<ExportFilesDTO> getImagesByProjectPaged(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "UPLOADED") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        Project project = projectService.getProject(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        ImageStatus imageStatus = ImageStatus.fromStringSafe(status)
                .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + status));

        return imageService.getImagesByProjectPaged(project, imageStatus, page, size);
    }



}
