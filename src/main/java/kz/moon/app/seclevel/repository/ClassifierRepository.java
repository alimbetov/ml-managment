package kz.moon.app.seclevel.repository;


import kz.moon.app.seclevel.model.Classifier;
import kz.moon.app.seclevel.model.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassifierRepository extends JpaRepository<Classifier, Long> {

    // Ленивая загрузка с пагинацией
    Slice<Classifier> findAllBy(Pageable pageable);

    // Фильтрация по названию классификатора (пагинация)
    Slice<Classifier> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Подсчёт всех записей
    long count();

    // Подсчёт с фильтром по названию
    long countByNameContainingIgnoreCase(String name);
}
