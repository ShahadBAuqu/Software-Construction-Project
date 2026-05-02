package taskmanager.api;

import java.time.LocalDateTime;

/**
 * Represents a Task using Java Record (Immutable Data Object).
 * This structure automatically generates getters, constructor, equals, hashCode, and toString.
 * 
 * @param id Unique identifier for the task.
 * @param title Short title of the task.
 * @param description Detailed description.
 * @param dueDateTime Deadline for the task.
 * @param weatherSensitive Boolean flag if the task depends on weather conditions.
 */
public record Task(
    String id, 
    String title, 
    String description, 
    LocalDateTime dueDateTime, 
    boolean weatherSensitive
) {
    
}