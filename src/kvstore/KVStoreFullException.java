package kvstore;


public class KVStoreFullException extends Exception {

    public KVStoreFullException(String message) {
        super(message);
    }

    public KVStoreFullException(String message, Throwable cause) {
        super(message, cause);
    }

    public KVStoreFullException(Throwable cause) {
        super(cause);
    }
}

