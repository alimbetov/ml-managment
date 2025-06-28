package kz.moon.app.seclevel.ui.view;

import com.vaadin.flow.component.textfield.TextArea;
import kz.moon.app.seclevel.model.ClassifierCategory;
import kz.moon.app.seclevel.model.Classifier;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.services.ClassifierCategoryService;
import kz.moon.app.seclevel.services.ClassifierService;
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
@Route("category-list")
@PageTitle("Classifier Categories")
@PermitAll
public class ClassifierCategoryListView extends Main {

    private final ClassifierCategoryService categoryService;
    private final ClassifierService classifierService;

    private final TextField nameField;

    private final TextArea instructionsField;

    private final ComboBox<Classifier> classifierCombo;
    private final Button createBtn;
    private final TextField filterField;
    private final Grid<ClassifierCategory> categoryGrid = new Grid<>(ClassifierCategory.class, false);

    public ClassifierCategoryListView(ClassifierCategoryService categoryService, ClassifierService classifierService) {
        this.categoryService = categoryService;
        this.classifierService = classifierService;

        nameField = new TextField();
        nameField.setPlaceholder("Category name");
        nameField.setMaxLength(255);


        instructionsField = new TextArea("Instructions");
        instructionsField.setWidthFull();
        instructionsField.setHeight("150px");
        instructionsField.setPlaceholder("Enter instructions here...");

        classifierCombo = new ComboBox<>("Classifier");
        List<Classifier> classifiers = classifierService.findAllClassifiers();
        classifierCombo.setItems(classifiers);
        classifierCombo.setItemLabelGenerator(Classifier::getName);
        classifierCombo.setPlaceholder("Select classifier");

        createBtn = new Button("Create", event -> createCategory());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        filterField = new TextField();
        filterField.setPlaceholder("Filter by name...");
        filterField.setClearButtonVisible(true);
        filterField.setWidthFull();
        filterField.addValueChangeListener(e -> categoryGrid.getDataProvider().refreshAll());

        configureGrid();
        setupDataProvider();

        setSizeFull();
        add(new kz.moon.app.base.ui.component.ViewToolbar("Category List",
                kz.moon.app.base.ui.component.ViewToolbar.group(nameField, instructionsField, classifierCombo, createBtn)));
        add(filterField);
        add(categoryGrid);
    }

    private void configureGrid() {
        categoryGrid.addColumn(ClassifierCategory::getName)
                .setHeader("Name")
                .setAutoWidth(true)
                .setSortable(true);
        categoryGrid.addColumn(ClassifierCategory::getInstructions)
                .setHeader("Instructions")
                .setAutoWidth(true);
        categoryGrid.addColumn(category -> Optional.ofNullable(category.getClassifier())
                        .map(Classifier::getName).orElse(""))
                .setHeader("Classifier")
                .setAutoWidth(true)
                .setSortable(true);
        categoryGrid.addColumn(category -> {
                    // show project name via classifier if available
                    return Optional.ofNullable(category.getClassifier())
                            .map(cls -> Optional.ofNullable(cls.getProject()).map(Project::getName).orElse(""))
                            .orElse("");
                })
                .setHeader("Project")
                .setAutoWidth(true);

        categoryGrid.addComponentColumn(category -> {
            Button editButton = new Button("Edit", click -> editCategory(category));
            Button deleteButton = new Button("Delete", click -> deleteCategory(category));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions");

        categoryGrid.setPageSize(10);
        categoryGrid.setSizeFull();
    }

    private void setupDataProvider() {
        CallbackDataProvider<ClassifierCategory, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    String sortBy = query.getSortOrders().isEmpty() ? "id"
                            : query.getSortOrders().get(0).getSorted();
                    boolean asc = query.getSortOrders().isEmpty()
                            || query.getSortOrders().get(0).getDirection()
                            == com.vaadin.flow.data.provider.SortDirection.ASCENDING;
                    return categoryService.find(filterField.getValue(), offset, limit, sortBy, asc).stream();
                },
                query -> (int) categoryService.count(filterField.getValue())
        );
        categoryGrid.setDataProvider(dataProvider);
    }

    private void createCategory() {
        if (classifierCombo.getValue() != null) {
            categoryService.createCategory(
                    nameField.getValue(), instructionsField.getValue(),
                    classifierCombo.getValue().getId()
            );
            categoryGrid.getDataProvider().refreshAll();
            nameField.clear();
            instructionsField.clear();
            classifierCombo.clear();
            Notification.show("Category created", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("Select a classifier", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void editCategory(ClassifierCategory category) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Category");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        TextField nameField = new TextField("Name", category.getName(), "");

        TextArea instructionsField = new TextArea("Instructions");
        instructionsField.setWidthFull();
        instructionsField.setHeight("150px");
        instructionsField.setValue(Optional.ofNullable(category.getInstructions()).orElse(""));

        ComboBox<Classifier> classifierField = new ComboBox<>("Classifier", classifierService.findAllClassifiers());
        classifierField.setItemLabelGenerator(Classifier::getName);
        classifierField.setValue(category.getClassifier());

        Button saveButton = new Button("Save", event -> {
            category.setName(nameField.getValue());
            category.setInstructions(instructionsField.getValue());
            category.setClassifier(classifierField.getValue());
            categoryService.updateCategory(category);
            categoryGrid.getDataProvider().refreshAll();
            dialog.close();
            Notification.show("Category updated", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        dialog.add(new VerticalLayout(nameField, instructionsField, classifierField, buttons));
        dialog.open();
    }

    private void deleteCategory(ClassifierCategory category) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");
        confirmDialog.setCloseOnEsc(true);
        confirmDialog.setCloseOnOutsideClick(true);

        Span text = new Span("Delete category: " + category.getName() + " ?");
        Button deleteButton = new Button("Delete", event -> {
            categoryService.deleteCategory(category.getId());
            categoryGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            Notification.show("Category deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Cancel", event -> confirmDialog.close());
        HorizontalLayout buttons = new HorizontalLayout(deleteButton, cancelButton);

        confirmDialog.add(new VerticalLayout(text, buttons));
        confirmDialog.open();
    }
}
