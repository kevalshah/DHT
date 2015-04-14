package command;

/**
 * Request Code Constants
 */
public class RequestCodes {

    // Required Request Codes
    public final static byte PUT = 0x01;
    public final static byte GET = 0x02;
    public final static byte REMOVE = 0x03;
    public final static byte SHUTDOWN = 0x04;

    // Custom Request Codes (Must have value > 0x20)
    public final static byte FWD_PUT = 0x21;
    public final static byte FWD_GET = 0x22;
    public final static byte FWD_REMOVE = 0x23;
    public final static byte POTENTIAL_IMS_PUT = 0x24;
    public final static byte POTENTIAL_IMS_GET = 0x25;
    public final static byte POTENTIAL_IMS_REMOVE = 0x26;
    public final static byte REPLICA_PUT = 0x27;
    public final static byte REPLICA_GET = 0x28;
    public final static byte REPLICA_REMOVE = 0x29;
    public final static byte JOIN_REQ = 0x2A;
    public final static byte FWD_JOIN = 0x2B;
    public final static byte POTENTIAL_IMS_JOIN = 0x2C;
    public final static byte PRED_ALIVE_REQ = 0x2D;
    public final static byte SUCC_ALIVE_REQ = 0x2E;
    public final static byte POTENTIAL_IMPS_UPDATE = 0x2F;

    // This will be used for testing
    public final static byte NODE_LIST_REQUEST = 0x30;

    // Cannot be initialized
    private RequestCodes() {}
}
