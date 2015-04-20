package message;

import command.RequestCodes;
import command.ResponseCodes;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class PayloadTest {

    @Test
    public void testBuildPayloadWithOnlyCommand() throws Exception {
        byte command = 0x23;
        byte[] payload = Payload.buildPayloadWithOnlyCommand(command);
        assertEquals(1, payload.length);
        assertEquals(command, payload[0]);
    }

    @Test
    public void testBuildStandardRequestPayloadWithKey() throws Exception {
        byte command = RequestCodes.GET;
        // Generate random key
        Random random = new Random();
        byte[] key = new byte[32];
        random.nextBytes(key);
        // Build payload
        byte[] payload = Payload.buildStandardRequestPayload(command, key);
        // Check if size is as expected
        assertEquals(33, payload.length);
        // Check if contents are as expected
        byte actualCommand = payload[0];
        byte[] actualKey = Arrays.copyOfRange(payload, Payload.KEY_START_INDEX, Payload.KEY_START_INDEX + Payload.KEY_SIZE_BYTES);
        assertEquals(command, actualCommand);
        assertArrayEquals(key, actualKey);
    }

    @Test
    public void testBuildStandardRequestPayloadWithKeyAndValueLessThanMaxLength() throws Exception {
        byte command = RequestCodes.PUT;

        // Generate random key
        Random random = new Random();
        byte[] key = new byte[32];
        random.nextBytes(key);

        // Generate random value less than max length
        byte[] valueLessThanMaxLength = new byte[14999];

        // Build payload
        byte[] payloadWithValueLessThanMaxLength = Payload.buildStandardRequestPayload(command, key,
                valueLessThanMaxLength);

        // Check if payload size is as expected
        assertEquals(Payload.COMMAND_CODE_SIZE_BYTES + Payload.KEY_SIZE_BYTES + Payload.VALUE_LENGTH_SIZE_BYTES + valueLessThanMaxLength.length,
                payloadWithValueLessThanMaxLength.length);

        // Check if contents are as expected
        byte actualCommandPVLM = payloadWithValueLessThanMaxLength[0];
        byte[] actualKeyPVLM = Arrays.copyOfRange(payloadWithValueLessThanMaxLength, Payload.KEY_START_INDEX, Payload.KEY_START_INDEX + Payload.KEY_SIZE_BYTES);
        short actualValueLengthPVLM = ByteBuffer.wrap(
                Arrays.copyOfRange(payloadWithValueLessThanMaxLength, Payload.REQUEST_VALUE_LENGTH_START_INDEX, Payload.REQUEST_VALUE_LENGTH_START_INDEX + Payload.VALUE_LENGTH_SIZE_BYTES)).order(
                ByteOrder.LITTLE_ENDIAN).getShort();
        byte[] actualValuePVLM = Arrays.copyOfRange(payloadWithValueLessThanMaxLength, Payload.REQUEST_VALUE_START_INDEX, payloadWithValueLessThanMaxLength.length);
        assertEquals(command, actualCommandPVLM);
        assertArrayEquals(key, actualKeyPVLM);
        assertEquals(valueLessThanMaxLength.length, actualValueLengthPVLM);
        assertArrayEquals(valueLessThanMaxLength, actualValuePVLM);
    }

    @Test
    public void testBuildStandardRequestPayloadWithKeyAndValueEqualToMaxLength() throws Exception {
        byte command = RequestCodes.PUT;

        // Generate random key
        Random random = new Random();
        byte[] key = new byte[32];
        random.nextBytes(key);

        // Generate random value equal to max length
        byte[] valueEqualToMaxLength = new byte[15000];
        random.nextBytes(valueEqualToMaxLength);

        // Build payload
        byte[] payloadWithValueEqualToMaxLength = Payload.buildStandardRequestPayload(command, key,
                valueEqualToMaxLength);

        // Check if payload size is as expected
        assertEquals(Payload.COMMAND_CODE_SIZE_BYTES + Payload.KEY_SIZE_BYTES + Payload.VALUE_LENGTH_SIZE_BYTES + valueEqualToMaxLength.length,
                payloadWithValueEqualToMaxLength.length);

        // Check if contents are as expected
        byte actualCommandPVEM = payloadWithValueEqualToMaxLength[0];
        byte[] actualKeyPVEM = Arrays.copyOfRange(payloadWithValueEqualToMaxLength, Payload.KEY_START_INDEX, Payload.KEY_START_INDEX + Payload.KEY_SIZE_BYTES);
        short actualValueLengthPVEM = ByteBuffer.wrap(
                Arrays.copyOfRange(payloadWithValueEqualToMaxLength, Payload.REQUEST_VALUE_LENGTH_START_INDEX, Payload.REQUEST_VALUE_LENGTH_START_INDEX + Payload.VALUE_LENGTH_SIZE_BYTES)).order(
                ByteOrder.LITTLE_ENDIAN).getShort();
        byte[] actualValuePVEM = Arrays.copyOfRange(payloadWithValueEqualToMaxLength, Payload.REQUEST_VALUE_START_INDEX,
                payloadWithValueEqualToMaxLength.length);
        assertEquals(command, actualCommandPVEM);
        assertArrayEquals(key, actualKeyPVEM);
        assertEquals(valueEqualToMaxLength.length, actualValueLengthPVEM);
        assertArrayEquals(valueEqualToMaxLength, actualValuePVEM);
    }

    @Test
    public void testBuildStandardRequestPayloadWithKeyAndValueGreaterThanMaxLength() throws Exception {
        byte command = RequestCodes.PUT;

        // Generate random key
        Random random = new Random();
        byte[] key = new byte[32];
        random.nextBytes(key);

        // Generate random value greater than max length
        byte[] valueGreaterThanMaxLength = new byte[15001];
        random.nextBytes(valueGreaterThanMaxLength);

        // Build payload
        byte[] payloadWithValueGreaterThanMaxLength = Payload.buildStandardRequestPayload(command, key,
                valueGreaterThanMaxLength);

        // Check if payload size is as expected
        assertEquals(Payload.COMMAND_CODE_SIZE_BYTES + Payload.KEY_SIZE_BYTES + Payload.VALUE_LENGTH_SIZE_BYTES + Payload.MAX_VALUE_SIZE_BYTES,
                payloadWithValueGreaterThanMaxLength.length);

        // Check if contents are as expected
        byte actualCommandPVGM = payloadWithValueGreaterThanMaxLength[0];
        byte[] actualKeyPVGM = Arrays.copyOfRange(payloadWithValueGreaterThanMaxLength, Payload.KEY_START_INDEX, Payload.KEY_START_INDEX + Payload.KEY_SIZE_BYTES);
        short actualValueLengthPVGM = ByteBuffer.wrap(
                Arrays.copyOfRange(payloadWithValueGreaterThanMaxLength, Payload.REQUEST_VALUE_LENGTH_START_INDEX, Payload.REQUEST_VALUE_LENGTH_START_INDEX + Payload.VALUE_LENGTH_SIZE_BYTES)).order(
                ByteOrder.LITTLE_ENDIAN).getShort();
        byte[] actualValuePVGM = Arrays.copyOfRange(payloadWithValueGreaterThanMaxLength, Payload.REQUEST_VALUE_START_INDEX,
                payloadWithValueGreaterThanMaxLength.length);
        assertEquals(command, actualCommandPVGM);
        assertArrayEquals(key, actualKeyPVGM);
        assertEquals(Payload.MAX_VALUE_SIZE_BYTES, actualValueLengthPVGM);
        assertArrayEquals(Arrays.copyOfRange(valueGreaterThanMaxLength, 0, valueGreaterThanMaxLength.length - 1), actualValuePVGM);
    }


    @Test
    public void testBuildStandardResponsePayload() throws Exception {
        byte command = ResponseCodes.OPERATION_SUCCESS;

        // Generate random value
        Random random = new Random();
        byte[] value = new byte[150];
        random.nextBytes(value);

        // Build payload
        byte[] payload = Payload.buildStandardResponsePayload(command, value);

        // Check if size is as expected
        assertEquals(Payload.COMMAND_CODE_SIZE_BYTES + Payload.VALUE_LENGTH_SIZE_BYTES + value.length, payload.length);

        // Check if contents are as expected
        byte extractedCommand = payload[0];
        assertEquals(command, extractedCommand);

        byte[] extractedValueLengthBytes = Arrays.copyOfRange(payload, Payload.RESPONSE_VALUE_LENGTH_START_INDEX, Payload.RESPONSE_VALUE_LENGTH_START_INDEX + Payload.VALUE_LENGTH_SIZE_BYTES);
        short extractedValueLength = ByteBuffer.wrap(extractedValueLengthBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
        assertEquals(value.length, extractedValueLength);

        byte[] extractedValue = Arrays.copyOfRange(payload, Payload.RESPONSE_VALUE_START_INDEX, payload.length);
        assertArrayEquals(value, extractedValue);
    }


    @Test
    public void testBuildForwardingRequestPayload() throws UnknownHostException {
        byte requestCode = RequestCodes.FWD_PUT;

        Random random = new Random();
        byte[] key = new byte[32];
        random.nextBytes(key);

        byte[] value = {0, 1, 2, 2, 0, 2};
        byte[] originalRequestPayload = Payload.buildStandardRequestPayload(requestCode, key, value);

        InetAddress ip = InetAddress.getLocalHost();
        int port = 55555;

        byte[] forwardingRequestPayload = forwardingRequestPayload = Payload.buildForwardingRequestPayload(requestCode, ip, port, originalRequestPayload);

        // Check if size is as expected
        assertEquals(Payload.COMMAND_CODE_SIZE_BYTES + Payload.IP_SIZE_BYTES + Payload.PORT_SIZE_BYTES + Payload.ACTUAL_PAYLOAD_LENGTH_SIZE_BYTES + originalRequestPayload.length, forwardingRequestPayload.length);

        // Check if contents are as expected
        byte[] extractedIPAsBytes = Arrays.copyOfRange(forwardingRequestPayload, Payload.IP_START_INDEX, Payload.IP_START_INDEX + Payload.IP_SIZE_BYTES);
        assertEquals(ip, InetAddress.getByAddress(extractedIPAsBytes));

        byte[] extractedPortAsBytes = Arrays.copyOfRange(forwardingRequestPayload, Payload.PORT_START_INDEX, Payload.PORT_START_INDEX + Payload.PORT_SIZE_BYTES);
        assertEquals(port, ByteBuffer.wrap(extractedPortAsBytes).order(ByteOrder.LITTLE_ENDIAN).getInt());

        byte[] extractedOriginalPayloadLength = Arrays.copyOfRange(forwardingRequestPayload, Payload.ACTUAL_PAYLOAD_LENGTH_START_INDEX, Payload.ACTUAL_PAYLOAD_LENGTH_START_INDEX + Payload.ACTUAL_PAYLOAD_LENGTH_SIZE_BYTES);
        assertEquals(originalRequestPayload.length, ByteBuffer.wrap(extractedOriginalPayloadLength).order(ByteOrder.LITTLE_ENDIAN).getInt());

        byte[] extractedOriginalPayload = Arrays.copyOfRange(forwardingRequestPayload, Payload.ACTUAL_PAYLOAD_START_INDEX, forwardingRequestPayload.length);
        assertArrayEquals(originalRequestPayload, extractedOriginalPayload);
    }


    @Test
    public void testBuildPayloadWithNodeList() throws Exception {
        byte[] dummyNodeList = new byte[15000];
        Random random = new Random();
        random.nextBytes(dummyNodeList);

        byte command = RequestCodes.SUCC_ALIVE_REQ;

        // Build payload
        byte[] payload = Payload.buildPayloadWithNodeList(command, dummyNodeList);

        // Check if size is as expected
        assertEquals(Payload.COMMAND_CODE_SIZE_BYTES + Payload.NODE_LIST_LENGTH_SIZE_BYTES + dummyNodeList.length, payload.length);

        // Check if contents are as expected
        byte extractedCommand = payload[0];
        assertEquals(command, extractedCommand);

        byte[] extractedNodeListLengthAsBytes = Arrays.copyOfRange(payload, Payload.NODE_LIST_LENGTH_START_INDEX, Payload.NODE_LIST_LENGTH_START_INDEX + Payload.NODE_LIST_LENGTH_SIZE_BYTES);
        assertEquals(dummyNodeList.length, ByteBuffer.wrap(extractedNodeListLengthAsBytes).order(ByteOrder.LITTLE_ENDIAN).getInt());

        byte[] extractedNodeList = Arrays.copyOfRange(payload, Payload.NODE_LIST_START_INDEX, payload.length);
        assertArrayEquals(dummyNodeList, extractedNodeList);
    }


    @Test
    public void testGetPayloadElementCommand() throws Exception {
        byte command = RequestCodes.GET;
        byte[] payload = Payload.buildPayloadWithOnlyCommand(command);
        byte[] extractedCommand = Payload.getPayloadElement(Payload.Element.COMMAND, payload);
        assertEquals(1, extractedCommand.length);
        assertEquals(command, extractedCommand[0]);
    }


    @Test
    public void testGetPayloadElementKey() throws Exception {
        Random random = new Random();
        byte[] key = new byte[32];
        random.nextBytes(key);

        byte[] payload = Payload.buildStandardRequestPayload(RequestCodes.REMOVE, key);
        byte[] extractedKey = Payload.getPayloadElement(Payload.Element.KEY, payload);
        assertEquals(Payload.KEY_SIZE_BYTES, extractedKey.length);
        assertArrayEquals(key, extractedKey);
    }


    @Test
    public void testGetPayloadElementRequestValueLength() throws Exception {
        byte[] key = new byte[32];

        Random random = new Random();
        byte[] value = new byte[14999];
        random.nextBytes(value);

        byte[] payload = Payload.buildStandardRequestPayload(RequestCodes.PUT, key, value);
        byte[] extractedValueLengthBytes = Payload.getPayloadElement(Payload.Element.REQUEST_VALUE_LENGTH, payload);
        assertEquals(Payload.VALUE_LENGTH_SIZE_BYTES, extractedValueLengthBytes.length);

        short extractedValueLength = ByteBuffer.wrap(extractedValueLengthBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
        assertEquals(value.length, extractedValueLength);
    }


    @Test
    public void testGetPayloadElementRequestValueBadValueLengthException() throws Exception {
        ByteBuffer buffer1 = ByteBuffer.allocate(1 + 32 + 2 + 15000);
        buffer1.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer buffer2 = ByteBuffer.allocate(1 + 32 + 2 + 15000);
        buffer2.order(ByteOrder.LITTLE_ENDIAN);
        buffer1.putShort(Payload.REQUEST_VALUE_LENGTH_START_INDEX, (short)-1);
        buffer2.putShort(Payload.REQUEST_VALUE_LENGTH_START_INDEX, (short)15001);

        byte[] badValueLengthPayload1 = buffer1.array();
        byte[] badValueLengthPayload2 = buffer2.array();

        try {
            byte[] extractedValue1 = Payload.getPayloadElement(Payload.Element.REQUEST_VALUE, badValueLengthPayload1);
            fail();
        } catch(InvalidMessageException e) {
            fail();
        } catch(BadValueLengthException e) {
        }

        try {
            byte[] extractedValue2 = Payload.getPayloadElement(Payload.Element.REQUEST_VALUE, badValueLengthPayload2);
            fail();
        } catch(InvalidMessageException e) {
            fail();
        } catch(BadValueLengthException e) {
        }
    }


    @Test
    public void testGetPayloadElementRequestValue() throws Exception {
        Random random = new Random();
        byte[] value = new byte[14999];
        random.nextBytes(value);

        byte[] payload = Payload.buildStandardRequestPayload(RequestCodes.PUT, new byte[32], value);
        byte[] extractedValue = Payload.getPayloadElement(Payload.Element.REQUEST_VALUE, payload);

        assertArrayEquals(value, extractedValue);
    }


    @Test
    public void testGetPayloadElementResponseValueLength() throws Exception {
        Random random = new Random();
        byte[] value = new byte[14999];
        random.nextBytes(value);

        byte[] payload = Payload.buildStandardResponsePayload(ResponseCodes.OPERATION_SUCCESS, value);
        byte[] extractedValueLengthBytes = Payload.getPayloadElement(Payload.Element.RESPONSE_VALUE_LENGTH, payload);
        assertEquals(Payload.VALUE_LENGTH_SIZE_BYTES, extractedValueLengthBytes.length);

        short extractedValueLength = ByteBuffer.wrap(extractedValueLengthBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
        assertEquals(value.length, extractedValueLength);
    }


    @Test
    public void testGetPayloadElementResponseValueBadValueLengthException() throws Exception {
        ByteBuffer buffer1 = ByteBuffer.allocate(1 + 2 + 15000);
        buffer1.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer buffer2 = ByteBuffer.allocate(1 + 2 + 15000);
        buffer2.order(ByteOrder.LITTLE_ENDIAN);
        buffer1.putShort(Payload.RESPONSE_VALUE_LENGTH_START_INDEX, (short)-1);
        buffer2.putShort(Payload.RESPONSE_VALUE_LENGTH_START_INDEX, (short)15001);

        byte[] badValueLengthPayload1 = buffer1.array();
        byte[] badValueLengthPayload2 = buffer2.array();

        try {
            byte[] extractedValue1 = Payload.getPayloadElement(Payload.Element.RESPONSE_VALUE, badValueLengthPayload1);
            fail();
        } catch(InvalidMessageException e) {
            fail();
        } catch(BadValueLengthException e) {
        }

        try {
            byte[] extractedValue2 = Payload.getPayloadElement(Payload.Element.RESPONSE_VALUE, badValueLengthPayload2);
            fail();
        } catch(InvalidMessageException e) {
            fail();
        } catch(BadValueLengthException e) {
        }
    }


    @Test
    public void testGetPayloadElementResponseValue() throws Exception {
        Random random = new Random();
        byte[] value = new byte[14999];
        random.nextBytes(value);

        byte[] payload = Payload.buildStandardResponsePayload(ResponseCodes.OPERATION_SUCCESS, value);
        byte[] extractedValue = Payload.getPayloadElement(Payload.Element.RESPONSE_VALUE, payload);

        assertArrayEquals(value, extractedValue);
    }


    @Test
    public void testGetPayloadElementIPAddress() throws Exception {
        InetAddress ip = InetAddress.getLocalHost();
        byte[] payloadToForward = Payload.buildStandardRequestPayload(RequestCodes.REMOVE, new byte[32]);
        byte[] payload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_REMOVE, ip, 2000, payloadToForward);
        byte[] extractedIPAsBytes = Payload.getPayloadElement(Payload.Element.IP_ADDRESS, payload);
        assertEquals(ip, InetAddress.getByAddress(extractedIPAsBytes));
    }


    @Test
    public void testGetPayloadElementPort() throws Exception {
        InetAddress ip = InetAddress.getLocalHost();
        int port = 45000;
        byte[] payloadToForward = Payload.buildStandardRequestPayload(RequestCodes.REMOVE, new byte[32]);
        byte[] payload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_REMOVE, ip, port, payloadToForward);
        byte[] extractedPortAsBytes = Payload.getPayloadElement(Payload.Element.PORT, payload);
        assertEquals(port, ByteBuffer.wrap(extractedPortAsBytes).order(ByteOrder.LITTLE_ENDIAN).getInt());
    }


    @Test
    public void testGetPayloadElementActualPayloadLength() throws Exception {
        InetAddress ip = InetAddress.getLocalHost();
        int port = 45000;
        byte[] payloadToForward = Payload.buildStandardRequestPayload(RequestCodes.REMOVE, new byte[32]);
        byte[] payload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_REMOVE, ip, port, payloadToForward);
        byte[] extractedActualPayloadLengthAsBytes = Payload.getPayloadElement(Payload.Element.REGULAR_FORWARD_PAYLOAD_LENGTH, payload);
        assertEquals(payloadToForward.length, ByteBuffer.wrap(extractedActualPayloadLengthAsBytes).order(ByteOrder.LITTLE_ENDIAN).getInt());
    }


    @Test
    public void testGetPayloadElementActualPayload() throws Exception {
        InetAddress ip = InetAddress.getLocalHost();
        int port = 45000;
        byte[] payloadToForward = Payload.buildStandardRequestPayload(RequestCodes.REMOVE, new byte[32]);
        byte[] payload = Payload.buildForwardingRequestPayload(RequestCodes.FWD_REMOVE, ip, port, payloadToForward);
        byte[] extractedActualPayload = Payload.getPayloadElement(Payload.Element.REGULAR_FORWARD_PAYLOAD, payload);
        assertArrayEquals(payloadToForward, extractedActualPayload);
    }


    @Test
    public void testGetPayloadElementNodeListLength() throws Exception {
        byte[] dummyNodeList = new byte[15000];
        Random random = new Random();
        random.nextBytes(dummyNodeList);

        byte command = RequestCodes.SUCC_ALIVE_REQ;

        // Build payload
        byte[] payload = Payload.buildPayloadWithNodeList(command, dummyNodeList);

        byte[] extractedNodeListLengthAsBytes = Payload.getPayloadElement(Payload.Element.NODE_LIST_LENGTH, payload);
        assertEquals(dummyNodeList.length, ByteBuffer.wrap(extractedNodeListLengthAsBytes).order(ByteOrder.LITTLE_ENDIAN).getInt());
    }


    @Test
    public void testGetPayloadElementNodeList() throws Exception {
        byte[] dummyNodeList = new byte[15000];
        Random random = new Random();
        random.nextBytes(dummyNodeList);

        byte command = RequestCodes.SUCC_ALIVE_REQ;

        // Build payload
        byte[] payload = Payload.buildPayloadWithNodeList(command, dummyNodeList);

        byte[] extractedNodeList = Payload.getPayloadElement(Payload.Element.NODE_LIST, payload);
        assertArrayEquals(dummyNodeList, extractedNodeList);
    }

}