package main;

public class RetriesExhaustedException extends Exception {

    public RetriesExhaustedException(String message) {
        super(message);
    }

    public RetriesExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetriesExhaustedException(Throwable cause) {
        super(cause);
    }
}
