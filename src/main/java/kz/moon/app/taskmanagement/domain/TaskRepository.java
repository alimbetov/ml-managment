package kz.moon.app.taskmanagement.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Ленивая загрузка с пагинацией (без фильтра)
    Slice<Task> findAllBy(Pageable pageable);

    // Ленивая загрузка с фильтром по description + пагинация
    Slice<Task> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    // Подсчёт всех
    long count();

    // Подсчёт по фильтру
    long countByDescriptionContainingIgnoreCase(String description);
}
