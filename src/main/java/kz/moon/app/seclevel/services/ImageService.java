package kz.moon.app.seclevel.services;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import kz.moon.app.seclevel.domain.User;
import kz.moon.app.seclevel.model.ClassifierCategory;
import kz.moon.app.seclevel.model.ImageData;
import kz.moon.app.seclevel.repository.ClassifierCategoryRepository;
import kz.moon.app.seclevel.repository.ImageStatus;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.repository.ImageRepository;
import kz.moon.app.seclevel.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final ProjectRepository projectRepository;
    private final MyUserDetailsService userDetailsService;
    private final ClassifierCategoryRepository classifierCategoryRepository;

    public ImageService(ImageRepository imageRepository,
                        ProjectRepository projectRepository,
                        ClassifierCategoryRepository classifierCategoryRepository,
                        MyUserDetailsService userDetailsService) {
        this.imageRepository = imageRepository;
        this.projectRepository = projectRepository;
        this.classifierCategoryRepository = classifierCategoryRepository;
        this.userDetailsService = userDetailsService;
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
        imageRepository.deleteById(imageId);
    }

    public List<ImageData> findAllImages() {
        return imageRepository.findAll();
    }

    public void saveUploadedFile(@NotNull String filename,
                                 @NotNull Long projectId,
                                 @NotNull InputStream inputStream) {
        saveUploadedFile(filename, projectId, inputStream, null, null, null);
    }

    public void saveUploadedFile(@NotNull String filename,
                                 @NotNull Long projectId,
                                 @NotNull InputStream inputStream,
                                 @Nullable Long parentImageId,
                                 @Nullable String parentFilename,
                                 @Nullable Long classifierCategoryId) {

        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        var createdBy = userDetailsService.getCurrentUser()
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        ImageData.ImageDataBuilder builder = ImageData.builder()
                .filename(filename)
                .fileHash(Integer.toHexString(filename.hashCode()))
                .project(project)
                .uploadDate(Instant.now())
                .status(ImageStatus.UPLOADED)
                .uploadedBy(createdBy);

        if (parentImageId != null) {
            imageRepository.findById(parentImageId).ifPresent(builder::parentImage);
        }
        if (parentFilename != null && !parentFilename.isBlank()) {
            builder.parentFilename(parentFilename);
        }

        if (classifierCategoryId != null) {
            builder.classifierCategory(
                    classifierCategoryRepository.findById(classifierCategoryId)
                            .orElseThrow(() -> new IllegalArgumentException("ClassifierCategory not found"))
            );
        }

        ImageData imageData = builder.build();
        updateImage(imageData);
    }

    public List<ImageData> getParentImages(List<Project> projects){
       return imageRepository.findImagesWithoutParentByProjects(projects);
    }



}
