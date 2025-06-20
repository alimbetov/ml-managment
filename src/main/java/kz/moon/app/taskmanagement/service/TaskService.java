package kz.moon.app.taskmanagement.service;

import kz.moon.app.taskmanagement.domain.Task;
import kz.moon.app.taskmanagement.domain.TaskRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class TaskService {

    private final TaskRepository taskRepository;
    private final Clock clock;

    TaskService(TaskRepository taskRepository, Clock clock) {
        this.taskRepository = taskRepository;
        this.clock = clock;
    }

    public void createTask(String description, @Nullable LocalDate dueDate) {
        if ("fail".equals(description)) {
            throw new RuntimeException("This is for testing the error handler");
        }
        var task = new Task();
        task.setDescription(description);
        task.setCreationDate(clock.instant());
        task.setDueDate(dueDate);
        taskRepository.saveAndFlush(task);
    }

    public void updateTask(Task task) {
        taskRepository.saveAndFlush(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public long count(@Nullable String filter) {
        if (filter == null || filter.isBlank()) {
            return taskRepository.count();
        } else {
            return taskRepository.countByDescriptionContainingIgnoreCase(filter);
        }
    }

    public List<Task> find(@Nullable String filter, int offset, int limit, String sortBy, boolean asc) {
        Pageable pageable = PageRequest.of(offset / limit, limit,
                asc ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);

        if (filter == null || filter.isBlank()) {
            return taskRepository.findAll(pageable).getContent();
        } else {
            return taskRepository.findByDescriptionContainingIgnoreCase(filter, pageable).getContent();
        }
    }
}
