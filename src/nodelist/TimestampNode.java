package nodelist;

import java.net.InetAddress;

public class TimestampNode extends Node {

    private long timestamp;

    public TimestampNode(InetAddress hostname, int receivingPort, int id) {
        super(hostname, receivingPort, id);
        timestamp = System.currentTimeMillis();
    }

    public TimestampNode(Node node) {
        super(node.getHostname(), node.getReceivingPort(), node.getId());
        timestamp = System.currentTimeMillis();
    }

    public boolean hasNodeExpired(int expirationPeriodMilliseconds) {
        return (System.currentTimeMillis() - timestamp) >= expirationPeriodMilliseconds;
    }


    public void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }
}
