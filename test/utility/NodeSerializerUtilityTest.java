package utility;

import nodelist.Node;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NodeSerializerUtilityTest {

    @Test
    public void testSerializeAndDeserialize() throws UnknownHostException {

        ArrayList<Node> originalNodeList = new ArrayList<Node>();
        originalNodeList.add(new Node(InetAddress.getLocalHost(), 1, 1));
        originalNodeList.add(new Node(InetAddress.getLocalHost(), 2, 2));
        originalNodeList.add(new Node(InetAddress.getLocalHost(), 3, 3));

        byte[] serializedNodeList = NodeSerializerUtility.serializeNodeList(originalNodeList);

        ArrayList<Node> deserializedNodeList = NodeSerializerUtility.deserialize(serializedNodeList);

        assertEquals(originalNodeList.size(), deserializedNodeList.size());
        assertEquals(originalNodeList.get(0), deserializedNodeList.get(0));
        assertEquals(originalNodeList.get(1), deserializedNodeList.get(1));
        assertEquals(originalNodeList.get(2), deserializedNodeList.get(2));
    }


    @Test
    public void testSerialize150NodeSize() throws Exception {
        ArrayList<Node> originalNodeList = new ArrayList<Node>();
        for(int i = 0; i < 150; i++) {
            originalNodeList.add(new Node(InetAddress.getLocalHost(), 500, 1));
        }

        byte[] serializedNodeList = NodeSerializerUtility.serializeNodeList(originalNodeList);
        System.out.println(serializedNodeList.length);
        assertTrue(serializedNodeList.length <= 17000);
    }
}