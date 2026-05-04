package taskmanager.api;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class SmartTaskManagerFrame extends JFrame {

    private final TaskManager taskManager;
    private final TaskService taskService;  // to be initialized from taskManager.impl
    private final SchedulePlanner schedulePlanner;

    private final JTable taskTable;
    private final DefaultTableModel tableModel;
    private final JButton updateWeatherButton;
    private final JLabel statusLabel;

    private final String[] columnNames = {"ID", "Title", "Due Time", "Weather Sensitive", "Status"};

    public SmartTaskManagerFrame(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.taskService = new taskmanager.impl.TaskServiceImpl();
        this.schedulePlanner = taskManager.getPlanner();

        setTitle("Smart Task Manager (Swing)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 400);

        tableModel = new DefaultTableModel(columnNames, 0);
        taskTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(taskTable);

        updateWeatherButton = new JButton("Update Weather for Selected Task");
        updateWeatherButton.setEnabled(false);

        statusLabel = new JLabel("Ready");

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(updateWeatherButton, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);

        // Initialization: load tasks
        loadTasks();

        // Wiring: select row → enable weather button
        taskTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            updateWeatherButton.setEnabled(selectedRow >= 0);
        });

        // “Update Weather” clicked
        updateWeatherButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow < 0) return;

            String taskId = (String) tableModel.getValueAt(selectedRow, 0);
            updateWeatherForTask(taskId);
        });
    }

    // ... Keep your existing loadTasks, populateTable, and updateWeatherForTask methods ...
    private void loadTasks() {
        Mono.just(taskManager.getTasks())
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(tasks -> SwingUtilities.invokeLater(() -> populateTable(tasks)))
                .subscribe();
    }


        private void populateTable(List<Task> tasks) {
        tableModel.setRowCount(0);
        for (Task t : tasks) {
            tableModel.addRow(new Object[]{t.getId(), t.getTitle(), t.getDueDateTime(), t.isWeatherSensitive(), "N/A"});
        }
    }

    private void updateWeatherForTask(String taskId) {
        Mono<WeatherForecast> forecastMono = taskManager.fetchWeather("Jeddah");  // fixed city

        forecastMono
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(forecast -> SwingUtilities.invokeLater(() -> {
                    // Simple weather‑aware status logic
                    Task selectedTask = taskManager.getTasks()
                             .stream()
                             .filter(t -> t.getId().equals(taskId))
                             .findFirst()
                             .orElse(null);

                    String status;

                    if (selectedTask != null && selectedTask.isWeatherSensitive()
                             && forecast.getPrecipitationProbability() > 0.6) {

                         status = "RISKY (rain)";

                    } else {
                         status = "SAFE";
                    }

                    updateTaskStatusInTable(taskId, status);
                    statusLabel.setText("Weather updated for task: " + taskId);
                }))
                .doOnError(error -> SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Weather fetch failed: " + error.getMessage());
                }))
                .subscribe();
    }




    private void updateTaskStatusInTable(String taskId, String status) {
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String idInTable = (String) tableModel.getValueAt(i, 0);
            if (idInTable.equals(taskId)) {
                tableModel.setValueAt(status, i, 4);
                break;
            }
        }
    }
}