package command;

import algorithm.ImmediateSuccessorRouter;
import algorithm.NoPotentialIMSException;
import message.BadValueLengthException;
import message.InvalidMessageException;
import message.Message;
import message.Payload;
import nodelist.Node;
import nodelist.NodeListController;
import protocol.JoinInitiator;
import utility.NodeSerializerUtility;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class JOINHandler {

    public static boolean hasHadPreviousJoinRequest = false;

    /**
     * Handles join requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static synchronized DatagramPacket handleJOINRequest(DatagramPacket incomingPacket) {

        // Extract header and payload
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);
        byte[] payload = Message.extractPayload(message);

        NodeListController nlc = NodeListController.getInstance();
        DatagramPacket packetToSend = null;

        try {
            // Get joining node
            byte[] extractedNodeList = Payload.getPayloadElement(Payload.Element.NODE_LIST, payload);
            ArrayList<Node> list = NodeSerializerUtility.deserialize(extractedNodeList);
            Node joiningNode = list.get(0);
            int joiningNodeID = joiningNode.getId();

            // Drop packet if joining node is already in view (as a predecessor, self, or in successor list)
            if(nlc.isNodeAlreadyInView(joiningNodeID)) {
                return null;
            }

            // Get predecessor and self
            Node predecessor = nlc.getPredecessor();
            Node self = nlc.getSelf();


            if(predecessor == null) {
                // CASE 1) Null predecessor and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // Look at self and successor list
                    // i) If no element found -> Send forward request to last successor
                    // ii) If element found -> Send potential ims request to element
                    int successorList[] = nlc.getSuccessorListIDs();
                    try {
                        int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(joiningNodeID, -1, self.getId(), successorList);
                        Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                        if(potentialIMS != null) {
                            byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_JOIN, joiningNode.getHostname(), incomingPacket.getPort(), payload);
                            byte[] newMessage = Message.buildMessage(header, newPayload);
                            packetToSend = new DatagramPacket(newMessage, newMessage.length, potentialIMS.getHostname(), potentialIMS.getReceivingPort());
                        } else {
                            // Drop packet
                            return null;
                        }
                    } catch(NoPotentialIMSException e) {
                        // Get last successor
                        Node lastSuccessor = nlc.getLastSuccessor();
                        if(lastSuccessor != null) {
                            // Prepare forward join packet
                            byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_JOIN, joiningNode.getHostname(), incomingPacket.getPort(), payload);
                            byte[] newMessage = Message.buildMessage(header, newPayload);
                            packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                        } else {
                            // Drop packet
                            return null;
                        }
                    }
                }
                // CASE 2) Null predecessor and empty successor list
                else {
                    // Add joining node as predecessor
                    nlc.setPredecessor(joiningNode);
                    JoinInitiator.isJoined = true;

                    // Prepare join response packet
                    byte[] succListToSend = NodeSerializerUtility.serializeNodeList(nlc.getSuccessorListWithSelf());
                    byte[] newPayload = Payload.buildPayloadWithNodeList(ResponseCodes.JOIN_REP, succListToSend);
                    byte[] newMessage = Message.buildMessage(header, newPayload);
                    packetToSend = new DatagramPacket(newMessage, newMessage.length, joiningNode.getHostname(), incomingPacket.getPort());

                    // Add node as successor if this is first join request this node is receiving
                    if(!hasHadPreviousJoinRequest) {
                        nlc.addFirstSuccessor(joiningNode);
                        hasHadPreviousJoinRequest = true;
                    }

                }
            }
            else {
                // CASE 3) Predecessor set and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // Look at predecessor, self, and successor list
                    // i) If self is IMS -> Add joining node as predecessor and send join response
                    // ii) If no element found in view -> Send forward request to last successor
                    // iii) If element found in successor list -> Send potential ims request to element
                    int successorList[] = nlc.getSuccessorListIDs();
                    try {
                        int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(joiningNodeID,
                                predecessor.getId(), self.getId(), successorList);
                        if(potentialIMSID == self.getId()) {
                            // Add joining node as predecessor
                            nlc.setPredecessor(joiningNode);
                            JoinInitiator.isJoined = true;

                            // Prepare join response packet
                            byte[] succListToSend = NodeSerializerUtility.serializeNodeList(nlc.getSuccessorListWithSelf());
                            byte[] newPayload = Payload.buildPayloadWithNodeList(ResponseCodes.JOIN_REP, succListToSend);
                            byte[] newMessage = Message.buildMessage(header, newPayload);
                            packetToSend = new DatagramPacket(newMessage, newMessage.length, joiningNode.getHostname(), incomingPacket.getPort());

                            // Add node as successor if this is first join request this node is receiving
                            if(!hasHadPreviousJoinRequest) {
                                nlc.addFirstSuccessor(joiningNode);
                                hasHadPreviousJoinRequest = true;
                            }
                        } else {
                            // Prepare potential ims packet to send to potential ims node
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_JOIN, joiningNode.getHostname(), incomingPacket.getPort(), payload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, potentialIMS.getHostname(), potentialIMS.getReceivingPort());
                            } else {
                                // Drop packet
                                return null;
                            }
                        }
                    } catch(NoPotentialIMSException e) {
                        // Get last successor
                        Node lastSuccessor = nlc.getLastSuccessor();
                        if(lastSuccessor != null) {
                            // Prepare forward join packet
                            byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_JOIN, joiningNode.getHostname(), incomingPacket.getPort(), payload);
                            byte[] newMessage = Message.buildMessage(header, newPayload);
                            packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                            System.out.println("Potential IMS sending to node" + lastSuccessor.getId());
                        } else {
                            // Drop packet
                            return null;
                        }
                    }
                }
                // CASE 4) Predecessor set and empty successor list
                else {
                    // Drop packet
                    return null;
                }
            }

        } catch(InvalidMessageException e) {
            e.printStackTrace();
        } catch(BadValueLengthException e) {
            e.printStackTrace();
        }

        return packetToSend;
    }


    /**
     * Handles forward join requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static synchronized DatagramPacket handleForwardJOINRequest(DatagramPacket incomingPacket) {
        // Extract header and payload
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);
        byte[] payload = Message.extractPayload(message);

        NodeListController nlc = NodeListController.getInstance();
        DatagramPacket packetToSend = null;

        try {
            // Get actual payload
            int port = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            byte[] actualPayload = Payload.getPayloadElement(Payload.Element.REGULAR_FORWARD_PAYLOAD, payload);

            // Get joining node
            byte[] extractedNodeList = Payload.getPayloadElement(Payload.Element.NODE_LIST, actualPayload);
            ArrayList<Node> list = NodeSerializerUtility.deserialize(extractedNodeList);
            Node joiningNode = list.get(0);
            int joiningNodeID = joiningNode.getId();

            // Drop packet if joining node is already in view (as a predecessor, self, or in successor list)
            if(nlc.isNodeAlreadyInView(joiningNodeID)) {
                return null;
            }

            // Get predecessor and self
            Node predecessor = nlc.getPredecessor();
            Node self = nlc.getSelf();

            if(predecessor == null) {
                // CASE 1) Null predecessor and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // Look at self and successor list
                    // i) If no element found -> Send forward request to last successor
                    // ii) If element found -> Send potential ims request to element
                    int successorList[] = nlc.getSuccessorListIDs();
                    try {
                        int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(joiningNodeID, -1, self.getId(), successorList);
                        Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                        if(potentialIMS != null) {
                            byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_JOIN, joiningNode.getHostname(), port, actualPayload);
                            byte[] newMessage = Message.buildMessage(header, newPayload);
                            packetToSend = new DatagramPacket(newMessage, newMessage.length, potentialIMS.getHostname(), potentialIMS.getReceivingPort());
                        } else {
                            // Drop packet
                            return null;
                        }
                    } catch(NoPotentialIMSException e) {
                        // Get last successor
                        Node lastSuccessor = nlc.getLastSuccessor();
                        if(lastSuccessor != null) {
                            // Prepare forward join packet
                            byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_JOIN, joiningNode.getHostname(), port, actualPayload);
                            byte[] newMessage = Message.buildMessage(header, newPayload);
                            packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                        } else {
                            // Drop packet
                            return null;
                        }
                    }

                }
                // CASE 2) Null predecessor and empty successor list
                else {
                    // Drop packet
                    return null;
                }
            }
            else {
                // CASE 3) Predecessor set and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // Look at predecessor, self, and successor list
                    // i) If self is IMS -> Add joining node as predecessor
                    // ii) If no element found in view -> Send forward request to last successor
                    // iii) If element found in successor list -> Send potential ims request to element
                    int[] successorList = nlc.getSuccessorListIDs();
                    try {
                        int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(joiningNodeID, predecessor.getId(), self.getId(), successorList);
                        if(potentialIMSID == self.getId()) {
                            // Add joining node as predecessor
                            nlc.setPredecessor(joiningNode);
                            JoinInitiator.isJoined = true;

                            // Prepare join response packet
                            byte[] succListToSend = NodeSerializerUtility.serializeNodeList(nlc.getSuccessorListWithSelf());
                            byte[] newPayload = Payload.buildPayloadWithNodeList(ResponseCodes.JOIN_REP, succListToSend);
                            byte[] newMessage = Message.buildMessage(header, newPayload);
                            packetToSend = new DatagramPacket(newMessage, newMessage.length, joiningNode.getHostname(), port);

                            // Add node as successor if this is first join request this node is receiving
                            if(!hasHadPreviousJoinRequest) {
                                nlc.addFirstSuccessor(joiningNode);
                                hasHadPreviousJoinRequest = true;
                            }
                        } else {
                            // Prepare potential ims packet to send to potential ims node
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_JOIN, joiningNode.getHostname(), port, actualPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, potentialIMS.getHostname(), potentialIMS.getReceivingPort());
                            } else {
                                // Drop packet
                                return null;
                            }
                        }

                    } catch(NoPotentialIMSException e) {
                        // Get last successor
                        Node lastSuccessor = nlc.getLastSuccessor();
                        if(lastSuccessor != null) {
                            // Prepare forward join packet
                            byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_JOIN, joiningNode.getHostname(), port, actualPayload);
                            byte[] newMessage = Message.buildMessage(header, newPayload);
                            packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                        } else {
                            // Drop packet
                            return null;
                        }

                    }
                }
                // CASE 4) Predecessor set and empty successor list
                else {
                    // Drop packet
                    return null;
                }
            }

        } catch(InvalidMessageException e) {
            e.printStackTrace();
        } catch(BadValueLengthException e) {
            e.printStackTrace();
        }

        return packetToSend;
    }


    /**
     * Handles potential immediate successor join requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static synchronized DatagramPacket handlePotentialImsJOINRequest(DatagramPacket incomingPacket) {
        // Extract header and payload
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);
        byte[] payload = Message.extractPayload(message);

        NodeListController nlc = NodeListController.getInstance();
        DatagramPacket packetToSend = null;

        try {
            // Get actual payload
            int port = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            byte[] actualPayload = Payload.getPayloadElement(Payload.Element.REGULAR_FORWARD_PAYLOAD, payload);

            // Get joining node
            byte[] extractedNodeList = Payload.getPayloadElement(Payload.Element.NODE_LIST, actualPayload);
            ArrayList<Node> list = NodeSerializerUtility.deserialize(extractedNodeList);
            Node joiningNode = list.get(0);
            int joiningNodeID = joiningNode.getId();

            // Drop packet if joining node is already in view (as a predecessor, self, or in successor list)
            if(nlc.isNodeAlreadyInView(joiningNodeID)) {
                return null;
            }

            // Get predecessor and self
            Node predecessor = nlc.getPredecessor();
            Node self = nlc.getSelf();

            if(predecessor == null) {
                // CASE 1) Null predecessor and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // Drop packet
                    return null;
                }
                // CASE 2) Null predecessor and empty successor list
                else {
                    // Drop packet
                    return null;
                }
            }
            else {
                // CASE 3) Predecessor set and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // Look at predecessor and self
                    // i) If self is correct IMS -> Add joining node and send join response
                    // ii) If not correct -> Send predecessor a potential ims join request
                    if(ImmediateSuccessorRouter.isSelfPotentialIMS(joiningNodeID, predecessor.getId(), self.getId())) {
                        // Add joining node as predecessor
                        nlc.setPredecessor(joiningNode);
                        JoinInitiator.isJoined = true;

                        // Prepare join response packet
                        byte[] succListToSend = NodeSerializerUtility.serializeNodeList(nlc.getSuccessorListWithSelf());
                        byte[] newPayload = Payload.buildPayloadWithNodeList(ResponseCodes.JOIN_REP, succListToSend);
                        byte[] newMessage = Message.buildMessage(header, newPayload);
                        packetToSend = new DatagramPacket(newMessage, newMessage.length, joiningNode.getHostname(), port);

                        // Add node as successor if this is first join request this node is receiving
                        if(!hasHadPreviousJoinRequest) {
                            nlc.addFirstSuccessor(joiningNode);
                            hasHadPreviousJoinRequest = true;
                        }
                    } else {
                        // Prepare potential ims join request to send to predecessor
                        byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_JOIN, joiningNode.getHostname(), port, actualPayload);
                        byte[] newMessage = Message.buildMessage(header, newPayload);
                        packetToSend = new DatagramPacket(newMessage, newMessage.length, predecessor.getHostname(), predecessor.getReceivingPort());
                    }
                }
                // CASE 4) Predecessor set and empty successor list
                else {
                    // Drop packet
                    return null;
                }
            }

        } catch(InvalidMessageException e) {
            e.printStackTrace();
        } catch(BadValueLengthException e) {
            e.printStackTrace();
        }

        return packetToSend;
    }





    /**
     * Handlers join responses
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static void handleJOINResponse(DatagramPacket incomingPacket) {

        // Extract header and payload
        byte[] message = incomingPacket.getData();
        byte[] payload = Message.extractPayload(message);

        NodeListController nlc = NodeListController.getInstance();

        try {
            // Extract node list from payload
            byte[] extractedList = Payload.getPayloadElement(Payload.Element.NODE_LIST, payload);
            ArrayList<Node> list = NodeSerializerUtility.deserialize(extractedList);

            // Set successor list as the extracted node list
            nlc.setSuccessorList(list);

            // Set flag so that join requests should no longer be sent
            JoinInitiator.isJoined = true;

        } catch(InvalidMessageException e) {
            e.printStackTrace();
        } catch(BadValueLengthException e) {
            e.printStackTrace();
        }
    }







}
