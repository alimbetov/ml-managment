package kz.moon.app.seclevel.services;

import kz.moon.app.seclevel.domain.RolesEnum;
import kz.moon.app.seclevel.model.ProjectUserAssignment;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.domain.User;

import kz.moon.app.seclevel.repository.ProjectUserAssignmentRepository;
import kz.moon.app.seclevel.repository.ProjectRepository;
import kz.moon.app.seclevel.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
public class ProjectUserAssignmentService {

    private final ProjectUserAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectUserAssignmentService(ProjectUserAssignmentRepository assignmentRepository,
                                        ProjectRepository projectRepository,
                                        UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectUserAssignment> find(String usernameFilter, int offset, int limit, String sortBy, boolean asc) {
        Pageable pageable = PageRequest.of(offset / limit, limit,
                asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        if (usernameFilter == null || usernameFilter.isEmpty()) {
            return assignmentRepository.findAllBy(pageable).getContent();
        } else {
            return assignmentRepository.findByUser_UsernameContainingIgnoreCase(usernameFilter, pageable).getContent();
        }
    }

    public long count(String usernameFilter) {
        if (usernameFilter == null || usernameFilter.isEmpty()) {
            return assignmentRepository.count();
        } else {
            return assignmentRepository.countByUser_UsernameContainingIgnoreCase(usernameFilter);
        }
    }

    public ProjectUserAssignment createAssignment(Long projectId, Long userId, RolesEnum role) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        ProjectUserAssignment assignment = ProjectUserAssignment.builder()
                .project(project)
                .user(user)
                .role(role)
                .build();
        return assignmentRepository.save(assignment);
    }

    public ProjectUserAssignment updateAssignment(ProjectUserAssignment assignment) {
        return assignmentRepository.save(assignment);
    }

    public  List<User> getUsersByProjectIn( List<Project> projects) {
       return assignmentRepository.findDistinctUsersByProjectIn(projects);
    }
    public  List<Project> getProjectsList( List<Project> projects) {
        return assignmentRepository.findDistinctProjectsIn(projects);
    }

    public void deleteAssignment(Long assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }
}
