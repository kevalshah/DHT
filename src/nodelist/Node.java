package nodelist;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Holds information about a node in the DHT
 */
public class Node implements Serializable {

    private InetAddress hostname;
    private int receivingPort;
    private int id;

    public Node(InetAddress hostname, int receivingPort, int id) {
        this.hostname = hostname;
        this.receivingPort = receivingPort;
        this.id = id;
    }

    public Node(Node node) {
        this.hostname = node.hostname;
        this.receivingPort = node.receivingPort;
        this.id = node.id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public InetAddress getHostname() {
        return hostname;
    }

    public void setHostname(InetAddress hostname) {
        this.hostname = hostname;
    }

    public int getReceivingPort() {
        return receivingPort;
    }

    public void setReceivingPort(int receivingPort) {
        this.receivingPort = receivingPort;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }

        if(!(obj instanceof Node)) { return false; }

        if (this == obj) { return true; }

        final Node other = (Node) obj;
        if(other.hostname == null && this.hostname == null) {
            if(other.receivingPort == this.receivingPort && other.id == this.id) {
                return true;
            }
            return false;
        }

        if(other.hostname == null && this.hostname != null) {
            return false;
        }

        if(other.hostname != null && this.hostname == null) {
            return false;
        }

        if(other.hostname.equals(this.hostname) && other.receivingPort == this.receivingPort && other.id == this.receivingPort) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        String returnString = "";
        returnString += (hostname == null ? "null" : hostname.getHostName() + ":" + receivingPort);
        returnString += " ID: " + (id >= 0 ? id : "-");
        return returnString;
    }


}
