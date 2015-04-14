package kvstore;


public class KVStoreKeyNotFoundException extends Exception {

    public KVStoreKeyNotFoundException(String message) {
        super(message);
    }

    public KVStoreKeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public KVStoreKeyNotFoundException(Throwable cause) {
        super(cause);
    }
}
