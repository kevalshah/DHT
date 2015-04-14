package message;

import command.RequestCodes;
import nodelist.Node;
import org.junit.Test;
import utility.NodeSerializerUtility;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.*;

public class MessageTest {

    @Test
    public void testBuildMessageSize() {
        try {
            byte[] header = new byte[21];

            byte[] payload = new byte[123];

            assertEquals(21 + 123, Message.buildMessage(header, payload).length);

        } catch(Exception e) {
            fail();
        }
    }


    @Test
    public void testBuildMessage() {
        try {
            byte[] header = new byte[21];

            byte[] payload = new byte[123];

            Message.buildMessage(header, payload);

        } catch(Exception e) {
            fail();
        }
    }


    @Test
    public void testBuildMessageInvalidMessageException() {
        byte[] header = new byte[21];
        try {
            Message.buildMessage(header, null);
            fail();
        } catch(InvalidMessageException e) {

        }

        try {
            Message.buildMessage(null, header);
            fail();
        } catch(InvalidMessageException e) {

        }

        try {
            Message.buildMessage(null, null);
            fail();
        } catch(InvalidMessageException e) {

        }
    }


    @Test
    public void testGetBytesInRange() throws InvalidMessageException {

        // Generate random header
        byte[] header = new byte[32];
        Random random = new Random();
        random.nextBytes(header);

        // Generate random payload
        byte[] payload = new byte[15234];
        random.nextBytes(payload);

        byte[] message = Message.buildMessage(header, payload);

        assertArrayEquals(header, Message.getBytesInRange(message, 0, 32));
        assertArrayEquals(payload, Message.getBytesInRange(message, 32, message.length - header.length));

    }


    @Test
    public void testExtractHeader() throws Exception {

    }

    @Test
    public void testExtractPayload() throws Exception {
        ArrayList<Node> nodeList = new ArrayList<Node>();
        nodeList.add(new Node(InetAddress.getLocalHost(), 2000, 1));
        nodeList.add(new Node(InetAddress.getLocalHost(), 2000, 2));
        nodeList.add(new Node(InetAddress.getLocalHost(), 2000, 3));
        nodeList.add(new Node(InetAddress.getLocalHost(), 2000, 4));
        nodeList.add(new Node(InetAddress.getLocalHost(), 2000, 5));
        byte[] nodeListAsBytes = NodeSerializerUtility.serializeNodeList(nodeList);
        byte[] payload = Payload.buildPayloadWithNodeList(RequestCodes.SUCC_ALIVE_REQ, nodeListAsBytes);

        byte[] message = Message.buildMessage(Header.buildMessageHeader(), payload);

        byte[] extractedPayload = Message.extractPayload(message);

        assertArrayEquals(payload, extractedPayload);



    }
}