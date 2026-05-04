package taskmanager.api;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class SmartTaskManagerFrame extends JFrame {

    private final TaskManager taskManager;
    private final TaskService taskService;
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

        updateWeatherButton = new JButton("Update Weather");

        JButton addButton = new JButton("Add Task");
        JButton deleteButton = new JButton("Delete Task");
        JButton suggestButton = new JButton("Suggest Schedule");

        updateWeatherButton.setEnabled(false);

        statusLabel = new JLabel("Ready");

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateWeatherButton);
        buttonPanel.add(suggestButton);

        add(buttonPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);

        loadTasks();

        taskTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            updateWeatherButton.setEnabled(selectedRow >= 0);
        });

        addButton.addActionListener(e -> {

            JTextField titleField = new JTextField();
            JTextField dateField = new JTextField("2025-01-01");
            JCheckBox weatherCheck = new JCheckBox("Weather Sensitive");

            JPanel panel = new JPanel(new java.awt.GridLayout(0, 1));
            panel.add(new JLabel("Task Title:"));
            panel.add(titleField);
            panel.add(new JLabel("Date (yyyy-MM-dd):"));
            panel.add(dateField);
            panel.add(weatherCheck);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "Add Task",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result == JOptionPane.OK_OPTION) {

                String title = titleField.getText();
                if (title == null || title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Title cannot be empty");
                    return;
                }

                try {
                    LocalDate dateOnly = LocalDate.parse(dateField.getText());
                    LocalDateTime date = dateOnly.atTime(12, 0);

                    String id = "task-" + System.currentTimeMillis();
                    boolean isWeatherSensitive = weatherCheck.isSelected();

                    Task task = new Task(id, title, date, isWeatherSensitive);

                    taskManager.addTask(task);
                    loadTasks();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date format (yyyy-MM-dd)");
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow >= 0) {
                String taskId = (String) tableModel.getValueAt(selectedRow, 0);
                taskManager.removeTask(taskId);
                loadTasks();
            }
        });

        updateWeatherButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow < 0) return;

            String taskId = (String) tableModel.getValueAt(selectedRow, 0);
            updateWeatherForTask(taskId);
        });

        suggestButton.addActionListener(e -> {
            schedulePlanner.suggestScheduleForLocation(taskManager.getTasks(), "Jeddah")
                    .subscribe(recommendations -> {
                        SwingUtilities.invokeLater(() -> {
                            StringBuilder result = new StringBuilder();

                            for (ScheduleRecommendation r : recommendations) {
                                result.append(r.task().getTitle())
                                      .append(" -> ")
                                      .append(r.recommendation())
                                      .append("\n");
                            }

                            JOptionPane.showMessageDialog(this, result.toString());
                        });
                    });
        });
    }

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
        Mono<WeatherForecast> forecastMono = taskManager.fetchWeather("Jeddah");

        forecastMono
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(forecast -> SwingUtilities.invokeLater(() -> {

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