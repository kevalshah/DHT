package protocol;

import command.*;
import message.Header;
import message.InvalidMessageException;
import message.Message;
import message.Payload;

import java.net.DatagramPacket;

public class MessageHandler implements Runnable {

    private DatagramPacket incomingPacket = null;

    public MessageHandler(DatagramPacket incomingPacket) {
        this.incomingPacket = incomingPacket;
    }


    protected DatagramPacket handleUNKNOWN() throws InvalidMessageException {
        byte[] message = incomingPacket.getData();
        byte[] header = Message.getBytesInRange(message, 0, Header.HEADER_SIZE_BYTES);
        byte[] responsePayload = null;

        responsePayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.UNRECOGNIZED_COMMAND);
        message = Message.buildMessage(header, responsePayload);
        return new DatagramPacket(message, message.length, incomingPacket.getAddress(), incomingPacket.getPort());
    }


    protected DatagramPacket handleMessage() throws Exception {
        DatagramPacket packetToSend = null;
        byte[] data = incomingPacket.getData();
        byte[] payload = Message.extractPayload(data);
        byte command = payload[0];
        switch(command) {
            case RequestCodes.GET:
                System.out.println("GET message received");
                packetToSend = GETHandler.handleClientGET(incomingPacket);
                break;

            case RequestCodes.FWD_GET:
                System.out.println("Forward GET message received");
                packetToSend = GETHandler.handleForwardGET(incomingPacket);
                break;

            case RequestCodes.POTENTIAL_IMS_GET:
                System.out.println("Potential IMS GET message received");
                packetToSend = GETHandler.handlePotentialIMSGET(incomingPacket);
                break;

            case RequestCodes.REPLICA_GET:
                System.out.println("Replica GET message received");
                packetToSend = GETHandler.handleReplicaGET(incomingPacket);
                break;

            case RequestCodes.PUT:
                System.out.println("PUT message received");
                packetToSend = PUTHandler.handleClientPUT(incomingPacket);
                break;

            case RequestCodes.FWD_PUT:
                System.out.println("Forward PUT message received");
                packetToSend = PUTHandler.handleForwardPUT(incomingPacket);
                break;

            case RequestCodes.POTENTIAL_IMS_PUT:
                System.out.println("Potential IMS PUT message received");
                packetToSend = PUTHandler.handlePotentialIMSPUT(incomingPacket);
                break;

            case RequestCodes.REPLICA_PUT:
                System.out.println("Replica PUT message received");
                packetToSend = PUTHandler.handleReplicaPUT(incomingPacket);
                break;

            case RequestCodes.REMOVE:
                System.out.println("REMOVE message received");
                packetToSend = REMOVEHandler.handleClientREMOVE(incomingPacket);
                break;

            case RequestCodes.FWD_REMOVE:
                System.out.println("Forward REMOVE message received");
                packetToSend = REMOVEHandler.handleForwardREMOVE(incomingPacket);
                break;

            case RequestCodes.POTENTIAL_IMS_REMOVE:
                System.out.println("Potential IMS REMOVE message received");
                packetToSend = REMOVEHandler.handlePotentialIMSREMOVE(incomingPacket);
                break;

            case RequestCodes.REPLICA_REMOVE:
                System.out.println("Replica REMOVE message received");
                packetToSend = REMOVEHandler.handleReplicaREMOVE(incomingPacket);
                break;

            case RequestCodes.SHUTDOWN:
                System.out.println("SHUTDOWN Message received");
                SHUTDOWNHandler.handleSHUTDOWNRequest(incomingPacket);
                break;

            case RequestCodes.JOIN_REQ:
                System.out.println("JOIN REQUEST message received");
                packetToSend = JOINHandler.handleJOINRequest(incomingPacket);
                break;

            case RequestCodes.FWD_JOIN:
                System.out.println("Forward JOIN REQUEST message received");
                packetToSend = JOINHandler.handleForwardJOINRequest(incomingPacket);
                break;

            case RequestCodes.POTENTIAL_IMS_JOIN:
                System.out.println("Potential IMS JOIN REQUEST message received");
                packetToSend = JOINHandler.handlePotentialImsJOINRequest(incomingPacket);
                break;

            case RequestCodes.NODE_LIST_REQUEST:
                System.out.println("NODE LIST REQUEST message received");
                packetToSend = NODELISTHandler.handleNODELISTRequest(incomingPacket);
                break;

            case RequestCodes.POTENTIAL_IMPS_UPDATE:
                System.out.println("POTENTIAL IMPS UPDATE message received");
                CHECKALIVEHandler.handlePotentialPredecessorUpdate(incomingPacket);
                break;

            case RequestCodes.PRED_ALIVE_REQ:
                packetToSend = CHECKALIVEHandler.handlePredecessorAliveRequest(incomingPacket);
                break;

            case RequestCodes.SUCC_ALIVE_REQ:
                packetToSend = CHECKALIVEHandler.handleSuccessorAliveRequest(incomingPacket);
                break;

            default:
                System.out.println("Unknown message received");
                packetToSend = handleUNKNOWN();
                break;
        }

        return packetToSend;

    }



    @Override
    public void run() {
        if(incomingPacket != null) {
            DatagramPacket packetToSend = null;
            try {
                packetToSend = handleMessage();
            } catch(Exception e) {
                e.printStackTrace();
            }
            if(packetToSend != null) {
                UDPSend.sendPacket(packetToSend);
            }
        }
    }
}
