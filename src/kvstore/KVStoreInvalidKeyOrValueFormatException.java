package kvstore;

public class KVStoreInvalidKeyOrValueFormatException extends Exception {

    public KVStoreInvalidKeyOrValueFormatException(String message) {
        super(message);
    }

    public KVStoreInvalidKeyOrValueFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public KVStoreInvalidKeyOrValueFormatException(Throwable cause) {
        super(cause);
    }
}
