package cache;


import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheController {


    private static CacheController singletonInstance;
    private ArrayList<CacheEntry> cache;
    private int cacheSize;

    private ReentrantReadWriteLock cacheListLock;


    protected CacheController(int cacheSize) {
        cache = new ArrayList<CacheEntry>();
        cacheListLock = new ReentrantReadWriteLock();
        this.cacheSize = cacheSize;
    }


    public static CacheController getInstance() {
        if(singletonInstance == null) {
            singletonInstance = new CacheController(50);
        }

        return singletonInstance;
    }


    public CacheEntry isUniqueIDInCache(byte[] uniqueID) {
        cacheListLock.readLock().lock();
        try {
            CacheEntry cacheEntryToCheck = new CacheEntry(uniqueID);
            for(CacheEntry cacheEntry : cache) {
                if(cacheEntry.equals(cacheEntryToCheck)) {
                    return cacheEntry;
                }
            }
            return null;
        } finally {
            cacheListLock.readLock().unlock();
        }
    }


    public void addToCache(byte[] uniqueID) {

        removeAllExpiredEntries();

        cacheListLock.writeLock().lock();
        try {
            if(cache.size() >= cacheSize) {
                cache.remove(0);
            }
            cache.add(new CacheEntry(uniqueID));
        } finally {
            cacheListLock.writeLock().unlock();
        }
    }




    public void removeAllExpiredEntries() {
        cacheListLock.writeLock().lock();
        try {
            for(CacheEntry cacheEntry : cache) {
                if(cacheEntry.isExpired()) {
                    cache.remove(cacheEntry);
                }
            }
        } finally {
            cacheListLock.writeLock().unlock();
        }
    }



}
