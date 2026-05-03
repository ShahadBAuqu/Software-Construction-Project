package taskmanager.impl;

import java.util.List;
import java.util.ArrayList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import taskmanager.api.Task;
import taskmanager.api.TaskNotFoundException;
import taskmanager.api.InvalidTaskException;

/**
 * TaskServiceImpl provides the core reactive logic for task management.
 * Uses Project Reactor (Mono/Flux) for all operations.
 * No UI or Swing code — pure API logic only.
 */
public class TaskServiceImpl {

    // قائمة لتخزين المهام
    private final List<Task> tasks = new ArrayList<>();

    /**
     * Adds a new task to the system.
     * Precondition: task must not be null.
     * Postcondition: task is stored in the list.
     * @param task the task to add
     * @return Mono<Void> completes when task is added
     */
    public Mono<Void> addTask(Task task) {
        return Mono.fromRunnable(() -> {
            if (task == null || task.id() == null || task.id().isBlank()) {
                throw new InvalidTaskException("Task or Task ID cannot be null or empty.");
            }
            tasks.add(task);
        });
    }

    /**
     * Removes a task by its ID.
     * Precondition: taskId must exist in the list.
     * Postcondition: task is removed from the list.
     * @param taskId the unique ID of the task to remove
     * @return Mono<Void> completes after removal
     */
    public Mono<Void> removeTask(String taskId) {
        return Mono.fromRunnable(() -> {
            boolean removed = tasks.removeIf(t -> t.id().equals(taskId));
            if (!removed) {
                throw new RuntimeException("Task not found: " + taskId);
            }
        });
    }

    /**
     * Finds a task by its ID.
     * @param taskId the unique ID to search for
     * @return Mono<Task> emitting the found task, or error if not found
     */
    public Mono<Task> findTaskById(String taskId) {
        return Flux.fromIterable(tasks)
                   .filter(t -> t.id().equals(taskId))
                   .next()
                   .switchIfEmpty(Mono.error(new RuntimeException("Task not found: " + taskId)));
    }

    /**
     * Returns all tasks as a reactive stream.
     * @return Flux<Task> emitting all tasks one by one
     */
    public Flux<Task> findAllTasks() {
        return Flux.fromIterable(tasks);
    }

    /**
     * Returns all tasks collected into a single List.
     * @return Mono<List<Task>> emitting the full list at once
     */
    public Mono<List<Task>> findAllTasksAsList() {
        return Flux.fromIterable(tasks)
                   .collectList();
    }
}



