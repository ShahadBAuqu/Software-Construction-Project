package taskmanager.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private final String apiKey;

    /**
     * Constructs the TaskManager with required dependencies.
     *
     * @param apiKey the API key used for weather fetching
     */
    public DefaultTaskManager(String apiKey) {
        this.taskService = new TaskServiceImpl();
        this.planner = new SchedulePlannerImpl(this);
        this.apiKey = apiKey;
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
     *
     * @param location the city/location name
     * @return Mono containing WeatherForecast
     * @throws WeatherAPIException if fetching fails
     */
    
    @Override
    public Mono<WeatherForecast> fetchWeather(String location) {

    String url =
        "https://api.openweathermap.org/data/2.5/weather?q="
        + location
        + "&appid="
        + apiKey
        + "&units=metric";

    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

    return Mono.fromFuture(
            client.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            )
    )
    .map(HttpResponse::body)
    .map(response -> {

        try {

            double temperature = 25.0;
            String condition = "Clear";
            double precipitation = 0.0;

            if (response.contains("\"all\":")) {

                try {

                    String precipitationPart =
                            response.split("\"all\":")[1]
                                    .split("}")[0]
                                    .trim();

                    precipitation =
                            Double.parseDouble(precipitationPart) / 100.0;

                } catch (Exception e) {

                    precipitation = 0.0;

                }
            }

            if (response.contains("\"temp\":")) {

                String tempPart =
                        response.split("\"temp\":")[1]
                                .split(",")[0];

                temperature =
                        Double.parseDouble(tempPart);

            }

            if (response.contains("\"main\":\"")) {

                String conditionPart =
                        response.split("\"main\":\"")[1]
                                .split("\"")[0];

                condition = conditionPart;

            }

            WeatherForecast forecast =
                    new WeatherForecast(
                            location,
                            java.time.LocalDateTime.now(),
                            temperature,
                            condition,
                            precipitation
                    );

            return forecast;

        } catch (Exception e) {

            throw new WeatherAPIException(
                    "Failed to parse weather data",
                    e
            );

        }

    })
    .onErrorMap(error ->
            new WeatherAPIException(
                    "Failed to fetch weather data",
                error
            )
    );

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
