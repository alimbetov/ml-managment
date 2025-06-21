package kz.moon.app.seclevel.ui.view;

import kz.moon.app.seclevel.services.ClassifierService;
import kz.moon.app.seclevel.services.ProjectService;
import kz.moon.app.seclevel.model.Classifier;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.domain.RolesEnum;
import kz.moon.app.seclevel.utils.SecurityUtils;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.annotation.Secured;

import java.util.Optional;
import java.util.List;

@Secured({"ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN"})
@Route("classifier-list")
@PageTitle("Classifier List")
@PermitAll
public class ClassifierListView extends Main {

    private final ClassifierService classifierService;
    private final ProjectService projectService;

    private final TextField nameField;
    private final TextField descriptionField;
    private final ComboBox<Project> projectCombo;
    private final Button createBtn;
    private final TextField filterField;
    private final Grid<Classifier> classifierGrid = new Grid<>(Classifier.class, false);

    public ClassifierListView(ClassifierService classifierService, ProjectService projectService) {
        this.classifierService = classifierService;
        this.projectService = projectService;

        nameField = new TextField();
        nameField.setPlaceholder("Classifier name");
        nameField.setMaxLength(255);
        descriptionField = new TextField();
        descriptionField.setPlaceholder("Description");
        projectCombo = new ComboBox<>("Project");
        List<Project> projects = projectService.findAllProjects();
        projectCombo.setItems(projects);
        projectCombo.setItemLabelGenerator(Project::getName);
        projectCombo.setPlaceholder("Select project");

        createBtn = new Button("Create", event -> createClassifier());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        // Optionally, only allow certain roles to create classifier (e.g., manager or admin)
        if (!SecurityUtils.hasRole(RolesEnum.ROLE_ADMIN)) {
            // if not admin, could disable creation or impose other logic
        }

        filterField = new TextField();
        filterField.setPlaceholder("Filter by name...");
        filterField.setClearButtonVisible(true);
        filterField.setWidthFull();
        filterField.addValueChangeListener(e -> classifierGrid.getDataProvider().refreshAll());

        configureGrid();
        setupDataProvider();

        setSizeFull();
        addClassName("padding-medium"); // alternative styling if no custom toolbar
        // If ViewToolbar is available, use it
        add(new kz.moon.app.base.ui.component.ViewToolbar("Classifier List",
                kz.moon.app.base.ui.component.ViewToolbar.group(nameField, descriptionField, projectCombo, createBtn)));
        add(filterField);
        add(classifierGrid);
    }

    private void configureGrid() {
        classifierGrid.addColumn(Classifier::getName)
                .setHeader("Name")
                .setAutoWidth(true)
                .setSortable(true);
        classifierGrid.addColumn(Classifier::getDescription)
                .setHeader("Description")
                .setAutoWidth(true)
                .setSortable(true);
        classifierGrid.addColumn(classifier -> Optional.ofNullable(classifier.getProject())
                        .map(Project::getName).orElse(""))
                .setHeader("Project")
                .setAutoWidth(true)
                .setSortable(true);
        classifierGrid.addColumn(classifier -> Optional.ofNullable(classifier.getCreatedBy())
                        .map(user -> user.getUsername()).orElse(""))
                .setHeader("Created By")
                .setAutoWidth(true);

        classifierGrid.addComponentColumn(classifier -> {
            Button editButton = new Button("Edit", click -> editClassifier(classifier));
            Button deleteButton = new Button("Delete", click -> deleteClassifier(classifier));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions");

        classifierGrid.setPageSize(10);
        classifierGrid.setSizeFull();
    }

    private void setupDataProvider() {
        CallbackDataProvider<Classifier, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    String sortBy = query.getSortOrders().isEmpty() ? "id"
                            : query.getSortOrders().get(0).getSorted();
                    boolean asc = query.getSortOrders().isEmpty()
                            || query.getSortOrders().get(0).getDirection()
                            == com.vaadin.flow.data.provider.SortDirection.ASCENDING;
                    return classifierService.find(filterField.getValue(), offset, limit, sortBy, asc).stream();
                },
                query -> (int) classifierService.count(filterField.getValue())
        );
        classifierGrid.setDataProvider(dataProvider);
    }

    private void createClassifier() {
        if (projectCombo.getValue() != null) {
            classifierService.createClassifier(
                    nameField.getValue(), descriptionField.getValue(),
                    projectCombo.getValue().getId()
            );
            classifierGrid.getDataProvider().refreshAll();
            nameField.clear();
            descriptionField.clear();
            projectCombo.clear();
            Notification.show("Classifier created", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("Select a project for the classifier", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void editClassifier(Classifier classifier) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Classifier");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        TextField nameField = new TextField("Name", classifier.getName(), "");
        TextField descriptionField = new TextField("Description", classifier.getDescription(), "");
        ComboBox<Project> projectField = new ComboBox<>("Project", projectService.findAllProjects());
        projectField.setItemLabelGenerator(Project::getName);
        projectField.setValue(classifier.getProject());

        Button saveButton = new Button("Save", event -> {
            classifier.setName(nameField.getValue());
            classifier.setDescription(descriptionField.getValue());
            classifier.setProject(projectField.getValue());
            classifierService.updateClassifier(classifier);
            classifierGrid.getDataProvider().refreshAll();
            dialog.close();
            Notification.show("Classifier updated", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        dialog.add(new VerticalLayout(nameField, descriptionField, projectField, buttons));
        dialog.open();
    }

    private void deleteClassifier(Classifier classifier) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");
        confirmDialog.setCloseOnEsc(true);
        confirmDialog.setCloseOnOutsideClick(true);

        Span text = new Span("Delete classifier: " + classifier.getName() + " ?");
        Button deleteButton = new Button("Delete", event -> {
            classifierService.deleteClassifier(classifier.getId());
            classifierGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            Notification.show("Classifier deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Cancel", event -> confirmDialog.close());
        HorizontalLayout buttons = new HorizontalLayout(deleteButton, cancelButton);

        confirmDialog.add(new VerticalLayout(text, buttons));
        confirmDialog.open();
    }
}
