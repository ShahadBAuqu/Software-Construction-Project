package taskmanager.api;


public class WeatherAPIException extends Exception {
    
    /**
     * @param message Detailed error message.
     * @param cause The underlying reason (like a network timeout).
     */
    public WeatherAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public WeatherAPIException(String message) {
        super(message);
    }
}