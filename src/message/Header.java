package message;

import utility.HexadecimalStringUtility;
import utility.UTF8StringUtility;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;


/**
 * Represents the header for messages
 */
public class Header {

    // Size of unique ID elements
    private static final int IP_ADDR_SIZE_BYTES = 4;
    private static final int RECEIVING_PORT_SIZE_BYTES = 2;
    private static final int RANDOM_VALUE_SIZE_BYTES = 2;
    private static final int CURRENT_TIME_SIZE_BYTES = 8;
    public static final int UNIQUE_ID_SIZE_BYTES = IP_ADDR_SIZE_BYTES + RECEIVING_PORT_SIZE_BYTES + RANDOM_VALUE_SIZE_BYTES + CURRENT_TIME_SIZE_BYTES; // Currently 16
    public static final int HEADER_SIZE_BYTES = UNIQUE_ID_SIZE_BYTES;

    // Start indexes for unique ID
    private static final int IP_ADDR_START_INDEX = 0;
    private static final int RECEIVING_PORT_START_INDEX = IP_ADDR_START_INDEX + IP_ADDR_SIZE_BYTES;
    private static final int RANDOM_VALUE_START_INDEX = RECEIVING_PORT_START_INDEX + RECEIVING_PORT_SIZE_BYTES;
    private static final int CURRENT_TIME_START_INDEX = RANDOM_VALUE_START_INDEX + RANDOM_VALUE_SIZE_BYTES;


    /**
     * Builds a unique ID
     * @return - Byte array containing the unique ID
     */
    protected static byte[] buildUniqueID(short receivingPort) {

        // Allocate 16 bytes to buffer
        ByteBuffer uniqueID = ByteBuffer.allocate(UNIQUE_ID_SIZE_BYTES);
        uniqueID.order(ByteOrder.LITTLE_ENDIAN);

        try {
            // Add Local Host IP Address as first 4 bytes in unique ID
            byte[] localIPAddr = InetAddress.getLocalHost().getAddress();
            uniqueID.put(localIPAddr, IP_ADDR_START_INDEX, IP_ADDR_SIZE_BYTES);

            // Add Source Port as next 2 bytes in unique ID
            uniqueID.putShort(RECEIVING_PORT_START_INDEX, receivingPort);

            // Add random bytes as next 2 bytes in unique ID
            short randomVal = (short) new Random().nextInt(Short.MAX_VALUE);
            uniqueID.putShort(RANDOM_VALUE_START_INDEX, randomVal);

            // Add current time as next 8 bytes in unique ID
            long currentTime = System.currentTimeMillis();
            uniqueID.putLong(CURRENT_TIME_START_INDEX, currentTime);

        } catch(UnknownHostException e) {
            e.printStackTrace();
            return null;
        }

        return uniqueID.array();
    }


    /**
     * Builds a message header which includes a unique ID
     * @param receivingPort - Receiving port for the local host
     * @return - Byte array representing the message header
     */
    public static byte[] buildMessageHeader(int receivingPort) {

        byte[] messageHeader = null;

        if(receivingPort > Short.MAX_VALUE) {
            messageHeader = buildUniqueID((short)0);
        } else {
            messageHeader = buildUniqueID((short)receivingPort);
        }

        return messageHeader;
    }


    /**
     * Builds a message header which includes a unique ID
     * @return - Byte array representing the message header
     */
    public static byte[] buildMessageHeader() {
        byte[] messageHeader = null;

        // Uses a random receiving port
        short receivingPort = (short)new Random().nextInt(Short.MAX_VALUE);
        messageHeader = buildUniqueID(receivingPort);

        return messageHeader;
    }


    /**
     * Converts header to a string representation
     * @param header - Header to convert
     * @return - String representation of header
     */
    public static String convertToString(byte[] header) {
        if(header.length < UNIQUE_ID_SIZE_BYTES) {
            return "Header: Invalid Header";
        }

        byte[] uniqueID = new byte[UNIQUE_ID_SIZE_BYTES];
        System.arraycopy(header, 0, uniqueID, 0, UNIQUE_ID_SIZE_BYTES);

        return "Header: " + HexadecimalStringUtility.bytesToHex(uniqueID);
    }



}
