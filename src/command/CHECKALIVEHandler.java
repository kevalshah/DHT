package command;

import algorithm.ImmediateSuccessorRouter;
import message.*;
import nodelist.Node;
import nodelist.NodeListController;
import protocol.UDPSend;
import utility.NodeSerializerUtility;

import java.net.DatagramPacket;
import java.util.ArrayList;

public class CHECKALIVEHandler {

    /**
     * Handles predecessor alive requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handlePredecessorAliveRequest(DatagramPacket incomingPacket) {
        // Extract header
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);

        DatagramPacket packetToSend = null;

        // Build response message
        byte[] newPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.PRED_ALIVE_REP);
        try {
            byte[] newMessage = Message.buildMessage(header, newPayload);
            packetToSend = new DatagramPacket(newMessage, newMessage.length, incomingPacket.getAddress(), incomingPacket.getPort());
        } catch(InvalidMessageException e) {
            e.printStackTrace();
        }

        return packetToSend;
    }


    /**
     * Handles successor alive requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleSuccessorAliveRequest(DatagramPacket incomingPacket) {
        // Extract header
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);

        NodeListController nlc = NodeListController.getInstance();
        DatagramPacket packetToSend = null;

        // Get node view
        ArrayList<Node> list = nlc.getSuccessorListWithPredecessorAndSelf();

        // Serialize list
        byte[] listToSend = NodeSerializerUtility.serializeNodeList(list);

        // Build response message
        byte[] newPayload = Payload.buildPayloadWithNodeList(ResponseCodes.SUCC_ALIVE_REP, listToSend);
        try {
            byte[] newMessage = Message.buildMessage(header, newPayload);
            packetToSend = new DatagramPacket(newMessage, newMessage.length, incomingPacket.getAddress(), incomingPacket.getPort());
        } catch(InvalidMessageException e) {
            e.printStackTrace();
        }

        return packetToSend;
    }


    /**
     * Handles successor alive responses
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static void handleSuccessorAliveResponse(DatagramPacket incomingPacket) {
        // Extract payload
        byte[] message = incomingPacket.getData();
        byte[] payload = Message.extractPayload(message);

        NodeListController nlc = NodeListController.getInstance();
        DatagramPacket packetToSend = null;

        try {
            byte[] serializedList = Payload.getPayloadElement(Payload.Element.NODE_LIST, payload);
            ArrayList<Node> list = NodeSerializerUtility.deserialize(serializedList);

            Node predecessorOfSuccessor = list.get(0);
            Node self = nlc.getSelf();
            Node firstSuccessor = nlc.getFirstSuccessor();

            // CASE 1) Predecessor of successor is null
            if(predecessorOfSuccessor.getId() < 0) {
                // Prepare a potential predecessor update message to send to first successor
                byte[] header = Header.buildMessageHeader();
                ArrayList<Node> listToSend = new ArrayList<Node>();
                listToSend.add(self);
                byte[] listToSendAsBytes = NodeSerializerUtility.serializeNodeList(listToSend);
                byte[] newPayload = Payload.buildPayloadWithNodeList(RequestCodes.POTENTIAL_IMPS_UPDATE, listToSendAsBytes);
                byte[] newMessage = Message.buildMessage(header, newPayload);
                packetToSend = new DatagramPacket(newMessage, newMessage.length, firstSuccessor.getHostname(), firstSuccessor.getReceivingPort());
            } else {
                // CASE 2) Predecessor of successor is this node
                if(self.getId() == predecessorOfSuccessor.getId()) {
                    // Update this node's successor list
                    ArrayList<Node> arrayListToSet = new ArrayList<Node>();
                    for(int i = 0; i < list.size(); i++) {
                        if(i != 0) {
                            arrayListToSet.add(list.get(i));
                        }
                    }

                    nlc.setSuccessorList(arrayListToSet);
                }
                // CASE 3) Predecessor of successor is another node (new node)
                else {
                    // Add new node as first successor
                    nlc.addFirstSuccessor(predecessorOfSuccessor);

                    // Prepare a potential predecessor update message to send to the other node
                    byte[] header = Header.buildMessageHeader();
                    ArrayList<Node> listToSend = new ArrayList<Node>();
                    listToSend.add(self);
                    byte[] listToSendAsBytes = NodeSerializerUtility.serializeNodeList(listToSend);
                    byte[] newPayload = Payload.buildPayloadWithNodeList(RequestCodes.POTENTIAL_IMPS_UPDATE, listToSendAsBytes);
                    byte[] newMessage = Message.buildMessage(header, newPayload);
                    packetToSend = new DatagramPacket(newMessage, newMessage.length, predecessorOfSuccessor.getHostname(), predecessorOfSuccessor.getReceivingPort());
                }
            }

            if(packetToSend != null) {
                UDPSend.sendPacket(packetToSend);
            }


        } catch(InvalidMessageException e) {
            e.printStackTrace();
        } catch(BadValueLengthException e) {
            e.printStackTrace();
        }

    }


    /**
     * Handles potential predecessor update requests
     * @param incomingPacket - Incoming packet
     */
    public static void handlePotentialPredecessorUpdate(DatagramPacket incomingPacket) {

        // Extract payload
        byte[] message = incomingPacket.getData();
        byte[] payload = Message.extractPayload(message);

        NodeListController nlc = NodeListController.getInstance();
        DatagramPacket packetToSend = null;

        byte[] serializedList = new byte[0];
        try {
            serializedList = Payload.getPayloadElement(Payload.Element.NODE_LIST, payload);
            ArrayList<Node> list = NodeSerializerUtility.deserialize(serializedList);

            Node potentialPredecessor = list.get(0);
            Node predecessor = nlc.getPredecessor();
            Node self = nlc.getSelf();

            // Update predecessor if predecessor is set to null
            if(predecessor == null) {
                nlc.setPredecessor(potentialPredecessor);
            }
            // Update predecessor if potential predecessor's ims is self
            else {
                if(ImmediateSuccessorRouter.isSelfPotentialIMS(potentialPredecessor.getId(), predecessor.getId(), self.getId())) {
                    nlc.setPredecessor(potentialPredecessor);
                }
            }

        } catch(InvalidMessageException e) {
            e.printStackTrace();
        } catch(BadValueLengthException e) {
            e.printStackTrace();
        }

    }


}
