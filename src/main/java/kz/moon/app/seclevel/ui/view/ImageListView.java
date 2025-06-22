package kz.moon.app.seclevel.ui.view;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import kz.moon.app.seclevel.domain.User;
import kz.moon.app.seclevel.model.ClassifierCategory;
import kz.moon.app.seclevel.model.Image;
import kz.moon.app.seclevel.model.Project;
import kz.moon.app.seclevel.repository.ImageStatus;
import kz.moon.app.seclevel.services.ClassifierCategoryService;
import kz.moon.app.seclevel.services.ImageService;
import kz.moon.app.seclevel.services.ProjectService;
import kz.moon.app.seclevel.services.ProjectUserAssignmentService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Secured({"ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN"})
@Route("image-list")
@PageTitle("Images")
@PermitAll
@Slf4j
public class ImageListView extends Main {

    private final ImageService imageService;
    private final ProjectService projectService;
    private final ProjectUserAssignmentService assignmentService;

    private final ClassifierCategoryService categoryService;

    private final ComboBox<Project> projectFilter;
    private final ComboBox<ImageStatus> statusFilter;
    private final ComboBox<User> authorFilter;
    private final ComboBox<Image> parentImageFilter;
    private final ComboBox<ClassifierCategory> classifierCategoryFilter;

    private final DatePicker uploadDateFilter;

    private final Button openUploadDialogBtn;

    private final Grid<Image> imageGrid = new Grid<>(Image.class, false);

    private final List<ImageStatus> imageStatusList = List.of(
            ImageStatus.UPLOADED,
            ImageStatus.IN_PROGRESS,
            ImageStatus.MARKED,
            ImageStatus.REJECTED,
            ImageStatus.REVIEWED,
            ImageStatus.APPROVED
    );

    public ImageListView(ImageService imageService, ProjectService projectService, ProjectUserAssignmentService projectUserAssignmentService,ClassifierCategoryService categoryService) {
        this.imageService = imageService;
        this.projectService = projectService;
        this.assignmentService = projectUserAssignmentService;
        this.categoryService = categoryService;
        var projectList = assignmentService.getAvailsableProjectsList();

        projectFilter = new ComboBox<>("Project");
        projectFilter.setItems(projectList);
        projectFilter.setItemLabelGenerator(Project::getName);
        projectFilter.addValueChangeListener(e -> imageGrid.getDataProvider().refreshAll());

        statusFilter = new ComboBox<>("Status", imageStatusList);
        statusFilter.addValueChangeListener(e -> imageGrid.getDataProvider().refreshAll());

        authorFilter = new ComboBox<>("Author");
        authorFilter.setItems(assignmentService.getUsersByProjectIn(projectList));
        authorFilter.setItemLabelGenerator(User::getUsername);
        authorFilter.addValueChangeListener(e -> imageGrid.getDataProvider().refreshAll());

        uploadDateFilter = new DatePicker("Upload Date");
        uploadDateFilter.addValueChangeListener(e -> imageGrid.getDataProvider().refreshAll());

        openUploadDialogBtn = new Button("Upload Image", click -> openUploadDialogWithFile());
        openUploadDialogBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


        parentImageFilter = new ComboBox<>("parentImage");
        parentImageFilter.setItems(imageService.getParentImages(projectList));
        parentImageFilter.setItemLabelGenerator(Image::getFilename);
        parentImageFilter.addValueChangeListener(e -> imageGrid.getDataProvider().refreshAll());


        classifierCategoryFilter = new ComboBox<>("Category");
        classifierCategoryFilter.setItems(categoryService.getAllClassifierCategoryByProjectIn(projectList));
        classifierCategoryFilter.setItemLabelGenerator(ClassifierCategory::getName);
        classifierCategoryFilter.addValueChangeListener(e -> imageGrid.getDataProvider().refreshAll());


        configureGrid();
        setupDataProvider();

        setSizeFull();

        add(new kz.moon.app.base.ui.component.ViewToolbar("Image",
                kz.moon.app.base.ui.component.ViewToolbar.group(
                        openUploadDialogBtn
                )));
        add(new kz.moon.app.base.ui.component.ViewToolbar("filtr",
                kz.moon.app.base.ui.component.ViewToolbar.group(
                        projectFilter, statusFilter, authorFilter, uploadDateFilter
                )));

        add(new kz.moon.app.base.ui.component.ViewToolbar("filtr",
                kz.moon.app.base.ui.component.ViewToolbar.group(
                        parentImageFilter, classifierCategoryFilter
                )));

        add(imageGrid);
    }


    private void configureGrid() {

        imageGrid.addComponentColumn(image -> {
            Icon statusIcon;
            switch (image.getStatus()) {
                case UPLOADED -> statusIcon = VaadinIcon.CLOUD_UPLOAD_O.create();
                case IN_PROGRESS -> statusIcon = VaadinIcon.TIME_FORWARD.create();
                case MARKED -> statusIcon = VaadinIcon.FLAG.create();
                case REVIEWED -> statusIcon = VaadinIcon.SEARCH.create();
                case APPROVED -> statusIcon = VaadinIcon.CHECK.create();
                case REJECTED -> statusIcon = VaadinIcon.CLOSE_SMALL.create();
                default -> statusIcon = VaadinIcon.QUESTION.create();
            }
            statusIcon.getElement().setAttribute("title", image.getStatus().name());
            return statusIcon;
        }).setHeader("Status").setAutoWidth(true);

        imageGrid.addColumn(Image::getFilename)
                .setHeader("Filename")
                .setAutoWidth(true)
                .setSortable(false);
        imageGrid.addColumn(image -> Optional.ofNullable(image.getProject())
                        .map(Project::getName).orElse(""))
                .setHeader("Project")
                .setAutoWidth(false);
        imageGrid.addColumn(image -> Optional.ofNullable(image.getUploadedBy())
                        .map(User::getUsername).orElse(""))
                .setHeader("Uploaded By")
                .setAutoWidth(false);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                .withZone(ZoneId.of("Asia/Almaty")); // Казахстанское время
        imageGrid.addColumn(image ->
                        image.getUploadDate() != null
                                ? formatter.format(image.getUploadDate())
                                : "")
                .setHeader("Upload Date")
                .setAutoWidth(true)
                .setSortable(false);



        imageGrid.addColumn(image -> Optional.ofNullable(image.getParentImage())
                        .map(Image::getFilename).orElse(""))
                .setHeader("ParentImage")
                .setAutoWidth(true);

        imageGrid.addColumn(image -> Optional.ofNullable(image.getClassifierCategory())
                        .map(ClassifierCategory::getlevelName).orElse(""))
                .setHeader("Category")
                .setAutoWidth(false);

        imageGrid.addComponentColumn(image -> {
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            editButton.getElement().setAttribute("title", "Edit");
            editButton.addClickListener(click -> editImage(image));

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
            deleteButton.getElement().setAttribute("title", "Delete");
            deleteButton.addClickListener(click -> deleteImage(image));

            HorizontalLayout actionsLayout = new HorizontalLayout(editButton, deleteButton);
            actionsLayout.setSpacing(false); // убираем лишние отступы
            actionsLayout.setPadding(false); // убираем паддинги
            return actionsLayout;
        }).setHeader("Actions").setAutoWidth(true);


        imageGrid.setPageSize(25);
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

                    return imageService.find(
                            projectFilter.getValue(),
                            statusFilter.getValue(),
                            authorFilter.getValue(),
                            parentImageFilter.getValue(),
                            classifierCategoryFilter.getValue(),
                            uploadDateFilter.getValue(),
                            offset, limit, sortBy, asc
                    ).stream();
                },
                query -> (int) imageService.count(
                        projectFilter.getValue(),
                        statusFilter.getValue(),
                        authorFilter.getValue(),
                        parentImageFilter.getValue(),
                        classifierCategoryFilter.getValue(),
                        uploadDateFilter.getValue()
                )
        );
        imageGrid.setDataProvider(dataProvider);
    }
    private void editImage(Image image) {
        var projectList = assignmentService.getAvailsableProjectsList();
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Image");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        TextField filenameField = new TextField("Filename", image.getFilename(), "");
        ComboBox<Project> projectField = new ComboBox<>("Project", projectList);
        projectField.setItemLabelGenerator(Project::getName);
        projectField.setValue(image.getProject());
        ComboBox<ImageStatus> statusField = new ComboBox<>("Status", imageStatusList);
        statusField.setValue(image.getStatus());


        ComboBox<Image> parentImageField = new ComboBox<>("Parent Image", imageService.findAllImages());
        parentImageField.setItemLabelGenerator(Image::getFilename);
        parentImageField.setValue(image.getParentImage());

        ComboBox<ClassifierCategory> classifierCategoryField
                = new ComboBox<>("Classifier Category", categoryService.getAllClassifierCategoryByProjectIn(projectList));
        classifierCategoryField.setItemLabelGenerator(ClassifierCategory::getName);
        classifierCategoryField.setValue(image.getClassifierCategory());



        Button saveButton = new Button("Save", event -> {
            image.setFilename(filenameField.getValue());
            image.setProject(projectField.getValue());
            image.setStatus(statusField.getValue());
            image.setParentImage(parentImageField.getValue());
            image.setParentFilename(image.getFilename());
            image.setClassifierCategory(classifierCategoryField.getValue());
            imageService.updateImage(image);
            imageGrid.getDataProvider().refreshAll();
            dialog.close();
            Notification.show("Image updated", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        dialog.add(new VerticalLayout(filenameField, projectField, statusField,parentImageField,classifierCategoryField, buttons));
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

        ComboBox<Project> projectField = new ComboBox<>("Project", projectService.findAllProjects());
        projectField.setItemLabelGenerator(Project::getName);

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
