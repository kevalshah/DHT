package protocol;

import command.CHECKALIVEHandler;
import command.RequestCodes;
import command.ResponseCodes;
import message.Header;
import message.Message;
import message.Payload;
import nodelist.Node;
import nodelist.NodeListController;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class SuccessorCheckInitiator implements Runnable {

    protected byte[] buildPredecessorAliveRequestPayload() {
        return Payload.buildPayloadWithOnlyCommand(RequestCodes.SUCC_ALIVE_REQ);
    }


    @Override
    public void run() {
        try {
            System.out.println("Successor check initiator is now running");
            NodeListController nlc = NodeListController.getInstance();
            byte[] payload = buildPredecessorAliveRequestPayload();
            while(true) {

                DatagramSocket socket = null;
                Node firstSuccessor = nlc.getFirstSuccessor();
                if(firstSuccessor != null && JoinInitiator.isJoined) {

//                    System.out.println("SUCCESSOR CHECK ALIVE RUNNING");

                    boolean aliveReply = false;
                    try {
                        socket = new DatagramSocket();
                        socket.setSoTimeout(1000);
                        byte[] header = Header.buildMessageHeader();
                        byte[] message = Message.buildMessage(header, payload);
                        DatagramPacket packet = new DatagramPacket(message, message.length, firstSuccessor.getHostname(), firstSuccessor.getReceivingPort());

                        int numAttempts = 0;
                        while(numAttempts < 3) {
                            try {
                                socket.send(packet);

                                byte[] receiveBuffer = new byte[17000];
                                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                                socket.receive(receivePacket);
                                byte[] data = receivePacket.getData();
                                byte[] extractedHeader = Message.extractHeader(data);
                                byte[] extractedPayload = Message.extractPayload(data);

                                // Compare unique IDs
                                if(Arrays.equals(header, extractedHeader)) {
                                    if(extractedPayload[0] == ResponseCodes.SUCC_ALIVE_REP) {
                                        CHECKALIVEHandler.handleSuccessorAliveResponse(receivePacket);
                                        aliveReply = true;
                                        break;
                                    }
                                }

                            } catch(SocketTimeoutException e) {
                                numAttempts++;
                                socket.setSoTimeout(socket.getSoTimeout() * 2);
                            }
                        }

                        if(aliveReply == false) {
                            System.out.println("Removing first successor");
                            nlc.removeFirstSuccessor();
                        }

                    } catch(Exception e) {
                        e.printStackTrace();
                    } finally {
                        if(socket != null) {
                            socket.close();
                        }

                    }
                }

                Thread.sleep(1000);

            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
