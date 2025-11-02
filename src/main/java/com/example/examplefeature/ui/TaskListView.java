package com.example.examplefeature.ui;

import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
// --- IMPORTAÇÕES PARA QR CODE ---
import com.example.examplefeature.QRCodeGenerator;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
// --- FIM IMPORTAÇÕES QR CODE ---

// --- IMPORTAÇÕES PARA CÂMBIO DE MOEDA ---
import com.example.ExchangeRateService;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
// --- FIM IMPORTAÇÕES CÂMBIO ---

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

    // --- Variáveis de Serviço ---
    private final TaskService taskService;
    // (Adicionada para a Ficha 2, Tarefa C)
    private final ExchangeRateService exchangeService = new ExchangeRateService();

    // --- Componentes da UI (Tarefas) ---
    final TextField description;
    final DatePicker dueDate;
    final Button createBtn;
    final Grid<Task> taskGrid;

    TaskListView(TaskService taskService) {
        this.taskService = taskService;

        // --- UI de Criação de Tarefas ---
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

        // --- Configuração da Grelha (Grid) de Tarefas ---
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        taskGrid = new Grid<>();
        taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        taskGrid.addColumn(Task::getDescription).setHeader("Description");
        taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate()).map(dateFormatter::format).orElse("Never"))
                .setHeader("Due Date");
        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate())).setHeader("Creation Date");

        // Coluna do Botão QR Code
        taskGrid.addComponentColumn(task -> {
            Button qrButton = new Button("QR");
            qrButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            qrButton.setTooltipText("Generate QR Code");
            qrButton.addClickListener(event -> showQrCodeDialog(task)); // LIGAÇÃO AO MÉTODO
            return qrButton;
        }).setHeader("QR").setWidth("5em").setFlexGrow(0);

        taskGrid.setSizeFull();

        // --- Layout Principal ---
        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Task List", ViewToolbar.group(description, dueDate, createBtn)));
        add(taskGrid);

        // --- INÍCIO: Funcionalidade de Câmbio de Moeda (Ficha 2, Tarefa C) ---

        H3 title = new H3("Conversor de Moeda");

        TextField amountField = new TextField("Valor a converter (EUR)");
        amountField.setPlaceholder("10.00");

        Select<String> toCurrency = new Select<>();
        toCurrency.setLabel("Converter para");
        toCurrency.setItems("USD", "BRL", "GBP", "JPY"); // Moedas de exemplo
        toCurrency.setValue("USD"); // Valor por defeito

        Button convertButton = new Button("Converter");
        Paragraph resultDisplay = new Paragraph("Resultado:");

        HorizontalLayout converterLayout = new HorizontalLayout(
                amountField,
                toCurrency,
                convertButton
        );
        converterLayout.setAlignItems(Alignment.BASELINE);

        convertButton.addClickListener(click -> {
            try {
                double amount = Double.parseDouble(amountField.getValue());
                String from = "EUR"; // Fixo, por exemplo
                String to = toCurrency.getValue();

                // Chamar o serviço de backend
                double convertedValue = exchangeService.convert(from, to, amount);

                if (convertedValue != -1.0) {
                    resultDisplay.setText(String.format(
                            "Resultado: %.2f %s = %.2f %s",
                            amount, from, convertedValue, to
                    ));
                } else {
                    resultDisplay.setText("Erro: Não foi possível converter.");
                }
            } catch (NumberFormatException e) {
                resultDisplay.setText("Erro: 'Valor' tem de ser um número.");
            }
        });

        // Adicionar os novos componentes à vista principal
        add(
                title,
                converterLayout,
                resultDisplay
        );

        // --- FIM: Funcionalidade de Câmbio de Moeda ---
    }

    private void createTask() {
        taskService.createTask(description.getValue(), dueDate.getValue());
        taskGrid.getDataProvider().refreshAll();
        description.clear();
        dueDate.clear();
        Notification.show("Task added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    // --- Método showQrCodeDialog PARA APRESENTAÇÃO ---
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
}