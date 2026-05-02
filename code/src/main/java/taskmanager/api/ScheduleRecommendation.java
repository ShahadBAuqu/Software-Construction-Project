package taskmanager.api;

import java.util.List;


public record ScheduleRecommendation(List<Task> tasks, String recommendation) {
    // This structure allows the UI to display multiple suggested tasks at once.
}