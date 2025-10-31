package com.example.examplefeature.ui;

import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
// START: NOVAS IMPORTAÇÕES ESSENCIAIS PARA QR CODE
import com.example.examplefeature.QRCodeGenerator;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
// END: NOVAS IMPORTAÇÕES

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("")
@PageTitle("Task List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
class TaskListView extends Main {

    private final TaskService taskService;

    final TextField description;
    final DatePicker dueDate;
    final Button createBtn;
    final Grid<Task> taskGrid;

    TaskListView(TaskService taskService) {
        this.taskService = taskService;

        description = new TextField();
        description.setPlaceholder("What do you want to do?");
        description.setAriaLabel("Task description");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");

        dueDate = new DatePicker();
        dueDate.setPlaceholder("Due date");
        dueDate.setAriaLabel("Due date");

        createBtn = new Button("Create", event -> createTask());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        taskGrid = new Grid<>();
        taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        taskGrid.addColumn(Task::getDescription).setHeader("Description");
        taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate()).map(dateFormatter::format).orElse("Never"))
                .setHeader("Due Date");
        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate())).setHeader("Creation Date");

        // START: ADICIONAR COLUNA DO BOTÃO QR CODE
        taskGrid.addComponentColumn(task -> {
            Button qrButton = new Button("QR");
            qrButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            qrButton.setTooltipText("Generate QR Code");
            qrButton.addClickListener(event -> showQrCodeDialog(task)); // LIGAÇÃO AO MÉTODO
            return qrButton;
        }).setHeader("QR").setWidth("5em").setFlexGrow(0);
        // END: ADICIONAR COLUNA DO BOTÃO QR CODE

        taskGrid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Task List", ViewToolbar.group(description, dueDate, createBtn)));
        add(taskGrid);
    }

    private void createTask() {
        taskService.createTask(description.getValue(), dueDate.getValue());
        taskGrid.getDataProvider().refreshAll();
        description.clear();
        dueDate.clear();
        Notification.show("Task added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    // START: MÉTODO showQrCodeDialog PARA APRESENTAÇÃO
    private void showQrCodeDialog(Task task) {

        // 1. Definir o conteúdo do QR Code (usando campos da classe Task)
        String taskData = String.format("Task ID: %d | Description: %s | Due: %s",
                task.getId(),
                task.getDescription(),
                task.getDueDate() != null ? task.getDueDate().toString() : "No Due Date");

        // 2. Criar o StreamResource
        StreamResource resource = new StreamResource("qrcode-" + task.getId() + ".png", () -> {
            try {
                // Chama a sua lógica de geração
                return new ByteArrayInputStream(QRCodeGenerator.generateQRCodeImage(taskData, 300, 300));
            } catch (Exception e) {
                Notification.show("Erro ao gerar QR Code: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                e.printStackTrace();
                return new ByteArrayInputStream(new byte[0]);
            }
        });

        // 3. Apresentar o QR Code num Dialog
        Image qrImage = new Image(resource, "QR Code for task");
        qrImage.setMaxWidth("300px");
        qrImage.setMaxHeight("300px");

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("QR Code: " + task.getDescription());
        dialog.add(qrImage);
        dialog.open();
    }
    // END: MÉTODO showQrCodeDialog PARA APRESENTAÇÃO
}