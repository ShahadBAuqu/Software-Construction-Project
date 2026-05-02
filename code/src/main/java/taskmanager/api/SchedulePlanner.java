package taskmanager.api;

import java.util.List;

import reactor.core.publisher.Mono;

public class SchedulePlanner { 

    public Mono<Void> scheduleTask(Task task) {
        return Mono.empty();
    }

    public Mono<List<Task>> getRecommendedSchedule() {
        return Mono.empty();
    }
}