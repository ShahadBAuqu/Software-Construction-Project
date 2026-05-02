package taskmanager.api;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class TaskService { 

    public Mono<Void> addTask(Task task) { 
        return Mono.empty(); 
    }

    public Mono<Void> removeTask(String taskId) { 
        return Mono.empty(); 
    }

    public Mono<Task> findTaskById(String taskId) { 
        return Mono.empty(); 
    }

    public Flux<Task> findAllTasks() { 
        return Flux.empty(); 
    }

    public Mono<List<Task>> findAllTasksAsList() { 
        return Mono.empty(); 
    }
}