package kz.moon.app.seclevel.repository;


import kz.moon.app.seclevel.model.ClassifierCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassifierCategoryRepository extends JpaRepository<ClassifierCategory, Long> {

    // Ленивая загрузка с пагинацией
    Slice<ClassifierCategory> findAllBy(Pageable pageable);

    // Фильтрация по имени категории
    Slice<ClassifierCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Подсчет всех записей
    long count();

    // Подсчет с фильтром по имени
    long countByNameContainingIgnoreCase(String name);
}

