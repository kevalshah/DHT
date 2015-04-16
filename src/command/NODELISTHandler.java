package command;

import message.InvalidMessageException;
import message.Message;
import message.Payload;
import nodelist.Node;
import nodelist.NodeListController;
import utility.NodeSerializerUtility;

import java.net.DatagramPacket;
import java.util.ArrayList;

public class NODELISTHandler {


    /**
     * Handles node list requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleNODELISTRequest(DatagramPacket incomingPacket) {
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);

        NodeListController nlc = NodeListController.getInstance();
        DatagramPacket packetToSend = null;

        // Serialize node view
        ArrayList<Node> nodeList = nlc.getSuccessorListWithPredecessorAndSelf();
        byte[] serializedNodeList = NodeSerializerUtility.serializeNodeList(nodeList);

        // Build payload
        byte[] payload = Payload.buildPayloadWithNodeList(ResponseCodes.NODE_LIST_RESPONSE, serializedNodeList);
        byte[] messageToSend = null;
        try {
            messageToSend = Message.buildMessage(header, payload);
            packetToSend = new DatagramPacket(messageToSend, messageToSend.length, incomingPacket.getAddress(), incomingPacket.getPort());
        } catch(InvalidMessageException e) {
            e.printStackTrace();
        }
        return packetToSend;
    }



}
