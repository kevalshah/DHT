package timestamp;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Timestamp {

    private static final long IMPS_TIMEOUT = 20000;
    private static final long IMS_TIMEOUT = 20000;
    private long predecessorCheckTimestamp;
    private ReentrantReadWriteLock predecessorCheckTimestampLock;

    private long successorCheckTimestamp;
    private ReentrantReadWriteLock successorCheckTimestampLock;


    private static Timestamp singletonTimestamp;

    protected Timestamp(){
        predecessorCheckTimestamp = -1;
        successorCheckTimestamp = -1;
        predecessorCheckTimestampLock = new ReentrantReadWriteLock();
        successorCheckTimestampLock = new ReentrantReadWriteLock();
    }

    public static Timestamp getInstance(){
        if(singletonTimestamp == null) {
            singletonTimestamp = new Timestamp();
        }
        return singletonTimestamp;
    }


    public long getPredecessorCheckTimestamp(){
        predecessorCheckTimestampLock.readLock().lock();
        try {
            return predecessorCheckTimestamp;
        } finally {
            predecessorCheckTimestampLock.readLock().unlock();
        }
    }


    public void setPredecessorCheckTimestamp() {
        predecessorCheckTimestampLock.writeLock().lock();
        predecessorCheckTimestamp = System.currentTimeMillis();
        predecessorCheckTimestampLock.writeLock().unlock();
    }


    public long getSuccessorCheckTimestamp(){
        successorCheckTimestampLock.readLock().lock();
        try {
            return successorCheckTimestamp;
        } finally {
            successorCheckTimestampLock.readLock().unlock();
        }
    }


    public void setSuccessorCheckTimestamp() {
        successorCheckTimestampLock.writeLock().lock();
        successorCheckTimestamp = System.currentTimeMillis();
        successorCheckTimestampLock.writeLock().unlock();
    }


    public boolean hasPredecessorTimestampExpired() {
        predecessorCheckTimestampLock.readLock().lock();
        try {
            return ((System.currentTimeMillis() - predecessorCheckTimestamp) >= IMPS_TIMEOUT);
        } finally {
            predecessorCheckTimestampLock.readLock().unlock();
        }
    }


    public boolean hasSuccessorTimestampExpired() {
        successorCheckTimestampLock.readLock().lock();
        try {
            return ((System.currentTimeMillis() - successorCheckTimestamp) >= IMS_TIMEOUT);
        } finally {
            successorCheckTimestampLock.readLock().unlock();
        }
    }


}
