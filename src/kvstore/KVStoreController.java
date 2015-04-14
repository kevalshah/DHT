package kvstore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages all operations on key-value data store
 */
public class KVStoreController {

    private final static int MAX_CAPACITY = 100;

    private ConcurrentMap<String, String> keyValueStore;
    private int maxCapacity;

    private static KVStoreController kvStoreController = null;

    
    protected KVStoreController(int capacity) {
        keyValueStore = new ConcurrentHashMap<String, String>();
        maxCapacity = capacity;
    }


    /**
     * Get singleton instance of KVStoreController
     * @return - singleton instance
     */
    public synchronized static KVStoreController getInstance() {
        if(kvStoreController == null) {
            kvStoreController = new KVStoreController(MAX_CAPACITY);
        }
        return kvStoreController;
    }


    /**
     * Checks to see if the key value store is full
     * @return - True if key value store is full, false otherwise.
     */
    public boolean isKVStoreFull() {
        return keyValueStore.size() >= maxCapacity;
    }


    /**
     * Gets the maximum capacity for key value store
     * @return - the max capacity
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }


    /**
     * Attempts to insert key-value pair into key value store
     * @param key - Key of key-value pair to insert
     * @param value - Value of key-value pair to insert
     * @throws KVStoreInvalidKeyOrValueFormatException - Thrown if key or value provided is null or empty
     * @throws KVStoreFullException - Thrown if key value store is full
     */
    public void put(String key, String value)
            throws KVStoreFullException, KVStoreInvalidKeyOrValueFormatException {

        // Check if key and/or value is not null or empty. If null or empty, throw exception.
        if(key == null || value == null || key.isEmpty() || value.isEmpty()) {
            throw new KVStoreInvalidKeyOrValueFormatException("Operation failed: Invalid format for key or value.");
        }

        // Check if key value store is full. If full, throw exception.
        if(isKVStoreFull()) {
            throw new KVStoreFullException("Operation failed: Key value store is full");
        }

        keyValueStore.put(key, value);
    }


    /**
     * Attempt to fetch the value for the input key from the key value store
     * @param key - Key of key-value pair to fetch
     * @return - Value of the associated key from key value store
     * @throws KVStoreKeyNotFoundException - Thrown if key does not exist in key value store
     */
    public String get(String key) throws KVStoreKeyNotFoundException {

        // Check if key exists in key value store. If not, throw exception.
        if(!keyValueStore.containsKey(key)) {
            throw new KVStoreKeyNotFoundException("Operation failed: Key does not exist in key value store");
        }

        return keyValueStore.get(key);
    }


    /**
     * Attempts to permanently remove a key-value pair from the key value store
     * @param key - Key of key-value pair to remove
     * @throws KVStoreKeyNotFoundException - Thrown if key does not exist in key value store
     */
    public void remove(String key) throws KVStoreKeyNotFoundException {

        // Check if key exists in key value store. If not, throw exception.
        if(!keyValueStore.containsKey(key)) {
            throw new KVStoreKeyNotFoundException("Operation failed: Key does not exist in key value store");
        }

        keyValueStore.remove(key);
    }


}
