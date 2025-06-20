package kz.moon.app.taskmanagement.ui.view;

import kz.moon.app.base.ui.component.ViewToolbar;
import kz.moon.app.taskmanagement.domain.Task;
import kz.moon.app.taskmanagement.service.TaskService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.annotation.Secured;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Secured({"ROLE_USER", "ROLE_MANAGER"})
@Route("task-list")
@PageTitle("Task List")
@PermitAll
public class TaskListView extends Main {

    private final TaskService taskService;
    private final Clock clock;

    private final TextField description;
    private final DatePicker dueDate;
    private final Button createBtn;
    private final Grid<Task> taskGrid = new Grid<>(Task.class, false);
    private final TextField filterField;

    public TaskListView(TaskService taskService, Clock clock) {
        this.taskService = taskService;
        this.clock = clock;

        description = new TextField();
        description.setPlaceholder("What do you want to do?");
        description.setAriaLabel("Task description");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");
        description.addKeyPressListener(Key.ENTER, e -> createTask());

        dueDate = new DatePicker();
        dueDate.setPlaceholder("Due date");
        dueDate.setAriaLabel("Due date");

        createBtn = new Button("Create", event -> createTask());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        filterField = new TextField();
        filterField.setPlaceholder("Filter by description...");
        filterField.setClearButtonVisible(true);
        filterField.setWidthFull();
        filterField.addValueChangeListener(e -> taskGrid.getDataProvider().refreshAll());

        configureGrid();
        setupDataProvider();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Task List", ViewToolbar.group(description, dueDate, createBtn)));
        add(filterField);
        add(taskGrid);
    }

    private void configureGrid() {
        taskGrid.addColumn(Task::getDescription)
                .setHeader("Description")
                .setAutoWidth(true)
                .setSortable(true);

        taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate())
                        .map(LocalDate::toString)
                        .orElse("Never"))
                .setHeader("Due Date")
                .setAutoWidth(true)
                .setSortable(true);

        taskGrid.addColumn(task -> task.getCreationDate().toString())
                .setHeader("Creation Date")
                .setAutoWidth(true)
                .setSortable(true);

        taskGrid.addComponentColumn(task -> {
            Button editButton = new Button("Edit", click -> editTask(task));
            Button deleteButton = new Button("Delete", click -> deleteTask(task));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions");

        taskGrid.setPageSize(10);
        taskGrid.setSizeFull();
    }

    private void setupDataProvider() {
        CallbackDataProvider<Task, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();

                    String sortBy = query.getSortOrders().isEmpty() ? "creationDate"
                            : query.getSortOrders().get(0).getSorted();
                    boolean asc = query.getSortOrders().isEmpty()
                            || query.getSortOrders().get(0).getDirection() == com.vaadin.flow.data.provider.SortDirection.ASCENDING;

                    return taskService.find(
                            filterField.getValue(),
                            offset, limit, sortBy, asc
                    ).stream();
                },
                query -> (int) taskService.count(
                        filterField.getValue()
                )
        );

        taskGrid.setDataProvider(dataProvider);
    }

    private void createTask() {
        taskService.createTask(description.getValue(), dueDate.getValue());
        taskGrid.getDataProvider().refreshAll();
        description.clear();
        dueDate.clear();
        Notification.show("Task added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void editTask(Task task) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Task");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        TextField descriptionField = new TextField("Description", task.getDescription(), "");
        DatePicker dueDateField = new DatePicker("Due Date", task.getDueDate());

        Button saveButton = new Button("Save", event -> {
            task.setDescription(descriptionField.getValue());
            task.setDueDate(dueDateField.getValue());
            taskService.updateTask(task);
            taskGrid.getDataProvider().refreshAll();
            dialog.close();
            Notification.show("Task updated", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(new VerticalLayout(descriptionField, dueDateField, buttons));
        dialog.open();
    }

    private void deleteTask(Task task) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");
        confirmDialog.setCloseOnEsc(true);
        confirmDialog.setCloseOnOutsideClick(true);

        Span text = new Span("Are you sure you want to delete task: " + task.getDescription() + " ?");

        Button deleteButton = new Button("Delete", event -> {
            taskService.deleteTask(task.getId());
            taskGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            Notification.show("Task deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancel", event -> confirmDialog.close());
        HorizontalLayout buttons = new HorizontalLayout(deleteButton, cancelButton);

        confirmDialog.add(new VerticalLayout(text, buttons));
        confirmDialog.open();
    }
}
