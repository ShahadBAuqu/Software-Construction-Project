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
        this.taskService = new TaskService(); 
        this.schedulePlanner = new SchedulePlanner();

        setTitle("Smart Task Manager (Reactive Edition)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);

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

        taskTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            updateWeatherButton.setEnabled(selectedRow >= 0);
        });

        updateWeatherButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow < 0) return;

            String taskId = (String) tableModel.getValueAt(selectedRow, 0);
            updateWeatherForTask(taskId);
        });

        loadTasks();
    }

    private void loadTasks() {
        taskManager.getAllTasks()
            .collectList()
            .subscribeOn(Schedulers.boundedElastic())
            .publishOn(Schedulers.fromExecutor(SwingUtilities::invokeLater))
            .subscribe(this::populateTable, error -> statusLabel.setText("Error loading tasks"));
    }

    private void populateTable(List<Task> tasks) {
        tableModel.setRowCount(0);
        for (Task t : tasks) {
            tableModel.addRow(new Object[]{
                t.id(), 
                t.title(), 
                t.dueDateTime(),
                t.weatherSensitive(), 
                "Pending"
            });
        }
    }

    private void updateWeatherForTask(String taskId) {
        statusLabel.setText("Fetching weather for task: " + taskId);
        
        taskManager.fetchWeather("Riyadh")
            .subscribeOn(Schedulers.boundedElastic())
            .publishOn(Schedulers.fromExecutor(SwingUtilities::invokeLater))
            .subscribe(forecast -> {
                String status = forecast.getPrecipitationProbability() > 0.5 ? "RISKY (Rain)" : "SAFE";
                updateTaskStatusInTable(taskId, status);
                statusLabel.setText("Weather updated.");
            }, error -> statusLabel.setText("Weather fetch failed."));
    }

    private void updateTaskStatusInTable(String taskId, String status) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(taskId)) {
                tableModel.setValueAt(status, i, 4);
                break;
            }
        }
    }
}