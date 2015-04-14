package cache;

public class CacheEntry {

    public static final long VALID_PERIOD = 15000;

    private long timestamp;
    private String msg_id;

    public CacheEntry(byte[] msg_id){
        this.msg_id = "";
        for (Byte b : msg_id){
            this.msg_id = this.msg_id + b.toString();
        }
        timestamp = System.currentTimeMillis();
    }

    public boolean isExpired(){
        return (timestamp + VALID_PERIOD < System.currentTimeMillis());
    }

    public String getId(){
        return this.msg_id;
    }
}
