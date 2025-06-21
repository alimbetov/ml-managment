package kz.moon.app.seclevel.ui.view;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import kz.moon.app.seclevel.domain.RolesEnum;
import kz.moon.app.seclevel.model.ProjectUserAssignment;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.domain.User;

import kz.moon.app.seclevel.services.ProjectUserAssignmentService;
import kz.moon.app.seclevel.services.ProjectService;
import kz.moon.app.seclevel.repository.UserRepository;
import com.vaadin.flow.component.button.Button;

import com.vaadin.flow.component.dialog.Dialog;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.Span;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.annotation.Secured;
import java.util.Optional;
import java.util.List;

@Secured({"ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN"})
@Route("assignment-list")
@PageTitle("Project User Assignments")
@PermitAll
public class ProjectUserAssignmentListView extends Main {

    private final ProjectUserAssignmentService assignmentService;
    private final ProjectService projectService;
    private final UserRepository userRepository; // or a UserService providing users

    private final ComboBox<Project> projectCombo;
    private final ComboBox<User> userCombo;
    private final ComboBox<RolesEnum> roleCombo;
    private final Button createBtn;
    private final TextField filterField;
    private final Grid<ProjectUserAssignment> assignmentGrid = new Grid<>(ProjectUserAssignment.class, false);

    public ProjectUserAssignmentListView(ProjectUserAssignmentService assignmentService,
                                         ProjectService projectService,
                                         UserRepository userRepository) {
        this.assignmentService = assignmentService;
        this.projectService = projectService;
        this.userRepository = userRepository;

        projectCombo = new ComboBox<>("Project");
        List<Project> projects = projectService.findAllProjects();
        projectCombo.setItems(projects);
        projectCombo.setItemLabelGenerator(Project::getName);
        projectCombo.setPlaceholder("Select project");
        userCombo = new ComboBox<>("User");
        List<User> users = userRepository.findAll();
        userCombo.setItems(users);
        userCombo.setItemLabelGenerator(user -> user.getUsername()); // assuming User has getUsername()
        userCombo.setPlaceholder("Select user");
        roleCombo = new ComboBox<>("Role");
        roleCombo.setItems(RolesEnum.values());
        roleCombo.setPlaceholder("Select role");

        createBtn = new Button("Assign", event -> createAssignment());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        filterField = new TextField();
        filterField.setPlaceholder("Filter by username...");
        filterField.setClearButtonVisible(true);
        filterField.setWidthFull();
        filterField.addValueChangeListener(e -> assignmentGrid.getDataProvider().refreshAll());

        configureGrid();
        setupDataProvider();

        setSizeFull();
        add(new kz.moon.app.base.ui.component.ViewToolbar("Assignments",
                kz.moon.app.base.ui.component.ViewToolbar.group(projectCombo, userCombo, roleCombo, createBtn)));
        add(filterField);
        add(assignmentGrid);
    }

    private void configureGrid() {
        assignmentGrid.addColumn(assignment -> Optional.ofNullable(assignment.getProject())
                        .map(Project::getName).orElse(""))
                .setHeader("Project")
                .setAutoWidth(true)
                .setSortable(true);
        assignmentGrid.addColumn(assignment -> Optional.ofNullable(assignment.getUser())
                        .map(user -> user.getUsername()).orElse(""))
                .setHeader("User")
                .setAutoWidth(true)
                .setSortable(true);
        assignmentGrid.addColumn(assignment -> assignment.getRole().name())
                .setHeader("Role")
                .setAutoWidth(true);

        assignmentGrid.addComponentColumn(assignment -> {
            Button deleteButton = new Button("Delete", click -> deleteAssignment(assignment));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            return new HorizontalLayout(deleteButton);
        }).setHeader("Actions");

        assignmentGrid.setPageSize(10);
        assignmentGrid.setSizeFull();
    }

    private void setupDataProvider() {
        CallbackDataProvider<ProjectUserAssignment, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    String sortBy = query.getSortOrders().isEmpty() ? "id"
                            : query.getSortOrders().get(0).getSorted();
                    boolean asc = query.getSortOrders().isEmpty()
                            || query.getSortOrders().get(0).getDirection()
                            == com.vaadin.flow.data.provider.SortDirection.ASCENDING;
                    return assignmentService.find(filterField.getValue(), offset, limit, sortBy, asc).stream();
                },
                query -> (int) assignmentService.count(filterField.getValue())
        );
        assignmentGrid.setDataProvider(dataProvider);
    }

    private void createAssignment() {
        if (projectCombo.getValue() != null && userCombo.getValue() != null && roleCombo.getValue() != null) {
            assignmentService.createAssignment(
                    projectCombo.getValue().getId(),
                    userCombo.getValue().getId(),
                    roleCombo.getValue()
            );
            assignmentGrid.getDataProvider().refreshAll();
            projectCombo.clear();
            userCombo.clear();
            roleCombo.clear();
            Notification.show("User assigned to project", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("Select project, user, and role", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteAssignment(ProjectUserAssignment assignment) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Remove");
        confirmDialog.setCloseOnEsc(true);
        confirmDialog.setCloseOnOutsideClick(true);

        Span text = new Span("Remove user " + assignment.getUser().getUsername() +
                " from project " + assignment.getProject().getName() + "?");
        Button deleteButton = new Button("Remove", event -> {
            assignmentService.deleteAssignment(assignment.getId());
            assignmentGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            Notification.show("Assignment removed", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Cancel", event -> confirmDialog.close());
        HorizontalLayout buttons = new HorizontalLayout(deleteButton, cancelButton);

        confirmDialog.add(new VerticalLayout(text, buttons));
        confirmDialog.open();
    }
}
