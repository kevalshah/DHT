package message;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Represents payload for messages
 */
public class Payload {

    /**
     * Enumeration constants for different types of payload elements
     */
    public enum Element {
        COMMAND, KEY, REQUEST_VALUE_LENGTH,
        REQUEST_VALUE, RESPONSE_VALUE_LENGTH,
        RESPONSE_VALUE, IP_ADDRESS, PORT,
        ACTUAL_PAYLOAD_LENGTH, ACTUAL_PAYLOAD,
        NODE_LIST_LENGTH, NODE_LIST
    }

    // Element sizes - General
    public static final int COMMAND_CODE_SIZE_BYTES = 1;
    public static final int KEY_SIZE_BYTES = 32;
    public static final int VALUE_LENGTH_SIZE_BYTES = 2;
    public static final int MAX_VALUE_SIZE_BYTES = 15000;
    public static final int IP_SIZE_BYTES = 4;
    public static final int PORT_SIZE_BYTES = 4;
    public static final int ACTUAL_PAYLOAD_LENGTH_SIZE_BYTES = 4;
    public static final int NODE_LIST_LENGTH_SIZE_BYTES = 4;
    public static final int CUSTOM_VALUE_LENGTH_SIZE_BYTES = 4;

    // Start indexes - Standard Request payload
    public final static int REQUEST_CODE_START_INDEX = 0;
    public final static int KEY_START_INDEX = REQUEST_CODE_START_INDEX + COMMAND_CODE_SIZE_BYTES;
    public final static int REQUEST_VALUE_LENGTH_START_INDEX = KEY_START_INDEX + KEY_SIZE_BYTES;
    public final static int REQUEST_VALUE_START_INDEX = REQUEST_VALUE_LENGTH_START_INDEX + VALUE_LENGTH_SIZE_BYTES;

    // Start indexes - Standard Response payload
    public final static int RESPONSE_CODE_START_INDEX = 0;
    public final static int RESPONSE_VALUE_LENGTH_START_INDEX = RESPONSE_CODE_START_INDEX + COMMAND_CODE_SIZE_BYTES;
    public final static int RESPONSE_VALUE_START_INDEX = RESPONSE_VALUE_LENGTH_START_INDEX + VALUE_LENGTH_SIZE_BYTES;

    // Start indexes - Forwarding Request payload
    public final static int FORWARDING_REQUEST_START_INDEX = 0;
    public final static int IP_START_INDEX = FORWARDING_REQUEST_START_INDEX + COMMAND_CODE_SIZE_BYTES;
    public final static int PORT_START_INDEX = IP_START_INDEX + IP_SIZE_BYTES;
    public final static int ACTUAL_PAYLOAD_LENGTH_START_INDEX = PORT_START_INDEX + PORT_SIZE_BYTES;
    public final static int ACTUAL_PAYLOAD_START_INDEX = ACTUAL_PAYLOAD_LENGTH_START_INDEX + ACTUAL_PAYLOAD_LENGTH_SIZE_BYTES;

    // Start indexes - Check Alive/Join Request and Response payload
    public final static int CHK_ALIVE_OR_JOIN_COMMAND_START_INDEX = 0;
    public final static int NODE_LIST_LENGTH_START_INDEX = CHK_ALIVE_OR_JOIN_COMMAND_START_INDEX + COMMAND_CODE_SIZE_BYTES;
    public final static int NODE_LIST_START_INDEX = NODE_LIST_LENGTH_START_INDEX + NODE_LIST_LENGTH_SIZE_BYTES;


    /**
     * Build payload with only a command value
     * @param command - Command to build payload with
     * @return - Byte array representing payload
     */
    public static byte[] buildPayloadWithOnlyCommand(byte command) {
        byte[] payload = new byte[1];
        payload[0] = command;
        return payload;
    }


    /**
     Builds a request payload with a request code and key
     @param requestCode - request command
     @param key - key
     @return - byte array representing request payload
     */
    public static byte[] buildStandardRequestPayload(byte requestCode, byte[] key) {
        byte[] requestPayload = new byte[COMMAND_CODE_SIZE_BYTES + KEY_SIZE_BYTES];
        // Add request code
        requestPayload[REQUEST_CODE_START_INDEX] = requestCode;
        // Add key
        System.arraycopy(key, 0, requestPayload, KEY_START_INDEX, KEY_SIZE_BYTES);
        return requestPayload;
    }


    /**
     * Builds a request payload with a request code, key, and value
     * @param requestCode - request command
     * @param key - key
     * @param value - value associated with key (Max. length = 15000 bytes)
     * @return - byte array representing request payload
     */
    public static byte[] buildStandardRequestPayload(byte requestCode, byte[] key, byte[] value) {
        byte[] requestPayload = new byte[COMMAND_CODE_SIZE_BYTES + KEY_SIZE_BYTES + VALUE_LENGTH_SIZE_BYTES + Math.min(value.length, MAX_VALUE_SIZE_BYTES)];
        // Add request code
        requestPayload[REQUEST_CODE_START_INDEX] = requestCode;
        // Add key
        System.arraycopy(key, 0, requestPayload, KEY_START_INDEX, KEY_SIZE_BYTES);
        // Add value
        System.arraycopy(value, 0, requestPayload, REQUEST_VALUE_START_INDEX, Math.min(value.length, MAX_VALUE_SIZE_BYTES));
        // Add value length
        ByteBuffer req_buf = ByteBuffer.wrap(requestPayload);
        req_buf.order(ByteOrder.LITTLE_ENDIAN);
        req_buf.putShort(REQUEST_VALUE_LENGTH_START_INDEX, (short) Math.min((value.length), MAX_VALUE_SIZE_BYTES));
        return req_buf.array();
    }


    /**
     * Builds a response payload with response code and value
     * @param responseCode - response code
     * @param value - value (Max length = 15000 bytes)
     * @return - byte array representing response payload
     */
    public static byte[] buildStandardResponsePayload(byte responseCode, byte[] value) {
        byte[] responsePayload = new byte[COMMAND_CODE_SIZE_BYTES + VALUE_LENGTH_SIZE_BYTES + Math.min(value.length, MAX_VALUE_SIZE_BYTES)];
        // Add response code
        responsePayload[RESPONSE_CODE_START_INDEX] = responseCode;
        // Add value
        System.arraycopy(value, 0, responsePayload, RESPONSE_VALUE_START_INDEX, Math.min(value.length, MAX_VALUE_SIZE_BYTES));
        // Add value length
        ByteBuffer resp_buf = ByteBuffer.wrap(responsePayload);
        resp_buf.order(ByteOrder.LITTLE_ENDIAN);
        resp_buf.putShort(RESPONSE_VALUE_LENGTH_START_INDEX, (short) Math.min((value.length), MAX_VALUE_SIZE_BYTES));
        return resp_buf.array();
    }


    /**
     * Builds a request payload for forwarding requests (GET, PUT, or REMOVE)
     * @param requestCode - Request code
     * @param address - IP of original sender (typically a client)
     * @param port - Receiving port of original sender (typically a client)
     * @param requestPayloadToForward - Request payload to forward
     * @return - Byte array representing payload
     */
    public static byte[] buildForwardingRequestPayload(byte requestCode, InetAddress address, int port, byte[] requestPayloadToForward) {
        byte[] requestPayload = new byte[COMMAND_CODE_SIZE_BYTES + IP_SIZE_BYTES + PORT_SIZE_BYTES + ACTUAL_PAYLOAD_LENGTH_SIZE_BYTES + requestPayloadToForward.length];
        // Add request code
        requestPayload[0] = requestCode;
        // Add IP
        byte[] ipAsBytes = address.getAddress();
        System.arraycopy(ipAsBytes, 0, requestPayload, IP_START_INDEX, IP_SIZE_BYTES);
        // Add request payload to forward
        System.arraycopy(requestPayloadToForward, 0, requestPayload, ACTUAL_PAYLOAD_START_INDEX, requestPayloadToForward.length);
        // Add request payload length and port
        int valueLength = requestPayloadToForward.length;
        ByteBuffer buffer = ByteBuffer.wrap(requestPayload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(ACTUAL_PAYLOAD_LENGTH_START_INDEX, valueLength);
        buffer.putInt(PORT_START_INDEX, port);
        return buffer.array();
    }


    /**
     * Builds a payload that includes a node list
     * @param command - Command
     * @param nodeList - serialized node list
     * @return - Byte array representing payload
     */
    public static byte[] buildPayloadWithNodeList(byte command, byte[] nodeList) {
        byte[] payload = new byte[COMMAND_CODE_SIZE_BYTES + NODE_LIST_LENGTH_SIZE_BYTES + nodeList.length];
        // Add request code
        payload[0] = command;
        // Add node list
        System.arraycopy(nodeList, 0, payload, NODE_LIST_START_INDEX, nodeList.length);
        // Add node list length
        int nodeListLength = nodeList.length;
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(NODE_LIST_LENGTH_START_INDEX, nodeListLength);
        return buffer.array();
    }


    /**
     * Gets the element specified from the payload provided
     * @param name - Name of the element
     * @param payload - Payload to extract element from
     * @return - Byte array representing the element
     * @throws message.InvalidMessageException
     * @throws message.BadValueLengthException
     */
    public static byte[] getPayloadElement(Element name, byte[] payload)
            throws InvalidMessageException, BadValueLengthException {
        byte[] elementToReturn = null;
        try {
            switch(name) {
                case COMMAND:
                    elementToReturn = Arrays.copyOfRange(payload, 0, 1);
                    break;
                case KEY:
                    elementToReturn = Arrays.copyOfRange(payload, KEY_START_INDEX, KEY_START_INDEX + KEY_SIZE_BYTES);
                    break;
                case REQUEST_VALUE_LENGTH:
                    elementToReturn = Arrays.copyOfRange(payload, REQUEST_VALUE_LENGTH_START_INDEX, REQUEST_VALUE_LENGTH_START_INDEX + VALUE_LENGTH_SIZE_BYTES);
                    break;
                case REQUEST_VALUE:
                    short requestValueLength = ByteBuffer.wrap(Arrays.copyOfRange(payload, REQUEST_VALUE_LENGTH_START_INDEX, REQUEST_VALUE_LENGTH_START_INDEX + VALUE_LENGTH_SIZE_BYTES)).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    if(requestValueLength <= 0 || requestValueLength > MAX_VALUE_SIZE_BYTES) {
                        throw new BadValueLengthException();
                    }
                    elementToReturn = Arrays.copyOfRange(payload, REQUEST_VALUE_START_INDEX, REQUEST_VALUE_START_INDEX + requestValueLength);
                    break;
                case RESPONSE_VALUE_LENGTH:
                    elementToReturn = Arrays.copyOfRange(payload, RESPONSE_VALUE_LENGTH_START_INDEX, RESPONSE_VALUE_LENGTH_START_INDEX + VALUE_LENGTH_SIZE_BYTES);
                    break;
                case RESPONSE_VALUE:
                    short responseValueLength = ByteBuffer.wrap(Arrays.copyOfRange(payload, RESPONSE_VALUE_LENGTH_START_INDEX, RESPONSE_VALUE_LENGTH_START_INDEX + VALUE_LENGTH_SIZE_BYTES)).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    if(responseValueLength <= 0 || responseValueLength > MAX_VALUE_SIZE_BYTES) {
                        throw new BadValueLengthException();
                    }
                    elementToReturn = Arrays.copyOfRange(payload, RESPONSE_VALUE_START_INDEX, RESPONSE_VALUE_START_INDEX + responseValueLength);
                    break;
                case IP_ADDRESS:
                    elementToReturn = Arrays.copyOfRange(payload, IP_START_INDEX, IP_START_INDEX + IP_SIZE_BYTES);
                    break;
                case PORT:
                    elementToReturn = Arrays.copyOfRange(payload, PORT_START_INDEX, PORT_START_INDEX + PORT_SIZE_BYTES);
                    break;
                case ACTUAL_PAYLOAD_LENGTH:
                    elementToReturn = Arrays.copyOfRange(payload, ACTUAL_PAYLOAD_LENGTH_START_INDEX, ACTUAL_PAYLOAD_LENGTH_START_INDEX + ACTUAL_PAYLOAD_LENGTH_SIZE_BYTES);
                    break;
                case ACTUAL_PAYLOAD:
                    int actualPayloadLength = ByteBuffer.wrap(Arrays.copyOfRange(payload, ACTUAL_PAYLOAD_LENGTH_START_INDEX, ACTUAL_PAYLOAD_LENGTH_START_INDEX + ACTUAL_PAYLOAD_LENGTH_SIZE_BYTES)).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    elementToReturn = Arrays.copyOfRange(payload, ACTUAL_PAYLOAD_START_INDEX, ACTUAL_PAYLOAD_START_INDEX + actualPayloadLength);
                    break;
                case NODE_LIST_LENGTH:
                    elementToReturn = Arrays.copyOfRange(payload, NODE_LIST_LENGTH_START_INDEX, NODE_LIST_LENGTH_START_INDEX + NODE_LIST_LENGTH_SIZE_BYTES);
                    break;
                case NODE_LIST:
                    int nodeListLength = ByteBuffer.wrap(Arrays.copyOfRange(payload, NODE_LIST_LENGTH_START_INDEX, NODE_LIST_LENGTH_START_INDEX + NODE_LIST_LENGTH_SIZE_BYTES)).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    elementToReturn = Arrays.copyOfRange(payload, NODE_LIST_START_INDEX, NODE_LIST_START_INDEX + nodeListLength);
                    break;
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            throw new InvalidMessageException("Invalid payload");
        }

        return elementToReturn;
    }





}
