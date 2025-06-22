package kz.moon.app.seclevel.repository;

import kz.moon.app.seclevel.domain.User;
import kz.moon.app.seclevel.model.ClassifierCategory;
import kz.moon.app.seclevel.model.Image;
import kz.moon.app.seclevel.model.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Slice<Image> findAllBy(Pageable pageable);

    long count();

    @Query("""
SELECT i FROM Image i
WHERE (:projectFilter IS NULL OR i.project = :projectFilter)
  AND (:statusFilter IS NULL OR i.status = :statusFilter)
  AND (:authorFilter IS NULL OR i.uploadedBy = :authorFilter)
  AND (:parentImageFilter IS NULL OR i.parentImage = :parentImageFilter)
  AND (:classifierCategoryFilter IS NULL OR i.classifierCategory = :classifierCategoryFilter)
  AND (i.uploadDate >= :uploadDateFilterStart AND i.uploadDate < :uploadDateFilterEnd)
""")
    Slice<Image> findAllWithFilters(@Param("projectFilter") Project projectFilter,
                                    @Param("statusFilter") ImageStatus statusFilter,
                                    @Param("authorFilter") User authorFilter,
                                    @Param("parentImageFilter") Image parentImageFilter,
                                    @Param("classifierCategoryFilter") ClassifierCategory classifierCategoryFilter,
                                    @Param("uploadDateFilterStart") Instant uploadDateFilterStart,
                                    @Param("uploadDateFilterEnd") Instant uploadDateFilterEnd,
                                    Pageable pageable);


    @Query("""
SELECT COUNT(i) FROM Image i
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
                             @Param("parentImageFilter") Image parentImageFilter,
                             @Param("classifierCategoryFilter") ClassifierCategory classifierCategoryFilter,
                             @Param("uploadDateFilterStart") Instant uploadDateFilterStart,
                             @Param("uploadDateFilterEnd") Instant uploadDateFilterEnd);

    @Query("SELECT i FROM Image i WHERE i.project IN :projects AND i.parentImage IS NULL")
    List<Image> findImagesWithoutParentByProjects(@Param("projects") List<Project> projects);



    }

