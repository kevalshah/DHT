package protocol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPSend {

    /**
     * Sends the packet to the respective destination as set in the input packet
     * @param packetToSend - Datagram packet to send
     */
    public static void sendPacket(DatagramPacket packetToSend) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();

            // Only send packet if not null
            if(packetToSend != null) {
//                System.out.println("Sending packet to " + packetToSend.getAddress() + ":" + packetToSend.getPort());
                socket.send(packetToSend);
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(socket != null) {
                socket.close();
            }
        }
    }

}
