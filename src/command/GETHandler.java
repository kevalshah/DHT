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
import timestamp.Timestamp;
import utility.HashUtility;
import utility.UTF8StringUtility;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

        DatagramPacket packetToSend = null;
        NodeListController nlc = NodeListController.getInstance();

        try {
            // Get key from payload
            byte[] keyAsBytes = Payload.getPayloadElement(Payload.Element.KEY, payload);
            String key = UTF8StringUtility.bytesUTF8ToString(keyAsBytes);

            int keyRequestID = HashUtility.simpleHash(key, HashUtility.DEFAULT_HASH_RANGE);

            Node predecessor = nlc.getPredecessor();
            Node self = nlc.getSelf();
            if(predecessor == null) {
                // CASE 1) Null predecessor and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // i) Compare request ID with self -> If same, perform search in local kvstore
                    // ii) If above fails, look in successor list. If no potential ims found -> Send forward request to last successor
                    // ii) If potential ims found in successor list -> Send potential ims request to element

                    if(self.getId() == keyRequestID) {
                        // Perform search in local kvstore
                        packetToSend = performGetOperation(key, header, self.getHostname(), self.getReceivingPort(), incomingPacket.getAddress(),
                                incomingPacket.getPort());
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, -1, self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims get packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(
                                        RequestCodes.POTENTIAL_IMS_GET, self.getHostname(), self.getReceivingPort(),
                                        incomingPacket.getAddress(), incomingPacket.getPort(), payload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, potentialIMS.getHostname(), potentialIMS.getReceivingPort());
                            } else {
                                // Build error packet
                                byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                                byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, incomingPacket.getAddress(), incomingPacket.getPort(), stdPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, self.getHostname(), self.getReceivingPort());
                            }
                        } catch(NoPotentialIMSException e) {
                            // Get last successor
                            Node lastSuccessor = nlc.getLastSuccessor();
                            if(lastSuccessor != null) {
                                // Prepare forward get packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(RequestCodes.FWD_GET,
                                        self.getHostname(), self.getReceivingPort(), incomingPacket.getAddress(),
                                        incomingPacket.getPort(), payload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                            } else {
                                // Build error packet
                                byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                                byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, incomingPacket.getAddress(), incomingPacket.getPort(), stdPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, self.getHostname(), self.getReceivingPort());
                            }
                        }
                    }
                }
                // CASE 2) Null predecessor and empty successor list
                else {
                    // Search local kvstore
                    packetToSend = performGetOperation(key, header, self.getHostname(), self.getReceivingPort(), incomingPacket.getAddress(),
                            incomingPacket.getPort());
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
                        // Perform search in local kvstore
                        packetToSend = performGetOperation(key, header, self.getHostname(), self.getReceivingPort(), incomingPacket.getAddress(),
                                incomingPacket.getPort());
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, predecessor.getId(), self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims get packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(
                                        RequestCodes.POTENTIAL_IMS_GET, self.getHostname(), self.getReceivingPort(),
                                        incomingPacket.getAddress(), incomingPacket.getPort(), payload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, potentialIMS.getHostname(), potentialIMS.getReceivingPort());
                            } else {
                                // Build error packet
                                byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                                byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, incomingPacket.getAddress(), incomingPacket.getPort(), stdPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, self.getHostname(), self.getReceivingPort());
                            }
                        } catch(NoPotentialIMSException e) {
                            // Get last successor
                            Node lastSuccessor = nlc.getLastSuccessor();
                            if(lastSuccessor != null) {
                                // Prepare forward get packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(RequestCodes.FWD_GET,
                                        self.getHostname(), self.getReceivingPort(), incomingPacket.getAddress(),
                                        incomingPacket.getPort(), payload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                            } else {
                                // Build error packet
                                byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                                byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, incomingPacket.getAddress(), incomingPacket.getPort(), stdPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, self.getHostname(), self.getReceivingPort());
                            }
                        }
                    }
                }
                // CASE 4) Predecessor set and empty successor list
                else {
                    // Build error packet
                    byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                    byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, incomingPacket.getAddress(), incomingPacket.getPort(), stdPayload);
                    byte[] newMessage = Message.buildMessage(header, newPayload);
                    packetToSend = new DatagramPacket(newMessage, newMessage.length, self.getHostname(), self.getReceivingPort());
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
     * Handles forwarding get requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleForwardGET(DatagramPacket incomingPacket) {
        // Extract header and payload
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);
        byte[] payload = Message.extractPayload(message);

        NodeListController nlc = NodeListController.getInstance();
        DatagramPacket packetToSend = null;

        try {
            // Get actual payload
            InetAddress returnAddress = InetAddress.getByAddress(Payload.getPayloadElement(Payload.Element.IP_ADDRESS, payload));
            int returnPort = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            InetAddress clientAddress = InetAddress.getByAddress(
                    Payload.getPayloadElement(Payload.Element.CLIENT_IP_ADDRESS, payload));
            int clientPort = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.CLIENT_PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            byte[] actualPayload = Payload.getPayloadElement(Payload.Element.CLIENT_FORWARD_PAYLOAD, payload);

            // Get key from payload
            byte[] keyAsBytes = Payload.getPayloadElement(Payload.Element.KEY, actualPayload);
            String key = UTF8StringUtility.bytesUTF8ToString(keyAsBytes);
            int keyRequestID = HashUtility.simpleHash(key, HashUtility.DEFAULT_HASH_RANGE);

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
                        // Perform search in local kvstore
                        packetToSend = performGetOperation(key, header, returnAddress, returnPort, clientAddress, clientPort);
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, -1, self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims get packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(
                                        RequestCodes.POTENTIAL_IMS_GET, returnAddress, returnPort, clientAddress,
                                        clientPort, actualPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, potentialIMS.getHostname(), potentialIMS.getReceivingPort());
                            } else {
                                // Build error packet
                                byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                                byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, returnAddress, returnPort);
                            }
                        } catch(NoPotentialIMSException e) {
                            // Get last successor
                            Node lastSuccessor = nlc.getLastSuccessor();
                            if(lastSuccessor != null) {
                                // Prepare forward get packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(RequestCodes.FWD_GET,
                                        returnAddress, returnPort, clientAddress, clientPort, actualPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                            } else {
                                // Build error packet
                                byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                                byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, returnAddress, returnPort);
                            }
                        }
                    }
                }
                // CASE 2) Null predecessor and empty successor list
                else {
                    // Build error packet
                    byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                    byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
                    byte[] newMessage = Message.buildMessage(header, newPayload);
                    packetToSend = new DatagramPacket(newMessage, newMessage.length, returnAddress, returnPort);
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
                        // Perform search in local kvstore
                        packetToSend = performGetOperation(key, header, returnAddress, returnPort, clientAddress, clientPort);
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, predecessor.getId(), self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims get packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(
                                        RequestCodes.POTENTIAL_IMS_GET, returnAddress, returnPort, clientAddress,
                                        clientPort, actualPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, potentialIMS.getHostname(), potentialIMS.getReceivingPort());
                            } else {
                                // Build error packet
                                byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                                byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, returnAddress, returnPort);
                            }
                        } catch(NoPotentialIMSException e) {
                            // Get last successor
                            Node lastSuccessor = nlc.getLastSuccessor();
                            if(lastSuccessor != null) {
                                // Prepare forward get packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(RequestCodes.FWD_GET,
                                        returnAddress, returnPort, clientAddress, clientPort, actualPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, lastSuccessor.getHostname(), lastSuccessor.getReceivingPort());
                            } else {
                                // Build error packet
                                byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                                byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
                                byte[] newMessage = Message.buildMessage(header, newPayload);
                                packetToSend = new DatagramPacket(newMessage, newMessage.length, returnAddress, returnPort);
                            }
                        }
                    }
                }
                // CASE 4) Predecessor set and empty successor list
                else {
                    // Build error packet
                    byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                    byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
                    byte[] newMessage = Message.buildMessage(header, newPayload);
                    packetToSend = new DatagramPacket(newMessage, newMessage.length, returnAddress, returnPort);
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
     * Handles replica get requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleReplicaGET(DatagramPacket incomingPacket) {
        DatagramPacket packetToSend = null;

        // Unused for now


        return packetToSend;
    }


    /**
     * Handles potential ims get requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handlePotentialIMSGET(DatagramPacket incomingPacket) {
        // Extract header and payload
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);
        byte[] payload = Message.extractPayload(message);

        NodeListController nlc = NodeListController.getInstance();
        DatagramPacket packetToSend = null;

        try {
            // Get actual payload
            InetAddress returnAddress = InetAddress.getByAddress(Payload.getPayloadElement(Payload.Element.IP_ADDRESS, payload));
            int returnPort = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            InetAddress clientAddress = InetAddress.getByAddress(Payload.getPayloadElement(Payload.Element.CLIENT_IP_ADDRESS, payload));
            int clientPort = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.CLIENT_PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            byte[] actualPayload = Payload.getPayloadElement(Payload.Element.CLIENT_FORWARD_PAYLOAD, payload);

            // Get key from payload
            byte[] keyAsBytes = Payload.getPayloadElement(Payload.Element.KEY, actualPayload);
            String key = UTF8StringUtility.bytesUTF8ToString(keyAsBytes);
            int keyRequestID = HashUtility.simpleHash(key, HashUtility.DEFAULT_HASH_RANGE);

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
                        // Perform search in local kvstore
                        packetToSend = performGetOperation(key, header, returnAddress, returnPort, clientAddress, clientPort);
                    } else {
                        // Build error packet
                        byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                        byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
                        byte[] newMessage = Message.buildMessage(header, newPayload);
                        packetToSend = new DatagramPacket(newMessage, newMessage.length, returnAddress, returnPort);
                    }
                }
                // CASE 2) Null predecessor and empty successor list
                else {
                    // Build error packet
                    byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
                    byte[] newPayload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
                    byte[] newMessage = Message.buildMessage(header, newPayload);
                    packetToSend = new DatagramPacket(newMessage, newMessage.length, returnAddress, returnPort);
                }
            }
            else {
                // CASE 3) Predecessor set
                if(nlc.getSuccessorListLength() > 0) {
                    // Look at predecessor and self
                    // i) If self is correct IMS -> Do local search on kvstore
                    // ii) If not correct -> Send predecessor a potential ims get request
                    if(ImmediateSuccessorRouter.isSelfPotentialIMS(keyRequestID, predecessor.getId(), self.getId())) {
                        // Perform search in local kvstore
                        packetToSend = performGetOperation(key, header, returnAddress, returnPort, clientAddress, clientPort);

                    } else {
                        // Prepare potential ims get request to send to predecessor
                        byte[] newPayload = Payload.buildClientForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_GET,
                                returnAddress, returnPort, clientAddress, clientPort, actualPayload);
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
     * Performs a get operation on local kvstore and builds a response packet
     * @param key - Key to GET
     * @param header - Header to include in response packet
     * @param clientAddress - Client address to respond to
     * @param clientPort - Client port
     * @return
     */
    protected static DatagramPacket performGetOperation(String key, byte[] header, InetAddress destinationAddress, int destinationPort, InetAddress clientAddress,
                                                        int clientPort) {
        DatagramPacket packet = null;
        byte[] payload = null;

        KVStoreController kvStoreController = KVStoreController.getInstance();
        try {
            // Attempt to get value for input key from key value store
            String value = kvStoreController.get(key);
//            cacheController.add(ce);

                /* If key was found in key-value store, build a response payload with value and
                    with response code: Operation Success */

            byte[] valueToBytes = UTF8StringUtility.stringToBytesUTF8(value);
            byte[] stdPayload = Payload.buildStandardResponsePayload(ResponseCodes.OPERATION_SUCCESS, valueToBytes);
            payload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);

        } catch(KVStoreKeyNotFoundException e) {

                /* If key is not found in key-value store, build a response payload
                   with response code: Non-existent-key */

            byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.NON_EXISTENT_KEY);
            payload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
        }

        if(payload != null && header != null && destinationAddress != null) {
            try {
                byte[] message = Message.buildMessage(header, payload);
                packet = new DatagramPacket(message, message.length, destinationAddress, destinationPort);
            } catch(InvalidMessageException e) {
                e.printStackTrace();
            }

        }
        return packet;
    }





}
