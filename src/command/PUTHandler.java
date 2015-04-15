package command;

import algorithm.ImmediateSuccessorRouter;
import algorithm.NoPotentialIMSException;
import kvstore.KVStoreController;
import kvstore.KVStoreFullException;
import kvstore.KVStoreInvalidKeyOrValueFormatException;
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

public class PUTHandler {

    /**
     * Handles client put requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleClientPUT(DatagramPacket incomingPacket) {
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);
        byte[] payload = Message.extractPayload(message);

        DatagramPacket packetToSend = null;
        NodeListController nlc = NodeListController.getInstance();

        try {
            // Get key from payload
            byte[] keyAsBytes = Payload.getPayloadElement(Payload.Element.KEY, payload);
            String key = UTF8StringUtility.bytesUTF8ToString(keyAsBytes);
            int keyRequestID = HashUtility.hashString(key);

            // Get value from payload
            byte[] valueAsBytes = Payload.getPayloadElement(Payload.Element.REQUEST_VALUE, payload);
            String value = UTF8StringUtility.bytesUTF8ToString(valueAsBytes);


            Node predecessor = nlc.getPredecessor();
            Node self = nlc.getSelf();
            if(predecessor == null) {
                // CASE 1) Null predecessor and filled successor list
                if(nlc.getSuccessorListLength() > 0) {
                    // i) Compare request ID with self -> If same, perform search in local kvstore
                    // ii) If above fails, look in successor list. If no potential ims found -> Send forward request to last successor
                    // ii) If potential ims found in successor list -> Send potential ims request to element

                    if(self.getId() == keyRequestID) {
                        // Perform put operation on local kvstore
                        packetToSend = performPutOperation(key, value, header, incomingPacket.getAddress(), incomingPacket.getPort());
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, -1,
                                    self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims put packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_PUT, incomingPacket.getAddress(), incomingPacket.getPort(), payload);
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
                                // Prepare forward put packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_PUT, incomingPacket.getAddress(), incomingPacket.getPort(), payload);
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
                        // Perform put operation on local kvstore
                        packetToSend = performPutOperation(key, value, header, incomingPacket.getAddress(), incomingPacket.getPort());
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
                        // Perform put operation on local kvstore
                        packetToSend = performPutOperation(key, value, header, incomingPacket.getAddress(), incomingPacket.getPort());
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, predecessor.getId(), self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims put packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_PUT, incomingPacket.getAddress(), incomingPacket.getPort(), payload);
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
                                // Prepare forward put packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_PUT, incomingPacket.getAddress(), incomingPacket.getPort(), payload);
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
     * Handles forwarding put requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleForwardPUT(DatagramPacket incomingPacket) {
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

            // Get value from payload
            byte[] valueAsBytes = Payload.getPayloadElement(Payload.Element.REQUEST_VALUE, actualPayload);
            String value = UTF8StringUtility.bytesUTF8ToString(valueAsBytes);

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
                        // Perform put operation on local kvstore
                        packetToSend = performPutOperation(key, value, header, clientAddress, clientPort);
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, -1, self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims put packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_PUT, clientAddress, clientPort, actualPayload);
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
                                // Prepare forward put packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_PUT, clientAddress, clientPort, actualPayload);
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
                        // Perform put operation on local kvstore
                        packetToSend = performPutOperation(key, value, header, clientAddress, clientPort);
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, predecessor.getId(), self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims put packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_PUT, clientAddress, clientPort, actualPayload);
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
                                // Prepare forward put packet
                                byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_PUT, clientAddress, clientPort, actualPayload);
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
     * Handles replica put requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handleReplicaPUT(DatagramPacket incomingPacket) {
        DatagramPacket packetToSend = null;




        return packetToSend;
    }



    /**
     * Handles potential ims put requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static DatagramPacket handlePotentialIMSPUT(DatagramPacket incomingPacket) {
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

            // Get value from payload
            byte[] valueAsBytes = Payload.getPayloadElement(Payload.Element.REQUEST_VALUE, actualPayload);
            String value = UTF8StringUtility.bytesUTF8ToString(valueAsBytes);

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
                        // Perform put operation on local kvstore
                        packetToSend = performPutOperation(key, value, header, clientAddress, clientPort);
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
                    // ii) If not correct -> Send predecessor a potential ims put request
                    if(ImmediateSuccessorRouter.isSelfPotentialIMS(keyRequestID, predecessor.getId(), self.getId())) {
                        // Perform put operation on local kvstore
                        packetToSend = performPutOperation(key, value, header, clientAddress, clientPort);


                    } else {
                        // Prepare potential ims put request to send to predecessor
                        byte[] newPayload = Payload.buildForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_PUT, clientAddress, clientPort, actualPayload);
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
     * Performs a put operation on local kvstore and builds a response packet
     * @param key - Key to store
     * @param value - Value associated with key
     * @param header - Header to include in response packet
     * @param clientAddress - Client address
     * @param clientPort - Client port
     * @return
     */
    protected static DatagramPacket performPutOperation(String key, String value, byte[] header, InetAddress clientAddress, int clientPort) {
        DatagramPacket packet = null;
        byte[] payload = null;

        KVStoreController kvStoreController = KVStoreController.getInstance();
        try {
            // Attempt to put key-value pair in key value store
            kvStoreController.put(key, value);
//            cacheController.add(ce);

            /* If key was put in key-value store, build a response payload
               with response code: Operation Success */

            payload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.OPERATION_SUCCESS);
            System.out.println("PUT operation succeeded");

            // Forward to replicas only if <key, value> was inserted
//            ReplicaForward.forwardPUTtoReplica(header, keyAsBytes, valueAsBytes);

        } catch(KVStoreFullException e) {

            /* If key-value store is full, build a response payload
               with response code: Out of Space */

            payload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.OUT_OF_SPACE);
            System.out.println("PUT operation failed - out of space");

        } catch(KVStoreInvalidKeyOrValueFormatException e) {

            /* If key or value is in wrong format, build a response payload
               with response code: Internal KVStore Failure */

            payload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
            System.out.println("PUT operation failed - Internal KV Store Failure");
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
