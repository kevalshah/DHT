package timestamp;

import java.sql.Timestamp;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CheckAliveTimestamp {

    private Timestamp predecessorCheckTimestamp;
    private ReentrantReadWriteLock predecessorCheckTimestampLock;

    private Timestamp successorCheckTimestamp;
    private ReentrantReadWriteLock successorCheckTimestampLock;

    private static CheckAliveTimestamp singletonCheckAliveTimestamp;

    protected CheckAliveTimestamp(){
        predecessorCheckTimestamp = null;
        successorCheckTimestamp = null;
        predecessorCheckTimestampLock = new ReentrantReadWriteLock();
        successorCheckTimestampLock = new ReentrantReadWriteLock();
    }

    public static CheckAliveTimestamp getInstance(){
        if(singletonCheckAliveTimestamp == null) {
            singletonCheckAliveTimestamp = new CheckAliveTimestamp();
        }
        return singletonCheckAliveTimestamp;
    }

    public Timestamp getPredecessorCheckTimestamp(){
        predecessorCheckTimestampLock.readLock().lock();
        try {
            return predecessorCheckTimestamp;
        } finally {
            predecessorCheckTimestampLock.readLock().unlock();
        }
    }

    public void setPredecessorCheckTimestamp() {
        predecessorCheckTimestampLock.writeLock().lock();
        predecessorCheckTimestamp = new Timestamp(System.currentTimeMillis());
        predecessorCheckTimestampLock.writeLock().unlock();
    }

    public Timestamp getSuccessorCheckTimestamp(){
        successorCheckTimestampLock.readLock().lock();
        try {
            return successorCheckTimestamp;
        } finally {
            successorCheckTimestampLock.readLock().unlock();
        }
    }

    public void setSuccessorCheckTimestamp() {
        successorCheckTimestampLock.writeLock().lock();
        successorCheckTimestamp = new Timestamp(System.currentTimeMillis());
        successorCheckTimestampLock.writeLock().unlock();
    }

//    public boolean hasPredecessorTimestampExpired() {
//        successorCheckTimestampLock.readLock().lock();
//        try {
//            return ((System.currentTimeMillis() - successorCheckTimestamp) >= IMPS_TIMEOUT);
//        } finally {
//            successorCheckTimestampLock.readLock().unlock();
//        }
//    }
//
//    public boolean check_ims_expire() {
//        predecessorCheckTimestampLock.readLock().lock();
//        try {
//            return ((System.currentTimeMillis() - predecessorCheckTimestamp) >= IMS_TIMEOUT);
//        } finally {
//            predecessorCheckTimestampLock.readLock().unlock();
//        }
//    }




}
