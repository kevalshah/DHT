package algorithm;

public class NoPotentialIMSException extends Exception {

    public NoPotentialIMSException() {

    }

    public NoPotentialIMSException(String message) {
        super(message);
    }

    public NoPotentialIMSException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoPotentialIMSException(Throwable cause) {
        super(cause);
    }
}
