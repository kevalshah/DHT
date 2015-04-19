package protocol;

import command.CHECKALIVEHandler;
import command.RequestCodes;
import command.ResponseCodes;
import message.Header;
import message.Message;
import message.Payload;
import nodelist.Node;
import nodelist.NodeListController;
import nodelist.RemovedNodeList;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class PredecessorCheckInitiator implements Runnable {

    protected byte[] buildPredecessorAliveRequestPayload() {
        return Payload.buildPayloadWithOnlyCommand(RequestCodes.PRED_ALIVE_REQ);
    }

    @Override
    public void run() {
        try {
            System.out.println("Predecessor check initiator is now running");
            NodeListController nlc = NodeListController.getInstance();
            byte[] payload = buildPredecessorAliveRequestPayload();
            while(true) {

                DatagramSocket socket = null;
                Node predecessor = nlc.getPredecessor();
                if(predecessor != null && JoinInitiator.isJoined) {

//                    System.out.println("PREDECESSOR CHECK ALIVE RUNNING");

                    boolean aliveReply = false;
                    try {
                        socket = new DatagramSocket();
                        socket.setSoTimeout(1000);
                        byte[] header = Header.buildMessageHeader();
                        byte[] message = Message.buildMessage(header, payload);
                        DatagramPacket packet = new DatagramPacket(message, message.length, predecessor.getHostname(), predecessor.getReceivingPort());

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
                                    if(extractedPayload[0] == ResponseCodes.PRED_ALIVE_REP) {
                                        aliveReply = true;
                                        break;
                                    }
                                }

                            } catch(SocketTimeoutException e) {
                                numAttempts++;
                                socket.setSoTimeout(socket.getSoTimeout() * 2);
                            }
                        }

                        if(!aliveReply) {
                            System.out.println("Removing predecessor");
                            nlc.removePredecessor(predecessor);
                        }

                    } catch(Exception e) {

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
