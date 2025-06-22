package kz.moon.app.seclevel.repository;


import kz.moon.app.seclevel.model.ClassifierCategory;
import kz.moon.app.seclevel.model.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassifierCategoryRepository extends JpaRepository<ClassifierCategory, Long> {

    // Ленивая загрузка с пагинацией
    Slice<ClassifierCategory> findAllBy(Pageable pageable);

    // Фильтрация по имени категории
    Slice<ClassifierCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Подсчет всех записей
    long count();

    // Подсчет с фильтром по имени
    long countByNameContainingIgnoreCase(String name);

    @Query("""
    SELECT cc 
    FROM ClassifierCategory cc 
    JOIN FETCH cc.classifier c 
    JOIN FETCH c.project p
    WHERE p IN :projects
    """)
    List<ClassifierCategory> findAllByProjectIn(@Param("projects") List<Project> projects);

}

