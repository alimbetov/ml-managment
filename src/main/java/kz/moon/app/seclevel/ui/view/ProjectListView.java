package kz.moon.app.seclevel.ui.view;


import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.services.ProjectService;
import kz.moon.app.seclevel.utils.SecurityUtils;
import kz.moon.app.seclevel.domain.RolesEnum;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.annotation.Secured;

import java.util.Optional;

@Secured({"ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN"})
@Route("project-list")
@PageTitle("Project List")
@PermitAll
public class ProjectListView extends Main {

    private final ProjectService projectService;

    // UI Components
    private final TextField nameField;
    private final TextField industryField;
    private final Button createBtn;
    private final TextField filterField;
    private final Grid<Project> projectGrid = new Grid<>(Project.class, false);

    public ProjectListView(ProjectService projectService) {
        this.projectService = projectService;

        // Input fields for new Project
        nameField = new TextField();
        nameField.setPlaceholder("Project name");
        nameField.setMaxLength(255);
        industryField = new TextField();
        industryField.setPlaceholder("Industry");

        createBtn = new Button("Create", event -> createProject());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        // Only allow project creation for admin users
        if (!SecurityUtils.hasRole(RolesEnum.ROLE_ADMIN)) {
            createBtn.setVisible(false);
        }

        // Filter field for project name
        filterField = new TextField();
        filterField.setPlaceholder("Filter by name...");
        filterField.setClearButtonVisible(true);
        filterField.setWidthFull();
        filterField.addValueChangeListener(e -> projectGrid.getDataProvider().refreshAll());

        configureGrid();
        setupDataProvider();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        // Optional: using a custom ViewToolbar component for header (if available)
        add(new kz.moon.app.base.ui.component.ViewToolbar("Project List",
                kz.moon.app.base.ui.component.ViewToolbar.group(nameField, industryField, createBtn)));
        add(filterField);
        add(projectGrid);
    }

    private void configureGrid() {
        projectGrid.addColumn(Project::getName)
                .setHeader("Name")
                .setAutoWidth(true)
                .setSortable(true);
        projectGrid.addColumn(Project::getIndustry)
                .setHeader("Industry")
                .setAutoWidth(true)
                .setSortable(true);
        projectGrid.addColumn(project -> project.getCreatedDate().toString())
                .setHeader("Created Date")
                .setAutoWidth(true)
                .setSortable(true);
        projectGrid.addColumn(project -> Optional.ofNullable(project.getCreatedBy())
                        .map(user -> user.getUsername()) // assuming User has getUsername()
                        .orElse(""))
                .setHeader("Created By")
                .setAutoWidth(true);

        projectGrid.addComponentColumn(project -> {
            Button editButton = new Button("Edit", click -> editProject(project));
            Button deleteButton = new Button("Delete", click -> deleteProject(project));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            // Optionally, restrict deletion to admins
            if (!SecurityUtils.hasRole(RolesEnum.ROLE_ADMIN)) {
                deleteButton.setEnabled(false);
            }
            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions");

        projectGrid.setPageSize(10);
        projectGrid.setSizeFull();
    }

    private void setupDataProvider() {
        CallbackDataProvider<Project, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    String sortBy = query.getSortOrders().isEmpty() ? "createdDate"
                            : query.getSortOrders().get(0).getSorted();
                    boolean asc = query.getSortOrders().isEmpty()
                            || query.getSortOrders().get(0).getDirection()
                            == com.vaadin.flow.data.provider.SortDirection.ASCENDING;
                    return projectService.find(filterField.getValue(), offset, limit, sortBy, asc).stream();
                },
                query -> (int) projectService.count(filterField.getValue())
        );
        projectGrid.setDataProvider(dataProvider);
    }

    private void createProject() {
        projectService.createProject(nameField.getValue(), "", industryField.getValue());
        projectGrid.getDataProvider().refreshAll();
        nameField.clear();
        industryField.clear();
        Notification.show("Project created", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void editProject(Project project) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Project");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        TextField nameField = new TextField("Name", project.getName(), "");
        TextField industryField = new TextField("Industry", project.getIndustry(), "");
        TextArea descriptionField = new TextArea("Description", project.getDescription());
        descriptionField.setWidthFull();

        Button saveButton = new Button("Save", event -> {
            project.setName(nameField.getValue());
            project.setIndustry(industryField.getValue());
            project.setDescription(descriptionField.getValue());
            projectService.updateProject(project);
            projectGrid.getDataProvider().refreshAll();
            dialog.close();
            Notification.show("Project updated", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(new VerticalLayout(nameField, industryField, descriptionField, buttons));
        dialog.open();
    }

    private void deleteProject(Project project) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");
        confirmDialog.setCloseOnEsc(true);
        confirmDialog.setCloseOnOutsideClick(true);

        Span text = new Span("Are you sure you want to delete project: " + project.getName() + "?");
        Button deleteButton = new Button("Delete", event -> {
            projectService.deleteProject(project.getId());
            projectGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            Notification.show("Project deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Cancel", event -> confirmDialog.close());
        HorizontalLayout buttons = new HorizontalLayout(deleteButton, cancelButton);

        confirmDialog.add(new VerticalLayout(text, buttons));
        confirmDialog.open();
    }
}
