package command;

import message.Message;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class GETHandler {

    /**
     * Handles client get requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleClientGET(DatagramPacket incomingPacket) {
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);
        byte[] payload = Message.extractPayload(message);

        InetAddress ipFromPacket = incomingPacket.getAddress();
        int portFromPacket = incomingPacket.getPort();


        DatagramPacket packetToSend = null;




        return packetToSend;
    }


    /**
     * Handles forwarding get requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleForwardGET(DatagramPacket incomingPacket) {
        DatagramPacket packetToSend = null;




        return packetToSend;
    }


    /**
     * Handles replica get requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleReplicaGET(DatagramPacket incomingPacket) {
        DatagramPacket packetToSend = null;




        return packetToSend;
    }


    /**
     * Handles potential ims get requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handlePotentialIMSGET(DatagramPacket incomingPacket) {
        DatagramPacket packetToSend = null;


        return packetToSend;
    }





}
