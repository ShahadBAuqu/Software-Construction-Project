package taskmanager.impl;

import taskmanager.api.TaskManager;

/**
 * Builder implementation for TaskManager.
 */
public class DefaultTaskManagerBuilder implements TaskManager.TaskManagerBuilder {

    private String apiKey;
    private String storagePath;

    /**
     * Sets weather API key.
     * Preconditions: apiKey must not be null
     * Postconditions: apiKey is stored
     * Side effects: none
     */
    @Override
    public TaskManager.TaskManagerBuilder withWeatherApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Sets optional storage path.
     * Preconditions: none
     * Postconditions: path is stored
     * Side effects: none
     */
    @Override
    public TaskManager.TaskManagerBuilder withStoragePath(String path) {
        this.storagePath = path;
        return this;
    }

    /**
     * Builds the TaskManager instance.
     * Preconditions: apiKey should be provided
     * Postconditions: returns a configured TaskManager
     * Side effects: creates new object
     */
    @Override
    public TaskManager build() {
        return new DefaultTaskManager(apiKey);
    }
}
