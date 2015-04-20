package command;

import algorithm.ImmediateSuccessorRouter;
import algorithm.NoPotentialIMSException;
import cache.CacheController;
import cache.CacheEntry;
import kvstore.KVStoreController;
import kvstore.KVStoreFullException;
import kvstore.KVStoreInvalidKeyOrValueFormatException;
import message.BadValueLengthException;
import message.InvalidMessageException;
import message.Message;
import message.Payload;
import nodelist.Node;
import nodelist.NodeListController;
import protocol.ReplicaForward;
import timestamp.Timestamp;
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
            int keyRequestID = HashUtility.simpleHash(key, HashUtility.DEFAULT_HASH_RANGE);

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
                        packetToSend = performPutOperation(key, value, header, self.getHostname(), self.getReceivingPort(), incomingPacket.getAddress(), incomingPacket.getPort());
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, -1,
                                    self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims put packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(
                                        RequestCodes.POTENTIAL_IMS_PUT, self.getHostname(), self.getReceivingPort(),
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
                                // Prepare forward put packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(RequestCodes.FWD_PUT,
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
                    // Perform put operation on local kvstore
                    packetToSend = performPutOperation(key, value, header, self.getHostname(), self.getReceivingPort(), incomingPacket.getAddress(), incomingPacket.getPort());
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
                        packetToSend = performPutOperation(key, value, header, self.getHostname(), self.getReceivingPort(), incomingPacket.getAddress(), incomingPacket.getPort());
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, predecessor.getId(), self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims put packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(
                                        RequestCodes.POTENTIAL_IMS_PUT, self.getHostname(), self.getReceivingPort(),
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
                                // Prepare forward put packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(RequestCodes.FWD_PUT,
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
            InetAddress returnAddress = InetAddress.getByAddress(Payload.getPayloadElement(Payload.Element.IP_ADDRESS, payload));
            int returnPort = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            InetAddress clientAddress = InetAddress.getByAddress(Payload.getPayloadElement(Payload.Element.CLIENT_IP_ADDRESS, payload));
            int clientPort = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.CLIENT_PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            byte[] actualPayload = Payload.getPayloadElement(Payload.Element.CLIENT_FORWARD_PAYLOAD, payload);

            // Get key from payload
            byte[] keyAsBytes = Payload.getPayloadElement(Payload.Element.KEY, actualPayload);
            String key = UTF8StringUtility.bytesUTF8ToString(keyAsBytes);
            int keyRequestID = HashUtility.simpleHash(key, HashUtility.DEFAULT_HASH_RANGE);

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
                        packetToSend = performPutOperation(key, value, header, returnAddress, returnPort, clientAddress, clientPort);
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, -1, self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims put packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(
                                        RequestCodes.POTENTIAL_IMS_PUT, returnAddress, returnPort, clientAddress,
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
                                // Prepare forward put packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(RequestCodes.FWD_PUT,
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
                        // Perform put operation on local kvstore
                        packetToSend = performPutOperation(key, value, header, returnAddress, returnPort, clientAddress, clientPort);
                    } else {
                        int successorList[] = nlc.getSuccessorListIDs();
                        try {
                            int potentialIMSID = ImmediateSuccessorRouter.findPotentialIMS(keyRequestID, predecessor.getId(), self.getId(), successorList);
                            Node potentialIMS = nlc.getNodeByID(potentialIMSID);
                            if(potentialIMS != null) {
                                // Prepare potential ims put packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(
                                        RequestCodes.POTENTIAL_IMS_PUT, returnAddress, returnPort, clientAddress,
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
                                // Prepare forward put packet
                                byte[] newPayload = Payload.buildClientForwardingRequestPayload(RequestCodes.FWD_PUT,
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
     * Handles replica put requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static void handleReplicaPUT(DatagramPacket incomingPacket) {
        byte[] message = incomingPacket.getData();
        byte[] payload = Message.extractPayload(message);

        try {
            // Get key from payload
            String key = UTF8StringUtility.bytesUTF8ToString(Payload.getPayloadElement(Payload.Element.KEY, payload));

            // Get value from payload
            String value = UTF8StringUtility.bytesUTF8ToString(Payload.getPayloadElement(Payload.Element.REQUEST_VALUE, payload));

            // Perform a put operation
            performPutOperation(key, value, null, null, -1, null, -1);


        } catch(Exception e) {
            e.printStackTrace();
        }

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
            InetAddress returnAddress = InetAddress.getByAddress(Payload.getPayloadElement(Payload.Element.IP_ADDRESS, payload));
            int returnPort = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            InetAddress clientAddress = InetAddress.getByAddress(Payload.getPayloadElement(Payload.Element.CLIENT_IP_ADDRESS, payload));
            int clientPort = ByteBuffer.wrap(Payload.getPayloadElement(Payload.Element.CLIENT_PORT, payload)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            byte[] actualPayload = Payload.getPayloadElement(Payload.Element.CLIENT_FORWARD_PAYLOAD, payload);

            // Get key from payload
            byte[] keyAsBytes = Payload.getPayloadElement(Payload.Element.KEY, actualPayload);
            String key = UTF8StringUtility.bytesUTF8ToString(keyAsBytes);
            int keyRequestID = HashUtility.simpleHash(key, HashUtility.DEFAULT_HASH_RANGE);

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
                        packetToSend = performPutOperation(key, value, header, returnAddress, returnPort, clientAddress, clientPort);
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
                    // ii) If not correct -> Send predecessor a potential ims put request
                    if(ImmediateSuccessorRouter.isSelfPotentialIMS(keyRequestID, predecessor.getId(), self.getId())) {
                        // Perform put operation on local kvstore
                        packetToSend = performPutOperation(key, value, header, returnAddress, returnPort, clientAddress, clientPort);


                    } else {
                        // Prepare potential ims put request to send to predecessor
                        byte[] newPayload = Payload.buildClientForwardingRequestPayload(RequestCodes.POTENTIAL_IMS_PUT,
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
     * Performs a put operation on local kvstore and builds a response packet
     * @param key - Key to store
     * @param value - Value associated with key
     * @param header - Header to include in response packet
     * @param clientAddress - Client address
     * @param clientPort - Client port
     * @return
     */
    protected static DatagramPacket performPutOperation(String key, String value, byte[] header, InetAddress destinationAddress, int destinationPort, InetAddress clientAddress, int clientPort) {
        DatagramPacket packet = null;
        byte[] payload = null;

        CacheController cacheController = CacheController.getInstance();
        KVStoreController kvStoreController = KVStoreController.getInstance();
        try {

            CacheEntry cacheEntry = cacheController.isUniqueIDInCache(header);
            if(cacheEntry == null) {
                // Add new cache entry
                cacheController.addToCache(header);
            }
            else {
                // If cache entry was found, check if expired
                if(cacheEntry.isExpired()) {
                    // If expired, remove from cache and add new cache entry
                    cacheController.removeAllExpiredEntries();
                    cacheController.addToCache(header);
                } else {
                    // If not expired - build error packet as request has already been serviced

                    // For now returning null
                    return null;
                }

            }


            // Attempt to put key-value pair in key value store
            kvStoreController.put(key, value);


            /* If key was put in key-value store, build a response payload
               with response code: Operation Success */

            byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.OPERATION_SUCCESS);
            payload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
            System.out.println("PUT operation succeeded");

            // Forward to replicas only if <key, value> was inserted
            ReplicaForward.forwardPUTtoReplica(header, key, value);

        } catch(KVStoreFullException e) {

            /* If key-value store is full, build a response payload
               with response code: Out of Space */

            byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.OUT_OF_SPACE);
            payload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
            System.out.println("PUT operation failed - out of space");

        } catch(KVStoreInvalidKeyOrValueFormatException e) {

            /* If key or value is in wrong format, build a response payload
               with response code: Internal KVStore Failure */

            byte[] stdPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.INTERNAL_KVSTORE_FAILURE);
            payload = Payload.buildForwardingRequestPayload(ResponseCodes.CLIENT_FWD_RESPONSE, clientAddress, clientPort, stdPayload);
            System.out.println("PUT operation failed - Internal KV Store Failure");
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
