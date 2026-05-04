package taskmanager.impl;

import java.time.LocalDateTime;
import java.util.List;

import reactor.core.publisher.Mono;
import taskmanager.api.InvalidTaskException;
import taskmanager.api.SchedulePlanner;
import taskmanager.api.Task;
import taskmanager.api.TaskManager;
import taskmanager.api.TaskNotFoundException;
import taskmanager.api.TaskService;
import taskmanager.api.WeatherAPIException;
import taskmanager.api.WeatherForecast;

/**
 * Default implementation of TaskManager.
 * Acts as the main facade that coordinates task operations and scheduling.
 */
public class DefaultTaskManager implements TaskManager {

    private final TaskService taskService;
    private final SchedulePlanner planner;

    /**
     * Constructs the TaskManager with required dependencies.
     *
     * @param apiKey the API key used for weather fetching
     */
    public DefaultTaskManager(String apiKey) {
        this.taskService = new TaskServiceImpl();
        this.planner = new SchedulePlannerImpl(this);
    }

    /**
     * Adds a new task to the system.
     * Preconditions: task must not be null
     * Postconditions: the task is stored and retrievable
     * Side effects: modifies internal task storage
     *
     * @param task the task to add
     * @throws InvalidTaskException if task is null or invalid
     */
    @Override
    public void addTask(Task task) {
        taskService.addTask(task).block();
    }

    /**
     * Removes a task from the system.
     * Preconditions: taskId must exist in the system
     * Postconditions: the task is removed from storage
     * Side effects: modifies internal task storage
     *
     * @param taskId the ID of the task
     * @throws TaskNotFoundException if task does not exist
     */
    @Override
    public void removeTask(String taskId) {
        taskService.removeTask(taskId).block();
    }

    /**
     * Retrieves all tasks.
     * Preconditions: none
     * Postconditions: returns a list containing all stored tasks
     * Side effects: none
     *
     * @return list of tasks
     */
    @Override
    public List<Task> getTasks() {
        return taskService.findAllTasksAsList().block();
    }

    /**
     * Fetches weather information asynchronously.
     * Preconditions: location must not be null or empty
     * Postconditions: returns a WeatherForecast wrapped in Mono
     * Side effects: may perform external API call (simulated here)
     *
     * @param location the city/location name
     * @return Mono containing WeatherForecast
     * @throws WeatherAPIException if fetching fails
     */
    @Override
    public Mono<WeatherForecast> fetchWeather(String location) {
        return Mono.fromSupplier(() -> {
            if (location == null || location.isEmpty()) {
                throw new WeatherAPIException("Invalid location", null);
            }

            return new WeatherForecast(
                    location,
                    LocalDateTime.now(),
                    30.0,
                    "Sunny",
                    0.8
            );
        });
    }

    /**
     * Returns the schedule planner instance.
     * Preconditions: none
     * Postconditions: returns a valid SchedulePlanner
     * Side effects: none
     *
     * @return SchedulePlanner instance
     */
    @Override
    public SchedulePlanner getPlanner() {
        return planner;
    }
}
