package message;

import java.util.Arrays;

/**
 * Represents a message (Header + Payload)
 */
public class Message {

    /**
     * Builds a message with a header and payload
     * @param header - Header to add to message (cannot be null)
     * @param payload - Payload to add to message (cannot be null)
     * @return - Byte array representation of message (header + payload)
     * @throws message.InvalidMessageException
     */
    public static byte[] buildMessage(byte[] header, byte[] payload) throws InvalidMessageException{

        byte[] message;

        try {
            message = new byte[header.length + payload.length];

            // Copy header into message
            System.arraycopy(header, 0, message, 0, header.length);

            // Copy payload into message
            System.arraycopy(payload, 0, message, header.length, payload.length);
        } catch(Exception e) {
            throw new InvalidMessageException("Invalid header or payload");
        }

        return message;
    }


    /**
     * Builds a message with a header
     * @param header - Header to add to message (cannot be null)
     * @return - Byte array representation of message (header)
     * @throws message.InvalidMessageException
     */
    public static byte[] buildMessage(byte[] header) throws InvalidMessageException{

        byte[] message;

        try {
            message = new byte[header.length];

            // Copy header into message
            System.arraycopy(header, 0, message, 0, header.length);

        } catch(Exception e) {
            throw new InvalidMessageException("Invalid header");
        }

        return message;
    }


    /**
     * Extracts header from a message
     * @param message - Message to extract header from
     * @return - Byte array representing header
     */
    public static byte[] extractHeader(byte[] message) {
        return Arrays.copyOfRange(message, 0, Header.HEADER_SIZE_BYTES);
    }


    /**
     * Extracts payload from a message
     * @param message - Message to extract payload from
     * @return - Byte array representing payload
     */
    public static byte[] extractPayload(byte[] message) {
        return Arrays.copyOfRange(message, Header.HEADER_SIZE_BYTES, message.length);
    }


    /**
     * Gets bytes in specified range from a message
     * @param message - Message to get bytes from
     * @param startIndex - Start index of range
     * @param lengthToCopy - Length to copy in bytes from start index
     * @return - Bytes in the range specified from message
     * @throws message.InvalidMessageException
     */
    public static byte[] getBytesInRange(byte[] message, int startIndex, int lengthToCopy) throws InvalidMessageException {

        byte[] range;

        try {
            range = new byte[lengthToCopy];
            System.arraycopy(message, startIndex, range, 0, lengthToCopy);
        } catch(Exception e) {
            throw new InvalidMessageException("Invalid message or range specified");
        }

        return range;
    }


}
