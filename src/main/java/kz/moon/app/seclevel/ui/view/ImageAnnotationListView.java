package kz.moon.app.seclevel.ui.view;


import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;
import kz.moon.app.seclevel.model.ImageAnnotation;
import kz.moon.app.seclevel.model.Image;
import kz.moon.app.seclevel.model.ClassifierCategory;
import kz.moon.app.seclevel.services.ImageAnnotationService;
import kz.moon.app.seclevel.services.ImageService;
import kz.moon.app.seclevel.services.ClassifierCategoryService;
import com.vaadin.flow.component.button.Button;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import com.vaadin.flow.data.provider.CallbackDataProvider;


import com.vaadin.flow.router.PageTitle;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.annotation.Secured;

import java.time.Instant;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.List;

@Secured({"ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN"})
@Route("annotation-list")
@PageTitle("Image Annotations")
@PermitAll
public class ImageAnnotationListView extends Main {

    private final ImageAnnotationService annotationService;
    private final ImageService imageService;
    private final ClassifierCategoryService categoryService;

    private final ComboBox<Image> imageCombo;
    private final ComboBox<ClassifierCategory> categoryCombo;
    private final TextArea annotationField;
    private final Checkbox validatedCheckbox;
    private final Button createBtn;
    private final TextField filterField = new TextField();
    private final Grid<ImageAnnotation> annotationGrid = new Grid<>(ImageAnnotation.class, false);

    public ImageAnnotationListView(ImageAnnotationService annotationService,
                                   ImageService imageService,
                                   ClassifierCategoryService categoryService) {
        this.annotationService = annotationService;
        this.imageService = imageService;
        this.categoryService = categoryService;

        imageCombo = new ComboBox<>("Image");
        List<Image> images = imageService.findAllImages();
        imageCombo.setItems(images);
        imageCombo.setItemLabelGenerator(img -> img.getFilename());
        imageCombo.setPlaceholder("Select image");

        categoryCombo = new ComboBox<>("Category");
        List<ClassifierCategory> categories = categoryService.findAllCategories();
        categoryCombo.setItems(categories);
        categoryCombo.setItemLabelGenerator(ClassifierCategory::getName);
        categoryCombo.setPlaceholder("Select category");

        annotationField = new TextArea("Annotation JSON");
        annotationField.setPlaceholder("Annotation JSON data...");
        annotationField.setWidthFull();
        validatedCheckbox = new Checkbox("Validated");

        createBtn = new Button("Add", event -> createAnnotation());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        filterField.setPlaceholder("Filter by category name...");
        filterField.setClearButtonVisible(true);
        filterField.setWidthFull();
        filterField.addValueChangeListener(e -> annotationGrid.getDataProvider().refreshAll());

        configureGrid();
        setupDataProvider();

        setSizeFull();
        add(new kz.moon.app.base.ui.component.ViewToolbar("Annotation List",
                kz.moon.app.base.ui.component.ViewToolbar.group(imageCombo, categoryCombo, createBtn)));
        add(filterField);
        add(annotationGrid);
    }

    private void configureGrid() {
        annotationGrid.addColumn(annotation -> Optional.ofNullable(annotation.getImage())
                        .map(img -> img.getFilename()).orElse(""))
                .setHeader("Image")
                .setAutoWidth(true);
        annotationGrid.addColumn(annotation -> Optional.ofNullable(annotation.getCategory())
                        .map(cat -> cat.getName()).orElse(""))
                .setHeader("Category")
                .setAutoWidth(true)
                .setSortable(true);
        annotationGrid.addColumn(ImageAnnotation::getAnnotationJson)
                .setHeader("Data (JSON)")
                .setAutoWidth(true);
        annotationGrid.addColumn(annotation -> annotation.isValidated() ? "Yes" : "No")
                .setHeader("Validated")
                .setAutoWidth(true);
        annotationGrid.addColumn(annotation -> {
                    Instant createdAt = annotation.getCreatedAt();
                    return createdAt != null ? DateTimeFormatter.ISO_INSTANT.format(createdAt) : "";
                })
                .setHeader("Created At")
                .setAutoWidth(true)
                .setSortable(true);
        annotationGrid.addColumn(annotation -> {
                    Instant validatedAt = annotation.getValidatedAt();
                    return validatedAt != null ? DateTimeFormatter.ISO_INSTANT.format(validatedAt) : "";
                })
                .setHeader("Validated At")
                .setAutoWidth(true);

        annotationGrid.addComponentColumn(annotation -> {
            Button editButton = new Button("Edit", click -> editAnnotation(annotation));
            Button deleteButton = new Button("Delete", click -> deleteAnnotation(annotation));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions");

        annotationGrid.setPageSize(10);
        annotationGrid.setSizeFull();
    }

    private void setupDataProvider() {
        CallbackDataProvider<ImageAnnotation, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    String sortBy = query.getSortOrders().isEmpty() ? "id"
                            : query.getSortOrders().get(0).getSorted();
                    boolean asc = query.getSortOrders().isEmpty()
                            || query.getSortOrders().get(0).getDirection()
                            == com.vaadin.flow.data.provider.SortDirection.ASCENDING;
                    return annotationService.find(filterField.getValue(), offset, limit, sortBy, asc).stream();
                },
                query -> (int) annotationService.count(filterField.getValue())
        );
        annotationGrid.setDataProvider(dataProvider);
    }

    private void createAnnotation() {
        if (imageCombo.getValue() != null && categoryCombo.getValue() != null) {
            annotationService.createAnnotation(
                    imageCombo.getValue().getId(),
                    categoryCombo.getValue().getId(),
                    annotationField.getValue(),
                    validatedCheckbox.getValue()
            );
            annotationGrid.getDataProvider().refreshAll();
            imageCombo.clear();
            categoryCombo.clear();
            annotationField.clear();
            validatedCheckbox.clear();
            Notification.show("Annotation added", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("Select image and category", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void editAnnotation(ImageAnnotation annotation) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Annotation");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        ComboBox<Image> imageField = new ComboBox<>("Image", imageService.findAllImages());
        imageField.setItemLabelGenerator(img -> img.getFilename());
        imageField.setValue(annotation.getImage());
        ComboBox<ClassifierCategory> categoryField = new ComboBox<>("Category", categoryService.findAllCategories());
        categoryField.setItemLabelGenerator(ClassifierCategory::getName);
        categoryField.setValue(annotation.getCategory());
        TextArea annotationDataField = new TextArea("Annotation JSON", annotation.getAnnotationJson());
        annotationDataField.setWidthFull();
        Checkbox validatedField = new Checkbox("Validated");
        validatedField.setValue(annotation.isValidated());

        Button saveButton = new Button("Save", event -> {
            annotation.setImage(imageField.getValue());
            annotation.setCategory(categoryField.getValue());
            annotation.setAnnotationJson(annotationDataField.getValue());
            boolean wasValidated = annotation.isValidated();
            annotation.setValidated(validatedField.getValue());
            if (!wasValidated && validatedField.getValue()) {
                // if just now marked validated, set the timestamp
                annotation.setValidatedAt(Instant.now());
            }
            annotationService.updateAnnotation(annotation);
            annotationGrid.getDataProvider().refreshAll();
            dialog.close();
            Notification.show("Annotation updated", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        dialog.add(new VerticalLayout(imageField, categoryField, annotationDataField, validatedField, buttons));
        dialog.open();
    }

    private void deleteAnnotation(ImageAnnotation annotation) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");
        confirmDialog.setCloseOnEsc(true);
        confirmDialog.setCloseOnOutsideClick(true);

        Span text = new Span("Delete annotation ID: " + annotation.getId() + " ?");
        Button deleteButton = new Button("Delete", event -> {
            annotationService.deleteAnnotation(annotation.getId());
            annotationGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            Notification.show("Annotation deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Cancel", event -> confirmDialog.close());
        HorizontalLayout buttons = new HorizontalLayout(deleteButton, cancelButton);

        confirmDialog.add(new VerticalLayout(text, buttons));
        confirmDialog.open();
    }
}
