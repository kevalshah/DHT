package command;

import message.InvalidMessageException;
import message.Message;
import message.Payload;
import protocol.UDPSend;

import java.net.DatagramPacket;

public class SHUTDOWNHandler {


    /**
     * Handles shutdown requests
     * @param incomingPacket - Incoming packet
     * @return
     */
    public static void handleSHUTDOWNRequest(DatagramPacket incomingPacket) {
        byte[] message = incomingPacket.getData();
        byte[] header = Message.extractHeader(message);

        DatagramPacket packetToSend = null;
        byte[] newPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.OPERATION_SUCCESS);
        try {
            byte[] newMessage = Message.buildMessage(header, newPayload);
            packetToSend = new DatagramPacket(newMessage, newMessage.length, incomingPacket.getAddress(), incomingPacket.getPort());
            UDPSend.sendPacket(packetToSend);
            System.exit(-1);
        } catch(InvalidMessageException e) {
            e.printStackTrace();
        }
        }



}
