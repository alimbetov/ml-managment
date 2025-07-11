package kz.moon.app.seclevel.repository;


import kz.moon.app.seclevel.model.ImageAnnotation;
import kz.moon.app.seclevel.model.ImageData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageAnnotationRepository extends JpaRepository<ImageAnnotation, Long> {

    // Пагинация всех аннотаций
    Slice<ImageAnnotation> findAllBy(Pageable pageable);

    // Фильтрация по имени категории (аннотации определенной категории)
    Slice<ImageAnnotation> findByCategory_NameContainingIgnoreCase(String categoryName, Pageable pageable);

    // Подсчет всех записей
    long count();

    // Подсчет с фильтром по имени категории
    long countByCategory_NameContainingIgnoreCase(String categoryName);

    // Удаление всех аннотаций, привязанных к изображению
    void deleteByImage(ImageData image);
}
