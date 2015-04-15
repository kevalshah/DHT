package protocol;

import main.Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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

//                // Print packet source information
//                InetAddress IPAddress = receivePacket.getAddress();
//                int port = receivePacket.getPort();
//                System.out.println("RECEIVED Packet: " + IPAddress.getHostName() + ":" + port);

                // Start new message handler thread
                Thread messageHandlerThread = new Thread(new MessageHandler(receivePacket));
                messageHandlerThread.start();
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
