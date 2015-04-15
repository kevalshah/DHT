package command;

import algorithm.ImmediateSuccessorRouter;
import algorithm.NoPotentialIMSException;
import kvstore.KVStoreController;
import kvstore.KVStoreKeyNotFoundException;
import message.BadValueLengthException;
import message.InvalidMessageException;
import message.Message;
import message.Payload;
import nodelist.Node;
import nodelist.NodeListController;
import timestamp.CheckAliveTimestamp;
import utility.HashUtility;
import utility.UTF8StringUtility;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class REMOVEHandler {

    /**
     * Handles client remove requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleClientREMOVE(DatagramPacket incomingPacket) {
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);
        byte[] payload = Message.extractPayload(message);

        DatagramPacket packetToSend = null;
        NodeListController nlc = NodeListController.getInstance();
        InetAddress ipFromPacket = incomingPacket.getAddress();
        int portFromPacket = incomingPacket.getPort();

        try {
            // Get key from payload
            byte[] keyAsBytes = Payload.getPayloadElement(Payload.Element.KEY, payload);
            String key = UTF8StringUtility.bytesUTF8ToString(keyAsBytes);

            int keyRequestID = HashUtility.hashString(key);

            Node predecessor = nlc.getPredecessor();
            Node self = nlc.getSelf();
            if(predecessor == null) {
                // CASE 1) Null predecessor and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // i) Compare request ID with self -> If same, perform search in local kvstore
                    // ii) If above fails, look in successor list. If no potential ims found -> Send forward request to last successor
                    // ii) If potential ims found in successor list -> Send potential ims request to element

                    if(self.getId() == keyRequestID) {
                        // Perform remove operation on local kvstore
                        packetToSend = performRemoveOperation(key, header, incomingPacket.getAddress(), incomingPacket.getPort());
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, -1,
                                    self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims remove packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_REMOVE, incomingPacket.getAddress(), incomingPacket.getPort(), payload);
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
                                // Prepare forward remove packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_REMOVE, incomingPacket.getAddress(), incomingPacket.getPort(), payload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                            } else {
                                // Drop packet
                                return null;
                            }
                        }
                    }
                }
                // CASE 2) Null predecessor and empty successor list
                else {
                    CheckAliveTimestamp timestamp = CheckAliveTimestamp.getInstance();
                    if(self.getId() == keyRequestID || (timestamp.getPredecessorCheckTimestamp() == null && timestamp.getSuccessorCheckTimestamp() == null)) {
                        // Perform remove operation on local kvstore
                        packetToSend = performRemoveOperation(key, header, incomingPacket.getAddress(), incomingPacket.getPort());
                    } else {


                    }
                }
            }
            else {
                // CASE 3) Predecessor set and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // Look at predecessor, self, and successor list
                    // i) If self is IMS -> Perform local search in kvstore
                    // ii) If no potential ims found in view -> Send forward request to last successor
                    // iii) If potential ims found in successor list -> Send potential ims request to element
                    if(ImmediateSuccessorRouter.isSelfPotentialIMS(keyRequestID, predecessor.getId(), self.getId())) {
                        // Perform remove operation on local kvstore
                        packetToSend = performRemoveOperation(key, header, incomingPacket.getAddress(), incomingPacket.getPort());
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, predecessor.getId(), self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims remove packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_REMOVE, incomingPacket.getAddress(), incomingPacket.getPort(), payload);
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
                                // Prepare forward remove packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_REMOVE, incomingPacket.getAddress(), incomingPacket.getPort(), payload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                            } else {
                                // Drop packet
                                return null;
                            }
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
     * Handles forwarding remove requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleForwardREMOVE(DatagramPacket incomingPacket) {
        // Extract header and payload
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);
        byte[] payload = Message.extractPayload(message);

        NodeListController nlc = NodeListController.getInstance();
        DatagramPacket packetToSend = null;

        try {
            // Get actual payload
            InetAddress clientAddress = InetAddress.getByAddress(Payload.getPayloadElement(Payload.Element.IP_ADDRESS, payload));
            int clientPort = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            byte[] actualPayload = Payload.getPayloadElement(Payload.Element.ACTUAL_PAYLOAD, payload);

            // Get key from payload
            byte[] keyAsBytes = Payload.getPayloadElement(Payload.Element.KEY, actualPayload);
            String key = UTF8StringUtility.bytesUTF8ToString(keyAsBytes);
            int keyRequestID = HashUtility.hashString(key);

            // Get predecessor and self
            Node predecessor = nlc.getPredecessor();
            Node self = nlc.getSelf();

            if(predecessor == null) {
                // CASE 1) Null predecessor and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // i) Compare request ID with self -> If same, perform search in local kvstore
                    // ii) If above fails, look in successor list. If no potential ims found -> Send forward request to last successor
                    // ii) If potential ims found in successor list -> Send potential ims request to element

                    if(self.getId() == keyRequestID) {
                        // Perform remove operation on local kvstore
                        packetToSend = performRemoveOperation(key, header, clientAddress, clientPort);
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, -1, self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims remove packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_REMOVE, clientAddress, clientPort, actualPayload);
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
                                // Prepare forward remove packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_REMOVE, clientAddress, clientPort, actualPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                            } else {
                                // Drop packet
                                return null;
                            }
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
                    // i) If self is IMS -> Perform local search in kvstore
                    // ii) If no potential ims found in view -> Send forward request to last successor
                    // iii) If potential ims found in successor list -> Send potential ims request to element
                    if(self.getId() == keyRequestID) {
                        // Perform remove operation on local kvstore
                        packetToSend = performRemoveOperation(key, header, clientAddress, clientPort);
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, predecessor.getId(), self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims remove packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_REMOVE, clientAddress, clientPort, actualPayload);
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
                                // Prepare forward remove packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_REMOVE, clientAddress, clientPort, actualPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                            } else {
                                // Drop packet
                                return null;
                            }
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
        } catch(UnknownHostException e) {
            e.printStackTrace();
        }


        return packetToSend;

    }


    /**
     * Handles replica remove requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleReplicaREMOVE(DatagramPacket incomingPacket) {
        DatagramPacket packetToSend = null;




        return packetToSend;
    }


    /**
     * Handles potential ims remove requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handlePotentialIMSREMOVE(DatagramPacket incomingPacket) {
        // Extract header and payload
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);
        byte[] payload = Message.extractPayload(message);

        NodeListController nlc = NodeListController.getInstance();
        DatagramPacket packetToSend = null;

        try {
            // Get actual payload
            InetAddress clientAddress = InetAddress.getByAddress(Payload.getPayloadElement(Payload.Element.IP_ADDRESS, payload));
            int clientPort = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            byte[] actualPayload = Payload.getPayloadElement(Payload.Element.ACTUAL_PAYLOAD, payload);

            // Get key from payload
            byte[] keyAsBytes = Payload.getPayloadElement(Payload.Element.KEY, actualPayload);
            String key = UTF8StringUtility.bytesUTF8ToString(keyAsBytes);
            int keyRequestID = HashUtility.hashString(key);

            // Get predecessor and self
            Node predecessor = nlc.getPredecessor();
            Node self = nlc.getSelf();

            if(predecessor == null) {
                // CASE 1) Null predecessor and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // i) Compare request ID with self -> If same, perform search in local kvstore
                    // ii) If above fails, look in successor list. If no potential ims found -> Send forward request to last successor
                    // ii) If potential ims found in successor list -> Send potential ims request to element

                    if(self.getId() == keyRequestID) {
                        // Perform remove operation on local kvstore
                        packetToSend = performRemoveOperation(key, header, clientAddress, clientPort);
                    } else {
                        // Drop packet
                        return null;
                    }
                }
                // CASE 2) Null predecessor and empty successor list
                else {
                    // Drop packet
                    return null;
                }
            }
            else {
                // CASE 3) Predecessor set
                if(nlc.getSuccessorListLength() > 0) {
                    // Look at predecessor and self
                    // i) If self is correct IMS -> Do local search on kvstore
                    // ii) If not correct -> Send predecessor a potential ims remove request
                    if(ImmediateSuccessorRouter.isSelfPotentialIMS(keyRequestID, predecessor.getId(), self.getId())) {
                        // Perform remove operation on local kvstore
                        packetToSend = performRemoveOperation(key, header, clientAddress, clientPort);

                    } else {
                        // Prepare potential ims remove request to send to predecessor
                        byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_REMOVE, clientAddress, clientPort, actualPayload);
                        byte[] newMessage = Message.buildMessage(header, newPayload);
                        packetToSend = new DatagramPacket(newMessage, newMessage.length, predecessor.getHostname(), predecessor.getReceivingPort());
                    }
                }

            }
        } catch(InvalidMessageException e) {
            e.printStackTrace();
        } catch(BadValueLengthException e) {
            e.printStackTrace();
        } catch(UnknownHostException e) {
            e.printStackTrace();
        }


        return packetToSend;

    }


    /**
     * Performs a remove operation on local kvstore and builds a response packet
     * @param key - Key to remove
     * @param header - Header to include in response packet
     * @param clientAddress - Client address
     * @param clientPort - Client port
     * @return
     */
    protected static DatagramPacket performRemoveOperation(String key, byte[] header, InetAddress clientAddress, int clientPort) {
        DatagramPacket packet = null;
        byte[] payload = null;

        KVStoreController kvStoreController = KVStoreController.getInstance();
        try {
            // Attempt to remove key-value pair from key value store
            kvStoreController.remove(key);
//            cacheController.add(ce);

            /* If key was removed from key-value store, build a response payload
               with response code: Operation Success */

            payload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.OPERATION_SUCCESS);
//            System.out.println("REMOVE operation succeeded");

            // Forward to replicas only if <key, value> was removed
//            ReplicaForward.forwardREMOVEToReplica(header, keyAsBytes);

        } catch(KVStoreKeyNotFoundException e) {

            /* If key is not found in key-value store, build a response payload
               with response code: Non-existent-key */

            payload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.NON_EXISTENT_KEY);
//            System.out.println("REMOVE operation failed - key not found");
        }

        if(payload != null && header != null) {
            try {
                byte[] message = Message.buildMessage(header, payload);
                packet = new DatagramPacket(message, message.length, clientAddress, clientPort);
            } catch(InvalidMessageException e) {
                e.printStackTrace();
            }

        }
        return packet;
    }




}
