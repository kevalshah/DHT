package utility;

import command.ResponseCodes;
import message.*;
import nodelist.Node;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class for logging stuff
 */
public class Logger {

    public static void printResponseMessage(byte[] responseMessage, boolean isResponseToGET) {

        byte[] header = Message.extractHeader(responseMessage);
        byte[] payload = Message.extractPayload(responseMessage);

        byte responseCode = payload[0];

        System.out.println("Header: " + Header.convertToString(header));

        if(isResponseToGET && responseCode == ResponseCodes.OPERATION_SUCCESS) {
            System.out.print("Response: OPERATION SUCCESS | ");
            short valueLength = ByteBuffer.wrap(Arrays.copyOfRange(payload, Payload.RESPONSE_VALUE_LENGTH_START_INDEX,
                    Payload.RESPONSE_VALUE_LENGTH_START_INDEX + Payload.VALUE_LENGTH_SIZE_BYTES)).order(
                    ByteOrder.LITTLE_ENDIAN).getShort();
            System.out.print("Value Length: " + valueLength + " | ");
            String value = UTF8StringUtility.bytesUTF8ToString(
                    Arrays.copyOfRange(payload, Payload.RESPONSE_VALUE_START_INDEX,
                            Payload.RESPONSE_VALUE_START_INDEX + valueLength));
            System.out.print("Value: " + value);
        } else {
            switch(responseCode) {
                case ResponseCodes.OPERATION_SUCCESS:
                    System.out.println("Response: OPERATION SUCCESS");
                    System.out.println();
                    break;
                case ResponseCodes.UNRECOGNIZED_COMMAND:
                    System.out.println("Response: UNRECOGNIZED COMMAND");
                    System.out.println();
                    break;
                case ResponseCodes.OUT_OF_SPACE:
                    System.out.println("Response: OUT OF SPACE");
                    System.out.println();
                    break;
                case ResponseCodes.SYSTEM_OVERLOAD:
                    System.out.println("Response: SYSTEM OVERLOAD");
                    System.out.println();
                    break;
                case ResponseCodes.INTERNAL_KVSTORE_FAILURE:
                    System.out.println("Response: INTERNAL KV STORE FAILURE");
                    System.out.println();
                    break;
                case ResponseCodes.CANNOT_SHUTDOWN:
                    System.out.println("Response: CANNOT SHUTDOWN");
                    System.out.println();
                    break;
                case ResponseCodes.NON_EXISTENT_KEY:
                    System.out.println("Response: NON EXISTENT KEY");
                    System.out.println();
                    break;
                case ResponseCodes.BAD_VALUE_LENGTH:
                    System.out.println("Response: BAD VALUE LENGTH");
                    System.out.println();
                    break;
                case ResponseCodes.NODE_LIST_RESPONSE:
                    System.out.println("Response: NODE LIST RESPONSE");
                    System.out.println();
                    try {
                        byte[] nodeList = Payload.getPayloadElement(Payload.Element.NODE_LIST, payload);
                        ArrayList<Node> nodes = NodeSerializerUtility.deserialize(nodeList);
                        if(nodes.size() == 0) {
                            System.out.println("Empty list");
                        }

                        int num = 1;
                        for(int i = 0; i < nodes.size(); i++) {
                            if(i == 0) {
                                System.out.println("PREDECESSOR: " + nodes.get(i));
                            } else if(i == 1) {
                                System.out.println("SELF: " + nodes.get(i));
                            } else {
                                System.out.println("[" + (num) + "] " + nodes.get(i));
                                num++;
                            }

                        }

                    } catch(InvalidMessageException e) {
                        e.printStackTrace();
                    } catch(BadValueLengthException e) {
                        e.printStackTrace();
                    }
            }
        }
    }


}
