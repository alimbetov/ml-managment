package kz.moon.app.seclevel.services;


import kz.moon.app.seclevel.model.Image;
import kz.moon.app.seclevel.repository.ImageStatus;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.repository.ImageRepository;
import kz.moon.app.seclevel.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final ProjectRepository projectRepository;

    private final MyUserDetailsService userDetailsService;
    public ImageService(ImageRepository imageRepository,
                        ProjectRepository projectRepository,
                        MyUserDetailsService userDetailsService) {
        this.imageRepository = imageRepository;
        this.projectRepository = projectRepository;
        this.userDetailsService = userDetailsService;
    }

    public List<Image> find(String filenameFilter, int offset, int limit, String sortBy, boolean asc) {
        Pageable pageable = PageRequest.of(offset / limit, limit,
                asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        if (filenameFilter == null || filenameFilter.isEmpty()) {
            return imageRepository.findAllBy(pageable).getContent();
        } else {
            return imageRepository.findByFilenameContainingIgnoreCase(filenameFilter, pageable).getContent();
        }
    }

    public long count(String filenameFilter) {
        if (filenameFilter == null || filenameFilter.isEmpty()) {
            return imageRepository.count();
        } else {
            return imageRepository.countByFilenameContainingIgnoreCase(filenameFilter);
        }
    }

    public Image createImage(String filename, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        Image image = Image.builder()
                .filename(filename)
                .fileHash(Integer.toHexString(filename.hashCode())) // simplistic hash
                .project(project)
                .uploadDate(Instant.now())
                .status(ImageStatus.UPLOADED)
                .build();
        // Set uploadedBy to current user if available
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof kz.moon.app.seclevel.domain.User) {
            image.setUploadedBy((kz.moon.app.seclevel.domain.User) auth.getPrincipal());
        }
        return imageRepository.save(image);
    }

    public Image updateImage(Image image) {
        return imageRepository.save(image);
    }

    public void deleteImage(Long imageId) {
        imageRepository.deleteById(imageId);
    }

    public List<Image> findAllImages() {
        return imageRepository.findAll();
    }

    public void saveUploadedFile2(String filename, Project project, InputStream inputStream) {
        var image = new Image();
        image.setUploadDate(Instant.now());
        image.setFilename(filename);
        image.setProject(project);
        image.setStatus(ImageStatus.UPLOADED);
        updateImage(image);
    }

    public void saveUploadedFile(String filename, Long projectId, InputStream inputStream) {
        try {
            var project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));

            var createdBy = userDetailsService.getCurrentUser()
                    .orElseThrow(() -> new IllegalArgumentException("user not found"));


            Image image = Image.builder()
                    .filename(filename)
                    .fileHash(Integer.toHexString(filename.hashCode())) // simplistic hash
                    .project(project)
                    .uploadDate(Instant.now())
                    .status(ImageStatus.UPLOADED)
                    .uploadedBy(createdBy)
                    .build();
            updateImage(image);

            // Сохраняем Image
            //updateImage(image);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

}
