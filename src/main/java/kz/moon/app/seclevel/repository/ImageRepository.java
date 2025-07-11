package kz.moon.app.seclevel.repository;

import kz.moon.app.seclevel.domain.User;
import kz.moon.app.seclevel.model.ClassifierCategory;
import kz.moon.app.seclevel.model.ImageData;
import kz.moon.app.seclevel.model.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageData, Long> {

    Slice<ImageData> findAllBy(Pageable pageable);

    long count();

    @Query("""
SELECT i FROM ImageData i
WHERE (:projectFilter IS NULL OR i.project = :projectFilter)
  AND (:statusFilter IS NULL OR i.status = :statusFilter)
  AND (:authorFilter IS NULL OR i.uploadedBy = :authorFilter)
  AND (:parentImageFilter IS NULL OR i.parentImage = :parentImageFilter)
  AND (:classifierCategoryFilter IS NULL OR i.classifierCategory = :classifierCategoryFilter)
  AND (i.uploadDate >= :uploadDateFilterStart AND i.uploadDate < :uploadDateFilterEnd)
""")
    Slice<ImageData> findAllWithFilters(@Param("projectFilter") Project projectFilter,
                                        @Param("statusFilter") ImageStatus statusFilter,
                                        @Param("authorFilter") User authorFilter,
                                        @Param("parentImageFilter") ImageData parentImageFilter,
                                        @Param("classifierCategoryFilter") ClassifierCategory classifierCategoryFilter,
                                        @Param("uploadDateFilterStart") Instant uploadDateFilterStart,
                                        @Param("uploadDateFilterEnd") Instant uploadDateFilterEnd,
                                        Pageable pageable);


    @Query("""
SELECT COUNT(i) FROM ImageData i
WHERE (:projectFilter IS NULL OR i.project = :projectFilter)
  AND (:statusFilter IS NULL OR i.status = :statusFilter)
  AND (:authorFilter IS NULL OR i.uploadedBy = :authorFilter)
  AND (:parentImageFilter IS NULL OR i.parentImage = :parentImageFilter)
  AND (:classifierCategoryFilter IS NULL OR i.classifierCategory = :classifierCategoryFilter)
  AND (i.uploadDate >= :uploadDateFilterStart AND i.uploadDate < :uploadDateFilterEnd)
""")
    long countAllWithFilters(@Param("projectFilter") Project projectFilter,
                             @Param("statusFilter") ImageStatus statusFilter,
                             @Param("authorFilter") User authorFilter,
                             @Param("parentImageFilter") ImageData parentImageFilter,
                             @Param("classifierCategoryFilter") ClassifierCategory classifierCategoryFilter,
                             @Param("uploadDateFilterStart") Instant uploadDateFilterStart,
                             @Param("uploadDateFilterEnd") Instant uploadDateFilterEnd);

    @Query("SELECT i FROM ImageData i WHERE i.project IN :projects AND i.parentImage IS NULL")
    List<ImageData> findImagesWithoutParentByProjects(@Param("projects") List<Project> projects);


    Optional<ImageData> findTop1ByProject_IdAndFileHashOrderByUploadDateDesc(Long projectId, String fileHash);
    }

