package kz.moon.app.seclevel.repository;


import kz.moon.app.seclevel.model.Image;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

    // Пагинация всех изображений
    Slice<Image> findAllBy(Pageable pageable);

    // Фильтрация по имени файла
    Slice<Image> findByFilenameContainingIgnoreCase(String filename, Pageable pageable);

    // Подсчет всех записей
    long count();

    // Подсчет по фильтру имени файла
    long countByFilenameContainingIgnoreCase(String filename);
}
