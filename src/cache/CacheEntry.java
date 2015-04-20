package cache;

import java.util.Arrays;

public class CacheEntry {

    public static final long VALID_PERIOD = 15000;

    private long timestamp;
    private byte[] uniqueID;

    public CacheEntry(byte[] uniqueID){
        this.uniqueID = uniqueID;
        timestamp = System.currentTimeMillis();
    }

    public boolean isExpired(){
        return (System.currentTimeMillis() - timestamp >= VALID_PERIOD);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }

        if(!(obj instanceof CacheEntry)) { return false; }

        if (this == obj) { return true; }

        if(this == null || obj == null) {
            return false;
        }

        final CacheEntry other = (CacheEntry) obj;
        if(Arrays.equals(other.uniqueID, this.uniqueID)) {
            return true;
        } else {
            return false;
        }

    }
}
