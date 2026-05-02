package taskmanager.api;

import java.time.LocalDateTime;

import javax.swing.SwingUtilities;

public class MainApp {

    public static void main(String[] args) {
      
        TaskManager tm = new TaskManager(); 

     
        Task task1 = new Task(
            "task-001",
            "Morning run",
            "Daily exercise", 
            LocalDateTime.now().plusHours(2),
            true
        );

        Task task2 = new Task(
            "task-002",
            "Coding session",
            "Final project work", 
            LocalDateTime.now().plusHours(4),
            false
        );

        tm.addTask(task1);
        tm.addTask(task2);

        System.out.println("Tasks loaded: " + tm.getTasks().size());

        // Wire this to the Swing UI
        SwingUtilities.invokeLater(() -> {
            SmartTaskManagerFrame frame = new SmartTaskManagerFrame(tm);
            frame.setVisible(true);
        });
    }
}