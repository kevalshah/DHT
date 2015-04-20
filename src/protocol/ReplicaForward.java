package protocol;

import command.RequestCodes;
import message.InvalidMessageException;
import message.Message;
import message.Payload;
import nodelist.Node;
import nodelist.NodeListController;
import utility.UTF8StringUtility;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

public class ReplicaForward {

    // Replication factor
    public static int BACKUP_NODE_NUMBER = 3;

    /**
     * Builds a message for forwarding a REMOVE request to replica and sends to replicas
     * @param uniqueID - Unique ID of message
     * @param key - Key for removal
     */
    public static void forwardREMOVEToReplica(byte[] uniqueID, String key) {

        byte[] keyToBytes = UTF8StringUtility.stringToBytesUTF8(key);

        byte[] payload = Payload.buildStandardRequestPayload(RequestCodes.REPLICA_REMOVE, keyToBytes);
        try {
            byte[] messageToSend = Message.buildMessage(uniqueID, payload);
            sendMessageToReplicas(messageToSend);
        } catch(InvalidMessageException e) {
            e.printStackTrace();
        }
    }


    /**
     * Builds a message for forwarding a PUT request to replicas and sends to replicas
     * @param uniqueID - Unique ID of message
     * @param key - Key of <key, value> pair to put
     * @param value - Value of <key, value> pair to put
     */
    public static void forwardPUTtoReplica(byte[] uniqueID, String key, String value) {

        byte[] keyToBytes = UTF8StringUtility.stringToBytesUTF8(key);
        byte[] valueToBytes = UTF8StringUtility.stringToBytesUTF8(value);

        byte[] payload = Payload.buildStandardRequestPayload(RequestCodes.REPLICA_PUT, keyToBytes, valueToBytes);
        try {
            byte[] messageToSend = Message.buildMessage(uniqueID, payload);
            sendMessageToReplicas(messageToSend);
        } catch(InvalidMessageException e) {
            e.printStackTrace();
        }
    }


    /**
     * Builds a message for forwarding a GET request to replica and sends to replicas
     * @param uniqueID - Unique ID of message
     * @param clientAddress - IP Address of client
     * @param clientPort - Receiving port of client
     * @param key - Key to get
     */
    public static void forwardGETtoReplica(byte[] uniqueID, InetAddress clientAddress, int clientPort, byte[] key) {
        //        byte[] payload = Payload.buildForwardingRequestPayload(RequestCodes.REPLICA_FWD_GET, clientAddress, clientPort, key);
        //        try {
        //            byte[] messageToSend = Message.buildMessage(uniqueID, payload);
        //            sendMessageToReplicas(messageToSend);
        //        } catch(InvalidMessageException e) {
        //
        //        }
    }


    /**
     * Sends message to replicas
     * @param message - Message to send to replicas
     */
    private static void sendMessageToReplicas(byte[] message) {
        ArrayList<Node> nl = NodeListController.getInstance().getTopSuccessors(BACKUP_NODE_NUMBER);
        for (Node node : nl){
            DatagramPacket pkt = new DatagramPacket(message, message.length, node.getHostname(), node.getReceivingPort());
            UDPSend.sendPacket(pkt);
        }
    }



}
