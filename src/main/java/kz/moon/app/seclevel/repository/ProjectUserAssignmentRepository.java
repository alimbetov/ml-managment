package kz.moon.app.seclevel.repository;


import kz.moon.app.seclevel.domain.User;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.model.ProjectUserAssignment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectUserAssignmentRepository extends JpaRepository<ProjectUserAssignment, Long> {

    // Пагинация всех назначений пользователей на проекты
    Slice<ProjectUserAssignment> findAllBy(Pageable pageable);

    // Фильтрация по имени пользователя (через связь User.username)
    Slice<ProjectUserAssignment> findByUser_UsernameContainingIgnoreCase(String username, Pageable pageable);

    // Подсчет всех записей
    long count();

    // Подсчет с фильтром по имени пользователя
    long countByUser_UsernameContainingIgnoreCase(String username);

    @Query("""
        select distinct pua.user
        from ProjectUserAssignment pua
        where pua.project in :projects
        """)
    List<User> findDistinctUsersByProjectIn(@Param("projects") List<Project> projects);

    @Query("""
        select distinct pua.project
        from ProjectUserAssignment pua
        where pua.project in :projects
        """)
    List<Project> findDistinctProjectsIn(@Param("projects") List<Project> projects);


}
