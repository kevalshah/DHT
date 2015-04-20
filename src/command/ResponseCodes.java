package command;

/**
 * Response Code Constants
 */
public class ResponseCodes {

    // Required Response Codes
    public final static byte OPERATION_SUCCESS = 0x00;
    public final static byte NON_EXISTENT_KEY = 0x01;
    public final static byte OUT_OF_SPACE = 0x02;
    public final static byte SYSTEM_OVERLOAD = 0x03;
    public final static byte INTERNAL_KVSTORE_FAILURE = 0x04;
    public final static byte UNRECOGNIZED_COMMAND = 0x05;

    // Custom Response Codes (Must have value > 0x20)
    public final static byte JOIN_REP = 0x31;
    public final static byte PRED_ALIVE_REP = 0x32;
    public final static byte SUCC_ALIVE_REP = 0x33;
    public final static byte CANNOT_SHUTDOWN = 0x34;
    public final static byte BAD_VALUE_LENGTH = 0x35;
    public final static byte CLIENT_FWD_RESPONSE = 0x36;

    // This will be used for testing
    public final static byte NODE_LIST_RESPONSE = 0x37;

    // Cannot be initialized
    private ResponseCodes() {}


}
