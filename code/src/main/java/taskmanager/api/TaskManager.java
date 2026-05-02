package taskmanager.api;

import java.util.ArrayList;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TaskManager { 

    // Local storage for tasks to make the app functional for now
    private final List<Task> tasks = new ArrayList<>();

    /**
     * Adds a new task to the system.
     * @param task The task object to be added.
     * @return A Mono emitting the added task.
     */
    public Mono<Task> addTask(Task task) {
        tasks.add(task);
        return Mono.just(task);
    }

    /**
     * Removes a task from the system by its ID.
     * @param taskId The unique identifier of the task.
     * @return A Mono indicating completion.
     */
    public Mono<Void> removeTask(String taskId) {
        tasks.removeIf(t -> t.id().equals(taskId));
        return Mono.empty();
    }

    /**
     * Retrieves all tasks currently in the system.
     * @return A Flux emitting all tasks.
     */
    public Flux<Task> getAllTasks() {
        return Flux.fromIterable(tasks);
    }

    /**
     * Helper method to get the list size (used in MainApp)
     * @return List of tasks
     */
    public List<Task> getTasks() {
        return tasks;
    }

    /**
     * Fetches weather information for a specific location.
     * @param location The city or area name.
     * @return A Mono containing weather data.
     */
    public Mono<WeatherForecast> fetchWeather(String location) {
        // Placeholder for weather fetching logic
        return Mono.empty();
    }
}