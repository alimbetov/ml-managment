package kz.moon.app.seclevel.ui.view;


import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import kz.moon.app.seclevel.model.Image;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.repository.ImageStatus;
import kz.moon.app.seclevel.services.ImageService;
import kz.moon.app.seclevel.services.ProjectService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.select.Select;
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

import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

@Secured({"ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN"})
@Route("image-list")
@PageTitle("Images")
@PermitAll
public class ImageListView extends Main {

    private final ImageService imageService;
    private final ProjectService projectService;

    private final TextField filenameField;
    private final ComboBox<Project> projectCombo;
    private final Button createBtn;

    private Button openUploadDialogBtn;

    private final TextField filterField;
    private final Grid<Image> imageGrid = new Grid<>(Image.class, false);

    private final  List<ImageStatus> imageStatusList = List.of(
            ImageStatus.UPLOADED,
            ImageStatus.IN_PROGRESS,
            ImageStatus.MARKED,
            ImageStatus.REJECTED,
            ImageStatus.REVIEWED,
            ImageStatus.APPROVED
            );


    public ImageListView(ImageService imageService, ProjectService projectService) {
        this.imageService = imageService;
        this.projectService = projectService;

        filenameField = new TextField();
        filenameField.setPlaceholder("Filename");
        projectCombo = new ComboBox<>("Project");
        List<Project> projects = projectService.findAllProjects();
        projectCombo.setItems(projects);
        projectCombo.setItemLabelGenerator(Project::getName);
        projectCombo.setPlaceholder("Select project");

        createBtn = new Button("Upload", event -> createImage());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);




        filterField = new TextField();
        filterField.setPlaceholder("Filter by filename...");
        filterField.setClearButtonVisible(true);
        filterField.setWidthFull();
        filterField.addValueChangeListener(e -> imageGrid.getDataProvider().refreshAll());




        configureGrid();
        setupDataProvider();

        setSizeFull();

        openUploadDialogBtn = new Button("Upload New Image", click -> openUploadDialogWithFile());
        openUploadDialogBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


        add(new kz.moon.app.base.ui.component.ViewToolbar("Image List",
                kz.moon.app.base.ui.component.ViewToolbar.group(filenameField, projectCombo, createBtn,openUploadDialogBtn)));
        add(filterField);
        add(imageGrid);
    }

    private void configureGrid() {
        imageGrid.addColumn(Image::getFilename)
                .setHeader("Filename")
                .setAutoWidth(true)
                .setSortable(true);
        imageGrid.addColumn(image -> Optional.ofNullable(image.getProject())
                        .map(Project::getName).orElse(""))
                .setHeader("Project")
                .setAutoWidth(true);
        imageGrid.addColumn(image -> Optional.ofNullable(image.getUploadedBy())
                        .map(user -> user.getUsername()).orElse(""))
                .setHeader("Uploaded By")
                .setAutoWidth(true);
        imageGrid.addColumn(image -> image.getUploadDate().toString())
                .setHeader("Upload Date")
                .setAutoWidth(true)
                .setSortable(true);
        imageGrid.addColumn(image -> image.getStatus().name())
                .setHeader("Status")
                .setAutoWidth(true)
                .setSortable(true);

        imageGrid.addComponentColumn(image -> {
            Button editButton = new Button("Edit", click -> editImage(image));
            Button deleteButton = new Button("Delete", click -> deleteImage(image));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions");

        imageGrid.setPageSize(10);
        imageGrid.setSizeFull();
    }

    private void setupDataProvider() {
        CallbackDataProvider<Image, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    String sortBy = query.getSortOrders().isEmpty() ? "uploadDate"
                            : query.getSortOrders().get(0).getSorted();
                    boolean asc = query.getSortOrders().isEmpty()
                            || query.getSortOrders().get(0).getDirection()
                            == com.vaadin.flow.data.provider.SortDirection.ASCENDING;
                    return imageService.find(filterField.getValue(), offset, limit, sortBy, asc).stream();
                },
                query -> (int) imageService.count(filterField.getValue())
        );
        imageGrid.setDataProvider(dataProvider);
    }

    private void createImage() {
        if (projectCombo.getValue() != null) {
            imageService.createImage(filenameField.getValue(), projectCombo.getValue().getId());
            imageGrid.getDataProvider().refreshAll();
            filenameField.clear();
            projectCombo.clear();
            Notification.show("Image uploaded", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("Select a project for the image", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void editImage(Image image) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Image");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        TextField filenameField = new TextField("Filename", image.getFilename(), "");
        ComboBox<Project> projectField = new ComboBox<>("Project", projectService.findAllProjects());
        projectField.setItemLabelGenerator(Project::getName);
        projectField.setValue(image.getProject());
        ComboBox<ImageStatus> statusField = new ComboBox<>("Status", imageStatusList);
        statusField.setLabel("Status");
        statusField.setValue(image.getStatus());

        Button saveButton = new Button("Save", event -> {
            image.setFilename(filenameField.getValue());
            image.setProject(projectField.getValue());
            image.setStatus(statusField.getValue());
            imageService.updateImage(image);
            imageGrid.getDataProvider().refreshAll();
            dialog.close();
            Notification.show("Image updated", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        dialog.add(new VerticalLayout(filenameField, projectField, statusField, buttons));
        dialog.open();
    }



    private void deleteImage(Image image) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");
        confirmDialog.setCloseOnEsc(true);
        confirmDialog.setCloseOnOutsideClick(true);

        Span text = new Span("Delete image: " + image.getFilename() + " ?");
        Button deleteButton = new Button("Delete", event -> {
            imageService.deleteImage(image.getId());
            imageGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            Notification.show("Image deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Cancel", event -> confirmDialog.close());
        HorizontalLayout buttons = new HorizontalLayout(deleteButton, cancelButton);

        confirmDialog.add(new VerticalLayout(text, buttons));
        confirmDialog.open();
    }

    private void openUploadDialogWithFile() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Upload Image (JPEG, PNG)");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        // Project selection
        ComboBox<Project> projectField = new ComboBox<>("Project", projectService.findAllProjects());
        projectField.setItemLabelGenerator(Project::getName);
        projectField.setPlaceholder("Select project");

        // Upload component - MemoryBuffer will store file in memory
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.setAcceptedFileTypes("image/jpeg", "image/png");

        upload.addSucceededListener(event -> {
            String filename = event.getFileName();

            if (projectField.getValue() == null) {
                Notification.show("Please select project first!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try (var inputStream = buffer.getInputStream()) {
                // Пример — ты можешь тут сохранить в БД или в FileSystem
                imageService.saveUploadedFile(filename, projectField.getValue().getId(), inputStream);
                imageGrid.getDataProvider().refreshAll();
                Notification.show("Image " + filename + " uploaded!", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                Notification.show("Upload failed!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(new VerticalLayout(projectField, upload, closeBtn));
        dialog.open();
    }

}

