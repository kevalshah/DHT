package algorithm;

import org.junit.Test;
import static org.junit.Assert.*;

public class KeyForwardingOnMultipleNodeTest {

    /**
     * Tests whether a key eventually gets routed to correct node
     * @throws Exception
     */
    @Test
    public void testMultipleNodeKeyRoute() throws Exception {

        DummyNode node = new DummyNode(2, 5, new int[]{7, 9, 10, 12});
        DummyNode node2 = new DummyNode(5, 7, new int[]{9, 10, 12, 20});
        DummyNode node3 = new DummyNode(7, 9, new int[]{10, 12, 20, 50});
        DummyNode node4 = new DummyNode(9, 10, new int[]{12, 20, 50, 2});
        DummyNode node5 = new DummyNode(10, 12, new int[]{20, 50, 2, 5});
        DummyNode node6 = new DummyNode(12, 20, new int[]{50, 2, 5, 7});
        DummyNode node7 = new DummyNode(20, 50, new int[]{2, 5, 7, 9});
        DummyNode node8 = new DummyNode(50, 2, new int[]{5, 7, 9, 10});

        int keyID = 0;

        int actualID = KeyForwarder.routeKeyToNode(keyID, node.selfID, node.predecessorID, node.successorList);
        assertEquals(7, actualID);

        actualID = KeyForwarder.routeKeyToNode(keyID, node2.selfID, node2.predecessorID, node2.successorList);
        assertEquals(9, actualID);

        actualID = KeyForwarder.routeKeyToNode(keyID, node3.selfID, node3.predecessorID, node3.successorList);
        assertEquals(10, actualID);

        actualID = KeyForwarder.routeKeyToNode(keyID, node4.selfID, node4.predecessorID, node4.successorList);
        assertEquals(2, actualID);

        actualID = KeyForwarder.routeKeyToNode(keyID, node8.selfID, node8.predecessorID, node8.successorList);
        assertEquals(2, actualID);
    }
}
