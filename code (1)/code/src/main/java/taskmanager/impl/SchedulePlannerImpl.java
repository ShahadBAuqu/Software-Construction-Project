package taskmanager.impl;

import taskmanager.api.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of SchedulePlanner.
 * Provides weather-based scheduling recommendations.
 */
public class SchedulePlannerImpl implements SchedulePlanner {

    private final TaskManager taskManager;

    /**
     * Constructs planner with access to TaskManager.
     *
     * @param taskManager the main task manager
     */
    public SchedulePlannerImpl(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    /**
     * Suggests schedule recommendations based on weather.
     * Preconditions: tasks and forecast must not be null
     * Postconditions: returns a list of recommendations
     * Side effects: none
     *
     * @param tasks list of tasks
     * @param forecast weather forecast
     * @return Mono<List<ScheduleRecommendation>>
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestSchedule(List<Task> tasks, WeatherForecast forecast) {
        return Mono.fromSupplier(() -> {
            if (tasks == null || forecast == null) {
                throw new InvalidTaskException("Tasks or forecast cannot be null");
            }

            List<ScheduleRecommendation> result = new ArrayList<>();

            for (Task t : tasks) {
                String rec;

                if (t.isWeatherSensitive() && forecast.getPrecipitationProbability() > 0.6) {
                    rec = "Not recommended";
                } else {
                    rec = "Good to go";
                }

                result.add(new ScheduleRecommendation(t, rec));
            }

            return result;
        });
    }

    /**
     * Suggests schedule using location (fetches weather internally).
     * Preconditions: tasks and location must not be null
     * Postconditions: returns recommendations based on fetched weather
     * Side effects: calls weather API
     *
     * @param tasks list of tasks
     * @param location location name
     * @return Mono<List<ScheduleRecommendation>>
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestScheduleForLocation(List<Task> tasks, String location) {
        return taskManager.fetchWeather(location)
                .flatMap(forecast -> suggestSchedule(tasks, forecast));
    }
}
