package protocol;


import command.JOINHandler;
import command.RequestCodes;
import command.ResponseCodes;
import main.Server;
import message.Header;
import message.InvalidMessageException;
import message.Message;
import message.Payload;
import nodelist.Node;
import nodelist.NodeListController;
import utility.NodeSerializerUtility;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

public class JoinInitiator implements Runnable {

    public static boolean isJoined = false;

    protected byte[] buildNewJoinRequestPayload() {
        NodeListController nlc = NodeListController.getInstance();

        // Prepare join list
        Node self = nlc.getSelf();
        ArrayList<Node> joinList = new ArrayList<Node>();
        joinList.add(self);
        byte[] joinListAsBytes = NodeSerializerUtility.serializeNodeList(joinList);

        byte[] payload = Payload.buildPayloadWithNodeList(RequestCodes.JOIN_REQ, joinListAsBytes);
        return payload;
    }


    @Override
    public void run() {
        try {
            System.out.println("Join Initiator is now running");
            NodeListController.getInstance().printView();
            byte[] payload = buildNewJoinRequestPayload();
            while(true) {

                DatagramSocket socket = null;
                if(!isJoined && Server.contactNodeAddress != null && Server.contactNodePort >= 0) {
                    try {
                        socket = new DatagramSocket();
                        socket.setSoTimeout(15000);
                        byte[] header = Header.buildMessageHeader();
                        byte[] message = Message.buildMessage(header, payload);
                        DatagramPacket packet = new DatagramPacket(message, message.length, Server.contactNodeAddress, Server.contactNodePort);
                        byte[] receiveBuffer = new byte[17000];
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                        try {
                            socket.send(packet);
                            socket.receive(receivePacket);
                            byte[] data = receivePacket.getData();
                            byte[] extractedHeader = Message.extractHeader(data);
                            byte[] extractedPayload = Message.extractPayload(data);

                            // Compare unique IDs
                            if(Arrays.equals(header, extractedHeader)) {
                                if(extractedPayload[0] == ResponseCodes.JOIN_REP) {
                                    System.out.println("Join Response message received");
                                    JOINHandler.handleJOINResponse(receivePacket);
                                }
                            }

                        } catch(SocketTimeoutException e) {

                        }
                    } catch(Exception e) {

                    } finally {
                        if(socket != null) {
                            socket.close();
                        }
                    }
                }

                Thread.sleep(5000);
                NodeListController.getInstance().printView();

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
