package taskmanager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taskmanager.api.InvalidTaskException;
import taskmanager.api.Task;
import taskmanager.api.TaskNotFoundException;
import taskmanager.api.TaskService;

/**
 * Implementation of TaskService.
 * Handles all task-related operations in a reactive way.
 */
public class TaskServiceImpl implements TaskService {

    // In-memory storage for tasks (id -> task)
    private final Map<String, Task> tasks = new HashMap<>();

    /**
     * Adds a new task.
     *
     * Preconditions:
     * - task must not be null
     * - task id must not already exist
     *
     * @param task the task to add
     * @return Mono<Void> completes when task is added
     * @throws InvalidTaskException if task is null or invalid
     */
    @Override
    public Mono<Void> addTask(Task task) {
        return Mono.fromRunnable(() -> {
            if (task == null)
                throw new InvalidTaskException("Task cannot be null");

            if (tasks.containsKey(task.getId()))
                throw new InvalidTaskException("Task with same ID already exists");

            tasks.put(task.getId(), task);
        });
    }

    /**
     * Removes a task by ID.
     *
     * Preconditions:
     * - taskId must exist
     *
     * @param taskId the ID of the task to remove
     * @return Mono<Void> completes when task is removed
     * @throws TaskNotFoundException if task does not exist
     */
    @Override
    public Mono<Void> removeTask(String taskId) {
        return Mono.fromRunnable(() -> {
            if (!tasks.containsKey(taskId))
                throw new TaskNotFoundException(taskId);

            tasks.remove(taskId);
        });
    }

    /**
     * Finds a task by its ID.
     *
     * Preconditions:
     * - taskId must exist
     *
     * @param taskId the task ID
     * @return Mono<Task> containing the found task
     * @throws TaskNotFoundException if task not found
     */
    @Override
    public Mono<Task> findTaskById(String taskId) {
        return Mono.fromSupplier(() -> {
            Task t = tasks.get(taskId);
            if (t == null)
                throw new TaskNotFoundException(taskId);
            return t;
        });
    }

    /**
     * Returns all tasks as a reactive stream.
     *
     * @return Flux<Task> stream of all tasks
     */
    @Override
    public Flux<Task> findAllTasks() {
        return Flux.fromIterable(tasks.values());
    }

    /**
     * Returns all tasks as a list.
     *
     * @return Mono<List<Task>> containing all tasks
     */
    @Override
    public Mono<List<Task>> findAllTasksAsList() {
        return Mono.fromSupplier(() -> new ArrayList<>(tasks.values()));
    }
}
