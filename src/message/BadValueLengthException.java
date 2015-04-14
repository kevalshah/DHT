package message;

public class BadValueLengthException extends Exception {

    public BadValueLengthException() {
    }

    public BadValueLengthException(String message) {
        super(message);
    }

    public BadValueLengthException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadValueLengthException(Throwable cause) {
        super(cause);
    }

    public BadValueLengthException(String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
