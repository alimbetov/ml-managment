package kz.moon.app.seclevel.repository;


import kz.moon.app.seclevel.model.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Ленивая загрузка с пагинацией (без фильтра)
    Slice<Project> findAllBy(Pageable pageable);

    // Ленивая загрузка с фильтрацией по названию + пагинация
    Slice<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Подсчёт всех записей
    long count();

    // Подсчёт по фильтру (название содержит подстроку, без учета регистра)
    long countByNameContainingIgnoreCase(String name);
}
