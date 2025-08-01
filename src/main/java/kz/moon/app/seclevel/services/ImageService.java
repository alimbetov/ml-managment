package kz.moon.app.seclevel.services;

import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import kz.moon.app.seclevel.domain.User;
import kz.moon.app.seclevel.dto.ExportFilesDTO;
import kz.moon.app.seclevel.mapers.ExportFilesMapper;
import kz.moon.app.seclevel.model.ClassifierCategory;
import kz.moon.app.seclevel.model.ImageAnnotation;
import kz.moon.app.seclevel.model.ImageData;
import kz.moon.app.seclevel.repository.*;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.utils.FileHashUtil;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;


import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageService {

    private final ProjectService projectService;

    private final MinioService minioService;
    private final ImageRepository imageRepository;
    private final ProjectRepository projectRepository;
    private final MyUserDetailsService userDetailsService;

    private final ImageAnnotationRepository annotationRepository;
    private final ClassifierCategoryRepository classifierCategoryRepository;

    public ImageService(ImageRepository imageRepository,
                        ProjectRepository projectRepository,
                        ClassifierCategoryRepository classifierCategoryRepository,
                        MyUserDetailsService userDetailsService,
                        ProjectService projectService,
                        ImageAnnotationRepository annotationRepository,
                        MinioService minioService) {
        this.imageRepository = imageRepository;
        this.projectRepository = projectRepository;
        this.classifierCategoryRepository = classifierCategoryRepository;
        this.userDetailsService = userDetailsService;
        this.projectService = projectService;
        this.annotationRepository = annotationRepository;
        this.minioService = minioService;
    }



    public InputStream getFile(String fileName) throws Exception {
        return minioService.getFile(fileName);
    }
    public StreamResource getFileAsStreamResource(String filename) {
        return new StreamResource(filename, () -> {
            try {
                return minioService.getFile(filename); // возвращает InputStream
            } catch (Exception e) {
                throw new RuntimeException("Could not load image: " + filename, e);
            }
        });
    }

    public List<ImageData> find(int offset, int limit, String sortBy, boolean asc) {
        Pageable pageable = PageRequest.of(offset / limit, limit,
                asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        return imageRepository.findAllBy(pageable).getContent();
    }

    public long count() {
        return imageRepository.count();
    }

    public List<ImageData> find(Project projectFilter,
                                ImageStatus statusFilter,
                                User authorFilter,
                                ImageData parentImageFilter,
                                ClassifierCategory classifierCategoryFilter,
                                LocalDate uploadDateFilter,
                                int offset, int limit, String sortBy, boolean asc) {

        Pageable pageable = PageRequest.of(offset / limit, limit,
                asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());

        Instant uploadDateFilterStart;
        Instant uploadDateFilterEnd;

        if (uploadDateFilter != null) {
            uploadDateFilterStart = uploadDateFilter.atStartOfDay(ZoneId.systemDefault()).toInstant();
            uploadDateFilterEnd = uploadDateFilter.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else {
            LocalDate today = LocalDate.now();
            uploadDateFilterStart = today.minusYears(50).atStartOfDay(ZoneId.systemDefault()).toInstant();
            uploadDateFilterEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        return imageRepository.findAllWithFilters(
                projectFilter,
                statusFilter,
                authorFilter,
                parentImageFilter,
                classifierCategoryFilter,
                uploadDateFilterStart,
                uploadDateFilterEnd,
                pageable
        ).getContent();
    }

    public long count(Project projectFilter,
                      ImageStatus statusFilter,
                      User authorFilter,
                      ImageData parentImageFilter,
                      ClassifierCategory classifierCategoryFilter,
                      LocalDate uploadDateFilter) {

        Instant uploadDateFilterStart;
        Instant uploadDateFilterEnd;

        if (uploadDateFilter != null) {
            uploadDateFilterStart = uploadDateFilter.atStartOfDay(ZoneId.systemDefault()).toInstant();
            uploadDateFilterEnd = uploadDateFilter.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else {
            LocalDate today = LocalDate.now();
            uploadDateFilterStart = today.minusYears(50).atStartOfDay(ZoneId.systemDefault()).toInstant();
            uploadDateFilterEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }


        return imageRepository.countAllWithFilters(
                projectFilter,
                statusFilter,
                authorFilter,
                parentImageFilter,
                classifierCategoryFilter,
                uploadDateFilterStart,
                uploadDateFilterEnd
        );
    }

    public ImageData updateImage(ImageData image) {
        return imageRepository.save(image);
    }




    public void deleteImage(Long imageId) {
        imageRepository.findById(imageId).ifPresent(image -> {
            // Удаляем из MinIO
            minioService.deleteFile(image.getFilename());

            // Удаляем аннотации (если есть)
            annotationRepository.deleteByImage(image);

            // Удаляем ImageData
            imageRepository.delete(image);
        });
    }

    public List<ImageData> findAllImages() {
        return imageRepository.findAll();
    }

    public void saveUploadedFile(@NotNull String filename,
                                 @NotNull Long projectId,
                                 @NotNull InputStream inputStream,
                                 @NotNull  String contentType,@NotNull  long size) {
        saveUploadedFile(filename, projectId, inputStream,contentType, size, null, null, null);
    }

    public void saveUploadedFile(@NotNull String originalFilename,
                                 @NotNull Long projectId,
                                 @NotNull InputStream inputStream,
                                 @NotNull  String contentType,
                                 @NotNull  long size,
                                 @Nullable Long parentImageId,
                                 @Nullable String parentFilename,
                                 @Nullable Long classifierCategoryId
                                ) {

        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        var createdBy = userDetailsService.getCurrentUser()
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String generatedFilename = "img_" + UUID.randomUUID() + extension;
        ImageData.ImageDataBuilder builder = ImageData.builder()
                .filename(generatedFilename)
                .fileHash(Integer.toHexString(generatedFilename.hashCode()))
                .project(project)
                .uploadDate(Instant.now())
                .status(ImageStatus.UPLOADED)
                .uploadedBy(createdBy);

        if (classifierCategoryId != null) {
            builder.classifierCategory(
                    classifierCategoryRepository.findById(classifierCategoryId)
                            .orElseThrow(() -> new IllegalArgumentException("ClassifierCategory not found"))
            );
        }

        if (parentImageId != null) {
            imageRepository.findById(parentImageId).ifPresent(builder::parentImage);
        }
        if (parentFilename != null && !parentFilename.isBlank()) {
            builder.parentFilename(parentFilename);
        }
        ImageData imageData = builder.build();
        imageData = updateImage(imageData);
        try {
            minioService.uploadFile(imageData.getFilename(),inputStream,contentType, size);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public List<ImageData> getParentImages(List<Project> projects){
       return imageRepository.findImagesWithoutParentByProjects(projects);
    }


    public ImageData saveUploadedFile(@NotNull Long projectId,
                                 @NotNull String vOriginalFilename,
                                 @NotNull InputStream inputStream,
                                 String contentType,
                                 Long  fileSize,
                                 String annotationJson,
                                 String status) throws Exception {

        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        var createdBy = userDetailsService.getCurrentUser()
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        var fileStatus = ImageStatus.fromStringSafe(status).orElse(ImageStatus.UPLOADED);
        // 1. Вычисляем hash
        String fileHash;
        try (InputStream stream = inputStream) {
            fileHash = FileHashUtil.calculateSHA256(stream);
        }
        // 2. Проверка на дубликаты
        Optional<ImageData> existingImage = imageRepository.findTop1ByProject_IdAndFileHashOrderByUploadDateDesc(projectId, fileHash);
        if (existingImage.isPresent()) {
            return existingImage.get();
        }
        // 3. Генерация имени файла
        String originalFilename = vOriginalFilename;
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String generatedFilename = "img_" + UUID.randomUUID() + extension;
        // 4. Загрузка в MinIO
        try (InputStream uploadStream = inputStream) {
            minioService.uploadFile(generatedFilename, uploadStream, contentType, fileSize);
        }

        // 6. Создание ImageData
        ImageData image = ImageData.builder()
                .filename(generatedFilename)
                .fileHash(fileHash)
                .uploadDate(Instant.now())
                .status(fileStatus)
                .uploadedBy(createdBy)
                .project(project)
                .build();

        image = imageRepository.save(image);

        // 7. Обработка аннотации (если передана)
        if (annotationJson != null && !annotationJson.isEmpty()) {
            String annotationContent = new String(annotationJson.getBytes());
            ImageAnnotation annotation = ImageAnnotation.builder()
                    .image(image)
                    .annotationJson(annotationContent)
                    .validated(false)
                    .createdAt(Instant.now())
                    .build();
            annotationRepository.save(annotation);
            // нужно внедрить imageAnnotationRepository
        }
        return image;
    }


    public Page<ExportFilesDTO> getImagesByProjectPaged(
            Project project,
            ImageStatus statusFilter,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadDate").descending());
        Page<ImageData> imagePage = imageRepository.findByProjectPaged(project, statusFilter, pageable);
        return imagePage.map(ExportFilesMapper::toDto);
    }

}
