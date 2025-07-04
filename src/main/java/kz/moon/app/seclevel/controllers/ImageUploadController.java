package kz.moon.app.seclevel.controllers;


import kz.moon.app.seclevel.model.ImageAnnotation;
import kz.moon.app.seclevel.model.ImageData;

import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.repository.ImageAnnotationRepository;
import kz.moon.app.seclevel.repository.ImageRepository;
import kz.moon.app.seclevel.repository.ImageStatus;

import kz.moon.app.seclevel.services.MinioService;
import kz.moon.app.seclevel.services.ProjectService;
import kz.moon.app.seclevel.utils.FileHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
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


    @PostMapping("/upload")
    public ImageData uploadImage(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "annotation", required = false) MultipartFile annotationJson,
                                 @RequestParam("fileStatus") String status,
                                 @RequestParam("projectId") Long projectId) throws Exception {

        var fileStatus = ImageStatus.fromStringSafe(status).orElse(ImageStatus.UPLOADED);
        // 1. Вычисляем hash
        String fileHash;
        try (InputStream stream = file.getInputStream()) {
            fileHash = FileHashUtil.calculateSHA256(stream);
        }
        // 2. Проверка на дубликаты
        Optional<ImageData> existingImage = imageDataRepository.findByFileHash(fileHash);
        if (existingImage.isPresent()) {
            return existingImage.get();
        }
        // 3. Генерация имени файла
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String generatedFilename = "img_" + UUID.randomUUID() + extension;
        // 4. Загрузка в MinIO
        try (InputStream uploadStream = file.getInputStream()) {
            minioService.uploadFile(generatedFilename, uploadStream, file.getContentType(), file.getSize());
        }
        // 5. Получение проекта
        Project project = projectService.getProject(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project with id " + projectId + " not found"));
        // 6. Создание ImageData
        ImageData image = ImageData.builder()
                .filename(generatedFilename)
                .fileHash(fileHash)
                .uploadDate(Instant.now())
                .status(fileStatus)
                .project(project)
                .build();

        image = imageDataRepository.save(image);

        // 7. Обработка аннотации (если передана)
        if (annotationJson != null && !annotationJson.isEmpty()) {
            String annotationContent = new String(annotationJson.getBytes());
            ImageAnnotation annotation = ImageAnnotation.builder()
                    .image(image)
                    .annotationJson(annotationContent)
                    .validated(false)
                    .createdAt(Instant.now())
                    .build();
            imageAnnotationRepository.save(annotation); // нужно внедрить imageAnnotationRepository
        }
        return image;
    }

}
