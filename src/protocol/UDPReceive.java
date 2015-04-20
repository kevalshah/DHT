package protocol;

import command.NODELISTHandler;
import command.RequestCodes;
import command.ResponseCodes;
import main.Server;
import message.Message;
import message.Payload;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Thread that is responsible for receiving incoming UDP packets
 */
public class UDPReceive implements Runnable {

    private DatagramSocket socket;
    private final static int MAX_BUFFER_SIZE = 17000;

    public UDPReceive(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {

            // Check if receiving socket is null. If so, create new socket.
            if(socket == null) {
                socket = new DatagramSocket();
                Server.localPort = socket.getLocalPort();
            }

            System.out.println("UDPReceive is running");
            System.out.println("Receiving on port " + socket.getLocalPort());

            while(true) {

                byte[] receiveData = new byte[MAX_BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                byte[] message = receivePacket.getData();
                byte[] header = Message.extractHeader(message);
                byte[] payload = Message.extractPayload(message);

                byte[] clientPayload;
                byte[] messageToSend;
                DatagramPacket packet;
                byte command = payload[0];
                switch(command) {
                    case ResponseCodes.CLIENT_FWD_RESPONSE:
                        clientPayload = Payload.getPayloadElement(Payload.Element.REGULAR_FORWARD_PAYLOAD, payload);
                        InetAddress clientAddress = InetAddress.getByAddress(Payload.getPayloadElement(Payload.Element.IP_ADDRESS, payload));
                        int clientPort = ByteBuffer.wrap(
                                Payload.getPayloadElement(Payload.Element.PORT, payload)).order(
                                ByteOrder.LITTLE_ENDIAN).getInt();
                        messageToSend = Message.buildMessage(header, clientPayload);
                        packet = new DatagramPacket(messageToSend, messageToSend.length, clientAddress, clientPort);
                        socket.send(packet);
                        break;
                    case RequestCodes.SHUTDOWN:
                        System.out.println("SHUTDOWN command received");
                        clientPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.OPERATION_SUCCESS);
                        messageToSend = Message.buildMessage(header, clientPayload);
                        packet = new DatagramPacket(messageToSend, messageToSend.length, receivePacket.getAddress(), receivePacket.getPort());
                        socket.send(packet);
                        socket.close();
                        System.exit(0);
                        break;
                    case RequestCodes.NODE_LIST_REQUEST:
                        System.out.println("NODELIST command received");
                        packet = NODELISTHandler.handleNODELISTRequest(receivePacket);
                        socket.send(packet);
                        break;
                    default:
                        if(RequestCodes.isRecognizedRequestCode(command) || ResponseCodes.isRecognizedResponseCode(command)) {
                            // Start new message handler thread
                            Thread messageHandlerThread = new Thread(new MessageHandler(receivePacket));
                            messageHandlerThread.start();
                        } else {
                            System.out.println("??? command received");
                            clientPayload = Payload.buildPayloadWithOnlyCommand(ResponseCodes.UNRECOGNIZED_COMMAND);
                            messageToSend = Message.buildMessage(header, clientPayload);
                            packet = new DatagramPacket(messageToSend, messageToSend.length, receivePacket.getAddress(), receivePacket.getPort());
                            socket.send(packet);
                        }
                        break;
                }

            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if(socket != null) {
                socket.close();
                socket = null;
            }
        }




    }
}
